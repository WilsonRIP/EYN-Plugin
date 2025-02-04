package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

/**
 * Opens a virtual anvil for the player.
 */
public class AnvilCommand extends BaseCommand {

    /**
     * Constructs a new AnvilCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file containing messages.
     */
    public AnvilCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the /anvil command.
     * Only players with the required permission ("eyn.anvil") can open the virtual anvil.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    the command arguments (unused).
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // Ensure only players can use this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        final Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.anvil")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        // Open the virtual anvil inventory for the player.
        player.openInventory(player.getServer().createInventory(player, InventoryType.ANVIL));
        player.sendMessage(formatMessage("messages.anvil.opened"));
        return true;
    }
}
