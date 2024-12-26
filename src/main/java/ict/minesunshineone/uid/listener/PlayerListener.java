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

        // 使用异步调度器
        plugin.getServer().getAsyncScheduler().runNow(plugin, (task) -> {
            uidManager.getUID(player.getUniqueId()).thenAccept(optionalUid -> {
                if (optionalUid.isEmpty()) {
                    uidManager.generateUID(player).thenAccept(uid -> {
                        // 使用区域调度器发送消息
                        player.getScheduler().run(plugin, (messageTask)
                                -> player.sendMessage(messageManager.getMessage("uid-generated-notify", "zh_CN", "uid", uid)),
                                () -> plugin.getLogger().warning(String.format("发送UID消息失败: %s", player.getName()))
                        );
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
