package com.poker.gto.solver.cfr;

import com.poker.gto.core.actions.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * 策略表
 *
 * 存储每个信息集上的策略(动作概率分布)
 */
public class Strategy {

    // 信息集 -> (动作 -> 概率)
    private final Map<String, Map<Action, Double>> strategies;

    public Strategy() {
        this.strategies = new HashMap<>();
    }

    /**
     * 获取在某个信息集上选择某个动作的概率
     *
     * @param infoSet 信息集Key
     * @param action  动作
     * @return 概率(0.0-1.0)
     */
    public double getProbability(String infoSet, Action action) {
        Map<Action, Double> actionProbs = strategies.get(infoSet);
        if (actionProbs == null) {
            return 0.0;
        }
        return actionProbs.getOrDefault(action, 0.0);
    }

    /**
     * 设置在某个信息集上选择某个动作的概率
     *
     * @param infoSet     信息集Key
     * @param action      动作
     * @param probability 概率
     */
    public void setProbability(String infoSet, Action action, double probability) {
        strategies.computeIfAbsent(infoSet, k -> new HashMap<>())
                  .put(action, probability);
    }

    /**
     * 获取某个信息集的完整策略
     *
     * @param infoSet 信息集Key
     * @return 动作概率分布(可能为null)
     */
    public Map<Action, Double> getStrategy(String infoSet) {
        return strategies.get(infoSet);
    }

    /**
     * 设置某个信息集的完整策略
     *
     * @param infoSet 信息集Key
     * @param strategy 动作概率分布
     */
    public void setStrategy(String infoSet, Map<Action, Double> strategy) {
        strategies.put(infoSet, new HashMap<>(strategy));
    }

    /**
     * 获取所有信息集
     *
     * @return 信息集集合
     */
    public Iterable<String> getInfoSets() {
        return strategies.keySet();
    }

    /**
     * 获取信息集数量
     *
     * @return 信息集数量
     */
    public int size() {
        return strategies.size();
    }

    /**
     * 清空策略
     */
    public void clear() {
        strategies.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy{\n");

        for (Map.Entry<String, Map<Action, Double>> entry : strategies.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ");

            Map<Action, Double> actionProbs = entry.getValue();
            for (Map.Entry<Action, Double> actionEntry : actionProbs.entrySet()) {
                sb.append(actionEntry.getKey().getType().name())
                  .append("=")
                  .append(String.format("%.3f", actionEntry.getValue()))
                  .append(" ");
            }

            sb.append("\n");
        }

        sb.append("}");
        return sb.toString();
    }
}
