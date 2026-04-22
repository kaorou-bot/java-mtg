package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Graveyard - 坟场，表示玩家的弃牌堆。
 *
 * 【功能说明】
 * - 存储被反击、弃牌、消灭或牺牲的牌
 * - 即时和法术咒语结算完毕后也放在这里
 * - 实现 Zone 接口
 *
 * 【规则依据】
 * - Rule 404.1: 坟场是玩家的弃牌堆。被反击、弃牌、消灭或牺牲的牌，
 *   以及结算完毕的即时和法术咒语都放在拥有者坟场顶部
 * - Rule 404.2: 每个坟场保持为单一正面朝上的牌堆。任意玩家可随时查看任意坟场中的牌
 * - Rule 404.3: 如果效果同时将两张或更多牌放入同一坟场，拥有者可以任意排列顺序
 */
public class Graveyard implements Zone {
    private final Player owner;  // 拥有者
    private final List<Card> cards;  // 牌张列表（正面朝上）

    /**
     * 创建坟场。
     *
     * @param owner 拥有者
     */
    public Graveyard(Player owner) {
        this.owner = owner;
        this.cards = new ArrayList<>();
    }

    /**
     * 添加牌到坟场顶部。
     *
     * 【规则依据】
     * - Rule 404.1: 牌放在拥有者坟场顶部
     *
     * @param card 要添加的牌
     */
    public void add(Card card) {
        cards.add(card);
    }

    /**
     * 添加多张牌（Rule 404.3: 拥有者可以排列顺序）。
     *
     * @param newCards 要添加的牌列表
     */
    public void addAll(List<Card> newCards) {
        cards.addAll(newCards);
    }

    /**
     * 从坟场移除特定牌。
     *
     * @param card 要移除的牌
     * @return 是否成功移除
     */
    public boolean remove(Card card) {
        return cards.remove(card);
    }

    /**
     * 获取坟场顶牌。
     *
     * @return 顶牌，坟场为空时返回 null
     */
    public Card getTop() {
        if (cards.isEmpty()) return null;
        return cards.get(cards.size() - 1);
    }

    /**
     * 获取坟场所有牌。
     *
     * @return 牌列表副本
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * 检查坟场是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    // ========== Zone 接口实现 ==========

    @Override
    public String getName() {
        return owner.getName() + "'s Graveyard";
    }

    @Override
    public boolean isPublic() {
        return true;  // 公开区域
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
