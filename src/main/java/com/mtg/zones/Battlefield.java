package com.mtg.zones;

import com.mtg.model.*;
import com.mtg.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Battlefield - 战场，表示所有永久物存在的共享游戏区域。
 *
 * 【功能说明】
 * - 存储所有战场上的永久物
 * - 提供按类型、控制者查询永久物的方法
 * - 实现 Zone 接口
 *
 * 【规则依据】
 * - Rule 403.1: 战场是玩家之间的共享区域
 * - Rule 403.2: 咒语或异能只影响战场，除非特别提及玩家或其他区域
 * - Rule 403.3: 永久物只存在于战场上
 * - Rule 403.4: 永久物进入战场时成为新对象
 *
 * 【永久物类型】
 * - Artifact（神器）、Battle（战斗）、Creature（生物）、Enchantment（结界）、Land（地）、Planeswalker（鹏洛客）
 *
 * 【陷阱警告】
 * - Battlefield.contains() 使用 isCard() 检查而非 isPermanent()
 * - 因为添加到战场时对象的 currentZone 可能为 null
 */
public class Battlefield implements Zone {
    private final List<PermanentCard> permanents;  // 战场上的永久物列表

    /**
     * 创建战场。
     */
    public Battlefield() {
        this.permanents = new ArrayList<>();
    }

    /**
     * 添加永久物到战场。
     *
     * @param permanent 要添加的永久物
     */
    public void add(PermanentCard permanent) {
        if (!permanents.contains(permanent)) {
            permanents.add(permanent);
        }
    }

    /**
     * 从战场移除永久物。
     *
     * @param permanent 要移除的永久物
     * @return 是否成功移除
     */
    public boolean remove(PermanentCard permanent) {
        return permanents.remove(permanent);
    }

    /**
     * 获取所有永久物。
     *
     * @return 永久物列表副本
     */
    public List<PermanentCard> getPermanents() {
        return new ArrayList<>(permanents);
    }

    /**
     * 获取所有生物。
     *
     * @return 生物列表
     */
    public List<CreatureCard> getCreatures() {
        List<CreatureCard> creatures = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof CreatureCard) {
                creatures.add((CreatureCard) p);
            }
        }
        return creatures;
    }

    /**
     * 获取所有地牌。
     *
     * @return 地牌列表
     */
    public List<LandCard> getLands() {
        List<LandCard> lands = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof LandCard) {
                lands.add((LandCard) p);
            }
        }
        return lands;
    }

    /**
     * 获取所有结界。
     *
     * @return 结界列表
     */
    public List<EnchantmentCard> getEnchantments() {
        List<EnchantmentCard> enchantments = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof EnchantmentCard) {
                enchantments.add((EnchantmentCard) p);
            }
        }
        return enchantments;
    }

    /**
     * 获取所有神器。
     *
     * @return 神器列表
     */
    public List<ArtifactCard> getArtifacts() {
        List<ArtifactCard> artifacts = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof ArtifactCard) {
                artifacts.add((ArtifactCard) p);
            }
        }
        return artifacts;
    }

    /**
     * 获取所有鹏洛客。
     *
     * @return 鹏洛客列表
     */
    public List<PlaneswalkerCard> getPlaneswalkers() {
        List<PlaneswalkerCard> planeswalkers = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof PlaneswalkerCard) {
                planeswalkers.add((PlaneswalkerCard) p);
            }
        }
        return planeswalkers;
    }

    /**
     * 获取所有战斗。
     *
     * @return 战斗列表
     */
    public List<BattleCard> getBattles() {
        List<BattleCard> battles = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p instanceof BattleCard) {
                battles.add((BattleCard) p);
            }
        }
        return battles;
    }

    /**
     * 获取由特定玩家控制的所有永久物。
     *
     * @param player 玩家
     * @return 永久物列表
     */
    public List<PermanentCard> getControlledBy(Player player) {
        List<PermanentCard> controlled = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (player.equals(p.getController())) {
                controlled.add(p);
            }
        }
        return controlled;
    }

    /**
     * 获取所有未横置的永久物。
     *
     * @return 未横置永久物列表
     */
    public List<PermanentCard> getUntapped() {
        List<PermanentCard> untapped = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (!p.isTapped()) {
                untapped.add(p);
            }
        }
        return untapped;
    }

    /**
     * 获取可以攻击的未横置生物。
     *
     * 【攻击条件】
     * - 未横置
     * - 属于主动玩家
     * - 无召唤 sickness 或有敏捷异能
     *
     * 【规则依据】
     * - Rule 508.1a: 生物必须未横置才能攻击
     *
     * @param player 玩家
     * @return 可攻击生物列表
     */
    public List<CreatureCard> getAttackingCreatures(Player player) {
        List<CreatureCard> attacking = new ArrayList<>();
        for (CreatureCard c : getCreatures()) {
            if (!c.isTapped() && player.equals(c.getController()) &&
                (!c.hasSummoningSickness() || hasHaste(c))) {
                attacking.add(c);
            }
        }
        return attacking;
    }

    private boolean hasHaste(CreatureCard creature) {
        // TODO: 检查敏捷异能
        return false;
    }

    /**
     * 获取可以阻挡的未横置生物。
     *
     * 【规则依据】
     * - Rule 509.1a: 生物必须未横置才能阻挡
     *
     * @param player 玩家
     * @return 可阻挡生物列表
     */
    public List<CreatureCard> getBlockingCreatures(Player player) {
        List<CreatureCard> blocking = new ArrayList<>();
        for (CreatureCard c : getCreatures()) {
            if (!c.isTapped() && player.equals(c.getController())) {
                blocking.add(c);
            }
        }
        return blocking;
    }

    /**
     * 获取指定类型的所有永久物。
     *
     * @param type 牌张类型
     * @return 永久物列表
     */
    public List<PermanentCard> getByType(CardType type) {
        List<PermanentCard> result = new ArrayList<>();
        for (PermanentCard p : permanents) {
            if (p.getType() == type) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * 检查战场是否为空。
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return permanents.isEmpty();
    }

    // ========== Zone 接口实现 ==========

    @Override
    public String getName() {
        return "Battlefield";
    }

    @Override
    public boolean isPublic() {
        return true;  // 战场是公开区域
    }

    @Override
    public Player getOwner() {
        return null;  // 共享区域，无单一拥有者
    }

    @Override
    public void add(GameObject object, Player controller) {
        if (object.isCard() && object.getCard() instanceof PermanentCard) {
            PermanentCard permanent = (PermanentCard) object.getCard();
            permanent.setController(controller);
            add(permanent);
        }
    }

    @Override
    public GameObject remove(GameObject object) {
        if (object.isPermanent() && object.getCard() instanceof PermanentCard) {
            PermanentCard permanent = (PermanentCard) object.getCard();
            if (permanents.remove(permanent)) {
                return permanent;
            }
        }
        return null;
    }

    @Override
    public List<GameObject> getContents() {
        List<GameObject> result = new ArrayList<>();
        for (PermanentCard p : permanents) {
            result.add(p);
        }
        return result;
    }

    @Override
    public boolean contains(GameObject object) {
        // 使用 isCard() 而非 isPermanent()，因为加入时 currentZone 可能为 null
        if (object.isCard() && object.getCard() instanceof PermanentCard) {
            return permanents.contains(object.getCard());
        }
        return false;
    }

    @Override
    public int size() {
        return permanents.size();
    }

    @Override
    public List<GameObject> getContentsSnapshot() {
        return getContents();
    }
}
