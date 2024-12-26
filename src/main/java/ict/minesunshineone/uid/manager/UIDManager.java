package ict.minesunshineone.uid.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;

import ict.minesunshineone.uid.UIDPlugin;
import ict.minesunshineone.uid.exception.UIDGenerationException;
import ict.minesunshineone.uid.monitoring.PerformanceMonitor;

public class UIDManager {

    private final UIDPlugin plugin;
    private final Map<UUID, CachedUID> uidCache;
    private final DatabaseManager dbManager;
    private final PerformanceMonitor performanceMonitor;
    private final long cacheDuration;
    private final AtomicInteger totalUIDCount;
    private volatile long lastCountUpdateTime;
    private static final long COUNT_CACHE_DURATION = 60000; // 1分钟更新一次计数
    private final Object uidGenerationLock = new Object(); // 添加锁对象

    private static class CachedUID {

        final long uid;
        final long timestamp;

        CachedUID(long uid) {
            this.uid = uid;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(long duration) {
            return System.currentTimeMillis() - timestamp > duration;
        }
    }

    public UIDManager(UIDPlugin plugin) {
        this.plugin = plugin;
        this.uidCache = new ConcurrentHashMap<>();
        this.dbManager = plugin.getDatabaseManager();
        this.performanceMonitor = plugin.getPerformanceMonitor();
        this.cacheDuration = plugin.getConfig().getLong("performance.cache-duration", 30) * 60 * 1000;
        this.totalUIDCount = new AtomicInteger(0);
        this.lastCountUpdateTime = 0;
        // 初始化时更新一次计数
        updateTotalUIDCount();
    }

    private void updateTotalUIDCount() {
        long now = System.currentTimeMillis();
        if (now - lastCountUpdateTime > COUNT_CACHE_DURATION) {
            synchronized (uidGenerationLock) { // 使用相同的锁
                if (now - lastCountUpdateTime > COUNT_CACHE_DURATION) {
                    totalUIDCount.set(dbManager.getTotalUIDCount());
                    lastCountUpdateTime = now;
                }
            }
        }
    }

    public CompletableFuture<Long> generateUID(Player player) {
        return CompletableFuture.supplyAsync(()
                -> performanceMonitor.measure("uid.generate", () -> {
                    synchronized (uidGenerationLock) { // 添加同步块
                        try {
                            // 在锁内重新获取最新计数
                            updateTotalUIDCount();
                            int currentCount = totalUIDCount.getAndIncrement();
                            long uid = plugin.getUIDGenerator().generateUID(currentCount);

                            // 先尝试保存到数据库
                            try {
                                dbManager.saveUID(player.getUniqueId(), uid);
                            } catch (Exception e) {
                                // 如果保存失败，回滚计数并抛出异常
                                totalUIDCount.decrementAndGet();
                                throw e;
                            }

                            // 保存成功后更新缓存
                            uidCache.put(player.getUniqueId(), new CachedUID(uid));
                            return uid;
                        } catch (Exception e) {
                            plugin.getLogger().severe(String.format("生成UID失败: %s", e.getMessage()));
                            throw new RuntimeException("生成UID失败", e);
                        }
                    }
                })
        );
    }

    public CompletableFuture<Optional<Long>> getUID(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            // 首先检查缓存
            CachedUID cached = uidCache.get(playerUUID);
            if (cached != null && !cached.isExpired(cacheDuration)) {
                return Optional.of(cached.uid);
            }

            // 如果缓存未命中或已过期,则查询数据库
            Long dbUid = dbManager.getUID(playerUUID);
            if (dbUid != null) {
                uidCache.put(playerUUID, new CachedUID(dbUid));
                return Optional.of(dbUid);
            }
            return Optional.empty();
        });
    }

    // 批量生成UID的方法
    public CompletableFuture<Map<UUID, Long>> generateUIDs(Collection<Player> players) {
        return CompletableFuture.supplyAsync(()
                -> performanceMonitor.measure("uid.generate.batch", () -> {
                    Map<UUID, Long> uidMap = new HashMap<>();
                    int totalUIDs = dbManager.getTotalUIDCount();

                    for (Player player : players) {
                        try {
                            long uid = plugin.getUIDGenerator().generateUID(totalUIDs + uidMap.size());
                            uidMap.put(player.getUniqueId(), uid);
                            uidCache.put(player.getUniqueId(), new CachedUID(uid));
                        } catch (UIDGenerationException e) {
                            plugin.getLogger().severe(String.format("为玩家 %s 生成UID失败: %s",
                                    player.getName(), e.getMessage()));
                        }
                    }

                    if (!uidMap.isEmpty()) {
                        dbManager.saveUIDs(uidMap);
                    }

                    return uidMap;
                })
        );
    }

    // 清理过期缓存
    public void cleanupCache() {
        uidCache.entrySet().removeIf(entry -> entry.getValue().isExpired(cacheDuration));
    }

    // 定期更新总数缓存的任务
    public void startCountUpdateTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin,
                (task) -> updateTotalUIDCount(),
                1200L, // 1分钟后开始
                1200L // 每1分钟执行一次
        );
    }
}
