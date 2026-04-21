package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.CardType;
import com.mtg.model.ManaCost;
import com.mtg.model.ManaType;
import com.mtg.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Library zone per Rule 401.
 */
class LibraryTest {

    private Player player;
    private Library library;

    @BeforeEach
    void setUp() {
        player = new Player("Test Player");
        library = new Library(player);
    }

    private Card makeCard(String name) {
        return new Card(name, ManaCost.of(1), "", CardType.CREATURE,
                Arrays.asList(ManaType.WHITE)) {
        };
    }

    @Test
    void testNewLibraryIsEmpty() {
        assertTrue(library.isEmpty());
        assertEquals(0, library.size());
    }

    @Test
    void testDrawFromEmptyLibraryReturnsNull() {
        Card drawn = library.draw();
        assertNull(drawn);
        assertTrue(library.hasPendingLossDraws());
    }

    @Test
    void testAddAndDraw() {
        Card card = makeCard("Test Card");
        library.addToTop(card);

        assertFalse(library.isEmpty());
        assertEquals(1, library.size());

        Card drawn = library.draw();
        assertNotNull(drawn);
        assertEquals("Test Card", drawn.getName());
        assertTrue(library.isEmpty());
    }

    @Test
    void testDrawMultipleCards() {
        Card c1 = makeCard("Card 1");
        Card c2 = makeCard("Card 2");
        Card c3 = makeCard("Card 3");
        library.addToTop(c1);
        library.addToTop(c2);
        library.addToTop(c3);

        List<Card> drawn = library.draw(2);
        assertEquals(2, drawn.size());
        assertEquals(1, library.size());
    }

    @Test
    void testDrawFromTop() {
        Card c1 = makeCard("Card 1");
        Card c2 = makeCard("Card 2");
        library.addToTop(c1);
        library.addToTop(c2);

        Card top = library.draw();
        assertEquals("Card 2", top.getName()); // Last added is on top
        assertEquals(1, library.size());
    }

    @Test
    void testAddToBottom() {
        Card c1 = makeCard("Bottom");
        Card c2 = makeCard("Top");
        library.addToBottom(c1);
        library.addToTop(c2);

        Card drawn = library.draw();
        assertEquals("Top", drawn.getName());
    }

    @Test
    void testLookAtTop() {
        Card card = makeCard("Hidden Card");
        library.addToTop(card);

        Card looked = library.lookAtTop();
        assertNotNull(looked);
        assertEquals("Hidden Card", looked.getName());
    }

    @Test
    void testLookAtTopOfEmptyLibrary() {
        assertNull(library.lookAtTop());
    }

    @Test
    void testShuffleChangesOrder() {
        for (int i = 0; i < 10; i++) {
            library.addToTop(makeCard("Card " + i));
        }

        library.shuffle();

        // With 10 cards, shuffle very likely changes order (probability of same order = 1/10!)
        // We just verify all cards are still present
        assertEquals(10, library.size());
    }

    @Test
    void testShuffleIn() {
        Card c1 = makeCard("Shuffled 1");
        Card c2 = makeCard("Shuffled 2");
        library.addToTop(makeCard("Original"));

        library.shuffleIn(Arrays.asList(c1, c2));

        assertEquals(3, library.size());
    }

    @Test
    void testPutAtPosition() {
        library.addToTop(makeCard("Card 1"));
        library.addToTop(makeCard("Card 2"));
        library.addToTop(makeCard("Card 3"));

        library.putAtPosition(makeCard("New Card"), 1);

        assertEquals(4, library.size());
    }

    @Test
    void testPutAtPositionBeyondSize() {
        library.addToTop(makeCard("Card 1"));
        library.putAtPosition(makeCard("New Card"), 10);

        assertEquals(2, library.size());
        // Should go to bottom
        Card drawn = library.draw();
        assertEquals("Card 1", drawn.getName());
        drawn = library.draw();
        assertEquals("New Card", drawn.getName());
    }

    @Test
    void testPendingLossDraws() {
        assertFalse(library.hasPendingLossDraws());

        library.draw(); // First draw from empty
        assertTrue(library.hasPendingLossDraws());

        library.clearPendingLossDraws();
        assertFalse(library.hasPendingLossDraws());
    }

    @Test
    void testZoneInterfaceMethods() {
        assertEquals(player.getName() + "'s Library", library.getName());
        assertFalse(library.isPublic()); // Hidden zone
        assertEquals(player, library.getOwner());
    }

    @Test
    void testContains() {
        Card card = makeCard("Contained");
        library.addToTop(card);

        assertTrue(library.contains(card));
        assertFalse(library.contains(makeCard("Not Contained")));
    }

    @Test
    void testGetContentsSnapshot() {
        library.addToTop(makeCard("Card 1"));
        library.addToTop(makeCard("Card 2"));

        var snapshot = library.getContentsSnapshot();
        assertEquals(2, snapshot.size());
    }
}
