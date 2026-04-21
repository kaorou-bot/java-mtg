package com.mtg.model;

import java.util.List;

public abstract class PermanentCard extends Card {
    private boolean tapped;
    private boolean summoningSickness;

    public PermanentCard(String name, ManaCost manaCost, String description,
                         CardType type, List<ManaType> color) {
        super(name, manaCost, description, type, color);
        this.tapped = false;
        this.summoningSickness = true;
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

    public abstract String getSubtypeName();
}
