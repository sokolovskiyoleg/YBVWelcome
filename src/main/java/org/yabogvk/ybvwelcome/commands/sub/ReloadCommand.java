package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

public class ReloadCommand extends SubCommand {

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

        plugin.reload();
        MessageUtils.sendMessage(sender, messageManager.getReloadSuccess());
    }
}
