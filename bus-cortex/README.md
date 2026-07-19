# 🧠 Bus Cortex: Unified Registry & Configuration Center

<p align="center">
<strong>Service Registry · Config Center · Health Probing · Namespace Isolation · bus Ecosystem Integration</strong>
</p>

-----

## 📖 Project Introduction

**Bus Cortex** is the registry and configuration center for the bus framework. It provides unified service registration, configuration management, health probing, and namespace-based multi-tenant isolation — all backed by a single `CacheX` storage abstraction (Memory / Redis / JDBC) with zero extra infrastructure.

- **Unified Registry**: API / MCP / Prompt / Version — four registries behind one `Registry<T>` interface
- **Service + Instance Model**: `ApiAssets` (definition) + `Instance` (runtime), following Nacos-style separation
- **Configuration Center**: Versioned publish, gray release routing, `@ConfigChange` callback annotations
- **Health Probing**: Pluggable `Prober` (HTTP / TCP / MCP Ping / Process PID), server-side active probing
- **Namespace Isolation**: Dynamic namespace resolution (Token / Header / context), `NamespaceGuard` enforced write isolation
- **Security**: HMAC-SHA256 Token + RBAC (ADMIN / PROVIDER / CONSUMER), rate limiting, circuit breaking
- **bus Ecosystem**: VortexBridge auto-syncs to bus-vortex; bus-metrics instrumentation built-in; bus-cache as the sole storage dependency
- **Three-Step Onboarding**: One dependency + one config line + one annotation

-----

## ✨ Core Features

### 🎯 Unified Registry Interface

Four registry types, one consistent API:

| Registry | Asset Type | Description |
|:---|:---|:---|
| **ApiRegistry** | `ApiAssets` + `Instance` | HTTP / gRPC / MCP / WebSocket / LLM API services |
| **McpRegistry** | `McpAssets` | MCP tool/server definitions |
| **PromptRegistry** | `PromptAssets` | Prompt template management |
| **VersionRegistry** | `VersionAssets` | Service version lifecycle (ACTIVE / DEPRECATED / DISABLED) |

```java
// Type-safe static facade (same pattern as bus-metrics Metrics)
Cortex.apiRegistry().register(service, instance);
Cortex.mcpRegistry().register(mcpAssets);

List<ApiAssets> services = Cortex.query(
    Vector.newBuilder().namespace_id("production").method("vortex.user.get").build(), ApiAssets.class);
```

### ⚡ Service + Instance Separation

```
ApiAssets (definition, canonical identity = namespace + type + method + version[:verb])
    └── Instance (runtime, unique by namespace + app_id + method + version + fingerprint)
```

**Instance Identity**: Multiple runtime instances may coexist for the same API definition. Runtime uniqueness is scoped by `namespace + app_id + method + version + fingerprint`.

Same-fingerprint re-registration is treated as an **idempotent TTL refresh**, not a conflict.

**Gateway Route Semantics**: When synchronized to bus-vortex, `ApiAssets.key` remains the lightweight public alias
`method:version:verbCode`, for example `dp.license.get:1.0:1`. Runtime lookup candidates are generated separately by
`Keying` in this order:

1. `namespace:type:app_id:method:version:verb`
2. `namespace:type:method:version:verb`
3. `namespace:app_id:method:version:verb`
4. `namespace:method:version:verb`
5. `type:app_id:method:version:verb`
6. `type:method:version:verb`
7. `app_id:method:version:verb`
8. `method:version:verb`

`method` / `version` / `verb` are required runtime dimensions; `namespace`, `type`, and `app_id` participate only
when present.

`Keying` is now a generic key-strategy interface. Registry/runtime routing uses `Keying<Keying.RegistrySpec>` with
the built-in `RegistryGenerator`, while the setting domain uses `Keying<Keying.SettingSpec>` with the built-in
`SettingGenerator`.

### 🔧 Configuration Center

| Capability | Implementation |
|:---|:---|
| **Versioned Publish** | `ConfigPublisher` with `Sequence` atomic version numbers (CacheX CAS, no clock drift) |
| **Version Rollback** | `DefaultConfig.rollback()` restores from `ConfigVersion` snapshots (retains last 10) |
| **Gray Release** | `GrayRouter` matches `GrayRule` (IP_LIST / IP_RANGE / PERCENTAGE / HEADER) |
| **Watch Callback** | `@ConfigChange` annotation — pure Java, no Spring dependency |
| **Push + Poll** | Config changes: push via `ConfigPublisher.notify()`; Instance changes: poll via `HealthProbeScheduler` scan+diff |

```java
// Publish
Cortex.publishConfig("app", "database.url", "jdbc:mysql://...");

// Watch
Cortex.config().watch("app", "database.url", newValue -> {
    dataSource.setUrl(newValue);
});

// Gray release
GrayRule rule = new GrayRule();
rule.setType(GrayType.PERCENTAGE);
rule.setPercentage(10);
rule.setGrayContent("jdbc:mysql://canary/...");
```

### 🏥 Health Probing

Server-side active probing with pluggable probers:

| Prober | Protocol | Use Case |
|:---|:---|:---|
| `HttpProber` | HTTP GET/HEAD | Standard web services |
| `TcpProber` | TCP connect | Non-HTTP services |
| `McpPingProber` | MCP JSON-RPC ping/pong | MCP servers |
| `ProcessProber` | Local PID check | Same-host processes |
| `CompositeProber` | Aggregates multiple probers | Multi-protocol services |

`HealthProbeScheduler` runs a scheduled thread pool (default parallelism = `min(32, instanceCount/10)`), probing all registered instances. Healthy instances get TTL refreshed; unhealthy instances expire naturally via CacheX TTL.

### 🛡️ Security & Guard

| Component | Function |
|:---|:---|
| `AccessTokenStore` | HMAC-SHA256 token issuance with CacheX-backed revocation |
| `AccessTokenResolver` | Two-step validation: HMAC verify (anti-forgery) + CacheX blacklist check (revocation) |
| `AccessGuard` | RBAC enforcement (ADMIN / PROVIDER / CONSUMER) |
| `NamespaceGuard` | Cross-namespace write prevention |
| `RateLimiter` | Token bucket rate limiting per namespace/method (CacheX counters) |
| `CircuitBreaker` | State machine: CLOSED → OPEN → HALF_OPEN |
| `ParamValidator` | Input validation (regex `^[a-zA-Z0-9._-]{1,128}$`) preventing CacheX key injection |

### 🌉 bus-vortex Integration (VortexBridge)

Automatic async sync to bus-vortex API gateway:

```
ApiRegistry.register()
    └── VortexBridge.onRegistered()
        └── ApiAssetsConverter.convert(ApiAssets, Instance) → Assets
            └── asyncQueue.offer(SyncEvent{REGISTER, asset})
                └── SyncWorker POST → bus-vortex /registry/push
```

- Bounded queue (capacity 10,000) prevents OOM when bus-vortex is down
- Exponential backoff retry with configurable max retries
- Idempotent: same API produces same Assets ID, bus-vortex overwrites safely
- Full sync endpoint for bus-vortex restart/reconnect recovery

### 📊 bus-metrics Integration

All operations instrumented via `Metrics` static facade:

| Metric | Type | Location |
|:---|:---|:---|
| `cortex.registry.register.total` | Counter | ApiRegistry.register |
| `cortex.registry.deregister.total` | Counter | ApiRegistry.deregister |
| `cortex.registry.instances.active` | Gauge | HealthProbeScheduler |
| `cortex.config.publish.total` | Counter | ConfigPublisher.publish |
| `cortex.health.check.total` | Counter | Prober implementations |
| `cortex.vortex.sync.total` | Counter | VortexBridge.SyncWorker |
| `cortex.security.token.issued` | Counter | AccessTokenStore.issue |
| `cortex.registry.query.duration` | Timer | AbstractRegistry.query |

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

### Step 2: Configure

```yaml
bus:
  cortex:
    server-addr: 127.0.0.1:8766   # The only required field
```

> **Namespace**: Not a fixed YAML config. Resolved dynamically at runtime from Token / request Header (`X-Namespace`) / management context. Falls back to `public` if unspecified.

### Step 3: Enable

```java
@SpringBootApplication
@EnableCortex
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

On startup: auto-registration, `@ConfigValue` field injection, and configuration change callbacks are all activated automatically.

-----

## 📝 Usage Examples

### 1. Programmatic API (Static Facade)

```java
import org.miaixz.bus.cortex.Cortex;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.cortex.registry.api.Instance;

// Register a service with instance
ApiAssets service = new ApiAssets();
service.setNamespace_id("production");
service.setApp_id("order-service");
service.setMethod("vortex.user.get");
service.setVersion("v1.0.0");
service.setPath("/v2/api");
service.setProtocol(1);  // HTTP
service.setVerb(1);      // GET

Instance instance = new Instance();
instance.setNamespace_id("production");
instance.setApp_id("order-service");
instance.setMethod("vortex.user.get");
instance.setVersion("v1.0.0");
instance.setHost("192.168.1.10");
instance.setPort(8080);
instance.setWeight(100);

Cortex.apiRegistry().register(service, instance);

// Query API definitions
List<ApiAssets> results = Cortex.query(
    Vector.newBuilder().namespace_id("production").method("vortex.user.get").build(),
    ApiAssets.class
);

// Deregister
Cortex.deregister("production", "vortex.user.get:v1.0.0");
```

### 2. Configuration Management

```java
// Publish configuration
Cortex.publishConfig("database", "datasource.url", "jdbc:mysql://prod:3306/app");

// Read configuration (with gray release support)
String url = Cortex.config().get("database", "datasource.url", clientIp);

// Watch for changes
Cortex.config().watch("database", "datasource.url", newValue -> {
    log.info("Config changed: {}", newValue);
    dataSource.setUrl(newValue);
});

// Rollback to a previous version
Cortex.config().rollback("database", "datasource.url", 5L);
```

### 3. Spring Annotation-Driven

```java
@Service
public class OrderService {

    // Auto-injected from config center, hot-reloadable
    @ConfigValue(group = "order", dataId = "max-retry")
    private int maxRetry;

    // Change callback
    @ConfigChange(group = "order", dataId = "max-retry")
    public void onMaxRetryChanged(ConfigChangeEvent event) {
        log.info("max-retry changed from {} to {}", event.getOldValue(), event.getNewValue());
    }
}
```

### 4. Watch Service Changes

```java
// Watch for instance changes
String watchId = Cortex.apiRegistry().watch(
    Vector.newBuilder().namespace_id("production").method("vortex.user.get").build(),
    (added, removed, updated) -> {
        log.info("Instances changed: +{} -{} ~{}", added.size(), removed.size(), updated.size());
    }
);

// Unsubscribe
Cortex.apiRegistry().unwatch(watchId);
```

### 5. MCP & Prompt Registry

```java
// Register MCP tool
McpAssets mcp = new McpAssets();
mcp.setNamespace_id("ai");
mcp.setToolName("code-search");
mcp.setTransport("stdio");
Cortex.mcpRegistry().register(mcp);

// Register prompt template
PromptAssets prompt = new PromptAssets();
prompt.setNamespace_id("ai");
prompt.setTemplate("Summarize the following: {{content}}");
Cortex.promptRegistry().register(prompt);
```

-----

## ⚙️ Full Configuration Reference

```yaml
bus:
  cache:
    type: memory                    # memory | redis | jdbc

  redis:                            # Required when cache.type=redis
    host: 127.0.0.1
    port: 6379
    database: 0

  cortex:
    server-addr: bus-cortex:8766    # Required — K8s: use Service DNS name

    vortex:
      enabled: false                # Enable VortexBridge sync
      sync-url: http://localhost:8080
      sync-interval: 30000          # ms
      max-retries: 3

    security:
      enabled: false
      hmac-secret: ${CORTEX_HMAC_SECRET}  # Must use env var in production
      token-expire-seconds: 86400

    health-check:
      interval-ms: 30000            # Probing interval
      timeout-ms: 5000              # Per-instance probe timeout
      max-retries: 3

    audit:
      enabled: false
```

### Configuration Properties

| Property | Type | Default | Description |
|:---|:---|:---|:---|
| `bus.cortex.server-addr` | String | — | **Required.** Registry center address |
| `bus.cortex.vortex.enabled` | boolean | `false` | Enable bus-vortex sync |
| `bus.cortex.vortex.sync-url` | String | — | bus-vortex sync endpoint URL |
| `bus.cortex.vortex.sync-interval` | long | `30000` | Full sync interval (ms) |
| `bus.cortex.vortex.max-retries` | int | `3` | Max retry attempts |
| `bus.cortex.security.enabled` | boolean | `false` | Enable Token + RBAC |
| `bus.cortex.security.hmac-secret` | String | — | HMAC-SHA256 signing key |
| `bus.cortex.security.token-expire-seconds` | long | `86400` | Token TTL (recommend 3600 for production) |
| `bus.cortex.health-check.interval-ms` | long | `30000` | Health probe interval |
| `bus.cortex.health-check.timeout-ms` | long | `5000` | Probe timeout per instance |
| `bus.cortex.health-check.max-retries` | int | `3` | Probe retry count |
| `bus.cortex.audit.enabled` | boolean | `false` | Enable operation audit logging |

-----

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        Application Layer                      │
│  Service A :8080    Service B :8081    Service C :8082        │
└───────────────────────────┬──────────────────────────────────┘
                            │ register / config
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                   bus-cortex (Registry + Config)              │
│  ApiRegistry / McpRegistry / PromptRegistry / VersionRegistry │
│  ConfigPublisher / ConfigWatcher / GrayRouter                 │
│         │ CacheX                    │ VortexBridge            │
│         ▼                           ▼                         │
│  ┌─────────────┐          ┌──────────────────┐               │
│  │  bus-cache  │          │   bus-vortex     │               │
│  │  Memory /   │          │  API Gateway     │               │
│  │  Redis /    │          │  Traffic Proxy   │               │
│  │  JDBC       │          └──────────────────┘               │
│  └─────────────┘                                              │
│         │ Metrics                                             │
│  ┌─────────────┐                                              │
│  │ bus-metrics │ ← counter / timer / gauge instrumentation    │
│  └─────────────┘                                              │
│  bus-starter/cortex provides Spring Boot auto-configuration   │
└──────────────────────────────────────────────────────────────┘
```

### Package Structure

```
org.miaixz.bus.cortex
├── [root]         Core abstractions (Cortex, Assets, Species, Vector, Registry, Config)
├── registry/      Registry implementations
│   ├── api/       ApiAssets + Instance + ApiRegistry + probers
│   ├── mcp/       McpAssets + McpRegistry + ProcessManager
│   ├── prompt/    PromptAssets + PromptRegistry
│   ├── version/   VersionAssets + VersionRegistry + VersionStatus
│   ├── label/     Label + Selector + MetadataRouter
│   ├── batch/     BatchOperation + BatchResult
│   └── depend/    DependencyGraph + ImpactAnalysis
├── config/        Configuration center (ConfigPublisher, ConfigWatcher, GrayRouter, DefaultConfig)
├── bridge/        bus-vortex sync (VortexBridge, SyncEvent, ApiAssetsConverter)
├── guard/         Security & protection (RateLimiter, CircuitBreaker, AccessTokenStore, AccessGuard, NamespaceGuard)
├── builtin/       Default implementations (CompositeProber, DefaultPublisher, DefaultNotifier)
└── magic/         Utilities (IdGenerator, Sequence, Fingerprint, AuditLogger, InstanceState)
```

-----

## ☸️ Kubernetes Deployment

### Single Cortex + N Services

```yaml
bus:
  cortex:
    server-addr: bus-cortex:8766   # K8s Service DNS
```

CacheX backend: Memory / Redis / JDBC all work.

### Multiple Cortex Pods (HPA)

**Must use Redis or JDBC** as CacheX backend — Memory cannot share uniqueness state across pods.

| Storage | Uniqueness Mechanism | Production Ready |
|:---|:---|:---|
| Memory | JVM synchronized/CAS | Development only |
| JDBC | UNIQUE constraint + upsert | Small scale |
| Redis | `SETNX` / Lua CAS | **Recommended** |

### Pod IP & Rolling Update

```yaml
# Inject Pod IP via Downward API
env:
  - name: POD_IP
    valueFrom:
      fieldRef:
        fieldPath: status.podIP

# Rolling update strategy (single active instance constraint)
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 0
    maxUnavailable: 1
```

TTL recommended: `healthCheck.intervalMs × 3` (default 90s). HealthProbeScheduler refreshes TTL for healthy instances; unhealthy instances expire naturally.

-----

## 🔄 Version Compatibility

| Bus Cortex Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |

-----

## 🔗 Related Documentation

- [Bus Cache Documentation](../bus-cache/README.md)
- [Bus Metrics Documentation](../bus-metrics/README.md)
- [Bus Starter Guide](../bus-starter/README.md)
- [Design Document (Chinese)](docs/design-v8.0.md)

-----

## 📄 License

[Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
