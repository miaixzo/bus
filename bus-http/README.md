# 🌐 Bus HTTP: High-Performance HTTP Client for Java

<p align="center">
<strong>Modern, Efficient, and Feature-Rich HTTP Client</strong>
</p>

-----

## 📖 Project Introduction

**Bus HTTP** is a high-performance HTTP client library for Java applications. Built with modern design principles, it provides a simple yet powerful API for making HTTP requests, supporting both synchronous and asynchronous operations, HTTP/2, WebSockets, and advanced features like connection pooling, caching, and interceptors.

-----

## ✨ Core Features

### 🎯 Basic Capabilities

* **Simple and Intuitive API**: Clean, fluent API design for easy integration
* **High Performance**: Connection pooling and reuse for optimal resource utilization
* **HTTP/2 Support**: Full support for HTTP/2 protocol with multiplexing
* **WebSocket Support**: Built-in WebSocket client for real-time communication
* **Synchronous & Asynchronous**: Support for both blocking and non-blocking requests
* **Flexible Configuration**: Extensive customization options for timeouts, proxies, SSL/TLS

### ⚡ Advanced Features

| Feature | Benefit | Description |
| :--- | :--- | :--- |
| **Connection Pool** | $\text{Performance } \uparrow$ | Automatic connection reuse reduces latency |
| **HTTP/2 Multiplexing** | $\text{Concurrency } \uparrow$ | Multiple requests over single connection |
| **Response Caching** | $\text{Network } \downarrow$ | Configurable cache reduces redundant calls |
| **Interceptors** | $\text{Flexibility } \uparrow$ | Transform requests and responses globally |
| **Automatic GZIP** | $\text{Bandwidth } \downarrow$ | Transparent compression/decompression |

### 🛡️ Security & Reliability

* **SSL/TLS Support**: Customizable SSL context and trust managers
* **Certificate Pinning**: Enhanced security with certificate pinning
* **Proxy Support**: HTTP, SOCKS proxy configurations
* **Automatic Retries**: Configurable retry mechanism for failed requests
* **Connection Timeout**: Comprehensive timeout controls

### 🌍 Protocol Support

**HTTP Protocols**: HTTP/1.1, HTTP/2

**Special Protocols**: WebSocket, STOMP

**Content Types**: JSON, XML, Form Data, Multipart, Stream, Text/Binary

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-http</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Basic Usage

#### 1. Simple GET Request

```java
import org.miaixz.bus.http.Httpx;

// Simple GET request
String response = Httpx.get("https://api.example.com/users");
System.out.println(response);

// GET with headers
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token123");
String response = Httpx.get("https://api.example.com/users", headers);
```

#### 2. POST Request with JSON

```java
// POST JSON data
String json = "{\"name\":\"John\",\"age\":30}";
String response = Httpx.post("https://api.example.com/users", json);

// POST with custom headers
Map<String, String> headers = new HashMap<>();
headers.put("Content-Type", "application/json");
headers.put("Authorization", "Bearer token123");
String response = Httpx.post("https://api.example.com/users", json, headers);
```

#### 3. PUT and DELETE Requests

```java
// PUT request
String json = "{\"id\":1,\"name\":\"Updated Name\"}";
String response = Httpx.put("https://api.example.com/users/1", json);

// DELETE request
String response = Httpx.delete("https://api.example.com/users/1");

// HEAD request
Map<String, String> headers = Httpx.head("https://api.example.com/users");
```

-----

## 📝 Usage Examples

### 1. Using Httpd (Advanced Client)

#### Create Client with Custom Configuration

```java
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.Builder;

// Create client with custom timeouts
Httpd client = new Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

// Execute request
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .get()
    .build();

Response response = client.newCall(request).execute();
String body = response.body().string();
```

#### Connection Pool Configuration

```java
// Configure connection pool
Httpd client = new Builder()
    .connectionPool(new ConnectionPool(
        10,                    // Max idle connections
        5,                     // Keep alive duration (minutes)
        TimeUnit.MINUTES
    ))
    .build();
```

### 2. File Upload (Multipart)

```java
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.RequestBody;
import org.miaixz.bus.http.MultipartBody;

// Upload file with form data
RequestBody requestBody = new MultipartBody.Builder()
    .setType(MultipartBody.FORM)
    .addFormDataPart("title", "My File")
    .addFormDataPart("description", "File description")
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

### 3. Form Data Submission

```java
import org.miaixz.bus.http.FormBody;
import org.miaixz.bus.http.RequestBody;

// Build form body
RequestBody formBody = new FormBody.Builder()
    .add("username", "john_doe")
    .add("password", "secret123")
    .add("remember", "true")
    .build();

Request request = new Request.Builder()
    .url("https://api.example.com/login")
    .post(formBody)
    .build();

Response response = client.newCall(request).execute();
```

### 4. Asynchronous Requests

```java
import org.miaixz.bus.http.Callback;

// Asynchronous GET request
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        System.err.println("Request failed: " + e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            String body = response.body().string();
            System.out.println("Response: " + body);
        } finally {
            response.close();
        }
    }
});
```

### 5. WebSocket Connection

```java
import org.miaixz.bus.http.WebSocket;
import org.miaixz.bus.http.WebSocketListener;
import org.miaixz.bus.http.Request;

// Create WebSocket request
Request request = new Request.Builder()
    .url("wss://echo.websocket.org")
    .build();

// Create WebSocket listener
WebSocketListener listener = new WebSocketListener() {
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        System.out.println("WebSocket connected");
        webSocket.send("Hello, WebSocket!");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("Received: " + text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        System.out.println("Closing: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.err.println("Error: " + t.getMessage());
    }
};

// Create WebSocket connection
WebSocket ws = client.newWebSocket(request, listener);
```

### 6. Using Interceptors

```java
import org.miaixz.bus.http.Interceptor;

// Logging interceptor
Interceptor loggingInterceptor = chain -> {
    Request request = chain.request();

    long startTime = System.nanoTime();
    System.out.println(String.format("Sending request %s on %s",
        request.url(), chain.connection()));

    Response response = chain.proceed(request);

    long endTime = System.nanoTime();
    System.out.println(String.format("Received response in %.1fms",
        (endTime - startTime) / 1e6d));

    return response;
};

// Add interceptor to client
Httpd client = new Builder()
    .addInterceptor(loggingInterceptor)
    .build();
```

### 7. Custom Headers and Authentication

```java
// Request with custom headers
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Authorization", "Bearer token123")
    .header("User-Agent", "MyApp/1.0")
    .header("Accept", "application/json")
    .get()
    .build();

// Basic Authentication
String credentials = Credentials.basic("username", "password");
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Authorization", credentials)
    .build();
```

### 8. Response Handling

```java
Response response = client.newCall(request).execute();

// Check response code
if (!response.isSuccessful()) {
    throw new IOException("Unexpected code " + response);
}

// Get response headers
Headers headers = response.headers();
String contentType = headers.get("Content-Type");

// Get response body as string
String responseBody = response.body().string();

// Get response body as bytes
byte[] bytes = response.body().bytes();

// Get response body as stream
InputStream inputStream = response.body().byteStream();

// Always close the response
response.close();
```

### 9. Caching Configuration

```java
// Configure cache (10 MB cache size)
Cache cache = new Cache(
    new File("cache_directory"),
    10 * 1024 * 1024  // 10 MB
);

Httpd client = new Builder()
    .cache(cache)
    .build();

// Request with cache control
Request request = new Request.Builder()
    .url("https://api.example.com/data")
    .header("Cache-Control", "max-stale=3600")
    .build();
```

### 10. Proxy Configuration

```java
// HTTP proxy
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

## 🔧 Configuration Options

### Httpd Client Configuration

```java
Httpd client = new Builder()
    // Timeouts
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)

    // Connection pool
    .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))

    // Interceptors
    .addInterceptor(loggingInterceptor)
    .addNetworkInterceptor(networkInterceptor)

    // Retry configuration
    .retryOnConnectionFailure(true)

    // Follow redirects
    .followRedirects(true)
    .followSslRedirects(true)

    // Protocols
    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))

    // Cache
    .cache(new Cache(cacheDir, 10 * 1024 * 1024))

    // Proxy
    .proxy(proxy)
    .proxyAuthenticator(proxyAuthenticator)

    // SSL/TLS
    .sslSocketFactory(sslSocketFactory, trustManager)
    .hostnameVerifier(hostnameVerifier)

    // Dispatcher (for async requests)
    .dispatcher(new Dispatcher(executorService))

    .build();
```

-----

## 💡 Best Practices

### 1. Reuse Httpd Instances

```java
// ✅ Recommended: Create a single shared instance
private static final Httpd HTTP_CLIENT = new Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .build();

// ❌ Not Recommended: Creating new instances for each request
Httpd client = new Builder().build();  // Wastes resources
```

### 2. Always Close Responses

```java
// ✅ Recommended: Use try-with-resources
try (Response response = client.newCall(request).execute()) {
    String body = response.body().string();
    // Process response
}

// ❌ Not Recommended: Not closing the response
Response response = client.newCall(request).execute();
String body = response.body().string();
// Response body not closed - potential memory leak
```

### 3. Handle Response Body Properly

```java
// ✅ Recommended: Response body can only be consumed once
try (Response response = client.newCall(request).execute()) {
    String body = response.body().string();
    // Cannot call response.body().string() again
}

// ❌ Not Recommended: Consuming body multiple times
String body1 = response.body().string();
String body2 = response.body().string();  // Throws IllegalStateException
```

### 4. Configure Appropriate Timeouts

```java
// ✅ Recommended: Set appropriate timeouts for your use case
Httpd client = new Builder()
    .connectTimeout(10, TimeUnit.SECONDS)   // Connection establishment
    .readTimeout(30, TimeUnit.SECONDS)      // Reading response body
    .writeTimeout(30, TimeUnit.SECONDS)     // Sending request body
    .build();
```

### 5. Use Connection Pooling

```java
// ✅ Recommended: Configure connection pool for high-throughput scenarios
Httpd client = new Builder()
    .connectionPool(new ConnectionPool(
        20,                    // Max idle connections
        5,                     // Keep alive duration
        TimeUnit.MINUTES
    ))
    .build();
```

-----

## ❓ Frequently Asked Questions

### Q1: How to enable request/response logging?

```java
// Add logging interceptor
Httpd client = new Builder()
    .addInterceptor(chain -> {
        Request request = chain.request();
        System.out.println("Request: " + request.url());

        Response response = chain.proceed(request);
        System.out.println("Response: " + response.code());
        return response;
    })
    .build();
```

### Q2: How to handle SSL/TLS errors?

```java
// Trust all certificates (for development only!)
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

### Q3: How to cancel a running request?

```java
// Synchronous request (interrupt thread)
Call call = client.newCall(request);
try {
    Response response = call.execute();
} catch (IOException e) {
    if (call.isCanceled()) {
        System.out.println("Request was canceled");
    }
}

// Asynchronous request
Call call = client.newCall(request);
call.enqueue(callback);
// Cancel later
call.cancel();
```

### Q4: How to upload/download large files?

```java
// Upload large file as stream
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

// Download large file
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

### Q5: How to implement retry logic?

```java
// Add retry interceptor
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

## 📊 API Reference

### HTTP Methods

| Method | Description |
| :--- | :--- |
| `GET` | Retrieve data |
| `POST` | Submit data |
| `PUT` | Update data |
| `DELETE` | Delete data |
| `HEAD` | Get headers only |
| `PATCH` | Partial update |
| `OPTIONS` | Get allowed methods |

### Main Classes

| Class | Description |
| :--- | :--- |
| `Httpd` | Core HTTP client |
| `Httpx` | Simplified HTTP client with static methods |
| `Httpv` | High-level fluent API client |
| `Request` | HTTP request object |
| `Response` | HTTP response object |
| `WebSocket` | WebSocket client |
| `Call` | Request execution interface |

### Request Body Types

| Type | Description |
| :--- | :--- |
| `RequestBody` | Base request body |
| `FormBody` | URL-encoded form data |
| `MultipartBody` | Multipart/form-data uploads |

-----

## 🔄 Version Compatibility

| Bus HTTP Version | JDK Version | HTTP Protocol |
| :--- | :--- | :--- |
| 8.x | 17+ | HTTP/1.1, HTTP/2 |

-----

## 🔗 Related Modules

- [bus-core](../bus-core) - Core utilities and data structures
- [bus-logger](../bus-logger) - Logging integration
- [bus-crypto](../bus-crypto) - Cryptography support for HTTPS
