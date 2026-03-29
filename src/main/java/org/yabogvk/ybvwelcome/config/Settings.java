package org.yabogvk.ybvwelcome.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.util.Locale;

public class Settings {
    public enum SerializerType {
        LEGACY,
        MINIMESSAGE;

        public static SerializerType fromConfig(String value) {
            if (value == null || value.isBlank()) {
                return LEGACY;
            }

            try {
                return SerializerType.valueOf(value.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ignored) {
                return LEGACY;
            }
        }
    }

    public enum DatabaseType {
        SQLITE,
        MYSQL;

        public static DatabaseType fromConfig(String value) {
            if (value == null || value.isBlank()) {
                return SQLITE;
            }

            try {
                return DatabaseType.valueOf(value.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ignored) {
                return SQLITE;
            }
        }
    }

    private final YBVWelcome plugin;

    private SerializerType serializerType;
    private DatabaseType databaseType;
    private int allowedSymbols;
    private boolean joinDisableVanilla;
    private boolean joinEnabled;
    private boolean firstJoinEnabled;
    private boolean quitDisableVanilla;
    private boolean quitEnabled;
    private int commandCooldown;

    public Settings(YBVWelcome plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfigManager().getMainConfig();

        serializerType = SerializerType.fromConfig(config.getString("serializer", "LEGACY"));
        databaseType = DatabaseType.fromConfig(config.getString("database.type", "sqlite"));
        allowedSymbols = config.getInt("messages.allowed-symbols", 100);
        joinDisableVanilla = config.getBoolean("messages.join.disable-vanilla", true);
        joinEnabled = config.getBoolean("messages.join.enabled", true);
        firstJoinEnabled = config.getBoolean("messages.join.firstjoin-enabled", true);
        quitDisableVanilla = config.getBoolean("messages.quit.disable-vanilla", true);
        quitEnabled = config.getBoolean("messages.quit.enabled", true);
        commandCooldown = config.getInt("commands.cooldown", 5);
    }

    public SerializerType getSerializerType() {
        return serializerType;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public int getAllowedSymbols() {
        return allowedSymbols;
    }

    public boolean isJoinDisableVanilla() {
        return joinDisableVanilla;
    }

    public boolean isJoinEnabled() {
        return joinEnabled;
    }

    public boolean isFirstJoinEnabled() {
        return firstJoinEnabled;
    }

    public boolean isQuitDisableVanilla() {
        return quitDisableVanilla;
    }

    public boolean isQuitEnabled() {
        return quitEnabled;
    }

    public int getCommandCooldown() {
        return commandCooldown;
    }
}
