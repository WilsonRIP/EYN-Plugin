package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.plugin.Plugin;

public class NameTagCommand extends BaseCommand {
    private final Set<UUID> hiddenNameTags = new HashSet<>();
    private final Plugin plugin;

    public NameTagCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Plugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;
        Player target = player;

        if (args.length > 0) {
            if (!checkPermission(player, "eyn.nametag.others")) {
                sendMessage(player, "messages.no_permission");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(player, "messages.error.player_not_found");
                return true;
            }
        } else if (!checkPermission(player, "eyn.nametag")) {
            sendMessage(player, "messages.no_permission");
            return true;
        }

        toggleNameTagVisibility(target);
        sendToggleMessage(sender, target);
        return true;
    }

    private void toggleNameTagVisibility(Player target) {
        if (hiddenNameTags.contains(target.getUniqueId())) {
            hiddenNameTags.remove(target.getUniqueId());
            updateNameTagVisibility(target, true);
        } else {
            hiddenNameTags.add(target.getUniqueId());
            updateNameTagVisibility(target, false);
        }
    }

    private void updateNameTagVisibility(Player target, boolean visible) {
        target.setMetadata("nametag", new FixedMetadataValue(plugin, !visible));
        
        // Update visibility for all online players
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.canSee(target) && p != target) {
                p.hidePlayer(plugin, target);
                p.showPlayer(plugin, target);
            }
        });
    }

    private void sendToggleMessage(CommandSender sender, Player target) {
        boolean isHidden = hiddenNameTags.contains(target.getUniqueId());
        String messageKey = isHidden ? "messages.nametag.hidden" : "messages.nametag.visible";
        
        if (sender == target) {
            sendMessage(target, messageKey + "_self");
        } else {
            sendMessage(sender, messageKey + "_other", "%player%", target.getName());
            sendMessage(target, messageKey + "_by", "%player%", sender.getName());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.nametag.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
} 