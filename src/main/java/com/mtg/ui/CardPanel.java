package com.mtg.ui;

import com.mtg.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CardPanel extends JPanel {
    private Card card;
    private boolean selected;
    private boolean playable;
    private Runnable onClick;

    private static final Color CREATURE_COLOR = new Color(139, 69, 19);
    private static final Color LAND_COLOR = new Color(85, 107, 47);
    private static final Color ENCHANTMENT_COLOR = new Color(128, 0, 128);
    private static final Color ARTIFACT_COLOR = new Color(192, 192, 192);
    private static final Color PLANESWALKER_COLOR = new Color(200, 100, 200);
    private static final Color BATTLE_COLOR = new Color(200, 150, 50);
    private static final Color INSTANT_COLOR = new Color(25, 25, 112);
    private static final Color SELECTED_COLOR = new Color(255, 215, 0);
    private static final Color PLAYABLE_COLOR = new Color(144, 238, 144);

    public CardPanel(Card card) {
        this.card = card;
        this.selected = false;
        this.playable = false;
        setupPanel();
    }

    private void setupPanel() {
        setPreferredSize(new Dimension(70, 95));
        setMaximumSize(new Dimension(70, 95));
        setMinimumSize(new Dimension(70, 95));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null && playable) {
                    onClick.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
        });

        updateBackground();
    }

    private void updateBackground() {
        if (selected) {
            setBackground(SELECTED_COLOR);
        } else if (playable) {
            setBackground(PLAYABLE_COLOR);
        } else {
            setBackground(getCardColor());
        }
    }

    private Color getCardColor() {
        if (card instanceof CreatureCard) {
            return CREATURE_COLOR;
        } else if (card instanceof LandCard) {
            return LAND_COLOR;
        } else if (card instanceof EnchantmentCard) {
            return ENCHANTMENT_COLOR;
        } else if (card instanceof ArtifactCard) {
            return ARTIFACT_COLOR;
        } else if (card instanceof PlaneswalkerCard) {
            return PLANESWALKER_COLOR;
        } else if (card instanceof BattleCard) {
            return BATTLE_COLOR;
        } else if (card instanceof SpellCard) {
            return INSTANT_COLOR;
        }
        return Color.GRAY;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(2, 2, getWidth() - 4, getHeight() - 4);

        g2d.setColor(Color.BLACK);

        if (card instanceof CreatureCard creature) {
            drawCreatureCard(g2d, creature);
        } else if (card instanceof LandCard land) {
            drawLandCard(g2d, land);
        } else if (card instanceof EnchantmentCard enchantment) {
            drawEnchantmentCard(g2d, enchantment);
        } else if (card instanceof ArtifactCard artifact) {
            drawArtifactCard(g2d, artifact);
        } else if (card instanceof PlaneswalkerCard planeswalker) {
            drawPlaneswalkerCard(g2d, planeswalker);
        } else if (card instanceof BattleCard battle) {
            drawBattleCard(g2d, battle);
        } else if (card instanceof SpellCard spell) {
            drawSpellCard(g2d, spell);
        }
    }

    private void drawCreatureCard(Graphics2D g2d, CreatureCard creature) {
        int w = getWidth();
        int h = getHeight();

        g2d.setColor(new Color(245, 222, 179));
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String manaCost = creature.getManaCost().toDisplayString();
        g2d.drawString(manaCost, 3, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(creature.getName(), 10);
        g2d.drawString(name, 3, 22);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(10, 30, w - 25, 35);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(10, 30, w - 25, 35);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String power = String.valueOf(creature.getCurrentPower());
        String toughness = String.valueOf(creature.getCurrentToughness());
        g2d.drawString(power, 15, 52);
        g2d.drawString(toughness, w - 22, 52);

        if (creature.hasFlying()) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 6));
            g2d.drawString("Fly", 3, h - 18);
        }
        if (creature.hasVigilance()) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 6));
            g2d.drawString("Vig", 3, h - 10);
        }

        if (creature.isTapped()) {
            g2d.setColor(new Color(255, 0, 0, 100));
            g2d.fillRect(3, 3, w - 6, h - 6);
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(3, 3, w - 3, h - 3);
            g2d.drawLine(w - 3, 3, 3, h - 3);
        }
    }

    private void drawLandCard(Graphics2D g2d, LandCard land) {
        int w = getWidth();
        int h = getHeight();

        Color landColor = getLandColor(land);
        g2d.setColor(landColor);
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(land.getName(), 10);
        g2d.drawString(name, 3, 15);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String symbol = land.getProducedMana().getSymbol();
        g2d.drawString(symbol, w / 2 - 8, h / 2 + 8);

        if (land.isTapped()) {
            g2d.setColor(new Color(128, 128, 128, 150));
            g2d.fillRect(3, 3, w - 6, h - 6);
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(3, 3, w - 3, h - 3);
            g2d.drawLine(w - 3, 3, 3, h - 3);
        }
    }

    private void drawEnchantmentCard(Graphics2D g2d, EnchantmentCard enchantment) {
        int w = getWidth();
        int h = getHeight();

        g2d.setColor(new Color(220, 180, 255));
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String manaCost = enchantment.getManaCost().toDisplayString();
        g2d.drawString(manaCost, 3, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(enchantment.getName(), 10);
        g2d.drawString(name, 3, 22);

        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 6));
        g2d.drawString("Enchant", 3, 35);

        if (enchantment.getEnchantmentSubtype() != null) {
            g2d.drawString(enchantment.getEnchantmentSubtype(), 3, 45);
        }

        if (enchantment.isTapped()) {
            g2d.setColor(new Color(128, 128, 128, 150));
            g2d.fillRect(3, 3, w - 6, h - 6);
        }
    }

    private void drawArtifactCard(Graphics2D g2d, ArtifactCard artifact) {
        int w = getWidth();
        int h = getHeight();

        g2d.setColor(new Color(200, 200, 210));
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String manaCost = artifact.getManaCost().toDisplayString();
        g2d.drawString(manaCost, 3, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(artifact.getName(), 10);
        g2d.drawString(name, 3, 22);

        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 6));
        String type = artifact.isEquipment() ? "Equipment" : "Artifact";
        g2d.drawString(type, 3, 35);

        if (artifact.isTapped()) {
            g2d.setColor(new Color(128, 128, 128, 150));
            g2d.fillRect(3, 3, w - 6, h - 6);
        }
    }

    private void drawPlaneswalkerCard(Graphics2D g2d, PlaneswalkerCard planeswalker) {
        int w = getWidth();
        int h = getHeight();

        g2d.setColor(new Color(230, 180, 230));
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String manaCost = planeswalker.getManaCost().toDisplayString();
        g2d.drawString(manaCost, 3, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(planeswalker.getName(), 10);
        g2d.drawString(name, 3, 22);

        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 6));
        g2d.drawString("PW", 3, 35);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(planeswalker.getLoyalty()), w / 2 - 5, h - 15);

        if (planeswalker.isTapped()) {
            g2d.setColor(new Color(128, 128, 128, 150));
            g2d.fillRect(3, 3, w - 6, h - 6);
        }
    }

    private void drawBattleCard(Graphics2D g2d, BattleCard battle) {
        int w = getWidth();
        int h = getHeight();

        g2d.setColor(new Color(255, 220, 180));
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String manaCost = battle.getManaCost().toDisplayString();
        g2d.drawString(manaCost, 3, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(battle.getName(), 10);
        g2d.drawString(name, 3, 22);

        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 6));
        g2d.drawString("Battle", 3, 35);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(battle.getCurrentDefense()), w / 2 - 5, h - 15);

        if (battle.isTapped()) {
            g2d.setColor(new Color(128, 128, 128, 150));
            g2d.fillRect(3, 3, w - 6, h - 6);
        }
    }

    private void drawSpellCard(Graphics2D g2d, SpellCard spell) {
        int w = getWidth();
        int h = getHeight();

        Color spellColor = spell.getColor().isEmpty() ? Color.GRAY :
            getManaColor(spell.getColor().get(0));
        g2d.setColor(spellColor);
        g2d.fillRect(3, 3, w - 6, h - 6);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        g2d.drawString(spell.getManaCost().toDisplayString(), 3, 10);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        String name = truncate(spell.getName(), 10);
        g2d.drawString(name, 3, 22);

        g2d.setFont(new Font("Arial", Font.PLAIN, 6));
        g2d.drawString(spell.getSpellType().getName(), 3, 35);
    }

    private Color getLandColor(LandCard land) {
        ManaType type = land.getProducedMana();
        return switch (type) {
            case WHITE -> new Color(255, 255, 200);
            case BLUE -> new Color(200, 200, 255);
            case BLACK -> new Color(100, 100, 100);
            case RED -> new Color(255, 150, 150);
            case GREEN -> new Color(150, 255, 150);
            case COLORLESS -> Color.GRAY;
        };
    }

    private Color getManaColor(ManaType type) {
        return switch (type) {
            case WHITE -> new Color(255, 255, 200);
            case BLUE -> new Color(200, 200, 255);
            case BLACK -> new Color(80, 80, 80);
            case RED -> new Color(255, 150, 150);
            case GREEN -> new Color(150, 255, 150);
            case COLORLESS -> Color.GRAY;
        };
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateBackground();
        repaint();
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
        updateBackground();
        repaint();
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public Card getCard() {
        return card;
    }
}
