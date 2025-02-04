package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Command to open a player's EnderChest.
 *
 * <p>Usage:</p>
 * <ul>
 *     <li><code>/enderchest</code> - Opens the sender's own EnderChest (player only).</li>
 *     <li><code>/enderchest &lt;player&gt;</code> - Opens the specified player's EnderChest (requires additional permission).</li>
 * </ul>
 */
public class EnderChestCommand extends BaseCommand {

    public EnderChestCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Handles the execution of the EnderChest command.
     *
     * @param sender The command sender.
     * @param cmd    The command.
     * @param label  The command label.
     * @param args   The command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check for the base permission.
        if (!checkPermission(sender, "eyn.enderchest")) {
            return true;
        }

        final Player target;

        // If no argument is provided, the sender must be a player.
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.colorize(getMessage("messages.enderchest.console_usage")));
                return true;
            }
            target = (Player) sender;
        } else {
            // Check permission to open others' EnderChests.
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

    /**
     * Opens the EnderChest of the target player for the sender and sends appropriate feedback messages.
     *
     * @param sender The command sender.
     * @param target The player whose EnderChest is to be opened.
     */
    private void openEnderChest(final CommandSender sender, final Player target) {
        if (sender instanceof Player) {
            final Player sendingPlayer = (Player) sender;
            sendingPlayer.openInventory(target.getEnderChest());

            // Notify both players if the sender is accessing someone else's EnderChest.
            if (!sendingPlayer.equals(target)) {
                sendingPlayer.sendMessage(Utils.colorize(
                        getMessage("messages.enderchest.opened_other").replace("%player%", target.getName())
                ));
                target.sendMessage(Utils.colorize(
                        getMessage("messages.enderchest.opened_by").replace("%player%", sendingPlayer.getName())
                ));
            }
        } else {
            // Console can't open inventories; just confirm the action.
            sender.sendMessage(Utils.colorize(
                    getMessage("messages.enderchest.console_success").replace("%player%", target.getName())
            ));
        }
    }

    /**
     * Provides tab completion suggestions for the EnderChest command.
     *
     * @param sender The command sender.
     * @param cmd    The command.
     * @param alias  The command alias.
     * @param args   The command arguments.
     * @return A list of suggested completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.enderchest.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
}
