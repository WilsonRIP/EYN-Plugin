package com.example.eynplugin.commands;

import com.example.eynplugin.craftbukkit.SetExpFix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Command for managing player experience points.
 * Supports subcommands "give", "take", and "set" for modifying XP.
 */
public class XPCommand extends BaseCommand {
    private static final List<String> SUBCOMMANDS = Arrays.asList("give", "take", "set");
    private static final String PERMISSION_BASE = "eyn.xp";
    private static final String PERMISSION_OTHERS = "eyn.xp.others";

    /**
     * Constructs a new XPCommand.
     *
     * @param messagesConfig the configuration file for messages.
     */
    public XPCommand(FileConfiguration messagesConfig) {
        super(messagesConfig);
    }

    /**
     * Executes the XP command.
     * Usage: /xp <give|take|set> <amount> [player]
     *
     * @param sender  the command sender.
     * @param command the command executed.
     * @param label   the alias used.
     * @param args    the command arguments.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verify base permission
        if (!checkPermission(sender, PERMISSION_BASE)) {
            return true;
        }

        // Validate argument length.
        if (args.length < 2) {
            sendMessage(sender, "messages.xp.usage");
            return true;
        }

        final String action = args[0].toLowerCase();
        if (!SUBCOMMANDS.contains(action)) {
            sendMessage(sender, "messages.xp.usage");
            return true;
        }

        // Validate that the amount is a positive integer.
        if (!isPositiveInteger(args[1])) {
            sendMessage(sender, "messages.xp.invalid_amount");
            return true;
        }
        final int amount = Integer.parseInt(args[1]);

        // Determine the target player: if provided, target must be online.
        final Player target;
        if (args.length > 2) {
            if (!checkPermission(sender, PERMISSION_OTHERS)) {
                return true;
            }
            target = getTarget(sender, args[2]);
            if (target == null) {
                return true;
            }
        } else {
            if (!checkPlayer(sender)) {
                return true;
            }
            target = (Player) sender;
        }

        // Process the XP change.
        handleXPChange(sender, target, action, amount);
        return true;
    }

    /**
     * Handles the XP change logic based on the specified action.
     *
     * @param sender the command sender.
     * @param target the target player whose XP will be modified.
     * @param action the XP action ("give", "take", or "set").
     * @param amount the amount of XP to process.
     */
    private void handleXPChange(CommandSender sender, Player target, String action, int amount) {
        final int currentExp = SetExpFix.getTotalExperience(target);
        final int newExp;

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
                // Should never reach here since we already validate action.
                return;
        }

        SetExpFix.setTotalExperience(target, newExp);

        // Construct the message keys based on action and whether the target is the sender.
        if (target.equals(sender)) {
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

    /**
     * Provides tab completion for the XP command.
     *
     * @param sender  the command sender.
     * @param command the command.
     * @param alias   the alias used.
     * @param args    the current command arguments.
     * @return a list of matching suggestions.
     */
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
