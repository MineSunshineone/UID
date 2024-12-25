package ict.minesunshineone.uid.hook;

import org.bukkit.entity.Player;

import ict.minesunshineone.uid.UIDPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final UIDPlugin plugin;

    public PlaceholderAPIExpansion(UIDPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "uid";
    }

    @Override
    public String getAuthor() {
        return "你的名字";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("id")) {
            return plugin.getUIDManager().getUID(player.getUniqueId())
                    .thenApply(optionalUid -> optionalUid
                    .map(uid -> plugin.getUIDGenerator().formatUID(uid))
                    .orElse("未分配"))
                    .join();
        }

        return null;
    }
}
