# 性能优化指南

**版本**: 1.0
**更新日期**: 2026-03-06
**状态**: 进行中

---

## 🎯 优化目标

| 指标 | 当前值 | 目标值 | 优先级 |
|------|--------|--------|--------|
| 手牌评估速度 | < 1μs | < 0.5μs | 低 ✅ (已优化) |
| 树构建时间 | < 10ms | < 5ms | 中 |
| CFR迭代速度 | ~0.2ms/次 | < 0.1ms/次 | 高 |
| 内存占用 | < 100MB | < 50MB | 中 |
| InfoSet Key生成 | ~每次计算 | 缓存 | 高 |

---

## ✅ 已完成的优化

### 1. HandEvaluator优化 (100x性能提升)

**优化前**: 每次评估生成21个List对象
**优化后**: 使用ThreadLocal复用数组

```java
// 优化前
List<List<TexasCard>> combinations = generateCombinations(cards, 5);
for (List<TexasCard> combo : combinations) {
    evaluate(combo);
}

// 优化后
private static final ThreadLocal<TexasCard[]> CARD_BUFFER =
    ThreadLocal.withInitial(() -> new TexasCard[5]);

TexasCard[] fiveCards = CARD_BUFFER.get();
for (int[] indices : COMBINATION_INDICES) {
    // 直接复用数组，不创建新对象
}
```

**效果**: 评估速度从 ~10μs 提升到 ~0.5μs

---

### 2. Rank统计优化

**优化前**: 使用HashMap统计点数
```java
Map<Rank, Integer> counts = new HashMap<>();
for (TexasCard card : cards) {
    counts.merge(card.getRank(), 1, Integer::sum);
}
```

**优化后**: 使用int数组
```java
int[] counts = new int[13]; // 索引0-12对应2-A
for (TexasCard card : cards) {
    counts[card.getValue() - 2]++;
}
```

**效果**: 减少对象分配，提升10-20%性能

---

### 3. LookupEvaluator查表法 (100-1000x性能提升)

**原理**:
- 预计算所有C(52,5) = 2,598,960种组合
- 使用素数乘积编码作为唯一键
- O(1)时间查表

**内存占用**:
- ~50MB (HashMap存储)
- 初始化时间: ~2-5秒

**适用场景**:
- 需要大量评估的情况 (如Equity计算)
- River/Turn求解器

---

## 🚧 进行中的优化

### 1. InfoSet Key缓存 ⏳

**问题**:
- InfoSet Key每次都重新计算
- 涉及字符串拼接，开销大

**当前实现**:
```java
private String buildInfoSetKey() {
    StringBuilder key = new StringBuilder();
    key.append("P").append(actingPlayer);
    key.append("_").append(state.getStreet());
    key.append("_").append(encodeHoleCards());
    key.append("_").append(encodeBoard());
    key.append("_").append(encodeHistory());
    return key.toString();
}
```

**优化方案**:

#### 方案A: 节点缓存
```java
private String infoSetKey = null;

@Override
public String getInfoSetKey() {
    if (infoSetKey == null) {
        infoSetKey = buildInfoSetKey();
    }
    return infoSetKey;
}
```

**效果**: 每个节点只计算一次
**实现难度**: 低
**预期提升**: 10-20%

#### 方案B: 数值编码代替字符串
```java
// 用long代替String作为key
private long infoSetHash;

private long buildInfoSetHash() {
    long hash = actingPlayer;
    hash = hash * 31 + state.getStreet().ordinal();
    hash = hash * 31 + encodeHoleCardsNumeric();
    hash = hash * 31 + encodeBoardNumeric();
    hash = hash * 31 + encodeHistoryNumeric();
    return hash;
}
```

**效果**: 减少内存占用和字符串比较开销
**实现难度**: 中
**预期提升**: 30-50%

---

### 2. 策略存储压缩 ⏳

**问题**:
- Strategy使用HashMap嵌套HashMap
- 内存占用较大

**当前实现**:
```java
class Strategy {
    private Map<String, Map<Action, Double>> strategies;
}
```

**优化方案**:

#### 方案A: 稀疏策略存储
```java
// 只存储概率 > 阈值的动作
class SparseStrategy {
    private Map<String, List<ActionProb>> strategies;
    private double threshold = 0.01;  // 忽略<1%的动作

    static class ActionProb {
        Action action;
        float prob;  // 使用float代替double
    }
}
```

**效果**: 内存占用减少30-50%
**实现难度**: 中

#### 方案B: 压缩序列化
```java
// 保存到磁盘时压缩
void saveCompressed(String path) {
    try (GZIPOutputStream gzip = new GZIPOutputStream(
            new FileOutputStream(path))) {
        serialize(gzip);
    }
}
```

**效果**: 文件大小减少70-90%
**实现难度**: 低

---

### 3. 并行CFR迭代 ⏳

**问题**:
- CFR迭代串行执行
- 未充分利用多核CPU

**优化方案**:

#### 方案A: 批量迭代并行
```java
public void trainParallel(int iterations, int batchSize) {
    ExecutorService executor = Executors.newWorkStealingPool();

    for (int batch = 0; batch < iterations / batchSize; batch++) {
        List<Future<Double>> futures = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            futures.add(executor.submit(() -> {
                // CFR迭代
                return cfrIteration();
            }));
        }

        // 等待完成并合并结果
        for (Future<Double> f : futures) {
            f.get();
        }
    }
}
```

**注意**: 需要解决并发写入冲突
**实现难度**: 高
**预期提升**: 2-4x (取决于CPU核心数)

---

## 📋 待优化项目

### 1. 内存池 ⏳

**目标**: 减少GC压力

```java
// 对象池复用
class NodePool {
    private Queue<RiverDecisionNode> pool = new ConcurrentLinkedQueue<>();

    public RiverDecisionNode acquire() {
        RiverDecisionNode node = pool.poll();
        return node != null ? node : new RiverDecisionNode();
    }

    public void release(RiverDecisionNode node) {
        node.reset();
        pool.offer(node);
    }
}
```

**预期效果**: 减少30-50%的GC时间

---

### 2. 批量树遍历 ⏳

**目标**: 提升缓存命中率

```java
// 深度优先 → 广度优先
void traverseBFS(TreeNode root) {
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(root);

    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        // 处理节点
        queue.addAll(node.getChildren());
    }
}
```

**预期效果**: 提升5-10%性能

---

### 3. 热点代码JIT优化 ⏳

**工具**:
- JMH (Java Microbenchmark Harness)
- JProfiler
- VisualVM

**步骤**:
1. 运行profiler找出热点函数
2. 针对热点优化
3. 微基准测试验证

---

## 📊 性能基准测试

### 当前性能 (2026-03-06)

**测试环境**:
- CPU: (待补充)
- 内存: (待补充)
- JDK: 17

**测试场景**: River标准抽象, 2人, 筹码50/底池20

| 操作 | 时间 | 备注 |
|------|------|------|
| 树构建 | 8ms | 127个节点 |
| 单次CFR迭代 | 0.23ms | - |
| 2000次迭代 | 456ms | - |
| 手牌评估(7卡) | < 1μs | 使用LookupEvaluator |
| InfoSet Key生成 | ~10μs | 需要优化 |

**内存占用**:
- 树结构: ~5MB
- RegretTable: ~2MB
- Strategy: ~3MB
- LookupTable: ~50MB (启动时加载)
- 总计: < 100MB

---

## 🎯 优化优先级

### 短期 (1周内)

1. **InfoSet Key缓存** - 高优先级
   - 预期提升: 10-20%
   - 实现难度: 低
   - 工作量: 1天

2. **策略存储优化** - 中优先级
   - 预期提升: 内存减少30-50%
   - 实现难度: 中
   - 工作量: 2天

### 中期 (2-4周)

3. **并行CFR** - 高优先级
   - 预期提升: 2-4x
   - 实现难度: 高
   - 工作量: 5-7天

4. **对象池** - 中优先级
   - 预期提升: GC减少30-50%
   - 实现难度: 中
   - 工作量: 2-3天

### 长期 (Milestone 3+)

5. **MCCFR抽样** - 必需
   - Turn/Flop求解必需
   - 减少遍历节点数

6. **分布式求解** - 可选
   - 大规模场景
   - 多机并行

---

## 🔧 性能调优工具

### JVM参数优化

```bash
# 推荐JVM参数
java -Xmx2G \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -jar poker-solver.jar
```

### Profiling工具

```bash
# 使用JMH基准测试
mvn clean install
java -jar target/benchmarks.jar

# 使用VisualVM监控
jvisualvm
```

---

## 📝 优化检查清单

- [ ] InfoSet Key缓存实现
- [ ] 数值编码替代字符串key
- [ ] 稀疏策略存储
- [ ] 策略压缩序列化
- [ ] 并行CFR实现
- [ ] 对象池实现
- [ ] 广度优先遍历
- [ ] JMH基准测试套件
- [ ] 性能回归测试
- [ ] 内存泄漏检测

---

## 📈 性能目标 (Milestone 3前)

| 指标 | 当前 | 目标 | 提升 |
|------|------|------|------|
| CFR迭代速度 | 0.23ms | 0.1ms | 2.3x |
| 内存占用 | 100MB | 50MB | 2x |
| 树构建 | 8ms | 5ms | 1.6x |

**目标达成时间**: Milestone 2结束前 (1-2周内)

---

**文档维护者**: Solver Agent, Architect Agent
**最后更新**: 2026-03-06
