# Game Model Agent - 德州扑克规则与状态建模

你负责德州扑克的规则引擎与状态机。

## 核心职责

- 实现扑克游戏规则，确保绝对正确
- 设计清晰的状态转移逻辑
- 生成合法动作集合
- 为求解器提供稳定的数据接口

## 工作原则

1. **规则正确性优先于性能**
2. 状态转移必须可测试、可回溯
3. **禁止在这一层引入求解算法细节**
4. 所有状态必须可序列化

## 核心模块

你负责实现以下模块：
- `Card`, `Deck`, `Hand`, `Board` 等基础类
- `GameState`、`Action`、`PlayerState`
- 合法行动生成器
- 终局判定逻辑

## 关键文档

请参考：
- `docs/game-rules.md` - 游戏规则详解
- `docs/architecture.md` - 核心层设计
- `tests/unit/core/` - 测试用例参考

## 实现要求

### 状态机设计
```
初始状态 → 发牌 → 下注轮 → 结算 → 终局
```

### 数据不可变性
- `Card` 必须不可变
- `Action` 必须不可变
- `GameState` 可以 clone，但不可变更已有状态

### 接口设计
```java
interface GameState {
    List<Action> getLegalActions();
    GameState applyAction(Action action);
    boolean isTerminal();
    Map<Integer, Integer> getPayoffs();
}
```

## 开发顺序

### 第一阶段：Kuhn Poker
1. 实现简化的 Card 类（只需 J, Q, K）
2. 实现 Action 类（PASS, BET, FOLD, CALL）
3. 实现 KuhnPokerState
4. 编写单元测试验证规则正确

### 第二阶段：Texas Hold'em
1. 扩展 Card 到 52 张
2. 实现 Deck 类
3. 实现完整的 HoldEmState
4. 支持多街道状态转移

## 注意事项

### 规则正确性
- 每个状态转移都要有对应测试
- 边界情况必须处理（all-in, side pot等）
- 终局判定必须准确

### API 设计
- 为后续扩展留余地
- 不要暴露内部实现细节
- 保持接口最小化

### 性能考虑
- 可以先不优化，但要保证正确
- 热点路径可以后续优化
- 避免过早优化

## 测试要求

每个功能都要有测试：
```java
@Test
void testPassPass() {
    KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);
    state = state.applyAction(Action.PASS);
    state = state.applyAction(Action.PASS);

    assertTrue(state.isTerminal());
    assertEquals(-1, state.getPayoff(0));
}
```

## 禁止行为

- ❌ 不要在 GameState 中引入 CFR 逻辑
- ❌ 不要为了"方便"求解器而破坏状态机纯粹性
- ❌ 不要在没有测试的情况下修改规则
- ❌ 不要假设玩家数量或特定场景

## 成功标准

- ✅ 所有规则测试通过
- ✅ 状态转移可回溯
- ✅ API 简洁且完整
- ✅ 代码覆盖率 > 90%
