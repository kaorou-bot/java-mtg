package com.mtg.game;

import com.mtg.player.Player;
import com.mtg.player.Deck;
import com.mtg.model.*;
import com.mtg.zones.*;
import com.mtg.gamecore.PrioritySystem;
import com.mtg.gamecore.StateBasedActions;
import com.mtg.card.CardLibrary;
import com.mtg.resolution.CardEffectResolver;

/**
 * Game - 游戏核心类，管理万智牌游戏的完整状态和流程。
 *
 * 【功能说明】
 * - 管理两个玩家的游戏状态
 * - 管理回合流程（TurnManager）
 * - 管理优先权（PrioritySystem）
 * - 管理区域转换（ZoneManager）
 * - 处理咒语施放和结算
 * - 处理战斗宣告
 * - 检查状态基准动作
 *
 * 【规则依据】
 * - Rule 100: 游戏概述
 * - Rule 103: 游戏开始
 * - Rule 104: 游戏结束
 * - Rule 117: 优先权与时机
 * - Rule 500-514: 回合结构
 *
 * 【设计决策】
 * - 使用 ZoneManager 统一管理所有区域转换
 * - 使用 PrioritySystem 管理优先权轮转
 * - 咒语结算委托给 CardEffectResolver
 * - 回合推进委托给 TurnManager
 */
public class Game {
    // ========== 玩家相关 ==========
    private Player player1;          // 玩家1
    private Player player2;          // 玩家2
    private Player currentPlayer;     // 当前回合的玩家（主动玩家）
    private Player startingPlayer;    // 起始玩家（先手）

    // ========== 游戏状态 ==========
    private TurnManager turnManager;      // 回合管理器
    private ZoneManager zoneManager;      // 区域管理器
    private PrioritySystem prioritySystem; // 优先权系统
    private TurnPhase currentPhase;       // 当前阶段
    private int turnNumber;              // 回合数（第几回合）
    private boolean gameOver;             // 游戏是否结束
    private Player winner;                // 获胜玩家
    private GameLog gameLog;              // 游戏日志

    // ========== 构造函数 ==========

    /**
     * 创建新游戏实例。
     *
     * @param player1Name 玩家1名称
     * @param player2Name 玩家2名称
     */
    public Game(String player1Name, String player2Name) {
        this.player1 = new Player(player1Name);
        this.player2 = new Player(player2Name);
        this.turnManager = new TurnManager(this);
        this.prioritySystem = new PrioritySystem();
        this.gameLog = new GameLog();
        this.gameOver = false;
        this.winner = null;
        this.turnNumber = 0;
    }

    // ========== 游戏初始化 ==========

    /**
     * 开始游戏。
     *
     * 【执行步骤】
     * 1. 设置玩家对手关系
     * 2. 创建牌组并洗牌
     * 3. 初始化区域管理器
     * 4. 抽初始手牌（各7张）
     * 5. 随机决定先手玩家
     * 6. 初始化回合管理器
     *
     * 【规则依据】
     * - Rule 103.1: 决定第一个玩家
     * - Rule 103.4: 决定谁先手（随机）
     * - Rule 103.5: 抽7张初始手牌
     */
    public void startGame() {
        // 1. 设置对手关系（Rule 102.2）
        player1.setOpponent(player2);
        player2.setOpponent(player1);

        // 2. 创建牌组
        Deck deck1 = CardLibrary.createStarterDeck(player1.getName());
        Deck deck2 = CardLibrary.createStarterDeck(player2.getName());

        // 3. 初始化区域管理器
        zoneManager = new ZoneManager(this, player1, player2);

        // 4. 将牌放入牌库（设置拥有者和位置）
        for (Card card : deck1.getCards()) {
            card.setOwner(player1);
            zoneManager.getLibrary(player1).add(card, player1);
        }
        for (Card card : deck2.getCards()) {
            card.setOwner(player2);
            zoneManager.getLibrary(player2).add(card, player2);
        }

        // 5. 洗牌（Rule 103.2）
        zoneManager.shuffleLibrary(player1);
        zoneManager.shuffleLibrary(player2);

        // 6. 抽初始手牌（Rule 103.5: 7张）
        for (int i = 0; i < 7; i++) {
            drawCard(player1);
            drawCard(player2);
        }

        // 7. 随机决定先手（Rule 103.4）
        startingPlayer = Math.random() < 0.5 ? player1 : player2;
        currentPlayer = startingPlayer;

        // 8. 设置手牌的控制者
        for (Card card : player1.getHand().getCards()) {
            card.setController(player1);
        }
        for (Card card : player2.getHand().getCards()) {
            card.setController(player2);
        }

        // 9. 初始化游戏状态
        gameOver = false;
        winner = null;
        turnNumber = 1;

        // 10. 关联战场引用
        turnManager.setBattlefield(zoneManager.getBattlefield());

        // 11. 开始第一个回合
        turnManager.startTurn(currentPlayer);
        currentPhase = turnManager.getCurrentPhase();
        gameLog.logTurnStart(1, currentPlayer);
        gameLog.logPhase(currentPhase, currentPlayer);
    }

    // ========== 游戏动作 ==========

    /**
     * 玩家抓一张牌。
     *
     * 【流程】
     * 1. 从牌库顶抽牌
     * 2. 设置控制者
     * 3. 加入手牌
     *
     * 【规则依据】
     * - Rule 401.5: 从牌库顶抽牌
     * - Rule 121: 抓牌
     *
     * @param player 抓牌的玩家
     */
    public void drawCard(Player player) {
        Card card = zoneManager.drawCard(player);
        if (card != null) {
            card.setController(player);
            zoneManager.getHand(player).add(card, player);
            gameLog.logDraw(player, card.getName());
        }
    }

    /**
     * 出一张地牌。
     *
     * 【条件检查】
     * - 必须在主阶段
     * - 堆叠必须为空
     * - 必须有优先权
     * - 本回合未出过地
     *
     * 【规则依据】
     * - Rule 305.1: 地是永久物
     * - Rule 305.2: 一回合只能出一张地
     * - Rule 116.2a: 出地是特殊动作，不需要优先权（简化处理）
     *
     * @param player 出地的玩家
     * @param land 要出的地牌
     * @return 是否成功
     */
    public boolean playLand(Player player, LandCard land) {
        // 检查是否可以出地
        if (!canPlayLand(player)) {
            return false;
        }

        // 检查本回合是否已出过地
        if (player.hasPlayedLandThisTurn()) {
            return false;
        }

        // 从手牌移除，加入战场
        Hand hand = zoneManager.getHand(player);
        if (hand.getCards().contains(land)) {
            hand.remove(land);
            zoneManager.putOnBattlefield(land, player);
            player.setLandPlayedThisTurn(true);
            gameLog.logAction(player, "plays a land");
            return true;
        }
        return false;
    }

    /**
     * 施放咒语。
     *
     * 【条件检查】
     * - 玩家必须有优先权
     * - 咒语必须在手牌
     * - 必须支付得起费用
     *
     * 【流程】
     * 1. 从手牌移除
     * 2. 支付法力
     * 3. 加入堆叠
     * 4. 重置优先权计数
     *
     * 【规则依据】
     * - Rule 601.2: 施放咒语的步骤
     * - Rule 601.2h: 咒语进入堆叠
     *
     * @param player 施放者
     * @param card 要施放的牌
     * @return 是否成功
     */
    public boolean castSpell(Player player, Card card) {
        // 检查优先权
        if (!prioritySystem.playerHasPriority(player)) {
            return false;
        }

        // 检查是否在手牌
        Hand hand = zoneManager.getHand(player);
        if (!hand.getCards().contains(card)) {
            return false;
        }

        // 检查法力是否足够
        if (!player.canPayCost(card.getManaCost())) {
            return false;
        }

        // 执行施放：移除手牌、支付费用、进入堆叠
        hand.remove(card);
        player.payMana(card.getManaCost());
        zoneManager.castSpell(card, player);
        prioritySystem.onSpellCast(); // 重置 passCount

        return true;
    }

    /**
     * 放置永久物（生物、结界等）到战场。
     *
     * 【与 castSpell 的区别】
     * - permanent 不经过堆叠，直接进入战场
     * - 必须施放咒语来放置（简化处理：直接调用 putOnBattlefield）
     *
     * 【规则依据】
     * - Rule 601: 施放咒语
     * - Rule 608: 咒语结算
     *
     * @param player 施放者
     * @param card 要放置的牌
     * @return 是否成功
     */
    public boolean playPermanent(Player player, Card card) {
        // 必须是在主阶段
        if (!currentPhase.isMainPhase()) {
            return false;
        }

        // 必须有优先权
        if (!prioritySystem.playerHasPriority(player)) {
            return false;
        }

        // 地牌特殊处理
        if (card.getType() == CardType.LAND) {
            return playLand(player, (LandCard) card);
        }

        // 检查法力
        if (!player.canPayCost(card.getManaCost())) {
            return false;
        }

        // 检查手牌
        Hand hand = zoneManager.getHand(player);
        if (!hand.getCards().contains(card)) {
            return false;
        }

        // 移除手牌、支付费用、放置战场
        hand.remove(card);
        player.payMana(card.getManaCost());
        zoneManager.putOnBattlefield(card, player);
        prioritySystem.onSpellCast();

        return true;
    }

    /**
     * 传递优先权。
     *
     * 【规则依据】
     * - Rule 117.3d: 玩家可以跳过动作传递优先权
     * - Rule 117.4: 所有人连续 pass 后结算堆叠顶或进入下一阶段
     *
     * 【流程】
     * 1. 调用 PrioritySystem.pass()
     * 2. 如果所有人都 pass，结算堆叠
     */
    public void passPriority() {
        prioritySystem.pass();
        if (prioritySystem.allPlayersPassed()) {
            resolveTopOfStack();
        }
    }

    /**
     * 结算堆叠顶部的咒语或异能。
     *
     * 【规则依据】
     * - Rule 405.5: LIFO 顺序结算
     * - Rule 608: 咒语结算
     *
     * 【流程】
     * 1. 如果堆叠非空，取出并结算顶部项目
     * 2. 否则进入下一阶段
     * 3. 结算后重置优先权（主动玩家获得）
     */
    private void resolveTopOfStack() {
        Stack stack = zoneManager.getStack();
        if (!stack.isEmpty()) {
            // 取出并结算顶部项目
            Stack.StackItem item = stack.resolve();
            if (item instanceof Stack.SpellItem) {
                resolveSpell((Stack.SpellItem) item);
            }
            // 结算后主动玩家获得优先权（Rule 117.3b）
            prioritySystem.grantPriorityAfterResolve();
        } else {
            // 堆叠为空，进入下一阶段
            turnManager.advancePhase();
            currentPhase = turnManager.getCurrentPhase();
        }
    }

    /**
     * 结算咒语效果。
     *
     * 【规则依据】
     * - Rule 608.1: 检查目标合法性
     * - Rule 608.2: 执行咒语说明文字
     *
     * 【简化处理】
     * - 委托给 CardEffectResolver 处理各种咒语效果
     * - 即时和法术结算后移至坟场
     *
     * @param spellItem 堆叠上的咒语项目
     */
    private void resolveSpell(Stack.SpellItem spellItem) {
        Card card = spellItem.getCard();
        Player caster = spellItem.getController();

        // 使用 CardEffectResolver 处理各种咒语效果
        CardEffectResolver.resolveSpell(this, card, caster);

        // 即时/法术结算后移至坟场（Rule 404.1）
        if (card.getType() == CardType.INSTANT || card.getType() == CardType.SORCERY) {
            zoneManager.getGraveyard(caster).add(card);
        }
    }

    /**
     * 检查玩家是否可以出地。
     *
     * 【条件】
     * - 必须在主阶段
     * - 堆叠必须为空
     * - 必须有优先权
     * - 本回合未出过地
     *
     * @param player 玩家
     * @return 是否可以出地
     */
    public boolean canPlayLand(Player player) {
        return currentPhase.isMainPhase() &&
               zoneManager.getStack().isEmpty() &&
               prioritySystem.playerHasPriority(player) &&
               !player.hasPlayedLandThisTurn();
    }

    // ========== 回合管理 ==========

    /**
     * 进入下一个阶段/步骤。
     *
     * 【执行内容】
     * 1. 清空法力池（除清理阶段外）
     * 2. 推进到下一阶段
     * 3. 执行阶段特定逻辑
     * 4. 检查状态基准动作
     *
     * 【规则依据】
     * - Rule 500.5: 阶段结束时法力池清空
     * - Rule 500.2: 堆叠空且所有人 pass 时阶段结束
     * - Rule 704.2: 状态动作在获得优先权前检查
     *
     * 【简化处理】
     * - 每个阶段结束时统一清空法力池
     * - 清理阶段不清空（Rule 514.4）
     */
    public void nextPhase() {
        if (gameOver) return;

        // 1. 阶段结束时清空法力池（Rule 500.5, 703.4q）
        if (currentPhase != TurnPhase.CLEANUP) {
            player1.clearMana();
            player2.clearMana();
        }

        // 2. 推进阶段
        turnManager.advancePhase();
        currentPhase = turnManager.getCurrentPhase();
        gameLog.logPhase(currentPhase, currentPlayer);

        // 3. 执行阶段特定逻辑
        switch (currentPhase) {
            case UNTAAP -> turnManager.beginUntapStep();          // 重置步骤
            case UPKEEP -> turnManager.beginUpkeepStep();        // 维持步骤
            case DRAW -> turnManager.beginDrawStep();              // 抽牌步骤
            case COMBAT_START -> turnManager.beginCombat();        // 战斗开始
            case DECLARE_ATTACKERS -> turnManager.beginDeclareAttackers(); // 宣告攻击者
            case COMBAT_DAMAGE -> turnManager.resolveCombatDamage();     // 战斗伤害
            case COMBAT_END -> turnManager.endCombat();                 // 战斗结束
            case END -> turnManager.beginEndStep();                     // 结束步骤
            case CLEANUP -> turnManager.beginCleanupStep();             // 清理步骤
            case GAME_OVER -> gameOver = true;
            default -> {}
        }

        // 4. 检查状态基准动作（Rule 704.2）
        StateBasedActions sba = new StateBasedActions(zoneManager);
        sba.check(player1, player2, zoneManager.getBattlefield());
    }

    /**
     * 结束当前回合，进入对手回合。
     *
     * 【规则依据】
     * - Rule 500.3: 清理步骤结束后回合结束
     * - Rule 500.4: 回合切换
     */
    public void endTurn() {
        while (!gameOver && currentPhase != TurnPhase.END) {
            nextPhase();
        }
        nextPhase();
    }

    // ========== 战斗宣告 ==========

    /**
     * 宣告攻击者。
     *
     * @param creature 攻击的生物
     * @param target 攻击目标（对手玩家或鹏洛客）
     */
    public void declareAttacker(CreatureCard creature, Player target) {
        if (currentPhase == TurnPhase.DECLARE_ATTACKERS) {
            turnManager.declareAttacker(creature, target);
            gameLog.logAttack(creature.getName(), target.getName());
        }
    }

    /**
     * 宣告阻挡者。
     *
     * @param blocker 阻挡的生物
     * @param attacker 要阻挡的攻击生物
     */
    public void declareBlocker(CreatureCard blocker, CreatureCard attacker) {
        if (currentPhase == TurnPhase.DECLARE_BLOCKERS) {
            turnManager.declareBlocker(blocker, attacker);
            gameLog.logBlock(blocker.getName(), attacker.getName());
        }
    }

    // ========== Getter 方法 ==========

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Player getCurrentPlayer() { return currentPlayer; }

    /**
     * 获取对手玩家。
     *
     * @param player 当前玩家
     * @return 对手玩家
     */
    public Player getOpponent(Player player) {
        return player == player1 ? player2 : player1;
    }

    public TurnPhase getCurrentPhase() { return currentPhase; }
    public TurnManager getTurnManager() { return turnManager; }
    public PrioritySystem getPrioritySystem() { return prioritySystem; }
    public ZoneManager getZoneManager() { return zoneManager; }
    public Battlefield getBattlefield() { return zoneManager.getBattlefield(); }
    public Stack getStack() { return zoneManager.getStack(); }
    public GameLog getGameLog() { return gameLog; }
    public boolean isGameOver() { return gameOver; }
    public Player getWinner() { return winner; }
    public int getTurnNumber() { return turnNumber; }

    /**
     * 玩家输掉游戏。
     *
     * 【触发条件】
     * - 生命降到0或以下
     * - 中毒指示物达到10个
     * - 从空牌库抓牌
     *
     * @param player 输掉游戏的玩家
     * @param reason 失败原因（调试用）
     */
    public void loseGame(Player player, String reason) {
        if (!gameOver) {
            gameOver = true;
            winner = player.getOpponent();
            gameLog.logGameOver(winner, reason);
        }
    }

    /**
     * 检查是否是主阶段。
     */
    public boolean isMainPhase() {
        return currentPhase == TurnPhase.MAIN1 || currentPhase == TurnPhase.MAIN2;
    }

    /**
     * 检查是否是维持步骤。
     */
    public boolean isUpkeepStep() {
        return currentPhase == TurnPhase.UPKEEP;
    }

    /**
     * 检查 Aura 是否可以合法附属于目标。
     *
     * 【简化实现】
     * - 完整实现需要检查 enchant 关键词
     * - 如 "Enchant creature" 不能附属于地
     *
     * @param aura Aura 牌
     * @param target 目标永久物
     * @return 是否合法
     */
    public boolean isLegalAuraTarget(EnchantmentCard aura, PermanentCard target) {
        return true; // 简化：始终合法
    }

    // ========== UI 调用方法 ==========

    public boolean playCreature(Player player, CreatureCard creature) {
        return playPermanent(player, creature);
    }

    public boolean playEnchantment(Player player, EnchantmentCard enchantment) {
        return playPermanent(player, enchantment);
    }

    public boolean playArtifact(Player player, ArtifactCard artifact) {
        return playPermanent(player, artifact);
    }

    public boolean playPlaneswalker(Player player, PlaneswalkerCard planeswalker) {
        return playPermanent(player, planeswalker);
    }

    public boolean playBattle(Player player, BattleCard battle) {
        return playPermanent(player, battle);
    }

    public boolean playInstant(Player player, SpellCard spell) {
        return castSpell(player, spell);
    }

    // ========== 游戏监听器 ==========

    /**
     * 游戏事件监听器接口。
     *
     * 【用途】
     * - UI 可以通过实现此接口监听游戏事件
     * - 实时更新界面显示
     */
    public interface GameListener {
        /**
         * 阶段变化时调用。
         */
        void onPhaseChange(TurnPhase phase, Player currentPlayer);

        /**
         * 出牌时调用。
         */
        void onCardPlayed(Player player, Card card);

        /**
         * 造成伤害时调用。
         */
        void onDamageDealt(Player target, int amount);

        /**
         * 生物被消灭时调用。
         */
        void onCreatureDestroyed(CreatureCard creature);

        /**
         * 永久物被消灭时调用。
         */
        void onPermanentDestroyed(PermanentCard permanent);

        /**
         * 游戏结束时调用。
         */
        void onGameOver(Player winner);
    }

    private GameListener listener;

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public GameListener getListener() {
        return listener;
    }

    /**
     * 通知阶段变化。
     */
    public void notifyPhaseChange() {
        if (listener != null) {
            listener.onPhaseChange(currentPhase, currentPlayer);
        }
    }

    /**
     * 通知出牌事件。
     */
    public void notifyCardPlayed(Player player, Card card) {
        if (listener != null) {
            listener.onCardPlayed(player, card);
        }
    }

    /**
     * 通知伤害事件。
     */
    public void notifyDamageDealt(Player target, int amount) {
        if (listener != null) {
            listener.onDamageDealt(target, amount);
        }
    }

    /**
     * 通知生物消灭。
     */
    public void notifyCreatureDestroyed(CreatureCard creature) {
        if (listener != null) {
            listener.onCreatureDestroyed(creature);
        }
    }

    /**
     * 通知永久物消灭。
     */
    public void notifyPermanentDestroyed(PermanentCard permanent) {
        if (listener != null) {
            listener.onPermanentDestroyed(permanent);
        }
    }

    /**
     * 通知游戏结束。
     */
    public void notifyGameOver(Player winner) {
        if (listener != null) {
            listener.onGameOver(winner);
        }
    }
}
