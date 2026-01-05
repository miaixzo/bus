# 📦 Bus Parent: 父 POM 配置

<p align="center">
<strong>Bus 框架的集中式依赖管理和构建配置</strong>
</p>

-----

## 📖 项目介绍

**Bus Parent** 是 Bus 框架的父 POM (项目对象模型)配置模块。它为所有 Bus 框架模块提供集中式依赖管理、统一的构建配置和版本控制。

此模块仅包含一个 `pom.xml` 文件，并提供 `dependencyManagement` 声明以确保所有子模块之间的一致性。

-----

## ✨ 核心特性

* **集中式依赖管理**: 所有第三方库版本在一个地方管理
* **统一构建配置**: 所有模块的一致构建设置
* **版本控制**: 框架和依赖版本的单一真实来源
* **插件管理**: 标准化插件配置(编译器、surefire 等)
* **仓库管理**: 集中式 Maven 仓库配置
* **质量控制**: 集成的代码质量和分析插件

-----

## 🚀 快速开始

### Maven 父配置

要使用 Spring Boot 风格的依赖管理来使用 Bus 框架模块，请将 Bus 父项添加到项目的父 POM 中:

```xml
<parent>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-parent</artifactId>
    <version>8.5.1</version>
</parent>
```

### 在子模块中添加依赖

配置父项后，您可以添加特定的 Bus 模块而无需指定版本:

```xml
<!-- 添加所有 Bus 模块 -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
</dependency>
```

或根据需要添加单个模块:

```xml
<!-- 添加特定模块 -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
</dependency>

<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-mapper</artifactId>
</dependency>

<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-crypto</artifactId>
</dependency>
```

-----

## 📝 使用示例

### 示例 1: 使用 Bus Parent 的 Spring Boot 项目

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-parent</artifactId>
        <version>8.5.1</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- 核心工具 -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-core</artifactId>
        </dependency>

        <!-- 数据库访问 -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-mapper</artifactId>
        </dependency>

        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 示例 2: 多模块项目

**父 POM:**

```xml
<project>
    <parent>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-parent</artifactId>
        <version>8.5.1</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>project-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <modules>
        <module>module-common</module>
        <module>module-service</module>
        <module>module-web</module>
    </modules>
</project>
```

**子模块 POM:**

```xml
<project>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>project-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>module-common</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-core</artifactId>
        </dependency>
    </dependencies>
</project>
```

-----

## 📋 管理的依赖

Bus Parent POM 管理以下主要依赖的版本:

### 核心框架

* **Spring Framework**: 6.x
* **Spring Boot**: 3.x
* **MyBatis**: 3.5.x
* **MyBatis-Spring**: 3.x

### 数据库

* **Druid**: 1.2.x
* **HikariCP**: 4.x
* **MySQL Connector**: 8.x
* **PostgreSQL**: 42.x

### 工具

* **Lombok**: 最新版
* **Hutool**: 5.x
* **Commons Lang3**: 3.x
* **Guava**: 31.x

### 测试

* **JUnit**: 5.x
* **Mockito**: 4.x
* **TestContainers**: 最新版

-----

## 🔧 配置属性

### Java 版本

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### 源代码编码

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### 构建插件

父 POM 配置以下插件:

* **maven-compiler-plugin**: Java 编译
* **maven-surefire-plugin**: 单元测试
* **maven-failsafe-plugin**: 集成测试
* **maven-source-plugin**: 源代码 JAR 生成
* **maven-javadoc-plugin**: Javadoc 生成
* **maven-gpg-plugin**: 工件签名(用于发布)

-----

## 💡 最佳实践

### 1. 有效使用依赖管理

```xml
<!-- ✅ 推荐: 省略版本，使用父项的版本 -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <!-- 版本由父 POM 管理 -->
</dependency>

<!-- ❌ 不推荐: 硬编码版本 -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.1</version>  <!-- 不必要 -->
</dependency>
```

### 2. 排除不需要的依赖

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

### 3. 使用物料清单 (BOM)

或者，导入 BOM 而不用作父项:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-dependencies</artifactId>
            <version>8.5.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

-----

## ❓ 常见问题

### Q1: 如何覆盖托管依赖版本?

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <version>2.0.0</version>  <!-- 覆盖父项的版本 -->
</dependency>
```

### Q2: 可以将 Bus Parent 与其他父 POM 一起使用吗？

不可以，Maven 只允许一个父项。但是，您可以导入 Bus BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-dependencies</artifactId>
            <version>8.5.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Q3: 如何检查管理了哪些版本？

运行以下命令:

```bash
mvn help:effective-pom
```

这将显示有效 POM 及所有已解析的版本。

### Q4: 需要什么 Java 版本？

Bus Parent 8.x 需要 **Java 17** 或更高版本。

对于 Java 11 支持，请使用 Bus Parent 7.x。

-----

## 🔄 版本兼容性

| Bus Parent 版本 | Java 版本 | Spring Boot 版本 | 状态 |
| :--- | :--- | :--- | :--- |
| **8.x** | 17+ | 3.x | 当前 |
| 7.x | 11+ | 2.x | 维护中 |

-----

## 🔗 相关模块

* **[bus-core](../bus-core)**: 核心工具和基础类
* **[bus-mapper](../bus-mapper)**: MyBatis 增强框架
* **[bus-starter](../bus-starter)**: Spring Boot 启动器
* **[bus-crypto](../bus-crypto)**: 加密和安全工具
* **[bus-logger](../bus-logger)**: 日志框架集成

-----

## 📚 其他资源

* [GitHub 仓库](https://github.com/818000/bus)
* [Maven Central](https://mvnrepository.com/artifact/org.miaixz/bus-parent)
