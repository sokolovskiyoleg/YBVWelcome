package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.List;
import java.util.Locale;

public class ClearCommand extends SubCommand {

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

        String type = args[0].toLowerCase(Locale.ROOT);
        if (type.equals("join")) {
            welcomeService.clearPlayerJoinMessage(player);
        } else if (type.equals("quit")) {
            welcomeService.clearPlayerQuitMessage(player);
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
