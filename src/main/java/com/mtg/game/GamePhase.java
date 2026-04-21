package com.mtg.game;

public enum GamePhase {
    UNTAP("Untap", "Untap Phase"),
    UPKEEP("Upkeep", "Upkeep Phase"),
    DRAW("Draw", "Draw Phase"),
    MAIN1("Main 1", "Main Phase 1"),
    COMBAT_BEGIN("Combat Begin", "Begin Combat"),
    DECLARE_ATTACKERS("Attackers", "Declare Attackers"),
    DECLARE_BLOCKERS("Blockers", "Declare Blockers"),
    COMBAT_DAMAGE("Damage", "Combat Damage"),
    COMBAT_END("Combat End", "End Combat"),
    MAIN2("Main 2", "Main Phase 2"),
    END("End", "End Phase"),
    GAME_OVER("Game Over", "Game Over");

    private final String name;
    private final String description;

    GamePhase(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
