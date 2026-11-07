# Bus Native

集中维护 Bus 集成所需的第三方 GraalVM 可达性元数据和初始化配置。
本模块仅发布资源 JAR，不包含 Java 类，不声明运行时依赖。

- Bus 自身注册保留在原模块的 `org.miaixz/<模块>` 目录。
  第三方类型的反射、代理、JNI 注册及第三方框架的资源声明统一归入本模块对应的库目录。
  Bus 方法签名中的第三方参数类型仍属于 Bus 方法注册；Bus 自身的 SPI、自动配置清单和 JDK 配置留在使用它们的组件中。
- 第三方注册位于 `META-INF/native-image/<group>/<artifact>/<baseline-version>/`，
  与 Bus 组件一样使用版本线的基线目录，不为补丁版本单独建目录。
  基线必须是 Maven Central 实际发布的该版本线首个正式版本，
  通过对应坐标的 `maven-metadata.xml` 和版本 POM 核实，不能直接将补丁号归零或补 `.0`。
  例如 POI 使用 `5.5.0`，Bouncy Castle 使用官方版本名 `1.85`。
  每个 artifact 根目录必须存在 `index.json`，通过 `metadata-version` 指向该目录；
  每个版本目录都必须在索引中声明，不保留无索引的配置或空目录。
  索引通过 `tested-versions` 列出具体支持版本；列表必须包含基线版本，按版本升序排列。
  新增补丁版本时更新列表并验证配置，不能仅因版本已发布就将其视为已验证。
  Logback、Netty、SLF4J 包级初始化配置同样放入版本基线目录，并通过索引声明支持版本；
  artifact 根目录仅保留 `index.json`，不直接放置初始化配置。
  Netty 使用官方完整版本名 `4.2.0.Final`，保留 `.Final` 后缀。
- JDK 注册随使用它们的 Bus 组件维护，不在本模块单独建立 `jdk` 目录。
  通用泛型反射接口注册合并在 `bus-core` 的 `org.miaixz/bus-core/8.8.0/reachability-metadata.json`；
  Office 所需的 AWT、字体、ImageIO、Java2D JNI 和资源注册合并在
  `bus-office` 的 `org.miaixz/bus-office/8.8.0/reachability-metadata.json`，保留原有可达条件。
- Jackson 2 默认转换器、Temporal 服务客户端代理及 shaded gRPC 注册按各自依赖归档。
  Logback、SLF4J 和 shaded gRPC 的类初始化策略也由本模块维护。
  Logback 常规配置资源位于 `ch.qos.logback/logback-classic/1.6.0`，
  `logback-spring.*` 资源位于 `org.springframework.boot/spring-boot/4.1.0`。
  应用自己的类型、枚举和包含应用接口的组合代理应留在应用，不能迁入本模块。
- Bus 自有 OpenCV 绑定的 JNI 注册和该组件随包发布的 `lib/**` 资源声明保留在 `bus-opencv`，该模块不依赖 `bus-native`。
- Bouncy Castle Provider 的反射和初始化配置按实际类型归属归档为 `org.bouncycastle/bcprov-jdk18on/1.85`，
  该库由管理的 `bcpkix-jdk18on` 依赖传递引入，版本 `1.85` 在 `tested-versions` 中声明。
- 需要这些配置的集成模块直接引入本模块，`bus-core` 不依赖本模块；`bus-all` 将这些资源包含在聚合 JAR 内。
  常规 Bus 模块使用方无需手动补充依赖，本模块也不会引入第三方运行库。
- 保留原有可达条件及官方元数据去重结果。本模块补充官方仓库，应用仍需在 Native Build Tools 中启用官方元数据仓库。
- 原有 Bouncy Castle、Fastjson2、Netty、SLF4J、Spring Boot Loader 初始化参数一并迁入。
  使用方仍需验证自身应用的原生镜像构建。

仅在集成需要补充官方仓库未覆盖的配置时，在此维护差异项、版本索引和可达条件，避免重复写回各 Bus 模块。

POI full/lite 共有的模式类型只在 `poi-ooxml-lite` 元数据中声明，`poi-ooxml-full` 仅保留额外类型；本资源 JAR 始终包含两份配置。
`bus-image` 与 `bus-office` 可分别独立引入，因此各自保留 ImageIO 读写 SPI 规则。通用 `Object`/`Enum` 反射及 Bus core 自身声明统一归 `bus-core`。

在仓库根目录执行 `node .github/scripts/native.cjs`，可检查全部组件的归属、重复项和索引；上述两条独立 ImageIO SPI 规则会单独列出。
完整清理构建后，可执行 `node .github/scripts/artifact.cjs`，比对编译字节码、组件 JAR 与原生配置，并检查 `bus-all` 的有效覆盖及本地安装产物哈希。
`bus-opencv` 按聚合包现有依赖关系保持为独立的 JNI 产物。
