package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Command to display detailed player information.
 * Provides options to view various data (e.g., gamemode, health, location, IP, UUID)
 * with support for copying certain data and a "blur" toggle to hide sensitive details.
 */
public class PlayerInfoCommand extends BaseCommand {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Set<UUID> BLUR_ENABLED = new HashSet<>();
    private static final String BLUR_TEXT = "••••••••";

    public PlayerInfoCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPlayer(sender)) return true;
        if (!checkPermission(sender, "eyn.playerinfo")) return true;

        final Player player = (Player) sender;

        // Toggle blur if the first argument is "blur"
        if (args.length > 0 && args[0].equalsIgnoreCase("blur")) {
            toggleBlur(player);
            return true;
        }
        
        final Player target;
        if (args.length > 0) {
            target = getTarget(sender, args[0]);
            if (target == null) return true;
        } else {
            target = player;
        }

        displayPlayerInfo(player, target);
        return true;
    }

    /**
     * Toggles the blur mode for the player.
     * When enabled, sensitive details (e.g., IP and UUID) will be replaced with a placeholder.
     *
     * @param player The player toggling the blur mode.
     */
    private void toggleBlur(final Player player) {
        if (BLUR_ENABLED.contains(player.getUniqueId())) {
            BLUR_ENABLED.remove(player.getUniqueId());
            sendMessage(player, "messages.playerinfo.blur_disabled");
        } else {
            BLUR_ENABLED.add(player.getUniqueId());
            sendMessage(player, "messages.playerinfo.blur_enabled");
        }
    }

    /**
     * Displays detailed information about the target player to the sender.
     *
     * @param sender The player requesting the information.
     * @param target The player whose information is being displayed.
     */
    private void displayPlayerInfo(final Player sender, final Player target) {
        final Location loc = target.getLocation();
        final String firstPlayed = DATE_FORMAT.format(new Date(target.getFirstPlayed()));
        final boolean isBlurred = BLUR_ENABLED.contains(sender.getUniqueId());
        
        sendMessage(sender, "messages.playerinfo.header", "%player%", target.getName());
        sendMessage(sender, "messages.playerinfo.gamemode", "%gamemode%", target.getGameMode().toString());
        sendMessage(sender, "messages.playerinfo.health", "%health%", String.format("%.1f", target.getHealth()));
        sendMessage(sender, "messages.playerinfo.food", "%food%", String.valueOf(target.getFoodLevel()));

        // Location with copy button
        final String locationStr = String.format("%s, X: %.2f, Y: %.2f, Z: %.2f", 
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
        sendCopyableMessage(sender, "messages.playerinfo.location", locationStr, 
                "Click to copy location", locationStr);

        if (checkPermission(sender, "eyn.playerinfo.advanced")) {
            // IP Address with blur option and copy button
            final String ip = target.getAddress().getAddress().getHostAddress();
            sendCopyableMessage(sender, "messages.playerinfo.ip", 
                    isBlurred ? BLUR_TEXT : ip, 
                    "Click to copy IP address", ip);

            // UUID with copy button
            final String uuidStr = target.getUniqueId().toString();
            sendCopyableMessage(sender, "messages.playerinfo.uuid", 
                    isBlurred ? BLUR_TEXT : uuidStr,
                    "Click to copy UUID", uuidStr);
        }
        
        sendMessage(sender, "messages.playerinfo.first_played", "%date%", firstPlayed);
        sendMessage(sender, "messages.playerinfo.is_vanished", 
                "%vanished%", target.hasMetadata("vanished") ? "Yes" : "No");
        sendMessage(sender, "messages.playerinfo.is_op", 
                "%op%", target.isOp() ? "Yes" : "No");
        sendMessage(sender, "messages.playerinfo.footer");
    }

    /**
     * Sends a message to the player after replacing placeholders.
     *
     * @param player       The player to receive the message.
     * @param messageKey   The key used to retrieve the message from the configuration.
     * @param placeholders An array of placeholder-replacement pairs.
     */
    private void sendMessage(Player player, String messageKey, String... placeholders) {
        String message = getMessage(messageKey);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        player.sendMessage(colorize(message));
    }

    /**
     * Sends a clickable and hoverable message that allows the player to copy text.
     *
     * @param player      The player to send the message to.
     * @param messageKey  The key for the base message.
     * @param displayText The text to display for the clickable part.
     * @param hoverText   The text to show when hovering over the clickable part.
     * @param copyText    The text to copy when the clickable part is clicked.
     */
    private void sendCopyableMessage(final Player player, final String messageKey,
                                     final String displayText, final String hoverText, final String copyText) {
        final TextComponent baseComponent = new TextComponent(formatMessage(messageKey));
        final TextComponent clickablePart = new TextComponent(" " + displayText);
        clickablePart.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText));
        clickablePart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                Collections.singletonList(new Text(hoverText))));
        baseComponent.addExtra(clickablePart);
        player.spigot().sendMessage(baseComponent);
    }

    /**
     * Provides tab completion suggestions.
     * Suggests online player names and "blur" if it matches.
     *
     * @param sender  The command sender.
     * @param command The command.
     * @param alias   The alias used.
     * @param args    The current command arguments.
     * @return A list of matching suggestions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> completions = new ArrayList<>(filterStartingWith(getOnlinePlayerNames(), args[0]));
            if ("blur".startsWith(args[0].toLowerCase())) {
                completions.add("blur");
            }
            return completions;
        }
        return new ArrayList<>();
    }
    
    /**
     * Colorizes a string by translating alternate color codes.
     *
     * @param message The raw message.
     * @return The colorized message.
     */
    private String colorize(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
}
