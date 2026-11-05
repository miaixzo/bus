# Bus Native

集中维护 Bus 集成所需的第三方 GraalVM 可达性元数据和初始化配置。
本模块仅发布资源 JAR，不包含 Java 类，不声明运行时依赖。

- Bus 自身注册保留在原模块的 `org.miaixz/<模块>` 目录。
- 第三方注册及索引位于 `META-INF/native-image/<group>/<artifact>/<version>`。
  不绑定版本的 Netty、SLF4J 包级初始化配置放在对应 artifact 目录中。
- OpenCV 元数据保留在 `bus-opencv`，该模块不依赖 `bus-native`。
- Bouncy Castle 按实际管理的依赖归档为 `org.bouncycastle/bcpkix-jdk18on/1.85`。
- 需要这些配置的集成模块直接引入本模块，`bus-core` 不依赖本模块；`bus-all` 将这些资源包含在聚合 JAR 内。
  常规 Bus 模块使用方无需手动补充依赖，本模块也不会引入第三方运行库。
- 保留原有可达条件及官方元数据去重结果。本模块补充官方仓库，应用仍需在 Native Build Tools 中启用官方元数据仓库。
- 原有 Bouncy Castle、Fastjson2、Netty、SLF4J、Spring Boot Loader 初始化参数一并迁入。
  使用方仍需验证自身应用的原生镜像构建。

仅在集成需要补充官方仓库未覆盖的配置时，在此维护差异项、版本索引和可达条件，避免重复写回各 Bus 模块。
