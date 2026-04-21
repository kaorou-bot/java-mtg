package com.mtg.model;

import com.mtg.player.Player;
import com.mtg.zones.Zone;

/**
 * GameObject represents any object that can exist in a zone during the game.
 * This includes cards, tokens, spells on the stack, abilities on the stack, and emblems.
 *
 * Rule 109.1: An object is an ability on the stack, a card, a copy of a card,
 * a token, a spell, a permanent, or an emblem.
 */
public interface GameObject {

    /**
     * Get the unique identifier for this object.
     * Used to track objects across zone transitions (Rule 400.7).
     */
    String getId();

    /**
     * Get the owner of this object.
     * Rule 108.3: The owner of a card in the game is the player who started
     * the game with it in their deck.
     */
    Player getOwner();

    /**
     * Get the current controller of this object.
     * Rule 109.4: Only objects on the stack or battlefield have a controller.
     */
    Player getController();

    /**
     * Set the controller of this object.
     */
    void setController(Player controller);

    /**
     * Check if this object is a card (has a physical representation).
     */
    boolean isCard();

    /**
     * Check if this object is a permanent (on the battlefield).
     */
    boolean isPermanent();

    /**
     * Check if this object is a spell (on the stack).
     */
    boolean isSpell();

    /**
     * Get the card representation if this object is a card.
     * Returns null if this is not a card-based object.
     */
    Card getCard();

    /**
     * Called when this object moves to a new zone.
     * Rule 400.7: Object becomes a new object with no memory of previous existence.
     * Exceptions are handled in ZoneManager.
     */
    void onZoneChange(Zone newZone);
}
