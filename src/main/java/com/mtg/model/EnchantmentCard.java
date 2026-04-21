package com.mtg.model;

import java.util.List;

public class EnchantmentCard extends PermanentCard {
    private String subtype;

    public EnchantmentCard(String name, ManaCost manaCost, String description,
                           List<ManaType> color, String subtype) {
        super(name, manaCost, description, CardType.ENCHANTMENT, color);
        this.subtype = subtype;
    }

    @Override
    public String getSubtypeName() {
        return "Enchantment";
    }

    public String getEnchantmentSubtype() {
        return subtype;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", getName(), getManaCost().toDisplayString(), getSubtypeName());
    }
}
