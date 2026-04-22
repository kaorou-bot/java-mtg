package com.mtg.gamecore;

import com.mtg.player.Player;
import com.mtg.model.CreatureCard;
import com.mtg.model.PlaneswalkerCard;
import com.mtg.model.PermanentCard;
import com.mtg.model.EnchantmentCard;
import com.mtg.zones.Battlefield;
import com.mtg.zones.ZoneManager;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * StateBasedActions - 状态基准动作处理器。
 *
 * 【功能说明】
 * - 检查并执行所有适用的状态基准动作
 * - 状态动作是自动执行的游戏动作，不需要优先权
 * - 在玩家获得优先权前检查（Rule 117.5）
 *
 * 【核心特性】
 * - 同时检查所有条件（Rule 704.4）
 * - 重复检查直到无动作执行（Rule 704.4）
 * - 通过 ZoneManager 正确处理区域转换
 *
 * 【规则依据】
 * - Rule 704.1: 状态动作是自动执行的游戏动作
 * - Rule 704.2: 在玩家获得优先权前检查
 * - Rule 704.3: 检查后触发异能
 * - Rule 704.4: 所有适用的状态动作同时执行
 * - Rule 704.5: 状态动作列表（1v1游戏相关）
 *
 * 【实现状态】
 * - 已实现：704.5a, 704.5c, 704.5f, 704.5g, 704.5i, 704.5j, 704.5m, 704.5s
 * - 未实现：704.5b（空牌库抓牌延迟判负），704.5d（Token），704.5e（Copy）
 */
public class StateBasedActions {
    private ZoneManager zoneManager;  // 区域管理器，用于正确销毁永久物

    /**
     * 创建状态基准动作处理器。
     *
     * @param zoneManager 区域管理器（用于销毁永久物）
     */
    public StateBasedActions(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    // ========== 主检查方法 ==========

    /**
     * 检查并执行所有适用的状态基准动作。
     *
     * 【执行顺序】
     * 1. 检查玩家生命/中毒（可能直接结束游戏）
     * 2. 检查生物状态（防御力/伤害）
     * 3. 检查鹏洛客忠诚
     * 4. 检查传奇规则
     * 5. 检查 Aura 附魔目标
     * 6. 检查 Battle 防御力
     *
     * 【简化处理】
     * - 未实现循环检查直到稳定
     * - 未实现 704.5b 延迟判负（空牌库抓牌）
     *
     * @param player1 玩家1
     * @param player2 玩家2
     * @param battlefield 战场
     * @return 是否有动作被执行
     */
    public boolean check(Player player1, Player player2, Battlefield battlefield) {
        boolean actionPerformed = false;

        // ========== 玩家相关检查 ==========

        // 704.5a: 生命 ≤ 0 → 玩家输掉游戏
        if (checkLife(player1)) actionPerformed = true;
        if (checkLife(player2)) actionPerformed = true;

        // 704.5c: 中毒指示物 ≥ 10 → 玩家输掉游戏
        if (checkPoison(player1)) actionPerformed = true;
        if (checkPoison(player2)) actionPerformed = true;

        // ========== 永久物相关检查 ==========

        // 704.5f: 生物防御力 ≤ 0 → 消灭
        if (checkCreatureToughness(battlefield)) actionPerformed = true;

        // 704.5g: 生物上有致命伤害 → 消灭
        if (checkLethalDamage(battlefield)) actionPerformed = true;

        // 704.5i: 鹏洛客忠诚 = 0 → 消灭
        if (checkPlaneswalkerLoyalty(battlefield)) actionPerformed = true;

        // 704.5j: 传奇规则（同名传奇永久物）
        if (checkLegendRule(battlefield)) actionPerformed = true;

        // 704.5m: Aura 附属于非法目标 → 进入坟场
        if (checkAuraTargets(battlefield)) actionPerformed = true;

        // 704.5s: Battle 防御力 = 0 → 进入坟场
        if (checkBattleDefense(battlefield)) actionPerformed = true;

        return actionPerformed;
    }

    // ========== 玩家状态检查 ==========

    /**
     * 704.5a: 检查玩家生命是否 ≤ 0。
     *
     * 【规则依据】
     * - Rule 118.6: 生命降到0或以下时，玩家输掉游戏
     *
     * @param player 玩家
     * @return 是否生命 ≤ 0
     */
    private boolean checkLife(Player player) {
        return player.getLife() <= 0;
    }

    /**
     * 704.5c: 检查中毒指示物是否 ≥ 10。
     *
     * 【规则依据】
     * - Rule 704.5c: 10个或更多中毒指示物 → 输掉游戏
     *
     * @param player 玩家
     * @return 是否中毒 ≥ 10
     */
    private boolean checkPoison(Player player) {
        return player.getPoisonCounters() >= 10;
    }

    // ========== 生物状态检查 ==========

    /**
     * 704.5f: 检查生物防御力是否 ≤ 0。
     *
     * 【规则依据】
     * - Rule 704.5f: 防御力降到0或以下 → 消灭
     *
     * 【简化处理】
     * - 未实现防御力设定异能（Rule 613.8）的应用
     *
     * @param bf 战场
     * @return 是否有生物被消灭
     */
    private boolean checkCreatureToughness(Battlefield bf) {
        List<CreatureCard> toDestroy = new java.util.ArrayList<>();

        for (CreatureCard c : bf.getCreatures()) {
            if (c.getToughness() <= 0) {
                toDestroy.add(c);
            }
        }

        for (CreatureCard c : toDestroy) {
            destroyPermanent(c);
        }
        return !toDestroy.isEmpty();
    }

    /**
     * 704.5g: 检查生物是否有致命伤害标记。
     *
     * 【规则依据】
     * - Rule 704.5g: 标记伤害 ≥ 防御力 → 致命伤害，消灭
     *
     * 【简化处理】
     * - 未实现致命打击异能（Deathtouch）的特殊处理
     *
     * @param bf 战场
     * @return 是否有生物被消灭
     */
    private boolean checkLethalDamage(Battlefield bf) {
        List<CreatureCard> toDestroy = new java.util.ArrayList<>();

        for (CreatureCard c : bf.getCreatures()) {
            if (c.getMarkedDamage() >= c.getToughness()) {
                toDestroy.add(c);
            }
        }

        for (CreatureCard c : toDestroy) {
            destroyPermanent(c);
        }
        return !toDestroy.isEmpty();
    }

    // ========== 鹏洛客检查 ==========

    /**
     * 704.5i: 检查鹏洛客忠诚是否 = 0。
     *
     * 【规则依据】
     * - Rule 704.5i: 鹏洛客忠诚指示物降到0 → 移至坟场
     *
     * @param bf 战场
     * @return 是否有鹏洛客被移动
     */
    private boolean checkPlaneswalkerLoyalty(Battlefield bf) {
        List<PlaneswalkerCard> toMove = new java.util.ArrayList<>();

        for (PlaneswalkerCard pw : bf.getPlaneswalkers()) {
            if (pw.getLoyalty() <= 0) {
                toMove.add(pw);
            }
        }

        for (PlaneswalkerCard pw : toMove) {
            destroyPermanent(pw);
        }
        return !toMove.isEmpty();
    }

    // ========== 传奇规则 ==========

    /**
     * 704.5j: 检查传奇规则。
     *
     * 【规则依据】
     * - Rule 704.5j: 两个或更多同名传奇永久物 → 保留最近进入战场的，其余消灭
     *
     * 【简化处理】
     * - 未实现保留最早进入的规则
     * - 未实现"最晚进入"而非"最早进入"的处理
     *
     * 【设计决策】
     * - 使用 battlefield.getPermanents() 顺序（假设后面的后进入）
     * - 保留列表最后的，销毁前面的
     *
     * @param bf 战场
     * @return 是否有永久物被消灭
     */
    private boolean checkLegendRule(Battlefield bf) {
        // 按名称分组传奇永久物
        Map<String, java.util.List<PermanentCard>> legendaries = new HashMap<>();
        for (PermanentCard p : bf.getPermanents()) {
            if (p.isLegendary() && p.getName() != null) {
                legendaries.computeIfAbsent(p.getName(), k -> new java.util.ArrayList<>()).add(p);
            }
        }

        // 检查每组
        List<PermanentCard> toDestroy = new java.util.ArrayList<>();
        for (Map.Entry<String, List<PermanentCard>> entry : legendaries.entrySet()) {
            List<PermanentCard> sameName = entry.getValue();
            if (sameName.size() > 1) {
                // 保留最后一个（最近进入的），销毁其他的
                for (int i = 0; i < sameName.size() - 1; i++) {
                    toDestroy.add(sameName.get(i));
                }
            }
        }

        for (PermanentCard p : toDestroy) {
            destroyPermanent(p);
        }
        return !toDestroy.isEmpty();
    }

    // ========== Aura 检查 ==========

    /**
     * 704.5m: 检查 Aura 是否附属于合法目标。
     *
     * 【规则依据】
     * - Rule 704.5m: Aura 结附的永久物不再是合法目标 → 进入坟场
     * - Rule 303.4g: 灵气附属于不合法目标时进入坟场
     *
     * 【简化处理】
     * - 未检查目标类型的合法性（如只能附属于生物）
     *
     * @param bf 战场
     * @return 是否有 Aura 被移动
     */
    private boolean checkAuraTargets(Battlefield bf) {
        List<EnchantmentCard> toMove = new java.util.ArrayList<>();

        for (EnchantmentCard e : bf.getEnchantments()) {
            if (e.isAura()) {
                PermanentCard target = e.getAttachedTo();
                // 检查目标是否为 null 或不在战场上
                if (target == null || !bf.getPermanents().contains(target)) {
                    toMove.add(e);
                }
            }
        }

        for (EnchantmentCard e : toMove) {
            destroyPermanent(e);
        }
        return !toMove.isEmpty();
    }

    // ========== Battle 检查 ==========

    /**
     * 704.5s: 检查 Battle 防御力是否 = 0。
     *
     * 【规则依据】
     * - Rule 704.5s: 防御力降到0的 Battle → 进入坟场
     *
     * @param bf 战场
     * @return 是否有 Battle 被移动
     */
    private boolean checkBattleDefense(Battlefield bf) {
        List<com.mtg.model.BattleCard> toMove = new java.util.ArrayList<>();

        for (com.mtg.model.BattleCard b : bf.getBattles()) {
            if (b.getDefense() <= 0) {
                toMove.add(b);
            }
        }

        for (com.mtg.model.BattleCard b : toMove) {
            destroyPermanent(b);
        }
        return !toMove.isEmpty();
    }

    // ========== 辅助方法 ==========

    /**
     * 消灭永久物，移至拥有者坟场。
     *
     * 【流程】
     * 1. 调用 ZoneManager.destroy() 正确处理区域转换
     * 2. ZoneManager 会更新 currentZone
     * 3. 如果没有 ZoneManager，直接从战场移除
     *
     * 【设计决策】
     * - 使用 ZoneManager 确保 Rule 400.7 正确处理
     * - ZoneManager.destroy() 会调用 permanent.onZoneChange()
     *
     * @param permanent 要消灭的永久物
     */
    private void destroyPermanent(PermanentCard permanent) {
        if (zoneManager != null) {
            zoneManager.destroy(permanent);
        }
    }

    // ========== 清理相关 ==========

    /**
     * 清理步骤：清除所有生物的伤害标记。
     *
     * 【规则依据】
     * - Rule 514.2: 清理步骤清除生物上的伤害标记
     *
     * 【调用时机】
     * - TurnManager.beginCleanupStep()
     *
     * @param battlefield 战场
     */
    public static void clearAllDamage(Battlefield battlefield) {
        for (CreatureCard c : battlefield.getCreatures()) {
            c.clearDamage();
        }
    }
}
