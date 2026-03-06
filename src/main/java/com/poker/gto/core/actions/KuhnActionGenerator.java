package com.poker.gto.core.actions;

import com.poker.gto.core.game_state.KuhnPokerState;

import java.util.ArrayList;
import java.util.List;

/**
 * Kuhn Poker 动作生成器
 *
 * 根据游戏状态生成所有合法动作
 */
public class KuhnActionGenerator {

    /**
     * 获取当前状态下的所有合法动作
     *
     * @param state 游戏状态
     * @return 合法动作列表
     */
    public List<Action> getLegalActions(KuhnPokerState state) {
        if (state.isTerminal()) {
            return new ArrayList<>();  // 终局无合法动作
        }

        List<Action> actions = new ArrayList<>();
        List<Action> history = state.getHistory();

        if (history.isEmpty()) {
            // P0 第一次行动: 可以 PASS 或 BET
            actions.add(Action.PASS);
            actions.add(Action.BET);

        } else if (history.size() == 1) {
            Action firstAction = history.get(0);

            if (firstAction.getType() == Action.Type.PASS) {
                // P0 checked, P1 可以 PASS 或 BET
                actions.add(Action.PASS);
                actions.add(Action.BET);

            } else if (firstAction.getType() == Action.Type.BET) {
                // P0 bet, P1 可以 FOLD 或 CALL
                actions.add(Action.FOLD);
                actions.add(Action.CALL);

            } else {
                throw new IllegalStateException("Invalid first action: " + firstAction);
            }

        } else if (history.size() == 2) {
            Action firstAction = history.get(0);
            Action secondAction = history.get(1);

            if (firstAction.getType() == Action.Type.PASS &&
                secondAction.getType() == Action.Type.BET) {
                // P0 checked, P1 bet, P0 可以 FOLD 或 CALL
                actions.add(Action.FOLD);
                actions.add(Action.CALL);

            } else {
                // 其他情况应该已经是终局了
                throw new IllegalStateException("Game should be terminal after 2 actions: " + history);
            }

        } else {
            // Kuhn Poker 最多 3 个动作
            throw new IllegalStateException("Invalid history length: " + history.size());
        }

        return actions;
    }

    /**
     * 检查动作是否合法
     *
     * @param state  游戏状态
     * @param action 要检查的动作
     * @return 是否合法
     */
    public boolean isLegalAction(KuhnPokerState state, Action action) {
        List<Action> legalActions = getLegalActions(state);
        return legalActions.contains(action);
    }

    /**
     * 获取可能的动作数量
     *
     * @param state 游戏状态
     * @return 合法动作数量
     */
    public int getNumLegalActions(KuhnPokerState state) {
        return getLegalActions(state).size();
    }
}
