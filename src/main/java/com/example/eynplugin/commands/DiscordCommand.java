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

/**
 * DiscordCommand sends a clickable Discord invitation message to the player.
 * The message displays hover text and opens the Discord link when clicked.
 *
 * <p>
 * Example usage: /discord
 * </p>
 */
public class DiscordCommand extends BaseCommand {
    private final String discordLink;

    /**
     * Constructs a new DiscordCommand.
     *
     * @param luckPermsHandler the LuckPerms handler instance.
     * @param messagesConfig   the configuration file for messages.
     * @param config           the main configuration file containing the Discord link.
     */
    public DiscordCommand(final LuckPermsHandler luckPermsHandler, final FileConfiguration messagesConfig, final FileConfiguration config) {
        super(luckPermsHandler, messagesConfig);
        this.discordLink = config.getString("discord.link", "https://discord.gg/example");
    }

    /**
     * Processes the /discord command.
     * Only players with the proper permission ("eyn.discord") can use this command.
     *
     * @param sender  the command sender.
     * @param command the executed command.
     * @param label   the alias used.
     * @param args    command arguments (unused).
     * @return true after processing the command.
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("messages.player_only_command"));
            return true;
        }
        final Player player = (Player) sender;
        if (!Utils.checkPermission(player, "eyn.discord")) {
            player.sendMessage(formatMessage("messages.no_permission"));
            return true;
        }

        // Build the clickable Discord invitation message.
        final String messageText = formatMessage("messages.discord.message");
        final TextComponent discordMessage = new TextComponent(messageText);
        discordMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordLink));
        discordMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text[] { new Text(formatMessage("messages.discord.hover")) }));

        player.spigot().sendMessage(discordMessage);
        return true;
    }

    /**
     * Retrieves and formats a message from the messages configuration.
     * This method applies alternate color codes.
     *
     * @param key the configuration key.
     * @return the formatted message.
     */
    @Override
    protected String formatMessage(final String key) {
        String message = getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
