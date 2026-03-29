package org.yabogvk.ybvwelcome.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.color.Colorizer;

public final class MessageUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().build();
    private final YBVWelcome plugin;
    private volatile Colorizer colorizer;

    public MessageUtils(@Nullable YBVWelcome plugin, Colorizer colorizer) {
        this.plugin = plugin;
        this.colorizer = colorizer;
    }

    public void setColorizer(Colorizer colorizer) {
        this.colorizer = colorizer;
    }

    public Component parse(String message, @Nullable Player player) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        String processed = message;
        if (player != null) {
            processed = processed.replace("{player}", player.getName());

            if (plugin != null && plugin.isPlaceholderAPIEnabled()) {
                processed = PlaceholderAPI.setPlaceholders(player, processed);
            }
        }

        String colored = colorize(processed);
        return LEGACY_SERIALIZER.deserialize(colored);
    }

    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return colorizer.colorize(message);
    }

    public String stripColors(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        String colored = colorize(message);
        return ChatColor.stripColor(colored);
    }

    public void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            Player player = sender instanceof Player ? (Player) sender : null;
            sender.sendMessage(parse(message, player));
        }
    }
}
