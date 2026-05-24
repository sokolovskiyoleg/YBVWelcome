package org.yabogvk.ybvwelcome.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private static final String VERSION_KEY = "config-version";
    private static final int CONFIG_VERSION = 2;
    private static final int MESSAGES_VERSION = 2;

    private final YBVWelcome plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(YBVWelcome plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        mainConfig = loadConfiguration("config.yml", CONFIG_VERSION);
        messagesConfig = loadConfiguration("messages.yml", MESSAGES_VERSION);
    }

    private FileConfiguration loadConfiguration(String fileName, int targetVersion) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        try (InputStreamReader defaultConfigStream = new InputStreamReader(plugin.getResource(fileName), StandardCharsets.UTF_8)) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
            config.setDefaults(defaultConfig);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load default configuration for " + fileName);
        }

        config.options().copyDefaults(true);
        migrateConfiguration(fileName, config, targetVersion);
        saveConfiguration(config, file);

        return config;
    }

    private void migrateConfiguration(String fileName, FileConfiguration config, int targetVersion) {
        int currentVersion = config.getInt(VERSION_KEY, 0);
        if (currentVersion >= targetVersion) {
            return;
        }

        config.set(VERSION_KEY, targetVersion);
        plugin.getLogger().info("Migrated " + fileName + " from version " + currentVersion + " to " + targetVersion + ".");
    }

    private void saveConfiguration(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save configuration file: " + file.getName());
        }
    }

    public void reload() {
        loadConfigs();
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
}
