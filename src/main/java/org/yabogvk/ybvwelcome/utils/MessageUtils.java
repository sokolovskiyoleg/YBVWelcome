package org.yabogvk.ybvwelcome.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;

public class MessageUtils {

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";
        return ColorizerProvider.COLORIZER.colorize(message);
    }

    public static String stripColors(String message) {
        if (message == null || message.isEmpty()) return "";
        return ChatColor.stripColor(colorize(message));
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(colorize(message));
        }
    }
}