package com.mtg.zones;

import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.List;

/**
 * Zone represents a place where objects can exist during the game.
 *
 * Rule 400.1: There are normally seven zones: library, hand, battlefield,
 * graveyard, stack, exile, and command.
 *
 * Rule 400.2: Public zones are zones in which all players can see the cards'
 * faces. Graveyard, battlefield, stack, exile, ante, and command are public zones.
 * Hidden zones are zones in which not all players can be expected to see the cards'
 * faces. Library and hand are hidden zones.
 */
public interface Zone {

    /**
     * Get the name of this zone.
     */
    String getName();

    /**
     * Check if this is a public zone.
     * Rule 400.2: Public zones are visible to all players.
     */
    boolean isPublic();

    /**
     * Get the owner of this zone (for player-specific zones).
     * Returns null for shared zones like Battlefield, Stack, etc.
     */
    Player getOwner();

    /**
     * Add an object to this zone.
     * Rule 400.6: When an object would move to a zone, replacement effects are applied.
     *
     * @param object The object to add
     * @param controller The controller of the object
     */
    void add(GameObject object, Player controller);

    /**
     * Remove an object from this zone.
     *
     * @param object The object to remove
     * @return The removed object, or null if not found
     */
    GameObject remove(GameObject object);

    /**
     * Get all objects in this zone.
     */
    List<GameObject> getContents();

    /**
     * Check if an object is in this zone.
     */
    boolean contains(GameObject object);

    /**
     * Get the number of objects in this zone.
     */
    int size();

    /**
     * Get a copy of contents for safe iteration.
     */
    List<GameObject> getContentsSnapshot();
}
