package com.poker.gto.core.ranges;

import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.Suit;
import com.poker.gto.core.cards.TexasCard;

import java.util.*;

/**
 * 德州扑克起手牌组合
 *
 * 表示169种起手牌类型：
 * - 13种对子：AA, KK, ..., 22
 * - 78种同花：AKs, AQs, ..., 32s
 * - 78种非同花：AKo, AQo, ..., 32o
 *
 * 不可变类
 */
public class HandCombo implements Comparable<HandCombo> {

    private final Rank highRank;  // 高牌
    private final Rank lowRank;   // 低牌
    private final boolean suited;  // 是否同花
    private final boolean isPair;  // 是否对子

    /**
     * 私有构造函数
     */
    private HandCombo(Rank highRank, Rank lowRank, boolean suited) {
        // 确保 highRank >= lowRank
        if (highRank.getValue() < lowRank.getValue()) {
            Rank temp = highRank;
            highRank = lowRank;
            lowRank = temp;
        }

        this.highRank = highRank;
        this.lowRank = lowRank;
        this.suited = suited;
        this.isPair = (highRank == lowRank);

        // 对子不能有suited属性
        if (isPair && suited) {
            throw new IllegalArgumentException("Pairs cannot be suited");
        }
    }

    /**
     * 创建手牌组合
     */
    public static HandCombo of(Rank rank1, Rank rank2, boolean suited) {
        return new HandCombo(rank1, rank2, suited);
    }

    /**
     * 从字符串解析 (如 "AKs", "AKo", "AA")
     */
    public static HandCombo parse(String str) {
        str = str.trim().toUpperCase();

        if (str.length() < 2 || str.length() > 3) {
            throw new IllegalArgumentException("Invalid hand combo: " + str);
        }

        // 解析两个rank
        Rank rank1 = Rank.fromSymbol(str.substring(0, 1));
        Rank rank2 = Rank.fromSymbol(str.substring(1, 2));

        // 对子
        if (rank1 == rank2) {
            if (str.length() == 2) {
                return new HandCombo(rank1, rank2, false);
            } else {
                throw new IllegalArgumentException("Pairs cannot have suit modifier: " + str);
            }
        }

        // 非对子
        if (str.length() == 2) {
            // 默认非同花
            return new HandCombo(rank1, rank2, false);
        } else if (str.length() == 3) {
            char suitChar = str.charAt(2);
            boolean suited = switch (suitChar) {
                case 'S' -> true;
                case 'O' -> false;
                default -> throw new IllegalArgumentException("Invalid suit modifier: " + suitChar);
            };
            return new HandCombo(rank1, rank2, suited);
        }

        throw new IllegalArgumentException("Invalid hand combo: " + str);
    }

    /**
     * 获取所有169种起手牌组合
     */
    public static List<HandCombo> getAllCombos() {
        List<HandCombo> combos = new ArrayList<>(169);
        Rank[] ranks = Rank.values();

        for (int i = 0; i < ranks.length; i++) {
            for (int j = 0; j <= i; j++) {
                Rank r1 = ranks[i];
                Rank r2 = ranks[j];

                if (i == j) {
                    // 对子
                    combos.add(new HandCombo(r1, r2, false));
                } else {
                    // 同花
                    combos.add(new HandCombo(r1, r2, true));
                    // 非同花
                    combos.add(new HandCombo(r1, r2, false));
                }
            }
        }

        return combos;
    }

    /**
     * 获取此组合可能的具体手牌数量
     *
     * - 对子: 6种 (C(4,2) = 6)
     * - 同花: 4种 (4种花色)
     * - 非同花: 12种 (4*3 = 12)
     */
    public int getPossibleHands() {
        if (isPair) {
            return 6;  // C(4,2)
        } else if (suited) {
            return 4;  // 4种花色
        } else {
            return 12; // 4*3
        }
    }

    /**
     * 生成所有可能的具体手牌 (TexasCard对)
     */
    public List<List<TexasCard>> generateAllHands() {
        List<List<TexasCard>> hands = new ArrayList<>();

        if (isPair) {
            // 对子：从4张牌中选2张
            Suit[] suits = Suit.values();
            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 4; j++) {
                    hands.add(List.of(
                        TexasCard.of(highRank, suits[i]),
                        TexasCard.of(highRank, suits[j])
                    ));
                }
            }
        } else if (suited) {
            // 同花：4种花色
            for (Suit suit : Suit.values()) {
                hands.add(List.of(
                    TexasCard.of(highRank, suit),
                    TexasCard.of(lowRank, suit)
                ));
            }
        } else {
            // 非同花：4*3 = 12种
            for (Suit suit1 : Suit.values()) {
                for (Suit suit2 : Suit.values()) {
                    if (suit1 != suit2) {
                        hands.add(List.of(
                            TexasCard.of(highRank, suit1),
                            TexasCard.of(lowRank, suit2)
                        ));
                    }
                }
            }
        }

        return hands;
    }

    /**
     * 判断给定的两张牌是否属于此组合
     */
    public boolean matches(TexasCard card1, TexasCard card2) {
        Rank r1 = card1.getRank();
        Rank r2 = card2.getRank();

        // 确保 r1 >= r2
        if (r1.getValue() < r2.getValue()) {
            Rank temp = r1;
            r1 = r2;
            r2 = temp;
        }

        // 检查rank
        if (r1 != highRank || r2 != lowRank) {
            return false;
        }

        // 检查suited
        if (isPair) {
            return card1.getSuit() != card2.getSuit();
        } else {
            boolean cardsSuited = (card1.getSuit() == card2.getSuit());
            return cardsSuited == suited;
        }
    }

    // ========== Getters ==========

    public Rank getHighRank() {
        return highRank;
    }

    public Rank getLowRank() {
        return lowRank;
    }

    public boolean isSuited() {
        return suited;
    }

    public boolean isPair() {
        return isPair;
    }

    // ========== 字符串表示 ==========

    @Override
    public String toString() {
        if (isPair) {
            return highRank.getSymbol() + lowRank.getSymbol();
        } else {
            return highRank.getSymbol() + lowRank.getSymbol() + (suited ? "s" : "o");
        }
    }

    /**
     * 获取在13x13矩阵中的位置
     * @return [row, col] 其中row和col都是0-12
     */
    public int[] getMatrixPosition() {
        int highIndex = 12 - (highRank.getValue() - 2);  // A=0, K=1, ..., 2=12
        int lowIndex = 12 - (lowRank.getValue() - 2);

        if (isPair) {
            return new int[]{highIndex, highIndex};
        } else if (suited) {
            // 同花在对角线下方
            return new int[]{lowIndex, highIndex};
        } else {
            // 非同花在对角线上方
            return new int[]{highIndex, lowIndex};
        }
    }

    // ========== 比较和相等性 ==========

    @Override
    public int compareTo(HandCombo other) {
        // 先比较高牌
        int highComp = Integer.compare(other.highRank.getValue(), this.highRank.getValue());
        if (highComp != 0) return highComp;

        // 再比较低牌
        int lowComp = Integer.compare(other.lowRank.getValue(), this.lowRank.getValue());
        if (lowComp != 0) return lowComp;

        // 最后比较suited (对子 > 同花 > 非同花)
        if (this.isPair && !other.isPair) return -1;
        if (!this.isPair && other.isPair) return 1;
        if (this.suited && !other.suited) return -1;
        if (!this.suited && other.suited) return 1;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandCombo that = (HandCombo) o;
        return suited == that.suited &&
               isPair == that.isPair &&
               highRank == that.highRank &&
               lowRank == that.lowRank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(highRank, lowRank, suited, isPair);
    }
}
