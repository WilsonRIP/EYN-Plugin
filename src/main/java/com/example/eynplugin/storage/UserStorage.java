package com.example.eynplugin.storage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Handles storage and retrieval of user data in a YAML file.
 */
public final class UserStorage {
    private final File file;
    private final FileConfiguration config;
    private static final Logger LOGGER = Logger.getLogger(UserStorage.class.getName());

    /**
     * Constructs a new UserStorage instance.
     *
     * @param dataFolder the folder where the storage file is located.
     */
    public UserStorage(final File dataFolder) {
        // Initialize logger; replace with your plugin's logger if available.
        LOGGER.info("UserStorage initialized");

        // Ensure the data folder exists.
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            LOGGER.warning("Could not create data folder: " + dataFolder.getAbsolutePath());
        }

        // Initialize the user storage file.
        this.file = new File(dataFolder, "users.yml");
        if (!this.file.exists()) {
            try {
                if (this.file.createNewFile()) {
                    LOGGER.info("Created new user storage file: " + file.getName());
                }
            } catch (IOException e) {
                LOGGER.severe("Could not create user storage file");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves user data associated with the specified UUID.
     *
     * @param uuid the unique identifier of the user.
     * @param data the data to save.
     */
    public void saveUser(final String uuid, final String data) {
        config.set(uuid, data);
        saveConfig();
        LOGGER.log(Level.INFO, "User saved: {0}", uuid);
    }

    /**
     * Retrieves the stored data for the specified user UUID.
     *
     * @param uuid the unique identifier of the user.
     * @return the user data as a String, or null if not found.
     */
    public String getUser(final String uuid) {
        return config.getString(uuid);
    }

    /**
     * Saves the configuration changes to the storage file.
     */
    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            LOGGER.severe("Could not save user storage file");
        }
    }
}
