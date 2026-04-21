package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TurnManager - turn structure per Rules 500-514.
 */
class TurnManagerTest {

    private TurnManager tm;
    private Player p1;
    private Player p2;
    private Battlefield bf;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        tm = new TurnManager(null);
        bf = new Battlefield();
        tm.setBattlefield(bf);
        tm.startTurn(p1); // startTurn increments turnNumber to 1
    }

    private CreatureCard makeCreature(String name, int power, int toughness) {
        return new CreatureCard(name, ManaCost.of(2),
                "", Arrays.asList(ManaType.GREEN), power, toughness,
                false, false, false, false, false) {};
    }

    @Test
    void testInitialPhase() {
        assertEquals(TurnPhase.UNTAAP, tm.getCurrentPhase());
    }

    @Test
    void testAdvancePhaseSequence() {
        // Starting from UNTAAP
        assertEquals(TurnPhase.UNTAAP, tm.getCurrentPhase());

        TurnPhase phase = tm.advancePhase();
        assertEquals(TurnPhase.UPKEEP, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.DRAW, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.MAIN1, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.COMBAT_START, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.DECLARE_ATTACKERS, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.DECLARE_BLOCKERS, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.COMBAT_DAMAGE, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.COMBAT_END, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.MAIN2, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.END, phase);

        phase = tm.advancePhase();
        assertEquals(TurnPhase.CLEANUP, phase);
    }

    @Test
    void testTurnNumberIncreases() {
        // setUp() already called startTurn() -> turnNumber = 1
        assertEquals(1, tm.getTurnNumber());

        // Advance through full turn to CLEANUP then switch
        for (int i = 0; i < 11; i++) tm.advancePhase();
        assertEquals(TurnPhase.CLEANUP, tm.getCurrentPhase());

        tm.advancePhase(); // Switches turn
        assertEquals(2, tm.getTurnNumber());
    }

    @Test
    void testStartTurn() {
        // startTurn already called in setUp (via startGame), so turnNumber is 1
        assertEquals(p1, tm.getActivePlayer());
        assertEquals(p2, tm.getNonActivePlayer());
        assertEquals(1, tm.getTurnNumber());
    }

    @Test
    void testSwitchTurn() {
        tm.startTurn(p1);
        Player originalActive = tm.getActivePlayer();

        tm.switchTurn();

        assertEquals(p2, tm.getActivePlayer());
        assertEquals(originalActive, tm.getNonActivePlayer());
    }

    @Test
    void testDeclareAttacker() {
        CreatureCard creature = makeCreature("Giant", 3, 3);
        creature.setController(p1);
        creature.untap();
        creature.clearSummoningSickness();
        bf.add(creature);

        tm.startTurn(p1);
        // Advance to DECLARE_ATTACKERS
        tm.advancePhase(); // UPKEEP
        tm.advancePhase(); // DRAW
        tm.advancePhase(); // MAIN1
        tm.advancePhase(); // COMBAT_START
        tm.advancePhase(); // DECLARE_ATTACKERS

        assertEquals(TurnPhase.DECLARE_ATTACKERS, tm.getCurrentPhase());

        boolean result = tm.canDeclareAttacker(creature);
        assertTrue(result);

        tm.declareAttacker(creature, p2);

        var attackers = tm.getAttackers();
        assertEquals(1, attackers.size());
        assertEquals(creature, attackers.get(0).creature);
        assertEquals(p2, attackers.get(0).target);
    }

    @Test
    void testCannotDeclareAttackerInWrongPhase() {
        CreatureCard creature = makeCreature("Giant", 3, 3);
        creature.setController(p1);
        bf.add(creature);

        tm.startTurn(p1);

        // In UPKEEP phase
        assertFalse(tm.canDeclareAttacker(creature));
    }

    @Test
    void testCannotAttackSummoningSick() {
        CreatureCard creature = makeCreature("New Creature", 3, 3);
        creature.setController(p1);
        bf.add(creature);

        // After creation, creature has summoning sickness by default
        assertTrue(creature.hasSummoningSickness());
        assertFalse(tm.canDeclareAttacker(creature));
    }

    @Test
    void testDeclareBlocker() {
        CreatureCard attacker = makeCreature("Attacker", 3, 3);
        attacker.setController(p1);
        attacker.clearSummoningSickness();
        bf.add(attacker);

        CreatureCard blocker = makeCreature("Blocker", 2, 2);
        blocker.setController(p2);
        bf.add(blocker);

        tm.startTurn(p1);
        // Advance to DECLARE_BLOCKERS
        for (int i = 0; i < 6; i++) tm.advancePhase();

        assertEquals(TurnPhase.DECLARE_BLOCKERS, tm.getCurrentPhase());

        boolean canBlock = tm.canDeclareBlocker(blocker);
        assertTrue(canBlock);

        tm.declareBlocker(blocker, attacker);

        var blockers = tm.getBlockers();
        assertEquals(1, blockers.size());
        assertEquals(blocker, blockers.get(0).blocker);
        assertEquals(attacker, blockers.get(0).attacker);
    }

    @Test
    void testEndCombatClearsAttackersAndBlockers() {
        CreatureCard attacker = makeCreature("Attacker", 3, 3);
        attacker.setController(p1);
        attacker.clearSummoningSickness();
        bf.add(attacker);

        CreatureCard blocker = makeCreature("Blocker", 2, 2);
        blocker.setController(p2);
        bf.add(blocker);

        // setUp() already called startTurn(), phase = UNTAAP
        for (int i = 0; i < 5; i++) tm.advancePhase(); // DECLARE_ATTACKERS
        assertEquals(TurnPhase.DECLARE_ATTACKERS, tm.getCurrentPhase());

        tm.declareAttacker(attacker, p2);

        tm.advancePhase(); // Move to DECLARE_BLOCKERS
        assertEquals(TurnPhase.DECLARE_BLOCKERS, tm.getCurrentPhase());

        tm.declareBlocker(blocker, attacker);

        assertEquals(1, tm.getAttackers().size());
        assertEquals(1, tm.getBlockers().size());

        tm.endCombat();

        assertEquals(0, tm.getAttackers().size());
        assertEquals(0, tm.getBlockers().size());
    }

    @Test
    void testBeginUntapStepUntapPermanents() {
        // setUp() already called startTurn() -> beginUntapStep(),
        // so existing creatures are already untapped.
        // Add a new tapped creature and verify beginUntapStep untaps it.
        CreatureCard creature = makeCreature("Tapped", 1, 1);
        creature.setController(p1);
        creature.tap();
        bf.add(creature);

        assertTrue(creature.isTapped()); // Tapped before untap step

        tm.beginUntapStep();

        assertFalse(creature.isTapped()); // Untapped during untap step
    }

    @Test
    void testGetters() {
        // startTurn already called in setUp (via startGame)
        assertEquals(p1, tm.getActivePlayer());
        assertEquals(p2, tm.getNonActivePlayer());
        assertEquals(TurnPhase.UNTAAP, tm.getCurrentPhase());
        assertEquals(1, tm.getTurnNumber());
        assertTrue(tm.getAttackers().isEmpty());
        assertTrue(tm.getBlockers().isEmpty());
    }
}
