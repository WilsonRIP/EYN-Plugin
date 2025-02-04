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

/**
 * Utility class for saving, restoring, and persisting player inventories.
 */
public class InventoryData {
    
    // In-memory storage for saved inventories and armor
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    
    // Folder where persisted inventory data will be saved
    private final File dataFolder;

    /**
     * Constructs an InventoryData instance with the specified data folder.
     *
     * @param dataFolder the folder where inventory data will be persisted
     */
    public InventoryData(final File dataFolder) {
        this.dataFolder = dataFolder;
        if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
            throw new IllegalStateException("Could not create data folder: " + this.dataFolder.getAbsolutePath());
        }
    }

    /**
     * Saves the current inventory and armor of the player in memory.
     *
     * @param player the player whose inventory will be saved
     */
    public void saveInventory(final Player player) {
        final UUID uuid = player.getUniqueId();
        final PlayerInventory inventory = player.getInventory();
        savedInventories.put(uuid, inventory.getContents());
        savedArmor.put(uuid, inventory.getArmorContents());
    }

    /**
     * Restores a player's inventory and armor from memory if it has been saved.
     *
     * @param player the player whose inventory will be restored
     */
    public void restoreInventory(final Player player) {
        final UUID uuid = player.getUniqueId();
        if (savedInventories.containsKey(uuid)) {
            player.getInventory().setContents(savedInventories.get(uuid));
            player.getInventory().setArmorContents(savedArmor.get(uuid));
            savedInventories.remove(uuid);
            savedArmor.remove(uuid);
        }
    }

    /**
     * Persists the player's inventory and armor to a YAML file.
     *
     * @param player     the player whose inventory will be persisted
     * @param identifier an identifier to distinguish between multiple inventories
     */
    public void persistInventory(final Player player, final String identifier) {
        final File inventoryFile = getInventoryFile(player.getUniqueId(), identifier);
        final YamlConfiguration config = new YamlConfiguration();
        
        config.set("inventory.contents", player.getInventory().getContents());
        config.set("inventory.armor", player.getInventory().getArmorContents());
        
        try {
            config.save(inventoryFile);
        } catch (IOException e) {
            // Replace with your preferred logging mechanism
            System.err.println("Failed to save inventory for player " + player.getUniqueId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a persisted inventory from a YAML file and applies it to the player.
     *
     * @param player     the player whose inventory will be loaded
     * @param identifier the identifier used when the inventory was persisted
     */
    public void loadPersistedInventory(final Player player, final String identifier) {
        final File inventoryFile = getInventoryFile(player.getUniqueId(), identifier);
        if (!inventoryFile.exists()) {
            return;
        }

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(inventoryFile);
        final ItemStack[] contents = (ItemStack[]) config.get("inventory.contents");
        final ItemStack[] armor = (ItemStack[]) config.get("inventory.armor");

        if (contents != null) {
            player.getInventory().setContents(contents);
        }
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
    }

    /**
     * Checks if there is a stored inventory for the specified player in memory.
     *
     * @param player the player to check
     * @return true if the player's inventory is stored, false otherwise
     */
    public boolean hasStoredInventory(final Player player) {
        return savedInventories.containsKey(player.getUniqueId());
    }

    /**
     * Clears the stored inventory for the specified player from memory.
     *
     * @param player the player whose stored inventory will be cleared
     */
    public void clearStoredInventory(final Player player) {
        final UUID uuid = player.getUniqueId();
        savedInventories.remove(uuid);
        savedArmor.remove(uuid);
    }

    /**
     * Clears all stored inventories from memory.
     */
    public void clearAllStoredInventories() {
        savedInventories.clear();
        savedArmor.clear();
    }
    
    /**
     * Constructs the file path for a player's persisted inventory.
     *
     * @param uuid       the player's UUID
     * @param identifier the identifier for the persisted inventory
     * @return the File object representing the persisted inventory file
     */
    private File getInventoryFile(final UUID uuid, final String identifier) {
        return new File(dataFolder, uuid.toString() + "_" + identifier + ".yml");
    }
}
