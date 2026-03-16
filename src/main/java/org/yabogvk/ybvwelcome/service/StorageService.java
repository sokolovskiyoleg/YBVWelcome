package org.yabogvk.ybvwelcome.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;
import org.yabogvk.ybvwelcome.repository.MessageRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public class StorageService {
    private final YBVWelcome plugin;
    private final MessageRepository repository;
    private final PlayerMessageCache cache;
    private final AsyncExecutor asyncExecutor;

    public StorageService(YBVWelcome plugin, MessageRepository repository,
                          PlayerMessageCache cache, AsyncExecutor asyncExecutor) {
        this.plugin = plugin;
        this.repository = repository;
        this.cache = cache;
        this.asyncExecutor = asyncExecutor;
    }

    public void loadPlayerData(Player player) {
        getOrLoadMessages(player);
    }

    public CompletableFuture<PlayerMessages> getOrLoadMessages(Player player) {
        PlayerMessages cached = cache.get(player.getUniqueId());
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return asyncExecutor.supplyIo(() -> loadMessages(player.getUniqueId(), player.getName()))
                .thenApply(messages -> {
                    cache.put(player.getUniqueId(), messages);
                    return messages;
                });
    }

    public CompletableFuture<Boolean> saveMessage(Player player, String message, Database.MessageType type) {
        return asyncExecutor.supplyIo(() -> {
                    try {
                        return repository.saveMessage(player.getUniqueId(), player.getName(), message, type);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                })
                .thenApply(saved -> {
                    if (saved) {
                        cache.updateMessage(player.getUniqueId(), message, type);
                    }
                    return saved;
                });
    }

    public CompletableFuture<Boolean> clearMessage(Player player, Database.MessageType type) {
        PlayerMessages cached = cache.get(player.getUniqueId());
        if (cached != null && !hasCachedMessage(cached, type)) {
            return CompletableFuture.completedFuture(false);
        }

        return asyncExecutor.supplyIo(() -> {
            try {
                if (cached == null && !repository.hasMessage(player.getUniqueId(), type)) {
                    return false;
                }
                return repository.clearMessage(player.getUniqueId(), type);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }).thenApply(cleared -> {
            if (cleared) {
                cache.clearMessage(player.getUniqueId(), type);
            }
            return cleared;
        });
    }

    @Nullable
    public PlayerMessages getCachedMessages(UUID uuid) {
        return cache.get(uuid);
    }

    public void reload() {
        cache.clear();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            loadPlayerData(onlinePlayer);
        }
    }

    public void close() {
        repository.close();
    }

    private PlayerMessages loadMessages(UUID uuid, String playerName) {
        try {
            return repository.loadMessages(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка загрузки данных для " + playerName, e);
            return new PlayerMessages(null, null);
        }
    }

    private boolean hasCachedMessage(PlayerMessages messages, Database.MessageType type) {
        if (type == Database.MessageType.WELCOME) {
            return messages.joinMessage() != null && !messages.joinMessage().isEmpty();
        }

        return messages.quitMessage() != null && !messages.quitMessage().isEmpty();
    }
}
