# 🚀 bus-starter: Spring Boot 集成启动器

## 📖 项目介绍

bus-starter 是一个全面的 Spring Boot 集成启动器，通过简单的注解为各种企业功能提供自动配置和启用。它简化了常见框架和服务的集成，允许开发者通过最少的配置启用功能。

## ✨ 核心特性

- **注解驱动**: 使用简单的 `@Enable*` 注解启用功能
- **自动配置**: Bean 和配置的自动设置
- **模块化设计**: 仅启用所需功能
- **零 XML**: 纯基于 Java 的配置
- **企业级**: 经过生产测试的集成

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <latestVersion>8.x.x</latestVersion>
</dependency>
```

## 📝 使用示例

### 示例 1: 启用 CORS 支持

```java
@SpringBootApplication
@EnableCors
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 示例 2: 启用响应包装器

```java
@SpringBootApplication
@EnableWrapper
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Wrapper 兼容模式配置

`@EnableWrapper` 会启用一条兼容现有项目的请求处理链，包含：

- request body 缓存与重复读取
- 非简单类型 controller 参数自动解析
- 原始 body 为空时按需合成 form body
- 参数与 header 的可选输入净化

推荐配置：

```yaml
bus:
  wrapper:
    enabled: true
    sanitize-input-values: true
    synthesize-form-body: true
    resolve-non-simple-arguments: true
    wrap-content-types: all
    include-multipart: true
```

兼容性说明：

- `synthesize-form-body=true` 时，保留当前从 `parameterMap` 合成 form body 的行为，同时允许
  `ContextBuilder.getParameters()` 再从该 body 反向恢复参数。
- `sanitize-input-values=true` 时，保留当前 wrapper 层对参数和 header 的 HTML escape 行为。
- `resolve-non-simple-arguments=true` 时，保留当前 `CompositeArgumentResolver` 对裸 DTO 参数的解析行为。
- `wrap-content-types` 支持 `all`、`json-form`、`json-only`。

迁移建议：

- 现有项目建议先保持 legacy 默认值，避免无测试前提下直接翻转行为。
- 新项目可以在有回归验证的前提下逐步关闭 legacy 行为。

### 示例 3: 启用 MyBatis Mapper

```java
@SpringBootApplication
@EnableMapper
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 示例 4: 启用 Dubbo RPC

```java
@SpringBootApplication
@EnableDubbo
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 示例 5: 启用 Druid 连接池

```java
@SpringBootApplication
@EnableDruid
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 示例 6: 启用 Druid 监控

```java
@SpringBootApplication
@EnableDruids
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 示例 7: 启用 Elasticsearch 集成

```java
@SpringBootApplication
@EnableElastic
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 示例 8: 启用多个功能

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

## 🔧 配置

### 可用注解

| 注解 | 描述 | 依赖 |
|:---|:---|:---|
| `@EnableCors` | 启用跨域资源共享 | 无 |
| `@EnableWrapper` | 启用统一 API 响应的响应包装器 | 无 |
| `@EnableMapper` | 启用 MyBatis Mapper 扫描 | MyBatis |
| `@EnableDruid` | 启用 Druid 连接池 | Druid |
| `@EnableDruids` | 启用 Druid 监控(包括 @EnableDruid) | Druid |
| `@EnableDubbo` | 启用 Apache Dubbo RPC | Dubbo |
| `@EnableI18n` | 启用国际化支持 | 无 |
| `@EnableSensitive` | 启用数据脱敏和加密 | bus-crypto |
| `@EnableThirdAuth` | 启用第三方认证(OAuth 等) | bus-extra |
| `@EnableStorage` | 启用 OSS 存储服务 | bus-storage |
| `@EnableValidate` | 启用参数验证 | javax.validation |
| `@EnableElastic` | 启用 Elasticsearch 集成 | Elasticsearch |
| `@EnableCrypto` | 启用加密操作 | bus-crypto |

### Elasticsearch 配置

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

**在服务中使用**:

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

### Druid 配置

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

# Druid 监控(使用 @EnableDruids 时)
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

访问 Druid 监控: `http://localhost:8080/druid`

### Dubbo 配置

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

### CORS 配置

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

## 💡 最佳实践

### 1. 选择性启用功能

仅启用您实际需要的功能以减少启动时间和依赖:

```java
@SpringBootApplication
@EnableCors
@EnableWrapper
@EnableValidate
public class Application {
    // 好: 仅所需功能
}
```

### 2. 为不同环境使用配置文件

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

### 3. 正确配置连接池

```yaml
spring:
  datasource:
    druid:
      initial-size: 5        # 初始连接数
      min-idle: 10           # 最小空闲连接
      max-active: 100        # 最大活动连接
      max-wait: 60000        # 最大等待时间(毫秒)
      test-on-borrow: false  # 借用时测试
      test-on-return: false  # 归还时测试
      test-while-idle: true  # 空闲时测试
      validation-query: SELECT 1
```

### 4. 仅在开发中启用监控

```java
@Profile("dev")
@EnableDruids
public class DevConfig {
    // 仅在开发中使用 Druid 监控
}
```

### 5. 使用功能切换

```yaml
extend:
  features:
    crypto: ${CRYPTO_ENABLED:false}
    storage: ${STORAGE_ENABLED:true}
```

## ❓ 常见问题

### 问: @Enable* 注解需要添加依赖吗？

答: 是的，某些功能需要额外依赖:
- `@EnableMapper` 需要 MyBatis
- `@EnableDubbo` 需要 Dubbo
- `@EnableElastic` 需要 Elasticsearch 客户端
- `@EnableCrypto` 需要 bus-crypto

### 问: 可以同时使用多个 @Enable* 注解吗？

答: 可以！您可以组合多个功能:

```java
@EnableCors
@EnableWrapper
@EnableMapper
@EnableDruid
public class Application { }
```

### 问: 如何配置 Druid 监控访问控制？

答: 在 application.yml 中配置用户名和密码:

```yaml
extend:
  druid:
    stat-view-servlet:
      login-username: admin
      login-password: ${DRUID_PASSWORD}
```

### 问: @EnableDruid 和 @EnableDruids 有什么区别？

答: `@EnableDruid` 启用 Druid 连接池，而 `@EnableDruids` 启用连接池和监控 Web 控制台。

### 问: 如何在某些配置文件中禁用特定功能？

答: 使用 @Profile 注解或在特定配置文件的 yml 中配置:

```java
@Profile("!prod")
@EnableDruids
public class DevConfig { }
```

### 问: @EnableWrapper 可以使用自定义响应格式吗？

答: 可以，在 application.yml 中配置包装器格式:

```yaml
extend:
  wrapper:
    code-field: code
    message-field: message
    data-field: data
```

## 🔍 高级配置

### 自定义包装器响应

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

### Elasticsearch 客户端自定义

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

### Dubbo 提供者配置

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

## 🔄 版本兼容性

- **Spring Boot**: 2.7.x, 3.x
- **JDK**: 8, 11, 17, 21+
- **Spring Framework**: 5.3.x, 6.x

## 📚 相关模块

- [bus-core](../bus-core): 核心工具和基本功能
- [bus-crypto](../bus-crypto): 加密操作
- [bus-extra](../bus-extra): 扩展功能
- [bus-storage](../bus-storage): 存储服务集成
