package com.mtg.model;

import java.util.List;

public abstract class PermanentCard extends Card {
    private boolean tapped;
    private boolean summoningSickness;
    private boolean legendary;

    public PermanentCard(String name, ManaCost manaCost, String description,
                         CardType type, List<ManaType> color) {
        super(name, manaCost, description, type, color);
        this.tapped = false;
        this.summoningSickness = true;
        this.legendary = name != null && name.contains("Legendary");
    }

    public boolean isTapped() {
        return tapped;
    }

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        this.tapped = false;
    }

    public boolean hasSummoningSickness() {
        return summoningSickness;
    }

    public void clearSummoningSickness() {
        this.summoningSickness = false;
    }

    public void resetTurn() {
        this.tapped = false;
        this.summoningSickness = false;
    }

    public boolean isLegendary() {
        return legendary;
    }

    public void setLegendary(boolean legendary) {
        this.legendary = legendary;
    }

    public abstract String getSubtypeName();
}
