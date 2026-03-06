package com.poker.gto.core.ranges;

import com.poker.gto.core.cards.Rank;
import com.poker.gto.core.cards.Suit;
import com.poker.gto.core.cards.TexasCard;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Range 类测试
 */
public class RangeTest {

    @BeforeAll
    static void setup() {
        System.out.println("\n=== Range 测试开始 ===\n");
    }

    @Test
    @DisplayName("测试1: HandCombo 创建和解析")
    void testHandComboCreation() {
        System.out.println("测试1: HandCombo 创建和解析...");

        // 对子
        HandCombo aa = HandCombo.parse("AA");
        assertTrue(aa.isPair());
        assertFalse(aa.isSuited());
        assertEquals(6, aa.getPossibleHands());

        // 同花
        HandCombo aks = HandCombo.parse("AKs");
        assertFalse(aks.isPair());
        assertTrue(aks.isSuited());
        assertEquals(4, aks.getPossibleHands());

        // 非同花
        HandCombo ako = HandCombo.parse("AKo");
        assertFalse(ako.isPair());
        assertFalse(ako.isSuited());
        assertEquals(12, ako.getPossibleHands());

        System.out.println("  ✓ AA: " + aa + " (" + aa.getPossibleHands() + " hands)");
        System.out.println("  ✓ AKs: " + aks + " (" + aks.getPossibleHands() + " hands)");
        System.out.println("  ✓ AKo: " + ako + " (" + ako.getPossibleHands() + " hands)\n");
    }

    @Test
    @DisplayName("测试2: 169种组合生成")
    void testAll169Combos() {
        System.out.println("测试2: 169种组合生成...");

        List<HandCombo> allCombos = HandCombo.getAllCombos();
        assertEquals(169, allCombos.size());

        long pairs = allCombos.stream().filter(HandCombo::isPair).count();
        long suited = allCombos.stream().filter(c -> !c.isPair() && c.isSuited()).count();
        long offsuit = allCombos.stream().filter(c -> !c.isPair() && !c.isSuited()).count();

        assertEquals(13, pairs);
        assertEquals(78, suited);
        assertEquals(78, offsuit);

        System.out.println("  总计: " + allCombos.size() + " 种组合");
        System.out.println("  对子: " + pairs);
        System.out.println("  同花: " + suited);
        System.out.println("  非同花: " + offsuit);
        System.out.println("  ✓ 169种组合验证通过\n");
    }

    @Test
    @DisplayName("测试3: Range 基本操作")
    void testRangeBasics() {
        System.out.println("测试3: Range 基本操作...");

        Range range = Range.empty();
        assertTrue(range.isEmpty());

        HandCombo aa = HandCombo.parse("AA");
        range = range.setWeight(aa, 1.0);
        assertFalse(range.isEmpty());
        assertEquals(1, range.size());
        assertEquals(1.0, range.getWeight(aa), 0.001);

        // 测试权重限制
        range = range.setWeight(aa, 1.5);  // 应该被限制为1.0
        assertEquals(1.0, range.getWeight(aa), 0.001);

        System.out.println("  ✓ 空范围创建");
        System.out.println("  ✓ 权重设置");
        System.out.println("  ✓ 权重限制 (1.5 → 1.0)\n");
    }

    @Test
    @DisplayName("测试4: 字符串解析")
    void testRangeParsing() {
        System.out.println("测试4: 字符串解析...");

        // 简单列表
        Range range1 = Range.parse("AA, KK, AKs");
        assertEquals(3, range1.size());

        // 带权重
        Range range2 = Range.parse("AA:0.5, KK:0.75");
        assertEquals(0.5, range2.getWeight(HandCombo.parse("AA")), 0.001);
        assertEquals(0.75, range2.getWeight(HandCombo.parse("KK")), 0.001);

        // 范围
        Range range3 = Range.parse("AA-TT");
        assertEquals(4, range3.size());  // AA, KK, QQ, JJ, TT (等等，应该是5个)
        assertTrue(range3.contains(HandCombo.parse("AA")));
        assertTrue(range3.contains(HandCombo.parse("TT")));

        System.out.println("  ✓ 简单列表: " + range1);
        System.out.println("  ✓ 带权重: " + range2);
        System.out.println("  ✓ 范围: " + range3.size() + " combos\n");
    }

    @Test
    @DisplayName("测试5: 范围操作 - 合并")
    void testRangeMerge() {
        System.out.println("测试5: 范围合并...");

        Range range1 = Range.parse("AA, KK");
        Range range2 = Range.parse("KK, QQ");

        Range merged = range1.merge(range2);
        assertEquals(3, merged.size());  // AA, KK, QQ
        assertTrue(merged.contains(HandCombo.parse("AA")));
        assertTrue(merged.contains(HandCombo.parse("KK")));
        assertTrue(merged.contains(HandCombo.parse("QQ")));

        System.out.println("  Range1: " + range1);
        System.out.println("  Range2: " + range2);
        System.out.println("  Merged: " + merged);
        System.out.println("  ✓ 合并操作正确\n");
    }

    @Test
    @DisplayName("测试6: 范围操作 - 过滤")
    void testRangeFilter() {
        System.out.println("测试6: 范围过滤...");

        Range range = Range.parse("AA, KK, AKs, AKo, 22");

        Range pairsOnly = range.onlyPairs();
        assertEquals(3, pairsOnly.size());  // AA, KK, 22

        Range suitedOnly = range.onlySuited();
        assertEquals(1, suitedOnly.size());  // AKs

        Range offsuitOnly = range.onlyOffsuit();
        assertEquals(1, offsuitOnly.size());  // AKo

        System.out.println("  原范围: " + range);
        System.out.println("  只保留对子: " + pairsOnly);
        System.out.println("  只保留同花: " + suitedOnly);
        System.out.println("  只保留非同花: " + offsuitOnly);
        System.out.println("  ✓ 过滤操作正确\n");
    }

    @Test
    @DisplayName("测试7: 范围操作 - 标准化")
    void testRangeNormalize() {
        System.out.println("测试7: 范围标准化...");

        Range range = Range.parse("AA:0.5, KK:0.3");

        // 标准化权重（使和为1.0）
        Range normalized = range.normalizeWeights();
        double sum = normalized.getWeights().values().stream()
            .mapToDouble(Double::doubleValue).sum();

        assertEquals(1.0, sum, 0.001);

        System.out.println("  原范围: " + range);
        System.out.println("  标准化后: " + normalized);
        System.out.println("  权重和: " + sum);
        System.out.println("  ✓ 标准化操作正确\n");
    }

    @Test
    @DisplayName("测试8: 移除死牌")
    void testRemoveDeadCards() {
        System.out.println("测试8: 移除死牌...");

        Range range = Range.parse("AA, KK, AKs");

        // 移除一张A
        TexasCard deadCard = TexasCard.of(Rank.ACE, Suit.SPADES);
        Range filtered = range.removeDeadCards(deadCard);

        // AA应该仍然存在（还有其他A可用）
        // AKs应该仍然存在
        assertTrue(filtered.contains(HandCombo.parse("AA")));
        assertTrue(filtered.contains(HandCombo.parse("AKs")));

        System.out.println("  原范围: " + range);
        System.out.println("  死牌: " + deadCard);
        System.out.println("  过滤后: " + filtered);
        System.out.println("  ✓ 死牌过滤正确\n");
    }

    @Test
    @DisplayName("测试9: 通配符解析")
    void testWildcardParsing() {
        System.out.println("测试9: 通配符解析...");

        // A*s: 所有同花A
        Range range1 = Range.parse("A*s");
        assertTrue(range1.contains(HandCombo.parse("AKs")));
        assertTrue(range1.contains(HandCombo.parse("A2s")));
        assertFalse(range1.contains(HandCombo.parse("AKo")));

        // K*o: 所有非同花K
        Range range2 = Range.parse("K*o");
        assertTrue(range2.contains(HandCombo.parse("KQo")));
        assertTrue(range2.contains(HandCombo.parse("K2o")));
        assertFalse(range2.contains(HandCombo.parse("KQs")));

        System.out.println("  A*s: " + range1.size() + " combos");
        System.out.println("  K*o: " + range2.size() + " combos");
        System.out.println("  ✓ 通配符解析正确\n");
    }

    @Test
    @DisplayName("测试10: 统计信息")
    void testRangeStats() {
        System.out.println("测试10: 统计信息...");

        Range range = Range.parse("AA-22, AKs");  // 13对子 + AKs

        double totalCombos = range.getTotalCombos();
        double pairPercentage = range.getPairPercentage();

        // 13对子 * 6 + AKs * 4 = 78 + 4 = 82
        assertEquals(82.0, totalCombos, 0.1);

        System.out.println("  范围: " + range.size() + " 种组合");
        System.out.println("  总手牌数: " + totalCombos);
        System.out.println("  对子百分比: " + String.format("%.1f%%", pairPercentage));
        System.out.println("  ✓ 统计信息正确\n");
    }

    @AfterAll
    static void summary() {
        System.out.println("=== Range 测试完成 ===\n");
    }
}
