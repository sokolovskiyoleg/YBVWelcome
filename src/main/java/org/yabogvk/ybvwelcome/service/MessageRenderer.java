package org.yabogvk.ybvwelcome.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

public class MessageRenderer {
    public Component render(String message, @Nullable Player player) {
        return MessageUtils.parse(message, player);
    }

    public void send(CommandSender sender, String message) {
        MessageUtils.sendMessage(sender, message);
    }

    public void broadcast(String message, @Nullable Player player) {
        if (message == null || message.equalsIgnoreCase("none")) {
            return;
        }

        Bukkit.broadcast(render(message, player));
    }
}
