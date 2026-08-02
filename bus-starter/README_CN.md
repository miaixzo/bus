# bus-starter

`bus-starter` 是 Bus 的 Spring Boot 启动与功能装配层，负责发现资源、条件化功能配置、属性绑定、默认 Bean 和
生命周期集成。可复用 Spring 机制保留在 `bus-spring`，业务算法保留在各自所属 Bus 组件。

## 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>${revision}</version>
</dependency>
```

## 启动模型

Spring Boot 通过唯一的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
发现 Starter Configuration。根 Package 包含：

- `GeniusBuilder`：统一定义 `bus.*` 配置前缀常量。
- `GeniusStarter`：装配共享 Spring Bean 访问和运行时上下文基础设施。

根 Package 不对外导出。JPMS 只导出 `org.miaixz.bus.starter.annotation`；Configuration、Properties 和实现
Package 仅定向开放给 Spring 基础设施。

### 默认开启的基础设施

| Configuration | 默认状态 | 关闭属性 | 职责 |
|---|---|---|---|
| `GeniusStarter` | 开启 | 无 | 注册 `SpringBuilder`、Bean 服务、`ContextManager`、`ContextBuilder` 和 `ContextDecorator`。 |
| `TaskConfiguration` | Task 相关类存在时开启 | `bus.context.task.enabled=false` | 在 Spring Boot TaskExecutor 中传播运行时上下文。 |
| `WebConfiguration` | Servlet 应用中开启 | `bus.context.web.enabled=false` | 为 Request、Async、Error Dispatch 注册 `ContextBindingFilter`。 |

这三个基础 Configuration 与产品功能不同。产品功能只有在对应 `bus.<feature>.enabled` 显式为 `true` 时才会
启用。

## 功能启用

| 功能 | Import 注解 | 必需属性 | 主要集成内容 |
|---|---|---|---|
| Auth | `@EnableAuth` | `bus.auth.enabled=true` | `bus-auth` |
| Cache | `@EnableCache` | `bus.cache.enabled=true` | `bus-cache` |
| CORS | `@EnableCors` | `bus.cors.enabled=true` | Servlet MVC |
| Cortex | `@EnableCortex` | `bus.cortex.enabled=true` | `bus-cortex` |
| Dubbo | `@EnableDubbo` | `bus.dubbo.enabled=true` | Apache Dubbo |
| Elastic | `@EnableElastic` | `bus.elastic.enabled=true` | Elasticsearch REST Client |
| Fabric | `@EnableFabric` | `bus.fabric.enabled=true` | TCP 和 WebSocket Service |
| Health | `@EnableHealth` | `bus.health.enabled=true` | Health Indicator 和 Availability |
| I18n | `@EnableI18n` | `bus.i18n.enabled=true` | MessageSource 和 i18n Adapter |
| Image | `@EnableImage` | `bus.image.enabled=true` | Image 和 DICOM 集成 |
| JDBC | `@EnableJdbc` | `bus.jdbc.enabled=true` | 动态 DataSource 路由 |
| JSON | `@EnableJson` | `bus.json.enabled=true` | ApplicationContext 级 JSON Provider |
| Limiter | `@EnableLimiter` | `bus.limiter.enabled=true` | Limiter 扫描和 Service |
| Mapper | `@EnableMapper` | `bus.mapper.enabled=true` | MyBatis、Tenant、Plugin 和 AOT |
| Metrics | `@EnableMetrics` | `bus.metrics.enabled=true` | Metrics Provider 和 Endpoint |
| Mongo | `@EnableMongo` | `bus.mongo.enabled=true` | Mongo ClientSettings Customizer |
| Notify | `@EnableNotify` | `bus.notify.enabled=true` | Notify Registry 和 Service |
| Office | `@EnableOffice` | `bus.office.enabled=true` | 文档转换和预览 |
| Pay | `@EnablePay` | `bus.pay.enabled=true` | Pay Registry 和 Service |
| Sensitive | `@EnableSensitive` | `bus.sensitive.enabled=true` | 日志脱敏和 MVC Body Advice |
| Storage | `@EnableStorage` | `bus.storage.enabled=true` | Storage Registry 和 Service |
| Tempus | `@EnableTempus` | `bus.tempus.enabled=true` | Temporal Client、Worker 和生命周期 |
| Tracer | `@EnableTracer` | `bus.tracer.enabled=true` | 分布式 Tracer |
| Validate | `@EnableValidate` | `bus.validate.enabled=true` | 方法校验和 Advice |
| Vortex | `@EnableVortex` | `bus.vortex.enabled=true` | 路由网关和 Asset 生命周期 |
| Wrapper | `@EnableWrapper` | `bus.wrapper.enabled=true` | 五项独立 MVC 能力 |
| ZooKeeper | `@EnableZookeeper` | `bus.zookeeper.enabled=true` | Apache Curator Client |

注解负责显式 Import 功能 Configuration，但不会绕过该 Configuration 的 `@ConditionalOnProperty`。在 Spring
Boot 应用中，发现资源已经提供所有候选 Configuration，因此属性是最终运行开关。可以使用注解明确表达所选
集成，但对应属性仍必须为 `true`。

```java
@SpringBootApplication
@EnableJson
@EnableSensitive
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

```yaml
bus:
  json:
    enabled: true
  sensitive:
    enabled: true
```

可选依赖通过 Class 条件保护，缺失时不会阻止共享 Starter 基础设施启动。默认 Bean 使用具体类型的
`@ConditionalOnMissingBean`，应用提供相同 Bean 类型即可完成替换。

## 敏感数据集成

当 `bus.sensitive.enabled=true` 时，`SensitiveConfiguration` 始终创建与传输协议无关的日志保护：

```text
Sanitizer -> SensitiveBinding -> bus-logger Executor
```

ApplicationContext 关闭时，`SensitiveBinding` 会解除 Sanitizer 注册。Servlet MVC 应用中，嵌套的
`SensitiveConfiguration.ServletConfiguration` 还会提供请求解密和响应加密或脱敏 Advice，不存在独立的
`SensitiveWebConfiguration`。

## Wrapper 能力

`bus.wrapper.enabled=true` 激活聚合 Wrapper Configuration，子功能开关如下：

| 能力 | 属性 | Wrapper 开启后的默认值 |
|---|---|---|
| RequestObject 绑定 | `bus.wrapper.request-binding.enabled` | `true` |
| Message Converter | `bus.wrapper.message-converters.enabled` | `true` |
| 有界 Body Cache | `bus.wrapper.body-cache.enabled` | `false` |
| Response Advice | `bus.wrapper.response-advice.enabled` | `false` |
| Route Prefix | `bus.wrapper.route-prefix.enabled` | `false` |

Body Cache 的 Request 和 Response 默认上限均为 1 MiB。Multipart 缓存和 Response 缓存仍需显式开启。

## 安全与生命周期默认值

- Tenant 身份来自认证运行时上下文，请求输入不能覆盖。
- RequestObject 绑定必须使用 `@RequestObject` 显式启用，并排除框架类型和简单标量。
- Body Cache 始终有容量边界，默认不缓存 Multipart。
- Sensitive 配置的 `toString()` 诊断不会暴露 Key。
- 第三方 Client 和长期运行 Service 声明明确的初始化与销毁回调。
- Context 所属 Registry、Operator 和 Worker 会在 ApplicationContext 关闭时释放。

## Native Image

Spring AOT 会生成大部分 Configuration 和 Bean 反射提示，因此仓库中的 Reachability Metadata 只保留兼容所需
的精确构造函数和动态发现入口。禁止以下宽泛授权：

- `allDeclaredConstructors` 和 `allPublicConstructors`
- `allDeclaredMethods` 和 `allPublicMethods`
- `allDeclaredFields` 和 `allPublicFields`

Abarth Metadata 审计会根据当前 Class 文件解析所有配置的类型、构造函数、方法和字段，并在重新引入宽泛授权
时直接失败。

## 迁移规则

- 统一使用 `XxxConfiguration`，禁止恢复已删除的 `XxxAutoConfiguration`。
- 运行时传播统一使用 `ContextState`、`ContextScope` 和 `ContextDecorator`。
- Starter 配置前缀统一使用 `GeniusBuilder`。
- 业务代码不得导入未导出的 Starter 实现 Package。
- 按 Bean 类型覆盖默认实现，不依赖 Configuration 实现类。

## 验证边界

Bus 不承载也不运行测试。Starter 集成、生命周期、Module Path、AOT、Metadata 和 Native Image 测试均位于
相邻的 Abarth 仓库。Bus 构建必须显式跳过测试。
