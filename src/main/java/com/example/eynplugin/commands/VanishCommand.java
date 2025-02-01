package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VanishCommand extends BaseCommand {
    private final Plugin plugin;
    private static final String VANISH_METADATA = "vanished";
    private static final int ACTION_BAR_FADE_TICKS = 60; // 3 seconds (20 ticks per second)
    private final Map<UUID, BukkitRunnable> actionBarTasks = new HashMap<>();

    public VanishCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Plugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.vanish")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.no_permission")));
            return true;
        }

        switch (label.toLowerCase()) {
            case "vanish":
            case "v":
                if (args.length > 0 && checkPermission(player, "eyn.vanish.others")) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.vanish.no_target")));
                        return true;
                    }
                    toggleVanish(target, player);
                } else {
                    toggleVanish(player, null);
                }
                break;

            case "vanishlist":
            case "vlist":
                if (!checkPermission(player, "eyn.vanish.list")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.no_permission")));
                    return true;
                }
                showVanishedPlayers(player);
                break;
        }
        return true;
    }

    private void sendActionBar(Player player, String message) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(coloredMessage));
    }

    private void showPersistentActionBar(Player player, String message) {
        // Cancel any existing task
        BukkitRunnable existingTask = actionBarTasks.remove(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Create new persistent action bar task
        BukkitRunnable task = new BukkitRunnable() {
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
        
        task.runTaskTimer(plugin, 0L, 20L); // Update every second
        actionBarTasks.put(player.getUniqueId(), task);
    }

    private void showFadingActionBar(Player player, String message) {
        // Cancel any existing task
        BukkitRunnable existingTask = actionBarTasks.remove(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Create new fading action bar task
        BukkitRunnable task = new BukkitRunnable() {
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

    private void toggleVanish(Player target, Player commander) {
        boolean isVanished = target.hasMetadata(VANISH_METADATA);
        
        if (isVanished) {
            // Make player visible
            target.removeMetadata(VANISH_METADATA, plugin);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, target);
            }
            target.removePotionEffect(PotionEffectType.INVISIBILITY);
            
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.vanish.disabled")));
            showFadingActionBar(target, getMessage("messages.vanish.disabled_actionbar"));
            
            if (commander != null && !commander.equals(target)) {
                commander.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    getMessage("messages.vanish.disabled_other").replace("%player%", target.getName())));
            }
        } else {
            // Make player invisible
            target.setMetadata(VANISH_METADATA, new FixedMetadataValue(plugin, true));
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("eyn.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, target);
                }
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.vanish.enabled")));
            showPersistentActionBar(target, getMessage("messages.vanish.enabled_actionbar"));
            
            if (commander != null && !commander.equals(target)) {
                commander.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    getMessage("messages.vanish.enabled_other").replace("%player%", target.getName())));
            }
        }
    }

    private void showVanishedPlayers(Player player) {
        List<String> vanishedPlayers = Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.hasMetadata(VANISH_METADATA))
            .map(Player::getName)
            .collect(Collectors.toList());

        if (vanishedPlayers.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.vanish.no_vanished")));
        } else {
            String playerList = String.join(", ", vanishedPlayers);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.vanish.list").replace("%players%", playerList)));
        }
    }
} 