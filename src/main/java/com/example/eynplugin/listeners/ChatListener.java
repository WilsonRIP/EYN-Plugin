package com.example.eynplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ChatListener implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration messagesConfig;

    public ChatListener(final JavaPlugin plugin, final FileConfiguration messagesConfig) {
        this.plugin = plugin;
        this.messagesConfig = messagesConfig;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        event.setMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Could not find message key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 