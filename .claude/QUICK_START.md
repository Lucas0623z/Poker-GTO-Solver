# 快速开始指南 - 德州扑克 GTO 项目

## 🎯 项目概览

这是一个基于 **CFR（Counterfactual Regret Minimization）** 算法的德州扑克 GTO 求解器项目。

**关键特点**:
- ✅ 不依赖大模型训练，纯算法求解
- ✅ 模块化架构，7个专职 subagent 协作
- ✅ 从 toy game 逐步扩展到完整 Hold'em
- ✅ 完整的测试验证体系

---

## 📋 前置要求

### 必需软件
- Node.js >= 18.0
- Java JDK >= 17（或 TypeScript/Rust，根据你的技术选型）
- Git
- Claude Code CLI

### 可选但推荐
- Maven（Java 项目）或 npm（TypeScript 项目）
- SQLite Browser（查看数据库）
- IDE（IntelliJ IDEA / VS Code）

---

## 🚀 第一步：环境设置

### 1. 安装 Claude Code（已完成）
```bash
npm i -g @anthropic-ai/claude-code
```

### 2. 安装 MCP 服务器
```bash
# 核心 MCP（必需）
npm install -g @modelcontextprotocol/server-filesystem
npm install -g @modelcontextprotocol/server-git
npm install -g @modelcontextprotocol/server-memory

# 增强 MCP（推荐）
npm install -g @modelcontextprotocol/server-sequential-thinking
npm install -g @modelcontextprotocol/server-brave-search
npm install -g @modelcontextprotocol/server-sqlite
```

### 3. 配置环境变量

创建 `D:\GTO\.env` 文件：
```bash
# Brave Search API（可选，用于搜索算法文档）
BRAVE_API_KEY=your_brave_api_key_here

# GitHub Token（可选，如果要托管到 GitHub）
GITHUB_TOKEN=ghp_your_token_here
```

**获取 Brave API Key**:
1. 访问 https://brave.com/search/api/
2. 注册账号（免费）
3. 获取 API key（每月 2000 次免费）

### 4. 设置环境变量（Windows）
```bash
setx BRAVE_API_KEY "your_key_here"
```

---

## 📁 第二步：创建项目结构

### 1. 初始化 Git 仓库
```bash
cd D:\GTO
git init
```

### 2. 创建 .gitignore
```bash
# Java
target/
*.class
*.jar
*.war

# Node
node_modules/
package-lock.json

# IDE
.idea/
.vscode/
*.iml

# 输出
output/
*.log

# 环境变量
.env

# 数据库
*.db
*.db-shm
*.db-wal
```

### 3. 创建项目目录
```bash
mkdir -p docs
mkdir -p src/{core,solver,abstraction,io,app}
mkdir -p src/core/{cards,evaluator,ranges,game_state,actions,tree}
mkdir -p src/solver/{cfr,cfr_plus,mccfr,metrics}
mkdir -p src/abstraction/{bet_sizing,hand_buckets,state_filters}
mkdir -p src/io/{config,parser,export}
mkdir -p src/app/{cli,api}
mkdir -p tests/{unit,integration,benchmark}
mkdir -p tests/integration/toy-games/{kuhn-poker,leduc-poker}
mkdir -p config/scenarios
mkdir -p output/{strategies,logs,reports}
```

### 4. 验证 Claude Code 配置
```bash
# 检查配置文件是否存在
ls D:\GTO\.claude\config.json

# 测试 MCP 连接
npx -y @modelcontextprotocol/server-filesystem D:/GTO
```

---

## 🤖 第三步：启动 Subagents

### Agent 启动顺序

#### Phase 1: 项目初始化

**1. 启动 Architect Agent**
```bash
claude-code agent start architect
```

任务：
- [ ] 创建 `docs/architecture.md`
- [ ] 定义模块接口
- [ ] 制定开发路线图
- [ ] 创建 `docs/milestones.md`

**2. 启动 Testing Agent**
```bash
claude-code agent start testing
```

任务：
- [ ] 建立测试框架（JUnit 5 或 Jest）
- [ ] 创建 Kuhn Poker 验证计划
- [ ] 编写基础测试工具类

---

#### Phase 2: 核心模块开发

**3. 并行启动 Game Model 和 Evaluator**

```bash
# 终端 1
claude-code agent start game-model

# 终端 2
claude-code agent start evaluator
```

**Game Model Agent 任务**:
- [ ] 实现 `Card`, `Deck`, `Rank`, `Suit` 类
- [ ] 实现 `GameState`, `PlayerState` 状态机
- [ ] 实现 `Action`, `ActionType`, `ActionGenerator`
- [ ] 编写单元测试

**Evaluator Agent 任务**:
- [ ] 实现 5 卡牌力评估（先简单版本）
- [ ] 实现 7 卡评估（后续优化）
- [ ] 实现 Equity 计算器
- [ ] 性能 benchmark

---

#### Phase 3: 博弈树与求解器

**4. 启动 Tree & Abstraction Agent**
```bash
claude-code agent start tree-abstraction
```

任务：
- [ ] 设计 TreeNode 结构
- [ ] 实现 InfoSet（信息集）
- [ ] 配置 Bet Size Abstraction
- [ ] 生成简单博弈树（Kuhn Poker）

**5. 启动 Solver Agent**
```bash
claude-code agent start solver
```

任务：
- [ ] 实现基础 CFR 算法
- [ ] 在 Kuhn Poker 上验证
- [ ] 计算 Exploitability
- [ ] 输出策略文件

---

#### Phase 4: 接口封装

**6. 启动 Interface Agent**
```bash
claude-code agent start interface
```

任务：
- [ ] 创建 CLI 命令
- [ ] 配置文件解析
- [ ] 结果导出（JSON/CSV）
- [ ] 使用文档

---

## 🧪 第四步：验证开发环境

### 1. 测试 Kuhn Poker 求解

创建测试场景 `config/scenarios/kuhn-poker.json`:
```json
{
  "game": "kuhn-poker",
  "players": 2,
  "iterations": 10000,
  "output": "output/strategies/kuhn-poker-strategy.json"
}
```

运行求解器：
```bash
java -jar target/poker-gto-solver.jar solve --config config/scenarios/kuhn-poker.json
```

**预期结果**: Exploitability < 0.01

### 2. 验证手牌评估器

创建测试 `tests/unit/evaluator/HandEvaluatorTest.java`:
```java
@Test
public void testRoyalFlush() {
    Hand hand = Hand.parse("AsKsQsJsTs");
    assertEquals(HandRank.ROYAL_FLUSH, evaluator.evaluate(hand));
}
```

---

## 📊 开发里程碑

### Milestone 1: Toy Game 验证 ✅
- [ ] CFR 在 Kuhn Poker 上收敛
- [ ] Exploitability < 0.01
- [ ] 策略接近已知均衡

### Milestone 2: River 子博弈 🚧
- [ ] 固定公共牌的 River 求解
- [ ] 范围 vs 范围对抗
- [ ] 输出策略频率矩阵

### Milestone 3: Turn/Flop 扩展 ⏳
- [ ] 添加 Chance Node
- [ ] 实现 MCCFR 抽样
- [ ] 优化状态抽象

### Milestone 4: 完整流程 ⏳
- [ ] Preflop 范围定义
- [ ] 完整 4 街求解
- [ ] 可用的 CLI 工具

---

## 🔍 Agent 协作示例

### 场景：实现 River 求解

**Step 1**: Architect 定义接口
```java
// src/core/tree/TreeNode.java
public interface TreeNode {
    String getInfoSet();
    List<Action> getActions();
    Map<Action, TreeNode> getChildren();
}
```

**Step 2**: Game Model 实现状态
```java
// src/core/game_state/RiverState.java
public class RiverState implements TreeNode {
    private List<Card> board;
    private Range range1;
    private Range range2;
    // ...
}
```

**Step 3**: Evaluator 提供 Equity
```java
// src/core/evaluator/EquityCalculator.java
public double calculateEquity(Range range1, Range range2, List<Card> board) {
    // 计算范围对范围的 equity
}
```

**Step 4**: Solver 运行 CFR
```java
// src/solver/cfr/CFR.java
public Strategy solve(TreeNode root, int iterations) {
    for (int i = 0; i < iterations; i++) {
        cfr(root, 1.0, 1.0);
    }
    return getAverageStrategy();
}
```

**Step 5**: Testing 验证结果
```java
@Test
public void testRiverSolverConvergence() {
    TreeNode river = createRiverScenario();
    Strategy strategy = solver.solve(river, 10000);
    assertTrue(strategy.getExploitability() < 0.05);
}
```

**Step 6**: Interface 导出结果
```bash
poker-solver export --strategy output/river-strategy.json --format csv
```

---

## 📚 文档索引

已创建的配置文档：
- ✅ `.claude/claude-code-subagents.md` - Subagent 定义
- ✅ `.claude/context-folders.md` - 上下文配置
- ✅ `.claude/mcp-servers.md` - MCP 服务器配置
- ✅ `.claude/config.json` - 主配置文件
- ✅ `.claude/QUICK_START.md` - 本文档

待创建的文档（由 Architect Agent 负责）:
- ⏳ `docs/architecture.md` - 架构设计
- ⏳ `docs/game-rules.md` - 德州扑克规则
- ⏳ `docs/cfr-algorithm.md` - CFR 算法说明
- ⏳ `docs/abstraction-plan.md` - 抽象策略
- ⏳ `docs/test-plan.md` - 测试计划
- ⏳ `docs/milestones.md` - 里程碑详细规划

---

## 🐛 常见问题

### Q1: MCP 服务器启动失败
```bash
# 检查安装
npm list -g | grep modelcontextprotocol

# 重新安装
npm install -g @modelcontextprotocol/server-filesystem
```

### Q2: Agent 无法访问文件
- 检查 `.claude/config.json` 中的路径是否正确
- 确认上下文配置的 include/exclude 路径

### Q3: CFR 不收敛
- 增加迭代次数
- 检查 regret 更新公式
- 验证终局 utility 计算是否正确
- 先在 Kuhn Poker 上验证算法

### Q4: 性能太慢
- 使用 Profiler 找到热点
- 优化 Evaluator（缓存结果）
- 考虑使用 MCCFR 抽样
- 粗化状态抽象

---

## 📞 下一步

### 立即开始
1. 运行 Architect Agent，创建架构文档
2. 并行启动 Game Model 和 Evaluator Agent
3. 在 Kuhn Poker 上验证 CFR 实现

### 推荐阅读
- [CFR 原始论文](http://modelai.gettysburg.edu/2013/cfr/cfr.pdf)
- [Poker AI 综述](https://arxiv.org/abs/1701.01724)
- [PioSOLVER 设计思路](https://www.piosolver.com/)

### 寻求帮助
- 查看 `.claude/` 目录下的配置文档
- 让 Architect Agent 解答架构问题
- 让 Testing Agent 帮助调试

---

## ✅ 配置检查清单

开始开发前，确认以下项目：

- [ ] Node.js 和 Java/TypeScript 环境安装
- [ ] Claude Code CLI 安装
- [ ] 6 个核心 MCP 服务器安装完成
- [ ] `.claude/config.json` 配置正确
- [ ] 项目目录结构创建完成
- [ ] Git 仓库初始化
- [ ] .gitignore 配置完成
- [ ] 环境变量设置（如果使用 Brave Search）

---

**祝开发顺利！🎉**

如有问题，请查阅 `.claude/` 目录下的详细文档，或让相应的 Agent 提供帮助。
