# 项目里程碑规划

**版本**: 0.1.0
**更新日期**: 2026-03-06
**状态**: 执行中

---

## 里程碑概览

| 里程碑 | 目标 | 预计时间 | 状态 |
|--------|------|----------|------|
| M1 | Toy Game 验证 | 1-2 周 | 🚧 进行中 |
| M2 | River 子博弈 | 2-3 周 | ⏳ 未开始 |
| M3 | Turn/Flop 扩展 | 3-4 周 | ⏳ 未开始 |
| M4 | 完整流程 | 2-3 周 | ⏳ 未开始 |

---

## Milestone 1: Toy Game 验证

### 目标

证明 CFR 算法实现正确，能在小型博弈中收敛到纳什均衡。

### 关键成果

1. **Kuhn Poker 求解器**
   - 完整实现 Kuhn Poker 规则
   - Vanilla CFR 算法
   - 10,000 次迭代收敛
   - Exploitability < 0.01

2. **Leduc Poker 求解器**（可选，时间允许）
   - 扩展到 2 轮下注
   - 支持公共牌
   - 100,000 次迭代收敛

### 任务分解

#### Task 1.1: 项目基础设施 ✅

**负责 Agent**: Architect

**任务**:
- [x] 创建架构文档
- [x] 创建 CFR 算法文档
- [x] 创建游戏规则文档
- [x] 创建里程碑规划
- [x] 初始化 Git 仓库
- [ ] 创建 Maven/Gradle 项目
- [ ] 配置依赖

**产出**:
- `pom.xml` 或 `build.gradle`
- 项目目录结构

**验收标准**:
- ✅ 可以编译通过
- ✅ 可以运行测试

**预计时间**: 1 天

---

#### Task 1.2: 核心数据结构

**负责 Agent**: Game Model

**任务**:
- [ ] 实现 `Card` 类
- [ ] 实现 `Action` 类（PASS, BET, FOLD, CALL）
- [ ] 实现 `GameState` 类（Kuhn Poker）
- [ ] 实现 `ActionGenerator`

**代码文件**:
```
src/core/cards/Card.java
src/core/actions/Action.java
src/core/game_state/KuhnPokerState.java
src/core/actions/KuhnActionGenerator.java
```

**产出**:
- 可用的游戏状态表示
- 合法动作生成

**验收标准**:
- ✅ 可以表示所有 Kuhn Poker 状态
- ✅ 可以正确生成合法动作
- ✅ 单元测试覆盖率 > 80%

**预计时间**: 2 天

---

#### Task 1.3: Kuhn Poker 评估器

**负责 Agent**: Evaluator

**任务**:
- [ ] 实现牌力比较（J < Q < K）
- [ ] 实现终局收益计算
- [ ] 编写单元测试

**代码文件**:
```
src/core/evaluator/KuhnEvaluator.java
tests/unit/evaluator/KuhnEvaluatorTest.java
```

**产出**:
- 简单的牌力评估器

**验收标准**:
- ✅ 正确判断 J vs Q (Q 胜)
- ✅ 正确判断 Q vs K (K 胜)
- ✅ 正确计算终局收益

**预计时间**: 1 天

---

#### Task 1.4: 博弈树构建

**负责 Agent**: Tree & Abstraction

**任务**:
- [ ] 实现 `TreeNode` 接口
- [ ] 实现 `DecisionNode`
- [ ] 实现 `TerminalNode`
- [ ] 实现 `TreeBuilder`（完整枚举）
- [ ] 生成 Kuhn Poker 完整博弈树

**代码文件**:
```
src/core/tree/TreeNode.java
src/core/tree/DecisionNode.java
src/core/tree/TerminalNode.java
src/core/tree/KuhnTreeBuilder.java
```

**产出**:
- 可遍历的博弈树

**验收标准**:
- ✅ 树包含所有可能路径
- ✅ 信息集正确划分
- ✅ 终局节点收益正确
- ✅ 树节点数 < 20（Kuhn Poker 很小）

**预计时间**: 2-3 天

---

#### Task 1.5: CFR 求解器

**负责 Agent**: Solver

**任务**:
- [ ] 实现 `RegretTable`
- [ ] 实现 `Strategy` 类
- [ ] 实现 Vanilla CFR 核心算法
- [ ] 实现 Exploitability 计算
- [ ] 添加收敛日志

**代码文件**:
```
src/solver/cfr/RegretTable.java
src/solver/cfr/Strategy.java
src/solver/cfr/VanillaCFR.java
src/solver/metrics/Exploitability.java
```

**产出**:
- 可运行的 CFR 求解器

**验收标准**:
- ✅ 10,000 次迭代后收敛
- ✅ Exploitability < 0.01
- ✅ 策略接近已知均衡（误差 < 5%）

**预计时间**: 3-4 天

---

#### Task 1.6: 测试与验证

**负责 Agent**: Testing

**任务**:
- [ ] 编写单元测试（每个模块）
- [ ] 编写集成测试（完整流程）
- [ ] 对比已知均衡策略
- [ ] 性能测试（求解时间）
- [ ] 编写测试报告

**测试文件**:
```
tests/unit/core/KuhnStateTest.java
tests/unit/solver/VanillaCFRTest.java
tests/integration/KuhnPokerIntegrationTest.java
tests/benchmark/KuhnBenchmark.java
```

**产出**:
- 完整测试套件
- 测试报告文档

**验收标准**:
- ✅ 单元测试覆盖率 > 80%
- ✅ 所有测试通过
- ✅ 与已知均衡策略对比误差 < 5%
- ✅ 求解时间 < 10 秒

**预计时间**: 2-3 天

---

#### Task 1.7: CLI 工具

**负责 Agent**: Interface

**任务**:
- [ ] 创建命令行入口
- [ ] 实现配置文件解析
- [ ] 实现策略导出（JSON）
- [ ] 添加日志输出

**代码文件**:
```
src/app/cli/KuhnSolverCLI.java
src/io/config/ConfigLoader.java
src/io/export/JSONExporter.java
```

**使用示例**:
```bash
# 运行 Kuhn Poker 求解器
java -jar poker-solver.jar solve-kuhn --iterations 10000 --output kuhn-strategy.json

# 输出
Iteration 1000: exploitability = 0.1234
Iteration 2000: exploitability = 0.0567
...
Iteration 10000: exploitability = 0.0034
Strategy saved to kuhn-strategy.json
```

**产出**:
- 可执行 JAR
- 配置文件模板

**验收标准**:
- ✅ 可以通过命令行运行
- ✅ 可以导出策略文件
- ✅ 日志清晰易读

**预计时间**: 1-2 天

---

### Milestone 1 验收标准

**功能验收**:
- [x] 文档完整（架构、算法、规则、里程碑）
- [ ] Kuhn Poker 求解器可运行
- [ ] 10,000 次迭代收敛
- [ ] Exploitability < 0.01
- [ ] 策略接近已知均衡

**质量验收**:
- [ ] 单元测试覆盖率 > 80%
- [ ] 所有测试通过
- [ ] 代码遵循架构设计
- [ ] 模块边界清晰

**性能验收**:
- [ ] 求解时间 < 10 秒
- [ ] 内存占用 < 100 MB

**文档验收**:
- [ ] 代码有清晰注释
- [ ] 关键算法有说明
- [ ] 有使用文档

---

## Milestone 2: River 子博弈

### 目标

实现德州扑克 River 阶段的 GTO 求解器。

### 关键成果

1. **德州扑克核心模块**
   - 52 张牌系统
   - 7 卡手牌评估器
   - 范围表示

2. **River 求解器**
   - 固定公共牌场景
   - 范围 vs 范围对抗
   - 有限下注抽象
   - 输出策略矩阵

### 任务分解

#### Task 2.1: 德州扑克数据结构

**负责 Agent**: Game Model

**任务**:
- [ ] 扩展 `Card` 到 52 张
- [ ] 实现 `Deck` 类
- [ ] 实现 `Range` 类
- [ ] 实现 River `GameState`

**代码文件**:
```
src/core/cards/Rank.java
src/core/cards/Suit.java
src/core/cards/Deck.java
src/core/ranges/Range.java
src/core/game_state/RiverState.java
```

**预计时间**: 3 天

---

#### Task 2.2: 7 卡评估器

**负责 Agent**: Evaluator

**任务**:
- [ ] 实现 `HandRank` 枚举
- [ ] 实现 7 卡评估算法
- [ ] 实现 Equity 计算器（蒙特卡洛）
- [ ] 性能优化（查表法）

**代码文件**:
```
src/core/evaluator/HandRank.java
src/core/evaluator/HandEvaluator.java
src/core/evaluator/EquityCalculator.java
```

**性能目标**:
- 单次评估 < 1μs
- 10,000 次 equity 计算 < 1 秒

**预计时间**: 5-7 天

---

#### Task 2.3: Bet Size Abstraction

**负责 Agent**: Tree & Abstraction

**任务**:
- [ ] 设计 bet sizing 策略
- [ ] 实现配置化抽象
- [ ] 限制树规模

**配置示例**:
```json
{
  "river_abstraction": {
    "bet_sizes": [0.5, 1.0, 2.0],  // pot 的倍数
    "max_raises": 2
  }
}
```

**预计时间**: 2-3 天

---

#### Task 2.4: River 博弈树生成

**负责 Agent**: Tree & Abstraction

**任务**:
- [ ] 生成 River 决策树
- [ ] 处理范围分桶
- [ ] 优化树规模

**预计时间**: 3-4 天

---

#### Task 2.5: River CFR 求解

**负责 Agent**: Solver

**任务**:
- [ ] 扩展 CFR 到支持范围
- [ ] 实现 MCCFR 抽样（可选）
- [ ] 优化大规模求解

**目标**:
- 简单 River 场景 100,000 次迭代收敛
- Exploitability < 0.05

**预计时间**: 5-7 天

---

#### Task 2.6: 策略导出与展示

**负责 Agent**: Interface

**任务**:
- [ ] 策略矩阵导出（CSV）
- [ ] 可读性优化
- [ ] 范围热力图（可选）

**产出示例**:
```csv
Hand,Check,Bet(50%),Bet(100%),Bet(All-in)
AA,0.0,0.2,0.5,0.3
KK,0.1,0.3,0.4,0.2
...
```

**预计时间**: 2-3 天

---

### Milestone 2 验收标准

**功能验收**:
- [ ] River 求解器可运行
- [ ] 支持自定义公共牌
- [ ] 支持自定义范围
- [ ] 支持配置下注尺寸

**质量验收**:
- [ ] 收敛到低 exploitability (< 0.05)
- [ ] 策略符合 GTO 直觉
- [ ] 测试覆盖率 > 75%

**性能验收**:
- [ ] 简单场景求解 < 10 分钟
- [ ] 内存占用 < 2 GB

---

## Milestone 3: Turn/Flop 扩展

### 目标

扩展到多街道，实现 Turn 和 Flop 求解。

### 关键成果

1. **Chance Node 处理**
   - 发牌随机节点
   - 多街道状态转移

2. **MCCFR 优化**
   - 抽样策略
   - 减少计算量

3. **更强抽象**
   - Hand bucketing
   - 动态 bet sizing

### 任务分解（概要）

#### Task 3.1: Chance Node 实现
- 随机节点遍历
- 概率加权

**预计时间**: 3-4 天

#### Task 3.2: Turn 求解器
- Turn + River 联合求解
- 状态空间管理

**预计时间**: 7-10 天

#### Task 3.3: MCCFR 实现
- External Sampling
- Chance Sampling

**预计时间**: 5-7 天

#### Task 3.4: Flop 求解器
- Flop + Turn + River
- Hand bucketing

**预计时间**: 10-14 天

---

### Milestone 3 验收标准

**功能验收**:
- [ ] 支持 Turn 求解
- [ ] 支持 Flop 求解
- [ ] MCCFR 正常工作

**性能验收**:
- [ ] Turn 场景求解 < 1 小时
- [ ] Flop 场景求解 < 12 小时

---

## Milestone 4: 完整流程

### 目标

实现 Preflop 到 River 的完整德州扑克求解器。

### 关键成果

1. **Preflop 范围定义**
2. **完整 4 街求解**
3. **CLI 工具完善**
4. **策略可视化**

### 任务分解（概要）

#### Task 4.1: Preflop 范围
- 标准范围库
- 范围解析器

**预计时间**: 3-5 天

#### Task 4.2: 完整求解器
- 4 街联合求解
- 超大状态空间管理

**预计时间**: 14-21 天

#### Task 4.3: 结果分析工具
- EV 计算
- 范围对比
- 策略查看器

**预计时间**: 7-10 天

---

### Milestone 4 验收标准

**功能验收**:
- [ ] 支持完整 4 街求解
- [ ] 可配置场景
- [ ] 可导出完整策略

**可用性验收**:
- [ ] CLI 工具易用
- [ ] 文档完整
- [ ] 可供实际使用

---

## 风险与应对

### 风险 1: 性能瓶颈

**风险**:
- 状态空间过大
- 求解时间过长

**应对**:
- 更激进的抽象
- MCCFR 抽样
- 并行计算
- 代码性能优化

### 风险 2: 收敛问题

**风险**:
- 复杂场景不收敛
- Exploitability 高

**应对**:
- 使用 CFR+ 优化
- 增加迭代次数
- 调整抽象粒度
- 分阶段求解

### 风险 3: 内存不足

**风险**:
- 信息集过多
- OOM 错误

**应对**:
- 使用磁盘存储
- 压缩策略表
- 分批处理

---

## 时间线

```
Week 1-2:  Milestone 1 (Kuhn Poker)
Week 3-5:  Milestone 2 (River)
Week 6-9:  Milestone 3 (Turn/Flop)
Week 10-12: Milestone 4 (Complete)
```

**总预计时间**: 12 周（3 个月）

---

## 当前状态

**进度**: Milestone 1 - Task 1.1 完成

**下一步**:
1. 创建 Maven 项目
2. 实现核心数据结构
3. 实现 Kuhn Poker 规则

---

**文档维护者**: Architect Agent
**审核者**: 全体 Agents
**最后更新**: 2026-03-06
