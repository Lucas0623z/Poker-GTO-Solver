package com.poker.gto.cards;

import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.Suit;
import com.poker.gto.core.cards.TexasCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TexasCard 单元测试
 */
class TexasCardTest {

    @Test
    void testCardCreation() {
        TexasCard card = TexasCard.of(Rank.ACE, Suit.SPADES);
        assertNotNull(card);
        assertEquals(Rank.ACE, card.getRank());
        assertEquals(Suit.SPADES, card.getSuit());
    }

    @Test
    void testCardParsing() {
        TexasCard as = TexasCard.parse("As");
        assertEquals(Rank.ACE, as.getRank());
        assertEquals(Suit.SPADES, as.getSuit());

        TexasCard kh = TexasCard.parse("Kh");
        assertEquals(Rank.KING, kh.getRank());
        assertEquals(Suit.HEARTS, kh.getSuit());

        TexasCard td = TexasCard.parse("Td");
        assertEquals(Rank.TEN, td.getRank());
        assertEquals(Suit.DIAMONDS, td.getSuit());
    }

    @Test
    void testCardToString() {
        TexasCard card = TexasCard.of(Rank.ACE, Suit.SPADES);
        assertEquals("As", card.toString());

        TexasCard card2 = TexasCard.of(Rank.KING, Suit.HEARTS);
        assertEquals("Kh", card2.toString());
    }

    @Test
    void testCardUnicode() {
        TexasCard card = TexasCard.of(Rank.ACE, Suit.SPADES);
        assertEquals("A♠", card.toUnicodeString());
    }

    @Test
    void testCardComparison() {
        TexasCard ace = TexasCard.of(Rank.ACE, Suit.SPADES);
        TexasCard king = TexasCard.of(Rank.KING, Suit.SPADES);
        TexasCard two = TexasCard.of(Rank.TWO, Suit.HEARTS);

        assertTrue(ace.compareTo(king) > 0);
        assertTrue(king.compareTo(two) > 0);
        assertTrue(two.compareTo(ace) < 0);
    }

    @Test
    void testCardEquality() {
        TexasCard as1 = TexasCard.of(Rank.ACE, Suit.SPADES);
        TexasCard as2 = TexasCard.parse("As");

        // 应该是同一个实例（因为使用了缓存）
        assertSame(as1, as2);
        assertEquals(as1, as2);

        TexasCard ah = TexasCard.of(Rank.ACE, Suit.HEARTS);
        assertNotEquals(as1, ah);
    }

    @Test
    void testConstants() {
        assertNotNull(TexasCard.ACE_SPADES);
        assertEquals("As", TexasCard.ACE_SPADES.toString());

        assertNotNull(TexasCard.KING_HEARTS);
        assertEquals("Kh", TexasCard.KING_HEARTS.toString());
    }

    @Test
    void testInvalidCardParsing() {
        assertThrows(IllegalArgumentException.class, () -> TexasCard.parse("Xs"));
        assertThrows(IllegalArgumentException.class, () -> TexasCard.parse("Ax"));
        assertThrows(IllegalArgumentException.class, () -> TexasCard.parse("A"));
        assertThrows(IllegalArgumentException.class, () -> TexasCard.parse(""));
    }
}
