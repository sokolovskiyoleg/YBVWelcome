package org.yabogvk.ybvwelcome.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.YBVWelcome;
import org.yabogvk.ybvwelcome.utils.MessageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageManager {

    private final YBVWelcome plugin;
    private final Map<String, String> cachedMessages = new HashMap<>();
    private final Map<String, List<String>> cachedLists = new HashMap<>();
    private final Map<String, Map<String, String>> cachedGroupMessages = new HashMap<>();
    private boolean groupMessagesEnabled;

    public MessageManager(YBVWelcome plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getMessagesConfig();
        cachedMessages.clear();
        cachedLists.clear();
        cachedGroupMessages.clear();

        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                cachedMessages.put(key, MessageUtils.colorize(config.getString(key)));
            }
        }

        for (String key : config.getKeys(true)) {
            if (config.isList(key)) {
                cachedLists.put(key, config.getStringList(key).stream()
                        .map(MessageUtils::colorize)
                        .collect(Collectors.toList()));
            }
        }

        groupMessagesEnabled = config.getBoolean("group-messages.enabled", false);
        if (groupMessagesEnabled) {
            ConfigurationSection groups = config.getConfigurationSection("group-messages.list");
            if (groups != null) {
                for (String groupName : groups.getKeys(false)) {
                    ConfigurationSection groupSection = groups.getConfigurationSection(groupName);
                    if (groupSection != null) {
                        Map<String, String> groupData = new HashMap<>();
                        groupData.put("permission", groupSection.getString("permission"));
                        groupData.put("join", MessageUtils.colorize(groupSection.getString("join")));
                        groupData.put("quit", MessageUtils.colorize(groupSection.getString("quit")));
                        cachedGroupMessages.put(groupName, groupData);
                    }
                }
            }
        }
    }

    private String get(String path, String defaultValue) {
        return cachedMessages.getOrDefault(path, defaultValue);
    }

    private List<String> getList(String path) {
        return cachedLists.getOrDefault(path, Collections.emptyList());
    }

    public String getGroupMessage(Player player, String type) {
        if (!groupMessagesEnabled) return null;

        for (Map.Entry<String, Map<String, String>> entry : cachedGroupMessages.entrySet()) {
            Map<String, String> groupData = entry.getValue();
            String perm = groupData.get("permission");

            if (perm == null || perm.isEmpty() || player.hasPermission(perm)) {
                return groupData.get(type);
            }
        }
        return null;
    }

    public List<String> getRandomWelcomes() {
        return getList("random-welcomes");
    }

    public String getWelcomeButtonText() {
        return get("welcome-button.text", " [ПРИВЕТСТВОВАТЬ]");
    }

    public String getWelcomeButtonHover() {
        return get("welcome-button.hover", "Нажми сюда!");
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
