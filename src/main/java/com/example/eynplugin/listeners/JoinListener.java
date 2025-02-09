package com.example.eynplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class JoinListener implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration messagesConfig;

    public JoinListener(final JavaPlugin plugin, final FileConfiguration messagesConfig) {
        this.plugin = plugin;
        this.messagesConfig = messagesConfig;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        String joinMessage = formatMessage("messages.join").replace("%player%", event.getPlayer().getName());
        event.setJoinMessage(joinMessage);
    }

    private String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Could not find message key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 