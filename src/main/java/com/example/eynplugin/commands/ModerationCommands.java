package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.craftbukkit.BanLookup;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.OfflinePlayer;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

public class ModerationCommands extends BaseCommand {
    private final LuckPermsHandler luckPermsHandler;
    private final Plugin plugin;
    private final Logger logger;
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([dhms])");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final String FREEZE_METADATA = "frozen";

    public ModerationCommands(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Plugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.luckPermsHandler = luckPermsHandler;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    // Logging method for moderation actions
    private void logModerationAction(CommandSender sender, String action, String targetName, String reason) {
        String senderName = sender instanceof Player ? ((Player) sender).getName() : "CONSOLE";
        String logMessage = String.format("%s performed %s on %s. Reason: %s", 
            senderName, action, targetName, reason != null ? reason : "No reason provided");
        logger.info(logMessage);
    }

    private boolean canModerate(CommandSender sender, Player target) {
        if (target == null) return true;
        
        // Prevent self-moderation
        if (sender instanceof Player && sender.getName().equals(target.getName())) {
            sendMessage(sender, "messages.moderation.self_target");
            return false;
        }

        // Check OP immunity
        if (target.isOp()) {
            sendMessage(sender, "messages.moderation.target_is_op");
            return false;
        }

        // Check group weights if sender is a player
        if (sender instanceof Player) {
            Player moderator = (Player) sender;
            int moderatorWeight = getGroupWeight(moderator);
            int targetWeight = getGroupWeight(target);

            if (targetWeight >= moderatorWeight) {
                sendMessage(sender, "messages.moderation.insufficient_rank");
                return false;
            }
        }

        return true;
    }

    private int getGroupWeight(Player player) {
        if (luckPermsHandler == null) return 0;
        
        User user = luckPermsHandler.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return 0;

        Group primaryGroup = luckPermsHandler.getLuckPerms().getGroupManager()
            .getGroup(user.getPrimaryGroup());
        if (primaryGroup == null) return 0;

        return primaryGroup.getWeight().orElse(0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.moderation")) {
            sender.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        switch (label.toLowerCase()) {
            case "mute":
                handleMute(sender, args);
                break;
            case "unmute":
                handleUnmute(sender, args);
                break;
            case "ban":
                handleBan(sender, args);
                break;
            case "tempban":
                handleTempBan(sender, args);
                break;
            case "unban":
                handleUnban(sender, args);
                break;
            case "kick":
                handleKick(sender, args);
                break;
            case "freeze":
                handleFreeze(sender, args);
                break;
            case "tp":
            case "tpall":
            case "tphere":
                handleTeleport(sender, args, label.toLowerCase());
                break;
            case "burn":
                handleBurn(sender, args);
                break;
        }
        return true;
    }

    private void handleFreeze(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.freeze")) {
            sender.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return;
        }

        if (args.length < 1) {
            sendMessage(sender, "messages.moderation.freeze.usage");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "messages.moderation.player_not_found");
            return;
        }

        // Check if sender can moderate the target
        if (!canModerate(sender, target)) {
            return;
        }

        if (target.hasMetadata(FREEZE_METADATA)) {
            // Unfreeze the player
            target.removeMetadata(FREEZE_METADATA, plugin);
            
            // Broadcast unfreeze message
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                getMessage("messages.moderation.freeze.unfreeze_broadcast")
                    .replace("%player%", sender.getName())
                    .replace("%target%", target.getName())));
            
            // Send message to unfrozen player
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                getMessage("messages.moderation.freeze.unfreeze_target")));
        } else {
            // Freeze the player
            target.setMetadata(FREEZE_METADATA, new FixedMetadataValue(plugin, true));
            
            // Broadcast freeze message
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                getMessage("messages.moderation.freeze.broadcast")
                    .replace("%player%", sender.getName())
                    .replace("%target%", target.getName())));
            
            // Send message to frozen player
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                getMessage("messages.moderation.freeze.target_message")));
            
            // Log the freeze action
            logModerationAction(sender, "FREEZE", target.getName(), args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null);
        }
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.mute")) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.moderation.mute.usage");
            return true;
        }

        Player target = getTarget(sender, args[0]);
        if (target == null) return true;
        if (!canModerate(sender, target)) return true;

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        luckPermsHandler.mutePlayer(target.getUniqueId(), null, reason);

        broadcastMessage("messages.moderation.mute.broadcast",
            "%player%", sender.getName(),
            "%target%", target.getName(),
            "%reason%", reason);

        sendMessage(target, "messages.moderation.mute.target_message",
            "%reason%", reason);
        
        // Log the mute action
        logModerationAction(sender, "MUTE", target.getName(), reason);
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.unmute")) return true;
        if (args.length < 1) {
            sendMessage(sender, "messages.moderation.unmute.usage");
            return true;
        }

        Player target = getTarget(sender, args[0]);
        if (target == null) return true;

        luckPermsHandler.unmutePlayer(target.getUniqueId());
        
        broadcastMessage("messages.moderation.unmute.broadcast",
            "%player%", sender.getName(),
            "%target%", target.getName());

        sendMessage(target, "messages.moderation.unmute.target_message");
        return true;
    }

    private boolean handleBan(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.ban")) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.moderation.ban.usage");
            return true;
        }

        String targetNameOrIp = args[0];
        if (!IP_PATTERN.matcher(targetNameOrIp).matches()) {
            Player target = getTarget(sender, targetNameOrIp);
            if (target != null && !canModerate(sender, target)) return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String formattedReason = formatMessage("messages.moderation.ban.kick_message")
            .replace("%reason%", reason);

        if (IP_PATTERN.matcher(targetNameOrIp).matches()) {
            if (BanLookup.isIpBanned(targetNameOrIp)) {
                sendMessage(sender, "messages.moderation.ban.already_banned");
                return true;
            }
            BanLookup.banIp(targetNameOrIp, formattedReason, sender.getName());
            broadcastMessage("messages.moderation.ban.broadcast",
                "%player%", sender.getName(),
                "%target%", targetNameOrIp,
                "%reason%", reason);
        } else {
            if (BanLookup.isBanned(targetNameOrIp)) {
                sendMessage(sender, "messages.moderation.ban.already_banned");
                return true;
            }
            BanLookup.banPlayer(targetNameOrIp, formattedReason, sender.getName());
            broadcastMessage("messages.moderation.ban.broadcast",
                "%player%", sender.getName(),
                "%target%", targetNameOrIp,
                "%reason%", reason);
        }
        
        // Log the ban action
        logModerationAction(sender, "BAN", targetNameOrIp, reason);
        return true;
    }

    private boolean handleTempBan(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.tempban")) return true;
        if (args.length < 3) {
            sendMessage(sender, "messages.moderation.tempban.usage");
            return true;
        }

        String targetName = args[0];
        long duration = parseDuration(args[1]);
        if (duration < 0) {
            sendMessage(sender, "messages.moderation.tempban.invalid_duration");
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Date expiry = new Date(System.currentTimeMillis() + duration);
        
        BanLookup.tempBanPlayer(targetName, reason, expiry, sender.getName());
        
        broadcastMessage("messages.moderation.tempban.broadcast",
            "%player%", sender.getName(),
            "%target%", targetName,
            "%duration%", formatDuration(duration),
            "%reason%", reason);
        return true;
    }

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.unban")) return true;
        if (args.length < 1) {
            sendMessage(sender, "messages.moderation.unban.usage");
            return true;
        }

        String targetName = args[0];
        if (!BanLookup.isBanned(targetName)) {
            sendMessage(sender, "messages.moderation.unban.not_banned");
            return true;
        }

        BanLookup.unbanPlayer(targetName);
        
        broadcastMessage("messages.moderation.unban.broadcast",
            "%player%", sender.getName(),
            "%target%", targetName);
        return true;
    }

    private boolean handleKick(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.kick")) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.moderation.kick.usage");
            return true;
        }

        Player target = getTarget(sender, args[0]);
        if (target == null) return true;

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String formattedReason = formatMessage("messages.moderation.kick.kick_message")
            .replace("%reason%", reason);

        target.kickPlayer(formattedReason);
        
        broadcastMessage("messages.moderation.kick.broadcast",
            "%player%", sender.getName(),
            "%target%", target.getName(),
            "%reason%", reason);
        
        // Log the kick action
        logModerationAction(sender, "KICK", target.getName(), reason);
        return true;
    }

    private void handleTeleport(CommandSender sender, String[] args, String commandType) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.tp")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return;
        }

        switch (commandType) {
            case "tp":
                if (args.length == 0) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.invalid")));
                    return;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.no_target")));
                    return;
                }

                if (target.equals(player)) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.self_teleport")));
                    return;
                }

                // Log the teleport action
                logModerationAction(sender, "TELEPORT", target.getName(), null);
                
                player.teleport(target);
                player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.success")
                    .replace("%target%", target.getName())));
                break;

            case "tpall":
                int teleportedCount = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(player)) {
                        p.teleport(player);
                        teleportedCount++;
                    }
                }

                // Log the teleport all action
                logModerationAction(sender, "TELEPORT_ALL", String.valueOf(teleportedCount) + " players", null);
                
                player.sendMessage(Utils.colorize(getMessage("messages.moderation.tpall.success")));
                break;

            case "tphere":
                if (args.length == 0) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.invalid")));
                    return;
                }

                Player targetHere = Bukkit.getPlayer(args[0]);
                if (targetHere == null) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.no_target")));
                    return;
                }

                if (targetHere.equals(player)) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.self_teleport")));
                    return;
                }

                // Log the teleport here action
                logModerationAction(sender, "TELEPORT_HERE", targetHere.getName(), null);
                
                targetHere.teleport(player);
                player.sendMessage(Utils.colorize(getMessage("messages.moderation.tphere.success")
                    .replace("%target%", targetHere.getName())));
                break;

            default:
                player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.invalid")));
                break;
        }
    }

    private void handleBurn(CommandSender sender, String[] args) {
        // Check permission for burn command
        if (!checkPermission(sender, "eyn.burn")) {
            sender.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return;
        }

        // Validate arguments
        if (args.length < 2) {
            sender.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.usage")));
            return;
        }

        // Find target player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Utils.colorize(getMessage("messages.moderation.player_not_found")));
            return;
        }

        // Prevent self-burn for player senders
        if (sender instanceof Player playerSender) {
            if (playerSender.getName().equals(target.getName())) {
                sender.sendMessage(Utils.colorize(getMessage("messages.moderation.self_target")));
                return;
            }
        }

        // Parse burn duration, default to 5 seconds if not specified or invalid
        int duration = 5;
        if (args.length > 2) {
            try {
                duration = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.invalid_duration")));
                return;
            }
        }

        // Set fire ticks (1 second = 20 ticks)
        target.setFireTicks(duration * 20);

        // Log the burn action
        logModerationAction(sender, "BURN", target.getName(), duration + " seconds");

        // Send messages
        target.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.target_message")
            .replace("%duration%", String.valueOf(duration))));
        
        sender.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.sender_message")
            .replace("%target%", target.getName())
            .replace("%duration%", String.valueOf(duration))));
    }

    private long parseDuration(String duration) {
        Matcher matcher = DURATION_PATTERN.matcher(duration);
        if (!matcher.matches()) return -1;
        
        long value = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).charAt(0);
        
        return switch (unit) {
            case 'd' -> TimeUnit.DAYS.toMillis(value);
            case 'h' -> TimeUnit.HOURS.toMillis(value);
            case 'm' -> TimeUnit.MINUTES.toMillis(value);
            case 's' -> TimeUnit.SECONDS.toMillis(value);
            default -> -1;
        };
    }

    private String formatDuration(long millis) {
        if (millis < TimeUnit.MINUTES.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toSeconds(millis) + "s";
        }
        if (millis < TimeUnit.HOURS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toMinutes(millis) + "m";
        }
        if (millis < TimeUnit.DAYS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toHours(millis) + "h";
        }
        return TimeUnit.MILLISECONDS.toDays(millis) + "d";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        if (!sender.hasPermission("eyn." + cmd)) {
            return super.onTabComplete(sender, command, alias, args);
        }

        if (args.length == 1) {
            if (cmd.equals("unban")) {
                // Get all banned players using our BanLookup utility
                Set<String> bannedPlayers = new HashSet<>();
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    if (BanLookup.isBanned(player.getName())) {
                        bannedPlayers.add(player.getName());
                    }
                }
                return filterStartingWith(new ArrayList<>(bannedPlayers), args[0]);
            }
            return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }

        if (args.length == 2 && cmd.equals("tempban")) {
            return Arrays.asList("1d", "2d", "7d", "14d", "30d",
                               "1h", "2h", "4h", "8h", "12h",
                               "30m", "1m", "5m", "10m", "15m");
        }

        return super.onTabComplete(sender, command, alias, args);
    }
} 