package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ZoneManager and zone transitions.
 */
class ZoneManagerTest {

    private Game game;
    private Player p1;
    private Player p2;
    private ZoneManager zm;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        game = new Game("Alice", "Bob");
        zm = new ZoneManager(game, p1, p2);
    }

    private CreatureCard makeCreature(String name) {
        return new CreatureCard(name, ManaCost.of(2),
                "", Arrays.asList(ManaType.GREEN), 2, 2,
                false, false, false, false, false) {};
    }

    private LandCard makeLand(String name, ManaType produced) {
        return new LandCard(name, Arrays.asList(), produced);
    }

    private SpellCard makeInstant(String name) {
        return new SpellCard(name, ManaCost.of(1), "",
                Arrays.asList(ManaType.RED), SpellCard.SpellType.INSTANT);
    }

    @Test
    void testGetLibrary() {
        assertNotNull(zm.getLibrary(p1));
        assertNotNull(zm.getLibrary(p2));
        assertNotSame(zm.getLibrary(p1), zm.getLibrary(p2));
    }

    @Test
    void testGetHand() {
        assertNotNull(zm.getHand(p1));
        assertNotNull(zm.getHand(p2));
    }

    @Test
    void testGetGraveyard() {
        assertNotNull(zm.getGraveyard(p1));
        assertNotNull(zm.getGraveyard(p2));
    }

    @Test
    void testGetExile() {
        assertNotNull(zm.getExile(p1));
        assertNotNull(zm.getExile(p2));
    }

    @Test
    void testGetBattlefield() {
        assertNotNull(zm.getBattlefield());
    }

    @Test
    void testGetStack() {
        assertNotNull(zm.getStack());
        assertTrue(zm.getStack().isEmpty());
    }

    @Test
    void testDrawCard() {
        Card card = makeCreature("Test");
        card.setOwner(p1);
        card.setController(p1);
        zm.getLibrary(p1).addToTop(card);

        assertEquals(1, zm.getLibrary(p1).size());
        assertEquals(0, zm.getHand(p1).size());

        Card drawn = zm.drawCard(p1);

        assertNotNull(drawn);
        assertEquals("Test", drawn.getName());
        assertEquals(0, zm.getLibrary(p1).size());
        assertEquals(1, zm.getHand(p1).size());
    }

    @Test
    void testPutOnBattlefield() {
        CreatureCard creature = makeCreature("Giant");
        creature.setOwner(p1);
        creature.setController(p1);
        zm.getHand(p1).add(creature);

        assertEquals(0, zm.getBattlefield().size());

        zm.putOnBattlefield(creature, p1);

        assertEquals(1, zm.getBattlefield().size());
        assertFalse(creature.isTapped()); // Untapped when entering battlefield
    }

    @Test
    void testDestroy() {
        CreatureCard creature = makeCreature("Test");
        creature.setOwner(p1);
        creature.setController(p1);
        zm.putOnBattlefield(creature, p1);

        assertEquals(1, zm.getBattlefield().size());
        assertEquals(0, zm.getGraveyard(p1).size());

        zm.destroy(creature);

        assertEquals(0, zm.getBattlefield().size());
        assertEquals(1, zm.getGraveyard(p1).size());
    }

    @Test
    void testDiscard() {
        Card card = makeInstant("Shock");
        card.setOwner(p1);
        card.setController(p1);
        zm.getHand(p1).add(card);

        assertEquals(1, zm.getHand(p1).size());
        assertEquals(0, zm.getGraveyard(p1).size());

        zm.discard(card, p1);

        assertEquals(0, zm.getHand(p1).size());
        assertEquals(1, zm.getGraveyard(p1).size());
    }

    @Test
    void testCastSpell() {
        SpellCard spell = makeInstant("Lightning Bolt");
        spell.setOwner(p1);
        spell.setController(p1);
        zm.getHand(p1).add(spell);

        assertEquals(0, zm.getStack().size());

        zm.castSpell(spell, p1);

        assertEquals(0, zm.getHand(p1).size());
        assertEquals(1, zm.getStack().size());
    }

    @Test
    void testShuffleLibrary() {
        // Add ordered cards
        for (int i = 0; i < 5; i++) {
            Card c = makeCreature("C" + i);
            c.setOwner(p1);
            zm.getLibrary(p1).addToTop(c);
        }

        zm.shuffleLibrary(p1);

        // Order likely changed
        assertEquals(5, zm.getLibrary(p1).size());
    }

    @Test
    void testShuffleHandIntoLibrary() {
        Card c1 = makeCreature("In Hand");
        c1.setOwner(p1);
        c1.setController(p1);
        zm.getHand(p1).add(c1);

        assertEquals(1, zm.getHand(p1).size());
        assertEquals(0, zm.getLibrary(p1).size());

        zm.shuffleHandIntoLibrary(p1);

        assertEquals(0, zm.getHand(p1).size());
        assertEquals(1, zm.getLibrary(p1).size());
    }
}
