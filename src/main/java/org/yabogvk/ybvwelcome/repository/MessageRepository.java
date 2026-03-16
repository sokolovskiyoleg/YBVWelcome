package org.yabogvk.ybvwelcome.repository;

import org.jetbrains.annotations.NotNull;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

import java.sql.SQLException;
import java.util.UUID;

public interface MessageRepository {
    @NotNull
    PlayerMessages loadMessages(@NotNull UUID uuid) throws SQLException;

    boolean saveMessage(@NotNull UUID uuid, @NotNull String playerName,
                        @NotNull String message, @NotNull Database.MessageType type) throws SQLException;

    boolean hasMessage(@NotNull UUID uuid, @NotNull Database.MessageType type) throws SQLException;

    boolean clearMessage(@NotNull UUID uuid, @NotNull Database.MessageType type) throws SQLException;

    void close();
}
