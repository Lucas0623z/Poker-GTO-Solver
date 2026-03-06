# CFR 算法详解

**Counterfactual Regret Minimization (反事实遗憾最小化)**

**版本**: 0.1.0
**更新日期**: 2026-03-06

---

## 1. 算法背景

### 1.1 目标

在不完全信息博弈中，找到**纳什均衡策略**（Nash Equilibrium）。

**纳什均衡的定义**:
当所有玩家都采用均衡策略时，任何玩家单方面改变策略都不能获得更高收益。

### 1.2 挑战

- 状态空间巨大（德州扑克有 10^160 个状态）
- 玩家信息不对称（看不到对手的手牌）
- 多轮决策，策略空间复杂

### 1.3 CFR 的优势

- **可证明收敛**：理论保证收敛到纳什均衡
- **实用性强**：已成功应用于扑克 AI（Libratus, DeepStack）
- **易于实现**：核心算法简洁明了
- **可扩展**：有多种优化版本（CFR+, MCCFR）

---

## 2. 核心概念

### 2.1 信息集 (Information Set)

**定义**: 玩家无法区分的游戏状态集合。

**示例**（德州扑克）:
- 玩家持有 AhKs
- 公共牌是 7h 8s 9c
- 历史动作是 "bet-call"

即使对手手牌不同，玩家看到的信息相同，属于同一个信息集。

**信息集Key格式**:
```
"P0_RIVER_AhKs_7h8s9c2d3h_bet-call"
```

### 2.2 策略 (Strategy)

**定义**: 在每个信息集上，对所有合法动作的概率分布。

**符号**:
- σ: 策略
- σ(I, a): 在信息集 I 上选择动作 a 的概率

**示例**:
```
信息集: "P0_RIVER_AhKs_..."
策略:
  - FOLD: 0.0
  - CHECK: 0.3
  - BET(50%pot): 0.5
  - BET(100%pot): 0.2
```

### 2.3 反事实价值 (Counterfactual Value)

**定义**: 假设一定会到达当前信息集，玩家采用某策略能获得的期望收益。

**符号**:
- v^σ(I): 信息集 I 的反事实价值
- v^σ(I, a): 在 I 上选择动作 a 的反事实价值

**计算公式**:
```
v^σ(I) = Σ_a σ(I, a) × v^σ(I, a)
```

### 2.4 即时遗憾 (Instantaneous Regret)

**定义**: 选择某个动作相比当前策略的额外收益。

**符号**:
- r(I, a): 动作 a 在信息集 I 上的即时遗憾

**计算公式**:
```
r^t(I, a) = v^σ(I, a) - v^σ(I)
```

**含义**:
- r > 0: "后悔"没选这个动作
- r < 0: "庆幸"没选这个动作

### 2.5 累积遗憾 (Cumulative Regret)

**定义**: 所有历史迭代的遗憾总和。

**符号**:
- R^T(I, a): 到第 T 次迭代，动作 a 的累积遗憾

**计算公式**:
```
R^T(I, a) = Σ_{t=1}^T r^t(I, a)
```

### 2.6 Regret Matching

**核心思想**: 下一次策略应该更多选择累积遗憾高的动作。

**公式**:
```
σ^{T+1}(I, a) = R^T_+(I, a) / Σ_b R^T_+(I, b)
```

其中 `R^T_+(I, a) = max(R^T(I, a), 0)` （负遗憾归零）

**特例**: 如果所有动作遗憾都 ≤ 0，使用均匀策略。

---

## 3. Vanilla CFR 算法

### 3.1 伪代码

```
function CFR(iterations):
    for t = 1 to iterations:
        for player p in players:
            π^p = 1.0  // 玩家 p 的到达概率
            π^-p = 1.0 // 对手的到达概率
            CFR_Recursive(root, p, π^p, π^-p)

    return AverageStrategy()

function CFR_Recursive(node, player, π^p, π^-p):
    // 终局节点
    if node.isTerminal():
        return node.getPayoff(player)

    // 随机节点（发牌）
    if node.isChance():
        value = 0
        for each outcome in node.getOutcomes():
            prob = node.getProbability(outcome)
            child = node.getChild(outcome)
            value += prob × CFR_Recursive(child, player, π^p, π^-p)
        return value

    // 决策节点
    I = node.getInfoSet()
    actions = node.getActions()

    // 获取当前策略
    σ = GetCurrentStrategy(I, actions)

    // 计算每个动作的反事实价值
    actionValues = {}
    nodeValue = 0

    for action in actions:
        if node.actingPlayer == player:
            newπ^p = π^p × σ(action)
            actionValue = CFR_Recursive(node.getChild(action), player, newπ^p, π^-p)
        else:
            newπ^-p = π^-p × σ(action)
            actionValue = CFR_Recursive(node.getChild(action), player, π^p, newπ^-p)

        actionValues[action] = actionValue
        nodeValue += σ(action) × actionValue

    // 更新遗憾和策略（仅对当前玩家）
    if node.actingPlayer == player:
        for action in actions:
            regret = actionValues[action] - nodeValue
            R(I, action) += π^-p × regret
            strategySum(I, action) += π^p × σ(action)

    return nodeValue

function GetCurrentStrategy(I, actions):
    // Regret Matching
    strategy = {}
    normalizingSum = 0

    for action in actions:
        positiveRegret = max(R(I, action), 0)
        strategy[action] = positiveRegret
        normalizingSum += positiveRegret

    if normalizingSum > 0:
        for action in actions:
            strategy[action] /= normalizingSum
    else:
        // 均匀策略
        for action in actions:
            strategy[action] = 1.0 / len(actions)

    return strategy

function AverageStrategy():
    // 归一化累积策略
    avgStrategy = {}
    for infoSet I:
        normalizingSum = sum(strategySum(I, a) for all a)
        if normalizingSum > 0:
            for action in I.actions:
                avgStrategy(I, action) = strategySum(I, action) / normalizingSum
        else:
            avgStrategy(I, action) = 1.0 / len(I.actions)

    return avgStrategy
```

### 3.2 关键步骤解释

#### Step 1: 遍历博弈树
- 递归访问每个节点
- 计算到达概率（reach probability）

#### Step 2: 计算反事实价值
- 终局节点：返回收益
- 随机节点：加权平均所有可能结果
- 决策节点：根据当前策略计算期望值

#### Step 3: 更新遗憾
```
regret = v(I, a) - v(I)
R(I, a) += π^-p × regret
```

**为什么乘以 π^-p？**
- 对手越容易到达这个状态，遗憾的权重越高

#### Step 4: 累积策略
```
strategySum(I, a) += π^p × σ(a)
```

**为什么乘以 π^p？**
- 自己越容易到达，这个策略越重要

#### Step 5: 生成新策略
使用 Regret Matching 根据累积遗憾生成下一次策略。

---

## 4. 收敛性保证

### 4.1 理论保证

**定理** (Zinkevich et al., 2007):

经过 T 次迭代后，平均策略的 exploitability 满足：

```
exploitability ≤ O(√(|I| × |A|) / √T)
```

其中：
- |I|: 信息集数量
- |A|: 平均每个信息集的动作数
- T: 迭代次数

**含义**:
- 迭代次数越多，越接近纳什均衡
- 收敛速度 O(1/√T)

### 4.2 实际收敛

**Kuhn Poker**:
- 信息集数量: ~12
- 10,000 次迭代 → exploitability < 0.001

**Leduc Poker**:
- 信息集数量: ~288
- 100,000 次迭代 → exploitability < 0.01

**Texas Hold'em (简化)**:
- 信息集数量: 10^6+
- 需要数十亿次迭代

---

## 5. CFR 变种

### 5.1 CFR+ (CFR Plus)

**改进**: 更激进的 Regret Matching

**变化**:
```
// Vanilla CFR
R^t(I, a) = R^{t-1}(I, a) + regret

// CFR+
R^t(I, a) = max(R^{t-1}(I, a) + regret, 0)
```

**优势**:
- 收敛速度更快（经验上提升 2-3 倍）
- 遗憾不会无限累积负值

**适用场景**:
- 中大型博弈
- 需要快速收敛

### 5.2 MCCFR (Monte Carlo CFR)

**改进**: 使用抽样减少计算量

**核心思想**:
不遍历完整博弈树，而是：
1. 随机采样部分路径
2. 只更新采样路径上的遗憾
3. 通过多次抽样逼近完整CFR

**常见抽样策略**:

#### Outcome Sampling
- 每次迭代只采样一条完整路径
- 适合深度大的博弈树

#### External Sampling
- 对手的行动全部遍历
- 只采样随机事件（发牌）
- 平衡准确性和速度

#### Chance Sampling
- 只采样随机事件
- 所有玩家行动都遍历
- 德州扑克常用方法

**优势**:
- 大幅减少计算量（10-100倍）
- 适合巨大状态空间

**劣势**:
- 收敛速度比 Vanilla CFR 慢
- 需要更多迭代次数

### 5.3 Linear CFR

**改进**: 给最近的迭代更高权重

**策略计算**:
```
// Vanilla CFR
avgStrategy = strategySum / T

// Linear CFR
avgStrategy = Σ_{t=1}^T t × strategy^t / Σ_{t=1}^T t
```

**优势**:
- 适应非平稳博弈
- 更快适应策略变化

---

## 6. 实现技巧

### 6.1 InfoSet Key 设计

**高效编码**:
```java
String buildInfoSetKey(GameState state, int player) {
    StringBuilder sb = new StringBuilder();
    sb.append('P').append(player);
    sb.append('_').append(state.getStreet().ordinal());

    // 编码手牌（自己可见）
    List<Card> holeCards = state.getPlayerCards(player);
    sb.append('_').append(encodeCards(holeCards));

    // 编码公共牌
    sb.append('_').append(encodeCards(state.getBoard()));

    // 编码动作历史
    sb.append('_').append(encodeHistory(state.getActionHistory()));

    return sb.toString();
}
```

**注意**:
- 不能包含对手的手牌（信息不对称）
- 同一信息集的所有状态必须生成相同 key
- 编码要紧凑（减少内存）

### 6.2 存储优化

**Regret Table**:
```java
// 使用嵌套 HashMap
Map<String, Map<Action, Double>> regrets = new HashMap<>();

// 访问
double getRegret(String infoSet, Action action) {
    return regrets.getOrDefault(infoSet, Collections.emptyMap())
                  .getOrDefault(action, 0.0);
}
```

**策略累积**:
```java
Map<String, Map<Action, Double>> strategySum = new HashMap<>();
```

**内存估计**:
- 每个信息集: ~100 bytes
- 100 万信息集: ~100 MB
- 可接受范围

### 6.3 并行化

**可并行部分**:
- 不同玩家的 CFR 遍历（需要同步）
- MCCFR 的多次采样（完全独立）

**示例**:
```java
ExecutorService executor = Executors.newFixedThreadPool(numThreads);
List<Future<Double>> futures = new ArrayList<>();

for (int i = 0; i < numSamples; i++) {
    futures.add(executor.submit(() -> {
        return mccrSample(root, player);
    }));
}

// 合并结果
for (Future<Double> future : futures) {
    totalValue += future.get();
}
```

---

## 7. 调试技巧

### 7.1 验证正确性

**检查点**:
1. ✅ 策略概率和为 1
2. ✅ 终局节点收益正确
3. ✅ 信息集划分合理
4. ✅ 遗憾更新方向正确

**单元测试**:
```java
@Test
public void testStrategyNormalization() {
    Map<Action, Double> strategy = solver.getStrategy(infoSet);
    double sum = strategy.values().stream().mapToDouble(d -> d).sum();
    assertEquals(1.0, sum, 1e-6);
}
```

### 7.2 监控收敛

**关键指标**:
```java
class ConvergenceMonitor {
    public void logIteration(int iteration, Strategy strategy) {
        double exploitability = calculateExploitability(strategy);
        double avgRegret = calculateAverageRegret();

        System.out.printf("Iteration %d: exploitability=%.6f, avgRegret=%.6f%n",
                         iteration, exploitability, avgRegret);
    }
}
```

**预期趋势**:
- Exploitability 应该持续下降
- 最终稳定在接近 0 的值

### 7.3 常见错误

**错误 1**: 忘记归一化策略
```java
// ❌ 错误
strategy.put(action, positiveRegret);

// ✅ 正确
strategy.put(action, positiveRegret / normalizingSum);
```

**错误 2**: 信息集包含不可见信息
```java
// ❌ 错误：包含对手手牌
key = player + "_" + myCards + "_" + opponentCards;

// ✅ 正确：只包含可见信息
key = player + "_" + myCards + "_" + board + "_" + history;
```

**错误 3**: 到达概率计算错误
```java
// ❌ 错误：总是用自己的概率
newReach = reach * strategy.get(action);

// ✅ 正确：根据行动玩家区分
if (actingPlayer == player) {
    newReachPlayer *= strategy.get(action);
} else {
    newReachOpponent *= strategy.get(action);
}
```

---

## 8. 参考资源

### 8.1 论文

1. **[CFR 原始论文]** Zinkevich, M., et al. (2007). "Regret Minimization in Games with Incomplete Information"
   - https://poker.cs.ualberta.ca/publications/NIPS07-cfr.pdf

2. **[CFR+ 改进]** Tammelin, O. (2014). "Solving Large Imperfect Information Games Using CFR+"
   - https://arxiv.org/abs/1407.5042

3. **[MCCFR]** Lanctot, M., et al. (2009). "Monte Carlo Sampling for Regret Minimization in Extensive Games"
   - https://papers.nips.cc/paper/2009/file/00411460f7c92d2124a67ea0f4cb5f85-Paper.pdf

### 8.2 开源实现

- **OpenSpiel**: Google 的博弈框架
  - https://github.com/deepmind/open_spiel

- **PokerSnowie**: 商业扑克求解器（闭源）
  - https://www.pokersnowie.com/

---

## 9. 下一步

1. ✅ 理解 CFR 算法原理
2. ⏳ 在 Kuhn Poker 上实现 Vanilla CFR
3. ⏳ 验证收敛性（exploitability < 0.01）
4. ⏳ 扩展到 Leduc Poker
5. ⏳ 实现 CFR+ 优化
6. ⏳ 尝试 MCCFR 抽样

---

**文档维护者**: Solver Agent
**参考**: CFR 原始论文 (Zinkevich et al., 2007)
**最后更新**: 2026-03-06
