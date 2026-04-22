package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;
import com.mtg.game.Game;

import java.util.*;

/**
 * Library - 牌库，表示玩家的牌组（牌张抽取来源）。
 *
 * 【功能说明】
 * - 存储玩家的所有牌张
 * - 提供抓牌、洗牌、查看顶牌等操作
 * - 实现 Zone 接口
 *
 * 【规则依据】
 * - Rule 401.1: 游戏开始时，每个玩家的牌组成为其牌库
 * - Rule 401.2: 牌库必须保持为单一正面朝下的牌堆
 * - Rule 401.4: 效果将两张或更多牌放入牌库特定位置时，拥有者可以任意排列
 * - Rule 401.5: 查看顶牌时，如果顶牌在施放过程中改变，新牌不能被查看
 * - Rule 401.7: 将牌放在牌库顶第 N 张位置但牌少于 N 张时，牌放在底部
 *
 * 【失败条件】
 * - Rule 104.3c: 如果玩家抓牌数多于牌库剩余牌数，抓完剩余牌后在获得优先权时输掉游戏
 *
 * 【陷阱警告】
 * - 牌库为空时 draw() 返回 null，但游戏不会立即结束
 * - 玩家在获得优先权时（空牌库判负）才输掉游戏
 */
public class Library implements Zone {
    private final Player owner;              // 拥有者
    private final List<Card> cards;         // 牌张列表（正面朝下，顶牌在索引 0）
    private boolean isPublic = false;        // 始终为隐藏区域

    // 追踪强制抓牌数量（用于空牌库判负）
    private int pendingLossDraws = 0;

    /**
     * 创建牌库。
     *
     * @param owner 拥有者
     */
    public Library(Player owner) {
        this.owner = owner;
        this.cards = new ArrayList<>();
    }

    /**
     * 从牌库顶抓一张牌。
     *
     * 【规则依据】
     * - Rule 401.5: 从牌库顶抽牌
     *
     * @return 抓到的牌，牌库为空时返回 null
     */
    public Card draw() {
        if (cards.isEmpty()) {
            // Rule 104.3c: 无牌可抓 - 获得优先权时输掉游戏
            pendingLossDraws++;
            return null;
        }
        Card card = cards.remove(0);
        card.onZoneChange(this);
        return card;
    }

    /**
     * 抓多张牌。
     *
     * @param n 抓牌数量
     * @return 抓到的牌列表
     */
    public List<Card> draw(int n) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Card card = draw();
            if (card != null) {
                drawn.add(card);
            }
        }
        return drawn;
    }

    /**
     * 查看牌库顶牌。
     *
     * 【规则依据】
     * - Rule 401.5: 如果允许查看顶牌
     *
     * @return 顶牌，不允许查看时返回 null
     */
    public Card lookAtTop() {
        if (cards.isEmpty()) return null;
        return cards.get(0);
    }

    /**
     * 将牌添加到牌库顶。
     *
     * @param card 要添加的牌
     */
    public void addToTop(Card card) {
        cards.add(0, card);
    }

    /**
     * 将牌添加到牌库底。
     *
     * 【规则依据】
     * - Rule 401.7: 当放置位置超出牌库大小时，牌放在底部
     *
     * @param card 要添加的牌
     */
    public void addToBottom(Card card) {
        cards.add(card);
    }

    /**
     * 将牌放在牌库特定位置。
     *
     * 【规则依据】
     * - Rule 401.7: 位置 0 = 顶部，位置 1 = 第二张，以此类推
     * - 如果位置 >= 牌库大小，牌放在底部
     *
     * @param card 要放置的牌
     * @param position 从顶牌开始的位置
     */
    public void putAtPosition(Card card, int position) {
        int actualPosition = Math.min(position, cards.size());
        cards.add(actualPosition, card);
    }

    /**
     * 洗牌库 - 随机化牌张顺序。
     */
    public void shuffle() {
        Collections.shuffle(cards, new Random());
    }

    /**
     * 将特定牌张列表洗入牌库。
     *
     * @param cardsToShuffle 要洗入的牌列表
     */
    public void shuffleIn(List<Card> cardsToShuffle) {
        cards.addAll(cardsToShuffle);
        shuffle();
    }

    /**
     * 检查牌库是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * 获取牌库大小。
     *
     * @return 牌张数量
     */
    public int size() {
        return cards.size();
    }

    /**
     * 检查是否有待处理的失败抓牌。
     *
     * @return 是否有待处理抓牌
     */
    public boolean hasPendingLossDraws() {
        return pendingLossDraws > 0;
    }

    /**
     * 清除待处理的失败抓牌（当游戏失败被处理时）。
     */
    public void clearPendingLossDraws() {
        pendingLossDraws = 0;
    }

    // ========== Zone 接口实现 ==========

    @Override
    public String getName() {
        return owner.getName() + "'s Library";
    }

    @Override
    public boolean isPublic() {
        return false;  // 隐藏区域
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard()) {
            Card card = object.getCard();
            if (!cards.contains(card)) {
                cards.add(card);
            }
        }
    }

    @Override
    public GameObject remove(GameObject object) {
        if (object.isCard()) {
            Card card = object.getCard();
            if (cards.remove(card)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(card);
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        if (object.isCard()) {
            return cards.contains(object.getCard());
        }
        return false;
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        List<GameObject> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(card);
        }
        return result;
    }
}
