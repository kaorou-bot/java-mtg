package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.model.PermanentCard;
import com.mtg.player.Player;
import com.mtg.game.Game;

import java.util.List;

/**
 * ZoneManager - 区域管理器，处理所有区域转换并维护所有区域的引用。
 *
 * 【功能说明】
 * - 管理所有游戏区域（牌库、手牌、战场、坟场、放逐、堆叠）
 * - 处理所有区域之间的卡牌移动
 * - 应用替代效果和处理触发式异能
 *
 * 【规则依据】
 * - Rule 400.6: 对象从一个区域移动到另一个区域时的处理步骤
 *   1. 确定移动对象的事件
 *   2. 如果移动到公开区域，拥有者检查是否有异能影响此移动
 *   3. 应用替代效果
 *   4. 执行移动
 *   5. 处理触发的异能
 * - Rule 400.7: 对象成为新对象，无之前存在的记忆
 *
 * 【设计决策】
 * - 使用数组索引管理两个玩家的独立区域
 * - 区域转换统一通过 moveObject() 方法处理
 * - 每次区域转换必须调用 Card.onZoneChange()
 *
 * 【陷阱警告】
 * - ZoneManager 依赖 Game.getPlayer1() 确定玩家索引
 * - Player.equals() 必须按 name 比较（ZoneManager.getPlayerIndex 依赖）
 */
public class ZoneManager {
    private final Game game;                      // 游戏引用（用于确定玩家索引）
    private final Library[] libraries;             // 牌库数组（按玩家索引）
    private final Hand[] hands;                   // 手牌数组
    private final Graveyard[] graveyards;         // 坟场数组
    private final Exile[] exiles;                // 放逐区数组
    private final Battlefield battlefield;         // 战场（共享）
    private final Stack stack;                    // 堆叠（共享）

    /**
     * 创建区域管理器，初始化所有区域。
     *
     * 【初始化内容】
     * - 两个玩家的独立区域（牌库、手牌、坟场、放逐）
     * - 共享区域（战场、堆叠）
     *
     * @param game 游戏引用
     * @param player1 玩家1
     * @param player2 玩家2
     */
    public ZoneManager(Game game, Player player1, Player player2) {
        this.game = game;
        this.battlefield = new Battlefield();
        this.stack = new Stack();

        // 创建玩家独立区域数组
        this.libraries = new Library[2];
        this.hands = new Hand[2];
        this.graveyards = new Graveyard[2];
        this.exiles = new Exile[2];

        // 初始化玩家1区域
        libraries[0] = new Library(player1);
        hands[0] = new Hand(player1);
        graveyards[0] = new Graveyard(player1);
        exiles[0] = new Exile();

        // 初始化玩家2区域
        libraries[1] = new Library(player2);
        hands[1] = new Hand(player2);
        graveyards[1] = new Graveyard(player2);
        exiles[1] = new Exile();
    }

    // ========== 区域访问方法 ==========

    /**
     * 获取玩家的牌库。
     *
     * @param player 玩家
     * @return 牌库
     */
    public Library getLibrary(Player player) {
        return libraries[getPlayerIndex(player)];
    }

    /**
     * 获取玩家的手牌。
     *
     * @param player 玩家
     * @return 手牌
     */
    public Hand getHand(Player player) {
        return hands[getPlayerIndex(player)];
    }

    /**
     * 获取玩家的坟场。
     *
     * @param player 玩家
     * @return 坟场
     */
    public Graveyard getGraveyard(Player player) {
        return graveyards[getPlayerIndex(player)];
    }

    /**
     * 获取玩家的放逐区。
     *
     * @param player 玩家
     * @return 放逐区
     */
    public Exile getExile(Player player) {
        return exiles[getPlayerIndex(player)];
    }

    /**
     * 获取战场。
     *
     * @return 战场
     */
    public Battlefield getBattlefield() {
        return battlefield;
    }

    /**
     * 获取堆叠。
     *
     * @return 堆叠
     */
    public Stack getStack() {
        return stack;
    }

    /**
     * 根据玩家获取索引（0=玩家1，1=玩家2）。
     *
     * 【依赖条件】
     * - Player.equals() 必须按 name 比较
     * - game.getPlayer1() 返回非 null
     *
     * @param player 玩家
     * @return 玩家索引（0或1）
     */
    private int getPlayerIndex(Player player) {
        return player.equals(game.getPlayer1()) ? 0 : 1;
    }

    // ========== 区域转换方法 ==========

    /**
     * 移动对象到新区域（使用原控制者）。
     *
     * 【执行步骤】
     * 1. 从源区域移除
     * 2. 添加到目标区域
     * 3. 更新对象的区域引用（Rule 400.7）
     * 4. 处理触发的异能
     *
     * 【规则依据】
     * - Rule 400.6: 区域转换标准流程
     * - Rule 400.7: 对象成为新对象
     *
     * @param object 要移动的对象
     * @param from 源区域
     * @param to 目标区域
     */
    public void moveObject(GameObject object, Zone from, Zone to) {
        moveObject(object, from, to, object.getController());
    }

    /**
     * 移动对象到新区域（指定控制者）。
     *
     * @param object 要移动的对象
     * @param from 源区域
     * @param to 目标区域
     * @param newController 新的控制者
     */
    public void moveObject(GameObject object, Zone from, Zone to, Player newController) {
        // Step 1: 从源区域移除
        if (from != null) {
            from.remove(object);
        }

        // Step 2: 添加到目标区域
        to.add(object, newController);

        // Step 3: 更新对象的区域引用（Rule 400.7）
        if (object.isCard()) {
            Card card = object.getCard();
            card.onZoneChange(to);
        }

        // Step 4: 处理触发的异能（Rule 400.7e）
        // 在公开区域中，对象可以被触发的异能发现
    }

    // ========== 常用区域操作 ==========

    /**
     * 从玩家牌库抽一张牌到手牌。
     *
     * 【规则依据】
     * - Rule 401.5: 从牌库顶抽牌
     *
     * @param player 抽牌的玩家
     * @return 抽到的牌，牌库为空时返回 null
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
     * 弃置一张牌到手牌到坟场。
     *
     * 【规则依据】
     * - Rule 701.11: 弃牌
     *
     * @param card 要弃置的牌
     * @param player 弃牌的玩家
     */
    public void discard(Card card, Player player) {
        Hand hand = getHand(player);
        Graveyard graveyard = getGraveyard(player);
        moveObject(card, hand, graveyard, player);
    }

    /**
     * 将永久物放置到战场。
     *
     * 【执行步骤】
     * 1. 从手牌移除
     * 2. 添加到战场
     * 3. 更新区域引用
     * 4. 取消横置
     *
     * 【规则依据】
     * - Rule 110.2: 永久物进入战场
     * - Rule 502.3: 重置步骤横置的永久物
     *
     * @param card 要放置的卡牌
     * @param controller 控制者
     */
    public void putOnBattlefield(Card card, Player controller) {
        Battlefield bf = getBattlefield();
        Hand hand = getHand(controller);
        if (hand.getCards().contains(card)) {
            hand.remove(card);
        }
        bf.add(card, controller);
        card.onZoneChange(bf);

        // 永久物进入战场时取消横置
        if (card instanceof PermanentCard) {
            ((PermanentCard) card).untap();
        }
    }

    /**
     * 消灭永久物，移至拥有者坟场。
     *
     * 【规则依据】
     * - Rule 701.7: 消灭
     *
     * @param permanent 要消灭的永久物
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
     * 牺牲永久物，移至拥有者坟场。
     *
     * 【规则依据】
     * - Rule 701.14: 牺牲
     *
     * @param permanent 要牺牲的永久物
     * @param player 牺牲它的玩家
     */
    public void sacrifice(PermanentCard permanent, Player player) {
        Battlefield bf = getBattlefield();
        Graveyard graveyard = getGraveyard(player);
        bf.remove(permanent);
        graveyard.add(permanent);
        permanent.onZoneChange(graveyard);
    }

    /**
     * 放逐永久物到放逐区。
     *
     * 【规则依据】
     * - Rule 406.1: 放逐区
     *
     * @param permanent 要放逐的永久物
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
     * 反击咒语，移至拥有者坟场。
     *
     * 【规则依据】
     * - Rule 701.5: 反击咒语
     *
     * @param card 被反击的牌
     * @param player 牌的拥有者
     */
    public void counterSpell(Card card, Player player) {
        Stack st = getStack();
        Graveyard graveyard = getGraveyard(player);
        st.remove(card);
        graveyard.add(card);
        card.onZoneChange(graveyard);
    }

    /**
     * 施放咒语，从手牌移到堆叠。
     *
     * 【规则依据】
     * - Rule 601.2h: 咒语进入堆叠
     *
     * @param card 要施放的牌
     * @param caster 施放者
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
     * 洗牌库。
     *
     * 【规则依据】
     * - Rule 103.2: 游戏开始时洗牌
     *
     * @param player 要洗牌的玩家
     */
    public void shuffleLibrary(Player player) {
        getLibrary(player).shuffle();
    }

    /**
     * 将手牌洗回牌库（用于多手规则/换调度）。
     *
     * @param player 手牌将被洗入牌库的玩家
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
