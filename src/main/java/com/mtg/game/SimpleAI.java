package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;

import java.util.*;

/**
 * SimpleAI - 简单 AI 对手，实现基本的人工智能决策。
 *
 * 【功能说明】
 * - 自动执行 AI 玩家的回合
 * - 基本的出牌策略
 * - 战斗宣告和阻挡
 *
 * 【设计决策】
 * - 简化策略：优先出高费卡牌
 * - 攻击策略：全部攻击（简化版）
 * - 阻挡策略：阻挡最大威胁
 */
public class SimpleAI {
    private final Game game;
    private final Player aiPlayer;
    private CombatResolver combatResolver;

    public SimpleAI(Game game, Player aiPlayer) {
        this.game = game;
        this.aiPlayer = aiPlayer;
        this.combatResolver = null;
    }

    /**
     * 设置战斗解析器。
     */
    public void setCombatResolver(CombatResolver resolver) {
        this.combatResolver = resolver;
    }

    /**
     * 执行 AI 回合。
     */
    public void takeTurn() {
        aiPlayer.startTurn();
        playCards();
        enterCombat();
        playCards();
        endTurn();
    }

    /**
     * 出牌阶段。
     */
    private void playCards() {
        boolean playedCard;

        do {
            playedCard = false;
            List<Card> playableCards = getPlayableCards();
            if (!playableCards.isEmpty()) {
                // 按费用排序（优先出高费）
                playableCards.sort((a, b) -> {
                    int costA = a.getManaCost().getGenericMana() + a.getManaCost().getColoredMana().size();
                    int costB = b.getManaCost().getGenericMana() + b.getManaCost().getColoredMana().size();
                    return costB - costA;
                });

                Card cardToPlay = playableCards.get(0);
                if (playCard(cardToPlay)) {
                    playedCard = true;
                }
            }
        } while (playedCard && !game.isGameOver());
    }

    /**
     * 获取可出的牌。
     */
    private List<Card> getPlayableCards() {
        List<Card> playable = new ArrayList<>();
        var hand = game.getZoneManager().getHand(aiPlayer);

        for (Card card : hand.getCards()) {
            if (canPlayCard(card)) {
                playable.add(card);
            }
        }

        return playable;
    }

    /**
     * 检查是否可以出这张牌。
     */
    private boolean canPlayCard(Card card) {
        if (!aiPlayer.canPayCost(card.getManaCost())) {
            return false;
        }
        if (card instanceof LandCard) {
            return !aiPlayer.hasPlayedLandThisTurn();
        }
        return true;
    }

    /**
     * 出牌。
     */
    private boolean playCard(Card card) {
        if (game.isGameOver()) {
            return false;
        }

        boolean success = false;

        if (card instanceof LandCard land) {
            success = game.playLand(aiPlayer, land);
        } else if (card instanceof CreatureCard creature) {
            success = game.playCreature(aiPlayer, creature);
        } else if (card instanceof EnchantmentCard enchantment) {
            success = game.playEnchantment(aiPlayer, enchantment);
        } else if (card instanceof ArtifactCard artifact) {
            success = game.playArtifact(aiPlayer, artifact);
        } else if (card instanceof PlaneswalkerCard planeswalker) {
            success = game.playPlaneswalker(aiPlayer, planeswalker);
        } else if (card instanceof SpellCard spell) {
            success = game.playInstant(aiPlayer, spell);
        }

        return success;
    }

    /**
     * 进入战斗阶段。
     */
    private void enterCombat() {
        TurnPhase currentPhase = game.getCurrentPhase();

        if (currentPhase != TurnPhase.COMBAT_START &&
            currentPhase != TurnPhase.DECLARE_ATTACKERS &&
            currentPhase != TurnPhase.DECLARE_BLOCKERS) {
            return;
        }

        while (game.getCurrentPhase() != TurnPhase.COMBAT_START && !game.isGameOver()) {
            game.nextPhase();
        }

        if (game.isGameOver()) return;

        game.nextPhase();
        declareAttackers();

        if (game.getCurrentPhase() == TurnPhase.DECLARE_BLOCKERS) {
            declareBlockers();
            game.nextPhase();
        }
    }

    /**
     * 宣告攻击者。
     */
    private void declareAttackers() {
        if (game.isGameOver()) return;

        Player opponent = aiPlayer.getOpponent();
        List<CreatureCard> attackers = aiPlayer.getAttackingCreatures();

        for (CreatureCard attacker : attackers) {
            if (attacker.canAttack()) {
                game.declareAttacker(attacker, opponent);
            }
        }
    }

    /**
     * 宣告阻挡者。
     */
    private void declareBlockers() {
        if (game.isGameOver() || combatResolver == null) return;

        List<CombatResolver.DeclaredAttacker> attackers = combatResolver.getAttackers();

        for (CombatResolver.DeclaredAttacker attackerInfo : attackers) {
            CreatureCard attacker = attackerInfo.creature;

            // 寻找可阻挡的最大威胁
            CombatResolver.DeclaredBlocker bestBlocker = null;
            int bestPower = 0;

            for (CreatureCard blocker : aiPlayer.getCreatures()) {
                if (blocker.isTapped()) continue;

                // 检查是否已用来阻挡
                boolean alreadyBlocking = combatResolver.getBlockers().stream()
                    .anyMatch(db -> db.blocker == blocker);
                if (alreadyBlocking) continue;

                // 检查是否可以阻挡
                if (canBlockCreature(blocker, attacker)) {
                    int power = attacker.getCurrentPower();
                    if (power > bestPower) {
                        bestPower = power;
                        game.declareBlocker(blocker, attacker);
                    }
                }
            }
        }
    }

    /**
     * 检查阻挡者是否可以阻挡攻击者。
     */
    private boolean canBlockCreature(CreatureCard blocker, CreatureCard attacker) {
        if (attacker.hasFlying() && !blocker.hasFlying() && !blocker.hasReach()) {
            return false;
        }
        return true;
    }

    /**
     * 结束回合。
     */
    private void endTurn() {
        if (!game.isGameOver()) {
            aiPlayer.clearMana();
        }
    }

    /**
     * 获取 AI 玩家。
     */
    public Player getAIPlayer() {
        return aiPlayer;
    }
}
