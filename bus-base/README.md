# 📦 Bus Base: Base Module for Bus Framework

<p align="center">
<strong>Foundation Module Providing Common Utilities and Configurations</strong>
</p>

-----

## 📖 Project Introduction

**Bus Base** is the foundational module of the Bus framework, providing common utilities, shared configurations, and base functionality used across all other Bus modules. It serves as the building block for the entire ecosystem.

-----

## ✨ Core Features

- **Common Utilities**: Shared utility classes and helper methods
- **Base Configurations**: Default configurations for all Bus modules
- **Constant Definitions**: Centralized constants and enums
- **Exception Hierarchy**: Base exception classes for error handling
- **Shared Interfaces**: Common interfaces used across modules

-----

## 🚀 Usage

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-base</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Automatic Inclusion

**Note**: This module is typically included automatically as a transitive dependency when using other Bus modules. You usually don't need to add it explicitly.

-----

## 🔧 Components

### Common Utilities

- String utilities
- Collection utilities
- Date/time utilities
- IO utilities
- Validation utilities

### Base Configurations

- Default charset settings
- Locale configurations
- Time zone settings
- Logging configurations

### Shared Interfaces

- Serializable markers
- Lifecycle interfaces
- Configuration interfaces
- Provider interfaces

-----

## 💡 Use Cases

- As a foundation for custom Bus modules
- Accessing shared utilities across modules
- Standardizing configuration management
- Providing common exception handling

-----

## 🔄 Version Compatibility

| Bus Base Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

-----

## 📄 License

[License information]

-----

## 🔗 Related Documentation

- [Bus Core Documentation](../bus-core/README.md)
