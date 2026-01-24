package org.yabogvk.ybvwelcome.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.yabogvk.ybvwelcome.YBVWelcome;

public class PlayerJoinListener implements Listener {
    private final YBVWelcome plugin;

    public PlayerJoinListener(YBVWelcome plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = plugin.getConfig();
        Player player = event.getPlayer();

        if (config.getBoolean("messages.join.disable-vanilla", true)) {
            event.setJoinMessage(null);
        }

        if (!config.getBoolean("messages.join.enabled", true)) {
            return;
        }

        boolean isFirst = !player.hasPlayedBefore() && config.getBoolean("messages.join.firstjoin-enabled", true);

        plugin.getCore().loadAndJoin(player, isFirst);
    }
}