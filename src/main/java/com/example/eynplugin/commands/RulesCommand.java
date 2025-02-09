package com.example.eynplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RulesCommand implements CommandExecutor {

    private final Path rulesFilePath;
    private final FileConfiguration messagesConfig;

    public RulesCommand(FileConfiguration messagesConfig, String dataFolderPath) {
        this.messagesConfig = messagesConfig;
        this.rulesFilePath = Paths.get(dataFolderPath, "rules.txt");
        createRulesFileIfNotExist();
    }

    private void createRulesFileIfNotExist() {
        if (!Files.exists(rulesFilePath)) {
            try {
                Files.createFile(rulesFilePath);
                // Optionally, add some default rules
                Files.write(rulesFilePath, List.of("1. Be respectful to all players.", "2. No griefing.", "3. No cheating."));
            } catch (IOException e) {
                System.err.println("Could not create rules.txt: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eyn.rules")) {
            sender.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        try {
            List<String> rules = Files.readAllLines(rulesFilePath);
            player.sendMessage(formatMessage("messages.rules.header")); // Send header message
            for (String rule : rules) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &f" + rule)); // Send each rule
            }
            player.sendMessage(formatMessage("messages.rules.footer")); // Send footer message
        } catch (IOException e) {
            player.sendMessage(formatMessage("messages.rules.error")); // Send error message
            System.err.println("Could not read rules.txt: " + e.getMessage());
        }

        return true;
    }

    private String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Could not find message key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 