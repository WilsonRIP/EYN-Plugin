package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.EYNPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import com.example.eynplugin.Utils;
import org.bukkit.entity.Player;

public class ReloadCommand extends BaseCommand {
    private final EYNPlugin plugin;

    public ReloadCommand(EYNPlugin plugin, LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkPermission(sender, "eyn.reload")) {
            return true;
        }

        try {
            plugin.reloadConfigs();
            sender.sendMessage(Utils.colorize(getMessage("messages.reload.success")));
        } catch (Exception e) {
            String errorKey = sender instanceof Player ?
                "messages.reload.failed" :
                "messages.reload.failed_console";
            sender.sendMessage(Utils.colorize(getMessage(errorKey)));
            plugin.getLogger().severe("Config reload failed: " + e.getMessage());
        }
        return true;
    }
} 