package com.poker.gto.core.evaluator;

import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.Suit;
import com.poker.gto.core.cards.TexasCard;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 7卡手牌评估器
 *
 * 从7张牌中找出最佳的5张牌组合
 */
public class HandEvaluator {

    // 预计算的C(7,5) = 21种组合的索引（public供LookupEvaluator使用）
    public static final int[][] COMBINATION_INDICES = {
        {0, 1, 2, 3, 4}, {0, 1, 2, 3, 5}, {0, 1, 2, 3, 6},
        {0, 1, 2, 4, 5}, {0, 1, 2, 4, 6}, {0, 1, 2, 5, 6},
        {0, 1, 3, 4, 5}, {0, 1, 3, 4, 6}, {0, 1, 3, 5, 6},
        {0, 1, 4, 5, 6}, {0, 2, 3, 4, 5}, {0, 2, 3, 4, 6},
        {0, 2, 3, 5, 6}, {0, 2, 4, 5, 6}, {0, 3, 4, 5, 6},
        {1, 2, 3, 4, 5}, {1, 2, 3, 4, 6}, {1, 2, 3, 5, 6},
        {1, 2, 4, 5, 6}, {1, 3, 4, 5, 6}, {2, 3, 4, 5, 6}
    };

    // ThreadLocal重用数组，避免重复创建
    private static final ThreadLocal<TexasCard[]> CARD_BUFFER =
        ThreadLocal.withInitial(() -> new TexasCard[5]);

    /**
     * 评估7张牌（2底牌 + 5公共牌）
     *
     * @param cards 7张牌
     * @return 评估结果
     */
    public static EvaluatedHand evaluate(List<TexasCard> cards) {
        if (cards.size() != 7) {
            throw new IllegalArgumentException("Must provide exactly 7 cards");
        }

        // 使用预计算的索引，避免生成21个List对象
        EvaluatedHand best = null;
        TexasCard[] fiveCards = CARD_BUFFER.get();

        for (int[] indices : COMBINATION_INDICES) {
            // 直接使用索引访问，不创建新List
            for (int i = 0; i < 5; i++) {
                fiveCards[i] = cards.get(indices[i]);
            }

            EvaluatedHand evaluated = evaluateFiveCards(fiveCards);
            if (best == null || evaluated.compareTo(best) > 0) {
                best = evaluated;
            }
        }

        return best;
    }

    /**
     * 评估7张牌（使用Hand对象）
     */
    public static EvaluatedHand evaluate(Hand hand) {
        return evaluate(hand.getCards());
    }

    /**
     * 评估5张牌（数组版本，性能优化）
     * 注意：此方法会修改cards数组的顺序（排序）
     */
    public static EvaluatedHand evaluateFiveCards(TexasCard[] cards) {
        // 按点数降序排序（直接在数组上排序）
        Arrays.sort(cards, (a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // 计算点数统计（优化：只计算一次，传递给所有check方法）
        int[] rankCounts = getRankCountsOptimized(cards);

        // 检查各种牌型
        EvaluatedHand result;

        if ((result = checkStraightFlush(cards, rankCounts)) != null) return result;
        if ((result = checkFourOfAKind(cards, rankCounts)) != null) return result;
        if ((result = checkFullHouse(cards, rankCounts)) != null) return result;
        if ((result = checkFlush(cards, rankCounts)) != null) return result;
        if ((result = checkStraight(cards, rankCounts)) != null) return result;
        if ((result = checkThreeOfAKind(cards, rankCounts)) != null) return result;
        if ((result = checkTwoPair(cards, rankCounts)) != null) return result;
        if ((result = checkOnePair(cards, rankCounts)) != null) return result;

        return checkHighCard(cards);
    }

    /**
     * 检查同花顺
     */
    private static EvaluatedHand checkStraightFlush(TexasCard[] cards, int[] rankCounts) {
        if (!isFlush(cards)) {
            return null;
        }

        List<Rank> straightRanks = getStraightRanks(cards);
        if (straightRanks == null) {
            return null;
        }

        return new EvaluatedHand(HandRank.STRAIGHT_FLUSH, straightRanks, Arrays.asList(cards));
    }

    /**
     * 检查四条（优化版）
     */
    private static EvaluatedHand checkFourOfAKind(TexasCard[] cards, int[] rankCounts) {
        // 从高到低查找四条
        for (int i = 12; i >= 0; i--) {
            if (rankCounts[i] == 4) {
                Rank quadRank = Rank.fromValue(i + 2);

                // 找踢脚牌（剩下的那张）
                Rank kicker = null;
                for (TexasCard card : cards) {
                    if (card.getRank() != quadRank) {
                        kicker = card.getRank();
                        break;
                    }
                }

                List<Rank> kickers = Arrays.asList(quadRank, quadRank, quadRank, quadRank, kicker);
                return new EvaluatedHand(HandRank.FOUR_OF_A_KIND, kickers, Arrays.asList(cards));
            }
        }

        return null;
    }

    /**
     * 检查葫芦（三条+对子）（优化版）
     */
    private static EvaluatedHand checkFullHouse(TexasCard[] cards, int[] rankCounts) {
        Rank tripRank = null;
        Rank pairRank = null;

        // 从高到低找三条
        for (int i = 12; i >= 0; i--) {
            if (rankCounts[i] == 3) {
                tripRank = Rank.fromValue(i + 2);
                break;
            }
        }

        if (tripRank == null) {
            return null;
        }

        // 从高到低找对子（排除三条的点数）
        int tripIndex = tripRank.getValue() - 2;
        for (int i = 12; i >= 0; i--) {
            if (i != tripIndex && rankCounts[i] >= 2) {
                pairRank = Rank.fromValue(i + 2);
                break;
            }
        }

        if (pairRank == null) {
            return null;
        }

        List<Rank> kickers = Arrays.asList(tripRank, tripRank, tripRank, pairRank, pairRank);
        return new EvaluatedHand(HandRank.FULL_HOUSE, kickers, Arrays.asList(cards));
    }

    /**
     * 检查同花（优化版）
     */
    private static EvaluatedHand checkFlush(TexasCard[] cards, int[] rankCounts) {
        if (!isFlush(cards)) {
            return null;
        }

        // cards已经按降序排序，直接提取rank
        List<Rank> kickers = new ArrayList<>(5);
        for (TexasCard card : cards) {
            kickers.add(card.getRank());
        }

        return new EvaluatedHand(HandRank.FLUSH, kickers, Arrays.asList(cards));
    }

    /**
     * 检查顺子（优化版）
     */
    private static EvaluatedHand checkStraight(TexasCard[] cards, int[] rankCounts) {
        List<Rank> straightRanks = getStraightRanks(cards);
        if (straightRanks == null) {
            return null;
        }

        return new EvaluatedHand(HandRank.STRAIGHT, straightRanks, Arrays.asList(cards));
    }

    /**
     * 检查三条（优化版）
     */
    private static EvaluatedHand checkThreeOfAKind(TexasCard[] cards, int[] rankCounts) {
        // 从高到低找三条
        for (int i = 12; i >= 0; i--) {
            if (rankCounts[i] == 3) {
                Rank tripRank = Rank.fromValue(i + 2);

                // 找另外两张牌（cards已排序，直接按顺序找）
                List<Rank> kickers = new ArrayList<>(5);
                kickers.add(tripRank);
                kickers.add(tripRank);
                kickers.add(tripRank);

                for (TexasCard card : cards) {
                    if (card.getRank() != tripRank) {
                        kickers.add(card.getRank());
                    }
                }

                return new EvaluatedHand(HandRank.THREE_OF_A_KIND, kickers, Arrays.asList(cards));
            }
        }

        return null;
    }

    /**
     * 检查两对（优化版）
     */
    private static EvaluatedHand checkTwoPair(TexasCard[] cards, int[] rankCounts) {
        // 从高到低找两对
        Rank pair1 = null;
        Rank pair2 = null;

        for (int i = 12; i >= 0; i--) {
            if (rankCounts[i] == 2) {
                if (pair1 == null) {
                    pair1 = Rank.fromValue(i + 2);
                } else {
                    pair2 = Rank.fromValue(i + 2);
                    break;
                }
            }
        }

        if (pair1 == null || pair2 == null) {
            return null;
        }

        // 找踢脚牌（剩下的那张）
        Rank kicker = null;
        for (TexasCard card : cards) {
            Rank r = card.getRank();
            if (r != pair1 && r != pair2) {
                kicker = r;
                break;
            }
        }

        List<Rank> kickerList = Arrays.asList(pair1, pair1, pair2, pair2, kicker);
        return new EvaluatedHand(HandRank.TWO_PAIR, kickerList, Arrays.asList(cards));
    }

    /**
     * 检查一对（优化版）
     */
    private static EvaluatedHand checkOnePair(TexasCard[] cards, int[] rankCounts) {
        // 从高到低找对子
        for (int i = 12; i >= 0; i--) {
            if (rankCounts[i] == 2) {
                Rank pairRank = Rank.fromValue(i + 2);

                // 找另外三张牌（cards已排序）
                List<Rank> kickers = new ArrayList<>(5);
                kickers.add(pairRank);
                kickers.add(pairRank);

                for (TexasCard card : cards) {
                    if (card.getRank() != pairRank) {
                        kickers.add(card.getRank());
                    }
                }

                return new EvaluatedHand(HandRank.ONE_PAIR, kickers, Arrays.asList(cards));
            }
        }

        return null;
    }

    /**
     * 高牌（优化版）
     */
    private static EvaluatedHand checkHighCard(TexasCard[] cards) {
        // cards已经按降序排序，直接提取rank
        List<Rank> kickers = new ArrayList<>(5);
        for (TexasCard card : cards) {
            kickers.add(card.getRank());
        }

        return new EvaluatedHand(HandRank.HIGH_CARD, kickers, Arrays.asList(cards));
    }

    // ========== 辅助方法 ==========

    /**
     * 判断是否同花（优化版）
     */
    private static boolean isFlush(TexasCard[] cards) {
        Suit firstSuit = cards[0].getSuit();
        for (int i = 1; i < cards.length; i++) {
            if (cards[i].getSuit() != firstSuit) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取顺子的点数（降序），如果不是顺子返回null（优化版）
     */
    private static List<Rank> getStraightRanks(TexasCard[] cards) {
        // 提取不重复的ranks（cards已排序）
        List<Rank> ranks = new ArrayList<>(5);
        Rank lastRank = null;
        for (TexasCard card : cards) {
            if (card.getRank() != lastRank) {
                ranks.add(card.getRank());
                lastRank = card.getRank();
            }
        }

        if (ranks.size() < 5) {
            return null;
        }

        // 检查普通顺子（如AKQJT）
        boolean isStraight = true;
        for (int i = 0; i < 4; i++) {
            if (ranks.get(i).getValue() - ranks.get(i + 1).getValue() != 1) {
                isStraight = false;
                break;
            }
        }

        if (isStraight) {
            return ranks;
        }

        // 检查A-2-3-4-5（轮子）
        if (ranks.get(0) == Rank.ACE &&
            ranks.contains(Rank.TWO) &&
            ranks.contains(Rank.THREE) &&
            ranks.contains(Rank.FOUR) &&
            ranks.contains(Rank.FIVE)) {
            return Arrays.asList(Rank.FIVE, Rank.FOUR, Rank.THREE, Rank.TWO, Rank.ACE);
        }

        return null;
    }

    /**
     * 获取点数计数（优化版：使用int[]代替HashMap）
     * 索引对应Rank.getValue() - 2 (因为2的value是2, A的value是14)
     */
    private static int[] getRankCountsOptimized(TexasCard[] cards) {
        int[] counts = new int[13]; // 索引0-12对应2-A
        for (TexasCard card : cards) {
            counts[card.getValue() - 2]++;
        }
        return counts;
    }

    /**
     * 获取点数计数（保留原方法用于兼容性）
     */
    private static Map<Rank, Integer> getRankCounts(List<TexasCard> cards) {
        Map<Rank, Integer> counts = new HashMap<>();
        for (TexasCard card : cards) {
            counts.merge(card.getRank(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * 生成组合（C(n, k)）
     */
    private static List<List<TexasCard>> generateCombinations(List<TexasCard> cards, int k) {
        List<List<TexasCard>> result = new ArrayList<>();
        generateCombinationsHelper(cards, k, 0, new ArrayList<>(), result);
        return result;
    }

    private static void generateCombinationsHelper(
        List<TexasCard> cards,
        int k,
        int start,
        List<TexasCard> current,
        List<List<TexasCard>> result
    ) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinationsHelper(cards, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}
