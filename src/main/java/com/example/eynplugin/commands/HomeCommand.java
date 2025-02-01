package com.example.eynplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.storage.HomeManager;

public class HomeCommand implements CommandExecutor {
    private final HomeManager homeManager;
    private final FileConfiguration config;
    private final FileConfiguration messagesConfig;

    public HomeCommand(HomeManager homeManager, FileConfiguration config, FileConfiguration messagesConfig) {
        this.homeManager = homeManager;
        this.config = config;
        this.messagesConfig = messagesConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(formatMessage("messages.home.usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "set":
            case "sethome":
                if (args.length < 2) {
                    player.sendMessage(formatMessage("messages.home.sethome.usage"));
                    return true;
                }
                setHome(player, args[1]);
                break;
            case "del":
            case "delete":
            case "delhome":
                if (args.length < 2) {
                    player.sendMessage(formatMessage("messages.home.delhome.usage"));
                    return true;
                }
                deleteHome(player, args[1]);
                break;
            case "rename":
            case "renamehome":
                if (args.length < 3) {
                    player.sendMessage(formatMessage("messages.home.rename.usage"));
                    return true;
                }
                renameHome(player, args[1], args[2]);
                break;
            default:
                teleportToHome(player, args[0]);
                break;
        }
        return true;
    }

    private void setHome(Player player, String homeName) {
        if (!player.hasPermission("eyn.home.set")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return;
        }

        int maxHomes = getMaxHomes(player);
        if (homeManager.getHomeCount(player.getUniqueId()) >= maxHomes && !player.hasPermission("eyn.home.unlimited")) {
            player.sendMessage(formatMessage("messages.home.sethome.limit").replace("%max%", String.valueOf(maxHomes)));
            return;
        }

        homeManager.setHome(player.getUniqueId(), homeName, player.getLocation());
        player.sendMessage(formatMessage("messages.home.sethome.success").replace("%name%", homeName));
    }

    private void deleteHome(Player player, String homeName) {
        if (!player.hasPermission("eyn.home.delete")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return;
        }

        if (!homeManager.deleteHome(player.getUniqueId(), homeName)) {
            player.sendMessage(formatMessage("messages.home.not_found"));
            return;
        }

        player.sendMessage(formatMessage("messages.home.delhome.success").replace("%name%", homeName));
    }

    private void renameHome(Player player, String oldName, String newName) {
        if (!player.hasPermission("eyn.home.rename")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return;
        }

        if (!homeManager.renameHome(player.getUniqueId(), oldName, newName)) {
            player.sendMessage(formatMessage("messages.home.not_found"));
            return;
        }

        player.sendMessage(formatMessage("messages.home.rename.success")
            .replace("%old%", oldName)
            .replace("%new%", newName));
    }

    private void teleportToHome(Player player, String homeName) {
        if (!player.hasPermission("eyn.home.teleport")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return;
        }

        Location home = homeManager.getHome(player.getUniqueId(), homeName);
        if (home == null) {
            player.sendMessage(formatMessage("messages.home.not_found"));
            return;
        }

        player.teleport(home);
        player.sendMessage(formatMessage("messages.home.teleport.success").replace("%name%", homeName));
    }

    private int getMaxHomes(Player player) {
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("eyn.home.limit." + i)) {
                return i;
            }
        }
        return config.getInt("homes.default_limit", 3);
    }

    private String formatMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', 
            messagesConfig.getString(key, "&cMessage not found: " + key));
    }
} 