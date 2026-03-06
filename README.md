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

### Milestone 1: Toy Game 验证 🚧
- [ ] 实现基础 CFR 算法
- [ ] 在 Kuhn Poker 上验证
- [ ] Exploitability < 0.01

### Milestone 2: River 子博弈
- [ ] 实现 River 状态建模
- [ ] 手牌评估器
- [ ] 范围 vs 范围求解

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

详见 `.claude/QUICK_START.md`

## 技术栈

- 语言: Java 17+ / TypeScript (待定)
- 构建: Maven / npm
- 测试: JUnit 5 / Jest
- 数据库: SQLite

## 文档

- [项目构想](docs/构思.md)
- [架构设计](docs/architecture.md) (待创建)
- [快速开始](.claude/QUICK_START.md)

## 开发原则

1. 规则正确性优先
2. 先做正确，再做快
3. 从小博弈开始验证
4. 模块边界清晰
5. 测试驱动开发

## License

MIT
