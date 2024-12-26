# 🎮 ICTUID Plugin

> 一个简单但强大的 Minecraft 玩家 UID 管理插件! 让玩家管理变得更轻松!

## ✨ 这是什么?

ICTUID 是一个专为 Minecraft 服务器设计的玩家 UID 管理插件。它能高效地为玩家生成和管理唯一标识符，是服务器管理和数据追踪的得力助手。

## 🚀 主要特性

### 🎯 **智能 UID 系统**
- **灵活的分配策略（随机/顺序）**
- **智能的自动分配机制**
- **高度可配置的格式选项**
- **严格的唯一性校验**
- **批量导入导出支持**

### 💾 **企业级数据管理**
- **多数据库支持（MySQL/SQLite）**
- **高性能缓存系统**
- **全异步数据操作**
- **定时备份机制**
- **便捷的数据迁移**

### 🛠 **全方位管理工具**
- **直观的命令系统**
- **细粒度权限控制**
- **实时性能监控**
- **网页管理面板**
- **批量处理工具**

### 🌈 **极致开发体验**
- **完整的 API 支持**
- **丰富的事件系统**
- **PlaceholderAPI 集成**
- **Folia 全面兼容**
- **详尽的开发文档**

## 📦 安装

1. **下载最新版本的 ICTUID.jar**
2. **放入服务器的 `plugins` 文件夹**
3. **重启服务器**
4. **完成! 插件会自动生成配置文件**

## ⚙️ 主要配置

### 🎯 UID 生成设置

```yaml
uid:
  # 获得随机UID的玩家数量 (1-100)
  random-allocation-count: 100
  # 随机UID范围 (必须在1-9999之间)
  random-min: 1
  random-max: 100
  # 顺序UID的起始数字 (必须大于random-max)
  sequential-start: 10000
  # UID格式设置
  format:
    digits: 4 # 显示的位数
    pad-char: "0" # 用于补位的字符
```

### 💾 数据库设置

```yaml
database:
  host: "localhost"
  port: 3306
  name: "uidplugin"
  username: "root"
  password: "password"
  pool-size: 10
  parameters: "useSSL=false&allowPublicKeyRetrieval=true"

```
### ⚡性能优化

```yaml
performance:
  # 缓存时长(分钟)
  cache-duration: 30
  # 最大缓存数量
  max-cache-size: 1000
  # 缓存清理间隔(分钟)
  cache-cleanup-interval: 15
  # 数据库连接池监控间隔(分钟)
  pool-health-check-interval: 30
```

### 📊 性能监控
内置性能监控系统，帮助你实时了解插件运行状况：
```yaml
monitoring:
  # 是否启用性能监控
  enabled: true
  # 性能数据采样上限
  max-samples: 1000
  # 性能警告阈值(毫秒)
  warning-threshold: 50
  # 统计重置间隔(分钟)
  reset-interval: 60
```
### 🎨 消息自定义
```yaml
# 消息前缀
prefix: "&b&l『ICTUID』&r"
# 系统消息
no-permission: "%prefix% &c你没有权限执行此命令"
player-not-found: "%prefix% &c找不到该玩家"
# UID相关消息
uid-info: "%prefix% &a玩家 &e%player% &a的UID是: &b%uid%"
uid-generated: "%prefix% &a已为玩家 &e%player% &a生成新UID: &b%uid%"
uid-generated-notify: "%prefix% &a你的新UID是: &b%uid%"
# 性能统计消息
stats-header: "%prefix% &6=== 性能统计 ==="
stats-line: "%prefix% &7%operation%: &e%avg%ms &7(次数: &e%count%&7)"
# 帮助消息
help-header: "%prefix% &6=== UID插件帮助 ==="
help-get: "%prefix% &e/uid get [玩家名] &7- 查看UID"
help-generate: "%prefix% &e/uid generate [玩家名] &7- 生成新UID"
help-stats: "%prefix% &e/uid stats &7- 查看性能统计"
```
### 颜色代码说明

| 代码 | 颜色 | 用途 |
|------|------|------|
| `&a` | 绿色 | 成功消息 |
| `&b` | 天蓝色 | 插件前缀 |
| `&c` | 红色 | 错误消息 |
| `&e` | 黄色 | 重要信息 |
| `&7` | 灰色 | 普通文本 |
| `&l` | 加粗 | 强调文本 |
| `&r` | 重置 | 清除格式 |

### 变量说明

| 变量 | 描述 |
|------|------|
| `%prefix%` | 插件消息前缀 |
| `%player%` | 玩家名称 |
| `%uid%` | 玩家的 UID |
| `%operation%` | 操作名称 |
| `%avg%` | 平均响应时间 |
| `%count%` | 操作次数 |

## 🎯 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/uid get [玩家名]` | 查看玩家的 UID | uidplugin.get |
| `/uid generate [玩家名]` | 为玩家生成新 UID | uidplugin.generate |
| `/uid stats` | 查看性能统计 | uidplugin.stats |

## 🔌 PlaceholderAPI 变量

使用 `%uid_id%` 在任何支持 PAPI 的地方显示玩家的 UID!


## 🔧 开发计划

- [ ] Web 管理面板
- [ ] 更多数据库支持
- [ ] API 接口开放
- [ ] 更多语言支持

## 🤝 贡献

欢迎提交 Issue 和 Pull Request!

## 📝 许可证

本项目采用 MIT 许可证

## 👨‍💻 作者

Made with ❤️ by MineSunshineone

---

如果觉得这个插件对你有帮助,请给个 ⭐️ 支持一下!

