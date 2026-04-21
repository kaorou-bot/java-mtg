package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Exile represents the exile zone - a holding area for objects.
 *
 * Rule 406.1: The exile zone is essentially a holding area for objects.
 * Some spells and abilities exile an object without any way to return that
 * object to another zone. Other spells and abilities exile an object only temporarily.
 * Rule 406.2: To exile an object is to put it into the exile zone.
 * Rule 406.3: Exiled cards are kept face up by default and may be examined
 * by any player at any time. Cards exiled face down cannot be examined
 * except when instructions allow it.
 * Rule 406.8: Previously called the "removed-from-the-game zone."
 */
public class Exile implements Zone {
    private final List<ExileEntry> entries;

    public Exile() {
        this.entries = new ArrayList<>();
    }

    /**
     * Exile a card.
     */
    public void exile(Card card) {
        exile(card, null, false);
    }

    /**
     * Exile a card with metadata.
     * @param card The card to exile
     * @param reason The reason for exiling (e.g., "exiled by Shadowborn Demon")
     * @param faceDown Whether the card is exiled face down
     */
    public void exile(Card card, String reason, boolean faceDown) {
        ExileEntry entry = new ExileEntry(card, reason, faceDown);
        entries.add(entry);
    }

    /**
     * Get all exile entries.
     */
    public List<ExileEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Get all face-up exiled cards.
     * Rule 406.3: Face-up exiled cards may be examined by any player.
     */
    public List<Card> getFaceUpCards() {
        List<Card> faceUp = new ArrayList<>();
        for (ExileEntry entry : entries) {
            if (!entry.faceDown) {
                faceUp.add(entry.card);
            }
        }
        return faceUp;
    }

    /**
     * Remove a card from exile.
     */
    public boolean remove(Card card) {
        return entries.removeIf(entry -> entry.card.equals(card));
    }

    /**
     * Get the exile entry for a card.
     */
    public ExileEntry getEntry(Card card) {
        for (ExileEntry entry : entries) {
            if (entry.card.equals(card)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Check if exile is empty.
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Represents an entry in the exile zone.
     */
    public static class ExileEntry {
        public final Card card;
        public final String exileReason;
        public final boolean faceDown;
        public final String id;

        public ExileEntry(Card card, String exileReason, boolean faceDown) {
            this.card = card;
            this.exileReason = exileReason;
            this.faceDown = faceDown;
            this.id = UUID.randomUUID().toString();
        }

        public boolean isFaceDown() {
            return faceDown;
        }

        public String getExileReason() {
            return exileReason;
        }

        public Card getCard() {
            return card;
        }
    }

    // ========== Zone Implementation ==========

    @Override
    public String getName() {
        return "Exile";
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public Player getOwner() {
        return null;  // Shared zone
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard()) {
            exile(object.getCard());
        }
    }

    @Override
    public GameObject remove(GameObject object) {
        if (object.isCard()) {
            Card card = object.getCard();
            if (remove(card)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (ExileEntry entry : entries) {
            if (!entry.faceDown) {
                result.add(entry.card);
            }
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        if (object.isCard()) {
            for (ExileEntry entry : entries) {
                if (entry.card.equals(object.getCard())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }
}
