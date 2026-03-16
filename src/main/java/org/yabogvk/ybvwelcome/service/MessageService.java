package org.yabogvk.ybvwelcome.service;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageService {
    private final YBVWelcome plugin;
    private final MessageManager messageManager;
    private final MessageRenderer renderer;

    public MessageService(YBVWelcome plugin, MessageManager messageManager, MessageRenderer renderer) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.renderer = renderer;
    }

    public void reload() {
    }

    public void send(CommandSender sender, String message) {
        renderer.send(sender, message);
    }

    public void broadcastJoin(Player player, @Nullable PlayerMessages messages) {
        renderer.broadcast(resolveMessage(player, messages, Database.MessageType.WELCOME), player);
    }

    public void broadcastQuit(Player player, @Nullable PlayerMessages messages) {
        renderer.broadcast(resolveMessage(player, messages, Database.MessageType.QUIT), player);
    }

    public void broadcastFirstJoin(Player target) {
        renderer.broadcast(messageManager.getFirstJoin(), target);

        String rawPrompt = messageManager.getFirstJoinButtonPrompt();
        if (rawPrompt == null || rawPrompt.equalsIgnoreCase("none")) {
            return;
        }

        Component promptComponent = renderer.render(rawPrompt, target);
        Set<java.util.UUID> clickedPlayers = ConcurrentHashMap.newKeySet();

        Component button = renderer.render(messageManager.getWelcomeButtonText(), target)
                .hoverEvent(HoverEvent.showText(renderer.render(messageManager.getWelcomeButtonHover(), target)))
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

    public String resolveMessage(Player player, @Nullable PlayerMessages messages, Database.MessageType type) {
        String customMessage;
        String customFormat;
        String defaultMessage;
        String defaultFormat;
        String groupType;

        if (type == Database.MessageType.WELCOME) {
            customMessage = messages != null ? messages.joinMessage() : null;
            customFormat = messageManager.getFormatJoinCustom();
            defaultMessage = messageManager.getJoinDefault();
            defaultFormat = messageManager.getFormatJoinDefault();
            groupType = "join";
        } else {
            customMessage = messages != null ? messages.quitMessage() : null;
            customFormat = messageManager.getFormatQuitCustom();
            defaultMessage = messageManager.getQuitDefault();
            defaultFormat = messageManager.getFormatQuitDefault();
            groupType = "quit";
        }

        if (customMessage != null && !customMessage.isEmpty()) {
            return customFormat.replace("{message}", customMessage);
        }

        String groupMessage = messageManager.getGroupMessage(player, groupType);
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

        if (plugin.isPlaceholderAPIEnabled()) {
            processedMessage = PlaceholderAPI.setPlaceholders(clicker, processedMessage);
        }

        clicker.chat(processedMessage);
    }
}
