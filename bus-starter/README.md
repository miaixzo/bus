# bus-starter

`bus-starter` is the Spring Boot startup and feature-assembly layer for Bus. It centralizes configuration discovery,
`bus.*` property binding, conditional feature activation, default Bean registration, third-party client lifecycle, and
Spring AOT compatibility. Reusable Spring mechanics remain in `bus-spring`; business algorithms remain in their owning
Bus modules.

## Responsibility boundary

The Starter is responsible for:

- Spring Boot configuration discovery;
- default-enabled shared context infrastructure;
- opt-in product feature configuration;
- validated immutable configuration properties;
- conditional Beans and application override points;
- third-party client/service creation and destruction;
- MVC advice, filters, resolvers, and converters selected by configuration;
- feature-specific AOT and Native Image integration.

The Starter is not a replacement for the underlying modules. It must not absorb reusable Spring mechanics from
`bus-spring`, masking algorithms from `bus-sensitive`, logging behavior from `bus-logger`, or domain functionality from
other Bus components.

## Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>${revision}</version>
</dependency>
```

Add the owning Bus module and required third-party library for each feature selected by the application. Optional
libraries are guarded by classpath conditions so their absence does not prevent unrelated Starter infrastructure from
loading.

## Startup model

Spring Boot discovers candidates through:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Early Boot listeners and environment processors are registered through the single Starter-owned
`META-INF/spring.factories` file. `bus-spring` supplies their reusable implementations but does not publish a competing
discovery resource.

The root package contains two primary classes:

| Type | Responsibility |
|---|---|
| `GeniusBuilder` | Authoritative compile-time constants for all `bus.*` configuration prefixes. |
| `GeniusStarter` | Registers shared Spring Bean services and application-context-owned runtime context infrastructure. |

The root package therefore remains meaningful and non-empty.

## Default-enabled infrastructure

Infrastructure required by multiple features is enabled independently from product features:

| Configuration | Default | Disable property | Responsibility |
|---|---|---|---|
| `GeniusStarter` | enabled | none | Registers Bean services, environment/provider services, runtime context, and task decorator. |
| `TaskConfiguration` | enabled when Boot task classes exist | `bus.context.task.enabled=false` | Composes ordered task decorators and propagates runtime context. |
| `WebConfiguration` | enabled for Servlet applications | `bus.context.web.enabled=false` disables binding only | Registers the shared `RequestContext` and conditionally registers context binding for request, async, and error dispatches. |

`GeniusStarter` contributes replaceable defaults for:

- `SpringContext`;
- `BeanProvider`;
- `BeanRegistry`;
- `BeanMetadata`;
- `EnvironmentResolver`;
- `ProviderRegistry`;
- `ContextManager`;
- `ContextBuilder`;
- `SpringBuilder`;
- `ContextDecorator`.

Each uses a concrete `@ConditionalOnMissingBean` contract. Applications can replace one service without replacing the
entire infrastructure graph.

### Context propagation defaults

`TaskConfiguration` sorts all `TaskDecorator` Beans, removes duplicate instances, ensures one `ContextDecorator`, and
installs a composite decorator on Spring Boot task executors. `WebConfiguration` always supplies the replaceable
`RequestContext` Bean in Servlet applications and registers `ContextBindingFilter` at
`Ordered.HIGHEST_PRECEDENCE + 10` for `REQUEST`, `ASYNC`, and `ERROR` dispatches unless binding is disabled.

```yaml
bus:
  context:
    task:
      enabled: true
    web:
      enabled: true
```

Both switches default to `true` when their required runtime classes are present.

## Feature activation model

Product features use one deterministic activation order: an explicit `@EnableXxx` annotation always enables its
feature, including when `bus.<feature>.enabled=false`; without the annotation, the feature is enabled only when its
`bus.<feature>.enabled` property is `true`. If neither activation source is present, the feature remains disabled.

| Feature | Import annotation | Property | Main responsibility |
|---|---|---|---|
| Auth | `@EnableAuth` | `bus.auth.enabled` | Authentication service and method resolution. |
| Cache | `@EnableCache` | `bus.cache.enabled` | Cache provider assembly and AspectJ proxy support. |
| CORS | `@EnableCors` | `bus.cors.enabled` | Validated Servlet MVC CORS policy. |
| Cortex | `@EnableCortex` | `bus.cortex.enabled` | Cortex registry and integration assembly. |
| Dubbo | `@EnableDubbo` | `bus.dubbo.enabled` | Apache Dubbo integration. |
| Elastic | `@EnableElastic` | `bus.elastic.enabled` | Elasticsearch REST client lifecycle. |
| Fabric | `@EnableFabric` | `bus.fabric.enabled` | TCP, WebSocket, and DNS service lifecycle. |
| Health | `@EnableHealth` | `bus.health.enabled` | System health and availability integration. |
| I18n | `@EnableI18n` | `bus.i18n.enabled` | Message source and Bus i18n adapter. |
| Image | `@EnableImage` | `bus.image.enabled` | Image and DICOM provider integration. |
| JDBC | `@EnableJdbc` | `bus.datasource.url` or `spring.datasource.url` | Validated dynamic data sources and routing. |
| JSON | `@EnableJson` | `bus.json.enabled` | Application-context JSON provider selection. |
| Limiter | `@EnableLimiter` | `bus.limiter.enabled` | Limiter scanning and service registration. |
| Mapper | `@EnableMapper` | `bus.mapper.enabled` | MyBatis mapper scanning, plugins, tenant context, and AOT. |
| Metrics | `@EnableMetrics` | `bus.metrics.enabled` | Metrics providers and endpoint. |
| Mongo | `@EnableMongo` | `bus.mongo.enabled` | Mongo client settings customization. |
| Notify | `@EnableNotify` | `bus.notify.enabled` | Notification registry and service lifecycle. |
| Office | `@EnableOffice` | `bus.office.enabled` | Document conversion and preview service. |
| Pay | `@EnablePay` | `bus.pay.enabled` | Payment registry and service. |
| Sensitive | `@EnableSensitive` | `bus.sensitive.enabled` | Log sanitizer lifecycle and optional MVC body advice. |
| Storage | `@EnableStorage` | `bus.storage.enabled` | Storage providers, registry, cache, and service. |
| Tempus | `@EnableTempus` | `bus.tempus.enabled` | Temporal clients, workers, and lifecycle. |
| Tracer | `@EnableTracer` | `bus.tracer.enabled` | Distributed tracing integration. |
| Validate | `@EnableValidate` | `bus.validate.enabled` | Method validation and exception advice. |
| Vortex | `@EnableVortex` | `bus.vortex.enabled` | Reactive routing gateway and asset lifecycle. |
| Wrapper | `@EnableWrapper` | `bus.wrapper.enabled` | MVC binding, converters, caching, advice, and route prefixes. |
| ZooKeeper | `@EnableZookeeper` | `bus.zookeeper.enabled` | Apache Curator client lifecycle. |

Each annotation imports the feature configuration directly. The shared `@ConditionalOnEnabled` rule from
`bus-spring` accepts that explicit annotation before evaluating `bus.<feature>.enabled` as the secondary source. Both
activation paths therefore reach the same feature configuration without creating a parallel implementation.

When JSON integration is enabled, `JsonConfiguration` selects one `JsonProvider` and `JsonBinding` installs that exact
provider for `JsonKit`, cached request-body parsing, and other shared static JSON consumers. Closing the Spring context
removes only the provider owned by that binding. If more than one JSON engine is present, set
`bus.json.provider=fastjson`, `gson`, or `jackson`; `AUTO` accepts exactly one available engine.

## Quick start

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

The annotations make selected integrations explicit in application code. The matching properties activate them.

## Configuration principles

All feature prefixes come from `GeniusBuilder`; do not duplicate prefix constants in another module. Configuration
properties validate invalid combinations during binding or Bean creation and defensively copy mutable collections or
arrays where necessary.

Common rules:

- every product feature has an `enabled` switch;
- optional dependencies use class conditions;
- application overrides use concrete Bean types or documented Bean names;
- secrets are excluded from diagnostic `toString()` output;
- timeouts use `Duration` where supported;
- long-running clients and services declare explicit destroy callbacks;
- context-owned global registrations are released when the context closes.

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

Wildcard origins cannot be combined with credentials. Arrays are copied defensively. Defaults include GET, POST, PUT,
OPTIONS, and DELETE, while the feature itself remains disabled until explicitly enabled.

## Sensitive data

```yaml
bus:
  sensitive:
    enabled: true
    debug: false
```

The transport-neutral path is always available when enabled:

```text
Sanitizer -> SensitiveBinding -> bus-logger Executor
```

`SensitiveBinding` unregisters its sanitizer when the owning application context closes. In Servlet MVC applications,
the nested `SensitiveConfiguration.ServletConfiguration` additionally contributes request decryption and response
encryption or masking advice. There is no separate `SensitiveWebConfiguration`.

Encryption keys must come from a protected external configuration source. Diagnostic output masks key material.

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

Each host must contain a valid port in the range `1..65535`. Timeouts and connection limits must be positive, and the
per-route limit cannot exceed the total limit.

## Fabric and DNS

`bus.fabric.enabled=true` or `@EnableFabric` activates the Fabric parent integration. The explicit annotation has
priority over the property. TCP socket support is enabled by default after the parent is active; WebSocket and DNS
remain child capabilities and require their own `enabled=true` property. DNS is deliberately imported by
`FabricConfiguration`, so `bus.fabric.dns.enabled=true` cannot create a second independent Fabric entry point.

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

The application must provide one `DnsSnapshotProvider`; it remains the owner of DNS zones and snapshots. Optional
`DnsSnapshotListener`, `DnsDynamicUpdateSink`, `DnsTsigKey`, and `TlsPolicy` beans extend lifecycle notifications,
dynamic updates, TSIG validation, and DoT respectively. The Starter owns only the runtime `DnsServer` bean and closes
it with the Spring context. DNS management, database access, and persistence are outside the Starter.

## JDBC

The Starter assembles JDBC automatically when the pool classes are available. Set `bus.datasource.enabled=false` to
disable automatic assembly; an explicit `@EnableJdbc` always has higher priority and still enables JDBC. Datasource
definitions use `bus.datasource` or `spring.datasource`, and both use the same root-primary plus `multi` structure.
They are never merged: a `bus.datasource` URL selects the complete Bus group and overrides `spring.datasource`.

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

JDBC responsibilities are fixed. Reusable `DataSourceResolver`, `DataSourceDefinition`, `DataSourceMapping`,
`DataSourceFactory`, `DynamicDataSource`, `DataSourceHolder`, and `AspectjJdbcProxy` live in `bus-spring` under
`org.miaixz.bus.spring.jdbc`. The Starter package retains only `JdbcConfiguration`, which assembles Beans, and
`JdbcDescriptor`, which defines the Bus-before-Spring prefix order and Hikari default. Both prefixes use
the same resolver path. The root `name` is the default route and every `multi` entry supplies an additional route.
Names must be nonblank and unique across the complete group. JDBC never references Mapper. When Mapper is enabled, its
own `DataSourceListener` synchronizes dialect state for initial and runtime route changes. The routing Bean owns every
pool created from these definitions: replacing or removing a route closes the unreferenced pool, and application-context
shutdown closes every remaining pool exactly once.

Service methods select a named datasource with `@DataSource`. A method annotation overrides its class annotation, and
nested invocations restore the exact parent route on return or failure:

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

Routing must occur before transaction advice obtains a connection. Put `@DataSource` on the externally invoked service
transaction boundary, or call a routed inner operation through another Spring bean proxy. Self-invocation through
`this` bypasses AOP, and an already active outer transaction cannot change its acquired connection. Dynamic routing is
not a distributed transaction mechanism. Tenant or routing information must come from trusted runtime context rather
than request-controlled model fields.

## Mapper

Mapper integration covers:

- deterministic mapper classpath scanning;
- `MapperFactoryBean` and scanner registration;
- XML/resource location resolution;
- ordered plugin construction;
- tenant identity from `ContextBuilder`;
- tenant exception advice;
- AOT Bean factory initialization and runtime hints.

Business code must not overwrite tenant identity through request binding. Custom mapper plugins should use the
documented provider and interceptor extension points instead of modifying the Starter registry after startup.
`@EnableMapper` scans its declaring package when no package attribute is supplied. Property activation uses
`bus.mapper.base-package`; when that is also absent, Spring Boot application packages are scanned for explicit
`@Mapper` interfaces. An unresolved scan scope fails startup instead of silently registering no Mapper. Dialects are
bound to the owning MyBatis `Configuration`, so two application contexts cannot overwrite each other's route provider.

## Wrapper capabilities

`bus.wrapper.enabled=true` activates the aggregate wrapper configuration. Child features remain independently
controlled:

| Capability | Property | Default after Wrapper is enabled |
|---|---|---|
| Request-object binding | `bus.wrapper.request-binding.enabled` | `true` |
| Message converters | `bus.wrapper.message-converters.enabled` | `true` |
| Bounded body cache | `bus.wrapper.body-cache.enabled` | `false` |
| Response advice | `bus.wrapper.response-advice.enabled` | `false` |
| Route prefix | `bus.wrapper.route-prefix.enabled` | `false` |

```yaml
bus:
  wrapper:
    enabled: true
    request-binding:
      enabled: true
    message-converters:
      enabled: true
    body-cache:
      enabled: false
    response-advice:
      enabled: false
    route-prefix:
      enabled: false
```

Request-object binding requires `@RequestObject`, excludes framework and simple scalar types, and does not allow request
input to replace trusted tenant context. Body caching is bounded; multipart and response diagnostic caching remain
opt-in.

## Client and service lifecycle

The Starter owns the lifecycle of clients and long-running services it creates:

| Feature | Lifecycle examples |
|---|---|
| Elastic | REST transport/client close. |
| Fabric | TCP and WebSocket service start/stop. |
| Notify, Office, Pay, Storage | Registry/service creation and context cleanup. |
| Tempus | Client, worker factory, workers, and shutdown. |
| Vortex | Router graph, server start/stop, and asset lifecycle. |
| ZooKeeper | Curator client start/close. |

An application-provided replacement Bean owns its own lifecycle unless the replacement contract states otherwise.

## Bean override rules

Prefer replacing the concrete product contract:

```java
@Bean
StorageService customStorageService(...) {
    return new StorageService(...);
}
```

Do not depend on configuration implementation classes from business code. Configuration and property packages are
opened to Spring for framework access but are not exported as general JPMS APIs. Only
`org.miaixz.bus.starter.annotation` is exported.

## Package layout

| Package group | Content |
|---|---|
| root | Shared startup infrastructure and property-prefix constants. |
| `annotation` | Public `@Enable*` annotations. |
| `context` | Default task and Servlet context propagation. |
| feature packages | One feature's configuration, properties, services, and lifecycle collaborators. |
| `wrapper.*` | Independently controlled MVC wrapper capabilities. |

There are no `internal` packages. Feature implementation types stay in their current feature package.

## Security defaults

- Product features are opt-in.
- Credentialed CORS rejects wildcard origins.
- Sensitive keys and credentials are not exposed by property diagnostics.
- Request binding does not source authenticated tenant identity from user input.
- Cached bodies have explicit bounds and multipart caching is opt-in.
- Logging sanitization is owned by `bus-sensitive`, not `bus-logger`.
- Context state is isolated per application context and restored after asynchronous execution.
- Third-party resources are closed with the owning Spring context.

## Native Image and AOT

Spring AOT generates most configuration and Bean reflection hints. Checked-in reachability metadata therefore lists only
exact constructors and dynamically accessed members. Entries and nested member lists are sorted A–Z.

The following broad grants are prohibited:

- `allDeclaredConstructors` and `allPublicConstructors`;
- `allDeclaredMethods` and `allPublicMethods`;
- `allDeclaredFields` and `allPublicFields`.

The Abarth metadata audit resolves every configured class, constructor, method, field, proxy, and resource against the
current runtime model and also enforces ordering.

## Migration rules

- Use `XxxConfiguration`; removed `XxxAutoConfiguration` names must not return.
- Use `ContextState`, `ContextScope`, and `ContextDecorator` for runtime propagation.
- Use `GeniusBuilder` for Starter property prefixes.
- Keep reusable mechanics in `bus-spring` and domain behavior in the owning Bus module.
- Do not introduce an `internal` package under Starter.
- Override defaults by Bean type or documented Bean name.
- Keep `AutoConfiguration.imports`, `spring.factories`, module descriptors, and reachability metadata aligned with class
  renames.

## Verification boundary

Bus contains and runs no tests. Starter integration, binding, lifecycle, module-path, metadata, AOT, and Native Image
tests are maintained in the sibling Abarth repository. Bus builds must skip tests explicitly.
