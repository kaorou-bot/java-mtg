# MTG Battle - Phase 1 & 2 开发文档

## 概述

MTG Battle 是一个基于 Magic: The Gathering 2026年2月27日综合规则的1v1对战游戏。本文档记录 Phase 1 和 Phase 2 的实现内容。

**Phase 1 目标**: 实现核心框架 — 区域系统、优先权系统、回合结构、状态基准动作。  
**Phase 2 目标**: 实现异能系统、效果系统、卡牌效果解析。

---

## 项目架构

```
src/main/java/com/mtg/
├── model/          # 游戏模型（Card, CreatureCard, LandCard 等）
├── zones/          # 区域系统（Library, Hand, Battlefield, Stack 等）
├── gamecore/       # 核心机制（PrioritySystem, StateBasedActions）
├── game/           # 游戏逻辑（Game, TurnManager, TurnPhase）
├── player/         # 玩家（Player, Deck）
├── card/           # 卡牌库（CardLibrary）
├── abilities/      # 异能系统（TriggeredAbility, ActivatedAbility）
├── effects/        # 效果系统（ReplacementEffect, PreventionEffect）
├── resolution/     # 卡牌效果解析（CardEffectResolver）
└── ui/             # 图形界面（GameFrame, HandPanel, BattlefieldPanel）
```

---

## Phase 1: 核心框架 ✓

## 区域系统 (Section 400)

### Zone 接口

所有区域实现统一的 `Zone` 接口：

```java
public interface Zone {
    String getName();
    boolean isPublic();
    Player getOwner();
    void add(GameObject object, Player controller);
    GameObject remove(GameObject object);
    List<GameObject> getContents();
    boolean contains(GameObject object);
    int size();
    List<GameObject> getContentsSnapshot();
}
```

### 区域列表

| 区域 | 规则 | 可见性 | 说明 |
|------|------|--------|------|
| `Library` | 401 | 私有 | 牌库，顶部抽牌 |
| `Hand` | 402 | 私有 | 手牌，标准上限7张 |
| `Battlefield` | 403 | 公开 | 战场，所有永久物所在 |
| `Graveyard` | 404 | 公开 | 坟场，弃牌/消灭的卡 |
| `Stack` | 405 | 公开 | 堆叠，咒语/异能等待解决 |
| `Exile` | 406 | 公开 | 放逐区，含元数据（原因、是否背面） |

### ZoneManager

`ZoneManager` 是所有区域转换的中央管理器（Rule 400.6）：

- `drawCard(Player)` — 抽牌
- `putOnBattlefield(Card, Player)` — 放置永久物
- `destroy(PermanentCard)` — 消灭（移至坟场）
- `castSpell(Card, Player)` — 施放咒语（移至堆叠）
- `shuffleLibrary(Player)` — 洗牌

**重要**: 区域转换时，`Card.onZoneChange(Zone)` 被调用以更新 `currentZone` 引用（Rule 400.7）。

---

## 优先权系统 (Section 117)

### PrioritySystem

管理优先权的获取和传递：

```java
public class PrioritySystem {
    void grantPriorityToActive();      // 回合开始时
    void onSpellCast();                // 出咒语后重置传递计数
    void pass();                       // 玩家传递优先权
    boolean allPlayersPassed();        // 两人连续传递 → 解决堆叠
    boolean playerHasPriority(Player); // 检查是否有优先权
}
```

**规则**:
- Rule 117.3a: 主动玩家在大多数阶段开始时获得优先权
- Rule 117.3b: 咒语/异能解决后，主动玩家获得优先权
- Rule 117.3c: 出咒语或激活异能后，该玩家获得优先权
- Rule 117.3d: 所有玩家连续传递 → 解决堆叠顶部或进入下一阶段
- Rule 117.4: 堆叠为空且所有人传递 → 当前步骤/阶段结束

---

## 回合结构 (Sections 500-514)

### TurnPhase 枚举

完整的回合阶段枚举（14个值）：

```
Beginning Phase
  UNTAAP     — 横置阶段（Rule 502，无优先权）
  UPKEEP     — 维持阶段（Rule 503）
  DRAW       — 抽牌阶段（Rule 504）

Main Phases
  MAIN1      — 战斗前主阶段
  MAIN2      — 战斗后主阶段

Combat Phase
  COMBAT_START      — 战斗开始（Rule 507）
  DECLARE_ATTACKERS — 宣告攻击（Rule 508）
  DECLARE_BLOCKERS  — 宣告阻挡（Rule 509）
  COMBAT_DAMAGE    — 战斗伤害（Rule 510）
  COMBAT_DAMAGE_FIRST — 先攻伤害
  COMBAT_END       — 战斗结束（Rule 511）

Ending Phase
  END     — 结束阶段（Rule 513）
  CLEANUP — 清理阶段（Rule 514）
```

### TurnManager

回合管理器，负责：
- 阶段推进 (`advancePhase()`)
- 宣告攻击/阻挡 (`declareAttacker`, `declareBlocker`)
- 战斗伤害解决（支持先攻）

---

## 状态基准动作 (Section 704)

### StateBasedActions

自动检查并执行的状态基准动作：

| 规则 | 检查内容 |
|------|---------|
| 704.5a | 生命 ≤ 0 |
| 704.5c | 中毒 ≥ 10 |
| 704.5f | 生物防御力 ≤ 0 → 消灭 |
| 704.5g | 生物上有致命伤害 → 消灭 |
| 704.5i | 鹏洛客忠诚 = 0 → 消灭 |
| 704.5j | 传奇规则（同名牌） |
| 704.5m | Aura 附属于非法目标 → 移至坟场 |
| 704.5s | Battle 防御力 = 0 |

---

## 测试覆盖

### 测试文件

```
src/test/java/com/mtg/
├── zones/
│   ├── LibraryTest.java       — 抽牌、洗牌、Rule 401
│   ├── HandTest.java          — 手牌大小、弃牌、Rule 402
│   ├── GraveyardTest.java     — 坟场操作
│   ├── ExileTest.java         — 放逐区（含 ExileEntry）
│   ├── BattlefieldTest.java   — 永久物管理、Rule 403
│   ├── StackTest.java        — LIFO 堆叠、Rule 405
│   └── ZoneManagerTest.java  — 区域转换集成测试
├── gamecore/
│   ├── PrioritySystemTest.java    — 优先权传递
│   └── StateBasedActionsTest.java — SBA 检查
├── game/
│   ├── TurnPhaseTest.java     — 阶段枚举
│   └── TurnManagerTest.java  — 回合结构
├── abilities/
│   └── TriggeredAbilityTest.java — 触发异能系统
└── resolution/
    └── CardEffectResolverTest.java — 卡牌效果解析
```

### 运行测试

```bash
# 使用 Maven（需要 Maven 已安装）
mvn test

# 或手动运行（下载 JUnit Platform Console）
java -jar junit-platform-console-standalone.jar \
  --class-path "target/classes:target/test-classes" \
  --scan-classpath
```

**当前状态**: 153 个测试全部通过

---

## 主要代码变更（Phase 1 + 2）

### Phase 1 新增文件
- `zones/Zone.java` — 区域接口
- `zones/Library.java` — 牌库（Rule 401）
- `zones/Hand.java` — 手牌（Rule 402）
- `zones/Graveyard.java` — 坟场（Rule 404）
- `zones/Exile.java` — 放逐区（Rule 406）
- `zones/Battlefield.java` — 战场（Rule 403）
- `zones/Stack.java` — 堆叠（Rule 405）
- `zones/ZoneManager.java` — 区域转换管理器
- `gamecore/PrioritySystem.java` — 优先权系统（Rule 117）
- `gamecore/StateBasedActions.java` — 状态基准动作（Rule 704）
- `game/TurnPhase.java` — 回合阶段枚举

### Phase 2 新增文件
- `abilities/TriggeredAbility.java` — 触发异能基类（Rule 603）
- `abilities/TriggeredAbilityManager.java` — 触发异能管理器
- `abilities/ActivatedAbility.java` — 激活异能基类（Rule 602）
- `effects/ReplacementEffect.java` — 替代效果（Rule 614）
- `effects/PreventionEffect.java` — 预防效果（Rule 615）
- `effects/GameEvent.java` — 游戏事件模型
- `effects/EffectManager.java` — 效果管理器
- `resolution/CardEffectResolver.java` — 卡牌效果解析器

### 修改文件
- `game/Game.java` — 重构使用 ZoneManager、PrioritySystem，新增 GameListener
- `game/TurnManager.java` — 完全重写，支持所有阶段，增加 setBattlefield()
- `model/Card.java` — 实现 GameObject 接口，添加 Zone 跟踪
- `model/CreatureCard.java` — 添加伤害标记、先攻、敏捷等能力，新增 setter 方法
- `model/EnchantmentCard.java` — 添加 Aura 附魔支持
- `model/PermanentCard.java` — 添加传奇属性
- `player/Player.java` — 添加 getOpponent()、addPermanent()、mana 管理
- `zones/Stack.java` — 增强，支持异能入堆叠
- `gamecore/StateBasedActions.java` — 改为实例方法，正确通过 ZoneManager 销毁永久物

---

## Phase 2: 异能与效果系统 ✓

### 异能系统

#### TriggeredAbility (Rule 603)

触发式异能基类：

```java
public abstract class TriggeredAbility {
    protected String name;
    protected Card sourceCard;
    protected Player controller;
    
    boolean checkTrigger(Game game);        // 检查触发条件
    void execute(Game game);                 // 执行效果
    String getDescription();                 // 描述
}
```

**常见触发类型**:
- `OnPhaseBeginAbility` — 阶段开始触发（如"在你的维持阶段开始时"）
- `OnZoneChangeAbility` — 区域改变触发（如"当此生物进战场时"）
- `OnDamageDealtAbility` — 造成伤害时触发

**TriggeredAbilityManager** 管理所有触发异能：
- 注册/注销异能
- 检查触发条件
- 按 APNAP 顺序放入堆叠（Rule 603.6）

#### ActivatedAbility (Rule 602)

激活式异能基类：

```java
public abstract class ActivatedAbility {
    protected ManaCost manaCost;
    protected boolean isTapped;      // 需要横置源
    protected boolean isSacrificed;  // 需要牺牲源
    
    boolean canActivate(Game game);  // 检查是否可激活
    boolean activate(Game game);     // 支付费用并激活
    void execute(Game game);         // 执行效果
}
```

**激活费用支持**:
- 法力费用
- 横置费用
- 牺牲费用
- 其他自定义费用

### 效果系统

#### ReplacementEffect (Rule 614)

替代效果，修改游戏事件：

```java
public abstract class ReplacementEffect {
    boolean applies(GameEvent event, Game game);
    GameEvent apply(GameEvent event, Game game);
}
```

**示例**: "如果一个来源将对您造成伤害，防止其中的1点伤害。"

#### PreventionEffect (Rule 615)

预防效果，阻止伤害或事件：

```java
public abstract class PreventionEffect {
    boolean applies(GameEvent event, Game game);
    GameEvent apply(GameEvent event, Game game);
}
```

#### GameEvent

游戏事件模型：

```java
public class GameEvent {
    String eventType;  // "damage", "draw", "enterBattlefield" 等
    Object source;     // 事件来源
    Player player;     // 受影响的玩家
    Object target;     // 目标对象
    int amount;        // 数量（伤害、抽牌等）
}
```

#### EffectManager

管理所有活跃效果：

- `addReplacementEffect()`
- `addPreventionEffect()`
- `addContinuousEffect()`
- `processEvent()` — 按顺序应用所有效果

### 卡牌效果解析

#### CardEffectResolver

统一的卡牌效果解析器，支持：

**伤害效果**:
- `dealDamageToAny()` — 对任意目标造成伤害
- `dealDamageTo()` — 对特定目标造成伤害

**生物效果**:
- `buffTargetCreature()` — 增强生物（+X/+X）
- `destroyPermanent()` — 消灭永久物
- `exilePermanent()` — 放逐永久物

**玩家效果**:
- `gainLifeAndDraw()` — 获得生命并抽牌
- `produceBlackMana()` — 产生法力

**关键词能力**:
- `addKeywordUntilEndOfTurn()` — 添加关键词（飞行、先攻等）

**Stack 增强**:
- `pushAbility(TriggeredAbility)` — 触发异能入堆叠
- `pushAbility(ActivatedAbility)` — 激活异能入堆叠

---

## Phase 3: 待开发

- 回合阶段 UI 完整集成
- 游戏日志完善
- 更多卡牌效果实现
- AI 对手
- 网络对战

---

## 参考规则

- Magic: The Gathering Comprehensive Rules (February 27, 2026)
- Section 400: Zones
- Section 401: Library
- Section 402: Hand
- Section 403: Battlefield
- Section 404: Graveyard
- Section 405: Stack
- Section 406: Exile
- Section 117: Timing and Priority
- Section 500: Starting the Turn
- Sections 502-514: Turn Structure
- Section 603: Triggered Abilities
- Section 602: Activated Abilities
- Section 614: Replacement Effects
- Section 615: Prevention Effects
