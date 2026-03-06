# 测试计划

**版本**: 0.1.0
**更新日期**: 2026-03-06
**负责 Agent**: Testing

---

## 1. 测试策略

### 1.1 测试金字塔

```
         /\
        /  \  E2E Tests (端到端测试)
       /____\
      /      \
     / Integr \  Integration Tests (集成测试)
    /__________\
   /            \
  /    Unit      \  Unit Tests (单元测试)
 /________________\
```

**比例建议**:
- 单元测试: 70%
- 集成测试: 20%
- 端到端测试: 10%

### 1.2 测试原则

1. **测试先行**: 先写测试，再写实现
2. **独立性**: 测试之间不相互依赖
3. **可重复**: 多次运行结果一致
4. **快速**: 单元测试 < 1s，集成测试 < 10s
5. **清晰**: 测试名称说明测试目的

---

## 2. 单元测试

### 2.1 Cards Module

**测试文件**: `tests/unit/core/cards/`

#### CardTest.java

```java
class CardTest {
    @Test
    void testCardCreation() {
        Card card = new Card(Rank.ACE, Suit.SPADES);
        assertEquals(Rank.ACE, card.getRank());
        assertEquals(Suit.SPADES, card.getSuit());
    }

    @Test
    void testCardToString() {
        Card card = new Card(Rank.KING, Suit.HEARTS);
        assertEquals("Kh", card.toString());
    }

    @Test
    void testCardEquality() {
        Card c1 = new Card(Rank.ACE, Suit.SPADES);
        Card c2 = new Card(Rank.ACE, Suit.SPADES);
        assertEquals(c1, c2);
    }

    @Test
    void testCardImmutability() {
        Card card = new Card(Rank.QUEEN, Suit.DIAMONDS);
        // 应该没有 setter 方法
        assertThrows(NoSuchMethodException.class, () -> {
            card.getClass().getMethod("setRank", Rank.class);
        });
    }
}
```

#### DeckTest.java

```java
class DeckTest {
    @Test
    void testDeckSize() {
        Deck deck = new Deck();
        assertEquals(52, deck.size());
    }

    @Test
    void testDeckDeal() {
        Deck deck = new Deck();
        Card card = deck.deal();
        assertNotNull(card);
        assertEquals(51, deck.size());
    }

    @Test
    void testDeckShuffle() {
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        deck2.shuffle();

        // 洗牌后顺序应该不同（概率极高）
        List<Card> cards1 = deck1.deal(10);
        List<Card> cards2 = deck2.deal(10);
        assertNotEquals(cards1, cards2);
    }

    @Test
    void testDeckReset() {
        Deck deck = new Deck();
        deck.deal(10);
        deck.reset();
        assertEquals(52, deck.size());
    }
}
```

---

### 2.2 Evaluator Module

**测试文件**: `tests/unit/core/evaluator/`

#### KuhnEvaluatorTest.java

```java
class KuhnEvaluatorTest {
    private KuhnEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new KuhnEvaluator();
    }

    @Test
    void testJackVsQueen() {
        Card jack = Card.JACK;
        Card queen = Card.QUEEN;
        assertTrue(evaluator.compare(jack, queen) < 0);
    }

    @Test
    void testQueenVsKing() {
        Card queen = Card.QUEEN;
        Card king = Card.KING;
        assertTrue(evaluator.compare(queen, king) < 0);
    }

    @Test
    void testKingVsJack() {
        Card king = Card.KING;
        Card jack = Card.JACK;
        assertTrue(evaluator.compare(king, jack) > 0);
    }

    @Test
    void testPayoffCheckCheck() {
        // J vs Q, both check
        int payoff = evaluator.calculatePayoff(Card.JACK, Card.QUEEN, "pass-pass");
        assertEquals(-1, payoff);
    }

    @Test
    void testPayoffBetFold() {
        // Any card, opponent folds
        int payoff = evaluator.calculatePayoff(Card.JACK, Card.KING, "bet-fold");
        assertEquals(1, payoff);
    }

    @Test
    void testPayoffBetCall() {
        // K vs Q, bet-call
        int payoff = evaluator.calculatePayoff(Card.KING, Card.QUEEN, "bet-call");
        assertEquals(2, payoff);
    }
}
```

#### HandEvaluatorTest.java

```java
class HandEvaluatorTest {
    private HandEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FastHandEvaluator();
    }

    @Test
    void testRoyalFlush() {
        List<Card> cards = parseCards("AsKsQsJsTs");
        EvaluationResult result = evaluator.evaluate(cards);
        assertEquals(HandRank.ROYAL_FLUSH, result.getRank());
    }

    @Test
    void testStraightFlush() {
        List<Card> cards = parseCards("9h8h7h6h5h");
        EvaluationResult result = evaluator.evaluate(cards);
        assertEquals(HandRank.STRAIGHT_FLUSH, result.getRank());
    }

    @Test
    void testFourOfAKind() {
        List<Card> cards = parseCards("KsKhKdKcAh");
        EvaluationResult result = evaluator.evaluate(cards);
        assertEquals(HandRank.FOUR_OF_A_KIND, result.getRank());
    }

    @Test
    void testFullHouse() {
        List<Card> cards = parseCards("QsQhQd7s7h");
        EvaluationResult result = evaluator.evaluate(cards);
        assertEquals(HandRank.FULL_HOUSE, result.getRank());
    }

    @Test
    void testFlush() {
        List<Card> cards = parseCards("AdJd9d6d4d");
        EvaluationResult result = evaluator.evaluate(cards);
        assertEquals(HandRank.FLUSH, result.getRank());
    }

    @Test
    void testCompareFullHouseVsFlush() {
        List<Card> fullHouse = parseCards("QsQhQd7s7h");
        List<Card> flush = parseCards("AdJd9d6d4d");

        EvaluationResult r1 = evaluator.evaluate(fullHouse);
        EvaluationResult r2 = evaluator.evaluate(flush);

        assertTrue(r1.compareTo(r2) > 0);
    }

    @Test
    void test7CardEvaluation() {
        // 7张牌，应该选出最好的5张
        List<Card> sevenCards = parseCards("AsKsQsJsTs2h3d");
        EvaluationResult result = evaluator.evaluate(sevenCards);
        assertEquals(HandRank.ROYAL_FLUSH, result.getRank());
    }
}
```

---

### 2.3 Game State Module

**测试文件**: `tests/unit/core/game_state/`

#### KuhnPokerStateTest.java

```java
class KuhnPokerStateTest {
    @Test
    void testInitialState() {
        KuhnPokerState state = new KuhnPokerState();
        assertEquals(2, state.getPotSize());  // 2 antes
        assertEquals(0, state.getCurrentPlayer());
        assertFalse(state.isTerminal());
    }

    @Test
    void testPassPass() {
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);
        state.applyAction(Action.PASS);
        state.applyAction(Action.PASS);

        assertTrue(state.isTerminal());
        assertEquals(-1, state.getPayoff(0));
        assertEquals(1, state.getPayoff(1));
    }

    @Test
    void testBetFold() {
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.KING);
        state.applyAction(Action.BET);
        state.applyAction(Action.FOLD);

        assertTrue(state.isTerminal());
        assertEquals(1, state.getPayoff(0));
    }

    @Test
    void testBetCall() {
        KuhnPokerState state = new KuhnPokerState(Card.KING, Card.QUEEN);
        state.applyAction(Action.BET);
        state.applyAction(Action.CALL);

        assertTrue(state.isTerminal());
        assertEquals(2, state.getPayoff(0));
    }

    @Test
    void testPassBetFold() {
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);
        state.applyAction(Action.PASS);
        state.applyAction(Action.BET);
        state.applyAction(Action.FOLD);

        assertTrue(state.isTerminal());
        assertEquals(-1, state.getPayoff(0));
    }

    @Test
    void testClone() {
        KuhnPokerState state1 = new KuhnPokerState(Card.KING, Card.QUEEN);
        state1.applyAction(Action.BET);

        KuhnPokerState state2 = state1.clone();
        state2.applyAction(Action.CALL);

        // state1 不应该改变
        assertFalse(state1.isTerminal());
        assertTrue(state2.isTerminal());
    }
}
```

---

### 2.4 CFR Solver Module

**测试文件**: `tests/unit/solver/cfr/`

#### RegretTableTest.java

```java
class RegretTableTest {
    @Test
    void testGetRegretDefaultZero() {
        RegretTable table = new RegretTable();
        double regret = table.getRegret("test", Action.BET);
        assertEquals(0.0, regret);
    }

    @Test
    void testUpdateRegret() {
        RegretTable table = new RegretTable();
        table.updateRegret("test", Action.BET, 0.5);
        assertEquals(0.5, table.getRegret("test", Action.BET));

        table.updateRegret("test", Action.BET, 0.3);
        assertEquals(0.8, table.getRegret("test", Action.BET));
    }

    @Test
    void testNegativeRegret() {
        RegretTable table = new RegretTable();
        table.updateRegret("test", Action.FOLD, -0.5);
        assertEquals(-0.5, table.getRegret("test", Action.FOLD));
    }
}
```

#### StrategyTest.java

```java
class StrategyTest {
    @Test
    void testStrategyNormalization() {
        Strategy strategy = new Strategy();
        strategy.setProbability("test", Action.BET, 0.5);
        strategy.setProbability("test", Action.PASS, 0.5);

        Map<Action, Double> probs = strategy.getStrategy("test");
        double sum = probs.values().stream().mapToDouble(d -> d).sum();
        assertEquals(1.0, sum, 1e-6);
    }

    @Test
    void testUniformStrategy() {
        Strategy strategy = new Strategy();
        List<Action> actions = Arrays.asList(Action.PASS, Action.BET);

        Map<Action, Double> uniform = strategy.getUniformStrategy(actions);
        assertEquals(0.5, uniform.get(Action.PASS), 1e-6);
        assertEquals(0.5, uniform.get(Action.BET), 1e-6);
    }
}
```

---

## 3. 集成测试

### 3.1 Kuhn Poker 集成测试

**测试文件**: `tests/integration/KuhnPokerIntegrationTest.java`

```java
class KuhnPokerIntegrationTest {
    @Test
    void testCFRConvergence() {
        // 构建博弈树
        TreeBuilder builder = new KuhnTreeBuilder();
        TreeNode root = builder.buildTree();

        // 运行 CFR
        CFRSolver solver = new VanillaCFR(2);
        Strategy strategy = solver.solve(root, 10000);

        // 验证收敛
        double exploitability = solver.getExploitability(strategy);
        assertTrue(exploitability < 0.01,
                   "Exploitability should be < 0.01, got: " + exploitability);
    }

    @Test
    void testStrategyQuality() {
        TreeBuilder builder = new KuhnTreeBuilder();
        TreeNode root = builder.buildTree();

        CFRSolver solver = new VanillaCFR(2);
        Strategy strategy = solver.solve(root, 10000);

        // 验证玩家1持J的策略（应该总是 pass）
        Map<Action, Double> p0Jack = strategy.getStrategy("P0_J");
        assertTrue(p0Jack.get(Action.PASS) > 0.95,
                   "P0 with Jack should almost always pass");

        // 验证玩家2持K面对bet的策略（应该总是 call）
        Map<Action, Double> p1KingBet = strategy.getStrategy("P1_K_bet");
        assertTrue(p1KingBet.get(Action.CALL) > 0.95,
                   "P1 with King should almost always call");
    }

    @Test
    void testGameValue() {
        TreeBuilder builder = new KuhnTreeBuilder();
        TreeNode root = builder.buildTree();

        CFRSolver solver = new VanillaCFR(2);
        Strategy strategy = solver.solve(root, 10000);

        double gameValue = solver.getGameValue(strategy, 0);

        // 已知 Kuhn Poker 的博弈价值: -1/18 for P0
        assertEquals(-1.0/18.0, gameValue, 0.01);
    }
}
```

---

### 3.2 River 求解器集成测试

**测试文件**: `tests/integration/RiverSolverTest.java`

```java
class RiverSolverTest {
    @Test
    void testSimpleRiverScenario() {
        // 设置场景
        List<Card> board = parseCards("7h8s9c2d3h");
        Range p1Range = Range.parse("AA,KK,QQ");
        Range p2Range = Range.parse("JJ,TT,99");

        RiverSolver solver = new RiverSolver();
        solver.setBoard(board);
        solver.setRanges(p1Range, p2Range);
        solver.setBetSizes(Arrays.asList(0.5, 1.0));

        // 运行求解
        Strategy strategy = solver.solve(100000);

        // 验证收敛
        double exploitability = solver.getExploitability(strategy);
        assertTrue(exploitability < 0.05);
    }

    @Test
    void testPolarizedVsBluffCatcher() {
        // P1: nuts 或 air (极化范围)
        // P2: bluff catcher (抓诈唬牌)
        List<Card> board = parseCards("As8s7s6s2c");
        Range p1Range = Range.parse("KsQs,KsJs,22"); // 同花 + 暗三
        Range p2Range = Range.parse("AhKh,AhQh,AhJh"); // 顶对

        RiverSolver solver = new RiverSolver();
        solver.setBoard(board);
        solver.setRanges(p1Range, p2Range);

        Strategy strategy = solver.solve(100000);

        // P1 应该用 nuts 下注，用 air 混合 bet/check
        // P2 应该用 bluff catcher 混合 call/fold
        assertStrategySensible(strategy);
    }
}
```

---

## 4. 性能测试

### 4.1 Evaluator 性能测试

**测试文件**: `tests/benchmark/EvaluatorBenchmark.java`

```java
class EvaluatorBenchmark {
    @Test
    void benchmarkHandEvaluation() {
        HandEvaluator evaluator = new FastHandEvaluator();
        List<Card> cards = parseCards("AsKsQsJsTs");

        long startTime = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            evaluator.evaluate(cards);
        }
        long endTime = System.nanoTime();

        long avgTime = (endTime - startTime) / 1_000_000;
        System.out.printf("Avg evaluation time: %d ns%n", avgTime);

        // 目标: < 100 ns per evaluation
        assertTrue(avgTime < 100);
    }

    @Test
    void benchmarkEquityCalculation() {
        EquityCalculator calculator = new MonteCarloEquityCalculator(10000);
        List<Card> holeCards = parseCards("AsKs");
        List<Card> board = parseCards("7h8s9c");

        long startTime = System.currentTimeMillis();
        double equity = calculator.calculateEquity(holeCards, board, 1);
        long endTime = System.currentTimeMillis();

        System.out.printf("Equity calculation time: %d ms%n", endTime - startTime);

        // 目标: < 100 ms
        assertTrue((endTime - startTime) < 100);
    }
}
```

---

### 4.2 CFR 性能测试

**测试文件**: `tests/benchmark/CFRBenchmark.java`

```java
class CFRBenchmark {
    @Test
    void benchmarkKuhnPokerSolve() {
        TreeBuilder builder = new KuhnTreeBuilder();
        TreeNode root = builder.buildTree();

        CFRSolver solver = new VanillaCFR(2);

        long startTime = System.currentTimeMillis();
        solver.solve(root, 10000);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.printf("Kuhn Poker solve time: %d ms%n", duration);

        // 目标: < 10 秒
        assertTrue(duration < 10000);
    }

    @Test
    void benchmarkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        // 运行求解
        TreeBuilder builder = new KuhnTreeBuilder();
        TreeNode root = builder.buildTree();
        CFRSolver solver = new VanillaCFR(2);
        solver.solve(root, 10000);

        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memUsed = (memAfter - memBefore) / 1024 / 1024;

        System.out.printf("Memory used: %d MB%n", memUsed);

        // 目标: < 100 MB
        assertTrue(memUsed < 100);
    }
}
```

---

## 5. 端到端测试

### 5.1 CLI 端到端测试

**测试文件**: `tests/e2e/CLIEndToEndTest.java`

```java
class CLIEndToEndTest {
    @Test
    void testKuhnPokerCLI() throws Exception {
        // 运行 CLI 命令
        Process process = Runtime.getRuntime().exec(
            "java -jar poker-solver.jar solve-kuhn --iterations 10000 --output test-output.json"
        );

        int exitCode = process.waitFor();
        assertEquals(0, exitCode);

        // 验证输出文件
        File outputFile = new File("test-output.json");
        assertTrue(outputFile.exists());

        // 解析策略
        Strategy strategy = JSONParser.parseStrategy(outputFile);
        assertNotNull(strategy);

        // 验证策略质量
        assertTrue(strategy.getExploitability() < 0.01);
    }

    @Test
    void testConfigFileParsing() throws Exception {
        // 创建配置文件
        String config = """
            {
              "game": "kuhn",
              "iterations": 5000,
              "output": "kuhn-strategy.json"
            }
            """;
        Files.writeString(Path.of("test-config.json"), config);

        // 运行求解
        Process process = Runtime.getRuntime().exec(
            "java -jar poker-solver.jar --config test-config.json"
        );

        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
    }
}
```

---

## 6. 特殊测试

### 6.1 已知均衡验证

**测试文件**: `tests/verification/KnownEquilibriumTest.java`

```java
class KnownEquilibriumTest {
    @Test
    void testKuhnPokerEquilibrium() {
        TreeBuilder builder = new KuhnTreeBuilder();
        TreeNode root = builder.buildTree();

        CFRSolver solver = new VanillaCFR(2);
        Strategy computed = solver.solve(root, 50000);

        // 加载已知均衡策略
        Strategy known = loadKuhnEquilibrium();

        // 比较策略
        double difference = compareStrategies(computed, known);
        assertTrue(difference < 0.05,
                   "Strategy should be within 5% of known equilibrium");
    }

    private Strategy loadKuhnEquilibrium() {
        // Kuhn (1950) 的已知均衡
        Strategy strategy = new Strategy();

        // P0 (first player)
        strategy.setProbability("P0_J", Action.PASS, 1.0);
        strategy.setProbability("P0_Q", Action.PASS, 1.0);
        strategy.setProbability("P0_K", Action.BET, 1.0/3.0);
        strategy.setProbability("P0_K", Action.PASS, 2.0/3.0);

        // ... 其他信息集

        return strategy;
    }
}
```

---

### 6.2 Toy Game 回归测试

**测试文件**: `tests/regression/ToyGameRegressionTest.java`

```java
class ToyGameRegressionTest {
    @Test
    void testRockPaperScissors() {
        // 石头剪刀布的纳什均衡是均匀分布
        TreeNode root = buildRPSTree();
        CFRSolver solver = new VanillaCFR(2);
        Strategy strategy = solver.solve(root, 10000);

        Map<Action, Double> p0Strategy = strategy.getStrategy("P0_start");
        assertEquals(1.0/3.0, p0Strategy.get(Action.ROCK), 0.05);
        assertEquals(1.0/3.0, p0Strategy.get(Action.PAPER), 0.05);
        assertEquals(1.0/3.0, p0Strategy.get(Action.SCISSORS), 0.05);
    }

    @Test
    void testPrisonersDilemma() {
        // 囚徒困境的纳什均衡是双方都背叛
        TreeNode root = buildPrisonersDilemmaTree();
        CFRSolver solver = new VanillaCFR(2);
        Strategy strategy = solver.solve(root, 10000);

        Map<Action, Double> p0Strategy = strategy.getStrategy("P0");
        assertTrue(p0Strategy.get(Action.DEFECT) > 0.95);
    }
}
```

---

## 7. 测试覆盖率目标

| 模块 | 覆盖率目标 | 当前覆盖率 |
|------|-----------|-----------|
| Cards | 90% | 0% |
| Evaluator | 85% | 0% |
| Game State | 90% | 0% |
| Actions | 85% | 0% |
| Tree | 80% | 0% |
| CFR Solver | 85% | 0% |
| Abstraction | 75% | 0% |
| IO | 70% | 0% |
| CLI | 60% | 0% |

**总体目标**: 80%+

---

## 8. 持续集成

### 8.1 CI 配置 (GitHub Actions)

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v2

      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'

      - name: Run tests
        run: mvn test

      - name: Generate coverage report
        run: mvn jacoco:report

      - name: Upload coverage
        uses: codecov/codecov-action@v2
```

---

## 9. 测试工具

### 9.1 测试框架
- **JUnit 5**: 单元测试和集成测试
- **Mockito**: Mock 对象
- **JMH**: 性能测试（可选）

### 9.2 覆盖率工具
- **JaCoCo**: Java 代码覆盖率

### 9.3 断言库
- **AssertJ**: 流式断言（可选）

---

## 10. 测试最佳实践

### 10.1 命名约定

```java
// 格式: test[被测方法]_[场景]_[预期结果]

@Test
void testEvaluate_RoyalFlush_ReturnsHighestRank() { ... }

@Test
void testApplyAction_BetWhenNoChips_ThrowsException() { ... }

@Test
void testCFR_AfterThousandIterations_Converges() { ... }
```

### 10.2 AAA 模式

```java
@Test
void testExample() {
    // Arrange (准备)
    Card card = new Card(Rank.ACE, Suit.SPADES);
    HandEvaluator evaluator = new FastHandEvaluator();

    // Act (执行)
    EvaluationResult result = evaluator.evaluate(card);

    // Assert (断言)
    assertEquals(HandRank.HIGH_CARD, result.getRank());
}
```

### 10.3 测试独立性

```java
// ❌ 错误：测试之间共享状态
private static int counter = 0;

@Test
void test1() {
    counter++;
    assertEquals(1, counter);
}

@Test
void test2() {
    counter++;
    assertEquals(2, counter);  // 依赖 test1 先运行
}

// ✅ 正确：每个测试独立
@Test
void test1() {
    int localCounter = 0;
    localCounter++;
    assertEquals(1, localCounter);
}

@Test
void test2() {
    int localCounter = 0;
    localCounter++;
    assertEquals(1, localCounter);
}
```

---

## 11. 下一步

1. ✅ 测试计划文档完成
2. ⏳ 设置测试框架（JUnit 5）
3. ⏳ 编写第一个测试（CardTest）
4. ⏳ 实现对应功能
5. ⏳ 持续迭代测试和实现

---

**文档维护者**: Testing Agent
**审核者**: 全体 Agents
**最后更新**: 2026-03-06
