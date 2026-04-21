package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Graveyard represents a player's discard pile.
 *
 * Rule 404.1: A player's graveyard is their discard pile. Any object that's
 * countered, discarded, destroyed, or sacrificed is put on top of its owner's
 * graveyard, as is any instant or sorcery spell that finishes resolving.
 * Rule 404.2: Each graveyard is kept in a single face-up pile. A player can
 * examine the cards in any graveyard at any time.
 * Rule 404.3: If an effect or rule puts two or more cards into the same
 * graveyard at the same time, the owner of those cards may arrange them in any order.
 */
public class Graveyard implements Zone {
    private final Player owner;
    private final List<Card> cards;  // Face-up

    public Graveyard(Player owner) {
        this.owner = owner;
        this.cards = new ArrayList<>();
    }

    /**
     * Add a card to the top of the graveyard.
     * Rule 404.1: Cards go on top of owner's graveyard.
     */
    public void add(Card card) {
        cards.add(card);
    }

    /**
     * Add multiple cards (Rule 404.3: owner may arrange order).
     */
    public void addAll(List<Card> newCards) {
        cards.addAll(newCards);
    }

    /**
     * Remove a specific card from the graveyard.
     */
    public boolean remove(Card card) {
        return cards.remove(card);
    }

    /**
     * Get the top card of the graveyard.
     */
    public Card getTop() {
        if (cards.isEmpty()) return null;
        return cards.get(cards.size() - 1);
    }

    /**
     * Get all cards in the graveyard.
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Check if graveyard is empty.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    // ========== Zone Implementation ==========

    @Override
    public String getName() {
        return owner.getName() + "'s Graveyard";
    }

    @Override
    public boolean isPublic() {
        return true;  // Public zone
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard()) {
            add(object.getCard());
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
    public int size() {
        return cards.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }
}
