package com.example.eynplugin.commands;

import com.example.eynplugin.storage.HomeManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Handles home-related commands for players.
 * <p>
 * Supported commands:
 * - /home <name>                 : Teleports to a home.
 * - /home set <name>             : Sets a home.
 * - /home del (or delete) <name> : Deletes a home.
 * - /home rename <old> <new>     : Renames a home.
 * </p>
 */
public class HomeCommand implements org.bukkit.command.CommandExecutor {

    private final HomeManager homeManager;
    private final FileConfiguration config;
    private final FileConfiguration messagesConfig;

    /**
     * Constructs a new HomeCommand.
     *
     * @param homeManager    the home manager instance.
     * @param config         the main configuration file.
     * @param messagesConfig the messages configuration file.
     */
    public HomeCommand(final HomeManager homeManager, final FileConfiguration config, final FileConfiguration messagesConfig) {
        this.homeManager = homeManager;
        this.config = config;
        this.messagesConfig = messagesConfig;
    }

    /**
     * Processes the /home command.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    the command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        final Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(formatMessage("messages.home.usage"));
            return true;
        }

        final String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "set":
            case "sethome":
                if (args.length < 2) {
                    player.sendMessage(formatMessage("messages.home.sethome.usage"));
                    return true;
                }
                setHome(player, args[1]);
                break;
            case "del":
            case "delete":
            case "delhome":
                if (args.length < 2) {
                    player.sendMessage(formatMessage("messages.home.delhome.usage"));
                    return true;
                }
                deleteHome(player, args[1]);
                break;
            case "rename":
            case "renamehome":
                if (args.length < 3) {
                    player.sendMessage(formatMessage("messages.home.rename.usage"));
                    return true;
                }
                renameHome(player, args[1], args[2]);
                break;
            default:
                teleportToHome(player, args[0]);
                break;
        }
        return true;
    }

    /**
     * Sets a home for the player.
     *
     * @param player   the player setting the home.
     * @param homeName the name of the home.
     */
    private void setHome(final Player player, final String homeName) {
        if (!player.hasPermission("eyn.home.set")) {
            sendNoPermission(player);
            return;
        }

        final int maxHomes = getMaxHomes(player);
        if (homeManager.getHomeCount(player.getUniqueId()) >= maxHomes && !player.hasPermission("eyn.home.unlimited")) {
            player.sendMessage(formatMessage("messages.home.sethome.limit").replace("%max%", String.valueOf(maxHomes)));
            return;
        }

        homeManager.setHome(player.getUniqueId(), homeName, player.getLocation());
        player.sendMessage(formatMessage("messages.home.sethome.success").replace("%name%", homeName));
    }

    /**
     * Deletes a player's home.
     *
     * @param player   the player deleting the home.
     * @param homeName the name of the home to delete.
     */
    private void deleteHome(final Player player, final String homeName) {
        if (!player.hasPermission("eyn.home.delete")) {
            sendNoPermission(player);
            return;
        }

        if (!homeManager.deleteHome(player.getUniqueId(), homeName)) {
            player.sendMessage(formatMessage("messages.home.not_found"));
            return;
        }
        player.sendMessage(formatMessage("messages.home.delhome.success").replace("%name%", homeName));
    }

    /**
     * Renames an existing home for the player.
     *
     * @param player  the player renaming the home.
     * @param oldName the current name of the home.
     * @param newName the new name for the home.
     */
    private void renameHome(final Player player, final String oldName, final String newName) {
        if (!player.hasPermission("eyn.home.rename")) {
            sendNoPermission(player);
            return;
        }

        if (!homeManager.renameHome(player.getUniqueId(), oldName, newName)) {
            player.sendMessage(formatMessage("messages.home.not_found"));
            return;
        }

        player.sendMessage(formatMessage("messages.home.rename.success")
                .replace("%old%", oldName)
                .replace("%new%", newName));
    }

    /**
     * Teleports the player to the specified home.
     *
     * @param player   the player to teleport.
     * @param homeName the name of the home.
     */
    private void teleportToHome(final Player player, final String homeName) {
        if (!player.hasPermission("eyn.home.teleport")) {
            sendNoPermission(player);
            return;
        }

        final Location home = homeManager.getHome(player.getUniqueId(), homeName);
        if (home == null) {
            player.sendMessage(formatMessage("messages.home.not_found"));
            return;
        }
        player.teleport(home);
        player.sendMessage(formatMessage("messages.home.teleport.success").replace("%name%", homeName));
    }

    /**
     * Determines the maximum number of homes a player can have based on their permissions.
     *
     * @param player the player to check.
     * @return the maximum number of homes.
     */
    private int getMaxHomes(final Player player) {
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("eyn.home.limit." + i)) {
                return i;
            }
        }
        return config.getInt("homes.default_limit", 3);
    }

    /**
     * Formats a message from the messages configuration by applying alternate color codes.
     *
     * @param key the configuration key for the message.
     * @return the formatted message.
     */
    private String formatMessage(final String key) {
        return ChatColor.translateAlternateColorCodes('&',
                messagesConfig.getString(key, "&cMessage not found: " + key));
    }

    /**
     * Sends a standard no-permission message to the player.
     *
     * @param player the player to notify.
     */
    private void sendNoPermission(final Player player) {
        player.sendMessage(formatMessage("messages.no_permission"));
    }
}
