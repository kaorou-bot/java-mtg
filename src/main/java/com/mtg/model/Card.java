package com.mtg.model;

import com.mtg.player.Player;
import com.mtg.zones.Zone;

import java.util.List;
import java.util.UUID;

/**
 * Card - 卡牌基类，实现了 GameObject 接口。
 *
 * 【功能说明】
 * - 存储卡牌的基本属性（名称、费用、类型、颜色）
 * - 追踪卡牌当前位置（currentZone）
 * - 实现 GameObject 接口，统一处理游戏对象
 *
 * 【重要概念】
 * - Card.isPermanent(): 检查卡牌是否在战场上
 * - Card.isSpell(): 检查卡牌是否在堆叠中
 * - 这些方法依赖 currentZone，所以区域转换时必须调用 onZoneChange()
 *
 * 【规则依据】
 * - Rule 200: 卡牌属性
 * - Rule 202: 法力费用
 * - Rule 203: 颜色
 * - Rule 300-314: 牌张类型
 * - Rule 400.7: 区域转换后成为新对象
 *
 * 【设计决策】
 * - 使用 UUID 标识卡牌实例
 * - owner 和 controller 可以不同（控制效应）
 * - currentZone 用于判断卡牌当前状态
 *
 * 【陷阱警告】
 * - 直接添加到战场时，currentZone 可能为 null
 * - 使用 isCard() 而非 isPermanent() 检查战场包含
 */
public abstract class Card implements GameObject {
    protected String name;           // 卡牌名称
    protected ManaCost manaCost;     // 法力费用
    protected String description;     // 规则描述文本
    protected CardType type;        // 牌张类型
    protected List<ManaType> color; // 颜色
    protected String id;             // 唯一标识符
    protected Player owner;          // 拥有者（不会改变）
    protected Player controller;      // 控制者（可能改变）
    protected Zone currentZone;      // 当前所在区域

    /**
     * 创建卡牌。
     *
     * @param name 名称
     * @param manaCost 法力费用
     * @param description 规则描述
     * @param type 牌张类型
     * @param color 颜色列表
     */
    public Card(String name, ManaCost manaCost, String description, CardType type, List<ManaType> color) {
        this.name = name;
        this.manaCost = manaCost;
        this.description = description;
        this.type = type;
        this.color = color;
        this.id = UUID.randomUUID().toString();
    }

    // ========== GameObject 实现 ==========

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Player getController() {
        return controller;
    }

    @Override
    public void setController(Player controller) {
        this.controller = controller;
    }

    @Override
    public boolean isCard() {
        return true;
    }

    /**
     * 检查是否是永久物。
     *
     * 【判断依据】
     * - currentZone 不为 null
     * - currentZone 名称为 "Battlefield"
     *
     * 【陷阱】
     * - 直接添加到战场时 currentZone 可能为 null
     * - Battlefield.add() 之后需要调用 onZoneChange()
     *
     * @return 是否在战场上
     */
    @Override
    public boolean isPermanent() {
        return currentZone != null && currentZone.getName().equals("Battlefield");
    }

    /**
     * 检查是否是咒语（在堆叠中）。
     *
     * @return 是否在堆叠中
     */
    @Override
    public boolean isSpell() {
        return currentZone != null && currentZone.getName().equals("Stack");
    }

    @Override
    public Card getCard() {
        return this;
    }

    /**
     * 区域转换时调用，更新 currentZone。
     *
     * 【规则依据】
     * - Rule 400.7: 对象从一个区域进入另一个区域后，成为一个新对象
     * - 新的 Card 实例没有之前区域的信息
     *
     * 【调用时机】
     * - ZoneManager 区域转换时
     * - 必须调用此方法更新 currentZone
     *
     * @param newZone 新的区域
     */
    @Override
    public void onZoneChange(Zone newZone) {
        this.currentZone = newZone;
    }

    // ========== 卡牌属性 ==========

    public String getName() {
        return name;
    }

    public ManaCost getManaCost() {
        return manaCost;
    }

    public String getDescription() {
        return description;
    }

    public CardType getType() {
        return type;
    }

    public List<ManaType> getColor() {
        return color;
    }

    /**
     * 获取颜色指示符描述。
     *
     * @return 颜色描述字符串
     */
    public String getColorIndicator() {
        if (color == null || color.isEmpty()) {
            return "Colorless";
        }
        StringBuilder sb = new StringBuilder();
        for (ManaType c : color) {
            sb.append(c.getName());
        }
        return sb.toString();
    }

    /**
     * 获取当前所在区域。
     *
     * @return 当前区域，可能为 null
     */
    public Zone getCurrentZone() {
        return currentZone;
    }

    /**
     * 设置拥有者。
     *
     * 【规则依据】
     * - Rule 108.3: 牌的拥有者是游戏开始时确定的，不会改变
     *
     * @param owner 拥有者
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", name, manaCost.toDisplayString(), type.getName());
    }
}
