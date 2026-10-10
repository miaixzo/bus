# Bus Metrics

`bus-metrics` is the provider-neutral metrics component for Bus. It supplies counters, gauges, timers, histograms,
EWMA meters, cardinality control, explicit SLO accounting, LLM metrics, host/JVM binders, Prometheus scraping and
versioned Cortex snapshots.

The module requires Java 21. Native metrics are always available. Micrometer, OpenTelemetry, Prometheus, bus-health
and Servlet support remain optional Maven dependencies.

## Provider model

| Provider | Required runtime object | Ownership |
|:--|:--|:--|
| `NativeProvider` | none | owns and closes its scheduler |
| `MicrometerProvider` | one `MeterRegistry` | removes its meters; never closes the registry |
| `OpenTelemetryProvider` | one `OpenTelemetry` API object | closes its callbacks; never closes the SDK |
| `PrometheusProvider` | zero or one `PrometheusRegistry` | unregisters its collectors; never clears or closes an external registry |

All providers expose the same local read contract. An empty timer or histogram has `count=0`, `total=0`, `max=0`,
and percentile reads return `Double.NaN`. Input is validated before either local aggregation or an external backend is
changed.

The instrumentation scope is `org.miaixz.bus.metrics`. Typed registration preserves descriptor name, kind, numeric
kind, unit, description and ordered attribute schema. A provider rejects families that collapse to the same final
backend name.

## Direct use

```java
Provider provider = new NativeProvider();
Counter created = provider.counter("order.created", Tag.of("region", "cn"));
created.increment();
Timer latency = provider.timer("payment.duration", Tag.of("operation", "capture"));
latency.record(25, TimeUnit.MILLISECONDS);
Histogram payload = provider.histogram("response.size", Tag.of("route", "orders"));
payload.record(1536);
AtomicLong queueSize = new AtomicLong();
Gauge queue = provider.gauge("queue.size", queueSize, AtomicLong::doubleValue);
provider.close();
```

`Metrics` remains the static compatibility facade. `Metrics.installProvider(provider, owner)` returns a reversible
`ProviderLease`; closing it restores the previous facade state but does not close either provider.

## Rates and distributions

Each provider caches `Meter` by name and tags and owns one daemon scheduler that advances all 1-, 5- and 15-minute
EWMA rates every five seconds. `RatePair` uses `<name>.total`, `<name>.successes` and `<name>.errors`; one success or
error increments `total` exactly once. An empty pair returns error rate `0` and success rate `1`.

Native distribution aggregation uses a bounded T-Digest. Timer rolling reads use 60 one-second buckets for one minute
and 60 five-second buckets for five minutes. Rotation is lazy and needs no timer scheduler.

```java
double p95 = latency.percentile(0.95, TimeUnit.MILLISECONDS);
double rolling = latency.percentile(0.95, TimeUnit.MILLISECONDS, Timer.Window.ONE_MINUTE);
```

`NativeTimer.rotate1m()` and `rotate5m()` remain compatibility operations that explicitly clear the corresponding
rolling view. `BucketWindow`, `NativeRatePair`, `NativeLlmTimer`, `CardinalityViolation` and
`org.miaixz.bus.metrics.observe.Test` are retained compatibility types.

## Cardinality

`CardinalityGuard.Scope` is provider-local. `firstN(n)` maps overflow to `__overflow__`; `topN(n)` admits a bounded,
stable set and maps later values to `__other__`; `deny()` removes the tag. Each replacement or denial increments the
scope count and publishes one `CardinalityViolation`. Provider diagnostics record it as
`bus.metrics.resources.dropped{category="cardinality",reason="replaced|denied"}`. Listener failures are isolated and
listeners are removed when the provider closes.

## LLM metrics

```java
LlmSample sample = Metrics.llmTimer("ai.chat", "tenant", "public")
        .start("gpt", "openai", "chat");
sample.recordFirstToken();
sample.stop(inputTokens, outputTokens, finishReason);
```

Base tags are preserved. `model`, `provider`, `operation`, `finish_reason`, `type` and `error_type` are reserved dynamic
tags. Tokens must be non-negative. First-token is first-write-wins, and only the first terminal `stop` or `error`
records duration, token, cost or error data. Unknown price models record no cost.

## Explicit SLO tracking

SLO registration does not subscribe to metrics. Metric name and tags are compatibility metadata; callers submit every
observation explicitly.

```java
SloTracker slo = Metrics.slo()
        .trackLatency("checkout.latency", "http.server.requests", 300, 0.999)
        .onBudgetExhausted("checkout.latency", event -> alert(event));
slo.record("checkout.latency", durationMillis, failed);
```

The error budget uses 60 bounded time buckets. Availability uses the stricter of the target and
`1 - maxErrorRatio`. Exhaustion callbacks are edge-triggered and may fire again after recovery.

## Host, JVM and exporters

`JvmMetrics` only reports JVM runtime, threads, class loading, heap and GC. Host CPU, memory, paging, disk, filesystem,
network, process and container binders use `HealthMetricSource` backed by bus-health. Unsupported capabilities omit a
family instead of publishing synthetic zeroes. `SystemMetrics` remains an explicit compatibility binder and is not
installed by the starter.

`PrometheusExporter` consumes `ScrapeSupport`, does not branch on provider type, and owns neither provider nor registry.
Its content type is `text/plain; version=0.0.4; charset=utf-8`. Prometheus gauges read live state during scrape.

`CortexExporter` requires `SnapshotSupport`. Each push performs one `CacheX.write` at
`metrics:{space}:{serviceId}:snapshot`. The versioned JSON contains collection time, all families and all points.
Closing it only stops its own scheduler.

## Spring starter

`bus-starter` uses one `MetricsConfiguration` entry point with backend linkage isolated in nested configurations.

```yaml
bus:
  metrics:
    enabled: true
    provider: native
    jvm: true
    http: true
    endpoint:
      enabled: false
      path: /metricz
    startup:
      enabled: false
    cardinality:
      default-max: 100
      deny-list: [user_id, trace_id, request_id]
      rules:
        - tag: uri
          policy: first-n
          max: 1000
    host:
      enabled: true
      required: false
      convention: otel-1.44.0
      cache-ttl: 1s
      process:
        current-only: true
        state-counts: false
        state-counts-cache-ttl: 10s
    cortex:
      enabled: false
      interval: 15s
      space: default
      service-id: application
```

`provider` accepts `native`, `micrometer`, `opentelemetry`, `prometheus` or `auto`. Auto chooses Native when no
external candidate is available, chooses the sole external candidate, and fails when multiple external candidates
are available. Explicit Micrometer and OpenTelemetry selection requires exactly one corresponding runtime bean.
Explicit Prometheus selection reuses one registry bean or creates a provider-private registry when none exists;
merely adding Prometheus core to the classpath never makes it an automatic candidate.

The scrape endpoint is disabled by default and fails startup when explicitly enabled without scrape capability.
Optional host collection logs and disables itself when bus-health/JNA initialization fails; `host.required=true`
turns the same condition into a startup failure.
