package com.mtg.model;

import java.util.List;

public class BattleCard extends PermanentCard {
    private int defense;
    private int currentDefense;
    private String subtype;

    public BattleCard(String name, ManaCost manaCost, String description,
                     List<ManaType> color, int defense, String subtype) {
        super(name, manaCost, description, CardType.PLANESWALKER, color);
        this.defense = defense;
        this.currentDefense = defense;
        this.subtype = subtype;
    }

    @Override
    public String getSubtypeName() {
        return "Battle";
    }

    public int getDefense() {
        return defense;
    }

    public int getCurrentDefense() {
        return currentDefense;
    }

    public void modifyDefense(int amount) {
        this.currentDefense += amount;
    }

    public boolean isDestroyed() {
        return currentDefense <= 0;
    }

    @Override
    public String toString() {
        return String.format("%s %s - Defense: %d", getName(), getManaCost().toDisplayString(), currentDefense);
    }
}
