package org.yabogvk.ybvwelcome.core;

import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerCache;

public class MessageService {
    private final MessageManager mm;

    public MessageService(MessageManager mm) {
        this.mm = mm;
    }

    public String resolveJoinMessage(Player player, PlayerCache cache) {
        if (cache != null && cache.joinMsg() != null && !cache.joinMsg().isEmpty()) {
            return mm.getFormatJoinCustom().replace("{message}", cache.joinMsg());
        }
        String group = mm.getGroupMessage(player, "join");
        if (group != null) return group;

        return mm.getFormatJoinDefault().replace("{message}", mm.getJoinDefault());
    }

    public String resolveQuitMessage(Player player, PlayerCache cache) {
        if (cache != null && cache.quitMsg() != null && !cache.quitMsg().isEmpty()) {
            return mm.getFormatQuitCustom().replace("{message}", cache.quitMsg());
        }
        String group = mm.getGroupMessage(player, "quit");
        if (group != null) return group;

        return mm.getFormatQuitDefault().replace("{message}", mm.getQuitDefault());
    }
}