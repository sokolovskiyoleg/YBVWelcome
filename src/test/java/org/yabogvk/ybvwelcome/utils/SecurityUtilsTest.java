package org.yabogvk.ybvwelcome.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilsTest {

    @Test
    void sanitizePlayerNameReturnsNullForInvalidInput() {
        assertNull(SecurityUtils.sanitizePlayerName("bad name!"));
    }

    @Test
    void requireValidPlayerNameReturnsSanitizedValue() {
        assertEquals("Player_One", SecurityUtils.requireValidPlayerName("Player_One"));
    }

    @Test
    void requireValidPlayerNameThrowsForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> SecurityUtils.requireValidPlayerName("bad name!"));
    }

    @Test
    void sanitizeMessageContentRemovesControlCharactersAndTruncates() {
        String input = "hello\u0000" + "a".repeat(1005);

        String sanitized = SecurityUtils.sanitizeMessageContent(input);

        assertTrue(sanitized.startsWith("helloa"));
        assertEquals(1000, sanitized.length());
        assertTrue(sanitized.endsWith("..."));
    }
}
