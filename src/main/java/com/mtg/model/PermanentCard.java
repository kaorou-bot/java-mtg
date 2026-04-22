package com.mtg.model;

import java.util.List;

/**
 * PermanentCard - 永久物卡牌基类。
 *
 * 【功能说明】
 * - 所有永久物类型（生物、法器、地、结界、鹏洛客、战斗）的基类
 * - 管理永久物的通用状态（横置、召唤 sickness、传奇状态）
 *
 * 【规则依据】
 * - Rule 110: 永久物
 * - Rule 302: 生物
 * - Rule 110.1: 永久物是进入战场后一直留在战场的牌或衍生物
 *
 * 【设计决策】
 * - tapped: 标记永久物是否横置
 * - summoningSickness: 标记生物是否有召唤 sickness
 * - legendary: 标记是否为传奇
 */
public abstract class PermanentCard extends Card {
    private boolean tapped;  // 是否横置
    private boolean summoningSickness;  // 是否有召唤 sickness
    private boolean legendary;  // 是否为传奇

    /**
     * 创建永久物卡牌。
     *
     * @param name 名称
     * @param manaCost 法力费用
     * @param description 规则描述
     * @param type 牌张类型
     * @param color 颜色列表
     */
    public PermanentCard(String name, ManaCost manaCost, String description,
                         CardType type, List<ManaType> color) {
        super(name, manaCost, description, type, color);
        this.tapped = false;
        this.summoningSickness = true;  // 新进入战场的永久物默认有召唤 sickness
        this.legendary = name != null && name.contains("Legendary");
    }

    // ========== 横置状态 ==========

    public boolean isTapped() {
        return tapped;
    }

    /**
     * 横置此永久物。
     */
    public void tap() {
        this.tapped = true;
    }

    /**
     * 取消横置此永久物。
     */
    public void untap() {
        this.tapped = false;
    }

    // ========== 召唤 sickness ==========

    public boolean hasSummoningSickness() {
        return summoningSickness;
    }

    /**
     * 清除召唤 sickness。
     */
    public void clearSummoningSickness() {
        this.summoningSickness = false;
    }

    /**
     * 重置回合状态。
     */
    public void resetTurn() {
        this.tapped = false;
        this.summoningSickness = false;
    }

    // ========== 传奇状态 ==========

    public boolean isLegendary() {
        return legendary;
    }

    public void setLegendary(boolean legendary) {
        this.legendary = legendary;
    }

    /**
     * 获取永久物子类型名称。
     *
     * @return 子类型名称
     */
    public abstract String getSubtypeName();
}
