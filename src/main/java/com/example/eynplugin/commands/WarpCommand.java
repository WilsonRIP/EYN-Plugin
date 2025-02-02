package com.example.eynplugin.commands;

import com.example.eynplugin.EYNPlugin;
import com.example.eynplugin.api.LuckPermsHandler;
import net.md_5.bungee.api.ChatColor; // Bungee chat colors for TextComponent
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles warp-related commands:
 * - /warp: Teleport to a warp.
 * - /setwarp: Set a new warp.
 * - /delwarp: Delete an existing warp.
 * - /warplist: List available warps (with clickable teleport).
 * - /renamewarp: Rename a warp.
 */
public class WarpCommand extends BaseCommand {
    private final File warpsFile;
    private FileConfiguration warpsConfig;
    private final EYNPlugin plugin;

    /**
     * Constructs a new WarpCommand instance.
     *
     * @param luckPermsHandler the LuckPerms handler
     * @param messagesConfig   the messages configuration
     * @param dataFolder       the plugin's data folder
     * @param plugin           the main plugin instance (for logging)
     */
    public WarpCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, File dataFolder, EYNPlugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
        this.warpsFile = new File(dataFolder, "warps.yml");
        loadWarps();
    }

    /**
     * Loads the warps configuration from file, creating it if necessary.
     */
    private void loadWarps() {
        if (!warpsFile.exists()) {
            try {
                if (warpsFile.createNewFile()) {
                    plugin.getLogger().info("Created warps.yml file.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create warps.yml: " + e.getMessage());
            }
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    /**
     * Saves the warps configuration to file.
     */
    private void saveWarps() {
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warps.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color(getMessage("messages.player_only_command")));
            return true;
        }
        final Player player = (Player) sender;
        if (!checkPermission(player, "eyn.warp")) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }

        // Use command.getName() for consistent handling of aliases.
        final String cmdName = command.getName().toLowerCase();
        switch (cmdName) {
            case "warp":
                if (args.length < 1) {
                    player.sendMessage(color(getMessage("messages.warp.usage")));
                    return true;
                }
                teleportToWarp(player, args[0]);
                break;
            case "setwarp":
                if (args.length < 1) {
                    player.sendMessage(color(getMessage("messages.warp.set_usage")));
                    return true;
                }
                setWarp(player, args[0]);
                break;
            case "delwarp":
                if (args.length < 1) {
                    player.sendMessage(color(getMessage("messages.warp.del_usage")));
                    return true;
                }
                deleteWarp(player, args[0]);
                break;
            case "warplist":
                listWarps(player);
                break;
            case "renamewarp":
                if (args.length < 2) {
                    player.sendMessage(color(getMessage("messages.warp.rename_usage")));
                    return true;
                }
                renameWarp(player, args[0], args[1]);
                break;
            default:
                player.sendMessage(color(getMessage("messages.error.generic")));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        final Player player = (Player) sender;
        if (!checkPermission(player, "eyn.warp")) {
            return Collections.emptyList();
        }
        final String cmdName = command.getName().toLowerCase();
        if ((cmdName.equals("warp") || cmdName.equals("delwarp") || cmdName.equals("renamewarp")) && args.length == 1) {
            final String partial = args[0].toLowerCase();
            final Set<String> keys = warpsConfig.getKeys(false);
            return keys.stream()
                    .filter(warp -> warp.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Teleports the player to the warp location.
     *
     * @param player   the player to teleport.
     * @param warpName the name of the warp.
     */
    private void teleportToWarp(final Player player, final String warpName) {
        if (!warpsConfig.contains(warpName)) {
            player.sendMessage(color(getMessage("messages.warp.not_found").replace("%warp%", warpName)));
            return;
        }
        final Location loc = (Location) warpsConfig.get(warpName);
        if (loc == null) {
            player.sendMessage(color(getMessage("messages.error.generic")));
            return;
        }
        player.teleport(loc);
        player.sendMessage(color(getMessage("messages.warp.teleported").replace("%warp%", warpName)));
    }

    /**
     * Sets a warp at the player's current location.
     *
     * @param player   the player setting the warp.
     * @param warpName the desired warp name.
     */
    private void setWarp(final Player player, final String warpName) {
        if (warpsConfig.contains(warpName)) {
            player.sendMessage(color(getMessage("messages.warp.already_exists").replace("%warp%", warpName)));
            return;
        }
        warpsConfig.set(warpName, player.getLocation());
        saveWarps();
        player.sendMessage(color(getMessage("messages.warp.set").replace("%warp%", warpName)));
    }

    /**
     * Deletes an existing warp.
     *
     * @param player   the player deleting the warp.
     * @param warpName the name of the warp to delete.
     */
    private void deleteWarp(final Player player, final String warpName) {
        if (!warpsConfig.contains(warpName)) {
            player.sendMessage(color(getMessage("messages.warp.not_found").replace("%warp%", warpName)));
            return;
        }
        warpsConfig.set(warpName, null);
        saveWarps();
        player.sendMessage(color(getMessage("messages.warp.deleted").replace("%warp%", warpName)));
    }

    /**
     * Sends a clickable list of available warps to the player.
     *
     * @param player the player to receive the list.
     */
    private void listWarps(final Player player) {
        final Set<String> warps = warpsConfig.getKeys(false);
        if (warps.isEmpty()) {
            player.sendMessage(color(getMessage("messages.warp.no_warps")));
            return;
        }
        player.sendMessage(color(getMessage("messages.warp.list_header")));

        final TextComponent message = new TextComponent();
        final List<String> sortedWarps = new ArrayList<>(warps);
        Collections.sort(sortedWarps);
        boolean first = true;
        for (final String warp : sortedWarps) {
            if (!first) {
                message.addExtra(new TextComponent(", "));
            }
            first = false;
            final TextComponent warpComponent = new TextComponent(warp);
            warpComponent.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            warpComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
            warpComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to teleport to " + warp)));
            message.addExtra(warpComponent);
        }
        player.spigot().sendMessage(message);
    }

    /**
     * Renames an existing warp.
     *
     * @param player  the player renaming the warp.
     * @param oldName the current warp name.
     * @param newName the new warp name.
     */
    private void renameWarp(final Player player, final String oldName, final String newName) {
        if (!warpsConfig.contains(oldName)) {
            player.sendMessage(color(getMessage("messages.warp.not_found").replace("%warp%", oldName)));
            return;
        }
        if (warpsConfig.contains(newName)) {
            player.sendMessage(color(getMessage("messages.warp.already_exists").replace("%warp%", newName)));
            return;
        }
        final Location loc = (Location) warpsConfig.get(oldName);
        warpsConfig.set(newName, loc);
        warpsConfig.set(oldName, null);
        saveWarps();
        player.sendMessage(color(getMessage("messages.warp.renamed")
                .replace("%old%", oldName)
                .replace("%new%", newName)));
    }

    /**
     * Translates color codes in a message.
     *
     * @param message the message to colorize.
     * @return the colorized message.
     */
    private String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
