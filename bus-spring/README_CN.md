# bus-spring

`bus-spring` 是 Bus 可复用的 Spring 集成层，提供 ApplicationContext 级 Bean 访问、运行时上下文传播、
Spring Boot 生命周期支持和通用 Web 基础设施。它提供可复用的条件机制，但不决定具体功能：启用注解、属性
前缀、功能选择和应用启动装配均由 `bus-starter` 负责。

## 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-spring</artifactId>
    <version>${revision}</version>
</dependency>
```

## Package 结构

| Package | 职责 |
|---|---|
| `org.miaixz.bus.spring` | 运行时上下文和 Spring 门面 API。 |
| `annotation` | 合并注解检查、占位符绑定及 `@RequestObject`。 |
| `aop` | 可复用的 Spring AOP 基础设施。 |
| `bean` | Context 内 Bean 查询、注册、元数据、环境及 Provider 服务。 |
| `jdbc` | 可复用的数据源解析、连接池创建、动态路由、路由作用域、注解和切面。 |
| `web` | 请求访问和 Servlet 上下文绑定根类型。 |
| `web.advice` | 可复用的 MVC Response Advice。 |
| `web.converter` | HTTP Message Converter 及注册接口。 |
| `web.interceptor` | MVC 请求拦截支持。 |
| `web.resolver` | 显式 RequestObject 参数解析。 |
| `web.routing` | Controller 路由前缀处理。 |
| `web.wrapper` | 有界 Request/Response Body Wrapper。 |
| `boot` | Spring Boot RunListener 和生命周期基础类型。 |
| `boot.banner` | Banner 选择与渲染。 |
| `boot.condition` | 注解优先的通用 Spring Boot 启用条件。 |
| `boot.environment` | 早期 EnvironmentPostProcessor。 |
| `boot.listener` | Spring Boot 和 Spring Cloud 早期 Listener。 |
| `boot.startup` | 启动阶段测量与报告。 |

根 Package 有意保持非空。`ContextBuilder`、`ContextManager`、`ContextProvider`、`ContextState`、
`ContextScope`、`ContextDecorator` 和 `SpringBuilder` 是稳定的根级能力。

`boot.condition` 提供 `@ConditionalOnEnabled` 和 `EnabledCondition`，统一保证显式启用注解的优先级高于对应
配置项；具体启用注解和属性前缀仍由使用它的 Starter 模块定义。`name` 默认为 `enabled`，`matchIfMissing`
默认为 `false`，并同时支持 Configuration 类型和单个 Bean 方法。

## JDBC 数据源基础设施

`org.miaixz.bus.spring.jdbc.DataSource` 是 Service 层选择数据源的公共 Spring 契约，注解值必须对应已解析的
数据源路由。`DataSourceResolver`、`DataSourceDefinition` 和 `DataSourceMapping` 将有序配置前缀解析为一个
经过校验的数据源映射；`DataSourceFactory` 创建连接池；`DynamicDataSource` 执行路由；每个
ApplicationContext 拥有独立的 `DataSourceHolder`，提供可精确恢复的嵌套路由作用域；`DataSourceListener`
通知可选集成数据源的初始化、增加、替换和删除；
`AspectjJdbcProxy` 在事务取得连接前解释注解。这些类型不依赖 Mapper 和 Starter 装配，`bus-starter` 只提供
支持的前缀顺序、默认连接池类型和 Spring Bean 装配。

## 运行时上下文

运行时状态由每个 Spring ApplicationContext 自己的 `ContextManager` 持有。`ContextState` 是不可变、已分离
的快照，包含 Request ID、防御性复制的认证信息以及解析后的 Token/API Key 凭证元数据，不持有 Servlet
对象、请求缓存或 ThreadLocal 容器。凭证只在 Servlet 边界解析一次，诊断输出始终脱敏。

```java
ContextState state = contextBuilder.capture();

try (ContextScope ignored = contextBuilder.install(state)) {
    operation.run();
}
```

关闭 `ContextScope` 会恢复工作线程原有状态。跨线程池传播使用相同模型：

```java
executor.execute(contextDecorator.decorate(task));
```

`ContextProvider` 扩展可以提供认证数据。Provider 在所属 ApplicationContext 内排序和解析，不存在静态
ApplicationContext 注册表。

`ContextBuilder.getCredential()` 在 Token 和 API Key 同时存在时优先返回 Token；`getToken()` 与
`getApiKey()` 分别读取各自的状态，因此不会丢失次优先级凭证。解析顺序复用 `Http.Auth`，覆盖 Header、
Parameter、可用的缓存 JSON Body 和 Cookie。上下文解析绝不直接消费原始请求流；启用 Body Cache 后，缓存
过滤器固定先于上下文绑定执行。

## Spring 门面

`SpringBuilder` 组合了同一 Context 所属的六个单一职责服务：

- `SpringContext`：持有 ApplicationContext。
- `BeanProvider`：只读 Bean 查询。
- `BeanRegistry`：BeanDefinition 和 Singleton 变更。
- `BeanMetadata`：无副作用的 Bean 类型检查。
- `EnvironmentResolver`：属性、Profile、应用名和占位符解析。
- `ProviderRegistry`：有序 Provider 发现及 Context 内缓存。

只需要单一职责时应直接依赖对应服务；确实需要组合能力时再使用 `SpringBuilder` 门面。

## Web 基础设施

- `ContextBindingFilter` 在 Request、Async 和 Error Dispatch 中安装并恢复运行时状态。
- `RequestContext` 读取 Header、Parameter、Cookie、PathVariable、Multipart 及缓存 JSON 字段。
- `@RequestObject` 显式标记需要 Bus RequestObject 绑定的 Controller 参数。
- Body Wrapper 对请求缓存和响应诊断缓存执行固定容量限制。
- Route Prefix、Converter、Advice 和 Resolver Package 只提供通用机制，由 Starter 负责激活。

## Spring Boot 生命周期

Boot 实现 Package 包含 `SpringApplicationRunListener`、EnvironmentPostProcessor、配置 Listener、Banner 和
启动指标。它们不单独持有 `spring.factories`；唯一发现清单由 `bus-starter` 提供，并指向这些实现。

## Native Image 与 JPMS

模块只导出可复用公共 Package，Boot 实现 Package 仅定向开放给 Spring Boot。Reachability Metadata 对动态
发现的生命周期类型只声明精确构造函数，禁止使用宽泛的 `allDeclared*` 和 `allPublic*` 反射授权。

## 验证边界

Bus 不承载也不运行测试。Context、Web、Module Path、AOT、生命周期、Metadata 和 Native Image 测试均位于
相邻的 Abarth 仓库。Bus 构建必须显式跳过测试。
