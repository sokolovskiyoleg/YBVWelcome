package org.yabogvk.ybvwelcome.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.yabogvk.ybvwelcome.YBVWelcome;

public class PlayerQuitListener implements Listener {
    private final YBVWelcome plugin;

    public PlayerQuitListener(YBVWelcome plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FileConfiguration config = plugin.getConfig();
        Player player = event.getPlayer();

        if (config.getBoolean("messages.quit.disable-vanilla", true)) {
            event.setQuitMessage(null);
        }

        if (!config.getBoolean("messages.quit.enabled", true)) {
            return;
        }

        plugin.getCore().handlePlayerQuit(player);
    }
}