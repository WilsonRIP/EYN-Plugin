package com.example.eynplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.example.eynplugin.craftbukkit.SetExpFix;
import java.util.Arrays;
import java.util.List;

public class XPCommand extends BaseCommand {
    private static final List<String> SUBCOMMANDS = Arrays.asList("give", "take", "set");
    private static final String PERMISSION_BASE = "eyn.xp";
    private static final String PERMISSION_OTHERS = "eyn.xp.others";

    public XPCommand(FileConfiguration messagesConfig) {
        super(messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPermission(sender, PERMISSION_BASE)) return true;
        if (args.length < 2) {
            sendMessage(sender, "messages.xp.usage");
            return true;
        }

        String action = args[0].toLowerCase();
        if (!SUBCOMMANDS.contains(action)) {
            sendMessage(sender, "messages.xp.usage");
            return true;
        }

        if (!isPositiveInteger(args[1])) {
            sendMessage(sender, "messages.xp.invalid_amount");
            return true;
        }

        int amount = Integer.parseInt(args[1]);
        Player target;

        if (args.length > 2) {
            if (!checkPermission(sender, PERMISSION_OTHERS)) return true;
            target = getTarget(sender, args[2]);
            if (target == null) return true;
        } else {
            if (!checkPlayer(sender)) return true;
            target = (Player) sender;
        }

        handleXPChange(sender, target, action, amount);
        return true;
    }

    private void handleXPChange(CommandSender sender, Player target, String action, int amount) {
        int currentExp = SetExpFix.getTotalExperience(target);
        int newExp;

        switch (action) {
            case "give":
                newExp = currentExp + amount;
                break;
            case "take":
                newExp = Math.max(0, currentExp - amount);
                break;
            case "set":
                newExp = Math.max(0, amount);
                break;
            default:
                return;
        }

        SetExpFix.setTotalExperience(target, newExp);
        
        if (target == sender) {
            sendMessage(sender, "messages.xp." + action + ".self", 
                "%amount%", String.valueOf(amount),
                "%total%", String.valueOf(newExp));
        } else {
            sendMessage(sender, "messages.xp." + action + ".other",
                "%player%", target.getName(),
                "%amount%", String.valueOf(amount),
                "%total%", String.valueOf(newExp));
            sendMessage(target, "messages.xp." + action + ".target",
                "%amount%", String.valueOf(amount),
                "%total%", String.valueOf(newExp));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION_BASE)) {
            return super.onTabComplete(sender, command, alias, args);
        }

        if (args.length == 1) {
            return filterStartingWith(SUBCOMMANDS, args[0]);
        }

        if (args.length == 3 && sender.hasPermission(PERMISSION_OTHERS)) {
            return filterStartingWith(getOnlinePlayerNames(), args[2]);
        }

        return super.onTabComplete(sender, command, alias, args);
    }
} 