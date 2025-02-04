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
 * Command handler for the /online command.
 * Displays a list of online players with optional visibility of vanished players based on permissions.
 */
public class OnlinePlayersCommand extends BaseCommand {
    private static final String PERMISSION_ONLINE = "eyn.online";
    private static final String PERMISSION_SEE_VANISHED = "eyn.vanish.see";
    private static final String VANISHED_METADATA_KEY = "vanished";

    /**
     * Constructs a new OnlinePlayersCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file for messages.
     */
    public OnlinePlayersCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Processes the /online command.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    the command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, PERMISSION_ONLINE)) {
            sendMessage(sender, "messages.no_permission");
            return true;
        }

        final List<Player> visiblePlayers = getVisiblePlayers(sender);
        if (visiblePlayers.isEmpty()) {
            sendMessage(sender, "messages.online.no_players");
            return true;
        }

        displayPlayerList(sender, visiblePlayers);
        return true;
    }

    /**
     * Checks silently whether the sender has the given permission.
     * Non-player senders (e.g. console) are assumed to have all permissions.
     *
     * @param sender     the command sender.
     * @param permission the permission to check.
     * @return true if the sender has the permission, false otherwise.
     */
    protected boolean hasPermission(CommandSender sender, String permission) {
        return (sender instanceof Player) ? ((Player) sender).hasPermission(permission) : true;
    }

    /**
     * Returns the list of players visible to the sender.
     * Vanished players are only included if the sender has permission to see them.
     *
     * @param sender the command sender.
     * @return list of visible players.
     */
    public List<Player> getVisiblePlayers(CommandSender sender) {
        final boolean canSeeVanished = (sender instanceof Player) &&
                ((Player) sender).hasPermission(PERMISSION_SEE_VANISHED);

        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> canSeeVanished || !isVanished(player))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a given player is vanished.
     *
     * @param player the player to check.
     * @return true if the player is vanished; false otherwise.
     */
    public boolean isVanished(Player player) {
        final List<MetadataValue> meta = player.getMetadata(VANISHED_METADATA_KEY);
        return meta != null && !meta.isEmpty() && meta.get(0).asBoolean();
    }

    /**
     * Formats and displays the online player list to the sender.
     *
     * @param sender  the command sender.
     * @param players the list of players to display.
     */
    public void displayPlayerList(CommandSender sender, List<Player> players) {
        sendMessage(sender, "messages.online.header", "%count%", String.valueOf(players.size()));

        final String separator = ChatColor.translateAlternateColorCodes('&', "&a, ");
        final String playerList = players.stream()
                .map(this::formatPlayerName)
                .collect(Collectors.joining(separator));

        sendMessage(sender, "messages.online.list", "%players%", playerList);
        sendMessage(sender, "messages.online.footer");
    }

    /**
     * Formats a player's name with appropriate color coding based on permissions.
     * Appends vanish status if the player is vanished.
     *
     * @param player the player to format.
     * @return the formatted player name.
     */
    public String formatPlayerName(Player player) {
        final String colorPrefix;
        if (player.isOp()) {
            colorPrefix = "&c";
        } else if (player.hasPermission("eyn.staff")) {
            colorPrefix = "&b";
        } else {
            colorPrefix = "&a";
        }

        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(colorPrefix).append(player.getName());

        if (isVanished(player)) {
            nameBuilder.append("&7(V)&a");
        }

        return ChatColor.translateAlternateColorCodes('&', nameBuilder.toString());
    }

    /**
     * Sends a colored message to the sender using the messages configuration.
     * Performs placeholder replacement using key-value pairs.
     *
     * @param sender       the command sender.
     * @param messageKey   the key to the message in the configuration.
     * @param placeholders key-value pairs for placeholder replacement.
     */
    protected void sendMessage(CommandSender sender, String messageKey, String... placeholders) {
        String message = getMessage(messageKey);
        if (message == null || message.isEmpty()) {
            return;
        }
        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
