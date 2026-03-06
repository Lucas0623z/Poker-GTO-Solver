package com.poker.gto.app.cli;

import com.poker.gto.core.cards.Card;
import com.poker.gto.core.tree.DecisionNode;
import com.poker.gto.core.tree.KuhnTreeBuilder;
import com.poker.gto.solver.cfr.Strategy;
import com.poker.gto.solver.cfr.VanillaCFR;

import java.util.List;

/**
 * Kuhn Poker 求解器命令行工具
 *
 * 运行 CFR 算法求解 Kuhn Poker 的纳什均衡策略
 */
public class KuhnSolverCLI {

    public static void main(String[] args) {
        System.out.println("=== Kuhn Poker CFR Solver ===\n");

        // 解析参数
        int iterations = 10000;
        if (args.length > 0) {
            try {
                iterations = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid iteration count: " + args[0]);
                System.err.println("Usage: java KuhnSolverCLI [iterations]");
                System.exit(1);
            }
        }

        System.out.println("Iterations: " + iterations);
        System.out.println();

        // 构建博弈树
        System.out.println("Building game tree...");
        KuhnTreeBuilder treeBuilder = new KuhnTreeBuilder();

        // 使用 J vs Q 作为示例
        Card player0Card = Card.JACK;
        Card player1Card = Card.QUEEN;

        DecisionNode root = treeBuilder.buildTree(player0Card, player1Card);

        int nodeCount = treeBuilder.countNodes(root);
        int infoSetCount = treeBuilder.countInfoSets(root);

        System.out.println("Tree nodes: " + nodeCount);
        System.out.println("Info sets: " + infoSetCount);
        System.out.println();

        // 运行 CFR
        System.out.println("Running Vanilla CFR...");
        System.out.println();

        VanillaCFR solver = new VanillaCFR();
        long startTime = System.currentTimeMillis();

        Strategy strategy = solver.solve(root, iterations);

        long endTime = System.currentTimeMillis();
        double elapsedSeconds = (endTime - startTime) / 1000.0;

        System.out.println();
        System.out.println("=== Results ===");
        System.out.println("Time elapsed: " + String.format("%.2f", elapsedSeconds) + " seconds");
        System.out.println("Final exploitability: " + String.format("%.6f", solver.getExploitability()));
        System.out.println();

        // 打印策略
        System.out.println("=== Average Strategy ===");
        System.out.println(strategy);
        System.out.println();

        // 分析策略
        analyzeStrategy(strategy);

        // 测试所有发牌组合
        System.out.println("\n=== Testing All Card Deals ===\n");
        testAllDeals(iterations);
    }

    /**
     * 分析策略
     */
    private static void analyzeStrategy(Strategy strategy) {
        System.out.println("=== Strategy Analysis ===");

        int infoSetCount = strategy.size();
        System.out.println("Total info sets: " + infoSetCount);

        // 检查策略是否归一化
        boolean allNormalized = true;
        for (String infoSet : strategy.getInfoSets()) {
            var actionProbs = strategy.getStrategy(infoSet);
            if (actionProbs != null) {
                double sum = actionProbs.values().stream()
                                        .mapToDouble(Double::doubleValue)
                                        .sum();

                if (Math.abs(sum - 1.0) > 0.001) {
                    System.out.println("WARNING: Info set " + infoSet +
                                     " has sum = " + sum + " (not normalized)");
                    allNormalized = false;
                }
            }
        }

        if (allNormalized) {
            System.out.println("✓ All strategies properly normalized");
        }

        System.out.println();
    }

    /**
     * 测试所有发牌组合
     */
    private static void testAllDeals(int iterations) {
        KuhnTreeBuilder treeBuilder = new KuhnTreeBuilder();
        VanillaCFR solver = new VanillaCFR();

        Card[] allCards = {Card.JACK, Card.QUEEN, Card.KING};

        for (Card card0 : allCards) {
            for (Card card1 : allCards) {
                if (!card0.equals(card1)) {
                    System.out.println("Deal: P0=" + card0 + ", P1=" + card1);

                    DecisionNode root = treeBuilder.buildTree(card0, card1);
                    solver.reset();

                    long startTime = System.currentTimeMillis();
                    Strategy strategy = solver.solve(root, iterations);
                    long endTime = System.currentTimeMillis();

                    double elapsedMs = endTime - startTime;
                    double exploitability = solver.getExploitability();

                    System.out.println("  Time: " + String.format("%.0f", elapsedMs) + " ms");
                    System.out.println("  Exploitability: " + String.format("%.6f", exploitability));
                    System.out.println();
                }
            }
        }
    }
}
