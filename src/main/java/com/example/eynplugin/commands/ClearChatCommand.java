package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command that clears the chat for all online players.
 * Implements a cooldown for players who use the command.
 */
public class ClearChatCommand extends BaseCommand {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_SECONDS = 3;

    /**
     * Constructs a new ClearChatCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration for messages.
     */
    public ClearChatCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Processes the /clearchat command.
     * If the sender is not a player, the chat is cleared immediately.
     * If the sender is a player, a cooldown is enforced.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    command arguments (unused).
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // For non-player senders (console), simply clear chat using sender's name.
        if (!(sender instanceof Player)) {
            clearChat(sender.getName());
            return true;
        }

        final Player player = (Player) sender;
        if (!checkPermission(player, "eyn.clearchat")) {
            return true;
        }

        final long now = System.currentTimeMillis();
        final long lastUsed = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        final long elapsed = now - lastUsed;
        if (elapsed < COOLDOWN_SECONDS * 1000L) {
            final long secondsLeft = COOLDOWN_SECONDS - (elapsed / 1000);
            sendMessage(player, "messages.clearchat.cooldown", "%seconds%", String.valueOf(secondsLeft));
            return true;
        }

        // Update cooldown and clear chat.
        cooldowns.put(player.getUniqueId(), now);
        clearChat(player.getName());
        return true;
    }

    /**
     * Clears the chat for all online players and notifies them with a message.
     *
     * @param clearer the name of the player or sender who cleared the chat.
     */
    private void clearChat(final String clearer) {
        // Generate 100 blank lines using Java 11's String.repeat method.
        final String blankLines = "\n".repeat(100);
        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(blankLines);
            sendMessage(onlinePlayer, "messages.clearchat.cleared", "%player%", clearer);
        }
    }
}
