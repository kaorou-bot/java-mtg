package com.mtg.effects;

import com.mtg.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * GameEvent - 游戏事件类，表示可以被效果修改的游戏事件。
 *
 * 【功能说明】
 * - 表示游戏中发生的各种事件（伤害、抓牌、区域转换等）
 * - 替代效果和预防效果可以修改这些事件
 * - 存储事件的详细信息（来源、目标、数量等）
 *
 * 【用途】
 * - Rule 614/615: 替代效果和预防效果修改事件
 * - TriggeredAbility: 检查触发条件时使用
 */
public class GameEvent {
    private String eventType;  // 事件类型
    private Object source;  // 来源（卡牌、异能或其他来源）
    private Player player;  // 受影响玩家
    private Object target;  // 事件目标
    private int amount;  // 数量（用于伤害、抓牌等）
    private Map<String, Object> properties;  // 其他属性

    // 事件类型常量
    public static final String DAMAGE = "damage";  // 伤害事件
    public static final String DRAW = "draw";  // 抓牌事件
    public static final String ENTER_BATTLEFIELD = "enterBattlefield";  // 进入战场
    public static final String LEAVE_BATTLEFIELD = "leaveBattlefield";  // 离开战场
    public static final String LIFE_CHANGE = "lifeChange";  // 生命变化

    /**
     * 创建游戏事件。
     *
     * @param eventType 事件类型
     * @param source 来源
     * @param player 受影响玩家
     */
    public GameEvent(String eventType, Object source, Player player) {
        this.eventType = eventType;
        this.source = source;
        this.player = player;
        this.amount = 0;
        this.properties = new HashMap<>();
    }

    public String getEventType() {
        return eventType;
    }

    public Object getSource() {
        return source;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * 添加属性。
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * 获取属性。
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public String toString() {
        return "GameEvent[" + eventType + " source=" + source + " player=" + player +
               " amount=" + amount + "]";
    }
}