package com.poker.gto.core.tree;

import com.poker.gto.core.game_state.RiverState;

/**
 * River博弈树节点接口
 *
 * River阶段只需要两种节点类型:
 * - RiverDecisionNode: 玩家决策节点
 * - RiverTerminalNode: 终局节点
 */
public interface RiverTreeNode {

    /**
     * 节点类型
     */
    enum NodeType {
        DECISION,   // 决策节点
        TERMINAL    // 终局节点
    }

    /**
     * 获取节点类型
     */
    NodeType getType();

    /**
     * 获取游戏状态
     */
    RiverState getState();

    /**
     * 是否是终局节点
     */
    boolean isTerminal();

    /**
     * 获取信息集 Key(仅对 DecisionNode 有效)
     * 对于 TerminalNode,返回 null
     */
    String getInfoSetKey();
}
