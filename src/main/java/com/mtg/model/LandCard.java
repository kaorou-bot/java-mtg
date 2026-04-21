package com.mtg.model;

import java.util.List;

public class LandCard extends PermanentCard {
    private ManaType producedMana;

    public LandCard(String name, List<ManaType> color, ManaType producedMana) {
        super(name, new ManaCost(), "Land - produces mana", CardType.LAND, color);
        this.producedMana = producedMana;
    }

    @Override
    public String getSubtypeName() {
        return "Land";
    }

    public ManaType getProducedMana() {
        return producedMana;
    }

    @Override
    public String toString() {
        return String.format("%s (produces %s)", getName(), producedMana.getName());
    }
}
