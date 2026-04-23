package com.mtg.game;

/**
 * TargetType - 目标类型枚举，定义咒语和异能可以指定的目标类型。
 *
 * Based on Rules 601.2c and 114.3:
 * - 目标必须满足咒语/异能描述的限制
 * - 某些咒语/异能没有目标（如 "destroy all creatures"）
 *
 * 【设计决策】
 * - 按 MTG 规则的目标类型分类
 * - 每种类型有对应的合法性检查方法
 */
public enum TargetType {
    /** 任意玩家 */
    PLAYER("Player", true),

    /** 对手玩家 */
    OPPONENT("Opponent", true),

    /** 任意生物 */
    CREATURE("Creature", true),

    /** 对手生物 */
    OPPONENT_CREATURE("Opponent Creature", true),

    /** 任意永久物 */
    PERMANENT("Permanent", true),

    /** 任意结界 */
    ENCHANTMENT("Enchantment", true),

    /** 灵气（已附着的） */
    AURA("Aura", true),

    /** 任意神器 */
    ARTIFACT("Artifact", true),

    /** 任意地 */
    LAND("Land", true),

    /** 任意鹏洛客 */
    PLANESWALKER("Planeswalker", true),

    /** 任意战役 */
    BATTLE("Battle", true),

    /** 堆叠上的咒语 */
    SPELL("Spell", true),

    /** 牌库顶的牌 */
    TOP_OF_LIBRARY("Top of Library", false),

    /** 坟场中的牌 */
    CARD_IN_GRAVEYARD("Card in Graveyard", false),

    /** 放逐区中的牌 */
    CARD_IN_EXILE("Card in Exile", false);

    private final String displayName;
    private final boolean isTargetable;

    TargetType(String displayName, boolean isTargetable) {
        this.displayName = displayName;
        this.isTargetable = isTargetable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTargetable() {
        return isTargetable;
    }
}
