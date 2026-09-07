package com.privatepm.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;

public class PrivateMessageListener implements Listener {

    private static final Set<String> HANDLED_LABELS = new HashSet<>();
    static {
        HANDLED_LABELS.add("msg");
        HANDLED_LABELS.add("tell");
        HANDLED_LABELS.add("w");
        HANDLED_LABELS.add("m");
        HANDLED_LABELS.add("pm");
    }

    private final PrivatePMPlugin plugin;

    public PrivateMessageListener(PrivatePMPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage();
        if (raw.isEmpty() || raw.charAt(0) != '/') {
            return;
        }

        String withoutSlash = raw.substring(1);
        int spaceIndex = withoutSlash.indexOf(' ');
        String label = (spaceIndex == -1) ? withoutSlash : withoutSlash.substring(0, spaceIndex);
        String remainder = (spaceIndex == -1) ? "" : withoutSlash.substring(spaceIndex + 1).trim();

        int colonIndex = label.indexOf(':');
        if (colonIndex != -1) {
            label = label.substring(colonIndex + 1);
        }

        label = label.toLowerCase();

        if (!HANDLED_LABELS.contains(label)) {
            return;
        }

        Player sender = event.getPlayer();

        if (!sender.hasPermission("privatepm.use")) {
            return;
        }

        event.setCancelled(true);

        handlePrivateMessage(sender, remainder);
    }

    private void handlePrivateMessage(Player sender, String remainder) {
        FileConfiguration config = plugin.getConfig();

        if (remainder.isEmpty()) {
            sender.sendMessage(color(config.getString("messages.usage",
                    "&cUsage: /msg <player> <message>")));
            return;
        }

        String[] parts = remainder.split(" ", 2);
        String targetName = parts[0];

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            sender.sendMessage(color(config.getString("messages.usage",
                    "&cUsage: /msg <player> <message>")));
            return;
        }

        String message = parts[1].trim();

        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(color(config.getString("messages.player-not-online",
                    "&cThat player is not online.")));
            return;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(color(config.getString("messages.self-message",
                    "&cYou cannot message yourself.")));
            return;
        }

        String senderFormat = config.getString("formats.sender", "&e(To &a{receiver}&e) &e{message}");
        String receiverFormat = config.getString("formats.receiver", "&e(From &a{sender}&e) &e{message}");

        String senderOutput = senderFormat
                .replace("{sender}", sender.getName())
                .replace("{receiver}", target.getName())
                .replace("{message}", message);

        String receiverOutput = receiverFormat
                .replace("{sender}", sender.getName())
                .replace("{receiver}", target.getName())
                .replace("{message}", message);

        sender.sendMessage(color(senderOutput));
        target.sendMessage(color(receiverOutput));
    }

    private String color(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
