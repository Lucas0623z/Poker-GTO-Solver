# Subagents 使用指南

**更新日期**: 2026-03-06
**状态**: ✅ 已配置完成

---

## 📊 Subagents 概览

本项目配置了 7 个专职 subagents，每个负责特定的开发任务。

| Agent | 职责 | 系统提示词 | 状态 |
|-------|------|-----------|------|
| **Architect** | 架构设计与审查 | `.claude/subagents/architect/system.md` | ✅ 已配置 |
| **Game Model** | 游戏规则与状态机 | `.claude/subagents/game-model/system.md` | ✅ 已配置 |
| **Evaluator** | 手牌评估与 Equity | `.claude/subagents/evaluator/system.md` | ✅ 已配置 |
| **Tree & Abstraction** | 博弈树与抽象层 | `.claude/subagents/tree-abstraction/system.md` | ✅ 已配置 |
| **Solver** | CFR 算法实现 | `.claude/subagents/solver/system.md` | ✅ 已配置 |
| **Testing** | 测试与验证 | `.claude/subagents/testing/system.md` | ✅ 已配置 |
| **Interface** | CLI 与结果导出 | `.claude/subagents/interface/system.md` | ✅ 已配置 |

---

## 🚀 如何使用 Subagents

### 方法 1：直接调用（推荐）

在与 Claude Code 交互时，直接指定使用哪个 agent：

```
@architect 请审查当前的模块设计是否符合架构原则

@game-model 请实现 Kuhn Poker 的状态机

@solver 请实现 CFR 算法的 regret update 逻辑

@testing 请为 Card 类编写单元测试
```

### 方法 2：通过任务描述触发

根据任务类型，Claude Code 会自动选择合适的 agent：

```
我需要设计模块接口           → Architect Agent
实现扑克牌比大小逻辑         → Game Model Agent
优化手牌评估器性能           → Evaluator Agent
生成 River 博弈树            → Tree & Abstraction Agent
验证 CFR 算法是否收敛        → Solver Agent
编写集成测试                 → Testing Agent
导出策略到 CSV 文件          → Interface Agent
```

---

## 📋 各 Agent 详细说明

### 1. Architect Agent

**何时使用**:
- 设计新模块时
- 修改公共接口时
- 需要架构审查时
- 重构计划时

**示例任务**:
```
@architect 我想添加一个新的 Range 类，请帮我设计接口

@architect 请审查 GameState 的 API 设计是否合理

@architect 当前的模块依赖关系是否符合分层架构？
```

**输出**:
- 接口定义
- 设计说明
- 影响分析
- 实施建议

---

### 2. Game Model Agent

**何时使用**:
- 实现游戏规则时
- 状态转移逻辑时
- 行动生成时

**示例任务**:
```
@game-model 请实现 Kuhn Poker 的 GameState 类

@game-model 实现 check-bet-fold-call 的状态转移

@game-model 如何处理 all-in 和 side pot？
```

**输出**:
- 规则实现代码
- 状态机设计
- 单元测试

---

### 3. Evaluator Agent

**何时使用**:
- 实现牌力评估时
- 计算 Equity 时
- 优化性能时

**示例任务**:
```
@evaluator 请实现 7 卡手牌评估器

@evaluator 如何优化评估器性能到 < 100ns？

@evaluator 实现蒙特卡洛 Equity 计算器
```

**输出**:
- 评估算法实现
- 性能优化代码
- Benchmark 测试

---

### 4. Tree & Abstraction Agent

**何时使用**:
- 构建博弈树时
- 设计信息集时
- 配置抽象策略时

**示例任务**:
```
@tree-abstraction 请为 Kuhn Poker 构建完整博弈树

@tree-abstraction 如何设计 River 的信息集 Key？

@tree-abstraction 配置 bet sizing 为 0.5x, 1x, 2x pot
```

**输出**:
- 树构建代码
- 信息集设计
- 抽象配置

---

### 5. Solver Agent

**何时使用**:
- 实现 CFR 算法时
- 调试收敛问题时
- 优化求解性能时

**示例任务**:
```
@solver 请实现 Vanilla CFR 算法

@solver CFR 在 Kuhn Poker 上为什么不收敛？

@solver 如何实现 CFR+ 优化？
```

**输出**:
- CFR 实现代码
- 收敛分析
- 算法优化

---

### 6. Testing Agent

**何时使用**:
- 编写测试时
- 验证正确性时
- 性能测试时

**示例任务**:
```
@testing 请为 Card 类编写单元测试

@testing 验证 Kuhn Poker 求解器是否正确

@testing 设计 Evaluator 的性能测试
```

**输出**:
- 测试代码
- 测试报告
- 覆盖率分析

---

### 7. Interface Agent

**何时使用**:
- 设计 CLI 时
- 导出结果时
- 配置文件格式时

**示例任务**:
```
@interface 请设计 solve-kuhn 命令的参数

@interface 如何导出策略到 CSV 格式？

@interface 创建 River 场景的配置文件模板
```

**输出**:
- CLI 实现
- 导出器代码
- 配置文件模板

---

## 🔄 Agent 协作流程

### 典型开发流程

#### 1. 架构设计阶段
```
@architect 请设计 Kuhn Poker 求解器的模块结构
   ↓
Architect 输出接口定义和模块划分
```

#### 2. 实现阶段（并行）
```
@game-model 实现 Kuhn Poker 状态机
@evaluator 实现简单的牌力比较
@tree-abstraction 构建博弈树
   ↓
各 Agent 并行实现自己的模块
```

#### 3. 集成阶段
```
@solver 使用上述模块实现 CFR 求解器
   ↓
Solver 整合所有模块
```

#### 4. 验证阶段
```
@testing 验证 Kuhn Poker 求解器正确性
   ↓
Testing 编写测试并验证
```

#### 5. 交付阶段
```
@interface 创建 CLI 命令运行求解器
   ↓
Interface 提供用户接口
```

---

## 🎯 最佳实践

### 1. 明确指定 Agent

**好的做法**:
```
@game-model 请实现 Card 类
```

**不太好的做法**:
```
帮我实现 Card 类  ← 不明确，可能由错误的 agent 处理
```

### 2. 一次只用一个 Agent

**好的做法**:
```
@game-model 实现 GameState
[等待完成后]
@testing 为 GameState 编写测试
```

**不太好的做法**:
```
@game-model @testing 实现 GameState 并编写测试  ← 职责混乱
```

### 3. 尊重 Agent 职责

**好的做法**:
```
@architect 设计接口
@game-model 实现接口
```

**不太好的做法**:
```
@architect 实现 GameState  ← Architect 不实现代码
```

---

## 📁 项目目录结构

```
D:\GTO\
├── .claude/
│   ├── subagents/              ← Subagent 配置
│   │   ├── architect/
│   │   │   └── system.md       ← 系统提示词
│   │   ├── game-model/
│   │   │   └── system.md
│   │   ├── evaluator/
│   │   │   └── system.md
│   │   ├── tree-abstraction/
│   │   │   └── system.md
│   │   ├── solver/
│   │   │   └── system.md
│   │   ├── testing/
│   │   │   └── system.md
│   │   └── interface/
│   │       └── system.md
│   ├── config.json             ← 主配置
│   ├── claude-code-subagents.md
│   ├── context-folders.md
│   ├── mcp-servers.md
│   └── SUBAGENTS_GUIDE.md      ← 本文档
├── docs/                       ← 所有 agent 参考的文档
│   ├── 构想.md
│   ├── architecture.md
│   ├── cfr-algorithm.md
│   ├── game-rules.md
│   ├── milestones.md
│   └── test-plan.md
└── src/                        ← 代码（待创建）
```

---

## 🔍 验证配置

### 检查 Subagents 是否正确配置

```bash
# 查看所有 subagent 目录
ls -la .claude/subagents/

# 应该看到 7 个目录:
# architect/
# evaluator/
# game-model/
# interface/
# solver/
# testing/
# tree-abstraction/

# 验证每个目录都有 system.md
find .claude/subagents/ -name "system.md"
```

### 测试 Agent 调用

尝试调用一个 agent：
```
@architect 你好，请介绍一下你的职责
```

预期响应应包含：
- 架构相关的职责说明
- 工作原则
- 禁止行为

---

## 🚦 下一步

### Milestone 1 - Kuhn Poker 求解器

**推荐的 Agent 调用顺序**:

1. **@architect** - 创建项目结构和接口定义
2. **@game-model** - 实现 Kuhn Poker 规则
3. **@evaluator** - 实现简单的牌力比较
4. **@tree-abstraction** - 构建 Kuhn Poker 博弈树
5. **@solver** - 实现 CFR 算法
6. **@testing** - 验证所有模块并测试收敛性
7. **@interface** - 创建 CLI 命令

---

## 📚 参考文档

- `.claude/claude-code-subagents.md` - 详细的 agent 设计
- `.claude/config.json` - 主配置文件
- `docs/milestones.md` - 里程碑规划
- `docs/architecture.md` - 架构设计

---

## ❓ 常见问题

### Q1: 如何知道该用哪个 Agent？

**A**: 根据任务类型选择：
- 设计/架构 → Architect
- 规则/状态 → Game Model
- 评估/Equity → Evaluator
- 树/抽象 → Tree & Abstraction
- CFR/求解 → Solver
- 测试/验证 → Testing
- CLI/导出 → Interface

### Q2: 可以同时使用多个 Agent 吗？

**A**: 可以，但建议分步骤进行：
1. 先让一个 agent 完成它的任务
2. 再调用下一个 agent
3. 避免职责混乱

### Q3: Agent 会自动协作吗？

**A**: 不会自动，但可以：
1. 明确告诉 agent 使用其他 agent 的输出
2. 按照推荐的流程顺序调用
3. 查看相关文档了解模块接口

### Q4: 如何修改 Agent 的行为？

**A**: 编辑对应的 `system.md` 文件：
```bash
# 例如修改 Architect Agent 的提示词
vim .claude/subagents/architect/system.md
```

---

**文档维护者**: System
**最后更新**: 2026-03-06
**状态**: ✅ 所有 subagents 已配置完成，可以开始使用
