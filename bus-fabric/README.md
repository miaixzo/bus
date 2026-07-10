# Bus Fabric

Bus Fabric is the Bus module for protocol execution and shared network runtime concerns. It exposes one public entry
shape: start from `Fabric`, choose a protocol builder, then configure the request, connection, guards, observers, and
runtime options on that protocol chain.

This README documents the current API only. Historical facades and direct transport internals are intentionally not
shown here; application modules should enter through `Fabric` and stay on the protocol builder returned by that entry.

## Requirements

- Java 21+
- Maven:

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-fabric</artifactId>
</dependency>
```

## Module Boundaries

| Package | Responsibility |
| --- | --- |
| `org.miaixz.bus.fabric` | Shared contracts such as `Context`, `Options`, `Call`, `Payload`, `Headers`, `Message`, and `Session`. |
| `org.miaixz.bus.fabric.protocol.http` | HTTP request building, response handling, cache integration, download support, cookies, and HTTP body types. |
| `org.miaixz.bus.fabric.protocol.socket` | Raw TCP, TLS socket, UDP, and KCP sessions built around frame codecs. |
| `org.miaixz.bus.fabric.protocol.sse` | Server-sent event streams and reconnect metadata. |
| `org.miaixz.bus.fabric.protocol.websocket` | WebSocket connection setup, message callbacks, text/binary writes, ping, and close handling. |
| `org.miaixz.bus.fabric.protocol.stomp` | STOMP sessions opened over `ws` or `wss`, subscription helpers, heart-beat, ack/nack, and send operations. |
| `org.miaixz.bus.fabric.network` | Low-level address, DNS, proxy, TLS, TCP, UDP, AIO, KCP, and connection pooling primitives. |
| `org.miaixz.bus.fabric.guard` | Body, frame, TLS, and chain guards used before data is accepted or sent. |
| `org.miaixz.bus.fabric.observe` | Protocol lifecycle observation and listener bridging. |
| `org.miaixz.bus.fabric.cache` | Generic cache store implementations used by HTTP cache orchestration. |

## Entry Points

Create and reuse a `Context` when related calls should share options, DNS, runtime resources, listeners, and connection
pool state.

```java
Context context = Context.create();
```

| Entry | Builder | Use for |
| --- | --- | --- |
| `Fabric.http(context)` | `HttpX.Builder` | HTTP and HTTPS requests. |
| `Fabric.socket(context)` | `SocketX.Builder` | TCP, TLS socket, UDP, and KCP sessions. |
| `Fabric.sse(context)` | `SseX.Builder` | Server-sent event streams. |
| `Fabric.websocket(context)` | `WebSocketX.Builder` | WebSocket sessions. |
| `Fabric.stomp(context)` | `StompX.Builder` | STOMP sessions. |

Each entry also has a no-argument overload that creates a default context. Prefer passing an explicit context in
applications so options and lifecycle policy are visible at the call site.

## Basic Usage

### HTTP GET

```java
String text = Fabric.http(context)
        .get("https://example.com/search")
        .query("q", "bus")
        .executeText();
```

Use `execute()` when status, headers, or streaming response access matters. `HttpResponse` is closeable and should be
closed after reading.

```java
try (HttpResponse response = Fabric.http(context)
        .get("https://example.com/items/1001")
        .execute()) {
    int status = response.code();
    String body = response.text();
}
```

### HTTP POST

```java
String result = Fabric.http(context)
        .post("https://example.com/items")
        .header("User-Agent", "bus-fabric")
        .json("{\"name\":\"bus\"}")
        .executeText();
```

### Forms And Files

```java
String form = Fabric.http(context)
        .post("https://example.com/login")
        .form("username", "kimi")
        .form("password", "secret")
        .executeText();
```

```java
String uploaded = Fabric.http(context)
        .post("https://example.com/upload")
        .multipart()
        .form("bucket", "docs")
        .file("file", Path.of("report.md"))
        .progress((done, total) -> progress(done, total))
        .executeText();
```

### Socket

The socket entry is for protocol-neutral byte transport. Pick a transport, set a frame codec, then send `Payload`
values through the returned session.

```java
SocketSession session = Fabric.socket(context)
        .tcp("127.0.0.1", 9000)
        .frame(FrameCodec.line())
        .onText(System.out::println)
        .connect();

session.send(Payload.of("ping", StandardCharsets.UTF_8)).execute();
session.close();
```

KCP is exposed by the same socket chain:

```java
SocketSession session = Fabric.socket(context)
        .kcp("127.0.0.1", 7000)
        .frame(FrameCodec.line())
        .onText(this::handleLine)
        .connect();
```

### SSE

```java
SseSession session = Fabric.sse(context)
        .url("https://example.com/events")
        .lastId("42")
        .onEvent(event -> handle(event.event(), event.data()))
        .connect();

session.close();
```

### WebSocket

```java
WebSocketSession session = Fabric.websocket(context)
        .url("wss://example.com/ws")
        .protocol("chat")
        .onText(this::handleText)
        .connect();

session.send("hello").execute();
session.close();
```

### STOMP

```java
StompSession session = Fabric.stomp(context)
        .url("wss://example.com/stomp")
        .login("user", "secret")
        .connect();

session.topic("jobs", message -> handle(message.destination(), message.text(StandardCharsets.UTF_8)));
session.sendTo("/queue/jobs", "{\"id\":1001}").execute();
session.close();
```

## Advanced Usage

### Context And Options

`Context` is immutable. Change runtime behavior by creating a new context with a copied `Options` snapshot.

```java
Context limited = context.withOptions(
        context.options().materializeMaxBytes(16L * 1024L * 1024L));

String text = Fabric.http(limited)
        .get("https://example.com/report.txt")
        .executeText();
```

Important option keys currently read by the protocol chain:

| Option key | Expected value | Used by |
| --- | --- | --- |
| `materialize.maxBytes` | `Number` | `Payload`, HTTP bodies, socket messages, WebSocket messages, and STOMP messages when bytes are materialized. |
| `http.cache` | `HttpCache` | HTTP cache coordination. |
| `http.cookieJar` | `CookieJar` | Automatic HTTP cookie loading and saving. |
| `http.tlsSettings` | `TlsSettings` | HTTP TLS configuration. |
| `http.tlsContext` | `TlsContext` | HTTP TLS context. |
| `socket.tlsSettings` | `TlsSettings` | TLS socket configuration. |
| `socket.tlsContext` | `TlsContext` | TLS socket context. |
| `tlsSettings` | `TlsSettings` | Shared fallback TLS settings. |
| `tlsContext` | `TlsContext` | Shared fallback TLS context. |

### Materialization Limit

`bytes()` and `text()` are direct materialization APIs. They are convenient for small payloads and protected by
`Options.materializeMaxBytes(...)`. The default limit is `64 MiB`. Use streaming APIs for larger bodies, or raise the
limit explicitly at the context boundary.

### Async Calls

Every protocol builder can create a `Call<T>`. `enqueue()` starts background execution where the implementation
supports it; `await()` returns the completed value.

```java
Call<HttpResponse> call = Fabric.http(context)
        .get("https://example.com/items")
        .enqueue();

try (HttpResponse response = call.await(Duration.ofSeconds(5))) {
    String body = response.text();
}
```

### HTTP Cache

HTTP cache is attached through the context. Use `http.cache` for HTTP-specific configuration.

```java
try (HttpCache cache = HttpCache.create(
        DiskStore.open(Path.of("target/http-cache"), 128L * 1024L * 1024L),
        CachePolicy.defaults(),
        EventObserver.noop())) {

    Context cached = context.withOptions(context.options().with("http.cache", cache));

    String body = Fabric.http(cached)
            .get("https://example.com/cacheable")
            .executeText();

    HttpCacheStats stats = cache.stats();
}
```

### Cookies

Automatic cookies are opt-in. The shared `CookieJar` is attached through `http.cookieJar`.

```java
CookieJar jar = CookieJar.memory();
Context stateful = context.withOptions(context.options().with("http.cookieJar", jar));

Fabric.http(stateful)
        .get("https://example.com/login")
        .executeText();
```

### Resumable Downloads

Downloads are represented by `HttpDownload`. Build the `GET` request with the HTTP entry, then pass the request to the
download task. The task owns range resume, progress, pause, cancel, and sidecar metadata handling.

```java
HttpRequest request = Fabric.http(context)
        .get("https://example.com/archive.zip")
        .request();

Path file = HttpDownload.builder(context)
        .request(request)
        .target(Path.of("archive.zip"))
        .progress((done, total) -> progress(done, total))
        .resume(true)
        .execute();
```

### Proxy And TLS

HTTP proxy configuration belongs on the HTTP builder:

```java
String proxied = Fabric.http(context)
        .get("https://example.com")
        .proxy("http://proxy.local:8080")
        .proxyCredentials("user", "secret")
        .executeText();
```

TLS settings belong on the context. Use the protocol-specific key when only one protocol should be affected.

```java
TlsSettings tls = TlsSettings.builder()
        .certificate(CertificatePolicy.builder()
                .pin("example.com", "sha256/base64-pin-value")
                .build())
        .build();

Context pinned = context.withOptions(context.options().with("http.tlsSettings", tls));

String body = Fabric.http(pinned)
        .get("https://example.com")
        .executeText();
```

Socket TLS uses the socket entry and the socket-specific key:

```java
Context secureSocket = context.withOptions(context.options().with("socket.tlsSettings", tls));

SocketSession session = Fabric.socket(secureSocket)
        .tls("example.com", 443)
        .frame(FrameCodec.line())
        .connect();
```

### Guards

Guards reject or shape data at protocol boundaries. Use frame guards for stream-like writes and body guards for HTTP
message bodies. Combine more than one guard with `GuardChain`.

```java
GuardRule guard = GuardChain.of(
        org.miaixz.bus.fabric.guard.frame.RateGuard.of(1024 * 1024),
        org.miaixz.bus.fabric.guard.body.LimitGuard.of(8L * 1024L * 1024L));

String body = Fabric.http(context)
        .post("https://example.com/items")
        .guard(guard)
        .json("{\"name\":\"bus\"}")
        .executeText();
```

### Observation

Attach an observer to a builder to receive protocol events. `EventListenerBridge` maps fabric observation markers to
named callbacks such as call start/end, DNS, connect, TLS, cache, headers, body, close, and failure callbacks.

```java
EventObserver observer = EventListenerBridge.of(new EventListenerBridge.Listener() {

    @Override
    public void callStart(FabricEvent event) {
        trace(event);
    }

    @Override
    public void callFailed(FabricEvent event, Throwable cause) {
        warn(event, cause);
    }
});

String body = Fabric.http(context)
        .get("https://example.com")
        .observe(observer)
        .executeText();
```

### Socket Tuning

Socket options can be supplied directly on the socket builder. They are passed into the socket runtime and are also
available from the opened session attributes.

```java
SocketOptions options = SocketOptions.builder()
        .readBufferSize(16 * 1024)
        .writeChunkSize(16 * 1024)
        .connectTimeout(Duration.ofSeconds(3))
        .idleTimeout(Duration.ofSeconds(30))
        .build();

SocketSession session = Fabric.socket(context)
        .tcp("10.0.0.12", 9000)
        .socketOptions(options)
        .frame(FrameCodec.line())
        .connect();
```

### Protocol-specific Controls

```java
SseSession events = Fabric.sse(context)
        .url("https://example.com/events")
        .retry(Duration.ofSeconds(2))
        .reconnect(true)
        .onEvent(this::handleEvent)
        .connect();
```

```java
WebSocketSession socket = Fabric.websocket(context)
        .url("wss://example.com/ws")
        .protocol("chat.v2")
        .timeout(Duration.ofSeconds(10))
        .onText(this::handleText)
        .connect();
```

```java
StompSession stomp = Fabric.stomp(context)
        .url("wss://example.com/stomp")
        .heartBeat(Duration.ofSeconds(10), Duration.ofSeconds(10))
        .connect();

stomp.subscribe("/topic/audit", this::handleMessage);
```

## Migration Rules

- Use `Fabric.http(context)`, `Fabric.socket(context)`, `Fabric.sse(context)`, `Fabric.websocket(context)`, and
  `Fabric.stomp(context)` as application entry points.
- Do not call network connector classes directly from application modules. Network classes are shared implementation
  primitives for protocol builders.
- Do not restore old HTTP facade names or socket plugin APIs. Their current replacements are the protocol builders,
  `Context` options, `guard.*`, `observe.*`, `network.proxy.*`, and `network.tls.*`.
- Keep protocol concerns in their protocol packages. For example, HTTP cache and HTTP downloads belong under
  `protocol.http`; generic cache stores remain under `cache`.

## Recommended Verification

For code changes touching this module, run the module build and the external fabric integration checks used by the
repository.

```bash
mvn -f bus-fabric/pom.xml -DskipTests compile
```

The module native-image metadata points external tests to
`https://github.com/818000/abarth/tree/main/bus-testing/src/test`; use that external suite when integration resources
are available.
