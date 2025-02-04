package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PayCommand extends BaseCommand {
    private final Economy economy;

    public PayCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Economy economy) {
        super(luckPermsHandler, messagesConfig);
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure that the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }

        final Player player = (Player) sender;

        // Check for the required permission
        if (!Utils.checkPermission(player, "eyn.pay")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Validate the number of arguments
        if (args.length != 2) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.usage")));
            return true;
        }

        // Retrieve the target player
        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.player_not_found")
                    .replace("%player%", args[0])));
            return true;
        }

        // Prevent self-payment
        if (target.equals(player)) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.self_pay")));
            return true;
        }

        // Parse and validate the amount
        final double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.invalid_amount")));
            return true;
        }

        if (amount < 1) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.minimum_amount")));
            return true;
        }

        // Check if the sender has sufficient funds
        if (!economy.has(player, amount)) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.insufficient_funds")
                    .replace("%amount%", String.format("%.2f", amount))));
            return true;
        }

        // Process the withdrawal from the sender's account
        EconomyResponse withdrawalResponse = economy.withdrawPlayer(player, amount);
        if (!withdrawalResponse.transactionSuccess()) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.withdraw_failed")
                    .replace("%error%", withdrawalResponse.errorMessage)));
            return true;
        }

        // Process the deposit to the target's account
        EconomyResponse depositResponse = economy.depositPlayer(target, amount);
        if (!depositResponse.transactionSuccess()) {
            // Attempt a refund if deposit fails
            economy.depositPlayer(player, amount);
            player.sendMessage(Utils.colorize(getMessage("messages.pay.deposit_failed")
                    .replace("%error%", depositResponse.errorMessage)));
            return true;
        }

        // Format the amount for display
        final String formattedAmount = String.format("%.2f", amount);

        // Notify both sender and receiver of the successful transaction
        player.sendMessage(Utils.colorize(getMessage("messages.pay.success_sender")
                .replace("%amount%", formattedAmount)
                .replace("%player%", target.getName())));
        target.sendMessage(Utils.colorize(getMessage("messages.pay.success_receiver")
                .replace("%amount%", formattedAmount)
                .replace("%player%", player.getName())));

        return true;
    }
}
