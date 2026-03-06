package com.poker.gto.core.tree;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.game_state.KuhnPokerState;

import java.util.List;

/**
 * 博弈树节点接口
 *
 * 博弈树节点有三种类型:
 * - DecisionNode: 玩家决策节点
 * - TerminalNode: 终局节点
 * - ChanceNode: 随机节点(发牌)
 *
 * 对于 Kuhn Poker,我们只需要 DecisionNode 和 TerminalNode
 */
public interface TreeNode {

    /**
     * 节点类型
     */
    enum NodeType {
        DECISION,   // 决策节点
        TERMINAL,   // 终局节点
        CHANCE      // 随机节点
    }

    /**
     * 获取节点类型
     */
    NodeType getType();

    /**
     * 获取游戏状态
     */
    KuhnPokerState getState();

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
