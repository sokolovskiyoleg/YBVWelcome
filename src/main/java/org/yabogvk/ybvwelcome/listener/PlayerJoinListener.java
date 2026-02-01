package org.yabogvk.ybvwelcome.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.config.Settings;

public class PlayerJoinListener implements Listener {
    private final YBVWelcome plugin;
    private final Settings settings;

    public PlayerJoinListener(YBVWelcome plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (settings.joinDisableVanilla) {
            event.setJoinMessage(null);
        }

        if (!settings.joinEnabled) {
            return;
        }

        boolean isFirst = !player.hasPlayedBefore() && settings.firstJoinEnabled;

        plugin.getCore().loadAndJoin(player, isFirst);
    }
}
