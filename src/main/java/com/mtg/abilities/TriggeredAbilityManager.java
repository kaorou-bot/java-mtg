package com.mtg.abilities;

import com.mtg.game.Game;
import com.mtg.game.TurnPhase;
import com.mtg.model.Card;
import com.mtg.model.CreatureCard;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * TriggeredAbilityManager handles triggered abilities according to Rule 603.
 *
 * Rule 603.2: Abilities trigger at the beginning of each step/phase.
 * Rule 603.3: Each ability is checked separately and goes on the stack independently.
 * Rule 603.6: Abilities are put on the stack in APNAP order (Active Player first,
 * then Non-Active Player, and so on).
 */
public class TriggeredAbilityManager {
    private Game game;
    private Map<Player, List<TriggeredAbility>> abilityQueue;
    private Map<Card, List<TriggeredAbility>> registeredAbilities;

    public TriggeredAbilityManager(Game game) {
        this.game = game;
        this.abilityQueue = new HashMap<>();
        this.registeredAbilities = new HashMap<>();
    }

    /**
     * Register a triggered ability from a card.
     */
    public void registerAbility(TriggeredAbility ability) {
        Card source = ability.getSourceCard();
        registeredAbilities.computeIfAbsent(source, k -> new ArrayList<>()).add(ability);
    }

    /**
     * Remove abilities from a card that left the battlefield.
     */
    public void unregisterAbilitiesFromCard(Card card) {
        registeredAbilities.remove(card);
    }

    /**
     * Check for triggered abilities at the beginning of a phase/step.
     * Rule 603.2: Trigger conditions are checked continuously.
     * This is called at each step/phase transition.
     */
    public List<TriggeredAbility> checkTriggeredAbilities(TurnPhase phase, Player activePlayer) {
        List<TriggeredAbility> triggered = new ArrayList<>();

        // Check all registered abilities
        for (List<TriggeredAbility> abilities : registeredAbilities.values()) {
            for (TriggeredAbility ability : abilities) {
                // Reset memory for new trigger check
                if (ability instanceof OnPhaseBeginAbility) {
                    OnPhaseBeginAbility phaseAbility = (OnPhaseBeginAbility) ability;
                    if (phaseAbility.matchesPhase(phase)) {
                        triggered.add(ability);
                    }
                }
            }
        }

        // Sort by APNAP order (Rule 603.3d)
        sortByAPNAP(triggered, activePlayer);

        return triggered;
    }

    /**
     * Check for abilities triggered by zone changes.
     */
    public List<TriggeredAbility> checkZoneChangeAbilities(Card card, String fromZone, String toZone) {
        List<TriggeredAbility> triggered = new ArrayList<>();

        for (List<TriggeredAbility> abilities : registeredAbilities.values()) {
            for (TriggeredAbility ability : abilities) {
                if (ability instanceof OnZoneChangeAbility) {
                    OnZoneChangeAbility zoneAbility = (OnZoneChangeAbility) ability;
                    if (zoneAbility.matchesZoneChange(card, fromZone, toZone)) {
                        triggered.add(ability);
                    }
                }
            }
        }

        return triggered;
    }

    /**
     * Check for abilities triggered by damage dealt.
     */
    public List<TriggeredAbility> checkDamageAbilities(CreatureCard source, Player target, int amount) {
        List<TriggeredAbility> triggered = new ArrayList<>();

        for (List<TriggeredAbility> abilities : registeredAbilities.values()) {
            for (TriggeredAbility ability : abilities) {
                if (ability instanceof OnDamageDealtAbility) {
                    OnDamageDealtAbility damageAbility = (OnDamageDealtAbility) ability;
                    if (damageAbility.matchesDamage(source, target, amount)) {
                        triggered.add(ability);
                    }
                }
            }
        }

        return triggered;
    }

    /**
     * Put triggered abilities on the stack.
     */
    public void putOnStack(List<TriggeredAbility> abilities) {
        for (TriggeredAbility ability : abilities) {
            // Create a stack item for this triggered ability
            game.getZoneManager().getStack().pushAbility(ability);
        }
    }

    /**
     * Sort abilities by APNAP order.
     */
    private void sortByAPNAP(List<TriggeredAbility> abilities, Player activePlayer) {
        abilities.sort((a1, a2) -> {
            Player c1 = a1.getController();
            Player c2 = a2.getController();

            if (c1.equals(activePlayer) && !c2.equals(activePlayer)) {
                return -1;
            } else if (!c1.equals(activePlayer) && c2.equals(activePlayer)) {
                return 1;
            }
            return 0;
        });
    }

    /**
     * Clear all registered abilities.
     */
    public void clear() {
        registeredAbilities.clear();
        abilityQueue.clear();
    }

    // ========== Common Triggered Ability Types ==========

    /**
     * Ability that triggers at a specific phase.
     * Example: "At the beginning of your upkeep, draw a card."
     */
    public static abstract class OnPhaseBeginAbility extends TriggeredAbility {
        private TurnPhase triggerPhase;
        private String whosePhase; // "your", "each", "opponent's", etc.

        public OnPhaseBeginAbility(String name, Card source, Player controller,
                                   TurnPhase phase, String whosePhase) {
            super(name, source, controller);
            this.triggerPhase = phase;
            this.whosePhase = whosePhase;
        }

        public boolean matchesPhase(TurnPhase phase) {
            return this.triggerPhase == phase;
        }

        public TurnPhase getTriggerPhase() {
            return triggerPhase;
        }

        public String getWhosePhase() {
            return whosePhase;
        }
    }

    /**
     * Ability that triggers on zone change.
     * Example: "When Elvish Mystic enters the battlefield, add one mana of any color."
     */
    public static abstract class OnZoneChangeAbility extends TriggeredAbility {
        private String triggerZone; // "battlefield", "graveyard", etc.

        public OnZoneChangeAbility(String name, Card source, Player controller,
                                    String triggerZone) {
            super(name, source, controller);
            this.triggerZone = triggerZone;
        }

        public boolean matchesZoneChange(Card card, String fromZone, String toZone) {
            // Check if this ability triggered for this card and zone
            return card.equals(getSourceCard()) &&
                   triggerZone.equals(toZone);
        }

        public String getTriggerZone() {
            return triggerZone;
        }
    }

    /**
     * Ability that triggers when damage is dealt.
     * Example: "Whenever Goblin Electromancer deals damage to a player, draw a card."
     */
    public static abstract class OnDamageDealtAbility extends TriggeredAbility {
        public OnDamageDealtAbility(String name, Card source, Player controller) {
            super(name, source, controller);
        }

        public abstract boolean matchesDamage(CreatureCard source, Player target, int amount);
    }
}