package com.example.eynplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Command handler for the /ping command.
 * <p>
 * Displays the ping of a player. Without arguments, a player's own ping is shown.
 * With a target argument, it displays the target's ping if the sender has permission.
 * </p>
 */
public class PingCommand extends BaseCommand {

    /**
     * Constructs a new PingCommand.
     *
     * @param messagesConfig the configuration file for messages.
     */
    public PingCommand(FileConfiguration messagesConfig) {
        super(messagesConfig);
    }

    /**
     * Processes the /ping command.
     * <p>
     * If no argument is provided and the sender is a player, displays the sender's own ping.
     * If a target is specified, checks permission and displays the target's ping.
     * If the command is run from the console with no arguments, an error is shown.
     * </p>
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    the command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments provided and the sender is not a player, show an error.
        if (args.length == 0 && !(sender instanceof Player)) {
            sendMessage(sender, "messages.ping.console_error");
            return true;
        }

        // If no arguments provided, display sender's own ping.
        if (args.length == 0) {
            final Player player = (Player) sender;
            displayPing(sender, player);
            return true;
        }

        // For arguments provided, sender must have permission to check others' ping.
        if (!sender.hasPermission("eyn.ping.others")) {
            sendMessage(sender, "messages.no_permission");
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "messages.error.player_not_found");
            return true;
        }

        displayPing(sender, target);
        return true;
    }

    /**
     * Displays the ping of the target player to the sender with a color-coded indicator.
     *
     * @param sender the command sender.
     * @param target the player whose ping is to be displayed.
     */
    private void displayPing(CommandSender sender, Player target) {
        final int ping = target.getPing();
        final String quality;

        if (ping < 50) {
            quality = ChatColor.GREEN.toString();        // Excellent
        } else if (ping < 100) {
            quality = ChatColor.DARK_GREEN.toString();     // Good
        } else if (ping < 150) {
            quality = ChatColor.YELLOW.toString();         // Okay
        } else if (ping < 200) {
            quality = ChatColor.GOLD.toString();           // Mediocre
        } else {
            quality = ChatColor.RED.toString();            // Poor
        }

        final String pingText = quality + ping + "ms";

        if (sender.equals(target)) {
            sendMessage(sender, "messages.ping.self", "%ping%", pingText);
        } else {
            sendMessage(sender, "messages.ping.other",
                    "%player%", target.getName(),
                    "%ping%", pingText);
        }
    }

    /**
     * Provides tab completion suggestions for the /ping command.
     * <p>
     * If the sender has permission to check others' ping, returns a list of online player names
     * that start with the provided argument. Otherwise, returns an empty list.
     * </p>
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param alias   the alias used.
     * @param args    the current command arguments.
     * @return a list of matching suggestions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("eyn.ping.others")) {
            return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }
}
