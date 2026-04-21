package com.mtg.game;

import com.mtg.player.Player;
import com.mtg.player.Deck;
import com.mtg.model.*;
import com.mtg.zones.*;
import com.mtg.gamecore.PrioritySystem;
import com.mtg.gamecore.StateBasedActions;
import com.mtg.card.CardLibrary;

import java.util.List;
import java.util.ArrayList;

/**
 * Game represents the current state of a Magic: The Gathering game.
 *
 * This class has been refactored to use the new zone system based on MTG rules.
 */
public class Game {
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Player startingPlayer;
    private TurnManager turnManager;
    private ZoneManager zoneManager;
    private PrioritySystem prioritySystem;
    private TurnPhase currentPhase;
    private int turnNumber;
    private boolean gameOver;
    private Player winner;

    public Game(String player1Name, String player2Name) {
        this.player1 = new Player(player1Name);
        this.player2 = new Player(player2Name);
        this.turnManager = new TurnManager(this);
        this.prioritySystem = new PrioritySystem();
        this.gameOver = false;
        this.winner = null;
        this.turnNumber = 0;
    }

    // ========== Game Setup ==========

    public void startGame() {
        // Set owners for all cards
        player1.setOpponent(player2);
        player2.setOpponent(player1);

        // Create decks
        Deck deck1 = CardLibrary.createStarterDeck(player1.getName());
        Deck deck2 = CardLibrary.createStarterDeck(player2.getName());

        // Initialize zone manager
        zoneManager = new ZoneManager(this, player1, player2);

        // Add cards to libraries
        for (Card card : deck1.getCards()) {
            card.setOwner(player1);
            zoneManager.getLibrary(player1).add(card, player1);
        }
        for (Card card : deck2.getCards()) {
            card.setOwner(player2);
            zoneManager.getLibrary(player2).add(card, player2);
        }

        // Shuffle libraries
        zoneManager.shuffleLibrary(player1);
        zoneManager.shuffleLibrary(player2);

        // Draw initial hands
        for (int i = 0; i < 7; i++) {
            drawCard(player1);
            drawCard(player2);
        }

        // Determine starting player (random for now)
        startingPlayer = Math.random() < 0.5 ? player1 : player2;
        currentPlayer = startingPlayer;

        // Set up players
        for (Card card : player1.getHand().getCards()) {
            card.setController(player1);
        }
        for (Card card : player2.getHand().getCards()) {
            card.setController(player2);
        }

        gameOver = false;
        winner = null;
        turnNumber = 1;

        // Set battlefield reference for TurnManager
        turnManager.setBattlefield(zoneManager.getBattlefield());

        // Start first turn
        turnManager.startTurn(currentPlayer);
        currentPhase = turnManager.getCurrentPhase();
    }

    // ========== Game Actions ==========

    /**
     * Draw a card for a player.
     */
    public void drawCard(Player player) {
        Card card = zoneManager.drawCard(player);
        if (card != null) {
            card.setController(player);
            zoneManager.getHand(player).add(card, player);
        }
    }

    /**
     * Play a land card.
     * Rule 305.1, 116.2a: Special action during main phase.
     */
    public boolean playLand(Player player, LandCard land) {
        if (!canPlayLand(player)) {
            return false;
        }

        if (player.hasPlayedLandThisTurn()) {
            return false;
        }

        Hand hand = zoneManager.getHand(player);
        if (hand.getCards().contains(land)) {
            hand.remove(land);
            zoneManager.putOnBattlefield(land, player);
            player.setLandPlayedThisTurn(true);
            return true;
        }
        return false;
    }

    /**
     * Cast a spell from hand.
     */
    public boolean castSpell(Player player, Card card) {
        if (!prioritySystem.playerHasPriority(player)) {
            return false;
        }

        // Check if in hand
        Hand hand = zoneManager.getHand(player);
        if (!hand.getCards().contains(card)) {
            return false;
        }

        // Check mana
        if (!player.canPayCost(card.getManaCost())) {
            return false;
        }

        // Remove from hand, pay cost, add to stack
        hand.remove(card);
        player.payMana(card.getManaCost());
        zoneManager.castSpell(card, player);
        prioritySystem.onSpellCast();

        return true;
    }

    /**
     * Play a permanent (creature, enchantment, etc.) from hand.
     */
    public boolean playPermanent(Player player, Card card) {
        if (!currentPhase.isMainPhase()) {
            return false;
        }

        if (!prioritySystem.playerHasPriority(player)) {
            return false;
        }

        if (card.getType() == CardType.LAND) {
            return playLand(player, (LandCard) card);
        }

        if (!player.canPayCost(card.getManaCost())) {
            return false;
        }

        Hand hand = zoneManager.getHand(player);
        if (!hand.getCards().contains(card)) {
            return false;
        }

        hand.remove(card);
        player.payMana(card.getManaCost());
        zoneManager.putOnBattlefield(card, player);
        prioritySystem.onSpellCast();

        return true;
    }

    /**
     * Pass priority.
     */
    public void passPriority() {
        prioritySystem.pass();
        if (prioritySystem.allPlayersPassed()) {
            resolveTopOfStack();
        }
    }

    /**
     * Resolve top of stack.
     */
    private void resolveTopOfStack() {
        Stack stack = zoneManager.getStack();
        if (!stack.isEmpty()) {
            Stack.StackItem item = stack.resolve();
            if (item instanceof Stack.SpellItem) {
                resolveSpell((Stack.SpellItem) item);
            }
            prioritySystem.grantPriorityAfterResolve();
        } else {
            turnManager.advancePhase();
            currentPhase = turnManager.getCurrentPhase();
        }
    }

    /**
     * Resolve a spell.
     */
    private void resolveSpell(Stack.SpellItem spellItem) {
        Card card = spellItem.getCard();
        Player caster = spellItem.getController();
        Battlefield bf = zoneManager.getBattlefield();

        switch (card.getType()) {
            case CREATURE -> {
                zoneManager.putOnBattlefield(card, caster);
                CreatureCard creature = (CreatureCard) card;
                creature.untap();
            }
            case ENCHANTMENT -> zoneManager.putOnBattlefield(card, caster);
            case ARTIFACT -> zoneManager.putOnBattlefield(card, caster);
            case PLANESWALKER -> zoneManager.putOnBattlefield(card, caster);
            case INSTANT, SORCERY -> {
                resolveInstantSorcery(card, caster);
                zoneManager.getGraveyard(caster).add(card);
            }
            default -> {}
        }
    }

    /**
     * Resolve instant/sorcery effects (simplified).
     */
    private void resolveInstantSorcery(Card card, Player caster) {
        Player opponent = caster.getOpponent();
        Battlefield bf = zoneManager.getBattlefield();

        // Simplified spell effects
        switch (card.getName()) {
            case "Lightning Bolt" -> {
                List<CreatureCard> creatures = bf.getCreatures();
                if (!creatures.isEmpty()) {
                    creatures.get(0).addDamage(3);
                } else {
                    opponent.modifyLife(-3);
                }
            }
            case "Shock" -> {
                List<CreatureCard> creatures = bf.getCreatures();
                if (!creatures.isEmpty()) {
                    creatures.get(0).addDamage(2);
                } else {
                    opponent.modifyLife(-2);
                }
            }
            case "Dark Ritual" -> {
                for (int i = 0; i < 3; i++) {
                    caster.addMana(ManaType.BLACK);
                }
            }
        }
    }

    /**
     * Check if player can play a land.
     */
    public boolean canPlayLand(Player player) {
        return currentPhase.isMainPhase() &&
               zoneManager.getStack().isEmpty() &&
               prioritySystem.playerHasPriority(player) &&
               !player.hasPlayedLandThisTurn();
    }

    // ========== Turn Management ==========

    public void nextPhase() {
        if (gameOver) return;

        // Empty mana pools at end of phase (Rule 500.5)
        if (currentPhase != TurnPhase.CLEANUP) {
            player1.clearMana();
            player2.clearMana();
        }

        turnManager.advancePhase();
        currentPhase = turnManager.getCurrentPhase();

        // Handle phase-specific logic
        switch (currentPhase) {
            case UNTAAP -> turnManager.beginUntapStep();
            case UPKEEP -> turnManager.beginUpkeepStep();
            case DRAW -> turnManager.beginDrawStep();
            case COMBAT_START -> turnManager.beginCombat();
            case DECLARE_ATTACKERS -> turnManager.beginDeclareAttackers();
            case COMBAT_DAMAGE -> turnManager.resolveCombatDamage();
            case COMBAT_END -> turnManager.endCombat();
            case END -> turnManager.beginEndStep();
            case CLEANUP -> turnManager.beginCleanupStep();
            case GAME_OVER -> gameOver = true;
            default -> {}
        }

        // Check state-based actions
        StateBasedActions.check(player1, player2,
            zoneManager.getBattlefield(),
            zoneManager.getLibrary(player1),
            zoneManager.getLibrary(player2));
    }

    public void endTurn() {
        while (!gameOver && currentPhase != TurnPhase.END) {
            nextPhase();
        }
        nextPhase();
    }

    // ========== Combat ==========

    public void declareAttacker(CreatureCard creature, Player target) {
        if (currentPhase == TurnPhase.DECLARE_ATTACKERS) {
            turnManager.declareAttacker(creature, target);
        }
    }

    public void declareBlocker(CreatureCard blocker, CreatureCard attacker) {
        if (currentPhase == TurnPhase.DECLARE_BLOCKERS) {
            turnManager.declareBlocker(blocker, attacker);
        }
    }

    // ========== Getters ==========

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Player getOpponent(Player player) { return player == player1 ? player2 : player1; }
    public TurnPhase getCurrentPhase() { return currentPhase; }
    public TurnManager getTurnManager() { return turnManager; }
    public PrioritySystem getPrioritySystem() { return prioritySystem; }
    public ZoneManager getZoneManager() { return zoneManager; }
    public Battlefield getBattlefield() { return zoneManager.getBattlefield(); }
    public Stack getStack() { return zoneManager.getStack(); }
    public boolean isGameOver() { return gameOver; }
    public Player getWinner() { return winner; }
    public int getTurnNumber() { return turnNumber; }

    public void loseGame(Player player, String reason) {
        if (!gameOver) {
            gameOver = true;
            winner = player.getOpponent();
        }
    }

    public boolean isMainPhase() {
        return currentPhase == TurnPhase.MAIN1 || currentPhase == TurnPhase.MAIN2;
    }

    public boolean isUpkeepStep() {
        return currentPhase == TurnPhase.UPKEEP;
    }

    /**
     * Check if an Aura can legally enchant a target.
     */
    public boolean isLegalAuraTarget(EnchantmentCard aura, PermanentCard target) {
        return true; // Simplified - full implementation would check enchant keyword
    }

    // ========== Play Methods (called by UI) ==========

    public boolean playCreature(Player player, CreatureCard creature) {
        return playPermanent(player, creature);
    }

    public boolean playEnchantment(Player player, EnchantmentCard enchantment) {
        return playPermanent(player, enchantment);
    }

    public boolean playArtifact(Player player, ArtifactCard artifact) {
        return playPermanent(player, artifact);
    }

    public boolean playPlaneswalker(Player player, PlaneswalkerCard planeswalker) {
        return playPermanent(player, planeswalker);
    }

    public boolean playBattle(Player player, BattleCard battle) {
        return playPermanent(player, battle);
    }

    public boolean playInstant(Player player, SpellCard spell) {
        return castSpell(player, spell);
    }

    // ========== GameListener Interface ==========

    public interface GameListener {
        void onPhaseChange(TurnPhase phase, Player currentPlayer);
        void onCardPlayed(Player player, Card card);
        void onDamageDealt(Player target, int amount);
        void onCreatureDestroyed(CreatureCard creature);
        void onPermanentDestroyed(PermanentCard permanent);
        void onGameOver(Player winner);
    }

    private GameListener listener;

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public GameListener getListener() {
        return listener;
    }

    public void notifyPhaseChange() {
        if (listener != null) {
            listener.onPhaseChange(currentPhase, currentPlayer);
        }
    }

    public void notifyCardPlayed(Player player, Card card) {
        if (listener != null) {
            listener.onCardPlayed(player, card);
        }
    }

    public void notifyDamageDealt(Player target, int amount) {
        if (listener != null) {
            listener.onDamageDealt(target, amount);
        }
    }

    public void notifyCreatureDestroyed(CreatureCard creature) {
        if (listener != null) {
            listener.onCreatureDestroyed(creature);
        }
    }

    public void notifyPermanentDestroyed(PermanentCard permanent) {
        if (listener != null) {
            listener.onPermanentDestroyed(permanent);
        }
    }

    public void notifyGameOver(Player winner) {
        if (listener != null) {
            listener.onGameOver(winner);
        }
    }
}
