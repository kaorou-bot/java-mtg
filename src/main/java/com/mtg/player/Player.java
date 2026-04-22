package com.mtg.player;

import com.mtg.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Player - 玩家类，管理玩家的游戏状态。
 *
 * 【功能说明】
 * - 管理玩家的生命值、手牌、牌库
 * - 管理战场上的永久物
 * - 管理法力池和中毒指示物
 * - 管理对手关系
 *
 * 【规则依据】
 * - Rule 102: 玩家
 * - Rule 103: 游戏开始
 * - Rule 104: 游戏结束
 * - Rule 118: 生命值
 * - Rule 119: 中毒指示物
 *
 * 【设计决策】
 * - Player.equals() 按 name 比较
 * - ZoneManager.getPlayerIndex() 依赖此比较方法
 *
 * 【重要】
 * - 本类的 battlefield、creatures 等字段为备用副本
 * - 实际游戏状态通过 ZoneManager 访问
 */
public class Player {
    private String name;  // 玩家名称
    private int life;  // 生命值
    private Hand hand;  // 手牌
    private Deck deck;  // 牌库
    private Player opponent;  // 对手

    // 战场永久物（备用副本）
    private List<PermanentCard> battlefield;
    private List<CreatureCard> creatures;
    private List<LandCard> lands;
    private List<EnchantmentCard> enchantments;
    private List<ArtifactCard> artifacts;
    private List<PlaneswalkerCard> planeswalkers;
    private List<BattleCard> battles;

    private List<LandCard> manaSources;  // 法力来源
    private List<ManaType> availableMana;  // 可用法力（简化版本）
    private com.mtg.game.ManaPool manaPool;  // 法力池
    private int poisonCounters;  // 中毒指示物数量
    private boolean landPlayedThisTurn;  // 本回合是否已出地

    public static final int STARTING_LIFE = 20;  // 起始生命值
    public static final int STARTING_HAND_SIZE = 7;  // 起始手牌数量

    /**
     * 创建玩家。
     *
     * @param name 玩家名称
     */
    public Player(String name) {
        this.name = name;
        this.life = STARTING_LIFE;
        this.hand = new Hand();
        this.deck = new Deck();

        // 初始化战场列表
        this.battlefield = new ArrayList<>();
        this.creatures = new ArrayList<>();
        this.lands = new ArrayList<>();
        this.enchantments = new ArrayList<>();
        this.artifacts = new ArrayList<>();
        this.planeswalkers = new ArrayList<>();
        this.battles = new ArrayList<>();

        this.manaSources = new ArrayList<>();
        this.availableMana = new ArrayList<>();
        this.manaPool = new com.mtg.game.ManaPool();
        this.poisonCounters = 0;
    }

    // ========== 基础属性 ==========

    public String getName() {
        return name;
    }

    /**
     * 比较两个玩家是否相同（按名称）。
     *
     * 【重要】
     * - ZoneManager.getPlayerIndex() 依赖此方法
     * - 必须按 name 比较，不能按引用
     */
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

    /**
     * 修改生命值。
     *
     * @param amount 变化量（负数减少，正数增加）
     */
    public void modifyLife(int amount) {
        this.life += amount;
    }

    /**
     * 检查玩家是否已死亡。
     *
     * @return 生命值 ≤ 0 或中毒指示物 ≥ 10
     */
    public boolean isDead() {
        return life <= 0 || poisonCounters >= 10;
    }

    public Hand getHand() {
        return hand;
    }

    public Deck getDeck() {
        return deck;
    }

    // ========== 战场访问 ==========

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

    /**
     * 添加中毒指示物。
     *
     * @param amount 数量
     */
    public void addPoison(int amount) {
        this.poisonCounters += amount;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    // ========== 游戏动作 ==========

    /**
     * 抽初始手牌（7张）。
     */
    public void drawInitialHand() {
        for (int i = 0; i < STARTING_HAND_SIZE; i++) {
            drawCard();
        }
    }

    /**
     * 从牌库抽一张牌。
     *
     * @return 抽到的牌
     */
    public Card drawCard() {
        Card card = deck.draw();
        if (card != null) {
            hand.addCard(card);
        }
        return card;
    }

    /**
     * 出一张地牌。
     *
     * @param land 要出的地牌
     */
    public void playLand(LandCard land) {
        if (hand.removeCard(land)) {
            addPermanent(land);
        }
    }

    /**
     * 添加永久物到战场。
     *
     * @param permanent 要添加的永久物
     */
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

    /**
     * 从战场移除永久物。
     *
     * @param permanent 要移除的永久物
     */
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

    // ========== 法力管理 ==========

    /**
     * 添加法力到法力池。
     *
     * @param mana 法力类型
     */
    public void addMana(ManaType mana) {
        availableMana.add(mana);
    }

    /**
     * 清空法力池。
     */
    public void clearMana() {
        availableMana.clear();
        manaPool.clear();
    }

    /**
     * 产生法力（从所有未横置的地产生）。
     */
    public void produceMana() {
        availableMana.clear();
        manaPool.clear();
        for (LandCard land : manaSources) {
            if (!land.isTapped()) {
                ManaType produced = land.getProducedMana();
                availableMana.add(produced);
                manaPool.add(produced, 1);
            }
        }
    }

    /**
     * 获取法力池。
     *
     * @return 法力池
     */
    public com.mtg.game.ManaPool getManaPool() {
        return manaPool;
    }

    /**
     * 检查是否可以支付费用。
     *
     * @param cost 费用
     * @return 是否可以支付
     */
    public boolean canPayCost(ManaCost cost) {
        // 优先使用 manaPool
        if (manaPool != null && !manaPool.isEmpty()) {
            return manaPool.canPay(cost);
        }
        // 回退到 availableMana
        return cost.canPayWith(availableMana);
    }

    /**
     * 支付法力。
     *
     * @param cost 费用
     */
    public void payMana(ManaCost cost) {
        // 优先使用 manaPool
        if (manaPool != null && !manaPool.isEmpty()) {
            manaPool.pay(cost);
        } else {
            // 回退到 availableMana
            availableMana = cost.payMana(availableMana);
        }
    }

    // ========== 回合相关 ==========

    /**
     * 重置所有永久物（取消横置）。
     */
    public void untapAll() {
        for (PermanentCard permanent : battlefield) {
            permanent.untap();
        }
    }

    /**
     * 开始回合。
     */
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

    /**
     * 获取可以攻击的生物。
     *
     * @return 可攻击生物列表
     */
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

    /**
     * 重置所有生物状态。
     */
    public void resetCreaturesForCombat() {
        for (CreatureCard creature : creatures) {
            creature.resetStats();
        }
    }

    /**
     * Hand - 玩家手牌内部类。
     *
     * 【规则依据】
     * - Rule 402: 手牌
     */
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
