package com.poker.gto.core.evaluator;

import com.poker.gto.core.cards.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Two Plus Two 查找表评估器测试
 *
 * 测试目标：
 * 1. 验证查找表正确初始化（2,598,960个条目）
 * 2. 验证评估结果与传统算法一致
 * 3. 验证特殊牌型（皇家同花顺、最弱高牌等）
 * 4. 性能测试
 */
public class LookupEvaluatorTest {

    @BeforeAll
    static void setup() {
        System.out.println("\n=== Two Plus Two 评估器测试开始 ===\n");
    }

    @Test
    @DisplayName("测试1: 查找表初始化")
    void testLookupTableInitialization() {
        System.out.println("测试1: 查找表初始化...");

        // 触发初始化
        int tableSize = LookupEvaluator.getLookupTableSize();

        // 验证：C(52,5) = 2,598,960
        assertEquals(2_598_960, tableSize, "查找表应该包含2,598,960个条目");

        System.out.println("  ✓ 查找表大小正确: " + String.format("%,d", tableSize) + " 个条目\n");
    }

    @Test
    @DisplayName("测试2: 皇家同花顺 - 最强牌")
    void testRoyalFlush() {
        System.out.println("测试2: 皇家同花顺（最强牌）...");

        // A♠ K♠ Q♠ J♠ T♠
        TexasCard[] royalFlush = {
            TexasCard.of(Rank.ACE, Suit.SPADES),
            TexasCard.of(Rank.KING, Suit.SPADES),
            TexasCard.of(Rank.QUEEN, Suit.SPADES),
            TexasCard.of(Rank.JACK, Suit.SPADES),
            TexasCard.of(Rank.TEN, Suit.SPADES)
        };

        int rank = LookupEvaluator.evaluate5(royalFlush[0], royalFlush[1],
                                             royalFlush[2], royalFlush[3], royalFlush[4]);

        // 皇家同花顺应该是rank 1（最强）
        assertEquals(1, rank, "皇家同花顺应该是rank 1");

        System.out.println("  A♠ K♠ Q♠ J♠ T♠");
        System.out.println("  Rank: " + rank + " (1 = 最强)");
        System.out.println("  ✓ 皇家同花顺识别正确\n");
    }

    @Test
    @DisplayName("测试3: 四条")
    void testFourOfAKind() {
        System.out.println("测试3: 四条...");

        // A♠ A♥ A♦ A♣ K♠
        TexasCard[] quads = {
            TexasCard.of(Rank.ACE, Suit.SPADES),
            TexasCard.of(Rank.ACE, Suit.HEARTS),
            TexasCard.of(Rank.ACE, Suit.DIAMONDS),
            TexasCard.of(Rank.ACE, Suit.CLUBS),
            TexasCard.of(Rank.KING, Suit.SPADES)
        };

        int rank = LookupEvaluator.evaluate5(quads[0], quads[1], quads[2], quads[3], quads[4]);

        // 四条应该在10-166之间（同花顺是1-10，四条是11-166）
        assertTrue(rank >= 11 && rank <= 166, "四条应该在rank 11-166之间");

        System.out.println("  A♠ A♥ A♦ A♣ K♠");
        System.out.println("  Rank: " + rank);
        System.out.println("  ✓ 四条识别正确\n");
    }

    @Test
    @DisplayName("测试4: 葫芦")
    void testFullHouse() {
        System.out.println("测试4: 葫芦...");

        // K♠ K♥ K♦ Q♠ Q♥
        TexasCard[] fullHouse = {
            TexasCard.of(Rank.KING, Suit.SPADES),
            TexasCard.of(Rank.KING, Suit.HEARTS),
            TexasCard.of(Rank.KING, Suit.DIAMONDS),
            TexasCard.of(Rank.QUEEN, Suit.SPADES),
            TexasCard.of(Rank.QUEEN, Suit.HEARTS)
        };

        int rank = LookupEvaluator.evaluate5(fullHouse[0], fullHouse[1],
                                             fullHouse[2], fullHouse[3], fullHouse[4]);

        // 葫芦应该在167-322之间
        assertTrue(rank >= 167 && rank <= 322, "葫芦应该在rank 167-322之间");

        System.out.println("  K♠ K♥ K♦ Q♠ Q♥");
        System.out.println("  Rank: " + rank);
        System.out.println("  ✓ 葫芦识别正确\n");
    }

    @Test
    @DisplayName("测试5: 最弱高牌 7-5-4-3-2")
    void testWorstHighCard() {
        System.out.println("测试5: 最弱高牌...");

        // 7♠ 5♥ 4♦ 3♣ 2♠ （不同花）
        TexasCard[] worstHand = {
            TexasCard.of(Rank.SEVEN, Suit.SPADES),
            TexasCard.of(Rank.FIVE, Suit.HEARTS),
            TexasCard.of(Rank.FOUR, Suit.DIAMONDS),
            TexasCard.of(Rank.THREE, Suit.CLUBS),
            TexasCard.of(Rank.TWO, Suit.SPADES)
        };

        int rank = LookupEvaluator.evaluate5(worstHand[0], worstHand[1],
                                             worstHand[2], worstHand[3], worstHand[4]);

        // 最弱高牌应该是7462
        assertEquals(7462, rank, "7-5-4-3-2应该是最弱高牌 (rank 7462)");

        System.out.println("  7♠ 5♥ 4♦ 3♣ 2♠");
        System.out.println("  Rank: " + rank + " (7462 = 最弱)");
        System.out.println("  ✓ 最弱高牌识别正确\n");
    }

    @Test
    @DisplayName("测试6: 7卡评估 - 找最佳5张")
    void testSeven CardEvaluation() {
        System.out.println("测试6: 7卡评估...");

        // 7张牌: A♠ K♠ Q♠ J♠ T♠ 9♠ 8♠
        // 最佳5张: A♠ K♠ Q♠ J♠ T♠ (皇家同花顺)
        List<TexasCard> sevenCards = Arrays.asList(
            TexasCard.of(Rank.ACE, Suit.SPADES),
            TexasCard.of(Rank.KING, Suit.SPADES),
            TexasCard.of(Rank.QUEEN, Suit.SPADES),
            TexasCard.of(Rank.JACK, Suit.SPADES),
            TexasCard.of(Rank.TEN, Suit.SPADES),
            TexasCard.of(Rank.NINE, Suit.SPADES),
            TexasCard.of(Rank.EIGHT, Suit.SPADES)
        );

        int rank = LookupEvaluator.evaluate7(sevenCards);

        // 应该找到皇家同花顺 (rank 1)
        assertEquals(1, rank, "应该找到皇家同花顺");

        System.out.println("  7张牌: A♠ K♠ Q♠ J♠ T♠ 9♠ 8♠");
        System.out.println("  最佳5张: A♠ K♠ Q♠ J♠ T♠");
        System.out.println("  Rank: " + rank);
        System.out.println("  ✓ 7卡评估正确\n");
    }

    @Test
    @DisplayName("测试7: 与传统算法对比")
    void testConsistencyWithTraditionalEvaluator() {
        System.out.println("测试7: 与传统算法对比...");

        Random random = new Random(12345);  // 固定种子保证可重复
        Deck deck = new Deck();
        int testCount = 100;
        int consistentCount = 0;

        for (int i = 0; i < testCount; i++) {
            deck.reset();
            deck.shuffle(random);

            List<TexasCard> sevenCards = deck.deal(7);

            // 使用两种方法评估
            int lookupRank = LookupEvaluator.evaluate7(sevenCards);
            EvaluatedHand traditionalResult = HandEvaluator.evaluate(sevenCards);

            // 使用传统方法评估最佳5张牌，然后用lookup评估同样的牌
            TexasCard[] bestFive = traditionalResult.getBestFive().toArray(new TexasCard[5]);
            int lookupRankOfBestFive = LookupEvaluator.evaluate5(
                bestFive[0], bestFive[1], bestFive[2], bestFive[3], bestFive[4]
            );

            // lookup的7卡评估应该和传统方法找到的最佳5张评估一致
            if (lookupRank == lookupRankOfBestFive) {
                consistentCount++;
            }
        }

        System.out.println("  测试样本: " + testCount + " 手牌");
        System.out.println("  一致结果: " + consistentCount + " / " + testCount);
        System.out.println("  一致率: " + (100.0 * consistentCount / testCount) + "%");

        // 应该100%一致
        assertEquals(testCount, consistentCount, "两种算法应该100%一致");

        System.out.println("  ✓ 两种算法结果一致\n");
    }

    @Test
    @DisplayName("测试8: 对比不同牌型强度")
    void testHandRankings() {
        System.out.println("测试8: 对比不同牌型强度...");

        // 同花顺
        TexasCard[] straightFlush = {
            TexasCard.of(Rank.NINE, Suit.HEARTS),
            TexasCard.of(Rank.EIGHT, Suit.HEARTS),
            TexasCard.of(Rank.SEVEN, Suit.HEARTS),
            TexasCard.of(Rank.SIX, Suit.HEARTS),
            TexasCard.of(Rank.FIVE, Suit.HEARTS)
        };

        // 四条
        TexasCard[] fourOfAKind = {
            TexasCard.of(Rank.KING, Suit.SPADES),
            TexasCard.of(Rank.KING, Suit.HEARTS),
            TexasCard.of(Rank.KING, Suit.DIAMONDS),
            TexasCard.of(Rank.KING, Suit.CLUBS),
            TexasCard.of(Rank.QUEEN, Suit.SPADES)
        };

        // 葫芦
        TexasCard[] fullHouse = {
            TexasCard.of(Rank.JACK, Suit.SPADES),
            TexasCard.of(Rank.JACK, Suit.HEARTS),
            TexasCard.of(Rank.JACK, Suit.DIAMONDS),
            TexasCard.of(Rank.TEN, Suit.SPADES),
            TexasCard.of(Rank.TEN, Suit.HEARTS)
        };

        int sf = LookupEvaluator.evaluate5(straightFlush[0], straightFlush[1],
                                           straightFlush[2], straightFlush[3], straightFlush[4]);
        int foak = LookupEvaluator.evaluate5(fourOfAKind[0], fourOfAKind[1],
                                             fourOfAKind[2], fourOfAKind[3], fourOfAKind[4]);
        int fh = LookupEvaluator.evaluate5(fullHouse[0], fullHouse[1],
                                           fullHouse[2], fullHouse[3], fullHouse[4]);

        System.out.println("  同花顺 (9-high): rank " + sf);
        System.out.println("  四条 (K): rank " + foak);
        System.out.println("  葫芦 (J over T): rank " + fh);

        // 验证：同花顺 > 四条 > 葫芦（rank越小越强）
        assertTrue(sf < foak, "同花顺应该强于四条");
        assertTrue(foak < fh, "四条应该强于葫芦");

        System.out.println("  ✓ 牌型强度排序正确\n");
    }

    @Test
    @DisplayName("测试9: 特殊情况 - A-2-3-4-5 顺子（轮子）")
    void testWheelStraight() {
        System.out.println("测试9: A-2-3-4-5 顺子（轮子）...");

        // A♠ 2♥ 3♦ 4♣ 5♠ （不同花）
        TexasCard[] wheel = {
            TexasCard.of(Rank.ACE, Suit.SPADES),
            TexasCard.of(Rank.TWO, Suit.HEARTS),
            TexasCard.of(Rank.THREE, Suit.DIAMONDS),
            TexasCard.of(Rank.FOUR, Suit.CLUBS),
            TexasCard.of(Rank.FIVE, Suit.SPADES)
        };

        int rank = LookupEvaluator.evaluate5(wheel[0], wheel[1], wheel[2], wheel[3], wheel[4]);

        // 应该被识别为顺子（rank范围: 1600-1609）
        assertTrue(rank >= 1600 && rank <= 1609, "A-2-3-4-5应该被识别为顺子");

        System.out.println("  A♠ 2♥ 3♦ 4♣ 5♠");
        System.out.println("  Rank: " + rank);
        System.out.println("  ✓ 轮子顺子识别正确\n");
    }

    @AfterAll
    static void summary() {
        System.out.println("=== Two Plus Two 评估器测试完成 ===\n");
    }
}
