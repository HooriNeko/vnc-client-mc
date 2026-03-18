# VNC Client For MC

在Minecraft里显示远程电脑画面的Fabric Mod。

## 运行环境

- Minecraft 1.21.4
- Fabric Loader 0.16.9+
- Java 21

## 安装

1. 安装 Fabric API
2. 把 `vnc-client-mc-1.0.0.jar` 丢进 mods 文件夹

## 使用方法

先确保目标电脑开了VNC服务器（端口默认5900）。

### 命令

```
/vnc connect <IP或主机名>        - 连接VNC服务器
/vnc disconnect                  - 断开连接
/vnc hologram show                - 显示全息图
/vnc hologram hide                - 隐藏全息图
/vnc status                       - 查看连接状态
```

连接成功后全息图会自动显示在眼前。全息图会跟着你的视角动。

## 原理

用Vernacular库连VNC服务器，把远程画面截下来，每隔几帧渲染到屏幕上。用的是Minecraft的渲染回调，绑在玩家视角前面。

纯客户端Mod，不需要服务器装。

## 开发

```bash
./gradlew build
```

打包好的jar在 `build/libs/` 下面。

## TODO

- [ ] 全息图缩放和距离调整命令
- [ ] 更好的画质（目前为了性能有压缩）
- [ ] 键盘鼠标输入投射到VNC

## License

MIT
