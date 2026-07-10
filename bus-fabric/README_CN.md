# Bus Fabric

Bus Fabric 是 Bus 体系中负责协议执行与共享网络运行时能力的组件。公开入口只有一种形态：先从
`Fabric` 进入，再选择具体协议 Builder，并在该协议链路上配置请求、连接、守卫、观测与运行时选项。

本文档只描述当前 API。历史兼容门面和直接调用底层传输类的方式不再作为使用方式出现；业务模块应通过
`Fabric` 进入，并沿着返回的协议 Builder 完成调用。

## 运行要求

- Java 21+
- Maven 依赖：

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-fabric</artifactId>
</dependency>
```

## 模块边界

| 包 | 职责 |
| --- | --- |
| `org.miaixz.bus.fabric` | `Context`、`Options`、`Call`、`Payload`、`Headers`、`Message`、`Session` 等共享契约。 |
| `org.miaixz.bus.fabric.protocol.http` | HTTP 请求构建、响应处理、缓存接入、下载支持、Cookie 与 HTTP Body 类型。 |
| `org.miaixz.bus.fabric.protocol.socket` | 基于帧编解码的 TCP、TLS Socket、UDP、KCP 会话。 |
| `org.miaixz.bus.fabric.protocol.sse` | Server-Sent Events 流与重连元数据。 |
| `org.miaixz.bus.fabric.protocol.websocket` | WebSocket 连接、消息回调、文本/二进制写入、ping 与 close。 |
| `org.miaixz.bus.fabric.protocol.stomp` | 基于 `ws` 或 `wss` 打开的 STOMP 会话、订阅辅助方法、heart-beat、ack/nack 与发送操作。 |
| `org.miaixz.bus.fabric.network` | Address、DNS、代理、TLS、TCP、UDP、AIO、KCP、连接池等底层网络能力。 |
| `org.miaixz.bus.fabric.guard` | Body、Frame、TLS 与组合守卫。 |
| `org.miaixz.bus.fabric.observe` | 协议生命周期观测与监听器桥接。 |
| `org.miaixz.bus.fabric.cache` | HTTP 缓存编排复用的通用缓存存储实现。 |

## 入口

相关调用需要共享 options、DNS、运行时资源、监听器与连接池状态时，应创建并复用同一个 `Context`。

```java
Context context = Context.create();
```

| 入口 | Builder | 适用场景 |
| --- | --- | --- |
| `Fabric.http(context)` | `HttpX.Builder` | HTTP 与 HTTPS 请求。 |
| `Fabric.socket(context)` | `SocketX.Builder` | TCP、TLS Socket、UDP 与 KCP 会话。 |
| `Fabric.sse(context)` | `SseX.Builder` | Server-Sent Events 流。 |
| `Fabric.websocket(context)` | `WebSocketX.Builder` | WebSocket 会话。 |
| `Fabric.stomp(context)` | `StompX.Builder` | STOMP 会话。 |

每个入口也提供无参重载，会创建默认 `Context`。业务代码建议显式传入 `Context`，这样运行时选项和生命周期策略都能在调用点被看见。

## 基本用法

### HTTP GET

```java
String text = Fabric.http(context)
        .get("https://example.com/search")
        .query("q", "bus")
        .executeText();
```

需要读取状态码、响应头或流式内容时使用 `execute()`。`HttpResponse` 需要在读取完成后关闭。

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

### 表单与文件

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

socket 入口用于协议无关的字节传输。先选择传输方式，再配置帧编解码器，最后通过返回的会话发送 `Payload`。

```java
SocketSession session = Fabric.socket(context)
        .tcp("127.0.0.1", 9000)
        .frame(FrameCodec.line())
        .onText(System.out::println)
        .connect();

session.send(Payload.of("ping", StandardCharsets.UTF_8)).execute();
session.close();
```

KCP 也通过同一个 socket 链路暴露：

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

## 高级用法

### Context 与 Options

`Context` 是不可变对象。需要改变运行时行为时，通过复制后的 `Options` 创建新的 `Context`。

```java
Context limited = context.withOptions(
        context.options().materializeMaxBytes(16L * 1024L * 1024L));

String text = Fabric.http(limited)
        .get("https://example.com/report.txt")
        .executeText();
```

当前协议链路会读取的重要 option key：

| Option key | 期望值 | 使用方 |
| --- | --- | --- |
| `materialize.maxBytes` | `Number` | `Payload`、HTTP Body、Socket 消息、WebSocket 消息、STOMP 消息在一次性读取 bytes/text 时使用。 |
| `http.cache` | `HttpCache` | HTTP 缓存编排。 |
| `http.cookieJar` | `CookieJar` | HTTP 自动加载与保存 Cookie。 |
| `http.tlsSettings` | `TlsSettings` | HTTP TLS 配置。 |
| `http.tlsContext` | `TlsContext` | HTTP TLS 上下文。 |
| `socket.tlsSettings` | `TlsSettings` | TLS Socket 配置。 |
| `socket.tlsContext` | `TlsContext` | TLS Socket 上下文。 |
| `tlsSettings` | `TlsSettings` | 共享 TLS 配置 fallback。 |
| `tlsContext` | `TlsContext` | 共享 TLS 上下文 fallback。 |

### 一次性读取阈值

`bytes()` 与 `text()` 是一次性读取 API，适合小体量内容，并受 `Options.materializeMaxBytes(...)` 保护。
默认阈值为 `64 MiB`。大体量内容应使用流式 API，或在 `Context` 边界显式提高阈值。

### 异步调用

每个协议 Builder 都可以创建 `Call<T>`。`enqueue()` 会在实现支持时启动后台执行；`await()` 返回完成后的结果。

```java
Call<HttpResponse> call = Fabric.http(context)
        .get("https://example.com/items")
        .enqueue();

try (HttpResponse response = call.await(Duration.ofSeconds(5))) {
    String body = response.text();
}
```

### HTTP 缓存

HTTP 缓存通过 `Context` 挂载，HTTP 专用配置使用 `http.cache`。

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

### Cookie

自动 Cookie 是显式开启的能力。共享 `CookieJar` 通过 `http.cookieJar` 挂载。

```java
CookieJar jar = CookieJar.memory();
Context stateful = context.withOptions(context.options().with("http.cookieJar", jar));

Fabric.http(stateful)
        .get("https://example.com/login")
        .executeText();
```

### 断点下载

下载能力由 `HttpDownload` 表达。先通过 HTTP 入口构建 `GET` 请求，再把请求交给下载任务；下载任务负责 range resume、进度、暂停、取消与 sidecar 元数据。

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

### 代理与 TLS

HTTP 代理配置属于 HTTP Builder：

```java
String proxied = Fabric.http(context)
        .get("https://example.com")
        .proxy("http://proxy.local:8080")
        .proxyCredentials("user", "secret")
        .executeText();
```

TLS 配置属于 `Context`。只影响单个协议时使用协议专用 key。

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

TLS Socket 使用 socket 入口和 socket 专用 key：

```java
Context secureSocket = context.withOptions(context.options().with("socket.tlsSettings", tls));

SocketSession session = Fabric.socket(secureSocket)
        .tls("example.com", 443)
        .frame(FrameCodec.line())
        .connect();
```

### 守卫

守卫在协议边界拒绝或限制数据。Frame 守卫适用于流式写入，Body 守卫适用于 HTTP 消息体。多个守卫通过
`GuardChain` 组合。

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

### 观测

可以在 Builder 上挂载 observer 接收协议事件。`EventListenerBridge` 会把 fabric observation marker 映射到
call start/end、DNS、connect、TLS、cache、headers、body、close、failure 等命名回调。

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

### Socket 调优

Socket 参数可以直接配置到 socket Builder。这些参数会进入 socket 运行时，并可从打开后的会话 attributes 中读取。

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

### 协议专用控制

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

## 迁移规则

- 业务入口统一使用 `Fabric.http(context)`、`Fabric.socket(context)`、`Fabric.sse(context)`、
  `Fabric.websocket(context)`、`Fabric.stomp(context)`。
- 业务模块不要直接调用 network connector 类。network 包是协议 Builder 复用的底层实现能力。
- 不恢复历史 HTTP 门面名或 socket plugin API；对应能力进入当前协议 Builder、`Context` options、`guard.*`、
  `observe.*`、`network.proxy.*`、`network.tls.*`。
- 协议能力保持在协议包内。例如 HTTP cache 与 HTTP download 属于 `protocol.http`；通用缓存存储保留在 `cache`。

## 推荐验证

涉及本模块代码变更时，建议执行模块构建与仓库使用的外部 fabric 集成验证。

```bash
mvn -f bus-fabric/pom.xml -DskipTests compile
```

模块 native-image 元数据中的外部测试地址为
`https://github.com/818000/abarth/tree/main/bus-testing/src/test`；集成资源可用时，可使用该外部测试集继续验证。
