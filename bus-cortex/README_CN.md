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
| `guard.enabled` | `true` | 启用可选访问检查及 TokenGuard 预检 |
| `guard.token` | TokenGuardConfig 默认值 | Token 传输预校验选项 |
| `audit.enabled` | `false` | 装配审计记录器 |
| `release.enabled` | `false` | 存在 ReleaseStore 时装配只读发布 Registry |

## 宿主接线与事务

Starter 根据宿主提供的 Store 条件装配配置服务。当前定义读写需要 `ItemStore`、`ReferenceStore`、`SettingStore` 等；发布与审批需要 `RolloutStore`、`ApprovalStore`、`PublicationStore`、`ReleaseStore` 和 `RevisionStore`；运行时读取还需要 `WatchStore` 和 `SettingProvider`。具体组合以各 Bean 的 `ConditionalOnBean` 为准。

网关已完成访问控制时，可设置 `bus.cortex.guard.enabled=false`，跳过 Registry 和 Setting 的可选访问检查，包括宿主提供的 Guard。租户匹配、edition、审批状态、冻结摘要、发布回执和敏感保护仍然执行。开启时由宿主 `GuardStrategy` 提供权限决定；Cortex 不认证用户或凭据。普通读取无需操作人和请求编号；幂等写入需要请求编号，审批留痕还需要操作人。

宿主实现负责租户数据范围、当前实例与会话一致性、CAS 和原子事务。`SettingProvider` 只提供调用坐标和当前实例数据，不执行身份认证；审批策略有效性由 `ApprovalStore` 检查。`SettingStore` 原子保存当前定义及关系，`RolloutStore.submit` 原子校验冻结读集，`PublicationStore` 原子提交正式发布。

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

`Instance` 仅使用继承的整数 `status`：DELETED=-1、DISABLED=0、UP=1、DOWN=2、UNKNOWN=3、STARTING=4、MAINTENANCE=5。`modified` 是继承的普通修改时间，单位为毫秒。`health` JSON 文本统一保存 `scheme`、`path`、`time`（最近探测时间）、`origin`、`session`、`beat`、`lease`，不再保留对应的顶层字段。会话过期与实例是否可达分别判断；注册边界补齐 UNKNOWN，探测不会覆盖禁用、删除及维护状态。

`health`、`labels`、`metadata` 均保存 JSON 对象文本。labels 成员必须是字符串，metadata 可包含嵌套值；通过 `labelView()`、`metadataView()` 解析，不持久化 Map，不保留旧字段别名。程序 `version` 不参与 host/port 校验码计算。

健康信息由独立共享类 `org.miaixz.bus.cortex.health.HealthInfo` 表达，通过 `HealthInfo.parse(String)` 和 `toJson()` 统一完成严格解析、校验和序列化，仍以 `@Lob String health` 持久化。`healthView()` 返回独立、已校验的完整快照；调用方只修改负责的属性，再通过 `health(HealthInfo)` 校验并序列化完整对象，null 表示清空成员。共享类只描述七个既有属性，不承担 ORM 映射或执行职责，不保留健康 Map、字符串取值方法或补丁接口。探测器维护 time、origin，宿主管理 session、beat、lease；客户端上报的检查配置不会覆盖服务端观察值。建立或替换会话时清空旧探测观察，并保留本次提供的检查配置。Watch 和投递证据保留自己的 session 坐标，与 health.session 比较。Lattice 使用已有 edition 保护健康信息写入；健康变化事件只比较 status 和 health.origin，不直接比较整段 JSON。

探测按进程、会话及生效的协议、主机、端口和 HTTP 路径分组；等价默认配置共用检查，不同目标分别探测。结果通过 `RegistryStore.updateHealth` 更新现有观察，不调用普通注册。宿主原子校验未变化的服务定义和运行时行，提交后才更新缓存和发送事件；过期、停用、注销或替换的注册拒绝旧结果。`ApiAssets` 不再管理或注入心跳与租期默认值，正常保存元数据时清除其旧 `beat`、`lease` 键并保留其他扩展。

纯缓存探测要求后端支持条件替换。Starter 默认 Caffeine、其 MemoryCache 回退及单节点 Redis 已支持该操作；Hybrid 将原子比较交给主缓存，协调本地镜像变更，并在完成时根据主缓存当前值修复镜像。镜像键只负责枚举候选项，读取和扫描返回值始终来自主缓存。完整原子缓存能力标记保持原义，不支持的后端不降级为先读后写。

探测写回在一次后端联合条件替换中比较捕获的服务定义和实例值。API 运行时缓存分区包含已有 verb 坐标；精确服务采样先保留各关联快照，再按有效目标分组，公共进程查询仍按进程身份去重。持久化运行时查询不再回填缓存，注册和探测的提交后回调只失效相应运行时投影。共享 Store 与类型适配 Store 使用同一提交、回滚边界，单条关联写回失败不跳过同组其他关联。Lattice 在宿主上下文刷新后启动调度并在关闭时释放；仅使用 Starter 的客户端不自动启动探测。




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

## 配置项属性

`Item.Property` 使用不可变 `Map<String, JsonValue>` 保存完整扁平属性集合，由 `ItemCodec` 统一解析和序列化，不在存储正文中增加 Java 对象包装层：

```json
{"server.port":{"value":9090,"default":8080,"desc":"端口","required":true,"unit":"tcp","minimum":1}}
```

`value`、`default`、`desc` 和布尔型 `required` 有明确语义。其他属性保留原始 JSON 类型，不混入运行时业务值，也不自动成为校验规则；额外业务约束继续使用现有 Schema。字段缺失与显式 JSON null 保持区别。

`ValidationLevel.REQUIRED` 在处理敏感编辑意图后检查实际值，并执行完整校验。必填实际值不得缺失、为 null 或空白字符串；false、0 有效，默认值不能代替实际填写。注册使用 FULL，保留必填声明但不要求填入实际值。

动态属性沿用敏感存储、遮罩和不可变发布快照；`desc`、`required` 保持可见。扩展属性复用 SecretAction，使用独立定义片段坐标：配置项 `/password` 的 `unit` 对应 `#/~1password/unit`，与 `/password/value`、`/password/default` 区分。改名、恢复和导入保留完整属性；DELETE 显式删除属性，KEEP 保留原属性。

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
