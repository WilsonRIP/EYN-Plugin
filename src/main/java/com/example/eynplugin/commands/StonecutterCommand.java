package com.example.eynplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class StonecutterCommand implements CommandExecutor {

    private final FileConfiguration messagesConfig;

    public StonecutterCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eyn.stonecutter")) {
            sender.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        // Open the stonecutter inventory
        Inventory stonecutterInventory = Bukkit.createInventory(player, 1, ChatColor.DARK_GRAY + "Stonecutter");
        player.openInventory(stonecutterInventory);
        player.sendMessage(formatMessage("messages.stonecutter.opened"));

        return true;
    }

    private String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Could not find message key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 