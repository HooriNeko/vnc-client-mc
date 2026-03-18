# VNC Client For MC

在Minecraft里显示远程电脑画面的Fabric Mod。

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
/vnc hologram show               - 显示全息图
/vnc hologram hide              - 隐藏全息图
/vnc hologram toggle            - 切换显示/隐藏
/vnc hologram scale <数值>      - 设置缩放 (0.1-10)
/vnc hologram distance <数值>   - 设置距离 (1-20)
/vnc hologram quality <数值>     - 设置画质 (32=低, 64=中, 128=高, 256=最高)
/vnc status                     - 查看连接状态
```

### 按键

- `H` - 切换全息图显示

连接成功后全息图会自动显示在眼前，跟着你的视角动。

## 原理

用Vernacular库连VNC服务器，把远程画面截下来渲染到屏幕上。纯客户端Mod，不需要服务器装。

## 开发

```bash
./gradlew build
```

打包好的jar在 `build/libs/` 下面。

## License

MIT
