package com.poker.gto.core.ranges;

import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.TexasCard;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 德州扑克手牌范围
 *
 * 表示一个玩家可能的起手牌分布，每种组合有一个权重(0.0-1.0)
 * - 权重1.0: 总是有这手牌
 * - 权重0.5: 50%概率有这手牌
 * - 权重0.0: 从不有这手牌
 *
 * 例如：
 * - "AA, KK" - 只有AA和KK，各100%
 * - "AA:0.5, KK" - 50% AA，100% KK
 */
public class Range {

    // 169种组合的权重 (使用Map存储非零权重，节省空间)
    private final Map<HandCombo, Double> weights;

    /**
     * 私有构造函数
     */
    private Range(Map<HandCombo, Double> weights) {
        this.weights = new HashMap<>(weights);
    }

    /**
     * 创建空范围
     */
    public static Range empty() {
        return new Range(new HashMap<>());
    }

    /**
     * 创建包含所有169种组合的范围(权重均为1.0)
     */
    public static Range all() {
        Map<HandCombo, Double> weights = new HashMap<>();
        for (HandCombo combo : HandCombo.getAllCombos()) {
            weights.put(combo, 1.0);
        }
        return new Range(weights);
    }

    /**
     * 从单个组合创建
     */
    public static Range of(HandCombo combo) {
        return of(combo, 1.0);
    }

    /**
     * 从单个组合创建（带权重）
     */
    public static Range of(HandCombo combo, double weight) {
        Map<HandCombo, Double> weights = new HashMap<>();
        weights.put(combo, clampWeight(weight));
        return new Range(weights);
    }

    /**
     * 从多个组合创建（权重均为1.0）
     */
    public static Range of(HandCombo... combos) {
        Map<HandCombo, Double> weights = new HashMap<>();
        for (HandCombo combo : combos) {
            weights.put(combo, 1.0);
        }
        return new Range(weights);
    }

    /**
     * 从Map创建
     */
    public static Range fromMap(Map<HandCombo, Double> weights) {
        Map<HandCombo, Double> clampedWeights = new HashMap<>();
        for (Map.Entry<HandCombo, Double> entry : weights.entrySet()) {
            double weight = clampWeight(entry.getValue());
            if (weight > 0.0) {
                clampedWeights.put(entry.getKey(), weight);
            }
        }
        return new Range(clampedWeights);
    }

    /**
     * 从字符串解析 (使用RangeParser)
     */
    public static Range parse(String rangeString) {
        return RangeParser.parse(rangeString);
    }

    // ========== 查询操作 ==========

    /**
     * 获取组合的权重
     */
    public double getWeight(HandCombo combo) {
        return weights.getOrDefault(combo, 0.0);
    }

    /**
     * 设置组合的权重（返回新Range）
     */
    public Range setWeight(HandCombo combo, double weight) {
        Map<HandCombo, Double> newWeights = new HashMap<>(weights);
        weight = clampWeight(weight);
        if (weight > 0.0) {
            newWeights.put(combo, weight);
        } else {
            newWeights.remove(combo);
        }
        return new Range(newWeights);
    }

    /**
     * 判断是否包含组合（权重>0）
     */
    public boolean contains(HandCombo combo) {
        return weights.containsKey(combo) && weights.get(combo) > 0.0;
    }

    /**
     * 判断是否为空范围
     */
    public boolean isEmpty() {
        return weights.isEmpty();
    }

    /**
     * 获取所有非零权重的组合
     */
    public Set<HandCombo> getCombos() {
        return new HashSet<>(weights.keySet());
    }

    /**
     * 获取范围中所有可能的具体手牌（用于胜率计算等）
     * 当组合权重>0时，包含该组合的所有具体手牌
     */
    public List<Hand> getHands() {
        List<Hand> result = new ArrayList<>();
        for (Map.Entry<HandCombo, Double> entry : weights.entrySet()) {
            if (entry.getValue() <= 0.0) continue;
            for (List<TexasCard> cards : entry.getKey().generateAllHands()) {
                result.add(new Hand(cards));
            }
        }
        return result;
    }

    /**
     * 获取权重Map（不可修改）
     */
    public Map<HandCombo, Double> getWeights() {
        return Collections.unmodifiableMap(weights);
    }

    /**
     * 获取组合数量（权重>0的组合数）
     */
    public int size() {
        return weights.size();
    }

    /**
     * 获取总手牌数量（考虑权重和每种组合的可能手牌数）
     */
    public double getTotalCombos() {
        double total = 0.0;
        for (Map.Entry<HandCombo, Double> entry : weights.entrySet()) {
            total += entry.getKey().getPossibleHands() * entry.getValue();
        }
        return total;
    }

    // ========== 范围操作 ==========

    /**
     * 合并两个范围（权重相加，最大1.0）
     */
    public Range merge(Range other) {
        Map<HandCombo, Double> newWeights = new HashMap<>(this.weights);

        for (Map.Entry<HandCombo, Double> entry : other.weights.entrySet()) {
            HandCombo combo = entry.getKey();
            double otherWeight = entry.getValue();
            double currentWeight = newWeights.getOrDefault(combo, 0.0);
            newWeights.put(combo, Math.min(1.0, currentWeight + otherWeight));
        }

        return new Range(newWeights);
    }

    /**
     * 范围交集（取较小权重）
     */
    public Range intersect(Range other) {
        Map<HandCombo, Double> newWeights = new HashMap<>();

        for (HandCombo combo : this.weights.keySet()) {
            if (other.weights.containsKey(combo)) {
                double minWeight = Math.min(this.weights.get(combo), other.weights.get(combo));
                newWeights.put(combo, minWeight);
            }
        }

        return new Range(newWeights);
    }

    /**
     * 范围差集（从当前范围移除另一范围）
     */
    public Range subtract(Range other) {
        Map<HandCombo, Double> newWeights = new HashMap<>(this.weights);

        for (HandCombo combo : other.weights.keySet()) {
            newWeights.remove(combo);
        }

        return new Range(newWeights);
    }

    /**
     * 过滤范围（保留满足条件的组合）
     */
    public Range filter(Predicate<HandCombo> predicate) {
        Map<HandCombo, Double> newWeights = new HashMap<>();

        for (Map.Entry<HandCombo, Double> entry : weights.entrySet()) {
            if (predicate.test(entry.getKey())) {
                newWeights.put(entry.getKey(), entry.getValue());
            }
        }

        return new Range(newWeights);
    }

    /**
     * 缩放所有权重
     */
    public Range scale(double factor) {
        if (factor <= 0.0) {
            return Range.empty();
        }

        Map<HandCombo, Double> newWeights = new HashMap<>();
        for (Map.Entry<HandCombo, Double> entry : weights.entrySet()) {
            double newWeight = clampWeight(entry.getValue() * factor);
            if (newWeight > 0.0) {
                newWeights.put(entry.getKey(), newWeight);
            }
        }

        return new Range(newWeights);
    }

    /**
     * 标准化范围（使所有权重和为指定值，默认为最大可能手牌数1326）
     */
    public Range normalize() {
        return normalize(1326.0);
    }

    /**
     * 标准化范围（使总组合数为指定值）
     */
    public Range normalize(double targetTotal) {
        double currentTotal = getTotalCombos();
        if (currentTotal == 0.0) {
            return this;
        }

        double factor = targetTotal / currentTotal;
        return scale(factor);
    }

    /**
     * 标准化权重（使所有权重和为1.0）
     */
    public Range normalizeWeights() {
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight == 0.0) {
            return this;
        }

        Map<HandCombo, Double> newWeights = new HashMap<>();
        for (Map.Entry<HandCombo, Double> entry : weights.entrySet()) {
            newWeights.put(entry.getKey(), entry.getValue() / totalWeight);
        }

        return new Range(newWeights);
    }

    // ========== 移除死牌 ==========

    /**
     * 移除包含指定牌的组合（用于已知公共牌或对手牌时）
     */
    public Range removeDeadCards(TexasCard... deadCards) {
        return removeDeadCards(Arrays.asList(deadCards));
    }

    /**
     * 移除包含指定牌的组合
     */
    public Range removeDeadCards(List<TexasCard> deadCards) {
        Set<Rank> deadRanks = deadCards.stream()
            .map(TexasCard::getRank)
            .collect(Collectors.toSet());

        return filter(combo -> {
            // 检查是否使用了死牌的rank
            boolean usesDeadRank = deadRanks.contains(combo.getHighRank()) ||
                                   deadRanks.contains(combo.getLowRank());
            if (!usesDeadRank) {
                return true;
            }

            // 如果使用了死牌rank，需要检查具体组合是否可用
            // 这里简化处理：如果对子且有2张死牌，则移除；否则减少权重
            if (combo.isPair()) {
                long deadCount = deadCards.stream()
                    .filter(c -> c.getRank() == combo.getHighRank())
                    .count();
                return deadCount < 2;  // 对子需要至少2张可用牌
            }

            return true;  // 非对子：简化处理，假设总有可用花色
        });
    }

    // ========== 便捷过滤方法 ==========

    /**
     * 只保留对子
     */
    public Range onlyPairs() {
        return filter(HandCombo::isPair);
    }

    /**
     * 只保留同花
     */
    public Range onlySuited() {
        return filter(HandCombo::isSuited);
    }

    /**
     * 只保留非同花
     */
    public Range onlyOffsuit() {
        return filter(combo -> !combo.isPair() && !combo.isSuited());
    }

    /**
     * 只保留高牌大于等于指定rank的组合
     */
    public Range highCardAtLeast(Rank minRank) {
        return filter(combo -> combo.getHighRank().getValue() >= minRank.getValue());
    }

    // ========== 统计信息 ==========

    /**
     * 计算范围中对子的百分比
     */
    public double getPairPercentage() {
        double pairCombos = weights.entrySet().stream()
            .filter(e -> e.getKey().isPair())
            .mapToDouble(e -> e.getKey().getPossibleHands() * e.getValue())
            .sum();
        return pairCombos / getTotalCombos() * 100.0;
    }

    /**
     * 计算范围中同花的百分比
     */
    public double getSuitedPercentage() {
        double suitedCombos = weights.entrySet().stream()
            .filter(e -> e.getKey().isSuited())
            .mapToDouble(e -> e.getKey().getPossibleHands() * e.getValue())
            .sum();
        return suitedCombos / getTotalCombos() * 100.0;
    }

    // ========== 字符串表示 ==========

    @Override
    public String toString() {
        if (weights.isEmpty()) {
            return "Empty Range";
        }

        List<String> parts = new ArrayList<>();
        List<HandCombo> sortedCombos = new ArrayList<>(weights.keySet());
        sortedCombos.sort(HandCombo::compareTo);

        for (HandCombo combo : sortedCombos) {
            double weight = weights.get(combo);
            if (weight >= 0.999) {
                parts.add(combo.toString());
            } else {
                parts.add(String.format("%s:%.2f", combo, weight));
            }
        }

        return String.join(", ", parts);
    }

    /**
     * 简洁字符串表示（组合范围压缩）
     */
    public String toCompactString() {
        // TODO: 实现范围压缩 (如 "AA-TT" 而不是 "AA, KK, QQ, JJ, TT")
        return toString();
    }

    /**
     * 获取统计摘要
     */
    public String getSummary() {
        return String.format("Range: %d combos (%.1f total), Pairs: %.1f%%, Suited: %.1f%%",
            size(),
            getTotalCombos(),
            getPairPercentage(),
            getSuitedPercentage()
        );
    }

    // ========== 辅助方法 ==========

    /**
     * 限制权重在[0.0, 1.0]范围内
     */
    private static double clampWeight(double weight) {
        return Math.max(0.0, Math.min(1.0, weight));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return Objects.equals(weights, range.weights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weights);
    }
}
