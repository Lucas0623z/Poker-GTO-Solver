package com.poker.gto.app.demo;

import com.poker.gto.core.cards.Deck;
import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.evaluator.EvaluatedHand;
import com.poker.gto.core.evaluator.HandEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * 性能测试 - 评估优化效果
 */
public class PerformanceTest {

    public static void main(String[] args) {
        System.out.println("=== 手牌评估器性能测试 ===\n");

        // 预热JVM
        System.out.println("预热JVM...");
        warmup();

        // 测试1: 单次评估性能
        System.out.println("\n1. 单次评估性能测试");
        testSingleEvaluation();

        // 测试2: 批量评估性能
        System.out.println("\n2. 批量评估性能测试");
        testBatchEvaluation(100_000);
        testBatchEvaluation(1_000_000);

        // 测试3: Equity计算性能
        System.out.println("\n3. Equity计算性能测试");
        testEquityCalculation();

        System.out.println("\n=== 性能测试完成 ===");
    }

    private static void warmup() {
        Deck deck = new Deck();
        deck.shuffle();
        List<TexasCard> cards = deck.deal(7);

        // 预热10000次
        for (int i = 0; i < 10000; i++) {
            HandEvaluator.evaluate(cards);
        }
        System.out.println("预热完成");
    }

    private static void testSingleEvaluation() {
        Deck deck = new Deck();
        deck.shuffle();
        List<TexasCard> cards = deck.deal(7);

        long start = System.nanoTime();
        EvaluatedHand result = HandEvaluator.evaluate(cards);
        long end = System.nanoTime();

        double microSeconds = (end - start) / 1000.0;
        System.out.printf("   单次评估耗时: %.2f 微秒\n", microSeconds);
        System.out.println("   评估结果: " + result);
    }

    private static void testBatchEvaluation(int iterations) {
        List<List<TexasCard>> testCases = new ArrayList<>();

        // 准备测试数据
        for (int i = 0; i < iterations; i++) {
            Deck deck = new Deck();
            deck.shuffle();
            testCases.add(deck.deal(7));
        }

        // 开始测试
        long start = System.nanoTime();
        for (List<TexasCard> cards : testCases) {
            HandEvaluator.evaluate(cards);
        }
        long end = System.nanoTime();

        double totalMs = (end - start) / 1_000_000.0;
        double avgMicros = (end - start) / (double) iterations / 1000.0;
        double throughput = iterations / (totalMs / 1000.0);

        System.out.printf("\n   %,d次评估:\n", iterations);
        System.out.printf("   - 总耗时: %.2f 毫秒\n", totalMs);
        System.out.printf("   - 平均耗时: %.2f 微秒/次\n", avgMicros);
        System.out.printf("   - 吞吐量: %,.0f 次/秒\n", throughput);
    }

    private static void testEquityCalculation() {
        // 准备测试数据
        Deck deck = new Deck();
        Hand hand1 = Hand.parse("AsKs");
        Hand hand2 = Hand.parse("QhQd");
        List<TexasCard> board = new ArrayList<>();
        board.add(TexasCard.parse("Ah"));
        board.add(TexasCard.parse("Kh"));
        board.add(TexasCard.parse("Qc"));
        board.add(TexasCard.parse("7s"));
        board.add(TexasCard.parse("2c"));

        // 单次Equity计算
        long start = System.nanoTime();
        com.poker.gto.core.evaluator.EquityCalculator.calculateHeadsUp(hand1, hand2, board);
        long end = System.nanoTime();

        double microSeconds = (end - start) / 1000.0;
        System.out.printf("   单次Equity计算: %.2f 微秒\n", microSeconds);
    }
}
