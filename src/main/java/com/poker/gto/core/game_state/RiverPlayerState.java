package com.poker.gto.core.game_state;

import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.ranges.Range;

/**
 * River阶段的玩家状态
 *
 * 包含玩家的筹码、已投入、手牌/范围等信息
 */
public class RiverPlayerState {

    private final int playerIndex;
    private int stack;              // 剩余筹码
    private int invested;           // 本轮已投入
    private Hand hand;              // 具体手牌（如果已知）
    private Range range;            // 手牌范围（如果未知具体手牌）
    private boolean folded;

    /**
     * 创建玩家状态（使用具体手牌）
     */
    public RiverPlayerState(int playerIndex, int stack, Hand hand) {
        this.playerIndex = playerIndex;
        this.stack = stack;
        this.invested = 0;
        this.hand = hand;
        this.range = null;
        this.folded = false;
    }

    /**
     * 创建玩家状态（使用范围）
     */
    public RiverPlayerState(int playerIndex, int stack, Range range) {
        this.playerIndex = playerIndex;
        this.stack = stack;
        this.invested = 0;
        this.hand = null;
        this.range = range;
        this.folded = false;
    }

    /**
     * 深拷贝构造函数（保留所有状态）
     */
    private RiverPlayerState(int playerIndex, int stack, int invested, Hand hand, Range range, boolean folded) {
        this.playerIndex = playerIndex;
        this.stack = stack;
        this.invested = invested;
        this.hand = hand;
        this.range = range;
        this.folded = folded;
    }

    /**
     * 创建此玩家状态的深拷贝
     */
    public RiverPlayerState copy() {
        return new RiverPlayerState(playerIndex, stack, invested, hand, range, folded);
    }

    /**
     * 投入筹码
     */
    public void invest(int amount) {
        if (amount > stack) {
            throw new IllegalArgumentException("Cannot invest more than stack");
        }
        stack -= amount;
        invested += amount;
    }

    /**
     * 弃牌
     */
    public void fold() {
        folded = true;
    }

    /**
     * 是否可以行动
     */
    public boolean canAct() {
        return !folded && stack > 0;
    }

    /**
     * 是否All-in
     */
    public boolean isAllIn() {
        return stack == 0 && !folded;
    }

    // ========== Getters ==========

    public int getPlayerIndex() {
        return playerIndex;
    }

    public int getStack() {
        return stack;
    }

    public int getInvested() {
        return invested;
    }

    public Hand getHand() {
        return hand;
    }

    public Range getRange() {
        return range;
    }

    public boolean hasFolded() {
        return folded;
    }

    public boolean hasHand() {
        return hand != null;
    }

    public boolean hasRange() {
        return range != null;
    }

    @Override
    public String toString() {
        return String.format("Player%d{stack=%d, invested=%d, folded=%s}",
            playerIndex, stack, invested, folded);
    }
}
