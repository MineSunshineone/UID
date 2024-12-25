package ict.minesunshineone.uid.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ict.minesunshineone.uid.UIDPlugin;
import ict.minesunshineone.uid.manager.UIDManager;
import ict.minesunshineone.uid.util.MessageManager;

public class PlayerListener implements Listener {

    private final UIDPlugin plugin;
    private final UIDManager uidManager;
    private final MessageManager messageManager;

    public PlayerListener(UIDPlugin plugin) {
        this.plugin = plugin;
        this.uidManager = plugin.getUIDManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 异步检查和生成UID
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            uidManager.getUID(player.getUniqueId()).thenAccept(optionalUid -> {
                if (optionalUid.isEmpty()) {
                    uidManager.generateUID(player).thenAccept(uid -> {
                        player.sendMessage(messageManager.getMessage("uid-generated-notify", "zh_CN", "uid", uid));
                    });
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 可以在这里添加玩家退出时的清理逻辑
        // 比如清除缓存等
    }
}
