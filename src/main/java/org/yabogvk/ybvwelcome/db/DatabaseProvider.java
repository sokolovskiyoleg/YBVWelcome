package org.yabogvk.ybvwelcome.db;

import org.bukkit.configuration.ConfigurationSection;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.db.impl.MySQLDatabase;
import org.yabogvk.ybvwelcome.db.impl.SQLiteDatabase;

import java.io.File;
import java.sql.SQLException;

public class DatabaseProvider {
    public static Database database;

    public static void init(YBVWelcome plugin) throws SQLException {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("database");
        String type = (config != null) ? config.getString("type", "SQLITE").toUpperCase() : "SQLITE";

        if (type.equals("MYSQL")) {
            database = new MySQLDatabase(config.getConfigurationSection("mysql"));
        } else {
            database = new SQLiteDatabase(new File(plugin.getDataFolder(), "database.db"));
        }
    }
}