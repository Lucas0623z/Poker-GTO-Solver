package com.poker.gto.core.actions;

import com.poker.gto.core.game_state.RiverPlayerState;
import com.poker.gto.core.game_state.RiverState;

import java.util.ArrayList;
import java.util.List;

/**
 * River阶段动作生成器
 *
 * 根据游戏状态生成合法的动作列表
 */
public class RiverActionGenerator {

    private final BetSizeAbstraction betSizeAbstraction;

    /**
     * 创建动作生成器
     *
     * @param betSizeAbstraction 下注大小抽象
     */
    public RiverActionGenerator(BetSizeAbstraction betSizeAbstraction) {
        this.betSizeAbstraction = betSizeAbstraction;
    }

    /**
     * 生成合法动作列表
     *
     * @param state 当前游戏状态
     * @return 合法动作列表
     */
    public List<Action> generateActions(RiverState state) {
        if (state.isTerminal()) {
            return new ArrayList<>();  // 终局状态无动作
        }

        List<Action> actions = new ArrayList<>();
        int currentPlayer = state.getCurrentPlayer();
        RiverPlayerState playerState = state.getPlayer(currentPlayer);

        // 获取历史
        List<Action> history = state.getHistory();

        // 判断场景
        if (history.isEmpty()) {
            // 场景1：第一个玩家行动
            // 可以：过牌(PASS) 或 下注(BET)
            actions.add(Action.PASS);
            addBetActions(actions, state);
        } else {
            Action lastAction = history.get(history.size() - 1);

            if (lastAction.getType() == Action.Type.PASS) {
                // 场景2：对手过牌后
                // 可以：过牌(PASS) 或 下注(BET)
                actions.add(Action.PASS);
                addBetActions(actions, state);
            } else if (lastAction.getType() == Action.Type.BET) {
                // 场景3：面对下注
                // 可以：弃牌(FOLD), 跟注(CALL), 或 加注(BET)
                actions.add(Action.FOLD);
                actions.add(Action.CALL);

                // 加注（只有足够筹码才能加注）
                int opponentBet = lastAction.getAmount();
                int currentPot = state.getPot() + opponentBet;  // 包含对手下注的底池
                int playerStack = playerState.getStack();

                // 最小加注：至少要下注2倍对手的下注
                int minRaise = opponentBet * 2;

                if (playerStack > opponentBet && playerStack >= minRaise) {
                    // 可以加注
                    addRaiseActions(actions, state, opponentBet, currentPot);
                }
            }
        }

        return actions;
    }

    /**
     * 添加初始下注动作
     */
    private void addBetActions(List<Action> actions, RiverState state) {
        int currentPlayer = state.getCurrentPlayer();
        RiverPlayerState playerState = state.getPlayer(currentPlayer);
        int pot = state.getPot();
        int stack = playerState.getStack();

        for (BetSize betSize : betSizeAbstraction.getBetSizes()) {
            int amount = betSize.calculateAmount(pot, stack);

            // 确保下注金额有效
            if (amount > 0 && amount <= stack) {
                actions.add(new Action(Action.Type.BET, amount));
            }
        }

        // 总是包含All-in选项（如果还没包含）
        if (stack > 0) {
            boolean hasAllIn = actions.stream()
                .anyMatch(a -> a.getType() == Action.Type.BET && a.getAmount() == stack);

            if (!hasAllIn) {
                actions.add(new Action(Action.Type.BET, stack));
            }
        }
    }

    /**
     * 添加加注动作
     */
    private void addRaiseActions(List<Action> actions, RiverState state, int opponentBet, int currentPot) {
        int currentPlayer = state.getCurrentPlayer();
        RiverPlayerState playerState = state.getPlayer(currentPlayer);
        int stack = playerState.getStack();

        // 计算有效底池（用于计算加注大小）
        int effectivePot = currentPot;

        for (BetSize betSize : betSizeAbstraction.getBetSizes()) {
            int raiseTotal = opponentBet + betSize.calculateAmount(effectivePot);

            // 确保加注金额有效
            if (raiseTotal > opponentBet && raiseTotal <= stack) {
                // 避免重复
                int finalRaiseTotal = raiseTotal;
                boolean alreadyAdded = actions.stream()
                    .anyMatch(a -> a.getType() == Action.Type.BET && a.getAmount() == finalRaiseTotal);

                if (!alreadyAdded) {
                    actions.add(new Action(Action.Type.BET, raiseTotal));
                }
            }
        }

        // 总是包含All-in选项
        if (stack > opponentBet) {
            boolean hasAllIn = actions.stream()
                .anyMatch(a -> a.getType() == Action.Type.BET && a.getAmount() == stack);

            if (!hasAllIn) {
                actions.add(new Action(Action.Type.BET, stack));
            }
        }
    }

    /**
     * 获取动作数量（用于估算）
     */
    public int estimateActionCount() {
        // Pass + Bet sizes + All-in
        return 1 + betSizeAbstraction.size() + 1;
    }

    public BetSizeAbstraction getBetSizeAbstraction() {
        return betSizeAbstraction;
    }
}
