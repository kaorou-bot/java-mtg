package com.mtg.model;

import java.util.List;

public class PlaneswalkerCard extends PermanentCard {
    private int loyalty;
    private int maxLoyalty;

    public PlaneswalkerCard(String name, ManaCost manaCost, String description,
                            List<ManaType> color, int startingLoyalty) {
        super(name, manaCost, description, CardType.PLANESWALKER, color);
        this.loyalty = startingLoyalty;
        this.maxLoyalty = startingLoyalty + 3;
    }

    @Override
    public String getSubtypeName() {
        return "Planeswalker";
    }

    public int getLoyalty() {
        return loyalty;
    }

    public void modifyLoyalty(int amount) {
        this.loyalty += amount;
    }

    public boolean isDestroyed() {
        return loyalty <= 0;
    }

    public void activateAbility1() {
        // Default implementation - can be overridden
    }

    public void activateAbility2() {
        // Default implementation - can be overridden
    }

    public void activateAbility3() {
        // Default implementation - can be overridden
    }

    @Override
    public String toString() {
        return String.format("%s %s - Loyalty: %d", getName(), getManaCost().toDisplayString(), loyalty);
    }
}
