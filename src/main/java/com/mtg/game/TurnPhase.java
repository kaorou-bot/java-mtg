package com.mtg.game;

/**
 * TurnPhase represents all phases and steps in a Magic turn.
 *
 * Based on Rules 500-514:
 * - Beginning Phase (501): Untap (502), Upkeep (503), Draw (504)
 * - Precombat Main Phase (505)
 * - Combat Phase (506): Beginning of Combat (507), Declare Attackers (508),
 *   Declare Blockers (509), Combat Damage (510), End of Combat (511)
 * - Postcombat Main Phase (505)
 * - Ending Phase (512): End Step (513), Cleanup Step (514)
 */
public enum TurnPhase {
    // Beginning Phase
    UNTAAP("Untap Step", "Beginning Phase"),
    UPKEEP("Upkeep Step", "Beginning Phase"),
    DRAW("Draw Step", "Beginning Phase"),

    // Main Phases
    MAIN1("Precombat Main Phase", "Main Phase"),
    MAIN2("Postcombat Main Phase", "Main Phase"),

    // Combat Phase
    COMBAT_START("Beginning of Combat", "Combat Phase"),
    DECLARE_ATTACKERS("Declare Attackers", "Combat Phase"),
    DECLARE_BLOCKERS("Declare Blockers", "Combat Phase"),
    COMBAT_DAMAGE("Combat Damage", "Combat Phase"),
    COMBAT_DAMAGE_FIRST("First Strike Damage", "Combat Phase"),
    COMBAT_END("End of Combat", "Combat Phase"),

    // Ending Phase
    END("End Step", "Ending Phase"),
    CLEANUP("Cleanup Step", "Ending Phase"),

    GAME_OVER("Game Over", "");

    private final String name;
    private final String category;

    TurnPhase(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Check if this is a main phase.
     * Rule 505.6b: Land can only be played in main phase with empty stack.
     */
    public boolean isMainPhase() {
        return this == MAIN1 || this == MAIN2;
    }

    /**
     * Check if this is the untap step.
     * Rule 502.4: No priority during untap step.
     */
    public boolean isUntapStep() {
        return this == UNTAAP;
    }

    /**
     * Check if this is the cleanup step.
     * Rule 514.3: Players don't receive priority during cleanup unless triggered abilities.
     */
    public boolean isCleanupStep() {
        return this == CLEANUP;
    }

    /**
     * Check if priority is received during this phase.
     * Rule 502.4: No priority during untap.
     * Rule 514.3: Limited priority during cleanup.
     */
    public boolean hasPriority() {
        return !isUntapStep();
    }

    /**
     * Check if this is a combat phase.
     */
    public boolean isCombatPhase() {
        return this == COMBAT_START || this == DECLARE_ATTACKERS ||
               this == DECLARE_BLOCKERS || this == COMBAT_DAMAGE ||
               this == COMBAT_DAMAGE_FIRST || this == COMBAT_END;
    }

    /**
     * Check if this is the beginning phase.
     */
    public boolean isBeginningPhase() {
        return this == UNTAAP || this == UPKEEP || this == DRAW;
    }

    /**
     * Check if this is the ending phase.
     */
    public boolean isEndingPhase() {
        return this == END || this == CLEANUP;
    }
}
