package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Command for sending private messages.
 * Usage: /msg <player> <message>
 */
public class MsgCommand extends BaseCommand {

    public MsgCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "messages.player_only_command");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "messages.msg.usage");
            return true;
        }

        Player senderPlayer = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sendMessage(sender, "messages.msg.target_not_found", "%target%", args[0]);
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        sendMessage(sender, "messages.msg.sent", "%target%", target.getName(), "%message%", message);
        sendMessage(target, "messages.msg.received", "%sender%", senderPlayer.getName(), "%message%", message);
        return true;
    }
} 