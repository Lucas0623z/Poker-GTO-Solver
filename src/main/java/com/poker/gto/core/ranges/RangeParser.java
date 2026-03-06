package com.poker.gto.core.ranges;

import com.poker.gto.core.cards.Rank;

import java.util.*;
import java.util.regex.*;

/**
 * 手牌范围解析器
 *
 * 支持的格式：
 * 1. 单个组合: "AA", "AKs", "AKo"
 * 2. 带权重: "AA:0.5", "KK:0.75"
 * 3. 列表: "AA, KK, AKs"
 * 4. 范围: "AA-TT" (对子范围), "AKs-ATs" (同花范围), "AKo-ATo" (非同花范围)
 * 5. 混合: "AA-JJ, AKs, AKo:0.5"
 * 6. 通配符: "A*s" (所有同花A), "K*o" (所有非同花K), "*8s" (所有同花8)
 */
public class RangeParser {

    // 匹配单个组合的正则 (如 "AKs" 或 "AKs:0.5")
    private static final Pattern COMBO_PATTERN =
        Pattern.compile("([AKQJT98765432]{2}[SO]?)(?::(\\d*\\.?\\d+))?");

    // 匹配范围的正则 (如 "AA-TT" 或 "AKs-ATs")
    private static final Pattern RANGE_PATTERN =
        Pattern.compile("([AKQJT98765432]{2}[SO]?)-([AKQJT98765432]{2}[SO]?)(?::(\\d*\\.?\\d+))?");

    /**
     * 解析范围字符串
     */
    public static Range parse(String rangeString) {
        if (rangeString == null || rangeString.trim().isEmpty()) {
            return Range.empty();
        }

        Map<HandCombo, Double> weights = new HashMap<>();

        // 按逗号分割
        String[] parts = rangeString.split(",");
        for (String part : parts) {
            part = part.trim().toUpperCase();
            if (part.isEmpty()) {
                continue;
            }

            parsePart(part, weights);
        }

        return Range.fromMap(weights);
    }

    /**
     * 解析单个部分
     */
    private static void parsePart(String part, Map<HandCombo, Double> weights) {
        // 尝试匹配范围 (AA-TT)
        Matcher rangeMatcher = RANGE_PATTERN.matcher(part);
        if (rangeMatcher.matches()) {
            parseRange(rangeMatcher, weights);
            return;
        }

        // 尝试匹配单个组合 (AKs 或 AKs:0.5)
        Matcher comboMatcher = COMBO_PATTERN.matcher(part);
        if (comboMatcher.matches()) {
            parseCombo(comboMatcher, weights);
            return;
        }

        // 尝试通配符 (A*s, K*o, *8s)
        if (part.contains("*")) {
            parseWildcard(part, weights);
            return;
        }

        throw new IllegalArgumentException("Invalid range part: " + part);
    }

    /**
     * 解析单个组合
     */
    private static void parseCombo(Matcher matcher, Map<HandCombo, Double> weights) {
        String comboStr = matcher.group(1);
        String weightStr = matcher.group(2);

        HandCombo combo = HandCombo.parse(comboStr);
        double weight = weightStr != null ? Double.parseDouble(weightStr) : 1.0;

        weights.merge(combo, weight, Double::max);
    }

    /**
     * 解析范围 (如 AA-TT, AKs-ATs)
     */
    private static void parseRange(Matcher matcher, Map<HandCombo, Double> weights) {
        String startStr = matcher.group(1);
        String endStr = matcher.group(2);
        String weightStr = matcher.group(3);

        HandCombo start = HandCombo.parse(startStr);
        HandCombo end = HandCombo.parse(endStr);
        double weight = weightStr != null ? Double.parseDouble(weightStr) : 1.0;

        // 验证范围合法性
        if (start.isPair() != end.isPair()) {
            throw new IllegalArgumentException("Range must be of same type (both pairs or both non-pairs)");
        }

        if (!start.isPair() && start.isSuited() != end.isSuited()) {
            throw new IllegalArgumentException("Range must have same suited property");
        }

        // 生成范围内的所有组合
        List<HandCombo> combos = generateRange(start, end);
        for (HandCombo combo : combos) {
            weights.merge(combo, weight, Double::max);
        }
    }

    /**
     * 生成范围内的所有组合
     */
    private static List<HandCombo> generateRange(HandCombo start, HandCombo end) {
        List<HandCombo> combos = new ArrayList<>();

        if (start.isPair()) {
            // 对子范围 (如 AA-TT)
            int startVal = start.getHighRank().getValue();
            int endVal = end.getHighRank().getValue();

            // 确保从大到小
            if (startVal < endVal) {
                int temp = startVal;
                startVal = endVal;
                endVal = temp;
            }

            for (int val = startVal; val >= endVal; val--) {
                Rank rank = Rank.fromValue(val);
                combos.add(HandCombo.of(rank, rank, false));
            }
        } else {
            // 同花或非同花范围 (如 AKs-ATs)
            Rank highRank = start.getHighRank();
            boolean suited = start.isSuited();

            int startLowVal = start.getLowRank().getValue();
            int endLowVal = end.getLowRank().getValue();

            // 确保从大到小
            if (startLowVal < endLowVal) {
                int temp = startLowVal;
                startLowVal = endLowVal;
                endLowVal = temp;
            }

            for (int val = startLowVal; val >= endLowVal; val--) {
                Rank lowRank = Rank.fromValue(val);
                if (lowRank != highRank) {  // 避免对子
                    combos.add(HandCombo.of(highRank, lowRank, suited));
                }
            }
        }

        return combos;
    }

    /**
     * 解析通配符
     * - "A*s": 所有同花A (AKs, AQs, ..., A2s)
     * - "K*o": 所有非同花K (KQo, KJo, ..., K2o)
     * - "*8s": 所有同花8 (A8s, K8s, ..., 98s)
     */
    private static void parseWildcard(String part, Map<HandCombo, Double> weights) {
        // 提取权重
        double weight = 1.0;
        if (part.contains(":")) {
            String[] split = part.split(":");
            part = split[0].trim();
            weight = Double.parseDouble(split[1].trim());
        }

        // 提取suited/offsuit
        boolean suited;
        if (part.endsWith("S")) {
            suited = true;
            part = part.substring(0, part.length() - 1);
        } else if (part.endsWith("O")) {
            suited = false;
            part = part.substring(0, part.length() - 1);
        } else {
            throw new IllegalArgumentException("Wildcard must end with 's' or 'o': " + part);
        }

        // 提取rank
        if (part.length() != 2 || !part.contains("*")) {
            throw new IllegalArgumentException("Invalid wildcard format: " + part);
        }

        Rank fixedRank = null;
        boolean fixedIsHigh;  // true: 固定的是高牌, false: 固定的是低牌

        if (part.charAt(0) == '*') {
            // *8s: 8是固定低牌
            fixedRank = Rank.fromSymbol(part.substring(1, 2));
            fixedIsHigh = false;
        } else if (part.charAt(1) == '*') {
            // A*s: A是固定高牌
            fixedRank = Rank.fromSymbol(part.substring(0, 1));
            fixedIsHigh = true;
        } else {
            throw new IllegalArgumentException("Wildcard must contain '*': " + part);
        }

        // 生成所有组合
        for (Rank rank : Rank.values()) {
            if (rank == fixedRank) {
                continue;  // 跳过对子
            }

            HandCombo combo;
            if (fixedIsHigh) {
                // 固定高牌 (如 A*s: AKs, AQs, ...)
                if (rank.getValue() < fixedRank.getValue()) {
                    combo = HandCombo.of(fixedRank, rank, suited);
                } else {
                    continue;  // 跳过不合法的组合
                }
            } else {
                // 固定低牌 (如 *8s: A8s, K8s, ...)
                if (rank.getValue() > fixedRank.getValue()) {
                    combo = HandCombo.of(rank, fixedRank, suited);
                } else {
                    continue;  // 跳过不合法的组合
                }
            }

            weights.merge(combo, weight, Double::max);
        }
    }

    /**
     * 便捷方法：创建紧手范围 (典型的TAG玩家开牌范围)
     */
    public static Range tightRange() {
        return parse("AA-TT, AKs, AKo");
    }

    /**
     * 便捷方法：创建松手范围 (典型的LAG玩家开牌范围)
     */
    public static Range looseRange() {
        return parse("AA-22, AKs-A2s, KQs-K9s, QJs-QTs, JTs, AKo-ATo, KQo-KJo");
    }

    /**
     * 便捷方法：创建超紧范围 (只有顶级牌)
     */
    public static Range premiumRange() {
        return parse("AA, KK, QQ, AKs, AKo");
    }

    /**
     * 便捷方法：创建投机范围 (同花连牌和小对子)
     */
    public static Range speculativeRange() {
        return parse("22-66, 87s-54s, 76s-54s, 65s-54s");
    }
}
