# 🧠 Bus Cortex：统一注册与配置中心

<p align="center">
<strong>服务注册 · 配置中心 · 健康探测 · 命名空间隔离 · bus 生态联动</strong>
</p>

-----

## 📖 项目介绍

**Bus Cortex** 是 bus 框架的注册与配置中心模块。它提供统一的服务注册、配置管理、健康探测和基于命名空间的多租户隔离——所有数据通过单一 `CacheX` 存储抽象（Memory / Redis / JDBC）读写，无需额外基础设施。

- **统一注册表**：API / MCP / Prompt / Version 四类注册表，统一 `Registry<T>` 接口
- **服务+实例分离模型**：`ApiDefinition`（服务定义）+ `Instance`（运行时实例），参考 Nacos 设计
- **配置中心**：带版本号的发布、灰度路由、`@ConfigChange` 回调注解
- **健康探测**：可插拔 `Prober`（HTTP / TCP / MCP Ping / 进程 PID），服务端主动探测
- **命名空间隔离**：运行时动态解析命名空间（Token / Header / 上下文），`NamespaceGuard` 强制写隔离
- **安全防护**：HMAC-SHA256 Token + RBAC（ADMIN / PROVIDER / CONSUMER），限流，熔断
- **bus 生态联动**：VortexBridge 自动同步到 bus-vortex；内置 bus-metrics 埋点；bus-cache 是唯一存储依赖
- **三步接入**：一个依赖 + 一行配置 + 一个注解

-----

## ✨ 核心特性

### 🎯 统一注册表接口

四种注册类型，一套统一 API：

| 注册表 | 资产类型 | 说明 |
|:---|:---|:---|
| **ApiRegistry** | `ApiDefinition` + `Instance` | HTTP / gRPC / MCP / WebSocket / LLM API 服务 |
| **McpRegistry** | `McpAssets` | MCP 工具/服务器定义 |
| **PromptRegistry** | `PromptAssets` | Prompt 模板管理 |
| **VersionRegistry** | `VersionAssets` | 服务版本生命周期（ACTIVE / DEPRECATED / DISABLED） |

```java
// 类型安全的静态 Facade（与 bus-metrics Metrics 模式一致）
Cortex.apiRegistry().register(service, instance);
Cortex.mcpRegistry().register(mcpAssets);

List<ApiDefinition> services = Cortex.query(
    new Vector().setNamespace("production").setMethod("vortex.user.get"), ApiDefinition.class);
```

### ⚡ 服务与实例分离

```
ApiDefinition（服务定义，method+version 全局唯一）
    └── Instance（运行时实例，host:port，每个服务最多 1 个活动实例）
```

**唯一性约束**：同一 `namespace + method + version` 下，任意时刻只允许 **1 个**活动 `Instance`。通过 `SETNX`（Redis）/ `UNIQUE` 约束（JDBC）/ `synchronized`（Memory）原子保证。

相同 fingerprint 重复注册视为**幂等刷新 TTL**，不视为冲突。

### 🔧 配置中心

| 能力 | 实现 |
|:---|:---|
| **带版本号发布** | `ConfigPublisher` + `Sequence` 原子序列号（CacheX CAS，避免时钟回拨） |
| **版本回滚** | `DefaultConfig.rollback()` 从 `ConfigVersion` 快照还原（默认保留最近 10 个） |
| **灰度发布** | `GrayRouter` 匹配 `GrayRule`（IP_LIST / IP_RANGE / PERCENTAGE / HEADER） |
| **变更回调** | `@ConfigChange` 注解——纯 Java，无 Spring 依赖 |
| **推送+轮询双通路** | 配置变更：`ConfigPublisher.notify()` 直接推送；实例变更：`HealthProbeScheduler` 定时 scan+diff |

```java
// 发布配置
Cortex.publishConfig("app", "database.url", "jdbc:mysql://...");

// 订阅变更
Cortex.config().watch("app", "database.url", newValue -> {
    dataSource.setUrl(newValue);
});

// 灰度规则
GrayRule rule = new GrayRule();
rule.setType(GrayType.PERCENTAGE);
rule.setPercentage(10);
rule.setGrayContent("jdbc:mysql://canary/...");
```

### 🏥 健康探测

服务端主动探测，可插拔探测器：

| 探测器 | 协议 | 适用场景 |
|:---|:---|:---|
| `HttpProber` | HTTP GET/HEAD | 标准 Web 服务 |
| `TcpProber` | TCP 连接 | 非 HTTP 服务 |
| `McpPingProber` | MCP JSON-RPC ping/pong | MCP 服务器 |
| `ProcessProber` | 本地 PID 存活检测 | 同机进程 |
| `CompositeProber` | 聚合多个探测器 | 多协议服务 |

`HealthProbeScheduler` 内置线程池（默认并发度 = `min(32, instanceCount/10)`），对所有已注册实例并发探测。健康实例续期 TTL；不健康实例不续期，TTL 到期后由 CacheX 自动清理。

### 🛡️ 安全与防护

| 组件 | 功能 |
|:---|:---|
| `AccessTokenStore` | HMAC-SHA256 签发 Token，CacheX 支持主动吊销 |
| `AccessTokenResolver` | 两步校验：HMAC 重新验签（防伪造）+ CacheX 黑名单检查（支持吊销） |
| `AccessGuard` | RBAC 权限校验（ADMIN / PROVIDER / CONSUMER） |
| `NamespaceGuard` | 阻止跨 namespace 写操作 |
| `RateLimiter` | 令牌桶限流，按 namespace/method 维度（CacheX 计数） |
| `CircuitBreaker` | 状态机：CLOSED → OPEN → HALF_OPEN |
| `ParamValidator` | 入参校验（正则 `^[a-zA-Z0-9._-]{1,128}$`），防 CacheX key 注入 |

### 🌉 与 bus-vortex 联动（VortexBridge）

自动异步同步到 bus-vortex API 网关：

```
ApiRegistry.register()
    └── VortexBridge.onRegistered()
        └── ApiAssetsConverter.convert(ApiDefinition, Instance) → Assets
            └── asyncQueue.offer(SyncEvent{REGISTER, asset})
                └── SyncWorker POST → bus-vortex /_internal/registry/sync
```

- 有界队列（容量 10,000），bus-vortex 宕机时防止 OOM
- 指数退避重试，可配置最大重试次数
- 幂等推送：同一 API 产生相同 Assets ID，bus-vortex 端安全覆盖
- 支持全量同步接口，用于 bus-vortex 重启/重连恢复

### 📊 与 bus-metrics 联动

所有核心操作均通过 `Metrics` 静态门面埋点：

| 指标名 | 类型 | 埋点位置 |
|:---|:---|:---|
| `cortex.registry.register.total` | Counter | ApiRegistry.register |
| `cortex.registry.deregister.total` | Counter | ApiRegistry.deregister |
| `cortex.registry.instances.active` | Gauge | HealthProbeScheduler |
| `cortex.config.publish.total` | Counter | ConfigPublisher.publish |
| `cortex.health.check.total` | Counter | Prober 各实现 |
| `cortex.vortex.sync.total` | Counter | VortexBridge.SyncWorker |
| `cortex.security.token.issued` | Counter | AccessTokenStore.issue |
| `cortex.registry.query.duration` | Timer | AbstractRegistry.query |

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

### 第二步：一行配置

```yaml
bus:
  cortex:
    server-addr: 127.0.0.1:8766   # 唯一必填项
```

> **命名空间说明**：`namespace` 不作为应用侧固定 YAML 配置项。运行时由上层系统动态决定（Token 中的 namespace 声明 / 请求 Header `X-Namespace` / 管理后台上下文），未提供时回落到 `public`。

### 第三步：启用注解

```java
@SpringBootApplication
@EnableCortex
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

启用后自动完成：服务注册/注销、`@ConfigValue` 字段注入、配置变更回调。

-----

## 📝 使用示例

### 1. 编程式 API（静态门面）

```java
import org.miaixz.bus.cortex.Cortex;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.registry.api.ApiDefinition;
import org.miaixz.bus.cortex.registry.api.Instance;

// 注册服务 + 实例
ApiDefinition service = new ApiDefinition();
service.setNamespace("production");
service.setMethod("vortex.user.get");
service.setVersion("v1.0.0");
service.setPath("/v2/api");
service.setMode(1);  // 1=HTTP

Instance instance = new Instance();
instance.setHost("192.168.1.10");
instance.setPort(8080);
instance.setWeight(100);

Cortex.apiRegistry().register(service, instance);

// 查询 API 定义
List<ApiDefinition> results = Cortex.query(
    new Vector().setNamespace("production").setMethod("vortex.user.get"),
    ApiDefinition.class
);

// 注销
Cortex.deregister("production", "vortex.user.get:v1.0.0");
```

### 2. 配置管理

```java
// 发布配置
Cortex.publishConfig("database", "datasource.url", "jdbc:mysql://prod:3306/app");

// 读取配置（支持灰度）
String url = Cortex.config().get("database", "datasource.url", clientIp);

// 订阅变更
Cortex.config().watch("database", "datasource.url", newValue -> {
    log.info("配置变更: {}", newValue);
    dataSource.setUrl(newValue);
});

// 回滚到指定历史版本
Cortex.config().rollback("database", "datasource.url", 5L);
```

### 3. Spring 注解驱动

```java
@Service
public class OrderService {

    // 自动从配置中心注入，支持热更新
    @ConfigValue(group = "order", dataId = "max-retry")
    private int maxRetry;

    // 变更回调
    @ConfigChange(group = "order", dataId = "max-retry")
    public void onMaxRetryChanged(ConfigChangeEvent event) {
        log.info("max-retry 从 {} 变更为 {}", event.getOldValue(), event.getNewValue());
    }
}
```

### 4. 订阅实例变更

```java
// 订阅实例变化
String watchId = Cortex.apiRegistry().watch(
    new Vector().setNamespace("production").setMethod("vortex.user.get"),
    (added, removed, updated) -> {
        log.info("实例变更: 新增 {} 下线 {} 更新 {}",
            added.size(), removed.size(), updated.size());
    }
);

// 取消订阅
Cortex.apiRegistry().unwatch(watchId);
```

### 5. MCP 与 Prompt 注册

```java
// 注册 MCP 工具
McpAssets mcp = new McpAssets();
mcp.setNamespace("ai");
mcp.setToolName("code-search");
mcp.setTransport("stdio");
Cortex.mcpRegistry().register(mcp);

// 注册 Prompt 模板
PromptAssets prompt = new PromptAssets();
prompt.setNamespace("ai");
prompt.setTemplate("请总结以下内容：{{content}}");
Cortex.promptRegistry().register(prompt);
```

-----

## ⚙️ 完整配置参考

```yaml
bus:
  cache:
    type: memory                    # memory | redis | jdbc

  redis:                            # cache.type=redis 时必填
    host: 127.0.0.1
    port: 6379
    database: 0

  cortex:
    server-addr: bus-cortex:8766    # 必填；K8s 中配置 Service DNS 名

    vortex:
      enabled: false                # 启用 VortexBridge 同步
      sync-url: http://localhost:8080
      sync-interval: 30000          # 全量同步间隔（ms）
      max-retries: 3

    security:
      enabled: false
      hmac-secret: ${CORTEX_HMAC_SECRET}  # 生产环境必须用环境变量注入
      token-expire-seconds: 86400          # 生产建议 3600（1小时）

    health-check:
      interval-ms: 30000            # 探测间隔
      timeout-ms: 5000              # 单实例探测超时
      max-retries: 3

    audit:
      enabled: false
```

### 配置属性说明

| 属性 | 类型 | 默认值 | 说明 |
|:---|:---|:---|:---|
| `bus.cortex.server-addr` | String | — | **必填**，注册中心地址 |
| `bus.cortex.vortex.enabled` | boolean | `false` | 启用 bus-vortex 同步 |
| `bus.cortex.vortex.sync-url` | String | — | bus-vortex 同步接口地址 |
| `bus.cortex.vortex.sync-interval` | long | `30000` | 全量同步间隔（ms） |
| `bus.cortex.vortex.max-retries` | int | `3` | 最大重试次数 |
| `bus.cortex.security.enabled` | boolean | `false` | 启用 Token + RBAC |
| `bus.cortex.security.hmac-secret` | String | — | HMAC-SHA256 签名密钥 |
| `bus.cortex.security.token-expire-seconds` | long | `86400` | Token 有效期（生产建议 3600） |
| `bus.cortex.health-check.interval-ms` | long | `30000` | 健康探测间隔 |
| `bus.cortex.health-check.timeout-ms` | long | `5000` | 单实例探测超时 |
| `bus.cortex.health-check.max-retries` | int | `3` | 探测重试次数 |
| `bus.cortex.audit.enabled` | boolean | `false` | 启用操作审计日志 |

-----

## 🏗️ 整体架构

```
┌──────────────────────────────────────────────────────────────┐
│                        应用服务层                              │
│  Service A :8080    Service B :8081    Service C :8082        │
└───────────────────────────┬──────────────────────────────────┘
                            │ 注册/配置
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                   bus-cortex（注册表+配置中心）                   │
│  ApiRegistry / McpRegistry / PromptRegistry / VersionRegistry │
│  ConfigPublisher / ConfigWatcher / GrayRouter                 │
│         │ CacheX                    │ VortexBridge            │
│         ▼                           ▼                         │
│  ┌─────────────┐          ┌──────────────────┐               │
│  │  bus-cache  │          │   bus-vortex     │               │
│  │  Memory /   │          │  API 网关 代理    │               │
│  │  Redis /    │          └──────────────────┘               │
│  │  JDBC       │                                              │
│  └─────────────┘                                              │
│         │ 指标埋点                                             │
│  ┌─────────────┐                                              │
│  │ bus-metrics │ ← counter / timer / gauge                    │
│  └─────────────┘                                              │
│  bus-starter/cortex 提供 Spring Boot 自动配置                  │
└──────────────────────────────────────────────────────────────┘
```

### 包结构

```
org.miaixz.bus.cortex
├── [根包]         核心抽象（Cortex, Assets, Species, Vector, Registry, Config）
├── registry/      注册表实现
│   ├── api/       ApiDefinition + Instance + ApiRegistry + 探测器
│   ├── mcp/       McpAssets + McpRegistry + ProcessManager
│   ├── prompt/    PromptAssets + PromptRegistry
│   ├── version/   VersionAssets + VersionRegistry + VersionStatus
│   ├── label/     Label + Selector + MetadataRouter（标签路由）
│   ├── batch/     BatchOperation + BatchResult（批量操作）
│   └── depend/    DependencyGraph + ImpactAnalysis（依赖拓扑）
├── config/        配置中心（ConfigPublisher, ConfigWatcher, GrayRouter, DefaultConfig）
├── bridge/        bus-vortex 同步（VortexBridge, SyncEvent, ApiAssetsConverter）
├── guard/         安全防护（RateLimiter, CircuitBreaker, AccessTokenStore, AccessGuard, NamespaceGuard）
├── builtin/       默认实现（CompositeProber, DefaultPublisher, DefaultNotifier）
└── magic/         工具类（IdGenerator, Sequence, Fingerprint, AuditLogger, InstanceState）
```

-----

## ☸️ K8s 部署说明

### 单 Cortex + N 服务实例

```yaml
bus:
  cortex:
    server-addr: bus-cortex:8766   # K8s Service DNS 名
```

CacheX 后端：Memory / Redis / JDBC 均可。

### 多 Cortex Pod（HPA 扩容）

**必须使用 Redis 或 JDBC** 作为 CacheX 后端——Memory 模式无法在多 Pod 间共享唯一性状态。

| 存储 | 唯一性机制 | 生产可用 |
|:---|:---|:---|
| Memory | JVM synchronized/CAS | 仅限开发/测试 |
| JDBC | UNIQUE 约束 + upsert | 小规模生产 |
| Redis | `SETNX` / Lua CAS | **推荐** |

### Pod IP 与滚动发布

```yaml
# 通过 Downward API 注入 Pod IP
env:
  - name: POD_IP
    valueFrom:
      fieldRef:
        fieldPath: status.podIP

# 滚动发布策略（单活动实例约束）
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 0         # 禁止新旧 Pod 同时存在
    maxUnavailable: 1
```

TTL 建议：`healthCheck.intervalMs × 3`（默认 90s）。HealthProbeScheduler 为健康实例续期 TTL；不健康实例不续期，到期后由 CacheX 自动清理。

### PreStop Hook（防非优雅退出）

K8s 强杀 Pod（OOMKill / SIGKILL）不触发 `@PreDestroy`。建议配置 PreStop：

```yaml
lifecycle:
  preStop:
    exec:
      command: ["/bin/sh", "-c", "sleep 3"]
terminationGracePeriodSeconds: 30
```

三道防线：PreStop Hook → TTL 过期 → HealthProbeScheduler 主动探测清理。

-----

## 🔄 版本兼容性

| Bus Cortex 版本 | Spring Boot 版本 | JDK 版本 |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |

-----

## 🔗 相关文档

- [Bus Cache 文档](../bus-cache/README_CN.md)
- [Bus Metrics 文档](../bus-metrics/README_CN.md)
- [Bus Starter 指南](../bus-starter/README_CN.md)
- [设计文档](docs/design-v8.0.md)

-----

## 📄 许可证

[Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
