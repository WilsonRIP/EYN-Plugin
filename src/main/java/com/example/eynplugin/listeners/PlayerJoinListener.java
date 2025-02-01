package com.example.eynplugin.listeners;

import com.example.eynplugin.storage.UserStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final UserStorage userStorage;

    public PlayerJoinListener(JavaPlugin plugin, UserStorage userStorage) {
        this.userStorage = userStorage;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        String welcomeMessage = userStorage.getUser(uuid);
        if (welcomeMessage == null) {
            welcomeMessage = "Welcome, " + event.getPlayer().getName() + "!";
            userStorage.saveUser(uuid, welcomeMessage);
        }
        event.getPlayer().sendMessage(welcomeMessage);
    }
} 