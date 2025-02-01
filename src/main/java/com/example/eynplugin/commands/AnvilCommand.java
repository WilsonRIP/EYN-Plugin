package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import com.example.eynplugin.Utils;

public class AnvilCommand extends BaseCommand {

    public AnvilCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.anvil")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Open virtual anvil
        player.openInventory(player.getServer().createInventory(player, InventoryType.ANVIL));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.anvil.opened")));
        return true;
    }
} 