package com.mtg.effects;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.game.Game;

/**
 * ReplacementEffect represents a replacement effect according to Rule 614.
 *
 * Rule 614.1: Some continuous effects are replacement effects.
 * They monitor for a particular event and modify or replace it.
 *
 * Rule 614.12: A replacement effect "instead" replaces an event with one or more
 * other events.
 *
 * Example: "If a source would deal damage to you, prevent 1 of that damage."
 */
public abstract class ReplacementEffect {
    protected String name;
    protected Card sourceCard;
    protected Player controller;
    protected String eventType;  // "damage", "draw", "enterBattlefield", etc.
    protected boolean isReplacement; // true = replace, false = prevent

    public ReplacementEffect(String name, Card sourceCard, Player controller, String eventType) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.eventType = eventType;
        this.isReplacement = true;
    }

    /**
     * Check if this replacement effect applies to the given event.
     */
    public abstract boolean applies(GameEvent event, Game game);

    /**
     * Apply this replacement effect to modify the event.
     * Returns the modified event (or null to cancel).
     */
    public abstract GameEvent apply(GameEvent event, Game game);

    /**
     * Get the description of what this effect does.
     */
    public abstract String getDescription();

    // ========== Getters ==========

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isReplacement() {
        return isReplacement;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}