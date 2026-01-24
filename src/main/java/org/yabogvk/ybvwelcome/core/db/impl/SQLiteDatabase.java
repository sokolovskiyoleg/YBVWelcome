package org.yabogvk.ybvwelcome.core.db.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.core.db.Database;
import org.yabogvk.ybvwelcome.utils.SecurityUtils;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteDatabase implements Database {
    private final HikariDataSource dataSource;
    private static final Logger logger = Logger.getLogger("YBVWelcome-DB");

    public SQLiteDatabase(File file) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1);
        config.setPoolName("YBVWelcome-Pool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);

        setupDatabase();
        initializeTable();
    }

    private void setupDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode = DELETE");
            stmt.execute("PRAGMA synchronous = NORMAL");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not set up SQLite pragmas", e);
        }
    }

    private void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_messages (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "join_message TEXT, " +
                "quit_message TEXT, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        executeRawUpdate(sql);
    }

    @Override
    @Nullable
    public String getMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException {
        String sql = "SELECT " + type.getColumn() + " FROM player_messages WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String message = rs.getString(1);
                    return (message != null && !message.isEmpty())
                            ? SecurityUtils.sanitizeMessageContent(message) : null;
                }
            }
        }
        return null;
    }

    @Override
    public boolean setMessage(@NotNull UUID uuid, @NotNull String playerName,
                              @NotNull String message, @NotNull MessageType type) throws SQLException {

        String sql = "INSERT INTO player_messages (uuid, player_name, " + type.getColumn() + ") " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " +
                type.getColumn() + " = excluded." + type.getColumn() + ", " +
                "player_name = excluded.player_name, " +
                "last_updated = CURRENT_TIMESTAMP";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, SecurityUtils.sanitizePlayerName(playerName));
            stmt.setString(3, SecurityUtils.sanitizeMessageContent(message));

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean hasMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException {
        String sql = "SELECT 1 FROM player_messages WHERE uuid = ? AND " +
                type.getColumn() + " IS NOT NULL AND " + type.getColumn() + " != '' LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean deleteMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException {
        String sql = "UPDATE player_messages SET " + type.getColumn() +
                " = NULL, last_updated = CURRENT_TIMESTAMP WHERE uuid = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            return stmt.executeUpdate() > 0;
        }
    }

    private void executeRawUpdate(String sql) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing raw SQL: " + sql, e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }
}