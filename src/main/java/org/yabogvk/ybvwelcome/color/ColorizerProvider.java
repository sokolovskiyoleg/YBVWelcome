package org.yabogvk.ybvwelcome.color;

import lombok.experimental.UtilityClass;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.color.impl.LegacyColorizer;
import org.yabogvk.ybvwelcome.color.impl.MiniMessageColorizer;

import java.util.Locale;

@UtilityClass
public class ColorizerProvider {
    public static Colorizer COLORIZER;

    public static void init(Settings settings) {
        String serializerType = settings.serializer.toUpperCase(Locale.ENGLISH);
        COLORIZER = "MINIMESSAGE".equals(serializerType)
                ? new MiniMessageColorizer()
                : new LegacyColorizer();
    }
}