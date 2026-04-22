package com.mtg.abilities;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.game.Game;

/**
 * TriggeredAbility - 触发式异能类（Rule 603）。
 *
 * 【功能说明】
 * - 表示当触发条件满足时触发的异能
 * - 触发后进入堆叠，可以被响应
 *
 * 【规则依据】
 * - Rule 603.1: 触发式异能有触发条件和一个效果
 *   当触发条件发生时，异能触发并进入堆叠
 * - Rule 603.2: 触发式异能只在触发条件发生的时刻检查游戏状态
 *   （除非是延迟触发式异能）
 *
 * 【设计决策】
 * - 使用 checkTrigger() 检查触发条件
 * - 使用 execute() 执行异能效果
 * - 子类实现具体的触发条件和效果
 */
public abstract class TriggeredAbility {
    protected String name;  // 异能名称
    protected Card sourceCard;  // 源卡牌
    protected Player controller;  // 控制者
    protected boolean isOptional;  // 是否可选
    protected String memoryNote;  // 记忆备注（用于追踪信息）

    /**
     * 创建触发式异能。
     *
     * @param name 异能名称
     * @param sourceCard 源卡牌
     * @param controller 控制者
     */
    public TriggeredAbility(String name, Card sourceCard, Player controller) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.isOptional = false;
    }

    /**
     * 检查触发条件是否满足。
     *
     * 【默认实现】
     * - 返回 false
     * - 子类重写以实现具体的触发检查
     *
     * @param game 游戏
     * @return 是否满足触发条件
     */
    public boolean checkTrigger(Game game) {
        return false;
    }

    /**
     * 获取异能描述。
     *
     * @return 异能描述文本
     */
    public abstract String getDescription();

    /**
     * 执行异能效果。
     *
     * @param game 游戏
     */
    public abstract void execute(Game game);

    /**
     * 设置记忆备注（用于追踪信息的异能）。
     *
     * @param note 备注内容
     */
    public void setMemory(String note) {
        this.memoryNote = note;
    }

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        this.isOptional = optional;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}