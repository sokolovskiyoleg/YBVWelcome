package org.yabogvk.ybvwelcome.service;

import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.logging.Level;

public class WelcomeService {
    private final YBVWelcome plugin;
    private final StorageService storageService;
    private final MessageService messageService;
    private final AsyncExecutor asyncExecutor;
    private final MessageManager messageManager;
    private final MessageUtils messageUtils;

    public WelcomeService(YBVWelcome plugin, StorageService storageService,
                          MessageService messageService, AsyncExecutor asyncExecutor,
                          MessageManager messageManager, MessageUtils messageUtils) {
        this.plugin = plugin;
        this.storageService = storageService;
        this.messageService = messageService;
        this.asyncExecutor = asyncExecutor;
        this.messageManager = messageManager;
        this.messageUtils = messageUtils;
    }

    public void loadPlayerData(Player player) {
        storageService.loadPlayerData(player);
    }

    public void handleJoin(Player player, boolean firstJoin) {
        if (firstJoin) {
            storageService.loadPlayerData(player);
            messageService.broadcastFirstJoin(player);
            return;
        }

        PlayerMessages cached = storageService.getCachedMessages(player.getUniqueId());
        if (cached != null) {
            messageService.broadcastJoin(player, cached);
            return;
        }

        storageService.getOrLoadMessages(player)
                .thenAccept(messages -> asyncExecutor.runSync(() -> {
                    if (player.isOnline()) {
                        messageService.broadcastJoin(player, messages);
                    }
                }));
    }

    public void handleQuit(Player player) {
        messageService.broadcastQuit(player, storageService.getCachedMessages(player.getUniqueId()));
        storageService.unloadPlayerData(player.getUniqueId());
    }

    public void handleFirstJoin(Player player) {
        storageService.loadPlayerData(player);
        messageService.broadcastFirstJoin(player);
    }

    public void setPlayerWelcomeMessage(Player player, String message) {
        setPlayerMessage(player, message, Database.MessageType.WELCOME);
    }

    public void setPlayerQuitMessage(Player player, String message) {
        setPlayerMessage(player, message, Database.MessageType.QUIT);
    }

    public void clearPlayerJoinMessage(Player player) {
        clearPlayerMessage(player, Database.MessageType.WELCOME);
    }

    public void clearPlayerQuitMessage(Player player) {
        clearPlayerMessage(player, Database.MessageType.QUIT);
    }

    public void reload() {
        storageService.reload();
        messageService.reload();
    }

    public void close() {
        storageService.close();
    }

    private void setPlayerMessage(Player player, String message, Database.MessageType type) {
        if (messageLength(message) > plugin.getSettings().getAllowedSymbols()) {
            messageService.send(player, messageManager.getToManySymbols());
            return;
        }

        storageService.saveMessage(player, message, type)
                .whenComplete((saved, throwable) -> asyncExecutor.runSync(() -> {
                    if (throwable != null || !Boolean.TRUE.equals(saved)) {
                        if (throwable != null) {
                            plugin.getLogger().log(Level.WARNING,
                                    "Ошибка сохранения сообщения для " + player.getName() + " (" + type + ")",
                                    throwable);
                        }
                        messageService.send(player, messageManager.getErrorDatabase());
                        return;
                    }

                    messageService.send(player, messageManager.getSetSuccess());
                }));
    }

    private void clearPlayerMessage(Player player, Database.MessageType type) {
        storageService.clearMessage(player, type)
                .whenComplete((cleared, throwable) -> asyncExecutor.runSync(() -> {
                    if (throwable != null) {
                        plugin.getLogger().log(Level.WARNING,
                                "Ошибка очистки сообщения для " + player.getName() + " (" + type + ")",
                                throwable);
                        messageService.send(player, messageManager.getErrorDatabase());
                        return;
                    }

                    if (Boolean.TRUE.equals(cleared)) {
                        messageService.send(player, messageManager.getClearSuccess());
                    } else {
                        messageService.send(player, messageManager.getClearNoMessage());
                    }
                }));
    }

    private int messageLength(String message) {
        if (message == null) {
            return 0;
        }

        return messageUtils.stripColors(message).replace(" ", "").length();
    }
}
