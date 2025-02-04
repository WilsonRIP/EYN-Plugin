package com.example.eynplugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listener that notifies players when they break a block.
 */
public class BlockBreakListener implements Listener {

    /**
     * Constructs and registers the BlockBreakListener.
     *
     * @param plugin the main plugin instance.
     */
    public BlockBreakListener(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when a block is broken.
     * Sends a notification message to the player who broke the block.
     *
     * @param event the BlockBreakEvent.
     */
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        // Send a colorized message to the player.
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou broke a block!"));
    }
}
