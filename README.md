# 增强视野 · Enhanced Vision 1.0

适用于 **Minecraft 1.21.1 / NeoForge 21.1.x** 的客户端视觉模组。

增强周边视野、柔和的光学畸变与平滑疾跑 FOV。支持 TACZ 枪械视角和 Vivecraft VR 双眼画面，包含 Iris/Sodium 渲染兼容修复。默认按 **F8** 开关，在模组配置中调整参数。

**安装**：将 `enhanced-vision-1.0-1.21.1-neoforge.jar` 放入客户端 `mods` 文件夹。无需服务端安装。避免同时安装其他使用 `enhanced_vision` ID 的模组。

默认值：外围畸变强度 `0.15`；疾跑 FOV 增幅 `0.10`；平滑过渡 `0.4` 秒。配置保存在本机 `config/enhanced_vision-client.toml`。

**隐私**：本模组不采集个人信息，不发送网络请求，不包含遥测、广告或自动更新服务。仅使用游戏本身的客户端配置存储功能。

**许可证**：项目以 [MIT License](LICENSE) 开源，版权声明为 Copyright (c) 2026 Unikon。完整许可证随源码与发行 JAR 一同提供。NeoForge、Minecraft、Vivecraft、TACZ 等属于各自权利人，本模组不打包其代码或游戏资源，也不代表这些项目的官方认可。

**从源码构建**：安装 JDK 21 和 Gradle 8.14.3，执行 `gradle build`。编译依赖由 Gradle 下载；发布 JAR 位于 `build/libs/`。该源码包不含 Gradle 缓存、包装器二进制、测试记录和历史版本产物。
