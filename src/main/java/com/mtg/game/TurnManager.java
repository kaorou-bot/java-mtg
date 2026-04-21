package com.mtg.game;

import com.mtg.model.CreatureCard;
import com.mtg.model.PermanentCard;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;

import java.util.*;

/**
 * TurnManager handles turn structure according to Rules 500-514.
 *
 * Turn structure:
 * Beginning Phase → Precombat Main Phase → Combat Phase →
 * Postcombat Main Phase → Ending Phase
 */
public class TurnManager {
    private final Game game;
    private Battlefield battlefield;
    private Player activePlayer;
    private Player nonActivePlayer;
    private TurnPhase currentPhase;
    private List<DeclaredAttacker> attackers;
    private List<DeclaredBlocker> blockers;
    private int turnNumber;

    /**
     * Represents a declared attacker with its target.
     */
    public static class DeclaredAttacker {
        public final CreatureCard creature;
        public final Player target;  // Can be opponent player or planeswalker

        public DeclaredAttacker(CreatureCard creature, Player target) {
            this.creature = creature;
            this.target = target;
        }
    }

    /**
     * Represents a declared blocker with its attacker.
     */
    public static class DeclaredBlocker {
        public final CreatureCard blocker;
        public final CreatureCard attacker;

        public DeclaredBlocker(CreatureCard blocker, CreatureCard attacker) {
            this.blocker = blocker;
            this.attacker = attacker;
        }
    }

    public TurnManager(Game game) {
        this.game = game;
        this.attackers = new ArrayList<>();
        this.blockers = new ArrayList<>();
        this.turnNumber = 0;
        this.currentPhase = TurnPhase.UNTAAP;
    }

    /**
     * Set the battlefield reference. Required before using turn management
     * in tests or when Game is not fully initialized.
     */
    public void setBattlefield(Battlefield battlefield) {
        this.battlefield = battlefield;
    }

    // ========== Turn Control ==========

    /**
     * Start a new turn for the active player.
     */
    public void startTurn(Player player) {
        this.activePlayer = player;
        this.nonActivePlayer = player.getOpponent();
        this.attackers.clear();
        this.blockers.clear();
        turnNumber++;

        // Reset creatures
        Battlefield bf = battlefield;
        for (CreatureCard c : bf.getCreatures()) {
            if (c.getController().equals(activePlayer)) {
                c.clearSummoningSickness();
                c.resetForTurn();
            }
        }

        // Rule 500.4: Effects that last until a step/phase expire
        // (Implementation would track and expire effects)

        beginUntapStep();
    }

    /**
     * Advance to the next phase.
     */
    public TurnPhase advancePhase() {
        switch (currentPhase) {
            case UNTAAP -> currentPhase = TurnPhase.UPKEEP;
            case UPKEEP -> currentPhase = TurnPhase.DRAW;
            case DRAW -> currentPhase = TurnPhase.MAIN1;
            case MAIN1 -> currentPhase = TurnPhase.COMBAT_START;
            case COMBAT_START -> currentPhase = TurnPhase.DECLARE_ATTACKERS;
            case DECLARE_ATTACKERS -> currentPhase = TurnPhase.DECLARE_BLOCKERS;
            case DECLARE_BLOCKERS -> currentPhase = TurnPhase.COMBAT_DAMAGE;
            case COMBAT_DAMAGE -> currentPhase = TurnPhase.COMBAT_END;
            case COMBAT_END -> currentPhase = TurnPhase.MAIN2;
            case MAIN2 -> currentPhase = TurnPhase.END;
            case END -> currentPhase = TurnPhase.CLEANUP;
            case CLEANUP -> {
                switchTurn();
                currentPhase = TurnPhase.UNTAAP;
            }
            default -> {}
        }
        return currentPhase;
    }

    /**
     * Switch to the next player's turn.
     * Also increments turn number and starts the new turn.
     */
    public void switchTurn() {
        Player temp = activePlayer;
        activePlayer = nonActivePlayer;
        nonActivePlayer = temp;
        turnNumber++;
        // Clear combat state for new turn
        attackers.clear();
        blockers.clear();
        beginUntapStep();
    }

    // ========== Phase Begin Methods ==========

    /**
     * Rule 502: Untap Step
     * - Phase in/out permanents
     * - Day/night check
     * - Untap permanents
     * - No priority granted
     */
    public void beginUntapStep() {
        // Rule 502.3: Untap all permanents
        Battlefield bf = battlefield;
        for (PermanentCard p : bf.getPermanents()) {
            if (p.getController().equals(activePlayer)) {
                p.untap();
            }
        }

        // Rule 502.4: No priority during untap step
        // Priority will be granted when moving to upkeep
    }

    /**
     * Rule 503: Upkeep Step
     * - Trigger abilities from untap step
     * - Active player gets priority
     */
    public void beginUpkeepStep() {
        // Rule 503.1a: Triggered abilities from untap step and upkeep
        // would be queued here
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * Rule 504: Draw Step
     * - Draw a card (turn-based action)
     * - Active player gets priority
     */
    public void beginDrawStep() {
        // Rule 504.1: Active player draws a card
        game.drawCard(activePlayer);

        // Rule 504.2: Active player gets priority
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * Begin main phase.
     */
    public void beginMainPhase(TurnPhase main) {
        currentPhase = main;
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * Rule 507: Beginning of Combat Step
     * - Select defending player (in 1v1, it's automatic)
     * - Active player gets priority
     */
    public void beginCombat() {
        // Rule 506.2: In 1v1, non-active player is defending
        // Rule 507.2: Active player gets priority
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * Rule 508: Declare Attackers Step
     * - Turn-based action, no priority initially
     * - Active player declares attackers
     */
    public void beginDeclareAttackers() {
        // Rule 508.1: Declare attackers (turn-based action)
        // After declaration, active player gets priority
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * Rule 509: Declare Blockers Step
     * - Non-active player declares blockers
     * - Non-active player gets priority after
     */
    public void beginDeclareBlockers() {
        // Rule 509.1: Declare blockers
        if (game != null) {
            game.getPrioritySystem().grantPriorityToActive();
        }
    }

    /**
     * Rule 510: Combat Damage Step
     * - Assign and deal combat damage
     * - Handle first strike
     */
    public void resolveCombatDamage() {
        // First strike check (Rule 510.4)
        boolean hasFirstStrike = false;
        for (DeclaredAttacker da : attackers) {
            if (da.creature.hasFirstStrike() || da.creature.hasDoubleStrike()) {
                hasFirstStrike = true;
                break;
            }
        }
        for (DeclaredBlocker db : blockers) {
            if (db.blocker.hasFirstStrike() || db.blocker.hasDoubleStrike()) {
                hasFirstStrike = true;
                break;
            }
        }

        if (hasFirstStrike) {
            // First strike combat damage
            resolveCombatDamage(false);
            // Add second combat damage step
            currentPhase = TurnPhase.COMBAT_DAMAGE_FIRST;
        } else {
            resolveCombatDamage(true);
            currentPhase = TurnPhase.COMBAT_END;
        }
    }

    /**
     * Resolve combat damage for all attackers.
     */
    private void resolveCombatDamage(boolean isNormalDamage) {
        Battlefield bf = battlefield;

        for (DeclaredAttacker da : attackers) {
            CreatureCard attacker = da.creature;
            int damage = attacker.getCurrentPower();

            // Find if attacker is blocked
            List<DeclaredBlocker> blocking = getBlockersFor(attacker);

            if (blocking.isEmpty()) {
                // Unblocked - damage to defending player/planeswalker
                if (attacker.hasTrample()) {
                    // Trample: can assign excess to player
                    da.target.modifyLife(-damage);
                } else {
                    da.target.modifyLife(-damage);
                }
            } else {
                // Blocked - damage to blockers
                for (DeclaredBlocker db : blocking) {
                    CreatureCard blocker = db.blocker;
                    if (attacker.hasTrample()) {
                        // Assign lethal to first blocker, rest to player
                        blocker.addDamage(damage);
                        int excess = damage - blocker.getToughness();
                        if (excess > 0) {
                            da.target.modifyLife(-excess);
                        }
                    } else {
                        blocker.addDamage(damage);
                    }
                }
            }
        }

        // Blockers deal damage to attackers
        for (DeclaredBlocker db : blockers) {
            if (!attackers.contains(db.attacker)) continue;
            CreatureCard blocker = db.blocker;
            CreatureCard attacker = db.attacker;
            attacker.addDamage(blocker.getCurrentPower());
        }
    }

    private List<DeclaredBlocker> getBlockersFor(CreatureCard attacker) {
        List<DeclaredBlocker> result = new ArrayList<>();
        for (DeclaredBlocker db : blockers) {
            if (db.attacker.equals(attacker)) {
                result.add(db);
            }
        }
        return result;
    }

    /**
     * Rule 511: End of Combat Step
     * - Remove creatures from combat
     * Note: Priority is granted by Game.nextPhase() after this method returns.
     */
    public void endCombat() {
        // Rule 511.3: Remove all creatures from combat
        attackers.clear();
        blockers.clear();
    }

    /**
     * Rule 513: End Step
     * - Ending step triggered abilities
     */
    public void beginEndStep() {
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * Rule 514: Cleanup Step
     * - Discard down to max hand size
     * - Remove damage
     * - Check state-based actions
     */
    public void beginCleanupStep() {
        // Rule 514.1: Discard excess cards
        var hand = game.getZoneManager().getHand(activePlayer);
        while (hand.size() > 7) {
            // Would need UI to choose which cards to discard
            // For now, discard from end
        }

        // Rule 514.2: Remove damage
        Battlefield bf = battlefield;
        for (CreatureCard c : bf.getCreatures()) {
            c.clearDamage();
        }
    }

    // ========== Combat Declaration ==========

    /**
     * Declare a creature as an attacker.
     * Rule 508.1: Must be untapped, not summoning sick (unless haste).
     */
    public boolean canDeclareAttacker(CreatureCard creature) {
        if (currentPhase != TurnPhase.DECLARE_ATTACKERS) return false;
        if (!creature.getController().equals(activePlayer)) return false;
        if (creature.isTapped()) return false;
        if (creature.hasSummoningSickness() && !creature.hasHaste()) return false;
        if (isAlreadyAttacking(creature)) return false;
        return true;
    }

    public void declareAttacker(CreatureCard creature, Player target) {
        if (canDeclareAttacker(creature)) {
            creature.tap();
            attackers.add(new DeclaredAttacker(creature, target));
        }
    }

    /**
     * Declare a creature as a blocker.
     * Rule 509.1: Must be untapped.
     */
    public boolean canDeclareBlocker(CreatureCard creature) {
        if (currentPhase != TurnPhase.DECLARE_BLOCKERS) return false;
        if (!creature.getController().equals(nonActivePlayer)) return false;
        if (creature.isTapped()) return false;
        return true;
    }

    public void declareBlocker(CreatureCard creature, CreatureCard attacker) {
        if (canDeclareBlocker(creature)) {
            creature.tap();
            blockers.add(new DeclaredBlocker(creature, attacker));
        }
    }

    private boolean isAlreadyAttacking(CreatureCard creature) {
        for (DeclaredAttacker da : attackers) {
            if (da.creature.equals(creature)) return true;
        }
        return false;
    }

    // ========== Getters ==========

    public Player getActivePlayer() { return activePlayer; }
    public Player getNonActivePlayer() { return nonActivePlayer; }
    public TurnPhase getCurrentPhase() { return currentPhase; }
    public int getTurnNumber() { return turnNumber; }
    public List<DeclaredAttacker> getAttackers() { return new ArrayList<>(attackers); }
    public List<DeclaredBlocker> getBlockers() { return new ArrayList<>(blockers); }
}
