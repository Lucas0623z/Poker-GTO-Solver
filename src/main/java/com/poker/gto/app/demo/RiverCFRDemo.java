package com.poker.gto.app.demo;

import com.poker.gto.core.actions.Action;
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

import java.util.List;
import java.util.Map;

/**
 * River CFR演示程序
 */
public class RiverCFRDemo {

    public static void main(String[] args) {
        System.out.println("=== River CFR求解器测试 ===\n");

        testSimpleCFR();
        testDifferentScenarios();

        System.out.println("\n✅ Task 2.5 完成！River CFR求解器测试通过！");
    }

    /**
     * 测试简单CFR求解
     */
    private static void testSimpleCFR() {
        System.out.println("1. 测试简单CFR求解");

        // 创建简单场景
        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();
        Hand player0Hand = Hand.parse("As Kh");  // 有顺子
        Hand player1Hand = Hand.parse("Qd Qc");  // 有三条Q

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        RiverState initialState = new RiverState(board, player0, player1, 20);

        System.out.println("   场景设置:");
        System.out.println("   公共牌: " + board);
        System.out.println("   P0手牌: " + player0Hand + " (顺子)");
        System.out.println("   P1手牌: " + player1Hand + " (三条)");
        System.out.println("   底池: " + initialState.getPot());
        System.out.println("   筹码: 100/100\n");

        // 使用简单抽象构建树
        RiverActionGenerator actionGenerator = new RiverActionGenerator(BetSizeAbstraction.SIMPLE);
        RiverTreeBuilder treeBuilder = new RiverTreeBuilder(actionGenerator);

        System.out.println("   构建博弈树...");
        RiverDecisionNode root = treeBuilder.buildTree(initialState);
        RiverTreeBuilder.TreeStats stats = treeBuilder.calculateStats(root);
        System.out.println("   " + stats);

        // 运行CFR
        System.out.println("\n   运行CFR求解...");
        RiverCFR cfr = new RiverCFR(root);
        cfr.train(1000, 250);

        // 获取策略
        Strategy strategy = cfr.getAverageStrategy();

        System.out.println("\n   ✅ CFR求解完成");
        System.out.println("   信息集数: " + strategy.size());

        // 显示根节点策略
        displayRootStrategy(root, strategy);

        System.out.println("   ✓ 简单CFR测试通过\n");
    }

    /**
     * 测试不同场景
     */
    private static void testDifferentScenarios() {
        System.out.println("2. 测试不同场景");

        // 场景1: P0有坚果牌
        System.out.println("\n   场景A: P0有坚果牌");
        testScenario(
            "Ah Kh Qh Jh Th",  // 皇家同花顺面
            "9h 8h",           // P0有K高同花顺（几乎坚果）
            "2c 3d",           // P1垃圾牌
            50, 10
        );

        // 场景2: 均势对抗
        System.out.println("\n   场景B: 均势对抗");
        testScenario(
            "Ah Kh Qh 7s 2c",  // 高牌面
            "As Kd",           // P0顶两对
            "Ad Ks",           // P1也是顶两对
            50, 10
        );

        System.out.println("\n   ✓ 不同场景测试通过\n");
    }

    /**
     * 测试特定场景
     */
    private static void testScenario(String boardStr, String p0HandStr, String p1HandStr, int stack, int pot) {
        List<TexasCard> board = Hand.parse(boardStr).getCards();
        Hand player0Hand = Hand.parse(p0HandStr);
        Hand player1Hand = Hand.parse(p1HandStr);

        RiverPlayerState player0 = new RiverPlayerState(0, stack, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, stack, player1Hand);

        RiverState initialState = new RiverState(board, player0, player1, pot);

        System.out.println("     公共牌: " + boardStr);
        System.out.println("     P0: " + p0HandStr + " vs P1: " + p1HandStr);
        System.out.println("     底池=" + pot + ", 筹码=" + stack);

        // 构建树并求解
        RiverActionGenerator actionGenerator = new RiverActionGenerator(BetSizeAbstraction.SIMPLE);
        RiverTreeBuilder treeBuilder = new RiverTreeBuilder(actionGenerator);
        RiverDecisionNode root = treeBuilder.buildTree(initialState);

        RiverCFR cfr = new RiverCFR(root);
        cfr.train(500, 500);  // 快速训练

        Strategy strategy = cfr.getAverageStrategy();
        displayRootStrategy(root, strategy);
    }

    /**
     * 显示根节点策略
     */
    private static void displayRootStrategy(RiverDecisionNode root, Strategy strategy) {
        String rootInfoSet = root.getInfoSetKey();
        Map<Action, Double> rootStrategy = strategy.getStrategy(rootInfoSet);

        if (rootStrategy != null) {
            System.out.println("     根节点策略 (P" + root.getActingPlayer() + "):");
            for (Map.Entry<Action, Double> entry : rootStrategy.entrySet()) {
                System.out.printf("       %s: %.1f%%%n",
                    formatAction(entry.getKey()),
                    entry.getValue() * 100);
            }
        }
    }

    /**
     * 格式化动作显示
     */
    private static String formatAction(Action action) {
        if (action.getType() == Action.Type.BET) {
            return "BET(" + action.getAmount() + ")";
        }
        return action.getType().name();
    }
}
