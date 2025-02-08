package com.example.eynplugin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.example.eynplugin.api.LuckPermsHandler;

/**
 * BaseCommand provides common functionality for all commands in the EYN Plugin.
 * It implements CommandExecutor and TabCompleter to handle command execution and tab completion.
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final FileConfiguration messagesConfig;
    protected final LuckPermsHandler luckPermsHandler;

    /**
     * Constructs a BaseCommand with LuckPerms support.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration for messages.
     */
    public BaseCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        this.luckPermsHandler = luckPermsHandler;
        this.messagesConfig = messagesConfig;
    }

    /**
     * Constructs a BaseCommand without LuckPerms support.
     *
     * @param messagesConfig the configuration for messages.
     */
    public BaseCommand(final FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
        this.luckPermsHandler = null;
    }

    @Override
    public abstract boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args);

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return new ArrayList<>();
    }

    /**
     * Checks whether the sender has the specified permission.
     * If not, sends a "no permission" message.
     *
     * @param sender     the command sender.
     * @param permission the permission to check.
     * @return true if the sender has the permission; false otherwise.
     */
    protected boolean checkPermission(final CommandSender sender, final String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sendMessage(sender, "messages.no_permission");
        return false;
    }

    /**
     * Checks whether the sender is a player.
     * If not, sends a "player only" message.
     *
     * @param sender the command sender.
     * @return true if the sender is a player; false otherwise.
     */
    protected boolean checkPlayer(final CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }
        sendMessage(sender, "messages.player_only_command");
        return false;
    }

    /**
     * Retrieves a target player by name.
     * If the player is not found, sends a "player not found" message.
     *
     * @param sender the command sender.
     * @param name   the target player's name.
     * @return the target Player if found; null otherwise.
     */
    protected Player getTarget(final CommandSender sender, final String name) {
        final Player target = Bukkit.getPlayer(name);
        if (target == null) {
            sendMessage(sender, "messages.error.player_not_found");
            return null;
        }
        return target;
    }

    /**
     * Sends a formatted and colorized message to the specified sender.
     *
     * @param sender   the recipient of the message.
     * @param key      the configuration key for the message.
     */
    protected void sendMessage(final CommandSender sender, final String key) {
        sender.sendMessage(formatMessage(key));
    }

    /**
     * Sends a formatted and colorized message to the specified sender with placeholder replacement.
     * Placeholders must be provided in key-value pairs.
     *
     * @param sender       the recipient of the message.
     * @param key          the configuration key for the message.
     * @param replacements key-value pairs for placeholder replacement.
     * @throws IllegalArgumentException if an odd number of replacements is provided.
     */
    protected void sendMessage(final CommandSender sender, final String key, final String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be in pairs");
        }
        String message = formatMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(message);
    }

    /**
     * Broadcasts a formatted and colorized message to all players with placeholder replacement.
     *
     * @param key          the configuration key for the message.
     * @param replacements key-value pairs for placeholder replacement.
     * @throws IllegalArgumentException if an odd number of replacements is provided.
     */
    protected void broadcastMessage(final String key, final String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be in pairs");
        }
        String message = formatMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        Bukkit.broadcastMessage(message);
    }

    /**
     * Retrieves a message from the configuration and translates alternate color codes.
     *
     * @param key the configuration key for the message.
     * @return the formatted message.
     */
    protected String formatMessage(final String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            message = "&cMessage not found: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Returns a list of online player names.
     *
     * @return a list of online player names.
     */
    protected List<String> getOnlinePlayerNames() {
        return Arrays.asList("Online: " + Integer.toString(Bukkit.getOnlinePlayers().size()));
    }

    /**
     * Filters the provided list, returning only those strings that start with the given prefix.
     *
     * @param list   the list of strings to filter.
     * @param prefix the prefix to filter by.
     * @return a filtered list of strings.
     */
    protected List<String> filterStartingWith(final List<String> list, final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return list;
        }
        final String lowercasePrefix = prefix.toLowerCase();
        return list.stream()
                .filter(str -> str.toLowerCase().startsWith(lowercasePrefix))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the given string can be parsed as an integer.
     *
     * @param str the string to check.
     * @return true if the string represents an integer; false otherwise.
     */
    protected boolean isInteger(final String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the given string can be parsed as a positive integer.
     *
     * @param str the string to check.
     * @return true if the string represents a positive integer; false otherwise.
     */
    protected boolean isPositiveInteger(final String str) {
        try {
            return Integer.parseInt(str) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Retrieves a raw message from the configuration.
     *
     * @param key the configuration key for the message.
     * @return the raw message string.
     */
    protected String getMessage(final String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            message = "Message not found: " + key;
        }
        return message;
    }

    protected int parseInteger(final String input, final int defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }
}
