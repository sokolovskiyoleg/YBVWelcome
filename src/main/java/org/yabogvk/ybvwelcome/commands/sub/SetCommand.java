package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.List;
import java.util.Locale;

public class SetCommand extends SubCommand {

    public SetCommand(YBVWelcome plugin, MessageManager messageManager, WelcomeService welcomeService,
                      MessageUtils messageUtils) {
        super(plugin, messageManager, welcomeService, messageUtils);
    }

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
            messageUtils.sendMessage(sender, messageManager.getUsageSet());
            return;
        }

        String type = args[0].toLowerCase(Locale.ROOT);
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        switch (type) {
            case "join" -> welcomeService.setPlayerWelcomeMessage(player, message);
            case "quit" -> welcomeService.setPlayerQuitMessage(player, message);
            default -> messageUtils.sendMessage(player, messageManager.getUsageSet());
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
