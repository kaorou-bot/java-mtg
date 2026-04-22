package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand - 手牌，表示玩家持有的手牌。
 *
 * 【功能说明】
 * - 存储玩家当前持有的手牌
 * - 实现 Zone 接口
 *
 * 【规则依据】
 * - Rule 402.1: 手牌是玩家持有已抽取卡牌的地方
 * - Rule 402.2: 每个玩家有手牌上限，通常为7张
 *   玩家可以有任意数量手牌，但在清理步骤必须弃牌到手牌上限
 * - Rule 402.3: 玩家可以任意方式整理手牌并随时查看，其他玩家不能查看
 */
public class Hand implements Zone {
    private final Player owner;  // 拥有者
    private final List<Card> cards;  // 手牌列表
    private final int maxSize;  // 手牌上限

    /**
     * 使用默认手牌上限创建手牌。
     *
     * @param owner 拥有者
     */
    public Hand(Player owner) {
        this(owner, 7);
    }

    /**
     * 创建手牌。
     *
     * @param owner 拥有者
     * @param maxSize 手牌上限
     */
    public Hand(Player owner, int maxSize) {
        this.owner = owner;
        this.cards = new ArrayList<>();
        this.maxSize = maxSize;
    }

    /**
     * 添加牌到手牌。
     *
     * 【规则依据】
     * - Rule 402.1/402.2: 玩家可以有任意数量手牌
     *   但必须在清理步骤弃牌到手牌上限（Rule 514.1）
     *
     * @param card 要添加的牌
     */
    public void add(Card card) {
        if (!cards.contains(card)) {
            cards.add(card);
        }
    }

    /**
     * 弃牌到手牌上限。
     *
     * 【规则依据】
     * - Rule 402.2: 弃多余手牌
     *
     * @return 被弃置的牌列表
     */
    public List<Card> discardDownToMax() {
        List<Card> discarded = new ArrayList<>();
        while (cards.size() > maxSize) {
            discarded.add(cards.remove(cards.size() - 1));
        }
        return discarded;
    }

    /**
     * 检查手牌是否已满。
     *
     * @return 是否已满
     */
    public boolean isFull() {
        return cards.size() >= maxSize;
    }

    /**
     * 获取手牌上限。
     *
     * @return 手牌上限
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 获取手牌副本。
     *
     * 【规则依据】
     * - Rule 402.3: 玩家可以随时查看手牌
     *
     * @return 手牌列表副本
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    // ========== Zone 接口实现 ==========

    @Override
    public String getName() {
        return owner.getName() + "'s Hand";
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
            add(object.getCard());
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
    public int size() {
        return cards.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }
}
