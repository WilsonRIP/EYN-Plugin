package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Statistic;

public class PlaytimeCommand extends BaseCommand {

    public PlaytimeCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getMessage("player_only_command")));
            return true;
        }
        final Player player = (Player) sender;

        // Check permission for playtime command.
        if (!Utils.checkPermission(player, "eyn.playtime")) {
            player.sendMessage(colorize(getMessage("messages.no_permission")));
            return true;
        }

        final Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(player, "messages.playtime.no_data", "%player%", args[0]);
                return true;
            }
        } else {
            target = player;
        }

        showPlaytime(player, target);
        return true;
    }

    /**
     * Displays the playtime of the target player.
     *
     * @param sender The player who requested the playtime information.
     * @param target The player whose playtime will be displayed.
     */
    private void showPlaytime(Player sender, Player target) {
        // Convert ticks to minutes: ticks / 20 ticks per second / 60 seconds per minute.
        final long totalMinutes = target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
        final long hours = totalMinutes / 60;
        final long minutes = totalMinutes % 60;

        if (sender.equals(target)) {
            sendMessage(sender, "messages.playtime.self", 
                        "%hours%", String.valueOf(hours), 
                        "%minutes%", String.valueOf(minutes));
        } else {
            sendMessage(sender, "messages.playtime.other", 
                        "%player%", target.getName(), 
                        "%hours%", String.valueOf(hours), 
                        "%minutes%", String.valueOf(minutes));
        }
    }

    /**
     * Retrieves, replaces placeholders, colorizes, and sends a message to a player.
     *
     * @param player The player to receive the message.
     * @param messageKey The key to fetch the base message.
     * @param placeholders Variable-length array containing placeholder keys and values.
     */
    private void sendMessage(Player player, String messageKey, String... placeholders) {
        String message = getMessage(messageKey);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        player.sendMessage(colorize(message));
    }

    /**
     * Translates alternate color codes in the provided message.
     *
     * @param message The message to colorize.
     * @return The colorized message.
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
