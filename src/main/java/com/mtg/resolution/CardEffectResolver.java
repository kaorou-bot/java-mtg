package com.mtg.resolution;

import com.mtg.game.Game;
import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import com.mtg.zones.ZoneManager;

import java.util.List;

/**
 * CardEffectResolver - 卡牌效果结算器，处理咒语和异能效果的结算。
 *
 * 【功能说明】
 * - 根据卡牌类型和名称结算咒语效果
 * - 提供各种常用的效果处理方法
 * - 是卡牌效果系统的核心
 *
 * 【设计决策】
 * - 使用静态方法，便于调用
 * - 每个咒语/异能类型有专门的结算方法
 * - 简化处理：目标选择使用第一个符合条件的对象
 */
public class CardEffectResolver {

    /**
     * 结算咒语卡牌。
     *
     * @param game 游戏
     * @param card 咒语卡牌
     * @param caster 施放者
     */
    public static void resolveSpell(Game game, Card card, Player caster) {
        switch (card.getType()) {
            case CREATURE -> resolveCreatureSpell(game, (CreatureCard) card, caster);
            case ENCHANTMENT -> resolveEnchantmentSpell(game, (EnchantmentCard) card, caster);
            case ARTIFACT -> resolveArtifactSpell(game, (ArtifactCard) card, caster);
            case PLANESWALKER -> resolvePlaneswalkerSpell(game, (PlaneswalkerCard) card, caster);
            case INSTANT, SORCERY -> resolveInstantSorcery(game, (SpellCard) card, caster);
            default -> {}
        }
    }

    /**
     * 结算生物咒语 - 放置到战场。
     *
     * @param game 游戏
     * @param creature 生物卡牌
     * @param caster 施放者
     */
    private static void resolveCreatureSpell(Game game, CreatureCard creature, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(creature, caster);
        creature.untap();
    }

    /**
     * 结算结界咒语 - 放置到战场。
     *
     * @param game 游戏
     * @param enchantment 结界卡牌
     * @param caster 施放者
     */
    private static void resolveEnchantmentSpell(Game game, EnchantmentCard enchantment, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(enchantment, caster);
    }

    /**
     * 结算神器咒语 - 放置到战场。
     *
     * @param game 游戏
     * @param artifact 神器卡牌
     * @param caster 施放者
     */
    private static void resolveArtifactSpell(Game game, ArtifactCard artifact, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(artifact, caster);
    }

    /**
     * 结算鹏洛客咒语 - 放置到战场。
     *
     * @param game 游戏
     * @param planeswalker 鹏洛客卡牌
     * @param caster 施放者
     */
    private static void resolvePlaneswalkerSpell(Game game, PlaneswalkerCard planeswalker, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(planeswalker, caster);
    }

    /**
     * 结算即时/法术咒语。
     *
     * 【简化处理】
     * - 根据卡牌名称执行对应效果
     * - 目标选择简化：使用第一个符合条件的对象
     *
     * @param game 游戏
     * @param spell 咒语卡牌
     * @param caster 施放者
     */
    private static void resolveInstantSorcery(Game game, SpellCard spell, Player caster) {
        ZoneManager zm = game.getZoneManager();
        Battlefield bf = zm.getBattlefield();
        Player opponent = caster.getOpponent();

        // 根据卡牌名称执行效果
        switch (spell.getName()) {
            // 伤害咒语
            case "Lightning Bolt" -> dealDamageToAny(spell, game, caster, opponent, 3);
            case "Shock" -> dealDamageToAny(spell, game, caster, opponent, 2);
            case "Fireball" -> dealDamageToAny(spell, game, caster, opponent, 5);

            // 抓牌/生命咒语
            case "Revitalize" -> gainLifeAndDraw(spell, game, caster, 3, 1);

            // 消灭咒语
            case "Doom Blade" -> destroyTargetNonBlack(spell, game, caster, opponent, bf);
            case "Cancel" -> counterTargetSpell(spell, game, caster);

            // 法力咒语
            case "Dark Ritual" -> produceBlackMana(caster, 3);

            // 生物增强咒语
            case "Giant Growth" -> buffTargetCreature(spell, game, caster, 3, 3);

            // 搜索咒语
            case "Rampant Growth" -> searchLibraryForLand(game, caster);

            // 弃牌咒语
            case "Duress" -> opponentDiscards(spell, game, opponent);

            default -> {
                // 未知咒语 - 移到坟场
                zm.getGraveyard(caster).add(spell);
            }
        }
    }

    // ========== 效果辅助方法 ==========

    /**
     * 对任意目标（玩家或生物）造成伤害。
     *
     * 【简化处理】
     * - 如果战场有生物，伤害给第一个生物
     * - 否则伤害给对手
     *
     * @param source 伤害来源
     * @param game 游戏
     * @param caster 施放者
     * @param opponent 对手
     * @param amount 伤害量
     */
    public static void dealDamageToAny(Card source, Game game, Player caster, Player opponent, int amount) {
        Battlefield bf = game.getZoneManager().getBattlefield();
        List<CreatureCard> creatures = bf.getCreatures();

        if (!creatures.isEmpty()) {
            // 伤害给第一个生物（简化目标选择）
            CreatureCard target = creatures.get(0);
            target.addDamage(amount);
            game.notifyDamageDealt(target.getController(), amount);
        } else {
            // 伤害给对手
            opponent.modifyLife(-amount);
            game.notifyDamageDealt(opponent, amount);
        }
    }

    /**
     * 对特定目标造成伤害。
     *
     * @param source 伤害来源
     * @param game 游戏
     * @param caster 施放者
     * @param target 目标（玩家或生物）
     * @param amount 伤害量
     */
    public static void dealDamageTo(Card source, Game game, Player caster, Object target, int amount) {
        if (target instanceof Player) {
            ((Player) target).modifyLife(-amount);
            game.notifyDamageDealt((Player) target, amount);
        } else if (target instanceof CreatureCard) {
            ((CreatureCard) target).addDamage(amount);
            game.notifyDamageDealt(((CreatureCard) target).getController(), amount);
        }
    }

    /**
     * 获得生命并抓牌。
     *
     * @param source 来源卡牌
     * @param game 游戏
     * @param target 目标玩家
     * @param lifeGain 生命恢复量
     * @param cardsToDraw 抓牌数量
     */
    public static void gainLifeAndDraw(Card source, Game game, Player target, int lifeGain, int cardsToDraw) {
        target.modifyLife(lifeGain);

        for (int i = 0; i < cardsToDraw; i++) {
            game.drawCard(target);
        }
    }

    /**
     * 消灭非黑色生物。
     *
     * 【简化处理】
     * - 消灭第一个非黑色生物
     *
     * @param source 来源卡牌
     * @param game 游戏
     * @param caster 施放者
     * @param opponent 对手
     * @param bf 战场
     */
    public static void destroyTargetNonBlack(Card source, Game game, Player caster, Player opponent, Battlefield bf) {
        List<CreatureCard> creatures = bf.getCreatures();

        for (CreatureCard creature : creatures) {
            if (!creature.getColor().contains(ManaType.BLACK)) {
                game.getZoneManager().destroy(creature);
                game.notifyPermanentDestroyed(creature);
                break;
            }
        }
    }

    /**
     * 反击堆叠顶的咒语。
     *
     * @param source 来源卡牌
     * @param game 游戏
     * @param caster 施放者
     */
    public static void counterTargetSpell(Card source, Game game, Player caster) {
        // 简化：反击堆叠顶的咒语
        if (!game.getZoneManager().getStack().isEmpty()) {
            var topItem = game.getZoneManager().getStack().peek();
            if (topItem instanceof com.mtg.zones.Stack.SpellItem) {
                Card countered = ((com.mtg.zones.Stack.SpellItem) topItem).getCard();
                game.getZoneManager().counterSpell(countered, countered.getOwner());
            }
        }
    }

    /**
     * 产生黑色法力。
     *
     * @param player 玩家
     * @param amount 法力数量
     */
    public static void produceBlackMana(Player player, int amount) {
        for (int i = 0; i < amount; i++) {
            player.addMana(ManaType.BLACK);
        }
    }

    /**
     * 增强目标生物 +X/+X（持续到回合结束）。
     *
     * 【简化处理】
     * - 增强第一个生物
     *
     * @param source 来源卡牌
     * @param game 游戏
     * @param caster 施放者
     * @param powerBonus 力量加成
     * @param toughnessBonus 防御力加成
     */
    public static void buffTargetCreature(Card source, Game game, Player caster, int powerBonus, int toughnessBonus) {
        Battlefield bf = game.getZoneManager().getBattlefield();
        List<CreatureCard> creatures = bf.getCreatures();

        if (!creatures.isEmpty()) {
            CreatureCard target = creatures.get(0);  // 简化目标选择
            target.modifyPower(powerBonus);
            target.modifyToughness(toughnessBonus);
        }
    }

    /**
     * 从牌库搜索地牌（简化）。
     *
     * 【简化处理】
     * - 直接抓一张牌
     *
     * @param game 游戏
     * @param player 玩家
     */
    public static void searchLibraryForLand(Game game, Player player) {
        // 简化：直接抓一张牌
        game.drawCard(player);
    }

    /**
     * 强制对手弃牌。
     *
     * @param source 来源卡牌
     * @param game 游戏
     * @param opponent 对手
     */
    public static void opponentDiscards(Card source, Game game, Player opponent) {
        var hand = game.getZoneManager().getHand(opponent);
        List<Card> cards = hand.getCards();

        if (!cards.isEmpty()) {
            // 弃置第一个非生物咒语
            for (Card card : cards) {
                if (card.getType() != CardType.CREATURE) {
                    game.getZoneManager().discard(card, opponent);
                    break;
                }
            }
        }
    }

    /**
     * 消灭目标永久物。
     *
     * @param game 游戏
     * @param permanent 永久物
     */
    public static void destroyPermanent(Game game, PermanentCard permanent) {
        game.getZoneManager().destroy(permanent);
        game.notifyPermanentDestroyed(permanent);
    }

    /**
     * 放逐目标永久物。
     *
     * @param game 游戏
     * @param permanent 永久物
     */
    public static void exilePermanent(Game game, PermanentCard permanent) {
        game.getZoneManager().exile(permanent);
    }

    /**
     * 横置目标永久物。
     *
     * @param permanent 永久物
     */
    public static void tapPermanent(PermanentCard permanent) {
        permanent.tap();
    }

    /**
     * 取消横置目标永久物。
     *
     * @param permanent 永久物
     */
    public static void untapPermanent(PermanentCard permanent) {
        permanent.untap();
    }

    /**
     * 为生物添加关键词异能（持续到回合结束）。
     *
     * @param creature 生物
     * @param keyword 关键词异能名称
     */
    public static void addKeywordUntilEndOfTurn(CreatureCard creature, String keyword) {
        switch (keyword.toLowerCase()) {
            case "flying" -> creature.setHasFlying(true);
            case "first strike" -> creature.setHasFirstStrike(true);
            case "double strike" -> creature.setHasDoubleStrike(true);
            case "trample" -> creature.setHasTrample(true);
            case "vigilance" -> creature.setHasVigilance(true);
            case "haste" -> creature.setHaste(true);
            case "deathtouch" -> creature.setHasDeathtouch(true);
            default -> {}
        }
    }
}