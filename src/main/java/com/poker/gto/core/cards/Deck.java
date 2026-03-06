package com.poker.gto.core.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 扑克牌牌组
 *
 * 标准52张牌的牌组，支持洗牌和发牌
 */
public class Deck {

    private final List<TexasCard> cards;
    private int nextCardIndex;
    private final Random random;

    /**
     * 创建一副新牌（已洗牌）
     */
    public Deck() {
        this(new Random());
    }

    /**
     * 创建一副新牌（使用指定随机数生成器）
     *
     * @param random 随机数生成器（用于测试时指定种子）
     */
    public Deck(Random random) {
        this.cards = new ArrayList<>(52);
        this.random = random;
        reset();
        shuffle();
    }

    /**
     * 重置牌组（恢复到完整的52张牌，未洗牌）
     */
    public void reset() {
        cards.clear();
        nextCardIndex = 0;

        // 添加所有52张牌
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(TexasCard.of(rank, suit));
            }
        }
    }

    /**
     * 洗牌
     */
    public void shuffle() {
        Collections.shuffle(cards, random);
        nextCardIndex = 0;
    }

    /**
     * 发一张牌
     *
     * @return 牌
     * @throws IllegalStateException 如果牌已发完
     */
    public TexasCard deal() {
        if (nextCardIndex >= cards.size()) {
            throw new IllegalStateException("No more cards in deck");
        }
        return cards.get(nextCardIndex++);
    }

    /**
     * 发多张牌
     *
     * @param count 发牌数量
     * @return 牌列表
     */
    public List<TexasCard> deal(int count) {
        if (nextCardIndex + count > cards.size()) {
            throw new IllegalStateException(
                String.format("Not enough cards. Requested: %d, Available: %d",
                    count, cards.size() - nextCardIndex)
            );
        }

        List<TexasCard> dealt = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            dealt.add(deal());
        }
        return dealt;
    }

    /**
     * 移除指定的牌（用于设置已知牌）
     *
     * @param card 要移除的牌
     * @return 是否成功移除
     */
    public boolean remove(TexasCard card) {
        return cards.remove(card);
    }

    /**
     * 移除多张牌
     *
     * @param cardsToRemove 要移除的牌列表
     */
    public void removeAll(List<TexasCard> cardsToRemove) {
        cards.removeAll(cardsToRemove);
        nextCardIndex = 0;  // 重置索引
    }

    /**
     * 获取剩余牌数
     */
    public int remainingCards() {
        return cards.size() - nextCardIndex;
    }

    /**
     * 获取牌组大小
     */
    public int size() {
        return cards.size();
    }

    /**
     * 是否已发完所有牌
     */
    public boolean isEmpty() {
        return nextCardIndex >= cards.size();
    }

    /**
     * 获取剩余的牌（不移除）
     */
    public List<TexasCard> getRemainingCards() {
        return new ArrayList<>(cards.subList(nextCardIndex, cards.size()));
    }

    /**
     * 创建一副移除了指定牌的牌组（用于计算equity）
     *
     * @param deadCards 已知的死牌
     * @return 新的牌组
     */
    public static Deck createWithDeadCards(List<TexasCard> deadCards) {
        Deck deck = new Deck();
        deck.removeAll(deadCards);
        deck.shuffle();
        return deck;
    }

    @Override
    public String toString() {
        return String.format("Deck{total=%d, remaining=%d, dealt=%d}",
            cards.size(), remainingCards(), nextCardIndex);
    }
}
