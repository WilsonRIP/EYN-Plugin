package com.example.eynplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listens for damage events on players and sends them a notification message.
 */
public class EntityDamageListener implements Listener {

    /**
     * Constructs a new EntityDamageListener and registers it with the provided plugin.
     *
     * @param plugin the main plugin instance.
     */
    public EntityDamageListener(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when an entity takes damage.
     * If the entity is a player, sends them a damage notification.
     *
     * @param event the EntityDamageEvent.
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.sendMessage("You took damage!");
        }
    }
}
