package com.mtg.model;

import java.util.List;

public class CreatureCard extends PermanentCard {
    private int power;
    private int toughness;
    private int currentPower;
    private int currentToughness;
    private int markedDamage;
    private boolean hasFirstStrike;
    private boolean hasDoubleStrike;
    private boolean hasFlying;
    private boolean hasVigilance;
    private boolean hasTrample;
    private boolean hasDeathtouch;
    private boolean hasHaste;

    public CreatureCard(String name, ManaCost manaCost, String description,
                        List<ManaType> color, int power, int toughness,
                        boolean hasFirstStrike, boolean hasDoubleStrike,
                        boolean hasFlying, boolean hasVigilance, boolean hasTrample) {
        super(name, manaCost, description, CardType.CREATURE, color);
        this.power = power;
        this.toughness = toughness;
        this.currentPower = power;
        this.currentToughness = toughness;
        this.markedDamage = 0;
        this.hasFirstStrike = hasFirstStrike;
        this.hasDoubleStrike = hasDoubleStrike;
        this.hasFlying = hasFlying;
        this.hasVigilance = hasVigilance;
        this.hasTrample = hasTrample;
        this.hasDeathtouch = false;
        this.hasHaste = false;
    }

    @Override
    public String getSubtypeName() {
        return "Creature";
    }

    public int getPower() {
        return power;
    }

    public int getToughness() {
        return currentToughness;
    }

    public int getBaseToughness() {
        return toughness;
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public int getCurrentToughness() {
        return currentToughness;
    }

    public int getMarkedDamage() {
        return markedDamage;
    }

    public void addDamage(int amount) {
        this.markedDamage += amount;
    }

    public void clearDamage() {
        this.markedDamage = 0;
    }

    public boolean canAttack() {
        return !isTapped() && (!hasSummoningSickness() || hasHaste);
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

    public boolean hasDeathtouch() {
        return hasDeathtouch;
    }

    public boolean hasHaste() {
        return hasHaste;
    }

    public void setHaste(boolean hasHaste) {
        this.hasHaste = hasHaste;
    }

    public void resetStats() {
        this.currentPower = power;
        this.currentToughness = toughness;
        this.markedDamage = 0;
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

    public boolean hasLethalDamage() {
        return markedDamage >= currentToughness;
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
        if (hasDeathtouch) sb.append("Deathtouch ");
        if (hasHaste) sb.append("Haste ");
        return sb.toString().trim();
    }
}
