package com.mtg.ui;

import com.mtg.model.*;
import com.mtg.player.Player;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlayerPanel extends JPanel {
    private Player player;
    private boolean isCurrentPlayer;
    private JLabel nameLabel;
    private JLabel lifeLabel;
    private JLabel manaLabel;
    private JLabel deckLabel;
    private JLabel phaseLabel;

    public PlayerPanel(Player player) {
        this.player = player;
        setupPanel();
    }

    private void setupPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createTitledBorder("Player Info"));
        setPreferredSize(new Dimension(400, 50));

        nameLabel = new JLabel();
        lifeLabel = new JLabel();
        manaLabel = new JLabel();
        deckLabel = new JLabel();
        phaseLabel = new JLabel();

        Font boldFont = new Font("Arial", Font.BOLD, 14);

        nameLabel.setFont(boldFont);
        lifeLabel.setFont(boldFont);
        manaLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        deckLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        phaseLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        add(nameLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(lifeLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(manaLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(deckLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(phaseLabel);

        updateDisplay();
    }

    public void updateDisplay() {
        nameLabel.setText(player.getName());
        lifeLabel.setText("Life: " + player.getLife());
        lifeLabel.setForeground(player.getLife() <= 5 ? Color.RED : Color.BLACK);

        // 显示法力池
        com.mtg.game.ManaPool pool = player.getManaPool();
        if (pool != null && !pool.isEmpty()) {
            manaLabel.setText("Mana: " + pool.toDisplayString());
            manaLabel.setForeground(Color.BLUE);
        } else {
            List<ManaType> mana = player.getAvailableMana();
            StringBuilder manaStr = new StringBuilder("Mana: ");
            for (ManaType m : mana) {
                manaStr.append(m.getSymbol()).append(" ");
            }
            if (mana.isEmpty()) {
                manaStr.append("-");
            }
            manaLabel.setText(manaStr.toString());
            manaLabel.setForeground(Color.BLACK);
        }

        deckLabel.setText("Deck: " + player.getDeck().size() + " | Hand: " + player.getHand().size());
    }

    public void setCurrentPlayer(boolean isCurrent) {
        this.isCurrentPlayer = isCurrent;
        if (isCurrent) {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLUE, 2),
                player.getName() + " (Current Turn)"
            ));
        } else {
            setBorder(BorderFactory.createTitledBorder(player.getName()));
        }
        updateDisplay();
    }

    public void updatePhase(String phaseName) {
        phaseLabel.setText("Phase: " + phaseName);
    }

    public void updateLife(int life) {
        lifeLabel.setText("Life: " + life);
        lifeLabel.setForeground(life <= 5 ? Color.RED : Color.BLACK);
    }

    public Player getPlayer() {
        return player;
    }
}
