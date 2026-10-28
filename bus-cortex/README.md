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
| `guard.enabled` | `true` | Enable optional Cortex access checks and TokenGuard pre-validation |
| `guard.token` | TokenGuardConfig defaults | Token transport pre-validation options |
| `audit.enabled` | `false` | Assemble the audit logger |
| `release.enabled` | `false` | Assemble the read-only release registry when ReleaseStore exists |

## Host wiring and transactions

Starter assembles setting services conditionally from host-provided Stores. Current-state operations use `ItemStore`, `ReferenceStore`, `SettingStore` and related services. Approval and publication use `RolloutStore`, `ApprovalStore`, `PublicationStore`, `ReleaseStore` and `RevisionStore`. Runtime delivery also requires `WatchStore` and `SettingProvider`. Each Bean's `ConditionalOnBean` declares its exact dependency set.

`SettingProvider` supplies invocation coordinates and current process data without authenticating callers. Host implementations enforce tenant isolation, process-session consistency, CAS and atomic commits. Approval policy validation belongs to `ApprovalStore`. `SettingStore` commits current definitions and relationships together; `RolloutStore.submit` atomically validates the frozen read-set; `PublicationStore` commits successful snapshots and publication state.

Set `bus.cortex.guard.enabled=false` when an upstream gateway handles access policy. This bypasses optional guard checks for both registry and setting services, including host-supplied guards; it never bypasses tenant matching, edition checks, approval state, frozen checksums, receipt idempotency or secret protection. With guards enabled, host `GuardStrategy` implementations supply access decisions; Cortex does not authenticate accounts or credentials. Reads need no audit actor or request; idempotent mutations require a request, and approval evidence also requires an actor.

The default `NoOpSecretCodec` rejects sensitive content. Hosts must provide real protection; there is no silent plaintext fallback. TokenGuard only pre-validates credential presence and transport; authentication, permission and license checks remain host responsibilities.

Registry Beans can be injected directly. The static Cortex facade requires a bound Runtime. Starter creates registry-only Runtime when Curator is absent; configuration facade access then fails explicitly. RuntimeHandle releases the binding when the application context closes.

## Current API examples

These method fragments receive Registry, Curator and request arguments from the host. Tenant and application coordinates identify data scope; authentication is a host responsibility.

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

API service definitions use global scope and are unique by `app_id + method + verb + version`; registration requires `app_id`, and one method may support separate GET, POST and PUT definitions; MCP and Prompt operations require an explicit tenant. Shared processes use the root `Instance` entity with `app_id` ownership. Registry maintains service associations; runtime instances are identified by `checksum`.

`Instance` uses the inherited integer `status` only: DELETED=-1, DISABLED=0, UP=1, DOWN=2, UNKNOWN=3, STARTING=4, MAINTENANCE=5. `modified` is the inherited general modification time in epoch milliseconds. The `health` JSON text stores `scheme`, `path`, `time` (the most recent probe time), `origin`, `session`, `beat`, and `lease`; these are not separate instance fields. Session expiry is independent of reachability. New registrations default to UNKNOWN at the registration boundary; active probes do not override disabled, retired, or maintenance instances.

`health`, `labels`, and `metadata` are JSON object **text**, suitable for persistence. Labels require string values; metadata may contain nested values. `labelView()` and `metadataView()` parse these attributes without persistent Map fields or compatibility aliases. Program `version` does not participate in the host/port checksum.

`org.miaixz.bus.cortex.health.HealthInfo` is the shared typed value of the seven-member health JSON document and owns its strict parsing, validation and serialization through `HealthInfo.parse(String)` and `toJson()`. `Instance.healthView()` returns a detached validated snapshot; callers change their own members and pass the complete value to `health(HealthInfo)`. Null members clear values. Persistence remains `@Lob String health`; the shared value has no ORM mapping or execution responsibilities. There is no health Map, string-key getter or patch API. Probers own `time` and `origin`; the host owns `session`, `beat`, and `lease`. Client reports may supply check configuration but cannot overwrite server observations. Session establishment or replacement clears old probe observations while retaining the supplied check configuration. Watch and delivery evidence keep their own session coordinate and compare it with `health.session`. Lattice fences API association writes using the original modification time and observation snapshot; setting instance sessions retain their existing concurrency validation; health-change events compare status and `health.origin`, not the entire JSON text.

Probe cycles group registrations by process/session and the effective protocol, host, port and HTTP path. Equivalent defaults share I/O; different targets are checked separately. Results update existing observations through `RegistryStore.updateHealth`, never normal registration. The host atomically fences the unchanged service and runtime row and publishes cache/event effects only after commit. Stale, disabled, removed or replaced registrations reject the result. `ApiAssets` no longer owns or supplies heartbeat/lease defaults; normal metadata saves remove its old `beat` and `lease` keys while retaining unrelated extensions.

Cache-only probing requires backend conditional replacement. The default Starter Caffeine backend and its MemoryCache fallback support it, as does the single-node Redis backend; Hybrid delegates the primary comparison, coordinates local mirror mutations and reconciles completions from the current primary value. Mirror keys only enumerate candidates; reads and scan values always come from the primary. The complete atomic-cache capability flag keeps its existing meaning, and unsupported backends do not fall back to read-then-write.

Probe writes compare the captured service definition and runtime value in one guarded backend replacement. API runtime cache partitions include the existing verb coordinate. Exact-service sampling retains distinct association snapshots before grouping effective targets, while the public process query still deduplicates process identities. Durable runtime reads do not warm caches, and durable registration/probe callbacks only invalidate their runtime projection. Shared and typed host stores use the same commit/rollback callback boundary; one association failure does not skip its peers. Lattice owns the scheduler lifecycle after its context refresh; Starter clients alone do not start a probe scheduler.




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

## Configuration property attributes

`Item.Property` owns an immutable `Map<String, JsonValue>` of flat attributes. `ItemCodec` alone parses and serializes it; no Java wrapper is added to persisted content:

```json
{"server.port":{"value":9090,"default":8080,"desc":"Port","required":true,"unit":"tcp","minimum":1}}
```

`value`, `default`, `desc` and boolean `required` have defined semantics. Other attributes retain their exact JSON types and remain outside runtime business values; they do not create new validation rules. Use the existing Schema for additional business constraints. Missing attributes and explicit JSON null remain distinct.

`ValidationLevel.REQUIRED` checks explicit actual values after protected edit actions, in addition to full validation. A required value cannot be absent, null or a blank string; false and zero are valid, and defaults do not satisfy this explicit edit check. Registration uses FULL, which retains declarations without demanding populated values.

Dynamic attributes follow existing protected storage, masking and immutable publication snapshots. `desc` and `required` remain visible. Exact extension intents reuse SecretAction with definition fragment coordinates: property `/password`, attribute `unit` becomes `#/~1password/unit`. These coordinates are distinct from `/password/value` and `/password/default`. Renames, restores and imports retain complete attributes; explicit DELETE removes an attribute, while KEEP preserves it.

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
