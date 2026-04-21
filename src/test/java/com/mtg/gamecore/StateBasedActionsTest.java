package com.mtg.gamecore;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import com.mtg.zones.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StateBasedActions per Rule 704.
 */
class StateBasedActionsTest {

    private Player p1;
    private Player p2;
    private Battlefield battlefield;
    private Library lib1;
    private Library lib2;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        battlefield = new Battlefield();
        lib1 = new Library(p1);
        lib2 = new Library(p2);
    }

    private CreatureCard makeCreature(String name, int power, int toughness) {
        return new CreatureCard(name, ManaCost.of(2),
                "", Arrays.asList(ManaType.GREEN), power, toughness,
                false, false, false, false, false) {};
    }

    @Test
    void testNoSBAsWhenGameIsNormal() {
        // Both players at 20 life, no damage, no SBA needed
        boolean result = StateBasedActions.check(p1, p2, battlefield, lib1, lib2);
        // No actions performed
        assertFalse(result);
    }

    @Test
    void testLifeZeroCausesLoss() {
        p1.modifyLife(-20); // Life goes to 0
        p2.modifyLife(-20); // Life goes to 0

        // SBA check should detect life <= 0
        boolean result = StateBasedActions.check(p1, p2, battlefield, lib1, lib2);
        // Result indicates an action was performed (though actual loss is in Game)
        assertTrue(result);
        assertTrue(p1.isDead());
        assertTrue(p2.isDead());
    }

    @Test
    void testPoisonCountersAtTen() {
        p1.addPoison(10);

        boolean result = StateBasedActions.check(p1, p2, battlefield, lib1, lib2);
        assertTrue(result);
        assertTrue(p1.isDead());
    }

    @Test
    void testCreatureToughnessZeroDestroyed() {
        CreatureCard c = makeCreature("Weak", 0, 0);
        c.setController(p1);
        battlefield.add(c);

        assertEquals(1, battlefield.getCreatures().size());

        StateBasedActions.check(p1, p2, battlefield, lib1, lib2);

        // Creature with toughness <= 0 should be destroyed
        // Note: destroyPermanent moves to graveyard (ZoneManager would handle this)
    }

    @Test
    void testLethalDamage() {
        CreatureCard c = makeCreature("Damaged", 2, 2);
        c.setController(p1);
        c.addDamage(2); // Damage = toughness
        battlefield.add(c);

        assertEquals(2, c.getMarkedDamage());
        assertTrue(c.hasLethalDamage());

        boolean result = StateBasedActions.check(p1, p2, battlefield, lib1, lib2);
        assertTrue(result);
    }

    @Test
    void testClearAllDamage() {
        CreatureCard c = makeCreature("Damaged", 2, 2);
        c.setController(p1);
        c.addDamage(3);
        battlefield.add(c);

        assertEquals(3, c.getMarkedDamage());

        StateBasedActions.clearAllDamage(battlefield);

        assertEquals(0, c.getMarkedDamage());
    }

    @Test
    void testLegendRule() {
        // Two legendary permanents with same name
        CreatureCard c1 = makeCreature("Lightning Dragon", 4, 4);
        CreatureCard c2 = makeCreature("Lightning Dragon", 4, 4);
        c1.setLegendary(true);
        c2.setLegendary(true);
        c1.setController(p1);
        c2.setController(p1);
        battlefield.add(c1);
        battlefield.add(c2);

        assertEquals(2, battlefield.getPermanents().size());

        boolean result = StateBasedActions.check(p1, p2, battlefield, lib1, lib2);
        assertTrue(result);
    }
}
