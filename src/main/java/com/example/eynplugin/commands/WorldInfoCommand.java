package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.List;

public class WorldInfoCommand extends BaseCommand {

    public WorldInfoCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command is executed by a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getMessage("messages.player_only_command")));
            return true;
        }
        
        final Player player = (Player) sender;
        
        // Check for the necessary permission.
        if (!checkPermission(player, "eyn.worldinfo")) {
            player.sendMessage(colorize(getMessage("messages.no_permission")));
            return true;
        }

        final World world = player.getWorld();
        final String worldName = world.getName();
        final long worldTime = world.getTime();
        final String weather = world.hasStorm() ? "Stormy" : "Clear";
        final int playerCount = world.getPlayers().size();

        // Send world information messages using helper method with placeholder replacements.
        sendMessage(player, "messages.worldinfo.header", "%world%", worldName);
        sendMessage(player, "messages.worldinfo.time", "%time%", String.valueOf(worldTime));
        sendMessage(player, "messages.worldinfo.weather", "%weather%", weather);
        sendMessage(player, "messages.worldinfo.players", "%count%", String.valueOf(playerCount));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // No tab completion implemented.
        return null;
    }

    /**
     * Sends a colorized message to the specified player after replacing placeholders.
     *
     * @param player The player to send the message to.
     * @param messageKey The key used to fetch the message from the configuration.
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
     * Colorizes a string by translating alternate color codes.
     *
     * @param message The raw message.
     * @return The colorized message.
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
