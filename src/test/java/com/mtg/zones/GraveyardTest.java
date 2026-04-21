package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Graveyard zone per Rule 404.
 */
class GraveyardTest {

    private Player player;
    private Graveyard graveyard;

    @BeforeEach
    void setUp() {
        player = new Player("Test Player");
        graveyard = new Graveyard(player);
    }

    private Card makeCard(String name) {
        return new Card(name, ManaCost.of(1), "", CardType.INSTANT,
                Arrays.asList(ManaType.BLUE)) {};
    }

    @Test
    void testNewGraveyardIsEmpty() {
        assertTrue(graveyard.isEmpty());
        assertEquals(0, graveyard.size());
    }

    @Test
    void testAddCard() {
        Card card = makeCard("Test Card");
        graveyard.add(card);

        assertFalse(graveyard.isEmpty());
        assertEquals(1, graveyard.size());
    }

    @Test
    void testAddAll() {
        graveyard.add(makeCard("C1"));
        graveyard.add(makeCard("C2"));
        graveyard.add(makeCard("C3"));

        assertEquals(3, graveyard.size());
    }

    @Test
    void testRemoveCard() {
        Card card = makeCard("Test");
        graveyard.add(card);

        assertTrue(graveyard.remove(card));
        assertTrue(graveyard.isEmpty());

        assertFalse(graveyard.remove(card)); // Already removed
    }

    @Test
    void testGetTop() {
        graveyard.add(makeCard("First"));
        graveyard.add(makeCard("Second"));
        graveyard.add(makeCard("Top"));

        // Most recently added is on top (index = size-1)
        Card top = graveyard.getTop();
        assertNotNull(top);
        assertEquals("Top", top.getName());
    }

    @Test
    void testGetTopOfEmptyGraveyard() {
        assertNull(graveyard.getTop());
    }

    @Test
    void testGetCards() {
        graveyard.add(makeCard("C1"));
        graveyard.add(makeCard("C2"));

        var cards = graveyard.getCards();
        assertEquals(2, cards.size());
    }

    @Test
    void testZoneInterfaceMethods() {
        assertEquals(player.getName() + "'s Graveyard", graveyard.getName());
        assertTrue(graveyard.isPublic()); // Rule 404.2: any player can examine
        assertEquals(player, graveyard.getOwner());
    }
}
