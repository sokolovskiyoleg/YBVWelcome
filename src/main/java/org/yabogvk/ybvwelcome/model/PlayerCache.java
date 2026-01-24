package org.yabogvk.ybvwelcome.model;

import org.jetbrains.annotations.Nullable;


public record PlayerCache(
        @Nullable String joinMsg,
        @Nullable String quitMsg
) {
    public boolean isEmpty() {
        return (joinMsg == null || joinMsg.isEmpty()) &&
                (quitMsg == null || quitMsg.isEmpty());
    }
}