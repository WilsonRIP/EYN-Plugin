package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.UUID;
import java.util.Collections;
import java.util.List;

public class GodCommand extends BaseCommand {

    private final HashSet<UUID> godModePlayers = new HashSet<>();

    public GodCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.god")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.no_permission")));
            return true;
        }

        if (args.length > 0 && checkPermission(player, "eyn.god.others")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                toggleGodMode(target);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    getMessage("messages.god.toggle_other").replace("%player%", target.getName())));
                return true;
            }
        }

        toggleGodMode(player);
        return true;
    }

    private void toggleGodMode(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (godModePlayers.contains(playerUUID)) {
            godModePlayers.remove(playerUUID);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.god.disabled")));
        } else {
            godModePlayers.add(playerUUID);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.god.enabled")));
        }
    }

    public boolean isGodMode(Player player) {
        return godModePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
} 