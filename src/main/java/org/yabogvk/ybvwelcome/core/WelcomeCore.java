package org.yabogvk.ybvwelcome.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.core.db.Database;
import org.yabogvk.ybvwelcome.core.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.core.db.impl.SQLiteDatabase;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.io.File;
import java.sql.SQLException;

public class WelcomeCore {
    private final MessageManager messageManager;
    private final YBVWelcome plugin;
    private Database db;

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

    private void handlePlayerMessage(Player player, Database.MessageType type, String defaultMessage, String preset) {
        async(() -> {
            try {
                String message = db.getMessage(player.getUniqueId(), type);
                sync(() -> {
                    String finalContent = (message != null && !message.isEmpty()) ? message : defaultMessage;
                    String formatted = preset + " " + finalContent;
                    sendMessageToAll(formatted);
                });
            } catch (Exception e) {
                sync(() -> {
                    sendMessageToAll(preset + " " + defaultMessage);
                    plugin.getLogger().warning("Ошибка при получении сообщения для " + player.getName() + ": " + e.getMessage());
                });
            }
        });
    }

    public void handlePlayerJoin(Player player) {
        String preset = messageManager.getJoinPreset(player);
        handlePlayerMessage(player, Database.MessageType.WELCOME, messageManager.getJoinMessage(player), preset);
    }

    public void handlePlayerFirstJoin(Player player) {
        String msg = messageManager.getPresetFirstJoin(player) + " " + messageManager.getFirstJoin();
        sendMessageToAll(msg);
    }

    public void handlePlayerQuit(Player player) {
        String preset = messageManager.getQuitPreset(player);
        handlePlayerMessage(player, Database.MessageType.QUIT, messageManager.getQuitMessage(player), preset);
    }

    public void setPlayerMessage(Player player, String message, Database.MessageType type) {
        if (player == null || message == null || message.trim().isEmpty()) return;

        if (checkLengthMessage(message) > plugin.getConfig().getInt("messages.allowed-symbols")) {
            MessageUtils.sendMessage(player, messageManager.getToManySymbols());
            return;
        }

        async(() -> {
            try {
                boolean success = db.setMessage(player.getUniqueId(), player.getName(), message, type);
                sync(() -> {
                    if (success) {
                        MessageUtils.sendMessage(player, messageManager.getSetSuccess());
                    } else {
                        MessageUtils.sendMessage(player, messageManager.getError());
                    }
                });
            } catch (SQLException e) {
                sync(() -> {
                    MessageUtils.sendMessage(player, messageManager.getErrorDatabase());
                    plugin.getLogger().severe("Database error: " + e.getMessage());
                });
            }
        });
    }

    private int checkLengthMessage(String message) {
        if (message == null) return 0;
        String cleanMessage = MessageUtils.stripColors(message).replace(" ", "");
        return cleanMessage.length();
    }

    public void clearPlayerMessage(Player player, Database.MessageType type) {
        if (player == null) return;

        async(() -> {
            try {
                if (!db.hasMessage(player.getUniqueId(), type)) {
                    sync(() -> MessageUtils.sendMessage(player, messageManager.getClearNoMessage()));
                    return;
                }

                boolean success = db.deleteMessage(player.getUniqueId(), type);
                sync(() -> {
                    if (success) {
                        MessageUtils.sendMessage(player, messageManager.getClearSuccess());
                    } else {
                        MessageUtils.sendMessage(player, messageManager.getClearNoMessage());
                    }
                });
            } catch (SQLException e) {
                sync(() -> MessageUtils.sendMessage(player, messageManager.getErrorDatabase()));
            }
        });
    }

    public void setPlayerWelcomeMessage(Player player, String message) { setPlayerMessage(player, message, Database.MessageType.WELCOME); }
    public void setPlayerQuitMessage(Player player, String message) { setPlayerMessage(player, message, Database.MessageType.QUIT); }
    public void clearPlayerJoinMessage(Player player) { clearPlayerMessage(player, Database.MessageType.WELCOME); }
    public void clearPlayerQuitMessage(Player player) { clearPlayerMessage(player, Database.MessageType.QUIT); }

    private void sendMessageToAll(String rawMessage) {
        async(() -> {
            String coloredMessage = MessageUtils.colorize(rawMessage);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(coloredMessage);
            }
        });
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
}