package com.mtg.model;

import java.util.List;

public class CreatureCard extends PermanentCard {
    private int power;
    private int toughness;
    private int currentPower;
    private int currentToughness;
    private boolean hasFirstStrike;
    private boolean hasDoubleStrike;
    private boolean hasFlying;
    private boolean hasVigilance;
    private boolean hasTrample;

    public CreatureCard(String name, ManaCost manaCost, String description,
                        List<ManaType> color, int power, int toughness,
                        boolean hasFirstStrike, boolean hasDoubleStrike,
                        boolean hasFlying, boolean hasVigilance, boolean hasTrample) {
        super(name, manaCost, description, CardType.CREATURE, color);
        this.power = power;
        this.toughness = toughness;
        this.currentPower = power;
        this.currentToughness = toughness;
        this.hasFirstStrike = hasFirstStrike;
        this.hasDoubleStrike = hasDoubleStrike;
        this.hasFlying = hasFlying;
        this.hasVigilance = hasVigilance;
        this.hasTrample = hasTrample;
    }

    @Override
    public String getSubtypeName() {
        return "Creature";
    }

    public int getPower() {
        return power;
    }

    public int getToughness() {
        return toughness;
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public int getCurrentToughness() {
        return currentToughness;
    }

    public boolean canAttack() {
        return !isTapped() && !hasSummoningSickness();
    }

    public boolean hasFirstStrike() {
        return hasFirstStrike;
    }

    public boolean hasDoubleStrike() {
        return hasDoubleStrike;
    }

    public boolean hasFlying() {
        return hasFlying;
    }

    public boolean hasVigilance() {
        return hasVigilance;
    }

    public boolean hasTrample() {
        return hasTrample;
    }

    public void resetStats() {
        this.currentPower = power;
        this.currentToughness = toughness;
        untap();
    }

    public void resetForTurn() {
        this.currentPower = power;
        this.currentToughness = toughness;
    }

    public void modifyPower(int amount) {
        this.currentPower += amount;
    }

    public void modifyToughness(int amount) {
        this.currentToughness += amount;
    }

    public boolean isDestroyed() {
        return currentToughness <= 0;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %d/%d", getName(), getManaCost().toDisplayString(), currentPower, currentToughness);
    }

    public String getAbilitiesString() {
        StringBuilder sb = new StringBuilder();
        if (hasFirstStrike) sb.append("First Strike ");
        if (hasDoubleStrike) sb.append("Double Strike ");
        if (hasFlying) sb.append("Flying ");
        if (hasVigilance) sb.append("Vigilance ");
        if (hasTrample) sb.append("Trample ");
        return sb.toString().trim();
    }
}
