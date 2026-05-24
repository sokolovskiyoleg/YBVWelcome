package org.yabogvk.ybvwelcome.service;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageServiceTest {

    private FakeDatabase database;
    private PlayerMessageCache cache;
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        database = new FakeDatabase();
        cache = new PlayerMessageCache();
        storageService = new StorageService(null, database, cache, new ImmediateAsyncExecutor());
    }

    @Test
    void getOrLoadMessagesReturnsCachedValueWithoutRepositoryCall() {
        UUID playerId = UUID.randomUUID();
        PlayerMessages cachedMessages = new PlayerMessages("hello", null);
        cache.put(playerId, cachedMessages);

        Player player = player(playerId, "PlayerOne");
        PlayerMessages result = storageService.getOrLoadMessages(player).join();

        assertEquals(cachedMessages, result);
        assertEquals(0, database.loadCalls);
    }

    @Test
    void saveMessageUsesPlayerSnapshotAndUpdatesCache() {
        UUID playerId = UUID.randomUUID();
        database.saveResult = true;

        boolean saved = storageService.saveMessage(player(playerId, "PlayerOne"), "hello", Database.MessageType.WELCOME).join();

        assertTrue(saved);
        assertEquals(new PlayerMessages("hello", null), cache.get(playerId));
        assertEquals(playerId, database.lastSavedUuid);
        assertEquals("PlayerOne", database.lastSavedPlayerName);
    }

    @Test
    void clearMessageShortCircuitsWhenCacheAlreadyShowsNoValue() {
        UUID playerId = UUID.randomUUID();
        cache.put(playerId, new PlayerMessages(null, "bye"));

        boolean cleared = storageService.clearMessage(player(playerId, "PlayerOne"), Database.MessageType.WELCOME).join();

        assertFalse(cleared);
        assertEquals(0, database.hasMessageCalls);
        assertEquals(0, database.clearCalls);
    }

    @Test
    void unloadPlayerDataPreventsStaleAsyncLoadFromRecachingPlayer() {
        ControlledAsyncExecutor executor = new ControlledAsyncExecutor();
        storageService = new StorageService(null, database, cache, executor);
        UUID playerId = UUID.randomUUID();
        PlayerMessages loadedMessages = new PlayerMessages("hello", null);
        database.loadedMessages = loadedMessages;

        CompletableFuture<PlayerMessages> future = storageService.getOrLoadMessages(player(playerId, "PlayerOne"));
        storageService.unloadPlayerData(playerId);
        executor.completePending();

        assertEquals(loadedMessages, future.join());
        org.junit.jupiter.api.Assertions.assertNull(cache.get(playerId));
    }

    private Player player(UUID uuid, String name) {
        InvocationHandler handler = (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getUniqueId" -> uuid;
                case "getName" -> name;
                case "isOnline" -> true;
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "TestPlayer{" + name + '}';
                default -> throw new UnsupportedOperationException(method.getName());
            };
        };

        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                handler
        );
    }

    private static final class ImmediateAsyncExecutor extends AsyncExecutor {
        private ImmediateAsyncExecutor() {
            super(null);
        }

        @Override
        public <T> CompletableFuture<T> supplyIo(java.util.function.Supplier<T> supplier) {
            return CompletableFuture.completedFuture(supplier.get());
        }
    }

    private static final class ControlledAsyncExecutor extends AsyncExecutor {
        private Supplier<?> pendingSupplier;
        private CompletableFuture<Object> pendingFuture;

        private ControlledAsyncExecutor() {
            super(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> CompletableFuture<T> supplyIo(Supplier<T> supplier) {
            pendingSupplier = supplier;
            pendingFuture = new CompletableFuture<>();
            return (CompletableFuture<T>) pendingFuture;
        }

        private void completePending() {
            pendingFuture.complete(pendingSupplier.get());
        }
    }

    private static final class FakeDatabase implements Database {
        private int loadCalls;
        private int hasMessageCalls;
        private int clearCalls;
        private boolean saveResult;
        private UUID lastSavedUuid;
        private String lastSavedPlayerName;
        private PlayerMessages loadedMessages = new PlayerMessages(null, null);

        @Override
        public String getMessage(UUID uuid, MessageType type) {
            return null;
        }

        @Override
        public PlayerMessages getMessages(UUID uuid) {
            loadCalls++;
            return loadedMessages;
        }

        @Override
        public boolean setMessage(UUID uuid, String playerName, String message, MessageType type) {
            lastSavedUuid = uuid;
            lastSavedPlayerName = playerName;
            return saveResult;
        }

        @Override
        public boolean hasMessage(UUID uuid, MessageType type) {
            hasMessageCalls++;
            return false;
        }

        @Override
        public boolean deleteMessage(UUID uuid, MessageType type) {
            clearCalls++;
            return false;
        }

        @Override
        public void close() {
        }
    }
}
