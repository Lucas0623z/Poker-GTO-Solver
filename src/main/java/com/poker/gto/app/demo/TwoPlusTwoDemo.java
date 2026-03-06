package com.poker.gto.app.demo;

import com.poker.gto.core.cards.*;
import com.poker.gto.core.evaluator.*;

import java.util.*;

/**
 * Two Plus Two 算法演示
 *
 * 展示：
 * 1. 查找表评估器的正确性
 * 2. 性能对比（传统算法 vs 查找表算法）
 * 3. 内存占用
 */
public class TwoPlusTwoDemo {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  Two Plus Two 扑克手牌评估器演示");
        System.out.println("=".repeat(60) + "\n");

        // 1. 算法原理
        System.out.println("【算法原理】");
        System.out.println("Two Plus Two算法使用素数乘积编码 + 查找表：");
        System.out.println("  1. 为每张牌分配一个唯一素数");
        System.out.println("  2. 5张牌的编码 = 5个素数的乘积");
        System.out.println("  3. 预计算所有C(52,5) = 2,598,960种组合");
        System.out.println("  4. 查表时间复杂度: O(1)");
        System.out.println();

        // 2. 初始化
        System.out.println("【查找表初始化】");
        long initStart = System.currentTimeMillis();
        int tableSize = LookupEvaluator.getLookupTableSize();
        long initTime = System.currentTimeMillis() - initStart;
        System.out.println("  查找表大小: " + String.format("%,d", tableSize) + " 个条目");
        System.out.println("  初始化时间: " + initTime + " ms");
        System.out.println();

        // 3. 正确性验证
        System.out.println("【正确性验证】");
        demonstrateCorrectness();
        System.out.println();

        // 4. 性能对比
        System.out.println("【性能对比】");
        performanceBenchmark();
        System.out.println();

        // 5. 实战示例
        System.out.println("【实战示例：德州扑克 River 场景】");
        realWorldExample();
        System.out.println();

        System.out.println("=".repeat(60));
        System.out.println("  演示完成！");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * 演示正确性 - 各种牌型
     */
    private static void demonstrateCorrectness() {
        System.out.println("测试各种牌型识别：\n");

        // 皇家同花顺
        testHand("皇家同花顺", "As Ks Qs Js Ts", 1, 1, 10);

        // 同花顺
        testHand("同花顺 (9-high)", "9h 8h 7h 6h 5h", 11, 1599, null);

        // 四条
        testHand("四条 (KKKK)", "Ks Kh Kd Kc Qs", 11, 166, null);

        // 葫芦
        testHand("葫芦 (QQQ over JJ)", "Qs Qh Qd Js Jh", 167, 322, null);

        // 同花
        testHand("同花 (A-high)", "As Ks Qs Js 9s", 323, 1599, null);

        // 顺子
        testHand("顺子 (A-high)", "As Kh Qd Jc Ts", 1600, 1609, null);

        // 顺子（轮子）
        testHand("顺子 (5-high wheel)", "As 2h 3d 4c 5s", 1600, 1609, null);

        // 三条
        testHand("三条 (JJJ)", "Js Jh Jd 9s 8h", 1610, 2467, null);

        // 两对
        testHand("两对 (TT and 99)", "Ts Th 9s 9h 8d", 2468, 3325, null);

        // 一对
        testHand("一对 (88)", "8s 8h Kd Qc Jh", 3326, 6185, null);

        // 高牌
        testHand("高牌 (K-high)", "Ks Qh Jd 9c 7s", 6186, 7461, null);

        // 最弱高牌
        testHand("最弱高牌 (7-5-4-3-2)", "7s 5h 4d 3c 2s", 7462, 7462, 7462);
    }

    /**
     * 测试单个手牌
     */
    private static void testHand(String name, String handStr, int minRank, int maxRank, Integer expectedRank) {
        Hand hand = Hand.parse(handStr);
        TexasCard[] cards = hand.getCards().toArray(new TexasCard[5]);

        int rank = LookupEvaluator.evaluate5(cards[0], cards[1], cards[2], cards[3], cards[4]);

        boolean inRange = rank >= minRank && rank <= maxRank;
        boolean exactMatch = expectedRank == null || rank == expectedRank;

        String status = (inRange && exactMatch) ? "✓" : "✗";
        String rankStr = String.format("rank %d", rank);

        if (expectedRank != null) {
            rankStr = String.format("rank %d (expected %d)", rank, expectedRank);
        } else {
            rankStr = String.format("rank %d (range %d-%d)", rank, minRank, maxRank);
        }

        System.out.printf("  %s %-25s %s → %s\n",
            status, name + ":", hand.toUnicodeString(), rankStr);
    }

    /**
     * 性能基准测试
     */
    private static void performanceBenchmark() {
        int iterations = 100_000;

        System.out.println("测试：评估 " + String.format("%,d", iterations) + " 手随机7张牌\n");

        // 预热
        Deck warmupDeck = new Deck();
        for (int i = 0; i < 1000; i++) {
            warmupDeck.reset();
            warmupDeck.shuffle();
            List<TexasCard> cards = warmupDeck.deal(7);
            HandEvaluator.evaluate(cards);
            LookupEvaluator.evaluate7(cards);
        }

        // 生成测试数据
        List<List<TexasCard>> testHands = new ArrayList<>(iterations);
        for (int i = 0; i < iterations; i++) {
            Deck deck = new Deck();
            deck.shuffle();
            testHands.add(deck.deal(7));
        }

        // 测试传统算法
        long traditionalStart = System.nanoTime();
        for (List<TexasCard> hand : testHands) {
            HandEvaluator.evaluate(hand);
        }
        long traditionalTime = System.nanoTime() - traditionalStart;

        // 测试查找表算法
        long lookupStart = System.nanoTime();
        for (List<TexasCard> hand : testHands) {
            LookupEvaluator.evaluate7(hand);
        }
        long lookupTime = System.nanoTime() - lookupStart;

        // 结果
        double traditionalMs = traditionalTime / 1_000_000.0;
        double lookupMs = lookupTime / 1_000_000.0;
        double speedup = (double) traditionalTime / lookupTime;

        System.out.println("【传统算法】");
        System.out.printf("  总时间: %.2f ms\n", traditionalMs);
        System.out.printf("  平均: %.3f μs/手\n", traditionalMs * 1000 / iterations);
        System.out.println();

        System.out.println("【查找表算法 (Two Plus Two)】");
        System.out.printf("  总时间: %.2f ms\n", lookupMs);
        System.out.printf("  平均: %.3f μs/手\n", lookupMs * 1000 / iterations);
        System.out.println();

        System.out.println("【性能提升】");
        System.out.printf("  加速比: %.1fx\n", speedup);
        System.out.printf("  效率提升: %.1f%%\n", (speedup - 1) * 100);

        if (speedup < 2) {
            System.out.println("  注意: 加速比低于预期，可能是JVM未充分优化");
        }
    }

    /**
     * 实战示例：德州扑克 River 对抗
     */
    private static void realWorldExample() {
        System.out.println("场景: 对手全下，你需要快速计算是否跟注\n");

        // 公共牌
        List<TexasCard> board = Arrays.asList(
            TexasCard.of(Rank.ACE, Suit.HEARTS),
            TexasCard.of(Rank.KING, Suit.DIAMONDS),
            TexasCard.of(Rank.QUEEN, Suit.HEARTS),
            TexasCard.of(Rank.JACK, Suit.CLUBS),
            TexasCard.of(Rank.TEN, Suit.SPADES)
        );

        // 你的底牌
        List<TexasCard> yourHole = Arrays.asList(
            TexasCard.of(Rank.ACE, Suit.SPADES),
            TexasCard.of(Rank.KING, Suit.HEARTS)
        );

        // 对手可能的底牌
        List<TexasCard> opponentHole = Arrays.asList(
            TexasCard.of(Rank.NINE, Suit.HEARTS),
            TexasCard.of(Rank.EIGHT, Suit.HEARTS)
        );

        // 合并7张牌
        List<TexasCard> yourSeven = new ArrayList<>(board);
        yourSeven.addAll(yourHole);

        List<TexasCard> opponentSeven = new ArrayList<>(board);
        opponentSeven.addAll(opponentHole);

        // 评估
        int yourRank = LookupEvaluator.evaluate7(yourSeven);
        int opponentRank = LookupEvaluator.evaluate7(opponentSeven);

        EvaluatedHand yourHand = HandEvaluator.evaluate(yourSeven);
        EvaluatedHand opponentHand = HandEvaluator.evaluate(opponentSeven);

        // 显示
        System.out.println("公共牌: " + formatCards(board));
        System.out.println();

        System.out.println("你的底牌: " + formatCards(yourHole));
        System.out.println("你的牌型: " + yourHand.getRank());
        System.out.println("最佳5张: " + formatCards(yourHand.getBestFive()));
        System.out.println("强度值: " + yourRank);
        System.out.println();

        System.out.println("对手底牌: " + formatCards(opponentHole));
        System.out.println("对手牌型: " + opponentHand.getRank());
        System.out.println("最佳5张: " + formatCards(opponentHand.getBestFive()));
        System.out.println("强度值: " + opponentRank);
        System.out.println();

        if (yourRank < opponentRank) {
            System.out.println("结果: 你获胜！ ✓");
        } else if (yourRank > opponentRank) {
            System.out.println("结果: 对手获胜 ✗");
        } else {
            System.out.println("结果: 平局");
        }
    }

    /**
     * 格式化卡牌列表
     */
    private static String formatCards(List<TexasCard> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(cards.get(i).toUnicodeString());
        }
        return sb.toString();
    }
}
