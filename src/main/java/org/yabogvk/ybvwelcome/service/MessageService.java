package org.yabogvk.ybvwelcome.service;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class MessageService {
    private final BooleanSupplier placeholderEnabled;
    private final MessageManager messageManager;
    private final MessageUtils messageUtils;

    public MessageService(BooleanSupplier placeholderEnabled, MessageManager messageManager, MessageUtils messageUtils) {
        this.placeholderEnabled = placeholderEnabled;
        this.messageManager = messageManager;
        this.messageUtils = messageUtils;
    }

    public void send(CommandSender sender, String message) {
        messageUtils.sendMessage(sender, message);
    }

    public void broadcastJoin(Player player, @Nullable PlayerMessages messages) {
        broadcast(resolveMessage(player, messages, Database.MessageType.WELCOME), player);
    }

    public void broadcastQuit(Player player, @Nullable PlayerMessages messages) {
        broadcast(resolveMessage(player, messages, Database.MessageType.QUIT), player);
    }

    public void broadcastFirstJoin(Player target) {
        broadcast(messageManager.getFirstJoin(), target);

        String rawPrompt = messageManager.getFirstJoinButtonPrompt();
        if (rawPrompt == null || rawPrompt.equalsIgnoreCase("none")) {
            return;
        }

        Component promptComponent = messageUtils.parse(rawPrompt, target);
        Set<java.util.UUID> clickedPlayers = new HashSet<>();

        Component button = messageUtils.parse(messageManager.getWelcomeButtonText(), target)
                .hoverEvent(HoverEvent.showText(messageUtils.parse(messageManager.getWelcomeButtonHover(), target)))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (!(audience instanceof Player clicker)) {
                        return;
                    }
                    if (clicker.getUniqueId().equals(target.getUniqueId())) {
                        return;
                    }
                    if (clickedPlayers.add(clicker.getUniqueId())) {
                        sendRandomWelcome(clicker, target);
                    }
                }, builder -> builder.uses(-1).lifetime(Duration.ofMinutes(10))));

        Bukkit.broadcast(promptComponent.append(Component.space()).append(button));
    }

    private void broadcast(String message, @Nullable Player player) {
        if (message == null || message.equalsIgnoreCase("none")) {
            return;
        }

        Bukkit.broadcast(messageUtils.parse(message, player));
    }

    public String resolveMessage(Player player, @Nullable PlayerMessages messages, Database.MessageType type) {
        boolean join = type == Database.MessageType.WELCOME;
        String customMessage = messages == null ? null : (join ? messages.joinMessage() : messages.quitMessage());
        String customFormat = join ? messageManager.getFormatJoinCustom() : messageManager.getFormatQuitCustom();
        String defaultMessage = join ? messageManager.getJoinDefault() : messageManager.getQuitDefault();
        String defaultFormat = join ? messageManager.getFormatJoinDefault() : messageManager.getFormatQuitDefault();
        if (customMessage != null && !customMessage.isEmpty()) {
            return customFormat.replace("{message}", customMessage);
        }

        String groupMessage = messageManager.getGroupMessage(player, join ? "join" : "quit");
        if (groupMessage != null) {
            return groupMessage;
        }

        return defaultFormat.replace("{message}", defaultMessage);
    }

    private void sendRandomWelcome(Player clicker, Player target) {
        List<String> variants = messageManager.getRandomWelcomes();
        if (variants.isEmpty()) {
            return;
        }

        String randomMessage = variants.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(variants.size()));
        String processedMessage = randomMessage.replace("{player}", target.getName());

        if (placeholderEnabled.getAsBoolean()) {
            processedMessage = PlaceholderAPI.setPlaceholders(clicker, processedMessage);
        }

        clicker.chat(processedMessage);
    }
}
