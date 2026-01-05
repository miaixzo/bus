# 🚀 bus-starter: Spring Boot Integration Starter

## 📖 Project Introduction

bus-starter is a comprehensive Spring Boot integration starter that provides automatic configuration and enablement for various enterprise features through simple annotations. It streamlines the integration of common frameworks and services, allowing developers to enable functionality with minimal configuration.

## ✨ Core Features

- **Annotation-Driven**: Enable features with simple `@Enable*` annotations
- **Auto-Configuration**: Automatic setup of beans and configurations
- **Modular Design**: Enable only what you need
- **Zero XML**: Pure Java-based configuration
- **Enterprise-Ready**: Production-tested integrations

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <latestVersion>8.5.0</latestVersion>
</dependency>
```

## 📝 Usage Examples

### Example 1: Enable CORS Support

```java
@SpringBootApplication
@EnableCors
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 2: Enable Response Wrapper

```java
@SpringBootApplication
@EnableWrapper
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 3: Enable MyBatis Mapper

```java
@SpringBootApplication
@EnableMapper
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 4: Enable Dubbo RPC

```java
@SpringBootApplication
@EnableDubbo
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 5: Enable Druid Connection Pool

```java
@SpringBootApplication
@EnableDruid
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 6: Enable Druid Monitoring

```java
@SpringBootApplication
@EnableDruids
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 7: Enable Elasticsearch Integration

```java
@SpringBootApplication
@EnableElastic
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Example 8: Enable Multiple Features

```java
@SpringBootApplication
@EnableCors
@EnableWrapper
@EnableMapper
@EnableDubbo
@EnableCrypto
@EnableValidate
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 🔧 Configuration

### Available Annotations

| Annotation | Description | Dependencies |
|:---|:---|:---|
| `@EnableCors` | Enable Cross-Origin Resource Sharing | None |
| `@EnableWrapper` | Enable response wrapper for unified API responses | None |
| `@EnableMapper` | Enable MyBatis Mapper scanning | MyBatis |
| `@EnableDruid` | Enable Druid connection pool | Druid |
| `@EnableDruids` | Enable Druid monitoring (includes @EnableDruid) | Druid |
| `@EnableDubbo` | Enable Apache Dubbo RPC | Dubbo |
| `@EnableI18n` | Enable internationalization support | None |
| `@EnableSensitive` | Enable data masking and encryption | bus-crypto |
| `@EnableThirdAuth` | Enable third-party authentication (OAuth, etc.) | bus-extra |
| `@EnableStorage` | Enable OSS storage service | bus-storage |
| `@EnableValidate` | Enable parameter validation | javax.validation |
| `@EnableElastic` | Enable Elasticsearch integration | Elasticsearch |
| `@EnableCrypto` | Enable cryptographic operations | bus-crypto |

### Elasticsearch Configuration

**application.yml**:

```yaml
extend:
  elastic:
    hosts: 192.168.100.126:29200
    schema: http
    connect-timeout: 60000
    socket-timeout: 60000
    connection-request-timeout: 60000
    max-connect-total: 2000
    max-connect-per-route: 500
```

**Usage in Service**:

```java
@Service
public class SearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    public SearchResponse search(String index, QueryBuilder query) {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(query);
        request.source(sourceBuilder);

        return restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }
}
```

### Druid Configuration

**application.yml**:

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/database
      username: root
      password: password
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      test-while-idle: true
      validation-query: SELECT 1

# Druid Monitoring (when @EnableDruids is used)
extend:
  druid:
    stat-view-servlet:
      enabled: true
      url-pattern: /druid/*
      login-username: admin
      login-password: admin123
    web-stat-filter:
      enabled: true
      url-pattern: /*
```

Access Druid monitoring at: `http://localhost:8080/druid`

### Dubbo Configuration

**application.yml**:

```yaml
spring:
  application:
    name: demo-service

dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: zookeeper://127.0.0.1:2181
  protocol:
    name: dubbo
    port: 20880
  scan:
    base-packages: org.miaixz.demo.service
```

### CORS Configuration

**application.yml**:

```yaml
extend:
  cors:
    enabled: true
    allowed-origins: "*"
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

## 💡 Best Practices

### 1. Enable Features Selectively

Only enable the features you actually need to reduce startup time and dependencies:

```java
@SpringBootApplication
@EnableCors
@EnableWrapper
@EnableValidate
public class Application {
    // Good: Only what's needed
}
```

### 2. Use Profiles for Different Environments

```yaml
# application-dev.yml
extend:
  druid:
    stat-view-servlet:
      enabled: true

# application-prod.yml
extend:
  druid:
    stat-view-servlet:
      enabled: false
```

### 3. Configure Connection Pools Properly

```yaml
spring:
  datasource:
    druid:
      initial-size: 5        # Initial connections
      min-idle: 10           # Minimum idle connections
      max-active: 100        # Maximum active connections
      max-wait: 60000        # Max wait time (ms)
      test-on-borrow: false  # Test on borrow
      test-on-return: false  # Test on return
      test-while-idle: true  # Test while idle
      validation-query: SELECT 1
```

### 4. Enable Monitoring in Development Only

```java
@Profile("dev")
@EnableDruids
public class DevConfig {
    // Druid monitoring only in dev
}
```

### 5. Use Feature Toggles

```yaml
extend:
  features:
    crypto: ${CRYPTO_ENABLED:false}
    storage: ${STORAGE_ENABLED:true}
```

## ❓ FAQ

### Q: Do I need to add dependencies for @Enable* annotations?

A: Yes, some features require additional dependencies:
- `@EnableMapper` requires MyBatis
- `@EnableDubbo` requires Dubbo
- `@EnableElastic` requires Elasticsearch client
- `@EnableCrypto` requires bus-crypto

### Q: Can I use multiple @Enable* annotations together?

A: Yes! You can combine multiple features:

```java
@EnableCors
@EnableWrapper
@EnableMapper
@EnableDruid
public class Application { }
```

### Q: How do I configure Druid monitoring access control?

A: Configure username and password in application.yml:

```yaml
extend:
  druid:
    stat-view-servlet:
      login-username: admin
      login-password: ${DRUID_PASSWORD}
```

### Q: What's the difference between @EnableDruid and @EnableDruids?

A: `@EnableDruid` enables the Druid connection pool, while `@EnableDruids` enables both the connection pool AND the monitoring web console.

### Q: How do I disable specific features in certain profiles?

A: Use @Profile annotation or configure in profile-specific yml:

```java
@Profile("!prod")
@EnableDruids
public class DevConfig { }
```

### Q: Can I use @EnableWrapper with custom response format?

A: Yes, configure the wrapper format in application.yml:

```yaml
extend:
  wrapper:
    code-field: code
    message-field: message
    data-field: data
```

## 🔍 Advanced Configuration

### Custom Wrapper Response

```java
@Configuration
public class WrapperConfig {
    @Bean
    public WrapperAdvisor wrapperAdvisor() {
        return WrapperAdvisor.builder()
            .codeFieldName("code")
            .messageFieldName("message")
            .dataFieldName("data")
            .successCode(200)
            .build();
    }
}
```

### Elasticsearch Client Customization

```java
@Configuration
public class ElasticConfig {
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("localhost", 9200, "http")
            )
        );
    }
}
```

### Dubbo Provider Configuration

```java
@Service(version = "1.0.0")
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(String id) {
        return userMapper.selectById(id);
    }
}
```

```java
@RestController
public class UserController {
    @Reference(version = "1.0.0")
    private UserService userService;
}
```

## 🔄 Version Compatibility

- **Spring Boot**: 2.7.x, 3.x
- **JDK**: 8, 11, 17, 21+
- **Spring Framework**: 5.3.x, 6.x

## 📚 Related Modules

- [bus-core](../bus-core): Core utilities and basic functionality
- [bus-crypto](../bus-crypto): Cryptographic operations
- [bus-extra](../bus-extra): Extended functionality
- [bus-storage](../bus-storage): Storage service integration
