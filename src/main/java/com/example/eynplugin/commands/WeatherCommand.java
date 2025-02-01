package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.Arrays;
import java.util.List;

public class WeatherCommand extends BaseCommand {

    public WeatherCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.weather")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.weather.invalid")));
            return true;
        }

        World world = player.getWorld();
        switch (args[0].toLowerCase()) {
            case "rain":
                world.setStorm(true);
                world.setThundering(false);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.weather.rain")));
                break;
            case "thunder":
                world.setStorm(true);
                world.setThundering(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.weather.thunder")));
                break;
            case "clear":
            case "sunny":
                world.setStorm(false);
                world.setThundering(false);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.weather.clear")));
                break;
            default:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.weather.invalid")));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("rain", "thunder", "clear", "sunny");
        }
        return null;
    }
} 