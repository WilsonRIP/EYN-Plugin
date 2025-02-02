package com.example.eynplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.api.LuckPermsHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClearChatCommand extends BaseCommand {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_SECONDS = 3;

    public ClearChatCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If sender is not a player, simply clear chat using sender's name
        if (!(sender instanceof Player)) {
            clearChat(sender.getName());
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.clearchat")) {
            return true;
        }

        long now = System.currentTimeMillis();
        long lastUsed = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if ((now - lastUsed) < (COOLDOWN_SECONDS * 1000L)) {
            long secondsLeft = COOLDOWN_SECONDS - ((now - lastUsed) / 1000);
            sendMessage(player, "messages.clearchat.cooldown", "%seconds%", String.valueOf(secondsLeft));
            return true;
        }

        // Update cooldown and clear chat
        cooldowns.put(player.getUniqueId(), now);
        clearChat(player.getName());
        return true;
    }

    private void clearChat(String clearer) {
        // Using Java 11 String.repeat to generate 100 blank lines
        String blankLines = "\n".repeat(100);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(blankLines);
            sendMessage(player, "messages.clearchat.cleared", "%player%", clearer);
        }
    }
} 