package com.mtg.game;

import com.mtg.model.Card;
import com.mtg.player.Player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GameLog - 游戏日志系统，记录所有游戏动作。
 *
 * 【功能说明】
 * - 记录所有游戏动作（出牌、战斗、伤害等）
 * - 提供日志查询和回放功能
 * - 支持调试和游戏回放
 *
 * 【日志类型】
 * - 普通日志：一般游戏信息
 * - 动作日志：玩家执行的动作
 * - 阶段日志：回合/阶段变化
 * - 伤害日志：伤害记录
 * - 区域日志：区域转换
 *
 * 【设计决策】
 * - 使用 LogEntry 存储每条日志
 * - 包含时间戳、回合数、阶段、玩家等信息
 * - 支持导出完整日志
 */
public class GameLog {
    private final List<LogEntry> entries;  // 日志条目列表
    private int currentTurn;  // 当前回合数
    private TurnPhase currentPhase;  // 当前阶段

    /**
     * 创建游戏日志。
     */
    public GameLog() {
        this.entries = new ArrayList<>();
        this.currentTurn = 1;
        this.currentPhase = TurnPhase.UNTAAP;
    }

    /**
     * 添加普通日志。
     *
     * @param message 日志消息
     */
    public void log(String message) {
        entries.add(new LogEntry(currentTurn, currentPhase, null, LogType.INFO, message));
    }

    /**
     * 记录玩家动作。
     *
     * @param player 玩家
     * @param action 动作描述
     */
    public void logAction(Player player, String action) {
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.ACTION, action));
    }

    /**
     * 记录阶段变化。
     *
     * @param phase 新阶段
     * @param player 当前玩家
     */
    public void logPhase(TurnPhase phase, Player player) {
        this.currentPhase = phase;
        String playerName = player != null ? player.getName() : "Unknown";
        String message = playerName + " - " + phase.getName();
        entries.add(new LogEntry(currentTurn, phase, player, LogType.PHASE, message));
    }

    /**
     * 记录回合开始。
     *
     * @param turn 回合数
     * @param player 主动玩家
     */
    public void logTurnStart(int turn, Player player) {
        this.currentTurn = turn;
        String playerName = player != null ? player.getName() : "Unknown";
        String message = "=== Turn " + turn + ": " + playerName + " ===";
        entries.add(new LogEntry(turn, currentPhase, player, LogType.TURN, message));
    }

    /**
     * 记录伤害事件。
     *
     * @param target 目标
     * @param amount 伤害量
     * @param source 伤害来源
     */
    public void logDamage(Player target, int amount, String source) {
        String targetName = target != null ? target.getName() : "Unknown";
        String message = source + " deals " + amount + " damage to " + targetName;
        entries.add(new LogEntry(currentTurn, currentPhase, target, LogType.DAMAGE, message));
    }

    /**
     * 记录生物伤害。
     *
     * @param target 目标生物
     * @param amount 伤害量
     * @param source 伤害来源
     */
    public void logCreatureDamage(String target, int amount, String source) {
        String message = source + " deals " + amount + " damage to " + target;
        entries.add(new LogEntry(currentTurn, currentPhase, null, LogType.DAMAGE, message));
    }

    /**
     * 记录生命变化。
     *
     * @param player 玩家
     * @param newLife 新生命值
     * @param change 变化量
     */
    public void logLifeChange(Player player, int newLife, int change) {
        String sign = change >= 0 ? "+" : "";
        String playerName = player != null ? player.getName() : "Unknown";
        String message = playerName + " life: " + newLife + " (" + sign + change + ")";
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.LIFE, message));
    }

    /**
     * 记录区域转换。
     *
     * @param card 卡牌
     * @param fromZone 原区域
     * @param toZone 新区域
     */
    public void logZoneChange(Card card, String fromZone, String toZone) {
        String cardName = card != null ? card.getName() : "Unknown";
        String message = cardName + ": " + fromZone + " -> " + toZone;
        Player controller = card != null ? card.getController() : null;
        entries.add(new LogEntry(currentTurn, currentPhase, controller, LogType.ZONE_CHANGE, message));
    }

    /**
     * 记录出牌。
     *
     * @param player 玩家
     * @param card 卡牌
     */
    public void logCardPlayed(Player player, Card card) {
        String playerName = player != null ? player.getName() : "Unknown";
        String cardName = card != null ? card.getName() : "Unknown";
        String message = playerName + " plays " + cardName;
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.CARD_PLAYED, message));
    }

    /**
     * 记录咒语施放。
     *
     * @param player 玩家
     * @param card 咒语卡牌
     */
    public void logSpellCast(Player player, Card card) {
        String playerName = player != null ? player.getName() : "Unknown";
        String cardName = card != null ? card.getName() : "Unknown";
        String message = playerName + " casts " + cardName;
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.SPELL_CAST, message));
    }

    /**
     * 记录咒语结算。
     *
     * @param card 结算的咒语
     */
    public void logSpellResolve(Card card) {
        String cardName = card != null ? card.getName() : "Unknown";
        String message = cardName + " resolves";
        entries.add(new LogEntry(currentTurn, currentPhase, null, LogType.SPELL_RESOLVE, message));
    }

    /**
     * 记录战斗宣告。
     *
     * @param attacker 攻击生物
     * @param target 攻击目标
     */
    public void logAttack(String attacker, String target) {
        String message = attacker + " attacks " + target;
        entries.add(new LogEntry(currentTurn, currentPhase, null, LogType.ATTACK, message));
    }

    /**
     * 记录战斗阻挡。
     *
     * @param blocker 阻挡生物
     * @param attacker 被阻挡的攻击者
     */
    public void logBlock(String blocker, String attacker) {
        String message = blocker + " blocks " + attacker;
        entries.add(new LogEntry(currentTurn, currentPhase, null, LogType.BLOCK, message));
    }

    /**
     * 记录永久物消灭。
     *
     * @param card 消灭的卡牌
     * @param reason 原因
     */
    public void logDestroyed(String card, String reason) {
        String message = card + " destroyed" + (reason != null ? " (" + reason + ")" : "");
        entries.add(new LogEntry(currentTurn, currentPhase, null, LogType.DESTROYED, message));
    }

    /**
     * 记录游戏结束。
     *
     * @param winner 获胜玩家
     * @param reason 获胜原因
     */
    public void logGameOver(Player winner, String reason) {
        String winnerName = winner != null ? winner.getName() : "Unknown";
        String message = "=== GAME OVER: " + winnerName + " wins (" + reason + ") ===";
        entries.add(new LogEntry(currentTurn, currentPhase, winner, LogType.GAME_OVER, message));
    }

    /**
     * 记录优先权传递。
     *
     * @param player 传递优先权的玩家
     */
    public void logPriorityPassed(Player player) {
        String playerName = player != null ? player.getName() : "Unknown";
        String message = playerName + " passes priority";
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.PRIORITY, message));
    }

    /**
     * 记录弃牌。
     *
     * @param player 弃牌玩家
     * @param card 弃置的卡牌
     */
    public void logDiscard(Player player, Card card) {
        String playerName = player != null ? player.getName() : "Unknown";
        String cardName = card != null ? card.getName() : "Unknown";
        String message = playerName + " discards " + cardName;
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.DISCARD, message));
    }

    /**
     * 记录抓牌。
     *
     * @param player 抓牌玩家
     * @param cardName 抓到的牌名（null 表示未知）
     */
    public void logDraw(Player player, String cardName) {
        String playerName = player != null ? player.getName() : "Unknown";
        String message = playerName + " draws a card";
        if (cardName != null) {
            message += " (" + cardName + ")";
        }
        entries.add(new LogEntry(currentTurn, currentPhase, player, LogType.DRAW, message));
    }

    /**
     * 获取完整日志文本。
     *
     * @return 格式化后的日志字符串
     */
    public String getFullLog() {
        StringBuilder sb = new StringBuilder();
        for (LogEntry entry : entries) {
            sb.append(entry.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取指定回合后的所有日志。
     *
     * @param fromIndex 起始索引
     * @return 日志条目列表
     */
    public List<LogEntry> getEntriesSince(int fromIndex) {
        if (fromIndex < 0 || fromIndex >= entries.size()) {
            return new ArrayList<>(entries);
        }
        return new ArrayList<>(entries.subList(fromIndex, entries.size()));
    }

    /**
     * 获取所有日志条目。
     *
     * @return 日志条目列表
     */
    public List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * 获取日志条目数量。
     *
     * @return 条目数量
     */
    public int size() {
        return entries.size();
    }

    /**
     * 清空日志。
     */
    public void clear() {
        entries.clear();
        currentTurn = 1;
        currentPhase = TurnPhase.UNTAAP;
    }

    /**
     * 获取最新的一条日志。
     *
     * @return 最新日志条目，无则返回 null
     */
    public LogEntry getLastEntry() {
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(entries.size() - 1);
    }

    /**
     * 获取指定回合的所有日志。
     *
     * @param turn 回合数
     * @return 日志条目列表
     */
    public List<LogEntry> getEntriesForTurn(int turn) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : entries) {
            if (entry.turnNumber == turn) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * 获取指定类型的日志。
     *
     * @param type 日志类型
     * @return 日志条目列表
     */
    public List<LogEntry> getEntriesByType(LogType type) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : entries) {
            if (entry.type == type) {
                result.add(entry);
            }
        }
        return result;
    }

    // ========== 日志条目类 ==========

    /**
     * LogEntry - 日志条目，表示单条日志记录。
     *
     * 【字段】
     * - turnNumber: 回合数
     * - phase: 当前阶段
     * - player: 涉及的玩家
     * - type: 日志类型
     * - message: 日志消息
     * - timestamp: 时间戳
     */
    public static class LogEntry {
        public final int turnNumber;  // 回合数
        public final TurnPhase phase;  // 当前阶段
        public final Player player;  // 涉及的玩家（可为 null）
        public final LogType type;  // 日志类型
        public final String message;  // 日志消息
        public final Instant timestamp;  // 时间戳
        public final String formattedTime;  // 格式化时间

        public LogEntry(int turnNumber, TurnPhase phase, Player player, LogType type, String message) {
            this.turnNumber = turnNumber;
            this.phase = phase;
            this.player = player;
            this.type = type;
            this.message = message;
            this.timestamp = Instant.now();
            this.formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        @Override
        public String toString() {
            return String.format("[%s T%d/%s] %s", formattedTime, turnNumber, phase.getName(), message);
        }
    }

    /**
     * LogType - 日志类型枚举。
     */
    public enum LogType {
        INFO,           // 普通信息
        ACTION,          // 玩家动作
        PHASE,           // 阶段变化
        TURN,            // 回合开始
        DAMAGE,          // 伤害
        LIFE,            // 生命变化
        ZONE_CHANGE,     // 区域转换
        CARD_PLAYED,     // 出牌
        SPELL_CAST,      // 施放咒语
        SPELL_RESOLVE,   // 咒语结算
        ATTACK,          // 攻击宣告
        BLOCK,           // 阻挡宣告
        DESTROYED,       // 消灭
        GAME_OVER,       // 游戏结束
        PRIORITY,        // 优先权
        DISCARD,         // 弃牌
        DRAW             // 抓牌
    }
}
