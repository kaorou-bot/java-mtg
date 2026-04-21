package com.mtg.model;

public enum CardType {
    LAND("Land"),
    CREATURE("Creature"),
    INSTANT("Instant"),
    SORCERY("Sorcery"),
    ENCHANTMENT("Enchantment"),
    ARTIFACT("Artifact"),
    PLANESWALKER("Planeswalker");

    private final String name;

    CardType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
