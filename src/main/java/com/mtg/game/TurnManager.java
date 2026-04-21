package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;
import java.util.*;

public class TurnManager {
    private Player currentPlayer;
    private Player opponent;
    private GamePhase currentPhase;
    private List<CreatureCard> attackers;
    private List<CreatureCard> blockers;

    public TurnManager(Player player1, Player player2) {
        this.currentPlayer = player1;
        this.opponent = player2;
        this.currentPhase = GamePhase.UNTAP;
        this.attackers = new ArrayList<>();
        this.blockers = new ArrayList<>();
    }

    public void startGame() {
        currentPlayer = currentPlayer;
        opponent = opponent;
        startTurn();
    }

    public void startTurn() {
        currentPlayer.startTurn();
        currentPhase = GamePhase.UNTAP;
        attackers.clear();
        blockers.clear();
        currentPlayer.resetCreaturesForCombat();
    }

    public GamePhase advancePhase() {
        currentPhase = switch (currentPhase) {
            case UNTAP -> GamePhase.UPKEEP;
            case UPKEEP -> GamePhase.DRAW;
            case DRAW -> {
                currentPlayer.drawCard();
                if (currentPlayer.getDeck().isEmpty()) {
                    yield GamePhase.GAME_OVER;
                }
                yield GamePhase.MAIN1;
            }
            case MAIN1 -> GamePhase.COMBAT_BEGIN;
            case COMBAT_BEGIN -> GamePhase.DECLARE_ATTACKERS;
            case DECLARE_ATTACKERS -> GamePhase.DECLARE_BLOCKERS;
            case DECLARE_BLOCKERS -> GamePhase.COMBAT_DAMAGE;
            case COMBAT_DAMAGE -> {
                resolveCombatDamage();
                yield GamePhase.COMBAT_END;
            }
            case COMBAT_END -> GamePhase.MAIN2;
            case MAIN2 -> GamePhase.END;
            case END -> {
                switchTurn();
                yield GamePhase.UNTAP;
            }
            default -> currentPhase;
        };
        return currentPhase;
    }

    public void switchTurn() {
        Player temp = currentPlayer;
        currentPlayer = opponent;
        opponent = temp;
        currentPlayer.startTurn();
    }

    private void resolveCombatDamage() {
        for (CreatureCard attacker : attackers) {
            if (blockers.contains(attacker)) {
                CreatureCard blocker = findBlocker(attacker);
                resolveCreatureCombat(attacker, blocker);
            } else {
                opponent.modifyLife(-attacker.getCurrentPower());
            }
        }
        cleanupDestroyedCreatures();
    }

    private void resolveCreatureCombat(CreatureCard attacker, CreatureCard blocker) {
        int attackerDamage = attacker.getCurrentPower();
        int blockerDamage = blocker.getCurrentToughness();

        blocker.modifyToughness(-attackerDamage);

        if (!blocker.hasFirstStrike() && !attacker.hasFirstStrike()) {
            attacker.modifyToughness(-blockerDamage);
        } else if (attacker.hasFirstStrike() && !blocker.hasFirstStrike()) {
            blocker.modifyToughness(0);
        } else if (!attacker.hasFirstStrike() && blocker.hasFirstStrike()) {
            attacker.modifyToughness(-blockerDamage);
        }
    }

    private CreatureCard findBlocker(CreatureCard attacker) {
        return blockers.get(blockers.indexOf(attacker));
    }

    private void cleanupDestroyedCreatures() {
        currentPlayer.getCreatures().removeIf(CreatureCard::isDestroyed);
        opponent.getCreatures().removeIf(CreatureCard::isDestroyed);
    }

    public boolean canDeclareAttacker(CreatureCard creature) {
        return currentPhase == GamePhase.DECLARE_ATTACKERS &&
               creature.canAttack() &&
               !attackers.contains(creature);
    }

    public void declareAttacker(CreatureCard creature) {
        if (canDeclareAttacker(creature)) {
            creature.tap();
            attackers.add(creature);
        }
    }

    public boolean canDeclareBlocker(CreatureCard creature) {
        return currentPhase == GamePhase.DECLARE_BLOCKERS &&
               opponent.getBattlefield().contains(creature) &&
               !blockers.contains(creature);
    }

    public void declareBlocker(CreatureCard creature, CreatureCard attacker) {
        if (canDeclareBlocker(creature) && attackers.contains(attacker)) {
            creature.tap();
            blockers.add(creature);
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOpponent() {
        return opponent;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public List<CreatureCard> getAttackers() {
        return new ArrayList<>(attackers);
    }

    public List<CreatureCard> getBlockers() {
        return new ArrayList<>(blockers);
    }

    public boolean isInCombat() {
        return currentPhase == GamePhase.DECLARE_ATTACKERS ||
               currentPhase == GamePhase.DECLARE_BLOCKERS ||
               currentPhase == GamePhase.COMBAT_DAMAGE;
    }
}
