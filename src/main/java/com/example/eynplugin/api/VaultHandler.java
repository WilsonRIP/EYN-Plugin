package com.example.eynplugin.api;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * VaultHandler provides basic economy operations using Vault.
 * It currently supports depositing and withdrawing funds from a player's account.
 */
public class VaultHandler {

    private final Economy economy;

    /**
     * Constructs a new VaultHandler.
     *
     * @param plugin   the main plugin instance (currently unused, but available for future enhancements)
     * @param economy  the Vault economy instance.
     */
    public VaultHandler(final JavaPlugin plugin, final Economy economy) {
        this.economy = economy;
    }

    /**
     * Deposits the specified amount into the player's account.
     *
     * @param player the OfflinePlayer whose account will be credited.
     * @param amount the amount to deposit.
     * @return true if the transaction succeeded; false otherwise.
     */
    public boolean depositPlayer(final OfflinePlayer player, final double amount) {
        final EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    /**
     * Withdraws the specified amount from the player's account.
     *
     * @param player the OfflinePlayer whose account will be debited.
     * @param amount the amount to withdraw.
     * @return true if the transaction succeeded; false otherwise.
     */
    public boolean withdrawPlayer(final OfflinePlayer player, final double amount) {
        final EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    // Additional Vault-related methods can be added here as needed.
}
