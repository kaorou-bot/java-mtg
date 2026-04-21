package com.mtg.player;

import com.mtg.model.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private int life;
    private Hand hand;
    private Deck deck;
    private Player opponent;

    // Battlefield - all permanent cards
    private List<PermanentCard> battlefield;
    private List<CreatureCard> creatures;
    private List<LandCard> lands;
    private List<EnchantmentCard> enchantments;
    private List<ArtifactCard> artifacts;
    private List<PlaneswalkerCard> planeswalkers;
    private List<BattleCard> battles;

    private List<LandCard> manaSources;
    private List<ManaType> availableMana;
    private int poisonCounters;
    private boolean landPlayedThisTurn;

    public static final int STARTING_LIFE = 20;
    public static final int STARTING_HAND_SIZE = 7;

    public Player(String name) {
        this.name = name;
        this.life = STARTING_LIFE;
        this.hand = new Hand();
        this.deck = new Deck();

        this.battlefield = new ArrayList<>();
        this.creatures = new ArrayList<>();
        this.lands = new ArrayList<>();
        this.enchantments = new ArrayList<>();
        this.artifacts = new ArrayList<>();
        this.planeswalkers = new ArrayList<>();
        this.battles = new ArrayList<>();

        this.manaSources = new ArrayList<>();
        this.availableMana = new ArrayList<>();
        this.poisonCounters = 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return name != null ? name.equals(player.name) : player.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public int getLife() {
        return life;
    }

    public void modifyLife(int amount) {
        this.life += amount;
    }

    public boolean isDead() {
        return life <= 0 || poisonCounters >= 10;
    }

    public Hand getHand() {
        return hand;
    }

    public Deck getDeck() {
        return deck;
    }

    // Battlefield accessors
    public List<PermanentCard> getBattlefield() {
        return new ArrayList<>(battlefield);
    }

    public List<CreatureCard> getCreatures() {
        return new ArrayList<>(creatures);
    }

    public List<LandCard> getLands() {
        return new ArrayList<>(lands);
    }

    public List<EnchantmentCard> getEnchantments() {
        return new ArrayList<>(enchantments);
    }

    public List<ArtifactCard> getArtifacts() {
        return new ArrayList<>(artifacts);
    }

    public List<PlaneswalkerCard> getPlaneswalkers() {
        return new ArrayList<>(planeswalkers);
    }

    public List<BattleCard> getBattles() {
        return new ArrayList<>(battles);
    }

    public List<LandCard> getManaSources() {
        return manaSources;
    }

    public List<ManaType> getAvailableMana() {
        return new ArrayList<>(availableMana);
    }

    public int getPoisonCounters() {
        return poisonCounters;
    }

    public void addPoison(int amount) {
        this.poisonCounters += amount;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void drawInitialHand() {
        for (int i = 0; i < STARTING_HAND_SIZE; i++) {
            drawCard();
        }
    }

    public Card drawCard() {
        Card card = deck.draw();
        if (card != null) {
            hand.addCard(card);
        }
        return card;
    }

    public void playLand(LandCard land) {
        if (hand.removeCard(land)) {
            addPermanent(land);
        }
    }

    public void addPermanent(PermanentCard permanent) {
        battlefield.add(permanent);

        if (permanent instanceof CreatureCard creature) {
            creatures.add(creature);
        } else if (permanent instanceof LandCard land) {
            lands.add(land);
            manaSources.add(land);
        } else if (permanent instanceof EnchantmentCard enchantment) {
            enchantments.add(enchantment);
        } else if (permanent instanceof ArtifactCard artifact) {
            artifacts.add(artifact);
        } else if (permanent instanceof PlaneswalkerCard planeswalker) {
            planeswalkers.add(planeswalker);
        } else if (permanent instanceof BattleCard battle) {
            battles.add(battle);
        }
    }

    public void removePermanent(PermanentCard permanent) {
        battlefield.remove(permanent);

        if (permanent instanceof CreatureCard creature) {
            creatures.remove(creature);
        } else if (permanent instanceof LandCard land) {
            lands.remove(land);
            manaSources.remove(land);
        } else if (permanent instanceof EnchantmentCard enchantment) {
            enchantments.remove(enchantment);
        } else if (permanent instanceof ArtifactCard artifact) {
            artifacts.remove(artifact);
        } else if (permanent instanceof PlaneswalkerCard planeswalker) {
            planeswalkers.remove(planeswalker);
        } else if (permanent instanceof BattleCard battle) {
            battles.remove(battle);
        }
    }

    public void addMana(ManaType mana) {
        availableMana.add(mana);
    }

    public void clearMana() {
        availableMana.clear();
    }

    public void produceMana() {
        availableMana.clear();
        for (LandCard land : manaSources) {
            if (!land.isTapped()) {
                availableMana.add(land.getProducedMana());
            }
        }
    }

    public boolean canPayCost(ManaCost cost) {
        return cost.canPayWith(availableMana);
    }

    public void payMana(ManaCost cost) {
        availableMana = cost.payMana(availableMana);
    }

    public void untapAll() {
        for (PermanentCard permanent : battlefield) {
            permanent.untap();
        }
    }

    public void startTurn() {
        produceMana();
        landPlayedThisTurn = false;
        for (CreatureCard creature : creatures) {
            creature.clearSummoningSickness();
            if (creature instanceof CreatureCard) {
                creature.resetForTurn();
            }
        }
    }

    public boolean hasPlayedLandThisTurn() {
        return landPlayedThisTurn;
    }

    public void setLandPlayedThisTurn(boolean played) {
        this.landPlayedThisTurn = played;
    }

    public List<CreatureCard> getAttackingCreatures() {
        List<CreatureCard> attacking = new ArrayList<>();
        for (CreatureCard creature : creatures) {
            if (creature.canAttack() && !creature.isTapped()) {
                attacking.add(creature);
            }
        }
        return attacking;
    }

    public List<CreatureCard> getBlockableCreatures() {
        return new ArrayList<>(creatures);
    }

    public void addCreatureToBattlefield(CreatureCard creature) {
        addPermanent(creature);
    }

    public void removeCreatureFromBattlefield(CreatureCard creature) {
        removePermanent(creature);
    }

    public void resetCreaturesForCombat() {
        for (CreatureCard creature : creatures) {
            creature.resetStats();
        }
    }

    public static class Hand {
        private final List<Card> cards;
        private static final int MAX_HAND_SIZE = 10;

        public Hand() {
            this.cards = new ArrayList<>();
        }

        public void addCard(Card card) {
            if (cards.size() < MAX_HAND_SIZE) {
                cards.add(card);
            }
        }

        public boolean removeCard(Card card) {
            return cards.remove(card);
        }

        public Card getCard(int index) {
            if (index >= 0 && index < cards.size()) {
                return cards.get(index);
            }
            return null;
        }

        public List<Card> getCards() {
            return new ArrayList<>(cards);
        }

        public int size() {
            return cards.size();
        }
    }
}
