# Bus Errors

`bus-errors` classifies failures, governs public error responses for MVC and Vortex, and publishes structured diagnostics through `bus-logger`.

| Type | Purpose |
|---|---|
| `ErrorType` | A stable semantic identifier. Use the common types where they apply and a module-qualified name such as `cache.backend-timeout` for component-specific failures. |
| `ErrorDescriptor` | An immutable failure value containing a type, an optional legacy application code, a public message key, and a detached map of approved public parameters. |
| `ClassifiedException` | An unchecked carrier for an error descriptor and its diagnostic cause. |

Use `org.miaixz:bus-errors` with the Bus BOM. The JPMS module `bus.errors` exports the core API and its policy, recovery, SPI, and transport packages.

```java
var rejection = new ErrorDescriptor(ErrorType.RATE_LIMITED, "limits.rate");

var failure = new ErrorDescriptor(ErrorType.DEPENDENCY_UNAVAILABLE, "storage.unavailable");
throw new ClassifiedException(failure, cause);
```

Return a descriptor for an expected rejection or use `ClassifiedException` when a failure needs to propagate.
`publicParameters` carries values approved for disclosure. The exception message is the message key and the cause
remains available for diagnostics.

## Public HTTP responses

`ErrorBoundary` classifies the failure and records its outcome. The MVC, Servlet, and reactive adapters use verified
status and route facts, public messages, and approved response headers. Protocol renderers preserve the required
response shape, including legacy `Message`, JSON-RPC errors, and SSE error frames. Upstream HTTP errors pass through
the public error mapping before the gateway responds.

Starter's `bus.errors.routing` selects empty native HTTP responses by listener, path, and verified numeric status.
The adapters use the existing protocol or legacy contract for other errors. A committed stream can emit a safe
protocol frame or terminate. A connector rejection before dispatch and opaque upstream success data sit outside
application error classification.

## Error diagnostics

`ErrorLogPolicy` selects severity and safe fields. The standard observer delegates output to `bus-logger`, whose
provider thresholds control what is emitted. At ordinary thresholds, expected client errors typically use `DEBUG`,
oversized payloads use `INFO`, gateway overload uses `WARN`, and unexpected terminal failures use `ERROR`. `TRACE`
adds bounded diagnostic detail. The Starter switch `bus.errors.logging.enabled` controls the standard subscription.
See the [Starter configuration](../bus-starter/README.md#shared-http-error-responses).
