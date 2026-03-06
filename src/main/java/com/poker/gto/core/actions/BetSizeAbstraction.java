package com.poker.gto.core.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 下注大小抽象
 *
 * 定义在特定场景下可用的下注大小集合
 */
public class BetSizeAbstraction {

    private final String name;
    private final List<BetSize> betSizes;

    /**
     * 创建下注大小抽象
     *
     * @param name 名称
     * @param betSizes 可用的下注大小列表
     */
    public BetSizeAbstraction(String name, List<BetSize> betSizes) {
        this.name = name;
        this.betSizes = new ArrayList<>(betSizes);
    }

    /**
     * 创建下注大小抽象（可变参数）
     */
    public BetSizeAbstraction(String name, BetSize... betSizes) {
        this(name, Arrays.asList(betSizes));
    }

    public String getName() {
        return name;
    }

    public List<BetSize> getBetSizes() {
        return new ArrayList<>(betSizes);
    }

    /**
     * 获取下注大小数量
     */
    public int size() {
        return betSizes.size();
    }

    @Override
    public String toString() {
        return String.format("%s (%d sizes)", name, betSizes.size());
    }

    // ========== 预定义的抽象 ==========

    /**
     * 简单抽象：只有半池和满池
     */
    public static final BetSizeAbstraction SIMPLE = new BetSizeAbstraction(
        "简单抽象",
        BetSize.HALF_POT,
        BetSize.FULL_POT
    );

    /**
     * 标准抽象：1/3, 2/3, 满池
     */
    public static final BetSizeAbstraction STANDARD = new BetSizeAbstraction(
        "标准抽象",
        BetSize.THIRD_POT,
        BetSize.TWO_THIRDS_POT,
        BetSize.FULL_POT
    );

    /**
     * 精细抽象：1/3, 半池, 2/3, 满池, 1.5倍超池
     */
    public static final BetSizeAbstraction FINE = new BetSizeAbstraction(
        "精细抽象",
        BetSize.THIRD_POT,
        BetSize.HALF_POT,
        BetSize.TWO_THIRDS_POT,
        BetSize.FULL_POT,
        BetSize.OVERBET_150
    );

    /**
     * River专用：半池, 3/4底池, 满池, 1.5倍超池
     */
    public static final BetSizeAbstraction RIVER = new BetSizeAbstraction(
        "River抽象",
        BetSize.HALF_POT,
        BetSize.THREE_QUARTERS_POT,
        BetSize.FULL_POT,
        BetSize.OVERBET_150
    );
}
