package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;

import java.util.*;

/**
 * TargetSelector - 目标选择器，处理咒语和异能的目标选择。
 *
 * 【功能说明】
 * - 提供合法目标列表
 * - 支持单目标和多目标
 * - 验证目标合法性
 *
 * 【设计决策】
 * - 静态方法便于调用
 * - 根据 TargetType 返回符合条件的对象列表
 * - 简化实现：自动选择符合条件的第一个目标
 *
 * 【规则依据】
 * - Rule 114: Targets
 * - Rule 114.1: 需要目标的咒语/异能必须有合法目标才能施放
 * - Rule 114.3: 目标必须在施放时选择
 */
public class TargetSelector {

    /**
     * 获取指定类型的合法目标列表。
     *
     * @param game 游戏实例
     * @param targetType 目标类型
     * @param caster 施放者
     * @return 合法目标列表
     */
    public static List<Object> getLegalTargets(Game game, TargetType targetType, Player caster) {
        List<Object> targets = new ArrayList<>();
        Player opponent = caster.getOpponent();
        Battlefield bf = game.getZoneManager().getBattlefield();

        switch (targetType) {
            case PLAYER -> {
                targets.add(caster);
                targets.add(opponent);
            }
            case OPPONENT -> {
                targets.add(opponent);
            }
            case CREATURE -> {
                targets.addAll(bf.getCreatures());
            }
            case OPPONENT_CREATURE -> {
                targets.addAll(bf.getControlledBy(opponent));
                targets.addAll(bf.getControlledBy(caster).stream()
                    .filter(c -> c instanceof CreatureCard)
                    .toList());
            }
            case PERMANENT -> {
                targets.addAll(bf.getPermanents());
            }
            case ENCHANTMENT -> {
                targets.addAll(bf.getEnchantments());
            }
            case AURA -> {
                targets.addAll(bf.getEnchantments().stream()
                    .filter(e -> e instanceof EnchantmentCard enc && enc.isAura())
                    .toList());
            }
            case ARTIFACT -> {
                targets.addAll(bf.getArtifacts());
            }
            case LAND -> {
                targets.addAll(bf.getLands());
            }
            case PLANESWALKER -> {
                targets.addAll(bf.getPlaneswalkers());
            }
            case BATTLE -> {
                targets.addAll(bf.getBattles());
            }
            case SPELL -> {
                targets.addAll(game.getZoneManager().getStack().getContents());
            }
            case TOP_OF_LIBRARY -> {
                // 牌库顶需要特别处理，简化返回空列表
            }
            case CARD_IN_GRAVEYARD -> {
                targets.addAll(game.getZoneManager().getGraveyard(caster).getContents());
                targets.addAll(game.getZoneManager().getGraveyard(opponent).getContents());
            }
            case CARD_IN_EXILE -> {
                targets.addAll(game.getZoneManager().getExile(caster).getFaceUpCards());
                targets.addAll(game.getZoneManager().getExile(opponent).getFaceUpCards());
            }
            default -> {}
        }

        return targets;
    }

    /**
     * 检查目标是否合法。
     *
     * @param target 目标
     * @param targetType 目标类型
     * @param caster 施放者
     * @return 是否合法
     */
    public static boolean isLegalTarget(Object target, TargetType targetType, Player caster) {
        if (target == null) {
            return false;
        }

        Player opponent = caster.getOpponent();

        return switch (targetType) {
            case PLAYER -> target instanceof Player;
            case OPPONENT -> target.equals(opponent);
            case CREATURE -> target instanceof CreatureCard;
            case OPPONENT_CREATURE -> {
                if (target instanceof CreatureCard creature) {
                    yield creature.getController().equals(opponent);
                }
                yield false;
            }
            case PERMANENT -> target instanceof PermanentCard;
            case ENCHANTMENT -> target instanceof EnchantmentCard;
            case AURA -> target instanceof EnchantmentCard enc && enc.isAura();
            case ARTIFACT -> target instanceof ArtifactCard;
            case LAND -> target instanceof LandCard;
            case PLANESWALKER -> target instanceof PlaneswalkerCard;
            case BATTLE -> target instanceof BattleCard;
            case SPELL -> target instanceof Card || target instanceof com.mtg.zones.Stack.StackItem;
            case TOP_OF_LIBRARY, CARD_IN_GRAVEYARD, CARD_IN_EXILE -> target instanceof Card;
        };
    }

    /**
     * 获取单个目标（简化版本，自动选择第一个合法目标）。
     *
     * @param game 游戏实例
     * @param targetType 目标类型
     * @param caster 施放者
     * @return 第一个合法目标，无则返回 null
     */
    public static Object getSingleTarget(Game game, TargetType targetType, Player caster) {
        List<Object> targets = getLegalTargets(game, targetType, caster);
        return targets.isEmpty() ? null : targets.get(0);
    }

    /**
     * 为咒语创建目标列表。
     *
     * @param game 游戏实例
     * @param targetTypes 目标类型列表
     * @param caster 施放者
     * @return 目标列表
     */
    public static List<Target> createTargets(Game game, List<TargetType> targetTypes, Player caster) {
        List<Target> targets = new ArrayList<>();
        for (TargetType type : targetTypes) {
            Object target = getSingleTarget(game, type, caster);
            if (target != null) {
                targets.add(new Target(type, target));
            }
        }
        return targets;
    }

    /**
     * 检查所有目标是否仍然合法（用于结算时）。
     *
     * @param targets 目标列表
     * @return 所有目标是否合法
     */
    public static boolean checkAllTargetsLegal(List<Target> targets) {
        if (targets == null || targets.isEmpty()) {
            return true; // 无目标总是合法的
        }
        return targets.stream().allMatch(Target::isLegal);
    }

    /**
     * 获取合法目标数量。
     *
     * @param game 游戏实例
     * @param targetType 目标类型
     * @param caster 施放者
     * @return 合法目标数量
     */
    public static int countLegalTargets(Game game, TargetType targetType, Player caster) {
        return getLegalTargets(game, targetType, caster).size();
    }
}
