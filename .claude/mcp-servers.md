# MCP 服务器配置 - 德州扑克 GTO 项目

## MCP (Model Context Protocol) 简介

MCP 允许 Claude Code 通过标准化协议访问外部工具和数据源，扩展 AI 的能力。对于这个项目，我们需要精心选择必要的 MCP 服务器。

---

## 核心 MCP 服务器（必需）

### 1. Filesystem MCP ⭐⭐⭐⭐⭐

**用途**: 文件系统访问和操作

**为什么需要**:
- 所有 agent 都需要读写源代码
- 管理项目文件结构
- 读取配置、导出结果

**安装**:
```bash
npm install -g @modelcontextprotocol/server-filesystem
```

**配置** (`.claude/mcp-config.json`):
```json
{
  "filesystem": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "D:/GTO"],
    "env": {}
  }
}
```

**适用 Agent**: 全部

---

### 2. Git MCP ⭐⭐⭐⭐⭐

**用途**: Git 版本控制操作

**为什么需要**:
- 跟踪代码变更历史
- 创建分支进行实验性开发
- 提交里程碑版本
- 回滚错误修改

**安装**:
```bash
npm install -g @modelcontextprotocol/server-git
```

**配置**:
```json
{
  "git": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-git"],
    "env": {}
  }
}
```

**适用 Agent**:
- Architect (审查变更)
- Testing (回归测试)
- 所有 agent (提交自己的模块)

**推荐工作流**:
```bash
# 每个 agent 在自己的分支工作
git checkout -b feature/architect-setup
git checkout -b feature/game-model-basic
git checkout -b feature/evaluator-impl

# 完成后合并
git merge feature/game-model-basic
```

---

### 3. Memory MCP ⭐⭐⭐⭐

**用途**: 跨会话记忆和知识图谱

**为什么需要**:
- 记住项目的设计决策
- 追踪已解决的问题
- 保存重要的调试信息
- 维护架构演进历史

**安装**:
```bash
npm install -g @modelcontextprotocol/server-memory
```

**配置**:
```json
{
  "memory": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-memory"],
    "env": {}
  }
}
```

**适用 Agent**:
- Architect (记录架构决策)
- Solver (记录收敛问题)
- Testing (记录已知bug)

**使用场景**:
- "记住：我们决定先实现 Kuhn Poker 验证 CFR 正确性"
- "记住：River 子博弈的 exploitability < 0.01 是可接受的"
- "记住：EquityCalculator 在大范围对抗时性能瓶颈在 XXX"

---

## 推荐 MCP 服务器（强烈建议）

### 4. Sequential Thinking MCP ⭐⭐⭐⭐

**用途**: 增强复杂问题的推理能力

**为什么需要**:
- CFR 算法实现需要严密的数学推理
- 博弈树设计需要考虑多层决策
- 状态抽象策略需要权衡多个因素

**安装**:
```bash
npm install -g @modelcontextprotocol/server-sequential-thinking
```

**配置**:
```json
{
  "sequential-thinking": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-sequential-thinking"],
    "env": {}
  }
}
```

**适用 Agent**:
- Architect (复杂架构决策)
- Solver (算法正确性推理)
- Tree & Abstraction (状态空间优化)

---

### 5. Brave Search MCP ⭐⭐⭐⭐

**用途**: 搜索最新技术文档和学术论文

**为什么需要**:
- 查找 CFR/CFR+ 算法最新优化
- 搜索德州扑克 GTO 相关论文
- 查找手牌评估算法实现
- 学习其他 solver 的设计思路

**安装**:
```bash
npm install -g @modelcontextprotocol/server-brave-search
```

**配置**:
```json
{
  "brave-search": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-brave-search"],
    "env": {
      "BRAVE_API_KEY": "${BRAVE_API_KEY}"
    }
  }
}
```

**获取 API Key**:
1. 访问 https://brave.com/search/api/
2. 注册账号
3. 获取免费 API key (每月 2000 次查询)
4. 设置环境变量: `export BRAVE_API_KEY=your_key_here`

**适用 Agent**:
- Solver (查找算法优化)
- Evaluator (查找评估算法)
- Architect (学习最佳实践)

**搜索示例**:
- "CFR+ algorithm poker implementation"
- "hand evaluator optimization techniques"
- "poker game tree abstraction methods"

---

### 6. SQLite MCP ⭐⭐⭐

**用途**: 轻量级数据库存储

**为什么需要**:
- 存储策略训练结果
- 缓存 equity 计算
- 保存实验数据
- 记录收敛历史

**安装**:
```bash
npm install -g @modelcontextprotocol/server-sqlite
```

**配置**:
```json
{
  "sqlite": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-sqlite", "--db-path", "D:/GTO/output/poker-gto.db"],
    "env": {}
  }
}
```

**适用 Agent**:
- Solver (保存策略)
- Evaluator (缓存 equity)
- Testing (基准测试结果)

**数据库设计建议**:
```sql
-- 策略表
CREATE TABLE strategies (
    id INTEGER PRIMARY KEY,
    scenario TEXT,
    infoset TEXT,
    action TEXT,
    probability REAL,
    iteration INTEGER,
    timestamp DATETIME
);

-- Equity 缓存
CREATE TABLE equity_cache (
    range1_hash TEXT,
    range2_hash TEXT,
    board TEXT,
    equity REAL,
    PRIMARY KEY (range1_hash, range2_hash, board)
);

-- 收敛历史
CREATE TABLE convergence_history (
    iteration INTEGER,
    exploitability REAL,
    runtime_ms INTEGER,
    timestamp DATETIME
);
```

---

## 可选 MCP 服务器（根据需要）

### 7. GitHub MCP ⭐⭐⭐

**用途**: GitHub 托管和协作

**何时需要**:
- 项目托管在 GitHub
- 需要创建 Issues/PRs
- 需要 CI/CD 集成

**安装**:
```bash
npm install -g @modelcontextprotocol/server-github
```

**配置**:
```json
{
  "github": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-github"],
    "env": {
      "GITHUB_TOKEN": "${GITHUB_TOKEN}"
    }
  }
}
```

**获取 Token**:
1. GitHub Settings → Developer settings → Personal access tokens
2. 生成 classic token
3. 权限: repo, workflow
4. 设置环境变量: `export GITHUB_TOKEN=ghp_xxx`

---

### 8. Postgres MCP ⭐⭐

**用途**: 高性能关系型数据库

**何时需要**:
- 策略数据量非常大 (> 10GB)
- 需要复杂查询分析
- 多人协作需要共享数据库

**安装**:
```bash
npm install -g @modelcontextprotocol/server-postgres
```

**配置**:
```json
{
  "postgres": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-postgres"],
    "env": {
      "DATABASE_URL": "postgresql://user:pass@localhost:5432/poker_gto"
    }
  }
}
```

**注意**: 第一阶段建议用 SQLite，后期数据量大了再迁移到 Postgres

---

## 暂时不需要的 MCP 服务器

### ❌ Sentry MCP
- **原因**: 项目早期不需要生产环境监控
- **何时添加**: MVP 完成后，有真实用户时

### ❌ Slack MCP
- **原因**: 单人开发，不需要团队通知
- **何时添加**: 多人协作时

### ❌ Puppeteer MCP
- **原因**: 不需要浏览器自动化
- **何时添加**: 如果要做 Web 可视化 UI 的 E2E 测试

---

## 完整配置文件

创建 `.claude/mcp-config.json`:

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "D:/GTO"]
    },
    "git": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-git"]
    },
    "memory": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-memory"]
    },
    "sequential-thinking": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-sequential-thinking"]
    },
    "brave-search": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-brave-search"],
      "env": {
        "BRAVE_API_KEY": "${BRAVE_API_KEY}"
      }
    },
    "sqlite": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-sqlite",
        "--db-path",
        "D:/GTO/output/poker-gto.db"
      ]
    }
  }
}
```

---

## 环境变量配置

创建 `.env` 文件（记得添加到 .gitignore）:

```bash
# Brave Search API
BRAVE_API_KEY=your_brave_api_key_here

# GitHub (可选)
GITHUB_TOKEN=ghp_your_github_token_here

# Database (如果使用 Postgres)
DATABASE_URL=postgresql://user:pass@localhost:5432/poker_gto
```

---

## 安装步骤

### 1. 一键安装所有必需 MCP 服务器

```bash
# 核心服务器
npm install -g @modelcontextprotocol/server-filesystem
npm install -g @modelcontextprotocol/server-git
npm install -g @modelcontextprotocol/server-memory
npm install -g @modelcontextprotocol/server-sequential-thinking

# 推荐服务器
npm install -g @modelcontextprotocol/server-brave-search
npm install -g @modelcontextprotocol/server-sqlite

# 可选服务器（按需安装）
# npm install -g @modelcontextprotocol/server-github
```

### 2. 配置环境变量

```bash
# Windows
setx BRAVE_API_KEY "your_key_here"

# Linux/Mac
echo 'export BRAVE_API_KEY="your_key_here"' >> ~/.bashrc
source ~/.bashrc
```

### 3. 测试 MCP 连接

```bash
# 测试 filesystem
npx -y @modelcontextprotocol/server-filesystem D:/GTO

# 测试 git
cd D:/GTO
npx -y @modelcontextprotocol/server-git

# 测试 memory
npx -y @modelcontextprotocol/server-memory
```

---

## 各 Agent 使用的 MCP 服务器映射

| Agent | Filesystem | Git | Memory | Sequential Thinking | Brave Search | SQLite |
|-------|------------|-----|--------|---------------------|--------------|--------|
| **Architect** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Game Model** | ✅ | ✅ | ❌ | ❌ | ⚠️ | ❌ |
| **Evaluator** | ✅ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ |
| **Tree & Abstraction** | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ |
| **Solver** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Testing** | ✅ | ✅ | ✅ | ❌ | ⚠️ | ✅ |
| **Interface** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |

图例:
- ✅ 强烈推荐
- ⚠️ 可选，根据需要
- ❌ 不需要

---

## 成本估算

### 免费服务
- Filesystem: 免费
- Git: 免费
- Memory: 免费
- Sequential Thinking: 免费
- SQLite: 免费

### 需要 API Key 的服务
- **Brave Search**: 免费层 2000 次/月，足够开发使用
- **GitHub**: 免费

### 总结
✅ **所有推荐的 MCP 服务器都是免费的！**

---

## 使用建议

### 第一阶段 (Milestone 1-2)
只启用核心 MCP:
- Filesystem
- Git
- Memory

### 第二阶段 (Milestone 3-4)
添加增强 MCP:
- Sequential Thinking (复杂算法推理)
- Brave Search (查找优化方法)
- SQLite (保存实验结果)

### 项目成熟后
根据需要添加:
- GitHub (开源发布)
- Postgres (大规模数据)
- Sentry (错误监控)

---

## 故障排查

### MCP 服务器无法启动
```bash
# 检查是否正确安装
npm list -g @modelcontextprotocol/server-filesystem

# 重新安装
npm uninstall -g @modelcontextprotocol/server-filesystem
npm install -g @modelcontextprotocol/server-filesystem
```

### 环境变量未生效
```bash
# 检查环境变量
echo $BRAVE_API_KEY  # Linux/Mac
echo %BRAVE_API_KEY%  # Windows

# 手动设置
export BRAVE_API_KEY="your_key"  # 当前会话
```

### SQLite 数据库锁定
```bash
# 关闭所有访问数据库的进程
# 删除锁文件
rm D:/GTO/output/poker-gto.db-shm
rm D:/GTO/output/poker-gto.db-wal
```

---

## 参考资源

- [MCP 官方文档](https://modelcontextprotocol.io/)
- [MCP 服务器列表](https://github.com/modelcontextprotocol/servers)
- [Brave Search API](https://brave.com/search/api/)
- [Claude Code 文档](https://docs.claude.com/claude-code)
