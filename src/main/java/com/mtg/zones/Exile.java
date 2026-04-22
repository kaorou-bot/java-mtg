package com.mtg.zones;

import com.mtg.model.Card;
import com.mtg.model.GameObject;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Exile - 放逐区，用于存储被放逐对象的暂存区域。
 *
 * 【功能说明】
 * - 存储被放逐的卡牌
 * - 某些咒语和异能永久放逐，有些则暂时放逐
 * - 实现 Zone 接口
 *
 * 【规则依据】
 * - Rule 406.1: 放逐区本质上是对象的暂存区域
 * - Rule 406.2: 放逐对象就是将其放入放逐区
 * - Rule 406.3: 默认情况下放逐的牌正面朝上保管，任意玩家可随时查看
 *   正面朝下放逐的牌只能在规则允许时查看
 * - Rule 406.8: 之前称为"移除游戏区域"
 */
public class Exile implements Zone {
    private final List<ExileEntry> entries;  // 放逐记录列表

    /**
     * 创建放逐区。
     */
    public Exile() {
        this.entries = new ArrayList<>();
    }

    /**
     * 放逐一张牌。
     *
     * @param card 要放逐的牌
     */
    public void exile(Card card) {
        exile(card, null, false);
    }

    /**
     * 带元数据放逐一张牌。
     *
     * @param card 要放逐的牌
     * @param reason 放逐原因（如 "exiled by Shadowborn Demon"）
     * @param faceDown 是否正面朝下放逐
     */
    public void exile(Card card, String reason, boolean faceDown) {
        ExileEntry entry = new ExileEntry(card, reason, faceDown);
        entries.add(entry);
    }

    /**
     * 获取所有放逐记录。
     *
     * @return 放逐记录列表
     */
    public List<ExileEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * 获取所有正面朝上的放逐牌。
     *
     * 【规则依据】
     * - Rule 406.3: 正面朝上的放逐牌任意玩家可查看
     *
     * @return 牌列表
     */
    public List<Card> getFaceUpCards() {
        List<Card> faceUp = new ArrayList<>();
        for (ExileEntry entry : entries) {
            if (!entry.faceDown) {
                faceUp.add(entry.card);
            }
        }
        return faceUp;
    }

    /**
     * 从放逐区移除牌。
     *
     * @param card 要移除的牌
     * @return 是否成功移除
     */
    public boolean remove(Card card) {
        return entries.removeIf(entry -> entry.card.equals(card));
    }

    /**
     * 获取牌的放逐记录。
     *
     * @param card 牌
     * @return 放逐记录，无则返回 null
     */
    public ExileEntry getEntry(Card card) {
        for (ExileEntry entry : entries) {
            if (entry.card.equals(card)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * 检查放逐区是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * ExileEntry - 放逐记录，表示放逐区中的一个条目。
     *
     * 【用途】
     * - 记录被放逐的牌
     * - 记录放逐原因
     * - 记录是否正面朝下
     */
    public static class ExileEntry {
        public final Card card;  // 被放逐的牌
        public final String exileReason;  // 放逐原因
        public final boolean faceDown;  // 是否正面朝下
        public final String id;  // 唯一标识符

        public ExileEntry(Card card, String exileReason, boolean faceDown) {
            this.card = card;
            this.exileReason = exileReason;
            this.faceDown = faceDown;
            this.id = UUID.randomUUID().toString();
        }

        public boolean isFaceDown() {
            return faceDown;
        }

        public String getExileReason() {
            return exileReason;
        }

        public Card getCard() {
            return card;
        }
    }

    // ========== Zone 接口实现 ==========

    @Override
    public String getName() {
        return "Exile";
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public Player getOwner() {
        return null;  // 共享区域
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard()) {
            exile(object.getCard());
        }
    }

    @Override
    public GameObject remove(GameObject object) {
        if (object.isCard()) {
            Card card = object.getCard();
            if (remove(card)) {
                return card;
            }
        }
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (ExileEntry entry : entries) {
            if (!entry.faceDown) {
                result.add(entry.card);
            }
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        if (object.isCard()) {
            for (ExileEntry entry : entries) {
                if (entry.card.equals(object.getCard())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }
}
