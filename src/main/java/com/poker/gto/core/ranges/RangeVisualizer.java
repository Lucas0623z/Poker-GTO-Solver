package com.poker.gto.core.ranges;

import com.poker.gto.core.cards.Rank;

/**
 * 手牌范围可视化工具
 *
 * 将范围显示为13x13矩阵：
 * - 对角线：对子 (AA, KK, QQ, ...)
 * - 对角线上方：非同花 (AKo, AQo, ...)
 * - 对角线下方：同花 (AKs, AQs, ...)
 *
 * 示例：
 * ```
 *     A    K    Q    J    T    9    8    7    6    5    4    3    2
 * A  [AA] [AKo][AQo][AJo][ATo][A9o][A8o][A7o][A6o][A5o][A4o][A3o][A2o]
 * K  [AKs][KK] [KQo][KJo][KTo][K9o][K8o][K7o][K6o][K5o][K4o][K3o][K2o]
 * Q  [AQs][KQs][QQ] [QJo][QTo][Q9o][Q8o][Q7o][Q6o][Q5o][Q4o][Q3o][Q2o]
 * ...
 * ```
 */
public class RangeVisualizer {

    private static final Rank[] RANKS = Rank.values();

    /**
     * 将范围可视化为13x13矩阵（ASCII）
     */
    public static String visualize(Range range) {
        return visualize(range, VisualizationMode.ASCII);
    }

    /**
     * 将范围可视化为13x13矩阵
     */
    public static String visualize(Range range, VisualizationMode mode) {
        StringBuilder sb = new StringBuilder();

        // 表头
        sb.append("      ");
        for (Rank rank : RANKS) {
            sb.append(String.format("%-5s", rank.getSymbol()));
        }
        sb.append("\n");

        // 每一行
        for (int row = 0; row < 13; row++) {
            Rank rowRank = RANKS[row];
            sb.append(String.format("%-2s    ", rowRank.getSymbol()));

            for (int col = 0; col < 13; col++) {
                Rank colRank = RANKS[col];

                HandCombo combo = getCombo(rowRank, colRank);
                double weight = range.getWeight(combo);

                String cell = formatCell(combo, weight, mode);
                sb.append(String.format("%-5s", cell));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 生成紧凑的范围热力图（使用符号表示权重）
     */
    public static String visualizeCompact(Range range) {
        StringBuilder sb = new StringBuilder();

        // 表头
        sb.append("   ");
        for (Rank rank : RANKS) {
            sb.append(" " + rank.getSymbol());
        }
        sb.append("\n");

        // 每一行
        for (int row = 0; row < 13; row++) {
            Rank rowRank = RANKS[row];
            sb.append(rowRank.getSymbol() + " ");

            for (int col = 0; col < 13; col++) {
                Rank colRank = RANKS[col];

                HandCombo combo = getCombo(rowRank, colRank);
                double weight = range.getWeight(combo);

                sb.append(" " + getWeightSymbol(weight));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 生成统计信息
     */
    public static String getStats(Range range) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total Combos: %.1f / 1326\n", range.getTotalCombos()));
        sb.append(String.format("Percentage: %.1f%%\n", range.getTotalCombos() / 1326.0 * 100.0));
        sb.append(String.format("Pairs: %.1f%%\n", range.getPairPercentage()));
        sb.append(String.format("Suited: %.1f%%\n", range.getSuitedPercentage()));
        sb.append(String.format("Offsuit: %.1f%%\n",
            100.0 - range.getPairPercentage() - range.getSuitedPercentage()));
        return sb.toString();
    }

    /**
     * 获取指定位置的组合
     */
    private static HandCombo getCombo(Rank rowRank, Rank colRank) {
        int rowVal = rowRank.getValue();
        int colVal = colRank.getValue();

        if (rowVal == colVal) {
            // 对角线：对子
            return HandCombo.of(rowRank, colRank, false);
        } else if (rowVal > colVal) {
            // 对角线下方：同花
            return HandCombo.of(rowRank, colRank, true);
        } else {
            // 对角线上方：非同花
            return HandCombo.of(colRank, rowRank, false);
        }
    }

    /**
     * 格式化单元格
     */
    private static String formatCell(HandCombo combo, double weight, VisualizationMode mode) {
        if (weight == 0.0) {
            return mode == VisualizationMode.ASCII ? "[ ]" : "·";
        }

        switch (mode) {
            case ASCII:
                if (weight >= 0.999) {
                    return "[" + combo.toString().substring(0, Math.min(3, combo.toString().length())) + "]";
                } else {
                    return String.format("[%.0f%%]", weight * 100);
                }

            case SYMBOLS:
                return getWeightSymbol(weight);

            case PERCENTAGE:
                return String.format("%.0f%%", weight * 100);

            default:
                return "?";
        }
    }

    /**
     * 将权重转换为符号
     */
    private static String getWeightSymbol(double weight) {
        if (weight >= 0.999) return "█";  // 100%
        if (weight >= 0.75) return "▓";   // 75%+
        if (weight >= 0.50) return "▒";   // 50%+
        if (weight >= 0.25) return "░";   // 25%+
        if (weight > 0.0) return "·";     // >0%
        return " ";                        // 0%
    }

    /**
     * 可视化模式
     */
    public enum VisualizationMode {
        ASCII,       // [AA], [AKs], [ ]
        SYMBOLS,     // █, ▓, ▒, ░, ·
        PERCENTAGE   // 100%, 50%, 0%
    }
}
