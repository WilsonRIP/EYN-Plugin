package com.example.eynplugin.api;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultHandler {

    private final Economy economy;

    public VaultHandler(JavaPlugin plugin, Economy economy) {
        this.economy = economy;
    }

    public boolean depositPlayer(OfflinePlayer player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean withdrawPlayer(OfflinePlayer player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    // Add more Vault-related methods as needed
} 