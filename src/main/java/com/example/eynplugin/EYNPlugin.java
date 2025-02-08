package com.example.eynplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.commands.AnvilCommand;
import com.example.eynplugin.commands.BackCommand;
import com.example.eynplugin.commands.BalanceCommand;
import com.example.eynplugin.commands.ClearChatCommand;
import com.example.eynplugin.commands.ClearInventoryCommand;
import com.example.eynplugin.commands.DiscordCommand;
import com.example.eynplugin.commands.EnchantCommand;
import com.example.eynplugin.commands.EnderChestCommand;
import com.example.eynplugin.commands.FlyCommand;
import com.example.eynplugin.commands.GamemodeCommand;
import com.example.eynplugin.commands.GodCommand;
import com.example.eynplugin.commands.HealCommand;
import com.example.eynplugin.commands.ModerationCommands;
import com.example.eynplugin.commands.MsgCommand;
import com.example.eynplugin.commands.NameTagCommand;
import com.example.eynplugin.commands.NickCommand;
import com.example.eynplugin.commands.OnlinePlayersCommand;
import com.example.eynplugin.commands.PayCommand;
import com.example.eynplugin.commands.PingCommand;
import com.example.eynplugin.commands.PlayerInfoCommand;
import com.example.eynplugin.commands.PlaytimeCommand;
import com.example.eynplugin.commands.ReloadCommand;
import com.example.eynplugin.commands.SmithingTableCommand;
import com.example.eynplugin.commands.TpaCommand;
import com.example.eynplugin.commands.VanishCommand;
import com.example.eynplugin.commands.WarpCommand;
import com.example.eynplugin.commands.WorldInfoCommand;
import com.example.eynplugin.commands.XPCommand;
import com.example.eynplugin.listeners.FreezeListener;
import com.example.eynplugin.storage.HomeManager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;

/**
 * Main plugin class for EYN Plugin.
 * Handles initialization, dependency setup, configuration loading,
 * command and listener registration.
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

            if (!createDataFolder()) return;
            if (!setupLuckPerms()) return;
            setupEconomy();
            if (!setupMessagesConfig()) return;
            validateMessagesConfig();

            initializeManagers();
            registerListeners();
            registerAllCommands();

            getLogger().info("EYN Plugin has been enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable EYN Plugin: {0}", e.getMessage());
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

    /**
     * Ensures the plugin data folder exists.
     *
     * @return true if the folder exists or was created successfully; false otherwise.
     */
    private boolean createDataFolder() {
        final File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            getLogger().severe("Failed to create plugin data folder!");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    /**
     * Sets up the LuckPerms API.
     *
     * @return true if successfully connected; false otherwise.
     */
    private boolean setupLuckPerms() {
        try {
            luckPerms = LuckPermsProvider.get();
            getLogger().info("Successfully connected to LuckPerms API.");
            return true;
        } catch (IllegalStateException e) {
            getLogger().log(Level.SEVERE, "LuckPerms is required but not installed! Error: {0}", e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    /**
     * Sets up Vault economy integration, if available.
     */
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found - economy features will be disabled.");
            return;
        }
        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider found - economy features will be disabled.");
            return;
        }
        economy = rsp.getProvider();
        getLogger().log(Level.INFO, "Economy system connected successfully: {0}", economy.getName());
    }

    /**
     * Loads and initializes the messages configuration.
     *
     * @return true if messagesConfig loaded successfully; false otherwise.
     */
    private boolean setupMessagesConfig() {
        final File messagesFile = new File(getDataFolder(), MESSAGES_FILE);
        if (!messagesFile.exists()) {
            try {
                saveResource(MESSAGES_FILE, false);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to create messages.yml: {0}", e.getMessage());
                return false;
            }
        }

        try {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            try (final InputStream defStream = getResource(MESSAGES_FILE)) {
                if (defStream != null) {
                    final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defStream, StandardCharsets.UTF_8)
                    );
                    messagesConfig.setDefaults(defConfig);
                    messagesConfig.options().copyDefaults(true);
                    messagesConfig.save(messagesFile);
                }
            }
            return true;
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to load messages.yml: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks that required keys are present in messages.yml.
     */
    private void validateMessagesConfig() {
        final List<String> requiredKeys = Arrays.asList(
                "no_permission", "player_only_command", "error.generic",
                "fly.toggled", "back.success", "god.toggled",
                "gamemode.updated", "weather.updated", "durability.repaired",
                "tp.success", "warp.not_found", "discord.message",
                "vanish.toggled", "playtime.display", "playerinfo.display",
                "online.count", "clearchat.success", "mute.success",
                "ping.response", "nick.updated"
        );
        boolean hasErrors = false;
        for (final String key : requiredKeys) {
            if (!messagesConfig.contains(key)) {
                getLogger().log(Level.WARNING, "Missing required message key in messages.yml: {0}", key);
                hasErrors = true;
            }
        }
        if (hasErrors) {
            getLogger().warning("Some message keys are missing. Plugin functionality may be limited.");
        }
    }

    /**
     * Initializes plugin managers.
     */
    private void initializeManagers() {
        luckPermsHandler = new LuckPermsHandler(this, luckPerms);
        // Initialize HomeManager with the data folder. Logger is now static in HomeManager.
        homeManager = new HomeManager(new File(getDataFolder(), HOMES_DIRECTORY));
    }

    /**
     * Registers event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
    }

    /**
     * Registers all plugin commands.
     */
    private void registerAllCommands() {
        // Core commands.
        registerCommand("fly", new FlyCommand(luckPermsHandler, messagesConfig));
        registerCommand("back", new BackCommand(luckPermsHandler, messagesConfig));
        registerCommand("god", new GodCommand(luckPermsHandler, messagesConfig));

        // Register warp-related commands.
        registerWarpCommands();

        // Register vanish commands.
        registerVanishCommands();

        // Register utility commands.
        registerUtilityCommands();

        // Register moderation commands.
        registerModerationCommands();

        // Register miscellaneous commands.
        registerMiscCommands();

        // Additional commands.
        registerCommand("heal", new HealCommand(luckPermsHandler, messagesConfig));
        registerCommand("nametag", new NameTagCommand(luckPermsHandler, messagesConfig, this));
    }

    /**
     * Registers warp commands and their tab completers.
     */
    private void registerWarpCommands() {
        final WarpCommand warpCommand = new WarpCommand(this, luckPermsHandler, messagesConfig);
        final List<String> warpCmds = Arrays.asList("warp", "setwarp", "delwarp", "warplist", "renamewarp");
        for (final String cmd : warpCmds) {
            registerCommand(cmd, warpCommand);
        }
        // Register tab completers for warp commands that require it.
        for (final String cmd : Arrays.asList("warp", "delwarp", "renamewarp")) {
            registerTabCompleter(cmd, warpCommand);
        }
    }

    /**
     * Registers vanish commands.
     */
    private void registerVanishCommands() {
        vanishCommand = new VanishCommand(luckPermsHandler, messagesConfig, this);
        registerCommand("vanish", vanishCommand);
        registerCommand("vanishlist", vanishCommand);
    }

    /**
     * Registers utility commands.
     */
    private void registerUtilityCommands() {
        registerCommand("discord", new DiscordCommand(luckPermsHandler, messagesConfig, getConfig()));
        registerCommand("playtime", new PlaytimeCommand(luckPermsHandler, messagesConfig));
        registerCommand("playerinfo", new PlayerInfoCommand(luckPermsHandler, messagesConfig));
        registerCommand("online", new OnlinePlayersCommand(luckPermsHandler, messagesConfig));
        registerCommand("clearchat", new ClearChatCommand(luckPermsHandler, messagesConfig));
        registerCommand("anvil", new AnvilCommand(luckPermsHandler, messagesConfig));
        registerCommand("smithing", new SmithingTableCommand(luckPermsHandler, messagesConfig));
        registerCommand("enderchest", new EnderChestCommand(luckPermsHandler, messagesConfig));
        registerCommand("clearinventory", new ClearInventoryCommand(luckPermsHandler, messagesConfig));
        registerCommand("pay", new PayCommand(luckPermsHandler, messagesConfig, economy));
        registerCommand("balance", new BalanceCommand(luckPermsHandler, messagesConfig, economy));
    }

    /**
     * Registers moderation commands.
     */
    private void registerModerationCommands() {
        final ModerationCommands moderation = new ModerationCommands(luckPermsHandler, messagesConfig, this);
        final List<String> modCmds = Arrays.asList("mute", "unmute", "ban", "tempban", "unban", "kick", "freeze",
                                                    "tp", "tpall", "tphere", "burn");
        for (final String cmd : modCmds) {
            registerCommand(cmd, moderation);
        }
    }

    /**
     * Registers miscellaneous commands.
     */
    private void registerMiscCommands() {
        registerCommand("ping", new PingCommand(messagesConfig));
        registerCommand("nick", new NickCommand(messagesConfig));
        registerCommand("xp", new XPCommand(messagesConfig));
        registerCommand("worldinfo", new WorldInfoCommand(luckPermsHandler, messagesConfig));
        registerCommand("msg", new MsgCommand(luckPermsHandler, messagesConfig));
        registerCommand("enchant", new EnchantCommand(luckPermsHandler, messagesConfig));

        // Register gamemode commands as a group.
        final GamemodeCommand gamemodeCommand = new GamemodeCommand(luckPermsHandler, messagesConfig);
        for (final String cmd : Arrays.asList("gmc", "gms", "gmsp", "gma")) {
            registerCommand(cmd, gamemodeCommand);
        }

        registerCommand("eynreload", new ReloadCommand(this, luckPermsHandler, messagesConfig));
        registerCommand("tpa", new TpaCommand(this, luckPermsHandler, messagesConfig));
        registerCommand("tpaccept", new TpaCommand(this, luckPermsHandler, messagesConfig));
        registerCommand("tpdeny", new TpaCommand(this, luckPermsHandler, messagesConfig));
    }

    /**
     * Registers a command executor for a given command name.
     *
     * @param name     the command name.
     * @param executor the command executor.
     */
    private void registerCommand(final String name, final CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().log(Level.WARNING, "Failed to register command: /{0} (not defined in plugin.yml)", name);
        }
    }

    /**
     * Registers a tab completer for a given command name.
     *
     * @param name      the command name.
     * @param completer the tab completer.
     */
    private void registerTabCompleter(final String name, final TabCompleter completer) {
        if (getCommand(name) != null) {
            getCommand(name).setTabCompleter(completer);
        } else {
            getLogger().log(Level.WARNING, "Failed to register tab completer for: /{0}", name);
        }
    }

    /**
     * Returns the economy instance.
     *
     * @return the economy instance.
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Returns the LuckPerms API instance.
     *
     * @return the LuckPerms instance.
     */
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    /**
     * Returns the messages configuration.
     *
     * @return the messages configuration.
     */
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * Reloads the plugin configurations.
     */
    public void reloadConfigs() {
        reloadConfig();
        saveDefaultConfig();
        final File messagesFile = new File(getDataFolder(), MESSAGES_FILE);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        validateMessagesConfig();
        getLogger().info("Configuration reloaded successfully");
    }
}
