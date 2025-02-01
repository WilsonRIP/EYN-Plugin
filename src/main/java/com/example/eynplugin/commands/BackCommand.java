package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.UUID;
import java.util.Collections;
import java.util.List;

public class BackCommand extends BaseCommand {

    private final HashMap<UUID, Location> lastLocations = new HashMap<>();

    public BackCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.back")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        Location lastLocation = lastLocations.get(player.getUniqueId());
        if (lastLocation != null) {
            player.teleport(lastLocation);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("back.teleported")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("back.no_location")));
        }
        return true;
    }

    public void setLastLocation(Player player, Location location) {
        lastLocations.put(player.getUniqueId(), location);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
} 