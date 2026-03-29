package org.yabogvk.ybvwelcome.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.commands.sub.ClearCommand;
import org.yabogvk.ybvwelcome.commands.sub.ReloadCommand;
import org.yabogvk.ybvwelcome.commands.sub.SetCommand;
import org.yabogvk.ybvwelcome.commands.sub.SubCommand;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class WelcomeCommand implements CommandExecutor, TabCompleter {

    private final YBVWelcome plugin;
    private final MessageManager messageManager;
    private final Settings settings;
    private final MessageUtils messageUtils;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public WelcomeCommand(YBVWelcome plugin, MessageManager messageManager, WelcomeService welcomeService,
                          Settings settings, MessageUtils messageUtils) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.settings = settings;
        this.messageUtils = messageUtils;

        PluginCommand pluginCommand = plugin.getCommand("ybvwelcome");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }

        subCommands.add(new ReloadCommand(plugin, messageManager, welcomeService, messageUtils));
        subCommands.add(new SetCommand(plugin, messageManager, welcomeService, messageUtils));
        subCommands.add(new ClearCommand(plugin, messageManager, welcomeService, messageUtils));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ybvwelcome.use")) {
            messageUtils.sendMessage(sender, messageManager.getNoPermissions());
            return true;
        }

        if (args.length == 0) {
            messageUtils.sendMessage(sender, messageManager.getUsage());
            return true;
        }

        String subCommandName = args[0].toLowerCase(Locale.ROOT);
        Optional<SubCommand> subCmd = subCommands.stream()
                .filter(sc -> sc.getName().equalsIgnoreCase(subCommandName))
                .findFirst();

        if (sender instanceof Player player) {
            if (subCmd.isPresent() && subCmd.get().hasAccess(sender)
                    && (subCommandName.equals("set") || subCommandName.equals("clear"))) {
                int cooldownTime = settings.getCommandCooldown();
                if (cooldownTime > 0) {
                    long lastUsed = cooldowns.getOrDefault(player.getUniqueId(), 0L);
                    long timeRemainingMillis = (lastUsed + (cooldownTime * 1000L)) - System.currentTimeMillis();

                    if (timeRemainingMillis > 0) {
                        long timeLeftSeconds = (long) Math.ceil(timeRemainingMillis / 1000.0);
                        messageUtils.sendMessage(player, messageManager.getCooldown().replace("{time}", String.valueOf(timeLeftSeconds)));
                        return true;
                    }
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }

        if (subCmd.isPresent()) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            subCmd.get().execute(sender, subArgs);
        } else {
            messageUtils.sendMessage(sender, messageManager.getUsage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(sc -> sc.hasAccess(sender))
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            String subCommandName = args[0].toLowerCase(Locale.ROOT);
            Optional<SubCommand> subCmd = subCommands.stream()
                    .filter(sc -> sc.getName().equalsIgnoreCase(subCommandName))
                    .findFirst();

            if (subCmd.isPresent() && subCmd.get().hasAccess(sender)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                List<String> completions = subCmd.get().complete(sender, subArgs);
                if (completions != null) {
                    return completions.stream()
                            .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[args.length - 1].toLowerCase(Locale.ROOT)))
                            .collect(Collectors.toList());
                }
            }
        }
        return null;
    }
}
