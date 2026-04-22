package com.mtg.resolution;

import com.mtg.game.Game;
import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import com.mtg.zones.ZoneManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CardEffectResolver.
 */
class CardEffectResolverTest {

    private Game game;
    private Player p1;
    private Player p2;
    private ZoneManager zoneManager;
    private Battlefield battlefield;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice");
        p2 = new Player("Bob");
        p1.setOpponent(p2);
        p2.setOpponent(p1);

        game = new Game("Alice", "Bob");
        game.startGame();
        zoneManager = game.getZoneManager();
        battlefield = zoneManager.getBattlefield();
    }

    @Test
    void testDealDamageToCreature() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.GREEN), 3, 3,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);

        assertEquals(0, creature.getMarkedDamage());

        CardEffectResolver.dealDamageTo(null, game, p1, creature, 2);
        assertEquals(2, creature.getMarkedDamage());
    }

    @Test
    void testDealDamageToPlayer() {
        int initialLife = p2.getLife();

        CardEffectResolver.dealDamageTo(null, game, p1, p2, 3);
        assertEquals(initialLife - 3, p2.getLife());
    }

    @Test
    void testProduceBlackMana() {
        int initialMana = p1.getAvailableMana().size();

        CardEffectResolver.produceBlackMana(p1, 3);
        assertEquals(initialMana + 3, p1.getAvailableMana().size());
    }

    @Test
    void testBuffCreature() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.GREEN), 2, 2,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);

        assertEquals(2, creature.getCurrentPower());
        assertEquals(2, creature.getCurrentToughness());

        CardEffectResolver.buffTargetCreature(null, game, p1, 3, 3);
        assertEquals(5, creature.getCurrentPower());
        assertEquals(5, creature.getCurrentToughness());
    }

    @Test
    void testGainLifeAndDraw() {
        int initialLife = p1.getLife();
        int initialHandSize = zoneManager.getHand(p1).size();

        CardEffectResolver.gainLifeAndDraw(null, game, p1, 3, 1);
        assertEquals(initialLife + 3, p1.getLife());
        assertEquals(initialHandSize + 1, zoneManager.getHand(p1).size());
    }

    @Test
    void testTapPermanent() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.GREEN), 2, 2,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);

        assertFalse(creature.isTapped());

        CardEffectResolver.tapPermanent(creature);
        assertTrue(creature.isTapped());
    }

    @Test
    void testUntapPermanent() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.GREEN), 2, 2,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);
        creature.tap();

        assertTrue(creature.isTapped());

        CardEffectResolver.untapPermanent(creature);
        assertFalse(creature.isTapped());
    }

    @Test
    void testDestroyPermanent() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.GREEN), 2, 2,
            false, false, false, false, false
        );
        creature.setOwner(p1);
        creature.setController(p1);
        battlefield.add(creature);

        assertEquals(1, battlefield.getCreatures().size());

        CardEffectResolver.destroyPermanent(game, creature);
        assertEquals(0, battlefield.getCreatures().size());
    }

    @Test
    void testAddFlyingKeyword() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.GREEN), 2, 2,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);

        assertFalse(creature.hasFlying());

        CardEffectResolver.addKeywordUntilEndOfTurn(creature, "flying");
        assertTrue(creature.hasFlying());
    }

    @Test
    void testAddFirstStrikeKeyword() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(2), "",
            Arrays.asList(ManaType.WHITE), 2, 2,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);

        assertFalse(creature.hasFirstStrike());

        CardEffectResolver.addKeywordUntilEndOfTurn(creature, "first strike");
        assertTrue(creature.hasFirstStrike());
    }

    @Test
    void testAddTrampleKeyword() {
        CreatureCard creature = new CreatureCard(
            "Test Creature", ManaCost.of(3), "",
            Arrays.asList(ManaType.RED), 3, 3,
            false, false, false, false, false
        );
        creature.setController(p1);
        battlefield.add(creature);

        assertFalse(creature.hasTrample());

        CardEffectResolver.addKeywordUntilEndOfTurn(creature, "trample");
        assertTrue(creature.hasTrample());
    }
}