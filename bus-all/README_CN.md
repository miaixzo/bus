# Bus All

`bus-all` 是 Bus 框架的全量聚合发布模块。它通过一个 Maven 坐标提供当前 Bus 运行时模块的聚合入口，适合需要一次性引入完整框架能力的应用。

这个模块是便捷入口。生产环境如果需要更小的依赖面，建议导入 `bus-bom` 后只添加实际使用的模块。

-----

## 模块定位

`bus-all` 适用于：

- 快速评估 Bus 框架。
- 需要同时覆盖多个 Bus 模块的集成测试。
- 可以接受较完整类路径的内部工具。
- 明确以完整 Bus 运行时作为统一基础能力的应用。

`bus-all` 不是：

- BOM。依赖版本管理应使用 `bus-bom`。
- 父 POM。统一构建配置应使用 `bus-parent`。
- 单纯的 Spring Boot 启动器。只需要自动配置时应使用 `bus-starter`。

-----

## Maven 使用

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
    <version>x.x.x</version>
</dependency>
```

如果项目已经导入 `bus-bom`，可以省略版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-bom</artifactId>
            <version>x.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-all</artifactId>
    </dependency>
</dependencies>
```

-----

## Gradle 使用

```gradle
implementation "org.miaixz:bus-all:x.x.x"
```

配合 BOM 使用：

```gradle
dependencyManagement {
    imports {
        mavenBom "org.miaixz:bus-bom:x.x.x"
    }
}

dependencies {
    implementation "org.miaixz:bus-all"
}
```

-----

## 聚合模块

`bus-all` 聚合以下运行时模块：

- `bus-auth`：认证与授权工具
- `bus-base`：基础抽象
- `bus-cache`：缓存抽象与缓存集成
- `bus-core`：核心工具库
- `bus-cortex`：服务注册、发现与健康探测
- `bus-crypto`：加密、证书与安全工具
- `bus-extra`：扩展工具与集成能力
- `bus-fabric`：HTTP、Socket、WebSocket、SSE、STOMP 协议运行时
- `bus-gitlab`：GitLab API 集成
- `bus-health`：健康检查与诊断
- `bus-image`：图像处理工具
- `bus-limiter`：限流支持
- `bus-logger`：日志门面与日志集成
- `bus-mapper`：Mapper 与持久化辅助能力
- `bus-metrics`：指标能力
- `bus-notify`：通知集成
- `bus-office`：Office 文档工具
- `bus-pay`：支付集成
- `bus-proxy`：代理支持
- `bus-sensitive`：敏感数据脱敏
- `bus-setting`：配置工具
- `bus-shade`：依赖重定位与 shaded 支持
- `bus-starter`：Spring Boot 自动配置
- `bus-storage`：对象存储集成
- `bus-tempus`：时间与调度工具
- `bus-tracer`：链路追踪支持
- `bus-validate`：校验工具
- `bus-vortex`：网关与路由运行时

`bus-parent`、`bus-bom` 等构建管理模块不是运行时模块，不作为应用运行依赖使用。

-----

## 打包说明

`bus-all` 在打包阶段会将 Bus 运行时模块聚合到一个发布制品中，并合并 GraalVM native-image 元数据。发布 POM 会被规范化处理，使用方以 `bus-all` 作为单一入口，而不是把每个内部模块都作为普通传递依赖展开。

这种方式使用方便，但类路径范围会更大。对启动耗时、依赖面、native-image 体积或部署包大小敏感的项目，应优先选择按需引入单独模块。

-----

## 入口选择

| 需求 | 推荐入口 |
| --- | --- |
| 一个依赖引入完整 Bus 运行时 | `bus-all` |
| 为按需模块统一版本 | `bus-bom` |
| 统一构建配置 | `bus-parent` |
| Spring Boot 自动配置 | `bus-starter` |
| 最小运行时依赖集合 | 直接依赖具体模块 |

-----

## 许可证

Bus 使用 Apache License 2.0 发布。
