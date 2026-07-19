# Bus Framework

<p align="center">
  <strong>现代化、模块化的 Java 企业级框架 — 基于 Java 25+</strong>
</p>

<p align="center">
  <a href="https://github.com/818000/bus"><img src="https://img.shields.io/badge/GitHub-miaixz%2Fbus-blue?logo=github" alt="GitHub"/></a>
  <a href="https://mvnrepository.com/artifact/org.miaixz"><img src="https://img.shields.io/maven-central/v/org.miaixz/bus-core?label=Maven%20Central" alt="Maven Central"/></a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"/></a>
  <img src="https://img.shields.io/badge/Java-25%2B-orange" alt="Java 25+"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen" alt="Spring Boot 3.5.x"/>
</p>

---

## 项目介绍

**Bus** 是一个综合性、模块化的企业级 Java 框架，提供开箱即用的生产级组件，涵盖核心工具库、加密安全、HTTP 客户端、分布式链路追踪、支付集成、云存储等全场景能力。所有模块拥有一致的 API 风格和统一的配置方式，并通过 `bus-starter` 与 Spring Boot 无缝集成。

每个模块均可独立使用，按需引入，不带任何强制依赖。

---

## 模块总览

| 模块 | 说明 |
| :--- | :--- |
| **bus-all** | 元模块，一次性引入所有 Bus 模块 |
| **bus-auth** | 企业级认证与授权（OAuth2、JWT、RBAC） |
| **bus-base** | 基础类型、公共常量与异常体系 |
| **bus-bom** | 物料清单（BOM），无需继承父项目即可统一管理版本 |
| **bus-cache** | 多级缓存抽象（Redis、Caffeine、EhCache 等） |
| **bus-core** | 核心工具：反射、注解合成、类型转换、I/O 等 |
| **bus-cortex** | 分布式服务统一注册与配置中心 |
| **bus-crypto** | 加密框架：AES、RSA、SM2/SM3/SM4、摘要算法、编码工具 |
| **bus-extra** | 核心之外的额外工具与第三方集成 |
| **bus-gitlab** | GitLab API 客户端与 CI/CD 集成助手 |
| **bus-health** | 健康检查端点与系统监控 |
| **bus-fabric** | 统一 HTTP 客户端与非阻塞 Socket、WebSocket 抽象 |
| **bus-image** | 图像处理与 DICOM 医学影像专业支持 |
| **bus-limiter** | 热点检测、限流与服务降级 |
| **bus-logger** | 通用日志抽象，统一桥接所有主流日志框架 |
| **bus-mapper** | MyBatis 增强：类型处理器、通用 CRUD、查询 DSL |
| **bus-metrics** | 生产级指标采集与上报 |
| **bus-notify** | 多渠道通知服务（邮件、短信、钉钉、企业微信等） |
| **bus-office** | Office 文档处理：Excel、Word、PDF |
| **bus-opencv** | OpenCV 集成，支持计算机视觉任务 |
| **bus-pay** | 通用支付集成（支付宝、微信支付、银联等） |
| **bus-proxy** | 动态代理与 AOP 工具 |
| **bus-sensitive** | 数据脱敏与敏感字段保护 |
| **bus-setting** | 统一配置文件读取（properties、YAML、TOML 等） |
| **bus-shade** | 依赖 Shade 管理与重定位 |
| **bus-starter** | Bus 生态 Spring Boot 自动配置启动器 |
| **bus-storage** | 统一云存储抽象（S3、OSS、COS、MinIO 等） |
| **bus-tempus** | 时间与调度工具：Cron 表达式、日历操作 |
| **bus-tracer** | 分布式链路追踪集成（OpenTelemetry、SkyWalking 等） |
| **bus-validate** | 声明式校验框架，支持注解驱动 |
| **bus-vortex** | 高性能 API 网关与流量管理 |

---

## 快速开始

### 方式一 — 继承父项目 POM

继承 `bus-parent`，一步获得统一的依赖版本管理和构建配置：

```xml
<parent>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-parent</artifactId>
    <version>8.x.x</version>
</parent>
```

之后按需声明模块依赖，无需填写版本号：

```xml
<dependencies>
    <!-- 核心工具库 -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-core</artifactId>
    </dependency>

    <!-- Spring Boot 自动配置 -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-starter</artifactId>
    </dependency>

    <!-- 加密与安全 -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-crypto</artifactId>
    </dependency>
</dependencies>
```

### 方式二 — 导入 BOM（保留自己的父项目）

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-bom</artifactId>
            <version>8.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 方式三 — 一次性引入所有模块

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
    <version>8.x.x</version>
</dependency>
```

---

## 使用示例

### Spring Boot 项目（推荐）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-parent</artifactId>
        <version>8.x.x</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- Bus 全系 Spring Boot 自动配置 -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-starter</artifactId>
        </dependency>

        <!-- 数据库访问（MyBatis 增强） -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-mapper</artifactId>
        </dependency>

        <!-- 多渠道消息通知 -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-notify</artifactId>
        </dependency>

        <!-- 云存储（S3/OSS/COS 等） -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-storage</artifactId>
        </dependency>

    </dependencies>
</project>
```

### 多模块项目

**根 POM** (`pom.xml`)：

```xml
<project>
    <parent>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-parent</artifactId>
        <version>8.x.x</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-platform</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <modules>
        <module>platform-common</module>
        <module>platform-service</module>
        <module>platform-web</module>
    </modules>
</project>
```

**子模块** (`platform-common/pom.xml`)：

```xml
<project>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>my-platform</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>platform-common</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-crypto</artifactId>
        </dependency>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-validate</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

## 构建配置

`bus-parent` 提供以下默认配置，如有需要可在子项目中覆盖任意属性。

### Java 版本与编码

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### 关键依赖版本

| 组件 | 版本 |
| :--- | :--- |
| Java 最低版本 | 25+ |
| Spring Boot | 3.5.13 |
| MyBatis | 3.5.19 |
| MyBatis-Spring | 3.0.5 |

### 管理与预配置插件

| 插件 | 用途 |
| :--- | :--- |
| `maven-compiler-plugin` | 编译 Java 代码时保留参数名，并配置 Lombok 注解处理；编译 release 由当前 Maven settings/profile 提供 |
| `maven-resources-plugin` | 使用 UTF-8 和 Bus 资源占位符进行资源过滤 |
| `maven-jar-plugin` | 生成标准 JAR Manifest，并避免写入 `META-INF/maven` 描述信息 |
| `maven-war-plugin` | 生成标准 WAR Manifest，并避免写入 `META-INF/maven` 描述信息 |
| `maven-surefire-plugin` | 运行单元测试，并关闭测试阶段 module-path 以提升兼容性 |
| `maven-failsafe-plugin` | 运行集成测试，绑定 `integration-test` 和 `verify` 阶段 |
| `maven-source-plugin` | 打包源代码 JAR（发布用） |
| `maven-javadoc-plugin` | 生成 Javadoc JAR（发布用） |
| `spotless-maven-plugin` | 格式化源码、XML、Markdown、YAML、JSON、properties、SVG、service 和 POM 文件 |
| `groom-maven-plugin` | 规范化 Bus 模块和父 POM 的发布 POM |
| `maven-gpg-plugin` | GPG 签名（发布到 Maven Central） |
| `central-publishing-maven-plugin` | 通过 Central Portal 发布到 Maven Central |
| `git-commit-id-maven-plugin` | 构建阶段生成 `git.properties` |
| `spring-boot-maven-plugin` | 重新打包 Spring Boot 可执行归档 |
| `native-maven-plugin` | 集成 GraalVM reachability metadata 和 native image 构建 |

---

## 最佳实践

### 让父项目统一管理版本

```xml
<!-- 推荐：版本由 bus-parent 统一管理 -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
</dependency>

<!-- 不推荐：硬编码版本，增加维护成本 -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.x.x</version>
</dependency>
```

### 按需排除 bus-starter 中的可选模块

`bus-starter` 是一个便捷入口依赖，若不需要某个子模块，排除它可以让类路径更精简：

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-mapper</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 覆盖单个依赖版本

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.x.x</version>  <!-- 覆盖父项目管理的版本 -->
</dependency>
```

---

## 常见问题

**Q：我已有 Spring Boot 父项目，还能使用 Bus 吗？**

可以。直接在 `<dependencyManagement>` 中导入 `bus-bom` 即可享有完整的版本管理，无需替换现有父项目。

**Q：如何查看所有已管理的版本？**

```bash
mvn help:effective-pom | grep -A2 'artifactId>bus-'
```

或查看完整解析后的 POM：

```bash
mvn help:effective-pom
```

**Q：最低 Java 版本要求是什么？**

Bus 8.x 要求 **Java 25** 或更高版本。如需支持 Java 11，请使用 Bus 7.x 系列。

**Q：是否支持 GraalVM 原生镜像？**

支持。Bus 各模块在 `META-INF/native-image` 下提供 GraalVM 元数据，`bus-all` 在打包阶段会将各模块元数据合并为一份聚合 native-image 配置。

---

## 版本兼容性

| Bus 版本 | Java | Spring Boot | 状态 |
| :--- | :--- | :--- | :--- |
| **8.6.x** | 25+ | 3.5.x | **维护中** |
| 8.0.x - 8.5.x | 25+ | 3.x | 维护中 |
| 7.x | 11+ | 2.x | 已停止维护 |

---

## 开源协议

Bus 基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 协议开源。

Copyright © 2015–2026 [miaixz.org](https://miaixz.org) 及贡献者。

---

## 相关链接

* [GitHub 仓库](https://github.com/818000/bus)
* [问题反馈](https://github.com/818000/bus/issues)
* [版本记录](https://github.com/818000/bus/releases)
