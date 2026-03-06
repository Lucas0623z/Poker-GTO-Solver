package com.poker.gto.core.cards;

import java.util.Objects;

/**
 * 表示一张扑克牌（Kuhn Poker 简化版本）
 *
 * Kuhn Poker 只使用 3 张牌：Jack (J), Queen (Q), King (K)
 * 牌力大小：K > Q > J
 *
 * 这个类是不可变的 (immutable)
 */
public class Card implements Comparable<Card> {

    /**
     * 牌的点数
     */
    public enum Rank {
        JACK(11, "J"),
        QUEEN(12, "Q"),
        KING(13, "K");

        private final int value;
        private final String symbol;

        Rank(int value, String symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        public int getValue() {
            return value;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private final Rank rank;

    /**
     * 创建一张牌
     *
     * @param rank 牌的点数
     */
    public Card(Rank rank) {
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");
    }

    /**
     * 获取牌的点数
     *
     * @return 点数
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * 获取牌的数值（用于比较大小）
     *
     * @return 数值
     */
    public int getValue() {
        return rank.getValue();
    }

    /**
     * 比较两张牌的大小
     *
     * @param other 另一张牌
     * @return 负数表示小于，0 表示相等，正数表示大于
     */
    @Override
    public int compareTo(Card other) {
        return Integer.compare(this.getValue(), other.getValue());
    }

    /**
     * 字符串表示
     *
     * @return 牌的符号 (如 "J", "Q", "K")
     */
    @Override
    public String toString() {
        return rank.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank);
    }

    // 常量：预定义的 3 张牌
    public static final Card JACK = new Card(Rank.JACK);
    public static final Card QUEEN = new Card(Rank.QUEEN);
    public static final Card KING = new Card(Rank.KING);
}
