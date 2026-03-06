package com.poker.gto.solver.display;

import com.poker.gto.core.actions.Action;
import com.poker.gto.solver.cfr.Strategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 策略格式化器
 *
 * 将策略转换为人类可读的格式
 */
public class StrategyFormatter {

    /**
     * 格式化完整策略
     *
     * @param strategy 策略
     * @return 格式化后的字符串
     */
    public static String format(Strategy strategy) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GTO策略 ===\n");
        sb.append("信息集数量: ").append(strategy.size()).append("\n\n");

        List<String> infoSets = new ArrayList<>();
        for (String infoSet : strategy.getInfoSets()) {
            infoSets.add(infoSet);
        }
        Collections.sort(infoSets);

        for (String infoSet : infoSets) {
            sb.append(formatInfoSet(infoSet, strategy));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化单个信息集的策略
     *
     * @param infoSet 信息集Key
     * @param strategy 策略
     * @return 格式化后的字符串
     */
    public static String formatInfoSet(String infoSet, Strategy strategy) {
        Map<Action, Double> actionProbs = strategy.getStrategy(infoSet);
        if (actionProbs == null || actionProbs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 解析信息集
        InfoSetInfo info = parseInfoSet(infoSet);

        sb.append("【").append(info.description).append("】\n");
        sb.append("  信息集: ").append(infoSet).append("\n");

        // 按概率降序排序动作
        List<Map.Entry<Action, Double>> sortedActions = actionProbs.entrySet()
            .stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .collect(Collectors.toList());

        sb.append("  策略:\n");
        for (Map.Entry<Action, Double> entry : sortedActions) {
            Action action = entry.getKey();
            double prob = entry.getValue();

            if (prob > 0.001) {  // 只显示概率>0.1%的动作
                sb.append(String.format("    %s: %.1f%%\n",
                    formatAction(action),
                    prob * 100));
            }
        }

        return sb.toString();
    }

    /**
     * 格式化动作
     */
    private static String formatAction(Action action) {
        switch (action.getType()) {
            case PASS:
                return "过牌 (Check)";
            case BET:
                return String.format("下注 %d (Bet %d)", action.getAmount(), action.getAmount());
            case CALL:
                return "跟注 (Call)";
            case FOLD:
                return "弃牌 (Fold)";
            default:
                return action.toString();
        }
    }

    /**
     * 解析信息集描述
     */
    private static InfoSetInfo parseInfoSet(String infoSet) {
        InfoSetInfo info = new InfoSetInfo();

        // 解析格式: P{player}_RIVER_{board}_{hand}_{history}
        String[] parts = infoSet.split("_");

        if (parts.length >= 2) {
            info.player = parts[0];
            info.street = parts[1];
        }

        if (parts.length >= 3) {
            info.board = parts[2];
        }

        if (parts.length >= 4) {
            info.hand = parts[3];
        }

        if (parts.length >= 5) {
            info.history = parts[4];
        }

        // 生成描述
        info.description = generateDescription(info);

        return info;
    }

    /**
     * 生成信息集描述
     */
    private static String generateDescription(InfoSetInfo info) {
        StringBuilder desc = new StringBuilder();

        desc.append(info.player).append(" ");

        if (info.history.isEmpty()) {
            desc.append("首先行动");
        } else {
            desc.append("行动历史: ");
            for (char c : info.history.toCharArray()) {
                switch (c) {
                    case 'k':
                        desc.append("过牌→");
                        break;
                    case 'b':
                        desc.append("下注→");
                        break;
                    case 'c':
                        desc.append("跟注→");
                        break;
                    case 'f':
                        desc.append("弃牌→");
                        break;
                }
            }
            // 移除最后的箭头
            if (desc.length() > 0 && desc.charAt(desc.length() - 1) == '→') {
                desc.setLength(desc.length() - 1);
            }
        }

        return desc.toString();
    }

    /**
     * 格式化策略摘要（只显示主要动作）
     */
    public static String formatSummary(Strategy strategy) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 策略摘要 ===\n");
        sb.append("信息集数量: ").append(strategy.size()).append("\n\n");

        List<String> infoSets = new ArrayList<>();
        for (String infoSet : strategy.getInfoSets()) {
            infoSets.add(infoSet);
        }
        Collections.sort(infoSets);

        for (String infoSet : infoSets) {
            Map<Action, Double> actionProbs = strategy.getStrategy(infoSet);
            if (actionProbs == null) continue;

            // 找到最高概率的动作
            Action bestAction = null;
            double bestProb = 0.0;
            for (Map.Entry<Action, Double> entry : actionProbs.entrySet()) {
                if (entry.getValue() > bestProb) {
                    bestProb = entry.getValue();
                    bestAction = entry.getKey();
                }
            }

            if (bestAction != null) {
                InfoSetInfo info = parseInfoSet(infoSet);
                sb.append(String.format("  %s → %s (%.1f%%)\n",
                    info.description,
                    formatAction(bestAction),
                    bestProb * 100));
            }
        }

        return sb.toString();
    }

    /**
     * 信息集信息
     */
    private static class InfoSetInfo {
        String player = "";
        String street = "";
        String board = "";
        String hand = "";
        String history = "";
        String description = "";
    }
}
