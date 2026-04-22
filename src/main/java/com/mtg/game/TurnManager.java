package com.mtg.game;

import com.mtg.model.CreatureCard;
import com.mtg.model.PermanentCard;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;

import java.util.*;

/**
 * TurnManager - 回合管理器，管理万智牌游戏的回合流程和战斗宣告。
 *
 * 【功能说明】
 * - 管理回合的14个阶段/步骤
 * - 管理战斗宣告（攻击/阻挡）
 * - 处理战斗伤害结算
 * - 管理回合切换
 *
 * 【回合结构】
 * ```
 * 开始阶段 → 主要阶段1 → 战斗阶段 → 主要阶段2 → 结束阶段
 *   ↓              ↓            ↓              ↓           ↓
 * UNTAAP      MAIN1      COMBAT_START     MAIN2       END
 * UPKEEP                  DECLARE_ATK                  CLEANUP
 * DRAW                   DECLARE_DEF
 *                        COMBAT_DMG
 *                        COMBAT_END
 * ```
 *
 * 【规则依据】
 * - Rule 500: 回合概述
 * - Rule 501: 开始阶段
 * - Rule 502: 重置步骤
 * - Rule 503: 维持步骤
 * - Rule 504: 抽牌步骤
 * - Rule 505: 主要阶段
 * - Rule 506-511: 战斗阶段
 * - Rule 512-514: 结束阶段
 *
 * 【设计决策】
 * - 战斗宣告通过 DeclaredAttacker/DeclaredBlocker 记录
 * - 战斗伤害在 resolveCombatDamage() 中计算
 * - switchTurn() 会增加回合数并清空战斗状态
 */
public class TurnManager {
    private final Game game;                      // 游戏引用
    private Battlefield battlefield;                // 战场引用
    private Player activePlayer;                  // 主动玩家（当前回合玩家）
    private Player nonActivePlayer;               // 非主动玩家
    private TurnPhase currentPhase;               // 当前阶段
    private List<DeclaredAttacker> attackers;    // 宣告的攻击生物列表
    private List<DeclaredBlocker> blockers;      // 宣告的阻挡生物列表
    private int turnNumber;                     // 回合数

    // ========== 内部类：战斗宣告记录 ==========

    /**
     * 宣告的攻击者记录。
     *
     * 【用途】
     * - 记录哪个生物攻击哪个目标
     * - 用于战斗伤害计算
     *
     * @param creature 攻击的生物
     * @param target 攻击目标（对手玩家或鹏洛客）
     */
    public static class DeclaredAttacker {
        public final CreatureCard creature;  // 攻击生物
        public final Player target;         // 攻击目标（对手或鹏洛客）

        public DeclaredAttacker(CreatureCard creature, Player target) {
            this.creature = creature;
            this.target = target;
        }
    }

    /**
     * 宣告的阻挡者记录。
     *
     * 【用途】
     * - 记录哪个生物阻挡哪个攻击者
     * - 用于战斗伤害计算
     *
     * @param blocker 阻挡生物
     * @param attacker 被阻挡的攻击生物
     */
    public static class DeclaredBlocker {
        public final CreatureCard blocker;  // 阻挡生物
        public final CreatureCard attacker; // 被阻挡的攻击生物

        public DeclaredBlocker(CreatureCard blocker, CreatureCard attacker) {
            this.blocker = blocker;
            this.attacker = attacker;
        }
    }

    // ========== 构造函数 ==========

    /**
     * 创建回合管理器。
     *
     * @param game 游戏引用（用于访问优先权系统等）
     */
    public TurnManager(Game game) {
        this.game = game;
        this.attackers = new ArrayList<>();
        this.blockers = new ArrayList<>();
        this.turnNumber = 0;
        this.currentPhase = TurnPhase.UNTAAP;
    }

    /**
     * 设置战场引用。
     *
     * 【用途】
     * - 允许 TurnManager 独立于 Game 测试
     * - 在 Game.startGame() 中设置
     *
     * @param battlefield 战场
     */
    public void setBattlefield(Battlefield battlefield) {
        this.battlefield = battlefield;
    }

    // ========== 回合控制 ==========

    /**
     * 开始新回合。
     *
     * 【执行步骤】
     * 1. 设置主动/非主动玩家
     * 2. 清空战斗状态
     * 3. 增加回合数
     * 4. 重置生物状态
     * 5. 开始重置步骤
     *
     * 【规则依据】
     * - Rule 500.4: 回合开始时，清除持续到回合结束的效应
     * - Rule 502: 重置步骤
     *
     * @param player 主动玩家
     */
    public void startTurn(Player player) {
        this.activePlayer = player;
        this.nonActivePlayer = player.getOpponent();
        this.attackers.clear();
        this.blockers.clear();
        turnNumber++;

        // 重置所有生物状态
        Battlefield bf = battlefield;
        for (CreatureCard c : bf.getCreatures()) {
            if (c.getController().equals(activePlayer)) {
                c.clearSummoningSickness(); // 清除召唤 sickness
                c.resetForTurn();           // 重置力量/防御力
            }
        }

        // Rule 500.4: 清除持续到回合结束的效应（简化处理）

        beginUntapStep();
    }

    /**
     * 推进到下一阶段。
     *
     * 【阶段顺序】
     * UNTAAP → UPKEEP → DRAW → MAIN1 →
     * COMBAT_START → DECLARE_ATTACKERS → DECLARE_BLOCKERS →
     * COMBAT_DAMAGE → COMBAT_END → MAIN2 → END → CLEANUP → UNTAAP
     *
     * 【规则依据】
     * - Rule 500.2: 堆叠空且所有人 pass 时阶段结束
     *
     * @return 新的当前阶段
     */
    public TurnPhase advancePhase() {
        switch (currentPhase) {
            case UNTAAP -> currentPhase = TurnPhase.UPKEEP;
            case UPKEEP -> currentPhase = TurnPhase.DRAW;
            case DRAW -> currentPhase = TurnPhase.MAIN1;
            case MAIN1 -> currentPhase = TurnPhase.COMBAT_START;
            case COMBAT_START -> currentPhase = TurnPhase.DECLARE_ATTACKERS;
            case DECLARE_ATTACKERS -> currentPhase = TurnPhase.DECLARE_BLOCKERS;
            case DECLARE_BLOCKERS -> currentPhase = TurnPhase.COMBAT_DAMAGE;
            case COMBAT_DAMAGE -> currentPhase = TurnPhase.COMBAT_END;
            case COMBAT_END -> currentPhase = TurnPhase.MAIN2;
            case MAIN2 -> currentPhase = TurnPhase.END;
            case END -> currentPhase = TurnPhase.CLEANUP;
            case CLEANUP -> {
                switchTurn();
                currentPhase = TurnPhase.UNTAAP;
            }
            default -> {}
        }
        return currentPhase;
    }

    /**
     * 切换到对手回合。
     *
     * 【执行内容】
     * 1. 交换主动/非主动玩家
     * 2. 增加回合数
     * 3. 清空战斗状态
     * 4. 执行重置步骤
     *
     * 【规则依据】
     * - Rule 500.4: 回合切换
     *
     * 【重要】
     * - switchTurn() 会增加 turnNumber
     * - 会清空 attackers 和 blockers
     */
    public void switchTurn() {
        Player temp = activePlayer;
        activePlayer = nonActivePlayer;
        nonActivePlayer = temp;
        turnNumber++;
        // 清空战斗状态
        attackers.clear();
        blockers.clear();
        beginUntapStep();
    }

    // ========== 阶段开始方法 ==========

    /**
     * 开始重置步骤（Untap Step）。
     *
     * 【执行内容】
     * 1. 主动玩家重置所有永久物
     *
     * 【规则依据】
     * - Rule 502.3: 重置所有永久物
     * - Rule 502.4: 重置步骤没有优先权，不会触发异能
     *
     * 【简化处理】
     * - 相位（Day/Night）未实现
     */
    public void beginUntapStep() {
        Battlefield bf = battlefield;
        for (PermanentCard p : bf.getPermanents()) {
            if (p.getController().equals(activePlayer)) {
                p.untap(); // 重置（取消横置）
            }
        }
        // Rule 502.4: 无优先权，不触发异能
    }

    /**
     * 开始维持步骤（Upkeep Step）。
     *
     * 【规则依据】
     * - Rule 503.1a: 维持步骤触发的异能在这里进入堆叠
     * - Rule 117.3a: 主动玩家获得优先权
     *
     * 【简化处理】
     * - 触发异能管理未完整实现
     */
    public void beginUpkeepStep() {
        // Rule 503.1a: 触发异能排队
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * 开始抽牌步骤（Draw Step）。
     *
     * 【执行内容】
     * 1. 主动玩家抽一张牌（回合动作）
     * 2. 主动玩家获得优先权
     *
     * 【规则依据】
     * - Rule 504.1: 主动玩家抽一张牌（回合动作）
     * - Rule 504.2: 然后主动玩家获得优先权
     */
    public void beginDrawStep() {
        // Rule 504.1: 回合动作，自动抽牌
        game.drawCard(activePlayer);

        // Rule 504.2: 主动玩家获得优先权
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * 开始主要阶段。
     *
     * @param main MAIN1 或 MAIN2
     */
    public void beginMainPhase(TurnPhase main) {
        currentPhase = main;
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * 开始战斗阶段（Beginning of Combat）。
     *
     * 【规则依据】
     * - Rule 507.2: 主动玩家获得优先权
     * - Rule 506.2: 在1v1中，非主动玩家是防守方
     */
    public void beginCombat() {
        // Rule 507.2: 主动玩家获得优先权
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * 开始宣告攻击者步骤（Declare Attackers）。
     *
     * 【规则依据】
     * - Rule 508.1: 宣告攻击者（回合动作）
     * - Rule 508.1h: 完成后主动玩家获得优先权
     *
     * 【简化处理】
     * - 攻击限制和要求检查简化
     */
    public void beginDeclareAttackers() {
        // Rule 508.1: 玩家宣告攻击者
        // 完成后主动玩家获得优先权
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * 开始宣告阻挡者步骤（Declare Blockers）。
     *
     * 【规则依据】
     * - Rule 509.1: 非主动玩家宣告阻挡者
     * - Rule 509.1h: 完成后主动玩家获得优先权
     */
    public void beginDeclareBlockers() {
        if (game != null) {
            game.getPrioritySystem().grantPriorityToActive();
        }
    }

    /**
     * 解决战斗伤害。
     *
     * 【执行步骤】
     * 1. 检查是否有先攻/双重打击生物
     * 2. 如果有：先解决先攻伤害 → 正常伤害
     * 3. 如果没有：直接解决正常伤害
     *
     * 【规则依据】
     * - Rule 510.4: 先攻生物先分配伤害
     * - Rule 510.5: 双重打击生物在两个伤害步骤都分配
     */
    public void resolveCombatDamage() {
        // 1. 检查先攻/双重打击
        boolean hasFirstStrike = false;
        for (DeclaredAttacker da : attackers) {
            if (da.creature.hasFirstStrike() || da.creature.hasDoubleStrike()) {
                hasFirstStrike = true;
                break;
            }
        }
        for (DeclaredBlocker db : blockers) {
            if (db.blocker.hasFirstStrike() || db.blocker.hasDoubleStrike()) {
                hasFirstStrike = true;
                break;
            }
        }

        // 2. 根据是否有先攻生物决定伤害顺序
        if (hasFirstStrike) {
            // 先解决先攻伤害
            resolveCombatDamage(false);
            // 添加先攻伤害步骤
            currentPhase = TurnPhase.COMBAT_DAMAGE_FIRST;
        } else {
            // 直接解决正常伤害
            resolveCombatDamage(true);
            currentPhase = TurnPhase.COMBAT_END;
        }
    }

    /**
     * 解决战斗伤害（实际计算）。
     *
     * 【伤害分配规则】
     * 1. 无阻挡的攻击生物 → 伤害给目标（玩家/鹏洛客）
     * 2. 有阻挡的攻击生物 → 伤害给阻挡者
     * 3. 践踏生物 → 超出部分给玩家
     *
     * 【阻挡者伤害】
     * - 阻挡生物对攻击生物造成伤害
     *
     * @param isNormalDamage 是否是正常伤害（vs 先攻伤害）
     */
    private void resolveCombatDamage(boolean isNormalDamage) {
        Battlefield bf = battlefield;

        // 1. 攻击生物分配伤害
        for (DeclaredAttacker da : attackers) {
            CreatureCard attacker = da.creature;
            int damage = attacker.getCurrentPower();

            // 查找阻挡此攻击者的阻挡生物
            List<DeclaredBlocker> blocking = getBlockersFor(attacker);

            if (blocking.isEmpty()) {
                // 无阻挡：伤害给目标
                if (attacker.hasTrample()) {
                    // 践踏：全部伤害给目标
                    da.target.modifyLife(-damage);
                } else {
                    da.target.modifyLife(-damage);
                }
            } else {
                // 有阻挡：伤害给阻挡者
                for (DeclaredBlocker db : blocking) {
                    CreatureCard blocker = db.blocker;
                    if (attacker.hasTrample()) {
                        // 践踏：分配给阻挡者，超过的给目标
                        blocker.addDamage(damage);
                        int excess = damage - blocker.getToughness();
                        if (excess > 0) {
                            da.target.modifyLife(-excess);
                        }
                    } else {
                        blocker.addDamage(damage);
                    }
                }
            }
        }

        // 2. 阻挡生物对攻击生物造成伤害
        for (DeclaredBlocker db : blockers) {
            if (!attackers.contains(db.attacker)) continue;
            CreatureCard blocker = db.blocker;
            CreatureCard attacker = db.attacker;
            attacker.addDamage(blocker.getCurrentPower());
        }
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
     * 结束战斗步骤。
     *
     * 【规则依据】
     * - Rule 511.3: 移除所有生物的"战斗中"状态
     * - Rule 511.4: 主动玩家获得优先权
     */
    public void endCombat() {
        // Rule 511.3: 移除战斗状态
        attackers.clear();
        blockers.clear();
    }

    /**
     * 开始结束步骤（End Step）。
     *
     * 【规则依据】
     * - Rule 513.1: 结束步骤触发的异能
     * - Rule 117.3a: 主动玩家获得优先权
     */
    public void beginEndStep() {
        game.getPrioritySystem().grantPriorityToActive();
    }

    /**
     * 开始清理步骤（Cleanup Step）。
     *
     * 【执行内容】
     * 1. 弃牌到最大手牌数
     * 2. 清除所有生物的伤害标记
     *
     * 【规则依据】
     * - Rule 514.1: 弃多余手牌（需要玩家选择）
     * - Rule 514.2: 清除伤害标记
     *
     * 【简化处理】
     * - 弃牌需要UI选择，这里简化为自动弃最后几张
     */
    public void beginCleanupStep() {
        // Rule 514.1: 弃牌到手牌上限
        var hand = game.getZoneManager().getHand(activePlayer);
        while (hand.size() > 7) {
            // 需要UI选择弃哪些牌
            // 简化：丢弃最后一张
        }

        // Rule 514.2: 清除伤害标记
        Battlefield bf = battlefield;
        for (CreatureCard c : bf.getCreatures()) {
            c.clearDamage();
        }
    }

    // ========== 战斗宣告 ==========

    /**
     * 检查生物是否可以攻击。
     *
     * 【条件】
     * - 必须在宣告攻击者步骤
     * - 必须是自己的生物
     * - 不能已横置
     * - 不能有召唤 sickness（除非有敏捷）
     * - 不能已在攻击列表中
     *
     * @param creature 要检查的生物
     * @return 是否可以攻击
     */
    public boolean canDeclareAttacker(CreatureCard creature) {
        if (currentPhase != TurnPhase.DECLARE_ATTACKERS) return false;
        if (!creature.getController().equals(activePlayer)) return false;
        if (creature.isTapped()) return false;
        if (creature.hasSummoningSickness() && !creature.hasHaste()) return false;
        if (isAlreadyAttacking(creature)) return false;
        return true;
    }

    /**
     * 宣告攻击者。
     *
     * @param creature 攻击的生物
     * @param target 攻击目标
     */
    public void declareAttacker(CreatureCard creature, Player target) {
        if (canDeclareAttacker(creature)) {
            creature.tap();
            attackers.add(new DeclaredAttacker(creature, target));
        }
    }

    /**
     * 检查生物是否可以阻挡。
     *
     * 【条件】
     * - 必须在宣告阻挡者步骤
     * - 必须是对手的生物
     * - 不能已横置
     *
     * @param creature 要检查的生物
     * @return 是否可以阻挡
     */
    public boolean canDeclareBlocker(CreatureCard creature) {
        if (currentPhase != TurnPhase.DECLARE_BLOCKERS) return false;
        if (!creature.getController().equals(nonActivePlayer)) return false;
        if (creature.isTapped()) return false;
        return true;
    }

    /**
     * 宣告阻挡者。
     *
     * @param creature 阻挡的生物
     * @param attacker 要阻挡的攻击生物
     */
    public void declareBlocker(CreatureCard creature, CreatureCard attacker) {
        if (canDeclareBlocker(creature)) {
            creature.tap();
            blockers.add(new DeclaredBlocker(creature, attacker));
        }
    }

    /**
     * 检查生物是否已在攻击列表中。
     */
    private boolean isAlreadyAttacking(CreatureCard creature) {
        for (DeclaredAttacker da : attackers) {
            if (da.creature.equals(creature)) return true;
        }
        return false;
    }

    // ========== Getter 方法 ==========

    public Player getActivePlayer() { return activePlayer; }
    public Player getNonActivePlayer() { return nonActivePlayer; }
    public TurnPhase getCurrentPhase() { return currentPhase; }
    public int getTurnNumber() { return turnNumber; }

    /**
     * 获取所有宣告的攻击者（副本）。
     */
    public List<DeclaredAttacker> getAttackers() {
        return new ArrayList<>(attackers);
    }

    /**
     * 获取所有宣告的阻挡者（副本）。
     */
    public List<DeclaredBlocker> getBlockers() {
        return new ArrayList<>(blockers);
    }
}
