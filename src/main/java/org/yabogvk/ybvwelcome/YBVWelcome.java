package org.yabogvk.ybvwelcome;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.ybvwelcome.color.ColorizerProvider;
import org.yabogvk.ybvwelcome.commands.WelcomeCommand;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.db.DatabaseProvider;
import org.yabogvk.ybvwelcome.listener.PlayerJoinListener;
import org.yabogvk.ybvwelcome.listener.PlayerQuitListener;
import org.yabogvk.ybvwelcome.managers.ConfigManager;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.service.AsyncExecutor;
import org.yabogvk.ybvwelcome.service.MessageService;
import org.yabogvk.ybvwelcome.service.PlayerMessageCache;
import org.yabogvk.ybvwelcome.service.StorageService;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.sql.SQLException;
import java.util.logging.Level;

public final class YBVWelcome extends JavaPlugin {
    @Getter
    private ConfigManager configManager;
    @Getter
    private Settings settings;
    private MessageManager messageManager;
    @Getter
    private WelcomeService welcomeService;
    private MessageUtils messageUtils;
    @Getter
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        settings = new Settings(this);
        messageManager = new MessageManager(this);
        messageUtils = new MessageUtils(this, ColorizerProvider.create(settings));

        try {
            Database database = DatabaseProvider.create(this);
            AsyncExecutor asyncExecutor = new AsyncExecutor(this);
            StorageService storageService = new StorageService(getLogger(), database, new PlayerMessageCache(), asyncExecutor);
            MessageService runtimeMessageService = new MessageService(this::isPlaceholderAPIEnabled, messageManager, messageUtils);
            welcomeService = new WelcomeService(getLogger(), () -> settings.getAllowedSymbols(), storageService, runtimeMessageService,
                    asyncExecutor, messageManager, messageUtils);
            new WelcomeCommand(this, messageManager, welcomeService, settings, messageUtils);
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
        if (welcomeService != null) {
            welcomeService.close();
        }
    }

    public void reloadPlugin() {
        configManager.reload();
        settings.load();
        messageManager.reload();
        messageUtils.setColorizer(ColorizerProvider.create(settings));
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (welcomeService != null) {
            welcomeService.reload();
        }
    }

    private void registerListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
    }
}
