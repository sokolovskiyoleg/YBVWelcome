package org.yabogvk.ybvwelcome.db.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.utils.SecurityUtils;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLDatabase implements Database {
    private final HikariDataSource dataSource;
    private static final Logger logger = Logger.getLogger("YBVWelcome-DB");

    public MySQLDatabase(ConfigurationSection config) {
        HikariConfig hikariConfig = new HikariConfig();

        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String dbName = config.getString("database", "minecraft");

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(config.getString("username", "root"));
        hikariConfig.setPassword(config.getString("password", ""));

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(hikariConfig);
        initializeTable();
    }

    private void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_messages (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "join_message TEXT, " +
                "quit_message TEXT, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize MySQL table", e);
        }
    }

    @Override
    @Nullable
    public String getMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException {
        String sql = "SELECT " + type.getColumn() + " FROM player_messages WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String msg = rs.getString(1);
                    return (msg != null && !msg.isEmpty()) ? SecurityUtils.sanitizeMessageContent(msg) : null;
                }
            }
        }
        return null;
    }

    @Override
    public boolean setMessage(@NotNull UUID uuid, @NotNull String playerName, @NotNull String message, @NotNull MessageType type) throws SQLException {
        String sql = "INSERT INTO player_messages (uuid, player_name, " + type.getColumn() + ") " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                type.getColumn() + " = VALUES(" + type.getColumn() + "), " +
                "player_name = VALUES(player_name)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, SecurityUtils.sanitizePlayerName(playerName));
            stmt.setString(3, SecurityUtils.sanitizeMessageContent(message));
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean hasMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException {
        String sql = "SELECT 1 FROM player_messages WHERE uuid = ? AND " + type.getColumn() + " IS NOT NULL LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public boolean deleteMessage(@NotNull UUID uuid, @NotNull MessageType type) throws SQLException {
        String sql = "UPDATE player_messages SET " + type.getColumn() + " = NULL WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public void close() {
        if (dataSource != null) dataSource.close();
    }
}