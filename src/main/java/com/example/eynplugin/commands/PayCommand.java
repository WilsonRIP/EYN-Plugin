package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import net.milkbowl.vault.economy.Economy;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.pay")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.usage")));
            return true;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.player_not_found")
                    .replace("%player%", args[0])));
            return true;
        }

        // Check if player is trying to pay themselves
        if (target.equals(player)) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.self_pay")));
            return true;
        }

        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.invalid_amount")));
            return true;
        }

        // Validate amount
        if (amount < 1) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.minimum_amount")));
            return true;
        }

        // Check if player has enough money
        if (!economy.has(player, amount)) {
            player.sendMessage(Utils.colorize(getMessage("messages.pay.insufficient_funds")
                    .replace("%amount%", String.format("%.2f", amount))));
            return true;
        }

        // Process transaction
        economy.withdrawPlayer(player, amount);
        economy.depositPlayer(target, amount);

        // Send success messages
        String formattedAmount = String.format("%.2f", amount);
        player.sendMessage(Utils.colorize(getMessage("messages.pay.success_sender")
                .replace("%amount%", formattedAmount)
                .replace("%player%", target.getName())));
        
        target.sendMessage(Utils.colorize(getMessage("messages.pay.success_receiver")
                .replace("%amount%", formattedAmount)
                .replace("%player%", player.getName())));

        return true;
    }
} 