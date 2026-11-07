# Bus Preset

[English](README.md)

Bus Preset 集中打包 Bus 所用第三方库的 GraalVM Native Image 配置，包括反射、代理、JNI、资源的可达性元数据，以及类初始化设置。

| Maven 坐标 | Java 模块 | 运行时依赖 |
| --- | --- | --- |
| `org.miaixz:bus-preset` | `bus.preset` | 无 |

## 在应用中使用

需要这些配置的 Bus 集成模块已经依赖 `bus-preset`，`bus-all` 也内嵌了同一套资源。使用这些组件的应用会随依赖获得配置。

如果需要直接引入资源 JAR，先导入 [`bus-bom`](../bus-bom/pom.xml)，再声明以下依赖。未使用 BOM 时，请将依赖版本设为应用所用的 Bus 版本。

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-preset</artifactId>
</dependency>
```

构建原生镜像时，请在 Native Build Tools 中启用上游可达性元数据仓库。Bus Preset 补充 Bus 集成所需的注册。应用自己的配置要通过实际构建和运行原生可执行文件来验证。[GraalVM 可达性元数据](https://www.graalvm.org/latest/reference-manual/native-image/metadata/)

## 配置放在哪里

| 配置内容 | 维护位置 |
| --- | --- |
| 第三方反射、代理、JNI、资源和初始化的补充配置 | `bus-preset` 中对应第三方库的 Maven 坐标目录 |
| Bus 自有类型和方法、SPI、自动配置清单，以及组件使用的 JDK 注册 | 使用这些配置的 Bus 组件 |
| 应用类型、枚举和包含应用接口的组合代理 | 应用本身 |

Bus 方法的签名即使包含第三方参数类型，也随声明该方法的组件维护。通用 `Object` 和 `Enum` 反射注册位于 [`bus-core`](../bus-core/)，Office 所需的 AWT、字体和 ImageIO 配置位于 [`bus-office`](../bus-office/)。[`bus-opencv`](../bus-opencv/) 将自己的 JNI 绑定和本机库资源作为独立产物发布。

## 查找某个库的配置

打开 [`src/main/resources/META-INF/native-image`](src/main/resources/META-INF/native-image/)，按依赖的 Maven groupId 和 artifactId 查找。以 Netty 的包级初始化配置为例，目录如下。

```text
META-INF/native-image/
└── io.netty/
    └── netty-common/
        ├── index.json
        └── 4.2.0.Final/
            └── native-image.properties
```

| 索引字段 | 含义 | Netty 示例 |
| --- | --- | --- |
| `metadata-version` | 该版本线共用配置的目录名 | `4.2.0.Final` |
| `tested-versions` | 已用这套配置验证过的依赖版本 | `4.2.0.Final`、`4.2.17.Final` |

基线沿用官方发布名称。Netty 保留 `.Final` 后缀，Bouncy Castle 使用 `1.85`。[Netty 索引](src/main/resources/META-INF/native-image/io.netty/netty-common/index.json) · [Bouncy Castle 索引](src/main/resources/META-INF/native-image/org.bouncycastle/bcprov-jdk18on/index.json)

## 新增或更新配置

1. **确认实际归属。** 找到包含目标类或资源的 JAR。例如，Bouncy Castle Provider 的注册属于 `bcprov-jdk18on`，由管理的 `bcpkix-jdk18on` 传递引入。
2. **只补 Bus 所需的差异。** 先核对上游元数据，再补充必要注册。保留可达条件，以及方法参数和代理接口的顺序。
3. **使用正式发布的基线。** 通过 Maven Central 的 `maven-metadata.xml` 和版本 POM 核实该版本线的首个正式版本。版本目录和 `metadata-version` 使用同一个官方名称。
4. **记录已验证的版本。** `tested-versions` 必须包含基线并按版本升序排列，补丁版本验证通过后再加入。每个有配置的版本目录都要在对应 artifact 的 `index.json` 中声明。

POI full 和 lite 共有的 schema 注册放在 `poi-ooxml-lite`，`poi-ooxml-full` 补充其独有类型。`bus-image` 和 `bus-office` 可独立使用，因此各自保留 ImageIO 读写 SPI 注册。

## 本地检查

在仓库根目录运行以下命令。

| 命令 | 检查内容 |
| --- | --- |
| `node .github/scripts/native.cjs` | 检查源配置归属、重复项和版本索引，并单独报告两项共享的 ImageIO SPI 注册。 |
| `node .github/scripts/artifact.cjs` | 全量 `clean install` 后，核对字节码、源码 JAR、打包后的元数据、本地安装 JAR 的哈希和 `bus-all` 的配置覆盖。 |

CI 在全部 Bus 模块安装完成后运行两项检查。任一检查失败都会阻止后续发布任务。[构建动作](../.github/actions/build-project/action.yml) · [Maven Central 发布流程](../.github/workflows/maven-central.yml)
