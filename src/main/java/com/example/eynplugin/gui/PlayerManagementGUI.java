package com.example.eynplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.BanList;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;

import com.example.eynplugin.api.LuckPermsHandler;
import com.example.eynplugin.commands.ModerationCommands;
import com.example.eynplugin.utils.BanLookup;

public class PlayerManagementGUI implements Listener {

    private final Player target;
    private final FileConfiguration messagesConfig;
    private final Inventory inventory;
    private final Plugin plugin;
    private final LuckPermsHandler luckPermsHandler;
    private final ModerationCommands moderationCommands;
    private static final String FREEZE_METADATA = "frozen";

    public PlayerManagementGUI(Player target, FileConfiguration messagesConfig, Plugin plugin, LuckPermsHandler luckPermsHandler, ModerationCommands moderationCommands) {
        this.target = target;
        this.messagesConfig = messagesConfig;
        this.plugin = plugin;
        this.luckPermsHandler = luckPermsHandler;
        this.moderationCommands = moderationCommands;
        this.inventory = createInventory();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Manage: " + target.getName());

        // Teleport to Player
        inv.setItem(10, createGuiItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport to Player",
                ChatColor.GRAY + "Click to teleport to " + target.getName()));

        // Kick Player
        inv.setItem(11, createGuiItem(Material.IRON_BOOTS, ChatColor.YELLOW + "Kick Player",
                ChatColor.GRAY + "Click to kick " + target.getName()));

        // Ban Player
        inv.setItem(12, createGuiItem(Material.BARRIER, ChatColor.RED + "Ban Player",
                ChatColor.GRAY + "Click to ban " + target.getName()));

        // Mute Player
        inv.setItem(13, createGuiItem(Material.PAPER, ChatColor.BLUE + "Mute Player",
                ChatColor.GRAY + "Click to mute " + target.getName()));

         // Unmute Player
        inv.setItem(14, createGuiItem(Material.PAPER, ChatColor.BLUE + "Unmute Player",
                ChatColor.GRAY + "Click to unmute " + target.getName()));

        // Freeze Player
        inv.setItem(15, createGuiItem(Material.ICE, ChatColor.AQUA + "Freeze Player",
                ChatColor.GRAY + "Click to freeze/unfreeze " + target.getName()));

        return inv;
    }

    private ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == 10) {
            // Teleport to Player
            if (!player.hasPermission("eyn.manageplayer.teleport")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                player.closeInventory();
                return;
            }
            player.teleport(target);
            player.sendMessage(formatMessage("messages.manageplayer.teleported").replace("%player%", target.getName()));
            player.closeInventory();
        } else if (event.getSlot() == 11) {
            // Kick Player
            if (!player.hasPermission("eyn.manageplayer.kick")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                player.closeInventory();
                return;
            }
            if (!canModerate(player, target)) return;
            target.kickPlayer(formatMessage("messages.manageplayer.kicked_reason").replace("%player%", player.getName()));
            moderationCommands.broadcastMessage("messages.moderation.kick.broadcast",
                    "%player%", player.getName(),
                    "%target%", target.getName(),
                    "%reason%", "Managed by " + player.getName());
            moderationCommands.logModerationAction(player, "KICK", target.getName(), "Managed by " + player.getName());
            player.sendMessage(formatMessage("messages.manageplayer.kicked").replace("%player%", target.getName()));
            player.closeInventory();
        } else if (event.getSlot() == 12) {
            // Ban Player
            if (!player.hasPermission("eyn.manageplayer.ban")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                player.closeInventory();
                return;
            }
            if (!canModerate(player, target)) return;
            BanLookup.banPlayer(target.getName(), formatMessage("messages.manageplayer.banned_reason").replace("%player%", player.getName()), player.getName());
            target.kickPlayer(formatMessage("messages.manageplayer.banned_reason").replace("%player%", player.getName()));
            moderationCommands.broadcastMessage("messages.moderation.ban.broadcast",
                    "%player%", player.getName(),
                    "%target%", target.getName(),
                    "%reason%", "Managed by " + player.getName());
            moderationCommands.logModerationAction(player, "BAN", target.getName(), "Managed by " + player.getName());
            player.sendMessage(formatMessage("messages.manageplayer.banned").replace("%player%", target.getName()));
            player.closeInventory();
        } else if (event.getSlot() == 13) {
            // Mute Player
            if (!player.hasPermission("eyn.manageplayer.mute")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                player.closeInventory();
                return;
            }
            if (!canModerate(player, target)) return;
            luckPermsHandler.mutePlayer(target.getUniqueId(), null, "Managed by " + player.getName());
            moderationCommands.broadcastMessage("messages.moderation.mute.broadcast",
                    "%player%", player.getName(),
                    "%target%", target.getName(),
                    "%reason%", "Managed by " + player.getName());
            player.sendMessage(formatMessage("messages.manageplayer.muted").replace("%player%", target.getName()));
            player.closeInventory();
        } else if (event.getSlot() == 14) {
            // Unmute Player
            if (!player.hasPermission("eyn.manageplayer.unmute")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                player.closeInventory();
                return;
            }
            if (!canModerate(player, target)) return;
            luckPermsHandler.unmutePlayer(target.getUniqueId());
            moderationCommands.broadcastMessage("messages.moderation.unmute.broadcast",
                    "%player%", player.getName(),
                    "%target%", target.getName());
            player.sendMessage(formatMessage("messages.manageplayer.unmuted").replace("%player%", target.getName()));
            player.closeInventory();
        } else if (event.getSlot() == 15) {
            // Freeze Player
            if (!player.hasPermission("eyn.manageplayer.freeze")) {
                player.sendMessage(formatMessage("messages.no_permission"));
                player.closeInventory();
                return;
            }
            if (!canModerate(player, target)) return;
            if (target.hasMetadata(FREEZE_METADATA)) {
                // Unfreeze the player.
                target.removeMetadata(FREEZE_METADATA, plugin);
                Bukkit.broadcastMessage(formatMessage("messages.moderation.freeze.unfreeze_broadcast")
                        .replace("%player%", player.getName())
                        .replace("%target%", target.getName()));
                target.sendMessage(formatMessage("messages.moderation.freeze.unfreeze_target"));
            } else {
                // Freeze the player.
                target.setMetadata(FREEZE_METADATA, new FixedMetadataValue(plugin, true));
                Bukkit.broadcastMessage(formatMessage("messages.moderation.freeze.broadcast")
                        .replace("%player%", player.getName())
                        .replace("%target%", target.getName()));
                target.sendMessage(formatMessage("messages.moderation.freeze.target_message"));
            }
            player.closeInventory();
        }
    }

    private boolean canModerate(CommandSender sender, Player target) {
        if (target == null) return true;

        // Prevent self-moderation.
        if (sender instanceof Player && ((Player) sender).getName().equals(target.getName())) {
            sender.sendMessage(formatMessage("messages.moderation.self_target"));
            return false;
        }

        // Operator immunity check.
        if (target.isOp() && !(sender instanceof Player && ((Player) sender).hasPermission("eyn.moderation.advanced.bypass.op"))) {
            sender.sendMessage(formatMessage("messages.moderation.target_is_op"));
            return false;
        }

        // Check group weight restrictions for non-advanced moderators.
        if (sender instanceof Player) {
            final Player moderator = (Player) sender;
            final int moderatorWeight = moderationCommands.getGroupWeight(moderator);
            final int targetWeight = moderationCommands.getGroupWeight(target);
            final boolean isAdvanced = moderator.hasPermission("eyn.moderation.advanced");
            if (!isAdvanced && targetWeight >= moderatorWeight) {
                sender.sendMessage(formatMessage("messages.moderation.insufficient_rank"));
                return false;
            }
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