package com.example.eynplugin.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles storage and retrieval of user data in a YAML file.
 */
public class UserStorage {

    private final File file;
    private final FileConfiguration config;
    private final Logger logger;

    /**
     * Constructs a new UserStorage instance.
     *
     * @param dataFolder the folder where the storage file is located.
     */
    public UserStorage(File dataFolder) {
        // Initialize logger; replace with your plugin's logger if available.
        this.logger = Logger.getLogger("EYNPlugin");

        // Ensure the data folder exists.
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.warning("Could not create data folder: " + dataFolder.getAbsolutePath());
        }

        // Initialize the user storage file.
        file = new File(dataFolder, "users.yml");
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    logger.info("Created new user storage file: " + file.getName());
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create user storage file", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves user data associated with the specified UUID.
     *
     * @param uuid the unique identifier of the user.
     * @param data the data to save.
     */
    public void saveUser(String uuid, String data) {
        config.set(uuid, data);
        saveConfig();
    }

    /**
     * Retrieves the stored data for the specified user UUID.
     *
     * @param uuid the unique identifier of the user.
     * @return the user data as a String, or null if not found.
     */
    public String getUser(String uuid) {
        return config.getString(uuid);
    }

    /**
     * Saves the configuration changes to the storage file.
     */
    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save user storage file", e);
        }
    }
}
