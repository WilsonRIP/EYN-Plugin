package com.example.eynplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listener for player quit events.
 * Sends a goodbye message to the player when they leave the server.
 */
public class PlayerQuitListener implements Listener {

    /**
     * Constructs a new PlayerQuitListener and registers it with the plugin.
     *
     * @param plugin the main plugin instance.
     */
    public PlayerQuitListener(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when a player quits the server.
     * Sends a goodbye message to the quitting player.
     *
     * @param event the PlayerQuitEvent.
     */
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final String playerName = event.getPlayer().getName();
        event.getPlayer().sendMessage("Goodbye, " + playerName + "!");
    }
}
