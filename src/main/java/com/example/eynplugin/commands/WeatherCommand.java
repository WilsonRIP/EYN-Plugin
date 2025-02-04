package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class WeatherCommand extends BaseCommand {

    public WeatherCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the weather command.
     *
     * @param sender  The source of the command.
     * @param command The command that was executed.
     * @param label   The alias of the command used.
     * @param args    The command arguments.
     * @return true if the command was processed successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure that only players can run this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getMessage("messages.player_only_command")));
            return true;
        }

        final Player player = (Player) sender;

        // Check for the required permission.
        if (!Utils.checkPermission(player, "eyn.weather")) {
            player.sendMessage(colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Validate that a weather type argument is provided.
        if (args.length == 0) {
            sendMessage(player, "messages.weather.invalid");
            return true;
        }

        final String weatherArg = args[0].toLowerCase();
        final World world = player.getWorld();

        // Apply weather changes based on the argument.
        switch (weatherArg) {
            case "rain":
                applyWeather(world, true, false);
                sendMessage(player, "messages.weather.rain");
                break;
            case "thunder":
                applyWeather(world, true, true);
                sendMessage(player, "messages.weather.thunder");
                break;
            case "clear":
            case "sunny":
                applyWeather(world, false, false);
                sendMessage(player, "messages.weather.clear");
                break;
            default:
                sendMessage(player, "messages.weather.invalid");
                break;
        }
        return true;
    }

    /**
     * Provides tab completion for the weather command.
     *
     * @param sender  The source of the command.
     * @param command The command that was executed.
     * @param alias   The alias used.
     * @param args    The command arguments.
     * @return A list of suggestions for tab completion.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Provide suggestions for the first argument.
        if (args.length == 1) {
            return Collections.unmodifiableList(List.of("rain", "thunder", "clear", "sunny"));
        }
        return Collections.emptyList();
    }

    /**
     * Sends a colorized message to the specified player.
     *
     * @param player     The player to send the message to.
     * @param messageKey The key used to retrieve the message from the configuration.
     */
    private void sendMessage(Player player, String messageKey) {
        player.sendMessage(colorize(getMessage(messageKey)));
    }

    /**
     * Translates alternate color codes in the given message.
     *
     * @param message The message to colorize.
     * @return The colorized message.
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Applies the specified weather settings to the given world.
     *
     * @param world       The world to modify.
     * @param isStorming  True if the world should have a storm.
     * @param isThundering True if the world should be thundering.
     */
    private void applyWeather(World world, boolean isStorming, boolean isThundering) {
        world.setStorm(isStorming);
        world.setThundering(isThundering);
    }
}
