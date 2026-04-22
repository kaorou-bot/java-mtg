package com.mtg.abilities;

import com.mtg.game.Game;
import com.mtg.game.TurnPhase;
import com.mtg.model.Card;
import com.mtg.model.CreatureCard;
import com.mtg.model.ManaCost;
import com.mtg.model.ManaType;
import com.mtg.player.Player;
import com.mtg.zones.ZoneManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TriggeredAbility and TriggeredAbilityManager.
 */
class TriggeredAbilityTest {

    private Game game;
    private Player p1;
    private Player p2;
    private TriggeredAbilityManager manager;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        game = new Game("Alice", "Bob");
        manager = new TriggeredAbilityManager(game);
    }

    @Test
    void testRegisterAndUnregisterAbility() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(1), "Test",
            Arrays.asList(ManaType.GREEN), 1, 1,
            false, false, false, false, false
        );

        TriggeredAbility ability = new TriggeredAbility("Draw Trigger", creature, p1) {
            @Override
            public boolean checkTrigger(Game game) {
                return false;
            }

            @Override
            public String getDescription() {
                return "Draw a card";
            }

            @Override
            public void execute(Game game) {
                // Draw a card
            }
        };

        // Register ability
        manager.registerAbility(ability);

        // Unregister when card leaves battlefield
        manager.unregisterAbilitiesFromCard(creature);

        // Ability should be removed
        List<TriggeredAbility> triggered = manager.checkTriggeredAbilities(TurnPhase.UPKEEP, p1);
        assertEquals(0, triggered.size());
    }

    @Test
    void testOnPhaseBeginAbility() {
        CreatureCard creature = new CreatureCard(
            "Upkeep Creature", ManaCost.of(2), "At beginning of upkeep, draw a card",
            Arrays.asList(ManaType.BLUE), 2, 2,
            false, false, false, false, false
        );

        TriggeredAbility ability = new TriggeredAbilityManager.OnPhaseBeginAbility(
            "Upkeep Draw", creature, p1, TurnPhase.UPKEEP, "your"
        ) {
            @Override
            public void execute(Game game) {
                // Draw card effect
            }

            @Override
            public String getDescription() {
                return "At the beginning of your upkeep, draw a card";
            }
        };

        manager.registerAbility(ability);

        // Check on correct phase
        List<TriggeredAbility> triggered = manager.checkTriggeredAbilities(TurnPhase.UPKEEP, p1);
        assertEquals(1, triggered.size());

        // Check on wrong phase
        triggered = manager.checkTriggeredAbilities(TurnPhase.DRAW, p1);
        assertEquals(0, triggered.size());
    }

    @Test
    void testOnDamageDealtAbility() {
        CreatureCard creature = new CreatureCard(
            "Damage Trigger", ManaCost.of(3), "Whenever deals damage, draw a card",
            Arrays.asList(ManaType.RED), 3, 3,
            false, false, false, false, false
        );

        TriggeredAbility ability = new TriggeredAbilityManager.OnDamageDealtAbility(
            "Damage Draw", creature, p1
        ) {
            @Override
            public boolean matchesDamage(CreatureCard source, Player target, int amount) {
                return source.equals(getSourceCard()) && amount > 0;
            }

            @Override
            public void execute(Game game) {
                // Draw effect
            }

            @Override
            public String getDescription() {
                return "Whenever deals damage, draw a card";
            }
        };

        manager.registerAbility(ability);

        List<TriggeredAbility> triggered = manager.checkDamageAbilities(creature, p2, 3);
        assertEquals(1, triggered.size());
    }

    @Test
    void testAPNAPOrdering() {
        CreatureCard c1 = new CreatureCard(
            "Creature 1", ManaCost.of(2), "Test",
            Arrays.asList(ManaType.GREEN), 1, 1,
            false, false, false, false, false
        );
        CreatureCard c2 = new CreatureCard(
            "Creature 2", ManaCost.of(2), "Test",
            Arrays.asList(ManaType.BLUE), 1, 1,
            false, false, false, false, false
        );

        TriggeredAbility ability1 = new TriggeredAbilityManager.OnPhaseBeginAbility(
            "Ability 1", c1, p1, TurnPhase.UPKEEP, "your"
        ) {
            @Override
            public void execute(Game game) {}

            @Override
            public String getDescription() { return "Test"; }
        };

        TriggeredAbility ability2 = new TriggeredAbilityManager.OnPhaseBeginAbility(
            "Ability 2", c2, p2, TurnPhase.UPKEEP, "your"
        ) {
            @Override
            public void execute(Game game) {}

            @Override
            public String getDescription() { return "Test"; }
        };

        manager.registerAbility(ability1);
        manager.registerAbility(ability2);

        List<TriggeredAbility> triggered = manager.checkTriggeredAbilities(TurnPhase.UPKEEP, p1);
        assertEquals(2, triggered.size());

        // Active player's ability should come first
        assertEquals(p1, triggered.get(0).getController());
        assertEquals(p2, triggered.get(1).getController());
    }
}