package com.mtg.abilities;

import com.mtg.game.Game;
import com.mtg.game.TurnPhase;
import com.mtg.model.Card;
import com.mtg.model.CreatureCard;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * TriggeredAbilityManager - 触发式异能管理器（Rule 603）。
 *
 * 【功能说明】
 * - 管理所有注册的触发式异能
 * - 检查触发条件
 * - 将触发的异能放入堆叠
 * - 按 APNAP 顺序排序
 *
 * 【规则依据】
 * - Rule 603.2: 异能在每个步骤/阶段开始时触发
 * - Rule 603.3: 每个异能独立检查和进入堆叠
 * - Rule 603.6: 异能按 APNAP 顺序放入堆叠（主动玩家先，然后非主动玩家）
 */
public class TriggeredAbilityManager {
    private Game game;  // 游戏引用
    private Map<Player, List<TriggeredAbility>> abilityQueue;  // 异能队列
    private Map<Card, List<TriggeredAbility>> registeredAbilities;  // 已注册的异能

    /**
     * 创建触发式异能管理器。
     *
     * @param game 游戏
     */
    public TriggeredAbilityManager(Game game) {
        this.game = game;
        this.abilityQueue = new HashMap<>();
        this.registeredAbilities = new HashMap<>();
    }

    /**
     * 注册卡牌的触发式异能。
     *
     * @param ability 要注册的异能
     */
    public void registerAbility(TriggeredAbility ability) {
        Card source = ability.getSourceCard();
        registeredAbilities.computeIfAbsent(source, k -> new ArrayList<>()).add(ability);
    }

    /**
     * 取消注册卡牌的所有异能（卡牌离开战场时调用）。
     *
     * @param card 离开战场的卡牌
     */
    public void unregisterAbilitiesFromCard(Card card) {
        registeredAbilities.remove(card);
    }

    /**
     * 检查阶段开始时触发的异能。
     *
     * 【规则依据】
     * - Rule 603.2: 触发条件持续检查
     * - 在每个步骤/阶段转换时调用
     *
     * @param phase 当前阶段
     * @param activePlayer 主动玩家
     * @return 触发的异能列表
     */
    public List<TriggeredAbility> checkTriggeredAbilities(TurnPhase phase, Player activePlayer) {
        List<TriggeredAbility> triggered = new ArrayList<>();

        // 检查所有注册的异能
        for (List<TriggeredAbility> abilities : registeredAbilities.values()) {
            for (TriggeredAbility ability : abilities) {
                // 重置记忆以进行新的触发检查
                if (ability instanceof OnPhaseBeginAbility) {
                    OnPhaseBeginAbility phaseAbility = (OnPhaseBeginAbility) ability;
                    if (phaseAbility.matchesPhase(phase)) {
                        triggered.add(ability);
                    }
                }
            }
        }

        // 按 APNAP 顺序排序（Rule 603.3d）
        sortByAPNAP(triggered, activePlayer);

        return triggered;
    }

    /**
     * 检查区域转换触发的异能。
     *
     * @param card 区域转换的卡牌
     * @param fromZone 原区域
     * @param toZone 新区域
     * @return 触发的异能列表
     */
    public List<TriggeredAbility> checkZoneChangeAbilities(Card card, String fromZone, String toZone) {
        List<TriggeredAbility> triggered = new ArrayList<>();

        for (List<TriggeredAbility> abilities : registeredAbilities.values()) {
            for (TriggeredAbility ability : abilities) {
                if (ability instanceof OnZoneChangeAbility) {
                    OnZoneChangeAbility zoneAbility = (OnZoneChangeAbility) ability;
                    if (zoneAbility.matchesZoneChange(card, fromZone, toZone)) {
                        triggered.add(ability);
                    }
                }
            }
        }

        return triggered;
    }

    /**
     * 检查伤害触发异能。
     *
     * @param source 伤害来源（生物）
     * @param target 目标玩家
     * @param amount 伤害量
     * @return 触发的异能列表
     */
    public List<TriggeredAbility> checkDamageAbilities(CreatureCard source, Player target, int amount) {
        List<TriggeredAbility> triggered = new ArrayList<>();

        for (List<TriggeredAbility> abilities : registeredAbilities.values()) {
            for (TriggeredAbility ability : abilities) {
                if (ability instanceof OnDamageDealtAbility) {
                    OnDamageDealtAbility damageAbility = (OnDamageDealtAbility) ability;
                    if (damageAbility.matchesDamage(source, target, amount)) {
                        triggered.add(ability);
                    }
                }
            }
        }

        return triggered;
    }

    /**
     * 将触发的异能放入堆叠。
     *
     * @param abilities 要放入堆叠的异能列表
     */
    public void putOnStack(List<TriggeredAbility> abilities) {
        for (TriggeredAbility ability : abilities) {
            // 为异能创建堆叠项目
            game.getZoneManager().getStack().pushAbility(ability);
        }
    }

    /**
     * 按 APNAP 顺序排序异能。
     *
     * 【规则依据】
     * - Rule 603.3d: 异能按 APNAP 顺序放入堆叠
     *
     * @param abilities 要排序的异能列表
     * @param activePlayer 主动玩家
     */
    private void sortByAPNAP(List<TriggeredAbility> abilities, Player activePlayer) {
        abilities.sort((a1, a2) -> {
            Player c1 = a1.getController();
            Player c2 = a2.getController();

            if (c1.equals(activePlayer) && !c2.equals(activePlayer)) {
                return -1;
            } else if (!c1.equals(activePlayer) && c2.equals(activePlayer)) {
                return 1;
            }
            return 0;
        });
    }

    /**
     * 清除所有注册的异能。
     */
    public void clear() {
        registeredAbilities.clear();
        abilityQueue.clear();
    }

    // ========== 常用触发式异能类型 ==========

    /**
     * 在特定阶段开始的异能。
     *
     * 【示例】
     * - "At the beginning of your upkeep, draw a card."
     *
     * @param name 异能名称
     * @param source 源卡牌
     * @param controller 控制者
     * @param phase 触发阶段
     * @param whosePhase 谁的阶段（"your", "each", "opponent's" 等）
     */
    public static abstract class OnPhaseBeginAbility extends TriggeredAbility {
        private TurnPhase triggerPhase;  // 触发阶段
        private String whosePhase;  // 谁的阶段

        public OnPhaseBeginAbility(String name, Card source, Player controller,
                                   TurnPhase phase, String whosePhase) {
            super(name, source, controller);
            this.triggerPhase = phase;
            this.whosePhase = whosePhase;
        }

        public boolean matchesPhase(TurnPhase phase) {
            return this.triggerPhase == phase;
        }

        public TurnPhase getTriggerPhase() {
            return triggerPhase;
        }

        public String getWhosePhase() {
            return whosePhase;
        }
    }

    /**
     * 区域转换时触发的异能。
     *
     * 【示例】
     * - "When Elvish Mystic enters the battlefield, add one mana of any color."
     *
     * @param name 异能名称
     * @param source 源卡牌
     * @param controller 控制者
     * @param triggerZone 触发区域（如 "battlefield", "graveyard"）
     */
    public static abstract class OnZoneChangeAbility extends TriggeredAbility {
        private String triggerZone;  // 触发区域

        public OnZoneChangeAbility(String name, Card source, Player controller,
                                    String triggerZone) {
            super(name, source, controller);
            this.triggerZone = triggerZone;
        }

        public boolean matchesZoneChange(Card card, String fromZone, String toZone) {
            // 检查异能是否因此卡牌和区域变化而触发
            return card.equals(getSourceCard()) &&
                   triggerZone.equals(toZone);
        }

        public String getTriggerZone() {
            return triggerZone;
        }
    }

    /**
     * 造成伤害时触发的异能。
     *
     * 【示例】
     * - "Whenever Goblin Electromancer deals damage to a player, draw a card."
     *
     * @param name 异能名称
     * @param source 源卡牌
     * @param controller 控制者
     */
    public static abstract class OnDamageDealtAbility extends TriggeredAbility {
        public OnDamageDealtAbility(String name, Card source, Player controller) {
            super(name, source, controller);
        }

        /**
         * 检查伤害是否匹配触发条件。
         *
         * @param source 伤害来源
         * @param target 目标玩家
         * @param amount 伤害量
         * @return 是否匹配
         */
        public abstract boolean matchesDamage(CreatureCard source, Player target, int amount);
    }
}