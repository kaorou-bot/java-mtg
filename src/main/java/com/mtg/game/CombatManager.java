package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;
import java.util.List;

public class CombatManager {

    public static boolean canBlock(CreatureCard attacker, CreatureCard blocker) {
        if (attacker.hasFlying() && !blocker.hasFlying()) {
            return false;
        }
        return true;
    }

    public static int calculateDamage(CreatureCard attacker, CreatureCard blocker,
                                      boolean blocked, boolean trample) {
        if (!blocked || blocker == null) {
            return attacker.getCurrentPower();
        }

        int damage = attacker.getCurrentPower();
        if (trample && blocker.getCurrentToughness() <= damage) {
            damage -= blocker.getCurrentToughness();
            return attacker.getCurrentPower();
        }
        return damage;
    }

    public static void dealDamageToCreature(CreatureCard creature, int damage) {
        creature.modifyToughness(-damage);
    }

    public static void dealDamageToPlayer(Player player, int damage) {
        player.modifyLife(-damage);
    }

    public static boolean isLethalDamage(CreatureCard creature, int damage) {
        return creature.getCurrentToughness() <= damage;
    }
}
