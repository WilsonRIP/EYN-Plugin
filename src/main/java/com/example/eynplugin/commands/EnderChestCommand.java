package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.Utils;
import java.util.Collections;
import java.util.List;

public class EnderChestCommand extends BaseCommand {

    public EnderChestCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkPermission(sender, "eyn.enderchest")) {
            return true;
        }

        Player target;
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.colorize(getMessage("messages.enderchest.console_usage")));
                return true;
            }
            target = (Player) sender;
        } else {
            if (!checkPermission(sender, "eyn.enderchest.others")) {
                return true;
            }
            
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Utils.colorize(getMessage("messages.moderation.player_not_found")));
                return true;
            }
        }

        openEnderChest(sender, target);
        return true;
    }

    private void openEnderChest(CommandSender sender, Player target) {
        if (sender instanceof Player) {
            Player sendingPlayer = (Player) sender;
            sendingPlayer.openInventory(target.getEnderChest());
            
            if (!sendingPlayer.equals(target)) {
                sendingPlayer.sendMessage(Utils.colorize(
                    getMessage("messages.enderchest.opened_other")
                        .replace("%player%", target.getName())
                ));
                target.sendMessage(Utils.colorize(
                    getMessage("messages.enderchest.opened_by")
                        .replace("%player%", sendingPlayer.getName())
                ));
            }
        } else {
            // Console can't open inventories, just confirm
            sender.sendMessage(Utils.colorize(
                getMessage("messages.enderchest.console_success")
                    .replace("%player%", target.getName())
            ));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.enderchest.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
} 