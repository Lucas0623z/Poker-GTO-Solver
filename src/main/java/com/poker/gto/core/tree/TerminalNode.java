package com.poker.gto.core.tree;

import com.poker.gto.core.game_state.KuhnPokerState;

/**
 * 终局节点
 *
 * 表示游戏结束的节点,包含每个玩家的收益
 */
public class TerminalNode implements TreeNode {

    private final KuhnPokerState state;
    private final int[] payoffs;

    /**
     * 创建终局节点
     *
     * @param state 终局状态
     */
    public TerminalNode(KuhnPokerState state) {
        if (!state.isTerminal()) {
            throw new IllegalArgumentException("State must be terminal");
        }

        this.state = state;
        this.payoffs = state.getPayoffs();
    }

    @Override
    public NodeType getType() {
        return NodeType.TERMINAL;
    }

    @Override
    public KuhnPokerState getState() {
        return state;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public String getInfoSetKey() {
        return null;  // 终局节点没有信息集
    }

    /**
     * 获取玩家收益
     *
     * @param player 玩家编号
     * @return 该玩家的收益
     */
    public int getPayoff(int player) {
        return payoffs[player];
    }

    /**
     * 获取所有玩家的收益
     *
     * @return 收益数组
     */
    public int[] getPayoffs() {
        return payoffs.clone();
    }

    @Override
    public String toString() {
        return String.format("TerminalNode{payoffs=[%d, %d]}",
            payoffs[0], payoffs[1]);
    }
}
