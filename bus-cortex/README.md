# Bus Cortex

Cortex provides service registration and discovery, and application-qualified delivery of published configuration. Registry and Setting retain separate business responsibilities while sharing entities and infrastructure.

## Responsibility boundaries

| Area | Responsibility |
| --- | --- |
| Registry | Service definitions, route associations, instance discovery and health |
| Item / ItemEditor / ItemService | Current configuration, sensitive protection, management reads and editing concurrency |
| Reference | Application associations, aliases, overlays and pins to published Revisions |
| Rollout / Approval | Publication selection, frozen approval material and workflow state |
| Revision / Release | Immutable snapshots of successful publications, release records and publication transactions |
| Delivery / Watch | Published reads, instance-session checks, subscriptions and delivery evidence |
| Starter | Conditional assembly, dependency wiring and lifecycle binding |

Saving an Item creates neither a version nor an edit history. Reference `revision_id` pins a successfully published snapshot; Item `edition` controls editing concurrency. Rollout drafts belong to the approval workflow.

`ItemCodec`, `ReferenceCodec` and `Plan` parse configuration content, relationship options and approval material respectively. `ReleaseCodec` encodes, decodes and verifies published graphs using protocol `services-release-v3`.

## Spring Boot setup

Add `org.miaixz:bus-starter` at the same version as the other Bus modules in your project.
Enable assembly with `org.miaixz.bus.starter.annotation.EnableCortex` or `bus.cortex.enabled=true`.

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

## Configuration properties

All properties below are under `bus.cortex`. Existing `cache` options select the cache backend.

| Property | Default | Purpose |
| --- | --- | --- |
| `enabled` | `false` | Enable Cortex assembly |
| `max-watches-per-tenant` | `1000` | Positive per-tenant subscription limit |
| `watch-expire` | `24h` | Positive subscription expiry |
| `cache-expire` | `1h` | Positive local Cortex cache expiry |
| `cache` | Optional | Bus cache backend options |
| `watch.logging-enabled` | `false` | Register the watch logging listener |
| `bridge.mode` | `pull-only` | Assemble VortexBridge for `push+pull` |
| `bridge.url` | Empty | Required for `push+pull` mode |
| `bridge.max-retries` | `3` | Positive bridge retry limit |
| `bridge.source` | Empty | Bridge source marker |
| `guard.enabled` | `true` | Assemble TokenGuard configuration and strategy |
| `guard.token` | TokenGuardConfig defaults | Token transport pre-validation options |
| `audit.enabled` | `false` | Assemble the audit logger |
| `release.enabled` | `false` | Assemble the read-only release registry when ReleaseStore exists |

## Host wiring and transactions

Starter assembles setting services conditionally from host-provided Stores and Enforcer. Current-state operations use `ItemStore`, `ReferenceStore`, `SettingStore` and related services. Approval and publication use `RolloutStore`, `ApprovalStore`, `PublicationStore`, `ReleaseStore` and `RevisionStore`. Runtime delivery also requires `WatchStore` and `SettingEnforcer`. Each Bean's `ConditionalOnBean` declares its exact dependency set.

Host implementations enforce tenant isolation, authoritative instance reads and session checks, authorization, CAS and atomic commits. `SettingStore` commits current definitions and relationships together; `RolloutStore.submit` atomically validates the frozen read-set; `PublicationStore` commits successful snapshots and publication state.

The default `NoOpSecretCodec` rejects sensitive content. Hosts must provide real protection; there is no silent plaintext fallback. TokenGuard only pre-validates credential presence and transport; authentication, permission and license checks remain host responsibilities.

Registry Beans can be injected directly. The static Cortex facade requires a bound Runtime. Starter creates registry-only Runtime when Curator is absent; configuration facade access then fails explicitly. RuntimeHandle releases the binding when the application context closes.

## Current API examples

These method fragments receive Registry, Curator and request arguments from the host. Tenant and application identities must come from trusted context.

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

API service routes use global scope; MCP and Prompt operations require an explicit tenant. Shared processes use the root `Instance` entity with `app_id` ownership. Registry maintains service associations; runtime instances are identified by `checksum`.

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

## Publication flow

1. Prepare current edits with ItemService and save definitions with References through SettingService.
2. Create publication selection with RolloutService; submission validates the current read-set and freezes approval material.
3. Record approval decisions with ApprovalService; submitted material cannot be edited arbitrarily.
4. Publish approved material with ReleasePublisher; only successful publication creates Revision / Release data.
5. Curator reads stable or matching gray published content, without consulting editable Items or live References.

Gray selection belongs to publication. Pins must refer to published Revisions. Rollback also requires an approved publication workflow.

Rollout stores workflow state in inherited `status`: DRAFT=0, REVIEWING=1, APPROVED=2, REJECTED=3, PUBLISHING=4, FAILED=5, PUBLISHED=6. `Rollout.State` retains the transition rules.

All Watch event types share a tenant-scoped sequence, preserving monotonic mixed subscriptions. Outbox implementations must filter domain/resourceType before acquiring leases; unfiltered convenience methods delegate to the same atomic operation.

## Packages

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

## Secret management

`setting.secret` supports direct Alibaba Cloud KMS, AWS Secrets Manager, Azure Key Vault, Vault KV v2, Tencent Cloud SSM, and Huawei Cloud CSMS reads, explicit authorization, version selection, and bounded in-memory caching with refresh. No vendor SDK is required.

The concrete `SecretManager` owns connection registration via `register(connection, provider)`.
Its authorization contract is `SecretManager.AccessPolicy`; read capabilities are
`SecretProvider.Capability`, and fresh credentials come from `CloudCredentials.Source`.
A null `SecretReference.version()` selects the current backend version. Persisted JSON keeps its existing
`selector.mode/version` and `expectedType` fields. `SecretMaterial` exposes version, fetch time, expiration
and connection generation directly; `describe()` returns an immutable capability set.
Payload types remain configuration labels and do not split the complete-value cache.

Caching uses Bus `MemoryCache` directly. Inject an empty cache dedicated to this manager; the manager owns its close lifecycle and erases values through removal callbacks. Capacity belongs to MemoryCache; values exceeding 8192 characters are returned without being cached.

Lifecycle guards cover cached and uncached reads; close drains admitted reads and prevents them from returning secrets. Cache cleanup after a failed read is best effort, never waits for a busy resource, and cannot replace the original denial. Each HTTP attempt passes its capped ten-second deadline to both credential acquisition and transport; wrapped native timeouts fail immediately.

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

Secret failures directly throw Bus `ForbiddenException` for denied access or `CryptoException` for other failures. Public exceptions contain fixed, sanitized text without backend messages, credentials, causes or custom error codes; callers must not parse exception messages. Cloud adapters decide response retries from HTTP status and native platform error fields. At most three attempts are made for throttling and temporary service failures (HTTP 429/500/502/503, or recognized Alibaba/AWS/Tencent transient codes). Recognized transport I/O failures can retry; timeouts, denied access, missing values, malformed successful responses, and credential-source failures fail immediately. Each attempt reacquires and closes its credentials.

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
