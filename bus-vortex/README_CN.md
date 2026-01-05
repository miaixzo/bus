# 🌪️ Bus Vortex: 高性能 API 网关

<p align="center">
<strong>分布式、异步、可扩展、轻量级 API 网关</strong>
</p>

-----

## 📖 项目介绍

**Bus Vortex** 是一个基于 Spring WebFlux 构建的分布式、完全异步、高性能、可扩展且轻量级的 API 网关。受淘宝开放平台启发,它站在 Spring 生态系统的肩膀上,提供企业级 API 路由和管理能力。

-----

## ✨ 核心特性

### 🎯 开箱即用

* **零配置**: 在业务代码中添加注解后立即可用
* **自动参数验证**: 内置支持 JSR-303 国际化参数验证
* **模块化设计**: 独立实现验证和结果返回功能,易于定制
* **注解驱动**: 使用注解定义简单 API,易于维护
* **国际化支持**: 错误消息内置国际化
* **数字签名**: 使用数字签名进行参数验证
* **安全访问**: 通过 appKey 和 secret 机制实现平台访问

### 🛡️ 安全与可靠性

* **签名验证**: MD5、AES、RSA 加密算法确保数据传输安全
* **限流**: 漏桶和令牌桶策略进行流量控制
* **权限控制**: 基于 RBAC 的权限验证
* **会话管理**: 支持独立和分布式会话
* **身份验证**: JWT 和 accessToken 支持
* **文档**: 自动生成 API 文档

### 🌍 技术栈

- **加密**: MD5、AES、RSA
- **网络**: Netty(编解码、长连接、自动重连)
- **限流**: 漏桶、令牌桶算法
- **授权**: RBAC、验证
- **会话**: 独立、分布式会话管理
- **文档**: 基于注解的文档生成
- **身份验证**: JWT、accessToken
- **SDK**: Java、C#、JavaScript
- **格式**: XML、JSON

-----

## 🚀 功能 1: 基于参数的路由

### API 接口定义

```java
public class Assets {
    private String id;           // 唯一 API ID
    private String name;         // API 名称
    private String host;         // 目标主机名
    private int port;            // 目标端口
    private String url;          // 目标 URL
    private String method;       // 请求方法名称
    private HttpMethod httpMethod; // HTTP 方法
    private boolean principal;   // 需要令牌(0: 否, 1: 是)
    private boolean sign;        // 加密响应(0: 否, 1: 是)
    private boolean firewall;    // 防火墙(保留)
    private String version;      // API 版本(匹配请求参数 'v')
    private String description;  // API 描述
}
```

### 请求参数

| 参数 | 描述 |
|:---|:---|
| method | API 方法名称(例如 xxx.xxx.xxx)|
| v | API 版本号,与 method 一起使用(例如 1.1, 1.2)|
| format | 返回格式(支持 json、xml)|
| sign | 如果配置中启用 decrypt 且请求包含 sign 字段,则解密请求 |

### 配置文件

```yaml
extend:
  vortex:
    server:
      port: 8765              # 网关端口
      path: /router/rest      # 网关路径
      encrypt:
        enabled: true         # 启用加密
        key: xxxxxx           # 加密密钥
        type: AES             # 加密算法
        offset: xxxxxx        # 偏移量
      decrypt:
        enabled: true         # 启用解密
        key: xxxxxx           # 解密密钥
        type: AES             # 解密算法
        offset: xxxx          # 偏移量
      limit:
        enabled: true         # 启用限流
```

### 集成步骤

#### 1. 在 Spring Boot 主类上添加 @EnableVortex 注解

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

#### 2. 实现带有 Registry 的 Spring Bean 来缓存 API

```java
@Component
public class DbAssetRegistriesImpl implements Registry {
    // TODO: 实现注册表逻辑
}
```

#### 3. 实现 Authorize Spring Bean 进行身份验证

```java
public class AuthProviderImpl implements Authorize {
    // TODO: 实现身份验证逻辑
}
```

#### 4. 在 application.yml 中配置

### 扩展性

实现 WebFilter 来扩展网关功能,如限流、日志、黑名单、熔断(尚未实现)等。

```java
@Component
@Order("123")
public class CustomFilter implements WebFilter {
    // TODO: 实现过滤器逻辑
}
```

-----

## 🚀 功能 2: 基于版本的路由

### @ApiVersion

自动将版本前缀路径合并到 RequestMappingInfo。**建议**: 在类级别配置主要版本,可以在方法级别配置次要版本(将覆盖类级别的主要版本)。

### @ClientVersion

根据请求头中的 `cv` 和 `terminal` 参数路由到不同的处理方法(扩展 `RequestMappingHandlerMapping` 中的 `getCustomCondition` 方法)。

### @VersionMapping

结合 `RequestMapping` 功能与 `@ApiVersion` 和 `@ClientVersion` 的配置。

### 业务场景

- **ApiVersion**: 替代需要重新定义类或在代码中编写条件逻辑的版本定义路径进行 API 升级
- **ClientVersion**: 优雅地避免在处理已被客户端使用的接口时编写大量版本逻辑

### 示例用法

```java
@RequestMapping("/t")
@RestController
@ApiVersion("5")
public class TController {
    // 请求路径: /4/t/get
    @RequestMapping(value = "/get")
    public String get1() {
        return "旧 API";
    }

    // 请求路径: /5.1/t/get
    @RequestMapping(value = "/get", params = "data=tree")
    @ApiVersion("5.1")
    // 方法的 @ApiVersion 优先于类级别,便于次要版本升级
    public String get2() {
        return "新数据";
    }

    // 所有三个请求路径都是 /c,
    // 根据请求头中的客户端类型路由到不同方法
    @GetMapping("/c")
    @ClientVersion(expression = {"1>6.0.0"})
    public String cvcheck1() {
        return "类型 1 客户端,版本 6.0.0+";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.GT, version = "6.0.0")})
    public String cvcheck2() {
        return "类型 2 客户端,版本 > 6.0.0";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.LTE, version = "6.0.0")})
    public String cvcheck3() {
        return "类型 2 客户端,版本 <= 6.0.0";
    }
}
```

### 使用 @VersionMapping

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

## 📋 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-vortex</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 启用网关

```java
@EnableVortex
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 配置应用属性

```yaml
extend:
  vortex:
    server:
      port: 8765
      path: /router/rest
```

-----

## 💡 使用场景

- **微服务网关**: 微服务架构的统一入口
- **API 版本管理**: 通过基于版本的路由实现平滑的 API 升级
- **流量控制**: 高并发场景下的限流和流量整形
- **安全增强**: 签名验证、加密和访问控制
- **多租户路由**: 基于租户特定参数路由请求

-----

## 🔧 配置参考

### 核心配置

| 属性 | 类型 | 默认值 | 描述 |
|:---|:---|:---|:---|
| extend.vortex.server.port | int | 8765 | 网关服务器端口 |
| extend.vortex.server.path | String | /router/rest | 网关路由路径 |
| extend.vortex.encrypt.enabled | boolean | false | 启用加密 |
| extend.vortex.encrypt.key | String | - | 加密密钥 |
| extend.vortex.encrypt.type | String | AES | 加密算法 |
| extend.vortex.decrypt.enabled | boolean | false | 启用解密 |
| extend.vortex.limit.enabled | boolean | false | 启用限流 |

-----

## 🔄 版本兼容性

| Bus Vortex 版本 | Spring Boot 版本 | JDK 版本 |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## 📊 性能特征

- **异步非阻塞**: 基于 WebFlux 实现高并发
- **低延迟**: 最小的路由开销
- **高吞吐量**: 高效的请求处理
- **可扩展**: 支持水平扩展

-----

## 🛠️ 高级主题

### 自定义过滤器

实现 WebFilter 进行自定义请求/响应处理:

```java
@Component
@Order(1)
public class LoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 自定义逻辑
        return chain.filter(exchange);
    }
}
```

### 动态路由

使用 Registry 实现动态注册和更新路由。

### 负载均衡

与服务发现集成实现自动负载均衡。

-----

## ❓ FAQ

### Q: 如何添加自定义加密算法?

A: 实现加密接口并在应用属性中配置。

### Q: 如何处理高并发?

A: 启用限流并适当配置线程池。

### Q: 可以同时存在多个版本吗?

A: 可以,使用 @ApiVersion 和 @ClientVersion 进行特定于版本的路由。

-----

## 🤝 贡献

欢迎贡献!请随时提交拉取请求。

-----

**由 Miaixz 团队用 ❤️ 构建**
