package org.yabogvk.ybvwelcome.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final YBVWelcome plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(YBVWelcome plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        mainConfig = loadConfiguration("config.yml");
        messagesConfig = loadConfiguration("messages.yml");
    }

    private FileConfiguration loadConfiguration(String fileName) {
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
        saveConfiguration(config, file);

        return config;
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
