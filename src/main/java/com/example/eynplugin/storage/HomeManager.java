package com.example.eynplugin.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Manages player home data stored in separate YAML files.
 * Provides methods to set, retrieve, delete, rename, and count homes.
 */
public class HomeManager {
    private final File homesDir;
    private final Map<UUID, YamlConfiguration> playerHomes = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(HomeManager.class.getName());

    /**
     * Constructs a new HomeManager.
     *
     * @param homesDir the directory where home files will be stored.
     */
    public HomeManager(final File homesDir) {
        this.homesDir = homesDir;
        if (!homesDir.exists() && !homesDir.mkdirs()) {
            LOGGER.warning("Failed to create homes directory: " + homesDir.getAbsolutePath());
        }
    }

    /**
     * Sets a home for a player.
     *
     * @param playerUUID the player's UUID.
     * @param homeName   the name of the home.
     * @param location   the location to save as the home.
     */
    public void setHome(final UUID playerUUID, final String homeName, final Location location) {
        final YamlConfiguration config = getPlayerConfig(playerUUID);
        config.set(homeName, location);
        savePlayerConfig(playerUUID, config);
    }

    /**
     * Retrieves a player's home location.
     *
     * @param playerUUID the player's UUID.
     * @param homeName   the name of the home.
     * @return the Location of the home, or null if not found.
     */
    public Location getHome(final UUID playerUUID, final String homeName) {
        return getPlayerConfig(playerUUID).getLocation(homeName);
    }

    /**
     * Deletes a home for a player.
     *
     * @param playerUUID the player's UUID.
     * @param homeName   the name of the home to delete.
     * @return true if the home existed and was deleted; false otherwise.
     */
    public boolean deleteHome(final UUID playerUUID, final String homeName) {
        final YamlConfiguration config = getPlayerConfig(playerUUID);
        if (!config.contains(homeName)) {
            return false;
        }
        config.set(homeName, null);
        savePlayerConfig(playerUUID, config);
        return true;
    }

    /**
     * Renames an existing home.
     *
     * @param playerUUID the player's UUID.
     * @param oldName    the current home name.
     * @param newName    the new home name.
     * @return true if the home existed and was renamed; false otherwise.
     */
    public boolean renameHome(final UUID playerUUID, final String oldName, final String newName) {
        final YamlConfiguration config = getPlayerConfig(playerUUID);
        if (!config.contains(oldName)) {
            return false;
        }
        final Location location = config.getLocation(oldName);
        config.set(oldName, null);
        config.set(newName, location);
        savePlayerConfig(playerUUID, config);
        return true;
    }

    /**
     * Retrieves the number of homes a player has set.
     *
     * @param playerUUID the player's UUID.
     * @return the number of homes.
     */
    public int getHomeCount(final UUID playerUUID) {
        return getPlayerConfig(playerUUID).getKeys(false).size();
    }

    /**
     * Retrieves the YAML configuration for a player.
     * Loads the configuration from file if not already cached.
     *
     * @param playerUUID the player's UUID.
     * @return the YamlConfiguration for the player.
     */
    private YamlConfiguration getPlayerConfig(final UUID playerUUID) {
        if (playerHomes.containsKey(playerUUID)) {
            return playerHomes.get(playerUUID);
        }

        final File playerFile = new File(homesDir, playerUUID.toString() + ".yml");
        final YamlConfiguration config;
        if (playerFile.exists()) {
            config = YamlConfiguration.loadConfiguration(playerFile);
        } else {
            config = new YamlConfiguration();
        }

        playerHomes.put(playerUUID, config);
        return config;
    }

    /**
     * Saves a player's YAML configuration to file.
     *
     * @param playerUUID the player's UUID.
     * @param config     the YamlConfiguration to save.
     */
    private void savePlayerConfig(final UUID playerUUID, final YamlConfiguration config) {
        final File playerFile = new File(homesDir, playerUUID.toString() + ".yml");
        try {
            config.save(playerFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save home configuration for " + playerUUID, e);
        }
    }

    /**
     * Saves all cached player home configurations to file.
     */
    public void saveAll() {
        for (final Map.Entry<UUID, YamlConfiguration> entry : playerHomes.entrySet()) {
            savePlayerConfig(entry.getKey(), entry.getValue());
        }
    }
}
