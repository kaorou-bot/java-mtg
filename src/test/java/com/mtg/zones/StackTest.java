package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Stack.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Stack zone per Rule 405.
 */
class StackTest {

    private Stack stack;
    private Player caster;

    @BeforeEach
    void setUp() {
        stack = new Stack();
        caster = new Player("Caster");
    }

    private SpellCard makeInstant(String name) {
        return new SpellCard(name, ManaCost.of(1),
                "", Arrays.asList(ManaType.RED), SpellCard.SpellType.INSTANT);
    }

    @Test
    void testNewStackIsEmpty() {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
        assertNull(stack.peek());
    }

    @Test
    void testPushSpell() {
        SpellCard spell = makeInstant("Shock");
        SpellItem item = new SpellItem(spell, caster);
        stack.push(item);

        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());
        assertEquals("Shock", stack.peek().getName());
    }

    @Test
    void testResolveLIFO() {
        SpellCard s1 = makeInstant("First");
        SpellCard s2 = makeInstant("Second");

        stack.push(new SpellItem(s1, caster));
        stack.push(new SpellItem(s2, caster));

        StackItem resolved = stack.resolve();
        assertEquals("Second", resolved.getName()); // LIFO - last in, first out
        assertEquals(1, stack.size());

        resolved = stack.resolve();
        assertEquals("First", resolved.getName());
        assertTrue(stack.isEmpty());
    }

    @Test
    void testResolveEmptyStack() {
        assertNull(stack.resolve());
    }

    @Test
    void testPushAbility() {
        StringBuilder effectRan = new StringBuilder();
        AbilityItem ability = new AbilityItem("Tap for mana",
                caster, () -> effectRan.append("ran"));

        stack.push(ability);

        assertFalse(stack.isEmpty());
        assertEquals("Ability: Tap for mana", stack.peek().getName());
    }

    @Test
    void testPushTriggered() {
        TriggeredItem triggered = new TriggeredItem("At beginning of combat",
                caster, () -> {});

        stack.push(triggered);

        assertFalse(stack.isEmpty());
        assertEquals("Triggered: At beginning of combat", stack.peek().getName());
    }

    @Test
    void testCounterByIndex() {
        SpellCard s1 = makeInstant("First");
        SpellCard s2 = makeInstant("Second");

        stack.push(new SpellItem(s1, caster));
        stack.push(new SpellItem(s2, caster));

        // Counter the bottom one (index 0)
        StackItem countered = stack.counter(0);
        assertEquals("First", countered.getName());
        assertEquals(1, stack.size());
        assertEquals("Second", stack.peek().getName());
    }

    @Test
    void testCounterInvalidIndex() {
        assertNull(stack.counter(99));
        assertTrue(stack.isEmpty());
    }

    @Test
    void testGetItems() {
        stack.push(new SpellItem(makeInstant("S1"), caster));
        stack.push(new SpellItem(makeInstant("S2"), caster));

        var items = stack.getItems();
        assertEquals(2, items.size());
    }

    @Test
    void testSpellItemGetCard() {
        SpellCard spell = makeInstant("Lightning Bolt");
        SpellItem item = new SpellItem(spell, caster);

        assertEquals(spell, item.getCard());
        assertEquals(caster, item.getController());
        assertEquals(spell.getId(), item.getSourceId());
    }

    @Test
    void testZoneInterfaceMethods() {
        assertEquals("Stack", stack.getName());
        assertTrue(stack.isPublic());
        assertNull(stack.getOwner());
    }

    @Test
    void testStackItemResolution() {
        StringBuilder sb = new StringBuilder();
        AbilityItem ability = new AbilityItem("Test", caster, () -> sb.append("X"));

        assertEquals(0, sb.length());
        ability.resolve();
        assertEquals("X", sb.toString());
    }
}
