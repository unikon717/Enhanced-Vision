# Enhanced Vision / 增强视野

**A wider, more natural first-person view for Minecraft.**

**让 Minecraft 的第一人称视野更宽广、更自然。**

Enhanced Vision is a client-side visual enhancement mod for **Minecraft 1.21.1 / NeoForge**.

增强视野是一款适用于 **Minecraft 1.21.1 / NeoForge** 的客户端视觉增强模组。

---

## English

### Overview

Enhanced Vision expands peripheral visibility through smooth, nonlinear optical distortion rather than simply increasing the game's field of view. It aims to preserve the visual scale of objects near the centre of the screen while providing a wider view of the surrounding environment.

The mod also introduces smooth sprint FOV transitions, customizable visual settings, and integrated compatibility with TACZ and Vivecraft VR.

### Features

- **Peripheral Vision Expansion** — Expands the visible area around the edges of the screen with a gradual optical distortion curve.
- **Smooth Sprint FOV** — Gradually increases the field of view while sprinting and smoothly restores it when stopping.
- **Customizable Settings** — Adjust distortion strength, sprint FOV intensity, and transition duration.
- **First-Person Rendering** — Designed to preserve the visual scale of the centre view and held items.
- **TACZ Compatibility** — Supports firearm aiming and FOV behaviour.
- **Vivecraft VR Support** — Applies distortion independently to each eye in VR.
- **Iris & Sodium Compatibility** — Includes fixes for rendering-state conflicts.

### Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Client-side installation only

### Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Download `enhanced-vision-1.0-1.21.1-neoforge.jar`.
3. Place the JAR in your Minecraft `mods` folder.
4. Launch the game and customize the visual effects through the mod configuration screen.

Press **F8** to toggle Enhanced Vision.

### Configuration

Configuration is stored locally in `config/enhanced_vision-client.toml`.

### Building from Source

Requires JDK 21 and Gradle 8.14.3.

```bash
gradle build
```

The compiled JAR will be generated in `build/libs/`.

### Privacy

Enhanced Vision does not collect personal information, transmit telemetry, display advertisements, or include automatic update services. Configuration data is stored locally.

### License

Licensed under the [MIT License](LICENSE).

Copyright (c) 2026 Unikon.

Enhanced Vision is an independent Minecraft modification and is not officially affiliated with Mojang Studios, Microsoft.

---

## 简体中文

### 项目介绍

**增强视野（Enhanced Vision）** 是一款专注于第一人称视觉体验的 Minecraft 客户端模组。

模组通过平滑的非线性光学畸变扩展画面外围的可见范围，而不是单纯提高游戏的视野角度（FOV）。它尽可能保持中心目标的视觉比例，让玩家获得更加宽广、自然的周边视野。

此外，模组还提供平滑疾跑 FOV、自定义视觉参数，以及 TACZ 枪械和 Vivecraft VR 兼容支持。

### 主要功能

- **周边视野增强** — 通过连续的光学畸变曲线扩展屏幕外围可见范围。
- **平滑疾跑视野** — 疾跑时平缓扩大 FOV，停止后自然恢复。
- **自定义视觉参数** — 可调节畸变强度、疾跑视野增幅和过渡时间。
- **第一人称渲染** — 尽量保持中心目标和手持物品的视觉比例稳定。
- **TACZ 兼容** — 支持枪械开镜及其视野变化。
- **Vivecraft VR 支持** — 对 VR 左右眼分别应用视觉畸变。
- **Iris 与 Sodium 兼容** 

### 运行环境

- Minecraft 1.21.1
- NeoForge 21.1.x
- 仅需客户端安装

### 安装方法

1. 安装 Minecraft 1.21.1 对应的 NeoForge。
2. 下载 `enhanced-vision-1.0-1.21.1-neoforge.jar`。
3. 将 JAR 文件放入游戏实例的 `mods` 文件夹。
4. 启动游戏，在模组配置界面中调整视觉效果。

按 **F8** 可以快速开启或关闭增强视野。

### 配置

配置文件位于 `config/enhanced_vision-client.toml`。

### 从源码构建

需要 JDK 21 和 Gradle 8.14.3。

```bash
gradle build
```

编译后的 JAR 位于 `build/libs/`。

### 隐私

增强视野不会收集个人信息，不包含遥测、广告或自动更新服务。模组配置仅保存在本地。

### 开源许可证

本项目采用 [MIT License](LICENSE) 开源。

Copyright (c) 2026 Unikon。

增强视野是独立开发的 Minecraft 模组，与 Mojang Studios、Microsoft 没有官方关联。
