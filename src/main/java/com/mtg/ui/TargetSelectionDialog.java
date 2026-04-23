package com.mtg.ui;

import com.mtg.game.Game;
import com.mtg.game.Target;
import com.mtg.game.TargetSelector;
import com.mtg.game.TargetType;
import com.mtg.model.Card;
import com.mtg.player.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TargetSelectionDialog - 目标选择对话框，用于咒语和异能的目标选择。
 *
 * 【功能说明】
 * - 显示当前可选择的合法目标
 * - 支持单目标和多目标选择
 * - 颜色编码区分目标类型
 *
 * 【规则依据】
 * - Rule 114: Targets
 * - Rule 601.2c: 目标选择步骤
 */
public class TargetSelectionDialog extends JDialog {
    private final Game game;
    private final List<TargetType> requiredTypes;
    private final Player caster;
    private final List<Target> selectedTargets;
    private final JList<Object> targetList;
    private final DefaultListModel<Object> listModel;
    private boolean cancelled;

    public TargetSelectionDialog(Frame parent, Game game, List<TargetType> targetTypes, Player caster) {
        super(parent, "Select Target", true);
        this.game = game;
        this.requiredTypes = new ArrayList<>(targetTypes);
        this.caster = caster;
        this.selectedTargets = new ArrayList<>();
        this.cancelled = false;

        setLayout(new BorderLayout(10, 10));
        setSize(400, 400);
        setLocationRelativeTo(getParent());

        // 指令标签
        String instruction = requiredTypes.size() == 1
            ? "Select a target for this spell"
            : "Select " + requiredTypes.size() + " targets";
        JLabel instructionLabel = new JLabel(instruction, SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(instructionLabel, BorderLayout.NORTH);

        // 目标列表
        listModel = new DefaultListModel<>();
        targetList = new JList<>(listModel);
        targetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        targetList.setCellRenderer(new TargetCellRenderer());

        JScrollPane scrollPane = new JScrollPane(targetList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Targets"));
        add(scrollPane, BorderLayout.CENTER);

        // 已选目标标签
        JLabel selectedLabel = new JLabel("Selected: None", SwingConstants.CENTER);
        selectedLabel.setName("selectedLabel");
        add(selectedLabel, BorderLayout.EAST);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            confirmSelection();
            if (!cancelled) dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            cancelled = true;
            selectedTargets.clear();
            dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        populateTargets();
    }

    private void populateTargets() {
        listModel.clear();

        for (TargetType type : requiredTypes) {
            // 添加分隔符
            listModel.addElement(new TargetHeader(type.getDisplayName()));

            // 添加该类型的所有合法目标
            List<Object> targets = TargetSelector.getLegalTargets(game, type, caster);

            if (targets.isEmpty()) {
                listModel.addElement(new TargetHeader("  (No legal targets)"));
            } else {
                for (Object target : targets) {
                    listModel.addElement(new TargetWrapper(type, target));
                }
            }
        }
    }

    private void confirmSelection() {
        int[] selectedIndices = targetList.getSelectedIndices();

        if (selectedIndices.length < requiredTypes.size()) {
            JOptionPane.showMessageDialog(this,
                "Please select " + requiredTypes.size() + " target(s)",
                "Insufficient Targets",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 提取选中的目标（跳过标题行）
        selectedTargets.clear();
        int targetIndex = 0;

        for (int idx : selectedIndices) {
            Object item = listModel.getElementAt(idx);
            if (item instanceof TargetWrapper wrapper) {
                if (targetIndex < requiredTypes.size()) {
                    selectedTargets.add(new Target(requiredTypes.get(targetIndex), wrapper.target()));
                    targetIndex++;
                }
            }
        }
    }

    /**
     * 获取选择结果。
     */
    public List<Target> getSelectedTargets() {
        return new ArrayList<>(selectedTargets);
    }

    /**
     * 检查是否取消。
     */
    public boolean isCancelled() {
        return cancelled;
    }

    // ========== 内部类 ==========

    /**
     * 目标包装器。
     */
    private record TargetWrapper(TargetType type, Object target) {
        public String toString() {
            if (target instanceof Player p) {
                return "  " + p.getName() + " (Player)";
            } else if (target instanceof Card c) {
                return "  " + c.getName();
            }
            return "  " + target.toString();
        }
    }

    /**
     * 目标类型标题。
     */
    private static class TargetHeader {
        final String text;
        TargetHeader(String text) { this.text = text; }
        public String toString() { return text; }
    }

    /**
     * 目标列表单元格渲染器。
     */
    private class TargetCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                    int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof TargetHeader header) {
                setText(header.text);
                setFont(new Font("Arial", Font.BOLD, 12));
                setForeground(new Color(70, 130, 180));
                setBackground(list.getBackground());
            } else if (value instanceof TargetWrapper wrapper) {
                setText(wrapper.toString());

                Color typeColor = getColorForTargetType(wrapper.type());
                setForeground(typeColor);

                if (isSelected) {
                    setBackground(new Color(70, 130, 180));
                    setForeground(Color.WHITE);
                }
            }

            return this;
        }

        private Color getColorForTargetType(TargetType type) {
            return switch (type) {
                case PLAYER, OPPONENT -> new Color(100, 100, 180);
                case CREATURE, OPPONENT_CREATURE -> Color.ORANGE;
                case PERMANENT -> new Color(100, 100, 100);
                case ENCHANTMENT, AURA -> Color.MAGENTA;
                case ARTIFACT -> Color.CYAN;
                case LAND -> new Color(139, 69, 19);
                case PLANESWALKER -> Color.PINK;
                case BATTLE -> Color.YELLOW;
                default -> Color.BLACK;
            };
        }
    }
}
