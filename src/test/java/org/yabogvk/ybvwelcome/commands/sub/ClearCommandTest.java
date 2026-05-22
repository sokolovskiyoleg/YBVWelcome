package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.yabogvk.ybvwelcome.color.impl.LegacyColorizer;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClearCommandTest {

    private final ClearCommand clearCommand = new ClearCommand(
            null,
            null,
            new MessageUtils(null, new LegacyColorizer())
    );

    @Test
    void allowsDedicatedClearPermission() {
        assertTrue(clearCommand.hasAccess(senderWithPermissions(Set.of("ybvwelcome.clear"))));
    }

    @Test
    void allowsLegacySetPermissionForBackwardCompatibility() {
        assertTrue(clearCommand.hasAccess(senderWithPermissions(Set.of("ybvwelcome.set"))));
    }

    @Test
    void rejectsSenderWithoutAnySupportedPermission() {
        assertFalse(clearCommand.hasAccess(senderWithPermissions(Set.of())));
    }

    private CommandSender senderWithPermissions(Set<String> permissions) {
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "hasPermission" -> permissions.contains(args[0]);
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "TestSender";
            default -> throw new UnsupportedOperationException(method.getName());
        };

        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                handler
        );
    }
}
