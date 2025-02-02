package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.List;

public class WeatherCommand extends BaseCommand {

    public WeatherCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure only players can run this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getMessage("messages.player_only_command")));
            return true;
        }

        final Player player = (Player) sender;
        // Check for required permission.
        if (!Utils.checkPermission(player, "eyn.weather")) {
            player.sendMessage(colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Ensure an argument is provided.
        if (args.length == 0) {
            sendMessage(player, "messages.weather.invalid");
            return true;
        }

        final World world = player.getWorld();
        switch (args[0].toLowerCase()) {
            case "rain":
                world.setStorm(true);
                world.setThundering(false);
                sendMessage(player, "messages.weather.rain");
                break;
            case "thunder":
                world.setStorm(true);
                world.setThundering(true);
                sendMessage(player, "messages.weather.thunder");
                break;
            case "clear":
            case "sunny":
                world.setStorm(false);
                world.setThundering(false);
                sendMessage(player, "messages.weather.clear");
                break;
            default:
                sendMessage(player, "messages.weather.invalid");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Provide tab completion for the first argument only.
        if (args.length == 1) {
            return Collections.unmodifiableList(List.of("rain", "thunder", "clear", "sunny"));
        }
        return null;
    }

    /**
     * Helper method to retrieve and send a colorized message to a player.
     *
     * @param player The player to send the message to.
     * @param messageKey The key used to retrieve the message from the config.
     */
    private void sendMessage(Player player, String messageKey) {
        player.sendMessage(colorize(getMessage(messageKey)));
    }

    /**
     * Helper method for colorizing messages.
     *
     * @param message The message to colorize.
     * @return The colorized message.
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
