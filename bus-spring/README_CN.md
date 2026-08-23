# bus-spring

`bus-spring` 是 Bus 的可重用 Spring 集成层。它提供应用程序上下文拥有的Bean服务，
运行时上下文捕获和传播、Spring Boot 生命周期实用程序、注释助手和可重用的 Servlet MVC
基础设施。该模块提供可重复使用的条件机制，而不是产品功能决策；具体启用
注释、属性前缀、条件汇编和配置属性属于 `bus-starter`。

## 责任边界

```text
bus-spring                              bus-starter
-----------------------------------     -----------------------------------
Spring and Web integration mechanics    discovery and startup assembly
context capture/install/restore         bus.* configuration properties
Bean, environment, and condition APIs   feature activation decisions
Boot listener implementations           default Bean declarations
request/converter/wrapper primitives     feature-specific integration
```

这种分离使 Spring 实用程序可重用，而无需将每个Bus功能强加到应用程序中。 `bus-spring`
不包含 `spring.factories` 注册资源； `bus-starter` 拥有发现并引用生命周期
此处提供的实现。

## 依赖关系

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-spring</artifactId>
    <version>${revision}</version>
</dependency>
```

应用程序通常通过 `bus-starter` 传递接收该模块。只需要复用的时候就直接添加
需要 Spring API，并且应用程序将提供激活。

## 包结构

| 封装 | 责任 |
|-------------------------|------------------------------------------------------------------------------------------------------|
| `org.miaixz.bus.spring` | 运行时上下文 API 和 `SpringBuilder` 外观。                                                 |
| `annotation` | 合并注释处理、占位符绑定、包装器注释和 `@RequestObject`。          |
| `aop` | 具有 Bean 名称排除的可重用自动代理基础结构。                                        |
| `bean` | 聚焦 Bean 查找、注册、元数据、环境、上下文和提供者服务。            |
| `jdbc` | 可重用的数据源解析、池创建、动态路由、路由范围、注释和建议。 |
| `web` | 根 Servlet 请求访问和上下文绑定过滤器。                                              |
| `web.advice` | 可重用的 MVC 响应建议库实现。                                                    |
| `web.converter` | JSON/文本转换器、类型匹配、注册和 MVC 配置器。                              |
| `web.interceptor` | 请求拦截助手。                                                                        |
| `web.resolver` | 显式请求对象参数绑定和绑定选项。                                        |
| `web.routing` | 控制器路由前缀映射。                                                                     |
| `web.wrapper` | 有界请求/响应正文缓存包装器和过滤器。                                           |
| `boot` | Spring Boot 运行监听器和智能生命周期基类。                                             |
| `boot.banner` | 文本、图像和版本横幅选择和渲染。                                             |
| `boot.condition` | 可重用注解-优先 Spring Boot 激活条件。                                          |
| `boot.environment` | 早期 Spring Boot 和日志记录环境处理器。                                                |
| `boot.listener` | Spring Boot 配置监听器。                                                                 |
| `boot.startup` | 启动阶段、指标、报告器和 Bean 后处理。                                        |

根包有意保持填充状态。 `ContextBuilder`、`ContextManager`、`ContextProvider`、`ContextState`、
`ContextScope`、`ContextDecorator` 和 `SpringBuilder` 是稳定的公共功能，而不是空命名空间。

`boot.condition` 提供 `@ConditionalOnEnabled` 和 `EnabledCondition`。该条件给出了显式启用
注解优先于相应的属性，同时将具体的注解和属性前缀留给
依赖 Starter 模块。其 `name` 成员默认为 `enabled`，`matchIfMissing` 默认为 `false`，
条件可以保护配置类型或单个 Bean 方法。

## JDBC 数据源基础设施

`org.miaixz.bus.spring.jdbc.DataSource` 是用于在服务边界选择数据源的公共 Spring 合约。
它的值必须标识已解析的数据源路由。 `DataSourceResolver`、`DataSourceDefinition` 和
`DataSourceMapping` 将兼容属性前缀的有序列表解析为一个经过验证的映射；
`DataSourceFactory` 创建配置的池； `DynamicDataSource`执行路由；每个应用程序上下文都拥有一个
独立的 `DataSourceHolder` 用于精确的嵌套路由范围；并且 `DataSourceListener` 报告初始成功，
添加、替换和删除路线。 `AspectjJdbcProxy`
在事务通知获得连接之前解释注释。这些类型独立于Mapper并且
起动机总成。 `bus-starter` 仅提供支持的前缀顺序、默认池类型和 Spring Bean。

## 运行时上下文模型

运行时状态由每个 Spring 应用程序上下文的一个 `ContextManager` 拥有。它不存储在全局静态中
应用程序上下文注册表。

| 类型 | 责任 |
|--------------------|----------------------------------------------------------------------------------------------------------------------|
| `ContextManager` | 拥有当前线程状态并执行捕获、安装、恢复和清除操作。                          |
| `ContextState` | 不可变的分离快照，包含请求 ID、防御性授权副本和解析的凭证元数据。 |
| `ContextBuilder` | 用于请求 ID、授权、租户、凭证、令牌和 API 密钥访问的公共外观。                         |
| `ContextScope` | `AutoCloseable` 一次恢复先前状态的防护。                                                 |
| `ContextDecorator` | Spring `TaskDecorator` 将捕获的状态传播到执行程序任务。                                             |
| `ContextProvider` | 订购的可提供授权状态的扩展点。                                                         |

`ContextState` 从不保留 `HttpServletRequest`、缓存主体、多部分数据或线程本地容器。代币和
API 密钥凭证在 Servlet 边界解析一次，并仅保留为不可变凭证值
编辑诊断。这使得快照适合有界异步传播。

### 捕获并安装

```java
ContextState state = contextBuilder.capture();

try (ContextScope ignored = contextBuilder.install(state)) {
    operation.run();
}
```

关闭作用域会恢复工作线程之前的状态，包括操作抛出时的状态。

### 执行器传播

```java
Runnable decorated = contextDecorator.decorate(() -> service.process(command));
executor.execute(decorated);
```

`ContextDecorator` 在装饰时捕获，执行前安装，执行后恢复。和
当 Spring Boot 任务类存在时，`bus-starter`、`TaskConfiguration` 默认注册此装饰器。它可以
通过 `bus.context.task.enabled=false` 禁用。

### 上下文访问

```java
String requestId = contextBuilder.getRequestId();
String tenantId = contextBuilder.getTenantId();
String token = contextBuilder.getToken();
String apiKey = contextBuilder.getApiKey();
Http.Auth.Credential credential = contextBuilder.getCredential();
```

令牌和 API 密钥值独立存储。当两者都存在时，`getCredential()` 更喜欢令牌，而
`getToken()`和`getApiKey()`继续曝光各自的数值。分辨率如下 `Http.Auth`：标头，
参数、可用的缓存 JSON 正文和 cookie。原始请求主体永远不会被上下文解析消耗。

在拥有当前线程状态的集成边界处使用 `clear()`。它删除了请求 ID、授权、
当前线程拥有的令牌和 API 密钥状态。

## Spring Bean 服务

`SpringBuilder` 是一个提供六项重点服务的便利外观：

| 服务 | 责任 |
|-----------------------|-----------------------------------------------------------------------------------------|
| `SpringContext` | 持有所属 `ApplicationContext` 并发布事件。                             |
| `BeanProvider` | 按名称、类或 `TypeReference` 读取 Bean。                                         |
| `BeanRegistry` | 注册和删除 Bean 定义和单例。                                  |
| `BeanMetadata` | 解析 Bean 类型和配置源来源，没有实例化副作用。 |
| `EnvironmentResolver` | 解析属性、配置文件、应用程序名称、模式和占位符。               |
| `ProviderRegistry` | 发现有序Bus提供程序并将其缓存在所属上下文中。              |

### 查找

```java
OrderService service = springBuilder.getBean(OrderService.class);
Map<String, Validator> validators = springBuilder.getBeansOfType(Validator.class);
String[] names = springBuilder.getBeanNamesForType(Validator.class);
```

### 环境

```java
String applicationName = springBuilder.getApplicationName();
String profile = springBuilder.getActiveProfile();
String endpoint = springBuilder.replacePlaceholders("${service.endpoint}");

if (springBuilder.isProdMode()) {
    // production-specific application behavior
}
```

### 注册

```java
springBuilder.registerBeanDefinition("orderValidator", OrderValidator.class);
springBuilder.registerSingleton("clock", Clock.class, Clock.systemUTC());
```

Mutation API 旨在用于拥有 Bean 生命周期的基础设施。业务代码应该优先选择构造函数
注射。当只需要一项职责时，直接使用聚焦服务；当有多个时使用 `SpringBuilder`
真正需要一起提供服务。

## 注解工具

- `AnnotationWrapper` 和 `WrapperAnnotation` 支持合并或包装注释访问。
- `PlaceholderBinder`、`DefaultPlaceholderBinder` 和 `PlaceholderHandler` 解析包含以下内容的注释属性
环境占位符。
- `@RequestObject` 显式选择控制器方法参数到Bus请求对象绑定中。

`@RequestObject` 是故意明确的。解析器不会自动声明每个复杂的 MVC 参数。

## Servlet 上下文绑定

`ContextBindingFilter` 为请求建立运行时状态并为所有支持的调度路径恢复它，
包括正常、异步和错误调度。 `RequestContext` 提供对请求值的结构化访问：

- 标头和授权数据；
- 查询和表单参数；
- 饼干；
- 路径变量；
- 多部分值；
- 缓存的 JSON 正文字段。

通过 `bus-starter`，`WebConfiguration` 始终为 Servlet 集成提供一个可替换的 `RequestContext` Bean
并默认注册过滤器。显式禁用上下文绑定：

```yaml
bus:
  context:
    web:
      enabled: false
```

过滤器是可重复使用的基础设施；特定于功能的请求建议保留在相关的入门包中。

## 请求对象绑定

解析器包包含：

| 类型 | 责任 |
|---------------------------------|------------------------------------------------------------------------|
| `RequestObjectArgumentResolver` | 根据请求值构建显式选择的控制器参数。 |
| `AutoBindingTypeMatcher` | 确定哪些类型可以参与绑定。                     |
| `RequestBindingOptions` | 不可变的绑定行为和限制。                                 |
| `BindingDefaults` | 共享安全默认值。                                                  |
| `RequestWebMvcConfigurer` | 将解析器安装到 MVC 中。                                        |

框架类型和简单标量参数被排除。租户身份必须来自经过身份验证的运行时上下文
并且不能被请求输入覆盖。

## HTTP 消息转换

转换器包提供：

- `AbstractHttpMessageConverter`作为通用转换器底座；
- `JsonMessageConverter` 用于Bus JSON 提供商；
- `JsonTypeMatcher` 用于支持的 JSON 目标；
- `MessageConverterRegistrar` 用于确定性转换器放置；
- 用于 MVC 集成的 `JsonWebMvcConfigurer` 和 `TextWebMvcConfigurer`。

`bus-spring` 提供这些机制，但不会自动激活它们。 Starter 的包装转换器
配置拥有激活和条件。

## Body 缓存

`CachedBodyRequestWrapper` 和 `CachedBodyResponseWrapper` 公开可重复的有界体。 `CachedBodyFilter`适用
他们使用`BodyCacheOptions`。

安全规则：

- 请求和响应缓存大小是有限的；
- 多部分缓存是可选的；
- 仅包装支持的 HTTP 方法；
- 响应诊断缓存独立于响应传送；
- 包装器是请求范围的，并且永远不会输入 `ContextState`；
- 启用后，请求正文缓存在上下文绑定之前运行，因此可以在不消耗资源的情况下解析 JSON 凭据
原始 Servlet 流。

即使启用了聚合包装器支持，Starter 默认情况下也会禁用正文缓存。

## Response Advice 与路由

- `MessageResponseBodyAdvice` 提供可重用的 MVC 响应处理。
- `RoutePrefixHandlerMapping` 将配置的前缀应用于选定的控制器路由。
- `RoutePrefixOptions` 表示与 Starter 属性激活无关的路由前缀机制。
- `SentinelRequestHandler` 提供可重用的请求拦截集成。

这些包不决定应用程序是否启用响应包装、路由前缀或可选的
第三方功能。

## Spring Boot 生命周期

引导包包含应用程序启动之前和期间执行的基础结构：

- `SpringApplicationRunListener` 将Bus启动阶段与 Spring Boot 集成；
- `SpringSmartLifecycle`提供可重用的生命周期基础；
- 环境处理器准备日志记录和 Spring Boot 配置；
- 配置监听器观察 Spring Boot 配置阶段；
- 横幅实现呈现文本、图像和版本信息；
- 启动指标捕获稳定的顶级应用程序阶段；
- `SpringStartupPublisher` 通过Bus等可选集成发布完整的 Spring 启动摘要
指标提供者。

发现仍然集中在 `bus-starter/src/main/resources/META-INF/spring.factories` 中。不要添加第二个
发现文件至 `bus-spring`。

### 日志命名空间桥接

`LoggingEnvironmentPostProcessor` 将每个 `bus.logging.*` 属性映射到 Spring Boot 的本机 `logging.*` 命名空间
加载配置数据后。 Spring Boot 仍然拥有日志记录模型，因此支持的任何属性
`logging.*` 可以用 `bus.logging.*` 前缀编写。当两个名字都出现时，`bus.logging.*` 获胜。

例子：

```yaml
bus:
  logging:
    pattern:
      defaults: true
      console: "%d %-5level %logger - %msg%n"
    level:
      root: INFO
      org.miaixz: DEBUG
    file:
      name: app.log
```

`bus.logging.pattern.defaults` 默认为 `true`。启用后，Bus会贡献低优先级的内置控制台和文件
记录模式。将其设置为 `false` 将模式留给 Spring Boot。显式的
`bus.logging.pattern.console`、`bus.logging.pattern.file` 或相应的 `logging.pattern.*` 属性始终采用
优先。

在启动期间暴露为：

```properties
logging.level.root=INFO
logging.level.org.miaixz=DEBUG
logging.file.name=app.log
logging.pattern.console=%d %-5level %logger - %msg%n
```

## 通过 Starter 激活

可重复使用的基础设施由三个 Starter 配置组装而成：

| 配置 | 默认 | 禁用开关 |
|---------------------|----------------------------------|-------------------------------------------------------|
| `GeniusStarter` | 启用 | 无 |
| `TaskConfiguration` | 任务类存在时启用 | `bus.context.task.enabled=false` |
| `WebConfiguration` | 为 Servlet 应用程序启用 | `bus.context.web.enabled=false` 仅禁用绑定 |

缓存、映射器、敏感、存储或 Vortex 等产品功能是独立的，并由各自的控制
`bus.<feature>.enabled` 属性。

## 扩展指导

- 实现`ContextProvider`贡献授权状态；使用 Spring 排序来实现确定性优先级。
- 实现Bus `Provider` 并让 `ProviderRegistry` 在所属上下文中解析它。
- 重用 Starter 配置中的转换器、解析器、建议、路由或包装器原语，而不是添加
此处激活元数据。
- 请勿引入静态 `ApplicationContext` 支架。
- 不要在此模块中放置功能属性或第三方客户端生命周期。

## JPMS 与 Native Image

该模块导出可重用的公共包。不导出引导实现包；他们的开放范围很窄
用于框架访问的 Spring Boot 和 Spring Core。可选的 Spring、Servlet 和持久性依赖项保持静态
模块要求。

可访问性元数据包含精确的动态访问的构造函数和成员，按 A-Z 排序。博大`allDeclared*`
禁止 `allPublic*` 反射授予。

## 验证边界

Bus包含且不运行任何测试。上下文、Bean 服务、Web、模块路径、生命周期、元数据、AOT 和本机映像
测试保存在兄弟 Abarth 存储库中。Bus构建必须明确跳过测试。
