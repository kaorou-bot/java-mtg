package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Zone;

/**
 * Target - 目标类，表示咒语或异能的目标。
 *
 * 【功能说明】
 * - 封装目标的信息（类型和对象）
 * - 支持多种目标类型：玩家、生物、永久物等
 * - 提供目标合法性检查
 *
 * 【设计决策】
 * - 目标可以是任意对象（Player、CreatureCard 等）
 * - 使用 Object 类型简化存储，通过 instanceof 检查类型
 * - 支持目标组的概念（多目标咒语）
 *
 * 【规则依据】
 * - Rule 114: Targets
 * - Rule 114.1: 咒语/异能描述中提到 "target" 即需要指定目标
 * - Rule 114.3: 目标必须在施放时选择
 */
public class Target {
    private final TargetType targetType;
    private final Object target;

    /**
     * 创建目标。
     *
     * @param targetType 目标类型
     * @param target 目标对象（Player、CreatureCard、PermanentCard 等）
     */
    public Target(TargetType targetType, Object target) {
        this.targetType = targetType;
        this.target = target;
    }

    /**
     * 获取目标类型。
     */
    public TargetType getTargetType() {
        return targetType;
    }

    /**
     * 获取目标对象。
     */
    public Object getTarget() {
        return target;
    }

    /**
     * 获取目标名称（用于显示）。
     */
    public String getTargetName() {
        if (target == null) {
            return "Invalid Target";
        }

        if (target instanceof Player player) {
            return player.getName();
        } else if (target instanceof Card card) {
            return card.getName();
        } else if (target instanceof PermanentCard permanent) {
            return permanent.getName();
        }

        return target.toString();
    }

    /**
     * 检查目标是否仍然合法。
     *
     * 【规则依据】
     * - Rule 608.2b: 结算时检查目标合法性
     * - 不合法的目标被忽略
     *
     * @return 目标是否合法
     */
    public boolean isLegal() {
        if (target == null) {
            return false;
        }

        // 检查目标是否已被消灭/移除
        if (target instanceof PermanentCard permanent) {
            return permanent.isPermanent();
        }

        if (target instanceof Player) {
            return true;
        }

        if (target instanceof Card card) {
            return card.getCurrentZone() != null;
        }

        return true;
    }

    /**
     * 检查目标是否为玩家。
     */
    public boolean isPlayer() {
        return target instanceof Player;
    }

    /**
     * 检查目标是否为生物。
     */
    public boolean isCreature() {
        return target instanceof CreatureCard;
    }

    /**
     * 检查目标是否为永久物。
     */
    public boolean isPermanent() {
        return target instanceof PermanentCard;
    }

    /**
     * 获取目标玩家（如果目标是玩家）。
     */
    public Player getPlayer() {
        if (target instanceof Player) {
            return (Player) target;
        }
        return null;
    }

    /**
     * 获取目标生物（如果目标是生物）。
     */
    public CreatureCard getCreature() {
        if (target instanceof CreatureCard) {
            return (CreatureCard) target;
        }
        return null;
    }

    /**
     * 获取目标永久物（如果目标是永久物）。
     */
    public PermanentCard getPermanent() {
        if (target instanceof PermanentCard) {
            return (PermanentCard) target;
        }
        return null;
    }

    /**
     * 获取目标卡牌（如果目标是卡牌）。
     */
    public Card getCard() {
        if (target instanceof Card) {
            return (Card) target;
        }
        return null;
    }

    @Override
    public String toString() {
        return targetType.getDisplayName() + ": " + getTargetName();
    }
}
