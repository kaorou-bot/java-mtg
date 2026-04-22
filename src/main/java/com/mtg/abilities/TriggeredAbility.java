package com.mtg.abilities;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.game.Game;

/**
 * TriggeredAbility represents a triggered ability according to Rule 603.
 *
 * Rule 603.1: Triggered abilities have a trigger condition and an effect.
 * When the trigger condition occurs, the ability triggers and goes on the stack.
 *
 * Rule 603.2: Triggered abilities only check the game state at the moment
 * the trigger condition occurs (unless the ability is a delayed triggered ability).
 */
public abstract class TriggeredAbility {
    protected String name;
    protected Card sourceCard;
    protected Player controller;
    protected boolean isOptional;
    protected String memoryNote;

    public TriggeredAbility(String name, Card sourceCard, Player controller) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.isOptional = false;
    }

    /**
     * Check if this ability's trigger condition is met.
     * Default implementation returns false. Override for specific trigger checks.
     */
    public boolean checkTrigger(Game game) {
        return false;
    }

    /**
     * Get the description of what this ability does.
     */
    public abstract String getDescription();

    /**
     * Execute the effect of this ability.
     */
    public abstract void execute(Game game);

    /**
     * Set memory note for abilities that track information.
     */
    public void setMemory(String note) {
        this.memoryNote = note;
    }

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        this.isOptional = optional;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}