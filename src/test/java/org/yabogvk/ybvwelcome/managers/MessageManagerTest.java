package org.yabogvk.ybvwelcome.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisStd;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageManagerTest {

    @Test
    void groupMessagesRespectConfigOrderTopDown() throws Exception {
        YamlConfiguration messagesConfig = new YamlConfiguration();
        messagesConfig.set("group-messages.enabled", true);
        messagesConfig.set("defaults.join", "joined");
        messagesConfig.set("defaults.quit", "left");
        messagesConfig.set("group-messages.list.premium.permission", "group.premium");
        messagesConfig.set("group-messages.list.premium.join", "premium-join");
        messagesConfig.set("group-messages.list.premium.quit", "premium-quit");
        messagesConfig.set("group-messages.list.vip.permission", "group.vip");
        messagesConfig.set("group-messages.list.vip.join", "vip-join");
        messagesConfig.set("group-messages.list.vip.quit", "vip-quit");

        ConfigManager configManager = new ObjenesisStd().newInstance(ConfigManager.class);
        setField(configManager, "messagesConfig", messagesConfig);

        YBVWelcome plugin = new ObjenesisStd().newInstance(YBVWelcome.class);
        setField(plugin, "configManager", configManager);

        MessageManager messageManager = new MessageManager(plugin);

        assertEquals("premium-join", messageManager.getGroupMessage(player(true, true), "join"));
        assertEquals("joined", messageManager.getJoinDefault());
        assertEquals("left", messageManager.getQuitDefault());
    }

    private Player player(boolean premium, boolean vip) {
        InvocationHandler handler = (proxy, method, args) -> {
            return switch (method.getName()) {
                case "hasPermission" -> {
                    String permission = (String) args[0];
                    yield switch (permission) {
                        case "group.premium" -> premium;
                        case "group.vip" -> vip;
                        default -> false;
                    };
                }
                case "getUniqueId" -> UUID.randomUUID();
                case "getName" -> "PlayerOne";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "TestPlayer";
                default -> throw new UnsupportedOperationException(method.getName());
            };
        };

        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                handler
        );
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
