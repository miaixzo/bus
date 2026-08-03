# bus-spring

`bus-spring` is the reusable Spring integration layer for Bus. It provides application-context-owned Bean services,
runtime context capture and propagation, Spring Boot lifecycle utilities, annotation helpers, and reusable Servlet MVC
infrastructure. The module supplies reusable condition mechanics, not product-feature decisions; concrete enable
annotations, property prefixes, conditional assembly, and configuration properties belong to `bus-starter`.

## Responsibility boundary

```text
bus-spring                              bus-starter
-----------------------------------     -----------------------------------
Spring and Web integration mechanics    discovery and startup assembly
context capture/install/restore         bus.* configuration properties
Bean, environment, and condition APIs   feature activation decisions
Boot listener implementations           default Bean declarations
request/converter/wrapper primitives     feature-specific integration
```

This separation keeps Spring utilities reusable without forcing every Bus feature into an application. `bus-spring`
does not contain a `spring.factories` registration resource; `bus-starter` owns discovery and references the lifecycle
implementations provided here.

## Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-spring</artifactId>
    <version>${revision}</version>
</dependency>
```

Applications normally receive this module transitively through `bus-starter`. Add it directly when only the reusable
Spring APIs are required and activation will be provided by the application.

## Package structure

| Package | Responsibility |
|---|---|
| `org.miaixz.bus.spring` | Runtime context APIs and the `SpringBuilder` facade. |
| `annotation` | Merged annotation handling, placeholder binding, wrapper annotations, and `@RequestObject`. |
| `aop` | Reusable auto-proxy infrastructure with Bean-name exclusions. |
| `bean` | Focused Bean lookup, registration, metadata, environment, context, and provider services. |
| `jdbc` | Reusable datasource resolution, pool creation, dynamic routing, route scope, annotation, and advice. |
| `web` | Root Servlet request access and context-binding filter. |
| `web.advice` | Reusable MVC response advice base implementation. |
| `web.converter` | JSON/text converters, type matching, registration, and MVC configurers. |
| `web.interceptor` | Request interception helpers. |
| `web.resolver` | Explicit request-object argument binding and binding options. |
| `web.routing` | Controller route-prefix mapping. |
| `web.wrapper` | Bounded request/response body caching wrappers and filter. |
| `boot` | Spring Boot run listener and smart lifecycle base class. |
| `boot.banner` | Text, image, and version banner selection and rendering. |
| `boot.condition` | Reusable annotation-first Spring Boot activation condition. |
| `boot.environment` | Early Spring, logging, scenes, and cloud environment processors. |
| `boot.listener` | Spring Boot and Spring Cloud configuration listeners. |
| `boot.startup` | Startup stages, metrics, reporters, and Bean post-processing. |

The root package intentionally remains populated. `ContextBuilder`, `ContextManager`, `ContextProvider`, `ContextState`,
`ContextScope`, `ContextDecorator`, and `SpringBuilder` are stable public capabilities rather than an empty namespace.

`boot.condition` provides `@ConditionalOnEnabled` and `EnabledCondition`. The condition gives an explicit enable
annotation priority over the corresponding property, while leaving concrete annotations and property prefixes to the
consuming Starter module. Its `name` member defaults to `enabled`, `matchIfMissing` defaults to `false`, and the
condition can guard either a configuration type or an individual Bean method.

## JDBC datasource infrastructure

`org.miaixz.bus.spring.jdbc.DataSource` is the public Spring contract for selecting a datasource at a service boundary.
Its value must identify a resolved datasource route. `DataSourceResolver`, `DataSourceDefinition`, and
`DataSourceMapping` resolve an ordered list of compatible property prefixes into one validated mapping;
`DataSourceFactory` creates the configured pools; `DynamicDataSource` performs routing; each application context owns
an independent `DataSourceHolder` for exact nested route scopes; and `DataSourceListener` reports successful initial,
added, replaced, and removed routes. `AspectjJdbcProxy`
interprets the annotation before transaction advice obtains a connection. These types are independent of Mapper and
Starter assembly. `bus-starter` supplies only the supported prefix order, default pool type, and Spring Beans.

## Runtime context model

Runtime state is owned by one `ContextManager` per Spring application context. It is not stored in a global static
application-context registry.

| Type | Responsibility |
|---|---|
| `ContextManager` | Owns the current thread state and performs capture, install, restore, and clear operations. |
| `ContextState` | Immutable detached snapshot containing request ID, a defensive authorization copy, and resolved credential metadata. |
| `ContextBuilder` | Public facade for request IDs, authorization, tenant, credential, token, and API key access. |
| `ContextScope` | `AutoCloseable` guard that restores the previous state exactly once. |
| `ContextDecorator` | Spring `TaskDecorator` that propagates captured state to executor tasks. |
| `ContextProvider` | Ordered extension point that can supply authorization state. |

`ContextState` never retains `HttpServletRequest`, cached bodies, multipart data, or a thread-local container. Token and
API-key credentials are resolved once at the Servlet boundary and retained only as immutable credential values with
redacted diagnostics. This makes the snapshot suitable for bounded asynchronous propagation.

### Capture and install

```java
ContextState state = contextBuilder.capture();

try (ContextScope ignored = contextBuilder.install(state)) {
    operation.run();
}
```

Closing the scope restores the worker thread's previous state, including when the operation throws.

### Executor propagation

```java
Runnable decorated = contextDecorator.decorate(() -> service.process(command));
executor.execute(decorated);
```

`ContextDecorator` captures at decoration time, installs before execution, and restores afterward. With
`bus-starter`, `TaskConfiguration` registers this decorator by default when Spring Boot task classes are present. It can
be disabled with `bus.context.task.enabled=false`.

### Context access

```java
String requestId = contextBuilder.getRequestId();
String tenantId = contextBuilder.getTenantId();
String token = contextBuilder.getToken();
String apiKey = contextBuilder.getApiKey();
Http.Auth.Credential credential = contextBuilder.getCredential();
```

Token and API-key values are stored independently. `getCredential()` prefers the token when both are present, while
`getToken()` and `getApiKey()` continue to expose their respective values. Resolution follows `Http.Auth`: headers,
parameters, an available cached JSON body, and cookies. Raw request bodies are never consumed by context resolution.

Use `clear()` at an integration boundary that owns the current thread state. It removes the request ID, authorization,
token, and API-key state owned by the current thread.

## Spring Bean services

`SpringBuilder` is a convenience facade over six focused services:

| Service | Responsibility |
|---|---|
| `SpringContext` | Holds the owning `ApplicationContext` and publishes events. |
| `BeanProvider` | Reads Beans by name, class, or `TypeReference`. |
| `BeanRegistry` | Registers and removes Bean definitions and singletons. |
| `BeanMetadata` | Resolves Bean types and configuration-source origin without instantiation side effects. |
| `EnvironmentResolver` | Resolves properties, profiles, application name, modes, and placeholders. |
| `ProviderRegistry` | Discovers ordered Bus providers and caches them within the owning context. |

### Lookup

```java
OrderService service = springBuilder.getBean(OrderService.class);
Map<String, Validator> validators = springBuilder.getBeansOfType(Validator.class);
String[] names = springBuilder.getBeanNamesForType(Validator.class);
```

### Environment

```java
String applicationName = springBuilder.getApplicationName();
String profile = springBuilder.getActiveProfile();
String endpoint = springBuilder.replacePlaceholders("${service.endpoint}");

if (springBuilder.isProdMode()) {
    // production-specific application behavior
}
```

### Registration

```java
springBuilder.registerBeanDefinition("orderValidator", OrderValidator.class);
springBuilder.registerSingleton("clock", Clock.class, Clock.systemUTC());
```

Mutation APIs are intended for infrastructure that owns the Bean lifecycle. Business code should prefer constructor
injection. Use the focused service directly when only one responsibility is needed; use `SpringBuilder` when several
services are genuinely required together.

## Annotation utilities

- `AnnotationWrapper` and `WrapperAnnotation` support merged or wrapped annotation access.
- `PlaceholderBinder`, `DefaultPlaceholderBinder`, and `PlaceholderHandler` resolve annotation attributes containing
  environment placeholders.
- `@RequestObject` explicitly opts a controller method parameter into Bus request-object binding.

`@RequestObject` is deliberately explicit. The resolver does not claim every complex MVC parameter automatically.

## Servlet context binding

`ContextBindingFilter` establishes runtime state for a request and restores it for all supported dispatch paths,
including normal, asynchronous, and error dispatches. `RequestContext` provides structured access to request values:

- headers and authorization data;
- query and form parameters;
- cookies;
- path variables;
- multipart values;
- cached JSON body fields.

With `bus-starter`, `WebConfiguration` always supplies one replaceable `RequestContext` Bean for Servlet integrations
and registers the filter by default. Disable context binding explicitly with:

```yaml
bus:
  context:
    web:
      enabled: false
```

The filter is reusable infrastructure; feature-specific request advice remains in the relevant Starter package.

## Request-object binding

The resolver package contains:

| Type | Responsibility |
|---|---|
| `RequestObjectArgumentResolver` | Builds an explicitly selected controller argument from request values. |
| `AutoBindingTypeMatcher` | Determines which types may participate in binding. |
| `RequestBindingOptions` | Immutable binding behavior and limits. |
| `BindingDefaults` | Shared safe defaults. |
| `RequestWebMvcConfigurer` | Installs the resolver into MVC. |

Framework types and simple scalar arguments are excluded. Tenant identity must come from authenticated runtime context
and must not be overwritten by request input.

## HTTP message conversion

The converter package provides:

- `AbstractHttpMessageConverter` as the common converter base;
- `JsonMessageConverter` for the Bus JSON provider;
- `JsonTypeMatcher` for supported JSON targets;
- `MessageConverterRegistrar` for deterministic converter placement;
- `JsonWebMvcConfigurer` and `TextWebMvcConfigurer` for MVC integration.

`bus-spring` supplies these mechanics but does not automatically activate them. Starter's wrapper converter
configuration owns activation and conditions.

## Body caching

`CachedBodyRequestWrapper` and `CachedBodyResponseWrapper` expose repeatable bounded bodies. `CachedBodyFilter` applies
them using `BodyCacheOptions`.

Safety rules:

- request and response cache sizes are bounded;
- multipart caching is opt-in;
- only supported HTTP methods are wrapped;
- response diagnostic caching is independent from response delivery;
- wrappers are request-scoped and never enter `ContextState`;
- when enabled, request body caching runs before context binding so JSON credentials can be resolved without consuming
  the raw Servlet stream.

Starter keeps body caching disabled by default even when aggregate wrapper support is enabled.

## Response advice and routing

- `MessageResponseBodyAdvice` supplies reusable MVC response processing.
- `RoutePrefixHandlerMapping` applies configured prefixes to selected controller routes.
- `RoutePrefixProperties` represents route-prefix mechanics independent of Starter property activation.
- `SentinelRequestHandler` provides reusable request interception integration.

These packages do not decide whether the application enables response wrapping, route prefixes, or an optional
third-party feature.

## Spring Boot lifecycle

The Boot packages contain infrastructure executed before and during application startup:

- `SpringApplicationRunListener` integrates Bus startup stages with Spring Boot;
- `SpringSmartLifecycle` provides a reusable lifecycle base;
- environment processors prepare scenes, logging, Spring, and cloud configuration;
- configuration listeners observe Boot and Spring Cloud configuration phases;
- banner implementations render text, image, and version information;
- startup metrics capture application, module, child, and Bean stages;
- `StartupReporter` and related contracts publish startup diagnostics.

Discovery remains centralized in `bus-starter/src/main/resources/META-INF/spring.factories`. Do not add a second
discovery file to `bus-spring`.

## Activation through Starter

The reusable infrastructure is assembled by three Starter configurations:

| Configuration | Default | Disable switch |
|---|---|---|
| `GeniusStarter` | enabled | none |
| `TaskConfiguration` | enabled when task classes exist | `bus.context.task.enabled=false` |
| `WebConfiguration` | enabled for Servlet applications | `bus.context.web.enabled=false` disables binding only |

Product features such as cache, mapper, sensitive, storage, or Vortex are separate and remain controlled by their own
`bus.<feature>.enabled` properties.

## Extension guidance

- Implement `ContextProvider` to contribute authorization state; use Spring ordering for deterministic precedence.
- Implement a Bus `Provider` and let `ProviderRegistry` resolve it within the owning context.
- Reuse converter, resolver, advice, routing, or wrapper primitives from a Starter configuration rather than adding
  activation metadata here.
- Do not introduce a static `ApplicationContext` holder.
- Do not place feature properties or third-party client lifecycles in this module.

## JPMS and Native Image

The module exports reusable public packages. Boot implementation packages are not exported; they are opened narrowly to
Spring Boot and Spring Core for framework access. Optional Spring, Servlet, and persistence dependencies remain static
module requirements.

Reachability metadata contains exact dynamically accessed constructors and members, sorted A–Z. Broad `allDeclared*`
and `allPublic*` reflection grants are prohibited.

## Verification boundary

Bus contains and runs no tests. Context, Bean service, Web, module-path, lifecycle, metadata, AOT, and Native Image tests
are maintained in the sibling Abarth repository. Bus builds must skip tests explicitly.
