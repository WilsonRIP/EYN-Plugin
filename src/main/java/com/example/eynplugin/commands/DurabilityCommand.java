package com.example.eynplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.eynplugin.Utils;
import com.example.eynplugin.api.LuckPermsHandler;

/**
 * Command to adjust the durability (damage value) of the item in the player's main hand.
 */
public class DurabilityCommand extends BaseCommand {

    public DurabilityCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the durability command.
     * <p>
     * Usage: /<command> <durability>
     * </p>
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias used for this command.
     * @param args    The command arguments.
     * @return true if the command was processed.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Use pattern matching for instanceof to avoid extra cast.
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Utils.colorize(getMessage("player_only_command")));
            return true;
        }
        // Now use 'player' directly.
        if (!Utils.checkPermission(player, "eyn.durability")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Validate that exactly one argument is provided.
        if (args.length != 1) {
            player.sendMessage(Utils.colorize(getMessage("durability.invalid")));
            return true;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().isItem()) {
            player.sendMessage(Utils.colorize(getMessage("durability.no_item")));
            return true;
        }

        try {
            final int damageValue = Integer.parseInt(args[0]);
            if (damageValue < 0) {
                player.sendMessage(Utils.colorize(getMessage("durability.invalid")));
                return true;
            }

            final ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(damageValue);
                item.setItemMeta(meta);
                player.sendMessage(Utils.colorize(
                        getMessage("durability.changed").replace("%durability%", String.valueOf(damageValue))
                ));
            } else {
                player.sendMessage(Utils.colorize(getMessage("durability.invalid")));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.colorize(getMessage("durability.invalid")));
        }

        return true;
    }
}
