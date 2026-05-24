package org.yabogvk.ybvwelcome.service;

import org.junit.jupiter.api.Test;
import org.yabogvk.ybvwelcome.color.impl.LegacyColorizer;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WelcomeServiceMessageLengthTest {

    @Test
    void messageLengthIgnoresSpaces() throws Exception {
        WelcomeService service = new WelcomeService(
                Logger.getLogger("test"),
                () -> 100,
                null,
                null,
                null,
                null,
                new MessageUtils(null, new LegacyColorizer())
        );

        Method method = WelcomeService.class.getDeclaredMethod("messageLength", String.class);
        method.setAccessible(true);

        int length = (int) method.invoke(service, "a b c");
        int nullLength = (int) method.invoke(service, new Object[]{null});

        assertEquals(3, length);
        assertEquals(0, nullLength);
    }
}
