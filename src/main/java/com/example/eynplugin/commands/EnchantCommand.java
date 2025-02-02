package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnchantCommand extends BaseCommand {

    public EnchantCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @SuppressWarnings("deprecation") // Suppress warnings for getKey() usage
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "messages.player_only_command");
            return true;
        }

        Player player = (Player) sender;
        if (!checkPermission(player, "eyn.enchant")) {
            return true;
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
            String enchantName = args[0].toLowerCase();
            // Use the registry stream instead of getValues()
            Enchantment enchant = Registry.ENCHANTMENT.stream()
                .filter(e -> e.getKey().getKey().toLowerCase().contains(enchantName))
                .findFirst()
                .orElse(null);

            if (enchant == null) {
                sendMessage(player, "messages.enchant.invalid_enchant");
                return true;
            }

            int level = Integer.parseInt(args[1]);
            item.addUnsafeEnchantment(enchant, level);
            sendMessage(player, "messages.enchant.success",
                        "%enchant%", formatEnchantName(enchant),
                        "%level%", String.valueOf(level));
        } catch (NumberFormatException e) {
            sendMessage(player, "messages.enchant.invalid_level");
        }
        return true;
    }

    @SuppressWarnings("deprecation") // Suppress warnings for getKey() usage
    private String formatEnchantName(Enchantment enchant) {
        // Retrieve the key using getKey() and split underscores to format the name
        String name = enchant.getKey().getKey();
        String[] words = name.split("_");
        return Arrays.stream(words)
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}
