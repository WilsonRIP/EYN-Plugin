package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerInfoCommand extends BaseCommand {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Set<UUID> BLUR_ENABLED = new HashSet<>();
    private static final String BLUR_TEXT = "••••••••";

    public PlayerInfoCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig) {
        super(luckPermsHandler, messagesConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPlayer(sender)) return true;
        if (!checkPermission(sender, "eyn.playerinfo")) return true;

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("blur")) {
            toggleBlur(player);
            return true;
        }
        
        Player target;
        if (args.length > 0) {
            target = getTarget(sender, args[0]);
            if (target == null) return true;
        } else {
            target = player;
        }

        displayPlayerInfo(player, target);
        return true;
    }

    private void toggleBlur(Player player) {
        if (BLUR_ENABLED.contains(player.getUniqueId())) {
            BLUR_ENABLED.remove(player.getUniqueId());
            sendMessage(player, "messages.playerinfo.blur_disabled");
        } else {
            BLUR_ENABLED.add(player.getUniqueId());
            sendMessage(player, "messages.playerinfo.blur_enabled");
        }
    }

    private void displayPlayerInfo(Player sender, Player target) {
        Location loc = target.getLocation();
        String firstPlayed = DATE_FORMAT.format(new Date(target.getFirstPlayed()));
        boolean isBlurred = BLUR_ENABLED.contains(sender.getUniqueId());
        
        sendMessage(sender, "messages.playerinfo.header", "%player%", target.getName());
        sendMessage(sender, "messages.playerinfo.gamemode", "%gamemode%", target.getGameMode().toString());
        sendMessage(sender, "messages.playerinfo.health", "%health%", String.format("%.1f", target.getHealth()));
        sendMessage(sender, "messages.playerinfo.food", "%food%", String.valueOf(target.getFoodLevel()));

        // Location with copy button
        String locationStr = String.format("%s, X: %.2f, Y: %.2f, Z: %.2f", 
            loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
        sendCopyableMessage(sender, "messages.playerinfo.location", locationStr, 
            "Click to copy location", locationStr);

        if (checkPermission(sender, "eyn.playerinfo.advanced")) {
            // IP Address with blur option and copy button
            String ip = target.getAddress().getAddress().getHostAddress();
            sendCopyableMessage(sender, "messages.playerinfo.ip", 
                isBlurred ? BLUR_TEXT : ip, 
                "Click to copy IP address", ip);

            // UUID with copy button
            String uuid = target.getUniqueId().toString();
            sendCopyableMessage(sender, "messages.playerinfo.uuid", 
                isBlurred ? BLUR_TEXT : uuid,
                "Click to copy UUID", uuid);
        }
        
        sendMessage(sender, "messages.playerinfo.first_played", "%date%", firstPlayed);
        sendMessage(sender, "messages.playerinfo.is_vanished", 
            "%vanished%", target.hasMetadata("vanished") ? "Yes" : "No");
        sendMessage(sender, "messages.playerinfo.is_op", 
            "%op%", target.isOp() ? "Yes" : "No");
        sendMessage(sender, "messages.playerinfo.footer");
    }

    private void sendCopyableMessage(Player player, String messageKey, String displayText, 
            String hoverText, String copyText) {
        TextComponent baseComponent = new TextComponent(formatMessage(messageKey));
        TextComponent clickablePart = new TextComponent("§r" + displayText);
        
        clickablePart.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText));
        clickablePart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            Collections.singletonList(new Text(hoverText))));

        baseComponent.addExtra(clickablePart);
        player.spigot().sendMessage(baseComponent);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(filterStartingWith(getOnlinePlayerNames(), args[0]));
            if ("blur".startsWith(args[0].toLowerCase())) {
                completions.add("blur");
            }
            return completions;
        }
        return new ArrayList<>();
    }
} 