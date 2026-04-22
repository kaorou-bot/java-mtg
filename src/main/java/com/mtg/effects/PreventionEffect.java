package com.mtg.effects;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.game.Game;

/**
 * PreventionEffect represents a prevention effect according to Rule 615.
 *
 * Rule 615.1: Prevention effects are continuous effects.
 * They monitor for a particular event and prevent damage/draws/effects.
 *
 * Example: "Prevent all damage that target creature would deal this turn."
 */
public abstract class PreventionEffect {
    protected String name;
    protected Card sourceCard;
    protected Player controller;
    protected int amount;  // Amount to prevent (0 = all)
    protected String targetType;  // "creature", "player", "permanent"

    public PreventionEffect(String name, Card sourceCard, Player controller) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.amount = 0; // 0 means all
    }

    /**
     * Check if this prevention effect applies to the given event.
     */
    public abstract boolean applies(GameEvent event, Game game);

    /**
     * Apply this prevention effect.
     * Returns the modified event with prevention applied.
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}