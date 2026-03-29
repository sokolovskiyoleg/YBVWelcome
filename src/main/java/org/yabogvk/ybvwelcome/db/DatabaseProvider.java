package org.yabogvk.ybvwelcome.db;

import org.bukkit.configuration.ConfigurationSection;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.db.impl.MySQLDatabase;
import org.yabogvk.ybvwelcome.db.impl.SQLiteDatabase;

import java.io.File;
import java.sql.SQLException;

public final class DatabaseProvider {
    private DatabaseProvider() {
    }

    public static Database create(YBVWelcome plugin) throws SQLException {
        if (plugin.getSettings().getDatabaseType() == Settings.DatabaseType.MYSQL) {
            ConfigurationSection config = plugin.getConfigManager().getMainConfig().getConfigurationSection("database.mysql");
            return new MySQLDatabase(plugin.getLogger(), config);
        }

        return new SQLiteDatabase(plugin.getLogger(), new File(plugin.getDataFolder(), "database.db"));
    }
}
