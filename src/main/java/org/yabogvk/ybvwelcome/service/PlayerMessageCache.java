package org.yabogvk.ybvwelcome.service;

import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMessageCache {
    private final Map<UUID, PlayerMessages> cache = new ConcurrentHashMap<>();

    @Nullable
    public PlayerMessages get(UUID uuid) {
        return cache.get(uuid);
    }

    public void put(UUID uuid, PlayerMessages messages) {
        if (messages.hasNoMessages()) {
            cache.remove(uuid);
            return;
        }

        cache.put(uuid, messages);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }

    public void updateMessage(UUID uuid, String message, Database.MessageType type) {
        PlayerMessages current = cache.get(uuid);
        PlayerMessages updated;

        if (type == Database.MessageType.WELCOME) {
            updated = new PlayerMessages(message, current != null ? current.quitMessage() : null);
        } else {
            updated = new PlayerMessages(current != null ? current.joinMessage() : null, message);
        }

        put(uuid, updated);
    }

    public void clearMessage(UUID uuid, Database.MessageType type) {
        PlayerMessages current = cache.get(uuid);
        if (current == null) {
            return;
        }

        PlayerMessages updated;
        if (type == Database.MessageType.WELCOME) {
            updated = new PlayerMessages(null, current.quitMessage());
        } else {
            updated = new PlayerMessages(current.joinMessage(), null);
        }

        put(uuid, updated);
    }
}
