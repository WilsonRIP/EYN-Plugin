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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WarpCommand extends BaseCommand {
    private final File warpsFile;
    private FileConfiguration warpsConfig;

    public WarpCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, File dataFolder) {
        super(luckPermsHandler, messagesConfig);
        this.warpsFile = new File(dataFolder, "warps.yml");
        loadWarps();
    }

    private void loadWarps() {
        if (!warpsFile.exists()) {
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    private void saveWarps() {
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.warp")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.no_permission")));
            return true;
        }

        switch (label.toLowerCase()) {
            case "warp":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.warp.usage")));
                    return true;
                }
                teleportToWarp(player, args[0]);
                break;
            case "setwarp":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.warp.set_usage")));
                    return true;
                }
                setWarp(player, args[0]);
                break;
            case "delwarp":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.warp.del_usage")));
                    return true;
                }
                deleteWarp(player, args[0]);
                break;
            case "warplist":
                listWarps(player);
                break;
            case "renamewarp":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.warp.rename_usage")));
                    return true;
                }
                renameWarp(player, args[0], args[1]);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.warp")) {
            return new ArrayList<>();
        }

        switch (alias.toLowerCase()) {
            case "warp":
                if (args.length == 1) {
                    String partialName = args[0].toLowerCase();
                    return warpsConfig.getKeys(false).stream()
                        .filter(warp -> warp.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
                }
                break;
            case "delwarp":
            case "renamewarp":
                if (args.length == 1) {
                    String partialName = args[0].toLowerCase();
                    return warpsConfig.getKeys(false).stream()
                        .filter(warp -> warp.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
                }
                break;
        }
        return new ArrayList<>();
    }

    private void teleportToWarp(Player player, String warpName) {
        if (!warpsConfig.contains(warpName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.warp.not_found").replace("%warp%", warpName)));
            return;
        }

        Location loc = (Location) warpsConfig.get(warpName);
        player.teleport(loc);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            getMessage("messages.warp.teleported").replace("%warp%", warpName)));
    }

    private void setWarp(Player player, String warpName) {
        if (warpsConfig.contains(warpName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.warp.already_exists").replace("%warp%", warpName)));
            return;
        }

        warpsConfig.set(warpName, player.getLocation());
        saveWarps();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            getMessage("messages.warp.set").replace("%warp%", warpName)));
    }

    private void deleteWarp(Player player, String warpName) {
        if (!warpsConfig.contains(warpName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.warp.not_found").replace("%warp%", warpName)));
            return;
        }

        warpsConfig.set(warpName, null);
        saveWarps();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            getMessage("messages.warp.deleted").replace("%warp%", warpName)));
    }

    private void listWarps(Player player) {
        Set<String> warps = warpsConfig.getKeys(false);
        if (warps.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.warp.no_warps")));
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            getMessage("messages.warp.list_header")));

        TextComponent separator = new TextComponent(", ");
        separator.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        boolean first = true;
        for (String warp : warps) {
            if (!first) {
                player.spigot().sendMessage(separator);
            }
            first = false;

            TextComponent warpComponent = new TextComponent(warp);
            warpComponent.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            warpComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
            
            String hoverText = "Click to teleport to " + warp;
            warpComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new Text(hoverText)));
            
            player.spigot().sendMessage(warpComponent);
        }
    }

    private void renameWarp(Player player, String oldName, String newName) {
        if (!warpsConfig.contains(oldName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.warp.not_found").replace("%warp%", oldName)));
            return;
        }

        if (warpsConfig.contains(newName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                getMessage("messages.warp.already_exists").replace("%warp%", newName)));
            return;
        }

        Location loc = (Location) warpsConfig.get(oldName);
        warpsConfig.set(newName, loc);
        warpsConfig.set(oldName, null);
        saveWarps();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            getMessage("messages.warp.renamed").replace("%old%", oldName).replace("%new%", newName)));
    }
} 