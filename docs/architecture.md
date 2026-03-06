# 德州扑克 GTO 模拟器 - 架构设计文档

**版本**: 0.1.0
**更新日期**: 2026-03-06
**状态**: 草案

---

## 1. 架构概览

### 1.1 设计理念

本项目采用**分层架构**，核心原则：

1. **职责分离**: 规则层、评估层、求解层严格分离
2. **接口优先**: 模块间通过接口通信，降低耦合
3. **测试驱动**: 每个模块都可独立测试
4. **渐进式扩展**: 从简单博弈逐步扩展到完整德州扑克

### 1.2 架构分层

```
┌─────────────────────────────────────────┐
│        应用层 (Application Layer)        │
│  CLI / API / 配置解析 / 结果导出         │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        求解层 (Solver Layer)             │
│  CFR / CFR+ / MCCFR / 策略计算          │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        抽象层 (Abstraction Layer)        │
│  博弈树 / 信息集 / Bet抽象 / 状态过滤   │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        核心层 (Core Layer)               │
│  规则引擎 / 手牌评估 / 状态管理          │
└─────────────────────────────────────────┘
```

---

## 2. 核心模块设计

### 2.1 Core Layer - 核心层

#### 2.1.1 Cards Module (扑克牌模块)

**职责**: 表示扑克牌、牌组、牌型

**核心类**:

```java
// 枚举：花色
enum Suit {
    SPADES, HEARTS, DIAMONDS, CLUBS
}

// 枚举：点数
enum Rank {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6),
    SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13), ACE(14);

    private final int value;
}

// 类：单张牌
class Card {
    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit);
    public int getValue();
    public String toString(); // 如 "As", "Kh"
}

// 类：牌组
class Deck {
    private List<Card> cards;

    public Deck();                    // 标准52张牌
    public void shuffle();
    public Card deal();
    public List<Card> deal(int count);
    public void reset();
}

// 类：手牌
class Hand {
    private final List<Card> cards;

    public Hand(List<Card> cards);
    public int size();
    public boolean contains(Card card);
}
```

**设计原则**:
- Card 是不可变对象 (immutable)
- Deck 可变，支持发牌和重置
- Hand 可以表示 2 张底牌或 5/7 张完整手牌

---

#### 2.1.2 Evaluator Module (评估模块)

**职责**: 评估牌力、计算 Equity、判断胜负

**核心类**:

```java
// 枚举：牌型
enum HandRank {
    HIGH_CARD(0),
    ONE_PAIR(1),
    TWO_PAIR(2),
    THREE_OF_A_KIND(3),
    STRAIGHT(4),
    FLUSH(5),
    FULL_HOUSE(6),
    FOUR_OF_A_KIND(7),
    STRAIGHT_FLUSH(8),
    ROYAL_FLUSH(9);

    private final int strength;
}

// 类：评估结果
class EvaluationResult {
    private final HandRank rank;
    private final int score;          // 细粒度比较分数
    private final List<Card> best5;   // 最佳5张牌

    public int compareTo(EvaluationResult other);
}

// 接口：手牌评估器
interface HandEvaluator {
    EvaluationResult evaluate(Hand hand);
    EvaluationResult evaluate(List<Card> holeCards, List<Card> board);
    int compare(Hand hand1, Hand hand2);
}

// 类：快速评估器实现
class FastHandEvaluator implements HandEvaluator {
    // 使用查表法或位运算优化
    @Override
    public EvaluationResult evaluate(Hand hand) {
        // 实现评估逻辑
    }
}

// 接口：Equity 计算器
interface EquityCalculator {
    double calculateEquity(
        List<Card> holeCards,
        List<Card> board,
        int opponents
    );

    double calculateEquity(
        Range myRange,
        Range opponentRange,
        List<Card> board
    );
}

// 类：蒙特卡洛 Equity 计算器
class MonteCarloEquityCalculator implements EquityCalculator {
    private final HandEvaluator evaluator;
    private final int simulations;

    public MonteCarloEquityCalculator(HandEvaluator evaluator, int simulations);

    @Override
    public double calculateEquity(List<Card> holeCards, List<Card> board, int opponents) {
        // 蒙特卡洛模拟
    }
}
```

**性能目标**:
- 单次评估 < 100ns
- 支持批量优化
- 缓存重复计算

---

#### 2.1.3 Game State Module (游戏状态模块)

**职责**: 管理游戏状态、玩家行动、底池

**核心类**:

```java
// 枚举：游戏阶段
enum Street {
    PREFLOP, FLOP, TURN, RIVER
}

// 枚举：玩家位置
enum Position {
    SB, BB, UTG, MP, CO, BTN
}

// 类：玩家状态
class PlayerState {
    private final String id;
    private final Position position;
    private int stack;
    private int invested;           // 本轮已投入
    private List<Card> holeCards;
    private boolean folded;
    private boolean allIn;

    public PlayerState(String id, Position position, int stack);
    public void invest(int amount);
    public void fold();
    public boolean canAct();
}

// 类：底池
class Pot {
    private int mainPot;
    private List<SidePot> sidePots;

    public void addChips(int amount);
    public int getTotal();
    public List<Integer> distribute(List<PlayerState> winners);
}

// 类：游戏状态
class GameState {
    private final List<PlayerState> players;
    private final Deck deck;
    private final Pot pot;
    private Street currentStreet;
    private List<Card> board;
    private int currentPlayerIndex;

    public GameState(List<PlayerState> players, int smallBlind, int bigBlind);

    // 状态查询
    public Street getStreet();
    public List<Card> getBoard();
    public PlayerState getCurrentPlayer();
    public int getPotSize();

    // 状态转移
    public void dealHoleCards();
    public void dealFlop();
    public void dealTurn();
    public void dealRiver();
    public void applyAction(Action action);

    // 终局判定
    public boolean isTerminal();
    public List<PlayerState> getWinners();
    public Map<PlayerState, Integer> getPayoffs();
}
```

---

#### 2.1.4 Actions Module (行动模块)

**职责**: 定义和生成合法行动

**核心类**:

```java
// 枚举：行动类型
enum ActionType {
    FOLD, CHECK, CALL, BET, RAISE, ALL_IN
}

// 类：行动
class Action {
    private final ActionType type;
    private final int amount;        // bet/raise 金额

    public Action(ActionType type);
    public Action(ActionType type, int amount);

    public boolean isFold();
    public boolean isAggressive();   // bet/raise
}

// 接口：行动生成器
interface ActionGenerator {
    List<Action> getLegalActions(GameState state);
}

// 类：标准行动生成器
class StandardActionGenerator implements ActionGenerator {
    @Override
    public List<Action> getLegalActions(GameState state) {
        List<Action> actions = new ArrayList<>();
        PlayerState player = state.getCurrentPlayer();
        int toCall = state.getAmountToCall();

        // Fold（如果需要跟注）
        if (toCall > 0) {
            actions.add(new Action(ActionType.FOLD));
        }

        // Check（如果不需要跟注）
        if (toCall == 0) {
            actions.add(new Action(ActionType.CHECK));
        }

        // Call（如果需要跟注）
        if (toCall > 0 && toCall < player.getStack()) {
            actions.add(new Action(ActionType.CALL));
        }

        // Bet/Raise
        List<Integer> betSizes = getBetSizes(state);
        for (int size : betSizes) {
            if (size <= player.getStack()) {
                ActionType type = toCall == 0 ? ActionType.BET : ActionType.RAISE;
                actions.add(new Action(type, size));
            }
        }

        // All-in
        if (player.getStack() > toCall) {
            actions.add(new Action(ActionType.ALL_IN));
        }

        return actions;
    }

    private List<Integer> getBetSizes(GameState state) {
        // 从抽象配置获取下注尺寸
    }
}
```

---

### 2.2 Abstraction Layer - 抽象层

#### 2.2.1 Tree Module (博弈树模块)

**职责**: 构建博弈树、管理信息集

**核心类**:

```java
// 枚举：节点类型
enum NodeType {
    DECISION,    // 决策节点
    CHANCE,      // 随机节点（发牌）
    TERMINAL     // 终局节点
}

// 接口：树节点
interface TreeNode {
    NodeType getType();
    String getInfoSetKey();          // 信息集标识
    List<Action> getActions();
    TreeNode getChild(Action action);
    boolean isTerminal();
}

// 类：决策节点
class DecisionNode implements TreeNode {
    private final GameState state;
    private final int actingPlayer;
    private final String infoSetKey;
    private final Map<Action, TreeNode> children;

    public DecisionNode(GameState state, int actingPlayer);

    @Override
    public String getInfoSetKey() {
        // 生成信息集key：玩家看不到对手手牌
        // 格式: "P0_RIVER_AhKs_7h8s9c2d3h_bet-call"
        return buildInfoSetKey();
    }

    private String buildInfoSetKey() {
        StringBuilder key = new StringBuilder();
        key.append("P").append(actingPlayer);
        key.append("_").append(state.getStreet());
        key.append("_").append(encodeHoleCards());
        key.append("_").append(encodeBoard());
        key.append("_").append(encodeHistory());
        return key.toString();
    }
}

// 类：随机节点（发牌）
class ChanceNode implements TreeNode {
    private final Street street;
    private final Map<Card, TreeNode> children;  // 每张可能的牌

    @Override
    public NodeType getType() {
        return NodeType.CHANCE;
    }
}

// 类：终局节点
class TerminalNode implements TreeNode {
    private final GameState state;
    private final Map<Integer, Integer> payoffs;  // 玩家 -> 收益

    public TerminalNode(GameState state);

    public int getPayoff(int player) {
        return payoffs.get(player);
    }
}

// 接口：博弈树构建器
interface TreeBuilder {
    TreeNode buildTree(GameState initialState);
}

// 类：完整树构建器（小型博弈）
class FullTreeBuilder implements TreeBuilder {
    private final ActionGenerator actionGenerator;

    @Override
    public TreeNode buildTree(GameState initialState) {
        return buildNode(initialState);
    }

    private TreeNode buildNode(GameState state) {
        if (state.isTerminal()) {
            return new TerminalNode(state);
        }

        if (needsChanceNode(state)) {
            return buildChanceNode(state);
        }

        return buildDecisionNode(state);
    }
}
```

---

#### 2.2.2 Bet Abstraction Module (下注抽象)

**职责**: 限制下注尺寸，控制树规模

**核心类**:

```java
// 接口：下注抽象策略
interface BetAbstraction {
    List<Integer> getBetSizes(GameState state);
}

// 类：固定比例抽象
class FixedProportionAbstraction implements BetAbstraction {
    private final List<Double> proportions;  // 如 [0.5, 1.0, 2.0]

    @Override
    public List<Integer> getBetSizes(GameState state) {
        List<Integer> sizes = new ArrayList<>();
        int pot = state.getPotSize();

        for (double prop : proportions) {
            sizes.add((int)(pot * prop));
        }

        return sizes;
    }
}

// 类：街道相关抽象
class StreetDependentAbstraction implements BetAbstraction {
    private final Map<Street, BetAbstraction> streetAbstractions;

    @Override
    public List<Integer> getBetSizes(GameState state) {
        return streetAbstractions.get(state.getStreet())
                                 .getBetSizes(state);
    }
}
```

---

### 2.3 Solver Layer - 求解层

#### 2.3.1 CFR Module (CFR 算法)

**职责**: 实现 CFR 及其变种

**核心类**:

```java
// 类：策略
class Strategy {
    private final Map<String, Map<Action, Double>> infoSetStrategies;

    public double getProbability(String infoSet, Action action);
    public void setProbability(String infoSet, Action action, double prob);
    public Map<Action, Double> getStrategy(String infoSet);
}

// 类：Regret 表
class RegretTable {
    private final Map<String, Map<Action, Double>> regrets;

    public double getRegret(String infoSet, Action action);
    public void updateRegret(String infoSet, Action action, double regret);
    public double getCumulativeRegret(String infoSet, Action action);
}

// 接口：CFR 求解器
interface CFRSolver {
    Strategy solve(TreeNode root, int iterations);
    double getExploitability(Strategy strategy);
}

// 类：Vanilla CFR
class VanillaCFR implements CFRSolver {
    private final RegretTable regretTable;
    private final Strategy cumulativeStrategy;
    private final int numPlayers;

    public VanillaCFR(int numPlayers);

    @Override
    public Strategy solve(TreeNode root, int iterations) {
        for (int i = 0; i < iterations; i++) {
            for (int player = 0; player < numPlayers; player++) {
                cfr(root, player, new double[]{1.0, 1.0});
            }
        }
        return getAverageStrategy();
    }

    private double cfr(TreeNode node, int player, double[] reach) {
        if (node.isTerminal()) {
            return ((TerminalNode) node).getPayoff(player);
        }

        if (node.getType() == NodeType.CHANCE) {
            return cfrChance(node, player, reach);
        }

        return cfrDecision((DecisionNode) node, player, reach);
    }

    private double cfrDecision(DecisionNode node, int player, double[] reach) {
        String infoSet = node.getInfoSetKey();
        List<Action> actions = node.getActions();

        // 获取当前策略
        Map<Action, Double> strategy = getStrategy(infoSet, actions);

        // 计算每个动作的反事实价值
        Map<Action, Double> actionValues = new HashMap<>();
        double nodeValue = 0.0;

        for (Action action : actions) {
            double[] newReach = reach.clone();

            if (node.getActingPlayer() == player) {
                newReach[player] *= strategy.get(action);
            }

            TreeNode child = node.getChild(action);
            double actionValue = cfr(child, player, newReach);
            actionValues.put(action, actionValue);
            nodeValue += strategy.get(action) * actionValue;
        }

        // 更新 regret
        if (node.getActingPlayer() == player) {
            for (Action action : actions) {
                double regret = actionValues.get(action) - nodeValue;
                double reachProb = 1.0;
                for (int p = 0; p < numPlayers; p++) {
                    if (p != player) {
                        reachProb *= reach[p];
                    }
                }
                regretTable.updateRegret(infoSet, action, regret * reachProb);
            }
        }

        // 累积策略
        if (node.getActingPlayer() == player) {
            for (Action action : actions) {
                cumulativeStrategy.setProbability(
                    infoSet,
                    action,
                    cumulativeStrategy.getProbability(infoSet, action) +
                    reach[player] * strategy.get(action)
                );
            }
        }

        return nodeValue;
    }

    private Map<Action, Double> getStrategy(String infoSet, List<Action> actions) {
        // 使用 Regret Matching 计算策略
        Map<Action, Double> strategy = new HashMap<>();
        double normalizingSum = 0.0;

        for (Action action : actions) {
            double regret = Math.max(0, regretTable.getRegret(infoSet, action));
            strategy.put(action, regret);
            normalizingSum += regret;
        }

        if (normalizingSum > 0) {
            for (Action action : actions) {
                strategy.put(action, strategy.get(action) / normalizingSum);
            }
        } else {
            // 均匀分布
            double uniform = 1.0 / actions.size();
            for (Action action : actions) {
                strategy.put(action, uniform);
            }
        }

        return strategy;
    }

    private Strategy getAverageStrategy() {
        // 归一化累积策略
        Strategy avgStrategy = new Strategy();
        // ... 实现归一化逻辑
        return avgStrategy;
    }
}
```

---

### 2.4 Application Layer - 应用层

#### 2.4.1 CLI Module (命令行接口)

**职责**: 提供用户交互界面

```java
// 类：主命令行
class PokerSolverCLI {
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = buildOptions();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("solve")) {
                runSolver(cmd);
            } else if (cmd.hasOption("analyze")) {
                analyzeStrategy(cmd);
            } else {
                printHelp(options);
            }
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void runSolver(CommandLine cmd) {
        String configFile = cmd.getOptionValue("config");
        SolverConfig config = ConfigLoader.load(configFile);

        // 构建博弈树
        TreeBuilder builder = new FullTreeBuilder();
        TreeNode root = builder.buildTree(config.getInitialState());

        // 运行求解器
        CFRSolver solver = new VanillaCFR(config.getNumPlayers());
        Strategy strategy = solver.solve(root, config.getIterations());

        // 导出结果
        StrategyExporter.export(strategy, config.getOutputPath());
    }
}
```

---

## 3. 数据流

### 3.1 求解流程

```
配置文件
   ↓
[ConfigLoader] → SolverConfig
   ↓
[TreeBuilder] → GameState → TreeNode (博弈树)
   ↓
[CFRSolver] → 迭代执行
   ↓ (每次迭代)
   ├─ 遍历树节点
   ├─ 计算 Regret
   ├─ 更新策略
   └─ 累积平均策略
   ↓
Strategy (纳什均衡策略)
   ↓
[StrategyExporter] → JSON/CSV 文件
```

### 3.2 关键接口依赖

```
CLI
 └─> ConfigLoader
 └─> TreeBuilder
      └─> GameState
      └─> ActionGenerator
           └─> BetAbstraction
 └─> CFRSolver
      └─> RegretTable
      └─> Strategy
      └─> HandEvaluator
 └─> StrategyExporter
```

---

## 4. 技术选型

### 4.1 编程语言

**选择**: Java 17+

**理由**:
- 成熟的生态系统
- 优秀的性能（JIT 编译）
- 丰富的测试框架
- Claude Code 生成 Java 代码稳定

### 4.2 构建工具

**选择**: Maven

**依赖**:
```xml
<dependencies>
    <!-- 测试 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>

    <!-- JSON 处理 -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>

    <!-- CLI 参数解析 -->
    <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>1.6.0</version>
    </dependency>

    <!-- 日志 -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.9</version>
    </dependency>
</dependencies>
```

### 4.3 数据存储

**选择**: SQLite

**用途**:
- 保存训练历史
- 缓存 Equity 计算
- 存储策略文件

---

## 5. 设计原则

### 5.1 接口隔离原则 (ISP)

每个模块通过接口通信，便于替换实现：

```java
interface HandEvaluator { ... }
class FastHandEvaluator implements HandEvaluator { ... }
class PreciseHandEvaluator implements HandEvaluator { ... }
```

### 5.2 单一职责原则 (SRP)

每个类只负责一件事：
- `Card`: 表示一张牌
- `Deck`: 管理牌组
- `HandEvaluator`: 评估牌力
- `CFRSolver`: 运行 CFR 算法

### 5.3 依赖倒置原则 (DIP)

高层模块不依赖低层模块，都依赖抽象：

```java
// ✅ 好的设计
class CFRSolver {
    private final HandEvaluator evaluator;  // 依赖接口
}

// ❌ 坏的设计
class CFRSolver {
    private final FastHandEvaluator evaluator;  // 依赖具体类
}
```

### 5.4 不可变性 (Immutability)

核心数据结构尽量不可变：
- `Card`: 不可变
- `Action`: 不可变
- `EvaluationResult`: 不可变

### 5.5 测试优先

每个模块都要有单元测试：
```
src/core/cards/Card.java
tests/unit/core/cards/CardTest.java

src/solver/cfr/VanillaCFR.java
tests/unit/solver/cfr/VanillaCFRTest.java
```

---

## 6. 性能考虑

### 6.1 热点优化

**预计热点**:
1. HandEvaluator（评估频率极高）
2. TreeNode 遍历
3. Regret 更新
4. InfoSet Key 生成

**优化策略**:
- 使用对象池减少 GC
- 缓存重复计算
- 使用原始类型数组（避免装箱）
- InfoSet Key 使用 StringBuilder

### 6.2 内存优化

- 限制博弈树深度
- 使用状态抽象
- 及时释放不用的节点
- 使用弱引用缓存

---

## 7. 扩展性设计

### 7.1 支持不同博弈

通过接口抽象，支持：
- Kuhn Poker
- Leduc Poker
- Texas Hold'em
- 自定义博弈

### 7.2 支持不同算法

```java
interface CFRSolver {
    Strategy solve(TreeNode root, int iterations);
}

class VanillaCFR implements CFRSolver { ... }
class CFRPlus implements CFRSolver { ... }
class MCCFR implements CFRSolver { ... }
```

### 7.3 支持不同抽象策略

```java
interface BetAbstraction {
    List<Integer> getBetSizes(GameState state);
}

// 可插拔的抽象策略
```

---

## 8. 下一步

1. ✅ 完成架构设计文档
2. ⏳ 创建 CFR 算法详细说明
3. ⏳ 创建 Kuhn Poker 规则文档
4. ⏳ 创建里程碑详细规划
5. ⏳ 开始编写核心代码

---

**文档维护者**: Architect Agent
**审核者**: 待定
**最后更新**: 2026-03-06
