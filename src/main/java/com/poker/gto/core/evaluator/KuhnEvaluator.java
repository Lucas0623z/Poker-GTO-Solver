package com.poker.gto.core.evaluator;

import com.poker.gto.core.cards.Card;

/**
 * Kuhn Poker 评估器
 *
 * 由于 Kuhn Poker 只有 3 张牌 (J, Q, K),评估非常简单:
 * K > Q > J
 */
public class KuhnEvaluator {

    /**
     * 比较两张牌的强弱
     *
     * @param card1 第一张牌
     * @param card2 第二张牌
     * @return 正数表示 card1 更大,0 表示相等(不可能),负数表示 card2 更大
     */
    public int compare(Card card1, Card card2) {
        return card1.compareTo(card2);
    }

    /**
     * 判断 card1 是否比 card2 强
     *
     * @param card1 第一张牌
     * @param card2 第二张牌
     * @return card1 是否更强
     */
    public boolean isStronger(Card card1, Card card2) {
        return compare(card1, card2) > 0;
    }

    /**
     * 判断 card1 是否比 card2 弱
     *
     * @param card1 第一张牌
     * @param card2 第二张牌
     * @return card1 是否更弱
     */
    public boolean isWeaker(Card card1, Card card2) {
        return compare(card1, card2) < 0;
    }

    /**
     * 判断两张牌是否相等(在 Kuhn Poker 中不可能)
     *
     * @param card1 第一张牌
     * @param card2 第二张牌
     * @return 是否相等
     */
    public boolean isEqual(Card card1, Card card2) {
        return compare(card1, card2) == 0;
    }

    /**
     * 获取最强的牌
     *
     * @param card1 第一张牌
     * @param card2 第二张牌
     * @return 更强的那张牌
     */
    public Card getWinner(Card card1, Card card2) {
        return compare(card1, card2) > 0 ? card1 : card2;
    }

    /**
     * 获取牌的点数(用于调试)
     *
     * @param card 牌
     * @return 点数值
     */
    public int getValue(Card card) {
        return card.getValue();
    }
}
