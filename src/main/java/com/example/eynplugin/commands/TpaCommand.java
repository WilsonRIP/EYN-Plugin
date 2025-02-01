package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.Utils;
import java.util.*;
import com.example.eynplugin.EYNPlugin;
import java.util.concurrent.TimeUnit;
import org.bukkit.Sound;
import java.util.stream.Collectors;

public class TpaCommand extends BaseCommand {
    private final EYNPlugin plugin;
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_SECONDS = 30;
    private static final int REQUEST_EXPIRE_SECONDS = 60;

    public TpaCommand(EYNPlugin plugin, LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
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
            long cooldownEnd = cooldowns.get(player.getUniqueId()) + TimeUnit.SECONDS.toMillis(COOLDOWN_SECONDS);
            long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(cooldownEnd - System.currentTimeMillis());
            if (secondsLeft > 0) {
                sendMessage(player, "messages.tpa.cooldown", "%seconds%", String.valueOf(secondsLeft));
                return true;
            }
        }

        pendingRequests.put(target.getUniqueId(), player.getUniqueId());
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        sendMessage(player, "messages.tpa.sent", "%player%", target.getName());
        sendMessage(target, "messages.tpa.received", "%player%", player.getName());
        
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, 
            () -> expireRequest(target.getUniqueId()), REQUEST_EXPIRE_SECONDS * 20);
        
        return true;
    }

    /**
     * Handles teleport request acceptance
     * @param player Player accepting the request
     * @return true if handled successfully
     */
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

        if (!requester.isOnline() || !player.isOnline()) {
            sendMessage(player, "messages.tpa.requester_offline");
            return true;
        }
        if (!requester.teleport(player.getLocation())) {
            sendMessage(player, "messages.error.generic");
            plugin.getLogger().warning("Teleport failed for " + requester.getName());
        }
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
            if (target != null && target.isOnline()) {
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
                Collections.addAll(options, "accept", "deny");
            }
            options.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(sender.getName()))
                .collect(Collectors.toList()));
            return options;
        }
        return Collections.emptyList();
    }
} 