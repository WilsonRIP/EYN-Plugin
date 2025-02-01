package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;

public class FlyCommand extends BaseCommand {

    public FlyCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.fly")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.no_permission")));
            return true;
        }

        if (args.length > 0 && Utils.checkPermission(player, "eyn.fly.others")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                toggleFlight(target);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    getMessage("messages.fly.toggle_other").replace("%player%", target.getName())));
                return true;
            }
        }

        toggleFlight(player);
        return true;
    }

    private void toggleFlight(Player player) {
        boolean canFly = !player.getAllowFlight();
        player.setAllowFlight(canFly);
        player.setFlying(canFly);
        String message = canFly ? getMessage("messages.fly.enabled") : getMessage("messages.fly.disabled");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return suggestions;
        }
        return null;
    }
} 