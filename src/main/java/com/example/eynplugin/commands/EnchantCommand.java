package com.example.eynplugin.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.eynplugin.api.LuckPermsHandler;

/**
 * EnchantCommand allows players to apply enchantments to the item held in their main hand.
 * Usage:
 *   /enchant <enchantment> <level> [player]
 *   /enchant list -- shows the list of available enchantments.
 */
public class EnchantCommand extends BaseCommand {

    public EnchantCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.player_only_command");
            return true;
        }

        // Check permission to use the enchant command.
        if (!checkPermission(player, "eyn.enchant")) {
            return true;
        }

        // If no enchantment is specified or "list" is given, display the enchantment list.
        if (args.length < 1 || args[0].equalsIgnoreCase("list")) {
            return handleEnchantList(player);
        }

        // Must have at least 2 arguments: enchantment and level.
        if (args.length < 2) {
            sendMessage(player, "messages.enchant.usage");
            return true;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sendMessage(player, "messages.enchant.no_item");
            return true;
        }

        try {
            final Enchantment enchant = findEnchantment(args[0]);
            if (enchant == null) {
                sendMessage(player, "messages.enchant.invalid_enchant");
                return true;
            }

            final int level = Integer.parseInt(args[1]);
            if (!isValidLevel(enchant, level)) {
                sendMessage(player, "messages.enchant.invalid_level_range");
                return true;
            }

            applyEnchantment(item, enchant, level);
            sendSuccessMessage(player, enchant, level);
        } catch (NumberFormatException e) {
            sendMessage(player, "messages.enchant.invalid_level");
        }
        return true;
    }

    /**
     * Formats the enchantment name from its key.
     *
     * @param enchant the enchantment.
     * @return the formatted name (e.g., "Sharpness").
     */
    @SuppressWarnings("deprecation")
    private String formatEnchantName(final Enchantment enchant) {
        final String[] words = enchant.getKey().getKey().split("_");
        return Arrays.stream(words)
                     .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                     .collect(Collectors.joining(" "));
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            // Return both the raw key and formatted name for each enchantment.
            return Registry.ENCHANTMENT.stream()
                    .flatMap(enchant -> {
                        final String key = enchant.getKey().getKey();
                        final String name = formatEnchantName(enchant);
                        return List.of(key, name).stream();
                    })
                    .filter(s -> s.toLowerCase().contains(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Arrays.asList("1", "10", "100", "1000", "<level>");
        }
        return Collections.emptyList();
    }

    /**
     * Handles the "list" subcommand for enchantments.
     *
     * @param player the player requesting the list.
     * @return true after displaying the list.
     */
    @SuppressWarnings("deprecation")
    private boolean handleEnchantList(final Player player) {
        final String list = Registry.ENCHANTMENT.stream()
                .map(enchant -> formatEnchantName(enchant) + " (" + enchant.getKey().getKey() + ")")
                .collect(Collectors.joining(", "));
        sendMessage(player, "messages.enchant.list", "%enchantments%", list);
        return true;
    }

    /**
     * Finds an enchantment based on input (either its key or formatted name).
     *
     * @param input the input string.
     * @return the corresponding Enchantment, or null if not found.
     */
    @SuppressWarnings("deprecation")
    private Enchantment findEnchantment(final String input) {
        return Registry.ENCHANTMENT.stream()
                .filter(enchant -> enchant.getKey().getKey().equalsIgnoreCase(input) ||
                        formatEnchantName(enchant).equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates that the specified level is within the acceptable range for the enchantment.
     *
     * @param enchant the enchantment.
     * @param level   the level to validate.
     * @return true if valid; false otherwise.
     */
    private boolean isValidLevel(final Enchantment enchant, final int level) {
        return level >= enchant.getStartLevel() && level <= enchant.getMaxLevel();
    }

    /**
     * Applies the specified enchantment to the item.
     *
     * @param item   the item to enchant.
     * @param enchant the enchantment to apply.
     * @param level   the level of the enchantment.
     */
    private void applyEnchantment(final ItemStack item, final Enchantment enchant, final int level) {
        if (item.getType().name().endsWith("ENCHANTED_BOOK")) {
            final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            if (meta != null) {
                meta.addStoredEnchant(enchant, level, true);
                item.setItemMeta(meta);
            }
        } else {
            final ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(enchant, level, true);
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Sends a success message to the player after applying an enchantment.
     *
     * @param player  the player.
     * @param enchant the enchantment applied.
     * @param level   the level applied.
     */
    private void sendSuccessMessage(final Player player, final Enchantment enchant, final int level) {
        sendMessage(player, "messages.enchant.success", 
                "%enchant%", formatEnchantName(enchant),
                "%level%", String.valueOf(level));
    }
}
