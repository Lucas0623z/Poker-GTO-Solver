package com.poker.gto.core;

import com.poker.gto.core.actions.Action;
import com.poker.gto.core.cards.Card;
import com.poker.gto.core.game_state.KuhnPokerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KuhnPokerState 单元测试
 */
class KuhnPokerStateTest {

    @Test
    void testInitialState() {
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);

        assertEquals(0, state.getCurrentPlayer());
        assertEquals(2, state.getPot());
        assertEquals(1, state.getInvested(0));
        assertEquals(1, state.getInvested(1));
        assertFalse(state.isTerminal());
        assertTrue(state.getHistory().isEmpty());
    }

    @Test
    void testPassPass() {
        // P0 check, P1 check -> showdown
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);

        state = state.applyAction(Action.PASS);
        assertFalse(state.isTerminal());
        assertEquals(1, state.getCurrentPlayer());

        state = state.applyAction(Action.PASS);
        assertTrue(state.isTerminal());

        // Q > J, so P1 wins
        assertEquals(-1, state.getPayoff(0));  // P0 loses 1
        assertEquals(1, state.getPayoff(1));   // P1 wins 1
    }

    @Test
    void testBetFold() {
        // P0 bet, P1 fold -> P0 wins
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);

        state = state.applyAction(Action.BET);
        assertFalse(state.isTerminal());
        assertEquals(1, state.getCurrentPlayer());
        assertEquals(3, state.getPot());  // 2 + 1

        state = state.applyAction(Action.FOLD);
        assertTrue(state.isTerminal());

        // P1 folded, P0 wins pot
        assertEquals(1, state.getPayoff(0));   // P0 wins 1
        assertEquals(-1, state.getPayoff(1));  // P1 loses 1
    }

    @Test
    void testBetCall() {
        // P0 bet, P1 call -> showdown
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);

        state = state.applyAction(Action.BET);
        state = state.applyAction(Action.CALL);

        assertTrue(state.isTerminal());
        assertEquals(4, state.getPot());  // 2 + 1 + 1

        // Q > J, so P1 wins
        assertEquals(-2, state.getPayoff(0));  // P0 loses 2
        assertEquals(2, state.getPayoff(1));   // P1 wins 2
    }

    @Test
    void testPassBetFold() {
        // P0 check, P1 bet, P0 fold -> P1 wins
        KuhnPokerState state = new KuhnPokerState(Card.QUEEN, Card.JACK);

        state = state.applyAction(Action.PASS);
        state = state.applyAction(Action.BET);
        state = state.applyAction(Action.FOLD);

        assertTrue(state.isTerminal());

        // P0 folded, P1 wins pot
        assertEquals(-1, state.getPayoff(0));
        assertEquals(1, state.getPayoff(1));
    }

    @Test
    void testPassBetCall() {
        // P0 check, P1 bet, P0 call -> showdown
        KuhnPokerState state = new KuhnPokerState(Card.KING, Card.JACK);

        state = state.applyAction(Action.PASS);
        state = state.applyAction(Action.BET);
        state = state.applyAction(Action.CALL);

        assertTrue(state.isTerminal());
        assertEquals(4, state.getPot());

        // K > J, so P0 wins
        assertEquals(2, state.getPayoff(0));
        assertEquals(-2, state.getPayoff(1));
    }

    @Test
    void testInfoSetKey() {
        KuhnPokerState state = new KuhnPokerState(Card.JACK, Card.QUEEN);

        // P0 初始信息集
        String infoSet0 = state.getInfoSetKey(0);
        assertTrue(infoSet0.startsWith("P0_"));
        assertTrue(infoSet0.contains("J"));

        // P1 看到的信息集不同(不同的牌)
        String infoSet1 = state.getInfoSetKey(1);
        assertTrue(infoSet1.startsWith("P1_"));
        assertTrue(infoSet1.contains("Q"));

        // 应用动作后信息集改变
        state = state.applyAction(Action.BET);
        String newInfoSet1 = state.getInfoSetKey(1);
        assertTrue(newInfoSet1.contains("b"));  // 包含 bet 历史
    }

    @Test
    void testImmutability() {
        KuhnPokerState state1 = new KuhnPokerState(Card.JACK, Card.QUEEN);
        KuhnPokerState state2 = state1.applyAction(Action.PASS);

        // 原状态不应改变
        assertEquals(0, state1.getCurrentPlayer());
        assertTrue(state1.getHistory().isEmpty());

        // 新状态已改变
        assertEquals(1, state2.getCurrentPlayer());
        assertEquals(1, state2.getHistory().size());
    }
}
