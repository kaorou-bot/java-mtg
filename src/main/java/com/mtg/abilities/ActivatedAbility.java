package com.mtg.abilities;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.model.ManaCost;
import com.mtg.game.Game;

/**
 * ActivatedAbility represents an activated ability according to Rule 602.
 *
 * Rule 602.1: Activated abilities have a cost and an effect.
 * [Cost]: [Effect]
 * They can be activated anytime a player has priority.
 *
 * Rule 602.2: A player may activate an activated ability they control.
 *
 * Rule 602.5: Activated abilities are put on the stack and can be responded to.
 */
public abstract class ActivatedAbility {
    protected String name;
    protected Card sourceCard;
    protected Player controller;
    protected ManaCost manaCost;
    protected Object[] additionalCosts; // e.g., tapping, sacrificing, etc.
    protected boolean isTapped;        // Requires tapping the source
    protected boolean isSacrificed;     // Requires sacrificing the source
    protected boolean isActivated;

    public ActivatedAbility(String name, Card sourceCard, Player controller) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.isActivated = false;
    }

    /**
     * Set the mana cost of this ability.
     */
    public void setManaCost(ManaCost cost) {
        this.manaCost = cost;
    }

    /**
     * Set that this ability requires tapping the source.
     */
    public void requiresTapping(boolean tapped) {
        this.isTapped = tapped;
    }

    /**
     * Set that this ability requires sacrificing the source.
     */
    public void requiresSacrifice(boolean sacrificed) {
        this.isSacrificed = sacrificed;
    }

    /**
     * Check if the ability can be activated.
     */
    public boolean canActivate(Game game) {
        // Check if source is on battlefield
        if (sourceCard.getCurrentZone() == null ||
            !sourceCard.getCurrentZone().getName().equals("Battlefield")) {
            return false;
        }

        // Check if source is tapped (if required)
        if (isTapped && sourceCard instanceof com.mtg.model.PermanentCard) {
            if (((com.mtg.model.PermanentCard) sourceCard).isTapped()) {
                return false;
            }
        }

        // Check mana cost
        if (manaCost != null && !controller.canPayCost(manaCost)) {
            return false;
        }

        // Check additional activation requirements
        if (!canPayAdditionalCosts(game)) {
            return false;
        }

        return true;
    }

    /**
     * Check if additional costs can be paid.
     * Override for abilities with additional costs like sacrificing.
     */
    protected boolean canPayAdditionalCosts(Game game) {
        return true;
    }

    /**
     * Activate this ability - pay costs and put on stack.
     */
    public boolean activate(Game game) {
        if (!canActivate(game)) {
            return false;
        }

        // Pay mana cost
        if (manaCost != null) {
            controller.payMana(manaCost);
        }

        // Pay additional costs
        payAdditionalCosts(game);

        // Tap source if required
        if (isTapped && sourceCard instanceof com.mtg.model.PermanentCard) {
            ((com.mtg.model.PermanentCard) sourceCard).tap();
        }

        // Put on stack
        game.getZoneManager().getStack().pushAbility(this);
        isActivated = true;

        return true;
    }

    /**
     * Pay additional costs (override in subclasses).
     */
    protected void payAdditionalCosts(Game game) {
        // Override for sacrifice or other additional costs
    }

    /**
     * Execute the effect of this ability.
     */
    public abstract void execute(Game game);

    /**
     * Get the description of this ability.
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

    public ManaCost getManaCost() {
        return manaCost;
    }

    public boolean isTappedRequired() {
        return isTapped;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}