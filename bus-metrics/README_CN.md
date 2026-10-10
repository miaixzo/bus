# Bus Metrics

`bus-metrics` 是 Bus 的后端无关指标组件，提供 Counter、Gauge、Timer、Histogram、EWMA Meter、基数控制、显式
SLO、LLM 指标、Host/JVM Binder、Prometheus 抓取以及带版本的 Cortex 快照。

组件要求 Java 21。Native 后端始终可用；Micrometer、OpenTelemetry、Prometheus、bus-health 和 Servlet 均保持为
Maven 可选依赖。

## Provider 模型

| Provider | 所需运行时对象 | 生命周期归属 |
|:--|:--|:--|
| `NativeProvider` | 无 | 拥有并关闭自身调度器 |
| `MicrometerProvider` | 唯一 `MeterRegistry` | 移除自身 Meter，不关闭 Registry |
| `OpenTelemetryProvider` | 唯一 `OpenTelemetry` API 对象 | 关闭自身 callback，不关闭 SDK |
| `PrometheusProvider` | 0 或 1 个 `PrometheusRegistry` | 注销自身 Collector，不 clear/close 外部 Registry |

四种后端使用同一套本地读取语义。空 Timer/Histogram 的 `count/total/max` 为 `0`，percentile 返回
`Double.NaN`。非法输入会在修改本地聚合或第三方后端之前失败。

统一 instrumentation scope 为 `org.miaixz.bus.metrics`。typed 注册完整保留名称、类型、数值类型、单位、描述和有序
属性 schema；不同逻辑 family 映射到同一最终导出名时会立即失败。

## 直接使用

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

`Metrics` 继续作为静态兼容门面。`Metrics.installProvider(provider, owner)` 返回可逆 `ProviderLease`；关闭 lease
只恢复此前门面状态，不关闭新旧 Provider。

## 速率与分布

每个 Provider 按名称和标签缓存 `Meter`，并仅用一个 daemon scheduler 每 5 秒推进全部 1/5/15 分钟 EWMA。
`RatePair` 固定使用 `<name>.total`、`<name>.successes`、`<name>.errors`；每次成功或失败只增加一次 `total`。
空窗口的错误率为 `0`，成功率为 `1`。

Native 分布聚合使用有界 T-Digest。Timer 的一分钟视图使用 60 个一秒桶，五分钟视图使用 60 个五秒桶，采用惰性
轮转，不需要 Timer 专用线程。

```java
double p95 = latency.percentile(0.95, TimeUnit.MILLISECONDS);
double rolling = latency.percentile(0.95, TimeUnit.MILLISECONDS, Timer.Window.ONE_MINUTE);
```

`NativeTimer.rotate1m()`、`rotate5m()` 作为显式清空滚动视图的兼容入口保留。
`BucketWindow`、`NativeRatePair`、`NativeLlmTimer`、`CardinalityViolation` 和
`org.miaixz.bus.metrics.observe.Test` 均继续保留。

## 基数控制

`CardinalityGuard.Scope` 属于单个 Provider。`firstN(n)` 将超限值映射为 `__overflow__`；`topN(n)` 保留有界稳定值
集合并将后续值映射为 `__other__`；`deny()` 删除标签。每次替换或拒绝都会增加一次 Scope 计数并发布一个
`CardinalityViolation`。Provider diagnostics 记录
`bus.metrics.resources.dropped{category="cardinality",reason="replaced|denied"}`。Listener 异常被隔离，Provider
关闭时注销 Listener。

## LLM 指标

```java
LlmSample sample = Metrics.llmTimer("ai.chat", "tenant", "public")
        .start("gpt", "openai", "chat");
sample.recordFirstToken();
sample.stop(inputTokens, outputTokens, finishReason);
```

基础标签不会丢失。`model`、`provider`、`operation`、`finish_reason`、`type`、`error_type` 是动态保留标签。
Token 不允许为负；first-token 只接受第一次写入；仅第一次终止 `stop/error` 会记录数据；未知价格模型不产生伪 cost。

## 显式 SLO

SLO 不会自动订阅指标。metric name 和 tags 只是兼容元数据，调用方必须显式提交每次观测。

```java
SloTracker slo = Metrics.slo()
        .trackLatency("checkout.latency", "http.server.requests", 300, 0.999)
        .onBudgetExhausted("checkout.latency", event -> alert(event));
slo.record("checkout.latency", durationMillis, failed);
```

错误预算使用 60 个有界时间桶。Availability 取 target 与 `1 - maxErrorRatio` 中更严格的目标。耗尽回调按边沿触发，
恢复后再次耗尽可以再次触发。

## Host、JVM 与导出

`JvmMetrics` 只采集 JVM runtime、线程、类加载、heap 和 GC。Host CPU、内存、paging、磁盘、文件系统、网络、进程和
容器 Binder 使用由 bus-health 支持的 `HealthMetricSource`。能力不支持时省略 family，不发布伪零。
`SystemMetrics` 只作为显式兼容 Binder 保留，Starter 不自动注册。

`PrometheusExporter` 只消费 `ScrapeSupport`，不判断 Provider 类型，也不拥有 Provider/Registry。content type 为
`text/plain; version=0.0.4; charset=utf-8`。Prometheus Gauge 在 scrape 时实时读取状态。

`CortexExporter` 要求 `SnapshotSupport`。每次 push 只调用一次 `CacheX.write`，key 为
`metrics:{space}:{serviceId}:snapshot`；带版本 JSON 包含采集时间、全部 family 与 point。关闭 Exporter 只停止自身线程。

## Spring Starter

`bus-starter` 只有一个 `MetricsConfiguration` 配置入口，各后端通过嵌套配置隔离链接。

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

`provider` 支持 `native`、`micrometer`、`opentelemetry`、`prometheus`、`auto`。自动模式在没有外部候选时选
Native，只有一个外部候选时选该候选，同时存在多个外部候选时明确失败。显式选择 Micrometer 或 OpenTelemetry 时必须
存在且仅存在一个对应运行时 Bean；显式选择 Prometheus 时复用唯一 Registry Bean，未提供时创建 Provider 私有 Registry。
仅将 Prometheus Core 加入类路径不会使其成为自动候选。

抓取端点默认关闭；显式启用但 Provider 无 scrape capability 时启动失败。可选 Host 初始化失败时记录日志并关闭 Host；
`host.required=true` 将相同情况提升为启动失败。
