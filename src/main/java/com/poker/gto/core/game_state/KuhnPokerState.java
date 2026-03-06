package com.poker.gto.core.game_state;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.cards.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Kuhn Poker 游戏状态
 *
 * 规则:
 * - 2 个玩家
 * - 3 张牌: J, Q, K
 * - 每人发 1 张牌
 * - 每人初始 ante = 1
 * - P0 先行动
 * - 可选动作: PASS (check), BET (1 chip)
 * - 如果前面有 BET, 可以 FOLD 或 CALL
 * - Showdown: 牌大的赢
 *
 * 这个类是不可变的 (immutable)
 */
public class KuhnPokerState {

    // 玩家数量
    public static final int NUM_PLAYERS = 2;

    // Ante (每人开始投入)
    public static final int ANTE = 1;

    // Bet 大小
    public static final int BET_SIZE = 1;

    // 玩家手牌
    private final Card[] playerCards;

    // 底池大小
    private final int pot;

    // 每个玩家已投入的筹码
    private final int[] invested;

    // 当前行动玩家 (0 或 1)
    private final int currentPlayer;

    // 行动历史
    private final List<Action> history;

    // 是否终局
    private final boolean terminal;

    // 终局收益 (如果是终局)
    private final int[] payoffs;

    /**
     * 创建初始状态
     *
     * @param player0Card 玩家0的手牌
     * @param player1Card 玩家1的手牌
     */
    public KuhnPokerState(Card player0Card, Card player1Card) {
        this.playerCards = new Card[]{player0Card, player1Card};
        this.pot = ANTE * NUM_PLAYERS;
        this.invested = new int[]{ANTE, ANTE};
        this.currentPlayer = 0;  // P0 先行动
        this.history = new ArrayList<>();
        this.terminal = false;
        this.payoffs = null;
    }

    /**
     * 私有构造函数,用于创建新状态
     */
    private KuhnPokerState(
        Card[] playerCards,
        int pot,
        int[] invested,
        int currentPlayer,
        List<Action> history,
        boolean terminal,
        int[] payoffs
    ) {
        this.playerCards = playerCards;
        this.pot = pot;
        this.invested = invested;
        this.currentPlayer = currentPlayer;
        this.history = new ArrayList<>(history);
        this.terminal = terminal;
        this.payoffs = payoffs;
    }

    /**
     * 应用一个动作,返回新的状态
     *
     * @param action 玩家动作
     * @return 新的游戏状态
     */
    public KuhnPokerState applyAction(Action action) {
        if (terminal) {
            throw new IllegalStateException("Cannot apply action to terminal state");
        }

        List<Action> newHistory = new ArrayList<>(history);
        newHistory.add(action);

        int[] newInvested = invested.clone();
        int newPot = pot;
        int newCurrentPlayer = 1 - currentPlayer;
        boolean newTerminal = false;
        int[] newPayoffs = null;

        Action.Type actionType = action.getType();

        // 处理不同的动作
        switch (actionType) {
            case FOLD:
                // 弃牌,对手赢得底池
                newTerminal = true;
                newPayoffs = new int[NUM_PLAYERS];
                int opponent = 1 - currentPlayer;
                newPayoffs[opponent] = pot - invested[opponent];
                newPayoffs[currentPlayer] = -invested[currentPlayer];
                break;

            case PASS:
                // Check
                if (history.isEmpty()) {
                    // P0 check, P1 行动
                    // newCurrentPlayer 已经设置
                } else if (history.size() == 1 && history.get(0).getType() == Action.Type.PASS) {
                    // P0 check, P1 check -> Showdown
                    newTerminal = true;
                    newPayoffs = calculateShowdownPayoffs();
                } else {
                    throw new IllegalStateException("Invalid PASS action");
                }
                break;

            case BET:
                // 下注
                newInvested[currentPlayer] += BET_SIZE;
                newPot += BET_SIZE;
                // 对手行动
                break;

            case CALL:
                // 跟注
                int toCall = invested[1 - currentPlayer] - invested[currentPlayer];
                newInvested[currentPlayer] += toCall;
                newPot += toCall;
                // Showdown
                newTerminal = true;
                newPayoffs = calculateShowdownPayoffs();
                break;

            default:
                throw new IllegalArgumentException("Invalid action type: " + actionType);
        }

        return new KuhnPokerState(
            playerCards,
            newPot,
            newInvested,
            newCurrentPlayer,
            newHistory,
            newTerminal,
            newPayoffs
        );
    }

    /**
     * 计算 Showdown 收益
     */
    private int[] calculateShowdownPayoffs() {
        int[] payoffs = new int[NUM_PLAYERS];

        // 比较牌力
        int comparison = playerCards[0].compareTo(playerCards[1]);

        if (comparison > 0) {
            // P0 赢
            payoffs[0] = pot - invested[0];
            payoffs[1] = -invested[1];
        } else if (comparison < 0) {
            // P1 赢
            payoffs[0] = -invested[0];
            payoffs[1] = pot - invested[1];
        } else {
            // 平局(Kuhn Poker 不可能出现,因为没有重复的牌)
            payoffs[0] = 0;
            payoffs[1] = 0;
        }

        return payoffs;
    }

    /**
     * 获取玩家手牌
     */
    public Card getPlayerCard(int player) {
        return playerCards[player];
    }

    /**
     * 获取底池大小
     */
    public int getPot() {
        return pot;
    }

    /**
     * 获取玩家已投入筹码
     */
    public int getInvested(int player) {
        return invested[player];
    }

    /**
     * 获取当前行动玩家
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * 获取行动历史(不可修改)
     */
    public List<Action> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * 是否是终局
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * 获取玩家收益(仅在终局时)
     */
    public int getPayoff(int player) {
        if (!terminal) {
            throw new IllegalStateException("Cannot get payoff from non-terminal state");
        }
        return payoffs[player];
    }

    /**
     * 获取所有收益(仅在终局时)
     */
    public int[] getPayoffs() {
        if (!terminal) {
            throw new IllegalStateException("Cannot get payoffs from non-terminal state");
        }
        return payoffs.clone();
    }

    /**
     * 获取信息集 Key
     * 格式: "P{player}_{history}_{card}"
     * 注意: 不包含对手的牌
     */
    public String getInfoSetKey(int player) {
        StringBuilder key = new StringBuilder();
        key.append('P').append(player);
        key.append('_');

        // 编码历史
        for (Action action : history) {
            switch (action.getType()) {
                case PASS:
                    key.append('p');
                    break;
                case BET:
                    key.append('b');
                    break;
                case CALL:
                    key.append('c');
                    break;
                case FOLD:
                    key.append('f');
                    break;
            }
        }

        key.append('_');

        // 编码自己的牌
        key.append(playerCards[player].toString());

        return key.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KuhnPokerState{");
        sb.append("P0=").append(playerCards[0]);
        sb.append(", P1=").append(playerCards[1]);
        sb.append(", pot=").append(pot);
        sb.append(", invested=[").append(invested[0]).append(",").append(invested[1]).append("]");
        sb.append(", currentPlayer=").append(currentPlayer);
        sb.append(", history=");
        for (Action a : history) {
            sb.append(a.getType().name().charAt(0));
        }
        if (terminal) {
            sb.append(", payoffs=[").append(payoffs[0]).append(",").append(payoffs[1]).append("]");
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KuhnPokerState that = (KuhnPokerState) o;
        return pot == that.pot &&
               currentPlayer == that.currentPlayer &&
               terminal == that.terminal &&
               Objects.deepEquals(playerCards, that.playerCards) &&
               Objects.deepEquals(invested, that.invested) &&
               Objects.equals(history, that.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerCards[0], playerCards[1], pot, currentPlayer, terminal, history);
    }
}
