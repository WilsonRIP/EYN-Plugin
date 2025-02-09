package com.example.eynplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Bukkit;

public class EffectCommand implements CommandExecutor {

    private final FileConfiguration messagesConfig;

    public EffectCommand(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(formatMessage("messages.effect.usage"));
            return true;
        }

        Player target;
        PotionEffectType effectType;
        int duration = 60; // Default duration: 60 ticks (3 seconds)
        int amplifier = 1; // Default amplifier: 1

        // Determine the target player
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(formatMessage("messages.player_not_found").replace("%player%", args[0]));
                return true;
            }

            // Determine the effect type
            effectType = PotionEffectType.getByName(args[1].toUpperCase());
            if (effectType == null) {
                sender.sendMessage(formatMessage("messages.effect.invalid_effect").replace("%effect%", args[1]));
                return true;
            }

            // Parse duration and amplifier if provided
            if (args.length >= 4) {
                try {
                    duration = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(formatMessage("messages.effect.invalid_duration"));
                    return true;
                }

                try {
                    amplifier = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(formatMessage("messages.effect.invalid_amplifier"));
                    return true;
                }
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(formatMessage("messages.player_only_command"));
                return true;
            }
            target = (Player) sender;

            // Determine the effect type
            effectType = PotionEffectType.getByName(args[0].toUpperCase());
            if (effectType == null) {
                sender.sendMessage(formatMessage("messages.effect.invalid_effect").replace("%effect%", args[0]));
                return true;
            }

            try {
                duration = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(formatMessage("messages.effect.invalid_duration"));
                return true;
            }

            if (args.length >= 3) {
                try {
                    amplifier = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(formatMessage("messages.effect.invalid_amplifier"));
                    return true;
                }
            }
        }

        // Apply the potion effect
        PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier);
        target.addPotionEffect(potionEffect);

        // Send success message
        String effectName = effectType.getName();
        if (sender == target) {
            sender.sendMessage(formatMessage("messages.effect.applied_self")
                    .replace("%effect%", effectName)
                    .replace("%duration%", String.valueOf(duration))
                    .replace("%amplifier%", String.valueOf(amplifier)));
        } else {
            sender.sendMessage(formatMessage("messages.effect.applied_other")
                    .replace("%effect%", effectName)
                    .replace("%duration%", String.valueOf(duration))
                    .replace("%amplifier%", String.valueOf(amplifier))
                    .replace("%player%", target.getName()));
            target.sendMessage(formatMessage("messages.effect.received")
                    .replace("%effect%", effectName)
                    .replace("%duration%", String.valueOf(duration))
                    .replace("%amplifier%", String.valueOf(amplifier))
                    .replace("%sender%", sender.getName()));
        }

        return true;
    }

    private String formatMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Could not find message key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 