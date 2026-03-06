package com.poker.gto.app.demo;

import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.Suit;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.ranges.*;

import java.util.*;

/**
 * Range (手牌范围) 演示
 *
 * 展示：
 * 1. HandCombo 和 169种组合
 * 2. Range 创建和操作
 * 3. 字符串解析
 * 4. 范围操作（合并、过滤、标准化）
 * 5. 13x13 矩阵可视化
 */
public class RangeDemo {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  德州扑克手牌范围 (Range) 演示");
        System.out.println("=".repeat(70) + "\n");

        // 1. HandCombo 基础
        demonstrateHandCombo();

        // 2. Range 创建
        demonstrateRangeCreation();

        // 3. 字符串解析
        demonstrateRangeParsing();

        // 4. 范围操作
        demonstrateRangeOperations();

        // 5. 可视化
        demonstrateVisualization();

        // 6. 实战示例
        demonstrateRealWorld();

        System.out.println("=".repeat(70));
        System.out.println("  演示完成！");
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * 演示 HandCombo 基础
     */
    private static void demonstrateHandCombo() {
        System.out.println("【1. HandCombo - 169种起手牌组合】\n");

        // 三种类型
        HandCombo pair = HandCombo.parse("AA");
        HandCombo suited = HandCombo.parse("AKs");
        HandCombo offsuit = HandCombo.parse("AKo");

        System.out.println("对子 (Pair):");
        System.out.println("  " + pair + " - " + pair.getPossibleHands() + " 种具体手牌");

        System.out.println("\n同花 (Suited):");
        System.out.println("  " + suited + " - " + suited.getPossibleHands() + " 种具体手牌");

        System.out.println("\n非同花 (Offsuit):");
        System.out.println("  " + offsuit + " - " + offsuit.getPossibleHands() + " 种具体手牌");

        // 169种组合统计
        List<HandCombo> allCombos = HandCombo.getAllCombos();
        long pairs = allCombos.stream().filter(HandCombo::isPair).count();
        long suitedCombos = allCombos.stream().filter(c -> !c.isPair() && c.isSuited()).count();
        long offsuitCombos = allCombos.stream().filter(c -> !c.isPair() && !c.isSuited()).count();

        System.out.println("\n总计169种组合:");
        System.out.println("  对子: " + pairs + " (每种6个具体手牌)");
        System.out.println("  同花: " + suitedCombos + " (每种4个具体手牌)");
        System.out.println("  非同花: " + offsuitCombos + " (每种12个具体手牌)");
        System.out.println("  总手牌数: " + (pairs*6 + suitedCombos*4 + offsuitCombos*12) + " (C(52,2))");
        System.out.println();
    }

    /**
     * 演示 Range 创建
     */
    private static void demonstrateRangeCreation() {
        System.out.println("【2. Range 创建】\n");

        // 空范围
        Range empty = Range.empty();
        System.out.println("空范围: " + empty);

        // 单个组合
        Range singleCombo = Range.of(HandCombo.parse("AA"));
        System.out.println("单个组合: " + singleCombo);

        // 多个组合
        Range multiCombo = Range.of(
            HandCombo.parse("AA"),
            HandCombo.parse("KK"),
            HandCombo.parse("AKs")
        );
        System.out.println("多个组合: " + multiCombo);

        // 带权重
        Range weighted = Range.of(HandCombo.parse("AA"), 0.5);
        System.out.println("带权重: " + weighted);

        System.out.println();
    }

    /**
     * 演示字符串解析
     */
    private static void demonstrateRangeParsing() {
        System.out.println("【3. 字符串解析】\n");

        // 简单列表
        Range range1 = Range.parse("AA, KK, QQ");
        System.out.println("简单列表: \"AA, KK, QQ\"");
        System.out.println("  → " + range1);

        // 带权重
        Range range2 = Range.parse("AA:0.5, KK:0.75, QQ");
        System.out.println("\n带权重: \"AA:0.5, KK:0.75, QQ\"");
        System.out.println("  → " + range2);

        // 范围
        Range range3 = Range.parse("AA-JJ");
        System.out.println("\n对子范围: \"AA-JJ\"");
        System.out.println("  → " + range3);

        Range range4 = Range.parse("AKs-ATs");
        System.out.println("\n同花范围: \"AKs-ATs\"");
        System.out.println("  → " + range4);

        // 通配符
        Range range5 = Range.parse("A*s");
        System.out.println("\n通配符: \"A*s\" (所有同花A)");
        System.out.println("  → " + range5.size() + " 种组合");

        // 混合
        Range range6 = Range.parse("AA-TT, AKs, AKo:0.5, Q*s");
        System.out.println("\n混合: \"AA-TT, AKs, AKo:0.5, Q*s\"");
        System.out.println("  → " + range6.size() + " 种组合");

        System.out.println();
    }

    /**
     * 演示范围操作
     */
    private static void demonstrateRangeOperations() {
        System.out.println("【4. 范围操作】\n");

        Range range1 = Range.parse("AA, KK, AKs");
        Range range2 = Range.parse("KK, QQ, AKo");

        // 合并
        Range merged = range1.merge(range2);
        System.out.println("合并 (Merge):");
        System.out.println("  Range1: " + range1);
        System.out.println("  Range2: " + range2);
        System.out.println("  Merged: " + merged);

        // 交集
        Range intersected = range1.intersect(range2);
        System.out.println("\n交集 (Intersect):");
        System.out.println("  " + intersected);

        // 差集
        Range subtracted = range1.subtract(range2);
        System.out.println("\n差集 (Subtract):");
        System.out.println("  " + subtracted);

        // 过滤
        Range allRange = Range.parse("AA-22, AKs-A2s, AKo-ATo");
        System.out.println("\n过滤 (Filter):");
        System.out.println("  原范围: " + allRange.size() + " 种组合");
        System.out.println("  只保留对子: " + allRange.onlyPairs().size() + " 种");
        System.out.println("  只保留同花: " + allRange.onlySuited().size() + " 种");
        System.out.println("  只保留非同花: " + allRange.onlyOffsuit().size() + " 种");

        // 标准化
        Range weighted = Range.parse("AA:0.3, KK:0.2");
        Range normalized = weighted.normalizeWeights();
        System.out.println("\n标准化 (Normalize):");
        System.out.println("  原范围: " + weighted);
        System.out.println("  标准化: " + normalized);

        System.out.println();
    }

    /**
     * 演示可视化
     */
    private static void demonstrateVisualization() {
        System.out.println("【5. 范围可视化 - 13x13 矩阵】\n");

        // 紧手范围
        Range tightRange = RangeParser.premiumRange();
        System.out.println("Premium 范围 (超紧):");
        System.out.println(tightRange.getSummary());
        System.out.println("\n紧凑视图:");
        System.out.println(RangeVisualizer.visualizeCompact(tightRange));

        // TAG 范围
        Range tagRange = RangeParser.tightRange();
        System.out.println("TAG 范围 (紧凶):");
        System.out.println(tagRange.getSummary());
        System.out.println("\n紧凑视图:");
        System.out.println(RangeVisualizer.visualizeCompact(tagRange));

        // 自定义范围
        Range customRange = Range.parse("AA-99, AKs-AJs, AKo:0.5");
        System.out.println("自定义范围:");
        System.out.println(customRange.getSummary());
        System.out.println("\n符号视图 (█=100%, ▓=75%, ▒=50%, ░=25%):");
        System.out.println(RangeVisualizer.visualize(customRange, RangeVisualizer.VisualizationMode.SYMBOLS));

        System.out.println();
    }

    /**
     * 实战示例
     */
    private static void demonstrateRealWorld() {
        System.out.println("【6. 实战示例 - River 场景】\n");

        // 场景：你在按钮位，对手在大盲位
        // 公共牌：A♥ K♦ Q♥ 9♣ 2♠
        List<TexasCard> board = Arrays.asList(
            TexasCard.of(Rank.ACE, Suit.HEARTS),
            TexasCard.of(Rank.KING, Suit.DIAMONDS),
            TexasCard.of(Rank.QUEEN, Suit.HEARTS),
            TexasCard.of(Rank.NINE, Suit.CLUBS),
            TexasCard.of(Rank.TWO, Suit.SPADES)
        );

        System.out.println("场景: River 对抗");
        System.out.println("公共牌: " + formatBoard(board));
        System.out.println();

        // 对手的感知范围（基于游戏历史）
        Range villainRange = Range.parse("AA-99, AKs-ATs, AKo-AQo");
        System.out.println("对手感知范围 (Preflop):");
        System.out.println("  " + villainRange.getSummary());

        // 移除死牌
        Range villainRiverRange = villainRange.removeDeadCards(board);
        System.out.println("\n对手 River 范围 (移除死牌后):");
        System.out.println("  " + villainRiverRange.getSummary());

        // 你的范围（已知底牌）
        System.out.println("\n你的底牌: J♠ T♣");
        System.out.println("你的牌型: 顺子 (AKQJT)");

        // 分析
        System.out.println("\n范围分析:");
        System.out.println("  对手可能拿着:");
        System.out.println("    - 顶对: AK, AQ, AT (很强)");
        System.out.println("    - 大对子: KK, QQ, JJ, TT, 99 (中等)");
        System.out.println("    - 你领先所有这些组合 (顺子 > 对子)");

        System.out.println("\n决策:");
        System.out.println("  → 价值下注！你的顺子在这里很强");

        System.out.println();
    }

    /**
     * 格式化公共牌
     */
    private static String formatBoard(List<TexasCard> board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(board.get(i).toUnicodeString());
        }
        return sb.toString();
    }
}
