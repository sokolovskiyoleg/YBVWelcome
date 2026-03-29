package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(YBVWelcome plugin, MessageManager messageManager, WelcomeService welcomeService,
                         MessageUtils messageUtils) {
        super(plugin, messageManager, welcomeService, messageUtils);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "ybvwelcome.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (noPerm(sender)) return;

        plugin.reloadPlugin();
        messageUtils.sendMessage(sender, messageManager.getReloadSuccess());
    }
}
