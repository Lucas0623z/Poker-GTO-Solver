package com.poker.gto.core.evaluator;

/**
 * 德州扑克手牌牌型等级
 *
 * 从最弱到最强排列
 */
public enum HandRank {
    HIGH_CARD(0, "High Card"),          // 高牌
    ONE_PAIR(1, "One Pair"),            // 一对
    TWO_PAIR(2, "Two Pair"),            // 两对
    THREE_OF_A_KIND(3, "Three of a Kind"), // 三条
    STRAIGHT(4, "Straight"),            // 顺子
    FLUSH(5, "Flush"),                  // 同花
    FULL_HOUSE(6, "Full House"),        // 葫芦（满堂红）
    FOUR_OF_A_KIND(7, "Four of a Kind"), // 四条
    STRAIGHT_FLUSH(8, "Straight Flush"), // 同花顺
    ROYAL_FLUSH(9, "Royal Flush");       // 皇家同花顺

    private final int strength;
    private final String displayName;

    HandRank(int strength, String displayName) {
        this.strength = strength;
        this.displayName = displayName;
    }

    /**
     * 获取牌型强度（数值越大越强）
     */
    public int getStrength() {
        return strength;
    }

    /**
     * 获取牌型的显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 比较两个牌型的强度
     *
     * @param other 另一个牌型
     * @return 正数表示本牌型更强
     */
    public int compareStrength(HandRank other) {
        return Integer.compare(this.strength, other.strength);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
