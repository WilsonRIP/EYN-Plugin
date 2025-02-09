package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Collections;
import com.example.eynplugin.Utils;
import java.util.Arrays;
import org.bukkit.inventory.PlayerInventory;

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
                sender.sendMessage(Utils.colorize(getMessage("messages.moderation.player_not_found").replace("%player%", args[0])));
                return true;
            }
        }

        // Check if inventory is already empty
        PlayerInventory inv = target.getInventory();
        if (isInventoryEmpty(inv)) {
            if (target.equals(sender)) {
                sender.sendMessage(Utils.colorize(getMessage("messages.clearinventory.no_items")));
            } else {
                sender.sendMessage(Utils.colorize(getMessage("messages.clearinventory.target_no_items").replace("%player%", target.getName())));
            }
            
            return true;
        }

        // Clear inventory and armor
        clearInventory(inv);

        // Send messages
        if (target.equals(sender)) {
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

    /**
     * Checks if a player's inventory (including armor) is empty.
     *
     * @param inv The PlayerInventory to check.
     * @return True if the inventory is empty, false otherwise.
     */
    private boolean isInventoryEmpty(PlayerInventory inv) {
        return inv.isEmpty() && Arrays.stream(inv.getArmorContents()).allMatch(item -> item == null || item.getType().isAir());
    }

    /**
     * Clears a player's inventory, including armor.
     *
     * @param inv The PlayerInventory to clear.
     */
    private void clearInventory(PlayerInventory inv) {
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setItemInOffHand(null);
    }
}