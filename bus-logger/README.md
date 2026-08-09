# bus-logger

`bus-logger` is the framework-neutral logging facade used by Bus modules. It provides one API over the logging backend
available at runtime, keeps backend-native placeholder formatting, resolves caller identities, caches providers, and
offers a small pre-output processing pipeline. It does not own application logging configuration or sensitive-data
policy.

## Module responsibilities

The module is responsible for:

- selecting an available logging implementation;
- resolving and caching named or class-based log providers;
- exposing TRACE, DEBUG, INFO, WARN, and ERROR APIs;
- forwarding parameterized messages without eagerly formatting them;
- applying registered `Operator` instances before provider output;
- supplying normal and colored console fallbacks;
- exposing SPI contracts for additional logging backends.

The module is not responsible for:

- configuring Logback, Log4j 2, JUL, or another backend;
- choosing an application's log file, pattern, rotation, or retention policy;
- classifying passwords, tokens, cookies, or other protected data;
- retaining request or Spring application context state.

Sensitive-data classification belongs to `bus-sensitive`. Spring lifecycle registration of a sanitizer belongs to
`bus-starter`.

## Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>${revision}</version>
</dependency>
```

Backend dependencies are optional. The application should add and configure the backend it intends to use.

## Architecture

```text
application code
      |
      v
   Logger --------------------------+
      |                             |
      v                             v
  Loggable -> Executor -> Operator(s)
      |
      v
   Provider -> backend adapter -> logging framework
      ^
      |
 Registry -> Holder -> Factory -> SPI discovery
```

| Type       | Responsibility                                                                                         |
|------------|--------------------------------------------------------------------------------------------------------|
| `Logger`   | Static facade, caller resolution, level checks, aligned diagnostic output, and generic `log` dispatch. |
| `Level`    | Common `TRACE`, `DEBUG`, `INFO`, `WARN`, and `ERROR` level model.                                      |
| `Provider` | Backend-neutral logging operations and level checks.                                                   |
| `Factory`  | Creates and caches providers for names and classes.                                                    |
| `Holder`   | Selects or explicitly installs the default factory.                                                    |
| `Registry` | Resolves the cached provider for a name or class.                                                      |
| `Loggable` | Immutable event snapshot with a defensive copy of its argument array.                                  |
| `Operator` | Transforms a complete event or a named diagnostic value without writing output.                        |
| `Executor` | Applies operators in registration order and isolates logging from operator failures.                   |

## Supported backends

The module includes adapters for:

| Backend                | Factory                 | Provider                 |
|------------------------|-------------------------|--------------------------|
| SLF4J                  | `Slf4jLoggingFactory`   | `Slf4jLoggingProvider`   |
| Log4j 2                | `Log4jLoggingFactory`   | `Log4jLoggingProvider`   |
| Apache Commons Logging | `CommonsLoggingFactory` | `CommonsLoggingProvider` |
| JBoss Logging          | `JbossLoggingFactory`   | `JbossLoggingProvider`   |
| `java.util.logging`    | `JdkLoggingFactory`     | `JdkLoggingProvider`     |
| tinylog                | `TinyLoggingFactory`    | `TinyLoggingProvider`    |
| Colored console        | `ColorLoggingFactory`   | `ColorLoggingProvider`   |
| Plain console          | `NormalLoggingFactory`  | `NormalLoggingProvider`  |

Factories are discovered through `META-INF/services/org.miaixz.bus.logger.Factory`. Discovery selects the first
available SPI implementation. If no supported provider is available, a classpath `logging.properties` selects JUL;
otherwise the plain console provider is used.

Use `Logger.getFactory()` to inspect the selected factory class. Use `Holder.setDefaultFactory(...)` only when the
application must force a backend before the first provider is resolved.

## Basic usage

### Static facade

```java
import org.miaixz.bus.logger.Logger;

Logger.trace("Loading order: orderId={}", orderId);
Logger.debug("Resolved {} order lines", lines.size());
Logger.info("Order accepted: orderId={}", orderId);
Logger.warn("Retrying request: attempt={}", attempt);
Logger.error(failure, "Order processing failed: orderId={}", orderId);
```

The `{}` arguments remain separate until they reach the provider. Do not build messages through string concatenation
when placeholder formatting is sufficient.

### Reusable provider

Resolve a provider when a class performs frequent logging or must guard expensive diagnostic work:

```java
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.Registry;

private static final Provider LOG = Registry.get(OrderService.class);

if (LOG.isDebugEnabled()) {
    LOG.debug("Loaded order graph: {}", buildExpensiveDiagnostic(order));
}
```

`Registry.get(Class<?>)` and `Registry.get(String)` return providers cached by the selected factory.

### Exceptions

```java
try {
    repository.save(order);
} catch (RuntimeException failure) {
    Logger.error(failure, "Unable to persist order: orderId={}", order.getId());
    throw failure;
}
```

Pass the exception through the throwable overload. Do not interpolate the exception into the message and lose its stack
trace.

### Dynamic levels

```java
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Logger;

if (Logger.isEnabled(Level.DEBUG)) {
    Logger.log(Level.DEBUG, null, "Cache state: key={}, value={}", key, value);
}

Level previous = Logger.getLevel();
Logger.setLevel(Level.INFO);
```

Programmatic level changes affect providers that support runtime level control. Normal backend configuration remains the
preferred application-level mechanism.

### Aligned diagnostic output

The `Logger` overloads accepting `isEntry`, `tag`, and optional `width` are intended for structured startup and
lifecycle diagnostics:

```java
Logger.info(true, "Storage", "Initializing provider: type={}", providerType);
Logger.info(false, "Storage", 24, "Provider ready: type={}", providerType);
```

Use ordinary placeholder overloads for normal business logs.

## Event processing

An `Operator` can transform every event immediately before provider dispatch:

```java
import org.miaixz.bus.logger.Executor;
import org.miaixz.bus.logger.Loggable;
import org.miaixz.bus.logger.Operator;

Operator tenantTag = event -> new Loggable(
        event.level(),
        event.throwable(),
        "[tenant-a] " + event.format(),
        event.arguments());

Executor.register(tenantTag);
try {
    Logger.info("Order accepted: orderId={}", orderId);
} finally {
    Executor.unregister(tenantTag);
}
```

Operator behavior is deliberately constrained:

- registration is based on object identity;
- repeated registration of the same instance is reference-counted;
- execution order is registration order;
- returning `null` leaves the current event unchanged;
- an operator exception produces `[LOG PROCESSING FAILED]` without exposing rejected arguments;
- the hot path reads an immutable operator snapshot without registration locking.

`Executor.processValue(key, value)` applies the same operators to named values produced outside the facade. It is useful
for structured diagnostics that still require the application's registered protection policy.

## Sensitive logging

`bus-logger` intentionally contains no list of sensitive field names. When `bus-sensitive` and `bus-starter` are used,
`SensitiveConfiguration` creates a `Sanitizer`, and `SensitiveBinding` registers it with `Executor` for the lifetime of
the Spring application context.

```yaml
bus:
  sensitive:
    enabled: true
```

Use named placeholders when values require classification:

```java
Logger.warn("Login rejected: username={}, password={}", username, password);
```

The name immediately before a placeholder gives the sanitizer enough context to protect `password`. A positional value
without a meaningful field name cannot be classified safely.

## Custom backend

Implement `Factory` and `Provider`, then publish the factory through the Java service loader:

```java
public final class AcmeFactory implements Factory {

    @Override
    public String getName() {
        return "Acme";
    }

    @Override
    public Provider of(String name) {
        return new AcmeProvider(name);
    }

    @Override
    public Provider of(Class<?> type) {
        return of(type.getName());
    }

}
```

Resource file:

```text
META-INF/services/org.miaixz.bus.logger.Factory
```

Its content is the fully qualified factory class name. The adapter should preserve placeholder arguments and implement
all level checks accurately.

## Package layout

| Package                             | Content                                                            |
|-------------------------------------|--------------------------------------------------------------------|
| `org.miaixz.bus.logger`             | Public facade, event model, registry, executor, and SPI contracts. |
| `org.miaixz.bus.logger.magic`       | Shared abstract factory and provider implementations.              |
| `org.miaixz.bus.logger.magic.level` | Reusable level-specific contracts.                                 |
| `org.miaixz.bus.logger.nimble.*`    | Backend adapters.                                                  |

All of these packages are exported by the JPMS module. Backend modules are optional static requirements.

## Best practices

- Prefer parameterized logging over string concatenation.
- Guard expensive diagnostic construction with the matching level check.
- Use a class-based provider for stable logger identity.
- Pass failures to throwable overloads.
- Register cross-cutting operators once per lifecycle owner and always unregister them.
- Keep business masking rules out of logger factories and providers.
- Configure formatting, files, rotation, and retention in the selected backend.

## Native Image

Reachability metadata covers service-loaded factories and exact dynamically accessed members. Entries are sorted A–Z.
Broad reflection grants such as `allDeclaredConstructors`, `allDeclaredMethods`, and `allDeclaredFields` are prohibited.

## Verification boundary

Bus contains and runs no tests. Provider, pipeline, lifecycle, metadata, AOT, and Native Image tests are maintained in
the sibling Abarth repository. Bus builds must skip tests explicitly.
