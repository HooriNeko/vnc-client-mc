# VNC Client For MC

在Minecraft里把远程电脑桌面显示成全息投影，绑在地图上不动，还能键鼠操作。

## 支持版本

- Minecraft 1.21+
- Fabric Loader 0.16.0+
- Java 21

## 安装

1. 下载最新版本 [Releases](https://github.com/HooriNeko/vnc-client-mc/releases)
2. 安装 [Fabric API](https://modrinth.com/mod/fabric-api)
3. 把 jar 丢进 `.minecraft/mods`

## 命令

| 命令 | 说明 |
|------|------|
| `/vnc connect <IP>` | 连接VNC服务器（端口默认5900） |
| `/vnc disconnect` | 断开连接 |
| `/vnc holo place` | 在脚下放全息投影 |
| `/vnc holo remove` | 移除全息投影 |
| `/vnc holo scale <数值>` | 缩放（0.5-10） |
| `/vnc holo quality <数值>` | 画质（32=低，128=高，256=最高） |
| `/vnc control` | 进入/退出控制模式 |
| `/vnc status` | 状态 |

## 按键

- `H` - 开关全息投影
- `V` - 开关控制模式
- `ESC` - 退出控制模式

## 用法

1. 远程电脑开VNC服务（5900端口）
2. 进游戏，`/vnc connect <远程IP>`
3. `/vnc holo place` 在脚下放全息
4. `/vnc control` 进入控制模式
5. 鼠标点全息投影，键鼠就映射到远程电脑了
6. 玩完ESC退出控制模式

## 技术栈

- [Fabric](https://fabricmc.net/) - Mod加载器
- [Vernacular](https://github.com/nicholasngh/vnc-client-java) - Java VNC客户端库

## 开发

```bash
./gradlew build
```

jar在 `build/libs/`

## License

MIT
