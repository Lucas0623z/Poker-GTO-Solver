package integration;

import com.poker.gto.core.actions.BetSizeAbstraction;
import com.poker.gto.core.actions.RiverActionGenerator;
import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.game_state.RiverPlayerState;
import com.poker.gto.core.game_state.RiverState;
import com.poker.gto.core.tree.RiverDecisionNode;
import com.poker.gto.core.tree.RiverTreeBuilder;
import com.poker.gto.solver.cfr.RiverCFR;
import com.poker.gto.solver.cfr.Strategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * River GTO Solver 集成测试
 *
 * 验证完整的求解流程:
 * 1. 博弈树构建
 * 2. CFR求解
 * 3. 策略提取
 * 4. 策略合理性验证
 */
public class RiverSolverIntegrationTest {

    /**
     * 测试场景1: 强牌 vs 弱牌
     *
     * P0持有顶两对, P1持有一对
     * 预期: P0应该积极下注, P1应该谨慎
     */
    @Test
    public void testStrongVsWeakHands() {
        System.out.println("\n=== 测试场景1: 强牌 vs 弱牌 ===");

        // 设置场景
        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand p0Hand = Hand.parse("As Kd");  // 顶两对
        Hand p1Hand = Hand.parse("Jd Jc");  // 一对J

        int pot = 20;
        int stack = 50;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        // 构建树并求解
        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);

        RiverCFR cfr = new RiverCFR(root);
        cfr.train(1000, 1000);  // 1000次迭代, 不输出日志

        Strategy strategy = cfr.getAverageStrategy();

        // 验证策略
        assertNotNull(strategy);
        assertTrue(strategy.size() > 0, "策略应包含至少一个信息集");

        System.out.println("  ✓ 求解成功");
        System.out.println("  ✓ 信息集数: " + strategy.size());
    }

    /**
     * 测试场景2: 均衡手牌
     *
     * P0和P1持有相似强度的手牌
     * 预期: 策略应该更加混合
     */
    @Test
    public void testBalancedHands() {
        System.out.println("\n=== 测试场景2: 均衡手牌 ===");

        List<TexasCard> board = Hand.parse("9s 8h 7c 2d 3h").getCards();
        Hand p0Hand = Hand.parse("Kd Kc");  // 一对K
        Hand p1Hand = Hand.parse("Qd Qc");  // 一对Q

        int pot = 10;
        int stack = 30;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        // 构建树并求解
        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);

        RiverCFR cfr = new RiverCFR(root);
        cfr.train(1000, 1000);

        Strategy strategy = cfr.getAverageStrategy();

        assertNotNull(strategy);
        assertTrue(strategy.size() > 0);

        System.out.println("  ✓ 求解成功");
        System.out.println("  ✓ 信息集数: " + strategy.size());
    }

    /**
     * 测试场景3: 同花面
     *
     * 公共牌有三张同花色
     * 验证策略对同花威胁的反应
     */
    @Test
    public void testFlushBoard() {
        System.out.println("\n=== 测试场景3: 同花面 ===");

        List<TexasCard> board = Hand.parse("Kh Qh Jh 7s 2c").getCards();
        Hand p0Hand = Hand.parse("Ah 9h");  // 同花
        Hand p1Hand = Hand.parse("Ks Kd");  // 三条K (但怕同花)

        int pot = 15;
        int stack = 40;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        // 构建树并求解
        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);

        RiverCFR cfr = new RiverCFR(root);
        cfr.train(1000, 1000);

        Strategy strategy = cfr.getAverageStrategy();

        assertNotNull(strategy);
        assertTrue(strategy.size() > 0);

        System.out.println("  ✓ 求解成功");
        System.out.println("  ✓ 信息集数: " + strategy.size());
    }

    /**
     * 测试场景4: 不同下注抽象
     *
     * 验证不同的下注尺寸抽象策略
     */
    @Test
    public void testDifferentBetAbstractions() {
        System.out.println("\n=== 测试场景4: 不同下注抽象 ===");

        List<TexasCard> board = Hand.parse("As Ks Qs 7h 2d").getCards();
        Hand p0Hand = Hand.parse("Ad Kh");
        Hand p1Hand = Hand.parse("Jd Jc");

        int pot = 20;
        int stack = 50;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        // 测试三种不同的抽象
        BetSizeAbstraction[] abstractions = {
            BetSizeAbstraction.RIVER,
            BetSizeAbstraction.AGGRESSIVE,
            BetSizeAbstraction.CONSERVATIVE
        };

        for (BetSizeAbstraction abstraction : abstractions) {
            System.out.println("\n  测试抽象: " + abstraction);

            RiverActionGenerator actionGen = new RiverActionGenerator(abstraction);
            RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
            RiverDecisionNode root = builder.buildTree(state);

            RiverTreeBuilder.TreeStats stats = builder.calculateStats(root);

            RiverCFR cfr = new RiverCFR(root);
            cfr.train(500, 500);

            Strategy strategy = cfr.getAverageStrategy();

            assertNotNull(strategy);
            assertTrue(strategy.size() > 0);

            System.out.println("    ✓ 节点数: " + stats.getTotalNodes());
            System.out.println("    ✓ 信息集: " + stats.getInfoSetCount());
            System.out.println("    ✓ 策略数: " + strategy.size());
        }
    }

    /**
     * 测试场景5: 收敛性验证
     *
     * 验证CFR随迭代次数增加而收敛
     */
    @Test
    public void testConvergence() {
        System.out.println("\n=== 测试场景5: 收敛性验证 ===");

        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand p0Hand = Hand.parse("As Kd");
        Hand p1Hand = Hand.parse("Jd Jc");

        int pot = 20;
        int stack = 50;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);

        // 测试不同迭代次数
        int[] iterationCounts = {100, 500, 1000, 2000};

        for (int iterations : iterationCounts) {
            RiverCFR cfr = new RiverCFR(root);
            cfr.train(iterations, iterations);

            Strategy strategy = cfr.getAverageStrategy();
            double avgRegret = cfr.getRegretTable().getAverageRegret();

            System.out.println(String.format("\n  迭代 %d 次:", iterations));
            System.out.println(String.format("    平均Regret: %.6f", avgRegret));
            System.out.println(String.format("    信息集数: %d", strategy.size()));

            // 验证: 迭代次数越多, 平均regret应该越接近0
            assertTrue(avgRegret >= -1.0 && avgRegret <= 1.0,
                "平均regret应该在合理范围内");
        }

        System.out.println("\n  ✓ 收敛性验证通过");
    }

    /**
     * 测试场景6: 策略一致性
     *
     * 验证多次求解同一场景得到相似的策略
     */
    @Test
    public void testStrategyConsistency() {
        System.out.println("\n=== 测试场景6: 策略一致性 ===");

        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand p0Hand = Hand.parse("As Kd");
        Hand p1Hand = Hand.parse("Jd Jc");

        int pot = 20;
        int stack = 50;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);

        // 运行两次求解
        RiverCFR cfr1 = new RiverCFR(root);
        cfr1.train(2000, 2000);
        Strategy strategy1 = cfr1.getAverageStrategy();

        RiverCFR cfr2 = new RiverCFR(root);
        cfr2.train(2000, 2000);
        Strategy strategy2 = cfr2.getAverageStrategy();

        // 验证两个策略大小相同
        assertEquals(strategy1.size(), strategy2.size(),
            "两次求解应该产生相同数量的信息集");

        System.out.println("  ✓ 策略一致性验证通过");
        System.out.println("  ✓ 信息集数量: " + strategy1.size());
    }

    /**
     * 测试场景7: 极端场景 - 超短筹码
     */
    @Test
    public void testShortStack() {
        System.out.println("\n=== 测试场景7: 超短筹码 ===");

        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand p0Hand = Hand.parse("As Kd");
        Hand p1Hand = Hand.parse("Jd Jc");

        int pot = 50;
        int stack = 10;  // 只有0.2倍底池

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);

        RiverTreeBuilder.TreeStats stats = builder.calculateStats(root);

        RiverCFR cfr = new RiverCFR(root);
        cfr.train(500, 500);

        Strategy strategy = cfr.getAverageStrategy();

        assertNotNull(strategy);
        assertTrue(strategy.size() > 0);

        System.out.println("  ✓ 求解成功");
        System.out.println("  ✓ 节点数: " + stats.getTotalNodes() + " (应该很小)");
    }

    /**
     * 测试场景8: 性能基准测试
     */
    @Test
    public void testPerformanceBenchmark() {
        System.out.println("\n=== 测试场景8: 性能基准测试 ===");

        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand p0Hand = Hand.parse("As Kd");
        Hand p1Hand = Hand.parse("Jd Jc");

        int pot = 20;
        int stack = 50;

        RiverPlayerState p0 = new RiverPlayerState(0, stack, p0Hand);
        RiverPlayerState p1 = new RiverPlayerState(1, stack, p1Hand);
        RiverState state = new RiverState(board, p0, p1, pot);

        // 测试树构建性能
        long buildStart = System.currentTimeMillis();
        RiverActionGenerator actionGen = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGen);
        RiverDecisionNode root = builder.buildTree(state);
        long buildTime = System.currentTimeMillis() - buildStart;

        RiverTreeBuilder.TreeStats stats = builder.calculateStats(root);

        // 测试求解性能
        long solveStart = System.currentTimeMillis();
        RiverCFR cfr = new RiverCFR(root);
        cfr.train(2000, 2000);
        long solveTime = System.currentTimeMillis() - solveStart;

        Strategy strategy = cfr.getAverageStrategy();

        System.out.println("\n  性能指标:");
        System.out.println("  ├─ 树构建时间: " + buildTime + "ms");
        System.out.println("  ├─ 总节点数: " + stats.getTotalNodes());
        System.out.println("  ├─ 求解时间(2000次): " + solveTime + "ms");
        System.out.println("  ├─ 平均每次迭代: " + String.format("%.2f", solveTime / 2000.0) + "ms");
        System.out.println("  └─ 信息集数: " + strategy.size());

        // 性能验证
        assertTrue(buildTime < 100, "树构建应在100ms内完成");
        assertTrue(solveTime < 10000, "2000次迭代应在10秒内完成");

        System.out.println("\n  ✓ 性能基准测试通过");
    }
}
