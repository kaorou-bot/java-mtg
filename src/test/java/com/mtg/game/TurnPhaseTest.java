package com.mtg.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TurnPhase enum.
 */
class TurnPhaseTest {

    @Test
    void testAllPhasesExist() {
        TurnPhase[] phases = TurnPhase.values();
        assertEquals(14, phases.length);
    }

    @Test
    void testPhaseNames() {
        assertEquals("Untap Step", TurnPhase.UNTAAP.getName());
        assertEquals("Upkeep Step", TurnPhase.UPKEEP.getName());
        assertEquals("Draw Step", TurnPhase.DRAW.getName());
        assertEquals("Precombat Main Phase", TurnPhase.MAIN1.getName());
        assertEquals("Postcombat Main Phase", TurnPhase.MAIN2.getName());
        assertEquals("Beginning of Combat", TurnPhase.COMBAT_START.getName());
        assertEquals("Declare Attackers", TurnPhase.DECLARE_ATTACKERS.getName());
        assertEquals("Declare Blockers", TurnPhase.DECLARE_BLOCKERS.getName());
        assertEquals("Combat Damage", TurnPhase.COMBAT_DAMAGE.getName());
        assertEquals("First Strike Damage", TurnPhase.COMBAT_DAMAGE_FIRST.getName());
        assertEquals("End of Combat", TurnPhase.COMBAT_END.getName());
        assertEquals("End Step", TurnPhase.END.getName());
        assertEquals("Cleanup Step", TurnPhase.CLEANUP.getName());
        assertEquals("Game Over", TurnPhase.GAME_OVER.getName());
    }

    @Test
    void testCategories() {
        assertEquals("Beginning Phase", TurnPhase.UNTAAP.getCategory());
        assertEquals("Main Phase", TurnPhase.MAIN1.getCategory());
        assertEquals("Combat Phase", TurnPhase.COMBAT_START.getCategory());
        assertEquals("Ending Phase", TurnPhase.END.getCategory());
        assertEquals("", TurnPhase.GAME_OVER.getCategory());
    }

    @Test
    void testIsMainPhase() {
        assertTrue(TurnPhase.MAIN1.isMainPhase());
        assertTrue(TurnPhase.MAIN2.isMainPhase());
        assertFalse(TurnPhase.DECLARE_ATTACKERS.isMainPhase());
        assertFalse(TurnPhase.UPKEEP.isMainPhase());
    }

    @Test
    void testIsUntapStep() {
        assertTrue(TurnPhase.UNTAAP.isUntapStep());
        assertFalse(TurnPhase.UPKEEP.isUntapStep());
    }

    @Test
    void testIsCleanupStep() {
        assertTrue(TurnPhase.CLEANUP.isCleanupStep());
        assertFalse(TurnPhase.END.isCleanupStep());
    }

    @Test
    void testHasPriority() {
        assertFalse(TurnPhase.UNTAAP.hasPriority()); // Rule 502.4
        assertTrue(TurnPhase.UPKEEP.hasPriority());
        assertTrue(TurnPhase.MAIN1.hasPriority());
        assertTrue(TurnPhase.DECLARE_ATTACKERS.hasPriority());
    }

    @Test
    void testIsCombatPhase() {
        assertTrue(TurnPhase.COMBAT_START.isCombatPhase());
        assertTrue(TurnPhase.DECLARE_ATTACKERS.isCombatPhase());
        assertTrue(TurnPhase.DECLARE_BLOCKERS.isCombatPhase());
        assertTrue(TurnPhase.COMBAT_DAMAGE.isCombatPhase());
        assertTrue(TurnPhase.COMBAT_DAMAGE_FIRST.isCombatPhase());
        assertTrue(TurnPhase.COMBAT_END.isCombatPhase());

        assertFalse(TurnPhase.MAIN1.isCombatPhase());
        assertFalse(TurnPhase.UPKEEP.isCombatPhase());
    }

    @Test
    void testIsBeginningPhase() {
        assertTrue(TurnPhase.UNTAAP.isBeginningPhase());
        assertTrue(TurnPhase.UPKEEP.isBeginningPhase());
        assertTrue(TurnPhase.DRAW.isBeginningPhase());

        assertFalse(TurnPhase.MAIN1.isBeginningPhase());
    }

    @Test
    void testIsEndingPhase() {
        assertTrue(TurnPhase.END.isEndingPhase());
        assertTrue(TurnPhase.CLEANUP.isEndingPhase());

        assertFalse(TurnPhase.MAIN1.isEndingPhase());
    }
}
