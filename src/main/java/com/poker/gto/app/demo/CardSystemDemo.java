package com.poker.gto.app.demo;

import com.poker.gto.core.cards.*;
import com.poker.gto.core.evaluator.HandRank;

import java.util.List;

/**
 * 卡牌系统演示程序
 *
 * 测试52张牌系统的基本功能
 */
public class CardSystemDemo {

    public static void main(String[] args) {
        System.out.println("=== 德州扑克卡牌系统测试 ===\n");

        testRankAndSuit();
        testTexasCard();
        testDeck();
        testHandRank();

        System.out.println("\n✅ 所有基础功能测试通过！");
    }

    /**
     * 测试Rank和Suit
     */
    private static void testRankAndSuit() {
        System.out.println("1. 测试 Rank 和 Suit");
        System.out.println("   Ranks: " + Rank.values().length + " 个");
        System.out.println("   Suits: " + Suit.values().length + " 个");

        // 测试Rank
        assertEquals("Rank count", 13, Rank.values().length);
        assertEquals("Ace value", 14, Rank.ACE.getValue());
        assertEquals("Two value", 2, Rank.TWO.getValue());

        // 测试Suit
        assertEquals("Suit count", 4, Suit.values().length);
        assertEquals("Spades symbol", "♠", Suit.SPADES.getSymbol());

        // 测试解析
        Rank ace = Rank.fromSymbol("A");
        assertEquals("Parse Ace", Rank.ACE, ace);

        Suit hearts = Suit.fromShortName("h");
        assertEquals("Parse Hearts", Suit.HEARTS, hearts);

        System.out.println("   ✓ Rank 和 Suit 测试通过\n");
    }

    /**
     * 测试TexasCard
     */
    private static void testTexasCard() {
        System.out.println("2. 测试 TexasCard");

        // 创建牌
        TexasCard as = TexasCard.of(Rank.ACE, Suit.SPADES);
        System.out.println("   创建牌: " + as + " (Unicode: " + as.toUnicodeString() + ")");

        // 解析牌
        TexasCard kh = TexasCard.parse("Kh");
        System.out.println("   解析牌: " + kh + " (Unicode: " + kh.toUnicodeString() + ")");

        // 测试比较
        assertTrue("As > Kh", as.compareTo(kh) > 0);
        System.out.println("   比较: As > Kh ✓");

        // 测试缓存（同一张牌应该是同一个实例）
        TexasCard as2 = TexasCard.parse("As");
        assertTrue("Card cache", as == as2);
        System.out.println("   缓存: 同一张牌是同一实例 ✓");

        // 测试常量
        assertNotNull("Constant exists", TexasCard.ACE_SPADES);
        assertEquals("Constant value", "As", TexasCard.ACE_SPADES.toString());
        System.out.println("   常量: ACE_SPADES = " + TexasCard.ACE_SPADES + " ✓");

        System.out.println("   ✓ TexasCard 测试通过\n");
    }

    /**
     * 测试Deck
     */
    private static void testDeck() {
        System.out.println("3. 测试 Deck");

        // 创建新牌组
        Deck deck = new Deck();
        assertEquals("Deck size", 52, deck.size());
        System.out.println("   新牌组: " + deck);

        // 发牌
        TexasCard card1 = deck.deal();
        TexasCard card2 = deck.deal();
        System.out.println("   发出2张牌: " + card1 + ", " + card2);
        assertEquals("Remaining cards", 50, deck.remainingCards());

        // 发多张牌
        List<TexasCard> cards = deck.deal(5);
        System.out.println("   发出5张牌: " + cards);
        assertEquals("Remaining after deal 5", 45, deck.remainingCards());

        // 重置
        deck.reset();
        assertEquals("After reset", 52, deck.size());
        System.out.println("   重置后: " + deck);

        // 移除已知牌
        deck.remove(TexasCard.ACE_SPADES);
        deck.remove(TexasCard.ACE_HEARTS);
        assertEquals("After removing 2 cards", 50, deck.size());
        System.out.println("   移除2张A后: " + deck);

        System.out.println("   ✓ Deck 测试通过\n");
    }

    /**
     * 测试HandRank
     */
    private static void testHandRank() {
        System.out.println("4. 测试 HandRank");

        // 测试所有牌型
        assertEquals("HandRank count", 10, HandRank.values().length);
        System.out.println("   牌型数量: " + HandRank.values().length);

        // 显示所有牌型
        System.out.println("   所有牌型:");
        for (HandRank rank : HandRank.values()) {
            System.out.println("      " + rank.getStrength() + ". " + rank.getDisplayName());
        }

        // 测试强度比较
        assertTrue("Royal > Straight",
            HandRank.ROYAL_FLUSH.compareStrength(HandRank.STRAIGHT) > 0);
        assertTrue("Full House > Two Pair",
            HandRank.FULL_HOUSE.compareStrength(HandRank.TWO_PAIR) > 0);

        System.out.println("   ✓ HandRank 测试通过\n");
    }

    // ========== 辅助断言方法 ==========

    private static void assertEquals(String message, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message + ": condition is false");
        }
    }

    private static void assertNotNull(String message, Object obj) {
        if (obj == null) {
            throw new AssertionError(message + ": object is null");
        }
    }
}
