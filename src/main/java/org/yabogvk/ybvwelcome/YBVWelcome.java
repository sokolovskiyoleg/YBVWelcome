package org.yabogvk.ybvwelcome;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;
import org.yabogvk.ybvwelcome.commands.WelcomeCommand;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.core.WelcomeCore;
import org.yabogvk.ybvwelcome.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.listener.PlayerJoinListener;
import org.yabogvk.ybvwelcome.listener.PlayerQuitListener;
import org.yabogvk.ybvwelcome.managers.ConfigManager;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.repository.DatabaseMessageRepository;
import org.yabogvk.ybvwelcome.repository.MessageRepository;
import org.yabogvk.ybvwelcome.service.AsyncExecutor;
import org.yabogvk.ybvwelcome.service.MessageService;
import org.yabogvk.ybvwelcome.service.MessageRenderer;
import org.yabogvk.ybvwelcome.service.PlayerMessageCache;
import org.yabogvk.ybvwelcome.service.StorageService;
import org.yabogvk.ybvwelcome.service.WelcomeService;

import java.sql.SQLException;
import java.util.logging.Level;

public final class YBVWelcome extends JavaPlugin {
    private static YBVWelcome instance;

    private ConfigManager configManager;
    private Settings settings;
    private MessageManager messageManager;
    private AsyncExecutor asyncExecutor;
    private StorageService storageService;
    private MessageService runtimeMessageService;
    private WelcomeService welcomeService;
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
            MessageRepository messageRepository = new DatabaseMessageRepository(DatabaseProvider.database);
            asyncExecutor = new AsyncExecutor(this);
            storageService = new StorageService(this, messageRepository, new PlayerMessageCache(), asyncExecutor);
            runtimeMessageService = new MessageService(this, messageManager, new MessageRenderer());
            welcomeService = new WelcomeService(this, storageService, runtimeMessageService, asyncExecutor, messageManager);
            this.core = new WelcomeCore(welcomeService);
            new WelcomeCommand();
            registerListener();

            getLogger().info("YBVWelcome enabled successfully!");

        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not initialize database! Disabling plugin...", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        this.placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (!placeholderAPIEnabled) {
            getLogger().log(Level.WARNING, "PlaceholderAPI not found. PAPI placeholders will not work.");
        }
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.close();
        }
    }

    public void reload() {
        reloadPlugin();
    }

    public void reloadPlugin() {
        configManager.reload();
        settings.load();
        messageManager.reload();
        ColorizerProvider.init(settings);
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (welcomeService != null) {
            welcomeService.reload();
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

    public AsyncExecutor getAsyncExecutor() {
        return asyncExecutor;
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public MessageService getRuntimeMessageService() {
        return runtimeMessageService;
    }

    public WelcomeService getWelcomeService() {
        return welcomeService;
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
