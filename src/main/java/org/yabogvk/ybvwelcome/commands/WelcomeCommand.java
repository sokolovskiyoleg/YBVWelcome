package org.yabogvk.ybvwelcome.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.core.WelcomeCore;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WelcomeCommand extends AbstractCommand {

    private final YBVWelcome instance = YBVWelcome.getInstance();
    private final MessageManager messageManager = instance.getMessageManager();
    private final WelcomeCore core = instance.getCore();

    public WelcomeCommand() {
        super("ybvwelcome");
        registerAliases("welcome");
    }


    private boolean noPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm)) {
            MessageUtils.sendMessage(sender, messageManager.getNoPermissions());
            return true;
        }
        return false;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (noPerm(sender, "ybvwelcome.use")) return;

        if (args.length == 0) {
            MessageUtils.sendMessage(sender, messageManager.getUsage());
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "set"    -> handleSet(sender, args);
            case "clear"  -> handleClear(sender, args);
            default       -> MessageUtils.sendMessage(sender, messageManager.getUsage());
        }
    }

    private void handleReload(CommandSender sender) {
        if (noPerm(sender, "ybvwelcome.admin")) return;

        instance.reload();
        MessageUtils.sendMessage(sender, messageManager.getReloadSuccess());
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, messageManager.getOnlyForPlayers());
            return;
        }
        if (noPerm(player, "ybvwelcome.set")) return;

        if (args.length < 3) {
            MessageUtils.sendMessage(sender, messageManager.getUsageSet());
            return;
        }

        String type = args[1].toLowerCase();
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (type.equals("join")) {
            core.setPlayerWelcomeMessage(player, message);
        } else if (type.equals("quit")) {
            core.setPlayerQuitMessage(player, message);
        } else {
            MessageUtils.sendMessage(player, messageManager.getUsageSet());
        }
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, messageManager.getOnlyForPlayers());
            return;
        }
        if (noPerm(player, "ybvwelcome.set")) return;

        if (args.length < 2) {
            MessageUtils.sendMessage(sender, messageManager.getUsageClear());
            return;
        }

        String type = args[1].toLowerCase();
        if (type.equals("join")) {
            core.clearPlayerJoinMessage(player);
        } else if (type.equals("quit")) {
            core.clearPlayerQuitMessage(player);
        } else {
            MessageUtils.sendMessage(player, messageManager.getUsageClear());
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String... args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("ybvwelcome.admin")) completions.add("reload");
            if (sender.hasPermission("ybvwelcome.set")) {
                completions.add("set");
                completions.add("clear");
            }
            return filter(completions, args[0]);
        }

        if (args.length == 2 && sender.hasPermission("ybvwelcome.set")) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear")) {
                completions.add("join");
                completions.add("quit");
            }
        }

        return filter(completions, args[args.length - 1]);
    }

    private List<String> filter(List<String> list, String input) {
        String lastArg = input.toLowerCase();
        return list.stream()
                .filter(s -> s.startsWith(lastArg))
                .collect(Collectors.toList());
    }
}