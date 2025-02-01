package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for the online players list command.
 * Shows a list of online players with optional visibility of vanished players
 * based on permissions.
 */
public class OnlinePlayersCommand extends BaseCommand {
    private static final String PERMISSION_ONLINE = "eyn.online";
    private static final String PERMISSION_SEE_VANISHED = "eyn.vanish.see";
    private static final String VANISHED_METADATA_KEY = "vanished";

    public OnlinePlayersCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPermission(sender, PERMISSION_ONLINE)) return true;

        List<Player> visiblePlayers = getVisiblePlayers(sender);
        if (visiblePlayers.isEmpty()) {
            sendMessage(sender, "messages.online.no_players");
            return true;
        }

        displayPlayerList(sender, visiblePlayers);
        return true;
    }

    /**
     * Checks if the sender has permission to use the command.
     *
     * @param sender The command sender
     * @param permission The permission to check
     * @return true if sender has permission, false otherwise
     */
    protected boolean checkPermission(CommandSender sender, String permission) {
        if (!checkPermission(sender instanceof Player ? (Player) sender : null, permission)) {
            sendMessage(sender, "messages.no_permission");
            return false;
        }
        return true;
    }

    /**
     * Gets the list of players visible to the sender based on their permissions.
     *
     * @param sender The command sender
     * @return List of visible players
     */
    public List<Player> getVisiblePlayers(CommandSender sender) {
        boolean canSeeVanished = sender instanceof Player && 
            checkPermission((Player) sender, PERMISSION_SEE_VANISHED);

        return Bukkit.getOnlinePlayers().stream()
            .filter(player -> canSeeVanished || !isVanished(player))
            .collect(Collectors.toList());
    }

    /**
     * Checks if a player is vanished.
     *
     * @param player The player to check
     * @return true if player is vanished, false otherwise
     */
    public boolean isVanished(Player player) {
        List<MetadataValue> meta = player.getMetadata(VANISHED_METADATA_KEY);
        if (meta == null || meta.isEmpty()) return false;
        return meta.get(0).asBoolean();
    }

    /**
     * Formats and displays the player list to the sender.
     *
     * @param sender The command sender
     * @param players List of players to display
     */
    public void displayPlayerList(CommandSender sender, List<Player> players) {
        sendMessage(sender, "messages.online.header", "%count%", String.valueOf(players.size()));
        
        String playerList = players.stream()
            .map(this::formatPlayerName)
            .map(name -> ChatColor.translateAlternateColorCodes('&', name))
            .collect(Collectors.joining(ChatColor.translateAlternateColorCodes('&', "&a, ")));
        
        sendMessage(sender, "messages.online.list", "%players%", playerList);
        sendMessage(sender, "messages.online.footer");
    }

    /**
     * Formats a single player's name with vanish status if applicable.
     *
     * @param player The player to format
     * @return Formatted player name
     */
    public String formatPlayerName(Player player) {
        StringBuilder name = new StringBuilder();
        
        if (player.isOp()) {
            name.append("&c");
        } else if (checkPermission(player, "eyn.staff")) {
            name.append("&b");
        } else {
            name.append("&a");
        }
        
        name.append(player.getName());
        
        if (isVanished(player)) {
            name.append("&7(V)&a");
        }
        
        return name.toString();
    }

    /**
     * Sends a colored message to the sender from the messages configuration.
     *
     * @param sender The command sender
     * @param messageKey The message key in the configuration
     */
    protected void sendMessage(CommandSender sender, String messageKey) {
        sendMessage(sender, messageKey, placeholder -> placeholder);
    }

    /**
     * Sends a colored message to the sender from the messages configuration
     * with placeholder replacement.
     *
     * @param sender The command sender
     * @param messageKey The message key in the configuration
     * @param placeholderReplacer Function to replace placeholders in the message
     */
    protected void sendMessage(CommandSender sender, String messageKey, 
            java.util.function.Function<String, String> placeholderReplacer) {
        String message = getMessage(messageKey);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                placeholderReplacer.apply(message)));
        }
    }
}