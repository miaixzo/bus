# Bus Auth

[English](README.md) | 简体中文

Bus Auth 是面向标准认证协议、第三方身份平台和协议中立身份资源访问的模块化认证框架。所有可配置实现都统一作为
`Source` 暴露，作为 `SourceModule` 装配，由 `SourceDriver` 编译，并通过唯一的 `Dispatcher` 链路调用。

Bus Auth 提供认证协议实现和连接能力。接入项目继续负责 HTTP 端点、管理 CRUD、持久化、凭据、账户绑定、业务授权、
组织架构同步、任务调度、事务和用户会话。

## 环境与依赖

Bus 8.x 以 Java 21 字节码为目标，项目 CI 使用 JDK 25 构建。应用还必须在使用依赖 JSON 的功能前，通过
`bus.json.provider` 或 `JsonFactory.install(...)` 选择一个 `JsonKit` 实现。

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-auth</artifactId>
    <version>8.x.x</version>
</dependency>
```

JPMS 应用需要声明：

```java
requires bus.auth;
```

Protocol 和 Vendor 实现统一通过 `SourceConnector` 服务发现。Bus Auth 同时发布 `module-info.java` Provider 和
`META-INF/services` 元数据，因此模块路径和类路径使用相同的扩展模型。

## 领域模型

持久化管理层级固定为：

```text
Library
└── Provider
    └── Source
```

- `Library` 是顶层认证管理分组。
- `Provider` 聚合项目拥有的 Source 配置，不表示 OAuth、OpenID Connect 或 SAML 服务端角色。
- `Source` 是唯一的持久化路由和运行单元。
- `Blueprint` 是项目加载的完整 Library/Provider/Source 期望配置。
- `Roster` 是当前已经提交的只读 Blueprint 快照。
- `Scheme` 描述一个实现及其强类型 `Scheme.Options`。
- `SourceDescriptor` 向管理端暴露一个可选实现，但不会创建运行 Worker。
- `Capability` 定义强类型请求/响应操作，`Dispatcher` 是唯一运行时执行边界。
- `Outcome` 区分成功、预期拒绝和运行故障，普通认证失败不会变成异常完成的 Stage。
- `Realm` 定义协议中立的上游资源与关系，不是持久化实体，也不会形成第二套 Runtime。

## 架构

Protocol 和 Vendor 都是 Source 的特化分支：

```text
org.miaixz.bus.auth.source
├── protocol   LDAP、OAuth 2.x、OpenID Connect、RADIUS、SAML、SCIM
└── vendor     具名第三方平台、Variant 和可选 Realm Adapter
```

两个分支在 Runtime 装配前完成统一：

```text
                         SourceConnector SPI
                                  │
                      SourceDiscovery -> SourceSuite
                                  │
               ┌──────────────────┴──────────────────┐
ProtocolConnector -> ProtocolRegistry -> ProtocolModule
VendorConnector   -> VendorRegistry   -> VendorModule
               └──────────────────┬──────────────────┘
                                  │
                           SourceAggregate
                                  │
RuntimeBuilder -> SourceLookup -> SourceDriver -> SourceWorker -> Dispatcher
                                  │
                         RuntimeManager / Roster
```

`Registry` 和 `Registry.Connector` 是构建期注册契约。`registry` 包在注册冻结后负责 Blueprint 校验和 Roster 投影，
它不是另一套 Connector Registry。

`ProtocolModule` 和 `VendorModule` 是 `SourceAggregate` 持有的两个内置 `SourceModule` 实现。`SourceLookup` 冻结 Driver、
Descriptor 和反向路由索引。Runtime 编译时为每个 Source 创建最小权限的 `ScopedSourceServices`，Driver 不会获得完整的
`RuntimeServices` 容器。

## 内置实现

标准协议分支包括：

- LDAP Client 和 Server；
- OAuth 2.x Client 和 Authorization Server；
- OpenID Connect Client 和 Provider；
- RADIUS Server；
- SAML Client 和 Identity Provider；
- SCIM Server。

已注册的 Vendor 平台包括：

Afdian、Alipay、Aliyun、Amazon、Apple、Baidu、Coding、DingTalk、Douyin、Eleme、Facebook、Feishu、Figma、Gitee、
GitHub、GitLab、Google、Huawei、JD、Kujiale、LINE、LinkedIn、Meituan、Mi、Microsoft、Okta、OSChina、Pinterest、
Proginn、QQ、RedNote、Slack、Stack Overflow、Taobao、Teambition、Toutiao、Twitter、VK、WeChat、Weibo 和 Ximalaya。

当前为 Aliyun、DingTalk、Feishu、Figma、GitHub、GitLab、Google、Microsoft、Okta、Slack 和企业微信提供 Realm Adapter。
每个精确 Variant 自行声明可用 Capability；仅凭平台名称不能推断其支持 Realm、增量变更或单资源查询。

## Runtime 装配

项目必须显式提供全部外部基础设施：

- 为每个选中协议提供不可放宽规则的 `Policies`；
- 调用方拥有的 `Executor`；
- 原子 `CacheX<String, Object>` 后端和部署隔离标识；
- 只包含所选 SourceDriver 所需项目端口的 `WorkerSet`；
- 返回完整期望 Blueprint 修订的 `BlueprintLoader`；
- 可选的 `RosterListener`。

```java
WorkerSet workers = WorkerSet.builder()
        .secretLoader(secretLoader)
        .credentialStore(credentialStore)
        .keyLoader(keyLoader)
        .certificateLoader(certificateLoader)
        .build();

RuntimeServices services = new RuntimeServices(
        policies,
        executor,
        workers,
        authenticationCache,
        "production");

RuntimeManager runtime = Authorize.standard(services, blueprintLoader)
        .listener(rosterListener)
        .build(startupContext, startupTimeout)
        .toCompletableFuture()
        .join();
```

只配置所选 Driver 实际需要的 Worker 端口。候选编译时发现缺少必要端口会立即失败；Bus Auth 不会安装宽松的空实现。
`build(...)` 在暴露 Runtime 前加载、校验、编译并原子提交初始 Blueprint。`buildEmpty()` 仅用于明确需要从 revision zero、
无 Source 状态启动的管理进程。

## Source 发现

管理应用通过一个与具体实现无关的入口获得全部可选 Protocol 或 Vendor Variant：

```java
List<SourceDescriptor> choices = runtime.descriptor().sources();

SourceDescriptor selected = runtime.descriptor()
        .source("vendor/github/enterprise")
        .getOrNull();
```

每个 Descriptor 都提供稳定选择 ID、持久化 Source type、真实协议、展示元数据、配置表单、Capability Manifest、
Conformance 信息，以及无副作用的持久化 Source 匹配能力。Descriptor 不解析凭据、不创建 Worker、不读取 Roster，
也不发起网络请求。

## 调用、重载与生命周期

Source 认证、协议操作和 Realm 访问全部经过同一条显式路由：

### Source 认证

浏览器流程从已登记的回调目标开始：

```java
Roster.Reference reference = Roster.Reference.source(sourceId);
Callback.Target target = new Callback.Target(sourceId, registeredRedirectUri);
SourceWorkflow.Request.BrowserStart start =
        new SourceWorkflow.Request.BrowserStart(sourceId, target);

CompletionStage<Outcome<SourceWorkflow.Stage>> initiation = runtime.dispatcher().invoke(
        reference,
        SourceWorkflow.INITIATE,
        start,
        trustedContext,
        timeout);
```

成功结果为 `SourceWorkflow.Stage.Redirect` 时，项目负责重定向用户代理。项目回调端点随后把原始请求保存为
`Callback.Inbound`，并完成同一个 Source 交互：

```java
SourceWorkflow.Request.BrowserCallback completion =
        new SourceWorkflow.Request.BrowserCallback(sourceId, inboundCallback);

CompletionStage<Outcome<Identity>> identity = runtime.dispatcher().invoke(
        reference,
        SourceWorkflow.COMPLETE,
        completion,
        trustedContext,
        timeout);
```

Device 流程使用 `DeviceStart` 和 `DevicePoll`，直接流程使用 `Direct` 或 `OneTimeCode`。所有成功路径最终统一为经过验证的
`Identity`。Bus Auth 不负责把该 Identity 绑定到本地账户，这个决定属于接入项目。

### Realm 与协议操作

```java
Roster.Reference reference = Roster.Reference.source(sourceId);

CompletionStage<Outcome<Realm.Description>> result = runtime.dispatcher().invoke(
        reference,
        Realm.DESCRIBE,
        new Realm.Describe(),
        trustedContext,
        timeout);
```

调用方显式选择 Source 和 Capability；`Dispatcher` 不会根据不可信请求路径推断任何一个值。它统一校验生命周期、Roster
路由、Capability 声明、请求类型、认证边界和响应类型。项目传输层继续负责将正式协议请求和响应映射到自己的 HTTP、TCP
或 UDP 服务。

按照封闭结果族处理 `Outcome`：

```java
switch (outcome) {
    case Outcome.Succeeded<Realm.Description> success -> use(success.value());
    case Outcome.Rejected<Realm.Description> rejected -> reject(rejected.failure());
    case Outcome.Failed<Realm.Description> failed -> retryOrReport(failed.failure());
    default -> throw new IllegalStateException("Unsupported outcome");
}
```

`runtime.reload(context, timeout)` 每次都加载完整 Blueprint 候选。校验和编译全部完成后才会进行一次原子发布；任何失败都
不会改变当前 Roster 和 Worker。Revision 必须严格递增，并作为框架 Cache 的 generation，防止重载后继续使用旧协议状态。

应通过 `try`/`finally` 或 try-with-resources 确定性管理 `RuntimeManager` 生命周期。`close()` 拒绝新的 Dispatch 和 Reload，
并退役已编译 Worker，但不会关闭调用方拥有的 Executor、Cache、Loader、Store 或网络资源。最后一次成功提交的 Roster
仍然可以读取。

## 选择性装配与 Vendor 配置

`Authorize.standard(...)` 安装所有内置 Protocol 和 Vendor Connector。项目也可以保留全部 Protocol，只选择需要的 Vendor
平台：

```java
SourceAggregate aggregate = SourceSuite
        .load(GitHubManifest.ID, MicrosoftManifest.ID)
        .freeze();

RuntimeBuilder builder = Authorize.custom(services, blueprintLoader)
        .modules(aggregate.modules());

VendorConfigurer configurer = Authorize.clients(
        aggregate.vendorModule(),
        credentialWriter);
```

Runtime 装配和客户端 Vendor 配置必须使用同一个已经冻结的 `VendorModule`。`VendorConfigurer` 校验精确 Variant 表单，
通过短生命周期 `SecretLease` 传递明文，并且只交给项目拥有的 `VendorCredentialWriter` 保存。持久化 `VendorOptions`
只包含 `Credential.Reference`，绝不包含明文秘密材料。

## Realm 资源访问

`Realm` 是描述和读取上游身份与关系的共享协议中立契约。它提供 `DESCRIBE`、`SNAPSHOT`、可选 `CHANGES` 和可选
`RETRIEVE` Capability。调用方必须检查返回的 description、coverage、operations、limitations、resource types 和
continuation mode，不能假设所有 Adapter 行为一致。

Bus Auth 只执行经过认证的上游访问。接入项目负责同步调度、检查点持久化、字段映射、差异协调、删除策略、事务、重试，
以及本地组织、用户和用户组模型。

上游 API 没有分页时，Adapter 按原始接口完整读取，不得人为增加 page size 或总量限制；存在分页时，必须保留平台真实的
Cursor、Token、Offset 或 Link continuation 语义。

## JWT

常见签名、签发、验签和校验直接使用静态 `JWT` 门面。HS256 Secret 必须至少包含 256 bit 密钥材料。

```java
byte[] secret = secretBytes;

String compact = JWT.issue(
        Map.of("sub", "user-42", "role", "admin"),
        secret,
        "https://issuer.example",
        "bus-application",
        Duration.ofMinutes(15));

JWT.Requirements requirements = JWT.Requirements.of(
        "https://issuer.example",
        "bus-application",
        Duration.ofSeconds(30));

JWT verified = JWT.validate(compact, secret, requirements);
String subject = verified.claims().subject().getOrNull();
```

`verify(...)` 使用显式可信算法和密钥检查签名；`validate(...)` 还会校验时间声明以及可选的 Issuer/Audience 要求；
`isValid(...)` 是返回 boolean 的便捷形式。重复调用、非对称算法或显式 Clock 使用 `JwtService`；各协议包继续负责自己的
专用 JWT Claim 策略。

## 扩展 SPI

扩展一个标准协议需要提供：

- 一个或多个 `ProtocolScheme` 与 `ProtocolDriver` 配对；
- 一个原子绑定该协议全部 Driver 的 `ProtocolConnector`；
- 根 `SourceConnector` 服务的一条 `provides` 或 `META-INF/services` 声明。

扩展一个第三方平台需要提供：

- 一个 `VendorManifest` 和强类型 `VendorOptions` 实现；
- 每个精确 Variant 对应的 Adapter；
- 一个原子绑定完整平台注册内容的 `VendorConnector`；
- 根 `SourceConnector` 服务的一条 `provides` 或 `META-INF/services` 声明。

`SourceConnector` 是唯一发现服务。sealed 根接口只接纳 Protocol 和 Vendor 两个家族，两个 non-sealed 子接口继续允许外部
实现。`connect(registry)` 是同步构建期声明回调：它不建立远程连接、不保留 Registry、不访问项目数据，也不修改运行中的
Roster。

`Registry`、`ProtocolRegistry` 和 `VendorRegistry` 在 Freeze 前支持单个注册、原子批量注册、单个卸载和原子批量卸载。
不希望使用服务发现时，可以通过 `SourceSuite.register(...)` 和 `registerAll(...)` 走统一显式扩展链路。

## 安全与所有权规则

- `Policies` 包含显式且不可放宽的算法、熵、时钟偏移、重放、消息大小、地址和安全传输规则，不提供宽松默认值。
- `FabricX` 是静态传输边界；SourceServices 不再提供第二套 Fabric 门面。
- `JsonKit` 是应用级静态 JSON 边界；Driver 和 Codec 不再接收或转发 `JsonProvider`。
- `Context` 只保存可信、非敏感调用元数据，禁止放入凭据、Code、Token、Assertion 或 Secret。
- Loader 返回项目拥有的 Record；Resolver 负责校验并转换为不可变认证值。
- `WorkerSlots` 声明所需项目数据端口；`SourceDriver.Dependencies` 声明所需框架服务。
- 稳定 Source ID、Vendor ID、Variant ID、Capability Key、Endpoint Target、Scope、Cursor 和 Wire 行为共同构成对外兼容边界。
- 诊断值和 `Roster.Fault` 禁止暴露 Options Body、Token、Credential、Exception、Stack Trace 或平台 Payload。

## Package 职责

| Package | 职责 |
|:--|:--|
| `org.miaixz.bus.auth` | 领域值、`Authorize`、`Registry`、`Roster`、`Dispatcher`、`Policies` 和 `Realm` |
| `org.miaixz.bus.auth.source` | Source 发现、Descriptor、Module、Driver、Workflow 和最小权限服务契约 |
| `org.miaixz.bus.auth.source.protocol` | 正式协议注册及各协议专用子包 |
| `org.miaixz.bus.auth.source.vendor` | Vendor Manifest、Options、Connector、Adapter、Lookup 和配置 |
| `org.miaixz.bus.auth.registry` | 完整 Blueprint 校验和不可变 Roster 投影 |
| `org.miaixz.bus.auth.runtime` | Runtime 装配、最小权限服务、原子重载、Dispatch 和生命周期 |
| `org.miaixz.bus.auth.worker` | 项目 Action Port、Worker Slot、Listener、Session 和已编译 SourceWorker |
| `org.miaixz.bus.auth.worker.loader` | 项目拥有的异步数据加载端口 |
| `org.miaixz.bus.auth.resolver` | 对项目加载 Record 进行纯校验和解析 |
| `org.miaixz.bus.auth.shared` | 跨协议 JOSE、JWT、PKCE、DPoP、Claim 及相关安全组件 |

## 构建

仓库 CI 使用 JDK 25，并按 Java 21 Release 兼容级别编译 Bus 8.x：

```bash
mvn -f bus-auth/pom.xml -Dmaven.compiler.release=21 clean package
```

## 许可证

Bus Auth 使用 [Apache License 2.0](../LICENSE) 发布。
