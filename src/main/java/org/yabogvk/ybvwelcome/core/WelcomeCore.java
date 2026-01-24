package org.yabogvk.ybvwelcome.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerCache;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WelcomeCore {
    private final YBVWelcome plugin;
    private final MessageManager messageManager;
    private final MessageService messageService;
    private final Database db;

    private final Map<UUID, PlayerCache> playerCache = new ConcurrentHashMap<>();

    public WelcomeCore(YBVWelcome plugin, MessageManager messageManager, Database database) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.db = database;
        this.messageService = new MessageService(messageManager);
    }



    public void loadPlayerData(Player player) {
        async(() -> {
            try {
                String join = db.getMessage(player.getUniqueId(), Database.MessageType.WELCOME);
                String quit = db.getMessage(player.getUniqueId(), Database.MessageType.QUIT);
                playerCache.put(player.getUniqueId(), new PlayerCache(join, quit));
            } catch (SQLException e) {
                plugin.getLogger().warning("Ошибка загрузки данных для " + player.getName());
            }
        });
    }

    public void loadAndJoin(Player player, boolean isFirstJoin) {
        async(() -> {
            try {
                String join = db.getMessage(player.getUniqueId(), Database.MessageType.WELCOME);
                String quit = db.getMessage(player.getUniqueId(), Database.MessageType.QUIT);
                playerCache.put(player.getUniqueId(), new PlayerCache(join, quit));

                sync(() -> {
                    if (isFirstJoin) {
                        handlePlayerFirstJoin(player);
                    } else {
                        handlePlayerJoin(player);
                    }
                });
            } catch (SQLException e) {
                sync(() -> handlePlayerJoin(player));
            }
        });
    }

    public void handlePlayerJoin(Player player) {
        PlayerCache data = playerCache.get(player.getUniqueId());
        String raw = messageService.resolveJoinMessage(player, data);
        broadcast(raw, player);
    }

    public void handlePlayerQuit(Player player) {
        PlayerCache data = playerCache.get(player.getUniqueId());
        String raw = messageService.resolveQuitMessage(player, data);

        broadcast(raw, player);
        playerCache.remove(player.getUniqueId());
    }

    public void handlePlayerFirstJoin(Player player) {
        String rawMessage = messageManager.getFirstJoin();
        broadcast(rawMessage, player);
    }

    public void setPlayerWelcomeMessage(Player player, String message) {
        setPlayerMessage(player, message, Database.MessageType.WELCOME);
    }

    public void setPlayerQuitMessage(Player player, String message) {
        setPlayerMessage(player, message, Database.MessageType.QUIT);
    }

    private void setPlayerMessage(Player player, String message, Database.MessageType type) {
        if (checkLengthMessage(message) > plugin.getConfig().getInt("messages.allowed-symbols")) {
            MessageUtils.sendMessage(player, messageManager.getToManySymbols());
            return;
        }

        async(() -> {
            try {
                db.setMessage(player.getUniqueId(), player.getName(), message, type);
                loadPlayerData(player);
                sync(() -> MessageUtils.sendMessage(player, messageManager.getSetSuccess()));
            } catch (SQLException e) {
                sync(() -> MessageUtils.sendMessage(player, messageManager.getErrorDatabase()));
            }
        });
    }

    public void clearPlayerJoinMessage(Player player) {
        clearPlayerMessage(player, Database.MessageType.WELCOME);
    }

    public void clearPlayerQuitMessage(Player player) {
        clearPlayerMessage(player, Database.MessageType.QUIT);
    }

    private void clearPlayerMessage(Player player, Database.MessageType type) {
        async(() -> {
            try {
                db.deleteMessage(player.getUniqueId(), type);
                loadPlayerData(player);
                sync(() -> MessageUtils.sendMessage(player, messageManager.getClearSuccess()));
            } catch (SQLException e) {
                sync(() -> MessageUtils.sendMessage(player, messageManager.getErrorDatabase()));
            }
        });
    }

    private void broadcast(String raw, Player player) {
        if (raw == null || raw.equalsIgnoreCase("none")) return;

        String formatted = raw.replace("{player}", player.getName());
        String colored = MessageUtils.colorize(formatted);

        Bukkit.broadcastMessage(colored);
    }

    private int checkLengthMessage(String message) {
        if (message == null) return 0;
        return MessageUtils.stripColors(message).replace(" ", "").length();
    }

    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void close() {
        if (db != null) db.close();
    }

    public void reload() {
        playerCache.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            async(() -> {
                try {
                    String join = db.getMessage(player.getUniqueId(), Database.MessageType.WELCOME);
                    String quit = db.getMessage(player.getUniqueId(), Database.MessageType.QUIT);
                    playerCache.put(player.getUniqueId(), new PlayerCache(join, quit));
                } catch (SQLException ignored) {}
            });
        }
    }
}