package com.mtg.ui;

import com.mtg.game.TurnPhase;
import com.mtg.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * PhaseIndicator - 回合阶段指示器，图形化显示当前回合的阶段进度。
 *
 * 【功能说明】
 * - 以可视化方式展示完整的回合结构
 * - 高亮显示当前阶段
 * - 标记已完成的阶段
 * - 显示主动玩家和回合数
 *
 * 【设计决策】
 * - 使用 BoxLayout 横向排列各阶段组
 * - 每个阶段使用 JLabel，带颜色编码
 * - 当前阶段使用醒目颜色，已完成阶段使用较浅颜色
 * - 点击阶段可快速跳转（简化版本仅用于显示）
 */
public class PhaseIndicator extends JPanel {
    private Player currentPlayer;
    private int turnNumber;
    private TurnPhase currentPhase;
    private TurnPhase completedPhase;

    // 颜色配置
    private static final Color COLOR_PENDING = new Color(200, 200, 200);
    private static final Color COLOR_CURRENT = new Color(70, 130, 180);
    private static final Color COLOR_COMPLETED = new Color(100, 180, 100);
    private static final Color COLOR_TEXT_DARK = new Color(30, 30, 30);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;

    // 阶段组
    private JPanel beginningPanel;
    private JPanel main1Panel;
    private JPanel combatPanel;
    private JPanel main2Panel;
    private JPanel endingPanel;

    // 阶段标签
    private Map<TurnPhase, JLabel> phaseLabels;

    public PhaseIndicator() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setPreferredSize(new Dimension(600, 80));
        setOpaque(true);
        setBackground(new Color(240, 240, 245));

        phaseLabels = new HashMap<>();

        setupPhaseGroups();
        updateDisplay();
    }

    private void setupPhaseGroups() {
        // 顶部：回合信息和阶段名称
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Turn Structure", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_TEXT_DARK);

        JLabel currentPhaseLabel = new JLabel("", SwingConstants.CENTER);
        currentPhaseLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentPhaseLabel.setName("currentPhaseLabel");

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(currentPhaseLabel, BorderLayout.CENTER);

        // 主体：阶段流程
        JPanel flowPanel = new JPanel();
        flowPanel.setLayout(new BoxLayout(flowPanel, BoxLayout.X_AXIS));
        flowPanel.setOpaque(false);

        // Beginning Phase
        beginningPanel = createPhaseGroup("Beginning", new TurnPhase[]{
            TurnPhase.UNTAAP, TurnPhase.UPKEEP, TurnPhase.DRAW
        });

        // Main Phase 1
        main1Panel = createPhaseGroup("Main 1", new TurnPhase[]{
            TurnPhase.MAIN1
        });

        // Combat Phase
        combatPanel = createPhaseGroup("Combat", new TurnPhase[]{
            TurnPhase.COMBAT_START, TurnPhase.DECLARE_ATTACKERS,
            TurnPhase.DECLARE_BLOCKERS, TurnPhase.COMBAT_DAMAGE,
            TurnPhase.COMBAT_DAMAGE_FIRST, TurnPhase.COMBAT_END
        });

        // Main Phase 2
        main2Panel = createPhaseGroup("Main 2", new TurnPhase[]{
            TurnPhase.MAIN2
        });

        // Ending Phase
        endingPanel = createPhaseGroup("Ending", new TurnPhase[]{
            TurnPhase.END, TurnPhase.CLEANUP
        });

        // 添加箭头连接
        flowPanel.add(beginningPanel);
        flowPanel.add(createArrow());
        flowPanel.add(main1Panel);
        flowPanel.add(createArrow());
        flowPanel.add(combatPanel);
        flowPanel.add(createArrow());
        flowPanel.add(main2Panel);
        flowPanel.add(createArrow());
        flowPanel.add(endingPanel);

        add(headerPanel, BorderLayout.NORTH);
        add(flowPanel, BorderLayout.CENTER);
    }

    private JPanel createPhaseGroup(String groupName, TurnPhase[] phases) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel nameLabel = new JLabel(groupName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        nameLabel.setForeground(COLOR_TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel phasesPanel = new JPanel();
        phasesPanel.setLayout(new BoxLayout(phasesPanel, BoxLayout.X_AXIS));
        phasesPanel.setOpaque(false);

        for (int i = 0; i < phases.length; i++) {
            TurnPhase phase = phases[i];
            JLabel label = createPhaseLabel(phase);
            phaseLabels.put(phase, label);
            phasesPanel.add(label);

            if (i < phases.length - 1) {
                phasesPanel.add(createSmallArrow());
            }
        }

        group.add(nameLabel);
        group.add(phasesPanel);

        return group;
    }

    private JLabel createPhaseLabel(TurnPhase phase) {
        String shortName = getShortPhaseName(phase);
        JLabel label = new JLabel(shortName, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 9));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        label.setName(phase.name());
        return label;
    }

    private String getShortPhaseName(TurnPhase phase) {
        return switch (phase) {
            case UNTAAP -> "Untap";
            case UPKEEP -> "Upkeep";
            case DRAW -> "Draw";
            case MAIN1 -> "Main 1";
            case MAIN2 -> "Main 2";
            case COMBAT_START -> "Start";
            case DECLARE_ATTACKERS -> "Atk";
            case DECLARE_BLOCKERS -> "Blk";
            case COMBAT_DAMAGE -> "Dmg";
            case COMBAT_DAMAGE_FIRST -> "1st Dmg";
            case COMBAT_END -> "End C.";
            case END -> "End";
            case CLEANUP -> "Cleanup";
            case GAME_OVER -> "Over";
        };
    }

    private JLabel createArrow() {
        JLabel arrow = new JLabel("→", SwingConstants.CENTER);
        arrow.setFont(new Font("Arial", Font.PLAIN, 12));
        arrow.setForeground(COLOR_TEXT_DARK);
        arrow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        return arrow;
    }

    private JLabel createSmallArrow() {
        JLabel arrow = new JLabel(">", SwingConstants.CENTER);
        arrow.setFont(new Font("Arial", Font.PLAIN, 8));
        arrow.setForeground(COLOR_TEXT_DARK);
        arrow.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        return arrow;
    }

    /**
     * 更新显示。
     *
     * @param turn 回合数
     * @param phase 当前阶段
     * @param player 主动玩家
     */
    public void update(int turn, TurnPhase phase, Player player) {
        this.turnNumber = turn;
        this.currentPhase = phase;
        this.currentPlayer = player;
        this.completedPhase = getPreviousPhase(phase);
        updateDisplay();
    }

    private void updateDisplay() {
        // 更新当前阶段标签
        JLabel currentPhaseLabel = (JLabel) ((JPanel) getComponent(0)).getComponent(1);
        if (currentPhase != null) {
            currentPhaseLabel.setText(String.format("Turn %d | %s | %s",
                turnNumber,
                currentPhase.getName(),
                currentPlayer != null ? currentPlayer.getName() : ""));
        }

        // 更新所有阶段标签的颜色
        for (Map.Entry<TurnPhase, JLabel> entry : phaseLabels.entrySet()) {
            TurnPhase phase = entry.getKey();
            JLabel label = entry.getValue();
            Color bgColor;
            Color fgColor;

            if (phase == currentPhase) {
                // 当前阶段
                bgColor = COLOR_CURRENT;
                fgColor = COLOR_TEXT_LIGHT;
            } else if (isPhaseCompleted(phase)) {
                // 已完成阶段
                bgColor = COLOR_COMPLETED;
                fgColor = COLOR_TEXT_DARK;
            } else {
                // 未到达阶段
                bgColor = COLOR_PENDING;
                fgColor = COLOR_TEXT_DARK;
            }

            label.setBackground(bgColor);
            label.setForeground(fgColor);
            label.setOpaque(true);
        }

        revalidate();
        repaint();
    }

    private boolean isPhaseCompleted(TurnPhase phase) {
        if (currentPhase == null || completedPhase == null) {
            return false;
        }

        // 获取阶段顺序
        TurnPhase[] order = {
            TurnPhase.UNTAAP, TurnPhase.UPKEEP, TurnPhase.DRAW,
            TurnPhase.MAIN1,
            TurnPhase.COMBAT_START, TurnPhase.DECLARE_ATTACKERS,
            TurnPhase.DECLARE_BLOCKERS, TurnPhase.COMBAT_DAMAGE,
            TurnPhase.COMBAT_DAMAGE_FIRST, TurnPhase.COMBAT_END,
            TurnPhase.MAIN2,
            TurnPhase.END, TurnPhase.CLEANUP
        };

        int phaseIndex = -1;
        int currentIndex = -1;

        for (int i = 0; i < order.length; i++) {
            if (order[i] == phase) {
                phaseIndex = i;
            }
            if (order[i] == completedPhase) {
                currentIndex = i;
            }
        }

        return phaseIndex >= 0 && currentIndex >= 0 && phaseIndex <= currentIndex;
    }

    private TurnPhase getPreviousPhase(TurnPhase phase) {
        if (phase == null) return null;

        TurnPhase[] order = {
            TurnPhase.UNTAAP, TurnPhase.UPKEEP, TurnPhase.DRAW,
            TurnPhase.MAIN1,
            TurnPhase.COMBAT_START, TurnPhase.DECLARE_ATTACKERS,
            TurnPhase.DECLARE_BLOCKERS, TurnPhase.COMBAT_DAMAGE,
            TurnPhase.COMBAT_DAMAGE_FIRST, TurnPhase.COMBAT_END,
            TurnPhase.MAIN2,
            TurnPhase.END, TurnPhase.CLEANUP
        };

        for (int i = 1; i < order.length; i++) {
            if (order[i] == phase) {
                return order[i - 1];
            }
        }
        return null;
    }

    /**
     * 获取当前阶段。
     */
    public TurnPhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * 获取回合数。
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * 获取主动玩家。
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
}
