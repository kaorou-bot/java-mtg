package com.mtg.resolution;

import com.mtg.game.Game;
import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import com.mtg.zones.ZoneManager;

import java.util.List;

/**
 * CardEffectResolver handles the resolution of spell and ability effects.
 *
 * This is the core of the card effect system. Each spell/ability has
 * an effect that gets executed when it resolves.
 */
public class CardEffectResolver {

    /**
     * Resolve a spell card.
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
     * Resolve a creature spell - put on battlefield.
     */
    private static void resolveCreatureSpell(Game game, CreatureCard creature, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(creature, caster);
        creature.untap();
    }

    /**
     * Resolve an enchantment spell - put on battlefield.
     */
    private static void resolveEnchantmentSpell(Game game, EnchantmentCard enchantment, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(enchantment, caster);
    }

    /**
     * Resolve an artifact spell - put on battlefield.
     */
    private static void resolveArtifactSpell(Game game, ArtifactCard artifact, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(artifact, caster);
    }

    /**
     * Resolve a planeswalker spell - put on battlefield.
     */
    private static void resolvePlaneswalkerSpell(Game game, PlaneswalkerCard planeswalker, Player caster) {
        ZoneManager zm = game.getZoneManager();
        zm.putOnBattlefield(planeswalker, caster);
    }

    /**
     * Resolve an instant or sorcery spell.
     */
    private static void resolveInstantSorcery(Game game, SpellCard spell, Player caster) {
        ZoneManager zm = game.getZoneManager();
        Battlefield bf = zm.getBattlefield();
        Player opponent = caster.getOpponent();

        // Execute the spell's effect based on its name
        switch (spell.getName()) {
            // ========== DAMAGE SPELLS ==========
            case "Lightning Bolt" -> dealDamageToAny(spell, game, caster, opponent, 3);
            case "Shock" -> dealDamageToAny(spell, game, caster, opponent, 2);
            case "Fireball" -> dealDamageToAny(spell, game, caster, opponent, 5);

            // ========== DRAW SPELLS ==========
            case "Revitalize" -> gainLifeAndDraw(spell, game, caster, 3, 1);

            // ========== DESTROY SPELLS ==========
            case "Doom Blade" -> destroyTargetNonBlack(spell, game, caster, opponent, bf);
            case "Cancel" -> counterTargetSpell(spell, game, caster);

            // ========== MANA SPELLS ==========
            case "Dark Ritual" -> produceBlackMana(caster, 3);

            // ========== CREATURE BUFF SPELLS ==========
            case "Giant Growth" -> buffTargetCreature(spell, game, caster, 3, 3);

            // ========== SEARCH/SPELL ==========
            case "Rampant Growth" -> searchLibraryForLand(game, caster);

            // ========== DISCARD SPELLS ==========
            case "Duress" -> opponentDiscards(spell, game, opponent);

            default -> {
                // Unknown spell - move to graveyard
                zm.getGraveyard(caster).add(spell);
            }
        }
    }

    // ========== HELPER METHODS FOR SPELL EFFECTS ==========

    /**
     * Deal damage to any target (player or creature).
     */
    public static void dealDamageToAny(Card source, Game game, Player caster, Player opponent, int amount) {
        Battlefield bf = game.getZoneManager().getBattlefield();
        List<CreatureCard> creatures = bf.getCreatures();

        if (!creatures.isEmpty()) {
            // Deal damage to first creature (simplified targeting)
            CreatureCard target = creatures.get(0);
            target.addDamage(amount);
            game.notifyDamageDealt(target.getController(), amount);
        } else {
            // Deal damage to opponent
            opponent.modifyLife(-amount);
            game.notifyDamageDealt(opponent, amount);
        }
    }

    /**
     * Deal damage to a specific target.
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
     * Gain life and optionally draw a card.
     */
    public static void gainLifeAndDraw(Card source, Game game, Player target, int lifeGain, int cardsToDraw) {
        target.modifyLife(lifeGain);

        for (int i = 0; i < cardsToDraw; i++) {
            game.drawCard(target);
        }
    }

    /**
     * Destroy a non-black creature.
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
     * Counter target spell on the stack.
     */
    public static void counterTargetSpell(Card source, Game game, Player caster) {
        // Simplified - counter any spell on stack
        if (!game.getZoneManager().getStack().isEmpty()) {
            var topItem = game.getZoneManager().getStack().peek();
            if (topItem instanceof com.mtg.zones.Stack.SpellItem) {
                Card countered = ((com.mtg.zones.Stack.SpellItem) topItem).getCard();
                game.getZoneManager().counterSpell(countered, countered.getOwner());
            }
        }
    }

    /**
     * Produce black mana.
     */
    public static void produceBlackMana(Player player, int amount) {
        for (int i = 0; i < amount; i++) {
            player.addMana(ManaType.BLACK);
        }
    }

    /**
     * Buff target creature with +X/+X until end of turn.
     */
    public static void buffTargetCreature(Card source, Game game, Player caster, int powerBonus, int toughnessBonus) {
        Battlefield bf = game.getZoneManager().getBattlefield();
        List<CreatureCard> creatures = bf.getCreatures();

        if (!creatures.isEmpty()) {
            CreatureCard target = creatures.get(0); // Simplified targeting
            target.modifyPower(powerBonus);
            target.modifyToughness(toughnessBonus);
        }
    }

    /**
     * Search library for a land card (simplified).
     */
    public static void searchLibraryForLand(Game game, Player player) {
        // Simplified: just draw a card
        game.drawCard(player);
    }

    /**
     * Force opponent to discard a card.
     */
    public static void opponentDiscards(Card source, Game game, Player opponent) {
        var hand = game.getZoneManager().getHand(opponent);
        List<Card> cards = hand.getCards();

        if (!cards.isEmpty()) {
            // Discard first non-creature spell
            for (Card card : cards) {
                if (card.getType() != CardType.CREATURE) {
                    game.getZoneManager().discard(card, opponent);
                    break;
                }
            }
        }
    }

    /**
     * Destroy target permanent.
     */
    public static void destroyPermanent(Game game, PermanentCard permanent) {
        game.getZoneManager().destroy(permanent);
        game.notifyPermanentDestroyed(permanent);
    }

    /**
     * Exile target permanent.
     */
    public static void exilePermanent(Game game, PermanentCard permanent) {
        game.getZoneManager().exile(permanent);
    }

    /**
     * Tap target permanent.
     */
    public static void tapPermanent(PermanentCard permanent) {
        permanent.tap();
    }

    /**
     * Untap target permanent.
     */
    public static void untapPermanent(PermanentCard permanent) {
        permanent.untap();
    }

    /**
     * Add keyword ability to creature until end of turn.
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