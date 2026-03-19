# VNC Client For MC

在Minecraft世界里放置VNC全息投影，支持键鼠操作远程电脑。

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
/vnc control                    - 进入/退出控制模式
/vnc status                     - 查看状态
```

### 按键

- `H` - 切换全息投影显示/隐藏
- `V` - 切换控制模式

### 使用流程

1. `/vnc connect <你的VNC服务器IP>`
2. `/vnc holo place` - 在脚下放置全息投影
3. `/vnc control` - 进入控制模式
4. 用鼠标点击全息投影上的位置，会映射到远程电脑屏幕
5. 按ESC退出控制模式

### 控制模式说明

进入控制模式后：
- 鼠标移动会映射到VNC屏幕坐标
- 左键/中键/右键点击会发送到远程
- 滚轮可以滚动
- 键盘输入会转发到远程电脑
- 按ESC退出控制模式

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
