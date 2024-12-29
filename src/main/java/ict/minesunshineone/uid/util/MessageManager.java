package ict.minesunshineone.uid.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import ict.minesunshineone.uid.UIDPlugin;

public class MessageManager {

    private final UIDPlugin plugin;
    private final Map<String, Map<String, String>> messages;
    private final String defaultLocale;

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

    public String getMessage(String key, String locale, String... args) {
        String message = messages.getOrDefault(locale, messages.get("zh_CN")).get(key);
        if (message == null) {
            plugin.getLogger().warning(String.format("未找到消息键: %s", key));
            return "消息未找到";
        }

        // 首先替换前缀
        message = message.replace("%prefix%", messages.get(locale).get("prefix"));

        // 然后处理其他占位符
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("参数必须是键值对");
        }

        for (int i = 0; i < args.length; i += 2) {
            String placeholder = "%" + args[i] + "%";
            String value = String.valueOf(args[i + 1]);
            message = message.replace(placeholder, value);
        }

        // 使用 Vault 的颜色代码处理
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }

    public void reloadMessages(UIDPlugin plugin) {
        messages.clear();
        loadMessages(plugin);
    }

    // 获取所有支持的语言
    public String[] getSupportedLocales() {
        return messages.keySet().toArray(String[]::new);
    }
}
