package com.example.eynplugin.craftbukkit;

import org.bukkit.BanList;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Date;

@SuppressWarnings("deprecation")
public class BanLookup {
    private static final BanList<BanEntry<String>> banList = Bukkit.getBanList(BanList.Type.NAME);
    private static final BanList<BanEntry<String>> ipBanList = Bukkit.getBanList(BanList.Type.IP);

    public static boolean isBanned(String playerName) {
        return banList.isBanned(playerName);
    }

    public static boolean isIpBanned(String ip) {
        return ipBanList.isBanned(ip);
    }

    public static void banPlayer(String playerName, String reason, String source) {
        // Remove any existing bans first
        if (banList.isBanned(playerName)) {
            banList.pardon(playerName);
        }
        // Add the new ban
        banList.addBan(playerName, reason, (Date)null, source);
        kickIfOnline(playerName, reason);
    }

    public static void banIp(String ip, String reason, String source) {
        // Remove any existing bans first
        if (ipBanList.isBanned(ip)) {
            ipBanList.pardon(ip);
        }
        // Add the new IP ban
        ipBanList.addBan(ip, reason, (Date)null, source);
    }

    public static void tempBanPlayer(String playerName, String reason, Date expiry, String source) {
        // Remove any existing bans first
        if (banList.isBanned(playerName)) {
            banList.pardon(playerName);
        }
        // Add the new temporary ban
        banList.addBan(playerName, reason, expiry, source);
        kickIfOnline(playerName, reason);
    }

    public static void unbanPlayer(String playerName) {
        banList.pardon(playerName);
    }

    private static void kickIfOnline(String playerName, String reason) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            player.kickPlayer(reason);
        }
    }
} 