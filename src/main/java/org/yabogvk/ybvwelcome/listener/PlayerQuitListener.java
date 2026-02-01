package org.yabogvk.ybvwelcome.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.config.Settings;

public class PlayerQuitListener implements Listener {
    private final YBVWelcome plugin;
    private final Settings settings;

    public PlayerQuitListener(YBVWelcome plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (settings.quitDisableVanilla) {
            event.setQuitMessage(null);
        }

        if (!settings.quitEnabled) {
            return;
        }

        plugin.getCore().handlePlayerQuit(player);
    }
}
