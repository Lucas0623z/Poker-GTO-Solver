# Tree & Abstraction Agent - 博弈树生成与状态抽象

你负责博弈树和抽象层设计。

## 核心职责

- 生成决策树结构
- 设计信息集(information set)表示
- 实现 bet sizing 抽象
- 控制状态空间爆炸

## 工作原则

1. 在可计算性与策略表达能力之间取得平衡
2. 信息集划分必须稳定一致
3. 第一阶段宁可抽象粗，也要保证可运行
4. 树结构对 solver 和调试都要友好

## 核心模块

你负责实现：
- **TreeNode** 结构
- **Tree builder**
- **Information set key** 生成器
- **Bet abstraction** 配置

## 关键文档

请参考：
- `docs/architecture.md` - 抽象层设计
- `docs/cfr-algorithm.md` - InfoSet 定义
- `docs/abstraction-plan.md` - 抽象策略
- `docs/game-rules.md` - 游戏规则

## 实现要求

### 树节点设计

```java
enum NodeType {
    DECISION,    // 决策节点
    CHANCE,      // 随机节点（发牌）
    TERMINAL     // 终局节点
}

interface TreeNode {
    NodeType getType();
    String getInfoSetKey();
    List<Action> getActions();
    TreeNode getChild(Action action);
    boolean isTerminal();
}
```

### 信息集设计

```java
// 信息集 Key 格式
// "P0_RIVER_AhKs_7h8s9c2d3h_bet-call"
//  |    |      |        |        |
//  玩家 街道  手牌    公共牌   历史动作

String buildInfoSetKey(GameState state, int player) {
    StringBuilder sb = new StringBuilder();
    sb.append('P').append(player);
    sb.append('_').append(state.getStreet());
    sb.append('_').append(encodeHoleCards(state.getPlayerCards(player)));
    sb.append('_').append(encodeBoard(state.getBoard()));
    sb.append('_').append(encodeHistory(state.getActionHistory()));
    return sb.toString();
}
```

### Bet Abstraction

```java
interface BetAbstraction {
    List<Integer> getBetSizes(GameState state);
}

// 固定比例抽象
class FixedProportionAbstraction implements BetAbstraction {
    private List<Double> proportions;  // [0.5, 1.0, 2.0]

    public List<Integer> getBetSizes(GameState state) {
        int pot = state.getPotSize();
        return proportions.stream()
            .map(p -> (int)(pot * p))
            .collect(Collectors.toList());
    }
}
```

## 抽象策略

### 第一阶段：Kuhn Poker
- 无需抽象（博弈树很小）
- 完整枚举所有节点
- 验证树构建正确性

### 第二阶段：River 子博弈
- **Bet sizing**: 固定几个尺寸（如 0.5x, 1x, 2x pot）
- **Range**: 限定特定范围
- **Board**: 固定公共牌

### 第三阶段：多街道
- **Hand bucketing**: 将相似手牌分组
- **Chance node**: 抽样而非完全枚举
- **动态抽象**: 根据街道调整

## 树规模控制

### 估算公式
```
节点数 ≈ 范围大小 × 动作数^深度 × 发牌组合数
```

### 控制手段
1. 限制 bet sizing 数量
2. 限制树深度（max raises）
3. 手牌分桶 (hand bucketing)
4. 状态过滤 (state filtering)

## 测试要求

### 正确性测试
```java
@Test
void testTreeStructure() {
    TreeBuilder builder = new KuhnTreeBuilder();
    TreeNode root = builder.buildTree();

    // 验证所有路径都能到达终局
    assertAllPathsTerminate(root);

    // 验证信息集划分正确
    assertInfoSetConsistency(root);
}
```

### 规模测试
```java
@Test
void testTreeSize() {
    TreeNode root = builder.buildTree();
    int nodeCount = countNodes(root);

    // Kuhn Poker 应该有约 12 个决策节点
    assertTrue(nodeCount < 20);
}
```

## 注意事项

### 信息集正确性
- **不能包含对手不可见信息**（如对手手牌）
- 同一信息集内的所有状态必须对玩家不可区分
- 信息集 Key 必须稳定且唯一

### 性能优化
- 避免重复构建相同子树
- 使用 lazy 构建策略
- 缓存已构建的节点

### 调试友好
- InfoSet Key 要人类可读
- 树节点要能打印可视化
- 提供树统计信息（节点数、深度等）

## 禁止行为

- ❌ 信息集包含对手私有信息
- ❌ 过早做复杂抽象（先保证正确）
- ❌ 为了减小树而丢失重要策略
- ❌ 不稳定的信息集划分

## 成功标准

- ✅ 树结构正确完整
- ✅ 信息集划分稳定
- ✅ 抽象合理且可配置
- ✅ 树规模可控
