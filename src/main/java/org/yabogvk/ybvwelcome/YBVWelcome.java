package org.yabogvk.ybvwelcome;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;
import org.yabogvk.ybvwelcome.commands.WelcomeCommand;
import org.yabogvk.ybvwelcome.managers.ConfigManager;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.core.WelcomeCore;
import org.yabogvk.ybvwelcome.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.listener.PlayerJoinListener;
import org.yabogvk.ybvwelcome.listener.PlayerQuitListener;
import org.yabogvk.ybvwelcome.managers.MessageManager;

import java.sql.SQLException;
import java.util.logging.Level;

public final class YBVWelcome extends JavaPlugin {
    private static YBVWelcome instance;

    private ConfigManager configManager;
    private Settings settings;
    private MessageManager messageManager;
    private WelcomeCore core;
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        settings = new Settings(this);
        messageManager = new MessageManager(this);
        
        ColorizerProvider.init(settings);

        try {
            DatabaseProvider.init(this);
            this.core = new WelcomeCore(this, this.messageManager, DatabaseProvider.database);
            new WelcomeCommand();
            registerListener();

            getLogger().info("YBVWelcome enabled successfully!");

        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not initialize database! Disabling plugin...", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        
        this.placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (!placeholderAPIEnabled) {
            getLogger().log(Level.WARNING, "PlaceholderAPI not found. PAPI placeholders will not work.");
        }
    }

    @Override
    public void onDisable() {
        if (core != null) core.close();
    }

    public void reload() {
        configManager.reload();
        settings.load();
        messageManager.reload();
        ColorizerProvider.init(settings);
        if (core != null) {
            core.reload();
        }
    }

    // --- Getters ---

    public static YBVWelcome getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Settings getSettings() {
        return settings;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public WelcomeCore getCore() {
        return core;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
    }
}
