package org.yabogvk.ybvwelcome.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.util.Locale;
import java.util.logging.Level;

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

    @Getter
    private SerializerType serializerType;
    @Getter
    private DatabaseType databaseType;
    @Getter
    private int allowedSymbols;
    @Getter
    private boolean joinDisableVanilla;
    @Getter
    private boolean joinEnabled;
    @Getter
    private boolean firstJoinEnabled;
    @Getter
    private boolean quitDisableVanilla;
    @Getter
    private boolean quitEnabled;
    @Getter
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
        if (allowedSymbols < 1) {
            plugin.getLogger().log(Level.WARNING, "Invalid config: messages.allowed-symbols={0}. Using fallback 100.", allowedSymbols);
            allowedSymbols = 100;
        }
        joinDisableVanilla = config.getBoolean("messages.join.disable-vanilla", true);
        joinEnabled = config.getBoolean("messages.join.enabled", true);
        firstJoinEnabled = config.getBoolean("messages.join.firstjoin-enabled", true);
        quitDisableVanilla = config.getBoolean("messages.quit.disable-vanilla", true);
        quitEnabled = config.getBoolean("messages.quit.enabled", true);
        commandCooldown = config.getInt("commands.cooldown", 5);
        if (commandCooldown < 0) {
            plugin.getLogger().log(Level.WARNING, "Invalid config: commands.cooldown={0}. Using fallback 0.", commandCooldown);
            commandCooldown = 0;
        }
    }

}
