package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.List;
import java.util.Locale;

public class SetCommand extends SubCommand {

    public SetCommand(MessageManager messageManager, WelcomeService welcomeService,
                      MessageUtils messageUtils) {
        super(messageManager, welcomeService, messageUtils);
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
    public void execute(CommandSender sender, String[] args, int offset) {
        if (notPlayer(sender) || noPerm(sender)) return;

        Player player = (Player) sender;

        if (args.length - offset < 2) {
            messageUtils.sendMessage(sender, messageManager.getUsageSet());
            return;
        }

        String type = args[offset].toLowerCase(Locale.ROOT);
        String message = joinArgs(args, offset + 1);

        switch (type) {
            case "join" -> welcomeService.setPlayerWelcomeMessage(player, message);
            case "quit" -> welcomeService.setPlayerQuitMessage(player, message);
            default -> messageUtils.sendMessage(player, messageManager.getInvalidType().replace("{type}", type));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args, int offset) {
        if (args.length - offset == 1) {
            return List.of("join", "quit");
        }
        return super.complete(sender, args, offset);
    }

    private String joinArgs(String[] args, int offset) {
        StringBuilder builder = new StringBuilder();
        for (int i = offset; i < args.length; i++) {
            if (i > offset) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}
