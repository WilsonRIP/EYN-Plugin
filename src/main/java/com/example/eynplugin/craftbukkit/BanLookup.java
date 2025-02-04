package com.example.eynplugin.craftbukkit;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanLookup {
    private static final BanList<BanEntry> banList = Bukkit.getBanList(BanList.Type.NAME);
    private static final BanList<BanEntry> ipBanList = Bukkit.getBanList(BanList.Type.IP);
    private static final Map<String, UUID> bannedPlayers = new HashMap<>(); // Store banned player UUIDs

    //... (isBanned, isIpBanned, banIp, tempBanPlayer remain the same)

    public static void unbanPlayer(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore()) {
            UUID uuid = bannedPlayers.get(offlinePlayer.getName()); // Retrieve stored UUID
            if (uuid!= null) {
                banList.removeBan(offlinePlayer.getName());  // Use name for removal
                bannedPlayers.remove(offlinePlayer.getName());
            }
        }
    }

    //... (unbanIp remains the same)

    private static void banPlayer(String playerName, String reason, Date expiry, String source) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            UUID uuid = offlinePlayer.getUniqueId();

            // Remove any previous bans (important for tempbans):
            banList.getBanEntries().stream()
                .filter(entry -> entry.getTarget().equalsIgnoreCase(uuid.toString()))
                .findFirst()
                .ifPresent(entry -> banList.removeBan(entry.getTarget()));

            if (expiry == null) {
                banList.addBan(uuid.toString(), reason, null, source);
            } else {
                Instant instant = expiry.toInstant();
                banList.addBan(uuid.toString(), reason, Date.from(instant), source);
            }

            bannedPlayers.put(offlinePlayer.getName(), uuid); // Store UUID

            if (offlinePlayer.isOnline()) {
                kickIfOnline(playerName, reason);
            }
        }
    }

    //... (kickIfOnline remains the same)
}