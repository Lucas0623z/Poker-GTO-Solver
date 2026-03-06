package com.poker.gto.app.demo;

import com.poker.gto.core.cards.*;
import com.poker.gto.core.game_state.RiverPlayerState;
import com.poker.gto.core.game_state.RiverState;
import com.poker.gto.core.ranges.Range;

import java.util.List;

/**
 * River阶段数据结构演示程序
 */
public class RiverDemo {

    public static void main(String[] args) {
        System.out.println("=== River阶段数据结构测试 ===\n");

        testHand();
        testRange();
        testRiverState();

        System.out.println("\n✅ Task 2.1 完成！所有数据结构测试通过！");
    }

    /**
     * 测试Hand类
     */
    private static void testHand() {
        System.out.println("1. 测试 Hand 类");

        // 创建手牌
        Hand hand = Hand.of(TexasCard.ACE_SPADES, TexasCard.KING_HEARTS);
        System.out.println("   创建手牌: " + hand + " (Unicode: " + hand.toUnicodeString() + ")");

        // 从字符串解析
        Hand hand2 = Hand.parse("AsKh");
        System.out.println("   解析手牌: " + hand2);
        assertEquals("Hand parsing", hand, hand2);

        // 解析多张牌（带空格）
        Hand board = Hand.parse("7h 8s 9c");
        System.out.println("   解析公共牌: " + board);
        assertEquals("Board size", 3, board.size());

        // 合并手牌
        Hand combined = hand.combine(board);
        System.out.println("   合并牌: " + combined + " (共" + combined.size() + "张)");
        assertEquals("Combined size", 5, combined.size());

        // 排序
        List<TexasCard> sorted = combined.getSortedCards();
        System.out.println("   排序后: " + sorted);

        System.out.println("   ✓ Hand 测试通过\n");
    }

    /**
     * 测试Range类
     */
    private static void testRange() {
        System.out.println("2. 测试 Range 类");

        // 从空范围开始，逐步添加
        Range range = Range.empty();

        // 添加口袋对 AA
        range = range.merge(Range.parse("AA"));
        System.out.println("   添加AA: " + range);
        assertEquals("AA combos", 6, (int) range.getTotalCombos());  // C(4,2) = 6

        // 添加同花 AKs
        range = range.merge(Range.parse("AKs"));
        System.out.println("   添加AKs: " + range);
        assertEquals("AA+AKs combos", 10, (int) range.getTotalCombos());  // 6 + 4

        // 添加非同花 AKo
        range = range.merge(Range.parse("AKo"));
        System.out.println("   添加AKo: " + range);
        assertEquals("AA+AK combos", 22, (int) range.getTotalCombos());  // 6 + 4 + 12

        // 从字符串解析
        Range range2 = Range.parse("AA,KK,QQ");
        System.out.println("   解析范围 'AA,KK,QQ': " + range2);
        assertEquals("Parsed range size", 18, (int) range2.getTotalCombos());  // 6 + 6 + 6

        // 移除冲突牌
        List<TexasCard> board = List.of(
            TexasCard.ACE_SPADES,
            TexasCard.parse("7h"),
            TexasCard.parse("8s"),
            TexasCard.parse("9c"),
            TexasCard.parse("Td")
        );
        double sizeBefore = range.getTotalCombos();
        range = range.removeDeadCards(board);
        System.out.println("   移除冲突牌后: " + range + " (移除了" + (int) (sizeBefore - range.getTotalCombos()) + "个组合)");

        System.out.println("   ✓ Range 测试通过\n");
    }

    /**
     * 测试RiverState类
     */
    private static void testRiverState() {
        System.out.println("3. 测试 RiverState 类");

        // 创建公共牌
        List<TexasCard> board = List.of(
            TexasCard.parse("7h"),
            TexasCard.parse("8s"),
            TexasCard.parse("9c"),
            TexasCard.parse("Td"),
            TexasCard.parse("Jh")
        );
        System.out.println("   公共牌: " + board);

        // 创建玩家
        Hand player0Hand = Hand.parse("AsKh");
        Hand player1Hand = Hand.parse("QdQc");

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        System.out.println("   P0手牌: " + player0Hand);
        System.out.println("   P1手牌: " + player1Hand);

        // 创建River状态
        RiverState state = new RiverState(board, player0, player1, 20);
        System.out.println("\n   初始状态:");
        System.out.println(state);

        // 测试信息集Key
        String infoSet0 = state.getInfoSetKey(0);
        String infoSet1 = state.getInfoSetKey(1);
        System.out.println("\n   P0信息集: " + infoSet0);
        System.out.println("   P1信息集: " + infoSet1);

        // 验证
        assertFalse("Not terminal initially", state.isTerminal());
        assertEquals("Current player", 0, state.getCurrentPlayer());
        assertEquals("Pot", 20, state.getPot());
        assertEquals("Board size", 5, state.getBoard().size());

        System.out.println("\n   ✓ RiverState 测试通过\n");
    }

    // ========== 辅助断言方法 ==========

    private static void assertEquals(String message, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new AssertionError(message + ": condition is true");
        }
    }
}
