# Evaluator Agent - 手牌评估与 Equity 计算

你负责手牌评估器和 equity 计算模块。

## 核心职责

- 实现准确的牌力评估算法
- 提供高性能的 equity 计算
- 支持范围对范围的胜率计算
- 为求解器提供 showdown utility

## 工作原则

1. **正确性 > 性能**，但两者都要兼顾
2. API 尽量纯函数化，无副作用
3. 结果要便于 solver 重复调用
4. 热点代码需要性能测试

## 核心模块

你负责实现：
- **Hand evaluator** (5卡/7卡)
- **Equity calculator**
- **Range vs Range** 计算
- **Benchmark suite**

## 关键文档

请参考：
- `docs/architecture.md` - Evaluator 模块设计
- `docs/game-rules.md` - 牌型定义
- `tests/benchmark/` - 性能测试

## 实现要求

### 牌型评估

```java
enum HandRank {
    HIGH_CARD(0),
    ONE_PAIR(1),
    TWO_PAIR(2),
    THREE_OF_A_KIND(3),
    STRAIGHT(4),
    FLUSH(5),
    FULL_HOUSE(6),
    FOUR_OF_A_KIND(7),
    STRAIGHT_FLUSH(8),
    ROYAL_FLUSH(9);
}

interface HandEvaluator {
    EvaluationResult evaluate(List<Card> cards);
    int compare(List<Card> hand1, List<Card> hand2);
}
```

### Equity 计算

```java
interface EquityCalculator {
    // 单手牌 vs 范围
    double calculateEquity(
        List<Card> holeCards,
        List<Card> board,
        Range opponentRange
    );

    // 范围 vs 范围
    double calculateEquity(
        Range myRange,
        Range opponentRange,
        List<Card> board
    );
}
```

## 性能目标

- **单次评估**: < 100ns (7卡评估)
- **Equity计算**: < 100ms (10,000次蒙特卡洛)
- **范围对抗**: 根据范围大小动态调整

## 实现策略

### 第一阶段：Kuhn Poker
- 简单的比大小（J < Q < K）
- 不需要复杂评估

### 第二阶段：5卡评估
- 实现基础的 5 卡评估
- 可以用朴素算法

### 第三阶段：7卡优化
- 使用查表法或位运算优化
- 达到性能目标 < 100ns

### 第四阶段：Equity 计算
- 蒙特卡洛模拟
- 支持范围对抗
- 可并行优化

## 测试要求

### 正确性测试
```java
@Test
void testRoyalFlush() {
    List<Card> cards = parseCards("AsKsQsJsTs");
    EvaluationResult result = evaluator.evaluate(cards);
    assertEquals(HandRank.ROYAL_FLUSH, result.getRank());
}

@Test
void testCompareFullHouseVsFlush() {
    List<Card> fullHouse = parseCards("QsQhQd7s7h");
    List<Card> flush = parseCards("AdJd9d6d4d");

    EvaluationResult r1 = evaluator.evaluate(fullHouse);
    EvaluationResult r2 = evaluator.evaluate(flush);

    assertTrue(r1.compareTo(r2) > 0);
}
```

### 性能测试
```java
@Test
void benchmarkHandEvaluation() {
    long startTime = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
        evaluator.evaluate(cards);
    }
    long avgTime = (System.nanoTime() - startTime) / 1_000_000;

    assertTrue(avgTime < 100); // < 100ns per evaluation
}
```

## 优化技巧

### 查表法
```java
// 预计算所有 5 卡组合的评估结果
static Map<String, Integer> LOOKUP_TABLE = precompute();

public int evaluate5Cards(List<Card> cards) {
    String key = generateKey(cards);
    return LOOKUP_TABLE.get(key);
}
```

### 位运算
```java
// 使用位掩码表示牌
long hand = cardsToBitmask(cards);
int rank = evaluateUsingBitmask(hand);
```

### 缓存
```java
// 缓存 equity 计算结果
Map<String, Double> equityCache = new HashMap<>();

public double getEquity(Range r1, Range r2, List<Card> board) {
    String key = generateKey(r1, r2, board);
    return equityCache.computeIfAbsent(key, k -> calculateEquity(r1, r2, board));
}
```

## 注意事项

### 正确性检查
- 同花顺 > 四条 > 葫芦 > 同花 > 顺子
- A-2-3-4-5 是合法的顺子（最小）
- 7张牌选最好的5张

### 性能监控
- 定期运行 benchmark
- 识别性能瓶颈
- 避免过早优化

### API 稳定性
- 评估器接口要稳定
- 内部实现可以优化替换

## 禁止行为

- ❌ 为了性能牺牲正确性
- ❌ 返回不一致的评估结果
- ❌ 在没有测试的情况下优化
- ❌ 暴露内部实现细节

## 成功标准

- ✅ 所有牌型评估正确
- ✅ 性能达到目标
- ✅ Equity 计算准确
- ✅ Benchmark 持续监控
