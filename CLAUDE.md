# MTG Battle - Claude 项目助手配置

## 项目概述

MTG Battle 是一个基于 Magic: The Gathering 2026年2月27日综合规则的1v1 Java Swing 对战游戏。

**当前分支**: `java-mtg` (main: `main`)
**Commit**: `Phase 2 完成` — 异能系统、效果系统、卡牌效果解析

**运行方式**:
```bash
# 项目根目录
find src/main/java -name '*.java' > src-main-sources.txt
javac -d out -sourcepath src/main/java @src-main-sources.txt
java -cp out com.mtg.MtgBattle
```

---

## VSCode 配置

### 推荐扩展 (.vscode/extensions.json)

安装后 VSCode 会提示安装：
- **Language Support for Java™ by Red Hat** — 代码补全、跳转、重构
- **Test Runner for Java** — 直接在 VSCode 运行 JUnit 测试
- **Debugger for Java** — 断点调试
- **Maven for Java** — Maven 面板（如果安装了 Maven）
- **GitLens** — 增强 Git 功能

### 快捷操作

| 操作 | 快捷键 |
|------|--------|
| 运行 MTG Battle | `F5`（调试模式会自动编译） |
| 运行全部测试 | `Ctrl+Shift+P` → "Java: Run Tests" |
| 运行单个测试 | 在测试方法上右键 → "Run Test" |
| 重新编译主代码 | `Ctrl+Shift+P` → "Tasks: Run Task" → compile-main |
| 编译并运行测试 | `Ctrl+Shift+P` → "Tasks: Run Task" → run-tests |

### Task 说明 (.vscode/tasks.json)

- **compile-main** — 编译主代码到 `out/`
- **compile-tests** — 编译测试代码到 `test-classes/`（需先运行 download-junit）
- **run-tests** — 运行全部 153 个测试
- **download-junit** — 下载 JUnit Platform Console（一次性）
- **build-all** — 清理并重新编译

### 注意
- 本机无 Maven 时使用 `tasks.json` 中的 javac 命令
- `download-junit` 任务会下载 JUnit Platform Console 到 `~/junit-platform-console-standalone.jar`
- 首次运行测试前先执行 `download-junit`

**测试运行**（需手动下载 JUnit Platform Console）:
```bash
# 1. 下载 JUnit Platform Console
curl -L -o /tmp/junit-platform-console-standalone.jar \
  "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.3/junit-platform-console-standalone-1.9.3.jar"

# 2. 编译
find src/main/java -name "*.java" > /tmp/sources.txt
javac -d /tmp/mtg-classes @/tmp/sources.txt

# 3. 编译测试
JUNIT=~/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.9.3/junit-jupiter-api-5.9.3.jar
APIG=~/.m2/repository/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar
OPENTEST=~/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar
for f in $(find src/test/java -name "*.java"); do
  javac -d /tmp/mtg-test-classes -cp "$JUNIT:$APIG:$OPENTEST" \
    -sourcepath "src/main/java;src/test/java" "$f"
done

# 4. 运行测试
java -jar /tmp/junit-platform-console-standalone.jar \
  --class-path "/tmp/mtg-test-classes:/tmp/mtg-classes" --scan-classpath

# 当前状态: 153 tests, 全部通过
```

---

## 用户偏好

- **语言**: 中文为主，代码注释/文档用中文
- **响应风格**: 简洁、直接，不要过度解释
- **测试要求**: 每次新增功能必须写单元测试
- **提交风格**: 小步提交，每个逻辑变更单独提交，不要超长 commit message
- **代码注释**: 所有代码必须添加详细中文注释，包括：
  - 类/接口顶部：功能说明、规则依据、关键设计决策
  - 方法：参数说明、返回值说明、规则引用
  - 复杂逻辑：分步说明、边界条件说明

---

## 项目架构

```
src/main/java/com/mtg/
├── model/          # Card, CreatureCard, LandCard, PermanentCard, GameObject...
├── zones/          # Zone, Library, Hand, Battlefield, Graveyard, Exile, Stack, ZoneManager
├── gamecore/       # PrioritySystem, StateBasedActions
├── game/           # Game, TurnManager, TurnPhase, (废弃: GamePhase, CombatManager)
├── player/         # Player, Deck
├── card/           # CardLibrary
├── abilities/      # TriggeredAbility, ActivatedAbility, TriggeredAbilityManager
├── effects/        # ReplacementEffect, PreventionEffect, GameEvent, EffectManager
├── resolution/     # CardEffectResolver
└── ui/             # GameFrame, HandPanel, BattlefieldPanel, CardPanel, PlayerPanel
```

---

## Phase 1 已完成 ✓

**目标**: 核心框架 — 区域系统、优先权、回合结构、状态基准动作

所有区域实现 `Zone` 接口：

```java
public interface Zone {
    String getName();
    boolean isPublic();        // true: 公开区域 | false: 私有区域
    Player getOwner();
    void add(GameObject object, Player controller);
    GameObject remove(GameObject object);
    List<GameObject> getContents();
    boolean contains(GameObject object);
    int size();
    List<GameObject> getContentsSnapshot();
}
```

| 类 | 规则 | isPublic | 关键方法 |
|----|------|---------|---------|
| `Library` | 401 | false | draw(), shuffle(), addToTop/Bottom, putAtPosition |
| `Hand` | 402 | false | add(), discardDownToMax(), getMaxSize() |
| `Battlefield` | 403 | true | add(permanent), getCreatures(), getLands(), getControlledBy() |
| `Graveyard` | 404 | true | add(), getTop(), remove() |
| `Exile` | 406 | true | exile(card, reason, faceDown), ExileEntry, getFaceUpCards() |
| `Stack` | 405 | true | push(SpellItem), resolve() [LIFO], peek(), counter(index) |

**ZoneManager** 是所有区域转换的中央处理器（Rule 400.6）：
```java
Card drawCard(Player);              // Library → Hand
void putOnBattlefield(Card, Player); // Hand → Battlefield (自动untap)
void destroy(PermanentCard);        // Battlefield → Graveyard
void castSpell(Card, Player);       // Hand → Stack
void exile(PermanentCard);          // Battlefield → Exile
```

**注意**: 每次区域转换必须调用 `Card.onZoneChange(newZone)` 更新 `currentZone`（Rule 400.7）。
`Card.isPermanent()` 和 `Card.isSpell()` 依赖 `currentZone` 判断。

### 优先权系统 (Rule 117)

```java
PrioritySystem ps = new PrioritySystem();
ps.setPlayers(active, nonActive);
ps.grantPriorityToActive();     // 阶段开始
ps.pass();                     // AP→NAP 或 NAP→passCount++
ps.onSpellCast();              // 出咒语后重置 passCount
ps.allPlayersPassed();          // passCount >= 2 → 解决堆叠
```

### 回合结构 (Rule 500-514)

14 个阶段，按顺序：
```
UNTAAP → UPKEEP → DRAW → MAIN1 →
COMBAT_START → DECLARE_ATTACKERS → DECLARE_BLOCKERS →
COMBAT_DAMAGE → [COMBAT_DAMAGE_FIRST] → COMBAT_END →
MAIN2 → END → CLEANUP → (switchTurn → UNTAAP)
```

`TurnManager` 管理阶段推进和战斗宣告。**重要**: `switchTurn()` 会增加 `turnNumber` 并清空战斗状态。

### 状态基准动作 (Rule 704)

`StateBasedActions sba = new StateBasedActions(zoneManager); sba.check(p1, p2, battlefield)` 检查：
- 生命 ≤ 0 / 中毒 ≥ 10
- 生物防御力 ≤ 0 / 致命伤害
- 鹏洛客忠诚 = 0 / 传奇规则 / Aura 非法目标 / Battle 防御力 = 0

---

## Phase 2 已完成 ✓

**目标**: 异能系统、效果系统、卡牌效果解析

### 异能系统 (Rule 602-603)

| 类 | 规则 | 说明 |
|----|------|------|
| `TriggeredAbility` | 603 | 触发异能基类 |
| `TriggeredAbilityManager` | 603 | 触发异能管理器（注册、检查、APNAP排序） |
| `ActivatedAbility` | 602 | 激活异能基类（支持法力/横置/牺牲费用） |

### 效果系统 (Rule 614-615)

| 类 | 规则 | 说明 |
|----|------|------|
| `ReplacementEffect` | 614 | 替代效果 |
| `PreventionEffect` | 615 | 预防效果 |
| `GameEvent` | - | 游戏事件模型 |
| `EffectManager` | - | 效果管理器 |

### 卡牌效果解析

`CardEffectResolver` 统一处理咒语效果：
- `dealDamageTo()` — 造成伤害
- `buffTargetCreature()` — 增强生物
- `destroyPermanent()` — 消灭永久物
- `gainLifeAndDraw()` — 获得生命/抽牌
- `addKeywordUntilEndOfTurn()` — 添加关键词能力

---

## Phase 3 待开发

- 回合阶段 UI 完整集成
- 游戏日志系统
- 战斗系统增强（关键词异能）
- 法力系统完善
- 目标选择系统
- 更多卡牌效果

---

## 关键实现笔记

### Card.isPermanent() 陷阱
```java
// Card.isPermanent() 检查 currentZone.name == "Battlefield"
// 在测试中直接 bf.add(creature) 时 creature.currentZone == null
// 所以 Battlefield.contains() 应该检查 object.isCard() 而不是 object.isPermanent()
```

### Player.equals()
```java
// Player 重写了 equals() 和 hashCode()，按 name 比较
// ZoneManager.getPlayerIndex() 依赖此方法
```

### Hand.add() 不强制限制
```java
// Hand.add() 允许超过 maxSize（MTG 规则 402.2）
// 超过部分在清理阶段通过 discardDownToMax() 处理
```

### TurnManager 测试注意
```java
// TurnManager 可以用 setBattlefield(Battlefield) 独立测试（不依赖 Game）
// startTurn() 会调用 beginUntapStep()（清空横置状态）
// advancePhase() 从 UNTAAP 开始
```

---

## 参考规则文档

- 源文件: `MagicCompRules 20260227.docx` (项目根目录)
- 提取文本: `rules_extracted.txt`
- 规则分段: `phase1_rules.txt`, `phase1_full.txt`

关键规则章节:
- **400**: Zones
- **401**: Library
- **402**: Hand
- **403**: Battlefield
- **404**: Graveyard
- **405**: Stack
- **406**: Exile
- **117**: Timing and Priority
- **500**: Starting the Turn
- **502-514**: Turn Structure
- **603**: Triggered Abilities
- **602**: Activated Abilities
- **614**: Replacement Effects
- **704**: State-Based Actions
- **801**: Limited Range of Influence (1v1 忽略)

---

## 历史修复记录

### Phase 1 修复的 Bug
- `Battlefield.contains()` — 用 `isCard()` 而非 `isPermanent()` (卡加入时 `currentZone` 未设置)
- `ZoneManager.putOnBattlefield/destroy/castSpell` — 直接操作而不用 `moveObject`（`moveObject` 依赖 `from` 正确）
- `Player.equals()` — 按 name 比较（ZoneManager.getPlayerIndex 依赖）
- `TurnManager.switchTurn()` — 增加 turnNumber 并清空战斗状态
- `Hand.add()` — 不强制 maxSize（MTG 规则正确行为）
- `TurnManager.endCombat()` — 移除 `game.getPrioritySystem()` 调用（允许 null game）
- `Library.duplicate size()` — 删除重复的 `size()` 方法
- `Graveyard.isPublic()` — 返回 `true`（Rule 404.2: 任意玩家可查看）

### Phase 2 修复的 Bug
- `StateBasedActions` — 改为实例方法，添加 ZoneManager 引用，正确销毁永久物
- `CreatureCard` — 添加 keyword ability 的 setter 方法（setHasFlying 等）
- `Stack` — 添加 `pushAbility(TriggeredAbility/ActivatedAbility)` 支持异能入堆叠
