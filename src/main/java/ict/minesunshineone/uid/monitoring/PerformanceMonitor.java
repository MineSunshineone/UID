package ict.minesunshineone.uid.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import ict.minesunshineone.uid.UIDPlugin;

public class PerformanceMonitor {

    private final UIDPlugin plugin;
    private final Map<String, OperationStats> stats;
    private final int maxSampleSize;
    private final long warningThreshold;
    private final io.papermc.paper.threadedregions.scheduler.ScheduledTask resetTask;

    public PerformanceMonitor(UIDPlugin plugin) {
        this.plugin = plugin;
        this.stats = new ConcurrentHashMap<>();
        this.maxSampleSize = plugin.getConfig().getInt("monitoring.max-samples", 1000);
        this.warningThreshold = plugin.getConfig().getLong("monitoring.warning-threshold", 50);

        // 使用全局区域调度器替代异步调度器
        long resetInterval = plugin.getConfig().getLong("monitoring.reset-interval", 60) * 1200L;
        this.resetTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin,
                (task) -> resetStats(),
                resetInterval,
                resetInterval
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
