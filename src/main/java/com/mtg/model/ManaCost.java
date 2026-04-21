package com.mtg.model;

import java.util.ArrayList;
import java.util.List;

public class ManaCost {
    private final int genericMana;  // From numbers (e.g., 2 in {2}{U})
    private final List<ManaType> coloredMana;  // Specific colored: W, U, B, R, G
    private final int colorlessMana;  // From C symbol (must be paid with colorless)

    public ManaCost() {
        this.genericMana = 0;
        this.coloredMana = new ArrayList<>();
        this.colorlessMana = 0;
    }

    private ManaCost(int generic, List<ManaType> colored, int colorless) {
        this.genericMana = generic;
        this.coloredMana = new ArrayList<>(colored);
        this.colorlessMana = colorless;
    }

    public static ManaCost of(ManaType... types) {
        List<ManaType> colored = new ArrayList<>();
        int generic = 0;
        int colorless = 0;

        for (ManaType type : types) {
            if (type == ManaType.COLORLESS) {
                generic++;
            } else {
                colored.add(type);
            }
        }
        return new ManaCost(generic, colored, colorless);
    }

    public static ManaCost of(int generic) {
        return new ManaCost(generic, new ArrayList<>(), 0);
    }

    public static ManaCost of(int generic, ManaType colored) {
        List<ManaType> coloredList = new ArrayList<>();
        coloredList.add(colored);
        return new ManaCost(generic, coloredList, 0);
    }

    public static ManaCost of(int generic, ManaType colored1, ManaType colored2) {
        List<ManaType> coloredList = new ArrayList<>();
        coloredList.add(colored1);
        coloredList.add(colored2);
        return new ManaCost(generic, coloredList, 0);
    }

    public static ManaCost parse(String cost) {
        int generic = 0;
        List<ManaType> colored = new ArrayList<>();
        int colorless = 0;

        for (char c : cost.toUpperCase().toCharArray()) {
            switch (c) {
                case 'W' -> colored.add(ManaType.WHITE);
                case 'U' -> colored.add(ManaType.BLUE);
                case 'B' -> colored.add(ManaType.BLACK);
                case 'R' -> colored.add(ManaType.RED);
                case 'G' -> colored.add(ManaType.GREEN);
                case 'C' -> colorless++;
                default -> {
                    if (Character.isDigit(c)) {
                        generic += Character.getNumericValue(c);
                    }
                }
            }
        }
        return new ManaCost(generic, colored, colorless);
    }

    public int getGenericMana() {
        return genericMana;
    }

    public List<ManaType> getColoredMana() {
        return new ArrayList<>(coloredMana);
    }

    public int getColorlessMana() {
        return colorlessMana;
    }

    public int getTotalMana() {
        return genericMana + coloredMana.size() + colorlessMana;
    }

    public List<ManaType> getMana() {
        List<ManaType> all = new ArrayList<>();
        for (int i = 0; i < genericMana; i++) all.add(ManaType.COLORLESS);
        all.addAll(coloredMana);
        for (int i = 0; i < colorlessMana; i++) all.add(ManaType.COLORLESS);
        return all;
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (genericMana > 0) {
            sb.append("{").append(genericMana).append("}");
        }
        for (ManaType m : coloredMana) {
            sb.append("{").append(m.getSymbol()).append("}");
        }
        for (int i = 0; i < colorlessMana; i++) {
            sb.append("{C}");
        }
        return sb.toString();
    }

    /**
     * Check if this cost can be paid with the available mana.
     * Rules:
     * - Colored mana (W/U/B/R/G): Must be paid with exact color
     * - Colorless mana (C): Must be paid with colorless mana only
     * - Generic mana (numbers): Can be paid with any mana
     */
    public boolean canPayWith(List<ManaType> available) {
        List<ManaType> pool = new ArrayList<>(available);

        // Step 1: Pay colored mana requirements
        List<ManaType> neededColored = new ArrayList<>(coloredMana);
        for (ManaType needed : neededColored) {
            boolean found = false;
            for (int i = 0; i < pool.size(); i++) {
                if (pool.get(i) == needed) {
                    pool.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false; // Cannot pay colored requirement
        }

        // Step 2: Pay colorless mana requirements (C symbol)
        // Must use colorless mana only
        for (int i = 0; i < colorlessMana; i++) {
            boolean found = false;
            for (int j = 0; j < pool.size(); j++) {
                if (pool.get(j) == ManaType.COLORLESS) {
                    pool.remove(j);
                    found = true;
                    break;
                }
            }
            if (!found) return false; // Cannot pay colorless requirement
        }

        // Step 3: Pay generic mana (numbers)
        // Can use any mana (colored or colorless)
        for (int i = 0; i < genericMana; i++) {
            if (pool.isEmpty()) return false;
            pool.remove(0); // Use any available mana
        }

        return true;
    }

    /**
     * Pay this cost using the available mana pool.
     * Returns the remaining mana after payment.
     */
    public List<ManaType> payMana(List<ManaType> available) {
        List<ManaType> pool = new ArrayList<>(available);
        List<ManaType> used = new ArrayList<>();

        // Step 1: Pay colored mana requirements
        for (ManaType needed : new ArrayList<>(coloredMana)) {
            for (int i = 0; i < pool.size(); i++) {
                if (pool.get(i) == needed) {
                    used.add(pool.remove(i));
                    break;
                }
            }
        }

        // Step 2: Pay colorless mana requirements (C symbol)
        for (int i = 0; i < colorlessMana; i++) {
            for (int j = 0; j < pool.size(); j++) {
                if (pool.get(j) == ManaType.COLORLESS) {
                    used.add(pool.remove(j));
                    break;
                }
            }
        }

        // Step 3: Pay generic mana with remaining
        for (int i = 0; i < genericMana && !pool.isEmpty(); i++) {
            used.add(pool.remove(0));
        }

        return pool; // Return remaining mana
    }
}
