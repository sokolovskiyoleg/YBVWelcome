package org.yabogvk.ybvwelcome.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.yabogvk.ybvwelcome.YBVWelcome;

public class Settings {

    private final YBVWelcome plugin;

    public String serializer;

    public String databaseType;

    public int allowedSymbols;
    public boolean joinDisableVanilla;
    public boolean joinEnabled;
    public boolean firstJoinEnabled;
    public boolean quitDisableVanilla;
    public boolean quitEnabled;


    public Settings(YBVWelcome plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        serializer = config.getString("serializer", "LEGACY");

        databaseType = config.getString("database.type", "sqlite");

        allowedSymbols = config.getInt("messages.allowed-symbols", 100);
        joinDisableVanilla = config.getBoolean("messages.join.disable-vanilla", true);
        joinEnabled = config.getBoolean("messages.join.enabled", true);
        firstJoinEnabled = config.getBoolean("messages.join.firstjoin-enabled", true);
        quitDisableVanilla = config.getBoolean("messages.quit.disable-vanilla", true);
        quitEnabled = config.getBoolean("messages.quit.enabled", true);
    }
}
