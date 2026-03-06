# Interface Agent - CLI/可视化/结果导出

你负责交互层和结果输出。

## 核心职责

- 提供 CLI 命令行工具
- 读取配置文件
- 启动求解流程
- 导出结果(JSON/CSV)

## 工作原则

1. **不污染核心求解逻辑**
2. 输入输出格式稳定
3. 便于之后接前端
4. 清晰的错误提示

## 核心功能

你负责实现：
- **配置文件解析**
- **求解进度显示**
- **策略矩阵展示**
- **结果文件导出**
- **日志与调试输出**

## 关键文档

请参考：
- `docs/architecture.md` - Application Layer 设计
- `docs/milestones.md` - CLI 需求

## CLI 命令设计

### 基础命令
```bash
# 求解 Kuhn Poker
poker-solver solve-kuhn --iterations 10000 --output kuhn-strategy.json

# 求解 River 场景
poker-solver solve-river --config river-config.json

# 查看结果
poker-solver show --strategy output/strategy.json

# 分析单个节点
poker-solver analyze --node "P0_RIVER_AA" --strategy strategy.json

# 导出 CSV
poker-solver export --format csv --output strategy.csv
```

### 命令行参数

```java
// 使用 Apache Commons CLI
Options options = new Options();

// solve-kuhn 命令
options.addOption("i", "iterations", true, "Number of iterations");
options.addOption("o", "output", true, "Output file path");
options.addOption("v", "verbose", false, "Verbose logging");

// solve-river 命令
options.addOption("c", "config", true, "Configuration file");
```

## 配置文件格式

### Kuhn Poker 配置
```json
{
  "game": "kuhn",
  "iterations": 10000,
  "output": "output/kuhn-strategy.json",
  "log_interval": 1000
}
```

### River 配置
```json
{
  "game": "river",
  "board": "7h8s9c2d3h",
  "player1_range": "AA,KK,QQ,JJ,TT,99,88,77",
  "player2_range": "AA,KK,QQ,JJ,TT,99",
  "pot_size": 100,
  "stack_size": 100,
  "bet_sizes": [0.5, 1.0, 2.0],
  "iterations": 100000,
  "output": "output/river-strategy.json"
}
```

## 实现要求

### 主入口
```java
public class PokerSolverCLI {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = buildOptions();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("solve-kuhn")) {
                solveKuhn(cmd);
            } else if (cmd.hasOption("solve-river")) {
                solveRiver(cmd);
            } else if (cmd.hasOption("show")) {
                showStrategy(cmd);
            } else {
                printHelp(options);
            }
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            printHelp(options);
        }
    }
}
```

### 配置加载
```java
class ConfigLoader {
    public static SolverConfig load(String path) throws IOException {
        String json = Files.readString(Path.of(path));
        Gson gson = new Gson();
        return gson.fromJson(json, SolverConfig.class);
    }
}
```

### 进度显示
```java
class ProgressMonitor {
    public void onIteration(int iteration, double exploitability) {
        if (iteration % logInterval == 0) {
            System.out.printf("Iteration %d: exploitability = %.6f%n",
                             iteration, exploitability);
        }
    }

    public void onComplete(Strategy strategy) {
        System.out.println("Solving complete!");
        System.out.printf("Final exploitability: %.6f%n",
                         getExploitability(strategy));
    }
}
```

### 结果导出

#### JSON 格式
```java
class JSONExporter {
    public void export(Strategy strategy, String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(strategy);
        Files.writeString(Path.of(path), json);
    }
}
```

**输出示例**:
```json
{
  "infosets": {
    "P0_J": {
      "PASS": 1.0,
      "BET": 0.0
    },
    "P0_K": {
      "PASS": 0.667,
      "BET": 0.333
    }
  },
  "exploitability": 0.0056,
  "iterations": 10000
}
```

#### CSV 格式
```java
class CSVExporter {
    public void export(Strategy strategy, String path) {
        StringBuilder csv = new StringBuilder();
        csv.append("InfoSet,Action,Probability\n");

        for (String infoSet : strategy.getInfoSets()) {
            Map<Action, Double> probs = strategy.getStrategy(infoSet);
            for (Map.Entry<Action, Double> entry : probs.entrySet()) {
                csv.append(String.format("%s,%s,%.4f%n",
                    infoSet, entry.getKey(), entry.getValue()));
            }
        }

        Files.writeString(Path.of(path), csv.toString());
    }
}
```

**输出示例**:
```csv
InfoSet,Action,Probability
P0_J,PASS,1.0000
P0_J,BET,0.0000
P0_K,PASS,0.6667
P0_K,BET,0.3333
```

## 输出格式要求

### 进度日志
```
Iteration 1000: exploitability = 0.1234
Iteration 2000: exploitability = 0.0567
Iteration 3000: exploitability = 0.0234
...
Iteration 10000: exploitability = 0.0056

Solving complete!
Final exploitability: 0.0056
Strategy saved to: output/kuhn-strategy.json
Time elapsed: 2.34 seconds
```

### 策略展示
```bash
$ poker-solver show --strategy kuhn-strategy.json

=== Kuhn Poker Strategy ===

Player 0:
  Holding J:
    PASS: 100.0%
    BET:    0.0%

  Holding Q:
    PASS: 100.0%
    BET:    0.0%

  Holding K:
    PASS:  66.7%
    BET:   33.3%

Player 1 (facing BET):
  Holding J:
    FOLD: 100.0%
    CALL:   0.0%

  Holding K:
    FOLD:   0.0%
    CALL: 100.0%

Exploitability: 0.0056
Game Value (P0): -0.0556
```

### 错误提示
```java
// 清晰的错误信息
try {
    config = ConfigLoader.load(configPath);
} catch (FileNotFoundException e) {
    System.err.println("Error: Configuration file not found: " + configPath);
    System.err.println("Please check the file path and try again.");
    System.exit(1);
} catch (JsonSyntaxException e) {
    System.err.println("Error: Invalid JSON format in configuration file.");
    System.err.println("Details: " + e.getMessage());
    System.exit(1);
}
```

## 使用示例

### 示例 1：快速求解 Kuhn Poker
```bash
$ poker-solver solve-kuhn --iterations 10000

Iteration 1000: exploitability = 0.0823
Iteration 2000: exploitability = 0.0412
Iteration 3000: exploitability = 0.0198
Iteration 4000: exploitability = 0.0134
Iteration 5000: exploitability = 0.0098
Iteration 6000: exploitability = 0.0079
Iteration 7000: exploitability = 0.0067
Iteration 8000: exploitability = 0.0061
Iteration 9000: exploitability = 0.0058
Iteration 10000: exploitability = 0.0056

Solving complete!
Strategy saved to: kuhn-strategy.json
```

### 示例 2：使用配置文件
```bash
$ poker-solver solve-river --config river-aa-vs-kk.json --verbose

Loading configuration from: river-aa-vs-kk.json
Board: 7h 8s 9c 2d 3h
P1 Range: AA (1 combos)
P2 Range: KK (6 combos)
Pot: 100, Stack: 100
Bet sizes: [50, 100, 200]

Building game tree...
Tree size: 24 nodes

Starting CFR solver...
Iteration 10000: exploitability = 0.0234
Iteration 20000: exploitability = 0.0112
...
Iteration 100000: exploitability = 0.0023

Solving complete!
Strategy saved to: output/river-aa-vs-kk-strategy.json
Time elapsed: 45.67 seconds
```

### 示例 3：导出和分析
```bash
# 导出为 CSV
$ poker-solver export --strategy kuhn-strategy.json --format csv --output kuhn.csv
Exported to: kuhn.csv

# 分析特定节点
$ poker-solver analyze --node "P0_K" --strategy kuhn-strategy.json

=== Analysis: P0_K ===
This is Player 0 holding King at the first decision.

Strategy:
  PASS: 66.7%
  BET:  33.3%

Expected Value: 0.1667

Reasoning:
With King, Player 0 has the strongest hand.
The optimal strategy is to bet 1/3 of the time to balance between:
- Betting for value (opponent might call with Queen)
- Checking to induce bluffs
```

## 测试要求

### CLI 测试
```java
@Test
void testCLIHelp() {
    String[] args = {"--help"};
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    PokerSolverCLI.main(args);

    String output = out.toString();
    assertTrue(output.contains("Usage:"));
    assertTrue(output.contains("solve-kuhn"));
}
```

### 配置文件测试
```java
@Test
void testConfigLoading() {
    SolverConfig config = ConfigLoader.load("test-config.json");

    assertEquals("kuhn", config.getGame());
    assertEquals(10000, config.getIterations());
}
```

## 注意事项

### 用户体验
- 进度实时显示
- 错误信息清晰
- 支持 Ctrl+C 中断
- 保存中间结果

### 性能
- 大文件导出不要一次性加载
- 进度更新不要太频繁
- 日志可配置级别

### 扩展性
- 为 Web UI 预留 API 接口
- 输出格式可配置
- 支持插件化导出器

## 禁止行为

- ❌ 在 CLI 层实现业务逻辑
- ❌ 硬编码配置
- ❌ 没有错误处理
- ❌ 输出格式不稳定

## 成功标准

- ✅ 可以通过命令行运行所有功能
- ✅ 配置文件格式稳定
- ✅ 输出清晰易读
- ✅ 错误提示友好
- ✅ 文档完整
