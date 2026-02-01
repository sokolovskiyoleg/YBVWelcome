package org.yabogvk.ybvwelcome.core;

import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.db.Database;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.model.PlayerMessages;

public class MessageService {
    private final MessageManager mm;

    public MessageService(MessageManager mm) {
        this.mm = mm;
    }

    public String resolveJoinMessage(Player player, PlayerMessages cache) {
        return resolveMessage(player, cache, Database.MessageType.WELCOME);
    }

    public String resolveQuitMessage(Player player, PlayerMessages cache) {
        return resolveMessage(player, cache, Database.MessageType.QUIT);
    }

    private String resolveMessage(Player player, PlayerMessages cache, Database.MessageType type) {
        final String customMessage;
        final String customFormat;
        final String defaultMessage;
        final String defaultFormat;
        final String groupType;

        if (type == Database.MessageType.WELCOME) {
            customMessage = (cache != null) ? cache.joinMessage() : null;
            customFormat = mm.getFormatJoinCustom();
            defaultMessage = mm.getJoinDefault();
            defaultFormat = mm.getFormatJoinDefault();
            groupType = "join";
        } else {
            customMessage = (cache != null) ? cache.quitMessage() : null;
            customFormat = mm.getFormatQuitCustom();
            defaultMessage = mm.getQuitDefault();
            defaultFormat = mm.getFormatQuitDefault();
            groupType = "quit";
        }

        if (customMessage != null && !customMessage.isEmpty()) {
            return customFormat.replace("{message}", customMessage);
        }

        String groupMessage = mm.getGroupMessage(player, groupType);
        if (groupMessage != null) {
            return groupMessage;
        }

        return defaultFormat.replace("{message}", defaultMessage);
    }
}
