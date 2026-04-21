# MTG Battle - 完整开发计划

基于 2026年2月27日 万智牌综合规则文档

---

## 项目概述

**目标**: 实现一个符合万智牌官方规则的 1v1 对战游戏

**当前状态**: 基础框架已完成，包含部分游戏逻辑和 Swing GUI

**需要实现**: 完整的万智牌综合规则

---

## 第一阶段: 核心框架重构 (预计工作量: 大)

### 1.1 区域系统 (Zones - Section 400)

| 区域 | 类 | 职责 |
|------|-----|------|
| Library | `Library.java` | 牌库，顶部抽牌，空时判负 |
| Hand | `Hand.java` | 手牌，最大7张，弃牌规则 |
| Battlefield | `Battlefield.java` | 战场，所有永久物 |
| Graveyard | `Graveyard.java` | 坟场，弃牌和消灭的牌 |
| Stack | `Stack.java` | 堆栈，咒语和异能等待结算 |
| Exile | `Exile.java` | 放逐区 |

**实现要点**:
- 100.1 区域是游戏中所有对象可能存在的地点
- 401.5 抽牌只能从牌库顶部
- 402.1 手牌对对手不可见
- 404.3 坟场顺序可能影响游戏

### 1.2 优先权系统 (Priority - Section 117)

```java
public class PrioritySystem {
    Player activePlayer;
    Player currentPlayerWithPriority;

    void passPriority();
    boolean canCastSpell(Card card);
    void checkStateBasedActions();
    void checkTriggeredAbilities();
}
```

**规则要点**:
- 117.1 拥有优先权的玩家可以施放咒语或激活异能
- 117.3 大多数步骤/阶段开始时主动玩家获得优先权
- 117.4 所有玩家连续 pass，咒语结算或阶段结束
- 117.5 状态检查在优先权之前

### 1.3 堆叠系统 (Stack - Section 405)

```java
public class Stack {
    List<StackItem> stack;  // 咒语和异能

    void push(Spell spell);
    void push(Ability ability);
    StackItem resolve();  // LIFO 结算
    void counter(int index);
}
```

**规则要点**:
- 405.1 堆叠是所有等待结算的咒语和异能的放置区
- 405.4 先进后出 (LIFO) 结算
- 405.5 响应咒语在目标咒语之上

---

## 第二阶段: 回合结构 (Turn Structure - Section 500)

### 2.1 回合阶段管理器

```
回合结构:
┌─────────────────────────────────────────────┐
│ 1. 开始阶段 (Beginning Phase - 501)        │
│    ├─ 重置步骤 (Untap Step - 502)         │
│    ├─ 维持步骤 (Upkeep Step - 503)         │
│    └─ 抓牌步骤 (Draw Step - 504)           │
├─────────────────────────────────────────────┤
│ 2. 战斗前主要阶段 (Precombat Main - 505)   │
├─────────────────────────────────────────────┤
│ 3. 战斗阶段 (Combat Phase - 506)           │
│    ├─ 战斗开始 (Beginning of Combat - 507) │
│    ├─ 宣告攻击者 (Declare Attackers - 508) │
│    ├─ 宣告阻挡者 (Declare Blockers - 509)  │
│    ├─ 战斗伤害 (Combat Damage - 510)       │
│    │   └─ 先攻伤害 (First Strike Damage)   │
│    └─ 战斗结束 (End of Combat - 511)       │
├─────────────────────────────────────────────┤
│ 4. 战斗后主要阶段 (Postcombat Main - 505)  │
├─────────────────────────────────────────────┤
│ 5. 结束阶段 (Ending Phase - 512)          │
│    ├─ 结束步骤 (End Step - 513)            │
│    └─ 清理步骤 (Cleanup Step - 514)         │
└─────────────────────────────────────────────┘
```

### 2.2 状态检查时机

```java
public class TurnManager {
    // 504.1 抓牌步骤: 主动玩家抓一张牌
    // 514.1 清理步骤: 弃到手牌上限，受伤害生物重置
}
```

---

## 第三阶段: 咒语和异能 (Spells & Abilities - Section 600)

### 3.1 施放咒语流程 (Casting - Section 601)

```java
public class CastingProcess {
    // 601.2 施放咒语步骤:
    // a) 宣布施放
    // b) 选择模式、替代目标、X值
    // c) 宣布额外费用
    // d) 检查是否可合法施放
    // e) 计算总费用
    // f) 应用费用调整
    // g) 产生法术力并支付费用
    // h) 咒语进入堆叠
}
```

### 3.2 咒语结算 (Resolving - Section 608)

```java
public class ResolutionProcess {
    // 608.2 结算步骤:
    // a) 检查目标是否仍然合法
    // b) 按顺序执行说明
    // c) 检查非法动作，忽略
}
```

### 3.3 异能类型

| 类型 | Section | 说明 |
|------|----------|------|
| 激活异能 | 602 | 格式: 费用: 效果 |
| 触发异能 | 603 | 格式: 当/每当/在 [触发事件] |
| 静止异能 | 604 | 始终有效 |
| 法力异能 | 605 | 不使用堆叠 |
| 忠诚异能 | 606 | 鹏洛客能力 |

---

## 第四阶段: 战斗系统 (Combat - Section 508-510)

### 4.1 宣告攻击者 (Declare Attackers - 508)

```java
public class CombatSystem {
    void declareAttackers(List<CreatureCard> attackers,
                         Map<CreatureCard, Player> targets) {
        // 508.1a 只能宣告未横置的生物
        // 508.1b 宣告攻击目标 (牌手/鹏洛客/战役)
        // 508.1c 检查限制 (can't attack)
        // 508.1d 检查要求 (must attack if able)
        // 508.1f 横置攻击生物
        // 508.1g 支付攻击费用
        // 508.1h 攻击生物进入战斗
    }
}
```

### 4.2 宣告阻挡者 (Declare Blockers - 509)

```java
    void declareBlockers(Map<CreatureCard, List<CreatureCard>> blocking) {
        // 509.1a 选择阻挡生物
        // 509.1b 检查限制 (evasion abilities)
        // 509.1c 阻挡费用支付
        // 509.1g 阻挡生物进入战斗
        // 509.4 攻击生物标记为被阻挡/未阻挡
    }
```

### 4.3 战斗伤害 (Combat Damage - 510)

```java
    void resolveCombatDamage() {
        // 510.1 伤害分配 (同时)
        // - 攻击玩家分配己方伤害
        // - 防御玩家分配阻挡伤害
        // - 必须分配到每个阻挡者致命伤害

        // 510.2 伤害造成 (同时)

        // 510.4 先攻创建额外伤害步骤
    }
```

### 4.4 战斗相关异能

| 异能 | 规则 | 实现 |
|------|------|------|
| Flying | 702.9 | 阻挡限制: 只能被飞行/陆空阻挡 |
| First Strike | 702.7 | 先攻伤害步骤 |
| Double Strike | 702.8 | 先攻 + 正常伤害 |
| Trample | 702.19 | 超出伤害给牌手 |
| Deathtouch | 702.2 | 1点伤害消灭生物 |
| Vigilance | 702.20 | 攻击不横置 |
| Menace | 702.111 | 无法被1个生物阻挡 |

---

## 第五阶段: 法力系统 (Mana - Section 106)

### 5.1 法力规则

```java
public class ManaSystem {
    // 106.4 法力池在每个步骤/阶段结束时清空
    // 106.6 限制性法力 (只能用于特定费用)
    // 106.12 {T} 产生法力的异能需要优先权

    void produceMana(Player player) {
        // 从所有未横置的产费来源产生法力
        player.getManaPool().addAll(availableMana);
    }

    void emptyManaPool(Player player) {
        // 阶段结束时清空
        player.getManaPool().clear();
    }
}
```

### 5.2 费用支付 (Costs - 118)

```java
public class CostPayment {
    // 费用类型:
    // 1. 法力费用
    // 2. 额外费用 (kicker, 等)
    // 3. 替代费用 (flashback, 等)
    // 4. 生命费用
    // 5. 横置费用 {T}
    // 6. 献祭费用
    // 7. 弃牌费用
}
```

---

## 第六阶段: 状态检查 (State-Based Actions - 704)

### 6.1 状态动作列表

```java
public class StateBasedActions {
    static void checkAndProcess(Player player, GameState state) {
        // 704.5a 生命 <= 0 → 输掉游戏
        // 704.5b 从空牌库抓牌 → 输掉游戏
        // 704.5c 10+中毒指示物 → 输掉游戏
        // 704.5d Token 不在场 → 消失
        // 704.5e Copy 不在堆叠 → 消失
        // 704.5f 生物防御力 <= 0 → 进入坟场
        // 704.5g 生物受到 >= 防御力伤害 → 消灭
        // 704.5h 致命打击异能 → 消灭
        // 704.5i 鹏洛客忠诚 = 0 → 进入坟场
        // 704.5j Legend 规则 → 保留一个，其余进坟
        // 704.5v 战役防御 = 0 → 进入坟场

        // 704.5 重复检查直到无动作应用
    }
}
```

### 6.2 触发异能检查

```java
    // 704.3 在所有状态动作处理后检查触发异能
    // 603.2 触发异能自动触发
    // 603.3 在下一个拥有优先权时放入堆叠
```

---

## 第七阶段: 永久物系统 (Permanents - Section 110)

### 7.1 永久物类型

| 类型 | 规则 | 关键属性 |
|------|------|----------|
| 地 (305) | 战斗阶段外可使用，一回合一张 | 产费异能 |
| 生物 (302) | 可攻击/阻挡，有攻击防御 | 召唤 sickness |
| 法术 (301) | 无特殊规则 | 可成为武具/载具 |
| 结界 (303) | 可为灵气附属于永久物 | 附属于目标 |
| 鹏洛客 (306) | 有忠诚指示物，有 loyalty异能 | 忠诚异能限制 |
| 战役 (310) | 有防御指示物，受牌手保护 | 可被攻击 |

### 7.2 灵气系统 (Auras - 303.4)

```java
public class AuraSystem {
    // 303.4 灵气以"结附于 [对象]"表示
    // 303.5 灵气进场时附属于合法目标
    // 303.6 如果目标非法，灵气进入坟场
}
```

### 7.3 武具系统 (Equipment - 301.5)

```java
public class EquipmentSystem {
    // 301.5 武具是神器的一种副类别
    // 301.7 装备异能: 费用 to attach to creature
}
```

---

## 第八阶段: 关键词异能 (Keyword Abilities - 702)

### 8.1 战斗异能

| 异能 | 规则号 | 效果 |
|------|--------|------|
| Deathtouch | 702.2 | 1伤害消灭生物 |
| Defender | 702.3 | 不能攻击 |
| Double Strike | 702.8 | 先攻+正常 |
| First Strike | 702.7 | 先攻伤害 |
| Flying | 702.9 | 只能被飞行阻挡 |
| Haste | 702.10 | 可立即攻击 |
| Hexproof | 702.11 | 对手不能指定 |
| Indestructible | 702.12 | 不能被消灭 |
| Lifelink | 702.15 | 伤害回血 |
| Protection | 702.16 | 不能被指定/伤害/阻挡 |
| Reach | 702.17 | 可阻挡飞行 |
| Trample | 702.19 | 超出给牌手 |
| Vigilance | 702.20 | 攻击不横置 |
| Menace | 702.111 | 无法被1生物阻挡 |

### 8.2 异能关键词

| 异能 | 规则号 |
|------|--------|
| Flash | 702.8 |
| Kicker | 702.33 |
| Cycling | 702.29 |
| Equip | 702.6 |
| Enchant | 702.5 |
| Landhome | - |
| Mutate | 702.140 |
| Prowess | 702.108 |

---

## 第九阶段: 效应系统 (Effects - 609)

### 9.1 效应类型

| 类型 | 说明 |
|------|------|
| 一次性效应 (610) | 执行一次后完成 |
| 持续性效应 (611) | 持续有效 |
| 替换效应 (614) | 修改事件 |
| 防止效应 (615) | 防止伤害 |

### 9.2 层叠系统 (613.1)

```
应用顺序:
1. 复制效应
2. 控制权改变效应
3. 文字改变效应
4. 类型改变效应
5. 颜色改变效应
6. 添加/移除异能
7. 力量/防御力设定
8. 力量/防御力修正
9. 指示物
10. 时间戳顺序
```

---

## 第十阶段: 游戏开始和结束 (Starting & Ending)

### 10.1 游戏开始 (103)

```java
public void startGame() {
    // 103.1 决定第一个牌手
    // 103.2 每位牌手洗牌库
    // 103.3 牌库洗入命令区
    // 103.4 决定谁先手
    // 103.5 每位牌手抓7张牌
    // 103.6 Mulligan 规则 (可选实现)
}
```

### 10.2 游戏结束 (104)

```java
public void checkWinCondition() {
    // 104.1 获胜条件:
    // a) 生命 <= 0
    // b) 抓牌时牌库为空
    // c) 10+ 中毒指示物
    // d) 输掉游戏 (concede)
}
```

---

## 实现优先级

### P0 - 必须实现 (MVP)
1. 回合阶段系统
2. 优先权系统
3. 抽牌/弃牌
4. 法力使用
5. 战斗宣告和结算
6. 状态检查
7. 胜负判定

### P1 - 重要功能
1. 堆叠响应系统
2. 触发异能
3. 激活异能
4. 先攻/双重打击
5. 飞行/阻挡规则
6. 灵气附属于移除

### P2 - 完整功能
1. 所有关键词异能
2. 指示物系统 (+1/+1, -1/-1, 忠诚)
3. 复制咒语
4. 替换效应
5. 防止效应
6. 层叠系统

### P3 - 高级功能
1. 多人游戏
2. 备牌系统
3. 指挥台规则
4. 全部关键词异能

---

## 文件结构更新

```
src/main/java/com/mtg/
├── MtgBattle.java
├── zones/
│   ├── Zone.java
│   ├── Library.java
│   ├── Hand.java
│   ├── Battlefield.java
│   ├── Graveyard.java
│   ├── Stack.java
│   └── Exile.java
├── game/
│   ├── Game.java
│   ├── GamePhase.java
│   ├── TurnManager.java
│   ├── PrioritySystem.java
│   ├── StateBasedActions.java
│   ├── CombatManager.java
│   ├── CastingManager.java
│   └── ResolutionManager.java
├── model/ (已有)
├── player/ (已有)
├── card/ (已有)
├── ability/
│   ├── Ability.java
│   ├── ActivatedAbility.java
│   ├── TriggeredAbility.java
│   ├── StaticAbility.java
│   ├── ManaAbility.java
│   └── LoyaltyAbility.java
├── effect/
│   ├── Effect.java
│   ├── OneShotEffect.java
│   ├── ContinuousEffect.java
│   ├── ReplacementEffect.java
│   └── PreventionEffect.java
└── ui/ (已有)
```

---

## 测试计划

### 单元测试
- 法力支付测试
- 战斗结算测试
- 状态检查测试
- 异能触发测试

### 集成测试
- 完整回合流程
- 咒语施放和结算
- 战斗完整流程

### 手动测试
- GUI 交互测试
- 游戏完整流程测试
