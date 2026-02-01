package org.yabogvk.ybvwelcome.core;

import me.clip.placeholderapi.PlaceholderAPI;
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

import java.util.List;
import java.util.Map;
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
        playerCache.remove(player.getUniqueId());
    }

    public void handlePlayerFirstJoin(Player target) {
        String rawMessage = messageManager.getFirstJoin();
        if (rawMessage == null || rawMessage.equalsIgnoreCase("none")) return;

        String formatted = rawMessage.replace("{player}", target.getName());
        Component mainComponent = MessageUtils.parse(formatted);

        Component button = MessageUtils.parse(messageManager.getWelcomeButtonText())
                .hoverEvent(HoverEvent.showText(MessageUtils.parse(messageManager.getWelcomeButtonHover())))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player clicker) {
                        sendRandomWelcome(clicker, target);
                    }
                }));

        Bukkit.broadcast(mainComponent.append(button));
    }

    private void sendRandomWelcome(Player clicker, Player target) {
        List<String> variants = messageManager.getRandomWelcomes();
        if (variants.isEmpty()) return;

        String randomMsg = variants.get(new java.util.Random().nextInt(variants.size()));
        String finalMsg = randomMsg.replace("{player}", target.getName());

        clicker.chat(MessageUtils.colorize(finalMsg));
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

        String formatted = raw.replace("{player}", player.getName());

        if (plugin.isPlaceholderAPIEnabled()) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        String colored = MessageUtils.colorize(formatted);

        Bukkit.broadcastMessage(colored);
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
