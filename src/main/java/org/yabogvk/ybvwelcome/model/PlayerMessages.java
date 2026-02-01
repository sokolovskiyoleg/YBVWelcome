package org.yabogvk.ybvwelcome.model;

import org.jetbrains.annotations.Nullable;

public record PlayerMessages(
        @Nullable String joinMessage,
        @Nullable String quitMessage
) {
    public boolean hasNoMessages() {
        return (joinMessage == null || joinMessage.isEmpty()) &&
               (quitMessage == null || quitMessage.isEmpty());
    }
}
