package com.example.eynplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing common functionality for the EYN Plugin.
 * This class contains methods for formatting messages, checking permissions,
 * colorizing text, and safely sending messages to players.
 */
public final class Utils {
    private static final String PLUGIN_PREFIX = "[EYN] ";
    private static final Logger LOGGER = Logger.getLogger("EYNPlugin");

    // Private constructor to prevent instantiation.
    private Utils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Formats a message with the plugin prefix using the provided arguments.
     *
     * @param message the message format string.
     * @param args    optional arguments for String.format.
     * @return the formatted message with the plugin prefix.
     */
    public static String formatMessage(final String message, final Object... args) {
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
     * Checks whether the specified player has the given permission.
     * Logs a warning if the player is null or the permission string is invalid.
     *
     * @param player     the player to check.
     * @param permission the permission to check.
     * @return true if the player has the permission; false otherwise.
     */
    public static boolean hasPermission(final Player player, final String permission) {
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
     * Shorthand method to check whether the specified player has the given permission.
     *
     * @param player     the player to check.
     * @param permission the permission to check.
     * @return true if the player has the permission; false otherwise.
     */
    public static boolean checkPermission(final Player player, final String permission) {
        return hasPermission(player, permission);
    }

    /**
     * Converts alternate color codes in the given text to Minecraft color codes.
     *
     * @param text the text to colorize.
     * @return the colorized text; returns an empty string if the input is null.
     */
    public static String colorize(final String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Safely sends a message to the specified player, applying color codes.
     *
     * @param player  the player to send the message to.
     * @param message the message to send.
     */
    public static void sendMessage(final Player player, final String message) {
        if (player != null && message != null && player.isOnline()) {
            player.sendMessage(colorize(message));
        }
    }

    /**
     * Checks if the provided string is null or empty after trimming.
     *
     * @param str the string to check.
     * @return true if the string is null or empty; false otherwise.
     */
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Safely returns a player's name.
     *
     * @param player the player.
     * @return the player's name, or "Unknown" if the player is null.
     */
    public static String getPlayerName(final Player player) {
        return player != null ? player.getName() : "Unknown";
    }
}
