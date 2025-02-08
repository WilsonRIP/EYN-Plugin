package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.craftbukkit.BanLookup;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Handles various moderation commands such as mute, ban, kick, freeze, and teleport.
 * Advanced moderators (those with "eyn.moderation.advanced") can bypass some restrictions
 * like rank comparisons. Moderators with "eyn.moderation.advanced.bypass.op" can moderate operators.
 */
public class ModerationCommands extends BaseCommand {
    private final LuckPermsHandler luckPermsHandler;
    private final Plugin plugin;
    private static final Logger logger = LoggerFactory.getLogger(ModerationCommands.class);
    
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([dhms])");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final String FREEZE_METADATA = "frozen";

    /**
     * Constructs a new ModerationCommands instance.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file for messages.
     * @param plugin           the main plugin instance.
     */
    public ModerationCommands(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Plugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.luckPermsHandler = luckPermsHandler;
        this.plugin = plugin;
    }

    /**
     * Logs a moderation action.
     *
     * @param sender     the command sender.
     * @param action     the action type (e.g., "BAN", "MUTE").
     * @param targetName the target player's name or identifier.
     * @param reason     the reason for the action, or null if none.
     */
    private void logModerationAction(CommandSender sender, String action, String targetName, String reason) {
        final String senderName = (sender instanceof Player) ? ((Player) sender).getName() : "CONSOLE";
        final String logMessage = String.format("%s performed %s on %s. Reason: %s", 
                senderName, action, targetName, (reason != null ? reason : "No reason provided"));
        logger.info(logMessage);
    }

    /**
     * Determines whether the sender can moderate the target.
     * <p>
     * Advanced moderators with "eyn.moderation.advanced" may bypass the group weight check.
     * Moderators without advanced permission cannot moderate players with equal or higher rank.
     * Operators are immune unless the moderator has "eyn.moderation.advanced.bypass.op".
     * </p>
     *
     * @param sender the command sender.
     * @param target the target player.
     * @return true if moderation is allowed, false otherwise.
     */
    private boolean canModerate(CommandSender sender, Player target) {
        if (target == null) return true;

        // Prevent self-moderation.
        if (sender instanceof Player && sender.getName().equals(target.getName())) {
            sendMessage(sender, "messages.moderation.self_target");
            return false;
        }

        // Operator immunity check.
        if (target.isOp() && !(sender instanceof Player && ((Player) sender).hasPermission("eyn.moderation.advanced.bypass.op"))) {
            sendMessage(sender, "messages.moderation.target_is_op");
            return false;
        }

        // Check group weight restrictions for non-advanced moderators.
        if (sender instanceof Player) {
            final Player moderator = (Player) sender;
            final int moderatorWeight = getGroupWeight(moderator);
            final int targetWeight = getGroupWeight(target);
            final boolean isAdvanced = moderator.hasPermission("eyn.moderation.advanced");
            if (!isAdvanced && targetWeight >= moderatorWeight) {
                sendMessage(sender, "messages.moderation.insufficient_rank");
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the primary group weight of a player.
     *
     * @param player the player whose group weight is to be retrieved.
     * @return the group weight, or 0 if not available.
     */
    private int getGroupWeight(Player player) {
        if (luckPermsHandler == null) return 0;
        final User user = luckPermsHandler.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return 0;
        final Group primaryGroup = luckPermsHandler.getLuckPerms().getGroupManager().getGroup(user.getPrimaryGroup());
        return (primaryGroup != null) ? primaryGroup.getWeight().orElse(0) : 0;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Utils.colorize(getMessage("player_only_command")));
            return true;
        }

        // Base permission check for moderation commands.
        if (!checkPermission(player, "eyn.moderation")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        final String cmdName = command.getName().toLowerCase();
        return switch (cmdName) {
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            case "ban" -> handleBan(sender, args);
            case "tempban" -> handleTempBan(sender, args);
            case "unban" -> handleUnban(sender, args);
            case "kick" -> handleKick(sender, args);
            case "freeze" -> handleFreeze(sender, args);
            case "tp", "tpall", "tphere" -> handleTeleport(sender, args, cmdName);
            case "burn" -> handleBurn(sender, args);
            case "warn" -> {
                // Logic for warn command
                switchWarnCommand(player, args);
                yield true;
            }
            default -> {
                player.sendMessage(Utils.colorize(getMessage("messages.unknown_command")));
                yield true;
            }
        };
    }

    /**
     * Handles the freeze command.
     */
    private void handleFreeze(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.freeze")) {
            sender.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return;
        }
        if (args.length < 1) {
            sendMessage(sender, "messages.moderation.freeze.usage");
            return;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "messages.moderation.player_not_found");
            return;
        }
        if (!canModerate(sender, target)) return;

        if (target.hasMetadata(FREEZE_METADATA)) {
            // Unfreeze the player.
            target.removeMetadata(FREEZE_METADATA, plugin);
            Bukkit.broadcastMessage(Utils.colorize(getMessage("messages.moderation.freeze.unfreeze_broadcast")
                    .replace("%player%", sender.getName())
                    .replace("%target%", target.getName())));
            target.sendMessage(Utils.colorize(getMessage("messages.moderation.freeze.unfreeze_target")));
        } else {
            // Freeze the player.
            target.setMetadata(FREEZE_METADATA, new FixedMetadataValue(plugin, true));
            Bukkit.broadcastMessage(Utils.colorize(getMessage("messages.moderation.freeze.broadcast")
                    .replace("%player%", sender.getName())
                    .replace("%target%", target.getName())));
            target.sendMessage(Utils.colorize(getMessage("messages.moderation.freeze.target_message")));
            final String reason = (args.length > 1) ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
            logModerationAction(sender, "FREEZE", target.getName(), reason);
        }
    }

    /**
     * Handles the mute command.
     *
     * @return true if processed successfully.
     */
    private boolean handleMute(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.mute")) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.moderation.mute.usage");
            return true;
        }
        final Player target = getTarget(sender, args[0]);
        if (target == null) return true;
        if (!canModerate(sender, target)) return true;

        final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        luckPermsHandler.mutePlayer(target.getUniqueId(), null, reason);
        broadcastMessage("messages.moderation.mute.broadcast",
                "%player%", sender.getName(),
                "%target%", target.getName(),
                "%reason%", reason);
        sendMessage(target, "messages.moderation.mute.target_message", "%reason%", reason);
        logModerationAction(sender, "MUTE", target.getName(), reason);
        return true;
    }

    /**
     * Handles the unmute command.
     *
     * @return true if processed successfully.
     */
    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.unmute")) return true;
        if (args.length < 1) {
            sendMessage(sender, "messages.moderation.unmute.usage");
            return true;
        }
        final Player target = getTarget(sender, args[0]);
        if (target == null) return true;
        luckPermsHandler.unmutePlayer(target.getUniqueId());
        broadcastMessage("messages.moderation.unmute.broadcast",
                "%player%", sender.getName(),
                "%target%", target.getName());
        sendMessage(target, "messages.moderation.unmute.target_message");
        return true;
    }

    /**
     * Handles the ban command.
     *
     * @return true if processed successfully.
     */
    private boolean handleBan(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.ban")) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.moderation.ban.usage");
            return true;
        }

        final String targetIdentifier = args[0];
        final boolean isIP = IP_PATTERN.matcher(targetIdentifier).matches();
        if (!isIP) {
            final Player target = getTarget(sender, targetIdentifier);
            if (target != null && !canModerate(sender, target)) return true;
        }

        final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final String formattedReason = formatMessage("messages.moderation.ban.kick_message")
                .replace("%reason%", reason);

        if (isIP) {
            if (BanLookup.isBanned(targetIdentifier)) {
                sendMessage(sender, "messages.moderation.ban.already_banned");
                return true;
            }
            BanLookup.banPlayer(targetIdentifier, formattedReason, sender.getName());
            broadcastMessage("messages.moderation.ban.broadcast",
                    "%player%", sender.getName(),
                    "%target%", targetIdentifier,
                    "%reason%", reason);
        } else {
            if (BanLookup.isBanned(targetIdentifier)) {
                sendMessage(sender, "messages.moderation.ban.already_banned");
                return true;
            }
            BanLookup.banPlayer(targetIdentifier, formattedReason, sender.getName());
            broadcastMessage("messages.moderation.ban.broadcast",
                    "%player%", sender.getName(),
                    "%target%", targetIdentifier,
                    "%reason%", reason);
        }
        logModerationAction(sender, "BAN", targetIdentifier, reason);
        return true;
    }

    /**
     * Handles the temporary ban command.
     *
     * @return true if processed successfully.
     */
    private boolean handleTempBan(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.tempban")) return true;
        if (args.length < 3) {
            sendMessage(sender, "messages.moderation.tempban.usage");
            return true;
        }

        final String targetName = args[0];
        final long duration = parseDuration(args[1]);
        if (duration < 0) {
            sendMessage(sender, "messages.moderation.tempban.invalid_duration");
            return true;
        }

        final String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        final Date expiry = new Date(System.currentTimeMillis() + duration);
        BanLookup.tempBanPlayer(targetName, reason, expiry, sender.getName());
        broadcastMessage("messages.moderation.tempban.broadcast",
                "%player%", sender.getName(),
                "%target%", targetName,
                "%duration%", formatDuration(duration),
                "%reason%", reason);
        return true;
    }

    /**
     * Handles the unban command.
     *
     * @return true if processed successfully.
     */
    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.unban")) return true;
        if (args.length < 1) {
            sendMessage(sender, "messages.moderation.unban.usage");
            return true;
        }

        final String targetName = args[0];
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

    /**
     * Handles the kick command.
     *
     * @return true if processed successfully.
     */
    private boolean handleKick(CommandSender sender, String[] args) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, "eyn.kick")) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.moderation.kick.usage");
            return true;
        }

        final Player target = getTarget(sender, args[0]);
        if (target == null) return true;

        final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final String formattedReason = formatMessage("messages.moderation.kick.kick_message")
                .replace("%reason%", reason);
        target.kickPlayer(formattedReason);
        broadcastMessage("messages.moderation.kick.broadcast",
                "%player%", sender.getName(),
                "%target%", target.getName(),
                "%reason%", reason);
        logModerationAction(sender, "KICK", target.getName(), reason);
        return true;
    }

    /**
     * Handles teleport commands (tp, tpall, tphere).
     *
     * @param sender      the command sender.
     * @param args        command arguments.
     * @param commandType the specific teleport command type.
     */
    private void handleTeleport(CommandSender sender, String[] args, String commandType) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return;
        }
        final Player player = (Player) sender;
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
                final Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.no_target")));
                    return;
                }
                if (target.equals(player)) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.self_teleport")));
                    return;
                }
                logModerationAction(sender, "TELEPORT", target.getName(), null);
                player.teleport(target);
                player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.success")
                        .replace("%target%", target.getName())));
                break;
            case "tpall":
                int teleportedCount = 0;
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(player)) {
                        p.teleport(player);
                        teleportedCount++;
                    }
                }
                logModerationAction(sender, "TELEPORT_ALL", teleportedCount + " players", null);
                player.sendMessage(Utils.colorize(getMessage("messages.moderation.tpall.success")));
                break;
            case "tphere":
                if (args.length == 0) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.invalid")));
                    return;
                }
                final Player targetHere = Bukkit.getPlayer(args[0]);
                if (targetHere == null) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.no_target")));
                    return;
                }
                if (targetHere.equals(player)) {
                    player.sendMessage(Utils.colorize(getMessage("messages.moderation.tp.self_teleport")));
                    return;
                }
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

    /**
     * Handles the burn command.
     */
    private void handleBurn(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "eyn.burn")) {
            sender.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.usage")));
            return;
        }
        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Utils.colorize(getMessage("messages.moderation.player_not_found")));
            return;
        }
        if (sender instanceof Player playerSender && playerSender.getName().equals(target.getName())) {
            sender.sendMessage(Utils.colorize(getMessage("messages.moderation.self_target")));
            return;
        }
        int duration = 5;
        if (args.length > 2) {
            try {
                duration = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.invalid_duration")));
                return;
            }
        }
        target.setFireTicks(duration * 20);
        logModerationAction(sender, "BURN", target.getName(), duration + " seconds");
        target.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.target_message")
                .replace("%duration%", String.valueOf(duration))));
        sender.sendMessage(Utils.colorize(getMessage("messages.moderation.burn.sender_message")
                .replace("%target%", target.getName())
                .replace("%duration%", String.valueOf(duration))));
    }

    /**
     * Parses a duration string (e.g., "1d", "2h") into milliseconds.
     *
     * @param duration the duration string.
     * @return the duration in milliseconds, or -1 if invalid.
     */
    private long parseDuration(String duration) {
        final Matcher matcher = DURATION_PATTERN.matcher(duration);
        if (!matcher.matches()) return -1;
        final long value = Long.parseLong(matcher.group(1));
        final char unit = matcher.group(2).charAt(0);
        return switch (unit) {
            case 'd' -> TimeUnit.DAYS.toMillis(value);
            case 'h' -> TimeUnit.HOURS.toMillis(value);
            case 'm' -> TimeUnit.MINUTES.toMillis(value);
            case 's' -> TimeUnit.SECONDS.toMillis(value);
            default -> -1;
        };
    }

    /**
     * Formats a duration in milliseconds into a human-readable string.
     *
     * @param millis the duration in milliseconds.
     * @return a formatted duration string.
     */
    private String formatDuration(long millis) {
        if (millis < TimeUnit.MINUTES.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toSeconds(millis) + "s";
        } else if (millis < TimeUnit.HOURS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toMinutes(millis) + "m";
        } else if (millis < TimeUnit.DAYS.toMillis(1)) {
            return TimeUnit.MILLISECONDS.toHours(millis) + "h";
        } else {
            return TimeUnit.MILLISECONDS.toDays(millis) + "d";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final String cmd = command.getName().toLowerCase();
        if (!sender.hasPermission("eyn." + cmd)) {
            return super.onTabComplete(sender, command, alias, args);
        }
        if (args.length == 1) {
            if (cmd.equals("unban")) {
                final Set<String> bannedPlayers = new HashSet<>();
                for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
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
