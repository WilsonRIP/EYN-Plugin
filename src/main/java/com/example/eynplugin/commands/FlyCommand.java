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
 * If an argument is provided and the player has permission for others,
 * it toggles the target's flight mode.
 */
public class FlyCommand extends BaseCommand {

    public FlyCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only players can use this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(color(getMessage("messages.player_only_command")));
            return true;
        }
        final Player player = (Player) sender;

        // Check permission to toggle flight.
        if (!Utils.checkPermission(player, "eyn.fly")) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }

        // If a target argument is provided and player has permission for others,
        // try toggling flight for the target.
        if (args.length > 0 && Utils.checkPermission(player, "eyn.fly.others")) {
            final Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                toggleFlight(target);
                player.sendMessage(color(getMessage("messages.fly.toggle_other").replace("%player%", target.getName())));
                return true;
            } else {
                player.sendMessage(color(getMessage("messages.error.player_not_found").replace("%player%", args[0])));
                return true;
            }
        }

        // Otherwise, toggle flight for the sender.
        toggleFlight(player);
        return true;
    }

    /**
     * Toggles the flight state of a player and sends them a status message.
     *
     * @param player the player whose flight mode is to be toggled.
     */
    private void toggleFlight(final Player player) {
        final boolean enableFlight = !player.getAllowFlight();
        player.setAllowFlight(enableFlight);
        player.setFlying(enableFlight);
        final String message = enableFlight ? getMessage("messages.fly.enabled") : getMessage("messages.fly.disabled");
        player.sendMessage(color(message));
    }

    /**
     * Translates color codes using Bukkit's ChatColor utility.
     *
     * @param message the message to colorize.
     * @return the colorized message.
     */
    private String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
