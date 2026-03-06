package com.poker.gto.core.evaluator;

import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.ranges.Range;

import java.util.List;

/**
 * 胜率计算器
 *
 * 计算手牌在给定公共牌下的胜率（Equity）
 */
public class EquityCalculator {

    /**
     * 胜率结果
     */
    public static class EquityResult {
        public final double winRate;    // 胜率
        public final double tieRate;    // 平局率
        public final double loseRate;   // 输率
        public final double equity;     // 总胜率 = winRate + tieRate/2

        public EquityResult(double winRate, double tieRate, double loseRate) {
            this.winRate = winRate;
            this.tieRate = tieRate;
            this.loseRate = loseRate;
            this.equity = winRate + tieRate / 2.0;
        }

        @Override
        public String toString() {
            return String.format("Equity: %.2f%% (Win: %.2f%%, Tie: %.2f%%, Lose: %.2f%%)",
                equity * 100, winRate * 100, tieRate * 100, loseRate * 100);
        }
    }

    /**
     * 计算手牌 vs 手牌的胜率（River场景，所有牌已知）
     *
     * @param hand1 玩家1的手牌（2张）
     * @param hand2 玩家2的手牌（2张）
     * @param board 公共牌（5张）
     * @return 玩家1的胜率
     */
    public static EquityResult calculateHeadsUp(Hand hand1, Hand hand2, List<TexasCard> board) {
        if (hand1.size() != 2 || hand2.size() != 2) {
            throw new IllegalArgumentException("Each hand must have exactly 2 cards");
        }
        if (board.size() != 5) {
            throw new IllegalArgumentException("Board must have exactly 5 cards");
        }

        // 构造7张牌
        Hand fullHand1 = hand1.combine(new Hand(board));
        Hand fullHand2 = hand2.combine(new Hand(board));

        // 评估
        EvaluatedHand eval1 = HandEvaluator.evaluate(fullHand1);
        EvaluatedHand eval2 = HandEvaluator.evaluate(fullHand2);

        // 比较
        int comparison = eval1.compareTo(eval2);

        if (comparison > 0) {
            // Player 1 wins
            return new EquityResult(1.0, 0.0, 0.0);
        } else if (comparison < 0) {
            // Player 1 loses
            return new EquityResult(0.0, 0.0, 1.0);
        } else {
            // Tie
            return new EquityResult(0.0, 1.0, 0.0);
        }
    }

    /**
     * 计算手牌 vs 范围的胜率
     *
     * @param hand 玩家手牌（2张）
     * @param range 对手范围
     * @param board 公共牌（5张）
     * @return 玩家的平均胜率
     */
    public static EquityResult calculateVsRange(Hand hand, Range range, List<TexasCard> board) {
        if (hand.size() != 2) {
            throw new IllegalArgumentException("Hand must have exactly 2 cards");
        }
        if (board.size() != 5) {
            throw new IllegalArgumentException("Board must have exactly 5 cards");
        }

        int wins = 0;
        int ties = 0;
        int losses = 0;
        int total = 0;

        // 评估我方手牌
        Hand fullHand = hand.combine(new Hand(board));
        EvaluatedHand evalHand = HandEvaluator.evaluate(fullHand);

        // 对手范围中的每一手牌
        for (Hand opponentHand : range.getHands()) {
            // 检查是否与已知牌冲突
            boolean conflict = false;
            for (TexasCard card : opponentHand.getCards()) {
                if (hand.contains(card) || board.contains(card)) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) {
                continue;
            }

            // 评估对手手牌
            Hand fullOpponentHand = opponentHand.combine(new Hand(board));
            EvaluatedHand evalOpponent = HandEvaluator.evaluate(fullOpponentHand);

            // 比较
            int comparison = evalHand.compareTo(evalOpponent);
            if (comparison > 0) {
                wins++;
            } else if (comparison < 0) {
                losses++;
            } else {
                ties++;
            }
            total++;
        }

        if (total == 0) {
            throw new IllegalStateException("No valid hands in range after removing conflicts");
        }

        double winRate = (double) wins / total;
        double tieRate = (double) ties / total;
        double loseRate = (double) losses / total;

        return new EquityResult(winRate, tieRate, loseRate);
    }

    /**
     * 计算范围 vs 范围的胜率
     *
     * @param range1 玩家1的范围
     * @param range2 玩家2的范围
     * @param board 公共牌（5张）
     * @return 玩家1的平均胜率
     */
    public static EquityResult calculateRangeVsRange(Range range1, Range range2, List<TexasCard> board) {
        if (board.size() != 5) {
            throw new IllegalArgumentException("Board must have exactly 5 cards");
        }

        int totalWins = 0;
        int totalTies = 0;
        int totalLosses = 0;
        int totalCombos = 0;

        for (Hand hand1 : range1.getHands()) {
            // 检查hand1是否与公共牌冲突
            boolean conflict1 = false;
            for (TexasCard card : hand1.getCards()) {
                if (board.contains(card)) {
                    conflict1 = true;
                    break;
                }
            }
            if (conflict1) {
                continue;
            }

            // 评估hand1
            Hand fullHand1 = hand1.combine(new Hand(board));
            EvaluatedHand eval1 = HandEvaluator.evaluate(fullHand1);

            for (Hand hand2 : range2.getHands()) {
                // 检查hand2是否与hand1或公共牌冲突
                boolean conflict2 = false;
                for (TexasCard card : hand2.getCards()) {
                    if (hand1.contains(card) || board.contains(card)) {
                        conflict2 = true;
                        break;
                    }
                }
                if (conflict2) {
                    continue;
                }

                // 评估hand2
                Hand fullHand2 = hand2.combine(new Hand(board));
                EvaluatedHand eval2 = HandEvaluator.evaluate(fullHand2);

                // 比较
                int comparison = eval1.compareTo(eval2);
                if (comparison > 0) {
                    totalWins++;
                } else if (comparison < 0) {
                    totalLosses++;
                } else {
                    totalTies++;
                }
                totalCombos++;
            }
        }

        if (totalCombos == 0) {
            throw new IllegalStateException("No valid hand combinations after removing conflicts");
        }

        double winRate = (double) totalWins / totalCombos;
        double tieRate = (double) totalTies / totalCombos;
        double loseRate = (double) totalLosses / totalCombos;

        return new EquityResult(winRate, tieRate, loseRate);
    }
}
