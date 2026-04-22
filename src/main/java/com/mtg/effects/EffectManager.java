package com.mtg.effects;

import com.mtg.game.Game;
import com.mtg.model.Card;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * EffectManager - 效果管理器，处理替代效果和预防效果。
 *
 * 【功能说明】
 * - 管理所有活跃的替代效果和预防效果
 * - 应用效果到游戏事件
 * - 处理持续效果
 *
 * 【规则依据】
 * - Rule 614: 替代效果
 * - Rule 615: 预防效果
 * - Rule 616: 替代效果和预防效果的交互
 */
public class EffectManager {
    private Game game;  // 游戏引用
    private List<ReplacementEffect> replacementEffects;  // 替代效果列表
    private List<PreventionEffect> preventionEffects;  // 预防效果列表
    private List<ContinuousEffect> continuousEffects;  // 持续效果列表

    /**
     * 创建效果管理器。
     *
     * @param game 游戏
     */
    public EffectManager(Game game) {
        this.game = game;
        this.replacementEffects = new ArrayList<>();
        this.preventionEffects = new ArrayList<>();
        this.continuousEffects = new ArrayList<>();
    }

    /**
     * 添加替代效果。
     *
     * @param effect 替代效果
     */
    public void addReplacementEffect(ReplacementEffect effect) {
        replacementEffects.add(effect);
    }

    /**
     * 移除替代效果。
     *
     * @param effect 替代效果
     */
    public void removeReplacementEffect(ReplacementEffect effect) {
        replacementEffects.remove(effect);
    }

    /**
     * 添加预防效果。
     *
     * @param effect 预防效果
     */
    public void addPreventionEffect(PreventionEffect effect) {
        preventionEffects.add(effect);
    }

    /**
     * 移除预防效果。
     *
     * @param effect 预防效果
     */
    public void removePreventionEffect(PreventionEffect effect) {
        preventionEffects.remove(effect);
    }

    /**
     * 添加持续效果。
     *
     * @param effect 持续效果
     */
    public void addContinuousEffect(ContinuousEffect effect) {
        continuousEffects.add(effect);
    }

    /**
     * 移除持续效果。
     *
     * @param effect 持续效果
     */
    public void removeContinuousEffect(ContinuousEffect effect) {
        continuousEffects.remove(effect);
    }

    /**
     * 应用所有适用的替代效果到事件。
     *
     * 【规则依据】
     * - Rule 616.1: 一次检查并应用一个替代效果
     *
     * @param event 游戏事件
     * @return 应用效果后的事件
     */
    public GameEvent applyReplacementEffects(GameEvent event) {
        GameEvent result = event;

        // 收集适用的替代效果
        List<ReplacementEffect> applicable = new ArrayList<>();
        for (ReplacementEffect effect : replacementEffects) {
            if (effect.applies(result, game)) {
                applicable.add(effect);
            }
        }

        // 按顺序应用（Rule 616.1: 一次一个）
        for (ReplacementEffect effect : applicable) {
            GameEvent modified = effect.apply(result, game);
            if (modified == null) {
                // 事件被取消
                return null;
            }
            result = modified;
        }

        return result;
    }

    /**
     * 应用所有适用的预防效果到事件。
     *
     * @param event 游戏事件
     * @return 应用效果后的事件
     */
    public GameEvent applyPreventionEffects(GameEvent event) {
        GameEvent result = event;

        for (PreventionEffect effect : preventionEffects) {
            if (effect.applies(result, game)) {
                result = effect.apply(result, game);
                if (result == null) {
                    return null;
                }
            }
        }

        return result;
    }

    /**
     * 处理游戏事件，应用所有适用效果。
     *
     * 【执行顺序】
     * 1. 先应用替代效果
     * 2. 再应用预防效果
     *
     * @param event 游戏事件
     * @return 处理后的事件
     */
    public GameEvent processEvent(GameEvent event) {
        // 先应用替代效果
        GameEvent result = applyReplacementEffects(event);
        if (result == null) return null;

        // 然后应用预防效果
        result = applyPreventionEffects(result);
        return result;
    }

    /**
     * 清除所有效果（清理步骤使用）。
     */
    public void clear() {
        replacementEffects.clear();
        preventionEffects.clear();
        continuousEffects.clear();
    }

    /**
     * 移除来自特定卡牌的所有效果。
     *
     * @param source 源卡牌
     */
    public void removeEffectsFromSource(Card source) {
        replacementEffects.removeIf(e -> e.getSourceCard() == source);
        preventionEffects.removeIf(e -> e.getSourceCard() == source);
        continuousEffects.removeIf(e -> e.getSourceCard() == source);
    }

    // ========== Getter 方法 ==========

    public List<ReplacementEffect> getReplacementEffects() {
        return new ArrayList<>(replacementEffects);
    }

    public List<PreventionEffect> getPreventionEffects() {
        return new ArrayList<>(preventionEffects);
    }

    public List<ContinuousEffect> getContinuousEffects() {
        return new ArrayList<>(continuousEffects);
    }
}

/**
 * ContinuousEffect - 持续效果类（Rule 611）。
 *
 * 【功能说明】
 * - 修改对象的特征、对象控制权或游戏规则
 *
 * 【持续时间类型】
 * - Until end of turn（回合结束）
 * - Until end of combat（战斗结束）
 * - For as long as [condition]（条件满足期间）
 * - Indefinitely（永久）
 */
abstract class ContinuousEffect {
    protected String name;  // 效果名称
    protected Card sourceCard;  // 源卡牌
    protected Player controller;  // 控制者
    protected EffectDuration duration;  // 持续时间
    protected long startTime;  // 开始时间
    protected long endTime;  // 结束时间

    /**
     * 持续时间枚举。
     */
    public enum EffectDuration {
        END_OF_TURN,  // 回合结束
        END_OF_COMBAT,  // 战斗结束
        WHILE_ON_BATTLEFIELD,  // 在战场上期间
        INDEFINITE  // 永久
    }

    public ContinuousEffect(String name, Card sourceCard, Player controller, EffectDuration duration) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.duration = duration;
    }

    /**
     * 检查效果是否仍然活跃。
     */
    public boolean isActive() {
        if (duration == EffectDuration.INDEFINITE) {
            return sourceCard.getCurrentZone() != null &&
                   sourceCard.getCurrentZone().getName().equals("Battlefield");
        }
        return true;
    }

    /**
     * 应用此效果修改游戏状态或对象特征。
     *
     * @param game 游戏
     */
    public abstract void apply(Game game);

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public EffectDuration getDuration() {
        return duration;
    }
}