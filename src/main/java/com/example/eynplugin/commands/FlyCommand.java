package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /fly command, allowing players to toggle flight mode.
 * <p>
 * If a target argument is provided and the player has permission for others,
 * it toggles the target's flight mode; otherwise, it toggles the sender's flight mode.
 * </p>
 */
public class FlyCommand extends BaseCommand {

    /**
     * Constructs a new FlyCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file for messages.
     */
    public FlyCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Processes the /fly command.
     *
     * @param sender  the command sender.
     * @param command the command executed.
     * @param label   the alias used.
     * @param args    command arguments; if provided, the first argument is the target player's name.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // Ensure that only players can use this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(color(getMessage("messages.player_only_command")));
            return true;
        }
        final Player player = (Player) sender;

        // Check if the sender has permission to toggle flight.
        if (!Utils.checkPermission(player, "eyn.fly")) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }

        // Check for a target argument and permission to modify others.
        if (args.length > 0 && Utils.checkPermission(player, "eyn.fly.others")) {
            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(color(getMessage("messages.error.player_not_found").replace("%player%", args[0])));
                return true;
            }
            toggleFlight(target);
            player.sendMessage(color(getMessage("messages.fly.toggle_other").replace("%player%", target.getName())));
            return true;
        }

        // Otherwise, toggle flight mode for the sender.
        toggleFlight(player);
        return true;
    }

    /**
     * Toggles the flight state for the specified player.
     *
     * @param player the player whose flight mode will be toggled.
     */
    private void toggleFlight(final Player player) {
        final boolean enableFlight = !player.getAllowFlight();
        player.setAllowFlight(enableFlight);
        player.setFlying(enableFlight);

        final String message = enableFlight ? getMessage("messages.fly.enabled") : getMessage("messages.fly.disabled");
        player.sendMessage(color(message));
    }

    /**
     * Applies alternate color codes to the given message.
     *
     * @param message the message to colorize.
     * @return the colorized message.
     */
    private String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
