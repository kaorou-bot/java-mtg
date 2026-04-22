package com.mtg.gamecore;

import com.mtg.game.Game;
import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import com.mtg.zones.ZoneManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StateBasedActions per Rule 704.
 */
class StateBasedActionsTest {

    private Game game;
    private Player p1;
    private Player p2;
    private ZoneManager zoneManager;
    private Battlefield battlefield;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        game = new Game("Alice", "Bob");
        game.startGame();
        zoneManager = game.getZoneManager();
        battlefield = zoneManager.getBattlefield();
    }

    private CreatureCard makeCreature(String name, int power, int toughness) {
        return new CreatureCard(name, ManaCost.of(2),
                "", Arrays.asList(ManaType.GREEN), power, toughness,
                false, false, false, false, false) {};
    }

    @Test
    void testNoSBAsWhenGameIsNormal() {
        StateBasedActions sba = new StateBasedActions(zoneManager);

        // Both players at 20 life, no damage, no SBA needed
        boolean result = sba.check(p1, p2, battlefield);
        // No actions performed
        assertFalse(result);
    }

    @Test
    void testLifeZeroCausesLoss() {
        StateBasedActions sba = new StateBasedActions(zoneManager);

        p1.modifyLife(-20); // Life goes to 0
        p2.modifyLife(-20); // Life goes to 0

        // SBA check should detect life <= 0
        boolean result = sba.check(p1, p2, battlefield);
        // Result indicates an action was performed (though actual loss is in Game)
        assertTrue(result);
        assertTrue(p1.isDead());
        assertTrue(p2.isDead());
    }

    @Test
    void testPoisonCountersAtTen() {
        StateBasedActions sba = new StateBasedActions(zoneManager);
        p1.addPoison(10);

        boolean result = sba.check(p1, p2, battlefield);
        assertTrue(result);
        assertTrue(p1.isDead());
    }

    @Test
    void testCreatureToughnessZeroDestroyed() {
        StateBasedActions sba = new StateBasedActions(zoneManager);

        CreatureCard c = makeCreature("Weak", 0, 0);
        c.setOwner(p1);
        c.setController(p1);
        battlefield.add(c);

        assertEquals(1, battlefield.getCreatures().size());

        sba.check(p1, p2, battlefield);

        // Creature with toughness <= 0 should be destroyed
        assertEquals(0, battlefield.getCreatures().size());
    }

    @Test
    void testLethalDamage() {
        StateBasedActions sba = new StateBasedActions(zoneManager);

        CreatureCard c = makeCreature("Damaged", 2, 2);
        c.setOwner(p1);
        c.setController(p1);
        c.addDamage(2); // Damage = toughness
        battlefield.add(c);

        assertEquals(2, c.getMarkedDamage());
        assertTrue(c.hasLethalDamage());

        boolean result = sba.check(p1, p2, battlefield);
        assertTrue(result);

        // Creature should be destroyed
        assertEquals(0, battlefield.getCreatures().size());
    }

    @Test
    void testClearAllDamage() {
        CreatureCard c = makeCreature("Damaged", 2, 2);
        c.setOwner(p1);
        c.setController(p1);
        c.addDamage(3);
        battlefield.add(c);

        assertEquals(3, c.getMarkedDamage());

        StateBasedActions.clearAllDamage(battlefield);

        assertEquals(0, c.getMarkedDamage());
    }

    @Test
    void testLegendRule() {
        StateBasedActions sba = new StateBasedActions(zoneManager);

        // Two legendary permanents with same name
        CreatureCard c1 = makeCreature("Lightning Dragon", 4, 4);
        CreatureCard c2 = makeCreature("Lightning Dragon", 4, 4);
        c1.setLegendary(true);
        c2.setLegendary(true);
        c1.setOwner(p1);
        c2.setOwner(p1);
        c1.setController(p1);
        c2.setController(p1);
        battlefield.add(c1);
        battlefield.add(c2);

        assertEquals(2, battlefield.getPermanents().size());

        boolean result = sba.check(p1, p2, battlefield);
        assertTrue(result);

        // One should be destroyed
        assertEquals(1, battlefield.getPermanents().size());
    }

    @Test
    void testMultipleSBAsInOneCheck() {
        StateBasedActions sba = new StateBasedActions(zoneManager);

        // Multiple SBA conditions
        p1.modifyLife(-20); // Life <= 0

        CreatureCard c1 = makeCreature("Weak", 0, 0);
        c1.setOwner(p1);
        c1.setController(p1);
        battlefield.add(c1);

        boolean result = sba.check(p1, p2, battlefield);
        assertTrue(result);
    }
}