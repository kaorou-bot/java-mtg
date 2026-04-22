package com.mtg.game;

import com.mtg.model.CreatureCard;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CombatResolver - 战斗解析器，处理战斗宣告、阻挡和伤害结算。
 *
 * 【功能说明】
 * - 管理战斗宣告（攻击者、阻挡者）
 * - 计算战斗伤害
 * - 应用关键词异能效果
 * - 处理先攻/双重打击
 *
 * 【规则依据】
 * - Rule 506: 战斗阶段
 * - Rule 507: 宣告攻击者
 * - Rule 509: 宣告阻挡者
 * - Rule 510: 战斗伤害
 * - Rule 702: 关键词异能
 *
 * 【设计决策】
 * - 从 TurnManager 提取战斗逻辑
 * - 支持多种关键词异能
 * - 处理践踏、先攻、致命等异能
 */
public class CombatResolver {
    private final Game game;  // 游戏引用
    private Battlefield battlefield;  // 战场引用
    private Player activePlayer;  // 主动玩家
    private Player defendingPlayer;  // 防守玩家
    private List<DeclaredAttacker> attackers;  // 攻击者列表
    private List<DeclaredBlocker> blockers;  // 阻挡者列表
    private List<CombatDamageResult> damageResults;  // 伤害结算结果
    private GameLog gameLog;  // 游戏日志

    /**
     * 宣告的攻击者记录。
     */
    public static class DeclaredAttacker {
        public final CreatureCard creature;  // 攻击生物
        public final Player target;  // 攻击目标（对手或鹏洛客）

        public DeclaredAttacker(CreatureCard creature, Player target) {
            this.creature = creature;
            this.target = target;
        }
    }

    /**
     * 宣告的阻挡者记录。
     */
    public static class DeclaredBlocker {
        public final CreatureCard blocker;  // 阻挡生物
        public final CreatureCard attacker;  // 被阻挡的攻击生物

        public DeclaredBlocker(CreatureCard blocker, CreatureCard attacker) {
            this.blocker = blocker;
            this.attacker = attacker;
        }
    }

    /**
     * 战斗伤害结算结果。
     */
    public static class CombatDamageResult {
        public final CreatureCard source;  // 伤害来源
        public final Object target;  // 目标（玩家或生物）
        public final int damage;  // 伤害量
        public final boolean isLethal;  // 是否致命伤害
        public final String ability;  // 涉及的异能

        public CombatDamageResult(CreatureCard source, Object target, int damage, boolean isLethal, String ability) {
            this.source = source;
            this.target = target;
            this.damage = damage;
            this.isLethal = isLethal;
            this.ability = ability;
        }
    }

    /**
     * 创建战斗解析器。
     *
     * @param game 游戏
     */
    public CombatResolver(Game game) {
        this.game = game;
        this.attackers = new ArrayList<>();
        this.blockers = new ArrayList<>();
        this.damageResults = new ArrayList<>();
    }

    /**
     * 设置战场引用。
     *
     * @param battlefield 战场
     */
    public void setBattlefield(Battlefield battlefield) {
        this.battlefield = battlefield;
    }

    /**
     * 设置主动/防守玩家。
     *
     * @param active 主动玩家
     * @param defending 防守玩家
     */
    public void setPlayers(Player active, Player defending) {
        this.activePlayer = active;
        this.defendingPlayer = defending;
    }

    /**
     * 设置游戏日志。
     *
     * @param log 游戏日志
     */
    public void setGameLog(GameLog log) {
        this.gameLog = log;
    }

    /**
     * 宣告攻击者。
     *
     * @param creature 攻击生物
     * @param target 攻击目标
     */
    public void declareAttacker(CreatureCard creature, Player target) {
        attackers.add(new DeclaredAttacker(creature, target));
        if (gameLog != null) {
            gameLog.logAttack(creature.getName(), target.getName());
        }
    }

    /**
     * 宣告阻挡者。
     *
     * @param blocker 阻挡生物
     * @param attacker 被阻挡的攻击者
     */
    public void declareBlocker(CreatureCard blocker, CreatureCard attacker) {
        blockers.add(new DeclaredBlocker(blocker, attacker));
        if (gameLog != null) {
            gameLog.logBlock(blocker.getName(), attacker.getName());
        }
    }

    /**
     * 检查生物是否可以攻击。
     *
     * @param creature 生物
     * @return 是否可以攻击
     */
    public boolean canAttack(CreatureCard creature) {
        return !creature.isTapped() &&
               (!creature.hasSummoningSickness() || creature.hasHaste());
    }

    /**
     * 检查生物是否可以阻挡。
     *
     * @param creature 生物
     * @return 是否可以阻挡
     */
    public boolean canBlock(CreatureCard creature) {
        return !creature.isTapped();
    }

    /**
     * 检查攻击者是否可被阻挡（考虑飞行和威慑）。
     *
     * @param attacker 攻击生物
     * @return 是否可被阻挡
     */
    public boolean canBeBlocked(CreatureCard attacker) {
        // Menace: 需要2+生物阻挡（简化处理：总是可以阻挡）
        // Flying: 有飞行异能的生物只能被有飞行或敏捷的生物阻挡
        // 完整实现需要检查阻挡者的异能
        return true;
    }

    /**
     * 获取攻击者列表。
     *
     * @return 攻击者列表副本
     */
    public List<DeclaredAttacker> getAttackers() {
        return new ArrayList<>(attackers);
    }

    /**
     * 获取阻挡者列表。
     *
     * @return 阻挡者列表副本
     */
    public List<DeclaredBlocker> getBlockers() {
        return new ArrayList<>(blockers);
    }

    /**
     * 清空战斗宣告。
     */
    public void clearCombat() {
        attackers.clear();
        blockers.clear();
        damageResults.clear();
    }

    /**
     * 解决全部战斗伤害。
     *
     * 【执行顺序】
     * 1. 检查是否有先攻/双重打击生物
     * 2. 如果有，先解决先攻伤害，再解决正常伤害
     * 3. 如果没有，直接解决正常伤害
     *
     * 【规则依据】
     * - Rule 510.4: 先攻生物先分配伤害
     * - Rule 510.5: 双重打击生物在两个伤害步骤都分配
     */
    public void resolveAllCombatDamage() {
        damageResults.clear();

        // 1. 检查先攻/双重打击
        boolean hasFirstStrike = checkForFirstStrike();

        // 2. 根据是否有先攻生物决定伤害顺序
        if (hasFirstStrike) {
            // 先解决先攻伤害
            resolveCombatDamagePhase(false);
            // 再解决正常伤害（双重打击生物再次造成伤害）
            resolveCombatDamagePhase(true);
        } else {
            // 直接解决正常伤害
            resolveCombatDamagePhase(true);
        }
    }

    /**
     * 检查是否有先攻或双重打击生物。
     *
     * @return 是否有先攻/双重打击
     */
    private boolean checkForFirstStrike() {
        for (DeclaredAttacker da : attackers) {
            if (da.creature.hasFirstStrike() || da.creature.hasDoubleStrike()) {
                return true;
            }
        }
        for (DeclaredBlocker db : blockers) {
            if (db.blocker.hasFirstStrike() || db.blocker.hasDoubleStrike()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解决战斗伤害阶段。
     *
     * @param isNormalDamage 是否是正常伤害阶段
     */
    private void resolveCombatDamagePhase(boolean isNormalDamage) {
        // 1. 攻击生物分配伤害
        for (DeclaredAttacker da : attackers) {
            CreatureCard attacker = da.creature;

            // 检查是否在正确阶段造成伤害
            if (!isCorrectDamagePhase(attacker, isNormalDamage)) {
                continue;
            }

            int damage = attacker.getCurrentPower();
            List<DeclaredBlocker> blocking = getBlockersFor(attacker);

            if (blocking.isEmpty()) {
                // 无阻挡：伤害给目标（玩家或鹏洛客）
                resolveUnblockedDamage(attacker, da.target, damage);
            } else {
                // 有阻挡：伤害给阻挡者
                resolveBlockedDamage(attacker, blocking, da.target, damage);
            }
        }

        // 2. 阻挡生物对攻击生物造成伤害
        for (DeclaredBlocker db : blockers) {
            CreatureCard blocker = db.blocker;
            CreatureCard attacker = db.attacker;

            // 检查是否在正确阶段造成伤害
            if (!isCorrectDamagePhase(blocker, isNormalDamage)) {
                continue;
            }

            // 只对仍在战斗中的攻击者造成伤害
            if (!isAttackerInCombat(attacker)) {
                continue;
            }

            int blockerDamage = blocker.getCurrentPower();
            applyDamageToCreature(attacker, blockerDamage, blocker, "blocking");
        }
    }

    /**
     * 检查生物是否在正确的伤害阶段造成伤害。
     *
     * @param creature 生物
     * @param isNormalDamage 是否是正常伤害阶段
     * @return 是否应该造成伤害
     */
    private boolean isCorrectDamagePhase(CreatureCard creature, boolean isNormalDamage) {
        if (isNormalDamage) {
            // 正常伤害阶段：只有没有先攻的生物造成伤害（双重打击除外）
            return !creature.hasFirstStrike() || creature.hasDoubleStrike();
        } else {
            // 先攻伤害阶段：只有有先攻异能（包括双重打击）的生物造成伤害
            return creature.hasFirstStrike();
        }
    }

    /**
     * 检查攻击者是否仍在战斗中。
     *
     * @param attacker 攻击生物
     * @return 是否仍在战斗
     */
    private boolean isAttackerInCombat(CreatureCard attacker) {
        for (DeclaredAttacker da : attackers) {
            if (da.creature.equals(attacker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取阻挡特定攻击者的所有阻挡生物。
     *
     * @param attacker 攻击生物
     * @return 阻挡生物列表
     */
    private List<DeclaredBlocker> getBlockersFor(CreatureCard attacker) {
        List<DeclaredBlocker> result = new ArrayList<>();
        for (DeclaredBlocker db : blockers) {
            if (db.attacker.equals(attacker)) {
                result.add(db);
            }
        }
        return result;
    }

    /**
     * 解决无阻挡伤害。
     */
    private void resolveUnblockedDamage(CreatureCard attacker, Player target, int damage) {
        // 践踏异能：超出部分给玩家
        if (attacker.hasTrample()) {
            int damageToTarget = Math.min(damage, target.getLife());
            target.modifyLife(-damageToTarget);
            recordDamage(attacker, target, damageToTarget, false, "trample");
        } else {
            target.modifyLife(-damage);
            recordDamage(attacker, target, damage, false, null);
        }
    }

    /**
     * 解决有阻挡伤害。
     */
    private void resolveBlockedDamage(CreatureCard attacker, List<DeclaredBlocker> blocking,
                                     Player defendingPlayer, int damage) {
        // 践踏：可以分配超出伤害给玩家
        if (attacker.hasTrample()) {
            resolveTrampleDamage(attacker, blocking, defendingPlayer, damage);
        } else {
            // 非践踏：所有伤害给阻挡者
            for (DeclaredBlocker db : blocking) {
                applyDamageToCreature(db.blocker, damage, attacker, null);
            }
        }
    }

    /**
     * 解决践踏伤害。
     *
     * 【规则依据】
     * - Rule 702.19: 践踏
     * - 必须先分配足够杀死每个阻挡者的伤害，剩余才能分配给玩家
     * - 致命打击：任何正数伤害都视为足够杀死
     */
    private void resolveTrampleDamage(CreatureCard attacker, List<DeclaredBlocker> blocking,
                                      Player defendingPlayer, int damage) {
        int remaining = damage;

        // 1. 先分配足够杀死阻挡者的伤害
        for (DeclaredBlocker db : blocking) {
            CreatureCard blocker = db.blocker;
            int needed;
            if (attacker.hasDeathtouch()) {
                // 致命打击：只需 1 点即可杀死
                needed = 1;
            } else {
                // 剩余需伤害 = baseToughness - markedDamage
                needed = Math.max(0, blocker.getBaseToughness() - blocker.getMarkedDamage());
            }
            int damageToBlocker = Math.min(remaining, needed);
            applyDamageToCreature(blocker, damageToBlocker, attacker, "trample");
            remaining -= damageToBlocker;
        }

        // 2. 剩余伤害给玩家（践踏超出）
        if (remaining > 0) {
            defendingPlayer.modifyLife(-remaining);
            recordDamage(attacker, defendingPlayer, remaining, false, "trample excess");
        }
    }

    /**
     * 对生物应用伤害。
     *
     * @param target 目标生物
     * @param damage 伤害量
     * @param source 来源生物
     * @param ability 涉及的异能
     */
    private void applyDamageToCreature(CreatureCard target, int damage, CreatureCard source, String ability) {
        // 致命打击：任何正数伤害都是致命伤害
        if (source.hasDeathtouch() && damage > 0) {
            target.addDamage(target.getToughness());
            recordDamage(source, target, target.getToughness(), true, "deathtouch");
        } else {
            target.addDamage(damage);
            boolean isLethal = target.getMarkedDamage() >= target.getToughness();
            recordDamage(source, target, damage, isLethal, ability);
        }
    }

    /**
     * 记录伤害结果。
     */
    private void recordDamage(CreatureCard source, Object target, int damage, boolean isLethal, String ability) {
        damageResults.add(new CombatDamageResult(source, target, damage, isLethal, ability));

        if (gameLog != null && target instanceof Player) {
            gameLog.logDamage((Player) target, damage, source.getName());
        }
    }

    /**
     * 获取伤害结算结果。
     *
     * @return 伤害结果列表
     */
    public List<CombatDamageResult> getDamageResults() {
        return new ArrayList<>(damageResults);
    }

    /**
     * 应用关键词异能效果。
     *
     * 【异能列表】
     * - Lifelink: 造成伤害时获得等量生命
     * - Deathtouch: 任何正数伤害消灭生物
     */
    public void applyKeywordEffects() {
        for (CombatDamageResult result : damageResults) {
            // Lifelink：伤害来源控制者获得生命
            if (result.source.hasLifelink()) {
                Player controller = result.source.getController();
                controller.modifyLife(result.damage);
                if (gameLog != null) {
                    gameLog.logLifeChange(controller, controller.getLife(), result.damage);
                }
            }
        }
    }

    /**
     * 检查并应用致命伤害（SBA）。
     *
     * 【规则依据】
     * - Rule 704.5g: 标记伤害 ≥ 防御力 → 消灭
     */
    public void applyLethalDamageSBA() {
        List<CreatureCard> toDestroy = new ArrayList<>();

        for (CreatureCard c : battlefield.getCreatures()) {
            // 忽略具有不灭异能的生物
            if (c.hasIndestructible()) {
                continue;
            }

            // 检查致命伤害
            if (c.getMarkedDamage() >= c.getToughness()) {
                toDestroy.add(c);
                if (gameLog != null) {
                    gameLog.logDestroyed(c.getName(), "lethal damage");
                }
            }
        }

        // 消灭生物
        for (CreatureCard c : toDestroy) {
            game.getZoneManager().destroy(c);
        }
    }

    /**
     * 检查飞行阻挡限制。
     *
     * 【规则依据】
     * - Rule 702.9: 飞行
     * - 有飞行异能的生物只能被有飞行或敏捷异能的生物阻挡
     *
     * @param attacker 攻击生物
     * @param blocker 阻挡生物
     * @return 是否可以阻挡
     */
    public boolean canBlockAttacker(CreatureCard attacker, CreatureCard blocker) {
        // 如果攻击者有飞行
        if (attacker.hasFlying()) {
            // 阻挡者必须有飞行或敏捷
            return blocker.hasFlying() || blocker.hasReach();
        }
        // 无飞行：任何生物都可以阻挡
        return true;
    }

    /**
     * 检查威慑异能。
     *
     * 【规则依据】
     * - Rule 702.111: 威慑
     * - 威慑生物只能被2个或更多生物阻挡
     *
     * @param attacker 攻击生物
     * @return 是否受到威慑限制
     */
    public boolean hasMenace(CreatureCard attacker) {
        return attacker.hasMenace();
    }

    /**
     * 计算阻挡威慑生物所需的最少生物数。
     *
     * @param attacker 攻击生物
     * @return 所需最少阻挡者数
     */
    public int getRequiredBlockers(CreatureCard attacker) {
        return attacker.hasMenace() ? 2 : 1;
    }
}
