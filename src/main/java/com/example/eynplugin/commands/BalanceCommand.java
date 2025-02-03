package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BalanceCommand extends BaseCommand {
    private final Economy economy;

    public BalanceCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Economy economy) {
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
        if (!Utils.checkPermission(player, "eyn.balance")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Check own balance
        if (args.length == 0) {
            double balance = economy.getBalance(player);
            player.sendMessage(Utils.colorize(getMessage("messages.balance.self")
                    .replace("%amount%", String.format("%.2f", balance))));
            return true;
        }

        // Check other player's balance
        if (!Utils.checkPermission(player, "eyn.balance.others")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Utils.colorize(getMessage("messages.balance.player_not_found")
                    .replace("%player%", args[0])));
            return true;
        }

        double balance = economy.getBalance(target);
        player.sendMessage(Utils.colorize(getMessage("messages.balance.other")
                .replace("%player%", target.getName())
                .replace("%amount%", String.format("%.2f", balance))));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("eyn.balance.others")) {
            return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }
        return new ArrayList<>();
    }
} 