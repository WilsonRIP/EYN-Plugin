package com.example.eynplugin.craftbukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryData {
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final File dataFolder;

    public InventoryData(File dataFolder) {
        this.dataFolder = dataFolder;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void saveInventory(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerInventory inventory = player.getInventory();
        savedInventories.put(uuid, inventory.getContents());
        savedArmor.put(uuid, inventory.getArmorContents());
    }

    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        if (savedInventories.containsKey(uuid)) {
            player.getInventory().setContents(savedInventories.get(uuid));
            player.getInventory().setArmorContents(savedArmor.get(uuid));
            savedInventories.remove(uuid);
            savedArmor.remove(uuid);
        }
    }

    public void persistInventory(Player player, String identifier) {
        File inventoryFile = new File(dataFolder, player.getUniqueId() + "_" + identifier + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("inventory.contents", player.getInventory().getContents());
        config.set("inventory.armor", player.getInventory().getArmorContents());
        
        try {
            config.save(inventoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPersistedInventory(Player player, String identifier) {
        File inventoryFile = new File(dataFolder, player.getUniqueId() + "_" + identifier + ".yml");
        if (!inventoryFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(inventoryFile);
        ItemStack[] contents = ((ItemStack[]) config.get("inventory.contents"));
        ItemStack[] armor = ((ItemStack[]) config.get("inventory.armor"));

        if (contents != null) {
            player.getInventory().setContents(contents);
        }
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
    }

    public boolean hasStoredInventory(Player player) {
        return savedInventories.containsKey(player.getUniqueId());
    }

    public void clearStoredInventory(Player player) {
        UUID uuid = player.getUniqueId();
        savedInventories.remove(uuid);
        savedArmor.remove(uuid);
    }

    public void clearAllStoredInventories() {
        savedInventories.clear();
        savedArmor.clear();
    }
} 