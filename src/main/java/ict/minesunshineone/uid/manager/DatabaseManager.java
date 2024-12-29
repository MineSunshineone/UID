package ict.minesunshineone.uid.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ict.minesunshineone.uid.UIDPlugin;
import ict.minesunshineone.uid.exception.DatabaseException;

public class DatabaseManager implements AutoCloseable {

    private final UIDPlugin plugin;
    private HikariDataSource dataSource;
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    public DatabaseManager(UIDPlugin plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            FileConfiguration config = plugin.getConfig();
            HikariConfig hikariConfig = new HikariConfig();

            // 从配置中获取数据库连接信息
            String host = config.getString("database.host", "localhost");
            int port = config.getInt("database.port", 3306);
            String dbName = config.getString("database.name", "uidplugin");
            String parameters = config.getString("database.parameters", "useSSL=false&allowPublicKeyRetrieval=true");

            // 构建JDBC URL
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?%s",
                    host, port, dbName, parameters);

            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(config.getString("database.username", "root"));
            hikariConfig.setPassword(config.getString("database.password", ""));
            hikariConfig.setMaximumPoolSize(config.getInt("database.pool-size", 10));

            // 连接池优化
            hikariConfig.setMinimumIdle(config.getInt("database.min-idle", 5));
            hikariConfig.setIdleTimeout(300000); // 5分钟
            hikariConfig.setMaxLifetime(600000); // 10分钟
            hikariConfig.setConnectionTimeout(30000); // 30秒
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(hikariConfig);
            createTables();
            logger.info("数据库连接已成功建立");
        } catch (Exception e) {
            logger.severe(String.format("初始化数据库失败: %s", e.getMessage()));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_uids (
                    uuid VARCHAR(36) PRIMARY KEY,
                    uid BIGINT UNIQUE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

        } catch (SQLException e) {
            logger.severe(String.format("创建数据表失败: %s", e.getMessage()));
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    public void saveUID(UUID playerUUID, long uid) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO player_uids (uuid, uid) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE last_seen = CURRENT_TIMESTAMP")) {

            ps.setString(1, playerUUID.toString());
            ps.setLong(2, uid);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.severe(String.format("保存玩家 %s 的UID失败: %s", playerUUID, e.getMessage()));
            throw new DatabaseException("保存UID失败", e);
        }
    }

    public Long getUID(UUID playerUUID) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT uid FROM player_uids WHERE uuid = ?")) {

            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("uid");
            }
            return null;

        } catch (SQLException e) {
            logger.severe(String.format("获取UID失败: %s", e.getMessage()));
            return null;
        }
    }

    public int getTotalUIDCount() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM player_uids")) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            logger.severe(String.format("获取UID总数失败: %s", e.getMessage()));
            return 0;
        }
    }

    public void checkPoolHealth() {
        if (dataSource != null) {
            int active = dataSource.getHikariPoolMXBean().getActiveConnections();
            int idle = dataSource.getHikariPoolMXBean().getIdleConnections();
            int total = dataSource.getHikariPoolMXBean().getTotalConnections();

            logger.info(String.format("连接池状态 - 活动: %d, 空闲: %d, 总计: %d",
                    active, idle, total));

            if (active == total && total >= dataSource.getMaximumPoolSize()) {
                logger.warning("连接池已达到最大容量!");
            }
        }
    }

    public void saveUIDs(Map<UUID, Long> uidMap) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO player_uids (uuid, uid) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE last_seen = CURRENT_TIMESTAMP")) {

            conn.setAutoCommit(false);
            for (Map.Entry<UUID, Long> entry : uidMap.entrySet()) {
                ps.setString(1, entry.getKey().toString());
                ps.setLong(2, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            logger.severe(String.format("批量保存UID失败: %s", e.getMessage()));
            throw new DatabaseException("批量保存UID失败", e);
        }
    }

    public Map<UUID, Long> getUIDs(Collection<UUID> playerUUIDs) {
        Map<UUID, Long> result = new HashMap<>();
        if (playerUUIDs.isEmpty()) {
            return result;
        }

        String sql = "SELECT uuid, uid FROM player_uids WHERE uuid IN ("
                + String.join(",", Collections.nCopies(playerUUIDs.size(), "?")) + ")";

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            for (UUID uuid : playerUUIDs) {
                ps.setString(index++, uuid.toString());
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                long uid = rs.getLong("uid");
                result.put(uuid, uid);
            }

        } catch (SQLException e) {
            logger.severe(String.format("批量获取UID失败: %s", e.getMessage()));
        }
        return result;
    }

    public boolean isUIDExists(long uid) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM player_uids WHERE uid = ?")) {
            ps.setLong(1, uid);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.severe(String.format("检查UID是否存在时发生错误: %s", e.getMessage()));
            return false;
        }
    }

    public boolean setUID(UUID playerUUID, long newUID) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "UPDATE player_uids SET uid = ? WHERE uuid = ?")) {
            ps.setLong(1, newUID);
            ps.setString(2, playerUUID.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe(String.format("设置UID失败: %s", e.getMessage()));
            return false;
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
