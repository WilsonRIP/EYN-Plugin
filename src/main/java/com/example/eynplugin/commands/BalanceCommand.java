package com.example.eynplugin.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.example.eynplugin.Utils;
import com.example.eynplugin.api.LuckPermsHandler;

import net.milkbowl.vault.economy.Economy;

/**
 * BalanceCommand provides functionality to check a player's balance.
 * Usage:
 *   /balance            - Shows the sender's own balance.
 *   /balance <player>   - Shows the specified player's balance (requires additional permission).
 */
public class BalanceCommand extends BaseCommand {
    private final Economy economy;

    /**
     * Constructs a new BalanceCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file containing messages.
     * @param economy          the Vault economy instance.
     */
    public BalanceCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig, final Economy economy) {
        super(luckPermsHandler, messagesConfig);
        this.economy = economy;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // If sender is not a player, display an error.
        if (!(sender instanceof Player)) {
            String message = getMessage("messages.player_only_command");
            if (message != null) {
                sender.sendMessage(Utils.colorize(message));
            } else {
                sender.sendMessage(Utils.colorize("messages.player_only_command"));
            }
            return true;
        }
        final Player player = (Player) sender;

        // Check permission to view balance.
        if (!Utils.checkPermission(player, "eyn.balance")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Show sender's own balance if no target is specified.
        if (args.length == 0) {
            final double balance = economy.getBalance(player);
            player.sendMessage(Utils.colorize(getMessage("messages.balance.self")
                    .replace("%amount%", String.format("%.2f", balance))));
            return true;
        }

        // Check permission to view others' balances.
        if (!Utils.checkPermission(player, "eyn.balance.others")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Utils.colorize(getMessage("messages.balance.player_not_found")
                    .replace("%player%", args[0])));
            return true;
        }

        final double balance = economy.getBalance(target);
        player.sendMessage(Utils.colorize(getMessage("messages.balance.other")
                .replace("%player%", target.getName())
                .replace("%amount%", String.format("%.2f", balance))));
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1 && sender.hasPermission("eyn.balance.others")) {
            return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }
        return new ArrayList<>();
    }
}
