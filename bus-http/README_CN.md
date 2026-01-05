# 🌐 Bus HTTP：Java 高性能 HTTP 客户端

<p align="center">
<strong>现代、高效且功能丰富的 HTTP 客户端</strong>
</p>

-----

## 📖 项目简介

**Bus HTTP** 是为 Java 应用程序设计的高性能 HTTP 客户端库。采用现代设计原则构建，提供了简单而强大的 API 用于发起 HTTP 请求，支持同步和异步操作、HTTP/2、WebSocket 以及连接池、缓存和拦截器等高级功能。

-----

## ✨ 核心功能

### 🎯 基础能力

* **简单直观的 API**：清晰、流畅的 API 设计，易于集成
* **高性能**：连接池和复用实现最佳资源利用
* **HTTP/2 支持**：完全支持 HTTP/2 协议，支持多路复用
* **WebSocket 支持**：内置 WebSocket 客户端，用于实时通信
* **同步与异步**：支持阻塞和非阻塞请求
* **灵活配置**：超时、代理、SSL/TLS 等广泛的自定义选项

### ⚡ 高级功能

| 功能 | 好处 | 描述 |
| :--- | :--- | :--- |
| **连接池** | $\text{性能 } \uparrow$ | 自动连接复用减少延迟 |
| **HTTP/2 多路复用** | $\text{并发 } \uparrow$ | 单一连接上的多个请求 |
| **响应缓存** | $\text{网络 } \downarrow$ | 可配置缓存减少冗余调用 |
| **拦截器** | $\text{灵活性 } \uparrow$ | 全局转换请求和响应 |
| **自动 GZIP** | $\text{带宽 } \downarrow$ | 透明压缩/解压 |

### 🛡️ 安全与可靠性

* **SSL/TLS 支持**：可自定义 SSL 上下文和信任管理器
* **证书锁定**：通过证书固定增强安全性
* **代理支持**：HTTP、SOCKS 代理配置
* **自动重试**：可配置的失败请求重试机制
* **连接超时**：全面的超时控制

### 🌍 协议支持

**HTTP 协议**：HTTP/1.1、HTTP/2

**特殊协议**：WebSocket、STOMP

**内容类型**：JSON、XML、表单数据、Multipart、流、文本/二进制

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-http</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 基础用法

#### 1. 简单 GET 请求

```java
import org.miaixz.bus.http.Httpx;

// 简单 GET 请求
String response = Httpx.get("https://api.example.com/users");
System.out.println(response);

// 带请求头的 GET
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token123");
String response = Httpx.get("https://api.example.com/users", headers);
```

#### 2. POST JSON 请求

```java
// POST JSON 数据
String json = "{\"name\":\"张三\",\"age\":30}";
String response = Httpx.post("https://api.example.com/users", json);

// 带自定义请求头的 POST
Map<String, String> headers = new HashMap<>();
headers.put("Content-Type", "application/json");
headers.put("Authorization", "Bearer token123");
String response = Httpx.post("https://api.example.com/users", json, headers);
```

#### 3. PUT 和 DELETE 请求

```java
// PUT 请求
String json = "{\"id\":1,\"name\":\"更新的名称\"}";
String response = Httpx.put("https://api.example.com/users/1", json);

// DELETE 请求
String response = Httpx.delete("https://api.example.com/users/1");

// HEAD 请求
Map<String, String> headers = Httpx.head("https://api.example.com/users");
```

-----

## 📝 使用示例

### 1. 使用 Httpd（高级客户端）

#### 创建自定义配置客户端

```java
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.Builder;

// 创建自定义超时客户端
Httpd client = new Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

// 执行请求
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .get()
    .build();

Response response = client.newCall(request).execute();
String body = response.body().string();
```

#### 连接池配置

```java
// 配置连接池
Httpd client = new Builder()
    .connectionPool(new ConnectionPool(
        10,                    // 最大空闲连接数
        5,                     // 保持活跃时长（分钟）
        TimeUnit.MINUTES
    ))
    .build();
```

### 2. 文件上传（Multipart）

```java
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.RequestBody;
import org.miaixz.bus.http.MultipartBody;

// 上传带表单数据的文件
RequestBody requestBody = new MultipartBody.Builder()
    .setType(MultipartBody.FORM)
    .addFormDataPart("title", "我的文件")
    .addFormDataPart("description", "文件描述")
    .addFormDataPart("file",
        "filename.txt",
        RequestBody.create(
            MediaType.parse("text/plain"),
            new File("path/to/file.txt")
        )
    )
    .build();

Request request = new Request.Builder()
    .url("https://api.example.com/upload")
    .post(requestBody)
    .build();

Response response = client.newCall(request).execute();
```

### 3. 表单数据提交

```java
import org.miaixz.bus.http.FormBody;
import org.miaixz.bus.http.RequestBody;

// 构建表单体
RequestBody formBody = new FormBody.Builder()
    .add("username", "zhang_san")
    .add("password", "secret123")
    .add("remember", "true")
    .build();

Request request = new Request.Builder()
    .url("https://api.example.com/login")
    .post(formBody)
    .build();

Response response = client.newCall(request).execute();
```

### 4. 异步请求

```java
import org.miaixz.bus.http.Callback;

// 异步 GET 请求
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        System.err.println("请求失败：" + e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            String body = response.body().string();
            System.out.println("响应：" + body);
        } finally {
            response.close();
        }
    }
});
```

### 5. WebSocket 连接

```java
import org.miaixz.bus.http.WebSocket;
import org.miaixz.bus.http.WebSocketListener;
import org.miaixz.bus.http.Request;

// 创建 WebSocket 请求
Request request = new Request.Builder()
    .url("wss://echo.websocket.org")
    .build();

// 创建 WebSocket 监听器
WebSocketListener listener = new WebSocketListener() {
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        System.out.println("WebSocket 已连接");
        webSocket.send("你好，WebSocket！");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("收到：" + text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        System.out.println("关闭中：" + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.err.println("错误：" + t.getMessage());
    }
};

// 创建 WebSocket 连接
WebSocket ws = client.newWebSocket(request, listener);
```

### 6. 使用拦截器

```java
import org.miaixz.bus.http.Interceptor;

// 日志拦截器
Interceptor loggingInterceptor = chain -> {
    Request request = chain.request();

    long startTime = System.nanoTime();
    System.out.println(String.format("发送请求 %s 到 %s",
        request.url(), chain.connection()));

    Response response = chain.proceed(request);

    long endTime = System.nanoTime();
    System.out.println(String.format("收到响应，耗时 %.1fms",
        (endTime - startTime) / 1e6d));

    return response;
};

// 添加拦截器到客户端
Httpd client = new Builder()
    .addInterceptor(loggingInterceptor)
    .build();
```

### 7. 自定义请求头和认证

```java
// 带自定义请求头的请求
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Authorization", "Bearer token123")
    .header("User-Agent", "MyApp/1.0")
    .header("Accept", "application/json")
    .get()
    .build();

// 基本认证
String credentials = Credentials.basic("username", "password");
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Authorization", credentials)
    .build();
```

### 8. 响应处理

```java
Response response = client.newCall(request).execute();

// 检查响应码
if (!response.isSuccessful()) {
    throw new IOException("意外的代码 " + response);
}

// 获取响应头
Headers headers = response.headers();
String contentType = headers.get("Content-Type");

// 获取响应体为字符串
String responseBody = response.body().string();

// 获取响应体为字节数组
byte[] bytes = response.body().bytes();

// 获取响应体为流
InputStream inputStream = response.body().byteStream();

// 始终关闭响应
response.close();
```

### 9. 缓存配置

```java
// 配置缓存（10 MB 缓存大小）
Cache cache = new Cache(
    new File("cache_directory"),
    10 * 1024 * 1024  // 10 MB
);

Httpd client = new Builder()
    .cache(cache)
    .build();

// 带缓存控制的请求
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Cache-Control", "max-stale=3600")
    .build();
```

### 10. 代理配置

```java
// HTTP 代理
Proxy proxy = new Proxy(Proxy.Type.HTTP,
    new InetSocketAddress("proxy.example.com", 8080));

Httpd client = new Builder()
    .proxy(proxy)
    .proxyAuthenticator(new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            String credential = Credentials.basic("username", "password");
            return response.request().newBuilder()
                .header("Proxy-Authorization", credential)
                .build();
        }
    })
    .build();
```

-----

## 🔧 配置选项

### Httpd 客户端配置

```java
Httpd client = new Builder()
    // 超时
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)

    // 连接池
    .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))

    // 拦截器
    .addInterceptor(loggingInterceptor)
    .addNetworkInterceptor(networkInterceptor)

    // 重试配置
    .retryOnConnectionFailure(true)

    // 跟随重定向
    .followRedirects(true)
    .followSslRedirects(true)

    // 协议
    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))

    // 缓存
    .cache(new Cache(cacheDir, 10 * 1024 * 1024))

    // 代理
    .proxy(proxy)
    .proxyAuthenticator(proxyAuthenticator)

    // SSL/TLS
    .sslSocketFactory(sslSocketFactory, trustManager)
    .hostnameVerifier(hostnameVerifier)

    // 调度器（用于异步请求）
    .dispatcher(new Dispatcher(executorService))

    .build();
```

-----

## 💡 最佳实践

### 1. 复用 Httpd 实例

```java
// ✅ 推荐：创建单个共享实例
private static final Httpd HTTP_CLIENT = new Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .build();

// ❌ 不推荐：为每个请求创建新实例
Httpd client = new Builder().build();  // 浪费资源
```

### 2. 始终关闭响应

```java
// ✅ 推荐：使用 try-with-resources
try (Response response = client.newCall(request).execute()) {
    String body = response.body().string();
    // 处理响应
}

// ❌ 不推荐：不关闭响应
Response response = client.newCall(request).execute();
String body = response.body().string();
// 响应体未关闭 - 可能导致内存泄漏
```

### 3. 正确处理响应体

```java
// ✅ 推荐：响应体只能消费一次
try (Response response = client.newCall(request).execute()) {
    String body = response.body().string();
    // 不能再次调用 response.body().string()
}

// ❌ 不推荐：多次消费响应体
String body1 = response.body().string();
String body2 = response.body().string();  // 抛出 IllegalStateException
```

### 4. 配置适当的超时

```java
// ✅ 推荐：为用例设置适当的超时
Httpd client = new Builder()
    .connectTimeout(10, TimeUnit.SECONDS)   // 连接建立
    .readTimeout(30, TimeUnit.SECONDS)      // 读取响应体
    .writeTimeout(30, TimeUnit.SECONDS)     // 发送请求体
    .build();
```

### 5. 使用连接池

```java
// ✅ 推荐：为高吞吐量场景配置连接池
Httpd client = new Builder()
    .connectionPool(new ConnectionPool(
        20,                    // 最大空闲连接数
        5,                     // 保持活跃时长
        TimeUnit.MINUTES
    ))
    .build();
```

-----

## ❓ 常见问题

### Q1: 如何启用请求/响应日志记录？

```java
// 添加日志拦截器
Httpd client = new Builder()
    .addInterceptor(chain -> {
        Request request = chain.request();
        System.out.println("请求：" + request.url());

        Response response = chain.proceed(request);
        System.out.println("响应：" + response.code());
        return response;
    })
    .build();
```

### Q2: 如何处理 SSL/TLS 错误？

```java
// 信任所有证书（仅用于开发！）
X509TrustManager trustAllCerts = new X509TrustManager() {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
};

SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(null, new TrustManager[]{trustAllCerts}, new SecureRandom());

Httpd client = new Builder()
    .sslSocketFactory(sslContext.getSocketFactory(), trustAllCerts)
    .hostnameVerifier((hostname, session) -> true)
    .build();
```

### Q3: 如何取消正在运行的请求？

```java
// 同步请求（中断线程）
Call call = client.newCall(request);
try {
    Response response = call.execute();
} catch (IOException e) {
    if (call.isCanceled()) {
        System.out.println("请求已取消");
    }
}

// 异步请求
Call call = client.newCall(request);
call.enqueue(callback);
// 稍后取消
call.cancel();
```

### Q4: 如何上传/下载大文件？

```java
// 以流形式上传大文件
RequestBody requestBody = new RequestBody() {
    @Override
    public MediaType contentType() {
        return MediaType.parse("application/octet-stream");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (Source source = Okio.source(file)) {
            sink.writeAll(source);
        }
    }
};

// 下载大文件
Response response = client.newCall(request).execute();
try (InputStream inputStream = response.body().byteStream();
     OutputStream outputStream = new FileOutputStream(file)) {
    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
    }
}
```

### Q5: 如何实现重试逻辑？

```java
// 添加重试拦截器
Interceptor retryInterceptor = chain -> {
    Request request = chain.request();
    Response response = null;
    IOException exception = null;

    int retryCount = 0;
    int maxRetries = 3;

    while (retryCount < maxRetries) {
        try {
            response = chain.proceed(request);
            if (response.isSuccessful()) {
                return response;
            }
        } catch (IOException e) {
            exception = e;
        }
        retryCount++;
    }

    if (exception != null) {
        throw exception;
    }
    return response;
};

Httpd client = new Builder()
    .addInterceptor(retryInterceptor)
    .build();
```

-----

## 📊 API 参考

### HTTP 方法

| 方法 | 描述 |
| :--- | :--- |
| `GET` | 获取数据 |
| `POST` | 提交数据 |
| `PUT` | 更新数据 |
| `DELETE` | 删除数据 |
| `HEAD` | 仅获取请求头 |
| `PATCH` | 部分更新 |
| `OPTIONS` | 获取允许的方法 |

### 主要类

| 类 | 描述 |
| :--- | :--- |
| `Httpd` | 核心 HTTP 客户端 |
| `Httpx` | 带静态方法的简化 HTTP 客户端 |
| `Httpv` | 高级流畅 API 客户端 |
| `Request` | HTTP 请求对象 |
| `Response` | HTTP 响应对象 |
| `WebSocket` | WebSocket 客户端 |
| `Call` | 请求执行接口 |

### 请求体类型

| 类型 | 描述 |
| :--- | :--- |
| `RequestBody` | 基础请求体 |
| `FormBody` | URL 编码的表单数据 |
| `MultipartBody` | Multipart/form-data 上传 |

-----

## 🔄 版本兼容性

| Bus HTTP 版本 | JDK 版本 | HTTP 协议 |
| :--- | :--- | :--- |
| 8.x | 17+ | HTTP/1.1、HTTP/2 |

-----

## 🔗 相关模块

- [bus-core](../bus-core) - 核心工具和数据结构
- [bus-logger](../bus-logger) - 日志集成
- [bus-crypto](../bus-crypto) - HTTPS 加密支持
