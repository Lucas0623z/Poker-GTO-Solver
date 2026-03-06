package com.poker.gto.core.cards;

/**
 * 扑克牌点数
 *
 * 标准52张牌的13个等级，从2到A
 */
public enum Rank {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "T"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A");     // A最大，值为14

    private final int value;
    private final String symbol;

    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    /**
     * 获取点数的数值（用于比较大小）
     */
    public int getValue() {
        return value;
    }

    /**
     * 获取点数的符号
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * 从符号解析点数
     *
     * @param symbol 符号 (2-9, T, J, Q, K, A)
     * @return 对应的点数
     */
    public static Rank fromSymbol(String symbol) {
        for (Rank rank : values()) {
            if (rank.symbol.equalsIgnoreCase(symbol)) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid rank: " + symbol);
    }

    /**
     * 从数值获取点数
     *
     * @param value 数值 (2-14)
     * @return 对应的点数
     */
    public static Rank fromValue(int value) {
        for (Rank rank : values()) {
            if (rank.value == value) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid rank value: " + value);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
