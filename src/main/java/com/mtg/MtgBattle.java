package com.mtg;

import com.mtg.ui.GameFrame;
import javax.swing.*;

public class MtgBattle {
    public static void main(String[] args) {
        System.out.println("MTG Battle - Magic: The Gathering Battle Game");
        System.out.println("Starting...");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
