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
 * Vanilla CFR 求解器
 *
 * 实现标准的 Counterfactual Regret Minimization 算法
 *
 * 核心思想:
 * 1. 遍历博弈树
 * 2. 计算每个动作的反事实价值
 * 3. 更新遗憾(regret)
 * 4. 使用 Regret Matching 生成新策略
 * 5. 累积平均策略
 *
 * 参考: Zinkevich et al., 2007
 */
public class VanillaCFR {

    private static final int NUM_PLAYERS = 2;

    private final RegretTable regretTable;
    private final Map<String, Map<Action, Double>> strategySum;
    private final Exploitability exploitabilityCalculator;
    private int iterations;
    private TreeNode gameRoot;  // 保存根节点用于计算exploitability

    public VanillaCFR() {
        this.regretTable = new RegretTable();
        this.strategySum = new HashMap<>();
        this.exploitabilityCalculator = new Exploitability();
        this.iterations = 0;
        this.gameRoot = null;
    }

    /**
     * 运行 CFR 算法
     *
     * @param root       博弈树根节点
     * @param iterations 迭代次数
     * @return 平均策略
     */
    public Strategy solve(TreeNode root, int iterations) {
        this.gameRoot = root;  // 保存根节点

        for (int i = 0; i < iterations; i++) {
            this.iterations++;

            // 对每个玩家运行 CFR
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
     * CFR 递归核心算法
     *
     * @param node   当前节点
     * @param player 正在更新策略的玩家
     * @param reach  到达概率 [player0_reach, player1_reach]
     * @return 当前节点对 player 的反事实价值
     */
    private double cfr(TreeNode node, int player, double[] reach) {
        // 终局节点:返回收益
        if (node.isTerminal()) {
            TerminalNode terminal = (TerminalNode) node;
            return terminal.getPayoff(player);
        }

        DecisionNode decisionNode = (DecisionNode) node;
        int actingPlayer = decisionNode.getActingPlayer();
        String infoSet = decisionNode.getInfoSetKey();
        List<Action> actions = decisionNode.getActions();

        // 获取当前策略(使用 Regret Matching)
        Map<Action, Double> strategy = getStrategy(infoSet, actions);

        // 计算每个动作的反事实价值
        Map<Action, Double> actionValues = new HashMap<>();
        double nodeValue = 0.0;

        for (Action action : actions) {
            // 计算新的到达概率
            double[] newReach = reach.clone();
            newReach[actingPlayer] *= strategy.get(action);

            // 递归计算子节点价值
            TreeNode child = decisionNode.getChild(action);
            double actionValue = cfr(child, player, newReach);

            actionValues.put(action, actionValue);
            nodeValue += strategy.get(action) * actionValue;
        }

        // 如果是当前玩家的节点,更新遗憾和累积策略
        if (actingPlayer == player) {
            // 计算对手到达概率
            double opponentReach = reach[1 - player];

            // 更新遗憾
            for (Action action : actions) {
                double regret = actionValues.get(action) - nodeValue;
                regretTable.updateRegret(infoSet, action, regret * opponentReach);
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
     *
     * @param infoSet 信息集Key
     * @param actions 合法动作列表
     * @return 动作概率分布
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
            // 如果所有遗憾都 <= 0, 使用均匀策略
            double uniform = 1.0 / actions.size();
            for (Action action : actions) {
                strategy.put(action, uniform);
            }
        }

        return strategy;
    }

    /**
     * 计算平均策略
     *
     * @return 平均策略
     */
    public Strategy getAverageStrategy() {
        Strategy avgStrategy = new Strategy();

        for (Map.Entry<String, Map<Action, Double>> entry : strategySum.entrySet()) {
            String infoSet = entry.getKey();
            Map<Action, Double> actionSums = entry.getValue();

            // 计算总和
            double normalizingSum = actionSums.values().stream()
                                              .mapToDouble(Double::doubleValue)
                                              .sum();

            // 归一化
            Map<Action, Double> normalizedStrategy = new HashMap<>();
            if (normalizingSum > 0) {
                for (Map.Entry<Action, Double> actionEntry : actionSums.entrySet()) {
                    normalizedStrategy.put(
                        actionEntry.getKey(),
                        actionEntry.getValue() / normalizingSum
                    );
                }
            } else {
                // 均匀策略
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
     * 获取当前迭代次数
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * 获取平均遗憾(调试用)
     */
    public double getAverageRegret() {
        return regretTable.getAverageRegret();
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
     * 重置求解器状态
     */
    public void reset() {
        regretTable.clear();
        strategySum.clear();
        iterations = 0;
    }
}
