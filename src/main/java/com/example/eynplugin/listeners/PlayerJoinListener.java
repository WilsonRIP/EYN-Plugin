package com.example.eynplugin.listeners;

import com.example.eynplugin.storage.UserStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listener for player join events.
 * <p>
 * When a player joins, this listener checks for a custom welcome message in the user storage.
 * If none is found, it creates a default welcome message, saves it, and sends it to the player.
 * </p>
 */
public class PlayerJoinListener implements Listener {

    private final UserStorage userStorage;

    /**
     * Constructs a new PlayerJoinListener and registers it with the provided plugin.
     *
     * @param plugin      the main plugin instance.
     * @param userStorage the UserStorage instance for handling user data.
     */
    public PlayerJoinListener(final JavaPlugin plugin, final UserStorage userStorage) {
        this.userStorage = userStorage;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Called when a player joins the server. Retrieves a custom welcome message from
     * user storage. If no welcome message exists, a default one is generated, saved, and sent.
     *
     * @param event the PlayerJoinEvent.
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        String welcomeMessage = userStorage.getUser(uuid);
        if (welcomeMessage == null) {
            welcomeMessage = "Welcome, " + event.getPlayer().getName() + "!";
            userStorage.saveUser(uuid, welcomeMessage);
        }
        event.getPlayer().sendMessage(welcomeMessage);
    }
}
