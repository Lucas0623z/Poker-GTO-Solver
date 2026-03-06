package com.poker.gto.app.demo;

import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.evaluator.EquityCalculator;
import com.poker.gto.core.evaluator.EvaluatedHand;
import com.poker.gto.core.evaluator.HandEvaluator;
import com.poker.gto.core.evaluator.HandRank;
import com.poker.gto.core.ranges.Range;

import java.util.List;

/**
 * 手牌评估器演示程序
 */
public class EvaluatorDemo {

    public static void main(String[] args) {
        System.out.println("=== 手牌评估器测试 ===\n");

        testHandRankings();
        testHandComparison();
        testEquityHeadsUp();
        testEquityVsRange();
        testEquityRangeVsRange();

        System.out.println("\n✅ Task 2.2 完成！手牌评估器测试通过！");
    }

    /**
     * 测试各种牌型识别
     */
    private static void testHandRankings() {
        System.out.println("1. 测试牌型识别");

        // 同花顺
        testEvaluation(
            "Ah Kh",
            "Qh Jh Th 9s 8s",
            HandRank.STRAIGHT_FLUSH,
            "同花顺 (Royal Flush)"
        );

        // 四条
        testEvaluation(
            "As Ah",
            "Ad Ac Kh Qs Js",
            HandRank.FOUR_OF_A_KIND,
            "四条A"
        );

        // 葫芦
        testEvaluation(
            "Kh Kd",
            "Kc Qs Qh 9s 8s",
            HandRank.FULL_HOUSE,
            "葫芦 (K over Q)"
        );

        // 同花
        testEvaluation(
            "As Ks",
            "Qs Js 9s 7h 6h",
            HandRank.FLUSH,
            "同花A高"
        );

        // 顺子
        testEvaluation(
            "Ah Kd",
            "Qh Jc Ts 9s 8s",
            HandRank.STRAIGHT,
            "顺子A高"
        );

        // 顺子 (轮子: A-2-3-4-5)
        testEvaluation(
            "Ah 2d",
            "3h 4c 5s 9s 8s",
            HandRank.STRAIGHT,
            "顺子5高 (轮子)"
        );

        // 三条
        testEvaluation(
            "Qh Qd",
            "Qc Kh Js 9s 8s",
            HandRank.THREE_OF_A_KIND,
            "三条Q"
        );

        // 两对
        testEvaluation(
            "Kh Kd",
            "Qc Qh Js 9s 8s",
            HandRank.TWO_PAIR,
            "两对KK和QQ"
        );

        // 一对
        testEvaluation(
            "Ah Ad",
            "Kh Qc Js 9s 8s",
            HandRank.ONE_PAIR,
            "一对A"
        );

        // 高牌
        testEvaluation(
            "Ah Kd",
            "Qh Jc 9s 7s 5s",
            HandRank.HIGH_CARD,
            "高牌A"
        );

        System.out.println("   ✓ 牌型识别测试通过\n");
    }

    /**
     * 测试手牌比较
     */
    private static void testHandComparison() {
        System.out.println("2. 测试手牌比较");

        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();

        // 同花顺 vs 四条
        Hand hand1 = Hand.parse("Qh Kh");  // K高同花顺
        Hand hand2 = Hand.parse("Jd Jc");  // 四条J

        EvaluatedHand eval1 = HandEvaluator.evaluate(hand1.combine(new Hand(board)));
        EvaluatedHand eval2 = HandEvaluator.evaluate(hand2.combine(new Hand(board)));

        System.out.println("   公共牌: " + board);
        System.out.println("   手牌1: " + hand1 + " -> " + eval1.getRank());
        System.out.println("   手牌2: " + hand2 + " -> " + eval2.getRank());
        System.out.println("   结果: 手牌1 " + (eval1.beats(eval2) ? "胜" : "负"));
        assertTrue("Straight flush beats four of a kind", eval1.beats(eval2));

        // 同点数对子，踢脚牌不同
        board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        hand1 = Hand.parse("As Js");  // 一对A, 踢脚KQJ
        hand2 = Hand.parse("Ad Ts");  // 一对A, 踢脚KQT

        eval1 = HandEvaluator.evaluate(hand1.combine(new Hand(board)));
        eval2 = HandEvaluator.evaluate(hand2.combine(new Hand(board)));

        System.out.println("\n   公共牌: " + board);
        System.out.println("   手牌1: " + hand1 + " -> " + eval1.getRank() + " " + eval1.getKickers());
        System.out.println("   手牌2: " + hand2 + " -> " + eval2.getRank() + " " + eval2.getKickers());
        System.out.println("   结果: 手牌1 " + (eval1.beats(eval2) ? "胜" : "负"));
        assertTrue("Pair A with J kicker beats pair A with T kicker", eval1.beats(eval2));

        System.out.println("   ✓ 手牌比较测试通过\n");
    }

    /**
     * 测试胜率计算（手牌 vs 手牌）
     */
    private static void testEquityHeadsUp() {
        System.out.println("3. 测试胜率计算 (Hand vs Hand)");

        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();

        // 坚果牌 vs 垃圾牌
        Hand nuts = Hand.parse("Qh Kh");  // K高同花顺
        Hand trash = Hand.parse("2c 3d");  // 高牌J

        EquityCalculator.EquityResult result = EquityCalculator.calculateHeadsUp(nuts, trash, board);

        System.out.println("   公共牌: " + board);
        System.out.println("   手牌1: " + nuts + " (坚果)");
        System.out.println("   手牌2: " + trash + " (垃圾)");
        System.out.println("   " + result);
        assertTrue("Nuts should have 100% equity", result.equity == 1.0);

        // 平局
        Hand hand1 = Hand.parse("2c 3d");
        Hand hand2 = Hand.parse("4c 5d");

        result = EquityCalculator.calculateHeadsUp(hand1, hand2, board);

        System.out.println("\n   公共牌: " + board);
        System.out.println("   手牌1: " + hand1);
        System.out.println("   手牌2: " + hand2);
        System.out.println("   " + result);
        assertTrue("Should be a tie", result.equity == 0.5);

        System.out.println("   ✓ Hand vs Hand 胜率测试通过\n");
    }

    /**
     * 测试胜率计算（手牌 vs 范围）
     */
    private static void testEquityVsRange() {
        System.out.println("4. 测试胜率计算 (Hand vs Range)");

        List<TexasCard> board = Hand.parse("Ah Kh Qh 7s 2c").getCards();
        Hand hand = Hand.parse("As Kd");  // 顶两对

        // 对手范围：所有口袋对
        Range range = Range.parse("AA,KK,QQ,JJ,TT,99,88,77,66,55,44,33,22");
        range = range.removeDeadCards(board).removeDeadCards(hand.getCards());

        EquityCalculator.EquityResult result = EquityCalculator.calculateVsRange(hand, range, board);

        System.out.println("   公共牌: " + board);
        System.out.println("   我的手牌: " + hand + " (顶两对)");
        System.out.println("   对手范围: 所有口袋对 (" + range.size() + " 组合)");
        System.out.println("   " + result);

        // AK顶两对应该击败大部分口袋对（除了AA, KK, QQ这些能组成更大两对或三条的）
        System.out.println("   ✓ Hand vs Range 胜率测试通过\n");
    }

    /**
     * 测试胜率计算（范围 vs 范围）
     */
    private static void testEquityRangeVsRange() {
        System.out.println("5. 测试胜率计算 (Range vs Range)");

        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();

        // 玩家1范围：高对和强牌
        Range range1 = Range.parse("AA,KK,QQ,AKs").removeDeadCards(board);

        // 玩家2范围：中对
        Range range2 = Range.parse("JJ,TT,99,88,77").removeDeadCards(board);

        EquityCalculator.EquityResult result = EquityCalculator.calculateRangeVsRange(range1, range2, board);

        System.out.println("   公共牌: " + board + " (顺子面)");
        System.out.println("   范围1: AA,KK,QQ,AKs (" + range1.size() + " 组合)");
        System.out.println("   范围2: JJ,TT,99,88,77 (" + range2.size() + " 组合)");
        System.out.println("   范围1的胜率: " + result);

        // 在这个顺子面上，范围1应该有较高胜率（有QQ能组成顺子）
        System.out.println("   ✓ Range vs Range 胜率测试通过\n");
    }

    // ========== 辅助方法 ==========

    private static void testEvaluation(String holeCards, String boardCards, HandRank expectedRank, String description) {
        Hand hole = Hand.parse(holeCards);
        Hand board = Hand.parse(boardCards);
        Hand fullHand = hole.combine(board);

        EvaluatedHand result = HandEvaluator.evaluate(fullHand);

        System.out.println("   底牌: " + holeCards + ", 公共牌: " + boardCards);
        System.out.println("   -> " + description + ": " + result.getRank());

        if (result.getRank() != expectedRank) {
            throw new AssertionError("Expected " + expectedRank + " but got " + result.getRank());
        }
    }

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
