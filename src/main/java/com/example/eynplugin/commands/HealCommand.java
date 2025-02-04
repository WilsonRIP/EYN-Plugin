package com.example.eynplugin.commands;

import com.example.eynplugin.Utils;
import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * HealCommand allows players to heal themselves or others.
 * Usage:
 *   /heal              - Heals yourself.
 *   /heal <player>     - Heals the specified player (requires additional permission).
 */
public class HealCommand extends BaseCommand {

    /**
     * Constructs a new HealCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file for messages.
     */
    public HealCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Processes the /heal command.
     *
     * @param sender  the command sender.
     * @param cmd     the executed command.
     * @param label   the alias used.
     * @param args    the command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final Player target;
        // If no arguments provided and sender is not a player, show error.
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
                return true;
            }
            target = (Player) sender;
            // Check permission for self-healing.
            if (!checkPermission(target, "eyn.heal")) {
                return true;
            }
        } else {
            // When a target is specified, check permission for healing others.
            if (!checkPermission(sender, "eyn.heal.others")) {
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, "messages.error.player_not_found");
                return true;
            }
        }

        // Perform the healing process.
        healPlayer(target);

        // Send confirmation messages.
        if (target.equals(sender)) {
            sendMessage(target, "messages.heal.success");
        } else {
            sendMessage(sender, "messages.heal.other_success", "%player%", target.getName());
            sendMessage(target, "messages.heal.healed_by", "%player%", sender.getName());
        }
        return true;
    }

    /**
     * Heals the specified player by restoring health, food level, and clearing fire and potion effects
     * based on configuration values.
     *
     * @param player the player to heal.
     */
    @SuppressWarnings("deprecation")
    private void healPlayer(final Player player) {
        // Retrieve configuration values with sensible defaults.
        final double maxHealth = messagesConfig.getDouble("heal.max_health", 20.0);
        final int foodLevel = messagesConfig.getInt("heal.food_level", 20);
        final boolean clearFire = messagesConfig.getBoolean("heal.clear_fire", true);
        final boolean clearEffects = messagesConfig.getBoolean("heal.clear_effects", true);

        // Set the player's health to the lesser of the configured max and their actual maximum health.
        player.setHealth(Math.min(player.getMaxHealth(), maxHealth));
        player.setFoodLevel(foodLevel);

        if (clearFire) {
            player.setFireTicks(0);
        }
        if (clearEffects) {
            player.getActivePotionEffects().forEach(effect -> 
                player.removePotionEffect(effect.getType())
            );
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.heal.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
}
