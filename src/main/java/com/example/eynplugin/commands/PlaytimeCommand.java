package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Statistic;

public class PlaytimeCommand extends BaseCommand {

    public PlaytimeCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.playtime")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    getMessage("messages.playtime.no_data").replace("%player%", args[0])));
                return true;
            }
            showPlaytime(player, target);
        } else {
            showPlaytime(player, player);
        }
        return true;
    }

    private void showPlaytime(Player sender, Player target) {
        long totalMinutes = target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60; // Convert ticks to minutes
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (sender == target) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                getMessage("messages.playtime.self")
                    .replace("%hours%", String.valueOf(hours))
                    .replace("%minutes%", String.valueOf(minutes))));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                getMessage("messages.playtime.other")
                    .replace("%player%", target.getName())
                    .replace("%hours%", String.valueOf(hours))
                    .replace("%minutes%", String.valueOf(minutes))));
        }
    }
} 
