# 📊 Bus Metrics: 生产级指标框架

<p align="center">
<strong>基数守卫 · EWMA 速率 · SLO 追踪 · LLM 可观测性 · bus 生态联动</strong>
</p>

-----

## 📖 项目介绍

**Bus Metrics** 是 bus 框架的指标模块。它不止于简单的计数器/计时器采集，还提供基数爆炸防护、应用内 EWMA 速率计算、SLA 违约回调、SLO 错误预算追踪，以及 AI/LLM 原生可观测性——这些都是 Micrometer 和 Dropwizard Metrics 所不具备的能力。

- **基数守卫**：防止 userId/traceId/URI 等高基数 Tag 导致 OOM——Micrometer 生产事故的最常见原因
- **应用内速率**：`Meter` 直接在应用内提供 1m/5m/15m EWMA 速率（req/s），无需 PromQL
- **SLA 违约回调**：`Timer.onViolation()`——目前唯一具备此 API 的 Java 指标库
- **SLO 追踪**：合规率、剩余错误预算、燃尽速率，全部可在应用内计算
- **LLM 可观测性**：TTFT / ITL / Token 消耗 / 费用，遵循 OTel GenAI SIG 2025 规范
- **零依赖自研**：`NativeProvider` 自实现 T-Digest（P99 误差 < 1%），无第三方依赖
- **bus 生态联动**：推送到 bus-cortex 实现集群聚合；为 bus-vortex 提供流量感知路由权重

-----

## ✨ 核心特性

### 🛡️ CardinalityGuard — OOM 防护

Micrometer 最常见的生产事故：userId/traceId/URI 误用为 Tag 导致 `ConcurrentHashMap` 无限增长、OOM。`CardinalityGuard` 在所有 Tag 数组进入注册表之前统一拦截。

三种策略，通过 sealed interface `CardinalityPolicy` 实现：

| 策略 | 行为 |
|:---|:---|
| `firstN(n)` | 接受前 N 个不同值；超出后替换为 `__overflow__` |
| `topN(n)` | 保留出现频率最高的 N 个值（Count-Min Sketch 近似）；低频值替换为 `__other__` |
| `deny()` | 完全禁止此 Tag key，直接丢弃 |

YAML 配置驱动，代码零改动：

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

自监控：违规时触发 `cardinality.violations` 计数器自增，并以 WARN 级别输出日志（每个 key 每 60 秒最多一次，避免日志淹没）。

### ⚡ Meter — 应用内 EWMA 速率

Micrometer 只暴露累计 count/sum，req/s 必须靠外部 PromQL 计算。`Meter` 内嵌 EWMA 速率计算（算法与 Linux kernel load average 和 Dropwizard Metrics 完全一致），支持应用内自适应限流和熔断决策。

```java
Meter qps = Metrics.meter("http.requests", "method", "GET");
qps.increment();

// 应用内直接拿到速率，无需 Prometheus
if (qps.oneMinuteRate() > 10_000) {
    throttle();
}

// 成功/错误速率对
RatePair rp = Metrics.ratePair("payment.calls", "provider", "stripe");
rp.recordSuccess();
if (rp.errorRate() > 0.05) {
    CircuitBreaker.open("stripe");  // 应用内熔断
}
```

EWMA 参数（每 5 秒 tick 一次）：

| 窗口 | Alpha |
|:---|:---|
| 1 分钟 | `exp(-5/60)` = 0.9200 |
| 5 分钟 | `exp(-5/300)` = 0.9835 |
| 15 分钟 | `exp(-5/900)` = 0.9945 |

### 🔔 Timer.onViolation — SLA 违约回调

目前唯一具备此 API 的 Java 指标库。Micrometer 的 `.sla(Duration...)` 仅记录桶数，无任何回调机制，告警强依赖外部 Alertmanager。

```java
Metrics.timer("checkout.api")
    .onViolation(0.99, 300, MILLIS, 100,
        e -> cortexClient.alert("checkout-sla-breach",
             e.actualNanos() / 1e6 + "ms > 300ms"))
    .onViolation(0.50, 100, MILLIS, 500,
        e -> Logger.warn("checkout 中位数劣化"));
```

`ViolationEvent` 字段：`metricName`、`tags`、`percentile`、`actualNanos`、`thresholdNanos`、`violatedAt`。

### 📈 SloTracker — SLO 错误预算

```
SLI（技术指标）：p99 < 300ms，error rate < 0.1%
     ↓ 转化为
SLO（目标）：99.9% 的请求满足 SLI
     ↓ 表现为
错误预算：每月允许 0.1% 的请求失败（约 43 分钟停机）
     ↓ 超出则触发
SLA 违约 / 业务告警
```

```java
Metrics.slo()
    .trackLatency("checkout.api", 300, 0.999)
    .onBudgetExhausted("checkout.api.latency",
        e -> pagerDuty.trigger("SLO 错误预算耗尽，剩余: "
             + e.budgetRemaining()));

// 应用内查询
double compliance = Metrics.slo().compliance("checkout.api.latency");
double budget     = Metrics.slo().errorBudgetRemaining("checkout.api.latency");
double burnRate   = Metrics.slo().burnRate("checkout.api.latency");
```

Prometheus 自动导出：

```
slo_compliance_ratio{slo="checkout.latency",target="0.999"}  0.9987
slo_error_budget_remaining{slo="checkout.latency"}           0.13
```

### 🤖 LlmTimer — AI/LLM 原生可观测性

遵循 OTel GenAI SIG 2025 规范。目前无任何其他 Java 指标库对此有原生支持。

```java
LlmSample s = Metrics.llmTimer("ai.chat")
    .start("claude-opus-4-6", "anthropic", "chat");

stream.onFirstChunk(s::recordFirstToken);   // 记录 TTFT
stream.onComplete(resp -> s.stop(
    resp.usage().inputTokens(),
    resp.usage().outputTokens(),
    resp.stopReason()
));
stream.onError(s::error);
```

一次 `stop()` 自动记录 6 个指标：

| 指标名 | 类型 | 标签 | 说明 |
|:---|:---|:---|:---|
| `llm.call.duration` | Timer | model, provider, operation, finish_reason | 全程延迟 |
| `llm.call.ttft` | Timer | model, provider | 首 Token 延迟 |
| `llm.call.itl` | Timer | model, provider | Token 间延迟 |
| `llm.tokens` | Counter | model, provider, type=input\|output | Token 消耗 |
| `llm.cost` | Counter | model, provider | 估算费用（USD） |
| `llm.errors` | Counter | model, provider, error_type | 错误计数 |

### 🎯 T-Digest 精确尾部百分位

`NativeProvider` 自实现 T-Digest（约 200 行，零依赖）：

| | 固定 12 桶 | T-Digest |
|:---|:---|:---|
| P99 误差 | ~20% | < 1% |
| P99.9 误差 | > 50% | < 0.5% |
| 内存 | 96 bytes | ~300 bytes |
| 跨实例合并 | 不支持 | 支持 |
| 依赖 | 无 | 无（自实现） |

-----

## 🚀 快速开始

### 第一步：添加依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 第二步：启用

```java
@SpringBootApplication
@EnableMetrics
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

启动后自动完成：JVM 指标、系统指标、HTTP 请求指标自动注册，`/metricz` 端点立即可用。

### 第三步：使用

```java
// 计数器
Metrics.counter("order.created", "region", "cn").increment();

// 带 EWMA 速率的 Meter
Meter qps = Metrics.meter("http.requests", "method", "GET");
qps.increment();
double rps = qps.oneMinuteRate();

// 计时器 + SLA 回调
Metrics.timer("payment.process")
    .onViolation(0.99, 500, MILLIS, 100,
        e -> alert("P99 超过 500ms"));

// Gauge
Metrics.gauge("queue.size", queue, Queue::size);

// 直方图
Metrics.histogram("response.size").record(bytes);

// LLM 计时器
LlmSample s = Metrics.llmTimer("ai.chat")
    .start("claude-opus-4-6", "anthropic", "chat");
```

-----

## ⚙️ 功能详解

### 内置指标（RED 方法）

#### JVM 指标（`JvmMetrics`）

| 指标 | 类型 | 标签 |
|:---|:---|:---|
| `jvm.memory.used` | Gauge | area=heap\|nonheap |
| `jvm.memory.max` | Gauge | area=heap\|nonheap |
| `jvm.gc.pause` | Timer | gc=gcName |
| `jvm.threads.live` | Gauge | — |
| `jvm.threads.peak` | Gauge | — |

#### 系统指标（`SystemMetrics`）

| 指标 | 类型 |
|:---|:---|
| `system.cpu.load` | Gauge |
| `process.cpu.load` | Gauge |
| `process.uptime` | Gauge |

#### HTTP 指标（`HttpMetrics`）

Servlet Filter，自动埋点所有 HTTP 请求。URI 模板化（`/user/123` → `/user/{id}`）通过 bus-starter 中的 `HttpMetricsInterceptor` 实现（Spring MVC）。

| 指标 | 类型 | 标签 |
|:---|:---|:---|
| `http.server.requests` | Timer | method, uri, status, exception |
| `http.server.requests.rate` | Meter | method, uri, status |

#### 缓存指标（`CacheMetrics`）

条件激活——仅在 classpath 存在 bus-cache 时生效。包装 `CacheX` 实例，自动追踪命中/未命中/写入：

```java
CacheX<String, User> cache = CacheMetrics.instrument(rawCache, "user-cache");
```

### Prometheus 导出

`PrometheusExporter` 输出标准 Prometheus text 0.0.4 格式。bus-starter 中的 `MetricsEndpoint` 提供 `/metricz` 端点直接响应 scrape 请求。

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

## 🔗 bus 生态联动

### 与 bus-cortex 联动（CortexExporter）

定期将本实例指标快照推送到 bus-cortex（CacheX key：`metrics:{namespace}:{serviceId}:{metricName}`）。Cortex 汇聚所有实例数据，提供集群级 `/metricz`。

```yaml
bus:
  metrics:
    cortex:
      enabled: true
      interval-seconds: 15
```

### 与 bus-vortex 联动（VortexMetricsFeed）

向 bus-vortex 提供每路由的 P95 延迟和错误率，驱动流量感知动态权重调整：

```
VortexHandler.handle()
    └─> PerRouteMetrics.record(assetId, duration, success)
        └─> DynamicWeightAdjuster（每 10 秒）
            └─> effectiveWeight = entry.weight * (baseline_p95 / current_p95)
```

延迟高的实例自动降权，优雅降级而非直接摘除。

### 与 bus-tempus 联动（TempusMetrics）

自动埋点定时任务执行：

```java
TempusMetrics.recordExecution("daily-report", durationMs, success);
// 记录：tempus.job 计时器 + tempus.executions 速率计
```

-----

## 🔧 完整配置参考

```yaml
bus:
  metrics:
    provider: native              # native（默认）或 micrometer
    jvm: true                     # 启用 JVM 指标
    system: true                  # 启用系统指标
    health: true                  # 启用 bus-health 联动（classpath 有时自动启用）
    http: true                    # 启用 HTTP 请求指标
    endpoint: true                # 启用 /metricz 端点
    path: /metricz                # 端点路径

    cardinality:
      default-max: 100            # 未注册 key 的默认基数上限
      deny-list:                  # 永远禁止的高基数字段
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
      tick-interval-seconds: 5    # EWMA tick 间隔

    cortex:
      enabled: false
      interval-seconds: 15
      server-addr: ""
```

### 配置属性说明

| 属性 | 类型 | 默认值 | 说明 |
|:---|:---|:---|:---|
| `bus.metrics.provider` | String | `native` | 后端实现：`native` 或 `micrometer` |
| `bus.metrics.jvm` | boolean | `true` | 启用 JVM 指标 |
| `bus.metrics.system` | boolean | `true` | 启用系统指标 |
| `bus.metrics.http` | boolean | `true` | 启用 HTTP 指标 |
| `bus.metrics.endpoint` | boolean | `true` | 启用 `/metricz` 端点 |
| `bus.metrics.path` | String | `/metricz` | 端点路径 |
| `bus.metrics.cardinality.default-max` | int | `100` | 默认基数上限 |
| `bus.metrics.cardinality.deny-list` | List | — | 永远禁止的 Tag key |
| `bus.metrics.cortex.enabled` | boolean | `false` | 启用 CortexExporter |
| `bus.metrics.cortex.interval-seconds` | int | `15` | 推送间隔 |

-----

## 🏗️ 整体架构

```
┌─────────────────────────────────────────────────────────┐
│               用户代码 / @EnableMetrics                   │
└───────────────────────────┬─────────────────────────────┘
                            │ 唯一入口
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
   （T-Digest，零依赖）   （委托 Micrometer）
          │
   ┌──────┼──────────────┐
   ▼      ▼              ▼
PrometheusExporter  CortexExporter  OpenTelemetryProvider
/metricz pull       推送 bus-cortex  OTLP push（可选）
```

-----

## 🆚 与竞品对比

| 能力 | Micrometer | Dropwizard | bus-metrics |
|:---|:---|:---|:---|
| 多后端适配 | ✅ 30+ | ✅ | ✅ Prometheus/OTLP/Cortex |
| 基数守卫（防 OOM） | ❌ | ❌ | ✅ |
| 应用内速率（req/s） | ❌ | ✅ Meter | ✅ |
| SLA 违约回调 | ❌ | ❌ | ✅ |
| SLO 追踪 + 错误预算 | ❌ | ❌ | ✅ |
| T-Digest 精确百分位 | ✅ HDR Histogram | ✅ 外部依赖 | ✅ 零依赖自实现 |
| 跨实例百分位合并 | ❌（client percentile） | ❌ | ✅（histogram bucket） |
| LLM/AI 专用指标 | ❌ | ❌ | ✅ TTFT/ITL/Token/Cost |
| bus 生态联动 | ❌ | ❌ | ✅ cortex/vortex/tempus |
| 零依赖运行 | ❌ 需 registry impl | ❌ | ✅ NativeProvider |

-----

## 🔄 版本兼容性

| Bus Metrics 版本 | Spring Boot 版本 | JDK 版本 |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |

-----

## 💡 使用场景

- **微服务可观测性**：HTTP、JVM、系统指标自动埋点，RED 方法开箱即用
- **应用内自适应限流**：`Meter.oneMinuteRate()` 驱动限流决策，无需外部系统
- **SLO 工程**：YAML 定义 SLO，应用内和 Prometheus 同时获得合规率/错误预算/燃尽速率
- **AI 应用监控**：完整 LLM 调用可观测性——TTFT、ITL、Token 费用，遵循 OTel GenAI SIG 2025
- **集群指标聚合**：CortexExporter 推送快照到 bus-cortex，实现多实例数据汇聚
- **流量感知路由**：VortexMetricsFeed 向 bus-vortex 提供实时 P95/错误率，驱动动态权重调整

-----

## 🤝 贡献

欢迎提交 Pull Request！
