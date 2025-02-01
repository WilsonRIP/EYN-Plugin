package com.example.eynplugin.storage;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class HomeManager {
    private final File homesDir;
    private final Map<UUID, YamlConfiguration> playerHomes = new HashMap<>();

    public HomeManager(File homesDir) {
        this.homesDir = homesDir;
        if (!homesDir.exists()) {
            homesDir.mkdirs();
        }
    }

    public void setHome(UUID playerUUID, String homeName, Location location) {
        YamlConfiguration config = getPlayerConfig(playerUUID);
        config.set(homeName, location);
        savePlayerConfig(playerUUID, config);
    }

    public Location getHome(UUID playerUUID, String homeName) {
        YamlConfiguration config = getPlayerConfig(playerUUID);
        return config.getLocation(homeName);
    }

    public boolean deleteHome(UUID playerUUID, String homeName) {
        YamlConfiguration config = getPlayerConfig(playerUUID);
        if (!config.contains(homeName)) {
            return false;
        }
        config.set(homeName, null);
        savePlayerConfig(playerUUID, config);
        return true;
    }

    public boolean renameHome(UUID playerUUID, String oldName, String newName) {
        YamlConfiguration config = getPlayerConfig(playerUUID);
        if (!config.contains(oldName)) {
            return false;
        }
        Location location = config.getLocation(oldName);
        config.set(oldName, null);
        config.set(newName, location);
        savePlayerConfig(playerUUID, config);
        return true;
    }

    public int getHomeCount(UUID playerUUID) {
        YamlConfiguration config = getPlayerConfig(playerUUID);
        return config.getKeys(false).size();
    }

    private YamlConfiguration getPlayerConfig(UUID playerUUID) {
        if (playerHomes.containsKey(playerUUID)) {
            return playerHomes.get(playerUUID);
        }

        File playerFile = new File(homesDir, playerUUID.toString() + ".yml");
        YamlConfiguration config;
        
        if (playerFile.exists()) {
            config = YamlConfiguration.loadConfiguration(playerFile);
        } else {
            config = new YamlConfiguration();
        }

        playerHomes.put(playerUUID, config);
        return config;
    }

    private void savePlayerConfig(UUID playerUUID, YamlConfiguration config) {
        try {
            config.save(new File(homesDir, playerUUID.toString() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, YamlConfiguration> entry : playerHomes.entrySet()) {
            savePlayerConfig(entry.getKey(), entry.getValue());
        }
    }
} 