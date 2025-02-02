package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GodCommand extends BaseCommand {

    private final Set<UUID> godModePlayers = new HashSet<>();

    public GodCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(translate("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.god")) {
            player.sendMessage(translate("messages.no_permission"));
            return true;
        }

        // If a target player's name is provided, attempt to toggle their god mode
        if (args.length > 0) {
            if (!checkPermission(player, "eyn.god.others")) {
                player.sendMessage(translate("messages.no_permission"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(translate("messages.player_not_found").replace("%player%", args[0]));
                return true;
            }
            toggleGodMode(target);
            player.sendMessage(translate("messages.god.toggle_other").replace("%player%", target.getName()));
            
            // Optionally notify the target if they are not the same as the sender
            if (!player.equals(target)) {
                target.sendMessage(translate("messages.god.toggle_self"));
            }
            return true;
        }

        // Toggle god mode for the sender if no target is provided
        toggleGodMode(player);
        return true;
    }

    private void toggleGodMode(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (godModePlayers.contains(playerUUID)) {
            godModePlayers.remove(playerUUID);
            player.sendMessage(translate("messages.god.disabled"));
        } else {
            godModePlayers.add(playerUUID);
            player.sendMessage(translate("messages.god.enabled"));
        }
    }

    public boolean isGodMode(Player player) {
        return godModePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Translates a message from the configuration by applying alternate color codes.
     *
     * @param key The key to retrieve the message.
     * @return The translated message string.
     */
    private String translate(String key) {
        return ChatColor.translateAlternateColorCodes('&', getMessage(key));
    }
}
