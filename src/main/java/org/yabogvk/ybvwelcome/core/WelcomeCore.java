package org.yabogvk.ybvwelcome.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.core.db.Database;
import org.yabogvk.ybvwelcome.core.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WelcomeCore {
    private final MessageManager messageManager;
    private final YBVWelcome plugin;
    private Database db;

    private final Map<UUID, PlayerCache> playerCache = new HashMap<>();

    public WelcomeCore(YBVWelcome plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        try {
            DatabaseProvider.init(plugin);
            this.db = DatabaseProvider.database;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private record PlayerCache(String joinMsg, String quitMsg) {}


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
        String finalMsg = null;
        UUID uuid = player.getUniqueId();
        PlayerCache data = playerCache.get(uuid);

        if (data != null && data.joinMsg() != null && !data.joinMsg().isEmpty()) {
            String format = messageManager.getFormatJoinCustom();
            finalMsg = format.replace("{message}", data.joinMsg());
        }

        if (finalMsg == null) {
            String groupContent = messageManager.getGroupMessage(player, "join");
            if (groupContent != null) {
                finalMsg = groupContent; // Просто строка из конфига группы
            }
        }

        if (finalMsg == null) {
            String content = messageManager.getJoinDefault();
            String format = messageManager.getFormatJoinDefault();
            finalMsg = format.replace("{message}", content);
        }

        if (finalMsg == null || finalMsg.equalsIgnoreCase("none")) return;

        finalMsg = finalMsg.replace("{player}", player.getName());

        broadcast(finalMsg);
    }

    public void handlePlayerQuit(Player player) {
        String finalMsg = null;
        UUID uuid = player.getUniqueId();
        PlayerCache data = playerCache.get(uuid);

        if (data != null && data.quitMsg() != null && !data.quitMsg().isEmpty()) {
            String format = messageManager.getFormatQuitCustom();
            finalMsg = format.replace("{message}", data.quitMsg());
        }

        if (finalMsg == null) {
            String groupContent = messageManager.getGroupMessage(player, "quit");
            if (groupContent != null) {
                finalMsg = groupContent;
            }
        }

        if (finalMsg == null) {
            String content = messageManager.getQuitDefault();
            String format = messageManager.getFormatQuitDefault();
            finalMsg = format.replace("{message}", content);
        }

        playerCache.remove(uuid);

        if (finalMsg == null || finalMsg.equalsIgnoreCase("none")) return;

        finalMsg = finalMsg.replace("{player}", player.getName());

        broadcast(finalMsg);
    }

    public void handlePlayerFirstJoin(Player player) {
        String format = messageManager.getFirstJoin();
        String finalMsg = format.replace("{player}", player.getName());
        broadcast(finalMsg);
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

    public void broadcast(String rawMessage) {
        String colored = MessageUtils.colorize(rawMessage);
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