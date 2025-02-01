package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.List;

public class WorldInfoCommand extends BaseCommand {

    public WorldInfoCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.worldinfo")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.no_permission")));
            return true;
        }

        World world = player.getWorld();
        String worldName = world.getName();
        long worldTime = world.getTime();
        String weather = world.hasStorm() ? "Stormy" : "Clear";
        int playerCount = world.getPlayers().size();

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            getMessage("messages.worldinfo.header").replace("%world%", worldName)));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            getMessage("messages.worldinfo.time").replace("%time%", String.valueOf(worldTime))));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            getMessage("messages.worldinfo.weather").replace("%weather%", weather)));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            getMessage("messages.worldinfo.players").replace("%count%", String.valueOf(playerCount))));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
} 