package com.poker.gto.solver.cfr;

import com.poker.gto.core.actions.Action;

import java.util.*;

/**
 * 稀疏策略存储
 *
 * 性能优化版本的策略类:
 * 1. 只存储概率 > 阈值的动作 (节省内存)
 * 2. 使用float代替double存储概率 (减少50%内存)
 * 3. 使用ArrayList代替HashMap (减少对象开销)
 *
 * 适用场景:
 * - 大规模求解器
 * - 内存受限环境
 * - 策略导出和序列化
 */
public class SparseStrategy {

    // 概率阈值: 低于此值的动作不存储 (默认1%)
    private static final float DEFAULT_THRESHOLD = 0.01f;

    private final float threshold;

    // 稀疏存储: InfoSet -> 动作概率列表
    private final Map<String, List<ActionProb>> strategies;

    /**
     * 动作-概率对 (使用float节省内存)
     */
    public static class ActionProb {
        public final Action action;
        public final float probability;

        public ActionProb(Action action, float probability) {
            this.action = action;
            this.probability = probability;
        }

        @Override
        public String toString() {
            return String.format("%s: %.3f", action, probability);
        }
    }

    /**
     * 创建稀疏策略 (使用默认阈值1%)
     */
    public SparseStrategy() {
        this(DEFAULT_THRESHOLD);
    }

    /**
     * 创建稀疏策略
     *
     * @param threshold 概率阈值 (低于此值的动作不存储)
     */
    public SparseStrategy(float threshold) {
        this.threshold = threshold;
        this.strategies = new HashMap<>();
    }

    /**
     * 从普通策略转换
     *
     * @param strategy 原始策略
     */
    public static SparseStrategy fromStrategy(Strategy strategy) {
        return fromStrategy(strategy, DEFAULT_THRESHOLD);
    }

    /**
     * 从普通策略转换
     *
     * @param strategy 原始策略
     * @param threshold 概率阈值
     */
    public static SparseStrategy fromStrategy(Strategy strategy, float threshold) {
        SparseStrategy sparse = new SparseStrategy(threshold);

        for (String infoSet : strategy.getInfoSets()) {
            Map<Action, Double> actionProbs = strategy.getStrategy(infoSet);

            List<ActionProb> sparseList = new ArrayList<>();
            for (Map.Entry<Action, Double> entry : actionProbs.entrySet()) {
                float prob = entry.getValue().floatValue();
                if (prob >= threshold) {
                    sparseList.add(new ActionProb(entry.getKey(), prob));
                }
            }

            // 归一化 (因为过滤掉了小概率动作)
            sparse.setStrategy(infoSet, sparseList);
        }

        return sparse;
    }

    /**
     * 设置信息集的策略
     *
     * @param infoSet 信息集
     * @param actionProbs 动作概率列表
     */
    public void setStrategy(String infoSet, List<ActionProb> actionProbs) {
        // 归一化
        float sum = 0.0f;
        for (ActionProb ap : actionProbs) {
            sum += ap.probability;
        }

        if (sum > 0) {
            List<ActionProb> normalized = new ArrayList<>(actionProbs.size());
            for (ActionProb ap : actionProbs) {
                normalized.add(new ActionProb(ap.action, ap.probability / sum));
            }
            strategies.put(infoSet, normalized);
        } else {
            strategies.put(infoSet, new ArrayList<>(actionProbs));
        }
    }

    /**
     * 获取信息集的策略
     *
     * @param infoSet 信息集
     * @return 动作概率列表 (null如果不存在)
     */
    public List<ActionProb> getStrategy(String infoSet) {
        return strategies.get(infoSet);
    }

    /**
     * 获取某个动作的概率
     *
     * @param infoSet 信息集
     * @param action 动作
     * @return 概率 (如果不存在返回0)
     */
    public float getProbability(String infoSet, Action action) {
        List<ActionProb> actionProbs = strategies.get(infoSet);
        if (actionProbs == null) {
            return 0.0f;
        }

        for (ActionProb ap : actionProbs) {
            if (ap.action.equals(action)) {
                return ap.probability;
            }
        }

        return 0.0f;
    }

    /**
     * 转换为普通策略
     */
    public Strategy toStrategy() {
        Strategy strategy = new Strategy();

        for (Map.Entry<String, List<ActionProb>> entry : strategies.entrySet()) {
            String infoSet = entry.getKey();
            List<ActionProb> actionProbs = entry.getValue();

            Map<Action, Double> actionMap = new HashMap<>();
            for (ActionProb ap : actionProbs) {
                actionMap.put(ap.action, (double) ap.probability);
            }

            strategy.setStrategy(infoSet, actionMap);
        }

        return strategy;
    }

    /**
     * 获取所有信息集
     */
    public Set<String> getInfoSets() {
        return strategies.keySet();
    }

    /**
     * 获取信息集数量
     */
    public int size() {
        return strategies.size();
    }

    /**
     * 获取阈值
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * 计算内存占用估计 (字节)
     */
    public long estimateMemoryUsage() {
        long bytes = 0;

        // 基础对象开销
        bytes += 16; // 对象头
        bytes += 4;  // threshold (float)
        bytes += 8;  // strategies引用

        // HashMap开销
        bytes += 32; // HashMap对象
        bytes += strategies.size() * 64; // 每个Entry约64字节

        // ActionProb列表
        for (List<ActionProb> list : strategies.values()) {
            bytes += 24; // ArrayList对象
            bytes += list.size() * (16 + 8 + 4); // 每个ActionProb: 对象头 + Action引用 + float
        }

        return bytes;
    }

    /**
     * 获取统计信息
     */
    public Stats getStats() {
        int totalActions = 0;
        int maxActions = 0;
        int minActions = Integer.MAX_VALUE;

        for (List<ActionProb> list : strategies.values()) {
            int size = list.size();
            totalActions += size;
            maxActions = Math.max(maxActions, size);
            minActions = Math.min(minActions, size);
        }

        double avgActions = totalActions * 1.0 / strategies.size();

        return new Stats(
            strategies.size(),
            totalActions,
            avgActions,
            minActions,
            maxActions,
            estimateMemoryUsage()
        );
    }

    /**
     * 统计信息
     */
    public static class Stats {
        public final int infoSetCount;
        public final int totalActions;
        public final double avgActionsPerInfoSet;
        public final int minActionsPerInfoSet;
        public final int maxActionsPerInfoSet;
        public final long estimatedMemoryBytes;

        public Stats(int infoSetCount, int totalActions, double avgActions,
                     int minActions, int maxActions, long memoryBytes) {
            this.infoSetCount = infoSetCount;
            this.totalActions = totalActions;
            this.avgActionsPerInfoSet = avgActions;
            this.minActionsPerInfoSet = minActions;
            this.maxActionsPerInfoSet = maxActions;
            this.estimatedMemoryBytes = memoryBytes;
        }

        @Override
        public String toString() {
            return String.format(
                "SparseStrategy Stats:\n" +
                "  信息集数: %d\n" +
                "  总动作数: %d\n" +
                "  平均动作/信息集: %.2f\n" +
                "  动作数范围: [%d, %d]\n" +
                "  估计内存: %.2f MB",
                infoSetCount,
                totalActions,
                avgActionsPerInfoSet,
                minActionsPerInfoSet,
                maxActionsPerInfoSet,
                estimatedMemoryBytes / 1024.0 / 1024.0
            );
        }
    }

    @Override
    public String toString() {
        return String.format("SparseStrategy{infoSets=%d, threshold=%.3f}",
            strategies.size(), threshold);
    }
}
