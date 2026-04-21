package com.mtg.model;

public enum ManaType {
    WHITE("W", "White"),
    BLUE("U", "Blue"),
    BLACK("B", "Black"),
    RED("R", "Red"),
    GREEN("G", "Green"),
    COLORLESS("C", "Colorless");

    private final String symbol;
    private final String name;

    ManaType(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}
