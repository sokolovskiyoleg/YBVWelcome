package org.yabogvk.ybvwelcome.commands.sub;

import org.bukkit.command.CommandSender;
import org.yabogvk.ybvwelcome.config.Settings;
import org.yabogvk.ybvwelcome.managers.MessageManager;
import org.yabogvk.ybvwelcome.service.WelcomeService;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DebugCommand extends SubCommand {
    private final Supplier<Settings> settingsSupplier;
    private final BooleanSupplier placeholderEnabledSupplier;

    public DebugCommand(MessageManager messageManager, WelcomeService welcomeService, MessageUtils messageUtils,
                        Supplier<Settings> settingsSupplier, BooleanSupplier placeholderEnabledSupplier) {
        super(messageManager, welcomeService, messageUtils);
        this.settingsSupplier = settingsSupplier;
        this.placeholderEnabledSupplier = placeholderEnabledSupplier;
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getPermission() {
        return "ybvwelcome.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args, int offset) {
        if (noPerm(sender)) return;

        Settings settings = settingsSupplier.get();

        messageUtils.sendMessage(sender, messageManager.getDebugHeader());
        messageUtils.sendMessage(sender, messageManager.getDebugSerializer().replace("{value}", settings.getSerializerType().name()));
        messageUtils.sendMessage(sender, messageManager.getDebugDatabaseType().replace("{value}", settings.getDatabaseType().name()));
        messageUtils.sendMessage(sender, messageManager.getDebugAllowedSymbols().replace("{value}", String.valueOf(settings.getAllowedSymbols())));
        messageUtils.sendMessage(sender, messageManager.getDebugCooldown().replace("{value}", String.valueOf(settings.getCommandCooldown())));
        messageUtils.sendMessage(sender, messageManager.getDebugJoinEnabled().replace("{value}", String.valueOf(settings.isJoinEnabled())));
        messageUtils.sendMessage(sender, messageManager.getDebugFirstJoinEnabled().replace("{value}", String.valueOf(settings.isFirstJoinEnabled())));
        messageUtils.sendMessage(sender, messageManager.getDebugQuitEnabled().replace("{value}", String.valueOf(settings.isQuitEnabled())));
        messageUtils.sendMessage(sender, messageManager.getDebugPlaceholderApi().replace("{value}", String.valueOf(placeholderEnabledSupplier.getAsBoolean())));
    }
}
