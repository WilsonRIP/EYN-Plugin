package com.example.eynplugin.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides utility methods to interact with LuckPerms and implement mute functionality.
 */
public class LuckPermsHandler {
    private final LuckPerms luckPerms;
    private final UserManager userManager;
    private final Map<UUID, Date> mutedPlayers = new HashMap<>();

    /**
     * Constructs a new LuckPermsHandler.
     *
     * @param plugin   the main plugin instance (unused currently, but may be useful for logging or scheduling tasks)
     * @param luckPerms the LuckPerms API instance.
     */
    public LuckPermsHandler(final Plugin plugin, final LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        this.userManager = luckPerms.getUserManager();
    }

    /**
     * Adds the specified permission to a player asynchronously.
     *
     * @param playerUUID the UUID of the player.
     * @param permission the permission string to add.
     * @return a CompletableFuture that completes when the permission has been added.
     */
    public CompletableFuture<Void> addPermission(final UUID playerUUID, final String permission) {
        return userManager.loadUser(playerUUID).thenAccept(user -> {
            final Node node = Node.builder(permission).build();
            user.data().add(node);
            userManager.saveUser(user);
        });
    }

    /**
     * Removes the specified permission from a player asynchronously.
     *
     * @param playerUUID the UUID of the player.
     * @param permission the permission string to remove.
     * @return a CompletableFuture that completes when the permission has been removed.
     */
    public CompletableFuture<Void> removePermission(final UUID playerUUID, final String permission) {
        return userManager.loadUser(playerUUID).thenAccept(user -> {
            final Node node = Node.builder(permission).build();
            user.data().remove(node);
            userManager.saveUser(user);
        });
    }

    /**
     * Checks if the given player has the specified permission.
     *
     * @param player     the player.
     * @param permission the permission to check.
     * @return true if the player has the permission; otherwise false.
     */
    public boolean hasPermission(final Player player, final String permission) {
        return player.hasPermission(permission);
    }

    /**
     * Returns the LuckPerms API instance.
     *
     * @return the LuckPerms instance.
     */
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    /**
     * Returns the UserManager from LuckPerms.
     *
     * @return the UserManager instance.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    // Mute functionality

    /**
     * Mutes a player.
     * The player's UUID is mapped to an optional expiry date; if expiryDate is null, the mute is permanent.
     * A muted player will receive a message notifying them of the mute.
     *
     * @param playerUUID the UUID of the player to mute.
     * @param expiryDate the expiration date of the mute; if null, the mute is permanent.
     * @param reason     the reason for muting.
     */
    public void mutePlayer(final UUID playerUUID, final Date expiryDate, final String reason) {
        mutedPlayers.put(playerUUID, expiryDate);
        final Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage("You have been muted. Reason: " + reason);
        }
    }

    /**
     * Unmutes a player.
     * The player's mute record is removed, and if the player is online, they are notified.
     *
     * @param playerUUID the UUID of the player to unmute.
     */
    public void unmutePlayer(final UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
        final Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendMessage("You have been unmuted.");
        }
    }

    /**
     * Checks if a player is currently muted.
     * If the mute has expired, it is automatically removed.
     *
     * @param playerUUID the UUID of the player.
     * @return true if the player is muted; false otherwise.
     */
    public boolean isMuted(final UUID playerUUID) {
        if (!mutedPlayers.containsKey(playerUUID)) return false;
        final Date expiryDate = mutedPlayers.get(playerUUID);
        if (expiryDate != null && expiryDate.before(new Date())) {
            mutedPlayers.remove(playerUUID);
            return false;
        }
        return true;
    }
}
