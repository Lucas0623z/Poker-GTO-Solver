package com.poker.gto.solver.cfr;

import com.poker.gto.core.actions.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Regret(遗憾)表
 *
 * 存储每个信息集上每个动作的累积遗憾
 */
public class RegretTable {

    // 信息集 -> (动作 -> 累积遗憾)
    private final Map<String, Map<Action, Double>> regrets;

    public RegretTable() {
        this.regrets = new HashMap<>();
    }

    /**
     * 获取某个动作的累积遗憾
     *
     * @param infoSet 信息集Key
     * @param action  动作
     * @return 累积遗憾
     */
    public double getRegret(String infoSet, Action action) {
        Map<Action, Double> actionRegrets = regrets.get(infoSet);
        if (actionRegrets == null) {
            return 0.0;
        }
        return actionRegrets.getOrDefault(action, 0.0);
    }

    /**
     * 更新某个动作的累积遗憾
     *
     * @param infoSet 信息集Key
     * @param action  动作
     * @param regret  本次遗憾增量
     */
    public void updateRegret(String infoSet, Action action, double regret) {
        regrets.computeIfAbsent(infoSet, k -> new HashMap<>())
               .merge(action, regret, Double::sum);
    }

    /**
     * 设置某个动作的累积遗憾(覆盖)
     *
     * @param infoSet 信息集Key
     * @param action  动作
     * @param regret  累积遗憾值
     */
    public void setRegret(String infoSet, Action action, double regret) {
        regrets.computeIfAbsent(infoSet, k -> new HashMap<>())
               .put(action, regret);
    }

    /**
     * 获取某个信息集的所有动作遗憾
     *
     * @param infoSet 信息集Key
     * @return 动作遗憾映射(可能为null)
     */
    public Map<Action, Double> getActionRegrets(String infoSet) {
        return regrets.get(infoSet);
    }

    /**
     * 获取平均遗憾(调试用)
     *
     * @return 平均遗憾值
     */
    public double getAverageRegret() {
        double total = 0.0;
        int count = 0;

        for (Map<Action, Double> actionRegrets : regrets.values()) {
            for (double regret : actionRegrets.values()) {
                total += Math.abs(regret);
                count++;
            }
        }

        return count > 0 ? total / count : 0.0;
    }

    /**
     * 清空所有遗憾
     */
    public void clear() {
        regrets.clear();
    }

    /**
     * 获取信息集数量
     *
     * @return 信息集数量
     */
    public int size() {
        return regrets.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegretTable{\n");

        for (Map.Entry<String, Map<Action, Double>> entry : regrets.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ");

            Map<Action, Double> actionRegrets = entry.getValue();
            for (Map.Entry<Action, Double> actionEntry : actionRegrets.entrySet()) {
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
