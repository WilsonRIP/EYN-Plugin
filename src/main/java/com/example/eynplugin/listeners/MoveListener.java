package com.example.eynplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

public class MoveListener implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration messagesConfig;
    private static final String FREEZE_METADATA = "frozen";

    public MoveListener(final JavaPlugin plugin, final FileConfiguration messagesConfig) {
        this.plugin = plugin;
        this.messagesConfig = messagesConfig;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(FREEZE_METADATA)) {
            event.setCancelled(true);
            player.sendMessage(formatMessage("messages.moderation.freeze.movement_blocked"));
        }
    }

    private String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Could not find message key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 