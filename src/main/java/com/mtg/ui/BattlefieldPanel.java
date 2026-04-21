package com.mtg.ui;

import com.mtg.model.*;
import com.mtg.player.Player;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BattlefieldPanel extends JPanel {
    private Player player;
    private Player opponent;
    private GameFrame gameFrame;
    private boolean isCurrentPlayerBattlefield;

    // Card panels for all permanent types
    private List<CardPanel> creaturePanels;
    private List<CardPanel> landPanels;
    private List<CardPanel> enchantmentPanels;
    private List<CardPanel> artifactPanels;
    private List<CardPanel> planeswalkerPanels;
    private List<CardPanel> battlePanels;

    public BattlefieldPanel(Player player, Player opponent,
                           boolean isCurrentPlayerBattlefield, GameFrame gameFrame) {
        this.player = player;
        this.opponent = opponent;
        this.gameFrame = gameFrame;
        this.isCurrentPlayerBattlefield = isCurrentPlayerBattlefield;
        this.creaturePanels = new ArrayList<>();
        this.landPanels = new ArrayList<>();
        this.enchantmentPanels = new ArrayList<>();
        this.artifactPanels = new ArrayList<>();
        this.planeswalkerPanels = new ArrayList<>();
        this.battlePanels = new ArrayList<>();
        setupPanel();
    }

    private void setupPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            isCurrentPlayerBattlefield ? "Your Battlefield" : "Opponent Battlefield"
        ));
        setPreferredSize(new Dimension(800, 150));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        setBackground(new Color(60, 60, 60));
    }

    public void updateBattlefield() {
        removeAll();
        creaturePanels.clear();
        landPanels.clear();
        enchantmentPanels.clear();
        artifactPanels.clear();
        planeswalkerPanels.clear();
        battlePanels.clear();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        // Creatures row
        mainPanel.add(createPermanentRow("Creatures", player.getCreatures(), creaturePanels, Color.ORANGE));

        // Lands row
        mainPanel.add(createPermanentRow("Lands", player.getLands(), landPanels, new Color(139, 69, 19)));

        // Enchantments row
        mainPanel.add(createPermanentRow("Enchantments", player.getEnchantments(), enchantmentPanels, Color.MAGENTA));

        // Artifacts row
        mainPanel.add(createPermanentRow("Artifacts", player.getArtifacts(), artifactPanels, Color.CYAN));

        // Planeswalkers row
        mainPanel.add(createPermanentRow("Planeswalkers", player.getPlaneswalkers(), planeswalkerPanels, Color.PINK));

        // Battles row
        mainPanel.add(createPermanentRow("Battles", player.getBattles(), battlePanels, Color.YELLOW));

        add(mainPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private <T extends PermanentCard> JPanel createPermanentRow(String label, List<T> permanents,
                                                                   List<CardPanel> panels, Color accentColor) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        rowPanel.setOpaque(false);

        JLabel typeLabel = new JLabel(label + ":");
        typeLabel.setForeground(accentColor);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        rowPanel.add(typeLabel);

        if (permanents.isEmpty()) {
            JLabel emptyLabel = new JLabel("(none)");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 9));
            rowPanel.add(emptyLabel);
        } else {
            for (T permanent : permanents) {
                CardPanel cardPanel = new CardPanel(permanent);

                if (isCurrentPlayerBattlefield && gameFrame != null) {
                    if (permanent instanceof CreatureCard creature) {
                        boolean canAttack = creature.canAttack();
                        cardPanel.setPlayable(canAttack);

                        if (gameFrame.isInCombatPhase() && gameFrame.getCurrentPlayer() == player) {
                            cardPanel.setOnClick(() -> gameFrame.onOwnCreatureClicked(creature));
                        } else if (!isCurrentPlayerBattlefield &&
                                   gameFrame.isInCombatPhase() &&
                                   gameFrame.getCurrentPlayer() == opponent) {
                            cardPanel.setOnClick(() -> gameFrame.onOpponentCreatureClicked(creature));
                        }
                    }
                }

                rowPanel.add(cardPanel);
                panels.add(cardPanel);
            }
        }

        return rowPanel;
    }

    public void refresh() {
        updateBattlefield();
    }

    public void setCurrentPlayerBattlefield(boolean isCurrent) {
        this.isCurrentPlayerBattlefield = isCurrent;
        setBorder(BorderFactory.createTitledBorder(
            isCurrent ? "Your Battlefield" : "Opponent Battlefield"
        ));
        updateBattlefield();
    }

    public Player getPlayer() {
        return player;
    }
}
