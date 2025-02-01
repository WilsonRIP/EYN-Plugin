package com.example.eynplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FreezeListener implements Listener {
    private static final String FREEZE_METADATA = "frozen";

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(FREEZE_METADATA)) {
            // Allow head movement but prevent position changes
            if (event.getTo().getX() != event.getFrom().getX() ||
                event.getTo().getY() != event.getFrom().getY() ||
                event.getTo().getZ() != event.getFrom().getZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Remove freeze metadata when player leaves
        if (player.hasMetadata(FREEZE_METADATA)) {
            player.removeMetadata(FREEZE_METADATA, player.getServer().getPluginManager().getPlugin("EYNPlugin"));
        }
    }
} 