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
import org.yabogvk.ybvwelcome.commands.sub.DebugCommand;
import org.yabogvk.ybvwelcome.commands.sub.ReloadCommand;
import org.yabogvk.ybvwelcome.commands.sub.SetCommand;
import org.yabogvk.ybvwelcome.commands.sub.SubCommand;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WelcomeCommand implements CommandExecutor, TabCompleter {
    private static final int COOLDOWN_CLEANUP_THRESHOLD = 256;

    private final MessageManager messageManager;
    private final Settings settings;
    private final MessageUtils messageUtils;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public WelcomeCommand(YBVWelcome plugin, MessageManager messageManager, WelcomeService welcomeService,
                          Settings settings, MessageUtils messageUtils) {
        this.messageManager = messageManager;
        this.settings = settings;
        this.messageUtils = messageUtils;

        PluginCommand pluginCommand = plugin.getCommand("ybvwelcome");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }

        subCommands.add(new ReloadCommand(plugin::reloadPlugin, messageManager, welcomeService, messageUtils));
        subCommands.add(new DebugCommand(messageManager, welcomeService, messageUtils, () -> settings, plugin::isPlaceholderAPIEnabled));
        subCommands.add(new SetCommand(messageManager, welcomeService, messageUtils));
        subCommands.add(new ClearCommand(messageManager, welcomeService, messageUtils));
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

        String subCommandName = args[0];
        SubCommand subCmd = findSubCommand(subCommandName);

        if (sender instanceof Player player) {
            if (subCmd != null && subCmd.hasAccess(sender)
                    && ("set".equalsIgnoreCase(subCommandName) || "clear".equalsIgnoreCase(subCommandName))) {
                int cooldownTime = settings.getCommandCooldown();
                if (cooldownTime > 0) {
                    cleanupCooldowns(cooldownTime);
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

        if (subCmd != null) {
            subCmd.execute(sender, args, 1);
        } else {
            messageUtils.sendMessage(sender, messageManager.getUsage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0];
            List<String> result = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                String name = subCommand.getName();
                if (subCommand.hasAccess(sender) && startsWithIgnoreCase(name, prefix)) {
                    result.add(name);
                }
            }
            return result;
        }

        if (args.length > 1) {
            String subCommandName = args[0];
            SubCommand subCmd = findSubCommand(subCommandName);

            if (subCmd != null && subCmd.hasAccess(sender)) {
                List<String> completions = subCmd.complete(sender, args, 1);
                if (completions != null) {
                    String prefix = args[args.length - 1];
                    List<String> result = new ArrayList<>();
                    for (String completion : completions) {
                        if (startsWithIgnoreCase(completion, prefix)) {
                            result.add(completion);
                        }
                    }
                    return result;
                }
            }
        }
        return null;
    }

    private SubCommand findSubCommand(String name) {
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(name)) {
                return subCommand;
            }
        }
        return null;
    }

    private boolean startsWithIgnoreCase(String value, String prefix) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private void cleanupCool
    downs(int cooldownSeconds) {
        if (cooldowns.size() < COOLDOWN_CLEANUP_THRESHOLD) {
            return;
        }

        long now = System.currentTimeMillis();
        long ttlMillis = cooldownSeconds * 1000L;
        cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > ttlMillis);
    }
}
