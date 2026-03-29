package org.yabogvk.ybvwelcome.color;

import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.color.impl.LegacyColorizer;
import org.yabogvk.ybvwelcome.color.impl.MiniMessageColorizer;

public final class ColorizerProvider {
    private ColorizerProvider() {
    }

    public static Colorizer create(Settings settings) {
        return create(settings.getSerializerType());
    }

    public static Colorizer create(Settings.SerializerType serializerType) {
        if (serializerType == Settings.SerializerType.MINIMESSAGE) {
            return new MiniMessageColorizer();
        }

        return new LegacyColorizer();
    }
}
