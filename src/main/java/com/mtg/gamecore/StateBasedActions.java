package com.mtg.gamecore;

import com.mtg.player.Player;
import com.mtg.model.CreatureCard;
import com.mtg.model.PlaneswalkerCard;
import com.mtg.model.PermanentCard;
import com.mtg.model.EnchantmentCard;
import com.mtg.zones.Battlefield;
import com.mtg.zones.Graveyard;
import com.mtg.zones.Library;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * StateBasedActions handles state-based actions according to Rule 704.
 *
 * Rule 704.1: State-based actions are game actions performed automatically, without using the stack.
 * Rule 704.2: Checked before player receives priority (Rule 117.5).
 * Rule 704.3: After state-based actions, check triggered abilities, repeat until stable.
 * Rule 704.4: All applicable state-based actions are performed simultaneously.
 * Rule 704.5: List of state-based actions for a two-player game.
 */
public class StateBasedActions {

    /**
     * Check and perform all applicable state-based actions.
     * @return true if any action was performed
     */
    public static boolean check(Player player1, Player player2,
                                 Battlefield battlefield,
                                 Library library1, Library library2) {
        boolean actionPerformed = false;

        // 704.5a: Life ≤ 0 → player loses
        if (checkLife(player1)) actionPerformed = true;
        if (checkLife(player2)) actionPerformed = true;

        // 704.5c: Poison counters ≥ 10
        if (checkPoison(player1)) actionPerformed = true;
        if (checkPoison(player2)) actionPerformed = true;

        // 704.5f: Creature toughness ≤ 0
        if (checkCreatureToughness(battlefield, player1, player2)) actionPerformed = true;

        // 704.5g: Lethal marked damage
        if (checkLethalDamage(battlefield, player1, player2)) actionPerformed = true;

        // 704.5i: Planeswalker loyalty = 0
        if (checkPlaneswalkerLoyalty(battlefield, player1, player2)) actionPerformed = true;

        // 704.5j: Legend rule
        if (checkLegendRule(battlefield, player1, player2)) actionPerformed = true;

        // 704.5m: Aura illegal target
        if (checkAuraTargets(battlefield, player1, player2)) actionPerformed = true;

        // 704.5s: Battle defense = 0
        if (checkBattleDefense(battlefield, player1, player2)) actionPerformed = true;

        return actionPerformed;
    }

    /**
     * 704.5a: Player's life total is 0 or less.
     * @return true if player loses
     */
    private static boolean checkLife(Player player) {
        return player.getLife() <= 0;
    }

    /**
     * 704.5c: 10 or more poison counters.
     */
    private static boolean checkPoison(Player player) {
        return player.getPoisonCounters() >= 10;
    }

    /**
     * 704.5f: Creature's toughness is 0 or less → destroy.
     */
    private static boolean checkCreatureToughness(Battlefield bf, Player p1, Player p2) {
        List<CreatureCard> toDestroy = new java.util.ArrayList<>();
        for (CreatureCard c : bf.getCreatures()) {
            if (c.getToughness() <= 0) {
                toDestroy.add(c);
            }
        }
        for (CreatureCard c : toDestroy) {
            destroyPermanent(c, bf, c.getOwner());
        }
        return !toDestroy.isEmpty();
    }

    /**
     * 704.5g: Lethal damage marked on creature.
     */
    private static boolean checkLethalDamage(Battlefield bf, Player p1, Player p2) {
        List<CreatureCard> toDestroy = new java.util.ArrayList<>();
        for (CreatureCard c : bf.getCreatures()) {
            if (c.getMarkedDamage() >= c.getToughness()) {
                toDestroy.add(c);
            }
        }
        for (CreatureCard c : toDestroy) {
            destroyPermanent(c, bf, c.getOwner());
        }
        return !toDestroy.isEmpty();
    }

    /**
     * 704.5i: Planeswalker loyalty = 0.
     */
    private static boolean checkPlaneswalkerLoyalty(Battlefield bf, Player p1, Player p2) {
        List<PlaneswalkerCard> toMove = new java.util.ArrayList<>();
        for (PlaneswalkerCard pw : bf.getPlaneswalkers()) {
            if (pw.getLoyalty() <= 0) {
                toMove.add(pw);
            }
        }
        for (PlaneswalkerCard pw : toMove) {
            destroyPermanent(pw, bf, pw.getOwner());
        }
        return !toMove.isEmpty();
    }

    /**
     * 704.5j: Legend rule - two legendary permanents with same name.
     */
    private static boolean checkLegendRule(Battlefield bf, Player p1, Player p2) {
        Map<String, java.util.List<PermanentCard>> legendaries = new HashMap<>();
        for (PermanentCard p : bf.getPermanents()) {
            if (p.isLegendary() && p.getName() != null) {
                legendaries.computeIfAbsent(p.getName(), k -> new java.util.ArrayList<>()).add(p);
            }
        }

        List<PermanentCard> toDestroy = new java.util.ArrayList<>();
        for (Map.Entry<String, List<PermanentCard>> entry : legendaries.entrySet()) {
            List<PermanentCard> sameName = entry.getValue();
            if (sameName.size() > 1) {
                // Keep only the most recently entered (first in list, destroy rest)
                for (int i = 1; i < sameName.size(); i++) {
                    toDestroy.add(sameName.get(i));
                }
            }
        }
        for (PermanentCard p : toDestroy) {
            destroyPermanent(p, bf, p.getOwner());
        }
        return !toDestroy.isEmpty();
    }

    /**
     * 704.5m: Aura enchanting illegal target → graveyard.
     */
    private static boolean checkAuraTargets(Battlefield bf, Player p1, Player p2) {
        List<EnchantmentCard> toMove = new java.util.ArrayList<>();
        for (EnchantmentCard e : bf.getEnchantments()) {
            if (e.isAura()) {
                PermanentCard target = e.getAttachedTo();
                if (target == null || !bf.getPermanents().contains(target)) {
                    toMove.add(e);
                }
            }
        }
        for (EnchantmentCard e : toMove) {
            destroyPermanent(e, bf, e.getOwner());
        }
        return !toMove.isEmpty();
    }

    /**
     * 704.5s: Battle defense = 0.
     */
    private static boolean checkBattleDefense(Battlefield bf, Player p1, Player p2) {
        List<com.mtg.model.BattleCard> toMove = new java.util.ArrayList<>();
        for (com.mtg.model.BattleCard b : bf.getBattles()) {
            if (b.getDefense() <= 0) {
                toMove.add(b);
            }
        }
        for (com.mtg.model.BattleCard b : toMove) {
            destroyPermanent(b, bf, b.getOwner());
        }
        return !toMove.isEmpty();
    }

    /**
     * Helper: Move permanent to owner's graveyard.
     */
    private static void destroyPermanent(PermanentCard permanent, Battlefield bf, Player owner) {
        bf.remove(permanent);
        // Owner graveyard would be set via ZoneManager
    }

    /**
     * Clear all marked damage during cleanup step (Rule 514.2).
     */
    public static void clearAllDamage(Battlefield battlefield) {
        for (CreatureCard c : battlefield.getCreatures()) {
            c.clearDamage();
        }
    }
}
