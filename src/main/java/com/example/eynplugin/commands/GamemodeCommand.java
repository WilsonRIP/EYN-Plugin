package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.example.eynplugin.Utils;
import org.bukkit.Bukkit;

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize(getMessage("messages.player_only_command")));
            return true;
        }
        Player player = (Player) sender;

        if (!checkPermission(player, "eyn.gamemode")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        Player target = player;
        if (args.length > 0 && checkPermission(player, "eyn.gamemode.others")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Utils.colorize(getMessage("messages.error.player_not_found")));
                return true;
            }
        }

        GameMode gameMode = GAMEMODE_MAP.get(label.toLowerCase());
        if (gameMode == null) {
            player.sendMessage(Utils.colorize(getMessage("messages.gamemode.invalid")));
            return true;
        }

        target.setGameMode(gameMode);
        if (target == player) {
            player.sendMessage(Utils.colorize(getMessage("messages.gamemode." + gameMode.name().toLowerCase())));
        } else {
            player.sendMessage(Utils.colorize(getMessage("messages.gamemode.changed_other")
                .replace("%player%", target.getName())
                .replace("%gamemode%", gameMode.name().toLowerCase())));
            target.sendMessage(Utils.colorize(getMessage("messages.gamemode." + gameMode.name().toLowerCase())));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("gmc", "gms", "gmsp", "gma");
        }
        return null;
    }
} 