package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.zones.Exile.ExileEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Exile zone per Rule 406.
 */
class ExileTest {

    private Exile exile;

    @BeforeEach
    void setUp() {
        exile = new Exile();
    }

    private Card makeCard(String name) {
        return new Card(name, ManaCost.of(1), "", CardType.INSTANT,
                Arrays.asList(ManaType.BLUE)) {};
    }

    @Test
    void testNewExileIsEmpty() {
        assertTrue(exile.isEmpty());
    }

    @Test
    void testExileCard() {
        Card card = makeCard("Test Card");
        exile.exile(card);

        assertFalse(exile.isEmpty());
        assertEquals(1, exile.getEntries().size());
    }

    @Test
    void testExileWithMetadata() {
        Card card = makeCard("Hidden Card");
        exile.exile(card, "Temporal Manipulation", true);

        List<ExileEntry> entries = exile.getEntries();
        assertEquals(1, entries.size());

        ExileEntry entry = entries.get(0);
        assertEquals("Temporal Manipulation", entry.getExileReason());
        assertTrue(entry.isFaceDown());
    }

    @Test
    void testGetFaceUpCards() {
        Card c1 = makeCard("Face Up");
        Card c2 = makeCard("Face Down");

        exile.exile(c1);
        exile.exile(c2, "Effect", true);

        List<Card> faceUp = exile.getFaceUpCards();
        assertEquals(1, faceUp.size());
        assertEquals("Face Up", faceUp.get(0).getName());
    }

    @Test
    void testGetEntry() {
        Card card = makeCard("Test");
        exile.exile(card, "Cast", false);

        ExileEntry entry = exile.getEntry(card);
        assertNotNull(entry);
        assertEquals("Cast", entry.getExileReason());
    }

    @Test
    void testRemove() {
        Card card = makeCard("Test");
        exile.exile(card);

        assertTrue(exile.remove(card));
        assertTrue(exile.isEmpty());

        assertFalse(exile.remove(card)); // Already removed
    }

    @Test
    void testZoneInterfaceMethods() {
        assertEquals("Exile", exile.getName());
        assertTrue(exile.isPublic()); // Public zone
        assertNull(exile.getOwner()); // Shared zone
    }
}
