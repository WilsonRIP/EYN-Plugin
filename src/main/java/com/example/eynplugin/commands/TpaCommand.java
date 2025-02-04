package com.example.eynplugin.commands;

import com.example.eynplugin.EYNPlugin;
import com.example.eynplugin.Utils;
import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Handles teleport request commands (Tpa, TpaAccept, TpaDeny).
 * Supports sending teleport requests, accepting/denying them, and enforces cooldowns.
 */
public class TpaCommand extends BaseCommand {

    private final EYNPlugin plugin;
    private final Map<UUID, UUID> pendingRequests = new HashMap<>(); // Key: target, Value: requester
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_SECONDS = 30;
    private static final int REQUEST_EXPIRE_SECONDS = 60;

    /**
     * Constructs a new TpaCommand.
     *
     * @param plugin           the main plugin instance.
     * @param luckPermsHandler the LuckPerms handler.
     * @param messagesConfig   the configuration file for messages.
     */
    public TpaCommand(EYNPlugin plugin, LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    /**
     * Processes the teleport request command.
     *
     * Usage:
     *   /tpa <player>     - Send a teleport request.
     *   /tpa accept       - Accept a pending teleport request.
     *   /tpa deny         - Deny a pending teleport request.
     *
     * @param sender  the command sender.
     * @param cmd     the command executed.
     * @param label   the alias used.
     * @param args    command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.tpa.console_error")));
            return true;
        }
        final Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        final String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "accept":
                return handleAccept(player);
            case "deny":
                return handleDeny(player);
            default:
                // Process sending a teleport request.
                return handleSendRequest(player, args[0]);
        }
    }

    /**
     * Handles sending a teleport request from the sender to the target.
     *
     * @param player   the requester.
     * @param targetName the name of the target player.
     * @return true after processing the request.
     */
    private boolean handleSendRequest(final Player player, final String targetName) {
        if (!checkPermission(player, "eyn.tpa")) {
            return true;
        }

        final Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sendMessage(player, "messages.tpa.player_not_found");
            return true;
        }
        if (target.equals(player)) {
            sendMessage(player, "messages.tpa.self_request");
            return true;
        }

        // Check cooldown for the requester.
        final UUID requesterId = player.getUniqueId();
        final long currentTime = System.currentTimeMillis();
        final long cooldownEnd = cooldowns.getOrDefault(requesterId, 0L) + TimeUnit.SECONDS.toMillis(COOLDOWN_SECONDS);
        final long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(Math.max(0, cooldownEnd - currentTime));
        if (secondsLeft > 0) {
            sendMessage(player, "messages.tpa.cooldown", "%seconds%", String.valueOf(secondsLeft));
            return true;
        }

        // Register the teleport request and update cooldown.
        pendingRequests.put(target.getUniqueId(), requesterId);
        cooldowns.put(requesterId, currentTime);

        sendMessage(player, "messages.tpa.sent", "%player%", target.getName());
        sendMessage(target, "messages.tpa.received", "%player%", player.getName());

        // Play notification sounds.
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        // Schedule request expiration.
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> expireRequest(target.getUniqueId()),
                REQUEST_EXPIRE_SECONDS * 20L);
        return true;
    }

    /**
     * Handles acceptance of a teleport request.
     *
     * @param player the player accepting the request.
     * @return true if the request was processed.
     */
    private boolean handleAccept(final Player player) {
        if (!checkPermission(player, "eyn.tpaccept")) {
            return true;
        }

        final UUID targetId = player.getUniqueId();
        final UUID requesterId = pendingRequests.remove(targetId);
        if (requesterId == null) {
            sendMessage(player, "messages.tpa.no_pending");
            return true;
        }

        final Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null || !requester.isOnline()) {
            sendMessage(player, "messages.tpa.requester_offline");
            return true;
        }

        if (!requester.teleport(player.getLocation())) {
            sendMessage(player, "messages.error.generic");
            plugin.getLogger().warning("Teleport failed for " + requester.getName());
            return true;
        }

        sendMessage(player, "messages.tpa.accepted", "%player%", requester.getName());
        sendMessage(requester, "messages.tpa.accepted_sender", "%player%", player.getName());
        return true;
    }

    /**
     * Handles denial of a teleport request.
     *
     * @param player the player denying the request.
     * @return true if the request was processed.
     */
    private boolean handleDeny(final Player player) {
        if (!checkPermission(player, "eyn.tpaccept")) {
            return true;
        }

        final UUID targetId = player.getUniqueId();
        final UUID requesterId = pendingRequests.remove(targetId);
        if (requesterId == null) {
            sendMessage(player, "messages.tpa.no_pending");
            return true;
        }

        final Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) {
            sendMessage(requester, "messages.tpa.denied", "%player%", player.getName());
        }
        sendMessage(player, "messages.tpa.denied_sender", "%player%", (requester != null ? requester.getName() : "Unknown"));
        return true;
    }

    /**
     * Expires a teleport request if it has not been accepted or denied.
     *
     * @param targetId the UUID of the target player.
     */
    private void expireRequest(final UUID targetId) {
        final UUID requesterId = pendingRequests.remove(targetId);
        if (requesterId != null) {
            final Player requester = Bukkit.getPlayer(requesterId);
            final Player target = Bukkit.getPlayer(targetId);
            if (requester != null && target != null && target.isOnline()) {
                sendMessage(requester, "messages.tpa.expired", "%player%", target.getName());
            }
        }
    }

    /**
     * Sends the usage message for the teleport request command.
     *
     * @param player the player to receive the usage message.
     */
    private void sendUsage(final Player player) {
        player.sendMessage(Utils.colorize(getMessage("messages.tpa.usage")));
    }

    /**
     * Provides tab completion suggestions for the TPA command.
     *
     * @param sender  the command sender.
     * @param cmd     the command.
     * @param alias   the alias used.
     * @param args    the command arguments.
     * @return a list of suggestions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> options = new ArrayList<>();
            if (checkPermission(sender, "eyn.tpaccept")) {
                options.add("accept");
                options.add("deny");
            }
            if (sender instanceof Player) {
                final String senderName = ((Player) sender).getName();
                options.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> !name.equalsIgnoreCase(senderName))
                        .collect(Collectors.toList()));
            }
            return options;
        }
        return Collections.emptyList();
    }
}
