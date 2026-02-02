package org.yabogvk.ybvwelcome.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class WelcomeCore {
    private final YBVWelcome plugin;
    private final MessageManager messageManager;
    private final MessageService messageService;
    private final Database db;

    private final Map<UUID, PlayerMessages> playerCache = new ConcurrentHashMap<>();

    public WelcomeCore(YBVWelcome plugin, MessageManager messageManager, Database database) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.db = database;
        this.messageService = new MessageService(messageManager);
    }

    public void loadPlayerData(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                PlayerMessages messages = db.getMessages(player.getUniqueId());
                updatePlayerCache(player.getUniqueId(), messages);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Ошибка загрузки данных для " + player.getName(), e);
            }
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public void loadAndJoin(Player player, boolean isFirstJoin) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return db.getMessages(player.getUniqueId());
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Ошибка загрузки данных для " + player.getName() + ", используется сообщение по-умолчанию.", e);
                        return new PlayerMessages(null, null);
                    }
                }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable))
                .thenAccept(messages -> {
                    updatePlayerCache(player.getUniqueId(), messages);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (isFirstJoin) {
                            handlePlayerFirstJoin(player);
                        } else {
                            handlePlayerJoin(player);
                        }
                    });
                });
    }

    private void updatePlayerCache(UUID uuid, PlayerMessages messages) {
        if (!messages.hasNoMessages()) {
            playerCache.put(uuid, messages);
        } else {
            playerCache.remove(uuid);
        }
    }

    public void handlePlayerJoin(Player player) {
        PlayerMessages data = playerCache.get(player.getUniqueId());
        String raw = messageService.resolveJoinMessage(player, data);
        broadcast(raw, player);
    }

    public void handlePlayerQuit(Player player) {
        PlayerMessages data = playerCache.get(player.getUniqueId());
        String raw = messageService.resolveQuitMessage(player, data);
        broadcast(raw, player);
    }

    public void handlePlayerFirstJoin(Player target) {
        String rawMessage = messageManager.getFirstJoin();
        if (rawMessage != null && !rawMessage.equalsIgnoreCase("none")) {
            String processed = rawMessage.replace("{player}", target.getName());
            if (plugin.isPlaceholderAPIEnabled()) {
                processed = PlaceholderAPI.setPlaceholders(target, processed);
            }
            Bukkit.broadcast(MessageUtils.parse(processed));
        }

        String rawPrompt = messageManager.getFirstJoinButtonPrompt();
        if (rawPrompt == null || rawPrompt.equalsIgnoreCase("none")) return;

        String processedPrompt = rawPrompt.replace("{player}", target.getName());
        if (plugin.isPlaceholderAPIEnabled()) {
            processedPrompt = PlaceholderAPI.setPlaceholders(target, processedPrompt);
        }
        Component promptComponent = MessageUtils.parse(processedPrompt);

        final Set<UUID> clickedPlayers = ConcurrentHashMap.newKeySet();

        String buttonText = messageManager.getWelcomeButtonText();
        String hoverText = messageManager.getWelcomeButtonHover();

        if (plugin.isPlaceholderAPIEnabled()) {
            buttonText = PlaceholderAPI.setPlaceholders(target, buttonText);
            hoverText = PlaceholderAPI.setPlaceholders(target, hoverText);
        }

        Component button = MessageUtils.parse(buttonText)
                .hoverEvent(HoverEvent.showText(MessageUtils.parse(hoverText)))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player clicker) {
                        if (clicker.getUniqueId().equals(target.getUniqueId())) {
                            return;
                        }
                        if (clickedPlayers.add(clicker.getUniqueId())) {
                            sendRandomWelcome(clicker, target);
                        }
                    }
                }, builder -> builder.uses(-1).lifetime(Duration.ofMinutes(10))));

        Bukkit.broadcast(promptComponent.append(Component.space()).append(button));
    }

    private void sendRandomWelcome(Player clicker, Player target) {
        List<String> variants = messageManager.getRandomWelcomes();
        if (variants.isEmpty()) return;

        String randomMsg = variants.get(new java.util.Random().nextInt(variants.size()));
        String processedMsg = randomMsg.replace("{player}", target.getName());

        if (plugin.isPlaceholderAPIEnabled()) {
            processedMsg = PlaceholderAPI.setPlaceholders(clicker, processedMsg);
        }

        clicker.chat(processedMsg);
    }

    public void setPlayerWelcomeMessage(Player player, String message) {
        setPlayerMessage(player, message, Database.MessageType.WELCOME);
    }

    public void setPlayerQuitMessage(Player player, String message) {
        setPlayerMessage(player, message, Database.MessageType.QUIT);
    }

    private void setPlayerMessage(Player player, String message, Database.MessageType type) {
        if (checkLengthMessage(message) > plugin.getSettings().allowedSymbols) {
            MessageUtils.sendMessage(player, messageManager.getToManySymbols());
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                db.setMessage(player.getUniqueId(), player.getName(), message, type);
                loadPlayerData(player);
                Bukkit.getScheduler().runTask(plugin, () -> MessageUtils.sendMessage(player, messageManager.getSetSuccess()));
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> MessageUtils.sendMessage(player, messageManager.getErrorDatabase()));
            }
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public void clearPlayerJoinMessage(Player player) {
        clearPlayerMessage(player, Database.MessageType.WELCOME);
    }

    public void clearPlayerQuitMessage(Player player) {
        clearPlayerMessage(player, Database.MessageType.QUIT);
    }

    private void clearPlayerMessage(Player player, Database.MessageType type) {
        CompletableFuture.runAsync(() -> {
            try {
                db.deleteMessage(player.getUniqueId(), type);
                loadPlayerData(player);
                Bukkit.getScheduler().runTask(plugin, () -> MessageUtils.sendMessage(player, messageManager.getClearSuccess()));
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> MessageUtils.sendMessage(player, messageManager.getErrorDatabase()));
            }
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    private void broadcast(String raw, Player player) {
        if (raw == null || raw.equalsIgnoreCase("none")) return;

        String processed = raw.replace("{player}", player.getName());
        if (plugin.isPlaceholderAPIEnabled()) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        Component component = MessageUtils.parse(processed);
        Bukkit.broadcast(component);
    }

    private int checkLengthMessage(String message) {
        if (message == null) return 0;
        return MessageUtils.stripColors(message).replace(" ", "").length();
    }

    public void close() {
        if (db != null) db.close();
    }

    public void reload() {
        playerCache.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }
}
