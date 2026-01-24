package org.yabogvk.ybvwelcome.utils;

import java.util.regex.Pattern;

/**
 * Security utilities for input validation and sanitization
 */
public class SecurityUtils {

    // Pattern for valid player names (alphanumeric, underscore, dash)
    private static final Pattern VALID_PLAYER_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,16}$");

    // Pattern for valid hex colors
    private static final Pattern VALID_HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    // Pattern for valid theme names
    private static final Pattern VALID_THEME_NAME = Pattern.compile("^[a-zA-Z0-9_-]+$");

    // Pattern for valid animation types
    private static final Pattern VALID_ANIMATION_TYPE = Pattern.compile("^[a-zA-Z0-9_-]+$");

    /**
     * Sanitize player name input
     * @param input Raw input string
     * @return Sanitized player name or null if invalid
     */
    public static String sanitizePlayerName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String sanitized = input.trim();

        // Check if it matches valid player name pattern
        if (!VALID_PLAYER_NAME.matcher(sanitized).matches()) {
            return null;
        }

        return sanitized;
    }

    /**
     * Sanitize general text input (remove potentially dangerous characters)
     * @param input Raw input string
     * @return Sanitized text
     */
    public static String sanitizeText(String input) {
        if (input == null) {
            return "";
        }

        // Remove control characters and limit length
        String sanitized = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .trim();

        // Limit length to prevent memory issues
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 997) + "...";
        }

        return sanitized;
    }

    /**
     * Sanitize theme name input
     * @param input Raw input string
     * @return Sanitized theme name or null if invalid
     */
    public static String sanitizeThemeName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String sanitized = input.trim().toLowerCase();

        if (!VALID_THEME_NAME.matcher(sanitized).matches()) {
            return null;
        }

        return sanitized;
    }

    /**
     * Sanitize animation type input
     * @param input Raw input string
     * @return Sanitized animation type or null if invalid
     */
    public static String sanitizeAnimationType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String sanitized = input.trim().toLowerCase();

        if (!VALID_ANIMATION_TYPE.matcher(sanitized).matches()) {
            return null;
        }

        return sanitized;
    }

    /**
     * Validate hex color input
     * @param input Raw input string
     * @return true if valid hex color
     */
    public static boolean isValidHexColor(String input) {
        if (input == null) {
            return false;
        }

        return VALID_HEX_COLOR.matcher(input).matches();
    }

    /**
     * Escape HTML-like characters to prevent injection
     * @param input Raw input string
     * @return Escaped string
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }

        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Check if input contains only safe characters for display
     * @param input Raw input string
     * @return true if input is safe
     */
    public static boolean isSafeForDisplay(String input) {
        if (input == null) {
            return true;
        }

        // Check for potentially dangerous patterns
        String lower = input.toLowerCase();

        // Block common injection patterns
        String[] dangerousPatterns = {
                "<script", "javascript:", "data:", "vbscript:",
                "onload=", "onerror=", "onclick=", "onmouseover="
        };

        for (String pattern : dangerousPatterns) {
            if (lower.contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sanitize message content for safe display
     * @param input Raw input string
     * @return Sanitized message content
     */
    public static String sanitizeMessageContent(String input) {
        if (input == null) {
            return "";
        }

        // Remove control characters and limit length
        String sanitized = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .trim();

        // Limit length to prevent memory issues
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 997) + "...";
        }

        // Don't escape HTML characters for Minecraft messages as it corrupts color codes
        // Only escape potentially dangerous characters that could cause issues
        // Note: Not escaping quotes and apostrophes as they're safe in Minecraft chat

        return sanitized;
    }
}