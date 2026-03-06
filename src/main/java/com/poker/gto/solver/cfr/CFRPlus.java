package com.poker.gto.solver.cfr;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.tree.DecisionNode;
import com.poker.gto.core.tree.TerminalNode;
import com.poker.gto.core.tree.TreeNode;
import com.poker.gto.solver.metrics.Exploitability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CFR+ (CFR Plus) 求解器
 *
 * CFR+ 相比 Vanilla CFR 的改进:
 * 1. 遗憾值不会变成负数 (Regret Floor = 0)
 * 2. 收敛速度更快 (经验上提升 2-3 倍)
 * 3. 适合中大规模博弈
 *
 * 核心区别:
 * - Vanilla CFR: R^t(a) = R^{t-1}(a) + regret
 * - CFR+:        R^t(a) = max(R^{t-1}(a) + regret, 0)
 *
 * 参考: Tammelin et al., 2014
 */
public class CFRPlus {

    private static final int NUM_PLAYERS = 2;

    private final RegretTable regretTable;
    private final Map<String, Map<Action, Double>> strategySum;
    private final Exploitability exploitabilityCalculator;
    private int iterations;
    private TreeNode gameRoot;

    public CFRPlus() {
        this.regretTable = new RegretTable();
        this.strategySum = new HashMap<>();
        this.exploitabilityCalculator = new Exploitability();
        this.iterations = 0;
        this.gameRoot = null;
    }

    /**
     * 运行 CFR+ 算法
     *
     * @param root       博弈树根节点
     * @param iterations 迭代次数
     * @return 平均策略
     */
    public Strategy solve(TreeNode root, int iterations) {
        this.gameRoot = root;

        for (int i = 0; i < iterations; i++) {
            this.iterations++;

            // 对每个玩家运行 CFR+
            for (int player = 0; player < NUM_PLAYERS; player++) {
                double[] reach = new double[NUM_PLAYERS];
                reach[0] = 1.0;
                reach[1] = 1.0;

                cfr(root, player, reach);
            }

            // 每 1000 次迭代输出一次日志
            if ((i + 1) % 1000 == 0 || i == iterations - 1) {
                logProgress(i + 1);
            }
        }

        return getAverageStrategy();
    }

    /**
     * CFR+ 递归核心算法
     */
    private double cfr(TreeNode node, int player, double[] reach) {
        // 终局节点
        if (node.isTerminal()) {
            TerminalNode terminal = (TerminalNode) node;
            return terminal.getPayoff(player);
        }

        DecisionNode decisionNode = (DecisionNode) node;
        int actingPlayer = decisionNode.getActingPlayer();
        String infoSet = decisionNode.getInfoSetKey();
        List<Action> actions = decisionNode.getActions();

        // 获取当前策略
        Map<Action, Double> strategy = getStrategy(infoSet, actions);

        // 计算每个动作的反事实价值
        Map<Action, Double> actionValues = new HashMap<>();
        double nodeValue = 0.0;

        for (Action action : actions) {
            double[] newReach = reach.clone();
            newReach[actingPlayer] *= strategy.get(action);

            TreeNode child = decisionNode.getChild(action);
            double actionValue = cfr(child, player, newReach);

            actionValues.put(action, actionValue);
            nodeValue += strategy.get(action) * actionValue;
        }

        // 更新遗憾和累积策略
        if (actingPlayer == player) {
            double opponentReach = reach[1 - player];

            // CFR+ 关键改进: 更新遗憾时使用 max(R + regret, 0)
            for (Action action : actions) {
                double regret = actionValues.get(action) - nodeValue;
                double oldRegret = regretTable.getRegret(infoSet, action);
                double newRegret = Math.max(oldRegret + regret * opponentReach, 0.0);  // Floor at 0
                regretTable.setRegret(infoSet, action, newRegret);
            }

            // 累积策略
            double playerReach = reach[player];
            for (Action action : actions) {
                strategySum.computeIfAbsent(infoSet, k -> new HashMap<>())
                           .merge(action, playerReach * strategy.get(action), Double::sum);
            }
        }

        return nodeValue;
    }

    /**
     * 使用 Regret Matching 计算当前策略
     */
    private Map<Action, Double> getStrategy(String infoSet, List<Action> actions) {
        Map<Action, Double> strategy = new HashMap<>();
        double normalizingSum = 0.0;

        // 计算正遗憾和
        for (Action action : actions) {
            double regret = regretTable.getRegret(infoSet, action);
            double positiveRegret = Math.max(0.0, regret);
            strategy.put(action, positiveRegret);
            normalizingSum += positiveRegret;
        }

        // 归一化
        if (normalizingSum > 0) {
            for (Action action : actions) {
                strategy.put(action, strategy.get(action) / normalizingSum);
            }
        } else {
            // 均匀策略
            double uniform = 1.0 / actions.size();
            for (Action action : actions) {
                strategy.put(action, uniform);
            }
        }

        return strategy;
    }

    /**
     * 计算平均策略
     */
    public Strategy getAverageStrategy() {
        Strategy avgStrategy = new Strategy();

        for (Map.Entry<String, Map<Action, Double>> entry : strategySum.entrySet()) {
            String infoSet = entry.getKey();
            Map<Action, Double> actionSums = entry.getValue();

            double normalizingSum = actionSums.values().stream()
                                              .mapToDouble(Double::doubleValue)
                                              .sum();

            Map<Action, Double> normalizedStrategy = new HashMap<>();
            if (normalizingSum > 0) {
                for (Map.Entry<Action, Double> actionEntry : actionSums.entrySet()) {
                    normalizedStrategy.put(
                        actionEntry.getKey(),
                        actionEntry.getValue() / normalizingSum
                    );
                }
            } else {
                double uniform = 1.0 / actionSums.size();
                for (Action action : actionSums.keySet()) {
                    normalizedStrategy.put(action, uniform);
                }
            }

            avgStrategy.setStrategy(infoSet, normalizedStrategy);
        }

        return avgStrategy;
    }

    /**
     * 计算当前策略的 exploitability
     */
    public double getExploitability() {
        if (gameRoot == null) {
            return Double.NaN;
        }
        Strategy currentStrategy = getAverageStrategy();
        return exploitabilityCalculator.calculate(gameRoot, currentStrategy);
    }

    /**
     * 获取当前迭代次数
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * 输出进度日志
     */
    private void logProgress(int iteration) {
        Strategy currentStrategy = getAverageStrategy();
        double exploitability = exploitabilityCalculator.calculate(gameRoot, currentStrategy);
        System.out.printf("Iteration %d: exploitability=%.6f%n", iteration, exploitability);
    }

    /**
     * 重置求解器状态
     */
    public void reset() {
        regretTable.clear();
        strategySum.clear();
        iterations = 0;
        gameRoot = null;
    }
}
