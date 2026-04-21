package com.mtg.gamecore;

import com.mtg.player.Player;
import com.mtg.zones.Stack;
import com.mtg.zones.Battlefield;
import com.mtg.zones.Library;

import java.util.ArrayList;
import java.util.List;

/**
 * PrioritySystem manages the priority system according to Rule 117.
 *
 * Rule 117.1: Unless a spell or ability instructs a player to take an action,
 * which player can take actions at any given time is determined by priority.
 *
 * Key rules:
 * - 117.1a: Can cast instants anytime with priority
 * - 117.1b: Can activate abilities anytime with priority
 * - 117.1c: Special actions (some anytime, some need main phase + empty stack)
 * - 117.1d: Can activate mana abilities even without priority when paying costs
 *
 * - 117.3a: Active player gets priority at start of most steps/phases
 * - 117.3b: Active player gets priority after spell/ability resolves
 * - 117.3c: Player gets priority after casting/activating
 * - 117.3d: Player can pass priority
 *
 * - 117.4: All players pass in succession → resolve top of stack or phase ends
 * - 117.5: Before priority, check state-based actions, then trigger abilities
 */
public class PrioritySystem {
    private Player activePlayer;
    private Player nonActivePlayer;
    private Player currentPriorityPlayer;
    private int passCount;

    public PrioritySystem() {
        this.passCount = 0;
    }

    /**
     * Set the players for the current turn.
     */
    public void setPlayers(Player active, Player nonActive) {
        this.activePlayer = active;
        this.nonActivePlayer = nonActive;
    }

    /**
     * Get the player who currently has priority.
     */
    public Player getPlayerWithPriority() {
        return currentPriorityPlayer;
    }

    /**
     * Grant priority to the active player.
     * Called at the beginning of most steps/phases (Rule 117.3a).
     */
    public void grantPriorityToActive() {
        passCount = 0;
        currentPriorityPlayer = activePlayer;
    }

    /**
     * Grant priority after a spell or ability resolves (Rule 117.3b).
     */
    public void grantPriorityAfterResolve() {
        passCount = 0;
        currentPriorityPlayer = activePlayer;
    }

    /**
     * Called when a player casts a spell or activates an ability (Rule 117.3c).
     */
    public void onSpellCast() {
        passCount = 0;
    }

    /**
     * Called when a player activates an ability (Rule 117.3c).
     */
    public void onAbilityActivated() {
        passCount = 0;
    }

    /**
     * Player passes priority (Rule 117.3d).
     */
    public void pass() {
        if (currentPriorityPlayer == null) return;

        if (currentPriorityPlayer == activePlayer) {
            currentPriorityPlayer = nonActivePlayer;
        } else {
            passCount++;
            if (passCount >= 2) {
                // All players passed - handled by Game
                currentPriorityPlayer = null;
            } else {
                currentPriorityPlayer = activePlayer;
            }
        }
    }

    /**
     * Check if all players have passed.
     */
    public boolean allPlayersPassed() {
        return passCount >= 2;
    }

    /**
     * Check if player has priority.
     */
    public boolean playerHasPriority(Player player) {
        return player != null && player.equals(currentPriorityPlayer);
    }

    /**
     * Get available actions for a player.
     */
    public List<String> getAvailableActions(Player player, Stack stack, boolean isMainPhase, boolean isUpkeepStep) {
        List<String> actions = new ArrayList<>();

        if (!player.equals(currentPriorityPlayer)) {
            return actions;
        }

        actions.add("Pass Priority");

        if (isMainPhase && stack.isEmpty()) {
            actions.add("Play Land");
        }

        if (isMainPhase || isUpkeepStep) {
            actions.add("Cast Spell");
        } else if (!stack.isEmpty()) {
            actions.add("Cast Instant");
        }

        actions.add("Activate Ability");

        return actions;
    }

    /**
     * Reset priority state.
     */
    public void reset() {
        currentPriorityPlayer = null;
        passCount = 0;
    }
}
