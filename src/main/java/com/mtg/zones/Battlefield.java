package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Battlefield represents the shared play area where permanents exist.
 *
 * Rule 403.1: Most of the area between the players represents the battlefield.
 * Rule 403.2: A spell or ability affects and checks only the battlefield
 * unless it specifically mentions a player or another zone.
 * Rule 403.3: Permanents exist only on the battlefield.
 * Rule 403.4: Whenever a permanent enters the battlefield, it becomes a new
 * object and has no relationship to any previous permanent represented by the same card.
 *
 * Permanent types (Rule 110.4): Artifact, Battle, Creature, Enchantment, Land, Planeswalker
 */
public class Battlefield implements Zone {
    private final List<PermanentCard> permanents;

    public Battlefield() {
        this.permanents = new ArrayList<>();
    }

    /**
     * Add a permanent to the battlefield.
     */
    public void add(PermanentCard permanent) {
        if (!permanents.contains(permanent)) {
            permanents.add(permanent);
        }
    }

    /**
     * Remove a permanent from the battlefield.
     */
    public boolean remove(PermanentCard permanent) {
        return permanents.remove(permanent);
    }

    /**
     * Get all permanents on the battlefield.
     */
    public List<PermanentCard> getPermanents() {
        return new ArrayList<>(permanents);
    }

    /**
     * Get all creatures.
     */
    public List<CreatureCard> getCreatures() {
        List<CreatureCard> creatures = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof CreatureCard) {
                creatures.add((CreatureCard) p);
            }
        }
        return creatures;
    }

    /**
     * Get all lands.
     */
    public List<LandCard> getLands() {
        List<LandCard> lands = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof LandCard) {
                lands.add((LandCard) p);
            }
        }
        return lands;
    }

    /**
     * Get all enchantments.
     */
    public List<EnchantmentCard> getEnchantments() {
        List<EnchantmentCard> enchantments = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof EnchantmentCard) {
                enchantments.add((EnchantmentCard) p);
            }
        }
        return enchantments;
    }

    /**
     * Get all artifacts.
     */
    public List<ArtifactCard> getArtifacts() {
        List<ArtifactCard> artifacts = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof ArtifactCard) {
                artifacts.add((ArtifactCard) p);
            }
        }
        return artifacts;
    }

    /**
     * Get all planeswalkers.
     */
    public List<PlaneswalkerCard> getPlaneswalkers() {
        List<PlaneswalkerCard> planeswalkers = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof PlaneswalkerCard) {
                planeswalkers.add((PlaneswalkerCard) p);
            }
        }
        return planeswalkers;
    }

    /**
     * Get all battles.
     */
    public List<BattleCard> getBattles() {
        List<BattleCard> battles = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof BattleCard) {
                battles.add((BattleCard) p);
            }
        }
        return battles;
    }

    /**
     * Get permanents controlled by a specific player.
     */
    public List<PermanentCard> getControlledBy(Player player) {
        List<PermanentCard> controlled = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (player.equals(p.getController())) {
                controlled.add(p);
            }
        }
        return controlled;
    }

    /**
     * Get untapped permanents.
     */
    public List<PermanentCard> getUntapped() {
        List<PermanentCard> untapped = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (!p.isTapped()) {
                untapped.add(p);
            }
        }
        return untapped;
    }

    /**
     * Get untapped creatures that can attack.
     * Rule 508.1a: Creatures must be untapped to attack.
     */
    public List<CreatureCard> getAttackingCreatures(Player player) {
        List<CreatureCard> attacking = new ArrayList<>();
        for (CreatureCard c : getCreatures()) {
            if (!c.isTapped() && player.equals(c.getController()) &&
                (!c.hasSummoningSickness() || hasHaste(c))) {
                attacking.add(c);
            }
        }
        return attacking;
    }

    private boolean hasHaste(CreatureCard creature) {
        // TODO: Check for haste ability
        return false;
    }

    /**
     * Get untapped creatures that can block.
     */
    public List<CreatureCard> getBlockingCreatures(Player player) {
        List<CreatureCard> blocking = new ArrayList<>();
        for (CreatureCard c : getCreatures()) {
            if (!c.isTapped() && player.equals(c.getController())) {
                blocking.add(c);
            }
        }
        return blocking;
    }

    /**
     * Get all permanents of a specific type.
     */
    public List<PermanentCard> getByType(CardType type) {
        List<PermanentCard> result = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p.getType() == type) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Check if battlefield is empty.
     */
    public boolean isEmpty() {
        return permanents.isEmpty();
    }

    // ========== Zone Implementation ==========

    @Override
    public String getName() {
        return "Battlefield";
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public Player getOwner() {
        return null;  // Shared zone
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard() && object.getCard() instanceof PermanentCard) {
            PermanentCard permanent = (PermanentCard) object.getCard();
            permanent.setController(controller);
            add(permanent);
        }
    }

    @Override
    public GameObject remove(GameObject object) {
        if (object.isPermanent() && object.getCard() instanceof PermanentCard) {
            PermanentCard permanent = (PermanentCard) object.getCard();
            if (permanents.remove(permanent)) {
                return permanent;
            }
        }
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (PermanentCard p : permanents) {
            result.add(p);
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        if (object.isCard() && object.getCard() instanceof PermanentCard) {
            return permanents.contains(object.getCard());
        }
        return false;
    }

    @Override
    public int size() {
        return permanents.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }
}
