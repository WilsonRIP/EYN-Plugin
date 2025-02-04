package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Handles the /back command to teleport a player to their last location.
 */
public class BackCommand extends BaseCommand {

    // Stores the last known location for each player by their UUID.
    private final HashMap<UUID, Location> lastLocations = new HashMap<>();

    /**
     * Constructs a new BackCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file containing messages.
     */
    public BackCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Executes the /back command.
     * If the player has a saved last location, teleports them there.
     * Otherwise, informs the player that no location is available.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    command arguments (unused).
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure only players can execute this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("player_only_command"));
            return true;
        }
        final Player player = (Player) sender;

        // Check for permission.
        if (!Utils.checkPermission(player, "eyn.back")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        final Location lastLocation = lastLocations.get(player.getUniqueId());
        if (lastLocation != null) {
            player.teleport(lastLocation);
            player.sendMessage(formatMessage("back.teleported"));
        } else {
            player.sendMessage(formatMessage("back.no_location"));
        }
        return true;
    }

    /**
     * Updates the last location for the specified player.
     *
     * @param player   the player whose location is being saved.
     * @param location the location to save.
     */
    public void setLastLocation(final Player player, final Location location) {
        lastLocations.put(player.getUniqueId(), location);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
