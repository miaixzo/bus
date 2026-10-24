# Bus Cortex

Cortex 提供服务注册与发现，以及按应用投递已发布配置的能力。Registry 和 Setting 分别承担服务治理与配置发布职责，共享实体和基础设施，不相互代替业务流程。

## 职责边界

| 范围 | 职责 |
| --- | --- |
| Registry | 服务定义、路由关联、实例发现与健康状态 |
| Item / ItemEditor / ItemService | 当前配置定义、敏感保护、管理读取与编辑并发校验 |
| Reference | 应用关联、别名、覆盖规则、固定已发布 Revision |
| Rollout / Approval | 发布选择、审批材料冻结与工作流状态 |
| Revision / Release | 成功发布的不可变快照、发布记录及发布事务 |
| Delivery / Watch | 已发布内容读取、实例会话校验、订阅与投递证据 |
| Starter | 条件装配、依赖接线与生命周期绑定 |

保存 Item 不生成版本或修改历史。Reference 的 `revision_id` 仅用于固定成功发布的快照；Item 的 `edition` 用于编辑并发控制。Rollout 的草稿属于审批工作流。

`ItemCodec`、`ReferenceCodec`、`Plan` 分别解析配置内容、关联规则和审批材料；`ReleaseCodec` 编解码并验证已发布图，协议标识为 `services-release-v3`。

## Spring Boot 接入

添加与项目其他 Bus 模块相同版本的 `org.miaixz:bus-starter` 依赖。
使用 `org.miaixz.bus.starter.annotation.EnableCortex`，或配置 `bus.cortex.enabled=true` 启用装配。

```yaml
bus:
  cortex:
    enabled: true
    max-watches-per-tenant: 1000
    watch-expire: 24h
    cache-expire: 1h
    watch:
      logging-enabled: false
    bridge:
      mode: pull-only
      url: ""
      max-retries: 3
      source: ""
    guard:
      enabled: true
    audit:
      enabled: false
    release:
      enabled: false
```

## 配置项

以下属性均位于 `bus.cortex` 下；缓存后端选项通过现有 `cache` 属性绑定。

| 属性 | 默认值 | 作用 |
| --- | --- | --- |
| `enabled` | `false` | 启用 Cortex 装配 |
| `max-watches-per-tenant` | `1000` | 每租户订阅上限，必须大于零 |
| `watch-expire` | `24h` | 订阅过期时间，必须为正 |
| `cache-expire` | `1h` | 本地 Cortex 缓存过期时间，必须为正 |
| `cache` | 可选 | Bus 缓存后端选项 |
| `watch.logging-enabled` | `false` | 注册订阅日志监听器 |
| `bridge.mode` | `pull-only` | `push+pull` 时装配 VortexBridge |
| `bridge.url` | 空 | `push+pull` 模式下必须非空 |
| `bridge.max-retries` | `3` | 桥接重试上限，必须大于零 |
| `bridge.source` | 空 | 桥接来源标记 |
| `guard.enabled` | `true` | 装配 TokenGuard 配置和策略 |
| `guard.token` | TokenGuardConfig 默认值 | Token 传输预校验选项 |
| `audit.enabled` | `false` | 装配审计记录器 |
| `release.enabled` | `false` | 存在 ReleaseStore 时装配只读发布 Registry |

## 宿主接线与事务

Starter 根据宿主提供的 Store 和 Enforcer 条件装配配置服务。当前定义读写需要 `ItemStore`、`ReferenceStore`、`SettingStore` 等；发布与审批需要 `RolloutStore`、`ApprovalStore`、`PublicationStore`、`ReleaseStore` 和 `RevisionStore`；运行时读取还需要 `WatchStore` 和 `SettingEnforcer`。具体组合以各 Bean 的 `ConditionalOnBean` 为准。

宿主实现负责租户隔离、权威实例读取和会话校验、授权、CAS 以及原子事务提交。`SettingStore` 原子保存当前定义和关联，`RolloutStore.submit` 原子校验冻结读集，`PublicationStore` 提交成功发布的快照及发布状态。

默认 `NoOpSecretCodec` 拒绝敏感内容，宿主需要提供真实保护实现；不会静默降级为明文。TokenGuard 只做凭据存在性及传输预校验，认证、权限与许可证检查仍由宿主执行。

Registry Bean 可以直接注入。静态 Cortex 门面需要绑定 Runtime；Starter 支持在没有 Curator 时创建 Registry-only Runtime；此时配置门面调用会明确报错。RuntimeHandle 在上下文关闭时释放绑定。

## 当前 API 示例

以下方法片段的 Registry、Curator 和请求参数均由宿主提供；请求的租户和应用身份必须来自可信上下文。

```java
import java.util.List;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Registry;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.registry.api.ApiAssets;

List<ApiAssets> services(Registry<ApiAssets> registry) {
    return registry.query(Vector.builder().app_id("orders").offset(0).limit(100).build());
}

List<Instance> instances(Registry<ApiAssets> registry) {
    return registry.queryInstances(null, "orders", "orders.read", "v1");
}
```

API 服务路由是全局范围；MCP 和 Prompt 操作必须提供明确的租户。共享进程统一使用根包的 `Instance`，通过 `app_id` 标识应用归属；服务关联由 Registry 维护，运行实例通过 `checksum` 标识。

```java
import org.miaixz.bus.cortex.Curator;
import org.miaixz.bus.cortex.setting.item.ItemQuery;

String published(Curator curator, ItemQuery trustedQuery) {
    return curator.get(trustedQuery);
}

String subscribe(Curator curator, ItemQuery trustedQuery) {
    return curator.watch(trustedQuery, value -> System.out.println("Published configuration changed"));
}

void unsubscribe(Curator curator, String watchId) {
    curator.unwatch(watchId);
}
```

## 发布链路

1. 用 ItemService 准备当前配置修改，通过 SettingService 联合保存定义与 Reference。
2. 用 RolloutService 创建发布选择；提交时校验当前读集，冻结审批材料。
3. 用 ApprovalService 记录审批决定，已提交材料不得任意修改。
4. 用 ReleasePublisher 执行批准后的发布；只有成功发布才产生 Revision / Release。
5. Curator 读取正式或匹配灰度规则的已发布内容，不回读可编辑 Item 或实时 Reference。

灰度选择属于发布链路；固定引用必须指向已发布 Revision。回滚也通过已批准的发布工作流执行。

Rollout 使用继承的 `status` 保存工作流状态：DRAFT=0、REVIEWING=1、APPROVED=2、REJECTED=3、PUBLISHING=4、FAILED=5、PUBLISHED=6。状态转移规则由 `Rollout.State` 保持。

Watch 的所有事件类型共享租户内序号，混合订阅保持递增。Outbox 实现必须在领取租约之前按 domain/resourceType 过滤；无过滤领取通过接口默认方法调用同一个原子操作。

## 包结构

```text
org.miaixz.bus.cortex
├── Cortex / Registry / Curator / Nature / Assets / Instance
├── registry/    api, mcp, prompt, routing, queries and batch operations
├── setting/     item, curator, reference, rollout, approval, revision, delivery, watch, secret
├── release/     Release, ReleaseCodec, ReleasePublisher, publication stores and read-only registry
├── bridge/      Vortex integration
├── guard/       authorization pre-validation
├── health/      health probers
├── builtin/     key generation, selectors and batch models
└── magic/       identity, event, watch and runtime infrastructure
```

## 凭据管理

现有 `setting.secret` 支持直接调用阿里云 KMS、AWS Secrets Manager、Azure Key Vault、Vault KV v2、腾讯云 SSM 与华为云 CSMS，提供显式授权、版本读取、有界内存缓存与刷新，无需厂商 SDK。

运行时入口为具体类 `SecretManager`，连接通过 `register(connection, provider)` 注册。
授权策略使用 `SecretManager.AccessPolicy`，能力使用 `SecretProvider.Capability`，
凭据来源使用 `CloudCredentials.Source`。`SecretReference.version()` 为 null 表示当前版本；
原配置 JSON 中的 `selector.mode/version` 与 `expectedType` 字段保持不变。
`SecretMaterial` 直接提供实际版本、获取时间、过期时间及连接 generation；
`describe()` 返回不可变能力集合。payload 类型仅作为配置标签，不再拆分完整值缓存。

缓存直接使用 Bus `MemoryCache`。注入的缓存必须为空、仅供此管理器使用，由管理器关闭；移除监听在替换、淘汰、过期和关闭时擦除缓存值。容量由 `MemoryCache` 配置，超过 8192 字符的值正常返回但不入缓存。

读取的生命周期保护覆盖缓存和非缓存模式；关闭会等待已进入的读取完成并阻止其继续返回密钥。读取失败后的缓存清理为尽力执行，不等待忙碌资源，也不覆盖原始异常。每次 HTTP 尝试将最多十秒的 deadline 同时传给凭据来源和 HTTP 执行，包装后的原生超时同样立即失败。

```java
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.cortex.setting.secret.SecretManager;
import org.miaixz.bus.cortex.setting.secret.SecretMaterial;

var cache = new MemoryCache<String, SecretMaterial>(256, 60_000);
try (var manager = new SecretManager(accessPolicy, cache)) {
    manager.register(connection, provider);
    // Resolve using an authenticated SecretContext.
}
```

Secret 直接使用 Bus `ForbiddenException` 表示访问被拒绝，其他失败使用 `CryptoException`。对外异常仅包含固定的脱敏文本，不携带云端错误消息、凭据、原始 cause 或自定义错误码；调用方不要解析异常消息。云适配层根据 HTTP 状态和平台原生错误字段判断重试：限流或临时服务故障（HTTP 429/500/502/503，以及阿里云、AWS、腾讯云已识别的瞬态错误）最多尝试三次。可识别的传输 I/O 故障允许重试；超时、权限拒绝、资源不存在、成功响应格式错误和凭据来源失败立即返回。每次尝试重新获取并关闭凭据。

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
