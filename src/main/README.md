# 🎮 ICTUID Plugin

> 一个简单但强大的 Minecraft 玩家 UID 管理插件! 让玩家管理变得更轻松!

## ✨ 这是什么?

ICTUID 是一个专为 Minecraft 服务器设计的玩家 UID 管理插件。它能高效地为玩家生成和管理唯一标识符,是服务器管理和数据追踪的得力助手。

## 🚀 主要特性

- 🎯 **智能 UID 系统**
  - 灵活的分配策略(随机/顺序)
  - 智能的自动分配机制
  - 高度可配置的格式选项
  - 严格的唯一性校验
  - 批量导入导出支持

- 💾 **企业级数据管理**
  - 多数据库支持(MySQL/SQLite)
  - 高性能缓存系统
  - 全异步数据操作
  - 定时备份机制
  - 便捷的数据迁移

- 🛠 **全方位管理工具**
  - 直观的命令系统
  - 细粒度权限控制
  - 实时性能监控
  - 网页管理面板
  - 批量处理工具

- 🌈 **极致开发体验**
  - 完整的 API 支持
  - 丰富的事件系统
  - PlaceholderAPI 集成
  - Folia 全面兼容
  - 详尽的开发文档

## 📦 安装

1. 下载最新版本的 ICTUID.jar
2. 放入服务器的 plugins 文件夹
3. 重启服务器
4. 完成! 插件会自动生成配置文件

## ⚙️ 配置

### 基础配置

## 🎯 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/uid get [玩家名]` | 查看玩家的 UID | uidplugin.get |
| `/uid generate [玩家名]` | 为玩家生成新 UID | uidplugin.generate |
| `/uid stats` | 查看性能统计 | uidplugin.stats |

## 🔌 PlaceholderAPI 变量

使用 `%uid_id%` 在任何支持 PAPI 的地方显示玩家的 UID!

## 📊 性能监控

内置性能监控系统,帮助你实时了解插件运行状况:

monitoring:
  enabled: true           # 启用性能监控
  max-samples: 1000       # 采样数量
  warning-threshold: 50   # 警告阈值(ms)
  reset-interval: 60      # 重置间隔(分钟)

## 🎨 消息自定义

所有消息都可以在配置文件中自定义,支持颜色代码:
messages:
  prefix: "&8[&bICTUID&8]&r"
  no-permission: "%prefix% &c你没有权限执行此命令"
  player-not-found: "%prefix% &c找不到该玩家"
  uid-get: "%prefix% &7玩家 &e%player% &7的UID是: &b%uid%"
  uid-generate: "%prefix% &7已为玩家 &e%player% &7生成新UID: &b%uid%"
  stats: "%prefix% &7性能统计:\n&7- 缓存命中率: &a%hit_rate%%\n&7- 平均响应时间: &e%avg_time%ms"
  reload: "%prefix% &a配置重载完成"
  error: "%prefix% &c发生错误: %error%"



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



