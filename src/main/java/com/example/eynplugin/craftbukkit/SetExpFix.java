package com.example.eynplugin.craftbukkit;

import org.bukkit.entity.Player;

/**
 * Utility class to correctly set and manage a player's experience.
 */
public class SetExpFix {

    /**
     * Sets the player's total experience points, recalculating the level and progress accordingly.
     *
     * @param player the player whose experience is to be set
     * @param exp    the total experience points to set; must be non-negative
     * @throws IllegalArgumentException if player is null or exp is negative
     */
    public static void setTotalExperience(final Player player, final int exp) {
        if (player == null || exp < 0) {
            throw new IllegalArgumentException("Player cannot be null and experience must be non-negative");
        }

        // Reset player's experience and level.
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        // Determine the player's level by iterating until the cumulative experience for the next level exceeds exp.
        int level = 0;
        while (getCumulativeExpForLevel(level + 1) <= exp) {
            level++;
        }
        final int xpAtCurrentLevelStart = getCumulativeExpForLevel(level);
        final int xpForNextLevel = getExpAtLevel(level);
        final int remainingExp = exp - xpAtCurrentLevelStart;

        // Set player's level and fractional progress toward the next level.
        player.setLevel(level);
        player.setExp(remainingExp / (float) xpForNextLevel);
    }

    /**
     * Calculates the cumulative experience required to reach a given level.
     *
     * @param level the level for which to calculate the total required experience
     * @return the total experience required to reach the given level
     */
    private static int getCumulativeExpForLevel(final int level) {
        int total = 0;
        for (int i = 0; i < level; i++) {
            total += getExpAtLevel(i);
        }
        return total;
    }

    /**
     * Calculates the experience required to advance from the given level to the next level.
     * <p>
     * Formula used:
     * <ul>
     *   <li>Levels 0–15: 2 * level + 7</li>
     *   <li>Levels 16–30: 5 * level - 38</li>
     *   <li>Levels 31+: 9 * level - 158</li>
     * </ul>
     *
     * @param level the current level
     * @return the experience required to advance from the given level to the next level
     */
    private static int getExpAtLevel(final int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    /**
     * Returns the player's total accumulated experience points.
     *
     * @param player the player whose total experience is calculated
     * @return the total experience points of the player
     */
    public static int getTotalExperience(final Player player) {
        final int level = player.getLevel();
        final int xpInCurrentLevel = Math.round(getExpAtLevel(level) * player.getExp());
        return getCumulativeExpForLevel(level) + xpInCurrentLevel;
    }

    /**
     * Returns the experience required to reach the next level from the player's current level.
     *
     * @param player the player whose next level experience requirement is calculated
     * @return the experience required to reach the next level
     */
    public static int getExpUntilNextLevel(final Player player) {
        return getExpAtLevel(player.getLevel());
    }

    /**
     * Sets the player's level to the given value and resets their experience progress.
     *
     * @param player the player whose level is to be set
     * @param level  the level to set
     */
    public static void setLevel(final Player player, final int level) {
        player.setLevel(level);
        player.setExp(0);
    }

    /**
     * Adds the specified amount of experience points to the player.
     *
     * @param player the player to whom experience will be added
     * @param exp    the amount of experience points to add
     */
    public static void addExperience(final Player player, final int exp) {
        setTotalExperience(player, getTotalExperience(player) + exp);
    }

    /**
     * Removes the specified amount of experience points from the player.
     * The player's experience will not drop below zero.
     *
     * @param player the player from whom experience will be removed
     * @param exp    the amount of experience points to remove
     */
    public static void removeExperience(final Player player, final int exp) {
        setTotalExperience(player, Math.max(0, getTotalExperience(player) - exp));
    }
}
