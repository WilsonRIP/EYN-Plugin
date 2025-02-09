package com.example.eynplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class GetPosCommand implements CommandExecutor {

    private final FileConfiguration messagesConfig;

    public GetPosCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();

        String worldName = loc.getWorld().getName();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        String message = formatMessage("messages.getpos.format")
                .replace("%world%", worldName)
                .replace("%x%", String.valueOf(x))
                .replace("%y%", String.valueOf(y))
                .replace("%z%", String.valueOf(z))
                .replace("%yaw%", String.valueOf(yaw))
                .replace("%pitch%", String.valueOf(pitch));

        player.sendMessage(message);
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