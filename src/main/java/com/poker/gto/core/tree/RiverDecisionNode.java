package com.poker.gto.core.tree;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.actions.RiverActionGenerator;
import com.poker.gto.core.game_state.RiverState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * River决策节点
 *
 * 表示某个玩家需要做决策的节点
 */
public class RiverDecisionNode implements RiverTreeNode {

    private final RiverState state;
    private final int actingPlayer;
    private final List<Action> actions;
    private final String infoSetKey;
    private final Map<Action, RiverTreeNode> children;

    /**
     * 创建决策节点
     *
     * @param state           游戏状态
     * @param actionGenerator 动作生成器
     */
    public RiverDecisionNode(RiverState state, RiverActionGenerator actionGenerator) {
        this.state = state;
        this.actingPlayer = state.getCurrentPlayer();
        this.actions = actionGenerator.generateActions(state);
        this.infoSetKey = state.getInfoSetKey(actingPlayer);
        this.children = new HashMap<>();

        // 延迟构建子节点,避免栈溢出
    }

    /**
     * 构建子节点
     *
     * @param actionGenerator 动作生成器
     */
    public void buildChildren(RiverActionGenerator actionGenerator) {
        for (Action action : actions) {
            RiverState newState = state.applyAction(action);
            RiverTreeNode child;

            if (newState.isTerminal()) {
                child = new RiverTerminalNode(newState);
            } else {
                child = new RiverDecisionNode(newState, actionGenerator);
            }

            children.put(action, child);
        }
    }

    /**
     * 递归构建整个子树
     *
     * @param actionGenerator 动作生成器
     */
    public void buildSubtree(RiverActionGenerator actionGenerator) {
        buildChildren(actionGenerator);

        for (RiverTreeNode child : children.values()) {
            if (child instanceof RiverDecisionNode) {
                ((RiverDecisionNode) child).buildSubtree(actionGenerator);
            }
        }
    }

    @Override
    public NodeType getType() {
        return NodeType.DECISION;
    }

    @Override
    public RiverState getState() {
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
    public RiverTreeNode getChild(Action action) {
        return children.get(action);
    }

    /**
     * 获取所有子节点
     */
    public Map<Action, RiverTreeNode> getChildren() {
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
        return String.format("RiverDecisionNode{player=%d, infoSet=%s, actions=%d}",
            actingPlayer, infoSetKey, actions.size());
    }
}
