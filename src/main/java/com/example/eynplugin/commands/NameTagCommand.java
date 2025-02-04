package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Command to toggle player name tag visibility.
 */
public class NameTagCommand extends BaseCommand {
    private final Set<UUID> hiddenNameTags = new HashSet<>();
    private final Plugin plugin;

    public NameTagCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, Plugin plugin) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    /**
     * Executes the name tag toggle command.
     *
     * @param sender The command sender.
     * @param cmd    The command.
     * @param label  The command label.
     * @param args   The command arguments.
     * @return true if the command executed successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        final Player player = (Player) sender;
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

    /**
     * Toggles the name tag visibility for the specified player.
     *
     * @param target The player whose name tag visibility is to be toggled.
     */
    private void toggleNameTagVisibility(final Player target) {
        if (hiddenNameTags.contains(target.getUniqueId())) {
            // Currently hidden – make visible.
            hiddenNameTags.remove(target.getUniqueId());
            updateNameTagVisibility(target, false);
        } else {
            // Currently visible – hide the name tag.
            hiddenNameTags.add(target.getUniqueId());
            updateNameTagVisibility(target, true);
        }
    }

    /**
     * Updates the name tag visibility for the target player and refreshes the view for all online players.
     *
     * @param target The player whose name tag is being updated.
     * @param hidden True if the name tag should be hidden; false if it should be visible.
     */
    private void updateNameTagVisibility(final Player target, final boolean hidden) {
        // Store the hidden state in metadata.
        target.setMetadata("nametag", new FixedMetadataValue(plugin, hidden));

        // Update the visibility for all online players.
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (!p.equals(target)) {
                if (hidden) {
                    p.hidePlayer(plugin, target);
                } else {
                    p.showPlayer(plugin, target);
                }
            }
        });
    }

    /**
     * Sends the appropriate toggle message to the command sender and target player.
     *
     * @param sender The command sender.
     * @param target The player whose name tag was toggled.
     */
    private void sendToggleMessage(final CommandSender sender, final Player target) {
        final boolean isHidden = hiddenNameTags.contains(target.getUniqueId());
        final String messageKey = isHidden ? "messages.nametag.hidden" : "messages.nametag.visible";

        if (sender.equals(target)) {
            sendMessage(target, messageKey + "_self");
        } else {
            sendMessage(sender, messageKey + "_other", "%player%", target.getName());
            sendMessage(target, messageKey + "_by", "%player%", sender.getName());
        }
    }

    /**
     * Provides tab completion for the command.
     *
     * @param sender The command sender.
     * @param cmd    The command.
     * @param alias  The alias used.
     * @param args   The command arguments.
     * @return A list of suggested completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.nametag.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
}
