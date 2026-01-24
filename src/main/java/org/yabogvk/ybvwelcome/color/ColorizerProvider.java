package org.yabogvk.ybvwelcome.color;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.yabogvk.ybvwelcome.color.impl.LegacyColorizer;
import org.yabogvk.ybvwelcome.color.impl.MiniMessageColorizer;

import java.util.Locale;

@UtilityClass
public class ColorizerProvider {
    public static Colorizer COLORIZER;

    public static void init(ConfigurationSection config) {
        String serializerType = config.getString("serializer", "LEGACY").toUpperCase(Locale.ENGLISH);
        COLORIZER = "MINIMESSAGE".equals(serializerType)
                ? new MiniMessageColorizer()
                : new LegacyColorizer();
    }
}