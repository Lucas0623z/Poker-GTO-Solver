# Claude Code 上下文文件夹配置

## 为什么需要上下文文件夹？

在 Claude Code 中，每个 subagent 需要访问特定的代码和文档。合理配置上下文文件夹可以：
- 减少无关文件的干扰
- 加快 agent 响应速度
- 提高代码理解准确性
- 控制 token 使用量

---

## 项目目录结构

```
poker-gto-solver/
├─ .claude/                      # Claude Code 配置目录
│  ├─ context/                   # 上下文配置（见下文详细配置）
│  │  ├─ architect/             # 架构 Agent 上下文
│  │  ├─ game-model/            # 规则建模 Agent 上下文
│  │  ├─ evaluator/             # 评估 Agent 上下文
│  │  ├─ tree-abstraction/      # 树抽象 Agent 上下文
│  │  ├─ solver/                # 求解器 Agent 上下文
│  │  ├─ testing/               # 测试 Agent 上下文
│  │  └─ interface/             # 接口 Agent 上下文
│  ├─ subagents/                # Subagent 定义
│  └─ config.json               # 主配置文件
│
├─ docs/                         # 项目文档
│  ├─ 构想.md                   # 项目总构想（已存在）
│  ├─ architecture.md           # 架构设计文档
│  ├─ game-rules.md             # 德州扑克规则详解
│  ├─ abstraction-plan.md       # 抽象策略文档
│  ├─ cfr-algorithm.md          # CFR 算法说明
│  ├─ milestones.md             # 里程碑规划
│  ├─ api/                      # API 文档
│  └─ test-plan.md              # 测试计划
│
├─ src/                          # 源代码
│  ├─ core/                     # 核心模块
│  │  ├─ cards/                 # 扑克牌基础类
│  │  │  ├─ Card.java/ts
│  │  │  ├─ Deck.java/ts
│  │  │  └─ Rank.java/ts
│  │  ├─ evaluator/             # 手牌评估
│  │  │  ├─ HandEvaluator.java/ts
│  │  │  ├─ EquityCalculator.java/ts
│  │  │  └─ HandRank.java/ts
│  │  ├─ ranges/                # 范围表示
│  │  │  ├─ Range.java/ts
│  │  │  └─ RangeParser.java/ts
│  │  ├─ game_state/            # 游戏状态
│  │  │  ├─ GameState.java/ts
│  │  │  ├─ PlayerState.java/ts
│  │  │  └─ Pot.java/ts
│  │  ├─ actions/               # 玩家动作
│  │  │  ├─ Action.java/ts
│  │  │  ├─ ActionType.java/ts
│  │  │  └─ ActionGenerator.java/ts
│  │  └─ tree/                  # 博弈树基础
│  │     ├─ TreeNode.java/ts
│  │     ├─ InfoSet.java/ts
│  │     └─ TreeBuilder.java/ts
│  │
│  ├─ solver/                   # 求解器
│  │  ├─ cfr/                   # CFR 实现
│  │  │  ├─ CFR.java/ts
│  │  │  ├─ VanillaCFR.java/ts
│  │  │  └─ Strategy.java/ts
│  │  ├─ cfr_plus/              # CFR+ 优化
│  │  │  └─ CFRPlus.java/ts
│  │  ├─ mccfr/                 # Monte Carlo CFR
│  │  │  └─ MCCFR.java/ts
│  │  └─ metrics/               # 收敛度量
│  │     ├─ Exploitability.java/ts
│  │     └─ Convergence.java/ts
│  │
│  ├─ abstraction/              # 抽象层
│  │  ├─ bet_sizing/            # 下注抽象
│  │  │  ├─ BetAbstraction.java/ts
│  │  │  └─ BetSizeGenerator.java/ts
│  │  ├─ hand_buckets/          # 手牌分桶
│  │  │  └─ HandBucketing.java/ts
│  │  └─ state_filters/         # 状态过滤
│  │     └─ StateFilter.java/ts
│  │
│  ├─ io/                       # 输入输出
│  │  ├─ config/                # 配置读取
│  │  │  ├─ ConfigParser.java/ts
│  │  │  └─ SolverConfig.java/ts
│  │  ├─ parser/                # 数据解析
│  │  │  └─ RangeParser.java/ts
│  │  └─ export/                # 结果导出
│  │     ├─ JSONExporter.java/ts
│  │     └─ CSVExporter.java/ts
│  │
│  └─ app/                      # 应用层
│     ├─ cli/                   # 命令行工具
│     │  ├─ CLI.java/ts
│     │  └─ commands/
│     └─ api/                   # API 接口（可选）
│
├─ tests/                        # 测试
│  ├─ unit/                     # 单元测试
│  │  ├─ core/
│  │  ├─ solver/
│  │  └─ abstraction/
│  ├─ integration/              # 集成测试
│  │  ├─ toy-games/            # Toy game 验证
│  │  │  ├─ kuhn-poker/
│  │  │  └─ leduc-poker/
│  │  └─ river-scenarios/
│  └─ benchmark/                # 性能测试
│     ├─ evaluator-bench/
│     └─ solver-bench/
│
├─ config/                       # 运行时配置
│  ├─ scenarios/                # 场景配置
│  └─ bet-abstractions/         # 下注抽象配置
│
├─ output/                       # 输出结果（忽略版本控制）
│  ├─ strategies/
│  ├─ logs/
│  └─ reports/
│
├─ scripts/                      # 辅助脚本
│  ├─ setup.sh
│  └─ run-benchmark.sh
│
├─ .gitignore
├─ package.json / pom.xml       # 依赖管理
└─ README.md
```

---

## 各 Agent 的上下文配置

### 1. Architect Agent

**配置文件**: `.claude/context/architect/context.json`

```json
{
  "name": "architect-context",
  "description": "架构师需要看到全局视图",
  "include": [
    "docs/**/*.md",
    "src/**/README.md",
    "src/**/*.java",
    "src/**/*.ts",
    "tests/integration/",
    "package.json",
    "pom.xml",
    ".gitignore"
  ],
  "exclude": [
    "output/**",
    "node_modules/**",
    "target/**",
    "*.class",
    "*.log"
  ],
  "maxFileSize": "500KB",
  "priority": [
    "docs/architecture.md",
    "docs/构想.md",
    "src/**/interfaces/",
    "src/**/README.md"
  ]
}
```

**访问范围**: 全项目视图，重点关注架构文档和模块接口

---

### 2. Game Model Agent

**配置文件**: `.claude/context/game-model/context.json`

```json
{
  "name": "game-model-context",
  "description": "规则建模 Agent 专注游戏逻辑",
  "include": [
    "src/core/cards/**",
    "src/core/game_state/**",
    "src/core/actions/**",
    "src/core/ranges/**",
    "docs/game-rules.md",
    "tests/unit/core/**",
    "tests/integration/toy-games/**"
  ],
  "exclude": [
    "src/solver/**",
    "src/abstraction/**",
    "src/app/**"
  ],
  "priority": [
    "docs/game-rules.md",
    "src/core/game_state/GameState.*",
    "src/core/actions/Action.*"
  ]
}
```

**访问范围**: 只看游戏规则相关代码，不看求解器

---

### 3. Evaluator Agent

**配置文件**: `.claude/context/evaluator/context.json`

```json
{
  "name": "evaluator-context",
  "description": "手牌评估 Agent 专注牌力计算",
  "include": [
    "src/core/cards/**",
    "src/core/evaluator/**",
    "docs/evaluator-algorithm.md",
    "tests/unit/evaluator/**",
    "tests/benchmark/evaluator-bench/**"
  ],
  "exclude": [
    "src/solver/**",
    "src/game_state/**",
    "src/app/**"
  ],
  "priority": [
    "src/core/evaluator/HandEvaluator.*",
    "src/core/evaluator/EquityCalculator.*",
    "tests/benchmark/evaluator-bench/**"
  ]
}
```

**访问范围**: 牌力评估和 equity 计算，加上性能测试

---

### 4. Tree & Abstraction Agent

**配置文件**: `.claude/context/tree-abstraction/context.json`

```json
{
  "name": "tree-abstraction-context",
  "description": "博弈树和抽象层 Agent",
  "include": [
    "src/core/tree/**",
    "src/abstraction/**",
    "src/core/game_state/**",
    "docs/abstraction-plan.md",
    "tests/unit/tree/**",
    "tests/unit/abstraction/**"
  ],
  "exclude": [
    "src/solver/**",
    "src/app/**"
  ],
  "priority": [
    "docs/abstraction-plan.md",
    "src/core/tree/TreeBuilder.*",
    "src/abstraction/bet_sizing/**"
  ]
}
```

**访问范围**: 树结构、信息集、抽象策略

---

### 5. Solver Agent

**配置文件**: `.claude/context/solver/context.json`

```json
{
  "name": "solver-context",
  "description": "CFR 求解器 Agent",
  "include": [
    "src/solver/**",
    "src/core/tree/**",
    "src/core/evaluator/**",
    "docs/cfr-algorithm.md",
    "tests/unit/solver/**",
    "tests/integration/toy-games/**"
  ],
  "exclude": [
    "src/app/**",
    "src/abstraction/**"
  ],
  "priority": [
    "docs/cfr-algorithm.md",
    "src/solver/cfr/**",
    "tests/integration/toy-games/kuhn-poker/**"
  ]
}
```

**访问范围**: CFR 算法实现、树遍历、toy game 验证

---

### 6. Testing & Verification Agent

**配置文件**: `.claude/context/testing/context.json`

```json
{
  "name": "testing-context",
  "description": "测试 Agent 需要访问所有代码",
  "include": [
    "src/**",
    "tests/**",
    "docs/test-plan.md",
    "docs/game-rules.md"
  ],
  "exclude": [
    "output/**",
    "node_modules/**",
    "target/**"
  ],
  "priority": [
    "docs/test-plan.md",
    "tests/integration/toy-games/**",
    "tests/unit/**"
  ]
}
```

**访问范围**: 全部源代码和测试，以便全面验证

---

### 7. Interface Agent

**配置文件**: `.claude/context/interface/context.json`

```json
{
  "name": "interface-context",
  "description": "CLI 和接口层 Agent",
  "include": [
    "src/app/**",
    "src/io/**",
    "config/**",
    "docs/cli-usage.md",
    "tests/integration/**"
  ],
  "exclude": [
    "src/solver/**",
    "src/core/evaluator/**"
  ],
  "priority": [
    "docs/cli-usage.md",
    "src/app/cli/**",
    "config/scenarios/**"
  ]
}
```

**访问范围**: 应用层、IO、配置文件

---

## 共享上下文

某些文档和文件是所有 agent 都需要的：

**配置文件**: `.claude/context/shared/context.json`

```json
{
  "name": "shared-context",
  "description": "所有 Agent 共享的核心文档",
  "include": [
    "docs/构想.md",
    "docs/architecture.md",
    "README.md",
    ".gitignore"
  ],
  "alwaysInclude": true
}
```

---

## 实施步骤

### 第一步：创建目录结构
```bash
mkdir -p .claude/context/{architect,game-model,evaluator,tree-abstraction,solver,testing,interface,shared}
```

### 第二步：复制配置文件
将上述 JSON 配置保存到对应的 `context.json` 文件中

### 第三步：在主配置中引用
在 `.claude/config.json` 中配置每个 agent 使用的上下文：

```json
{
  "subagents": {
    "architect": {
      "context": ["shared", "architect"]
    },
    "game-model": {
      "context": ["shared", "game-model"]
    },
    "evaluator": {
      "context": ["shared", "evaluator"]
    },
    // ... 其他 agent
  }
}
```

---

## 上下文优化建议

### 1. 控制文件大小
- 单个上下文文件夹不超过 10MB
- 优先级高的文件限制在 500KB 以内
- 大型数据文件放在 output/ 目录并排除

### 2. 使用 .gitignore 模式
- 继承项目的 .gitignore 规则
- 额外排除：
  - 编译产物 (*.class, target/, dist/)
  - 依赖目录 (node_modules/, .m2/)
  - 日志文件 (*.log)
  - 输出结果 (output/)

### 3. 动态调整上下文
根据开发阶段调整：
- **早期**: 重点关注文档和接口
- **中期**: 关注具体实现和测试
- **后期**: 关注性能优化和集成

### 4. 避免上下文冲突
- 每个 agent 只关注自己的职责范围
- 跨模块依赖通过接口而非具体实现
- 共享文档统一放在 docs/

---

## 验证上下文配置

### 检查清单
- [ ] 每个 agent 的上下文配置已创建
- [ ] context.json 文件格式正确
- [ ] include/exclude 路径有效
- [ ] 没有包含过大的文件
- [ ] priority 文件确实存在
- [ ] 主配置正确引用各上下文

### 测试方法
```bash
# 让每个 agent 列出它能看到的文件
claude-code agent list-files --agent architect
claude-code agent list-files --agent game-model
# ... 其他 agent
```

---

## 注意事项

1. **上下文不是越多越好**: 过多无关文件会影响理解准确性
2. **定期审查**: 项目演进时及时更新上下文配置
3. **文档优先**: 确保核心文档始终在高优先级
4. **测试代码要包含**: 帮助 agent 理解预期行为
5. **排除干扰**: 第三方库、编译产物不要包含在上下文中
