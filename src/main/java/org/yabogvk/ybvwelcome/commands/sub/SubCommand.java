package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final YBVWelcome plugin = YBVWelcome.getInstance();
    protected final MessageManager messageManager = plugin.getMessageManager();

    public abstract String getName();
    public abstract String getPermission();
    public abstract void execute(CommandSender sender, String[] args);

    public List<String> complete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    protected boolean noPerm(CommandSender sender) {
        if (!sender.hasPermission(getPermission())) {
            MessageUtils.sendMessage(sender, messageManager.getNoPermissions());
            return true;
        }
        return false;
    }

    protected boolean notPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, messageManager.getOnlyForPlayers());
            return true;
        }
        return false;
    }
}
