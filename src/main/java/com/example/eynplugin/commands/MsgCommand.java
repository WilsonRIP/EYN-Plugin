package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command for sending private messages.
 * Usage: /msg <player> <message>
 */
public class MsgCommand extends BaseCommand {
    private static final Map<UUID, UUID> lastMessaged = new HashMap<>();

    public MsgCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.player_only_command");
            return true;
        }

        if (args.length < 2) {
            sendMessage(player, "messages.msg.usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline() || !player.canSee(target)) {
            sendMessage(player, "messages.msg.target_not_found", "%target%", args[0]);
            return true;
        }

        if (isBlocked(player, target)) {
            sendMessage(player, "messages.msg.blocked", "%target%", target.getName());
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        updateLastMessaged(player, target);
        sendPrivateMessage(player, target, message);
        return true;
    }

    private boolean isBlocked(Player sender, Player receiver) {
        // Implement your block system logic here
        return false;
    }

    private void updateLastMessaged(Player sender, Player receiver) {
        lastMessaged.put(sender.getUniqueId(), receiver.getUniqueId());
        lastMessaged.put(receiver.getUniqueId(), sender.getUniqueId());
    }

    private void sendPrivateMessage(Player sender, Player receiver, String message) {
        String formattedSender = formatMessage("messages.msg.sent")
            .replace("%target%", receiver.getName())
            .replace("%message%", message);
        
        String formattedReceiver = formatMessage("messages.msg.received")
            .replace("%sender%", sender.getName())
            .replace("%message%", message);

        sender.sendMessage(formattedSender);
        receiver.sendMessage(formattedReceiver);
    }

    public static Player getLastMessaged(Player player) {
        UUID last = lastMessaged.get(player.getUniqueId());
        return last != null ? Bukkit.getPlayer(last) : null;
    }
} 