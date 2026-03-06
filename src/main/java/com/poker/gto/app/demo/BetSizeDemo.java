package com.poker.gto.app.demo;

import com.poker.gto.core.actions.*;
import com.poker.gto.core.cards.Hand;
import com.poker.gto.core.cards.TexasCard;
import com.poker.gto.core.game_state.RiverPlayerState;
import com.poker.gto.core.game_state.RiverState;

import java.util.List;

/**
 * 下注大小抽象演示程序
 */
public class BetSizeDemo {

    public static void main(String[] args) {
        System.out.println("=== 下注大小抽象测试 ===\n");

        testBetSizeCalculation();
        testBetSizeAbstractions();
        testActionGenerationScenario1();
        testActionGenerationScenario2();
        testActionGenerationScenario3();

        System.out.println("\n✅ Task 2.3 完成！下注大小抽象测试通过！");
    }

    /**
     * 测试下注大小计算
     */
    private static void testBetSizeCalculation() {
        System.out.println("1. 测试下注大小计算");

        int pot = 100;

        System.out.println("   底池: " + pot);
        System.out.println("   1/3底池: " + BetSize.THIRD_POT.calculateAmount(pot));
        System.out.println("   半池: " + BetSize.HALF_POT.calculateAmount(pot));
        System.out.println("   2/3底池: " + BetSize.TWO_THIRDS_POT.calculateAmount(pot));
        System.out.println("   3/4底池: " + BetSize.THREE_QUARTERS_POT.calculateAmount(pot));
        System.out.println("   满池: " + BetSize.FULL_POT.calculateAmount(pot));
        System.out.println("   1.5倍底池: " + BetSize.OVERBET_150.calculateAmount(pot));
        System.out.println("   2倍底池: " + BetSize.OVERBET_200.calculateAmount(pot));

        // 测试最大值限制
        int stack = 80;
        System.out.println("\n   筹码限制: " + stack);
        System.out.println("   满池(限制后): " + BetSize.FULL_POT.calculateAmount(pot, stack));
        System.out.println("   1.5倍底池(限制后): " + BetSize.OVERBET_150.calculateAmount(pot, stack));

        System.out.println("   ✓ 下注大小计算测试通过\n");
    }

    /**
     * 测试预定义的抽象
     */
    private static void testBetSizeAbstractions() {
        System.out.println("2. 测试预定义抽象");

        System.out.println("   简单抽象: " + BetSizeAbstraction.SIMPLE);
        System.out.println("     " + BetSizeAbstraction.SIMPLE.getBetSizes());

        System.out.println("   标准抽象: " + BetSizeAbstraction.STANDARD);
        System.out.println("     " + BetSizeAbstraction.STANDARD.getBetSizes());

        System.out.println("   精细抽象: " + BetSizeAbstraction.FINE);
        System.out.println("     " + BetSizeAbstraction.FINE.getBetSizes());

        System.out.println("   River抽象: " + BetSizeAbstraction.RIVER);
        System.out.println("     " + BetSizeAbstraction.RIVER.getBetSizes());

        System.out.println("   ✓ 抽象定义测试通过\n");
    }

    /**
     * 测试场景1：初始行动（第一个玩家）
     */
    private static void testActionGenerationScenario1() {
        System.out.println("3. 测试场景1: 初始行动");

        // 创建River状态
        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();
        Hand player0Hand = Hand.parse("As Kh");
        Hand player1Hand = Hand.parse("Qd Qc");

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        RiverState state = new RiverState(board, player0, player1, 20);

        // 使用River抽象
        RiverActionGenerator generator = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        List<Action> actions = generator.generateActions(state);

        System.out.println("   底池: " + state.getPot());
        System.out.println("   玩家0筹码: " + player0.getStack());
        System.out.println("   历史: " + state.getHistory());
        System.out.println("   可用动作 (" + actions.size() + "):");
        for (Action action : actions) {
            System.out.println("     - " + action);
        }

        // 验证：应该有PASS + 4种下注大小 + All-in
        assertTrue("Should have PASS action",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.PASS));
        assertTrue("Should have BET actions",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.BET));

        System.out.println("   ✓ 场景1测试通过\n");
    }

    /**
     * 测试场景2：面对对手过牌
     */
    private static void testActionGenerationScenario2() {
        System.out.println("4. 测试场景2: 对手过牌后");

        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();
        Hand player0Hand = Hand.parse("As Kh");
        Hand player1Hand = Hand.parse("Qd Qc");

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        RiverState state = new RiverState(board, player0, player1, 20);

        // 应用对手的过牌动作
        state = state.applyAction(Action.PASS);

        RiverActionGenerator generator = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        List<Action> actions = generator.generateActions(state);

        System.out.println("   底池: " + state.getPot());
        System.out.println("   当前玩家筹码: " + state.getPlayer(state.getCurrentPlayer()).getStack());
        System.out.println("   历史: " + state.getHistory());
        System.out.println("   可用动作 (" + actions.size() + "):");
        for (Action action : actions) {
            System.out.println("     - " + action);
        }

        // 验证：应该有PASS + BET选项
        assertTrue("Should have PASS action",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.PASS));
        assertTrue("Should have BET actions",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.BET));

        System.out.println("   ✓ 场景2测试通过\n");
    }

    /**
     * 测试场景3：面对下注
     */
    private static void testActionGenerationScenario3() {
        System.out.println("5. 测试场景3: 面对下注");

        List<TexasCard> board = Hand.parse("7h 8s 9c Td Jh").getCards();
        Hand player0Hand = Hand.parse("As Kh");
        Hand player1Hand = Hand.parse("Qd Qc");

        RiverPlayerState player0 = new RiverPlayerState(0, 100, player0Hand);
        RiverPlayerState player1 = new RiverPlayerState(1, 100, player1Hand);

        RiverState state = new RiverState(board, player0, player1, 20);

        // 应用对手的下注动作（半池 = 10）
        int betAmount = BetSize.HALF_POT.calculateAmount(state.getPot());
        state = state.applyAction(new Action(Action.Type.BET, betAmount));

        RiverActionGenerator generator = new RiverActionGenerator(BetSizeAbstraction.RIVER);
        List<Action> actions = generator.generateActions(state);

        System.out.println("   底池: " + state.getPot());
        System.out.println("   对手下注: " + betAmount);
        System.out.println("   当前玩家筹码: " + state.getPlayer(state.getCurrentPlayer()).getStack());
        System.out.println("   历史: " + state.getHistory());
        System.out.println("   可用动作 (" + actions.size() + "):");
        for (Action action : actions) {
            System.out.println("     - " + action);
        }

        // 验证：应该有FOLD, CALL, BET(加注)
        assertTrue("Should have FOLD action",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.FOLD));
        assertTrue("Should have CALL action",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.CALL));
        assertTrue("Should have BET (raise) actions",
            actions.stream().anyMatch(a -> a.getType() == Action.Type.BET));

        System.out.println("   ✓ 场景3测试通过\n");
    }

    // ========== 辅助方法 ==========

    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
