package ict.minesunshineone.uid.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.bukkit.scheduler.BukkitTask;

import ict.minesunshineone.uid.UIDPlugin;

public class PerformanceMonitor {

    private final UIDPlugin plugin;
    private final Map<String, OperationStats> stats;
    private final int maxSampleSize;
    private final int warningThreshold;
    private final BukkitTask resetTask;

    public PerformanceMonitor(UIDPlugin plugin) {
        this.plugin = plugin;
        this.stats = new ConcurrentHashMap<>();
        this.maxSampleSize = plugin.getConfig().getInt("monitoring.max-samples", 1000);
        this.warningThreshold = plugin.getConfig().getInt("monitoring.warning-threshold", 50);

        // 设置定期重置任务
        int resetInterval = plugin.getConfig().getInt("monitoring.reset-interval", 60);
        this.resetTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::resetStats,
                resetInterval * 1200L, // 转换为ticks
                resetInterval * 1200L
        );
    }

    public <T> T measure(String operation, Supplier<T> task) {
        long start = System.nanoTime();
        try {
            return task.get();
        } finally {
            long duration = System.nanoTime() - start;
            recordMetric(operation, duration / 1_000_000.0); // 转换为毫秒
        }
    }

    public void measure(String operation, Runnable task) {
        measure(operation, () -> {
            task.run();
            return null;
        });
    }

    private void recordMetric(String operation, double duration) {
        stats.computeIfAbsent(operation, k -> new OperationStats(maxSampleSize))
                .addSample(duration);
        checkPerformance(operation, duration);
    }

    public CompletableFuture<Map<String, OperationStats>> getStats() {
        return CompletableFuture.supplyAsync(() -> new HashMap<>(stats));
    }

    public void resetStats() {
        stats.clear();
    }

    private void checkPerformance(String operation, double duration) {
        if (duration > warningThreshold) {
            plugin.getLogger().warning(String.format(
                    "性能警告: 操作 %s 耗时 %.2f ms, 超过警告阈值 %d ms",
                    operation, duration, warningThreshold
            ));
        }
    }

    public void close() {
        if (resetTask != null) {
            resetTask.cancel();
        }
    }
}
