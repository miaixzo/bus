# Bus Auth

English | [简体中文](README_CN.md)

Bus Auth is a modular authentication framework for standards-based protocols, third-party identity platforms, and
protocol-neutral identity-resource access. Every configurable implementation is exposed as a `Source`, assembled as a
`SourceModule`, compiled by a `SourceDriver`, and invoked through one `Dispatcher` path.

Bus Auth supplies authentication protocol implementations and connection capabilities. The integrating project retains
ownership of HTTP endpoints, management CRUD, persistence, credentials, account binding, business authorization,
organization synchronization, scheduling, transactions, and user sessions.

## Requirements and dependency

Bus 8.x targets Java 21 bytecode and is built by the project CI with JDK 25. The application must also select one
`JsonKit` implementation through `bus.json.provider` or `JsonFactory.install(...)` before JSON-dependent operations are
used.

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-auth</artifactId>
    <version>8.x.x</version>
</dependency>
```

JPMS applications declare:

```java
requires bus.auth;
```

Protocol and Vendor implementations are discovered from the single `SourceConnector` service. Bus Auth publishes both
`module-info.java` providers and `META-INF/services` metadata, so the same extension model works on the module path and
the class path.

## Domain model

The persistent management hierarchy is fixed:

```text
Library
└── Provider
    └── Source
```

- `Library` is the top-level managed authentication grouping.
- `Provider` groups project-owned Source configurations; it is not an OAuth, OpenID Connect, or SAML server role.
- `Source` is the only persisted routing and runtime unit.
- `Blueprint` is the complete desired Library/Provider/Source configuration loaded by the project.
- `Roster` is the read-only, currently committed Blueprint snapshot.
- `Scheme` describes one implementation and its typed `Scheme.Options`.
- `SourceDescriptor` exposes one selectable implementation to management callers without creating a runtime worker.
- `Capability` defines a typed request/response operation; `Dispatcher` is the only runtime execution boundary.
- `Outcome` distinguishes success, expected rejection, and operational failure without turning normal authentication
  failures into exceptional stage completions.
- `Realm` defines protocol-neutral upstream resources and relationships; it is not a persistence entity or a second
  runtime.

## Architecture

Protocol and Vendor implementations are both Source specializations:

```text
org.miaixz.bus.auth.source
├── protocol   LDAP, OAuth 2.x, OpenID Connect, RADIUS, SAML and SCIM
└── vendor     named third-party platforms, variants and optional Realm adapters
```

They converge before runtime assembly:

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

`Registry` and `Registry.Connector` are build-time registration contracts. The `registry` package performs Blueprint
validation and Roster projection after registrations are frozen; it is not another Connector registry.

`ProtocolModule` and `VendorModule` are the two built-in `SourceModule` implementations retained by
`SourceAggregate`. `SourceLookup` freezes driver, descriptor, and reverse-routing indexes. Runtime compilation creates a
capability-limited `ScopedSourceServices` view for each Source; a driver never receives the complete
`RuntimeServices` container.

## Built-in implementations

Standards-based branches include:

- LDAP client and server;
- OAuth 2.x client and authorization server;
- OpenID Connect client and provider;
- RADIUS server;
- SAML client and identity provider;
- SCIM server.

Registered Vendor platforms include:

Afdian, Alipay, Aliyun, Amazon, Apple, Baidu, Coding, DingTalk, Douyin, Eleme, Facebook, Feishu, Figma, Gitee, GitHub,
GitLab, Google, Huawei, JD, Kujiale, LINE, LinkedIn, Meituan, Mi, Microsoft, Okta, OSChina, Pinterest, Proginn, QQ,
RedNote, Slack, Stack Overflow, Taobao, Teambition, Toutiao, Twitter, VK, WeChat, Weibo, and Ximalaya.

Realm adapters are currently supplied for Aliyun, DingTalk, Feishu, Figma, GitHub, GitLab, Google, Microsoft, Okta,
Slack, and WeChat Work. Capability availability is declared by each exact Variant; a platform name alone never implies
Realm, incremental-change, or retrieval support.

## Runtime assembly

The project supplies all external infrastructure explicitly:

- `Policies` containing a non-relaxable rule for every selected protocol;
- a caller-owned `Executor`;
- an atomic `CacheX<String, Object>` backend and deployment isolation identifier;
- a `WorkerSet` containing only the project ports required by the selected Source drivers;
- a `BlueprintLoader` returning the complete desired Blueprint revision;
- optional `RosterListener` instances.

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

Only configure Worker ports required by the selected drivers. Missing required ports fail during candidate compilation;
Bus Auth does not install permissive no-op implementations. `build(...)` loads, validates, compiles, and atomically
commits the initial Blueprint before exposing the runtime. `buildEmpty()` is reserved for administrative processes that
intentionally start with revision zero and no configured Sources.

## Source discovery

Management applications obtain every selectable protocol or Vendor Variant from one implementation-neutral surface:

```java
List<SourceDescriptor> choices = runtime.descriptor().sources();

SourceDescriptor selected = runtime.descriptor()
        .source("vendor/github/enterprise")
        .getOrNull();
```

Each descriptor exposes its stable selection ID, persisted Source type, exact protocol, presentation metadata,
configuration form, capability manifest, conformance information, and a side-effect-free persisted-Source matcher.
Descriptors never resolve credentials, construct workers, read Roster state, or perform network calls.

## Invocation, reload, and lifecycle

Source authentication, protocol operations, and Realm access all use the same explicit route:

### Source authentication

A browser flow starts with a registered callback target:

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

The project redirects the user agent when the successful stage is `SourceWorkflow.Stage.Redirect`. Its callback endpoint
then preserves the raw request as `Callback.Inbound` and completes the same Source interaction:

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

Device flows use `DeviceStart` and `DevicePoll`; direct flows use `Direct` or `OneTimeCode`. All successful paths converge
on a verified `Identity`. Bus Auth does not bind that identity to a local account—the integrating project owns that
decision.

### Realm and protocol operations

```java
Roster.Reference reference = Roster.Reference.source(sourceId);

CompletionStage<Outcome<Realm.Description>> result = runtime.dispatcher().invoke(
        reference,
        Realm.DESCRIBE,
        new Realm.Describe(),
        trustedContext,
        timeout);
```

The caller selects the Source and Capability; `Dispatcher` never infers either value from an untrusted request path. It
checks lifecycle state, Roster routing, capability declaration, request type, authentication boundary, and response
type. The project transport layer remains responsible for mapping formal protocol requests and responses to its HTTP,
TCP, or UDP server.

Process `Outcome` as a closed result family:

```java
switch (outcome) {
    case Outcome.Succeeded<Realm.Description> success -> use(success.value());
    case Outcome.Rejected<Realm.Description> rejected -> reject(rejected.failure());
    case Outcome.Failed<Realm.Description> failed -> retryOrReport(failed.failure());
    default -> throw new IllegalStateException("Unsupported outcome");
}
```

`runtime.reload(context, timeout)` always loads a complete Blueprint candidate. Validation and compilation finish before
one atomic publication; any failure leaves the active Roster and workers unchanged. Revisions strictly increase and
generation-scope framework caches so stale protocol state cannot be reused after reload.

Use `RuntimeManager` with deterministic lifecycle ownership, preferably in `try`/`finally` or try-with-resources.
`close()` rejects new dispatch and reload operations and retires compiled workers, but does not close caller-owned
executors, caches, loaders, stores, or network resources. The last committed Roster remains readable.

## Selective assembly and Vendor configuration

`Authorize.standard(...)` installs every built-in protocol and Vendor connector. A project may retain every protocol
while selecting only the required Vendor platforms:

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

Runtime assembly and client-side Vendor configuration must use the same frozen `VendorModule`. `VendorConfigurer`
validates the exact Variant form, passes plaintext through a short-lived `SecretLease`, and stores it only through the
project-owned `VendorCredentialWriter`. Persisted `VendorOptions` contain a `Credential.Reference`, never plaintext
secret material.

## Realm access

`Realm` is the shared, protocol-neutral contract for describing and reading upstream identities and relationships. Its
capabilities are `DESCRIBE`, `SNAPSHOT`, optional `CHANGES`, and optional `RETRIEVE`. Callers must inspect the returned
description, coverage, operations, limitations, resource types, and continuation mode instead of assuming every adapter
behaves alike.

Bus Auth only performs authenticated upstream access. The integrating project owns synchronization scheduling,
checkpoint persistence, mapping, reconciliation, deletion policy, transactions, retries, and its local organization,
user, and group models.

When an upstream API has no pagination, an adapter reads the original complete result without imposing an artificial
page size or total limit. When pagination exists, it preserves the platform's real cursor, token, offset, or link
continuation semantics.

## JWT

Use the static `JWT` facade for common signing, issuance, verification, and validation. An HS256 secret must contain at
least 256 bits of key material.

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

`verify(...)` checks the signature with an explicitly trusted algorithm and key. `validate(...)` also evaluates temporal
claims and optional issuer/audience requirements. `isValid(...)` is the boolean convenience form. Use `JwtService` for
repeated operations, asymmetric algorithms, or an explicit clock; protocol packages continue to own their specialized
JWT claim policies.

## Extension SPI

A standards-based protocol extension contributes:

- one or more `ProtocolScheme` and `ProtocolDriver` pairs;
- one `ProtocolConnector` that atomically binds every driver owned by the protocol;
- one `provides` or `META-INF/services` declaration for the root `SourceConnector` service.

A third-party platform extension contributes:

- one `VendorManifest` and typed `VendorOptions` implementation;
- one adapter for each exact Variant;
- one `VendorConnector` that atomically binds the complete platform registration;
- one `provides` or `META-INF/services` declaration for the root `SourceConnector` service.

`SourceConnector` is the only discovery service. Its sealed root admits the protocol and Vendor families, while the
non-sealed child interfaces remain open to external implementations. `connect(registry)` is a synchronous build-time
declaration callback: it does not establish a remote connection, retain the registry, access project data, or mutate a
running Roster.

`Registry`, `ProtocolRegistry`, and `VendorRegistry` support single registration, atomic bulk registration, single
removal, and atomic bulk removal before freeze. `SourceSuite.register(...)` and `registerAll(...)` provide the unified
explicit extension path when service discovery is not desired.

## Security and ownership rules

- `Policies` contains explicit non-relaxable algorithm, entropy, clock-skew, replay, message-size, address, and secure
  transport rules. It supplies no permissive defaults.
- `FabricX` is the static transport boundary; Source services do not receive a secondary Fabric facade.
- `JsonKit` is the static application-wide JSON boundary; drivers and codecs do not receive or forward a
  `JsonProvider`.
- `Context` contains trusted, non-secret invocation metadata only. Credentials, codes, tokens, assertions, and secret
  values must not be placed in it.
- Loaders return project-owned records; resolvers validate and convert them into immutable authentication values.
- `WorkerSlots` declares required project data ports; `SourceDriver.Dependencies` declares required framework services.
- Stable Source IDs, Vendor IDs, Variant IDs, capability keys, endpoint targets, scopes, cursors, and wire behavior form
  the public compatibility boundary.
- Diagnostic values and `Roster.Fault` must not expose options bodies, tokens, credentials, exceptions, stack traces,
  or platform payloads.

## Package responsibilities

| Package | Responsibility |
|:--|:--|
| `org.miaixz.bus.auth` | Domain values, `Authorize`, `Registry`, `Roster`, `Dispatcher`, `Policies`, and `Realm` |
| `org.miaixz.bus.auth.source` | Source discovery, descriptors, modules, drivers, workflows, and scoped service contract |
| `org.miaixz.bus.auth.source.protocol` | Formal protocol registration and protocol-specific child packages |
| `org.miaixz.bus.auth.source.vendor` | Vendor manifests, options, connectors, adapters, lookup, and configuration |
| `org.miaixz.bus.auth.registry` | Complete Blueprint validation and immutable Roster projections |
| `org.miaixz.bus.auth.runtime` | Runtime assembly, scoped services, atomic reload, dispatch, and lifecycle |
| `org.miaixz.bus.auth.worker` | Project action ports, Worker slots, listeners, sessions, and compiled Source workers |
| `org.miaixz.bus.auth.worker.loader` | Asynchronous project-owned data-loading ports |
| `org.miaixz.bus.auth.resolver` | Pure validation and parsing of project-loaded records |
| `org.miaixz.bus.auth.shared` | Cross-protocol JOSE, JWT, PKCE, DPoP, claims, and related security building blocks |

## Build

The repository CI compiles Bus 8.x with JDK 25 and Java 21 release compatibility:

```bash
mvn -f bus-auth/pom.xml -Dmaven.compiler.release=21 clean package
```

## License

Bus Auth is released under the [Apache License 2.0](../LICENSE).
