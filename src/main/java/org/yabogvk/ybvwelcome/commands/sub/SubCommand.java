package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.List;

public abstract class SubCommand {

    protected final YBVWelcome plugin;
    protected final MessageManager messageManager;
    protected final WelcomeService welcomeService;
    protected final MessageUtils messageUtils;

    protected SubCommand(YBVWelcome plugin, MessageManager messageManager, WelcomeService welcomeService,
                         MessageUtils messageUtils) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.welcomeService = welcomeService;
        this.messageUtils = messageUtils;
    }

    public abstract String getName();
    public abstract String getPermission();
    public abstract void execute(CommandSender sender, String[] args, int offset);

    public boolean hasAccess(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    public List<String> complete(CommandSender sender, String[] args, int offset) {
        return List.of();
    }

    protected boolean noPerm(CommandSender sender) {
        if (!hasAccess(sender)) {
            messageUtils.sendMessage(sender, messageManager.getNoPermissions());
            return true;
        }
        return false;
    }

    protected boolean notPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, messageManager.getOnlyForPlayers());
            return true;
        }
        return false;
    }
}
