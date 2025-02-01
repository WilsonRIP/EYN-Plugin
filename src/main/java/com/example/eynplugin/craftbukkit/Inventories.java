package com.example.eynplugin.craftbukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Inventories {
    
    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public static void clearInventorySlot(Player player, int slot) {
        player.getInventory().setItem(slot, null);
    }

    public static void addItem(Player player, ItemStack item) {
        player.getInventory().addItem(item);
    }

    public static void setItem(Player player, int slot, ItemStack item) {
        player.getInventory().setItem(slot, item);
    }

    public static ItemStack getItem(Player player, int slot) {
        return player.getInventory().getItem(slot);
    }

    public static boolean hasSpace(Player player) {
        return player.getInventory().firstEmpty() != -1;
    }

    public static void copyInventory(Player from, Player to) {
        PlayerInventory fromInv = from.getInventory();
        PlayerInventory toInv = to.getInventory();
        
        toInv.setContents(fromInv.getContents());
        toInv.setArmorContents(fromInv.getArmorContents());
    }

    public static Inventory createInventory(String title, int size) {
        return Bukkit.createInventory(null, size, title);
    }

    public static void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }

    public static void closeInventory(Player player) {
        player.closeInventory();
    }
} 