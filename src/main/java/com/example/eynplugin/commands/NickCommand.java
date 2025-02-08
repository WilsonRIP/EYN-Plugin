package com.example.eynplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Handles the /nick command, allowing players to change or reset their nickname.
 * <p>
 * Usage:
 *   /nick reset                  - Resets the player's nickname.
 *   /nick <nickname>             - Changes the player's own nickname.
 *   /nick <player> <nickname>    - Changes another player's nickname.
 * </p>
 */
public class NickCommand implements CommandExecutor {
    private final FileConfiguration messagesConfig;

    /**
     * Constructs a new NickCommand.
     *
     * @param messagesConfig the configuration file for messages.
     */
    public NickCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    /**
     * Processes the /nick command.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    the command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure that only players can use this command.
        if (!(sender instanceof Player)) {
            String message = formatMessage("messages.nick.console_error");
            if (message != null) {
                sender.sendMessage(message);
            } else {
                sender.sendMessage("messages.nick.console_error");
            }
            return true;
        }
        final Player player = (Player) sender;

        // Validate the argument count.
        if (args.length == 0 || args.length > 2) {
            player.sendMessage(formatMessage("messages.nick.usage"));
            return true;
        }

        // Handle a single argument command.
        if (args.length == 1) {
            // "reset" will reset the player's nickname.
            if (args[0].equalsIgnoreCase("reset")) {
                if (!player.hasPermission("eyn.nick.reset")) {
                    player.sendMessage(formatMessage("messages.no_permission"));
                    return true;
                }
                resetNickname(player);
            } else {
                // Otherwise, change the player's own nickname.
                if (!player.hasPermission("eyn.nick.self")) {
                    player.sendMessage(formatMessage("messages.no_permission"));
                    return true;
                }
                setNickname(player, args[0], false);
            }
            return true;
        }

        // Handle two-argument command: change another player's nickname.
        if (args.length == 2) {
            if (!player.hasPermission("eyn.nick.others")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                return true;
            }
            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(formatMessage("messages.moderation.player_not_found"));
                return true;
            }
            setNickname(target, args[1], true);
            String targetName = (target.getName() != null) ? target.getName() : "Unknown";
            player.sendMessage(formatMessage("messages.nick.changed_other")
                    .replace("%player%", targetName)
                    .replace("%nickname%", args[1]));
        }
        return true;
    }

    /**
     * Resets the player's nickname to their default name.
     *
     * @param player the player whose nickname is being reset.
     */
    private void resetNickname(final Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        player.sendMessage(formatMessage("messages.nick.reset"));
    }

    /**
     * Sets the nickname for a player.
     *
     * @param player   the player whose nickname is being set.
     * @param nickname the new nickname (supports color codes).
     * @param isOther  true if setting the nickname for another player.
     */
    private void setNickname(final Player player, final String nickname, final boolean isOther) {
        final String coloredNick = ChatColor.translateAlternateColorCodes('&', nickname);
        player.setDisplayName(coloredNick);
        player.setPlayerListName(coloredNick);
        if (!isOther) {
            player.sendMessage(formatMessage("messages.nick.changed_self")
                    .replace("%nickname%", nickname));
        }
    }

    /**
     * Retrieves and formats a message from the configuration, applying color codes.
     *
     * @param key the configuration key for the message.
     * @return the formatted message string.
     */
    private String formatMessage(final String key) {
        return ChatColor.translateAlternateColorCodes('&', 
                messagesConfig.getString(key, "&cMessage not found: " + key));
    }
}
