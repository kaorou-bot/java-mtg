package com.mtg.zones;

import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack represents the zone where spells and abilities wait to resolve.
 *
 * Rule 405.1: When a spell is cast, the card is put on the stack.
 * When an ability is activated or triggers, it goes on top of the stack.
 * Rule 405.2: The stack keeps track of the order that spells and abilities
 * were added. Each time an object is put on the stack, it's put on top.
 * Rule 405.3: If an effect puts multiple objects on the stack at the same time,
 * those controlled by the active player are put lowest, followed by other players
 * in APNAP order.
 * Rule 405.4: Each spell has the characteristics of its card.
 * Each activated/triggered ability on the stack has the text of the ability.
 * Rule 405.5: When all players pass in succession, the top spell or ability
 * on the stack resolves. If the stack is empty, the current step/phase ends.
 */
public class Stack implements Zone {
    private final List<StackItem> items;

    public Stack() {
        this.items = new ArrayList<>();
    }

    /**
     * Push a spell onto the stack.
     * Rule 405.2: New items go on top.
     */
    public void push(SpellItem spell) {
        items.add(spell);
    }

    /**
     * Push an activated ability onto the stack.
     */
    public void push(AbilityItem ability) {
        items.add(ability);
    }

    /**
     * Push a triggered ability onto the stack.
     */
    public void push(TriggeredItem triggered) {
        items.add(triggered);
    }

    /**
     * Peek at the top item without removing it.
     */
    public StackItem peek() {
        if (items.isEmpty()) return null;
        return items.get(items.size() - 1);
    }

    /**
     * Resolve and remove the top item.
     * Rule 405.5: LIFO order.
     */
    public StackItem resolve() {
        if (items.isEmpty()) return null;
        return items.remove(items.size() - 1);
    }

    /**
     * Check if the stack is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Get all items on the stack.
     */
    public List<StackItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Counter (remove) an item on the stack by index.
     */
    public StackItem counter(int index) {
        if (index >= 0 && index < items.size()) {
            return items.remove(index);
        }
        return null;
    }

    /**
     * Check if all players have passed.
     * Called when current player passes.
     */
    public boolean allPlayersPassed(List<Player> players) {
        // Implementation depends on tracking pass sequence
        // For now, just resolve top item
        return false;
    }

    // ========== Zone Implementation ==========

    @Override
    public String getName() {
        return "Stack";
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
        // Stack items are added via push methods
    }

    @Override
    public GameObject remove(GameObject object) {
        // Stack items are removed via resolve/counter methods
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (StackItem item : items) {
            if (item instanceof GameObject) {
                result.add((GameObject) item);
            }
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        // Stack items are identified by their source ID
        for (StackItem item : items) {
            if (item.getSourceId() != null && item.getSourceId().equals(object.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }

    /**
     * Base class for items on the stack.
     */
    public static abstract class StackItem {
        protected Player controller;
        protected String sourceId;

        public Player getController() {
            return controller;
        }

        public String getSourceId() {
            return sourceId;
        }

        public abstract String getName();

        public abstract void resolve();
    }

    /**
     * Represents a spell on the stack.
     */
    public static class SpellItem extends StackItem {
        private final com.mtg.model.Card card;

        public SpellItem(com.mtg.model.Card card, Player controller) {
            this.card = card;
            this.controller = controller;
            this.sourceId = card.getId();
        }

        public com.mtg.model.Card getCard() {
            return card;
        }

        @Override
        public String getName() {
            return card.getName();
        }

        @Override
        public void resolve() {
            // Spell resolution handled by game
        }
    }

    /**
     * Represents an activated ability on the stack.
     */
    public static class AbilityItem extends StackItem {
        private final String abilityText;
        private final Runnable effect;

        public AbilityItem(String abilityText, Player controller, Runnable effect) {
            this.abilityText = abilityText;
            this.controller = controller;
            this.effect = effect;
        }

        @Override
        public String getName() {
            return "Ability: " + abilityText;
        }

        @Override
        public void resolve() {
            if (effect != null) {
                effect.run();
            }
        }
    }

    /**
     * Represents a triggered ability on the stack.
     */
    public static class TriggeredItem extends StackItem {
        private final String triggerText;
        private final Runnable effect;

        public TriggeredItem(String triggerText, Player controller, Runnable effect) {
            this.triggerText = triggerText;
            this.controller = controller;
            this.effect = effect;
        }

        @Override
        public String getName() {
            return "Triggered: " + triggerText;
        }

        @Override
        public void resolve() {
            if (effect != null) {
                effect.run();
            }
        }
    }
}
