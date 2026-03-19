# VNC Client For MC

在Minecraft世界里放置VNC全息投影。

## 运行环境

- Minecraft 1.21+
- Fabric Loader 0.16.0+
- Java 21

## 安装

1. 安装 Fabric API
2. 把 `vnc-client-mc-*.jar` 丢进 mods 文件夹

## 使用方法

先确保目标电脑开了VNC服务器（默认端口5900）。

### 命令

```
/vnc connect <IP或主机名>        - 连接VNC服务器
/vnc disconnect                  - 断开连接
/vnc holo place                 - 在脚下放置全息投影
/vnc holo remove                - 移除全息投影
/vnc holo scale <数值>          - 设置缩放 (0.5-10)
/vnc holo quality <数值>         - 设置画质 (32=低, 64=中, 128=高, 256=最高)
/vnc status                     - 查看状态
```

### 按键

- `H` - 切换全息投影显示/隐藏

### 使用流程

1. `/vnc connect <你的VNC服务器IP>`
2. `/vnc holo place` - 在脚下放置全息投影
3. 全息投影会显示VNC画面，固定在世界某位置

## 技术细节

- 全息投影基于ArmorStand实体
- VNC连接使用Vernacular库
- 纯客户端Mod，不需要服务器装
- 单人游戏有效；多人游戏需要所有玩家都装此Mod

## 开发

```bash
./gradlew build
```

打包好的jar在 `build/libs/` 下面。

## License

MIT
