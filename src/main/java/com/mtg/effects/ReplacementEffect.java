package com.mtg.effects;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.game.Game;

/**
 * ReplacementEffect - 替代效果类（Rule 614）。
 *
 * 【功能说明】
 * - 持续监视特定事件并修改或替换它
 * - "instead" 关键词表示替代效果
 *
 * 【规则依据】
 * - Rule 614.1: 某些持续效果是替代效果
 * - Rule 614.12: 替代效果使用 "instead" 将事件替换为一个或多个其他事件
 *
 * 【示例】
 * - "If a source would deal damage to you, prevent 1 of that damage."
 *
 * 【设计决策】
 * - applies() 检查效果是否适用
 * - apply() 执行效果修改
 */
public abstract class ReplacementEffect {
    protected String name;  // 效果名称
    protected Card sourceCard;  // 源卡牌
    protected Player controller;  // 控制者
    protected String eventType;  // 事件类型（"damage", "draw", "enterBattlefield" 等）
    protected boolean isReplacement;  // true = 替代，false = 防止

    /**
     * 创建替代效果。
     *
     * @param name 效果名称
     * @param sourceCard 源卡牌
     * @param controller 控制者
     * @param eventType 事件类型
     */
    public ReplacementEffect(String name, Card sourceCard, Player controller, String eventType) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.eventType = eventType;
        this.isReplacement = true;
    }

    /**
     * 检查此替代效果是否适用于给定事件。
     *
     * @param event 游戏事件
     * @param game 游戏
     * @return 是否适用
     */
    public abstract boolean applies(GameEvent event, Game game);

    /**
     * 应用此替代效果修改事件。
     *
     * @param event 游戏事件
     * @param game 游戏
     * @return 修改后的事件（返回 null 表示取消事件）
     */
    public abstract GameEvent apply(GameEvent event, Game game);

    /**
     * 获取效果描述。
     *
     * @return 效果描述文本
     */
    public abstract String getDescription();

    // ========== Getter 方法 ==========

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isReplacement() {
        return isReplacement;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}