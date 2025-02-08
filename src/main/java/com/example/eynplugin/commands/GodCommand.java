package com.example.eynplugin.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.example.eynplugin.api.LuckPermsHandler;

/**
 * Handles the /god command, allowing players to toggle god mode.
 * If a target is specified and the sender has permission to modify others,
 * the target's god mode will be toggled.
 */
public class GodCommand extends BaseCommand {

    // Stores the UUIDs of players with god mode enabled.
    private final Set<UUID> godModePlayers = new HashSet<>();

    /**
     * Constructs a new GodCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file containing messages.
     */
    public GodCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Processes the /god command.
     * If a target argument is provided and the sender has the "eyn.god.others" permission,
     * toggles god mode for the target player. Otherwise, toggles god mode for the sender.
     *
     * @param sender  the command sender.
     * @param command the command executed.
     * @param label   the alias used.
     * @param args    command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // Only players can use this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(translate("messages.player_only_command"));
            return true;
        }
        final Player player = (Player) sender;

        // Check permission for god mode.
        if (!checkPermission(player, "eyn.god")) {
            player.sendMessage(translate("messages.no_permission"));
            return true;
        }

        // If a target player's name is provided, attempt to toggle their god mode.
        if (args.length > 0) {
            if (!checkPermission(player, "eyn.god.others")) {
                player.sendMessage(translate("messages.no_permission"));
                return true;
            }
            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(translate("messages.player_not_found").replace("%player%", args[0]));
                return true;
            }
            toggleGodMode(target);
            player.sendMessage(translate("messages.god.toggle_other").replace("%player%", target.getName()));
            // Optionally notify the target if they are not the sender.
            if (!player.equals(target)) {
                target.sendMessage(translate("messages.god.toggle_self"));
            }
            return true;
        }

        // Toggle god mode for the sender.
        toggleGodMode(player);
        return true;
    }

    /**
     * Toggles the god mode state for a player.
     *
     * @param player the player whose god mode is to be toggled.
     */
    private void toggleGodMode(final Player player) {
        final UUID playerUUID = player.getUniqueId();
        if (godModePlayers.contains(playerUUID)) {
            godModePlayers.remove(playerUUID);
            player.sendMessage(translate("messages.god.disabled"));
        } else {
            godModePlayers.add(playerUUID);
            player.sendMessage(translate("messages.god.enabled"));
        }
    }

    /**
     * Checks if a player currently has god mode enabled.
     *
     * @param player the player to check.
     * @return true if god mode is enabled; false otherwise.
     */
    public boolean isGodMode(final Player player) {
        return godModePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return Collections.emptyList();
    }

    /**
     * Translates and colorizes a message from the messages configuration.
     *
     * @param key the configuration key for the message.
     * @return the colorized message string.
     */
    protected String translate(final String key) {
        return ChatColor.translateAlternateColorCodes('&', getMessage(key));
    }
}
