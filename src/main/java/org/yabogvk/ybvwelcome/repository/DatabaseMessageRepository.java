package org.yabogvk.ybvwelcome.repository;

import org.jetbrains.annotations.NotNull;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.sql.SQLException;
import java.util.UUID;

public class DatabaseMessageRepository implements MessageRepository {
    private final Database database;

    public DatabaseMessageRepository(Database database) {
        this.database = database;
    }

    @Override
    public @NotNull PlayerMessages loadMessages(@NotNull UUID uuid) throws SQLException {
        return database.getMessages(uuid);
    }

    @Override
    public boolean saveMessage(@NotNull UUID uuid, @NotNull String playerName,
                               @NotNull String message, @NotNull Database.MessageType type) throws SQLException {
        return database.setMessage(uuid, playerName, message, type);
    }

    @Override
    public boolean hasMessage(@NotNull UUID uuid, @NotNull Database.MessageType type) throws SQLException {
        return database.hasMessage(uuid, type);
    }

    @Override
    public boolean clearMessage(@NotNull UUID uuid, @NotNull Database.MessageType type) throws SQLException {
        return database.deleteMessage(uuid, type);
    }

    @Override
    public void close() {
        database.close();
    }
}
