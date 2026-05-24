package org.yabogvk.ybvwelcome.service;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisStd;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;
import org.yabogvk.ybvwelcome.utils.MessageUtils;
import org.yabogvk.ybvwelcome.color.impl.LegacyColorizer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServiceTest {

    @Test
    void resolveMessageUsesCustomThenGroupThenDefault() throws Exception {
        MessageManager manager = new ObjenesisStd().newInstance(MessageManager.class);
        setField(manager, "cachedMessages", new HashMap<String, String>() {{
            put("formats.join.custom", "C:{message}");
            put("formats.join.default", "D:{message}");
            put("defaults.join", "default");
        }});
        setField(manager, "cachedLists", new HashMap<String, java.util.List<String>>());
        setField(manager, "cachedGroupMessages", java.util.List.of());
        setField(manager, "groupMessagesEnabled", false);

        MessageService service = new MessageService(() -> false, manager, new MessageUtils(null, new LegacyColorizer()));
        Player player = playerWithoutPermissions();

        String custom = service.resolveMessage(player, new PlayerMessages("mine", null), Database.MessageType.WELCOME);
        String fallback = service.resolveMessage(player, null, Database.MessageType.WELCOME);

        assertEquals("C:mine", custom);
        assertEquals("D:default", fallback);
    }

    private Player playerWithoutPermissions() {
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "hasPermission" -> false;
            case "getName" -> "PlayerOne";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.getName());
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
