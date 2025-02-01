package com.example.eynplugin.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class UserStorage {

    private final File file;
    private final FileConfiguration config;

    public UserStorage(File dataFolder) {
        file = new File(dataFolder, "users.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveUser(String uuid, String data) {
        config.set(uuid, data);
        saveConfig();
    }

    public String getUser(String uuid) {
        return config.getString(uuid);
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 