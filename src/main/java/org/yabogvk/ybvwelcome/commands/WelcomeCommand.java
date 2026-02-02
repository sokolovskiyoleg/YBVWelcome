package org.yabogvk.ybvwelcome.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.commands.sub.ClearCommand;
import org.yabogvk.ybvwelcome.commands.sub.ReloadCommand;
import org.yabogvk.ybvwelcome.commands.sub.SetCommand;
import org.yabogvk.ybvwelcome.commands.sub.SubCommand;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WelcomeCommand implements CommandExecutor, TabCompleter {

    private final YBVWelcome plugin = YBVWelcome.getInstance();
    private final MessageManager messageManager = plugin.getMessageManager();
    private final List<SubCommand> subCommands = new ArrayList<>();

    public WelcomeCommand() {
        PluginCommand pluginCommand = plugin.getCommand("ybvwelcome");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }

        subCommands.add(new ReloadCommand());
        subCommands.add(new SetCommand());
        subCommands.add(new ClearCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ybvwelcome.use")) {
            MessageUtils.sendMessage(sender, messageManager.getNoPermissions());
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendMessage(sender, messageManager.getUsage());
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        Optional<SubCommand> subCmd = subCommands.stream()
                .filter(sc -> sc.getName().equalsIgnoreCase(subCommandName))
                .findFirst();

        if (subCmd.isPresent()) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            subCmd.get().execute(sender, subArgs);
        } else {
            MessageUtils.sendMessage(sender, messageManager.getUsage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(sc -> sender.hasPermission(sc.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            String subCommandName = args[0].toLowerCase();
            Optional<SubCommand> subCmd = subCommands.stream()
                    .filter(sc -> sc.getName().equalsIgnoreCase(subCommandName))
                    .findFirst();

            if (subCmd.isPresent() && sender.hasPermission(subCmd.get().getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> completions = subCmd.get().complete(sender, subArgs);
                if (completions != null) {
                    return completions.stream()
                            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        return null;
    }
}
