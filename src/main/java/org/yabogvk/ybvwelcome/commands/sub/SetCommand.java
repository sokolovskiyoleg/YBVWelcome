package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.core.WelcomeCore;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.Arrays;
import java.util.List;
public class SetCommand extends SubCommand {

    private final WelcomeCore core = plugin.getCore();

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getPermission() {
        return "ybvwelcome.set";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (notPlayer(sender) || noPerm(sender)) return;

        Player player = (Player) sender;

        if (args.length < 2) {
            MessageUtils.sendMessage(sender, messageManager.getUsageSet());
            return;
        }

        String type = args[0].toLowerCase();
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (type.equals("join")) {
            core.setPlayerWelcomeMessage(player, message);
        } else if (type.equals("quit")) {
            core.setPlayerQuitMessage(player, message);
        } else {
            MessageUtils.sendMessage(player, messageManager.getUsageSet());
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
