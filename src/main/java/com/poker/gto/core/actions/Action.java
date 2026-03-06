package com.poker.gto.core.actions;

import java.util.Objects;

/**
 * 表示玩家的一个动作
 *
 * 这个类是不可变的 (immutable)
 */
public class Action {

    /**
     * 动作类型
     */
    public enum Type {
        PASS,   // 过牌 (Kuhn Poker 中的 check)
        BET,    // 下注
        FOLD,   // 弃牌
        CALL    // 跟注
    }

    private final Type type;
    private final int amount;  // 下注金额（仅对BET有效）

    /**
     * 创建一个动作（无金额）
     *
     * @param type 动作类型
     */
    public Action(Type type) {
        this(type, 0);
    }

    /**
     * 创建一个动作（带金额）
     *
     * @param type 动作类型
     * @param amount 下注金额
     */
    public Action(Type type, int amount) {
        this.type = Objects.requireNonNull(type, "Action type cannot be null");
        this.amount = amount;
    }

    /**
     * 获取动作类型
     *
     * @return 动作类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 获取下注金额
     *
     * @return 下注金额
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 是否是弃牌
     */
    public boolean isFold() {
        return type == Type.FOLD;
    }

    /**
     * 是否是激进动作 (bet)
     */
    public boolean isAggressive() {
        return type == Type.BET;
    }

    @Override
    public String toString() {
        if (type == Type.BET && amount > 0) {
            return type.name() + "(" + amount + ")";
        }
        return type.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return type == action.type && amount == action.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount);
    }

    // 常量：预定义的动作
    public static final Action PASS = new Action(Type.PASS);
    public static final Action BET = new Action(Type.BET);
    public static final Action FOLD = new Action(Type.FOLD);
    public static final Action CALL = new Action(Type.CALL);
}
