package com.mtg.model;

import java.util.List;

public class EnchantmentCard extends PermanentCard {
    private String subtype;
    private PermanentCard attachedTo;
    private boolean isAura;

    public EnchantmentCard(String name, ManaCost manaCost, String description,
                           List<ManaType> color, String subtype) {
        super(name, manaCost, description, CardType.ENCHANTMENT, color);
        this.subtype = subtype;
        this.isAura = subtype != null && (subtype.contains("Aura") || description.contains("Enchant"));
    }

    @Override
    public String getSubtypeName() {
        return subtype != null ? subtype : "Enchantment";
    }

    public String getEnchantmentSubtype() {
        return subtype;
    }

    public boolean isAura() {
        return isAura;
    }

    public void setAura(boolean isAura) {
        this.isAura = isAura;
    }

    public PermanentCard getAttachedTo() {
        return attachedTo;
    }

    public void attachTo(PermanentCard target) {
        this.attachedTo = target;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", getName(), getManaCost().toDisplayString(), getSubtypeName());
    }
}
