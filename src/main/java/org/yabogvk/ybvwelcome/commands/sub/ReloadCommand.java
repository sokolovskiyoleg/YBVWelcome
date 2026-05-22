package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

public class ReloadCommand extends SubCommand {
    private final Runnable reloadAction;

    public ReloadCommand(Runnable reloadAction, MessageManager messageManager, WelcomeService welcomeService,
                         MessageUtils messageUtils) {
        super(messageManager, welcomeService, messageUtils);
        this.reloadAction = reloadAction;
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
    public void execute(CommandSender sender, String[] args, int offset) {
        if (noPerm(sender)) return;

        reloadAction.run();
        messageUtils.sendMessage(sender, messageManager.getReloadSuccess());
    }
}
