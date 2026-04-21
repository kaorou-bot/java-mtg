package com.mtg.card;

import com.mtg.model.*;
import com.mtg.player.Deck;
import java.util.List;

public class CardLibrary {

    public static Deck createStarterDeck(String playerName) {
        Deck deck = new Deck();

        // Basic Lands (12 total - 2 of each)
        for (int i = 0; i < 2; i++) {
            deck.addCard(createPlains());
            deck.addCard(createIsland());
            deck.addCard(createSwamp());
            deck.addCard(createMountain());
            deck.addCard(createForest());
        }

        // White Cards
        for (int i = 0; i < 2; i++) {
            deck.addCard(createWhiteKnight());
        }
        deck.addCard(createGideon());
        deck.addCard(createRevitalize());

        // Blue Cards
        for (int i = 0; i < 2; i++) {
            deck.addCard(createCounterspell());
            deck.addCard(createWindDrake());
        }
        deck.addCard(createCancel());

        // Black Cards
        for (int i = 0; i < 2; i++) {
            deck.addCard(createDarkRitual());
            deck.addCard(createDoomBlade());
        }
        deck.addCard(createDuress());

        // Red Cards
        for (int i = 0; i < 2; i++) {
            deck.addCard(createLightningBolt());
            deck.addCard(createGoblinGuide());
        }
        deck.addCard(createShock());
        deck.addCard(createFireball());

        // Green Cards
        for (int i = 0; i < 2; i++) {
            deck.addCard(createGiantGrowth());
            deck.addCard(createWallOfBlossoms());
        }
        deck.addCard(createLlanowarElves());
        deck.addCard(createRampantGrowth());
        deck.addCard(createOakenform());

        // Colorless/Neutral
        deck.addCard(createSolRing());
        deck.addCard(createGideonOfTheTrials());
        deck.addCard(createInvasionOfTolvada());

        deck.shuffle();
        return deck;
    }

    // ============ WHITE CARDS ============

    public static Card createPlains() {
        return new LandCard("Plains", List.of(ManaType.WHITE), ManaType.WHITE);
    }

    public static Card createWhiteKnight() {
        return new CreatureCard(
            "White Knight",
            ManaCost.of(1, ManaType.WHITE),
            "First Strike - deals damage first in combat",
            List.of(ManaType.WHITE),
            2, 2,
            true, false, false, false, false
        );
    }

    public static Card createGideon() {
        return new CreatureCard(
            "Gideon's Representative",
            ManaCost.of(2, ManaType.WHITE),
            "Vigilance - attacks without tapping, 3/3",
            List.of(ManaType.WHITE),
            3, 3,
            false, false, false, true, false
        );
    }

    public static Card createRevitalize() {
        return new SpellCard(
            "Revitalize",
            ManaCost.of(1, ManaType.WHITE),
            "Target player gains 3 life",
            List.of(ManaType.WHITE),
            SpellCard.SpellType.INSTANT
        );
    }

    // ============ BLUE CARDS ============

    public static Card createIsland() {
        return new LandCard("Island", List.of(ManaType.BLUE), ManaType.BLUE);
    }

    public static Card createCounterspell() {
        return new SpellCard(
            "Counterspell",
            ManaCost.of(1, ManaType.BLUE),
            "Counter target spell",
            List.of(ManaType.BLUE),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createCancel() {
        return new SpellCard(
            "Cancel",
            ManaCost.of(2, ManaType.BLUE),
            "Counter target spell",
            List.of(ManaType.BLUE),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createWindDrake() {
        return new CreatureCard(
            "Wind Drake",
            ManaCost.of(2, ManaType.BLUE),
            "Flying - can only be blocked by flying",
            List.of(ManaType.BLUE),
            2, 2,
            false, false, true, false, false
        );
    }

    // ============ BLACK CARDS ============

    public static Card createSwamp() {
        return new LandCard("Swamp", List.of(ManaType.BLACK), ManaType.BLACK);
    }

    public static Card createDarkRitual() {
        return new SpellCard(
            "Dark Ritual",
            ManaCost.of(1, ManaType.BLACK),
            "Add 3 black mana",
            List.of(ManaType.BLACK),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createDoomBlade() {
        return new SpellCard(
            "Doom Blade",
            ManaCost.of(1, ManaType.BLACK),
            "Destroy target non-black creature",
            List.of(ManaType.BLACK),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createDuress() {
        return new SpellCard(
            "Duress",
            ManaCost.of(1, ManaType.BLACK),
            "Target opponent discards a non-creature spell",
            List.of(ManaType.BLACK),
            SpellCard.SpellType.SORCERY
        );
    }

    // ============ RED CARDS ============

    public static Card createMountain() {
        return new LandCard("Mountain", List.of(ManaType.RED), ManaType.RED);
    }

    public static Card createLightningBolt() {
        return new SpellCard(
            "Lightning Bolt",
            ManaCost.of(ManaType.RED),
            "Deal 3 damage to any target",
            List.of(ManaType.RED),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createShock() {
        return new SpellCard(
            "Shock",
            ManaCost.of(ManaType.RED),
            "Deal 2 damage to any target",
            List.of(ManaType.RED),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createGoblinGuide() {
        return new CreatureCard(
            "Goblin Guide",
            ManaCost.of(ManaType.RED),
            "Vigilance, 2/2",
            List.of(ManaType.RED),
            2, 2,
            false, false, false, true, false
        );
    }

    public static Card createFireball() {
        return new SpellCard(
            "Fireball",
            ManaCost.of(2, ManaType.RED),
            "Deal X damage divided as you choose",
            List.of(ManaType.RED),
            SpellCard.SpellType.SORCERY
        );
    }

    // ============ GREEN CARDS ============

    public static Card createForest() {
        return new LandCard("Forest", List.of(ManaType.GREEN), ManaType.GREEN);
    }

    public static Card createGiantGrowth() {
        return new SpellCard(
            "Giant Growth",
            ManaCost.of(ManaType.GREEN),
            "Target creature gets +3/+3 until end of turn",
            List.of(ManaType.GREEN),
            SpellCard.SpellType.INSTANT
        );
    }

    public static Card createWallOfBlossoms() {
        return new CreatureCard(
            "Wall of Blossoms",
            ManaCost.of(1, ManaType.GREEN),
            "Vigilance - taps to block, 0/4",
            List.of(ManaType.GREEN),
            0, 4,
            false, false, false, false, false
        );
    }

    public static Card createLlanowarElves() {
        return new CreatureCard(
            "Llanowar Elves",
            ManaCost.of(ManaType.GREEN),
            "Tap for 1 mana of any color",
            List.of(ManaType.GREEN),
            1, 1,
            false, false, false, false, false
        );
    }

    public static Card createRampantGrowth() {
        return new SpellCard(
            "Rampant Growth",
            ManaCost.of(1, ManaType.GREEN),
            "Search for 2 lands and put into play",
            List.of(ManaType.GREEN),
            SpellCard.SpellType.SORCERY
        );
    }

    // ============ NEUTRAL/COLORLESS ============

    public static Card createWallOfStone() {
        return new CreatureCard(
            "Wall of Stone",
            ManaCost.of(2, ManaType.COLORLESS),
            "Can only block, 0/8",
            List.of(),
            0, 8,
            false, false, false, false, false
        );
    }

    // ============ ENCHANTMENTS ============

    public static Card createSpiritMaze() {
        return new EnchantmentCard(
            "Spirit Maze",
            ManaCost.of(1, ManaType.BLUE),
            "Enchantment - Aura",
            List.of(ManaType.BLUE),
            "Aura"
        );
    }

    public static Card createOakenform() {
        return new EnchantmentCard(
            "Oaken Form",
            ManaCost.of(2, ManaType.GREEN),
            "Enchantment - Aura - Enchant creature",
            List.of(ManaType.GREEN),
            "Aura"
        );
    }

    public static Card createPacifism() {
        return new EnchantmentCard(
            "Pacifism",
            ManaCost.of(1, ManaType.WHITE),
            "Enchantment - Aura - Enchanted creature cannot attack",
            List.of(ManaType.WHITE),
            "Aura"
        );
    }

    public static Card createGreed() {
        return new EnchantmentCard(
            "Greed",
            ManaCost.of(2, ManaType.BLACK),
            "Enchantment - At end of turn, draw a card",
            List.of(ManaType.BLACK),
            "Normal"
        );
    }

    // ============ ARTIFACTS ============

    public static Card createSolRing() {
        return new ArtifactCard(
            "Sol Ring",
            ManaCost.of(1),
            "Artifact - Tap: Add 2 colorless mana",
            List.of(),
            false
        );
    }

    public static Card createGorgonsFlayer() {
        return new ArtifactCard(
            "Gorgon's Flayer",
            ManaCost.of(2),
            "Artifact - Equipped creature gets +1/+3",
            List.of(),
            true
        );
    }

    public static Card createMizziumTransreliquat() {
        return new ArtifactCard(
            "Mizzium Transreliquat",
            ManaCost.of(3),
            "Artifact - Can become copy of artifact or creature",
            List.of(),
            false
        );
    }

    // ============ PLANESWALKERS ============

    public static Card createGideonOfTheTrials() {
        return new PlaneswalkerCard(
            "Gideon of the Trials",
            ManaCost.of(2, ManaType.WHITE),
            "Planeswalker - Gideon - +1: Until next turn, prevent damage",
            List.of(ManaType.WHITE),
            4
        );
    }

    public static Card createJaceCunningCastaway() {
        return new PlaneswalkerCard(
            "Jace, Cunning Castaway",
            ManaCost.of(2, ManaType.BLUE),
            "Planeswalker - Jace - +1: Draw a card",
            List.of(ManaType.BLUE),
            5
        );
    }

    public static Card createLilianaUntouched() {
        return new PlaneswalkerCard(
            "Liliana, Untouched",
            ManaCost.of(3, ManaType.BLACK),
            "Planeswalker - Liliana - -X: Destroy X creatures",
            List.of(ManaType.BLACK),
            4
        );
    }

    public static Card createChandraTorch() {
        return new PlaneswalkerCard(
            "Chandra, Torch of Defiance",
            ManaCost.of(3, ManaType.RED),
            "Planeswalker - Chandra - +1: Deal 2 damage",
            List.of(ManaType.RED),
            4
        );
    }

    public static Card createNissaVoiceOfGenesis() {
        return new PlaneswalkerCard(
            "Nissa, Voice of Genesis",
            ManaCost.of(3, ManaType.GREEN),
            "Planeswalker - Nissa - +1: Put a land from deck into play",
            List.of(ManaType.GREEN),
            5
        );
    }

    // ============ BATTLES ============

    public static Card createInvasionOfTolvada() {
        return new BattleCard(
            "Invasion of Tolvada",
            ManaCost.of(3, ManaType.WHITE),
            "Battle - Siege - Defense 5",
            List.of(ManaType.WHITE),
            5,
            "Siege"
        );
    }

    public static Card createInvasionOfSegovia() {
        return new BattleCard(
            "Invasion of Segovia",
            ManaCost.of(2),
            "Battle - Defense 2",
            List.of(),
            2,
            "Normal"
        );
    }
}
