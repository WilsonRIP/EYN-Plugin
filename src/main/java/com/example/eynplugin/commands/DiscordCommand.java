package com.example.eynplugin.commands;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DiscordCommand extends BaseCommand {
    private final String discordLink;

    public DiscordCommand(LuckPermsHandler luckPermsHandler, FileConfiguration messagesConfig, FileConfiguration config) {
        super(luckPermsHandler, messagesConfig);
        this.discordLink = config.getString("discord.link", "https://discord.gg/example");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("messages.player_only_command")));
            return true;
        }

        Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.discord")) {
            player.sendMessage(Utils.colorize(getMessage("messages.no_permission")));
            return true;
        }

        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', getMessage("messages.discord.message")));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordLink));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new Text[] { new Text(ChatColor.translateAlternateColorCodes('&', getMessage("messages.discord.hover"))) }));
        
        player.spigot().sendMessage(message);
        return true;
    }
} 