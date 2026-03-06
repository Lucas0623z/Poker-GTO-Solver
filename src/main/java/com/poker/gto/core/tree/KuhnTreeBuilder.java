package com.poker.gto.core.tree;

import com.poker.gto.core.actions.KuhnActionGenerator;
import com.poker.gto.core.cards.Card;
import com.poker.gto.core.game_state.KuhnPokerState;

import java.util.ArrayList;
import java.util.List;

/**
 * Kuhn Poker 博弈树构建器
 *
 * 构建完整的 Kuhn Poker 博弈树
 *
 * Kuhn Poker 有 3! = 6 种发牌方式:
 * - P0=J, P1=Q
 * - P0=J, P1=K
 * - P0=Q, P1=J
 * - P0=Q, P1=K
 * - P0=K, P1=J
 * - P0=K, P1=Q
 */
public class KuhnTreeBuilder {

    private final KuhnActionGenerator actionGenerator;

    public KuhnTreeBuilder() {
        this.actionGenerator = new KuhnActionGenerator();
    }

    /**
     * 构建单个初始发牌的博弈树
     *
     * @param player0Card 玩家0的手牌
     * @param player1Card 玩家1的手牌
     * @return 根节点
     */
    public DecisionNode buildTree(Card player0Card, Card player1Card) {
        KuhnPokerState initialState = new KuhnPokerState(player0Card, player1Card);
        DecisionNode root = new DecisionNode(initialState, actionGenerator);
        root.buildSubtree(actionGenerator);
        return root;
    }

    /**
     * 构建所有可能发牌的博弈树根节点
     *
     * @return 所有根节点列表
     */
    public List<DecisionNode> buildAllTrees() {
        List<DecisionNode> roots = new ArrayList<>();

        Card[] allCards = {Card.JACK, Card.QUEEN, Card.KING};

        // 枚举所有发牌组合
        for (Card card0 : allCards) {
            for (Card card1 : allCards) {
                if (!card0.equals(card1)) {  // 不能发相同的牌
                    DecisionNode root = buildTree(card0, card1);
                    roots.add(root);
                }
            }
        }

        return roots;
    }

    /**
     * 统计博弈树节点数量(调试用)
     *
     * @param root 根节点
     * @return 节点总数
     */
    public int countNodes(TreeNode root) {
        if (root.isTerminal()) {
            return 1;
        }

        DecisionNode decisionNode = (DecisionNode) root;
        int count = 1;  // 当前节点

        for (TreeNode child : decisionNode.getChildren().values()) {
            count += countNodes(child);
        }

        return count;
    }

    /**
     * 统计信息集数量(调试用)
     *
     * @param root 根节点
     * @return 信息集数量
     */
    public int countInfoSets(TreeNode root) {
        List<String> infoSets = new ArrayList<>();
        collectInfoSets(root, infoSets);
        return infoSets.size();
    }

    private void collectInfoSets(TreeNode node, List<String> infoSets) {
        if (node.isTerminal()) {
            return;
        }

        DecisionNode decisionNode = (DecisionNode) node;
        String infoSet = decisionNode.getInfoSetKey();

        if (!infoSets.contains(infoSet)) {
            infoSets.add(infoSet);
        }

        for (TreeNode child : decisionNode.getChildren().values()) {
            collectInfoSets(child, infoSets);
        }
    }

    /**
     * 打印博弈树(调试用)
     *
     * @param root 根节点
     */
    public void printTree(TreeNode root) {
        printTree(root, "", true);
    }

    private void printTree(TreeNode node, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node);

        if (!node.isTerminal()) {
            DecisionNode decisionNode = (DecisionNode) node;
            List<TreeNode> children = new ArrayList<>(decisionNode.getChildren().values());

            for (int i = 0; i < children.size(); i++) {
                boolean isLast = (i == children.size() - 1);
                printTree(children.get(i), prefix + (isTail ? "    " : "│   "), isLast);
            }
        }
    }
}
