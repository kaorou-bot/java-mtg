package com.mtg.model;

import java.util.List;

public abstract class Card {
    protected String name;
    protected ManaCost manaCost;
    protected String description;
    protected CardType type;
    protected List<ManaType> color;

    public Card(String name, ManaCost manaCost, String description, CardType type, List<ManaType> color) {
        this.name = name;
        this.manaCost = manaCost;
        this.description = description;
        this.type = type;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ManaCost getManaCost() {
        return manaCost;
    }

    public String getDescription() {
        return description;
    }

    public CardType getType() {
        return type;
    }

    public List<ManaType> getColor() {
        return color;
    }

    public String getColorIndicator() {
        if (color == null || color.isEmpty()) {
            return "Colorless";
        }
        StringBuilder sb = new StringBuilder();
        for (ManaType c : color) {
            sb.append(c.getName());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", name, manaCost.toDisplayString(), type.getName());
    }
}
