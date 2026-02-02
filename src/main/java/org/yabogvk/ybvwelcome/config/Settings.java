package org.yabogvk.ybvwelcome.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.yabogvk.ybvwelcome.YBVWelcome;

public class Settings {

    private final YBVWelcome plugin;

    // General
    public String serializer;

    // Database
    public String databaseType;

    // Messages
    public int allowedSymbols;
    public boolean joinDisableVanilla;
    public boolean joinEnabled;
    public boolean firstJoinEnabled;
    public boolean quitDisableVanilla;
    public boolean quitEnabled;

    // Commands
    public int commandCooldown;

    public Settings(YBVWelcome plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfigManager().getMainConfig();

        // General
        serializer = config.getString("serializer", "LEGACY");

        // Database
        databaseType = config.getString("database.type", "sqlite");

        // Messages
        allowedSymbols = config.getInt("messages.allowed-symbols", 100);
        joinDisableVanilla = config.getBoolean("messages.join.disable-vanilla", true);
        joinEnabled = config.getBoolean("messages.join.enabled", true);
        firstJoinEnabled = config.getBoolean("messages.join.firstjoin-enabled", true);
        quitDisableVanilla = config.getBoolean("messages.quit.disable-vanilla", true);
        quitEnabled = config.getBoolean("messages.quit.enabled", true);

        // Commands
        commandCooldown = config.getInt("commands.cooldown", 5);
    }
}
