package org.yabogvk.ybvwelcome.commands;

import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
    YBVWelcome instance = YBVWelcome.getInstance();

    public AbstractCommand(String command){

        PluginCommand pluginCommand = instance.getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    public void registerAliases(String... aliases) {
        for (String alias : aliases) {
            Command aliasCommand = new CommandWrapper(alias);
            instance.getServer().getCommandMap().register("ybvwelcome", aliasCommand);
        }
    }

    private class CommandWrapper extends Command {
        public CommandWrapper(String name) {
            super(name);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return onCommand(sender, this, label, args);
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            List<String> completions = onTabComplete(sender, this, alias, args);
            return completions != null ? completions : super.tabComplete(sender, alias, args);
        }
    }

    public abstract void execute(CommandSender sender, String label, String[] args);

    public List<String> complete(CommandSender sender, String ... args){
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        execute(sender, label, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return filter(complete(sender, args), args);
    }

    private List<String> filter(List<String> list, String ... args){
        if (list == null) return null;
        String last = args[args.length - 1];
        List<String> result = new ArrayList<>();
        for (String arg : list) {
            if (arg.toLowerCase().startsWith(last.toLowerCase())) result.add(arg);
        }
        return result;
    }
}