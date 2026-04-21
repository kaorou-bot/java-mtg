package com.mtg.game;

import com.mtg.card.CardLibrary;
import com.mtg.model.*;
import com.mtg.player.Deck;
import com.mtg.player.Player;
import java.util.List;

public class Game {
    private Player player1;
    private Player player2;
    private TurnManager turnManager;
    private GamePhase currentPhase;
    private boolean gameOver;
    private Player winner;

    public interface GameListener {
        void onPhaseChange(GamePhase phase, Player currentPlayer);
        void onCardPlayed(Player player, Card card);
        void onDamageDealt(Player target, int amount);
        void onCreatureDestroyed(CreatureCard creature);
        void onPermanentDestroyed(PermanentCard permanent);
        void onGameOver(Player winner);
    }

    private GameListener listener;

    public Game(String player1Name, String player2Name) {
        this.player1 = new Player(player1Name);
        this.player2 = new Player(player2Name);
        this.turnManager = new TurnManager(player1, player2);
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public void startGame() {
        Deck deck1 = CardLibrary.createStarterDeck(player1.getName());
        Deck deck2 = CardLibrary.createStarterDeck(player2.getName());

        player1.getDeck().getCards().clear();
        player2.getDeck().getCards().clear();
        player1.getDeck().addCards(deck1.getCards());
        player2.getDeck().addCards(deck2.getCards());

        player1.drawInitialHand();
        player2.drawInitialHand();

        gameOver = false;
        winner = null;

        turnManager.startGame();
        currentPhase = turnManager.getCurrentPhase();

        if (listener != null) {
            listener.onPhaseChange(currentPhase, turnManager.getCurrentPlayer());
        }
    }

    public boolean playLand(Player player, LandCard land) {
        if (turnManager.getCurrentPhase() != GamePhase.MAIN1 &&
            turnManager.getCurrentPhase() != GamePhase.MAIN2) {
            return false;
        }

        if (player != turnManager.getCurrentPlayer()) {
            return false;
        }

        // Check if player has already played a land this turn (rule: one land per turn)
        if (player.hasPlayedLandThisTurn()) {
            return false;
        }

        if (player.getHand().removeCard(land)) {
            player.addPermanent(land);
            player.addMana(land.getProducedMana());
            player.setLandPlayedThisTurn(true);
            if (listener != null) {
                listener.onCardPlayed(player, land);
            }
            return true;
        }
        return false;
    }

    public boolean playCreature(Player player, CreatureCard creature) {
        if (!canPlayPermanent(player)) {
            return false;
        }

        if (!player.canPayCost(creature.getManaCost())) {
            return false;
        }

        if (player.getHand().removeCard(creature)) {
            player.payMana(creature.getManaCost());
            player.addCreatureToBattlefield(creature);
            if (listener != null) {
                listener.onCardPlayed(player, creature);
            }
            return true;
        }
        return false;
    }

    public boolean playEnchantment(Player player, EnchantmentCard enchantment) {
        if (!canPlayPermanent(player)) {
            return false;
        }

        if (!player.canPayCost(enchantment.getManaCost())) {
            return false;
        }

        if (player.getHand().removeCard(enchantment)) {
            player.payMana(enchantment.getManaCost());
            player.addPermanent(enchantment);
            if (listener != null) {
                listener.onCardPlayed(player, enchantment);
            }
            return true;
        }
        return false;
    }

    public boolean playArtifact(Player player, ArtifactCard artifact) {
        if (!canPlayPermanent(player)) {
            return false;
        }

        if (!player.canPayCost(artifact.getManaCost())) {
            return false;
        }

        if (player.getHand().removeCard(artifact)) {
            player.payMana(artifact.getManaCost());
            player.addPermanent(artifact);
            if (listener != null) {
                listener.onCardPlayed(player, artifact);
            }
            return true;
        }
        return false;
    }

    public boolean playPlaneswalker(Player player, PlaneswalkerCard planeswalker) {
        if (!canPlayPermanent(player)) {
            return false;
        }

        if (!player.canPayCost(planeswalker.getManaCost())) {
            return false;
        }

        if (player.getHand().removeCard(planeswalker)) {
            player.payMana(planeswalker.getManaCost());
            player.addPermanent(planeswalker);
            if (listener != null) {
                listener.onCardPlayed(player, planeswalker);
            }
            return true;
        }
        return false;
    }

    public boolean playBattle(Player player, BattleCard battle) {
        if (!canPlayPermanent(player)) {
            return false;
        }

        if (!player.canPayCost(battle.getManaCost())) {
            return false;
        }

        if (player.getHand().removeCard(battle)) {
            player.payMana(battle.getManaCost());
            player.addPermanent(battle);
            if (listener != null) {
                listener.onCardPlayed(player, battle);
            }
            return true;
        }
        return false;
    }

    private boolean canPlayPermanent(Player player) {
        GamePhase phase = turnManager.getCurrentPhase();
        return (phase == GamePhase.MAIN1 || phase == GamePhase.MAIN2) &&
               player == turnManager.getCurrentPlayer();
    }

    public boolean playInstant(Player player, SpellCard spell) {
        if (spell.getType() != CardType.INSTANT) {
            return false;
        }

        if (!player.canPayCost(spell.getManaCost())) {
            return false;
        }

        if (player.getHand().removeCard(spell)) {
            player.payMana(spell.getManaCost());
            resolveInstantSpell(player, spell);
            if (listener != null) {
                listener.onCardPlayed(player, spell);
            }
            return true;
        }
        return false;
    }

    private void resolveInstantSpell(Player player, SpellCard spell) {
        String name = spell.getName();
        Player opponent = getOpponent(player);
        List<CreatureCard> opponentCreatures = opponent.getCreatures();
        List<CreatureCard> playerCreatures = player.getCreatures();

        switch (name) {
            case "Lightning Bolt" -> {
                if (!opponentCreatures.isEmpty()) {
                    CreatureCard target = opponentCreatures.get(0);
                    CombatManager.dealDamageToCreature(target, 3);
                    checkCreatureDeath(target, opponent);
                } else {
                    CombatManager.dealDamageToPlayer(opponent, 3);
                    checkGameOver();
                }
                if (listener != null) {
                    listener.onDamageDealt(opponent, 3);
                }
            }
            case "Shock" -> {
                if (!opponentCreatures.isEmpty()) {
                    CreatureCard target = opponentCreatures.get(0);
                    CombatManager.dealDamageToCreature(target, 2);
                    checkCreatureDeath(target, opponent);
                } else {
                    CombatManager.dealDamageToPlayer(opponent, 2);
                    checkGameOver();
                }
                if (listener != null) {
                    listener.onDamageDealt(opponent, 2);
                }
            }
            case "Counterspell", "Cancel" -> {
                // Counter effect - spell is countered
            }
            case "Doom Blade" -> {
                if (!opponentCreatures.isEmpty()) {
                    CreatureCard target = opponentCreatures.get(0);
                    opponent.removeCreatureFromBattlefield(target);
                    if (listener != null) {
                        listener.onCreatureDestroyed(target);
                    }
                }
            }
            case "Dark Ritual" -> {
                for (int i = 0; i < 3; i++) {
                    player.addMana(ManaType.BLACK);
                }
            }
            case "Giant Growth" -> {
                if (!playerCreatures.isEmpty()) {
                    CreatureCard target = playerCreatures.get(0);
                    target.modifyPower(3);
                    target.modifyToughness(3);
                }
            }
            case "Revitalize" -> {
                player.modifyLife(3);
            }
        }
    }

    public void nextPhase() {
        if (gameOver) return;

        GamePhase prevPhase = currentPhase;
        currentPhase = turnManager.advancePhase();

        if (currentPhase == GamePhase.UNTAP) {
            turnManager.getCurrentPlayer().untapAll();
        }

        if (currentPhase == GamePhase.GAME_OVER) {
            gameOver = true;
        }

        if (listener != null) {
            listener.onPhaseChange(currentPhase, turnManager.getCurrentPlayer());
        }
    }

    public void endTurn() {
        while (currentPhase != GamePhase.END) {
            nextPhase();
        }
        nextPhase();
    }

    public void declareAttacker(CreatureCard creature) {
        if (turnManager.canDeclareAttacker(creature)) {
            turnManager.declareAttacker(creature);
        }
    }

    public void declareBlocker(CreatureCard blocker, CreatureCard attacker) {
        if (turnManager.canDeclareBlocker(blocker)) {
            turnManager.declareBlocker(blocker, attacker);
        }
    }

    public Player getOpponent(Player player) {
        return player == player1 ? player2 : player1;
    }

    public Player getCurrentPlayer() {
        return turnManager.getCurrentPlayer();
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    private void checkCreatureDeath(CreatureCard creature, Player owner) {
        if (creature.isDestroyed()) {
            owner.removeCreatureFromBattlefield(creature);
            if (listener != null) {
                listener.onCreatureDestroyed(creature);
            }
        }
    }

    private void checkGameOver() {
        if (player1.isDead()) {
            gameOver = true;
            winner = player2;
            if (listener != null) {
                listener.onGameOver(winner);
            }
        } else if (player2.isDead()) {
            gameOver = true;
            winner = player1;
            if (listener != null) {
                listener.onGameOver(winner);
            }
        }
    }

    public String getGameState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Game State ===\n");
        sb.append("Phase: ").append(currentPhase.getName()).append("\n");
        sb.append("Current Player: ").append(getCurrentPlayer().getName()).append("\n");
        sb.append("\n");
        sb.append(player1.getName()).append(" - Life: ").append(player1.getLife())
          .append(" | Deck: ").append(player1.getDeck().size())
          .append(" | Hand: ").append(player1.getHand().size()).append("\n");
        sb.append(player2.getName()).append(" - Life: ").append(player2.getLife())
          .append(" | Deck: ").append(player2.getDeck().size())
          .append(" | Hand: ").append(player2.getHand().size()).append("\n");
        return sb.toString();
    }
}
