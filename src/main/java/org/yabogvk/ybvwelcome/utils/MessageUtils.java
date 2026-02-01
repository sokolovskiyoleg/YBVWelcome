package org.yabogvk.ybvwelcome.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;

public class MessageUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().build();

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String colored = colorize(message);
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
            sender.sendMessage(parse(message));
        }
    }
}
