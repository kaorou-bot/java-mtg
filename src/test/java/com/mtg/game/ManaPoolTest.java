package com.mtg.game;

import com.mtg.model.ManaCost;
import com.mtg.model.ManaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ManaPool 单元测试。
 */
class ManaPoolTest {

    private ManaPool manaPool;

    @BeforeEach
    void setUp() {
        manaPool = new ManaPool();
    }

    @Test
    void testCreation() {
        assertNotNull(manaPool);
        assertTrue(manaPool.isEmpty());
        assertEquals(0, manaPool.getTotalMana());
    }

    @Test
    void testAddMana() {
        manaPool.add(ManaType.RED, 2);
        assertEquals(2, manaPool.getAmount(ManaType.RED));
        assertEquals(2, manaPool.getTotalMana());
        assertFalse(manaPool.isEmpty());
    }

    @Test
    void testAddMultipleManaTypes() {
        manaPool.add(ManaType.RED, 2);
        manaPool.add(ManaType.BLUE, 3);
        manaPool.add(ManaType.WHITE, 1);

        assertEquals(2, manaPool.getAmount(ManaType.RED));
        assertEquals(3, manaPool.getAmount(ManaType.BLUE));
        assertEquals(1, manaPool.getAmount(ManaType.WHITE));
        assertEquals(6, manaPool.getTotalMana());
    }

    @Test
    void testAddGenericMana() {
        manaPool.addGeneric(5);
        assertEquals(5, manaPool.getAmount(ManaType.COLORLESS));
        assertEquals(5, manaPool.getTotalMana());
    }

    @Test
    void testClear() {
        manaPool.add(ManaType.RED, 3);
        manaPool.add(ManaType.BLUE, 2);
        assertEquals(5, manaPool.getTotalMana());

        manaPool.clear();
        assertEquals(0, manaPool.getTotalMana());
        assertTrue(manaPool.isEmpty());
    }

    @Test
    void testDeductGeneric() {
        manaPool.add(ManaType.RED, 2);
        manaPool.add(ManaType.COLORLESS, 3);
        assertEquals(5, manaPool.getTotalMana());

        manaPool.deductGeneric(4);
        assertEquals(1, manaPool.getTotalMana());
    }

    @Test
    void testDeductMoreThanAvailable() {
        manaPool.add(ManaType.RED, 2);
        manaPool.deductGeneric(5);
        // 不会变负数
        assertEquals(0, manaPool.getTotalMana());
    }

    @Test
    void testHasAmount() {
        manaPool.add(ManaType.RED, 3);
        assertTrue(manaPool.hasAmount(2));
        assertTrue(manaPool.hasAmount(3));
        assertFalse(manaPool.hasAmount(4));
    }

    @Test
    void testCanPay() {
        manaPool.add(ManaType.RED, 3);
        ManaCost cost = ManaCost.of(2);
        assertTrue(manaPool.canPay(cost));
    }

    @Test
    void testCanPayInsufficient() {
        manaPool.add(ManaType.RED, 1);
        ManaCost cost = ManaCost.of(3);
        assertFalse(manaPool.canPay(cost));
    }

    @Test
    void testPay() {
        manaPool.add(ManaType.RED, 5);
        ManaCost cost = ManaCost.of(3);
        assertTrue(manaPool.pay(cost));
        assertEquals(2, manaPool.getTotalMana());
    }

    @Test
    void testPayNullCost() {
        assertTrue(manaPool.pay(null));
    }

    @Test
    void testPayInsufficient() {
        manaPool.add(ManaType.RED, 1);
        ManaCost cost = ManaCost.of(3);
        assertFalse(manaPool.pay(cost));
        // 法力不应该减少
        assertEquals(1, manaPool.getTotalMana());
    }

    @Test
    void testToDisplayString() {
        assertEquals("Empty", manaPool.toDisplayString());

        manaPool.add(ManaType.RED, 2);
        manaPool.add(ManaType.BLUE, 1);
        String display = manaPool.toDisplayString();
        assertTrue(display.contains("R"));
        assertTrue(display.contains("2"));
        assertTrue(display.contains("U"));
        assertTrue(display.contains("1"));
    }

    @Test
    void testGetDistribution() {
        manaPool.add(ManaType.RED, 2);
        manaPool.add(ManaType.GREEN, 3);

        var distribution = manaPool.getDistribution();
        assertEquals(2, distribution.get(ManaType.RED));
        assertEquals(3, distribution.get(ManaType.GREEN));
    }

    @Test
    void testGetAvailableManaString() {
        assertEquals("No mana available", manaPool.getAvailableManaString());

        manaPool.add(ManaType.RED, 2);
        assertEquals(manaPool.toDisplayString(), manaPool.getAvailableManaString());
    }

    @Test
    void testToString() {
        manaPool.add(ManaType.RED, 2);
        String str = manaPool.toString();
        assertNotNull(str);
        assertTrue(str.contains("ManaPool"));
    }

    @Test
    void testAddNullType() {
        // 添加 null 类型不应该抛出异常
        manaPool.add(null, 5);
        assertEquals(0, manaPool.getTotalMana());
    }

    @Test
    void testGetAmountForUnsetType() {
        // 获取未设置的法力类型应返回 0
        assertEquals(0, manaPool.getAmount(ManaType.RED));
    }

    @Test
    void testMultipleAddOperations() {
        manaPool.add(ManaType.RED, 1);
        manaPool.add(ManaType.RED, 2);
        manaPool.add(ManaType.RED, 3);
        assertEquals(6, manaPool.getAmount(ManaType.RED));
        assertEquals(6, manaPool.getTotalMana());
    }

    @Test
    void testDeductFromMultipleTypes() {
        manaPool.add(ManaType.RED, 2);
        manaPool.add(ManaType.BLUE, 2);
        manaPool.add(ManaType.GREEN, 2);
        assertEquals(6, manaPool.getTotalMana());

        manaPool.deductGeneric(5);
        // 扣除顺序：先扣除无色（0），然后按 RED -> BLUE -> GREEN 顺序
        assertEquals(1, manaPool.getTotalMana());
    }
}
