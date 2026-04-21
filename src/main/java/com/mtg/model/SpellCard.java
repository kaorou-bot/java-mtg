package com.mtg.model;

import java.util.List;

public class SpellCard extends Card {
    public enum SpellType {
        INSTANT("Instant"),
        SORCERY("Sorcery");

        private final String name;

        SpellType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private SpellType spellType;

    public SpellCard(String name, ManaCost manaCost, String description,
                    List<ManaType> color, SpellType spellType) {
        super(name, manaCost, description, spellType == SpellType.INSTANT ? CardType.INSTANT : CardType.SORCERY, color);
        this.spellType = spellType;
    }

    public SpellType getSpellType() {
        return spellType;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", name, manaCost.toDisplayString(), spellType.getName());
    }
}
