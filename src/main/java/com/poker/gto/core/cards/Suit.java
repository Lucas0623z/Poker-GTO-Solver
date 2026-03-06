package com.poker.gto.core.cards;

/**
 * 扑克牌花色
 *
 * 标准52张牌的4种花色
 */
public enum Suit {
    SPADES("♠", "s"),      // 黑桃
    HEARTS("♥", "h"),      // 红桃
    DIAMONDS("♦", "d"),    // 方块
    CLUBS("♣", "c");       // 梅花

    private final String symbol;      // Unicode符号
    private final String shortName;   // 简写 (s, h, d, c)

    Suit(String symbol, String shortName) {
        this.symbol = symbol;
        this.shortName = shortName;
    }

    /**
     * 获取花色的Unicode符号
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * 获取花色的简写
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * 从简写解析花色
     *
     * @param shortName 简写 (s/h/d/c)
     * @return 对应的花色
     */
    public static Suit fromShortName(String shortName) {
        for (Suit suit : values()) {
            if (suit.shortName.equalsIgnoreCase(shortName)) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Invalid suit: " + shortName);
    }

    @Override
    public String toString() {
        return shortName;
    }
}
