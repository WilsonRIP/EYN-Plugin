package com.example.eynplugin.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.example.eynplugin.EYNPlugin;
import com.example.eynplugin.api.LuckPermsHandler;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

/**
 * Handles warp-related commands:
 * - /warp: Teleport to a warp.
 * - /setwarp: Set a new warp.
 * - /delwarp: Delete an existing warp.
 * - /warplist: List available warps with clickable teleport links.
 * - /renamewarp: Rename a warp.
 */
public class WarpCommand extends BaseCommand {
    private final File warpsFile;
    private FileConfiguration warpsConfig;
    private final EYNPlugin plugin; // Use JavaPlugin for more general plugin access
    private static final String WARP_PERMISSION = "eyn.warp";
    private static final String WARP_SET_PERMISSION = "eyn.warp.set";
    private static final String WARP_DELETE_PERMISSION = "eyn.warp.delete";
    private static final String WARP_LIST_PERMISSION = "eyn.warp.list";
    private static final String WARP_RENAME_PERMISSION = "eyn.warp.rename";

    /**
     * Constructs a new WarpCommand instance.
     *
     * @param plugin           The main plugin instance.  Changed to JavaPlugin
     * @param luckPermsHandler the LuckPerms handler.
     * @param messagesConfig   the messages configuration.
     */
    public WarpCommand(final EYNPlugin plugin, final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        loadWarps();
    }

    /**
     * Loads the warps configuration from file, creating it if necessary.
     */
    private void loadWarps() {
        if (!warpsFile.exists()) {
            try {
                plugin.saveResource("warps.yml", false); // Use saveResource for embedded files
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save default warps.yml", e); //More descriptive
                 try {
                     if (!warpsFile.createNewFile()) {
                         plugin.getLogger().severe("Failed to create warps.yml file.");
                         return;
                     } else {
                         plugin.getLogger().info("Created warps.yml file.");
                     }
                 } catch (IOException ioException) {
                     plugin.getLogger().log(Level.SEVERE, "Failed to create warps.yml", ioException);
                     return; // Return early on failure.
                 }
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
            plugin.getLogger().log(Level.SEVERE, "Failed to save warps.yml", e);
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(getMessage("messages.player_only_command")));
            return true;
        }
        final String cmdName = command.getName().toLowerCase();

        return switch (cmdName) {
            case "warp" -> handleWarpCommand(player, args);
            case "setwarp" -> handleSetWarpCommand(player, args);
            case "delwarp" -> handleDeleteWarpCommand(player, args);
            case "warplist" -> handleWarpListCommand(player);
            case "renamewarp" -> handleRenameWarpCommand(player, args);
            default -> {
                player.sendMessage(color(getMessage("messages.error.generic")));
                yield true;
            }
        };
    }

    private boolean handleWarpCommand(Player player, String[] args) {
        if (!checkPermission(player, WARP_PERMISSION)) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(color(getMessage("messages.warp.usage")));
            return true;
        }
        return teleportToWarp(player, args[0]);
    }

    private boolean handleSetWarpCommand(Player player, String[] args) {
        if (!checkPermission(player, WARP_SET_PERMISSION)) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(color(getMessage("messages.warp.set_usage")));
            return true;
        }
        return setWarp(player, args[0]);
    }

    private boolean handleDeleteWarpCommand(Player player, String[] args) {
        if (!checkPermission(player, WARP_DELETE_PERMISSION)) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(color(getMessage("messages.warp.del_usage")));
            return true;
        }
        return deleteWarp(player, args[0]);
    }

    private boolean handleWarpListCommand(Player player) {
        if (!checkPermission(player, WARP_LIST_PERMISSION)) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }
        return listWarps(player);
    }

    private boolean handleRenameWarpCommand(Player player, String[] args) {
        if (!checkPermission(player, WARP_RENAME_PERMISSION)) {
            player.sendMessage(color(getMessage("messages.no_permission")));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(color(getMessage("messages.warp.rename_usage")));
            return true;
        }
        return renameWarp(player, args[0], args[1]);
    }

    /**
     * Teleports the player to the warp location.
     *
     * @param player   the player to teleport.
     * @param warpName the name of the warp.
     */
    private boolean teleportToWarp(final Player player, final String warpName) {
        if (!warpsConfig.contains(warpName)) {
            player.sendMessage(color(getMessage("messages.warp.not_found").replace("%warp%", warpName)));
            return true;
        }
        final Location loc = warpsConfig.getObject(warpName, Location.class); // Use getObject for cleaner type handling
        if (loc == null) {
            player.sendMessage(color(getMessage("messages.error.generic"))); //Could be more specific, like warp data corrupt
            plugin.getLogger().warning("Warp location for '" + warpName + "' is null.");
            return true;
        }
        player.teleport(loc);
        player.sendMessage(color(getMessage("messages.warp.teleported").replace("%warp%", warpName)));
        return true;
    }

    /**
     * Sets a warp at the player's current location.
     *
     * @param player   the player setting the warp.
     * @param warpName the desired warp name.
     */
    private boolean setWarp(final Player player, final String warpName) {
        if (warpsConfig.contains(warpName)) {
            player.sendMessage(color(getMessage("messages.warp.already_exists").replace("%warp%", warpName)));
            return true;
        }
        warpsConfig.set(warpName, player.getLocation());
        saveWarps();
        player.sendMessage(color(getMessage("messages.warp.set").replace("%warp%", warpName)));
        return true;
    }

    /**
     * Deletes an existing warp.
     *
     * @param player   the player deleting the warp.
     * @param warpName the name of the warp to delete.
     */
    private boolean deleteWarp(final Player player, final String warpName) {
        if (!warpsConfig.contains(warpName)) {
            player.sendMessage(color(getMessage("messages.warp.not_found").replace("%warp%", warpName)));
            return true;
        }
        warpsConfig.set(warpName, null);
        saveWarps();
        player.sendMessage(color(getMessage("messages.warp.deleted").replace("%warp%", warpName)));
        return true;
    }

    /**
     * Sends a clickable list of available warps to the player.
     *
     * @param player the player to receive the list.
     */
    private boolean listWarps(final Player player) {
        final Set<String> warps = warpsConfig.getKeys(false);
        if (warps.isEmpty()) {
            player.sendMessage(color(getMessage("messages.warp.no_warps")));
            return true;
        }
        player.sendMessage(color(getMessage("messages.warp.list_header")));

        // Build a clickable list using Bungee TextComponent.
        ComponentBuilder builder = new ComponentBuilder();
        final List<String> sortedWarps = new ArrayList<>(warps);
        Collections.sort(sortedWarps);

        for (int i = 0; i < sortedWarps.size(); i++) {
            String warp = sortedWarps.get(i);
            builder.append(warp)
                   .color(net.md_5.bungee.api.ChatColor.AQUA)
                   .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp))
                   .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to teleport to " + warp)));

            if (i < sortedWarps.size() - 1) {
                builder.append(", ").color(net.md_5.bungee.api.ChatColor.GRAY); // Consistent color for separators
            }
        }
        player.spigot().sendMessage(builder.create());
        return true;
    }

    /**
     * Renames an existing warp.
     *
     * @param player  the player renaming the warp.
     * @param oldName the current warp name.
     * @param newName the new warp name.
     */
    private boolean renameWarp(final Player player, final String oldName, final String newName) {
        if (!warpsConfig.contains(oldName)) {
            player.sendMessage(color(getMessage("messages.warp.not_found").replace("%warp%", oldName)));
            return true;
        }
        if (warpsConfig.contains(newName)) {
            player.sendMessage(color(getMessage("messages.warp.already_exists").replace("%warp%", newName)));
            return true;
        }
        final Location loc = warpsConfig.getObject(oldName, Location.class); // Use getObject
        warpsConfig.set(newName, loc);
        warpsConfig.set(oldName, null);
        saveWarps();
        player.sendMessage(color(getMessage("messages.warp.renamed")
                .replace("%old%", oldName)
                .replace("%new%", newName)));
        return true;
    }

    /**
     * Applies alternate color codes to a message.
     *
     * @param message the message to colorize.
     * @return the colorized message.
     */
    private String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "warp":
                if (args.length == 1) {
                    return filterWarps(player, args[0]);
                }
                break;
            case "delwarp":
            case "renamewarp": //First argument
                if (args.length == 1) {
                    return filterWarps(player, args[0]);
                }
                break;
        }
        return Collections.emptyList(); // Return an empty list instead of null.
    }

    private List<String> filterWarps(Player player, String start) {
        Set<String> warps = warpsConfig.getKeys(false);
        if (warps.isEmpty())
        {
            return Collections.emptyList();
        }
        return warps.stream()
                .filter(warp -> warp.toLowerCase().startsWith(start.toLowerCase()))
                .collect(Collectors.toList());
    }
}