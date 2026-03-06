package com.poker.gto.core.cards;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 表示一张标准52张牌的扑克牌
 *
 * 包含点数(Rank)和花色(Suit)
 * 这个类是不可变的 (immutable)
 *
 * 示例:
 * - As (Ace of Spades)
 * - Kh (King of Hearts)
 * - 7d (7 of Diamonds)
 */
public class TexasCard implements Comparable<TexasCard> {

    private final Rank rank;
    private final Suit suit;

    // 缓存所有52张牌实例，避免重复创建
    private static final Map<String, TexasCard> CARD_CACHE = new HashMap<>();

    static {
        // 预生成所有52张牌
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                String key = rank.getSymbol() + suit.getShortName();
                CARD_CACHE.put(key, new TexasCard(rank, suit));
            }
        }
    }

    /**
     * 私有构造函数，使用工厂方法创建
     */
    private TexasCard(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");
        this.suit = Objects.requireNonNull(suit, "Suit cannot be null");
    }

    /**
     * 创建一张牌（工厂方法）
     *
     * @param rank 点数
     * @param suit 花色
     * @return 牌实例
     */
    public static TexasCard of(Rank rank, Suit suit) {
        String key = rank.getSymbol() + suit.getShortName();
        return CARD_CACHE.get(key);
    }

    /**
     * 从字符串解析牌
     *
     * @param cardString 牌的字符串表示，如 "As", "Kh", "7d"
     * @return 牌实例
     */
    public static TexasCard parse(String cardString) {
        if (cardString == null || cardString.length() != 2) {
            throw new IllegalArgumentException("Invalid card string: " + cardString);
        }

        String rankStr = cardString.substring(0, 1);
        String suitStr = cardString.substring(1, 2);

        Rank rank = Rank.fromSymbol(rankStr);
        Suit suit = Suit.fromShortName(suitStr);

        return of(rank, suit);
    }

    /**
     * 获取点数
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * 获取花色
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * 获取点数的数值
     */
    public int getValue() {
        return rank.getValue();
    }

    /**
     * 比较牌的大小（仅比较点数，不考虑花色）
     */
    @Override
    public int compareTo(TexasCard other) {
        return Integer.compare(this.getValue(), other.getValue());
    }

    /**
     * 字符串表示
     *
     * @return 如 "As", "Kh", "7d"
     */
    @Override
    public String toString() {
        return rank.getSymbol() + suit.getShortName();
    }

    /**
     * 带Unicode符号的字符串表示
     *
     * @return 如 "A♠", "K♥", "7♦"
     */
    public String toUnicodeString() {
        return rank.getSymbol() + suit.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TexasCard card = (TexasCard) o;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    // ========== 常用牌常量 ==========

    // Aces
    public static final TexasCard ACE_SPADES = of(Rank.ACE, Suit.SPADES);
    public static final TexasCard ACE_HEARTS = of(Rank.ACE, Suit.HEARTS);
    public static final TexasCard ACE_DIAMONDS = of(Rank.ACE, Suit.DIAMONDS);
    public static final TexasCard ACE_CLUBS = of(Rank.ACE, Suit.CLUBS);

    // Kings
    public static final TexasCard KING_SPADES = of(Rank.KING, Suit.SPADES);
    public static final TexasCard KING_HEARTS = of(Rank.KING, Suit.HEARTS);
    public static final TexasCard KING_DIAMONDS = of(Rank.KING, Suit.DIAMONDS);
    public static final TexasCard KING_CLUBS = of(Rank.KING, Suit.CLUBS);

    // Queens
    public static final TexasCard QUEEN_SPADES = of(Rank.QUEEN, Suit.SPADES);
    public static final TexasCard QUEEN_HEARTS = of(Rank.QUEEN, Suit.HEARTS);
    public static final TexasCard QUEEN_DIAMONDS = of(Rank.QUEEN, Suit.DIAMONDS);
    public static final TexasCard QUEEN_CLUBS = of(Rank.QUEEN, Suit.CLUBS);

    // Jacks
    public static final TexasCard JACK_SPADES = of(Rank.JACK, Suit.SPADES);
    public static final TexasCard JACK_HEARTS = of(Rank.JACK, Suit.HEARTS);
    public static final TexasCard JACK_DIAMONDS = of(Rank.JACK, Suit.DIAMONDS);
    public static final TexasCard JACK_CLUBS = of(Rank.JACK, Suit.CLUBS);
}
