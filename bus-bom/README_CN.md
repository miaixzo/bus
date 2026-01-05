# 📦 Bus BOM: Bus 框架物料清单

<p align="center">
<strong>依赖管理以确保版本一致性</strong>
</p>

-----

## 📖 项目介绍

**Bus BOM** (Bill of Materials) 是一个 Maven POM 模块，为所有 Bus 框架模块提供集中式依赖管理。它通过在一个地方管理所有 Bus 库的依赖关系，确保项目中版本的统一性。

-----

## ✨ 核心特性

- **集中版本管理**: 所有 Bus 库版本的单一真实来源
- **一致依赖**: 确保所有 Bus 模块之间的版本兼容
- **简化配置**: 无需为单个依赖指定版本
- **轻松升级**: 通过更改单个版本即可更新所有 Bus 模块
- **冲突预防**: 避免 Bus 模块之间的版本冲突

-----

## 🚀 使用方式

### Maven 依赖管理

在项目的 `pom.xml` 中导入 BOM:

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
```

### 添加依赖

导入 BOM 后，添加 Bus 依赖时无需指定版本:

```xml
<dependencies>
    <!-- 核心模块 -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-core</artifactId>
    </dependency>

    <!-- 额外模块 -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-http</artifactId>
    </dependency>

    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-mapper</artifactId>
    </dependency>
</dependencies>
```

### Spring Boot 集成

对于 Spring Boot 项目，可以与 Spring Boot 的 BOM 结合使用:

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot BOM -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Bus BOM -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-bom</artifactId>
            <version>x.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

-----

## 📋 管理的依赖

Bus BOM 管理所有 Bus 框架模块的版本:

### 核心模块

- **bus-core**: 核心工具和工具类
- **bus-base**: 基础模块
- **bus-extra**: 扩展工具

### 集成模块

- **bus-mapper**: MyBatis 增强
- **bus-http**: HTTP 客户端
- **bus-logger**: 日志框架
- **bus-cache**: 缓存抽象
- **bus-validate**: 验证框架

### 安全模块

- **bus-crypto**: 加密工具
- **bus-auth**: 认证和授权
- **bus-sensitive**: 敏感数据脱敏

### 工具模块

- **bus-image**: 图像处理
- **bus-office**: Office 文档处理
- **bus-cron**: 定时任务
- **bus-pay**: 支付集成
- **bus-notify**: 通知服务
- **bus-storage**: 存储抽象

### 高级模块

- **bus-vortex**: API 网关
- **bus-limiter**: 限流和降级
- **bus-tracer**: 分布式追踪
- **bus-proxy**: 动态代理
- **bus-setting**: 配置管理
- **bus-shade**: JAR 加密
- **bus-starter**: Spring Boot 启动器
- **bus-parent**: 父 POM
- **bus-gitlab**: GitLab 集成
- **bus-opencv**: OpenCV 集成
- **bus-socket**: WebSocket 支持
- **bus-health**: 健康检查
- **bus-cron**: Cron 调度器

-----

## 💡 最佳实践

### 1. 始终使用 BOM

**推荐**:
```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <!-- 不指定版本 -->
</dependency>
```

**不推荐**:
```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.1</version>
</dependency>
```

### 2. BOM 置于首位

在其他依赖管理块之前导入 Bus BOM 以确保正确的优先级。

### 3. 版本覆盖

如果需要覆盖特定版本(不推荐):

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.1</version>
</dependency>
```

### 4. 一致升级

通过更改 BOM 版本同时更新所有 Bus 模块:

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-bom</artifactId>
    <version>8.6.0</version> <!-- 更新此版本 -->
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

-----

## 🔍 检查版本

要查看 BOM 管理的版本，可以:

```bash
mvn help:effective-pom
```

或检查实际的 `bus-bom/pom.xml` 文件:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-core</artifactId>
            <version>8.5.1</version>
        </dependency>
        <!-- ... 其他依赖 ... -->
    </dependencies>
</dependencyManagement>
```

-----

## ⚠️ 故障排除

### 版本冲突

如果遇到版本冲突:

1. 检查 `mvn dependency:tree` 查看使用的版本
2. 确保 BOM 在其他依赖管理之前导入
3. 检查其他库是否覆盖了 Bus 版本
4. 显式排除冲突的依赖

### 缺失依赖

如果找不到依赖:

1. 验证 BOM 版本是否正确
2. 检查 Bus 框架中是否存在该模块
3. 确保 Maven 仓库配置正确

-----

## 🔄 版本兼容性

| Bus BOM 版本 | Spring Boot 版本 | JDK 版本 |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## ❓ 常见问题

### 问: 必须使用 BOM 吗？

答: 虽然不是强制的，但强烈建议使用以确保版本兼容性。

### 问: 可以覆盖单个版本吗？

答: 可以，但不推荐，因为这可能导致兼容性问题。

### 问: 如何知道使用哪个版本的 BOM？

答: 使用与您的 Spring Boot 和 JDK 版本兼容的最新稳定版本。

### 问: 如果需要多个版本的 Bus 模块怎么办？

答: 不推荐这样做。为所有 Bus 依赖使用单个 BOM 版本。

-----

## 🤝 贡献

欢迎贡献！请随时提交 Pull Request。

-----

## 📄 许可证

[许可证信息]

-----

## 🔗 相关文档

- [Bus 框架文档](https://www.miaixz.org)
- [Maven BOM 文档](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [Bus 父 POM](../bus-parent/README.md)
