package com.mtg.effects;

import com.mtg.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * GameEvent represents an event in the game that can be modified by effects.
 */
public class GameEvent {
    private String eventType;
    private Object source;  // Card, ability, or other source
    private Player player;   // Player affected
    private Object target;   // Target of the event
    private int amount;      // For damage, draw, etc.
    private Map<String, Object> properties;

    // Event types
    public static final String DAMAGE = "damage";
    public static final String DRAW = "draw";
    public static final String ENTER_BATTLEFIELD = "enterBattlefield";
    public static final String LEAVE_BATTLEFIELD = "leaveBattlefield";
    public static final String LIFE_CHANGE = "lifeChange";

    public GameEvent(String eventType, Object source, Player player) {
        this.eventType = eventType;
        this.source = source;
        this.player = player;
        this.amount = 0;
        this.properties = new HashMap<>();
    }

    public String getEventType() {
        return eventType;
    }

    public Object getSource() {
        return source;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public String toString() {
        return "GameEvent[" + eventType + " source=" + source + " player=" + player +
               " amount=" + amount + "]";
    }
}