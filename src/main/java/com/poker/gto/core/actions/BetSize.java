package com.poker.gto.core.actions;

/**
 * 下注大小类型
 *
 * 表示相对于底池的下注大小
 */
public class BetSize {

    private final String name;          // 名称，如 "小注", "半池", "超池"
    private final double potFraction;   // 相对于底池的比例，如 0.5 表示半池

    /**
     * 创建下注大小
     *
     * @param name 名称
     * @param potFraction 底池比例（0.0 到 无穷大）
     */
    public BetSize(String name, double potFraction) {
        if (potFraction < 0) {
            throw new IllegalArgumentException("Pot fraction must be non-negative");
        }
        this.name = name;
        this.potFraction = potFraction;
    }

    /**
     * 计算具体的下注金额
     *
     * @param pot 当前底池大小
     * @return 下注金额
     */
    public int calculateAmount(int pot) {
        return (int) Math.round(pot * potFraction);
    }

    /**
     * 计算具体的下注金额（限制在最大值内）
     *
     * @param pot 当前底池大小
     * @param maxAmount 最大金额（如all-in）
     * @return 下注金额
     */
    public int calculateAmount(int pot, int maxAmount) {
        int amount = calculateAmount(pot);
        return Math.min(amount, maxAmount);
    }

    public String getName() {
        return name;
    }

    public double getPotFraction() {
        return potFraction;
    }

    @Override
    public String toString() {
        return String.format("%s (%.0f%% pot)", name, potFraction * 100);
    }

    // ========== 常用下注大小 ==========

    /** 1/3底池 */
    public static final BetSize THIRD_POT = new BetSize("1/3底池", 0.33);

    /** 半池 */
    public static final BetSize HALF_POT = new BetSize("半池", 0.5);

    /** 2/3底池 */
    public static final BetSize TWO_THIRDS_POT = new BetSize("2/3底池", 0.67);

    /** 3/4底池 */
    public static final BetSize THREE_QUARTERS_POT = new BetSize("3/4底池", 0.75);

    /** 满池 */
    public static final BetSize FULL_POT = new BetSize("满池", 1.0);

    /** 1.5倍底池 */
    public static final BetSize OVERBET_150 = new BetSize("1.5倍底池", 1.5);

    /** 2倍底池 */
    public static final BetSize OVERBET_200 = new BetSize("2倍底池", 2.0);
}
