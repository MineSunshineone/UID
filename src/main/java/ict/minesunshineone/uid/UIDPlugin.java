package ict.minesunshineone.uid;

import org.bukkit.plugin.java.JavaPlugin;

import ict.minesunshineone.uid.command.UIDCommand;
import ict.minesunshineone.uid.hook.PlaceholderAPIExpansion;
import ict.minesunshineone.uid.listener.PlayerListener;
import ict.minesunshineone.uid.manager.DatabaseManager;
import ict.minesunshineone.uid.manager.UIDManager;
import ict.minesunshineone.uid.monitoring.PerformanceMonitor;
import ict.minesunshineone.uid.util.MessageManager;
import ict.minesunshineone.uid.util.UIDGenerator;

public class UIDPlugin extends JavaPlugin {

    private UIDManager uidManager;
    private DatabaseManager dbManager;
    private MessageManager messageManager;
    private PerformanceMonitor performanceMonitor;
    private UIDGenerator uidGenerator;
    private static UIDPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // 加载配置文件
        saveDefaultConfig();

        // 初始化数据库连接
        dbManager = new DatabaseManager(this);

        // 初始化消息管理器
        messageManager = new MessageManager(this);

        // 初始化性能监控
        performanceMonitor = new PerformanceMonitor(this);

        // 初始化UID生成器
        uidGenerator = new UIDGenerator(this);

        // 初始化UID管理器
        uidManager = new UIDManager(this);

        // 注册命令
        registerCommands();

        // 注册事件监听器
        registerListeners();

        // 注册PAPI扩展
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIExpansion(this).register();
            getLogger().info("已成功挂钩 PlaceholderAPI!");
        }

        // 设置定期缓存清理任务
        long cleanupInterval = getConfig().getLong("performance.cache-cleanup-interval", 15) * 1200L; // 15分钟
        getServer().getScheduler().runTaskTimerAsynchronously(this,
                () -> uidManager.cleanupCache(),
                cleanupInterval,
                cleanupInterval
        );

        getLogger().info("UID插件已启用!");
    }

    @Override
    public void onDisable() {
        // 关闭数据库连接
        if (dbManager != null) {
            dbManager.close();
        }

        // 停止性能监控任务
        if (performanceMonitor != null) {
            performanceMonitor.close();
        }

        getLogger().info("UID插件已禁用!");
    }

    private void registerCommands() {
        var command = getCommand("uid");
        if (command != null) {
            command.setExecutor(new UIDCommand(this));
        } else {
            getLogger().warning("无法注册'uid'命令 - 请检查plugin.yml中是否已定义");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public static UIDPlugin getInstance() {
        return instance;
    }

    public UIDManager getUIDManager() {
        return uidManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public UIDGenerator getUIDGenerator() {
        return uidGenerator;
    }
}
