package org.yabogvk.ybvwelcome.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StorageService {
    private final YBVWelcome plugin;
    private final Database database;
    private final PlayerMessageCache cache;
    private final AsyncExecutor asyncExecutor;
    private final Map<UUID, Long> cacheTokens = new ConcurrentHashMap<>();

    public StorageService(YBVWelcome plugin, Database database,
                          PlayerMessageCache cache, AsyncExecutor asyncExecutor) {
        this.plugin = plugin;
        this.database = database;
        this.cache = cache;
        this.asyncExecutor = asyncExecutor;
    }

    public void loadPlayerData(Player player) {
        getOrLoadMessages(player);
    }

    public CompletableFuture<PlayerMessages> getOrLoadMessages(Player player) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        PlayerMessages cached = cache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        long token = nextToken(playerId);

        return asyncExecutor.supplyIo(() -> loadMessages(playerId, playerName))
                .thenApply(messages -> {
                    if (isCurrentToken(playerId, token)) {
                        cache.put(playerId, messages);
                    }
                    return messages;
                });
    }

    public CompletableFuture<Boolean> saveMessage(Player player, String message, Database.MessageType type) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        return asyncExecutor.supplyIo(() -> {
                    try {
                        return database.setMessage(playerId, playerName, message, type);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                })
                .thenApply(saved -> {
                    if (saved) {
                        cache.updateMessage(playerId, message, type);
                    }
                    return saved;
                });
    }

    public CompletableFuture<Boolean> clearMessage(Player player, Database.MessageType type) {
        UUID playerId = player.getUniqueId();
        PlayerMessages cached = cache.get(playerId);
        if (cached != null && !hasCachedMessage(cached, type)) {
            return CompletableFuture.completedFuture(false);
        }

        return asyncExecutor.supplyIo(() -> {
            try {
                if (cached == null && !database.hasMessage(playerId, type)) {
                    return false;
                }
                return database.deleteMessage(playerId, type);
            } catch (RuntimeException e) {
                throw e;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).thenApply(cleared -> {
            if (cleared) {
                cache.clearMessage(playerId, type);
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
        cacheTokens.clear();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            loadPlayerData(onlinePlayer);
        }
    }

    public void unloadPlayerData(UUID uuid) {
        nextToken(uuid);
        cache.remove(uuid);
    }

    public void close() {
        database.close();
    }

    private PlayerMessages loadMessages(UUID uuid, String playerName) {
        try {
            return database.getMessages(uuid);
        } catch (SQLException e) {
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

    private long nextToken(UUID uuid) {
        return cacheTokens.merge(uuid, 1L, Long::sum);
    }

    private boolean isCurrentToken(UUID uuid, long token) {
        return token == cacheTokens.getOrDefault(uuid, 0L);
    }
}
