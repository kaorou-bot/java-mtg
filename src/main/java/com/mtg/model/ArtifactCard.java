package com.mtg.model;

import java.util.List;

public class ArtifactCard extends PermanentCard {
    private boolean isEquipment;
    private int bonusPower;
    private int bonusToughness;
    private CreatureCard attachedCreature;

    public ArtifactCard(String name, ManaCost manaCost, String description,
                        List<ManaType> color, boolean isEquipment) {
        super(name, manaCost, description, CardType.ARTIFACT, color);
        this.isEquipment = isEquipment;
        this.bonusPower = 0;
        this.bonusToughness = 0;
    }

    @Override
    public String getSubtypeName() {
        return "Artifact";
    }

    public boolean isEquipment() {
        return isEquipment;
    }

    public void attachTo(CreatureCard creature) {
        this.attachedCreature = creature;
        creature.modifyPower(bonusPower);
        creature.modifyToughness(bonusToughness);
    }

    public void detach() {
        if (attachedCreature != null) {
            attachedCreature.modifyPower(-bonusPower);
            attachedCreature.modifyToughness(-bonusToughness);
            attachedCreature = null;
        }
    }

    public CreatureCard getAttachedCreature() {
        return attachedCreature;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", getName(), getManaCost().toDisplayString(), getSubtypeName());
    }
}
