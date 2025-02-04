package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

/**
 * Command that opens a virtual smithing table.
 */
public class SmithingTableCommand extends BaseCommand {

    /**
     * Constructs a new SmithingTableCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file containing messages.
     */
    public SmithingTableCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the smithing table command.
     * Only players with the required permission can open a virtual smithing table.
     *
     * @param sender  the source of the command.
     * @param command the command which was executed.
     * @param label   the alias of the command which was used.
     * @param args    additional command arguments (not used).
     * @return true, after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }

        final Player player = (Player) sender;
        
        // Check for the required permission.
        if (!Utils.checkPermission(player, "eyn.smithing")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Open the virtual smithing table inventory.
        player.openInventory(player.getServer().createInventory(player, InventoryType.SMITHING));
        player.sendMessage(Utils.colorize(getMessage("messages.smithing.opened")));
        return true;
    }
}
