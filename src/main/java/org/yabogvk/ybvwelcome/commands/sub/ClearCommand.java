package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.core.WelcomeCore;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.List;

public class ClearCommand extends SubCommand {

    private final WelcomeCore core = plugin.getCore();

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getPermission() {
        return "ybvwelcome.set";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (notPlayer(sender) || noPerm(sender)) return;

        Player player = (Player) sender;

        if (args.length < 1) {
            MessageUtils.sendMessage(sender, messageManager.getUsageClear());
            return;
        }

        String type = args[0].toLowerCase();
        if (type.equals("join")) {
            core.clearPlayerJoinMessage(player);
        } else if (type.equals("quit")) {
            core.clearPlayerQuitMessage(player);
        } else {
            MessageUtils.sendMessage(player, messageManager.getUsageClear());
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("join", "quit");
        }
        return super.complete(sender, args);
    }
}
