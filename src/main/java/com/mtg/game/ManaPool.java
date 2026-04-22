package com.mtg.game;

import com.mtg.model.ManaType;

import java.util.HashMap;
import java.util.Map;

/**
 * ManaPool - 法力池，管理玩家的可用法力。
 *
 * 【功能说明】
 * - 存储玩家当前可用的各种法力
 * - 提供法力添加、支付、清空等功能
 * - 追踪每种法力的数量
 *
 * 【规则依据】
 * - Rule 106: 法力
 * - Rule 106.4: 法力池在阶段/步骤结束时清空
 * - Rule 106.4a: 玩家选择如何支付未使用的法力
 *
 * 【设计决策】
 * - 使用 Map<ManaType, Integer> 存储每种法力的数量
 * - 提供 canPay() 检查是否可以支付费用
 * - 支付时减少对应法力数量
 */
public class ManaPool {
    private final Map<ManaType, Integer> mana;  // 法力存储
    private int totalMana;  // 总法力数量

    /**
     * 创建法力池。
     */
    public ManaPool() {
        this.mana = new HashMap<>();
        this.totalMana = 0;
        // 初始化所有法力类型为0
        for (ManaType type : ManaType.values()) {
            mana.put(type, 0);
        }
    }

    /**
     * 添加法力到法力池。
     *
     * @param type 法力类型
     * @param amount 法力数量
     */
    public void add(ManaType type, int amount) {
        if (type == null) return;
        int current = mana.getOrDefault(type, 0);
        mana.put(type, current + amount);
        totalMana += amount;
    }

    /**
     * 添加任意颜色法力。
     *
     * @param amount 法力数量
     */
    public void addGeneric(int amount) {
        mana.put(ManaType.COLORLESS, mana.getOrDefault(ManaType.COLORLESS, 0) + amount);
        totalMana += amount;
    }

    /**
     * 获取指定类型法力的数量。
     *
     * @param type 法力类型
     * @return 法力数量
     */
    public int getAmount(ManaType type) {
        return mana.getOrDefault(type, 0);
    }

    /**
     * 获取总法力数量。
     *
     * @return 总法力数
     */
    public int getTotalMana() {
        return totalMana;
    }

    /**
     * 检查是否有指定数量的法力。
     *
     * @param amount 法力数量
     * @return 是否有足够的法力
     */
    public boolean hasAmount(int amount) {
        return totalMana >= amount;
    }

    /**
     * 检查是否可以支付费用。
     *
     * 【简化实现】
     * - 只检查总法力数量是否足够
     * - 完整实现需要考虑颜色需求
     *
     * @param cost 法力费用
     * @return 是否可以支付
     */
    public boolean canPay(com.mtg.model.ManaCost cost) {
        if (cost == null) return true;
        return hasAmount(cost.getTotalMana());
    }

    /**
     * 支付法力（简化实现）。
     *
     * 【简化处理】
     * - 从任意法力类型扣除
     * - 完整实现需要扣除对应颜色
     *
     * @param cost 法力费用
     * @return 是否支付成功
     */
    public boolean pay(com.mtg.model.ManaCost cost) {
        if (cost == null) return true;
        int amount = cost.getTotalMana();
        if (totalMana < amount) {
            return false;
        }
        // 简化：从任意法力扣除
        deductGeneric(amount);
        return true;
    }

    /**
     * 扣除通用法力。
     *
     * @param amount 法力数量
     */
    public void deductGeneric(int amount) {
        if (amount <= 0) return;
        int remaining = amount;
        // 先扣除无色法力
        int colorless = mana.getOrDefault(ManaType.COLORLESS, 0);
        int deductColorless = Math.min(colorless, remaining);
        mana.put(ManaType.COLORLESS, colorless - deductColorless);
        remaining -= deductColorless;
        totalMana -= deductColorless;

        // 如果还需要，从其他颜色扣除
        if (remaining > 0) {
            for (ManaType type : ManaType.values()) {
                if (type == ManaType.COLORLESS) continue;
                int current = mana.getOrDefault(type, 0);
                int deduct = Math.min(current, remaining);
                mana.put(type, current - deduct);
                remaining -= deduct;
                totalMana -= deduct;
                if (remaining == 0) break;
            }
        }
    }

    /**
     * 清空法力池。
     *
     * 【规则依据】
     * - Rule 106.4: 法力池在阶段/步骤结束时清空
     */
    public void clear() {
        for (ManaType type : ManaType.values()) {
            mana.put(type, 0);
        }
        totalMana = 0;
    }

    /**
     * 获取法力池状态的字符串表示。
     *
     * @return 法力状态字符串
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        for (ManaType type : ManaType.values()) {
            int amount = mana.getOrDefault(type, 0);
            if (amount > 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(amount).append(" ").append(type.getSymbol());
            }
        }
        return sb.length() > 0 ? sb.toString() : "Empty";
    }

    /**
     * 获取法力分布图。
     *
     * @return 每种法力类型对应数量的映射
     */
    public Map<ManaType, Integer> getDistribution() {
        return new HashMap<>(mana);
    }

    /**
     * 获取法力池是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return totalMana == 0;
    }

    /**
     * 获取可用法力的描述字符串（用于 UI 显示）。
     *
     * @return 法力描述
     */
    public String getAvailableManaString() {
        if (isEmpty()) {
            return "No mana available";
        }
        return toDisplayString();
    }

    @Override
    public String toString() {
        return "ManaPool{" + toDisplayString() + "}";
    }
}
