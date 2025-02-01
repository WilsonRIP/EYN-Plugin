package com.example.eynplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing common functionality for the EYN Plugin.
 */
public final class Utils {
    private static final String PLUGIN_PREFIX = "[EYN] ";
    private static final Logger LOGGER = Logger.getLogger("EYNPlugin");

    private Utils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Formats a message with the plugin prefix and optional arguments.
     *
     * @param message The message to format
     * @param args Optional arguments for string formatting
     * @return Formatted message with plugin prefix
     * @throws IllegalArgumentException if message format is invalid
     */
    public static String formatMessage(String message, Object... args) {
        if (message == null) {
            LOGGER.warning("Attempted to format null message");
            return PLUGIN_PREFIX;
        }
        try {
            return PLUGIN_PREFIX + String.format(message, args);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error formatting message: " + message, e);
            return PLUGIN_PREFIX + message;
        }
    }

    /**
     * Checks if a player has a specific permission.
     *
     * @param player The player to check
     * @param permission The permission to check for
     * @return true if player has permission, false otherwise
     */
    public static boolean hasPermission(Player player, String permission) {
        if (player == null) {
            LOGGER.warning("Attempted to check permission for null player");
            return false;
        }
        if (permission == null || permission.isEmpty()) {
            LOGGER.warning("Attempted to check null or empty permission");
            return false;
        }
        return player.hasPermission(permission);
    }

    /**
     * Checks if a player has a specific permission.
     * 
     * @param player The player to check
     * @param permission The permission to check for
     * @return true if player has permission, false otherwise
     */
    public static boolean checkPermission(Player player, String permission) {
        if (player == null) return false;
        return player.hasPermission(permission);
    }

    /**
     * Converts color codes in a string to Minecraft colors.
     *
     * @param text The text to colorize
     * @return Colorized text
     */
    public static String colorize(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Safely sends a message to a player with color code translation.
     *
     * @param player The player to send the message to
     * @param message The message to send
     */
    public static void sendMessage(Player player, String message) {
        if (player != null && message != null && player.isOnline()) {
            player.sendMessage(colorize(message));
        }
    }

    /**
     * Validates if a string is null or empty.
     *
     * @param str The string to check
     * @return true if the string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Safely gets a player's name, returning "Unknown" if the player is null.
     *
     * @param player The player
     * @return The player's name or "Unknown" if null
     */
    public static String getPlayerName(Player player) {
        return player != null ? player.getName() : "Unknown";
    }
}