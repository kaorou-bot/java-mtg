package com.mtg.effects;

import com.mtg.game.Game;
import com.mtg.model.Card;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * EffectManager handles replacement and prevention effects.
 *
 * Rule 614/615: These effects modify or prevent events.
 * The manager tracks active effects and applies them to game events.
 */
public class EffectManager {
    private Game game;
    private List<ReplacementEffect> replacementEffects;
    private List<PreventionEffect> preventionEffects;
    private List<ContinuousEffect> continuousEffects;

    public EffectManager(Game game) {
        this.game = game;
        this.replacementEffects = new ArrayList<>();
        this.preventionEffects = new ArrayList<>();
        this.continuousEffects = new ArrayList<>();
    }

    /**
     * Add a replacement effect.
     */
    public void addReplacementEffect(ReplacementEffect effect) {
        replacementEffects.add(effect);
    }

    /**
     * Remove a replacement effect.
     */
    public void removeReplacementEffect(ReplacementEffect effect) {
        replacementEffects.remove(effect);
    }

    /**
     * Add a prevention effect.
     */
    public void addPreventionEffect(PreventionEffect effect) {
        preventionEffects.add(effect);
    }

    /**
     * Remove a prevention effect.
     */
    public void removePreventionEffect(PreventionEffect effect) {
        preventionEffects.remove(effect);
    }

    /**
     * Add a continuous effect.
     */
    public void addContinuousEffect(ContinuousEffect effect) {
        continuousEffects.add(effect);
    }

    /**
     * Remove a continuous effect.
     */
    public void removeContinuousEffect(ContinuousEffect effect) {
        continuousEffects.remove(effect);
    }

    /**
     * Apply all applicable replacement effects to an event.
     */
    public GameEvent applyReplacementEffects(GameEvent event) {
        GameEvent result = event;

        // Sort by timestamp (earlier effects apply first)
        List<ReplacementEffect> applicable = new ArrayList<>();
        for (ReplacementEffect effect : replacementEffects) {
            if (effect.applies(result, game)) {
                applicable.add(effect);
            }
        }

        // Apply in order (Rule 616.1: check and apply one replacement effect at a time)
        for (ReplacementEffect effect : applicable) {
            GameEvent modified = effect.apply(result, game);
            if (modified == null) {
                // Event was replaced/cancelled
                return null;
            }
            result = modified;
        }

        return result;
    }

    /**
     * Apply all applicable prevention effects to an event.
     */
    public GameEvent applyPreventionEffects(GameEvent event) {
        GameEvent result = event;

        for (PreventionEffect effect : preventionEffects) {
            if (effect.applies(result, game)) {
                result = effect.apply(result, game);
                if (result == null) {
                    return null;
                }
            }
        }

        return result;
    }

    /**
     * Process a game event through all applicable effects.
     */
    public GameEvent processEvent(GameEvent event) {
        // First apply replacement effects
        GameEvent result = applyReplacementEffects(event);
        if (result == null) return null;

        // Then apply prevention effects
        result = applyPreventionEffects(result);
        return result;
    }

    /**
     * Clear all effects (used during cleanup).
     */
    public void clear() {
        replacementEffects.clear();
        preventionEffects.clear();
        continuousEffects.clear();
    }

    /**
     * Remove effects from a specific source card.
     */
    public void removeEffectsFromSource(Card source) {
        replacementEffects.removeIf(e -> e.getSourceCard() == source);
        preventionEffects.removeIf(e -> e.getSourceCard() == source);
        continuousEffects.removeIf(e -> e.getSourceCard() == source);
    }

    // ========== Getters ==========

    public List<ReplacementEffect> getReplacementEffects() {
        return new ArrayList<>(replacementEffects);
    }

    public List<PreventionEffect> getPreventionEffects() {
        return new ArrayList<>(preventionEffects);
    }

    public List<ContinuousEffect> getContinuousEffects() {
        return new ArrayList<>(continuousEffects);
    }
}

/**
 * ContinuousEffect represents a continuous effect according to Rule 611.
 *
 * Rule 611.1: A continuous effect modifies characteristics of objects,
 * modifies control of objects, or modifies rules of the game.
 *
 * Duration types:
 * - Until end of turn
 * - Until end of combat
 * - For as long as [condition]
 * - Indefinitely
 */
abstract class ContinuousEffect {
    protected String name;
    protected Card sourceCard;
    protected Player controller;
    protected EffectDuration duration;
    protected long startTime;
    protected long endTime;

    public enum EffectDuration {
        END_OF_TURN,
        END_OF_COMBAT,
        WHILE_ON_BATTLEFIELD,
        INDEFINITE
    }

    public ContinuousEffect(String name, Card sourceCard, Player controller, EffectDuration duration) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.duration = duration;
    }

    /**
     * Check if this effect is still active.
     */
    public boolean isActive() {
        if (duration == EffectDuration.INDEFINITE) {
            return sourceCard.getCurrentZone() != null &&
                   sourceCard.getCurrentZone().getName().equals("Battlefield");
        }
        return true;
    }

    /**
     * Apply this effect to modify game state or object characteristics.
     */
    public abstract void apply(Game game);

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public EffectDuration getDuration() {
        return duration;
    }
}