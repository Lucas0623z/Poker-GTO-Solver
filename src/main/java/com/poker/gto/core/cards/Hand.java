package com.poker.gto.core.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示一手牌
 *
 * 可以是：
 * - 2张底牌（hole cards）
 * - 5张公共牌（board）
 * - 7张牌（2底牌 + 5公共牌）
 * - 或任意组合
 *
 * 这个类是不可变的
 */
public class Hand {

    private final List<TexasCard> cards;

    /**
     * 创建一手牌
     *
     * @param cards 牌列表
     */
    public Hand(List<TexasCard> cards) {
        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Hand cannot be empty");
        }
        this.cards = new ArrayList<>(cards);
    }

    /**
     * 从可变参数创建
     */
    public static Hand of(TexasCard... cards) {
        return new Hand(List.of(cards));
    }

    /**
     * 从字符串解析
     *
     * @param handString 如 "AsKh" 或 "As Kh Qd Jc Ts"
     */
    public static Hand parse(String handString) {
        String[] cardStrings = handString.trim().split("\\s+");
        List<TexasCard> cards = new ArrayList<>();

        // 如果是连续字符串（无空格），每2个字符一张牌
        if (cardStrings.length == 1 && cardStrings[0].length() > 2) {
            String str = cardStrings[0];
            if (str.length() % 2 != 0) {
                throw new IllegalArgumentException("Invalid hand string: " + handString);
            }
            for (int i = 0; i < str.length(); i += 2) {
                String cardStr = str.substring(i, i + 2);
                cards.add(TexasCard.parse(cardStr));
            }
        } else {
            // 空格分隔
            for (String cardStr : cardStrings) {
                cards.add(TexasCard.parse(cardStr));
            }
        }

        return new Hand(cards);
    }

    /**
     * 获取所有牌
     */
    public List<TexasCard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * 获取牌数
     */
    public int size() {
        return cards.size();
    }

    /**
     * 是否包含某张牌
     */
    public boolean contains(TexasCard card) {
        return cards.contains(card);
    }

    /**
     * 获取指定位置的牌
     */
    public TexasCard getCard(int index) {
        return cards.get(index);
    }

    /**
     * 添加牌（返回新的Hand）
     */
    public Hand addCard(TexasCard card) {
        List<TexasCard> newCards = new ArrayList<>(cards);
        newCards.add(card);
        return new Hand(newCards);
    }

    /**
     * 添加多张牌（返回新的Hand）
     */
    public Hand addCards(List<TexasCard> cardsToAdd) {
        List<TexasCard> newCards = new ArrayList<>(cards);
        newCards.addAll(cardsToAdd);
        return new Hand(newCards);
    }

    /**
     * 合并两手牌
     */
    public Hand combine(Hand other) {
        return addCards(other.cards);
    }

    /**
     * 获取按点数排序的牌（降序）
     */
    public List<TexasCard> getSortedCards() {
        List<TexasCard> sorted = new ArrayList<>(cards);
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return sorted;
    }

    @Override
    public String toString() {
        return cards.stream()
                    .map(TexasCard::toString)
                    .collect(Collectors.joining(" "));
    }

    /**
     * Unicode表示
     */
    public String toUnicodeString() {
        return cards.stream()
                    .map(TexasCard::toUnicodeString)
                    .collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hand hand = (Hand) o;
        return Objects.equals(cards, hand.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cards);
    }
}
