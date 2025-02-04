package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command to toggle vanish mode for players.
 * Players can vanish themselves or vanish others if permitted.
 * Vanished players are hidden from those without the "eyn.vanish.see" permission.
 */
public class VanishCommand extends BaseCommand {

    private final Plugin plugin;
    private static final String VANISH_METADATA = "vanished";
    private static final int ACTION_BAR_FADE_TICKS = 60; // 3 seconds (20 ticks per second)
    private final Map<UUID, BukkitRunnable> actionBarTasks = new HashMap<>();

    /**
     * Constructs a new VanishCommand.
     *
     * @param luckPermsHandler The LuckPerms handler.
     * @param messagesConfig   The messages configuration.
     * @param plugin           The plugin instance.
     */
    public VanishCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Plugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }

        final Player player = (Player) sender;

        // Check vanish permission.
        if (!checkPermission(player, "eyn.vanish")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Determine command based on label.
        final String lowerLabel = label.toLowerCase();
        if (lowerLabel.equals("vanish") || lowerLabel.equals("v")) {
            // If an argument is provided, attempt to toggle vanish for that target if permitted.
            if (args.length > 0 && checkPermission(player, "eyn.vanish.others")) {
                final Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(Utils.colorize(getMessage("messages.vanish.no_target")));
                    return true;
                }
                toggleVanish(target, player);
            } else {
                toggleVanish(player, null);
            }
        } else if (lowerLabel.equals("vanishlist") || lowerLabel.equals("vlist")) {
            if (!checkPermission(player, "eyn.vanish.list")) {
                player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
                return true;
            }
            showVanishedPlayers(player);
        }
        return true;
    }

    /**
     * Sends a message to the player's action bar.
     *
     * @param player  The target player.
     * @param message The message to send.
     */
    private void sendActionBar(final Player player, final String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.colorize(message)));
    }

    /**
     * Displays a persistent action bar message for the player.
     *
     * @param player  The target player.
     * @param message The message to display.
     */
    private void showPersistentActionBar(final Player player, final String message) {
        // Cancel any existing task.
        cancelExistingActionBarTask(player);

        final BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    actionBarTasks.remove(player.getUniqueId());
                    return;
                }
                sendActionBar(player, message);
            }
        };
        task.runTaskTimer(plugin, 0L, 20L); // Update every second.
        actionBarTasks.put(player.getUniqueId(), task);
    }

    /**
     * Displays a fading action bar message for the player.
     *
     * @param player  The target player.
     * @param message The message to display.
     */
    private void showFadingActionBar(final Player player, final String message) {
        // Cancel any existing task.
        cancelExistingActionBarTask(player);

        final BukkitRunnable task = new BukkitRunnable() {
            private int ticksLeft = ACTION_BAR_FADE_TICKS;

            @Override
            public void run() {
                if (ticksLeft <= 0 || !player.isOnline()) {
                    cancel();
                    actionBarTasks.remove(player.getUniqueId());
                    return;
                }
                sendActionBar(player, message);
                ticksLeft--;
            }
        };
        task.runTaskTimer(plugin, 0L, 1L);
        actionBarTasks.put(player.getUniqueId(), task);
    }

    /**
     * Cancels any existing action bar task for the player.
     *
     * @param player The player for whom to cancel the task.
     */
    private void cancelExistingActionBarTask(final Player player) {
        final BukkitRunnable existingTask = actionBarTasks.remove(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }
    }

    /**
     * Toggles vanish mode for the target player.
     * If a commander is provided and is not the target, sends extra feedback.
     *
     * @param target    The player whose vanish state will be toggled.
     * @param commander The player who initiated the command (may be null).
     */
    private void toggleVanish(final Player target, final Player commander) {
        final boolean isVanished = target.hasMetadata(VANISH_METADATA);

        if (isVanished) {
            // Make player visible.
            target.removeMetadata(VANISH_METADATA, plugin);
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, target);
            }
            target.removePotionEffect(PotionEffectType.INVISIBILITY);

            target.sendMessage(Utils.colorize(getMessage("messages.vanish.disabled")));
            showFadingActionBar(target, getMessage("messages.vanish.disabled_actionbar"));

            if (commander != null && !commander.equals(target)) {
                commander.sendMessage(Utils.colorize(
                        getMessage("messages.vanish.disabled_other").replace("%player%", target.getName())
                ));
            }
        } else {
            // Make player invisible.
            target.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));
            for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("eyn.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, target);
                }
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

            target.sendMessage(Utils.colorize(getMessage("messages.vanish.enabled")));
            showPersistentActionBar(target, getMessage("messages.vanish.enabled_actionbar"));

            if (commander != null && !commander.equals(target)) {
                commander.sendMessage(Utils.colorize(
                        getMessage("messages.vanish.enabled_other").replace("%player%", target.getName())
                ));
            }
        }
    }

    /**
     * Displays a list of currently vanished players to the requester.
     *
     * @param player The player to show the vanished players list.
     */
    private void showVanishedPlayers(final Player player) {
        final List<String> vanishedPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasMetadata(VANISH_METADATA))
                .map(Player::getName)
                .collect(Collectors.toList());

        if (vanishedPlayers.isEmpty()) {
            player.sendMessage(Utils.colorize(getMessage("messages.vanish.no_vanished")));
        } else {
            final String playerList = String.join(", ", vanishedPlayers);
            player.sendMessage(Utils.colorize(getMessage("messages.vanish.list").replace("%players%", playerList)));
        }
    }
}
