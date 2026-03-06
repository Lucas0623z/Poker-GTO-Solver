package com.poker.gto.app.demo;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.actions.BetSizeAbstraction;
import com.poker.gto.core.actions.RiverActionGenerator;
import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.game_state.RiverPlayerState;
import com.poker.gto.core.game_state.RiverState;
import com.poker.gto.core.tree.RiverDecisionNode;
import com.poker.gto.core.tree.RiverTerminalNode;
import com.poker.gto.core.tree.RiverTreeBuilder;
import com.poker.gto.core.tree.RiverTreeNode;

import java.util.List;

/**
 * River博弈树演示程序
 */
public class RiverTreeDemo {

    public static void main(String[] args) {
        System.out.println("=== River博弈树测试 ===\n");

        testSimpleTree();
        testTreeWithDifferentAbstractions();
        testTerminalPayoffs();

        System.out.println("\n✅ Task 2.4 完成！River博弈树生成测试通过！");
    }

    /**
     * 测试简单树构建
     */
    private static void testSimpleTree() {
        System.out.println("1. 测试简单树构建");

        // 创建初始状态
        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();
        Hand player0Hand = Hand.parse("As Kh");
        Hand player1Hand = Hand.parse("Qd Qc");

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        RiverState initialState = new RiverState(board, player0, player1, 20);

        // 使用简单抽象（只有2种下注大小）
        RiverActionGenerator actionGenerator = new RiverActionGenerator(BetSizeAbstraction.SIMPLE);
        RiverTreeBuilder builder = new RiverTreeBuilder(actionGenerator);

        System.out.println("   初始状态: " + initialState);
        System.out.println("   使用简单抽象 (半池, 满池)");

        // 构建树
        long startTime = System.currentTimeMillis();
        RiverDecisionNode root = builder.buildTree(initialState);
        long buildTime = System.currentTimeMillis() - startTime;

        // 统计信息
        RiverTreeBuilder.TreeStats stats = builder.calculateStats(root);

        System.out.println("\n   构建完成，耗时: " + buildTime + "ms");
        System.out.println(stats);

        // 验证根节点
        assertTrue("Root should be decision node", !root.isTerminal());
        assertTrue("Root should have actions", root.getActions().size() > 0);
        assertTrue("Root should have children", root.hasChildren());

        System.out.println("   ✓ 简单树构建测试通过\n");
    }

    /**
     * 测试不同抽象的树大小
     */
    private static void testTreeWithDifferentAbstractions() {
        System.out.println("2. 测试不同抽象的树大小");

        // 创建相同的初始状态
        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand player0Hand = Hand.parse("As Kd");
        Hand player1Hand = Hand.parse("Jd Jc");

        RiverPlayerState player0 = new RiverPlayerState(0, 50, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 50, player1Hand);

        RiverState initialState = new RiverState(board, player0, player1, 10);

        // 测试不同的抽象
        BetSizeAbstraction[] abstractions = {
            BetSizeAbstraction.SIMPLE,
            BetSizeAbstraction.STANDARD,
            BetSizeAbstraction.RIVER
        };

        System.out.println("   初始状态: 底池=" + initialState.getPot() + ", 筹码=50/50\n");

        for (BetSizeAbstraction abstraction : abstractions) {
            RiverActionGenerator actionGenerator = new RiverActionGenerator(abstraction);
            RiverTreeBuilder builder = new RiverTreeBuilder(actionGenerator);

            long startTime = System.currentTimeMillis();
            RiverDecisionNode root = builder.buildTree(initialState);
            long buildTime = System.currentTimeMillis() - startTime;

            RiverTreeBuilder.TreeStats stats = builder.calculateStats(root);

            System.out.println("   " + abstraction.getName() + ":");
            System.out.println("     构建耗时: " + buildTime + "ms");
            System.out.println("     总节点数: " + stats.getTotalNodes());
            System.out.println("     决策节点: " + stats.decisionNodes);
            System.out.println("     终局节点: " + stats.terminalNodes);
            System.out.println("     信息集数: " + stats.getInfoSetCount());
            System.out.println("     平均分支: " + String.format("%.2f", stats.getAverageBranchingFactor()));
            System.out.println();
        }

        System.out.println("   ✓ 不同抽象测试通过\n");
    }

    /**
     * 测试终局节点收益计算
     */
    private static void testTerminalPayoffs() {
        System.out.println("3. 测试终局节点收益");

        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();

        // 场景1: 玩家0有坚果牌
        Hand player0Hand = Hand.parse("Qh Kh");  // K高同花顺
        Hand player1Hand = Hand.parse("2c 3d");  // 垃圾牌

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        RiverState initialState = new RiverState(board, player0, player1, 20);

        // 模拟场景：P0 check, P1 check -> showdown
        RiverState afterP0Check = initialState.applyAction(Action.PASS);
        RiverState terminal = afterP0Check.applyAction(Action.PASS);

        assertTrue("Should be terminal", terminal.isTerminal());

        RiverTerminalNode terminalNode = new RiverTerminalNode(terminal);
        double[] payoffs = terminalNode.getPayoffs();

        System.out.println("   场景1: 双方过牌到摊牌");
        System.out.println("   P0手牌: " + player0Hand + " (K高同花顺)");
        System.out.println("   P1手牌: " + player1Hand + " (垃圾牌)");
        System.out.println("   底池: " + terminal.getPot());
        System.out.println("   收益: P0=" + payoffs[0] + ", P1=" + payoffs[1]);

        assertTrue("P0 should win the pot", payoffs[0] > 0);
        assertTrue("P1 should lose", payoffs[1] < 0);
        assertTrue("Zero-sum game", Math.abs(payoffs[0] + payoffs[1]) < 0.01);

        // 场景2: 玩家弃牌
        RiverState afterP0Bet = initialState.applyAction(new Action(Action.Type.BET, 10));
        RiverState afterP1Fold = afterP0Bet.applyAction(Action.FOLD);

        assertTrue("Should be terminal after fold", afterP1Fold.isTerminal());

        RiverTerminalNode foldNode = new RiverTerminalNode(afterP1Fold);
        double[] foldPayoffs = foldNode.getPayoffs();

        System.out.println("\n   场景2: P0下注10, P1弃牌");
        System.out.println("   P0投入: " + afterP1Fold.getPlayer(0).getInvested());
        System.out.println("   P1投入: " + afterP1Fold.getPlayer(1).getInvested());
        System.out.println("   收益: P0=" + foldPayoffs[0] + ", P1=" + foldPayoffs[1]);

        assertTrue("P0 should win when P1 folds", foldPayoffs[0] > 0);
        assertTrue("P1 should lose invested amount", foldPayoffs[1] < 0);
        assertTrue("Zero-sum game", Math.abs(foldPayoffs[0] + foldPayoffs[1]) < 0.01);

        // 场景3: 平局
        Hand player0TieHand = Hand.parse("2c 3d");
        Hand player1TieHand = Hand.parse("4c 5d");

        RiverPlayerState player0Tie = new RiverPlayerState(0, 100, player0TieHand);
        RiverPlayerState player1Tie = new RiverPlayerState(1, 100, player1TieHand);

        RiverState tieInitial = new RiverState(board, player0Tie, player1Tie, 20);
        RiverState tieTerminal = tieInitial.applyAction(Action.PASS).applyAction(Action.PASS);

        RiverTerminalNode tieNode = new RiverTerminalNode(tieTerminal);
        double[] tiePayoffs = tieNode.getPayoffs();

        System.out.println("\n   场景3: 平局（双方高牌相同）");
        System.out.println("   P0手牌: " + player0TieHand);
        System.out.println("   P1手牌: " + player1TieHand);
        System.out.println("   底池: " + tieTerminal.getPot());
        System.out.println("   收益: P0=" + tiePayoffs[0] + ", P1=" + tiePayoffs[1]);

        assertTrue("Both should get half pot in tie", Math.abs(tiePayoffs[0] - tiePayoffs[1]) < 0.01);
        assertTrue("Zero-sum game", Math.abs(tiePayoffs[0] + tiePayoffs[1]) < 0.01);

        System.out.println("\n   ✓ 终局收益测试通过\n");
    }

    // ========== 辅助方法 ==========

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
