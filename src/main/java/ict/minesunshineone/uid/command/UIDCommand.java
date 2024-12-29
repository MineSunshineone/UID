package ict.minesunshineone.uid.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ict.minesunshineone.uid.UIDPlugin;
import ict.minesunshineone.uid.manager.UIDManager;
import ict.minesunshineone.uid.monitoring.PerformanceMonitor;
import ict.minesunshineone.uid.util.MessageManager;

public class UIDCommand implements CommandExecutor, TabCompleter {

    private final UIDPlugin plugin;
    private final UIDManager uidManager;
    private final MessageManager messageManager;
    private final PerformanceMonitor performanceMonitor;

    public UIDCommand(UIDPlugin plugin) {
        this.plugin = plugin;
        this.uidManager = plugin.getUIDManager();
        this.messageManager = plugin.getMessageManager();
        this.performanceMonitor = plugin.getPerformanceMonitor();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        // 使用异步调度器替代runTaskAsynchronously
        plugin.getServer().getAsyncScheduler().runNow(plugin, (task) -> {
            try {
                performanceMonitor.measure("command." + args[0], () -> {
                    switch (args[0].toLowerCase()) {
                        case "get" ->
                            handleGet(sender, args);
                        case "generate" ->
                            handleGenerate(sender, args);
                        case "stats" ->
                            handleStats(sender);
                        case "set" ->
                            handleSet(sender, args);
                        default ->
                            sendHelpMessage(sender);
                    }
                    return null;
                });
            } catch (Exception e) {
                sender.sendMessage(messageManager.getMessage("error-executing", "zh_CN"));
                plugin.getLogger().severe(String.format("执行命令时发生错误: %s", e.getMessage()));
            }
        });

        return true;
    }

    private void handleGet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uidplugin.get")) {
            sender.sendMessage(messageManager.getMessage("no-permission", "zh_CN"));
            return;
        }

        Player target;
        if (args.length > 1) {
            target = plugin.getServer().getPlayer(args[1]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(messageManager.getMessage("player-required", "zh_CN"));
            return;
        }

        if (target == null) {
            sender.sendMessage(messageManager.getMessage("player-not-found", "zh_CN"));
            return;
        }

        uidManager.getUID(target.getUniqueId())
                .thenAccept(optionalUid -> {
                    if (sender instanceof Player senderPlayer) {
                        senderPlayer.getScheduler().run(plugin,
                                (task) -> optionalUid.ifPresentOrElse(
                                        uid -> sender.sendMessage(messageManager.getMessage("uid-info", "zh_CN",
                                                "player", target.getName(),
                                                "uid", plugin.getUIDGenerator().formatUID(uid))),
                                        () -> sender.sendMessage(messageManager.getMessage("uid-not-found", "zh_CN"))
                                ),
                                () -> plugin.getLogger().warning("发送UID消息失败")
                        );
                    } else {
                        plugin.getServer().getGlobalRegionScheduler().run(plugin, (task)
                                -> optionalUid.ifPresentOrElse(
                                        uid -> sender.sendMessage(messageManager.getMessage("uid-info", "zh_CN",
                                                "player", target.getName(),
                                                "uid", plugin.getUIDGenerator().formatUID(uid))),
                                        () -> sender.sendMessage(messageManager.getMessage("uid-not-found", "zh_CN"))
                                )
                        );
                    }
                });
    }

    private void handleGenerate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uidplugin.generate")) {
            sender.sendMessage(messageManager.getMessage("no-permission", "zh_CN"));
            return;
        }

        Player target = args.length > 1 ? plugin.getServer().getPlayer(args[1])
                : (sender instanceof Player ? (Player) sender : null);

        if (target == null) {
            sender.sendMessage(messageManager.getMessage("player-not-found", "zh_CN"));
            return;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, (task) -> {
            uidManager.generateUID(target)
                    .thenAccept(uid -> {
                        if (sender instanceof Player senderPlayer) {
                            senderPlayer.getScheduler().run(plugin,
                                    (messageTask) -> sender.sendMessage(messageManager.getMessage("uid-generated", "zh_CN",
                                            "player", target.getName(),
                                            "uid", plugin.getUIDGenerator().formatUID(uid))),
                                    () -> plugin.getLogger().warning("发送UID消息失败")
                            );
                        } else {
                            plugin.getServer().getGlobalRegionScheduler().run(plugin,
                                    (messageTask) -> sender.sendMessage(messageManager.getMessage("uid-generated", "zh_CN",
                                            "player", target.getName(),
                                            "uid", plugin.getUIDGenerator().formatUID(uid)))
                            );
                        }

                        if (sender != target) {
                            target.getScheduler().run(plugin,
                                    (messageTask) -> target.sendMessage(messageManager.getMessage("uid-generated-notify", "zh_CN",
                                            "uid", plugin.getUIDGenerator().formatUID(uid))),
                                    () -> plugin.getLogger().warning("发送UID消息失败")
                            );
                        }
                    });
        });
    }

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("uidplugin.stats")) {
            sender.sendMessage(messageManager.getMessage("no-permission", "zh_CN"));
            return;
        }

        performanceMonitor.getStats().thenAccept(stats -> {
            sender.sendMessage(messageManager.getMessage("stats-header", "zh_CN"));
            stats.forEach((key, value)
                    -> sender.sendMessage(messageManager.getMessage("stats-line", "zh_CN",
                            "operation", key,
                            "avg", String.format("%.2f", value.getAverage()),
                            "count", String.valueOf(value.getCount()))));
        });
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("uidplugin.set")) {
            sender.sendMessage(messageManager.getMessage("no-permission", "zh_CN"));
            return;
        }

        if (args.length != 3) {
            sender.sendMessage(messageManager.getMessage("help-set", "zh_CN"));
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messageManager.getMessage("player-not-found", "zh_CN"));
            return;
        }

        try {
            long newUID = Long.parseLong(args[2]);
            plugin.getUIDManager().setUID(target.getUniqueId(), newUID)
                    .thenAccept(success -> {
                        if (success) {
                            sender.sendMessage(messageManager.getMessage("uid-set", "zh_CN",
                                    "player", target.getName(),
                                    "uid", plugin.getUIDGenerator().formatUID(newUID)));
                        } else {
                            sender.sendMessage(messageManager.getMessage("uid-exists", "zh_CN"));
                        }
                    });
        } catch (NumberFormatException e) {
            sender.sendMessage(messageManager.getMessage("invalid-uid", "zh_CN"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("get", "generate", "stats", "set")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(s -> sender.hasPermission("uidplugin." + s))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("generate"))) {
            return null; // 返回在线玩家列表
        }
        return Collections.emptyList();
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(messageManager.getMessage("help-header", "zh_CN"));
        if (sender.hasPermission("uidplugin.get")) {
            sender.sendMessage(messageManager.getMessage("help-get", "zh_CN"));
        }
        if (sender.hasPermission("uidplugin.generate")) {
            sender.sendMessage(messageManager.getMessage("help-generate", "zh_CN"));
        }
        if (sender.hasPermission("uidplugin.stats")) {
            sender.sendMessage(messageManager.getMessage("help-stats", "zh_CN"));
        }
        if (sender.hasPermission("uidplugin.set")) {
            sender.sendMessage(messageManager.getMessage("help-set", "zh_CN"));
        }
    }
}
