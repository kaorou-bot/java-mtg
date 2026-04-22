package com.mtg.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameLog 单元测试。
 */
class GameLogTest {

    private GameLog gameLog;

    @BeforeEach
    void setUp() {
        gameLog = new GameLog();
    }

    @Test
    void testLogCreation() {
        assertNotNull(gameLog);
        assertEquals(0, gameLog.size());
    }

    @Test
    void testLog() {
        gameLog.log("Test message");
        assertEquals(1, gameLog.size());
    }

    @Test
    void testLogPhase() {
        gameLog.logPhase(com.mtg.game.TurnPhase.MAIN1, null);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.PHASE, entry.type);
        assertEquals("Precombat Main Phase", entry.phase.getName());
    }

    @Test
    void testLogTurnStart() {
        gameLog.logTurnStart(1, null);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.TURN, entry.type);
        assertEquals(1, entry.turnNumber);
    }

    @Test
    void testLogDamage() {
        gameLog.logDamage(null, 3, "Lightning Bolt");
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.DAMAGE, entry.type);
        assertTrue(entry.message.contains("Lightning Bolt"));
        assertTrue(entry.message.contains("3"));
    }

    @Test
    void testLogCardPlayed() {
        gameLog.logCardPlayed(null, null);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.CARD_PLAYED, entry.type);
    }

    @Test
    void testLogAttack() {
        gameLog.logAttack("Grizzly Bears", "Player 2");
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.ATTACK, entry.type);
        assertTrue(entry.message.contains("Grizzly Bears"));
        assertTrue(entry.message.contains("Player 2"));
    }

    @Test
    void testLogBlock() {
        gameLog.logBlock("Wall", "Grizzly Bears");
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.BLOCK, entry.type);
        assertTrue(entry.message.contains("Wall"));
        assertTrue(entry.message.contains("Grizzly Bears"));
    }

    @Test
    void testLogDestroyed() {
        gameLog.logDestroyed("Grizzly Bears", "lethal damage");
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.DESTROYED, entry.type);
        assertTrue(entry.message.contains("Grizzly Bears"));
    }

    @Test
    void testLogLifeChange() {
        com.mtg.player.Player player = new com.mtg.player.Player("Test");
        gameLog.logLifeChange(player, 17, -3);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.LIFE, entry.type);
    }

    @Test
    void testGetEntries() {
        gameLog.log("Message 1");
        gameLog.log("Message 2");
        gameLog.log("Message 3");

        assertEquals(3, gameLog.size());
        assertEquals(3, gameLog.getEntries().size());
    }

    @Test
    void testGetEntriesSince() {
        gameLog.log("Message 1");
        gameLog.log("Message 2");
        gameLog.log("Message 3");

        var entries = gameLog.getEntriesSince(1);
        assertEquals(2, entries.size());
    }

    @Test
    void testGetEntriesForTurn() {
        gameLog.logTurnStart(1, null);
        gameLog.log("Turn 1 message");

        gameLog.logTurnStart(2, null);
        gameLog.log("Turn 2 message");

        var turn1Entries = gameLog.getEntriesForTurn(1);
        assertEquals(2, turn1Entries.size());

        var turn2Entries = gameLog.getEntriesForTurn(2);
        assertEquals(2, turn2Entries.size());
    }

    @Test
    void testGetEntriesByType() {
        gameLog.log("Info");
        gameLog.logPhase(com.mtg.game.TurnPhase.DRAW, null);
        gameLog.logDamage(null, 3, "Test");

        var infoEntries = gameLog.getEntriesByType(GameLog.LogType.INFO);
        assertEquals(1, infoEntries.size());

        var phaseEntries = gameLog.getEntriesByType(GameLog.LogType.PHASE);
        assertEquals(1, phaseEntries.size());

        var damageEntries = gameLog.getEntriesByType(GameLog.LogType.DAMAGE);
        assertEquals(1, damageEntries.size());
    }

    @Test
    void testClear() {
        gameLog.log("Message");
        assertEquals(1, gameLog.size());

        gameLog.clear();
        assertEquals(0, gameLog.size());
    }

    @Test
    void testGetFullLog() {
        gameLog.log("Message 1");
        gameLog.log("Message 2");

        String fullLog = gameLog.getFullLog();
        assertNotNull(fullLog);
        assertTrue(fullLog.contains("Message 1"));
        assertTrue(fullLog.contains("Message 2"));
    }

    @Test
    void testLogEntryToString() {
        gameLog.logTurnStart(1, null);
        GameLog.LogEntry entry = gameLog.getLastEntry();

        String str = entry.toString();
        assertNotNull(str);
        assertTrue(str.contains("Turn 1"));
    }

    @Test
    void testLogSpellCast() {
        gameLog.logSpellCast(null, null);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.SPELL_CAST, entry.type);
    }

    @Test
    void testLogSpellResolve() {
        gameLog.logSpellResolve(null);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.SPELL_RESOLVE, entry.type);
    }

    @Test
    void testLogGameOver() {
        com.mtg.player.Player winner = new com.mtg.player.Player("Winner");
        gameLog.logGameOver(winner, "life reached 0");
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.GAME_OVER, entry.type);
        assertTrue(entry.message.contains("Winner"));
    }

    @Test
    void testLogPriorityPassed() {
        com.mtg.player.Player player = new com.mtg.player.Player("Test");
        gameLog.logPriorityPassed(player);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.PRIORITY, entry.type);
    }

    @Test
    void testLogDiscard() {
        gameLog.logDiscard(null, null);
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.DISCARD, entry.type);
    }

    @Test
    void testLogDraw() {
        gameLog.logDraw(null, "Lightning Bolt");
        assertEquals(1, gameLog.size());

        GameLog.LogEntry entry = gameLog.getLastEntry();
        assertEquals(GameLog.LogType.DRAW, entry.type);
        assertTrue(entry.message.contains("Lightning Bolt"));
    }
}
