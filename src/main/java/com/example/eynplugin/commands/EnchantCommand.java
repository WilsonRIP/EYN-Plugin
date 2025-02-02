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

    @SuppressWarnings("deprecation") // Suppress warnings for getKey() usage
    private String formatEnchantName(Enchantment enchant) {
        @SuppressWarnings("deprecation")
		String[] words = enchant.getKey().getKey().split("_");
        return Arrays.stream(words)
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Registry.ENCHANTMENT.stream()
                .map(enchant -> {
                    String key = enchant.getKey().getKey();
                    String name = formatEnchantName(enchant);
                    return Arrays.asList(key, name);
                })
                .flatMap(List::stream)
                .filter(s -> s.toLowerCase().contains(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            return Arrays.asList("1", "10", "100", "1000", "<level>");
        }
        
        return Collections.emptyList();
    }
}
