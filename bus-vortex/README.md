# 🌪️ Bus Vortex: High-Performance API Gateway

<p align="center">
<strong>Distributed, Asynchronous, Scalable, and Lightweight API Gateway</strong>
</p>

-----

## 📖 Project Introduction

**Bus Vortex** is a distributed, fully asynchronous, high-performance, scalable, and lightweight API gateway built on Spring WebFlux. Inspired by Taobao's Open Platform, it stands on the shoulders of the Spring ecosystem to provide enterprise-grade API routing and management capabilities.

-----

## ✨ Core Features

### 🎯 Out-of-the-Box Experience

* **Zero Configuration**: Start using immediately after adding annotations to your business code
* **Automatic Parameter Validation**: Built-in support for JSR-303 internationalized parameter validation
* **Modular Design**: Independent implementation of validation and result return functionality for easy customization
* **Annotation-Driven**: Simple API definitions using annotations for easy maintenance
* **i18n Support**: Built-in internationalization for error messages
* **Digital Signature**: Parameter verification using digital signatures
* **Secure Access**: Platform access via appKey and secret mechanism

### 🛡️ Security & Reliability

* **Signature Verification**: MD5, AES, RSA encryption algorithms for secure data transmission
* **Rate Limiting**: Leaky bucket and token bucket strategies for traffic control
* **Permission Control**: RBAC-based permission verification
* **Session Management**: Support for both standalone and distributed sessions
* **Authentication**: JWT and accessToken support
* **Documentation**: Auto-generated API documentation

### 🌍 Technology Stack

- **Encryption**: MD5, AES, RSA
- **Networking**: Netty (encoding/decoding, long connections, auto-reconnect)
- **Rate Limiting**: Leaky bucket, token bucket algorithms
- **Authorization**: RBAC, validation
- **Session**: Standalone, distributed session management
- **Documentation**: Annotation-based documentation generation
- **Authentication**: JWT, accessToken
- **SDK**: Java, C#, JavaScript
- **Formats**: XML, JSON

-----

## 🚀 Feature 1: Parameter-Based Routing

### API Interface Definition

```java
public class Assets {

    private String id;           // Unique API ID
    private String name;         // API name
    private String host;         // Target hostname
    private int port;            // Target port
    private String url;          // Target URL
    private String method;       // Request method name
    private HttpMethod httpMethod; // HTTP method
    private boolean principal;   // Requires token (0: no, 1: yes)
    private boolean sign;        // Encrypt response (0: no, 1: yes)
    private boolean firewall;    // Firewall (reserved)
    private String version;      // API version (matches request parameter 'v')
    private String description;  // API description
}
```

### Request Parameters

| Parameter | Description |
|:---|:---|
| method | API method name (e.g., xxx.xxx.xxx) |
| v | API version number, used with method (e.g., 1.1, 1.2) |
| format | Return format (supports json, xml) |
| sign | If decrypt is enabled in config and request contains sign field, decrypt request |

### Configuration File

```yaml
extend:
  vortex:
    server:
      port: 8765              # Gateway port
      path: /router/rest      # Gateway path
      encrypt:
        enabled: true         # Enable encryption
        key: xxxxxx           # Encryption key
        type: AES             # Encryption algorithm
        offset: xxxxxx        # Offset
      decrypt:
        enabled: true         # Enable decryption
        key: xxxxxx           # Decryption key
        type: AES             # Decryption algorithm
        offset: xxxx          # Offset
      limit:
        enabled: true         # Enable rate limiting
```

### Integration Steps

#### 1. Add `@EnableVortex` Annotation to Spring Boot Main Class

```java
@EnableVortex
@SpringBootApplication
public class TunnelApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TunnelApplication.class);
        app.run(args);
    }
}
```

#### 2. Implement a Spring Bean with `Registry` to Cache APIs

```java
@Component
public class DbAssetRegistriesImpl implements Registry {
    // TODO: Implement registry logic
}
```

#### 3. Implement an `Authorize` Spring Bean for Authentication

```java
public class AuthProviderImpl implements Authorize {
    // TODO: Implement authentication logic
}
```

#### 4. Configure in `application.yml`

### Extensibility

Implement `WebFilter` to extend gateway functionality, such as rate limiting, logging, blacklisting, circuit breaking (not yet implemented), etc.

```java
@Component
@Order("123")
public class CustomFilter implements WebFilter {
    // TODO: Implement filter logic
}
```

-----

## 🚀 Feature 2: Version-Based Routing

### @ApiVersion

Automatically merges a version-prefixed path to RequestMappingInfo. **Recommendation**: Configure major versions at class level, minor versions can be configured at method level (will override class-level major version).

### @ClientVersion

Routes to different handler methods based on `cv` and `terminal` parameters in request headers (extends `getCustomCondition` method in `RequestMappingHandlerMapping`).

### @VersionMapping

Combines `RequestMapping` functionality with configurations for both `@ApiVersion` and `@ClientVersion`.

### Business Scenarios

- **ApiVersion**: Replaces version-defined paths that require redefining classes or writing conditional logic in code for API upgrades
- **ClientVersion**: Elegantly avoids writing extensive version logic when dealing with interfaces already in use by clients

### Example Usage

```java
@RequestMapping("/t")
@RestController
@ApiVersion("5")
public class TController {
    // Request path: /4/t/get
    @RequestMapping(value = "/get")
    public String get1() {
        return "Old API";
    }

    // Request path: /5.1/t/get
    @RequestMapping(value = "/get", params = "data=tree")
    @ApiVersion("5.1")
    // Method's @ApiVersion takes precedence over class-level, convenient for minor version upgrades
    public String get2() {
        return "New data";
    }

    // All three request paths are /c,
    // Routes to different methods based on client type in header
    // (can be modified to use URL parameters by changing TerminalVersionExpression)
    @GetMapping("/c")
    @ClientVersion(expression = {"1>6.0.0"})
    public String cvcheck1() {
        return "Type 1 client, version 6.0.0+";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.GT, version = "6.0.0")})
    public String cvcheck2() {
        return "Type 2 client, version > 6.0.0";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.LTE, version = "6.0.0")})
    public String cvcheck3() {
        return "Type 2 client, version <= 6.0.0";
    }
}
```

### Using @VersionMapping

```java
@RestController
@VersionMapping(value = "/t", apiVersion = "5")
public class TController {

    @VersionMapping(value = "a", terminalVersion = @TerminalVersion(terminals = 1, op = VersionOperator.EQ, version = "3.0"))
    public String t() {
        return "5";
    }
}
```

-----

## 📋 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-vortex</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Enable Gateway

```java
@EnableVortex
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Configure Application Properties

```yaml
extend:
  vortex:
    server:
      port: 8765
      path: /router/rest
```

-----

## 💡 Use Cases

- **Microservices Gateway**: Unified entry point for microservices architecture
- **API Version Management**: Smooth API upgrades with version-based routing
- **Traffic Control**: Rate limiting and traffic shaping for high-concurrency scenarios
- **Security Enhancement**: Signature verification, encryption, and access control
- **Multi-Tenant Routing**: Route requests based on tenant-specific parameters

-----

## 🔧 Configuration Reference

### Core Configuration

| Property | Type | Default | Description |
|:---|:---|:---|:---|
| extend.vortex.server.port | int | 8765 | Gateway server port |
| extend.vortex.server.path | String | /router/rest | Gateway routing path |
| extend.vortex.encrypt.enabled | boolean | false | Enable encryption |
| extend.vortex.encrypt.key | String | - | Encryption key |
| extend.vortex.encrypt.type | String | AES | Encryption algorithm |
| extend.vortex.decrypt.enabled | boolean | false | Enable decryption |
| extend.vortex.limit.enabled | boolean | false | Enable rate limiting |

-----

## 🔄 Version Compatibility

| Bus Vortex Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## 📊 Performance Characteristics

- **Asynchronous Non-Blocking**: Built on WebFlux for high concurrency
- **Low Latency**: Minimal routing overhead
- **High Throughput**: Efficient request handling
- **Scalable**: Horizontal scaling support

-----

## 🛠️ Advanced Topics

### Custom Filters

Implement `WebFilter` for custom request/response processing:

```java
@Component
@Order(1)
public class LoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Custom logic
        return chain.filter(exchange);
    }
}
```

### Dynamic Routing

Register and update routes dynamically using `Registry` implementation.

### Load Balancing

Integrate with service discovery for automatic load balancing.

-----

## ❓ FAQ

### Q: How to add custom encryption algorithms?

A: Implement the encryption interface and configure it in the application properties.

### Q: How to handle high concurrency?

A: Enable rate limiting and configure thread pools appropriately.

### Q: Can multiple versions coexist?

A: Yes, use `@ApiVersion` and `@ClientVersion` for version-specific routing.

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
