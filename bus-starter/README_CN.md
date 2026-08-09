# bus-starter

`bus-starter` 是 Bus 的 Spring Boot 启动和功能组装层。它集中配置发现，
`bus.*` 属性绑定、条件功能激活、默认 Bean 注册、第三方客户端生命周期以及
Spring AOT 兼容性。可重复使用的弹簧机制保留在 `bus-spring` 中；商业算法仍然属于他们自己
Bus模块。

## 责任边界

Starter 负责：

- Spring Boot配置发现；
- 默认启用的共享上下文基础设施；
- 选择加入产品功能配置；
- 经验证的不可变配置属性；
- 条件 Bean 和应用程序覆盖点；
- 第三方客户端/服务的创建和销毁；
- 通过配置选择的 MVC 建议、过滤器、解析器和转换器；
- 特定功能的 AOT 和本机映像集成。

Starter 不能替代底层模块。它不能吸收可重复使用的 Spring 力学
`bus-spring`、`bus-sensitive` 的屏蔽算法、`bus-logger` 的日志记录行为或来自 `bus-logger` 的域功能
其他Bus组件。

## 依赖关系

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>${revision}</version>
</dependency>
```

为应用程序选择的每个功能添加所属的Bus模块和所需的第三方库。选修的
库受到类路径条件的保护，因此它们的缺失不会阻止不相关的 Starter 基础设施
加载中。

## 启动模型

Spring Boot 通过以下方式发现候选人：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

早期启动侦听器和环境处理器通过 Starter 拥有的单一注册
`META-INF/spring.factories` 文件。 `bus-spring` 提供了可重用的实现，但没有发布竞争的
发现资源。

根包包含两个主要类：

| 类型 | 责任 |
|-----------------|-----------------------------------------------------------------------------------------------------|
| `GeniusBuilder` | 所有 `bus.*` 配置前缀的权威编译时常量。                        |
| `GeniusStarter` | 注册共享 Spring Bean 服务和应用程序上下文拥有的运行时上下文基础结构。 |

因此，根包仍然有意义且非空。

## 默认启用的基础设施

多个功能所需的基础设施是独立于产品功能启用的：

| 配置 | 默认值 | 禁用属性 | 职责 |
|---------------------|--------------------------------------|-------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `GeniusStarter` | 已启用 | 无 | 注册 Bean 服务、环境/提供者服务、运行时上下文和任务装饰器。                                |
| `TaskConfiguration` | 当引导任务类存在时启用 | `bus.context.task.enabled=false` | 组成有序任务装饰器并传播运行时上下文。                                                            |
| `WebConfiguration` | 为 Servlet 应用程序启用 | `bus.context.web.enabled=false` 仅禁用绑定 | 注册共享 `RequestContext` 并有条件地注册请求、异步和错误调度的上下文绑定。 |

`GeniusStarter` 提供可替换的默认值：

- `SpringContext`；
- `BeanProvider`；
- `BeanRegistry`；
- `BeanMetadata`；
- `EnvironmentResolver`；
- `ProviderRegistry`；
- `ContextManager`；
- `ContextBuilder`；
- `SpringBuilder`；
- `ContextDecorator`。

每个都使用具体的 `@ConditionalOnMissingBean` 合约。应用程序可以替换一项服务而不替换其他服务
整个基础设施图。

### 上下文传播默认值

`TaskConfiguration` 对所有 `TaskDecorator` Bean 进行排序，删除重复实例，确保有一个 `ContextDecorator`，并且
在 Spring Boot 任务执行器上安装复合装饰器。 `WebConfiguration`始终提供可更换的
`RequestContext` Servlet 应用程序中的 Bean 并在以下位置注册 `ContextBindingFilter`
除非禁用绑定，否则将调度 `REQUEST`、`ASYNC` 和 `ERROR` 的 `Ordered.HIGHEST_PRECEDENCE + 10`。

```yaml
bus:
  context:
    task:
      enabled: true
    web:
      enabled: true
```

当存在所需的运行时类时，两个交换机都默认为 `true`。

## 功能激活模型

产品功能使用一种确定性激活顺序：显式 `@EnableXxx` 注释始终启用其功能，
包括`bus.<feature>.enabled=false`时；如果没有注释，该功能仅在其启用时启用
`bus.<feature>.enabled` 属性为 `true`。如果两个激活源都不存在，则该功能保持禁用状态。

| 特征 | 导入注释 | 属性 | 主要职责 |
|-----------|--------------------|-------------------------------------------------|---------------------------------------------------------------|
| Auth | `@EnableAuth` | `bus.auth.enabled` | 身份验证服务和方法解析。                 |
| 缓存 | `@EnableCache` | `bus.cache.enabled` | 缓存提供程序组件和 AspectJ 代理支持。            |
| CORS | `@EnableCors` | `bus.cors.enabled` | 已验证的 Servlet MVC CORS 策略。                            |
| Cortex | `@EnableCortex` | `bus.cortex.enabled` | Cortex 注册表和集成组件。                     |
| Dubbo | `@EnableDubbo` | `bus.dubbo.enabled` | Apache Dubbo 集成。                                     |
| Elastic | `@EnableElastic` | `bus.elastic.enabled` | Elasticsearch REST 客户端生命周期。                          |
| 结构 | `@EnableFabric` | `bus.fabric.enabled` | TCP、WebSocket 和 DNS 服务生命周期。                    |
| 运行状况 | `@EnableHealth` | `bus.health.enabled` | 系统运行状况和可用性集成。                   |
| I18n | `@EnableI18n` | `bus.i18n.enabled` | 消息源和Bus i18n 适配器。                          |
| 图像 | `@EnableImage` | `bus.image.enabled` | 图像和 DICOM 提供程序集成。                         |
| JDBC | `@EnableJdbc` | `bus.datasource.url` 或 `spring.datasource.url` | 已验证动态数据源和路由。                   |
| JSON | `@EnableJson` | `bus.json.enabled` | 应用程序上下文 JSON 提供程序选择。                  |
| 限制器 | `@EnableLimiter` | `bus.limiter.enabled` | 限制器扫描和服务注册。                    |
| 映射器 | `@EnableMapper` | `bus.mapper.enabled` | MyBatis 映射器扫描、插件、租户上下文和 AOT。    |
| 指标 | `@EnableMetrics` | `bus.metrics.enabled` | 指标提供程序和端点。                               |
| Mongo | `@EnableMongo` | `bus.mongo.enabled` | Mongo客户端设置定制。                          |
| 通知 | `@EnableNotify` | `bus.notify.enabled` | 通知注册表和服务生命周期。                  |
| 办公室 | `@EnableOffice` | `bus.office.enabled` | 文档转换和预览服务。                      |
| 支付 | `@EnablePay` | `bus.pay.enabled` | 支付登记和服务。                                 |
| 敏感 | `@EnableSensitive` | `bus.sensitive.enabled` | 注册脱敏器生命周期和可选的 MVC Body Advice。     |
| 存储 | `@EnableStorage` | `bus.storage.enabled` | 存储提供程序、注册表、缓存和服务。              |
| Tempus | `@EnableTempus` | `bus.tempus.enabled` | 临时客户端、工作人员和生命周期。                     |
| 跟踪器 | `@EnableTracer` | `bus.tracer.enabled` | 分布式跟踪集成。                              |
| 验证 | `@EnableValidate` | `bus.validate.enabled` | 方法验证和异常建议。                       |
| Vortex | `@EnableVortex` | `bus.vortex.enabled` | 反应式路由网关和资产生命周期。                 |
| 包装器 | `@EnableWrapper` | `bus.wrapper.enabled` | MVC 绑定、转换器、缓存、建议和路由前缀。 |
| ZooKeeper | `@EnableZookeeper` | `bus.zookeeper.enabled` | Apache Curator 客户端生命周期。                              |

每个注释直接导入功能配置。共享的 `@ConditionalOnEnabled` 规则来自
在将 `bus.<feature>.enabled` 评估为辅助来源之前，`bus-spring` 接受该显式注释。两个都
因此，激活路径可以达到相同的功能配置，而无需创建并行实现。

启用 JSON 集成后，`JsonConfiguration` 选择一个 `JsonProvider`，而 `JsonBinding` 则安装该确切的
`JsonKit`、缓存的请求正文解析和其他共享静态 JSON 使用者的提供者。关闭 Spring 上下文
仅删除该绑定所拥有的提供者。如果存在多个 JSON 引擎，请设置
`bus.json.provider=fastjson`、`gson` 或 `jackson`； `AUTO` 仅接受一种可用引擎。

## 快速开始

```java
import org.miaixz.bus.starter.annotation.EnableJson;
import org.miaixz.bus.starter.annotation.EnableSensitive;

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

注释使选定的集成在应用程序代码中明确显示。匹配的属性会激活它们。

## 配置原则

所有功能前缀均来自`GeniusBuilder`；不要在另一个模块中重复前缀常量。配置
属性在绑定或 Bean 创建期间验证无效组合，并防御性地复制可变集合或
必要时使用数组。

通用规则：

- 每个产品功能都有一个`enabled`开关；
- 可选依赖项使用类条件；
- 应用程序覆盖使用具体的 Bean 类型或记录的 Bean 名称；
- 诊断 `toString()` 输出中排除秘密；
- 在支持的情况下超时使用 `Duration`；
- 长期运行的客户端和服务声明显式销毁回调；
- 当上下文关闭时，上下文拥有的全局注册被释放。

## CORS

```yaml
bus:
  cors:
    enabled: true
    path: "/api/**"
    allowed-origins:
      - "https://console.example.com"
    allowed-headers:
      - "Authorization"
      - "Content-Type"
    allowed-methods:
      - "GET"
      - "POST"
    exposed-headers:
      - "X-Request-Id"
    allow-credentials: true
    max-age: 30m
```

通配符来源不能与凭据组合。数组是防御性复制的。默认值包括 GET、POST、PUT、
选项和删除，而该功能本身在明确启用之前保持禁用状态。

## 敏感数据

```yaml
bus:
  sensitive:
    enabled: true
    debug: false
```

启用后，传输中立路径始终可用：

```text
Sanitizer -> SensitiveBinding -> bus-logger Executor
```

当拥有的应用程序上下文关闭时，`SensitiveBinding` 取消注册其清理程序。在 Servlet MVC 应用程序中，
嵌套的 `SensitiveConfiguration.ServletConfiguration` 还提供请求解密和响应
加密或屏蔽建议。没有单独的`SensitiveWebConfiguration`。

加密密钥必须来自受保护的外部配置源。诊断输出掩盖了关键材料。

## Elasticsearch

```yaml
bus:
  elastic:
    enabled: true
    hosts: "127.0.0.1:9200"
    schema: "http"
    connect-timeout: 6s
    socket-timeout: 60s
    connection-request-timeout: 6s
    max-connect-total: 2000
    max-connect-per-route: 500
```

每个主机必须包含 `1..65535` 范围内的有效端口。超时和连接限制必须为正数，并且
每条路线的限制不能超过总限制。

## Fabric 与 DNS

`bus.fabric.enabled=true` 或 `@EnableFabric` 激活 Fabric 父级集成。显式注释有
优先于财产。父级激活后默认启用 TCP 套接字支持； WebSocket 和 DNS
保留子功能并需要自己的 `enabled=true` 属性。 DNS 是故意导入的
`FabricConfiguration`，因此`bus.fabric.dns.enabled=true`无法创建第二个独立的Fabric入口点。

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

申请时需提供一张`DnsSnapshotProvider`；它仍然是 DNS 区域和快照的所有者。选修的
`DnsSnapshotListener`、`DnsDynamicUpdateSink`、`DnsTsigKey` 和 `TlsPolicy` beans 扩展了生命周期通知，
分别是动态更新、TSIG 验证和 DoT。 Starter 仅拥有运行时 `DnsServer` bean 并将其关闭
与 Spring 上下文相关。 DNS 管理、数据库访问和持久性均位于 Starter 之外。

## JDBC

当池类可用时，Starter 会自动组装 JDBC。将 `bus.datasource.enabled=false` 设置为
禁用自动组装；显式的 `@EnableJdbc` 始终具有更高的优先级，并且仍然启用 JDBC。数据源
定义使用 `bus.datasource` 或 `spring.datasource`，并且两者都使用相同的根主加 `multi` 结构。他们
永远不会合并：`bus.datasource` URL 选择完整的Bus组并覆盖 `spring.datasource`。

```yaml
bus:
  datasource:
    name: master
    url: jdbc:mysql://127.0.0.1:3306/app
    username: app
    password: ${APP_DB_PASSWORD}
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

JDBC 职责是固定的。可重复使用 `DataSourceResolver`、`DataSourceDefinition`、`DataSourceMapping`、
`DataSourceFactory`、`DynamicDataSource`、`DataSourceHolder` 和 `AspectjJdbcProxy` 位于 `bus-spring` 下
`org.miaixz.bus.spring.jdbc`。 Starter包只保留了`JdbcConfiguration`，它组装了Beans，并且
`JdbcDescriptor`，定义了 Bus-before-Spring 前缀顺序和 Hikari 默认值。两个前缀使用相同的
解析器路径。根 `name` 是默认路由，每个 `multi` 条目都提供一条附加路由。名称必须是
在整个组中非空白且唯一。 JDBC 从不引用Mapper。当Mapper启用时，它自己的
`DataSourceListener` 同步初始和运行时路由更改的方言状态。路由 Bean 拥有每个池
根据这些定义创建：替换或删除路由会关闭未引用池和应用程序上下文
shutdown 仅关闭每个剩余池一次。

服务方法选择带有 `@DataSource` 的命名数据源。方法注释会覆盖其类注释，并且
嵌套调用在返回或失败时恢复确切的父路由：

```java
import org.miaixz.bus.spring.jdbc.DataSource;

@Service
public class OrderService {

    @DataSource("archive")
    @Transactional
    public void createOrder() {
        // Mapper operations use archive.
    }

}
```

路由必须在事务通知获得连接之前发生。将`@DataSource`放在外部调用的服务上
事务边界，或通过另一个 Spring bean 代理调用路由的内部操作。自调用通过
`this` 绕过 AOP，已经活动的外部事务无法更改其获取的连接。动态路由是
不是分布式事务机制。租户或路由信息必须来自受信任的运行时上下文，而不是
比请求控制的模型字段。

## Mapper

映射器集成涵盖：

- 确定性映射器类路径扫描；
- `MapperFactoryBean`和扫描仪注册；
- XML/资源位置解析；
- 有序的插件构建；
- 来自 `ContextBuilder` 的租户身份；
- 租户例外建议；
- AOT Bean 工厂初始化和运行时提示。

业务代码不得通过请求绑定覆盖租户身份。自定义映射器插件应该使用
记录提供程序和拦截器扩展点，而不是在启动后修改 Starter 注册表。
当未提供包属性时，`@EnableMapper` 扫描其声明包。属性激活用途
`bus.mapper.base-package`；当它也不存在时，Spring Boot 应用程序包将被扫描以查找显式的
`@Mapper` 接口。未解析的扫描范围会导致启动失败，而不是静默注册任何映射器。方言是
绑定到所属的 MyBatis `Configuration`，因此两个应用程序上下文不能覆盖彼此的路由提供者。

## Wrapper 能力

`bus.wrapper.enabled=true` 激活聚合包装器配置。子特征保持独立
受控：

| 能力 | 属性 | 启用 Wrapper 后的默认值 |
|------------------------|------------------------------------------|----------------------------------|
| 请求对象绑定 | `bus.wrapper.request-binding.enabled` | `true` |
| 消息转换器 | `bus.wrapper.message-converters.enabled` | `true` |
| 有界主体缓存 | `bus.wrapper.body-cache.enabled` | `false` |
| 回复建议 | `bus.wrapper.response-advice.enabled` | `false` |
| 路由前缀 | `bus.wrapper.route-prefix.enabled` | `false` |

```yaml
bus:
  wrapper:
    enabled: true
    request-binding:
      enabled: true
    message-converters:
      enabled: true
      type-policy: application
      allowed-types:
        - com.example.shared.dto.**
        - com.example.shared.dto1.**
    body-cache:
      enabled: false
    response-advice:
      enabled: false
    route-prefix:
      enabled: false
```

消息转换器目标类型策略是：

- `framework`：允许Bus框架类型、内置标量/容器类型和显式规则；
- `application`：还发现 Spring Boot 应用程序包，并且是默认值；
- `all`：允许每种目标类型，并且只能在受信任的环境中使用。

`allowed-types` 接受精确的类名，`*` 接受一个包段，`**` 接受任意数量的包段。
兼容性属性 `auto-type` 接受逗号分隔规则； `auto-type: "**"` 还明确允许每个
目标类型。对于新应用，首选 `allowed-types`。

请求对象绑定需要 `@RequestObject`，排除框架和简单标量类型，并且不允许请求
输入以替换受信任的租户上下文。正文缓存是有限的；多部分和响应诊断缓存仍然存在
选择加入。

## 客户端和服务生命周期

Starter 拥有其创建的客户端和长期运行服务的生命周期：

| 功能 | 生命周期示例 |
|------------------------------|-------------------------------------------------------|
| 弹性 | REST 传输/客户端关闭。                          |
| Fabric | TCP 和 WebSocket 服务启动/停止。                 |
| 通知、办公、支付、存储 | 注册表/服务创建和上下文清理。        |
| Tempus | 客户端、工人工厂、工人和关闭。        |
| Vortex | 路由器图、服务器启动/停止和资产生命周期。 |
| ZooKeeper | Curator 客户端启动/关闭。                           |

除非替换合同另有规定，否则应用程序提供的替换 Bean 拥有自己的生命周期。

## Bean 覆盖规则

优先替换具体产品合同：

```java
@Bean
StorageService customStorageService(...) {
    return new StorageService(...);
}
```

不依赖于业务代码中的配置实现类。配置和属性包已打开
到 Spring 进行框架访问，但不导出为通用 JPMS API。仅有的
`org.miaixz.bus.starter.annotation` 已导出。

## 包结构

| 包装组 | 内容 |
|------------------|---------------------------------------------------------------------------------|
| 根 | 共享启动基础结构和属性前缀常量。                    |
| `annotation` | 公共 `@Enable*` 注释。                                                  |
| `context` | 默认任务和 Servlet 上下文传播。                                   |
| 功能包 | 一项功能的配置、属性、服务和生命周期协作者。 |
| `wrapper.*` | 独立控制的 MVC 包装器功能。                              |

没有 `internal` 软件包。功能实现类型保留在当前的功能包中。

## 安全默认值

- 产品功能可供选择。
- 有凭据的 CORS 拒绝通配符来源。
- 属性诊断不会暴露敏感密钥和凭据。
- 请求绑定不会从用户输入中获取经过身份验证的租户身份。
- 缓存的主体有明确的边界，并且多部分缓存是可选的。
- 日志清理归 `bus-sensitive` 所有，而不是 `bus-logger`。
- 每个应用程序上下文的上下文状态是隔离的，并在异步执行后恢复。
- 第三方资源与所属 Spring 上下文关闭。

## Native Image 与 AOT

Spring AOT 生成大多数配置和 Bean 反射提示。因此，签入的可达性元数据仅列出
精确的构造函数和动态访问的成员。条目和嵌套成员列表按 A-Z 排序。

禁止以下广泛的赠款：

- `allDeclaredConstructors` 和 `allPublicConstructors`；
- `allDeclaredMethods` 和 `allPublicMethods`；
- `allDeclaredFields` 和 `allPublicFields`。

Abarth 元数据审计根据以下内容解析每个配置的类、构造函数、方法、字段、代理和资源
当前的运行时模型并且还强制执行排序。

## 迁移规则

- 使用`XxxConfiguration`；已删除的 `XxxAutoConfiguration` 名称不得返回。
- 使用 `ContextState`、`ContextScope` 和 `ContextDecorator` 进行运行时传播。
- 使用 `GeniusBuilder` 作为 Starter 属性前缀。
- 将可重用机制保留在 `bus-spring` 中，并将域行为保留在所属Bus模块中。
- 不要在 Starter 下引入 `internal` 包。
- 按 Bean 类型或记录的 Bean 名称覆盖默认值。
- 保持 `AutoConfiguration.imports`、`spring.factories`、模块描述符和可达性元数据与类保持一致
重命名。

## 验证边界

Bus包含且不运行任何测试。入门集成、绑定、生命周期、模块路径、元数据、AOT 和本机映像
测试保存在同级 Abarth 存储库中。Bus 构建必须明确跳过测试。
