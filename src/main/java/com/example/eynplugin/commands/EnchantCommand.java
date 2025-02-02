package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantCommand extends BaseCommand {

    public EnchantCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.player_only_command");
            return true;
        }

        if (!checkPermission(player, "eyn.enchant")) return true;

        if (args.length < 1 || args[0].equalsIgnoreCase("list")) {
            return handleEnchantList(player);
        }

        if (args.length < 2) {
            sendMessage(player, "messages.enchant.usage");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sendMessage(player, "messages.enchant.no_item");
            return true;
        }

        try {
            Enchantment enchant = findEnchantment(args[0]);
            if (enchant == null) {
                sendMessage(player, "messages.enchant.invalid_enchant");
                return true;
            }

            int level = Integer.parseInt(args[1]);
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

    @SuppressWarnings("deprecation")
    private Enchantment findEnchantment(String input) {
        String search = input.toLowerCase();
        return Registry.ENCHANTMENT.stream()
            .filter(e -> e.getKey().getKey().contains(search))
            .findFirst()
            .orElse(null);
    }

    private boolean isValidLevel(Enchantment enchant, int level) {
        return level > 0 && level <= enchant.getMaxLevel() * 2;
    }

    private void applyEnchantment(ItemStack item, Enchantment enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta esMeta) {
            esMeta.addStoredEnchant(enchant, level, true);
        } else {
            meta.addEnchant(enchant, level, true);
        }
        item.setItemMeta(meta);
    }

    private void sendSuccessMessage(Player player, Enchantment enchant, int level) {
        @SuppressWarnings("deprecation")
        String[] nameParts = enchant.getKey().getKey().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                formattedName.append(Character.toUpperCase(part.charAt(0)))
                           .append(part.substring(1).toLowerCase())
                           .append(" ");
            }
        }
        
        sendMessage(player, "messages.enchant.success",
            "%enchant%", formattedName.toString().trim(),
            "%level%", String.valueOf(level));
    }

    private boolean handleEnchantList(Player player) {
        sendMessage(player, "messages.enchant.list_header");
        for (Enchantment enchant : Registry.ENCHANTMENT) {
            @SuppressWarnings("deprecation")
            String formatted = formatMessage("messages.enchant.list_item")
                .replace("%enchant%", formatEnchantName(enchant))
                .replace("%key%", enchant.getKey().getKey());
            player.sendMessage(formatted);
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Registry.ENCHANTMENT.stream()
                .map(e -> e.getKey().getKey())
                .filter(name -> name.startsWith(args[0].toLowerCase()))
                .toList();
        }
        return Collections.emptyList();
    }

    private String formatEnchantName(Enchantment enchant) {
        @SuppressWarnings("deprecation")
		String[] words = enchant.getKey().getKey().split("_");
        return Arrays.stream(words)
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}
