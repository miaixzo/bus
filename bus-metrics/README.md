# 📊 Bus Metrics: Production-Grade Metrics for the Bus Framework

<p align="center">
<strong>Cardinality Guard · EWMA Rates · SLO Tracking · LLM Observability · bus Ecosystem Integration</strong>
</p>

-----

## 📖 Project Introduction

**Bus Metrics** is the metrics module for the bus framework. It goes beyond simple counter/timer collection to provide cardinality explosion prevention, in-process EWMA rate calculation, SLA violation callbacks, SLO error budget tracking, and AI/LLM-native observability — capabilities that Micrometer and Dropwizard Metrics do not offer.

- **Cardinality Guard**: Prevents OOM caused by high-cardinality tags (userId, traceId, URI) — the most common Micrometer production incident
- **In-Process Rates**: `Meter` provides 1m/5m/15m EWMA rates (req/s) directly in the application — no PromQL needed
- **SLA Violation Callbacks**: `Timer.onViolation()` — the only Java metrics library with this API
- **SLO Tracking**: Compliance rate, error budget remaining, burn rate — all computable in-process
- **LLM Observability**: TTFT / ITL / token usage / cost — following OTel GenAI SIG 2025 conventions
- **Zero-Dependency Native**: `NativeProvider` with self-implemented T-Digest (P99 error < 1%), no third-party dependencies
- **bus Ecosystem Integration**: Pushes to bus-cortex for cluster aggregation; feeds bus-vortex for traffic-aware routing weights

-----

## ✨ Core Features

### 🛡️ CardinalityGuard — OOM Prevention

The most common Micrometer production incident: userId/traceId/URI misused as tags causes `ConcurrentHashMap` unbounded growth and OOM. `CardinalityGuard` intercepts all tag arrays before they reach the registry.

Three policies via sealed interface `CardinalityPolicy`:

| Policy | Behavior |
|:---|:---|
| `firstN(n)` | Accept the first N distinct values; overflow replaced with `__overflow__` |
| `topN(n)` | Keep the top-N most frequent values (Count-Min Sketch); low-frequency replaced with `__other__` |
| `deny()` | Completely strip this tag key |

YAML-driven, zero code changes:

```yaml
bus:
  metrics:
    cardinality:
      default-max: 100
      deny-list: [user_id, trace_id, request_id]
      rules:
        - tag: uri
          policy: first-n
          max: 1000
        - tag: status
          policy: first-n
          max: 30
```

Self-monitoring: violations increment `cardinality.violations` counter and emit a throttled WARN log (at most once per key per 60 seconds).

### ⚡ Meter — In-Process EWMA Rates

Micrometer only exposes cumulative count/sum; req/s must be computed externally via PromQL. `Meter` embeds EWMA rate calculation (same algorithm as Linux kernel load average and Dropwizard Metrics), enabling in-process adaptive rate limiting and circuit breaking.

```java
Meter qps = Metrics.meter("http.requests", "method", "GET");
qps.increment();

// In-process rate — no Prometheus needed
if (qps.oneMinuteRate() > 10_000) {
    throttle();
}

// Success/error rate pair
RatePair rp = Metrics.ratePair("payment.calls", "provider", "stripe");
rp.recordSuccess();
if (rp.errorRate() > 0.05) {
    CircuitBreaker.open("stripe");  // In-process circuit breaking
}
```

EWMA parameters (5-second tick interval):

| Window | Alpha |
|:---|:---|
| 1 minute | `exp(-5/60)` = 0.9200 |
| 5 minutes | `exp(-5/300)` = 0.9835 |
| 15 minutes | `exp(-5/900)` = 0.9945 |

### 🔔 Timer.onViolation — SLA Violation Callbacks

The only Java metrics library with this API. Micrometer's `.sla(Duration...)` only records bucket counts with no callback mechanism; alerting requires external Alertmanager.

```java
Metrics.timer("checkout.api")
    .onViolation(0.99, 300, MILLIS, 100,
        e -> cortexClient.alert("checkout-sla-breach",
             e.actualNanos() / 1e6 + "ms > 300ms"))
    .onViolation(0.50, 100, MILLIS, 500,
        e -> Logger.warn("checkout median degraded"));
```

`ViolationEvent` fields: `metricName`, `tags`, `percentile`, `actualNanos`, `thresholdNanos`, `violatedAt`.

### 📈 SloTracker — SLO Error Budget

```
SLI (technical metric): p99 < 300ms, error rate < 0.1%
     ↓ expressed as
SLO (target): 99.9% of requests satisfy the SLI
     ↓ measured as
Error Budget: 0.1% of requests may fail per month (~43 min downtime)
     ↓ exhaustion triggers
SLA breach / business alert
```

```java
Metrics.slo()
    .trackLatency("checkout.api", 300, 0.999)
    .onBudgetExhausted("checkout.api.latency",
        e -> pagerDuty.trigger("SLO error budget exhausted, remaining: "
             + e.budgetRemaining()));

// Query in-process
double compliance = Metrics.slo().compliance("checkout.api.latency");
double budget     = Metrics.slo().errorBudgetRemaining("checkout.api.latency");
double burnRate   = Metrics.slo().burnRate("checkout.api.latency");
```

Prometheus auto-export:

```
slo_compliance_ratio{slo="checkout.latency",target="0.999"}  0.9987
slo_error_budget_remaining{slo="checkout.latency"}           0.13
```

### 🤖 LlmTimer — AI/LLM Native Observability

Following OTel GenAI SIG 2025 conventions. No other Java metrics library has native support for this.

```java
LlmSample s = Metrics.llmTimer("ai.chat")
    .start("claude-opus-4-6", "anthropic", "chat");

stream.onFirstChunk(s::recordFirstToken);   // Records TTFT
stream.onComplete(resp -> s.stop(
    resp.usage().inputTokens(),
    resp.usage().outputTokens(),
    resp.stopReason()
));
stream.onError(s::error);
```

One `stop()` call automatically records 6 metrics:

| Metric | Type | Tags | Description |
|:---|:---|:---|:---|
| `llm.call.duration` | Timer | model, provider, operation, finish_reason | End-to-end latency |
| `llm.call.ttft` | Timer | model, provider | Time to first token |
| `llm.call.itl` | Timer | model, provider | Inter-token latency |
| `llm.tokens` | Counter | model, provider, type=input\|output | Token usage |
| `llm.cost` | Counter | model, provider | Estimated cost (USD) |
| `llm.errors` | Counter | model, provider, error_type | Error count |

### 🎯 T-Digest Accurate Tail Percentiles

`NativeProvider` self-implements T-Digest (~200 lines, zero dependencies):

| | Fixed 12 Buckets | T-Digest |
|:---|:---|:---|
| P99 error | ~20% | < 1% |
| P99.9 error | > 50% | < 0.5% |
| Memory | 96 bytes | ~300 bytes |
| Cross-instance merge | Not supported | Supported |
| Dependencies | None | None (self-implemented) |

-----

## 🚀 Quick Start

### Step 1: Add Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Step 2: Enable

```java
@SpringBootApplication
@EnableMetrics
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

On startup: JVM metrics, system metrics, and HTTP request metrics are auto-registered. The `/metricz` endpoint is available immediately.

### Step 3: Use

```java
// Counter
Metrics.counter("order.created", "region", "cn").increment();

// Meter with EWMA rates
Meter qps = Metrics.meter("http.requests", "method", "GET");
qps.increment();
double rps = qps.oneMinuteRate();

// Timer with SLA callback
Metrics.timer("payment.process")
    .onViolation(0.99, 500, MILLIS, 100,
        e -> alert("P99 exceeded 500ms"));

// Gauge
Metrics.gauge("queue.size", queue, Queue::size);

// Histogram
Metrics.histogram("response.size").record(bytes);

// LLM timer
LlmSample s = Metrics.llmTimer("ai.chat")
    .start("claude-opus-4-6", "anthropic", "chat");
```

-----

## ⚙️ Feature Details

### Built-in Metrics (RED Method)

#### JVM Metrics (`JvmMetrics`)

| Metric | Type | Tags |
|:---|:---|:---|
| `jvm.memory.used` | Gauge | area=heap\|nonheap |
| `jvm.memory.max` | Gauge | area=heap\|nonheap |
| `jvm.gc.pause` | Timer | gc=gcName |
| `jvm.threads.live` | Gauge | — |
| `jvm.threads.peak` | Gauge | — |

#### System Metrics (`SystemMetrics`)

| Metric | Type |
|:---|:---|
| `system.cpu.load` | Gauge |
| `process.cpu.load` | Gauge |
| `process.uptime` | Gauge |

#### HTTP Metrics (`HttpMetrics`)

Servlet Filter that auto-instruments all HTTP requests. URI template normalization (`/user/123` → `/user/{id}`) is available via `HttpMetricsInterceptor` in bus-starter (Spring MVC).

| Metric | Type | Tags |
|:---|:---|:---|
| `http.server.requests` | Timer | method, uri, status, exception |
| `http.server.requests.rate` | Meter | method, uri, status |

#### Cache Metrics (`CacheMetrics`)

Conditional — only active when bus-cache is on the classpath. Wraps a `CacheX` instance with hit/miss/write tracking:

```java
CacheX<String, User> cache = CacheMetrics.instrument(rawCache, "user-cache");
```

### Prometheus Export

`PrometheusExporter` renders Prometheus text format 0.0.4. The `/metricz` endpoint (via `MetricsEndpoint` in bus-starter) serves the scrape payload directly.

```
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{method="GET",uri="/api/order",le="0.005"} 80000
http_server_requests_seconds_bucket{method="GET",uri="/api/order",le="+Inf"}  98432
http_server_requests_seconds_sum   492.16
http_server_requests_seconds_count 98432

# TYPE llm_call_duration_seconds histogram
llm_call_duration_seconds_bucket{model="claude-opus-4-6",provider="anthropic",le="0.5"} 1200

# TYPE slo_compliance_ratio gauge
slo_compliance_ratio{slo="checkout.latency",target="0.999"} 0.9987
```

-----

## 🔗 bus Ecosystem Integration

### bus-cortex Integration (CortexExporter)

Periodically pushes local metric snapshots to bus-cortex via CacheX. Key pattern: `metrics:{namespace}:{serviceId}:{metricName}`. Cortex aggregates across instances for cluster-level `/metricz`.

```yaml
bus:
  metrics:
    cortex:
      enabled: true
      interval-seconds: 15
```

### bus-vortex Integration (VortexMetricsFeed)

Feeds per-route P95 latency and error rate to bus-vortex for traffic-aware dynamic weight adjustment:

```
VortexHandler.handle()
    └─> PerRouteMetrics.record(assetId, duration, success)
        └─> DynamicWeightAdjuster (every 10s)
            └─> effectiveWeight = entry.weight * (baseline_p95 / current_p95)
```

Slow instances are automatically down-weighted rather than abruptly removed.

### bus-tempus Integration (TempusMetrics)

Auto-instruments scheduled job execution:

```java
TempusMetrics.recordExecution("daily-report", durationMs, success);
// Records: tempus.job timer + tempus.executions meter
```

-----

## 🔧 Full Configuration Reference

```yaml
bus:
  metrics:
    provider: native              # native (default) or micrometer
    jvm: true                     # Enable JVM metrics
    system: true                  # Enable system metrics
    health: true                  # Enable bus-health integration (auto when on classpath)
    http: true                    # Enable HTTP request metrics
    endpoint: true                # Enable /metricz endpoint
    path: /metricz                # Endpoint path

    cardinality:
      default-max: 100            # Default max distinct values for unregistered keys
      deny-list:                  # Always-denied high-cardinality fields
        - user_id
        - trace_id
        - request_id
      rules:
        - tag: uri
          policy: first-n
          max: 1000
        - tag: status
          policy: first-n
          max: 30

    slo:
      - name: checkout.latency
        metric: http.server.requests
        type: latency
        threshold-ms: 300
        percentile: 0.99
        target: 0.999
        window-minutes: 30

    rate-window:
      enabled: true
      tick-interval-seconds: 5    # EWMA tick interval

    cortex:
      enabled: false
      interval-seconds: 15
      server-addr: ""
```

### Configuration Properties

| Property | Type | Default | Description |
|:---|:---|:---|:---|
| `bus.metrics.provider` | String | `native` | Backend: `native` or `micrometer` |
| `bus.metrics.jvm` | boolean | `true` | Enable JVM metrics |
| `bus.metrics.system` | boolean | `true` | Enable system metrics |
| `bus.metrics.http` | boolean | `true` | Enable HTTP metrics |
| `bus.metrics.endpoint` | boolean | `true` | Enable `/metricz` endpoint |
| `bus.metrics.path` | String | `/metricz` | Endpoint path |
| `bus.metrics.cardinality.default-max` | int | `100` | Default cardinality limit |
| `bus.metrics.cardinality.deny-list` | List | — | Always-denied tag keys |
| `bus.metrics.cortex.enabled` | boolean | `false` | Enable CortexExporter |
| `bus.metrics.cortex.interval-seconds` | int | `15` | Push interval |

-----

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  User Code / @EnableMetrics              │
└───────────────────────────┬─────────────────────────────┘
                            │ single entry point
                            ▼
┌─────────────────────────────────────────────────────────┐
│                        Metrics                           │
│   counter / meter / gauge / timer / histogram / llmTimer │
└──────┬────────────┬──────────────┬──────────────────────┘
       │            │              │
       ▼            ▼              ▼
CardinalityGuard  Meter(EWMA)  SloTracker + LlmTimer
       │            │              │
       └────────────┴──────────────┘
                    │
              Provider (SPI)
             /              \
    NativeProvider      MicrometerProvider
   (T-Digest, zero-dep)  (delegates to Micrometer)
          │
   ┌──────┼──────────────┐
   ▼      ▼              ▼
PrometheusExporter  CortexExporter  OpenTelemetryProvider
/metricz pull       push bus-cortex  OTLP push (optional)
```

-----

## 🆚 Comparison with Alternatives

| Capability | Micrometer | Dropwizard | bus-metrics |
|:---|:---|:---|:---|
| Multi-backend export | ✅ 30+ | ✅ | ✅ Prometheus/OTLP/Cortex |
| Cardinality guard (OOM prevention) | ❌ | ❌ | ✅ |
| In-process rates (req/s) | ❌ | ✅ Meter | ✅ |
| SLA violation callbacks | ❌ | ❌ | ✅ |
| SLO tracking + error budget | ❌ | ❌ | ✅ |
| T-Digest accurate percentiles | ✅ HDR Histogram | ✅ external dep | ✅ zero-dep self-impl |
| Cross-instance percentile merge | ❌ client percentile | ❌ | ✅ histogram buckets |
| LLM/AI native metrics | ❌ | ❌ | ✅ TTFT/ITL/Token/Cost |
| bus ecosystem integration | ❌ | ❌ | ✅ cortex/vortex/tempus |
| Zero-dependency runtime | ❌ needs registry impl | ❌ | ✅ NativeProvider |

-----

## 🔄 Version Compatibility

| Bus Metrics Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |

-----

## 💡 Use Cases

- **Microservice Observability**: RED method (Rate/Errors/Duration) auto-instrumented for HTTP, JVM, and system
- **In-Process Adaptive Rate Limiting**: `Meter.oneMinuteRate()` drives throttling decisions without external systems
- **SLO Engineering**: Define SLOs in YAML, get compliance/error-budget/burn-rate in-process and in Prometheus
- **AI Application Monitoring**: Full LLM call observability — TTFT, ITL, token cost — following OTel GenAI SIG 2025
- **Cluster Metrics Aggregation**: CortexExporter pushes snapshots to bus-cortex for multi-instance aggregation
- **Traffic-Aware Routing**: VortexMetricsFeed feeds real-time P95/error-rate to bus-vortex for dynamic weight adjustment

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
