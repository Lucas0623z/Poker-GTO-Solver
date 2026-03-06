package com.poker.gto.core.tree;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.actions.KuhnActionGenerator;
import com.poker.gto.core.game_state.KuhnPokerState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 决策节点
 *
 * 表示某个玩家需要做决策的节点
 */
public class DecisionNode implements TreeNode {

    private final KuhnPokerState state;
    private final int actingPlayer;
    private final List<Action> actions;
    private final String infoSetKey;
    private final Map<Action, TreeNode> children;

    /**
     * 创建决策节点
     *
     * @param state         游戏状态
     * @param actionGenerator 动作生成器
     */
    public DecisionNode(KuhnPokerState state, KuhnActionGenerator actionGenerator) {
        this.state = state;
        this.actingPlayer = state.getCurrentPlayer();
        this.actions = actionGenerator.getLegalActions(state);
        this.infoSetKey = state.getInfoSetKey(actingPlayer);
        this.children = new HashMap<>();

        // 延迟构建子节点,避免栈溢出
    }

    /**
     * 构建子节点
     *
     * @param actionGenerator 动作生成器
     */
    public void buildChildren(KuhnActionGenerator actionGenerator) {
        for (Action action : actions) {
            KuhnPokerState newState = state.applyAction(action);
            TreeNode child;

            if (newState.isTerminal()) {
                child = new TerminalNode(newState);
            } else {
                child = new DecisionNode(newState, actionGenerator);
            }

            children.put(action, child);
        }
    }

    /**
     * 递归构建整个子树
     *
     * @param actionGenerator 动作生成器
     */
    public void buildSubtree(KuhnActionGenerator actionGenerator) {
        buildChildren(actionGenerator);

        for (TreeNode child : children.values()) {
            if (child instanceof DecisionNode) {
                ((DecisionNode) child).buildSubtree(actionGenerator);
            }
        }
    }

    @Override
    public NodeType getType() {
        return NodeType.DECISION;
    }

    @Override
    public KuhnPokerState getState() {
        return state;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String getInfoSetKey() {
        return infoSetKey;
    }

    /**
     * 获取行动玩家
     */
    public int getActingPlayer() {
        return actingPlayer;
    }

    /**
     * 获取合法动作列表
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * 获取执行某个动作后的子节点
     */
    public TreeNode getChild(Action action) {
        return children.get(action);
    }

    /**
     * 获取所有子节点
     */
    public Map<Action, TreeNode> getChildren() {
        return children;
    }

    /**
     * 是否已构建子节点
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("DecisionNode{player=%d, infoSet=%s, actions=%d}",
            actingPlayer, infoSetKey, actions.size());
    }
}
