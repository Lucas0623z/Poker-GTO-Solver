# 德州扑克 GTO 模拟器

基于 CFR（Counterfactual Regret Minimization）算法的德州扑克博弈论求解器。

## 项目目标

不依赖大模型训练，仅使用数学建模 + 搜索算法 + 博弈论方法，实现德州扑克 GTO 策略求解。

## 核心特性

- 基于 CFR/CFR+/MCCFR 算法
- 模块化架构设计
- 从简单博弈逐步扩展
- 完整的测试验证体系

## 开发阶段

### Milestone 1: Toy Game 验证 ✅ **已完成！**
- [x] 实现基础 CFR 算法
- [x] 在 Kuhn Poker 上验证
- [x] 核心数据结构(Card, Action, GameState)
- [x] 博弈树构建(TreeNode, DecisionNode, TerminalNode)
- [x] Vanilla CFR 求解器
- [x] **Exploitability 度量** ✅ **< 0.001**
- [x] **CFR+ 优化** (收敛速度提升 30%)
- [x] CLI 工具 + 对比工具
- [x] 基础单元测试
- [x] 算法验证完成

**成果**: Exploitability = 0.000170 (优秀！)

### Milestone 2: River 子博弈 ✅ **已完成 95%！**
- [x] **52张牌系统** (Rank, Suit, TexasCard, Deck) ✅
- [x] **HandRank枚举** (10种牌型) ✅
- [x] **基础系统测试通过** ✅
- [x] **7卡手牌评估器** (HandEvaluator) ✅
- [x] **快速查表评估器** (LookupEvaluator, 100x性能提升) ✅
- [x] **Equity计算器** (MonteCarloEquityCalculator) ✅
- [x] **范围表示系统** (Range, HandCombo, RangeParser) ✅
- [x] **River状态建模** (RiverState, RiverPlayerState) ✅
- [x] **River博弈树构建** (RiverTreeBuilder) ✅
- [x] **River CFR求解器** (RiverCFR, CFR+) ✅
- [x] **策略分析和导出** (StrategyAnalyzer, StrategyExporter) ✅
- [x] **完整Demo程序** (RiverGTOSolverDemo) ✅
- [x] **性能优化完成** (InfoSet Key缓存 + 稀疏策略存储) ✅
- [ ] 集成测试验证 (90%)
- [ ] 文档API完善 (80%)

**进度**: 核心功能100%, 性能优化100%, 总体98%

**成果**:
- ✅ River GTO Solver完整可用!
- ✅ **性能优化完成** (速度提升20%, 内存节省40%)
- 详见 [Milestone 2总结](docs/MILESTONE2_SUMMARY.md)
- 详见 [性能优化报告](docs/OPTIMIZATION_COMPLETED.md)

### Milestone 3: Turn/Flop 扩展
- [ ] 添加 Chance Node
- [ ] MCCFR 优化
- [ ] 状态抽象

### Milestone 4: 完整流程
- [ ] Preflop 到 River
- [ ] CLI 工具
- [ ] 策略导出

## 项目结构

```
poker-gto-solver/
├── .claude/          # Claude Code 配置
├── docs/             # 文档
├── src/              # 源代码
│   ├── core/        # 核心模块（规则、评估）
│   ├── solver/      # CFR 求解器
│   ├── abstraction/ # 状态抽象
│   └── app/         # CLI 应用
├── tests/           # 测试
└── config/          # 配置文件
```

## 快速开始

详见 [快速开始指南](docs/QUICK_START.md)

### 立即运行

```bash
# 使用 Maven
mvn clean compile
java -cp target/classes com.poker.gto.app.cli.KuhnSolverCLI 10000

# 或直接编译
javac -d bin -sourcepath src/main/java src/main/java/com/poker/gto/**/*.java
java -cp bin com.poker.gto.app.cli.KuhnSolverCLI
```

## 技术栈

- 语言: Java 17+ / TypeScript (待定)
- 构建: Maven / npm
- 测试: JUnit 5 / Jest
- 数据库: SQLite

## 文档

- [项目构想](docs/构思.md)
- [架构设计](docs/architecture.md)
- [CFR 算法详解](docs/cfr-algorithm.md)
- [游戏规则](docs/game-rules.md)
- [里程碑规划](docs/milestones.md)
- [快速开始](docs/QUICK_START.md)

## 开发原则

1. 规则正确性优先
2. 先做正确，再做快
3. 从小博弈开始验证
4. 模块边界清晰
5. 测试驱动开发

## License

MIT
