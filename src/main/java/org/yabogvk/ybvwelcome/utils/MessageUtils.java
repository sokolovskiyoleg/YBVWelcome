package org.yabogvk.ybvwelcome.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;

public class MessageUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().build();

    public static Component parse(String message, Player player) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        String processed = message;
        if (player != null) {
            processed = processed.replace("{player}", player.getName());

            if (YBVWelcome.getInstance().isPlaceholderAPIEnabled()) {
                processed = PlaceholderAPI.setPlaceholders(player, processed);
            }
        }

        String colored = colorize(processed);
        return LEGACY_SERIALIZER.deserialize(colored);
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return ColorizerProvider.COLORIZER.colorize(message);
    }

    public static String stripColors(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        String colored = colorize(message);
        return ChatColor.stripColor(colored);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            Player player = sender instanceof Player ? (Player) sender : null;
            sender.sendMessage(parse(message, player));
        }
    }
}
