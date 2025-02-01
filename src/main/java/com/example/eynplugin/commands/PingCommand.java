package com.example.eynplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class PingCommand extends BaseCommand {

    public PingCommand(FileConfiguration messagesConfig) {
        super(messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 && !(sender instanceof Player)) {
            sendMessage(sender, "messages.ping.console_error");
            return true;
        }

        if (args.length == 0) {
            Player player = (Player) sender;
            showPing(sender, player);
            return true;
        }

        if (!sender.hasPermission("eyn.ping.others")) {
            sendMessage(sender, "messages.no_permission");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "messages.error.player_not_found");
            return true;
        }

        showPing(sender, target);
        return true;
    }

    private void showPing(CommandSender sender, Player target) {
        int ping = target.getPing();
        String quality;
        
        if (ping < 50) quality = "§a";        // Green for excellent
        else if (ping < 100) quality = "§2";   // Dark green for good
        else if (ping < 150) quality = "§e";   // Yellow for okay
        else if (ping < 200) quality = "§6";   // Gold for mediocre
        else quality = "§c";                   // Red for poor

        String pingText = quality + ping + "ms";

        if (sender == target) {
            sendMessage(sender, "messages.ping.self", "%ping%", pingText);
        } else {
            sendMessage(sender, "messages.ping.other",
                "%player%", target.getName(),
                "%ping%", pingText);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("eyn.ping.others")) {
            return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }
        return new ArrayList<>();
    }
} 