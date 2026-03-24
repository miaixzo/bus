# Bus Framework

<p align="center">
  <strong>A Modern, Modular Java Enterprise Framework — Built for Java 17+</strong>
</p>

<p align="center">
  <a href="https://github.com/818000/bus"><img src="https://img.shields.io/badge/GitHub-miaixz%2Fbus-blue?logo=github" alt="GitHub"/></a>
  <a href="https://mvnrepository.com/artifact/org.miaixz"><img src="https://img.shields.io/maven-central/v/org.miaixz/bus-core?label=Maven%20Central" alt="Maven Central"/></a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"/></a>
  <img src="https://img.shields.io/badge/Java-17%2B-orange" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen" alt="Spring Boot 3.5.x"/>
</p>

---

## What is Bus?

**Bus** is a comprehensive, modular enterprise Java framework providing production-ready components for building modern applications. It covers everything from core utilities and cryptography to HTTP clients, distributed tracing, payment integration, and cloud storage — all with unified configuration and a consistent API style.

Every module is independently usable and integrates seamlessly into Spring Boot applications via `bus-starter`.

---

## Module Overview

| Module | Description |
| :--- | :--- |
| **bus-all** | Meta-module that pulls in all Bus modules as a single dependency |
| **bus-auth** | Enterprise authentication & authorization (OAuth2, JWT, RBAC) |
| **bus-base** | Shared base types, constants, and exception hierarchy |
| **bus-bom** | Bill of Materials — import versions without using Bus as a parent |
| **bus-cache** | Multi-level caching abstraction (Redis, Caffeine, EhCache, …) |
| **bus-core** | Core utilities: reflection, annotation synthesis, type conversion, I/O, and more |
| **bus-cortex** | Unified registry & configuration center for distributed services |
| **bus-crypto** | Cryptographic framework: AES, RSA, SM2/SM3/SM4, digests, encoding |
| **bus-extra** | Additional utilities and integrations beyond the core |
| **bus-gitlab** | GitLab API client and CI/CD integration helpers |
| **bus-health** | Health check endpoints and system health monitoring |
| **bus-http** | High-performance HTTP client with fluent API and retry support |
| **bus-image** | Professional image processing and DICOM medical imaging support |
| **bus-limiter** | Hotspot detection, rate limiting, and degradation |
| **bus-logger** | Universal logging abstraction that bridges all major log frameworks |
| **bus-mapper** | MyBatis enhancement: type handlers, generic CRUD, and query DSL |
| **bus-metrics** | Production-grade metrics collection and reporting |
| **bus-notify** | Multi-channel notification service (email, SMS, DingTalk, WeChat, …) |
| **bus-office** | Office document processing: Excel, Word, PDF |
| **bus-opencv** | OpenCV integration for computer vision tasks |
| **bus-pay** | Universal payment integration (Alipay, WeChat Pay, UnionPay, …) |
| **bus-proxy** | Dynamic proxy and AOP utilities |
| **bus-sensitive** | Data masking and sensitive-field protection |
| **bus-setting** | Unified configuration file reading (properties, YAML, TOML, …) |
| **bus-shade** | Shaded dependency management and relocation |
| **bus-socket** | Non-blocking socket and WebSocket abstractions |
| **bus-starter** | Spring Boot auto-configuration starter for the entire Bus ecosystem |
| **bus-storage** | Unified cloud storage abstraction (S3, OSS, COS, MinIO, …) |
| **bus-tempus** | Time and scheduling utilities: cron expressions, calendar operations |
| **bus-tracer** | Distributed tracing integration (OpenTelemetry, SkyWalking, …) |
| **bus-validate** | Declarative validation framework with annotation support |
| **bus-vortex** | High-performance API gateway and traffic management |

---

## Quick Start

### Option 1 — Use as Parent POM

Inherit from `bus-parent` to get managed dependency versions and build configuration in one step:

```xml
<parent>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-parent</artifactId>
    <version>8.5.9</version>
</parent>
```

Then declare only the modules you need — no version tags required:

```xml
<dependencies>
    <!-- Core utilities -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-core</artifactId>
    </dependency>

    <!-- Spring Boot auto-configuration -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-starter</artifactId>
    </dependency>

    <!-- Encryption / cryptography -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-crypto</artifactId>
    </dependency>
</dependencies>
```

### Option 2 — Import BOM (keeps your own parent)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-bom</artifactId>
            <version>8.5.9</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Option 3 — Add All Modules at Once

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
    <version>8.5.9</version>
</dependency>
```

---

## Usage Examples

### Spring Boot Project (Recommended)

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
        <version>8.5.9</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- Spring Boot auto-configuration for all Bus modules -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-starter</artifactId>
        </dependency>

        <!-- Database access (MyBatis enhancement) -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-mapper</artifactId>
        </dependency>

        <!-- Multi-channel notifications -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-notify</artifactId>
        </dependency>

        <!-- Cloud storage (S3, OSS, COS, …) -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-storage</artifactId>
        </dependency>

        <!-- Distributed tracing -->
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-tracer</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Multi-Module Project

**Parent POM** (`pom.xml`):

```xml
<project>
    <parent>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-parent</artifactId>
        <version>8.5.9</version>
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

**Child module** (`platform-common/pom.xml`):

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

## Build Configuration

`bus-parent` provides the following defaults — override any property in your own POM as needed.

### Java & Encoding

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### Key Managed Versions

| Component | Version |
| :--- | :--- |
| Spring Boot | 3.5.x |
| MyBatis | 3.5.x |
| MyBatis-Spring | 3.0.x |
| Java baseline | 17+ |

### Preconfigured Plugins

| Plugin | Purpose |
| :--- | :--- |
| `maven-compiler-plugin` | Java compilation with `--release 17` |
| `maven-surefire-plugin` | Unit test runner |
| `maven-failsafe-plugin` | Integration test runner |
| `maven-source-plugin` | Source JAR for release |
| `maven-javadoc-plugin` | Javadoc JAR for release |
| `maven-gpg-plugin` | GPG signing for Maven Central |

---

## Best Practices

### Let the Parent Manage Versions

```xml
<!-- Recommended: version is inherited from bus-parent -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
</dependency>

<!-- Avoid: hardcoding the version creates maintenance overhead -->
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.9</version>
</dependency>
```

### Exclude Optional Modules from bus-starter

`bus-starter` is a convenience dependency. Exclude sub-modules you do not use to keep the classpath lean:

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

### Override a Single Dependency Version

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.10</version>  <!-- overrides the parent-managed version -->
</dependency>
```

---

## Frequently Asked Questions

**Q: Can I use Bus with my own Spring Boot parent?**

Yes. Instead of inheriting `bus-parent`, import `bus-bom` inside `<dependencyManagement>` — this gives you all version management without replacing your parent.

**Q: How do I see every managed version?**

```bash
mvn help:effective-pom | grep -A2 'artifactId>bus-'
```

Or for the full resolved POM:

```bash
mvn help:effective-pom
```

**Q: What is the minimum Java version?**

Bus 8.x requires **Java 17** or higher. Older Bus 7.x series supports Java 11.

**Q: Is GraalVM native image supported?**

Yes. `bus-shade` includes the necessary GraalVM reflect-config and resource-config hints for native compilation.

---

## Version Compatibility

| Bus Version | Java | Spring Boot | Status |
| :--- | :--- | :--- | :--- |
| **8.5.x** | 17+ | 3.5.x | **Active** |
| 8.0.x – 8.4.x | 17+ | 3.x | Maintenance |
| 7.x | 11+ | 2.x | End of Life |

---

## License

Bus is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Copyright © 2015–2026 [miaixz.org](https://miaixz.org) and contributors.

---

## Links

* [GitHub Repository](https://github.com/miaixz/bus)
* [Maven Central](https://mvnrepository.com/artifact/org.miaixz)
* [Issue Tracker](https://github.com/miaixz/bus/issues)
* [Changelog](https://github.com/miaixz/bus/releases)
