# 扑克游戏规则详解

**版本**: 0.1.0
**更新日期**: 2026-03-06

---

## 目录

1. [Kuhn Poker](#1-kuhn-poker) - Milestone 1
2. [Leduc Poker](#2-leduc-poker) - Milestone 1
3. [Texas Hold'em](#3-texas-holdem) - Milestone 2-4

---

## 1. Kuhn Poker

### 1.1 游戏概述

**最简单的非平凡扑克游戏**，用于验证 CFR 算法正确性。

**特点**:
- 2 名玩家
- 3 张牌
- 1 轮下注
- 博弈树规模小（~12 个信息集）

### 1.2 规则详解

#### 1.2.1 初始设置

**牌组**:
- 只有 3 张牌：Jack (J), Queen (Q), King (K)
- 牌力大小：K > Q > J

**筹码**:
- 每人初始筹码无限
- Ante（底注）：每人 1 元

#### 1.2.2 游戏流程

**Phase 1: 发牌**
1. 洗牌
2. 玩家 1 获得 1 张牌（对玩家 2 不可见）
3. 玩家 2 获得 1 张牌（对玩家 1 不可见）
4. 剩余 1 张牌不使用

**Phase 2: 下注**

**玩家 1 行动**:
- **Pass (CHECK)**: 不下注，轮到玩家 2
- **Bet**: 下注 1 元

**玩家 2 行动**（如果玩家 1 CHECK）:
- **Pass (CHECK)**: 不下注，直接摊牌
- **Bet**: 下注 1 元

**玩家 2 行动**（如果玩家 1 BET）:
- **Fold**: 弃牌，玩家 1 获胜
- **Call**: 跟注 1 元，摊牌比牌

**玩家 1 行动**（如果玩家 1 CHECK，玩家 2 BET）:
- **Fold**: 弃牌，玩家 2 获胜
- **Call**: 跟注 1 元，摊牌比牌

#### 1.2.3 收益计算

**场景 1: 双方 CHECK**
- 比牌，大牌获胜
- 赢家获得 2 元（2 个 Ante）
- 相对于初始状态，赢家 +1 元，输家 -1 元

**场景 2: 一方 BET，另一方 FOLD**
- BET 的一方获胜
- 赢家获得 2 元
- 相对于初始状态，赢家 +1 元，输家 -1 元

**场景 3: 一方 BET，另一方 CALL**
- 比牌，大牌获胜
- 底池 = 4 元（2 Ante + 2 Bet）
- 相对于初始状态，赢家 +2 元，输家 -2 元

### 1.3 博弈树

```
        ROOT (发牌)
         / | \
        /  |  \
      J   Q    K  (玩家1的牌)
      |   |    |
    [P1: Pass or Bet]
```

**示例：玩家1持J**

```
P1持J
├─ P1 Pass
│   └─ P2持Q
│       ├─ P2 Pass → 摊牌 (P2赢，P1: -1)
│       └─ P2 Bet
│           ├─ P1 Fold → P2赢 (P1: -1)
│           └─ P1 Call → 摊牌 (P2赢，P1: -2)
│
└─ P1 Bet
    └─ P2持Q
        ├─ P2 Fold → P1赢 (P1: +1)
        └─ P2 Call → 摊牌 (P2赢，P1: -2)
```

### 1.4 已知纳什均衡策略

**玩家 1 (先手)**:
- 持 J: Pass 100%（永不下注）
- 持 Q: Pass 100%（诱敌深入）
- 持 K: Bet 3α（α ≈ 0.333）, Pass 1-3α

**玩家 2 (后手)**:

如果玩家 1 Pass:
- 持 J: Pass 100%
- 持 Q: Bet 1/3, Pass 2/3
- 持 K: Bet 100%

如果玩家 1 Bet:
- 持 J: Fold 100%
- 持 Q: Fold 100%（被 bluff 也要弃）
- 持 K: Call 100%

**均衡价值**:
- 玩家 1 期望收益: -1/18 ≈ -0.056
- 玩家 2 期望收益: +1/18 ≈ +0.056

（后手优势）

### 1.5 实现要点

**信息集编码**:
```java
// 玩家1持K，第一个行动
"P0_K"

// 玩家2持Q，玩家1已经Bet
"P1_Q_bet"

// 玩家1持J，玩家1Pass，玩家2Bet
"P0_J_pass-bet"
```

**终局判定**:
```java
boolean isTerminal(GameState state) {
    List<Action> history = state.getHistory();

    // Pass-Pass
    if (history.equals(Arrays.asList(PASS, PASS))) {
        return true;
    }

    // Bet-Fold
    if (history.size() == 2 &&
        history.get(0) == BET &&
        history.get(1) == FOLD) {
        return true;
    }

    // Bet-Call 或 Pass-Bet-Call
    if (history.get(history.size() - 1) == CALL) {
        return true;
    }

    return false;
}
```

---

## 2. Leduc Poker

### 2.1 游戏概述

**比 Kuhn Poker 复杂，但比 Hold'em 简单**，用于验证多轮下注和公共牌。

**特点**:
- 2 名玩家
- 6 张牌（3 种点数，每种 2 张）
- 2 轮下注（翻前 + 翻牌）
- 博弈树规模中等（~288 个信息集）

### 2.2 规则详解

#### 2.2.1 初始设置

**牌组**:
- 6 张牌：J♠ J♥, Q♠ Q♥, K♠ K♥
- 牌型：一对 > 单张
- 相同牌型比点数：K > Q > J

**筹码**:
- 每人初始筹码：无限
- Ante：每人 1 元
- 下注限制：每轮最多 1 次加注

#### 2.2.2 游戏流程

**Round 1: Preflop（翻前）**

1. 每人发 1 张底牌（私有）
2. 玩家 1 先行动
3. 可选动作：
   - Check
   - Bet 2 元
4. 下注轮继续直到双方行动一致

**Round 2: Flop（翻牌）**

1. 发 1 张公共牌（双方可见）
2. 玩家 1 先行动
3. 可选动作：
   - Check
   - Bet 4 元（翻牌圈下注是翻前的 2 倍）
4. 下注轮继续

**Showdown（摊牌）**

比较牌型：
- 一对（底牌 = 公共牌）> 单张
- 相同牌型比大小

#### 2.2.3 行动规则

**每轮下注限制**:
- Preflop: Bet 2 元
- Flop: Bet 4 元
- 每轮最多 1 次 Raise（即 Bet-Raise-Call/Fold）

**示例流程**:
```
Preflop:
  P1: Bet 2
  P2: Raise 2 (总共 4)
  P1: Call 2

Flop:
  公共牌: Q♥
  P1: Check
  P2: Bet 4
  P1: Call 4

Showdown:
  P1: K♠, 公共牌: Q♥ → 高牌 K
  P2: Q♠, 公共牌: Q♥ → 一对 Q
  P2 获胜，赢得 2 + 4 + 4 + 4 + 4 = 18 元
```

### 2.3 博弈树结构

```
ROOT (发牌)
 ├─ P1:J♠, P2:J♥
 ├─ P1:J♠, P2:Q♥
 ├─ P1:J♠, P2:Q♠
 ├─ ... (共 6×5 = 30 种组合)
 │
 └─ [Preflop 下注轮]
     ├─ Check-Check
     │   └─ [发公共牌]
     │       ├─ J♥
     │       ├─ Q♠
     │       └─ K♥
     │           └─ [Flop 下注轮]
     │
     ├─ Bet-Fold → 终局
     ├─ Bet-Call
     │   └─ [发公共牌]
     └─ Bet-Raise-Call
         └─ [发公共牌]
```

### 2.4 实现要点

**牌型评估**:
```java
enum LeducHandRank {
    HIGH_CARD,  // 单张
    PAIR        // 一对
}

class LeducEvaluator {
    public LeducHandRank evaluate(Card holeCard, Card board) {
        if (holeCard.getRank() == board.getRank()) {
            return LeducHandRank.PAIR;
        }
        return LeducHandRank.HIGH_CARD;
    }

    public int compare(Card h1, Card b, Card h2) {
        LeducHandRank r1 = evaluate(h1, b);
        LeducHandRank r2 = evaluate(h2, b);

        if (r1 != r2) {
            return r1.compareTo(r2);
        }

        // 相同牌型，比点数
        return h1.getRank().compareTo(h2.getRank());
    }
}
```

**信息集编码**:
```java
// Preflop: P0持K，已经Bet
"P0_PREFLOP_K_bet"

// Flop: P1持J，公共牌Q，历史Check-Bet
"P1_FLOP_J_Q_check-bet"

// Flop: P0持Q，公共牌Q（成对），历史Bet-Call
"P0_FLOP_Q_Q_bet-call"
```

---

## 3. Texas Hold'em

### 3.1 游戏概述

**经典扑克游戏**，项目的最终目标。

**特点**:
- 2-10 名玩家（本项目先做 Heads-Up，即 2 人）
- 52 张牌
- 4 轮下注（Preflop, Flop, Turn, River）
- 博弈树极大（~10^160 状态）

### 3.2 规则详解

#### 3.2.1 初始设置

**牌组**:
- 标准 52 张牌
- 4 种花色：♠ ♥ ♦ ♣
- 13 种点数：2-10, J, Q, K, A

**筹码**:
- 初始筹码：可配置（如 100 BB）
- 盲注：
  - 小盲（SB）：1 BB
  - 大盲（BB）：2 BB

#### 3.2.2 游戏流程

**Phase 1: Preflop（翻前）**

1. SB 和 BB 强制下盲注
2. 每人发 2 张底牌（私有）
3. SB 先行动
4. 可选动作：Fold, Call, Raise

**Phase 2: Flop（翻牌）**

1. 发 3 张公共牌
2. SB 先行动
3. 可选动作：Check, Bet, Fold, Call, Raise

**Phase 3: Turn（转牌）**

1. 发第 4 张公共牌
2. SB 先行动
3. 动作同 Flop

**Phase 4: River（河牌）**

1. 发第 5 张公共牌
2. SB 先行动
3. 动作同 Flop

**Showdown（摊牌）**

- 双方亮出底牌
- 从 7 张牌（2 张底牌 + 5 张公共牌）中选最好的 5 张
- 比较牌型

#### 3.2.3 牌型排名

从大到小：

1. **Royal Flush（皇家同花顺）**: A♠ K♠ Q♠ J♠ 10♠
2. **Straight Flush（同花顺）**: 9♥ 8♥ 7♥ 6♥ 5♥
3. **Four of a Kind（四条）**: K♠ K♥ K♦ K♣ A♠
4. **Full House（葫芦）**: Q♠ Q♥ Q♦ 7♠ 7♥
5. **Flush（同花）**: A♦ J♦ 9♦ 6♦ 4♦
6. **Straight（顺子）**: 10♠ 9♥ 8♦ 7♣ 6♠
7. **Three of a Kind（三条）**: 8♠ 8♥ 8♦ K♠ 5♥
8. **Two Pair（两对）**: A♠ A♥ 7♦ 7♣ K♠
9. **One Pair（一对）**: K♠ K♥ Q♦ 9♠ 5♥
10. **High Card（高牌）**: A♠ K♥ Q♦ 8♠ 5♥

### 3.3 Heads-Up 规则差异

**盲注**:
- BTN（按钮位）= SB（小盲）
- 另一玩家 = BB（大盲）

**行动顺序**:
- Preflop: BTN 先行动
- Flop/Turn/River: BB 先行动

### 3.4 实现简化

#### Milestone 2: River 子博弈

**简化**:
- 固定公共牌（如 7♥ 8♠ 9♣ 2♦ 3♥）
- 固定底池大小
- 固定筹码深度（SPR）
- 有限下注尺寸（如 0.5x, 1x, 2x pot）

**场景示例**:
```
Board: 7♥ 8♠ 9♣ 2♦ 3♥
Pot: 100
Effective Stack: 100 (SPR = 1)
Bet sizes: 50, 100, 200 (all-in)

P1 Range: [AA, KK, QQ, JJ, TT, 99, 88, 77, AK, AQ]
P2 Range: [AA, KK, QQ, JJ, TT, 99, 88, 77, 66, 55]
```

#### Milestone 3-4: 多街道扩展

**扩展顺序**:
1. River（已完成）
2. Turn + River
3. Flop + Turn + River
4. Preflop + Flop + Turn + River

**抽象策略**:
- Hand Bucketing（手牌分桶）
- Bet Abstraction（下注抽象）
- State Filtering（状态过滤）

### 3.5 实现要点

**7 卡评估器**:
```java
interface HandEvaluator {
    // 从 7 张牌中选最好的 5 张
    EvaluationResult evaluate7Cards(List<Card> cards);
}
```

**范围表示**:
```java
class Range {
    private Map<Hand, Double> combos;  // 手牌组合 -> 权重

    public Range(String rangeString);  // "AA,KK,QQ,AKs,AKo"

    public double getWeight(Hand hand);
    public List<Hand> getHands();
    public int size();
}
```

**Equity 计算**:
```java
class EquityCalculator {
    public double calculate(
        Range range1,
        Range range2,
        List<Card> board
    ) {
        // 枚举所有可能的手牌组合
        // 计算赢率
    }
}
```

---

## 4. 三种游戏对比

| 特性 | Kuhn Poker | Leduc Poker | Texas Hold'em |
|------|------------|-------------|---------------|
| **玩家数** | 2 | 2 | 2-10 (本项目:2) |
| **牌数** | 3 | 6 | 52 |
| **底牌** | 1张 | 1张 | 2张 |
| **公共牌** | 无 | 1张 | 5张 |
| **下注轮** | 1轮 | 2轮 | 4轮 |
| **信息集数** | ~12 | ~288 | 10^6+ |
| **博弈树大小** | 微小 | 小 | 极大 |
| **求解时间** | 秒级 | 分钟级 | 小时-天级 |
| **用途** | 算法验证 | 扩展验证 | 实际应用 |

---

## 5. 参考资源

### 5.1 Kuhn Poker

- [维基百科](https://en.wikipedia.org/wiki/Kuhn_poker)
- 已知均衡策略论文: Kuhn, H. W. (1950)

### 5.2 Leduc Poker

- [OpenSpiel 实现](https://github.com/deepmind/open_spiel/blob/master/open_spiel/games/leduc_poker.cc)

### 5.3 Texas Hold'em

- [扑克规则官网](https://www.pokernews.com/poker-rules/texas-holdem.htm)
- [手牌评估算法](https://en.wikipedia.org/wiki/Poker_probability)

---

## 6. 下一步

1. ✅ 理解 Kuhn Poker 规则
2. ⏳ 实现 Kuhn Poker 状态机
3. ⏳ 实现 Kuhn Poker 评估器
4. ⏳ 验证 CFR 在 Kuhn Poker 上收敛
5. ⏳ 扩展到 Leduc Poker
6. ⏳ 实现德州扑克 River 子博弈

---

**文档维护者**: Game Model Agent
**参考**: 游戏规则官方文档
**最后更新**: 2026-03-06
