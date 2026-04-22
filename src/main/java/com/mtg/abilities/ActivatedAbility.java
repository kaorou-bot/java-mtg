package com.mtg.abilities;

import com.mtg.player.Player;
import com.mtg.model.Card;
import com.mtg.model.ManaCost;
import com.mtg.game.Game;

/**
 * ActivatedAbility - 激活式异能类（Rule 602）。
 *
 * 【功能说明】
 * - 表示需要支付费用才能激活的异能
 * - 格式：[费用]: [效果]
 * - 可以在有优先权时激活
 *
 * 【规则依据】
 * - Rule 602.1: 激活式异能有费用和效果
 * - Rule 602.2: 玩家可以激活其控制的激活式异能
 * - Rule 602.5: 激活式异能进入堆叠，可以被响应
 *
 * 【设计决策】
 * - canActivate() 检查激活条件
 * - activate() 执行激活流程（支付费用、进入堆叠）
 * - execute() 执行异能效果
 */
public abstract class ActivatedAbility {
    protected String name;  // 异能名称
    protected Card sourceCard;  // 源卡牌
    protected Player controller;  // 控制者
    protected ManaCost manaCost;  // 法力费用
    protected Object[] additionalCosts;  // 额外费用（如横置、牺牲等）
    protected boolean isTapped;  // 是否需要横置来源
    protected boolean isSacrificed;  // 是否需要牺牲来源
    protected boolean isActivated;  // 是否已激活

    /**
     * 创建激活式异能。
     *
     * @param name 异能名称
     * @param sourceCard 源卡牌
     * @param controller 控制者
     */
    public ActivatedAbility(String name, Card sourceCard, Player controller) {
        this.name = name;
        this.sourceCard = sourceCard;
        this.controller = controller;
        this.isActivated = false;
    }

    /**
     * 设置异能法力费用。
     *
     * @param cost 法力费用
     */
    public void setManaCost(ManaCost cost) {
        this.manaCost = cost;
    }

    /**
     * 设置异能是否需要横置来源。
     *
     * @param tapped 是否需要横置
     */
    public void requiresTapping(boolean tapped) {
        this.isTapped = tapped;
    }

    /**
     * 设置异能是否需要牺牲来源。
     *
     * @param sacrificed 是否需要牺牲
     */
    public void requiresSacrifice(boolean sacrificed) {
        this.isSacrificed = sacrificed;
    }

    /**
     * 检查异能是否可以激活。
     *
     * 【检查条件】
     * - 来源在战场上
     * - 来源未横置（如果需要横置）
     * - 法力足够支付费用
     * - 可以支付额外费用
     *
     * @param game 游戏
     * @return 是否可以激活
     */
    public boolean canActivate(Game game) {
        // 检查来源是否在战场上
        if (sourceCard.getCurrentZone() == null ||
            !sourceCard.getCurrentZone().getName().equals("Battlefield")) {
            return false;
        }

        // 检查来源是否已横置（如果需要横置）
        if (isTapped && sourceCard instanceof com.mtg.model.PermanentCard) {
            if (((com.mtg.model.PermanentCard) sourceCard).isTapped()) {
                return false;
            }
        }

        // 检查法力费用
        if (manaCost != null && !controller.canPayCost(manaCost)) {
            return false;
        }

        // 检查额外费用
        if (!canPayAdditionalCosts(game)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否可以支付额外费用。
     *
     * 【子类重写】
     * - 如需要牺牲的异能检查是否有可牺牲的永久物
     *
     * @param game 游戏
     * @return 是否可以支付
     */
    protected boolean canPayAdditionalCosts(Game game) {
        return true;
    }

    /**
     * 激活异能 - 支付费用并放入堆叠。
     *
     * 【执行步骤】
     * 1. 检查是否可以激活
     * 2. 支付法力费用
     * 3. 支付额外费用
     * 4. 横置来源（如果需要）
     * 5. 放入堆叠
     *
     * @param game 游戏
     * @return 是否激活成功
     */
    public boolean activate(Game game) {
        if (!canActivate(game)) {
            return false;
        }

        // 支付法力费用
        if (manaCost != null) {
            controller.payMana(manaCost);
        }

        // 支付额外费用
        payAdditionalCosts(game);

        // 横置来源（如果需要）
        if (isTapped && sourceCard instanceof com.mtg.model.PermanentCard) {
            ((com.mtg.model.PermanentCard) sourceCard).tap();
        }

        // 放入堆叠
        game.getZoneManager().getStack().pushAbility(this);
        isActivated = true;

        return true;
    }

    /**
     * 支付额外费用（子类重写）。
     *
     * @param game 游戏
     */
    protected void payAdditionalCosts(Game game) {
        // 子类重写以处理牺牲或其他额外费用
    }

    /**
     * 执行异能效果。
     *
     * @param game 游戏
     */
    public abstract void execute(Game game);

    /**
     * 获取异能描述。
     *
     * @return 异能描述
     */
    public abstract String getDescription();

    // ========== Getter 方法 ==========

    public String getName() {
        return name;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public Player getController() {
        return controller;
    }

    public ManaCost getManaCost() {
        return manaCost;
    }

    public boolean isTappedRequired() {
        return isTapped;
    }

    @Override
    public String toString() {
        return name + " (" + getDescription() + ")";
    }
}