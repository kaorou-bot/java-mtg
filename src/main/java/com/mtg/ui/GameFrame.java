package com.mtg.ui;

import com.mtg.game.*;
import com.mtg.model.*;
import com.mtg.player.Player;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GameFrame extends JFrame implements Game.GameListener {
    private Game game;
    private PlayerPanel player1Panel;
    private PlayerPanel player2Panel;
    private HandPanel player1Hand;
    private HandPanel player2Hand;
    private BattlefieldPanel player1Battlefield;
    private BattlefieldPanel player2Battlefield;
    private JPanel buttonPanel;
    private JButton nextPhaseButton;
    private JButton endTurnButton;
    private JButton attackButton;
    private JList<String> logList;
    private DefaultListModel<String> logModel;

    private CreatureCard selectedAttacker;
    private PhaseIndicator phaseIndicator;
    private CombatResolver combatResolver;

    public GameFrame() {
        setTitle("MTG Battle - Magic: The Gathering");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        setupGame();
        setupUI();
        setupListeners();

        game.startGame();
        refresh();
    }

    private void setupGame() {
        game = new Game("Player 1", "Player 2");
        game.setListener(this);
    }

    private void setupUI() {
        JPanel topPanel = new JPanel(new BorderLayout());

        player1Panel = new PlayerPanel(game.getPlayer1());
        player2Panel = new PlayerPanel(game.getPlayer2());

        JPanel playerInfoPanel = new JPanel(new GridLayout(2, 1));
        playerInfoPanel.add(player2Panel);
        playerInfoPanel.add(player1Panel);

        phaseIndicator = new PhaseIndicator();

        topPanel.add(playerInfoPanel, BorderLayout.NORTH);
        topPanel.add(phaseIndicator, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        player2Hand = new HandPanel(game.getPlayer2(), false, this);
        player1Hand = new HandPanel(game.getPlayer1(), true, this);

        JPanel battlefieldPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        battlefieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        player2Battlefield = new BattlefieldPanel(game.getPlayer2(), game.getPlayer1(), false, this);
        player1Battlefield = new BattlefieldPanel(game.getPlayer1(), game.getPlayer2(), true, this);

        // 初始化战斗解析器
        combatResolver = new CombatResolver(game);

        // 连接战斗解析器到战场面板
        player1Battlefield.setCombatResolver(combatResolver);
        player2Battlefield.setCombatResolver(combatResolver);

        battlefieldPanel.add(player2Battlefield);
        battlefieldPanel.add(player1Battlefield);

        JPanel battlefieldContainer = new JPanel(new BorderLayout());
        battlefieldContainer.add(battlefieldPanel, BorderLayout.CENTER);

        JPanel handsContainer = new JPanel(new GridLayout(2, 1));
        handsContainer.add(player2Hand);
        handsContainer.add(player1Hand);

        centerPanel.add(handsContainer, BorderLayout.SOUTH);
        centerPanel.add(battlefieldContainer, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout());

        nextPhaseButton = new JButton("Next Phase");
        endTurnButton = new JButton("End Turn");
        attackButton = new JButton("Enter Combat");

        buttonPanel.add(nextPhaseButton);
        buttonPanel.add(endTurnButton);
        buttonPanel.add(attackButton);

        logModel = new DefaultListModel<>();
        logList = new JList<>(logModel);
        logList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logList);
        logScroll.setPreferredSize(new Dimension(250, 100));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
        add(logScroll, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void setupListeners() {
        nextPhaseButton.addActionListener(e -> {
            game.nextPhase();
            refresh();
        });

        endTurnButton.addActionListener(e -> {
            game.endTurn();
            refresh();
        });

        attackButton.addActionListener(e -> {
            TurnPhase phase = game.getCurrentPhase();
            if (phase == TurnPhase.MAIN1 || phase == TurnPhase.MAIN2) {
                while (game.getCurrentPhase() != TurnPhase.COMBAT_START) {
                    game.nextPhase();
                }
                game.nextPhase();
                refresh();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    GameFrame.this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    public void refresh() {
        Player currentPlayer = game.getCurrentPlayer();
        player1Panel.setCurrentPlayer(currentPlayer == game.getPlayer1());
        player2Panel.setCurrentPlayer(currentPlayer == game.getPlayer2());

        player1Panel.updateDisplay();
        player2Panel.updateDisplay();
        player1Panel.updatePhase(game.getCurrentPhase().getName());

        player1Hand.setCurrentPlayerHand(currentPlayer == game.getPlayer1());
        player2Hand.setCurrentPlayerHand(currentPlayer == game.getPlayer2());

        player1Hand.refresh();
        player2Hand.refresh();
        player1Battlefield.refresh();
        player2Battlefield.refresh();

        phaseIndicator.update(game.getTurnNumber(), game.getCurrentPhase(), currentPlayer);

        updateButtons();
        updateLogDisplay();
    }

    private void updateLogDisplay() {
        logModel.clear();
        GameLog gameLog = game.getGameLog();
        if (gameLog != null) {
            for (GameLog.LogEntry entry : gameLog.getEntries()) {
                logModel.addElement(entry.toString());
            }
        }
    }

    private void updateButtons() {
        TurnPhase phase = game.getCurrentPhase();
        Player current = game.getCurrentPlayer();

        nextPhaseButton.setEnabled(!game.isGameOver());
        endTurnButton.setEnabled(!game.isGameOver() && current == game.getPlayer1());

        attackButton.setEnabled(
            !game.isGameOver() &&
            (phase == TurnPhase.MAIN1 || phase == TurnPhase.MAIN2) &&
            current == game.getPlayer1()
        );

        if (phase == TurnPhase.DECLARE_ATTACKERS) {
            attackButton.setText("End Declare Attackers");
        } else if (phase == TurnPhase.DECLARE_BLOCKERS) {
            attackButton.setText("End Declare Blockers");
        } else {
            attackButton.setText("Enter Combat");
        }
    }

    public boolean canPlayCard(Card card) {
        if (game.isGameOver()) return false;
        Player current = game.getCurrentPlayer();
        if (current != game.getPlayer1()) return false;

        TurnPhase phase = game.getCurrentPhase();
        if (phase != TurnPhase.MAIN1 && phase != TurnPhase.MAIN2) return false;

        if (card instanceof LandCard) {
            return true;
        } else if (card instanceof CreatureCard creature) {
            return current.canPayCost(creature.getManaCost());
        } else if (card instanceof EnchantmentCard enchantment) {
            return current.canPayCost(enchantment.getManaCost());
        } else if (card instanceof ArtifactCard artifact) {
            return current.canPayCost(artifact.getManaCost());
        } else if (card instanceof PlaneswalkerCard planeswalker) {
            return current.canPayCost(planeswalker.getManaCost());
        } else if (card instanceof BattleCard battle) {
            return current.canPayCost(battle.getManaCost());
        } else if (card instanceof SpellCard spell) {
            return current.canPayCost(spell.getManaCost());
        }
        return false;
    }

    public boolean isInCombatPhase() {
        TurnPhase phase = game.getCurrentPhase();
        return phase == TurnPhase.DECLARE_ATTACKERS || phase == TurnPhase.DECLARE_BLOCKERS;
    }

    public void onCardClicked(Card card) {
        if (game.isGameOver()) return;
        Player current = game.getCurrentPlayer();
        if (current != game.getPlayer1()) return;

        TurnPhase phase = game.getCurrentPhase();
        if (phase != TurnPhase.MAIN1 && phase != TurnPhase.MAIN2) return;

        boolean played = false;

        if (card instanceof LandCard land) {
            played = game.playLand(current, land);
        } else if (card instanceof CreatureCard creature) {
            played = game.playCreature(current, creature);
        } else if (card instanceof EnchantmentCard enchantment) {
            played = game.playEnchantment(current, enchantment);
        } else if (card instanceof ArtifactCard artifact) {
            played = game.playArtifact(current, artifact);
        } else if (card instanceof PlaneswalkerCard planeswalker) {
            played = game.playPlaneswalker(current, planeswalker);
        } else if (card instanceof BattleCard battle) {
            played = game.playBattle(current, battle);
        } else if (card instanceof SpellCard spell) {
            played = game.playInstant(current, spell);
        }

        if (played) {
            refresh();
        }
    }

    public void onOwnCreatureClicked(CreatureCard creature) {
        if (game.isGameOver()) return;
        TurnPhase phase = game.getCurrentPhase();

        if (phase == TurnPhase.DECLARE_ATTACKERS && creature.canAttack()) {
            game.declareAttacker(creature, game.getCurrentPlayer().getOpponent());
            refresh();
        }
    }

    public void onOpponentCreatureClicked(CreatureCard blocker) {
        if (game.isGameOver()) return;
        if (selectedAttacker != null) {
            game.declareBlocker(blocker, selectedAttacker);
            selectedAttacker = null;
            refresh();
        }
    }

    @Override
    public void onPhaseChange(TurnPhase phase, Player currentPlayer) {
        refresh();
    }

    @Override
    public void onCardPlayed(Player player, Card card) {
        refresh();
    }

    @Override
    public void onDamageDealt(Player target, int amount) {
        refresh();
    }

    @Override
    public void onCreatureDestroyed(CreatureCard creature) {
        refresh();
    }

    @Override
    public void onPermanentDestroyed(PermanentCard permanent) {
        refresh();
    }

    @Override
    public void onGameOver(Player winner) {
        JOptionPane.showMessageDialog(
            this,
            winner.getName() + " wins!",
            "Game Over",
            JOptionPane.INFORMATION_MESSAGE
        );
        refresh();
    }

    public Player getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GameFrame().setVisible(true);
        });
    }
}
