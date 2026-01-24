package org.yabogvk.ybvwelcome;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;
import org.yabogvk.ybvwelcome.commands.WelcomeCommand;
import org.yabogvk.ybvwelcome.core.WelcomeCore;
import org.yabogvk.ybvwelcome.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.listener.PlayerJoinListener;
import org.yabogvk.ybvwelcome.listener.PlayerQuitListener;
import org.yabogvk.ybvwelcome.managers.MessageManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.logging.Level;

public final class YBVWelcome extends JavaPlugin {
    private static YBVWelcome instance;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private WelcomeCore core;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessagesConfig();
        instance = this;
        ColorizerProvider.init(getConfig());
        messageManager = new MessageManager(this);
        try {
            DatabaseProvider.init(this);
            this.messageManager = new MessageManager(this);
            this.core = new WelcomeCore(this, this.messageManager, DatabaseProvider.database);
            new WelcomeCommand();
            registerListener();

            getLogger().info("YBVWelcome enabled successfully!");

        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not initialize database! Disabling plugin...", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
        } else {
            getLogger().warning("Не найден PlaceholderAPI. Плейсхолдеры papi не будут работать");
        }
    }

    @Override
    public void onDisable() {
        if (core != null) core.close();
    }
    public static YBVWelcome getInstance() {
        return instance;
    }

    public static WelcomeCore getCore() {
        return instance.core;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
    }

    private void loadMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
            getLogger().info("Created default messages.yml");
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        try (InputStream defConfigStream = getResource("messages.yml")) {
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defConfigStream)
                );
                messagesConfig.setDefaults(defConfig);
                messagesConfig.options().copyDefaults(true);
                saveMessagesConfig();
            }
        } catch (Exception e) {
            getLogger().warning("Could not load default messages configuration: " + e.getMessage());
        }
    }

    public void reloadMessagesConfig() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save messages.yml!", e);
        }
    }

    public void reload() {
        reloadConfig();
        reloadMessagesConfig();
        if (messageManager != null) {
            messageManager.reload();
        }
        if (core != null) {
            core.reload();
        }

         ColorizerProvider.init(getConfig());
    }
}