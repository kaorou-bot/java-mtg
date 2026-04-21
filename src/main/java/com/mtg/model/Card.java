package com.mtg.model;

import com.mtg.player.Player;
import com.mtg.zones.Zone;

import java.util.List;
import java.util.UUID;

public abstract class Card implements GameObject {
    protected String name;
    protected ManaCost manaCost;
    protected String description;
    protected CardType type;
    protected List<ManaType> color;
    protected String id;
    protected Player owner;
    protected Player controller;
    protected Zone currentZone;

    public Card(String name, ManaCost manaCost, String description, CardType type, List<ManaType> color) {
        this.name = name;
        this.manaCost = manaCost;
        this.description = description;
        this.type = type;
        this.color = color;
        this.id = UUID.randomUUID().toString();
    }

    // ========== GameObject Implementation ==========

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Player getController() {
        return controller;
    }

    @Override
    public void setController(Player controller) {
        this.controller = controller;
    }

    @Override
    public boolean isCard() {
        return true;
    }

    @Override
    public boolean isPermanent() {
        return currentZone != null && currentZone.getName().equals("Battlefield");
    }

    @Override
    public boolean isSpell() {
        return currentZone != null && currentZone.getName().equals("Stack");
    }

    @Override
    public Card getCard() {
        return this;
    }

    @Override
    public void onZoneChange(Zone newZone) {
        this.currentZone = newZone;
    }

    // ========== Card Properties ==========

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

    public Zone getCurrentZone() {
        return currentZone;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", name, manaCost.toDisplayString(), type.getName());
    }
}
