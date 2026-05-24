package org.yabogvk.ybvwelcome.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.function.IntSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WelcomeService {
    private final Logger logger;
    private final IntSupplier allowedSymbolsSupplier;
    private final StorageService storageService;
    private final MessageService messageService;
    private final AsyncExecutor asyncExecutor;
    private final MessageManager messageManager;
    private final MessageUtils messageUtils;

    public WelcomeService(@NotNull Logger logger, @NotNull IntSupplier allowedSymbolsSupplier, StorageService storageService,
                          MessageService messageService, AsyncExecutor asyncExecutor,
                          MessageManager messageManager, MessageUtils messageUtils) {
        this.logger = logger;
        this.allowedSymbolsSupplier = allowedSymbolsSupplier;
        this.storageService = storageService;
        this.messageService = messageService;
        this.asyncExecutor = asyncExecutor;
        this.messageManager = messageManager;
        this.messageUtils = messageUtils;
    }

    public void handleJoin(Player player, boolean firstJoin) {
        if (firstJoin) {
            storageService.getOrLoadMessages(player);
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
    }

    public void close() {
        storageService.close();
    }

    private void setPlayerMessage(Player player, String message, Database.MessageType type) {
        if (messageLength(message) > allowedSymbolsSupplier.getAsInt()) {
            messageService.send(player, messageManager.getToManySymbols());
            return;
        }

        storageService.saveMessage(player, message, type)
                .whenComplete((saved, throwable) -> asyncExecutor.runSync(() -> {
                    if (throwable != null || !Boolean.TRUE.equals(saved)) {
                        if (throwable != null) {
                            logger.log(Level.WARNING,
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
                        logger.log(Level.WARNING,
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
        return message == null ? 0 : messageUtils.stripColors(message).replace(" ", "").length();
    }
}
