package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Command for changing a player's game mode.
 *
 * <p>
 * Usage:
 * <ul>
 *     <li><code>/gmc [player]</code> - Set game mode to Creative.</li>
 *     <li><code>/gms [player]</code> - Set game mode to Survival.</li>
 *     <li><code>/gmsp [player]</code> - Set game mode to Spectator.</li>
 *     <li><code>/gma [player]</code> - Set game mode to Adventure.</li>
 * </ul>
 * If no player is specified, the command applies to the sender.
 * </p>
 */
public class GamemodeCommand extends BaseCommand {

    private static final Map<String, GameMode> GAMEMODE_MAP = Map.of(
        "gmc", GameMode.CREATIVE,
        "gms", GameMode.SURVIVAL,
        "gmsp", GameMode.SPECTATOR,
        "gma", GameMode.ADVENTURE
    );

    public GamemodeCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    /**
     * Handles the execution of the gamemode command.
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias used for the command.
     * @param args    The command arguments. Optionally, the target player's name.
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure only players can run this command.
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }
        final Player player = (Player) sender;

        // Check for base gamemode permission.
        if (!checkPermission(player, "eyn.gamemode")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        // Determine the target: self by default, or another player if provided and permitted.
        final Player target;
        if (args.length > 0) {
            if (!checkPermission(player, "eyn.gamemode.others")) {
                player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Utils.colorize(getMessage("messages.error.player_not_found")));
                return true;
            }
        } else {
            target = player;
        }

        // Retrieve the GameMode from the command label.
        final GameMode gameMode = GAMEMODE_MAP.get(label.toLowerCase());
        if (gameMode == null) {
            player.sendMessage(Utils.colorize(getMessage("messages.gamemode.invalid")));
            return true;
        }

        // Set the target's game mode.
        target.setGameMode(gameMode);

        // Send feedback messages.
        final String gamemodeMessage = getMessage("messages.gamemode." + gameMode.name().toLowerCase());
        if (target.equals(player)) {
            player.sendMessage(Utils.colorize(gamemodeMessage));
        } else {
            player.sendMessage(Utils.colorize(getMessage("messages.gamemode.changed_other")
                    .replace("%player%", target.getName())
                    .replace("%gamemode%", gameMode.name().toLowerCase())));
            target.sendMessage(Utils.colorize(gamemodeMessage));
        }
        return true;
    }

    /**
     * Provides tab completion for the gamemode command.
     *
     * @param sender  The command sender.
     * @param command The command.
     * @param alias   The alias used.
     * @param args    The command arguments.
     * @return A list of possible completions for the command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("gmc", "gms", "gmsp", "gma");
        }
        return Collections.emptyList();
    }
}
