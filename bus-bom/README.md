# 📦 Bus BOM: Bill of Materials for Bus Framework

<p align="center">
<strong>Dependency Management for Consistent Versioning</strong>
</p>

-----

## 📖 Project Introduction

**Bus BOM** (Bill of Materials) is a Maven POM module that provides centralized dependency management for all Bus framework modules. It ensures version consistency across your project by managing all Bus library dependencies in one place.

-----

## ✨ Core Features

- **Centralized Version Management**: Single source of truth for all Bus library versions
- **Consistent Dependencies**: Ensures compatible versions across all Bus modules
- **Simplified Configuration**: No need to specify versions for individual dependencies
- **Easy Upgrades**: Update all Bus modules by changing a single version
- **Conflict Prevention**: Avoids version conflicts between Bus modules

-----

## 🚀 Usage

### Maven Dependency Management

Import the BOM in your project's `pom.xml`:

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

### Adding Dependencies

After importing the BOM, add Bus dependencies without specifying versions:

```xml
<dependencies>
    <!-- Core module -->
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-core</artifactId>
    </dependency>

    <!-- Additional modules -->
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

### Spring Boot Integration

For Spring Boot projects, you can combine with Spring Boot's BOM:

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

## 📋 Managed Dependencies

The Bus BOM manages versions for all Bus framework modules:

### Core Modules

- **bus-core**: Core utilities and tools
- **bus-base**: Base module
- **bus-extra**: Extension utilities

### Integration Modules

- **bus-mapper**: MyBatis enhancement
- **bus-http**: HTTP client
- **bus-logger**: Logging framework
- **bus-cache**: Caching abstraction
- **bus-validate**: Validation framework

### Security Modules

- **bus-crypto**: Cryptography utilities
- **bus-auth**: Authentication and authorization
- **bus-sensitive**: Sensitive data masking

### Utility Modules

- **bus-image**: Image processing
- **bus-office**: Office document handling
- **bus-cron**: Scheduled tasks
- **bus-pay**: Payment integration
- **bus-notify**: Notification service
- **bus-storage**: Storage abstraction

### Advanced Modules

- **bus-vortex**: API gateway
- **bus-limiter**: Rate limiting and degradation
- **bus-tracer**: Distributed tracing
- **bus-proxy**: Dynamic proxy
- **bus-setting**: Configuration management
- **bus-shade**: JAR encryption
- **bus-starter**: Spring Boot starter
- **bus-parent**: Parent POM
- **bus-gitlab**: GitLab integration
- **bus-opencv**: OpenCV integration
- **bus-socket**: WebSocket support
- **bus-health**: Health checks
- **bus-cron**: Cron scheduler

-----

## 💡 Best Practices

### 1. Always Use BOM

**Recommended**:
```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <!-- No version specified -->
</dependency>
```

**Not Recommended**:
```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.2</version>
</dependency>
```

### 2. Place BOM First

Import the Bus BOM before other dependency management blocks to ensure proper precedence.

### 3. Version Override

If you need to override a specific version (not recommended):

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>8.5.2</version>
</dependency>
```

### 4. Consistent Upgrades

Update all Bus modules simultaneously by changing the BOM version:

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-bom</artifactId>
    <version>8.6.0</version> <!-- Update this version -->
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

-----

## 🔍 Checking Versions

To see which versions are managed by the BOM, you can:

```bash
mvn help:effective-pom
```

Or check the actual `bus-bom/pom.xml` file:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-core</artifactId>
            <version>8.5.2</version>
        </dependency>
        <!-- ... other dependencies ... -->
    </dependencies>
</dependencyManagement>
```

-----

## ⚠️ Troubleshooting

### Version Conflicts

If you encounter version conflicts:

1. Check `mvn dependency:tree` to see which versions are being used
2. Ensure the BOM is imported before other dependency management
3. Check if other libraries are overriding Bus versions
4. Explicitly exclude conflicting dependencies

### Missing Dependencies

If a dependency is not found:

1. Verify the BOM version is correct
2. Check that the module exists in the Bus framework
3. Ensure your Maven repository configuration is correct

-----

## 🔄 Version Compatibility

| Bus BOM Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## ❓ FAQ

### Q: Do I need to use the BOM?

A: While not strictly required, it's highly recommended to ensure version compatibility.

### Q: Can I override individual versions?

A: Yes, but it's not recommended as it may lead to compatibility issues.

### Q: How do I know which version of the BOM to use?

A: Use the latest stable version compatible with your Spring Boot and JDK versions.

### Q: What if I need multiple versions of Bus modules?

A: This is not recommended. Use a single BOM version for all Bus dependencies.

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

-----

## 📄 License

[License information]

-----

## 🔗 Related Documentation

- [Bus Framework Documentation](https://www.miaixz.org)
- [Maven BOM Documentation](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [Bus Parent POM](../bus-parent/README.md)
