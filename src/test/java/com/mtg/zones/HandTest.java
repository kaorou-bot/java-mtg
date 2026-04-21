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
 * Tests for Hand zone per Rule 402.
 */
class HandTest {

    private Player player;
    private Hand hand;

    @BeforeEach
    void setUp() {
        player = new Player("Test Player");
        hand = new Hand(player);
    }

    private Card makeCard(String name) {
        return new Card(name, ManaCost.of(1), "", CardType.CREATURE,
                Arrays.asList(ManaType.WHITE)) {
        };
    }

    @Test
    void testNewHandIsEmpty() {
        assertTrue(hand.getCards().isEmpty());
        assertEquals(0, hand.size());
        assertFalse(hand.isFull());
    }

    @Test
    void testAddCard() {
        Card card = makeCard("Test Card");
        hand.add(card);

        assertEquals(1, hand.size());
        assertTrue(hand.getCards().contains(card));
    }

    @Test
    void testAddDuplicateDoesNotIncreaseSize() {
        Card card = makeCard("Test");
        hand.add(card);
        hand.add(card);

        assertEquals(1, hand.size());
    }

    @Test
    void testRemoveCard() {
        Card card = makeCard("Test");
        hand.add(card);
        hand.add(makeCard("Another"));

        hand.getCards().remove(card); // Remove from the copy
        assertEquals(2, hand.size()); // Original unchanged
    }

    @Test
    void testGetCardsReturnsCopy() {
        hand.add(makeCard("Card 1"));
        hand.add(makeCard("Card 2"));

        List<Card> cards = hand.getCards();
        cards.clear();

        assertEquals(2, hand.size()); // Original unchanged
    }

    @Test
    void testHandSizeLimit() {
        Hand smallHand = new Hand(player, 3);
        // Hand.add() does NOT enforce maxSize (per MTG rules 402.2,
        // excess cards are discarded during cleanup step)
        smallHand.add(makeCard("C1"));
        smallHand.add(makeCard("C2"));
        smallHand.add(makeCard("C3"));
        assertTrue(smallHand.isFull());

        smallHand.add(makeCard("C4"));
        assertEquals(4, smallHand.size()); // No limit enforcement on add()
    }

    @Test
    void testDiscardDownToMax() {
        Hand smallHand = new Hand(player, 3);
        smallHand.add(makeCard("C1"));
        smallHand.add(makeCard("C2"));
        smallHand.add(makeCard("C3"));
        smallHand.add(makeCard("C4"));
        smallHand.add(makeCard("C5"));

        List<Card> discarded = smallHand.discardDownToMax();

        assertEquals(3, smallHand.size());
        assertEquals(2, discarded.size());
    }

    @Test
    void testDiscardDownToMaxDoesNothingIfUnderLimit() {
        hand.add(makeCard("C1"));
        hand.add(makeCard("C2"));

        List<Card> discarded = hand.discardDownToMax();

        assertEquals(2, hand.size());
        assertTrue(discarded.isEmpty());
    }

    @Test
    void testMaxSize() {
        assertEquals(7, hand.getMaxSize());

        Hand custom = new Hand(player, 10);
        assertEquals(10, custom.getMaxSize());
    }

    @Test
    void testZoneInterfaceMethods() {
        assertEquals(player.getName() + "'s Hand", hand.getName());
        assertFalse(hand.isPublic()); // Hidden zone
        assertEquals(player, hand.getOwner());
    }

    @Test
    void testContains() {
        Card card = makeCard("Contained");
        hand.add(card);

        assertTrue(hand.contains(card));
        assertFalse(hand.contains(makeCard("Not There")));
    }

    @Test
    void testGetContentsSnapshot() {
        hand.add(makeCard("C1"));
        hand.add(makeCard("C2"));

        var snapshot = hand.getContentsSnapshot();
        assertEquals(2, snapshot.size());
    }
}
