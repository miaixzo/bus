# Bus Errors

`bus-errors` provides a shared, transport-independent contract for classified failures and integrates with `bus-logger` to publish error diagnostics.

| Type | Purpose |
|---|---|
| `ErrorType` | A stable semantic identifier. Use the common types where they apply and a module-qualified name such as `cache.backend-timeout` for component-specific failures. |
| `ErrorDescriptor` | An immutable failure value containing a type, an optional legacy application code, a public message key, and a detached map of approved public parameters. |
| `BusException` | An unchecked carrier for an error descriptor and its diagnostic cause. |

Use `org.miaixz:bus-errors` with the Bus BOM. The JPMS module name is `bus.errors` and the exported package is `org.miaixz.bus.errors`.

```java
var rejection = new ErrorDescriptor(ErrorType.RATE_LIMITED, "limits.rate");

var failure = new ErrorDescriptor(ErrorType.DEPENDENCY_UNAVAILABLE, "storage.unavailable");
throw new BusException(failure, cause);
```

Return a descriptor directly for an expected rejection. Use an exception when the failure must propagate through an exception boundary. The receiving application or protocol adapter chooses the status, headers, and body and resolves the public message key. Pass only values approved for disclosure in `publicParameters`. The exception message is the key and the cause remains available for diagnostics. On the JVM, Java object serialization can retain the descriptor and serializable diagnostics. Use an explicit protocol representation at external boundaries.

The existing Bus error classes, codes, and response envelopes remain available. See the [architecture](../docs/bus-errors-architecture_CN.md) and [migration matrix](../docs/bus-errors-component-migration_CN.md) for the rollout.
