package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

public class DurabilityCommand extends BaseCommand {

    public DurabilityCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.durability")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("durability.invalid")));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().isItem()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("durability.no_item")));
            return true;
        }

        try {
            int durability = Integer.parseInt(args[0]);
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(durability);
                item.setItemMeta(meta);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("durability.changed").replace("%durability%", String.valueOf(durability))));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("durability.invalid")));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("durability.invalid")));
        }
        return true;
    }
} 