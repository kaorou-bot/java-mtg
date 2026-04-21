package com.mtg.gamecore;

import com.mtg.player.Player;
import com.mtg.zones.Stack;
import com.mtg.zones.Stack.AbilityItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PrioritySystem per Rule 117.
 */
class PrioritySystemTest {

    private PrioritySystem ps;
    private Player active;
    private Player nonActive;

    @BeforeEach
    void setUp() {
        ps = new PrioritySystem();
        active = new Player("Active");
        nonActive = new Player("NonActive");
        ps.setPlayers(active, nonActive);
    }

    @Test
    void testInitialState() {
        assertNull(ps.getPlayerWithPriority());
        assertFalse(ps.allPlayersPassed());
    }

    @Test
    void testGrantPriorityToActive() {
        ps.grantPriorityToActive();

        assertEquals(active, ps.getPlayerWithPriority());
        assertFalse(ps.allPlayersPassed());
        assertTrue(ps.playerHasPriority(active));
        assertFalse(ps.playerHasPriority(nonActive));
    }

    @Test
    void testGrantPriorityAfterResolve() {
        ps.grantPriorityToActive();
        ps.grantPriorityAfterResolve();

        assertEquals(active, ps.getPlayerWithPriority());
    }

    @Test
    void testOnSpellCastResetsPassCount() {
        ps.grantPriorityToActive();
        ps.pass(); // AP -> NAP
        ps.pass(); // NAP -> passCount=1
        ps.onSpellCast(); // Should reset passCount

        // After spell cast, active player has priority again
        assertEquals(active, ps.getPlayerWithPriority());
        assertFalse(ps.allPlayersPassed());
    }

    @Test
    void testOnAbilityActivatedResetsPassCount() {
        ps.grantPriorityToActive();
        ps.pass();
        ps.pass();
        ps.onAbilityActivated();

        assertEquals(active, ps.getPlayerWithPriority());
    }

    @Test
    void testPassSequence() {
        ps.grantPriorityToActive();
        assertEquals(active, ps.getPlayerWithPriority());

        // Active passes to nonActive
        ps.pass();
        assertEquals(nonActive, ps.getPlayerWithPriority());

        // NonActive passes - both passed once, priority returns to AP
        ps.pass();
        assertFalse(ps.allPlayersPassed());
        assertEquals(active, ps.getPlayerWithPriority());
    }

    @Test
    void testMultiplePasses() {
        ps.grantPriorityToActive();
        ps.pass(); // AP -> NAP
        ps.pass(); // NAP -> AP, passCount=1
        ps.pass(); // AP -> NAP, passCount=1

        assertFalse(ps.allPlayersPassed());

        ps.pass(); // NAP -> all passed, passCount=2

        assertTrue(ps.allPlayersPassed());
    }

    @Test
    void testPlayerHasPriority() {
        ps.grantPriorityToActive();
        assertTrue(ps.playerHasPriority(active));
        assertFalse(ps.playerHasPriority(nonActive));

        ps.pass();
        assertFalse(ps.playerHasPriority(active));
        assertTrue(ps.playerHasPriority(nonActive));
    }

    @Test
    void testGetAvailableActionsMainPhase() {
        ps.grantPriorityToActive();
        Stack stack = new Stack();

        var actions = ps.getAvailableActions(active, stack, true, false);

        assertTrue(actions.contains("Pass Priority"));
        assertTrue(actions.contains("Play Land"));
        assertTrue(actions.contains("Cast Spell"));
        assertTrue(actions.contains("Activate Ability"));
    }

    @Test
    void testGetAvailableActionsNonPriorityPlayer() {
        ps.grantPriorityToActive();
        Stack stack = new Stack();

        var actions = ps.getAvailableActions(nonActive, stack, true, false);

        assertTrue(actions.isEmpty());
    }

    @Test
    void testGetAvailableActionsWithNonEmptyStack() {
        ps.grantPriorityToActive();
        Stack stack = new Stack();
        // Add a spell to stack
        stack.push(new AbilityItem("Test", active, () -> {}));

        var actions = ps.getAvailableActions(active, stack, false, false);

        assertTrue(actions.contains("Cast Instant"));
    }

    @Test
    void testReset() {
        ps.grantPriorityToActive();
        ps.reset();

        assertNull(ps.getPlayerWithPriority());
        assertFalse(ps.allPlayersPassed());
    }
}
