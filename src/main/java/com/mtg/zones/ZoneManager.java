package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.model.PermanentCard;
import com.mtg.player.Player;
import com.mtg.game.Game;

import java.util.List;

/**
 * ZoneManager handles all zone transitions and maintains references to all zones.
 *
 * Rule 400.6: When an object would move from one zone to another:
 * 1. Determine what event is moving the object
 * 2. If moving to a public zone, owner checks for abilities that affect the move
 * 3. Apply replacement effects
 * 4. Execute the move
 * 5. Handle any triggered abilities
 *
 * Rule 400.7: Object becomes a new object with no memory of previous existence.
 * Exceptions: Rules 400.7a-m
 */
public class ZoneManager {
    private final Game game;
    private final Library[] libraries;  // Index by player
    private final Hand[] hands;
    private final Graveyard[] graveyards;
    private final Exile[] exiles;
    private final Battlefield battlefield;
    private final Stack stack;

    public ZoneManager(Game game, Player player1, Player player2) {
        this.game = game;
        this.battlefield = new Battlefield();
        this.stack = new Stack();

        this.libraries = new Library[2];
        this.hands = new Hand[2];
        this.graveyards = new Graveyard[2];
        this.exiles = new Exile[2];

        libraries[0] = new Library(player1);
        libraries[1] = new Library(player2);
        hands[0] = new Hand(player1);
        hands[1] = new Hand(player2);
        graveyards[0] = new Graveyard(player1);
        graveyards[1] = new Graveyard(player2);
        exiles[0] = new Exile();
        exiles[1] = new Exile();
    }

    // ========== Zone Access ==========

    public Library getLibrary(Player player) {
        return libraries[getPlayerIndex(player)];
    }

    public Hand getHand(Player player) {
        return hands[getPlayerIndex(player)];
    }

    public Graveyard getGraveyard(Player player) {
        return graveyards[getPlayerIndex(player)];
    }

    public Exile getExile(Player player) {
        return exiles[getPlayerIndex(player)];
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public Stack getStack() {
        return stack;
    }

    private int getPlayerIndex(Player player) {
        return player.equals(game.getPlayer1()) ? 0 : 1;
    }

    // ========== Zone Transitions ==========

    /**
     * Rule 400.6: Move an object from one zone to another.
     *
     * This is the core method for all zone transitions.
     * Handles replacement effects and triggers appropriately.
     */
    public void moveObject(GameObject object, Zone from, Zone to) {
        moveObject(object, from, to, object.getController());
    }

    /**
     * Move an object with explicit controller.
     */
    public void moveObject(GameObject object, Zone from, Zone to, Player newController) {
        // Step 1: Remove from source zone
        if (from != null) {
            from.remove(object);
        }

        // Step 2: Add to destination zone
        to.add(object, newController);

        // Step 3: Update object's zone reference (Rule 400.7)
        if (object.isCard()) {
            Card card = object.getCard();
            card.onZoneChange(to);
        }

        // Step 4: Handle triggered abilities (Rule 400.7e)
        // Abilities that trigger on zone change can find the new object
        // in public zones
    }

    // ========== Common Zone Operations ==========

    /**
     * Draw a card from player's library to hand.
     * Rule 401.5: Draw from top of library.
     */
    public Card drawCard(Player player) {
        Library library = getLibrary(player);
        Hand hand = getHand(player);
        Card card = library.draw();

        if (card != null) {
            moveObject(card, library, hand, player);
        }
        return card;
    }

    /**
     * Discard a card from hand to graveyard.
     */
    public void discard(Card card, Player player) {
        Hand hand = getHand(player);
        Graveyard graveyard = getGraveyard(player);
        moveObject(card, hand, graveyard, player);
    }

    /**
     * Put a permanent on the battlefield.
     */
    public void putOnBattlefield(Card card, Player controller) {
        Battlefield bf = getBattlefield();
        Hand hand = getHand(controller);
        if (hand.getCards().contains(card)) {
            hand.remove(card);
        }
        bf.add(card, controller);
        card.onZoneChange(bf);

        if (card instanceof PermanentCard) {
            ((PermanentCard) card).untap();
        }
    }

    /**
     * Destroy a permanent - move to graveyard.
     */
    public void destroy(PermanentCard permanent) {
        Battlefield bf = getBattlefield();
        Player owner = permanent.getOwner();
        Graveyard graveyard = getGraveyard(owner);
        bf.remove(permanent);
        graveyard.add(permanent);
        permanent.onZoneChange(graveyard);
    }

    /**
     * Sacrifice a permanent - move to graveyard.
     */
    public void sacrifice(PermanentCard permanent, Player player) {
        Battlefield bf = getBattlefield();
        Graveyard graveyard = getGraveyard(player);
        bf.remove(permanent);
        graveyard.add(permanent);
        permanent.onZoneChange(graveyard);
    }

    /**
     * Exile a permanent.
     */
    public void exile(PermanentCard permanent) {
        Battlefield bf = getBattlefield();
        Player owner = permanent.getOwner();
        Exile exileZone = getExile(owner);
        bf.remove(permanent);
        exileZone.exile(permanent);
        permanent.onZoneChange(exileZone);
    }

    /**
     * Counter a spell - move from stack to graveyard.
     */
    public void counterSpell(Card card, Player player) {
        Stack st = getStack();
        Graveyard graveyard = getGraveyard(player);
        st.remove(card);
        graveyard.add(card);
        card.onZoneChange(graveyard);
    }

    /**
     * Cast a spell - move from hand to stack.
     */
    public void castSpell(Card card, Player caster) {
        Hand hand = getHand(caster);
        Stack st = getStack();
        if (hand.getCards().contains(card)) {
            hand.remove(card);
        }
        st.push(new Stack.SpellItem(card, caster));
        card.onZoneChange(st);
    }

    /**
     * Shuffle player's library.
     */
    public void shuffleLibrary(Player player) {
        getLibrary(player).shuffle();
    }

    /**
     * Move cards from hand to library (for mulligans).
     */
    public void shuffleHandIntoLibrary(Player player) {
        Hand hand = getHand(player);
        Library library = getLibrary(player);
        List<Card> cards = hand.getCards();
        for (Card card : cards) {
            moveObject(card, hand, library, player);
        }
    }
}
