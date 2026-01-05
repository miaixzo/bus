# 📦 Bus Parent: Parent POM Configuration

<p align="center">
<strong>Centralized Dependency Management and Build Configuration for Bus Framework</strong>
</p>

-----

## 📖 Project Introduction

**Bus Parent** is the parent POM (Project Object Model) configuration module for the Bus framework. It provides centralized dependency management, unified build configurations, and version control for all Bus framework modules.

This module consists only of a `pom.xml` file and provides `dependencyManagement` declarations to ensure consistency across all sub-modules.

-----

## ✨ Core Features

* **Centralized Dependency Management**: All third-party library versions are managed in one place
* **Unified Build Configuration**: Consistent build settings across all modules
* **Version Control**: Single source of truth for framework and dependency versions
* **Plugin Management**: Standardized plugin configurations (compiler, surefire, etc.)
* **Repository Management**: Centralized Maven repository configuration
* **Quality Control**: Integrated code quality and analysis plugins

-----

## 🚀 Quick Start

### Maven Parent Configuration

To use Bus framework modules with Spring Boot style dependency management, add the Bus parent to your project's parent POM:

```xml
<parent>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-parent</artifactId>
    <version>8.5.1</version>
</parent>
```

### Add Dependencies in Sub-modules

After configuring the parent, you can add specific Bus modules without specifying versions:

```xml
<!-- Add all Bus modules -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
</dependency>
```

Or add individual modules as needed:

```xml
<!-- Add specific modules -->
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

## 📝 Usage Examples

### Example 1: Spring Boot Project with Bus Parent

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
        <!-- Core utilities -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-core</artifactId>
        </dependency>

        <!-- Database access -->
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

### Example 2: Multi-Module Project

**Parent POM:**

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

**Child Module POM:**

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

## 📋 Managed Dependencies

The Bus Parent POM manages versions for the following major dependencies:

### Core Framework

* **Spring Framework**: 6.x
* **Spring Boot**: 3.x
* **MyBatis**: 3.5.x
* **MyBatis-Spring**: 3.x

### Database

* **Druid**: 1.2.x
* **HikariCP**: 4.x
* **MySQL Connector**: 8.x
* **PostgreSQL**: 42.x

### Utilities

* **Lombok**: Latest
* **Hutool**: 5.x
* **Commons Lang3**: 3.x
* **Guava**: 31.x

### Testing

* **JUnit**: 5.x
* **Mockito**: 4.x
* **TestContainers**: Latest

-----

## 🔧 Configuration Properties

### Java Version

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### Source Encoding

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### Build Plugins

The parent POM configures the following plugins:

* **maven-compiler-plugin**: Java compilation
* **maven-surefire-plugin**: Unit testing
* **maven-failsafe-plugin**: Integration testing
* **maven-source-plugin**: Source JAR generation
* **maven-javadoc-plugin**: Javadoc generation
* **maven-gpg-plugin**: Artifact signing (for releases)

-----

## 💡 Best Practices

### 1. Use Dependency Management Effectively

```xml
<!-- ✅ Recommended: Omit version, use parent's version -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <!-- Version managed by parent POM -->
</dependency>

<!-- ❌ Not Recommended: Hardcode version -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.1</version>  <!-- Unnecessary -->
</dependency>
```

### 2. Exclude Unwanted Dependencies

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

### 3. Use Bill of Materials (BOM)

Alternatively, import the BOM without using as parent:

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

## ❓ Frequently Asked Questions

### Q1: How do I override a managed dependency version?

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <version>2.0.0</version>  <!-- Override parent's version -->
</dependency>
```

### Q2: Can I use Bus Parent with other parent POMs?

No, Maven only allows one parent. However, you can import the Bus BOM:

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

### Q3: How do I check which versions are managed?

Run the following command:

```bash
mvn help:effective-pom
```

This will show the effective POM with all resolved versions.

### Q4: What Java version is required?

Bus Parent 8.x requires **Java 17** or higher.

For Java 11 support, use Bus Parent 7.x.

-----

## 🔄 Version Compatibility

| Bus Parent Version | Java Version | Spring Boot Version | Status |
| :--- | :--- | :--- | :--- |
| **8.x** | 17+ | 3.x | Current |
| 7.x | 11+ | 2.x | Maintenance |

-----

## 🔗 Related Modules

* **[bus-core](../bus-core)**: Core utilities and base classes
* **[bus-mapper](../bus-mapper)**: MyBatis enhancement framework
* **[bus-starter](../bus-starter)**: Spring Boot starter
* **[bus-crypto](../bus-crypto)**: Encryption and security utilities
* **[bus-logger](../bus-logger)**: Logging framework integration

-----

## 📚 Additional Resources

* [GitHub Repository](https://github.com/818000/bus)
* [Maven Central](https://mvnrepository.com/artifact/org.miaixz/bus-parent)
