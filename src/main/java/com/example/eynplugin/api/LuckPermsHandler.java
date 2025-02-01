package com.example.eynplugin.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LuckPermsHandler {
    private final LuckPerms luckPerms;
    private final UserManager userManager;
    private final Map<UUID, Date> mutedPlayers = new HashMap<>();

    public LuckPermsHandler(Plugin plugin, LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        this.userManager = luckPerms.getUserManager();
    }

    public CompletableFuture<Void> addPermission(UUID playerUUID, String permission) {
        return userManager.loadUser(playerUUID).thenAccept(user -> {
            user.data().add(Node.builder(permission).build());
            userManager.saveUser(user);
        });
    }

    public CompletableFuture<Void> removePermission(UUID playerUUID, String permission) {
        return userManager.loadUser(playerUUID).thenAccept(user -> {
            user.data().remove(Node.builder(permission).build());
            userManager.saveUser(user);
        });
    }

    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    // Mute functionality
    public void mutePlayer(UUID playerUUID, Date expiryDate, String reason) {
        mutedPlayers.put(playerUUID, expiryDate);
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage("You have been muted. Reason: " + reason);
        }
    }

    public void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage("You have been unmuted.");
        }
    }

    public boolean isMuted(UUID playerUUID) {
        if (!mutedPlayers.containsKey(playerUUID)) return false;
        Date expiryDate = mutedPlayers.get(playerUUID);
        if (expiryDate != null && expiryDate.before(new Date())) {
            mutedPlayers.remove(playerUUID);
            return false;
        }
        return true;
    }
} 
