package com.poker.gto.solver.display;

import com.poker.gto.core.actions.Action;
import com.poker.gto.solver.cfr.Strategy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 策略导出器
 *
 * 将策略导出到文件
 */
public class StrategyExporter {

    /**
     * 导出策略到文本文件
     *
     * @param strategy 策略
     * @param filename 文件名
     * @throws IOException IO异常
     */
    public static void exportToText(Strategy strategy, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.println(StrategyFormatter.format(strategy));
        }
    }

    /**
     * 导出策略摘要到文本文件
     *
     * @param strategy 策略
     * @param filename 文件名
     * @throws IOException IO异常
     */
    public static void exportSummaryToText(Strategy strategy, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.println(StrategyFormatter.formatSummary(strategy));
        }
    }

    /**
     * 导出策略到CSV文件
     *
     * @param strategy 策略
     * @param filename 文件名
     * @throws IOException IO异常
     */
    public static void exportToCSV(Strategy strategy, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            // CSV头
            writer.println("InfoSet,Player,History,Action,Probability");

            for (String infoSet : strategy.getInfoSets()) {
                Map<Action, Double> actionProbs = strategy.getStrategy(infoSet);
                if (actionProbs == null) continue;

                for (Map.Entry<Action, Double> entry : actionProbs.entrySet()) {
                    Action action = entry.getKey();
                    double prob = entry.getValue();

                    String actionStr = formatActionForCSV(action);

                    writer.printf("%s,%s,%s,%s,%.6f%n",
                        escapeCsv(infoSet),
                        extractPlayer(infoSet),
                        extractHistory(infoSet),
                        escapeCsv(actionStr),
                        prob);
                }
            }
        }
    }

    /**
     * 格式化动作（用于CSV）
     */
    private static String formatActionForCSV(Action action) {
        switch (action.getType()) {
            case PASS:
                return "CHECK";
            case BET:
                return "BET_" + action.getAmount();
            case CALL:
                return "CALL";
            case FOLD:
                return "FOLD";
            default:
                return action.getType().name();
        }
    }

    /**
     * 从信息集提取玩家
     */
    private static String extractPlayer(String infoSet) {
        if (infoSet.startsWith("P0")) return "P0";
        if (infoSet.startsWith("P1")) return "P1";
        return "UNKNOWN";
    }

    /**
     * 从信息集提取历史
     */
    private static String extractHistory(String infoSet) {
        String[] parts = infoSet.split("_");
        if (parts.length >= 5) {
            return parts[4];
        }
        return "";
    }

    /**
     * CSV转义
     */
    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
