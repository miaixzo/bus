# bus-spring

`bus-spring` 是 Bus 可复用的 Spring 集成层，提供 ApplicationContext 级 Bean 访问、运行时上下文传播、
Spring Boot 生命周期支持和通用 Web 基础设施。它保持被动：功能选择、属性条件和应用启动装配均由
`bus-starter` 负责。

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
| `web` | 请求访问和 Servlet 上下文绑定根类型。 |
| `web.advice` | 可复用的 MVC Response Advice。 |
| `web.converter` | HTTP Message Converter 及注册接口。 |
| `web.interceptor` | MVC 请求拦截支持。 |
| `web.resolver` | 显式 RequestObject 参数解析。 |
| `web.routing` | Controller 路由前缀处理。 |
| `web.wrapper` | 有界 Request/Response Body Wrapper。 |
| `boot` | Spring Boot RunListener 和生命周期基础类型。 |
| `boot.banner` | Banner 选择与渲染。 |
| `boot.environment` | 早期 EnvironmentPostProcessor。 |
| `boot.listener` | Spring Boot 和 Spring Cloud 早期 Listener。 |
| `boot.startup` | 启动阶段测量与报告。 |

根 Package 有意保持非空。`ContextBuilder`、`ContextManager`、`ContextProvider`、`ContextState`、
`ContextScope`、`ContextDecorator` 和 `SpringBuilder` 是稳定的根级能力。

## 运行时上下文

运行时状态由每个 Spring ApplicationContext 自己的 `ContextManager` 持有。`ContextState` 是不可变、已分离
的快照，包含 Request ID 和防御性复制的认证信息，不持有 Servlet 对象、请求缓存或 ThreadLocal 容器。

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
