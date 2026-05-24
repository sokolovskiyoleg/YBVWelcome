package org.yabogvk.ybvwelcome.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class SecurityUtils {
    private static final Pattern VALID_PLAYER_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,16}$");

    @Nullable
    public static String sanitizePlayerName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String sanitized = input.trim();
        return VALID_PLAYER_NAME.matcher(sanitized).matches() ? sanitized : null;
    }

    @NotNull
    public static String requireValidPlayerName(String input) {
        String sanitized = sanitizePlayerName(input);
        if (sanitized == null) {
            throw new IllegalArgumentException("Invalid player name: " + input);
        }
        return sanitized;
    }

    @NotNull
    public static String sanitizeMessageContent(String input) {
        if (input == null) {
            return "";
        }

        String sanitized = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
        if (sanitized.length() > 1000) {
            return sanitized.substring(0, 997) + "...";
        }
        return sanitized;
    }
}
