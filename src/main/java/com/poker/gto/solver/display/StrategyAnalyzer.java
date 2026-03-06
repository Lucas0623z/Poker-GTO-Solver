package com.poker.gto.solver.display;

import com.poker.gto.core.actions.Action;
import com.poker.gto.solver.cfr.Strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 策略分析器
 *
 * 分析策略的统计特性
 */
public class StrategyAnalyzer {

    /**
     * 分析策略
     *
     * @param strategy 策略
     * @return 分析结果
     */
    public static AnalysisResult analyze(Strategy strategy) {
        AnalysisResult result = new AnalysisResult();

        result.totalInfoSets = strategy.size();

        for (String infoSet : strategy.getInfoSets()) {
            Map<Action, Double> actionProbs = strategy.getStrategy(infoSet);
            if (actionProbs == null) continue;

            analyzeInfoSet(infoSet, actionProbs, result);
        }

        return result;
    }

    /**
     * 分析单个信息集
     */
    private static void analyzeInfoSet(String infoSet, Map<Action, Double> actionProbs, AnalysisResult result) {
        // 统计各类动作频率
        for (Map.Entry<Action, Double> entry : actionProbs.entrySet()) {
            Action action = entry.getKey();
            double prob = entry.getValue();

            switch (action.getType()) {
                case PASS:
                    result.totalCheckFreq += prob;
                    result.checkCount++;
                    break;
                case BET:
                    result.totalBetFreq += prob;
                    result.betCount++;
                    result.totalBetSize += action.getAmount() * prob;
                    break;
                case CALL:
                    result.totalCallFreq += prob;
                    result.callCount++;
                    break;
                case FOLD:
                    result.totalFoldFreq += prob;
                    result.foldCount++;
                    break;
            }
        }

        // 计算策略熵（混合度）
        double entropy = 0.0;
        for (double prob : actionProbs.values()) {
            if (prob > 0) {
                entropy -= prob * Math.log(prob) / Math.log(2);
            }
        }
        result.totalEntropy += entropy;
        result.entropyCount++;

        // 检测纯策略
        boolean isPure = actionProbs.values().stream()
            .anyMatch(prob -> prob > 0.99);
        if (isPure) {
            result.pureStrategyCount++;
        } else {
            result.mixedStrategyCount++;
        }
    }

    /**
     * 分析结果
     */
    public static class AnalysisResult {
        // 基本统计
        public int totalInfoSets = 0;
        public int pureStrategyCount = 0;
        public int mixedStrategyCount = 0;

        // 动作频率
        public double totalCheckFreq = 0.0;
        public double totalBetFreq = 0.0;
        public double totalCallFreq = 0.0;
        public double totalFoldFreq = 0.0;

        public int checkCount = 0;
        public int betCount = 0;
        public int callCount = 0;
        public int foldCount = 0;

        // 下注统计
        public double totalBetSize = 0.0;

        // 策略混合度
        public double totalEntropy = 0.0;
        public int entropyCount = 0;

        /**
         * 获取平均过牌频率
         */
        public double getAvgCheckFreq() {
            return checkCount > 0 ? totalCheckFreq / checkCount : 0.0;
        }

        /**
         * 获取平均下注频率
         */
        public double getAvgBetFreq() {
            return betCount > 0 ? totalBetFreq / betCount : 0.0;
        }

        /**
         * 获取平均跟注频率
         */
        public double getAvgCallFreq() {
            return callCount > 0 ? totalCallFreq / callCount : 0.0;
        }

        /**
         * 获取平均弃牌频率
         */
        public double getAvgFoldFreq() {
            return foldCount > 0 ? totalFoldFreq / foldCount : 0.0;
        }

        /**
         * 获取平均下注大小
         */
        public double getAvgBetSize() {
            return betCount > 0 ? totalBetSize / betCount : 0.0;
        }

        /**
         * 获取平均策略熵
         */
        public double getAvgEntropy() {
            return entropyCount > 0 ? totalEntropy / entropyCount : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                "=== 策略分析 ===\n" +
                "信息集总数: %d\n" +
                "  纯策略: %d (%.1f%%)\n" +
                "  混合策略: %d (%.1f%%)\n" +
                "\n" +
                "动作频率:\n" +
                "  过牌: %.1f%%\n" +
                "  下注: %.1f%%\n" +
                "  跟注: %.1f%%\n" +
                "  弃牌: %.1f%%\n" +
                "\n" +
                "下注统计:\n" +
                "  平均下注大小: %.1f\n" +
                "\n" +
                "策略混合度:\n" +
                "  平均熵: %.3f (0=纯策略, log2(n)=均匀分布)\n",
                totalInfoSets,
                pureStrategyCount, pureStrategyCount * 100.0 / Math.max(1, totalInfoSets),
                mixedStrategyCount, mixedStrategyCount * 100.0 / Math.max(1, totalInfoSets),
                getAvgCheckFreq() * 100,
                getAvgBetFreq() * 100,
                getAvgCallFreq() * 100,
                getAvgFoldFreq() * 100,
                getAvgBetSize(),
                getAvgEntropy()
            );
        }
    }
}
