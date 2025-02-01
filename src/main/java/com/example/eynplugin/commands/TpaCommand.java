package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.Utils;
import java.util.*;

public class TpaCommand extends BaseCommand {
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_SECONDS = 30;
    private static final int REQUEST_EXPIRE_SECONDS = 60;

    public TpaCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.tpa.console_error")));
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            return handleAccept(player);
        } else if (args[0].equalsIgnoreCase("deny")) {
            return handleDeny(player);
        }

        // Handle teleport request
        if (!checkPermission(player, "eyn.tpa")) return true;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(player, "messages.tpa.player_not_found");
            return true;
        }

        if (target.equals(player)) {
            sendMessage(player, "messages.tpa.self_request");
            return true;
        }

        if (cooldowns.containsKey(player.getUniqueId())) {
            long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + COOLDOWN_SECONDS) 
                - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                sendMessage(player, "messages.tpa.cooldown", "%seconds%", String.valueOf(secondsLeft));
                return true;
            }
        }

        pendingRequests.put(target.getUniqueId(), player.getUniqueId());
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        sendMessage(player, "messages.tpa.sent", "%player%", target.getName());
        sendMessage(target, "messages.tpa.received", "%player%", player.getName());
        
        Bukkit.getScheduler().runTaskLaterAsynchronously(Bukkit.getPluginManager().getPlugin("EYNPlugin"), 
            () -> expireRequest(target.getUniqueId()), REQUEST_EXPIRE_SECONDS * 20);
        
        return true;
    }

    private boolean handleAccept(Player player) {
        if (!checkPermission(player, "eyn.tpaccept")) return true;

        UUID requesterId = pendingRequests.remove(player.getUniqueId());
        if (requesterId == null) {
            sendMessage(player, "messages.tpa.no_pending");
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            sendMessage(player, "messages.tpa.requester_offline");
            return true;
        }

        requester.teleport(player.getLocation());
        sendMessage(player, "messages.tpa.accepted", "%player%", requester.getName());
        sendMessage(requester, "messages.tpa.accepted_sender", "%player%", player.getName());
        return true;
    }

    private boolean handleDeny(Player player) {
        if (!checkPermission(player, "eyn.tpaccept")) return true;

        UUID requesterId = pendingRequests.remove(player.getUniqueId());
        if (requesterId == null) {
            sendMessage(player, "messages.tpa.no_pending");
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) {
            sendMessage(requester, "messages.tpa.denied", "%player%", player.getName());
        }
        sendMessage(player, "messages.tpa.denied_sender", "%player%", (requester != null ? requester.getName() : "Unknown"));
        return true;
    }

    private void expireRequest(UUID targetId) {
        UUID requesterId = pendingRequests.remove(targetId);
        if (requesterId != null) {
            Player requester = Bukkit.getPlayer(requesterId);
            Player target = Bukkit.getPlayer(targetId);
            if (requester != null) {
                sendMessage(requester, "messages.tpa.expired", "%player%", (target != null ? target.getName() : "Unknown"));
            }
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(Utils.colorize(getMessage("messages.tpa.usage")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (checkPermission(sender, "eyn.tpaccept")) {
                options.add("accept");
                options.add("deny");
            }
            options.addAll(getOnlinePlayerNames());
            return options;
        }
        return Collections.emptyList();
    }
} 