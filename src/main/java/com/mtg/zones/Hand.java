package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand represents a player's hand of cards.
 *
 * Rule 402.1: The hand is where a player holds cards that have been drawn.
 * Rule 402.2: Each player has a maximum hand size, normally seven.
 * A player may have any number of cards in hand, but must discard down to
 * maximum hand size during the cleanup step.
 * Rule 402.3: A player may arrange their hand in any convenient fashion
 * and look at it at any time. A player can't look at another player's hand.
 */
public class Hand implements Zone {
    private final Player owner;
    private final List<Card> cards;
    private final int maxSize;

    public Hand(Player owner) {
        this(owner, 7);
    }

    public Hand(Player owner, int maxSize) {
        this.owner = owner;
        this.cards = new ArrayList<>();
        this.maxSize = maxSize;
    }

    /**
     * Add a card to hand.
     * Rule 402.1/402.2: Player may have any number of cards, but must
     * discard down to max hand size during cleanup (Rule 514.1).
     */
    public void add(Card card) {
        if (!cards.contains(card)) {
            cards.add(card);
        }
    }

    /**
     * Rule 402.2: Discard down to max hand size.
     * Returns list of discarded cards.
     */
    public List<Card> discardDownToMax() {
        List<Card> discarded = new ArrayList<>();
        while (cards.size() > maxSize) {
            discarded.add(cards.remove(cards.size() - 1));
        }
        return discarded;
    }

    /**
     * Check if hand is full.
     */
    public boolean isFull() {
        return cards.size() >= maxSize;
    }

    /**
     * Get maximum hand size.
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get a copy of cards in hand.
     * Rule 402.3: Player can look at their hand at any time.
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    // ========== Zone Implementation ==========

    @Override
    public String getName() {
        return owner.getName() + "'s Hand";
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
