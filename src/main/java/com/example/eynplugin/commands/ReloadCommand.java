package com.example.eynplugin.commands;

import com.example.eynplugin.EYNPlugin;
import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * ReloadCommand allows authorized users to reload the plugin configuration.
 * It supports reloading both for players and the console.
 */
public class ReloadCommand extends BaseCommand {
    private final EYNPlugin plugin;

    /**
     * Constructs a new ReloadCommand.
     *
     * @param plugin           the main plugin instance.
     * @param luckPermsHandler the LuckPerms handler.
     * @param messagesConfig   the configuration file for messages.
     */
    public ReloadCommand(EYNPlugin plugin, LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    /**
     * Processes the reload command.
     * Usage: /reload or /<alias>
     *
     * @param sender  the command sender.
     * @param cmd     the command executed.
     * @param label   the alias used.
     * @param args    command arguments (not used).
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkPermission(sender, "eyn.reload")) {
            return true;
        }

        try {
            plugin.reloadConfigs();
            sender.sendMessage(Utils.colorize(getMessage("messages.reload.success")));
        } catch (Exception e) {
            final String errorKey = (sender instanceof Player)
                    ? "messages.reload.failed"
                    : "messages.reload.failed_console";
            sender.sendMessage(Utils.colorize(getMessage(errorKey)));
            plugin.getLogger().severe("Config reload failed: " + e.getMessage());
        }
        return true;
    }
}
