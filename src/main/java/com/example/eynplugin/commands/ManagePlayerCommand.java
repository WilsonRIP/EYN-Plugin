package com.example.eynplugin.commands;

import com.example.eynplugin.gui.PlayerManagementGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.commands.ModerationCommands;

public class ManagePlayerCommand implements CommandExecutor {

    private final FileConfiguration messagesConfig;
    private final Plugin plugin;
    private final LuckPermsHandler luckPermsHandler;
    private final ModerationCommands moderationCommands;

    public ManagePlayerCommand(FileConfiguration messagesConfig, Plugin plugin, LuckPermsHandler luckPermsHandler, ModerationCommands moderationCommands) {
        this.messagesConfig = messagesConfig;
        this.plugin = plugin;
        this.luckPermsHandler = luckPermsHandler;
        this.moderationCommands = moderationCommands;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eyn.manageplayer")) {
            sender.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(formatMessage("messages.manageplayer.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("messages.player_not_found").replace("%player%", args[0]));
            return true;
        }

        // Open the player management GUI
        PlayerManagementGUI gui = new PlayerManagementGUI(target, messagesConfig, plugin, luckPermsHandler, moderationCommands);
        gui.open(player);

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