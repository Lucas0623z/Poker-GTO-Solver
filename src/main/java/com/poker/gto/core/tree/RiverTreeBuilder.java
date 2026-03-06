package com.poker.gto.core.tree;

import com.poker.gto.core.actions.RiverActionGenerator;
import com.poker.gto.core.game_state.RiverState;

/**
 * River博弈树构建器
 *
 * 从初始状态构建完整的River博弈树
 */
public class RiverTreeBuilder {

    private final RiverActionGenerator actionGenerator;

    /**
     * 创建树构建器
     *
     * @param actionGenerator 动作生成器
     */
    public RiverTreeBuilder(RiverActionGenerator actionGenerator) {
        this.actionGenerator = actionGenerator;
    }

    /**
     * 构建博弈树
     *
     * @param initialState 初始状态
     * @return 树的根节点
     */
    public RiverDecisionNode buildTree(RiverState initialState) {
        if (initialState.isTerminal()) {
            throw new IllegalArgumentException("Initial state cannot be terminal");
        }

        // 创建根节点
        RiverDecisionNode root = new RiverDecisionNode(initialState, actionGenerator);

        // 递归构建整个树
        root.buildSubtree(actionGenerator);

        return root;
    }

    /**
     * 计算树的统计信息
     */
    public TreeStats calculateStats(RiverDecisionNode root) {
        TreeStats stats = new TreeStats();
        traverseAndCount(root, stats);
        return stats;
    }

    /**
     * 遍历树并统计
     */
    private void traverseAndCount(RiverTreeNode node, TreeStats stats) {
        if (node.isTerminal()) {
            stats.terminalNodes++;
            return;
        }

        RiverDecisionNode decisionNode = (RiverDecisionNode) node;
        stats.decisionNodes++;
        stats.totalActions += decisionNode.getActions().size();

        // 添加信息集
        String infoSetKey = decisionNode.getInfoSetKey();
        if (!stats.infoSets.contains(infoSetKey)) {
            stats.infoSets.add(infoSetKey);
        }

        // 递归遍历子节点
        for (RiverTreeNode child : decisionNode.getChildren().values()) {
            traverseAndCount(child, stats);
        }
    }

    /**
     * 树统计信息
     */
    public static class TreeStats {
        public int decisionNodes = 0;
        public int terminalNodes = 0;
        public int totalActions = 0;
        public java.util.Set<String> infoSets = new java.util.HashSet<>();

        public int getTotalNodes() {
            return decisionNodes + terminalNodes;
        }

        public int getInfoSetCount() {
            return infoSets.size();
        }

        public double getAverageBranchingFactor() {
            return decisionNodes == 0 ? 0 : (double) totalActions / decisionNodes;
        }

        @Override
        public String toString() {
            return String.format(
                "TreeStats{\n" +
                "  总节点数: %d\n" +
                "  决策节点: %d\n" +
                "  终局节点: %d\n" +
                "  信息集数: %d\n" +
                "  总动作数: %d\n" +
                "  平均分支因子: %.2f\n" +
                "}",
                getTotalNodes(),
                decisionNodes,
                terminalNodes,
                getInfoSetCount(),
                totalActions,
                getAverageBranchingFactor()
            );
        }
    }
}
