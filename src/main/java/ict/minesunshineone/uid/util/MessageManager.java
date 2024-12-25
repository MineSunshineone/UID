package ict.minesunshineone.uid.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;

import ict.minesunshineone.uid.UIDPlugin;

public class MessageManager {

    private final UIDPlugin plugin;
    private final Map<String, Map<String, String>> messages;
    private final String defaultLocale;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    public MessageManager(UIDPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.defaultLocale = "zh_CN";
        loadMessages(plugin);
    }

    private void loadMessages(UIDPlugin plugin) {
        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection == null) {
            plugin.getLogger().warning("配置文件中未找到消息配置部分");
            return;
        }

        Map<String, String> localeMessages = new HashMap<>();
        for (String key : messagesSection.getKeys(false)) {
            String message = messagesSection.getString(key);
            if (message != null) {
                localeMessages.put(key, net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacyAmpersand().serialize(
                                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                                        .legacyAmpersand().deserialize(message)));
            }
        }
        messages.put(defaultLocale, localeMessages);
    }

    public String getMessage(String key, String locale, Object... args) {
        Map<String, String> localeMessages = messages.getOrDefault(locale, messages.get(defaultLocale));
        if (localeMessages == null) {
            return String.format("消息未找到: %s", key);
        }

        String message = localeMessages.getOrDefault(key, String.format("未知消息键: %s", key));
        if (args.length == 0) {
            return message;
        }

        // 替换占位符
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("参数必须是键值对");
        }

        String result = message;
        for (int i = 0; i < args.length; i += 2) {
            String placeholder = "%" + args[i] + "%";
            String value = String.valueOf(args[i + 1]);
            result = result.replace(placeholder, value);
        }

        // 检查是否有未替换的占位符
        if (hasUnreplacedPlaceholders(result)) {
            plugin.getLogger().warning(String.format("消息 '%s' 包含未替换的占位符: %s", key, result));
        }

        return result;
    }

    public void reloadMessages(UIDPlugin plugin) {
        messages.clear();
        loadMessages(plugin);
    }

    // 检查消息中是否有未替换的占位符
    private boolean hasUnreplacedPlaceholders(String message) {
        return PLACEHOLDER_PATTERN.matcher(message).find();
    }

    // 获取所有支持的语言
    public String[] getSupportedLocales() {
        return messages.keySet().toArray(String[]::new);
    }
}
