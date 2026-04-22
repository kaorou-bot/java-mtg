package com.mtg.zones;

import com.mtg.model.GameObject;
import com.mtg.player.Player;
import com.mtg.abilities.TriggeredAbility;
import com.mtg.abilities.ActivatedAbility;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack - 堆叠，表示咒语和异能等待结算的区域。
 *
 * 【功能说明】
 * - 存储等待结算的咒语和异能
 * - LIFO（后进先出）顺序结算
 * - 实现 Zone 接口
 *
 * 【规则依据】
 * - Rule 405.1: 咒语被施放时放入堆叠，异能激活或触发时也进入堆叠
 * - Rule 405.2: 堆叠记录咒语和异能被添加的顺序，新对象放在顶部
 * - Rule 405.3: 同时放入多个对象时，主动玩家控制的先放，然后按 APNAP 顺序
 * - Rule 405.4: 咒语具有卡牌的特征，异能具有异能文本
 * - Rule 405.5: 所有玩家连续 pass 后，堆叠顶端的咒语或异能结算，堆叠为空时当前步骤/阶段结束
 *
 * 【结算顺序】
 * - LIFO 顺序（最后放入的最先结算）
 * - 每个玩家都有机会响应堆叠顶的项目
 */
public class Stack implements Zone {
    private final List<StackItem> items;  // 堆叠上的项目列表

    public Stack() {
        this.items = new ArrayList<>();
    }

    /**
     * 将咒语推入堆叠。
     *
     * 【规则依据】
     * - Rule 405.2: 新项目放在顶部
     *
     * @param spell 要推入的咒语
     */
    public void push(SpellItem spell) {
        items.add(spell);
    }

    /**
     * 将激活式异能推入堆叠。
     *
     * @param ability 要推入的异能
     */
    public void push(AbilityItem ability) {
        items.add(ability);
    }

    /**
     * 将触发式异能推入堆叠。
     *
     * @param triggered 要推入的触发异能
     */
    public void push(TriggeredItem triggered) {
        items.add(triggered);
    }

    /**
     * 将触发式异能对象推入堆叠。
     *
     * @param ability TriggeredAbility 对象
     */
    public void pushAbility(TriggeredAbility ability) {
        String name = ability.getName();
        Player controller = ability.getController();
        Stack.AbilityItem item = new Stack.AbilityItem(name, controller, () -> ability.execute(null));
        items.add(item);
    }

    /**
     * 将激活式异能对象推入堆叠。
     *
     * @param ability ActivatedAbility 对象
     */
    public void pushAbility(ActivatedAbility ability) {
        String name = ability.getName();
        Player controller = ability.getController();
        Stack.AbilityItem item = new Stack.AbilityItem(name, controller, () -> ability.execute(null));
        items.add(item);
    }

    /**
     * 查看堆叠顶部的项目（不移除）。
     *
     * @return 顶部项目，无则返回 null
     */
    public StackItem peek() {
        if (items.isEmpty()) return null;
        return items.get(items.size() - 1);
    }

    /**
     * 结算并移除堆叠顶部的项目。
     *
     * 【规则依据】
     * - Rule 405.5: LIFO 顺序
     *
     * @return 结算的项目，无则返回 null
     */
    public StackItem resolve() {
        if (items.isEmpty()) return null;
        return items.remove(items.size() - 1);
    }

    /**
     * 检查堆叠是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 获取堆叠上所有项目的副本。
     *
     * @return 项目列表
     */
    public List<StackItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * 反击（移除）指定索引的项目。
     *
     * @param index 项目索引
     * @return 被移除的项目
     */
    public StackItem counter(int index) {
        if (index >= 0 && index < items.size()) {
            return items.remove(index);
        }
        return null;
    }

    /**
     * 检查是否所有玩家都已 pass。
     *
     * 【简化实现】
     * - 目前直接返回 false，由 Game 类处理 pass 逻辑
     *
     * @param players 玩家列表
     * @return 是否所有人都 pass
     */
    public boolean allPlayersPassed(List<Player> players) {
        return false;
    }

    // ========== Zone 接口实现 ==========

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
        return null;  // 共享区域
    }

    @Override
    public void add(GameObject object, Player controller) {
        // 堆叠项目通过 push 方法添加
    }

    @Override
    public GameObject remove(GameObject object) {
        // 堆叠项目通过 resolve/counter 方法移除
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
        // 堆叠项目通过源 ID 标识
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
     * StackItem - 堆叠项目基类。
     *
     * 【项目类型】
     * - SpellItem: 咒语
     * - AbilityItem: 激活式异能
     * - TriggeredItem: 触发式异能
     */
    public static abstract class StackItem {
        protected Player controller;  // 控制者
        protected String sourceId;    // 源卡牌 ID

        /**
         * 获取控制者。
         *
         * @return 控制者
         */
        public Player getController() {
            return controller;
        }

        /**
         * 获取源 ID。
         *
         * @return 源卡牌 ID
         */
        public String getSourceId() {
            return sourceId;
        }

        /**
         * 获取项目名称。
         *
         * @return 名称
         */
        public abstract String getName();

        /**
         * 结算此项目。
         */
        public abstract void resolve();
    }

    /**
     * SpellItem - 堆叠上的咒语。
     *
     * 【规则依据】
     * - Rule 405.4: 咒语具有卡牌的特征
     */
    public static class SpellItem extends StackItem {
        private final com.mtg.model.Card card;  // 咒语卡牌

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
            // 咒语结算由 Game 处理
        }
    }

    /**
     * AbilityItem - 堆叠上的激活式异能。
     *
     * 【规则依据】
     * - Rule 405.4: 异能具有异能文本
     * - Rule 602.5: 激活式异能进入堆叠，可以响应
     */
    public static class AbilityItem extends StackItem {
        private final String abilityText;  // 异能文本
        private final Runnable effect;     // 效果执行器

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
     * TriggeredItem - 堆叠上的触发式异能。
     *
     * 【规则依据】
     * - Rule 603.3: 触发式异能进入堆叠
     */
    public static class TriggeredItem extends StackItem {
        private final String triggerText;  // 触发文本
        private final Runnable effect;     // 效果执行器

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
