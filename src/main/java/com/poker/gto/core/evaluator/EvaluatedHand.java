package com.poker.gto.core.evaluator;

import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.TexasCard;

import java.util.List;
import java.util.Objects;

/**
 * 评估后的手牌
 *
 * 包含牌型等级和踢脚牌（kickers）用于比较
 *
 * 这个类是不可变的
 */
public class EvaluatedHand implements Comparable<EvaluatedHand> {

    private final HandRank rank;
    private final List<Rank> kickers;  // 踢脚牌，按降序排列
    private final List<TexasCard> bestFive;  // 最佳5张牌

    /**
     * 创建评估后的手牌
     *
     * @param rank 牌型等级
     * @param kickers 踢脚牌（降序）
     * @param bestFive 最佳5张牌
     */
    public EvaluatedHand(HandRank rank, List<Rank> kickers, List<TexasCard> bestFive) {
        if (kickers.size() != 5) {
            throw new IllegalArgumentException("Kickers must have exactly 5 ranks");
        }
        if (bestFive.size() != 5) {
            throw new IllegalArgumentException("Best five must have exactly 5 cards");
        }
        this.rank = rank;
        this.kickers = List.copyOf(kickers);
        this.bestFive = List.copyOf(bestFive);
    }

    /**
     * 比较两手牌的强弱
     *
     * @return 正数表示this更强，负数表示other更强，0表示平局
     */
    @Override
    public int compareTo(EvaluatedHand other) {
        // 先比较牌型
        int rankComparison = this.rank.compareStrength(other.rank);
        if (rankComparison != 0) {
            return rankComparison;
        }

        // 牌型相同，比较踢脚牌
        for (int i = 0; i < kickers.size(); i++) {
            int kickerComparison = Integer.compare(
                this.kickers.get(i).getValue(),
                other.kickers.get(i).getValue()
            );
            if (kickerComparison != 0) {
                return kickerComparison;
            }
        }

        // 完全相同
        return 0;
    }

    /**
     * 判断是否胜过另一手牌
     */
    public boolean beats(EvaluatedHand other) {
        return compareTo(other) > 0;
    }

    /**
     * 判断是否平局
     */
    public boolean ties(EvaluatedHand other) {
        return compareTo(other) == 0;
    }

    /**
     * 判断是否输给另一手牌
     */
    public boolean loses(EvaluatedHand other) {
        return compareTo(other) < 0;
    }

    // ========== Getters ==========

    public HandRank getRank() {
        return rank;
    }

    public List<Rank> getKickers() {
        return kickers;
    }

    public List<TexasCard> getBestFive() {
        return bestFive;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", rank, bestFive);
    }

    public String toDetailString() {
        return String.format("%s - Kickers: %s - Best 5: %s", rank, kickers, bestFive);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluatedHand that = (EvaluatedHand) o;
        return rank == that.rank && Objects.equals(kickers, that.kickers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, kickers);
    }
}
