package org.yabogvk.ybvwelcome.core.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.UUID;

public interface Database {
    enum MessageType {
        WELCOME("join_message"),
        QUIT("quit_message");

        private final String column;

        MessageType(String column) {
            this.column = column;
        }

        public String getColumn() {
            return column;
        }
    }

    @Nullable
    String getMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException;

    boolean setMessage(@NotNull UUID uuid, @NotNull String playerName,
                       @NotNull String message, @NotNull MessageType type) throws SQLException;

    boolean hasMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException;

    boolean deleteMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException;

    void close();
}