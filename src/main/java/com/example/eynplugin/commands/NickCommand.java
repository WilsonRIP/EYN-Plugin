package com.example.eynplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class NickCommand implements CommandExecutor {
    private final FileConfiguration messagesConfig;

    public NickCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.nick.console_error"));
            return true;
        }

        Player player = (Player) sender;

        // /nick reset
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("eyn.nick.reset")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                return true;
            }
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.sendMessage(formatMessage("messages.nick.reset"));
            return true;
        }

        // /nick <nickname>
        if (args.length == 1) {
            if (!player.hasPermission("eyn.nick.self")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                return true;
            }
            setNickname(player, args[0], false);
            return true;
        }

        // /nick <player> <nickname>
        if (args.length == 2) {
            if (!player.hasPermission("eyn.nick.others")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(formatMessage("messages.moderation.player_not_found"));
                return true;
            }

            setNickname(target, args[1], true);
            player.sendMessage(formatMessage("messages.nick.changed_other")
                .replace("%player%", target.getName())
                .replace("%nickname%", args[1]));
            return true;
        }

        player.sendMessage(formatMessage("messages.nick.usage"));
        return true;
    }

    private void setNickname(Player player, String nickname, boolean isOther) {
        String coloredNick = ChatColor.translateAlternateColorCodes('&', nickname);
        player.setDisplayName(coloredNick);
        player.setPlayerListName(coloredNick);
        
        if (!isOther) {
            player.sendMessage(formatMessage("messages.nick.changed_self")
                .replace("%nickname%", nickname));
        }
    }

    private String formatMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', 
            messagesConfig.getString(key, "&cMessage not found: " + key));
    }
} 