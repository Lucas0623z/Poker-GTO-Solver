package com.poker.gto.app.cli;

import com.poker.gto.core.cards.Card;
import com.poker.gto.core.tree.DecisionNode;
import com.poker.gto.core.tree.KuhnTreeBuilder;
import com.poker.gto.solver.cfr.CFRPlus;
import com.poker.gto.solver.cfr.Strategy;
import com.poker.gto.solver.cfr.VanillaCFR;

/**
 * CFR 算法对比工具
 *
 * 对比 Vanilla CFR 和 CFR+ 的收敛速度
 */
public class CFRComparison {

    public static void main(String[] args) {
        System.out.println("=== CFR Algorithm Comparison ===\n");

        int iterations = 10000;
        if (args.length > 0) {
            try {
                iterations = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid iteration count: " + args[0]);
                System.exit(1);
            }
        }

        // 构建博弈树
        KuhnTreeBuilder treeBuilder = new KuhnTreeBuilder();
        DecisionNode root = treeBuilder.buildTree(Card.JACK, Card.QUEEN);

        System.out.println("Test scenario: P0=J, P1=Q");
        System.out.println("Iterations: " + iterations);
        System.out.println();

        // 测试 Vanilla CFR
        System.out.println("=== Vanilla CFR ===");
        VanillaCFR vanillaSolver = new VanillaCFR();
        long vanillaStart = System.currentTimeMillis();
        Strategy vanillaStrategy = vanillaSolver.solve(root, iterations);
        long vanillaEnd = System.currentTimeMillis();
        double vanillaExploitability = vanillaSolver.getExploitability();
        double vanillaTime = (vanillaEnd - vanillaStart) / 1000.0;

        System.out.println("Time: " + String.format("%.3f", vanillaTime) + " seconds");
        System.out.println("Final exploitability: " + String.format("%.6f", vanillaExploitability));
        System.out.println();

        // 测试 CFR+
        System.out.println("=== CFR+ ===");
        CFRPlus plusSolver = new CFRPlus();
        long plusStart = System.currentTimeMillis();
        Strategy plusStrategy = plusSolver.solve(root, iterations);
        long plusEnd = System.currentTimeMillis();
        double plusExploitability = plusSolver.getExploitability();
        double plusTime = (plusEnd - plusStart) / 1000.0;

        System.out.println("Time: " + String.format("%.3f", plusTime) + " seconds");
        System.out.println("Final exploitability: " + String.format("%.6f", plusExploitability));
        System.out.println();

        // 对比结果
        System.out.println("=== Comparison ===");
        System.out.println("Vanilla CFR exploitability: " + String.format("%.6f", vanillaExploitability));
        System.out.println("CFR+ exploitability:        " + String.format("%.6f", plusExploitability));
        System.out.println();

        double improvement = (vanillaExploitability - plusExploitability) / vanillaExploitability * 100;
        if (plusExploitability < vanillaExploitability) {
            System.out.println("CFR+ is " + String.format("%.1f%%", improvement) + " better");
        } else if (plusExploitability > vanillaExploitability) {
            System.out.println("Vanilla CFR is " + String.format("%.1f%%", -improvement) + " better");
        } else {
            System.out.println("Both algorithms achieved the same exploitability");
        }

        System.out.println();
        System.out.println("Time difference: " +
            String.format("%.3f", Math.abs(vanillaTime - plusTime)) + " seconds");

        // 显示策略
        System.out.println("\n=== Vanilla CFR Strategy ===");
        System.out.println(vanillaStrategy);

        System.out.println("\n=== CFR+ Strategy ===");
        System.out.println(plusStrategy);

        // 详细收敛测试
        System.out.println("\n=== Convergence Test ===");
        testConvergence(root, 5000);
    }

    /**
     * 测试不同迭代次数下的收敛情况
     */
    private static void testConvergence(DecisionNode root, int maxIterations) {
        System.out.println("\nIterations | Vanilla CFR | CFR+");
        System.out.println("-----------|-------------|------------");

        int[] testPoints = {100, 500, 1000, 2000, 3000, 5000};

        for (int iterations : testPoints) {
            if (iterations > maxIterations) break;

            // Vanilla CFR
            VanillaCFR vanillaSolver = new VanillaCFR();
            vanillaSolver.solve(root, iterations);
            double vanillaExp = vanillaSolver.getExploitability();

            // CFR+
            CFRPlus plusSolver = new CFRPlus();
            plusSolver.solve(root, iterations);
            double plusExp = plusSolver.getExploitability();

            System.out.printf("%-10d | %-11.6f | %-11.6f%n",
                iterations, vanillaExp, plusExp);
        }
    }
}
