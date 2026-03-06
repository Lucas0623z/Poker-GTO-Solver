package com.poker.gto.core.tree;

import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.evaluator.EvaluatedHand;
import com.poker.gto.core.evaluator.HandEvaluator;
import com.poker.gto.core.game_state.RiverPlayerState;
import com.poker.gto.core.game_state.RiverState;

/**
 * River终局节点
 *
 * 表示游戏结束的节点,包含每个玩家的收益
 */
public class RiverTerminalNode implements RiverTreeNode {

    private final RiverState state;
    private final double[] payoffs;  // 使用double支持范围计算的期望收益

    /**
     * 创建终局节点
     *
     * @param state 终局状态
     */
    public RiverTerminalNode(RiverState state) {
        if (!state.isTerminal()) {
            throw new IllegalArgumentException("State must be terminal");
        }

        this.state = state;
        this.payoffs = calculatePayoffs(state);
    }

    /**
     * 计算玩家收益
     */
    private double[] calculatePayoffs(RiverState state) {
        double[] payoffs = new double[2];

        RiverPlayerState player0 = state.getPlayer(0);
        RiverPlayerState player1 = state.getPlayer(1);

        int pot = state.getPot();
        int invested0 = player0.getInvested();
        int invested1 = player1.getInvested();
        int initialPot = pot - invested0 - invested1;
        double halfInitialPot = initialPot / 2.0;

        // 检查是否有人弃牌
        if (player0.hasFolded()) {
            // 玩家0弃牌,玩家1获胜
            // P1无需showdown就赢得初始底池的P0那一半，同时得到P0的投入
            payoffs[0] = -halfInitialPot - invested0;
            payoffs[1] = halfInitialPot + invested0;
            return payoffs;
        }

        if (player1.hasFolded()) {
            // 玩家1弃牌,玩家0获胜
            // P0无需showdown就赢得初始底池的P1那一半，同时得到P1的投入
            payoffs[0] = halfInitialPot + invested1;
            payoffs[1] = -halfInitialPot - invested1;
            return payoffs;
        }

        // 摊牌 (Showdown)
        // 如果双方都有具体手牌,直接比较
        if (player0.hasHand() && player1.hasHand()) {
            return calculateShowdownPayoffs(state, player0, player1);
        }

        // 如果有范围,计算期望收益
        // 简化：这里假设双方都有具体手牌
        // 在实际CFR中,会使用范围计算
        return calculateShowdownPayoffs(state, player0, player1);
    }

    /**
     * 计算摊牌收益
     *
     * River收益计算（零和博弈）：
     * - pot = initialPot + invested0 + invested1
     * - initialPot是River开始前的底池（双方各占一半）
     * -赢家收益 = initialPot/2 + invested1 - invested0
     * - 输家收益 = -initialPot/2 - invested1
     * - 验证零和：winner + loser = initialPot/2 + inv1 - inv0 - initialPot/2 - inv1
     *                            = -inv0 (when inv0=0) = 0 ✓
     */
    private double[] calculateShowdownPayoffs(
        RiverState state,
        RiverPlayerState player0,
        RiverPlayerState player1
    ) {
        double[] payoffs = new double[2];

        // 评估双方手牌
        Hand fullHand0 = player0.getHand().combine(state.getBoardAsHand());
        Hand fullHand1 = player1.getHand().combine(state.getBoardAsHand());

        EvaluatedHand eval0 = HandEvaluator.evaluate(fullHand0);
        EvaluatedHand eval1 = HandEvaluator.evaluate(fullHand1);

        // 比较
        int comparison = eval0.compareTo(eval1);

        int pot = state.getPot();
        int invested0 = player0.getInvested();
        int invested1 = player1.getInvested();

        // 计算初始底池（River开始前）
        int initialPot = pot - invested0 - invested1;
        double halfInitialPot = initialPot / 2.0;

        if (comparison > 0) {
            // 玩家0获胜
            payoffs[0] = halfInitialPot + invested1 - invested0;
            payoffs[1] = -halfInitialPot - invested1;
        } else if (comparison < 0) {
            // 玩家1获胜
            payoffs[0] = -halfInitialPot - invested0;
            payoffs[1] = halfInitialPot + invested0 - invested1;
        } else {
            // 平局,平分底池
            payoffs[0] = -(invested0 - invested1) / 2.0;
            payoffs[1] = -(invested1 - invested0) / 2.0;
        }

        return payoffs;
    }

    @Override
    public NodeType getType() {
        return NodeType.TERMINAL;
    }

    @Override
    public RiverState getState() {
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
    public double getPayoff(int player) {
        return payoffs[player];
    }

    /**
     * 获取所有玩家的收益
     *
     * @return 收益数组
     */
    public double[] getPayoffs() {
        return payoffs.clone();
    }

    @Override
    public String toString() {
        return String.format("RiverTerminalNode{payoffs=[%.1f, %.1f]}",
            payoffs[0], payoffs[1]);
    }
}
