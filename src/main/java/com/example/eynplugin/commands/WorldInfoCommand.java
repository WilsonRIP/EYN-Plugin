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

    /**
     * Constructs a new WorldInfoCommand.
     *
     * @param luckPermsHandler The LuckPerms handler.
     * @param messagesConfig   The configuration file for messages.
     */
    public WorldInfoCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the world info command.
     *
     * @param sender  The source of the command.
     * @param command The command which was executed.
     * @param label   The alias of the command used.
     * @param args    The command arguments.
     * @return true if the command was processed successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure that only players can execute the command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getMessage("messages.player_only_command")));
            return true;
        }
        final Player player = (Player) sender;

        // Verify required permission.
        if (!checkPermission(player, "eyn.worldinfo")) {
            player.sendMessage(colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Retrieve world information.
        final World world = player.getWorld();
        final String worldName = world.getName();
        final long worldTime = world.getTime();
        final String weather = world.hasStorm() ? "Stormy" : "Clear";
        final int playerCount = world.getPlayers().size();

        // Send world information messages with placeholder replacements.
        sendMessage(player, "messages.worldinfo.header", "%world%", worldName);
        sendMessage(player, "messages.worldinfo.time", "%time%", String.valueOf(worldTime));
        sendMessage(player, "messages.worldinfo.weather", "%weather%", weather);
        sendMessage(player, "messages.worldinfo.players", "%count%", String.valueOf(playerCount));

        return true;
    }

    /**
     * Provides tab completion suggestions.
     *
     * @param sender  The source of the command.
     * @param command The command being executed.
     * @param alias   The alias used.
     * @param args    The command arguments.
     * @return A list of suggestions (none implemented here).
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // No tab completion is needed for this command.
        return null;
    }

    /**
     * Sends a colorized message to the specified player after replacing placeholders.
     *
     * @param player       The player to receive the message.
     * @param messageKey   The key to retrieve the message from the configuration.
     * @param placeholders An array of placeholder-replacement pairs.
     */
    private void sendMessage(Player player, String messageKey, String... placeholders) {
        String message = getMessage(messageKey);
        // Replace placeholders in pairs (key, value).
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        player.sendMessage(colorize(message));
    }

    /**
     * Translates alternate color codes in a string.
     *
     * @param message The raw message string.
     * @return The colorized message.
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
