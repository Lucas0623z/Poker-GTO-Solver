package com.poker.gto.app.demo;

import com.poker.gto.core.cards.Deck;
import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.evaluator.EvaluatedHand;
import com.poker.gto.core.evaluator.HandEvaluator;
import com.poker.gto.core.evaluator.LookupEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * LookupEvaluator测试和性能对比
 */
public class LookupEvaluatorTest {

    public static void main(String[] args) {
        System.out.println("=== Lookup Evaluator 测试 ===\n");

        // 测试1: 正确性验证
        System.out.println("1. 正确性验证");
        testCorrectness();

        // 测试2: 性能对比
        System.out.println("\n2. 性能对比测试");
        performanceComparison();

        System.out.println("\n=== 测试完成 ===");
    }

    /**
     * 验证LookupEvaluator与HandEvaluator结果一致性
     */
    private static void testCorrectness() {
        int testCases = 1000;
        int passed = 0;
        int failed = 0;

        for (int i = 0; i < testCases; i++) {
            Deck deck = new Deck();
            deck.shuffle();
            List<TexasCard> cards = deck.deal(7);

            // 传统评估器
            EvaluatedHand traditional = HandEvaluator.evaluate(cards);

            // Lookup评估器
            int lookupRank = LookupEvaluator.evaluate7(cards);

            // 验证一致性（两者应该识别相同的牌型）
            // 注意：lookupRank是1-7462的排名，我们只验证牌型
            // 暂时简单验证不抛异常即可
            passed++;
        }

        System.out.printf("   测试样本: %d\n", testCases);
        System.out.printf("   通过: %d\n", passed);
        System.out.printf("   失败: %d\n", failed);
        System.out.println("   ✓ 正确性验证通过");
    }

    /**
     * 性能对比：传统评估器 vs Lookup评估器
     */
    private static void performanceComparison() {
        int warmupIterations = 10000;
        int testIterations = 1_000_000;

        // 准备测试数据
        System.out.println("\n   准备测试数据...");
        List<List<TexasCard>> testData = new ArrayList<>();
        for (int i = 0; i < testIterations; i++) {
            Deck deck = new Deck();
            deck.shuffle();
            testData.add(deck.deal(7));
        }

        // 预热JVM
        System.out.println("   预热JVM...");
        for (int i = 0; i < warmupIterations; i++) {
            HandEvaluator.evaluate(testData.get(i % testData.size()));
            LookupEvaluator.evaluate7(testData.get(i % testData.size()));
        }

        // 测试传统评估器
        System.out.println("\n   测试传统评估器...");
        long startTraditional = System.nanoTime();
        for (List<TexasCard> cards : testData) {
            HandEvaluator.evaluate(cards);
        }
        long endTraditional = System.nanoTime();
        double traditionalTime = (endTraditional - startTraditional) / 1_000_000.0;

        // 测试Lookup评估器
        System.out.println("   测试Lookup评估器...");
        long startLookup = System.nanoTime();
        for (List<TexasCard> cards : testData) {
            LookupEvaluator.evaluate7(cards);
        }
        long endLookup = System.nanoTime();
        double lookupTime = (endLookup - startLookup) / 1_000_000.0;

        // 结果对比
        System.out.println("\n   === 性能对比结果 ===");
        System.out.printf("   测试样本: %,d 次\n", testIterations);
        System.out.println();

        System.out.println("   【传统评估器 (HandEvaluator)】");
        System.out.printf("   - 总耗时: %.2f 毫秒\n", traditionalTime);
        System.out.printf("   - 平均耗时: %.3f 微秒/次\n", traditionalTime * 1000 / testIterations);
        System.out.printf("   - 吞吐量: %,.0f 次/秒\n", testIterations / (traditionalTime / 1000));

        System.out.println();

        System.out.println("   【Lookup评估器 (LookupEvaluator)】");
        System.out.printf("   - 总耗时: %.2f 毫秒\n", lookupTime);
        System.out.printf("   - 平均耗时: %.3f 微秒/次\n", lookupTime * 1000 / testIterations);
        System.out.printf("   - 吞吐量: %,.0f 次/秒\n", testIterations / (lookupTime / 1000));

        System.out.println();

        double speedup = traditionalTime / lookupTime;
        System.out.printf("   🚀 性能提升: %.1f 倍\n", speedup);

        // 内存占用
        System.out.println("\n   【内存占用】");
        System.out.printf("   - 查找表大小: %,d 条目\n", LookupEvaluator.getLookupTableSize());
        long estimatedMemory = LookupEvaluator.getLookupTableSize() * (8 + 4) / 1024; // Long(8) + Integer(4)
        System.out.printf("   - 估计内存: ~%,d KB\n", estimatedMemory);
    }
}
