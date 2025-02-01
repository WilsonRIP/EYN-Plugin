package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final FileConfiguration messagesConfig;
    protected final LuckPermsHandler luckPermsHandler;

    public BaseCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        this.luckPermsHandler = luckPermsHandler;
        this.messagesConfig = messagesConfig;
    }

    // Constructor for commands that don't need LuckPerms
    public BaseCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
        this.luckPermsHandler = null;
    }

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    protected boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sendMessage(sender, "messages.no_permission");
        return false;
    }

    protected boolean checkPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }
        sendMessage(sender, "messages.player_only_command");
        return false;
    }

    protected Player getTarget(CommandSender sender, String name) {
        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            sendMessage(sender, "messages.error.player_not_found");
            return null;
        }
        return target;
    }

    protected void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(formatMessage(key));
    }

    protected void sendMessage(CommandSender sender, String key, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be in pairs");
        }
        
        String message = formatMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(message);
    }

    protected void broadcastMessage(String key, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be in pairs");
        }
        
        String message = formatMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        Bukkit.broadcastMessage(message);
    }

    protected String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            message = "&cMessage not found: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    protected List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .collect(Collectors.toList());
    }

    protected List<String> filterStartingWith(List<String> list, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return list;
        }
        String lowercasePrefix = prefix.toLowerCase();
        return list.stream()
            .filter(str -> str.toLowerCase().startsWith(lowercasePrefix))
            .collect(Collectors.toList());
    }

    protected boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean isPositiveInteger(String str) {
        try {
            return Integer.parseInt(str) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected String getMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            message = "Message not found: " + key;
        }
        return message;
    }
} 