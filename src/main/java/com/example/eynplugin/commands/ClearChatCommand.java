package com.example.eynplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.api.LuckPermsHandler;
import java.util.HashMap;
import java.util.UUID;

public class ClearChatCommand extends BaseCommand {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_SECONDS = 3;

    public ClearChatCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            clearChat(sender.getName());
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.clearchat")) {
            return true;
        }

        // Check cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + COOLDOWN_SECONDS) 
                - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                sendMessage(player, "messages.clearchat.cooldown", "%seconds%", String.valueOf(secondsLeft));
                return true;
            }
        }

        // Set cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        clearChat(player.getName());
        return true;
    }

    private void clearChat(String clearer) {
        StringBuilder blank = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            blank.append("\n");
        }
        
        String blankLines = blank.toString();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(blankLines);
            sendMessage(player, "messages.clearchat.cleared", "%player%", clearer);
        }
    }
} 