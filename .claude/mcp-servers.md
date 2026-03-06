# MCP 服务器配置 - 德州扑克 GTO 项目

**更新日期**: 2026-03-06
**状态**: ✅ 已安装验证

---

## 安装验证结果

| MCP 服务器 | 版本 | 状态 | 说明 |
|-----------|------|------|------|
| ✅ **filesystem** | 2026.1.14 | 正常 | 核心必需 |
| ✅ **memory** | 2026.1.26 | 正常 | 核心必需 |
| ✅ **sequential-thinking** | 2025.12.18 | 正常 | 强烈推荐 |
| ⚠️ **brave-search** | 0.6.2 | Deprecated (仍可用) | 推荐，需要API Key |
| ⚠️ **github** | 2025.4.8 | Deprecated (仍可用) | 可选，需要Token |
| ⚠️ **postgres** | 0.6.2 | Deprecated (仍可用) | 可选，替代SQLite |
| ❌ **git** | - | 不存在 | 使用 Bash 工具代替 |
| ❌ **sqlite** | - | 不存在 | 使用 Postgres 或文件存储 |

---

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

**状态**: ✅ 已安装 (v2026.1.14)

**配置**:
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

### 2. Memory MCP ⭐⭐⭐⭐

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

**状态**: ✅ 已安装 (v2026.1.26)

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

### 3. Sequential Thinking MCP ⭐⭐⭐⭐

**用途**: 增强复杂问题的推理能力

**为什么需要**:
- CFR 算法实现需要严密的数学推理
- 博弈树设计需要考虑多层决策
- 状态抽象策略需要权衡多个因素

**安装**:
```bash
npm install -g @modelcontextprotocol/server-sequential-thinking
```

**状态**: ✅ 已安装 (v2025.12.18)

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

### 4. Brave Search MCP ⭐⭐⭐⭐ (Deprecated 但仍可用)

**用途**: 搜索最新技术文档和学术论文

**状态**: ⚠️ 已安装 (v0.6.2) - Package deprecated 但功能正常

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
4. 设置环境变量: `setx BRAVE_API_KEY "your_key_here"` (Windows)

**适用 Agent**:
- Solver (查找算法优化)
- Evaluator (查找评估算法)
- Architect (学习最佳实践)

**搜索示例**:
- "CFR+ algorithm poker implementation"
- "hand evaluator optimization techniques"
- "poker game tree abstraction methods"

---

## 可选 MCP 服务器（根据需要）

### 5. GitHub MCP ⭐⭐⭐ (Deprecated 但仍可用)

**用途**: GitHub 托管和协作

**状态**: ⚠️ 已安装 (v2025.4.8) - Package deprecated 但功能正常

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
4. 设置环境变量: `setx GITHUB_TOKEN "ghp_xxx"` (Windows)

---

### 6. Postgres MCP ⭐⭐ (Deprecated 但仍可用)

**用途**: 高性能关系型数据库

**状态**: ⚠️ 已安装 (v0.6.2) - Package deprecated 但功能正常

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

**数据库设计建议**:
```sql
-- 策略表
CREATE TABLE strategies (
    id SERIAL PRIMARY KEY,
    scenario TEXT,
    infoset TEXT,
    action TEXT,
    probability REAL,
    iteration INTEGER,
    timestamp TIMESTAMP
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
    timestamp TIMESTAMP
);
```

---

## ❌ 不可用的 MCP 服务器

### Git MCP (不存在)

**替代方案**: 直接使用 Bash 工具执行 git 命令

**示例**:
```bash
# 通过 Bash 工具执行 git 操作
git status
git add .
git commit -m "message"
git push
```

**优势**: 更直接，无需额外配置

---

### SQLite MCP (不存在)

**替代方案 1**: 使用 Postgres MCP (已安装)

**替代方案 2**: 使用文件存储 (JSON/CSV)

**示例** (JSON 存储):
```java
// 保存策略到 JSON
Gson gson = new Gson();
String json = gson.toJson(strategy);
Files.writeString(Path.of("output/strategy.json"), json);

// 读取策略
String json = Files.readString(Path.of("output/strategy.json"));
Strategy strategy = gson.fromJson(json, Strategy.class);
```

**优势**: 简单，无需数据库配置

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

## 实际可用配置文件

创建 `.claude/mcp-config.json`:

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "D:/GTO"]
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
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "DATABASE_URL": "${DATABASE_URL}"
      }
    }
  }
}
```

---

## 环境变量配置

创建 `.env` 文件（记得添加到 .gitignore）:

```bash
# Brave Search API (可选)
BRAVE_API_KEY=your_brave_api_key_here

# GitHub (可选)
GITHUB_TOKEN=ghp_your_github_token_here

# Database (可选，如果使用 Postgres)
DATABASE_URL=postgresql://user:pass@localhost:5432/poker_gto
```

**设置环境变量 (Windows)**:
```bash
setx BRAVE_API_KEY "your_key_here"
setx GITHUB_TOKEN "ghp_xxx"
setx DATABASE_URL "postgresql://..."
```

---

## 安装步骤（已完成）

### ✅ 已安装的 MCP 服务器

```bash
# 核心服务器（已完成）
npm install -g @modelcontextprotocol/server-filesystem     # ✅ v2026.1.14
npm install -g @modelcontextprotocol/server-memory          # ✅ v2026.1.26
npm install -g @modelcontextprotocol/server-sequential-thinking  # ✅ v2025.12.18

# 推荐服务器（已完成）
npm install -g @modelcontextprotocol/server-brave-search   # ⚠️ v0.6.2 (deprecated)

# 可选服务器（已完成）
npm install -g @modelcontextprotocol/server-github         # ⚠️ v2025.4.8 (deprecated)
npm install -g @modelcontextprotocol/server-postgres       # ⚠️ v0.6.2 (deprecated)
```

### 验证安装

```bash
# 查看已安装的 MCP 服务器
npm list -g --depth=0 | grep @modelcontextprotocol
```

**输出**:
```
├── @modelcontextprotocol/server-brave-search@0.6.2
├── @modelcontextprotocol/server-filesystem@2026.1.14
├── @modelcontextprotocol/server-github@2025.4.8
├── @modelcontextprotocol/server-memory@2026.1.26
├── @modelcontextprotocol/server-postgres@0.6.2
└── @modelcontextprotocol/server-sequential-thinking@2025.12.18
```

---

## 各 Agent 使用的 MCP 服务器映射

| Agent | Filesystem | Memory | Sequential | Brave Search | GitHub | Postgres |
|-------|------------|--------|------------|--------------|--------|----------|
| **Architect** | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ❌ |
| **Game Model** | ✅ | ❌ | ❌ | ❌ | ⚠️ | ❌ |
| **Evaluator** | ✅ | ⚠️ | ⚠️ | ⚠️ | ⚠️ | ⚠️ |
| **Tree & Abstraction** | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ❌ |
| **Solver** | ✅ | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| **Testing** | ✅ | ✅ | ❌ | ❌ | ⚠️ | ⚠️ |
| **Interface** | ✅ | ❌ | ❌ | ❌ | ⚠️ | ⚠️ |

图例:
- ✅ 强烈推荐，必需使用
- ⚠️ 可选，根据需要
- ❌ 不需要

---

## 成本估算

### 免费服务
- Filesystem: ✅ 免费
- Memory: ✅ 免费
- Sequential Thinking: ✅ 免费
- GitHub: ✅ 免费

### 需要 API Key 的服务（免费额度）
- **Brave Search**: 免费层 2000 次/月，足够开发使用

### 需要自建的服务
- **Postgres**: 本地免费，云服务可能收费

### 总结
✅ **所有核心 MCP 服务器都是免费的！**

---

## 使用建议

### 第一阶段 (Milestone 1-2) - 当前推荐

启用核心 MCP:
- ✅ Filesystem（必需）
- ✅ Memory（推荐）
- ✅ Sequential Thinking（推荐）

使用 Bash 工具替代:
- Git 操作（直接用 git 命令）
- 文件存储（JSON/CSV）

### 第二阶段 (Milestone 3-4)

如需额外功能，添加:
- ⚠️ Brave Search（查找优化方法，需要 API Key）
- ⚠️ Postgres（大量数据存储）

### 项目成熟后

根据需要添加:
- ⚠️ GitHub（开源发布，自动化 PR）
- ❌ Sentry（错误监控，如有官方包）
- ❌ Slack（团队协作）

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
# Windows - 检查环境变量
echo %BRAVE_API_KEY%
echo %GITHUB_TOKEN%

# 手动设置（当前会话）
set BRAVE_API_KEY=your_key
set GITHUB_TOKEN=ghp_xxx

# 永久设置
setx BRAVE_API_KEY "your_key"
setx GITHUB_TOKEN "ghp_xxx"

# 重启终端后生效
```

### Deprecated 包的警告

如果看到 "Package no longer supported" 警告，不用担心：
- 包仍然可以正常使用
- 只是官方不再维护
- 可以继续使用，或寻找替代方案

---

## Git 操作替代方案

由于 `@modelcontextprotocol/server-git` 不存在，推荐直接使用 Bash 工具：

```bash
# 查看状态
git status

# 添加文件
git add .

# 提交
git commit -m "message"

# 推送
git push origin main

# 创建分支
git checkout -b feature/new-feature

# 查看历史
git log --oneline

# 查看差异
git diff
```

**优势**:
- 更直接，无需配置
- 支持所有 git 功能
- 性能更好

---

## 参考资源

- [MCP 官方文档](https://modelcontextprotocol.io/)
- [MCP GitHub 仓库](https://github.com/modelcontextprotocol/servers)
- [Brave Search API](https://brave.com/search/api/)
- [Claude Code 文档](https://docs.claude.com/claude-code)
- [npm MCP 包搜索](https://www.npmjs.com/search?q=%40modelcontextprotocol)

---

**文档维护者**: System
**最后验证**: 2026-03-06
**状态**: ✅ 所有推荐包已安装并验证
