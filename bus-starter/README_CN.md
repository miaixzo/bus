# bus-starter

`bus-starter` 是 Bus 的 Spring Boot 启动与功能装配层，负责发现资源、条件化功能配置、属性绑定、默认 Bean 和 生命周期集成。可复用
Spring 机制保留在 `bus-spring`，业务算法保留在各自所属 Bus 组件。

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

根 Package 不对外导出。JPMS 只导出 `org.miaixz.bus.starter.annotation`；Configuration、Properties 和实现 Package 仅定向开放给
Spring 基础设施。

### 默认开启的基础设施

| Configuration       | 默认状态              | 关闭属性                                         | 职责                                                                                               |
|---------------------|-----------------------|--------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `GeniusStarter`     | 开启                  | 无                                               | 注册 `SpringBuilder`、Bean 服务、`ContextManager`、`ContextBuilder` 和 `ContextDecorator`。        |
| `TaskConfiguration` | Task 相关类存在时开启 | `bus.context.task.enabled=false`                 | 在 Spring Boot TaskExecutor 中传播运行时上下文。                                                   |
| `WebConfiguration`  | Servlet 应用中开启    | `bus.context.web.enabled=false` 只关闭上下文绑定 | 注册共享 `RequestContext`，并按条件为 Request、Async、Error Dispatch 注册 `ContextBindingFilter`。 |

这三个基础 Configuration 与产品功能不同。产品功能采用固定优先级：显式 `@EnableXxx` 始终启用对应功能， 即使
`bus.<feature>.enabled=false` 也不能关闭它；未声明注解时，只有配置项为 `true` 才启用；两者都不存在时 保持禁用。

## 功能启用

| 功能      | 最高优先级注解     | 次级配置项                                      | 主要集成内容                        |
|-----------|--------------------|-------------------------------------------------|-------------------------------------|
| Auth      | `@EnableAuth`      | `bus.auth.enabled=true`                         | `bus-auth`                          |
| Cache     | `@EnableCache`     | `bus.cache.enabled=true`                        | `bus-cache`                         |
| CORS      | `@EnableCors`      | `bus.cors.enabled=true`                         | Servlet MVC                         |
| Cortex    | `@EnableCortex`    | `bus.cortex.enabled=true`                       | `bus-cortex`                        |
| Dubbo     | `@EnableDubbo`     | `bus.dubbo.enabled=true`                        | Apache Dubbo                        |
| Elastic   | `@EnableElastic`   | `bus.elastic.enabled=true`                      | Elasticsearch REST Client           |
| Fabric    | `@EnableFabric`    | `bus.fabric.enabled=true`                       | TCP、WebSocket 和 DNS Service       |
| Health    | `@EnableHealth`    | `bus.health.enabled=true`                       | Health Indicator 和 Availability    |
| I18n      | `@EnableI18n`      | `bus.i18n.enabled=true`                         | MessageSource 和 i18n Adapter       |
| Image     | `@EnableImage`     | `bus.image.enabled=true`                        | Image 和 DICOM 集成                 |
| JDBC      | `@EnableJdbc`      | `bus.datasource.url` 或 `spring.datasource.url` | 动态 DataSource 路由                |
| JSON      | `@EnableJson`      | `bus.json.enabled=true`                         | ApplicationContext 级 JSON Provider |
| Limiter   | `@EnableLimiter`   | `bus.limiter.enabled=true`                      | Limiter 扫描和 Service              |
| Mapper    | `@EnableMapper`    | `bus.mapper.enabled=true`                       | MyBatis、Tenant、Plugin 和 AOT      |
| Metrics   | `@EnableMetrics`   | `bus.metrics.enabled=true`                      | Metrics Provider 和 Endpoint        |
| Mongo     | `@EnableMongo`     | `bus.mongo.enabled=true`                        | Mongo ClientSettings Customizer     |
| Notify    | `@EnableNotify`    | `bus.notify.enabled=true`                       | Notify Registry 和 Service          |
| Office    | `@EnableOffice`    | `bus.office.enabled=true`                       | 文档转换和预览                      |
| Pay       | `@EnablePay`       | `bus.pay.enabled=true`                          | Pay Registry 和 Service             |
| Sensitive | `@EnableSensitive` | `bus.sensitive.enabled=true`                    | 日志脱敏和 MVC Body Advice          |
| Storage   | `@EnableStorage`   | `bus.storage.enabled=true`                      | Storage Registry 和 Service         |
| Tempus    | `@EnableTempus`    | `bus.tempus.enabled=true`                       | Temporal Client、Worker 和生命周期  |
| Tracer    | `@EnableTracer`    | `bus.tracer.enabled=true`                       | 分布式 Tracer                       |
| Validate  | `@EnableValidate`  | `bus.validate.enabled=true`                     | 方法校验和 Advice                   |
| Vortex    | `@EnableVortex`    | `bus.vortex.enabled=true`                       | 路由网关和 Asset 生命周期           |
| Wrapper   | `@EnableWrapper`   | `bus.wrapper.enabled=true`                      | 五项独立 MVC 能力                   |
| ZooKeeper | `@EnableZookeeper` | `bus.zookeeper.enabled=true`                    | Apache Curator Client               |

注解直接 Import 功能 Configuration。`bus-spring` 提供的统一 `@ConditionalOnEnabled` 规则优先接受显式注解， 只有未发现注解时才把
`bus.<feature>.enabled` 作为次级启用来源。两条启用路径始终落到同一个功能 Configuration，不会产生第二套实现。

启用 JSON 集成后，`JsonConfiguration` 选择唯一的 `JsonProvider`，`JsonBinding` 将同一实例绑定给 `JsonKit`、 缓存请求体解析及其他共享静态
JSON 调用；Spring 上下文关闭时只解除当前 Binding 持有的 Provider。Classpath 中同时存在多个 JSON 引擎时，必须将
`bus.json.provider` 设置为 `fastjson`、`gson` 或 `jackson`；`AUTO` 只接受 唯一可用引擎。

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

## Fabric 与 DNS

`bus.fabric.enabled=true` 或 `@EnableFabric` 启用 Fabric 父功能，显式注解的优先级高于配置项。父功能启用后， TCP Socket
默认开启；WebSocket 与 DNS 仍是子功能，必须分别配置 `enabled=true`。DNS 由
`FabricConfiguration` 统一导入，因此 `bus.fabric.dns.enabled=true` 不会形成第二条独立的 Fabric 启动入口。

```yaml
bus:
  fabric:
    enabled: true
    socket:
      enabled: true
      host: 0.0.0.0
      port: 7890
    websocket:
      enabled: false
    dns:
      enabled: true
      transport: UDP
      host: 0.0.0.0
      port: 53
      cache: true
      cache-max-entries: 10000
      cache-ttl: 30s
      cache-serve-stale-ttl: 5m
      cache-prefetch-before-expiry: 5s
      max-udp-payload-bytes: 1232
      rate-limit-per-second: 0
```

应用必须提供唯一的 `DnsSnapshotProvider`，DNS 区域与快照的所有权仍属于应用。可选的
`DnsSnapshotListener`、`DnsDynamicUpdateSink`、`DnsTsigKey` 和 `TlsPolicy` Bean 分别扩展快照通知、动态更新、 TSIG 校验和
DoT。Starter 只创建并随 Spring 上下文关闭运行期 `DnsServer`；管理、数据库访问与持久化均不属于 Starter 职责。

## JDBC 与多数据源路由

JDBC 在连接池类存在时由 Starter 自动装配；设置 `bus.datasource.enabled=false` 可以关闭自动装配，但显式
`@EnableJdbc` 的优先级最高，此时仍会启用 JDBC。数据源定义入口为 `bus.datasource` 和
`spring.datasource`。两套结构完全一致，都是根节点主库加 `multi` 附加库。只要 `bus.datasource` 声明了 URL，就整组使用 Bus
配置并忽略 `spring.datasource`，禁止跨前缀混合属性。

根节点的 `name` 是默认路由键，`multi` 中每项的 `name` 是 Service 切换键；名称必须非空且全组唯一。
`type` 可以省略，默认使用 Hikari：

JDBC 职责固定拆分。可复用的 `DataSourceResolver`、`DataSourceDefinition`、`DataSourceMapping`、
`DataSourceFactory`、`DynamicDataSource`、`DataSourceHolder` 和 `AspectjJdbcProxy` 全部位于 `bus-spring` 的
`org.miaixz.bus.spring.jdbc`。Starter 的 JDBC Package 只保留负责 Bean 装配的 `JdbcConfiguration`，以及定义 Bus 优先于
Spring 的前缀顺序和 Hikari 默认实现的 `JdbcDescriptor`。两个配置前缀共用同一 解析路径。JDBC 不引用 Mapper；启用 Mapper 时，由
Mapper 自己提供 `DataSourceListener`，同步初始化和运行期 增删的数据源方言。路由 Bean 对配置创建的连接池负责：替换或删除路由时关闭已无引用的连接池，Spring
上下文关闭时对其余连接池各关闭一次。

```yaml
bus:
  datasource:
    name: primary
    url: jdbc:mysql://127.0.0.1:3306/primary
    username: app
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
    multi:
      - name: archive
        url: jdbc:mysql://127.0.0.1:3306/archive
        username: app
        password: ${ARCHIVE_DB_PASSWORD}
        driver-class-name: com.mysql.cj.jdbc.Driver
        hikari:
          maximum-pool-size: 10
```

Service 使用 `@DataSource` 选择根节点或 `multi` 中的 `name`。方法注解覆盖类注解，嵌套调用 结束后恢复父级路由，异常场景也会恢复：

```java
import org.miaixz.bus.spring.jdbc.DataSource;

@Service
public class ParserService {

    @DataSource("archive")
    @Transactional
    public void parse() {
        // Mapper operations use archive.
    }

}
```

`@EnableMapper` 未声明扫描包时，默认扫描注解所在应用类的 Package；配置启用时先读取
`bus.mapper.base-package`，仍未配置则在 Spring Boot 应用 Package 中只扫描显式 `@Mapper` 接口。无法确定
扫描范围时直接终止启动，不再静默得到零个 Mapper。数据源方言提供器绑定到所属 MyBatis `Configuration`， 多个 Spring
上下文之间不会覆盖彼此的路由状态。

切换必须发生在事务取得连接之前，因此 `@DataSource` 应与 `@Transactional` 放在同一个对外 Service 方法， 或放在由另一个
Spring Bean 代理调用的内层方法。类内部通过 `this` 自调用不会经过 AOP，也不能在已经开始的
外层事务中切换连接；需要跨库事务时必须使用明确的分布式事务方案，不能依赖线程路由实现原子提交。

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

| 能力               | 属性                                     | Wrapper 开启后的默认值 |
|--------------------|------------------------------------------|------------------------|
| RequestObject 绑定 | `bus.wrapper.request-binding.enabled`    | `true`                 |
| Message Converter  | `bus.wrapper.message-converters.enabled` | `true`                 |
| 有界 Body Cache    | `bus.wrapper.body-cache.enabled`         | `false`                |
| Response Advice    | `bus.wrapper.response-advice.enabled`    | `false`                |
| Route Prefix       | `bus.wrapper.route-prefix.enabled`       | `false`                |

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

Abarth Metadata 审计会根据当前 Class 文件解析所有配置的类型、构造函数、方法和字段，并在重新引入宽泛授权 时直接失败。

## 迁移规则

- 统一使用 `XxxConfiguration`，禁止恢复已删除的 `XxxAutoConfiguration`。
- 运行时传播统一使用 `ContextState`、`ContextScope` 和 `ContextDecorator`。
- Starter 配置前缀统一使用 `GeniusBuilder`。
- 业务代码不得导入未导出的 Starter 实现 Package。
- 按 Bean 类型覆盖默认实现，不依赖 Configuration 实现类。

## 验证边界

Bus 不承载也不运行测试。Starter 集成、生命周期、Module Path、AOT、Metadata 和 Native Image 测试均位于 相邻的 Abarth 仓库。Bus
构建必须显式跳过测试。
