package org.yabogvk.ybvwelcome.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final YBVWelcome plugin;
    private FileConfiguration config;

    private final Map<String, String> cachedMessages = new HashMap<>();

    public MessageManager(YBVWelcome plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.config = plugin.getMessagesConfig();
        cachedMessages.clear();
    }

    private String get(String path, String defaultValue) {
        return cachedMessages.computeIfAbsent(path, key -> {
            String raw = config.getString(key, defaultValue);
            return MessageUtils.colorize(raw);
        });
    }


    public String getGroupMessage(Player player, String type) {
        if (!config.getBoolean("group-messages.enabled", false)) return null;

        ConfigurationSection groups = config.getConfigurationSection("group-messages.list");
        if (groups == null) return null;

        for (String key : groups.getKeys(false)) {
            String perm = groups.getString(key + ".permission");

            if (perm == null || perm.isEmpty() || player.hasPermission(perm)) {
                String raw = groups.getString(key + "." + type);
                return MessageUtils.colorize(raw);
            }
        }
        return null;
    }

    public String getJoinDefault() { return get("join.default", "&fприсоединился"); }

    public String getQuitDefault() { return get("quit.default", "&fвышел"); }

    public String getNoPermissions() { return get("commands.no-permissions", "&8[&c&l!&8] &7У вас &cнет прав!"); }

    public String getOnlyForPlayers() { return get("commands.only-for-players", "&cЭту команду может использовать только игрок!"); }

    public String getReloadSuccess() { return get("commands.reload-success", "&8[&a&l!&8] &7Конфигурация &aуспешно &7перезагружена!"); }

    public String getSetSuccess() { return get("commands.set-success", "&8[&a&l!&8] &7Сообщение &aсохранено&7!"); }

    public String getClearSuccess() { return get("commands.clear-success", "&8[&a&l!&8] &7Сообщение &aудалено&7!"); }

    public String getClearNoMessage() {
        return get("commands.clear-no-message", "&8[&c&l!&8] &7У Вас &cнет &7сохраненного сообщения!");
    }

    public String getErrorDatabase() { return get("commands.error-database", "&8[&c&l!&8] &7Ошибка базы данных!"); }

    public String getError() {
        return get("commands.error", "&8[&c&l!&8] &7Ошибка. Сообщите администратору!");
    }

    public String getToManySymbols() { return get("commands.to-many-symbols", "&8[&c&l!&8] &7Слишком много символов!"); }


    public String getUsage() { return get("commands.usage", "&6Использование: &e/ybvwelcome &7[set|clear|reload]"); }

    public String getUsageSet() { return get("commands.usageset", "&6Использование: &e/ybvwelcome set &7[join|quit] <сообщение>"); }

    public String getUsageClear() { return get("commands.usageclear", "&6Использование: &e/ybvwelcome clear &7[join|quit]"); }

    public String getFormatJoinCustom() { return get("formats.join.custom", "&8[&a+&8] &f{player}: &f{message}"); }
    public String getFormatJoinDefault() { return get("formats.join.default", "&8[&a+&8] &f{player} {message}"); }

    public String getFormatQuitCustom() { return get("formats.quit.custom", "&8[&c-&8] &f{player}: &f{message}"); }
    public String getFormatQuitDefault() { return get("formats.quit.default", "&8[&c-&8] &f{player} {message}"); }

    public String getFirstJoin() { return get("formats.first_join", "&f{player} &fприсоединился впервые!"); }

}