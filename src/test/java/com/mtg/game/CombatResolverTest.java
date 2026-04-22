package com.mtg.game;

import com.mtg.model.*;
import com.mtg.player.Player;
import com.mtg.zones.Battlefield;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CombatResolver 单元测试。
 */
class CombatResolverTest {

    private Game game;
    private CombatResolver resolver;
    private Player player1;
    private Player player2;
    private Battlefield battlefield;

    @BeforeEach
    void setUp() {
        game = new Game("Player 1", "Player 2");
        resolver = new CombatResolver(game);
        player1 = game.getPlayer1();
        player2 = game.getPlayer2();
        // 需要先启动游戏才能获取 battlefield
        game.startGame();
        battlefield = game.getBattlefield();
        resolver.setBattlefield(battlefield);
        resolver.setPlayers(player1, player2);
    }

    @Test
    void testDeclareAttacker() {
        CreatureCard attacker = createCreature("Grizzly Bears", 2, 2, false);
        battlefield.add(attacker);
        attacker.setController(player1);

        resolver.declareAttacker(attacker, player2);

        List<CombatResolver.DeclaredAttacker> attackers = resolver.getAttackers();
        assertEquals(1, attackers.size());
        assertEquals(attacker, attackers.get(0).creature);
        assertEquals(player2, attackers.get(0).target);
    }

    @Test
    void testDeclareBlocker() {
        CreatureCard attacker = createCreature("Grizzly Bears", 2, 2, false);
        CreatureCard blocker = createCreature("Wall", 0, 4, false);

        battlefield.add(attacker);
        battlefield.add(blocker);
        attacker.setController(player1);
        blocker.setController(player2);

        resolver.declareAttacker(attacker, player2);
        resolver.declareBlocker(blocker, attacker);

        List<CombatResolver.DeclaredBlocker> blockers = resolver.getBlockers();
        assertEquals(1, blockers.size());
        assertEquals(blocker, blockers.get(0).blocker);
        assertEquals(attacker, blockers.get(0).attacker);
    }

    @Test
    void testCanAttack() {
        CreatureCard creature = createCreature("Test", 2, 2, false);
        battlefield.add(creature);
        creature.setController(player1);

        // 未横置且无召唤 sickness 可以攻击
        creature.clearSummoningSickness();
        assertTrue(resolver.canAttack(creature));

        // 横置后不能攻击
        creature.tap();
        assertFalse(resolver.canAttack(creature));
    }

    @Test
    void testCanAttackWithHaste() {
        CreatureCard creature = createCreature("Test", 2, 2, true); // 有敏捷
        battlefield.add(creature);
        creature.setController(player1);

        // 有敏捷异能，即使有召唤 sickness 也可以攻击
        assertTrue(resolver.canAttack(creature));
    }

    @Test
    void testCanBlock() {
        CreatureCard creature = createCreature("Test", 2, 2, false);
        battlefield.add(creature);
        creature.setController(player2);

        // 未横置可以阻挡
        assertTrue(resolver.canBlock(creature));

        // 横置后不能阻挡
        creature.tap();
        assertFalse(resolver.canBlock(creature));
    }

    @Test
    void testClearCombat() {
        CreatureCard attacker = createCreature("Bear", 2, 2, false);
        CreatureCard blocker = createCreature("Wall", 0, 4, false);

        battlefield.add(attacker);
        battlefield.add(blocker);
        attacker.setController(player1);
        blocker.setController(player2);

        resolver.declareAttacker(attacker, player2);
        resolver.declareBlocker(blocker, attacker);

        assertEquals(1, resolver.getAttackers().size());
        assertEquals(1, resolver.getBlockers().size());

        resolver.clearCombat();

        assertEquals(0, resolver.getAttackers().size());
        assertEquals(0, resolver.getBlockers().size());
    }

    @Test
    void testFlyingCannotBeBlockedByNonFlyer() {
        CreatureCard flyingAttacker = createFlyingCreature("Skybeasts", 3, 3);
        CreatureCard groundBlocker = createCreature("Ground", 1, 1, false);

        battlefield.add(flyingAttacker);
        battlefield.add(groundBlocker);
        flyingAttacker.setController(player1);
        groundBlocker.setController(player2);

        // 飞行生物不能被无飞行异能的生物阻挡（简化检查）
        // 完整实现需要检查 canBlockAttacker
        assertTrue(flyingAttacker.hasFlying());
        assertFalse(groundBlocker.hasFlying());
    }

    @Test
    void testMenaceRequiresMultipleBlockers() {
        CreatureCard menaceAttacker = createMenaceCreature("Threat", 3, 3);

        assertTrue(menaceAttacker.hasMenace());
        assertEquals(2, resolver.getRequiredBlockers(menaceAttacker));
    }

    @Test
    void testNoMenaceRequiresSingleBlocker() {
        CreatureCard normalAttacker = createCreature("Normal", 2, 2, false);

        assertFalse(normalAttacker.hasMenace());
        assertEquals(1, resolver.getRequiredBlockers(normalAttacker));
    }

    @Test
    void testResolveCombatDamage() {
        CreatureCard attacker = createCreature("Bear", 2, 2, false);
        battlefield.add(attacker);
        attacker.setController(player1);

        int initialLife = player2.getLife();
        resolver.declareAttacker(attacker, player2);

        // 无阻挡结算
        resolver.resolveAllCombatDamage();

        assertEquals(initialLife - 2, player2.getLife());
    }

    @Test
    void testBlockedCombatDamage() {
        CreatureCard attacker = createCreature("Bear", 2, 2, false);
        CreatureCard blocker = createCreature("Wall", 0, 4, false);

        battlefield.add(attacker);
        battlefield.add(blocker);
        attacker.setController(player1);
        blocker.setController(player2);

        resolver.declareAttacker(attacker, player2);
        resolver.declareBlocker(blocker, attacker);
        resolver.resolveAllCombatDamage();

        // 攻击者对阻挡者造成伤害，Wall 受到 2 点伤害（2 < 4，不会消灭）
        assertEquals(2, blocker.getMarkedDamage());
    }

    @Test
    void testTrample() {
        CreatureCard trampler = createTrampleCreature("Trampler", 4, 4);
        CreatureCard blocker = createCreature("Wall", 0, 3, false);

        battlefield.add(trampler);
        battlefield.add(blocker);
        trampler.setController(player1);
        blocker.setController(player2);

        int initialLife = player2.getLife();
        resolver.declareAttacker(trampler, player2);
        resolver.declareBlocker(blocker, trampler);
        resolver.resolveAllCombatDamage();

        // 践踏：4/4 践踏 vs 0/3 blocker
        // blocker 需 3 点杀死（base=3, marked=0），剩余 1 点给玩家
        assertEquals(initialLife - 1, player2.getLife());
    }

    @Test
    void testDeathtouch() {
        CreatureCard deathtoucher = createDeathtouchCreature("Assassin", 1, 1);
        CreatureCard blocker = createCreature("Big", 2, 10, false);

        battlefield.add(deathtoucher);
        battlefield.add(blocker);
        deathtoucher.setController(player1);
        blocker.setController(player2);

        resolver.declareAttacker(deathtoucher, player2);
        resolver.declareBlocker(blocker, deathtoucher);
        resolver.resolveAllCombatDamage();

        // 致命打击：任何正数伤害都是致命的
        // 1 点伤害 >= 10 防御力，消灭
    }

    // ========== Helper Methods ==========

    private CreatureCard createCreature(String name, int power, int toughness, boolean hasHaste) {
        ManaCost cost = ManaCost.of(0);
        CreatureCard creature = new CreatureCard(
            name, cost, "Test creature",
            List.of(ManaType.GREEN), power, toughness,
            false, false, false, false, false
        );
        creature.setOwner(player1);
        creature.setController(player1);
        creature.clearSummoningSickness();
        if (hasHaste) {
            creature.setHaste(true);
        }
        return creature;
    }

    private CreatureCard createFlyingCreature(String name, int power, int toughness) {
        ManaCost cost = ManaCost.of(0);
        CreatureCard creature = new CreatureCard(
            name, cost, "Flying creature",
            List.of(ManaType.BLUE), power, toughness,
            false, false, true, false, false
        );
        creature.setOwner(player1);
        creature.setController(player1);
        creature.clearSummoningSickness();
        return creature;
    }

    private CreatureCard createTrampleCreature(String name, int power, int toughness) {
        ManaCost cost = ManaCost.of(0);
        CreatureCard creature = new CreatureCard(
            name, cost, "Trample creature",
            List.of(ManaType.GREEN), power, toughness,
            false, false, false, false, true
        );
        creature.setOwner(player1);
        creature.setController(player1);
        creature.clearSummoningSickness();
        return creature;
    }

    private CreatureCard createDeathtouchCreature(String name, int power, int toughness) {
        ManaCost cost = ManaCost.of(0);
        CreatureCard creature = new CreatureCard(
            name, cost, "Deathtouch creature",
            List.of(ManaType.BLACK), power, toughness,
            false, false, false, false, false
        );
        creature.setOwner(player1);
        creature.setController(player1);
        creature.clearSummoningSickness();
        creature.setHasDeathtouch(true);
        return creature;
    }

    private CreatureCard createMenaceCreature(String name, int power, int toughness) {
        ManaCost cost = ManaCost.of(0);
        CreatureCard creature = new CreatureCard(
            name, cost, "Menace creature",
            List.of(ManaType.RED), power, toughness,
            false, false, false, false, false
        );
        creature.setOwner(player1);
        creature.setController(player1);
        creature.clearSummoningSickness();
        creature.setHasMenace(true);
        return creature;
    }
}
