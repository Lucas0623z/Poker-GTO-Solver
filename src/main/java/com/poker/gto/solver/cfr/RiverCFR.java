package com.poker.gto.solver.cfr;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.tree.RiverDecisionNode;
import com.poker.gto.core.tree.RiverTerminalNode;
import com.poker.gto.core.tree.RiverTreeNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * River CFR求解器
 *
 * 使用CFR算法求解River博弈树，计算GTO策略
 */
public class RiverCFR {

    private final RiverDecisionNode gameRoot;
    private final RegretTable regretTable;
    private final Map<String, Map<Action, Double>> strategySum;  // 累积策略

    /**
     * 创建River CFR求解器
     *
     * @param gameRoot 博弈树根节点
     */
    public RiverCFR(RiverDecisionNode gameRoot) {
        this.gameRoot = gameRoot;
        this.regretTable = new RegretTable();
        this.strategySum = new HashMap<>();
    }

    /**
     * 训练CFR
     *
     * @param iterations 迭代次数
     */
    public void train(int iterations) {
        train(iterations, 100);  // 默认每100次迭代输出一次
    }

    /**
     * 训练CFR
     *
     * @param iterations 迭代次数
     * @param logInterval 日志输出间隔
     */
    public void train(int iterations, int logInterval) {
        System.out.println("开始训练 River CFR...");
        System.out.println("迭代次数: " + iterations);

        for (int i = 1; i <= iterations; i++) {
            // 对每个玩家执行CFR遍历
            for (int player = 0; player < 2; player++) {
                cfr(gameRoot, player, 1.0, 1.0);
            }

            // 定期输出进度
            if (i % logInterval == 0 || i == iterations) {
                logProgress(i);
            }
        }

        System.out.println("训练完成！");
    }

    /**
     * CFR递归遍历
     *
     * @param node 当前节点
     * @param player 当前更新的玩家
     * @param reachProb0 玩家0的到达概率
     * @param reachProb1 玩家1的到达概率
     * @return 当前节点的期望收益（对player而言）
     */
    private double cfr(RiverTreeNode node, int player, double reachProb0, double reachProb1) {
        // 终局节点：返回收益
        if (node.isTerminal()) {
            RiverTerminalNode terminal = (RiverTerminalNode) node;
            return terminal.getPayoff(player);
        }

        RiverDecisionNode decisionNode = (RiverDecisionNode) node;
        int actingPlayer = decisionNode.getActingPlayer();
        String infoSet = decisionNode.getInfoSetKey();
        List<Action> actions = decisionNode.getActions();

        // 计算当前策略
        Map<Action, Double> strategy = getStrategy(infoSet, actions);

        // 如果是当前玩家的决策节点
        if (actingPlayer == player) {
            // 计算每个动作的反事实价值
            Map<Action, Double> actionValues = new HashMap<>();
            double nodeValue = 0.0;

            for (Action action : actions) {
                RiverTreeNode child = decisionNode.getChild(action);
                double prob = strategy.get(action);

                double actionValue;
                if (player == 0) {
                    actionValue = cfr(child, player, reachProb0 * prob, reachProb1);
                } else {
                    actionValue = cfr(child, player, reachProb0, reachProb1 * prob);
                }

                actionValues.put(action, actionValue);
                nodeValue += prob * actionValue;
            }

            // 计算遗憾并更新
            double opponentReach = (player == 0) ? reachProb1 : reachProb0;

            for (Action action : actions) {
                double actionValue = actionValues.get(action);
                double regret = actionValue - nodeValue;
                regretTable.updateRegret(infoSet, action, opponentReach * regret);
            }

            // 累积策略
            double ownReach = (player == 0) ? reachProb0 : reachProb1;
            accumulateStrategy(infoSet, strategy, ownReach);

            return nodeValue;

        } else {
            // 对手的决策节点：按策略采样
            double expectedValue = 0.0;

            for (Action action : actions) {
                RiverTreeNode child = decisionNode.getChild(action);
                double prob = strategy.get(action);

                double childValue;
                if (actingPlayer == 0) {
                    childValue = cfr(child, player, reachProb0 * prob, reachProb1);
                } else {
                    childValue = cfr(child, player, reachProb0, reachProb1 * prob);
                }

                expectedValue += prob * childValue;
            }

            return expectedValue;
        }
    }

    /**
     * 根据遗憾计算当前策略（Regret Matching）
     *
     * @param infoSet 信息集
     * @param actions 可用动作
     * @return 策略分布
     */
    private Map<Action, Double> getStrategy(String infoSet, List<Action> actions) {
        Map<Action, Double> strategy = new HashMap<>();

        // 收集正遗憾
        double normalizingSum = 0.0;
        for (Action action : actions) {
            double regret = Math.max(0.0, regretTable.getRegret(infoSet, action));
            strategy.put(action, regret);
            normalizingSum += regret;
        }

        // 归一化
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

    /**
     * 累积策略
     *
     * @param infoSet 信息集
     * @param strategy 当前策略
     * @param reachProb 到达概率
     */
    private void accumulateStrategy(String infoSet, Map<Action, Double> strategy, double reachProb) {
        Map<Action, Double> sum = strategySum.computeIfAbsent(infoSet, k -> new HashMap<>());

        for (Map.Entry<Action, Double> entry : strategy.entrySet()) {
            Action action = entry.getKey();
            double prob = entry.getValue();
            sum.merge(action, reachProb * prob, Double::sum);
        }
    }

    /**
     * 获取平均策略
     *
     * @return 平均策略
     */
    public Strategy getAverageStrategy() {
        Strategy avgStrategy = new Strategy();

        for (Map.Entry<String, Map<Action, Double>> entry : strategySum.entrySet()) {
            String infoSet = entry.getKey();
            Map<Action, Double> sum = entry.getValue();

            // 计算总和
            double total = sum.values().stream().mapToDouble(Double::doubleValue).sum();

            if (total > 0) {
                // 归一化
                Map<Action, Double> normalized = new HashMap<>();
                for (Map.Entry<Action, Double> actionEntry : sum.entrySet()) {
                    normalized.put(actionEntry.getKey(), actionEntry.getValue() / total);
                }
                avgStrategy.setStrategy(infoSet, normalized);
            } else {
                // 均匀分布
                Map<Action, Double> uniform = new HashMap<>();
                double prob = 1.0 / sum.size();
                for (Action action : sum.keySet()) {
                    uniform.put(action, prob);
                }
                avgStrategy.setStrategy(infoSet, uniform);
            }
        }

        return avgStrategy;
    }

    /**
     * 输出训练进度
     */
    private void logProgress(int iteration) {
        System.out.printf("迭代 %d: 信息集数=%d, 平均遗憾=%.6f%n",
            iteration,
            regretTable.size(),
            regretTable.getAverageRegret());
    }

    /**
     * 获取遗憾表（用于调试）
     */
    public RegretTable getRegretTable() {
        return regretTable;
    }
}
