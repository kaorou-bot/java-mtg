package com.mtg.gamecore;

import com.mtg.player.Player;
import com.mtg.zones.Stack;
import com.mtg.zones.Battlefield;
import com.mtg.zones.Library;

import java.util.ArrayList;
import java.util.List;

/**
 * PrioritySystem - 优先权系统，管理万智牌游戏中的优先权轮转。
 *
 * 【功能说明】
 * - 追踪当前拥有优先权的玩家
 * - 处理优先权传递（pass）
 * - 判断堆叠结算时机
 *
 * 【核心概念】
 * - 优先权决定谁能执行游戏动作
 * - 主动玩家（Active Player）在大多数步骤开始时获得优先权
 * - 两人连续 pass 后结算堆叠或进入下一阶段
 *
 * 【规则依据】
 * - Rule 117.1: 拥有优先权的玩家可以施放咒语、激活异能、采取特殊动作
 * - Rule 117.3: 优先权的获取规则
 * - Rule 117.4: 所有人连续 pass 后的处理
 * - Rule 117.5: 获得优先权前的状态检查
 *
 * 【简化处理】
 * - 未实现法力异能特殊规则（Rule 117.1d）
 * - 未实现优先权转让
 */
public class PrioritySystem {
    private Player activePlayer;           // 主动玩家（当前回合玩家）
    private Player nonActivePlayer;       // 非主动玩家
    private Player currentPriorityPlayer;  // 当前拥有优先权的玩家
    private int passCount;                // 连续 pass 次数

    /**
     * 创建优先权系统。
     */
    public PrioritySystem() {
        this.passCount = 0;
    }

    // ========== 初始化 ==========

    /**
     * 设置当前回合的玩家。
     *
     * 【用途】
     * - 在回合开始时调用
     * - 用于确定主动/非主动玩家
     *
     * @param active 主动玩家
     * @param nonActive 非主动玩家
     */
    public void setPlayers(Player active, Player nonActive) {
        this.activePlayer = active;
        this.nonActivePlayer = nonActive;
    }

    // ========== 优先权管理 ==========

    /**
     * 获取当前拥有优先权的玩家。
     *
     * @return 当前优先权拥有者
     */
    public Player getPlayerWithPriority() {
        return currentPriorityPlayer;
    }

    /**
     * 授予主动玩家优先权。
     *
     * 【调用时机】
     * - 大多数步骤/阶段开始时
     * - 咒语/异能结算后
     *
     * 【规则依据】
     * - Rule 117.3a: 主动玩家在大多数步骤/阶段开始时获得优先权
     *
     * 【效果】
     * - 重置 passCount 为 0
     * - currentPriorityPlayer 设为 activePlayer
     */
    public void grantPriorityToActive() {
        passCount = 0;
        currentPriorityPlayer = activePlayer;
    }

    /**
     * 在咒语/异能结算后授予主动玩家优先权。
     *
     * 【规则依据】
     * - Rule 117.3b: 咒语或异能结算后，主动玩家获得优先权
     *
     * 【与 grantPriorityToActive 的区别】
     * - 功能相同，但语义不同
     * - 用于区分"阶段开始"和"结算后"的优先权授予
     */
    public void grantPriorityAfterResolve() {
        passCount = 0;
        currentPriorityPlayer = activePlayer;
    }

    /**
     * 咒语施放后重置优先权。
     *
     * 【规则依据】
     * - Rule 117.3c: 玩家施放咒语或激活异能后获得优先权
     *
     * 【效果】
     * - 重置 passCount 为 0
     * - 施放者继续保持优先权
     */
    public void onSpellCast() {
        passCount = 0;
    }

    /**
     * 异能激活后重置优先权。
     *
     * 【规则依据】
     * - Rule 117.3c: 玩家施放咒语或激活异能后获得优先权
     *
     * @see #onSpellCast()
     */
    public void onAbilityActivated() {
        passCount = 0;
    }

    /**
     * 玩家传递优先权。
     *
     * 【规则依据】
     * - Rule 117.3d: 玩家可以选择不采取动作传递优先权
     * - Rule 117.4: 所有玩家连续 pass 后，堆叠顶结算或阶段结束
     *
     * 【流程】
     * 1. 如果当前是主动玩家 → 优先权给非主动玩家
     * 2. 如果当前是非主动玩家 → passCount++
     * 3. 如果 passCount >= 2 → 所有人都 pass，堆叠结算
     *
     * 【简化处理】
     * - 未实现优先权转让给特定玩家
     */
    public void pass() {
        if (currentPriorityPlayer == null) return;

        if (currentPriorityPlayer == activePlayer) {
            // 主动玩家 pass → 优先权给非主动玩家
            currentPriorityPlayer = nonActivePlayer;
        } else {
            // 非主动玩家 pass
            passCount++;
            if (passCount >= 2) {
                // 两人连续 pass → 堆叠结算（由 Game 处理）
                currentPriorityPlayer = null;
            } else {
                // 回到主动玩家
                currentPriorityPlayer = activePlayer;
            }
        }
    }

    // ========== 状态查询 ==========

    /**
     * 检查是否所有玩家都连续 pass 了。
     *
     * 【条件】
     * - passCount >= 2
     *
     * 【用途】
     * - 用于判断是否可以结算堆叠或进入下一阶段
     *
     * @return 是否所有人都 pass
     */
    public boolean allPlayersPassed() {
        return passCount >= 2;
    }

    /**
     * 检查玩家是否拥有优先权。
     *
     * @param player 要检查的玩家
     * @return 是否拥有优先权
     */
    public boolean playerHasPriority(Player player) {
        return player != null && player.equals(currentPriorityPlayer);
    }

    /**
     * 获取玩家可用的动作列表。
     *
     * 【可用动作】
     * - Pass Priority（总是可用）
     * - Play Land（主阶段 + 空堆叠）
     * - Cast Spell / Cast Instant
     * - Activate Ability
     *
     * 【简化处理】
     * - 未检查动作的具体可行性
     * - 返回所有可能动作
     *
     * @param player 玩家
     * @param stack 堆叠（用于检查是否可出瞬间）
     * @param isMainPhase 是否是主阶段
     * @param isUpkeepStep 是否是维持步骤
     * @return 可用动作列表
     */
    public List<String> getAvailableActions(Player player, Stack stack, boolean isMainPhase, boolean isUpkeepStep) {
        List<String> actions = new ArrayList<>();

        // 必须有优先权才能执行动作
        if (!player.equals(currentPriorityPlayer)) {
            return actions;
        }

        // 总是可以 pass
        actions.add("Pass Priority");

        // 主阶段可以出地（Rule 305.2）
        if (isMainPhase && stack.isEmpty()) {
            actions.add("Play Land");
        }

        // 主阶段或维持步骤可以施放咒语
        if (isMainPhase || isUpkeepStep) {
            actions.add("Cast Spell");
        } else if (!stack.isEmpty()) {
            // 非主阶段只能施放瞬间
            actions.add("Cast Instant");
        }

        // 可以激活异能
        actions.add("Activate Ability");

        return actions;
    }

    /**
     * 重置优先权状态。
     *
     * 【用途】
     * - 游戏重新开始时调用
     * - 清空所有优先权状态
     */
    public void reset() {
        currentPriorityPlayer = null;
        passCount = 0;
    }
}
