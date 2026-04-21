package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;
import com.mtg.game.Game;

import java.util.*;

/**
 * Library represents a player's deck - where cards are drawn from.
 *
 * Rule 401.1: When a game begins, each player's deck becomes their library.
 * Rule 401.2: Each library must be kept in a single face-down pile.
 * Rule 401.4: If an effect puts two or more cards in a specific position
 * in a library at the same time, the owner may arrange them in any order.
 * Rule 401.5: When instructed to play with the top card revealed, or to look
 * at it, if the top card changes during casting, the new card can't be looked at.
 * Rule 401.7: If putting a card "Nth from the top" but fewer than N cards exist,
 * the card goes on the bottom.
 *
 * Loss Condition: Rule 104.3c - If a player would draw more cards than remain
 * in their library, they draw the remaining cards and lose the game.
 */
public class Library implements Zone {
    private final Player owner;
    private final List<Card> cards;  // Face-down, top is index 0
    private boolean isPublic = false;  // Always hidden zone

    // Track cards that player is forced to draw (for loss condition)
    private int pendingLossDraws = 0;

    public Library(Player owner) {
        this.owner = owner;
        this.cards = new ArrayList<>();
    }

    /**
     * Rule 401.5: Draw a card from the top of the library.
     * @return The drawn card, or null if library is empty
     */
    public Card draw() {
        if (cards.isEmpty()) {
            // Rule 104.3c: No cards to draw - will lose when priority received
            pendingLossDraws++;
            return null;
        }
        Card card = cards.remove(0);
        card.onZoneChange(this);
        return card;
    }

    /**
     * Draw multiple cards.
     */
    public List<Card> draw(int n) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Card card = draw();
            if (card != null) {
                drawn.add(card);
            }
        }
        return drawn;
    }

    /**
     * Rule 401.5: Look at the top card of the library (if allowed).
     * Returns null if not allowed to look.
     */
    public Card lookAtTop() {
        if (cards.isEmpty()) return null;
        return cards.get(0);
    }

    /**
     * Add a card to the top of the library.
     */
    public void addToTop(Card card) {
        cards.add(0, card);
    }

    /**
     * Add a card to the bottom of the library.
     * Rule 401.7: When placing a card Nth from top but fewer than N cards exist.
     */
    public void addToBottom(Card card) {
        cards.add(card);
    }

    /**
     * Rule 401.7: Put a card at a specific position from the top.
     * Position 0 = top, position 1 = second from top, etc.
     * If position >= size, card goes to bottom.
     */
    public void putAtPosition(Card card, int position) {
        int actualPosition = Math.min(position, cards.size());
        cards.add(actualPosition, card);
    }

    /**
     * Shuffle the library - randomize card order.
     */
    public void shuffle() {
        Collections.shuffle(cards, new Random());
    }

    /**
     * Shuffle a specific list of cards into the library.
     */
    public void shuffleIn(List<Card> cardsToShuffle) {
        cards.addAll(cardsToShuffle);
        shuffle();
    }

    /**
     * Check if library is empty.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Get library size.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Check if there are pending draws that will cause loss.
     */
    public boolean hasPendingLossDraws() {
        return pendingLossDraws > 0;
    }

    /**
     * Clear pending loss draws (when game loss is processed).
     */
    public void clearPendingLossDraws() {
        pendingLossDraws = 0;
    }

    // ========== Zone Implementation ==========

    @Override
    public String getName() {
        return owner.getName() + "'s Library";
    }

    @Override
    public boolean isPublic() {
        return false;  // Hidden zone
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard()) {
            Card card = object.getCard();
            if (!cards.contains(card)) {
                cards.add(card);
            }
        }
    }

    @Override
    public GameObject remove(GameObject object) {
        if (object.isCard()) {
            Card card = object.getCard();
            if (cards.remove(card)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(card);
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        if (object.isCard()) {
            return cards.contains(object.getCard());
        }
        return false;
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        List<GameObject> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(card);
        }
        return result;
    }
}
