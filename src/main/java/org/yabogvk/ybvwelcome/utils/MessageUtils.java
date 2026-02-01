package org.yabogvk.ybvwelcome.utils;

import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public class MessageUtils {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('&') + "[0-9A-FK-ORX]");

    public static Component parse(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        String colored = colorize(message);

        return LegacyComponentSerializer.legacySection().deserialize(colored);
    }


    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";
        return ColorizerProvider.COLORIZER.colorize(message);
    }

    public static String stripColors(String message) {
        if (message == null || message.isEmpty()) return "";
        return STRIP_COLOR_PATTERN.matcher(message).replaceAll("");
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(colorize(message));
        }
    }
}
