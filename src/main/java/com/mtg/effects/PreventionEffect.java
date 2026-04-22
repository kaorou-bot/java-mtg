package com.mtg.effects;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.game.Game;

/**
 * PreventionEffect - 预防效果类（Rule 615）。
 *
 * 【功能说明】
 * - 持续效果，监视特定事件并防止伤害/抓牌/效果
 *
 * 【规则依据】
 * - Rule 615.1: 预防效果是持续效果
 *
 * 【示例】
 * - "Prevent all damage that target creature would deal this turn."
 */
public abstract class PreventionEffect {
    protected String name;  // 效果名称
    protected Card sourceCard;  // 源卡牌
    protected Player controller;  // 控制者
    protected int amount;  // 预防数量（0 = 全部）
    protected String targetType;  // 目标类型（"creature", "player", "permanent"）

    /**
     * 创建预防效果。
     *
     * @param name 效果名称
     * @param sourceCard 源卡牌
     * @param controller 控制者
     */
    public PreventionEffect(String name, Card sourceCard, Player controller) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.amount = 0;  // 0 表示全部
    }

    /**
     * 检查此预防效果是否适用于给定事件。
     *
     * @param event 游戏事件
     * @param game 游戏
     * @return 是否适用
     */
    public abstract boolean applies(GameEvent event, Game game);

    /**
     * 应用此预防效果。
     *
     * @param event 游戏事件
     * @param game 游戏
     * @return 应用预防后的修改事件
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}