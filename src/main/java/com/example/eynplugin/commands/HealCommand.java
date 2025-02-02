package com.example.eynplugin.commands;

import com.example.eynplugin.Utils;
import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;

public class HealCommand extends BaseCommand {

    public HealCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }

        Player target;
        if (args.length > 0) {
            if (!checkPermission(sender, "eyn.heal.others")) return true;
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, "messages.error.player_not_found");
                return true;
            }
        } else {
            target = (Player) sender;
            if (!checkPermission(target, "eyn.heal")) return true;
        }

        healPlayer(target);
        
        if (target != sender) {
            sendMessage(sender, "messages.heal.other_success", "%player%", target.getName());
            sendMessage(target, "messages.heal.healed_by", "%player%", sender.getName());
        } else {
            sendMessage(target, "messages.heal.success");
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private void healPlayer(Player player) {
        // Get config values with defaults
        double maxHealth = messagesConfig.getDouble("heal.max_health", 20.0);
        int foodLevel = messagesConfig.getInt("heal.food_level", 20);
        boolean clearFire = messagesConfig.getBoolean("heal.clear_fire", true);
        boolean clearEffects = messagesConfig.getBoolean("heal.clear_effects", true);

        player.setHealth(Math.min(player.getMaxHealth(), maxHealth));
        player.setFoodLevel(foodLevel);
        
        if(clearFire) {
            player.setFireTicks(0);
        }
        
        if(clearEffects) {
            player.getActivePotionEffects().forEach(effect -> 
                player.removePotionEffect(effect.getType()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && checkPermission(sender, "eyn.heal.others")) {
            return getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
} 