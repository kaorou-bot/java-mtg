package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Battlefield zone per Rule 403.
 */
class BattlefieldTest {

    private Battlefield battlefield;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        battlefield = new Battlefield();
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);
    }

    private CreatureCard makeCreature(String name, int power, int toughness) {
        return new CreatureCard(name, ManaCost.of(2),
                "", Arrays.asList(ManaType.GREEN), power, toughness,
                false, false, false, false, false) {};
    }

    private LandCard makeLand(String name, ManaType produced) {
        return new LandCard(name, Arrays.asList(), produced);
    }

    @Test
    void testNewBattlefieldIsEmpty() {
        assertTrue(battlefield.isEmpty());
        assertEquals(0, battlefield.size());
    }

    @Test
    void testAddPermanent() {
        CreatureCard creature = makeCreature("Giant", 3, 3);
        creature.setController(p1);
        battlefield.add(creature);

        assertFalse(battlefield.isEmpty());
        assertEquals(1, battlefield.size());
        assertEquals(1, battlefield.getPermanents().size());
    }

    @Test
    void testRemovePermanent() {
        CreatureCard creature = makeCreature("Giant", 3, 3);
        battlefield.add(creature);

        assertTrue(battlefield.remove(creature));
        assertTrue(battlefield.isEmpty());

        assertFalse(battlefield.remove(creature)); // Already removed
    }

    @Test
    void testGetCreatures() {
        battlefield.add(makeCreature("C1", 1, 1));
        battlefield.add(makeCreature("C2", 2, 2));
        battlefield.add(makeLand("Forest", ManaType.GREEN));

        List<CreatureCard> creatures = battlefield.getCreatures();
        assertEquals(2, creatures.size());
    }

    @Test
    void testGetLands() {
        battlefield.add(makeLand("Forest", ManaType.GREEN));
        battlefield.add(makeLand("Mountain", ManaType.RED));
        battlefield.add(makeCreature("Giant", 3, 3));

        List<LandCard> lands = battlefield.getLands();
        assertEquals(2, lands.size());
    }

    @Test
    void testGetControlledBy() {
        CreatureCard c1 = makeCreature("P1 Creature", 1, 1);
        CreatureCard c2 = makeCreature("P2 Creature", 1, 1);
        c1.setController(p1);
        c2.setController(p2);

        battlefield.add(c1);
        battlefield.add(c2);

        assertEquals(1, battlefield.getControlledBy(p1).size());
        assertEquals(1, battlefield.getControlledBy(p2).size());
    }

    @Test
    void testGetUntapped() {
        CreatureCard c1 = makeCreature("Untapped", 1, 1);
        CreatureCard c2 = makeCreature("Tapped", 1, 1);
        c2.tap();

        battlefield.add(c1);
        battlefield.add(c2);

        List<PermanentCard> untapped = battlefield.getUntapped();
        assertEquals(1, untapped.size());
        assertEquals("Untapped", untapped.get(0).getName());
    }

    @Test
    void testGetByType() {
        battlefield.add(makeCreature("Creature", 1, 1));
        battlefield.add(makeLand("Land", ManaType.GREEN));

        assertEquals(1, battlefield.getByType(CardType.CREATURE).size());
        assertEquals(1, battlefield.getByType(CardType.LAND).size());
    }

    @Test
    void testGetContentsReturnsCopy() {
        battlefield.add(makeCreature("C1", 1, 1));
        List<PermanentCard> contents = battlefield.getPermanents();
        contents.clear();

        assertEquals(1, battlefield.size()); // Original unchanged
    }

    @Test
    void testZoneInterfaceMethods() {
        assertEquals("Battlefield", battlefield.getName());
        assertTrue(battlefield.isPublic()); // Public zone
        assertNull(battlefield.getOwner()); // Shared zone
    }

    @Test
    void testContains() {
        CreatureCard creature = makeCreature("Test", 1, 1);
        battlefield.add(creature);

        assertTrue(battlefield.contains(creature));
    }
}
