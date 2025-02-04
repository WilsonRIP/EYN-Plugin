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

    /**
     * Constructs a new PlaytimeCommand.
     *
     * @param luckPermsHandler The LuckPerms handler.
     * @param messagesConfig   The configuration file for messages.
     */
    public PlaytimeCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the playtime command.
     * <p>
     * Usage: /playtime [player]
     * If no target is provided, it displays the playtime of the sender.
     * </p>
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias used.
     * @param args    Command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getMessage("player_only_command")));
            return true;
        }
        final Player player = (Player) sender;

        // Check permission.
        if (!Utils.checkPermission(player, "eyn.playtime")) {
            player.sendMessage(colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Determine target: either the provided player argument or self.
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

        // Display playtime information.
        showPlaytime(player, target);
        return true;
    }

    /**
     * Displays the playtime of the target player to the sender.
     *
     * @param sender The player who requested the playtime information.
     * @param target The player whose playtime is being displayed.
     */
    private void showPlaytime(final Player sender, final Player target) {
        // Retrieve total playtime in ticks and convert to minutes.
        final long playTicks = target.getStatistic(Statistic.PLAY_ONE_MINUTE);
        final long totalMinutes = playTicks / 20 / 60;
        final long hours = totalMinutes / 60;
        final long minutes = totalMinutes % 60;

        if (sender.equals(target)) {
            sendMessage(sender, "messages.playtime.self", "%hours%", String.valueOf(hours), "%minutes%", String.valueOf(minutes));
        } else {
            sendMessage(sender, "messages.playtime.other", "%player%", target.getName(), "%hours%", String.valueOf(hours), "%minutes%", String.valueOf(minutes));
        }
    }

    /**
     * Retrieves a message from the configuration, replaces placeholders, colorizes it, and sends it to the player.
     *
     * @param player       The player to receive the message.
     * @param messageKey   The key used to fetch the base message.
     * @param placeholders An array of placeholder keys and their corresponding replacements.
     */
    private void sendMessage(final Player player, final String messageKey, final String... placeholders) {
        String message = getMessage(messageKey);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        player.sendMessage(colorize(message));
    }

    /**
     * Translates alternate color codes in the provided message.
     *
     * @param message The raw message.
     * @return The colorized message.
     */
    private String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
