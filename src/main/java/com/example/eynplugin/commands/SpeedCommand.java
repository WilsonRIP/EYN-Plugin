package com.example.eynplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

public class SpeedCommand implements CommandExecutor {

    private final FileConfiguration messagesConfig;

    public SpeedCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eyn.speed")) {
            sender.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(formatMessage("messages.speed.usage"));
            return true;
        }

        float speed;
        try {
            speed = Float.parseFloat(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(formatMessage("messages.speed.invalid_speed"));
            return true;
        }

        if (speed < 0 || speed > 10) {
            sender.sendMessage(formatMessage("messages.speed.range"));
            return true;
        }

        if (speed == 0) {
            player.setWalkSpeed(0.2f); // Default walk speed
            player.setFlySpeed(0.1f);  // Default fly speed
            sender.sendMessage(formatMessage("messages.speed.reset"));
            return true;
        }

        float bukkitSpeed = speed / 10f;
        bukkitSpeed = Math.max(-1f, Math.min(1f, bukkitSpeed));

        player.setWalkSpeed(bukkitSpeed);
        player.setFlySpeed(bukkitSpeed);

        sender.sendMessage(formatMessage("messages.speed.set")
                .replace("%speed%", String.valueOf(speed)));

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