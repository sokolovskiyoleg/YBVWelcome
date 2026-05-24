package org.yabogvk.ybvwelcome.service;

import org.junit.jupiter.api.Test;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerMessageCacheTest {

    private final PlayerMessageCache cache = new PlayerMessageCache();

    @Test
    void putRemovesEntryWhenMessagesAreEmpty() {
        UUID playerId = UUID.randomUUID();

        cache.put(playerId, new PlayerMessages(null, null));

        assertNull(cache.get(playerId));
    }

    @Test
    void updateMessagePreservesOtherMessageType() {
        UUID playerId = UUID.randomUUID();
        cache.put(playerId, new PlayerMessages(null, "bye"));

        cache.updateMessage(playerId, "hello", Database.MessageType.WELCOME);

        assertEquals(new PlayerMessages("hello", "bye"), cache.get(playerId));
    }

    @Test
    void clearMessageRemovesEntryWhenNothingLeft() {
        UUID playerId = UUID.randomUUID();
        cache.put(playerId, new PlayerMessages("hello", null));

        cache.clearMessage(playerId, Database.MessageType.WELCOME);

        assertNull(cache.get(playerId));
    }
}
