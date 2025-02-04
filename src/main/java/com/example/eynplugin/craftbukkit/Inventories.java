package com.example.eynplugin.craftbukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Utility class for common inventory operations.
 */
public final class Inventories {

    // Private constructor to prevent instantiation
    private Inventories() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Clears a player's inventory and armor.
     *
     * @param player The player whose inventory will be cleared.
     */
    public static void clearInventory(Player player) {
        if (player == null) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        // Clear armor by setting an empty armor contents array
        inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
    }

    /**
     * Clears a specific slot in a player's inventory.
     *
     * @param player The player whose inventory slot will be cleared.
     * @param slot   The slot index to clear.
     */
    public static void clearInventorySlot(Player player, int slot) {
        if (player == null) {
            return;
        }
        player.getInventory().setItem(slot, null);
    }

    /**
     * Adds an item to the player's inventory.
     *
     * @param player The player who will receive the item.
     * @param item   The item to add.
     */
    public static void addItem(Player player, ItemStack item) {
        if (player == null || item == null) {
            return;
        }
        player.getInventory().addItem(item);
    }

    /**
     * Sets an item in a specific slot in the player's inventory.
     *
     * @param player The player whose inventory will be modified.
     * @param slot   The slot index to set.
     * @param item   The item to place in the specified slot.
     */
    public static void setItem(Player player, int slot, ItemStack item) {
        if (player == null) {
            return;
        }
        player.getInventory().setItem(slot, item);
    }

    /**
     * Retrieves an item from a specific slot in the player's inventory.
     *
     * @param player The player whose inventory is being queried.
     * @param slot   The slot index from which to retrieve the item.
     * @return The ItemStack in the specified slot, or null if empty.
     */
    public static ItemStack getItem(Player player, int slot) {
        if (player == null) {
            return null;
        }
        return player.getInventory().getItem(slot);
    }

    /**
     * Checks if the player has at least one empty slot in their inventory.
     *
     * @param player The player whose inventory will be checked.
     * @return True if there is an empty slot; false otherwise.
     */
    public static boolean hasSpace(Player player) {
        if (player == null) {
            return false;
        }
        return player.getInventory().firstEmpty() != -1;
    }

    /**
     * Copies the entire inventory and armor from one player to another.
     *
     * @param from The player from whom to copy the inventory.
     * @param to   The player who will receive the copied inventory.
     */
    public static void copyInventory(Player from, Player to) {
        if (from == null || to == null) {
            return;
        }
        PlayerInventory fromInv = from.getInventory();
        PlayerInventory toInv = to.getInventory();

        toInv.setContents(fromInv.getContents());
        toInv.setArmorContents(fromInv.getArmorContents());
    }

    /**
     * Creates a new inventory with the specified title and size.
     *
     * @param title The title of the inventory.
     * @param size  The size of the inventory (must be a multiple of 9).
     * @return A new Inventory instance.
     */
    public static Inventory createInventory(String title, int size) {
        return Bukkit.createInventory(null, size, title);
    }

    /**
     * Opens the specified inventory for the player.
     *
     * @param player    The player for whom the inventory will be opened.
     * @param inventory The inventory to open.
     */
    public static void openInventory(Player player, Inventory inventory) {
        if (player == null || inventory == null) {
            return;
        }
        player.openInventory(inventory);
    }

    /**
     * Closes the currently open inventory for the player.
     *
     * @param player The player whose inventory will be closed.
     */
    public static void closeInventory(Player player) {
        if (player == null) {
            return;
        }
        player.closeInventory();
    }
}
