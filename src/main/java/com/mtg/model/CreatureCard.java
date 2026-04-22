package com.mtg.model;

import java.util.List;

/**
 * CreatureCard - 生物卡牌类。
 *
 * 【功能说明】
 * - 存储生物的力量/防御力
 * - 管理生物的特殊能力（飞行、先攻等）
 * - 管理伤害标记和召唤 sickness
 *
 * 【关键词异能】
 * - 战斗异能：Flying, First Strike, Double Strike, Trample, Vigilance, Deathtouch, Haste
 * - 这些能力影响战斗宣告和伤害结算
 *
 * 【规则依据】
 * - Rule 302: 生物
 * - Rule 302.5: 召唤 sickness
 * - Rule 510: 战斗伤害
 * - Section 702: 关键词异能
 *
 * 【设计决策】
 * - 使用 currentPower/currentToughness 追踪当前状态
 * - 使用 base power/toughness 存储基础值
 * - 伤害标记单独存储（markedDamage）
 */
public class CreatureCard extends PermanentCard {
    private int power;               // 基础力量
    private int toughness;           // 基础防御力
    private int currentPower;        // 当前力量（可能被增强影响）
    private int currentToughness;    // 当前防御力
    private int markedDamage;        // 标记伤害
    private boolean hasFirstStrike;   // 先攻
    private boolean hasDoubleStrike; // 双重打击
    private boolean hasFlying;       // 飞行
    private boolean hasVigilance;    // 警戒
    private boolean hasTrample;      // 践踏
    private boolean hasDeathtouch;    // 致命
    private boolean hasHaste;         // 敏捷

    /**
     * 创建生物卡牌。
     *
     * @param name 名称
     * @param manaCost 法力费用
     * @param description 规则描述
     * @param color 颜色
     * @param power 力量
     * @param toughness 防御力
     * @param hasFirstStrike 是否有先攻
     * @param hasDoubleStrike 是否有双重打击
     * @param hasFlying 是否有飞行
     * @param hasVigilance 是否有警戒
     * @param hasTrample 是否有践踏
     */
    public CreatureCard(String name, ManaCost manaCost, String description,
                        List<ManaType> color, int power, int toughness,
                        boolean hasFirstStrike, boolean hasDoubleStrike,
                        boolean hasFlying, boolean hasVigilance, boolean hasTrample) {
        super(name, manaCost, description, CardType.CREATURE, color);
        this.power = power;
        this.toughness = toughness;
        this.currentPower = power;
        this.currentToughness = toughness;
        this.markedDamage = 0;
        this.hasFirstStrike = hasFirstStrike;
        this.hasDoubleStrike = hasDoubleStrike;
        this.hasFlying = hasFlying;
        this.hasVigilance = hasVigilance;
        this.hasTrample = hasTrample;
        this.hasDeathtouch = false;
        this.hasHaste = false;
    }

    @Override
    public String getSubtypeName() {
        return "Creature";
    }

    // ========== 属性访问 ==========

    public int getPower() {
        return power;
    }

    /**
     * 获取当前防御力。
     *
     * 【注意】
     * - 返回 currentToughness 而非 base toughness
     * - 当前防御力可能被增强异能影响
     *
     * @return 当前防御力
     */
    public int getToughness() {
        return currentToughness;
    }

    public int getBaseToughness() {
        return toughness;
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public int getCurrentToughness() {
        return currentToughness;
    }

    public int getMarkedDamage() {
        return markedDamage;
    }

    // ========== 伤害管理 ==========

    /**
     * 添加标记伤害。
     *
     * @param amount 伤害量
     */
    public void addDamage(int amount) {
        this.markedDamage += amount;
    }

    /**
     * 清除所有标记伤害。
     *
     * 【调用时机】
     * - 清理步骤（Rule 514.2）
     */
    public void clearDamage() {
        this.markedDamage = 0;
    }

    // ========== 攻击检查 ==========

    /**
     * 检查是否可以攻击。
     *
     * 【条件】
     * - 未横置
     * - 无召唤 sickness 或有敏捷
     *
     * @return 是否可以攻击
     */
    public boolean canAttack() {
        return !isTapped() && (!hasSummoningSickness() || hasHaste);
    }

    // ========== 关键词异能 ==========

    public boolean hasFirstStrike() {
        return hasFirstStrike;
    }

    public boolean hasDoubleStrike() {
        return hasDoubleStrike;
    }

    public boolean hasFlying() {
        return hasFlying;
    }

    public boolean hasVigilance() {
        return hasVigilance;
    }

    public boolean hasTrample() {
        return hasTrample;
    }

    public boolean hasDeathtouch() {
        return hasDeathtouch;
    }

    public boolean hasHaste() {
        return hasHaste;
    }

    /**
     * 设置敏捷。
     *
     * @param hasHaste 是否有敏捷
     */
    public void setHaste(boolean hasHaste) {
        this.hasHaste = hasHaste;
    }

    // ========== 关键词异能 Setter（用于效果） ==========

    /**
     * 设置先攻异能。
     *
     * @param hasFirstStrike 是否有先攻
     */
    public void setHasFirstStrike(boolean hasFirstStrike) {
        this.hasFirstStrike = hasFirstStrike;
    }

    public void setHasDoubleStrike(boolean hasDoubleStrike) {
        this.hasDoubleStrike = hasDoubleStrike;
    }

    public void setHasFlying(boolean hasFlying) {
        this.hasFlying = hasFlying;
    }

    public void setHasVigilance(boolean hasVigilance) {
        this.hasVigilance = hasVigilance;
    }

    public void setHasTrample(boolean hasTrample) {
        this.hasTrample = hasTrample;
    }

    public void setHasDeathtouch(boolean hasDeathtouch) {
        this.hasDeathtouch = hasDeathtouch;
    }

    // ========== 状态重置 ==========

    /**
     * 重置生物所有状态（回合开始时调用）。
     *
     * 【重置内容】
     * - 当前力量 = 基础力量
     * - 当前防御力 = 基础防御力
     * - 标记伤害 = 0
     * - 取消横置
     *
     * 【调用时机】
     * - TurnManager.startTurn()
     */
    public void resetStats() {
        this.currentPower = power;
        this.currentToughness = toughness;
        this.markedDamage = 0;
        untap();
    }

    /**
     * 只重置力量/防御力（不清除伤害标记）。
     *
     * 【与 resetStats 的区别】
     * - 不清除标记伤害
     * - 不取消横置
     *
     * @see #resetStats()
     */
    public void resetForTurn() {
        this.currentPower = power;
        this.currentToughness = toughness;
    }

    /**
     * 修改力量。
     *
     * @param amount 变化量（正数增加，负数减少）
     */
    public void modifyPower(int amount) {
        this.currentPower += amount;
    }

    /**
     * 修改防御力。
     *
     * @param amount 变化量（正数增加，负数减少）
     */
    public void modifyToughness(int amount) {
        this.currentToughness += amount;
    }

    // ========== 状态检查 ==========

    /**
     * 检查生物是否被消灭（防御力 ≤ 0）。
     *
     * @return 是否应被消灭
     */
    public boolean isDestroyed() {
        return currentToughness <= 0;
    }

    /**
     * 检查是否有致命伤害（标记伤害 ≥ 防御力）。
     *
     * 【规则依据】
     * - Rule 704.5g: 标记伤害 ≥ 防御力 → 致命伤害
     *
     * @return 是否有致命伤害
     */
    public boolean hasLethalDamage() {
        return markedDamage >= currentToughness;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %d/%d", getName(), getManaCost().toDisplayString(), currentPower, currentToughness);
    }

    /**
     * 获取异能描述字符串。
     *
     * @return 关键词异能描述
     */
    public String getAbilitiesString() {
        StringBuilder sb = new StringBuilder();
        if (hasFirstStrike) sb.append("First Strike ");
        if (hasDoubleStrike) sb.append("Double Strike ");
        if (hasFlying) sb.append("Flying ");
        if (hasVigilance) sb.append("Vigilance ");
        if (hasTrample) sb.append("Trample ");
        if (hasDeathtouch) sb.append("Deathtouch ");
        if (hasHaste) sb.append("Haste ");
        return sb.toString().trim();
    }
}
