package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import java.util.List;
import java.util.Collections;
import com.example.eynplugin.Utils;

public class ClearInventoryCommand extends BaseCommand {

    public ClearInventoryCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check permission
        if (!checkPermission(sender, "eyn.clearinventory")) {
            return true;
        }

        Player target;
        
        if (args.length == 0) {
            // Clear sender's inventory
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.colorize(getMessage("messages.clearinventory.console_usage")));
                return true;
            }
            target = (Player) sender;
        } else {
            // Clear other player's inventory
            if (!checkPermission(sender, "eyn.clearinventory.others")) {
                return true;
            }
            
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Utils.colorize(getMessage("messages.moderation.player_not_found")));
                return true;
            }
        }

        // Clear inventory and armor
        target.getInventory().clear();
        target.getInventory().setArmorContents(new ItemStack[4]);
        target.updateInventory();

        // Send messages
        if (target == sender) {
            sender.sendMessage(Utils.colorize(getMessage("messages.clearinventory.self_cleared")));
        } else {
            sender.sendMessage(Utils.colorize(
                getMessage("messages.clearinventory.other_cleared")
                    .replace("%player%", target.getName())
            ));
            target.sendMessage(Utils.colorize(
                getMessage("messages.clearinventory.cleared_by_staff")
                    .replace("%staff%", sender.getName())
            ));
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.clearinventory.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
} 