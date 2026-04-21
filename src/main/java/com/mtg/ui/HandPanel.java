package com.mtg.ui;

import com.mtg.model.*;
import com.mtg.player.Player;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HandPanel extends JPanel {
    private Player player;
    private List<CardPanel> cardPanels;
    private GameFrame gameFrame;
    private boolean isCurrentPlayerHand;

    public HandPanel(Player player, boolean isCurrentPlayerHand, GameFrame gameFrame) {
        this.player = player;
        this.cardPanels = new ArrayList<>();
        this.gameFrame = gameFrame;
        this.isCurrentPlayerHand = isCurrentPlayerHand;
        setupPanel();
    }

    private void setupPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createTitledBorder(isCurrentPlayerHand ? "Your Hand" : "Opponent Hand"));
        setPreferredSize(new Dimension(800, 130));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        setBackground(new Color(50, 50, 50));
    }

    public void updateCards() {
        removeAll();
        cardPanels.clear();

        List<Card> cards = player.getHand().getCards();
        for (Card card : cards) {
            CardPanel cardPanel = new CardPanel(card);

            if (isCurrentPlayerHand && gameFrame != null) {
                cardPanel.setPlayable(gameFrame.canPlayCard(card));
                cardPanel.setOnClick(() -> gameFrame.onCardClicked(card));
            }

            cardPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            add(Box.createHorizontalStrut(5));
            add(cardPanel);
            cardPanels.add(cardPanel);
        }

        add(Box.createHorizontalGlue());
        revalidate();
        repaint();
    }

    public void refresh() {
        updateCards();
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentPlayerHand(boolean isCurrent) {
        this.isCurrentPlayerHand = isCurrent;
        setBorder(BorderFactory.createTitledBorder(isCurrent ? "Your Hand" : "Opponent Hand"));
        updateCards();
    }
}
