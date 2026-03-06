package com.poker.gto.core.game_state;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * River阶段游戏状态
 *
 * 表示德州扑克River（河牌）阶段的完整状态
 *
 * 特点：
 * - 公共牌已全部发出（5张）
 * - 玩家已看到所有公共牌
 * - 只剩最后一轮下注
 * - 支持2人对抗（Heads-Up）
 *
 * 这个类是不可变的
 */
public class RiverState {

    private static final int NUM_PLAYERS = 2;
    private static final int BOARD_SIZE = 5;

    // 公共牌（5张）
    private final List<TexasCard> board;

    // 玩家状态
    private final List<RiverPlayerState> players;

    // 底池
    private final int pot;

    // 当前行动玩家
    private final int currentPlayer;

    // 行动历史
    private final List<Action> history;

    // 是否终局
    private final boolean terminal;

    // InfoSet Key缓存 (性能优化)
    private String infoSetKeyP0 = null;
    private String infoSetKeyP1 = null;

    /**
     * 创建初始River状态
     *
     * @param board 公共牌（必须是5张）
     * @param player0 玩家0状态
     * @param player1 玩家1状态
     * @param pot 初始底池
     */
    public RiverState(
        List<TexasCard> board,
        RiverPlayerState player0,
        RiverPlayerState player1,
        int pot
    ) {
        if (board.size() != BOARD_SIZE) {
            throw new IllegalArgumentException("Board must have exactly 5 cards");
        }

        this.board = new ArrayList<>(board);
        this.players = List.of(player0, player1);
        this.pot = pot;
        this.currentPlayer = 0;  // Player 0先行动
        this.history = new ArrayList<>();
        this.terminal = false;
    }

    /**
     * 私有构造函数（用于创建新状态）
     */
    private RiverState(
        List<TexasCard> board,
        List<RiverPlayerState> players,
        int pot,
        int currentPlayer,
        List<Action> history,
        boolean terminal
    ) {
        this.board = board;
        this.players = players;
        this.pot = pot;
        this.currentPlayer = currentPlayer;
        this.history = history;
        this.terminal = terminal;
    }

    /**
     * 应用一个动作，返回新状态
     *
     * @param action 动作
     * @return 新的游戏状态
     */
    public RiverState applyAction(Action action) {
        if (terminal) {
            throw new IllegalStateException("Cannot apply action to terminal state");
        }

        // 复制历史
        List<Action> newHistory = new ArrayList<>(history);
        newHistory.add(action);

        // 深拷贝玩家状态
        RiverPlayerState newPlayer0 = copyPlayerState(players.get(0));
        RiverPlayerState newPlayer1 = copyPlayerState(players.get(1));
        List<RiverPlayerState> newPlayers = List.of(newPlayer0, newPlayer1);

        int newPot = pot;
        int newCurrentPlayer = 1 - currentPlayer;
        boolean newTerminal = false;

        Action.Type actionType = action.getType();
        RiverPlayerState actingPlayer = newPlayers.get(currentPlayer);

        switch (actionType) {
            case FOLD:
                // 弃牌，对手获胜
                actingPlayer.fold();
                newTerminal = true;
                break;

            case PASS:
                // Check/Pass
                if (history.isEmpty()) {
                    // 第一个玩家check，轮到对手
                } else if (history.size() == 1 && history.get(0).getType() == Action.Type.PASS) {
                    // 双方都check，到showdown
                    newTerminal = true;
                }
                break;

            case BET:
                // 下注或加注
                int betAmount = action.getAmount();
                if (betAmount > actingPlayer.getStack()) {
                    throw new IllegalArgumentException("Bet amount exceeds player stack");
                }
                actingPlayer.invest(betAmount);
                newPot += betAmount;
                // 继续游戏，等待对手反应
                break;

            case CALL:
                // 跟注
                // 找到需要跟注的金额（对手最后一次下注的金额）
                int callAmount = getLastBetAmount();
                if (callAmount > actingPlayer.getStack()) {
                    callAmount = actingPlayer.getStack();  // All-in call
                }
                actingPlayer.invest(callAmount);
                newPot += callAmount;
                newTerminal = true;  // 跟注后进入摊牌
                break;

            default:
                throw new IllegalArgumentException("Invalid action type: " + actionType);
        }

        return new RiverState(board, newPlayers, newPot, newCurrentPlayer, newHistory, newTerminal);
    }

    /**
     * 深拷贝玩家状态
     */
    private RiverPlayerState copyPlayerState(RiverPlayerState original) {
        return original.copy();
    }

    /**
     * 获取最后一次下注的金额
     */
    private int getLastBetAmount() {
        for (int i = history.size() - 1; i >= 0; i--) {
            Action action = history.get(i);
            if (action.getType() == Action.Type.BET) {
                return action.getAmount();
            }
        }
        return 0;
    }

    /**
     * 获取公共牌
     */
    public List<TexasCard> getBoard() {
        return Collections.unmodifiableList(board);
    }

    /**
     * 获取公共牌（作为Hand）
     */
    public Hand getBoardAsHand() {
        return new Hand(board);
    }

    /**
     * 获取玩家状态
     */
    public RiverPlayerState getPlayer(int index) {
        return players.get(index);
    }

    /**
     * 获取所有玩家
     */
    public List<RiverPlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * 获取底池
     */
    public int getPot() {
        return pot;
    }

    /**
     * 获取当前行动玩家
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * 获取行动历史
     */
    public List<Action> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * 是否终局
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * 获取信息集Key（用于CFR）
     *
     * 格式: "P{player}_RIVER_{board}_{hand/range}_{history}"
     *
     * 性能优化: 使用缓存避免重复计算
     */
    public String getInfoSetKey(int player) {
        // 检查缓存
        if (player == 0 && infoSetKeyP0 != null) {
            return infoSetKeyP0;
        }
        if (player == 1 && infoSetKeyP1 != null) {
            return infoSetKeyP1;
        }

        // 计算InfoSet Key
        String key = buildInfoSetKey(player);

        // 缓存结果
        if (player == 0) {
            infoSetKeyP0 = key;
        } else {
            infoSetKeyP1 = key;
        }

        return key;
    }

    /**
     * 构建InfoSet Key
     */
    private String buildInfoSetKey(int player) {
        StringBuilder key = new StringBuilder();
        key.append("P").append(player);
        key.append("_RIVER_");

        // 编码公共牌
        for (TexasCard card : board) {
            key.append(card);
        }
        key.append("_");

        // 编码手牌或范围（简化版）
        RiverPlayerState playerState = players.get(player);
        if (playerState.hasHand()) {
            key.append(playerState.getHand());
        } else {
            key.append("RANGE");
        }
        key.append("_");

        // 编码历史
        for (Action action : history) {
            switch (action.getType()) {
                case PASS:
                    key.append('k');
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

        return key.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RiverState{\n");
        sb.append("  Board: ").append(board).append("\n");
        sb.append("  Pot: ").append(pot).append("\n");
        sb.append("  Player0: ").append(players.get(0)).append("\n");
        sb.append("  Player1: ").append(players.get(1)).append("\n");
        sb.append("  Current: P").append(currentPlayer).append("\n");
        sb.append("  History: ").append(history).append("\n");
        sb.append("  Terminal: ").append(terminal).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
