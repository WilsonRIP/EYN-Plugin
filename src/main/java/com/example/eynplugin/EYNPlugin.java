package com.example.eynplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import com.example.eynplugin.commands.*;
import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import com.example.eynplugin.storage.HomeManager;
import com.example.eynplugin.listeners.FreezeListener;
import com.example.eynplugin.commands.ReloadCommand;

/**
 * Main plugin class for EYN Plugin.
 * Handles initialization, dependencies, and command registration.
 */
public class EYNPlugin extends JavaPlugin {
    private static final String MESSAGES_FILE = "messages.yml";
    private static final String HOMES_DIRECTORY = "homes";

    private LuckPerms luckPerms;
    private Economy economy;
    private LuckPermsHandler luckPermsHandler;
    private FileConfiguration messagesConfig;
    private VanishCommand vanishCommand;
    private HomeManager homeManager;

    @Override
    public void onEnable() {
        try {
            getLogger().info("EYN Plugin is initializing...");
            
            if (!createDataFolder()) {
                return;
            }

            if (!setupLuckPerms()) {
                return;
            }

            setupEconomy();
            if (!setupMessagesConfig()) {
                return;
            }
            validateMessagesConfig();

            initializeManagers();
            registerListeners();
            registerAllCommands();

            getLogger().info("EYN Plugin has been enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable EYN Plugin: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.saveAll();
        }
        getLogger().info("EYN Plugin has been disabled.");
    }

    private boolean createDataFolder() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Failed to create plugin data folder!");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private boolean setupLuckPerms() {
        try {
            luckPerms = LuckPermsProvider.get();
            getLogger().info("Successfully connected to LuckPerms API.");
            return true;
        } catch (IllegalStateException e) {
            getLogger().severe("LuckPerms is required but not installed! Error: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found - economy features will be disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider found - economy features will be disabled.");
            return;
        }

        economy = rsp.getProvider();
        getLogger().info("Economy system connected successfully: " + economy.getName());
    }

    private boolean setupMessagesConfig() {
        File messagesFile = new File(getDataFolder(), MESSAGES_FILE);
        
        if (!messagesFile.exists()) {
            try {
                saveResource(MESSAGES_FILE, false);
            } catch (Exception e) {
                getLogger().severe("Failed to create messages.yml: " + e.getMessage());
                return false;
            }
        }

        try {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            try (InputStream defStream = getResource(MESSAGES_FILE)) {
                if (defStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defStream, StandardCharsets.UTF_8)
                    );
                    messagesConfig.setDefaults(defConfig);
                    messagesConfig.options().copyDefaults(true);
                    messagesConfig.save(messagesFile);
                }
            }
            return true;
        } catch (IOException e) {
            getLogger().severe("Failed to load messages.yml: " + e.getMessage());
            return false;
        }
    }

    private void validateMessagesConfig() {
        List<String> requiredKeys = Arrays.asList(
            "no_permission", "player_only_command", "error.generic",
            "fly.toggled", "back.success", "god.toggled",
            "gamemode.updated", "weather.updated", "durability.repaired",
            "tp.success", "warp.not_found", "discord.message",
            "vanish.toggled", "playtime.display", "playerinfo.display",
            "online.count", "clearchat.success", "mute.success",
            "ping.response", "nick.updated"
        );

        boolean hasErrors = false;
        for (String key : requiredKeys) {
            if (!messagesConfig.contains(key)) {
                getLogger().warning("Missing required message key in messages.yml: " + key);
                hasErrors = true;
            }
        }

        if (hasErrors) {
            getLogger().warning("Some message keys are missing. Plugin functionality may be limited.");
        }
    }

    private void initializeManagers() {
        luckPermsHandler = new LuckPermsHandler(this, luckPerms);
        homeManager = new HomeManager(new File(getDataFolder(), HOMES_DIRECTORY));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
    }

    private void registerAllCommands() {
        // Core commands
        registerCommand("fly", new FlyCommand(luckPermsHandler, messagesConfig));
        registerCommand("back", new BackCommand(luckPermsHandler, messagesConfig));
        registerCommand("god", new GodCommand(luckPermsHandler, messagesConfig));

        // Gamemode commands
        for (String cmd : Arrays.asList("gmc", "gms", "gmsp")) {
            registerCommand(cmd, new GamemodeCommand(luckPermsHandler, messagesConfig));
        }

        registerWarpCommands();
        registerVanishCommands();
        registerUtilityCommands();
        registerModerationCommands();
        registerMiscCommands();
    }

    private void registerWarpCommands() {
        WarpCommand warpCommand = new WarpCommand(luckPermsHandler, messagesConfig, getDataFolder());
        for (String cmd : Arrays.asList("warp", "setwarp", "delwarp", "warplist", "renamewarp")) {
            registerCommand(cmd, warpCommand);
        }
        for (String cmd : Arrays.asList("warp", "delwarp", "renamewarp")) {
            registerTabCompleter(cmd, warpCommand);
        }
    }

    private void registerVanishCommands() {
        vanishCommand = new VanishCommand(luckPermsHandler, messagesConfig, this);
        registerCommand("vanish", vanishCommand);
        registerCommand("vanishlist", vanishCommand);
    }

    private void registerUtilityCommands() {
        registerCommand("discord", new DiscordCommand(luckPermsHandler, messagesConfig, getConfig()));
        registerCommand("playtime", new PlaytimeCommand(luckPermsHandler, messagesConfig));
        registerCommand("playerinfo", new PlayerInfoCommand(luckPermsHandler, messagesConfig));
        registerCommand("online", new OnlinePlayersCommand(luckPermsHandler, messagesConfig));
        registerCommand("clearchat", new ClearChatCommand(luckPermsHandler, messagesConfig));
    }

    private void registerModerationCommands() {
        ModerationCommands moderation = new ModerationCommands(luckPermsHandler, messagesConfig, this);
        for (String cmd : Arrays.asList("mute", "unmute", "ban", "tempban", "unban", "kick", "freeze", 
                                        "tp", "tpall", "tphere", "burn")) {
            registerCommand(cmd, moderation);
        }
    }

    private void registerMiscCommands() {
        registerCommand("ping", new PingCommand(messagesConfig));
        registerCommand("nick", new NickCommand(messagesConfig));
        registerCommand("xp", new XPCommand(messagesConfig));
        registerCommand("worldinfo", new WorldInfoCommand(luckPermsHandler, messagesConfig));
        registerCommand("anvil", new AnvilCommand(luckPermsHandler, messagesConfig));
        registerCommand("home", new HomeCommand(homeManager, getConfig(), messagesConfig));
        registerCommand("clearinventory", new ClearInventoryCommand(luckPermsHandler, messagesConfig));
        registerCommand("enderchest", new EnderChestCommand(luckPermsHandler, messagesConfig));
        
        // Add gamemode commands
        GamemodeCommand gamemodeCommand = new GamemodeCommand(luckPermsHandler, messagesConfig);
        for (String cmd : Arrays.asList("gmc", "gms", "gmsp", "gma")) {
            registerCommand(cmd, gamemodeCommand);
        }

        registerCommand("eynreload", new ReloadCommand(this, luckPermsHandler, messagesConfig));
        registerCommand("tpa", new TpaCommand(luckPermsHandler, messagesConfig));
        registerCommand("tpaccept", new TpaCommand(luckPermsHandler, messagesConfig));
        registerCommand("tpdeny", new TpaCommand(luckPermsHandler, messagesConfig));
    }

    private void registerCommand(String name, CommandExecutor executor) {
        Objects.requireNonNull(name, "Command name cannot be null");
        Objects.requireNonNull(executor, "Command executor cannot be null");
        
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("Failed to register command: /" + name + " (command not found in plugin.yml)");
        }
    }

    private void registerTabCompleter(String name, TabCompleter completer) {
        Objects.requireNonNull(name, "Command name cannot be null");
        Objects.requireNonNull(completer, "Tab completer cannot be null");
        
        if (getCommand(name) != null) {
            getCommand(name).setTabCompleter(completer);
        } else {
            getLogger().warning("Failed to register tab completer for: /" + name);
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void reloadConfigs() {
        reloadConfig();
        saveDefaultConfig();
        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), MESSAGES_FILE));
        validateMessagesConfig();
        getLogger().info("Configuration reloaded successfully");
    }
}