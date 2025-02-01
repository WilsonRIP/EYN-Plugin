package com.example.eynplugin.craftbukkit;

import org.bukkit.entity.Player;

public class SetExpFix {
    
    public static void setTotalExperience(Player player, int exp) {
        if (player == null || exp < 0) {
            throw new IllegalArgumentException("Player cannot be null and experience must be positive");
        }
        
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int level = getLevelFromExp(exp);
        int remainingExp = exp - getExpToLevel(level);
        
        player.setLevel(level);
        player.setExp(remainingExp / (float) getExpAtLevel(level));
    }

    private static int getLevelFromExp(int exp) {
        if (exp <= 352) return (int) (Math.sqrt(exp + 9) - 3);
        if (exp <= 1507) return (exp + 38) / 5;
        return (exp + 158) / 9;
    }

    private static int getExpToLevel(int level) {
        if (level <= 0) return 0;
        return getExpAtLevel(level - 1);
    }

    private static int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    public static int getTotalExperience(Player player) {
        int exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        return exp;
    }

    public static int getExpUntilNextLevel(Player player) {
        int level = player.getLevel();
        return getExpAtLevel(level);
    }

    public static void setLevel(Player player, int level) {
        player.setLevel(level);
        player.setExp(0);
    }

    public static void addExperience(Player player, int exp) {
        setTotalExperience(player, getTotalExperience(player) + exp);
    }

    public static void removeExperience(Player player, int exp) {
        setTotalExperience(player, Math.max(0, getTotalExperience(player) - exp));
    }
} 