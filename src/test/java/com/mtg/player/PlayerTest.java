package com.mtg.player;

import com.mtg.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Player class.
 */
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Test Player");
    }

    private CreatureCard makeCreature(String name, int power, int toughness) {
        return new CreatureCard(name, ManaCost.of(2),
                "", Arrays.asList(ManaType.GREEN), power, toughness,
                false, false, false, false, false) {};
    }

    @Test
    void testInitialState() {
        assertEquals("Test Player", player.getName());
        assertEquals(20, player.getLife());
        assertEquals(0, player.getPoisonCounters());
        assertFalse(player.isDead());
    }

    @Test
    void testModifyLife() {
        player.modifyLife(-5);
        assertEquals(15, player.getLife());

        player.modifyLife(3);
        assertEquals(18, player.getLife());
    }

    @Test
    void testLifeZeroIsDead() {
        player.modifyLife(-20);
        assertTrue(player.isDead());
    }

    @Test
    void testPoisonCounters() {
        player.addPoison(5);
        assertEquals(5, player.getPoisonCounters());
        assertFalse(player.isDead());

        player.addPoison(5);
        assertTrue(player.isDead()); // 10 poison counters
    }

    @Test
    void testOpponent() {
        Player opponent = new Player("Opponent");
        player.setOpponent(opponent);
        assertEquals(opponent, player.getOpponent());
    }

    @Test
    void testHand() {
        assertNotNull(player.getHand());
        assertEquals(0, player.getHand().size());

        Card card = makeCreature("Test", 1, 1);
        player.getHand().addCard(card);
        assertEquals(1, player.getHand().size());
    }

    @Test
    void testAddPermanent() {
        CreatureCard creature = makeCreature("Giant", 3, 3);
        player.addPermanent(creature);

        assertEquals(1, player.getBattlefield().size());
        assertEquals(1, player.getCreatures().size());
        assertEquals(0, player.getLands().size());
    }

    @Test
    void testRemovePermanent() {
        CreatureCard creature = makeCreature("Giant", 3, 3);
        player.addPermanent(creature);
        player.removePermanent(creature);

        assertEquals(0, player.getBattlefield().size());
        assertEquals(0, player.getCreatures().size());
    }

    @Test
    void testLandPlayedThisTurn() {
        assertFalse(player.hasPlayedLandThisTurn());

        player.setLandPlayedThisTurn(true);
        assertTrue(player.hasPlayedLandThisTurn());

        player.setLandPlayedThisTurn(false);
        assertFalse(player.hasPlayedLandThisTurn());
    }

    @Test
    void testMana() {
        player.addMana(ManaType.GREEN);
        player.addMana(ManaType.RED);

        var mana = player.getAvailableMana();
        assertEquals(2, mana.size());

        player.clearMana();
        assertTrue(player.getAvailableMana().isEmpty());
    }

    @Test
    void testProduceMana() {
        // Add a land as mana source
        LandCard forest = new LandCard("Forest", Arrays.asList(), ManaType.GREEN);
        forest.setController(player);
        player.addPermanent(forest);

        player.produceMana();
        assertFalse(player.getAvailableMana().isEmpty());
    }

    @Test
    void testCanPayCost() {
        player.addMana(ManaType.WHITE);
        player.addMana(ManaType.WHITE);

        ManaCost cost = ManaCost.of(ManaType.WHITE, ManaType.WHITE);
        assertTrue(player.canPayCost(cost));

        ManaCost expensive = ManaCost.of(ManaType.WHITE, ManaType.BLUE);
        assertFalse(player.canPayCost(expensive));
    }

    @Test
    void testUntapAll() {
        CreatureCard c1 = makeCreature("C1", 1, 1);
        CreatureCard c2 = makeCreature("C2", 1, 1);
        c1.tap();
        c2.tap();
        player.addPermanent(c1);
        player.addPermanent(c2);

        player.untapAll();

        assertFalse(c1.isTapped());
        assertFalse(c2.isTapped());
    }

    @Test
    void testStartTurn() {
        player.addMana(ManaType.RED);
        player.setLandPlayedThisTurn(true);

        player.startTurn();

        // Mana should be cleared and land flag reset
        assertFalse(player.hasPlayedLandThisTurn());
    }

    @Test
    void testGetAttackingCreatures() {
        CreatureCard c1 = makeCreature("Attacker", 2, 2);
        CreatureCard c2 = makeCreature("Tapped", 2, 2);
        c2.tap();
        c1.clearSummoningSickness();
        c2.clearSummoningSickness();
        player.addPermanent(c1);
        player.addPermanent(c2);

        var attacking = player.getAttackingCreatures();
        assertEquals(1, attacking.size());
        assertEquals("Attacker", attacking.get(0).getName());
    }

    @Test
    void testHandClass() {
        Player.Hand hand = player.getHand();

        Card c1 = makeCreature("C1", 1, 1);
        Card c2 = makeCreature("C2", 1, 1);

        hand.addCard(c1);
        hand.addCard(c2);

        assertEquals(2, hand.size()); // Both different cards added
        assertEquals(2, hand.getCards().size());

        hand.removeCard(c1);
        assertEquals(1, hand.getCards().size());
    }

    @Test
    void testHandMaxSize() {
        Player.Hand smallHand = new Player.Hand();
        // Max hand size is 10 by default
        for (int i = 0; i < 15; i++) {
            smallHand.addCard(makeCreature("C" + i, 1, 1));
        }
        assertEquals(10, smallHand.size());
    }
}
