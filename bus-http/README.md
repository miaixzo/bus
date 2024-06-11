#### 项目说明

HTTP是现代应用常用的一种交换数据和媒体的网络方式，高效地使用HTTP能让资源加载更快，节省带宽。高效的HTTP客户端，它有以下默认特性：

支持HTTP/2，允许所有同一个主机地址的请求共享同一个socket连接 连接池减少请求延时 透明的GZIP压缩减少响应数据的大小
缓存响应内容，避免一些完全重复的请求
当网络出现问题的时候依然坚守自己的职责，它会自动恢复一般的连接问题，如果你的服务有多个IP地址，当第一个IP请求失败时，会交替尝试你配置的其他IP，使用现代TLS技术(
SNI, ALPN)初始化新的连接，当握手失败时会回退到TLS 1.0。

### Httpd 使用

1.1. 异步GET请求 -new Httpd; -构造Request对象； -通过前两步中的对象构建Call对象； -通过Call#enqueue(Callback)方法来提交异步请求；

```java
    String url = "http://wwww.baidu.com";
Httpd httpd = new Httpd();
final Request request = new Request.Builder()
        .url(url)
        .get()//默认就是GET请求，可以不写
        .build();
NewCall call = httpd.newCall(request);

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){
        Logger.info("onFailure: ");
    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        Logger.info("onResponse: " + delegate.body().string());
    }
});
```

1.2. 同步GET请求 前面几个步骤和异步方式一样，只是最后一部是通过 NewCall#execute()
来提交请求，注意这种方式会阻塞调用线程，所以在Android中应放在子线程中执行，否则有可能引起ANR异常，Android3.0
以后已经不允许在主线程访问网络。

```java
    String url = "http://wwww.baidu.com";
Httpd httpd = new Httpd();
final Request request = new Request.Builder()
        .url(url)
        .build();
final NewCall call = httpd.newCall(request);

Thread(new Runnable() {
    @Override
    public void run () {
        try {
            Response delegate = call.execute();
            Logger.info("run: " + delegate.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}).

start();
``` 

2.1. POST方式提交String 这种方式与前面的区别就是在构造Request对象时，需要多构造一个RequestBody对象，用它来携带我们要提交的数据。在构造
RequestBody 需要指定MediaType，用于描述请求/响应
body 的内容类型，关于 MediaType 的更多信息可以查看 RFC 2045，RequstBody的几种构造方式：

```java
    MediaType mediaType = MediaType.valueOf("text/x-markdown; charsets=utf-8");
String requestBody = "I am Jdqm.";
Request request = new Request.Builder()
        .url("https://api.github.com/markdown/raw")
        .post(RequestBody.create(mediaType, requestBody))
        .build();
Httpd httpd = new Httpd();

newCall(request)

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){
        Logger.info("onFailure: " + e.getMessage());
    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        Logger.info(delegate.protocol() + " " + delegate.code() + " " + delegate.message());
        Headers headers = delegate.headers();
        for (int i = 0; i < headers.size(); i++) {
            Logger.info(headers.name(i) + ":" + headers.value(i));
        }
        Logger.info("onResponse: " + delegate.body().string());
    }
});
``` 

响应内容

```text
    http/1.1 200 OK 
    Date:Sat, 10 Mar 2018 05:23:20 GMT 
    Content-Type:text/html;charsets=utf-8
    Content-Length:18
    Server:GitHub.com 
    Status:200 OK 
    X-RateLimit-Limit:60
    X-RateLimit-Remaining:52
    X-RateLimit-Reset:1520661052
    X-CommonMarker-Version:0.17.4
    Access-Control-Expose-Headers:ETag, Link, Retry-After, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval
    
    Access-Control-Allow-Origin:*
    Content-Security-Policy:default-src 'none'
    Strict-Transport-Security:max-age=31536000; includeSubdomains; preload 
    X-Content-Type-Options:nosniff 
    X-Frame-Options:deny 
    X-XSS-Protection:1; mode=block 
    X-Runtime-rack:0.019668
    Vary:Accept-Encoding 
    X-GitHub-Request-Id:1474:20A83:5CC0B6:7A7C1B:5AA36BC8 
    onResponse: <p>I am Jdqm.</p>
```

2.2 POST方式提交流

```java
    RequestBody requestBody = new RequestBody() {

    @Override
    public MediaType mediaType() {
        return MediaType.valueOf("text/x-markdown; charsets=utf-8");
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        sink.writeUtf8("I am Jdqm.");
    }
};

Request request = new Request.Builder()
        .url("https://api.github.com/markdown/raw")
        .post(requestBody)
        .build();
Httpd httpd = new Httpd();
    httpd.

newCall(request).

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){
        Logger.info("onFailure: " + e.getMessage());
    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        Logger.info(delegate.protocol() + " " + delegate.code() + " " + delegate.message());
        Headers headers = delegate.headers();
        for (int i = 0; i < headers.size(); i++) {
            Logger.info(headers.name(i) + ":" + headers.value(i));
        }
        Logger.info("onResponse: " + delegate.body().string());
    }
});
```

2.3. POST提交文件

```java
    MediaType mediaType = MediaType.valueOf("text/x-markdown; charsets=utf-8");
Httpd httpd = new Httpd();
File file = new File("test.md");
Request request = new Request.Builder()
        .url("https://api.github.com/markdown/raw")
        .post(RequestBody.create(mediaType, file))
        .build();
    httpd.

newCall(request).

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){
        Logger.info("onFailure: " + e.getMessage());
    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        Logger.info(delegate.protocol() + " " + delegate.code() + " " + delegate.message());
        Headers headers = delegate.headers();
        for (int i = 0; i < headers.size(); i++) {
            Logger.info(headers.name(i) + ":" + headers.value(i));
        }
        Logger.info("onResponse: " + delegate.body().string());
    }
});
```

2.4. POST方式提交表单

```java
    Httpd httpd = new Httpd();
RequestBody requestBody = new FormBody.Builder()
        .add("search", "Jurassic Park")
        .build();
Request request = new Request.Builder()
        .url("https://en.wikipedia.org/w/index.php")
        .post(requestBody)
        .build();
    
    httpd.

newCall(request).

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){
        Logger.info("onFailure: " + e.getMessage());
    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        Logger.info(delegate.protocol() + " " + delegate.code() + " " + delegate.message());
        Headers headers = delegate.headers();
        for (int i = 0; i < headers.size(); i++) {
            Logger.info(headers.name(i) + ":" + headers.value(i));
        }
        Logger.info("onResponse: " + delegate.body().string());
    }
});
```

2.5. POST方式提交分块请求 MultipartBody 可以构建复杂的请求体，与HTML文件上传形式兼容。多块请求体中每块请求都是一个请求体，可以定义自己的请求头。这些请求头可以用来描述这块请求，例如它的
Content-Disposition 。如果 Content-Length 和 Content-Type 可用的话，他们会被自动添加到请求头中

```java
    Httpd client = new Httpd();
MultipartBody body = new MultipartBody.Builder("AaB03x")
        .setType(MediaType.MULTIPART_FORM_DATA_TYPE)
        .addPart(
                Headers.of("Content-Disposition", "form-data; name=\"title\""),
                RequestBody.create(null, "Square Logo"))
        .addPart(
                Headers.of("Content-Disposition", "form-data; name=\"image\""),
                RequestBody.create(MediaType.valueOf("image/png"), new File("website/static/logo-square.png")))
        .build();

Request request = new Request.Builder()
        .header("Authorization", "Client-ID " + "...")
        .url("https://api.imgur.com/3/image")
        .post(body)
        .build();

NewCall call = client.newCall(request);
    call.

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){

    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        Logger.info(delegate.body().string());

    }

});
```

3.1. 拦截器 Httpd的拦截器链可谓是其整个框架的精髓，用户可传入的 interceptor 分为两类： ①一类是全局的 interceptor，该类
interceptor 在整个拦截器链中最早被调用，通过
Httpd.Builder#addInterceptor(Interceptor) 传入； ②另外一类是非网页请求的 interceptor
，这类拦截器只会在非网页请求中被调用，并且是在组装完请求之后，真正发起网络请求前被调用，所有的
interceptor 被保存在 List<Interceptor> interceptors 集合中，按照添加顺序来逐个调用，具体可参考
RealCall#getResponseWithInterceptorChain() 方法。通过
Httpd.Builder#addNetworkInterceptor(Interceptor) 传入；

这里举一个简单的示例，例如有这样一个需求，我要监控App通过 Httpd 发出的所有原始请求，以及整个请求所耗费的时间，针对这样的需求就可以使用第一类全局的
interceptor 在拦截器链头去做。

```java
    public class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long startTime = System.nanoTime();
        Logger.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response delegate = chain.proceed(request);

        long endTime = System.nanoTime();
        Logger.info(String.format("Received delegate for %s in %.1fms%n%s",
                delegate.request().url(), (endTime - startTime) / 1e6d, delegate.headers()));

        return delegate;
    }
}
```

```java
    Httpd httpd = new Httpd.Builder()
        .addInterceptor(new LoggingInterceptor())
        .build();
Request request = new Request.Builder()
        .url("http://www.publicobject.com/helloworld.txt")
        .header("User-Agent", "Httpd Example")
        .build();
    httpd.

newCall(request).

enqueue(new Callback() {
    @Override
    public void onFailure (NewCall call, IOException e){
        Logger.info("onFailure: " + e.getMessage());
    }

    @Override
    public void onResponse (NewCall call, Response delegate) throws IOException {
        ResponseBody body = delegate.body();
        if (null != body) {
            Logger.info("onResponse: " + delegate.body().string());
            body.close();
        }
    }
});
```

针对这个请求，打印出来的结果

```text
    Sending request http://www.publicobject.com/helloworld.txt on null
    User-Agent: Httpd Example
            
    Received delegate for https://publicobject.com/helloworld.txt in 1265.9ms
    Server: nginx/1.10.0 (Ubuntu)
    Date: Wed, 28 Mar 2018 08:19:48 GMT
    Content-Type: text/plain
    Content-Length: 1759
    Last-Modified: Tue, 27 May 2014 02:35:47 GMT
    Connection: keep-alive
    ETag: "5383fa03-6df"
    Accept-Ranges: bytes
```

注意到一点是这个请求做了重定向，原始的 request url 是 http://www.publicobject.com/helloworld.tx，而响应的 request url
是 https://publicobject.com/helloworld.txt，这说明一定发生了重定向，但是做了几次重定向其实我们这里是不知道的，要知道这些的话，可以使用
addNetworkInterceptor()去做。更多的关于
interceptor的使用以及它们各自的优缺点

## 其他

1. 推荐让 Httpd 保持单例，用同一个 Httpd 实例来执行你的所有请求，因为每一个 Httpd
   实例都拥有自己的连接池和线程池，重用这些资源可以减少延时和节省资源，如果为每个请求创建一个 Httpd
   实例，显然就是一种资源的浪费。当然，也可以使用如下的方式来创建一个新的 Httpd 实例，它们共享连接池、线程池和配置信息。

```java
    Httpd client = Httpd.newBuilder()
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build();
Response delegate = client.newCall(request).execute();
```

2. 每一个Call(其实现是RealCall)只能执行一次，否则会报异常，具体参见 RealCall#execute()

### Httpv 使用

- 支持URL 占位符
- 支持Lambda 回调
- 支持JSON自动封装解析
- 支持异步预处理器
- 支持回调执行器
- 支持全局监听器
- 支持回调阻断机制
- 支持过程控制
- 支持进度监听


* `sync(String url)`   开始一个同步 Http 请求
* `async(String url)`  开始一个异步 Http 请求
* `webSocket(String url)`  开始一个 WebSocket 连接
* `cancel(String tag)` 按标签取消（同步 | 异步 | WebSocket）连接
* `cancelAll()`        取消所有（同步 | 异步 | WebSocket）连接
* `request(Request request)`  Httpv 原生 HTTP 请求
* `webSocket(Request request, WebSocketListener listener)` Httpv 原生 WebSocket 连接
* `newBuilder()`       用于重新构建一个 Httpv 实例

```java
Httpv http = Httpv.builder()
        .baseUrl("http://api.example.com")
        .addMsgConvertor(new GsonMsgConvertor())
        .build();
```

### 同步请求

使用方法`sync(String url)`开始一个同步请求：

```java
List<User> users = http.sync("/users") // http://api.example.com/users
        .get()                         // GET请求
        .getBody()                     // 获取响应报文体
        .toList(User.class);           // 得到目标数据
```

方法`sync`返回一个同步`CoverHttp`，可链式使用。

### 异步请求

使用方法`async(String url)`开始一个异步请求：

```java
http.async("/users/1")                //  http://api.miaixz.org/users/1
        .

setOnResponse((HttpResult result)->{
// 得到目标数据
User user = result.getBody().toBean(User.class);
        })
                .

get();                       // GET请求
```

方法`async`返回一个异步`CoverHttp`，可链式使用。

### WebSocket

使用方法`webSocket(String url)`开始一个 WebSocket 通讯：

```java
http.webSocket("/chat")
        .

setOnOpen((WebSocket ws, HttpResult res)->{
        ws.

send("向服务器问好");
        })
                .

setOnMessage((WebSocket ws，Message msg)->{
// 从服务器接收消息（自动反序列化）
Chat chat = msg.toBean(Chat.class);
// 相同的消息发送给服务器（自动序列化 Chat 对象）
        ws.

send(chat);
        })
                .

listen();                     // 启动监听
```

方法`webSocket`返回一个支持 WebSocket 的`CoverHttp`，也可链式使用。

#### 第一步、确定请求方式

同步 Httpv（`sync`）、异步 Httpv（`async`）或 WebSocket（`webSocket`）

#### 第二步、构建请求任务

* `addXxxPara` - 添加请求参数
* `setOnXxxx` - 设置回调函数
* `tag` - 添加标签
* ...

#### 第三步、调用请求方法

Httpv 请求方法：

* `get()` - GET 请求
* `post()` - POST 请求
* `put()` - PUT 请求
* `delete()` - DELETE 请求
* ...

Websocket 方法：

* `listen()` - 启动监听

#### 任意请求，都遵循请求三部曲！

* `sync(String url)`   开始一个同步 HTTP 请求
* `async(String url)`  开始一个异步 HTTP 请求
* `webSocket(String url)`  开始一个 WebSocket 连接
* `cancel(String tag)` 按标签取消（同步 | 异步 | WebSocket）连接
* `cancelAll()`        取消所有（同步 | 异步 | WebSocket）连接
* `request(Request request)`  原生 HTTP 请求
* `webSocket(Request request, WebSocketListener listener)` Httpv 原生 WebSocket 连接

```java
http.async("https://api.miaixz.org/auth/login")
        .

addBodyPara("username","jack")
        .

addBodyPara("password","xxxx")
        .

setOnResponse((HttpResult result)->{
// 得到返回数据，使用 Mapper 可省去定义一个实体类
Mapper mapper = result.getBody().toMapper();
// 登录是否成功
boolean success = mapper.getBool("success");
        })
                .

post();
```

### 配置`Httpv`

工具类`Httpv`还支持以 SPI 方式注入自定义配置，分以下两步：

#### 第一步、新建一个配置类，实现[`org.miaixz.bus.http.metric.Config`]接口

例如：

```java
public class HttpvConfig implements Config {

    @Override
    public void with(Httpv.Builder builder) {
        // 在这里对 HTTP.Builder 做一些自定义的配置
        builder.baseUrl("https://api.miaixz.org");
        // 如果项目中添加了 fastjson 或  gson 或  jackson 依赖
        // Httpv 会自动注入它们提供的 Convertor 
        // 所以这里就不需要再配置 Convertor 了 (内部实现自动注入的原理也是 SPI)
        // 但如果没有添加这些依赖，那还需要自定义一个 Convertor
        builder.addMsgConvertor(new MyMsgConvertor());
    }

}
```

## 文件下载

Httpv 并没有把文件的下载排除在常规的请求之外，同一套API，它优雅的设计使得下载与常规请求融合的毫无违和感，一个最简单的示例：

```java
http.sync("bus-http/test.zip")
        .

get()                           // 使用 GET 方法（其它方法也可以，看服务器支持）
        .

getBody()                       // 得到报文体
        .

toFile("bus-http/test.zip")     // 下载到指定的路径
        .

start();                        // 启动下载

        http.

sync("/download/test.zip").

get().

getBody()
        .

toFolder("bus-http")            // 下载到指定的目录，文件名将根据下载信息自动生成
        .

start();
```

或使用异步连接方式：

```java
http.async("bus-http/test.zip")
        .

setOnResponse((HttpResult result)->{
        result.

getBody().

toFolder("bus-http").

start();
        })
                .

get();
```

这里要说明一下：`sync`与`async`
的区别在于连接服务器并得到响应这个过程的同步与异步（这个过程的耗时在大文件下载中占比极小），而`start`方法启动的下载过程则都是异步的。

### 下载进度监听

就直接上代码啦，诸君一看便懂：

```java
http.sync("/download/test.zip")
        .

get()
        .

getBody()
        .

stepBytes(1024)   // 设置每接收 1024 个字节执行一次进度回调（不设置默认为 8192）  
//     .stepRate(0.01)    // 设置每接收 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .

setOnProcess((Process process)->{           // 下载进度回调
long doneBytes = process.getDoneBytes();   // 已下载字节数
long totalBytes = process.getTotalBytes(); // 总共的字节数
double rate = process.getRate();           // 已下载的比例
boolean isDone = process.isDone();         // 是否下载完成
        })
                .

toFolder("bus-http/")        // 指定下载的目录，文件名将根据下载信息自动生成
//     .toFile("bus-http/test.zip")  // 指定下载的路径，若文件已存在则覆盖
        .

setOnSuccess((File file)->{   // 下载成功回调

        })
        .

start();
```

值得一提的是：由于 Httpv
并没有把下载做的很特别，这里设置的进度回调不只对下载文件起用作，即使对响应JSON的常规请求，只要设置了进度回调，它也会告诉你报文接收的进度（提前是服务器响应的报文有`Content-Length`
头），例如：

```java
List<User> users = http.sync("/users")
        .get()
        .getBody()
        .stepBytes(2)
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toList(User.class);
```

### 下载过程控制

过于简单：还是直接上代码：

```java
Ctrl ctrl = http.sync("bus-http/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toFolder("bus-http/")
        .start();   // 该方法返回一个下载过程控制器

        ctrl.

status();      // 下载状态
        ctrl.

pause();       // 暂停下载
        ctrl.

resume();      // 恢复下载
        ctrl.

cancel();      // 取消下载（同时会删除文件，不可恢复）
```

无论是同步还是异步发起的下载请求，都可以做以上的控制：

```java
http.async("bus-http/test.zip")
        .

setOnResponse((HttpResult result)->{
// 拿到下载控制器
Ctrl ctrl = result.getBody().toFolder("bus-http/").start();
        })
                .

get();
```

### 实现断点续传

Httpv 对断点续传并没有再做更高层次的封装，因为这是app该去做的事情，它在设计上使各种网络问题的处理变简单的同时力求纯粹。下面的示例可以看到，Httpv
通过一个失败回调拿到 **断点**，便将复杂的问题变得简单：

```java
http.sync("bus-http/test.zip")
        .

get()
        .

getBody()
        .

toFolder("bus-http/")
        .

setOnFailure((Failure failure)->{         // 下载失败回调，以便接收诸如网络错误等失败信息
IOException e = failure.getException();  // 具体的异常信息
long doneBytes = failure.getDoneBytes(); // 已下载的字节数（断点），需要保存，用于断点续传
File file = failure.getFile();           // 下载生成的文件，需要保存 ，用于断点续传（只保存路径也可以）
        })
                .

start();
```

下面代码实现续传：

```java
long doneBytes =...    // 拿到保存的断点
File file =...        // 待续传的文件

        http.

sync("bus-http/test.zip")
        .

setRange(doneBytes)                         // 设置断点（已下载的字节数）
        .

get()
        .

getBody()
        .

toFile(file)                                // 下载到同一个文件里
        .

setAppended()                               // 开启文件追加模式
        .

setOnSuccess((File file)->{

        })
        .

setOnFailure((Failure failure)->{

        })
        .

start();
```

### 实现分块下载

当文件很大时，有时候我们会考虑分块下载，与断点续传的思路是一样的，示例代码：

```java
    private static String url = "https://www.miaixz.org/dl/test.zip";
private static Httpv httpv;

public static void httpv() {
    Httpv.Builder builder = Httpv.builder();
    ConvertProvider.inject(builder);
    Config.config(builder);
    httpv = builder.build();
    long totalSize = httpv.sync(url).get().getBody()
            .close()                   // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
            .getLength();              // 获得待下载文件的大小（由于未消费报文体，所以该请求不会消耗下载报文体的时间和网络流量）
    downloads(totalSize, 0);      // 从第 0 块开始下载
    sleep(50000);                // 等待下载完成（不然本例的主线程就结束啦）
}

static void downloads(long totalSize, int index) {
    long size = 3 * 1024 * 1024;                 // 每块下载 3M
    long start = index * size;
    long end = Math.min(start + size, totalSize);
    httpv.sync(url)
            .setRange(start, end)                // 设置本次下载的范围
            .get().getBody()
            .toFile("bus-http/test.zip")         // 下载到同一个文件里
            .setAppended()                       // 开启文件追加模式
            .setOnSuccess((File file) -> {
                if (end < totalSize) {           // 若未下载完，则继续下载下一块
                    downloads(totalSize, index + 1);
                } else {
                    System.out.println("下载完成");
                }
            })
            .start();
}
```

## 文件上传

一个简单文件上传的示例：

```java
http.sync("/upload")
        .

addFilePara("test","bus-http/test.zip")
        .

post();     // 上传发法一般使用 POST 或 PUT，看服务器支持
```

异步上传也是完全一样：

```java
http.async("/upload")
        .

addFilePara("test","bus-http/test.zip")
        .

post();
```

```java
http.async("/upload")
        .

bodyType("multipart/form")
        .

addFilePara("test","bus-http/test.zip")
        .

post();
```

### 上传进度监听

Httpv 的上传进度监听，监听的是所有请求报文体的发送进度，示例代码：

```java
http.sync("/upload")
        .

addBodyPara("name","Jack")
        .

addBodyPara("age",20)
        .

addFilePara("avatar","bus-http/avatar.jpg")
        .

stepBytes(1024)   // 设置每发送 1024 个字节执行一次进度回调（不设置默认为 8192）  
//     .stepRate(0.01)    // 设置每发送 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .

setOnProcess((Process process)->{           // 上传进度回调
long doneBytes = process.getDoneBytes();   // 已发送字节数
long totalBytes = process.getTotalBytes(); // 总共的字节数
double rate = process.getRate();           // 已发送的比例
boolean isDone = process.isDone();         // 是否发送完成
        })
                .

post();
```

咦！怎么感觉和下载的进度回调的一样？没错！Httpv 还是使用同一套API处理上传和下载的进度回调，区别只在于上传是在`get/post`
方法之前使用这些API，下载是在`getBody`方法之后使用。很好理解：`get/post`
之前是准备发送请求时段，有上传的含义，而`getBody`之后，已是报文响应的时段，当然是下载。

### 上传过程控制

上传文件的过程控制就很简单，和常规请求一样，只有异步发起的上传可以取消：

```java
HttpCall call = http.async("/upload")
        .addFilePara("test", "bus-http/test.zip")
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .post();

        call.

cancel();  // 取消上传
```

### Httpx 使用

- 暂无

### Httpz 使用

- 支持多线程异步请求
- 支持Http/Https协议
- 支持同步/异步请求
- 支持异步延迟执行
- 支持Cookie持久化
- 支持JSON、表单提交
- 支持文件和图片上传/批量上传，支持同步/异步上传，支持进度提示
- 支持文件流上传

1.同步Get请求(访问百度首页,自动处理https单向认证)

```java
    String url = "https://www.baidu.com";
String resp = Httpz.get().url(url).build().execute().string();
```

2.异步Get请求(访问百度首页)

```java
    Httpz.get().

url("https://www.baidu.com").

build().

executeAsync(new StringCallback() {
    @Override
    public void onFailure (NewCall call, Exception e,int id){
        Logger.error(e.getMessage(), e);
    }

    @Override
    public void onSuccess (NewCall call, String delegate,int id){
        Logger.info("delegate:{}", delegate);
    }
});
```

3.百度搜索关键字'微信机器人'

```java
    Httpz.get().

url("http://www.baidu.com/s").

addParams("wd","微信机器人").

addParams("tn","baidu").

build().

execute().

string();
```

4.异步下载一张百度图片，有下载进度,保存为/tmp/tmp.jpg

```java
    String savePath = "tmp.jpg";
String imageUrl = "http://t7.baidu.com/it/u=3204887199,3790688592&fm=79&app=86&f=JPEG";
    Httpz.

newBuilder().

addNetworkInterceptor(new FileInterceptor() {
    @Override
    public void updateProgress ( long downloadLenth, long totalLength, boolean isFinish){
        Logger.info("updateProgress downloadLenth:" + downloadLenth +
                ",totalLength:" + totalLength + ",isFinish:" + isFinish);
    }
}).

build().

get().

url(imageUrl).

build().

executeAsync(new FileCallback(savePath) {//save file to /tmp/tmp.jpg
    @Override
    public void onFailure (NewCall call, Exception e,int id){
        Logger.error(e.getMessage(), e);
    }

    @Override
    public void onSuccess (NewCall call, File file,int id){
        Logger.info("filePath:" + file.getAbsolutePath());
    }

    @Override
    public void onSuccess (NewCall call, InputStream fileStream,int id){
        Logger.info("onSuccessWithInputStream");
    }
});

```

5.同步下载文件

```java
    String savePath = "tmp.jpg";
String imageUrl = "http://t7.baidu.com/it/u=3204887199,3790688592&fm=79&app=86&f=JPEG";
InputStream is = Httpz.get().url(imageUrl).build().execute().byteStream();
    ...
```

6.上传文件

```java
    String url = "https://www.xxx.com";
byte[] imageContent = FileKit.readBytes("/tmp/test.png");
Response delegate = Httpz.post()
        .url(url)
        .addFile("file", "b.jpg", imageContent)
        .build()
        .execute();
    System.out.

println(delegate.body().

string());
```

7.上传文件(通过文件流)

```java
    InputStream is = new FileInputStream("/tmp/logo.jpg");
Response delegate = Httpz.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()
        .post()
        .url("上传地址")
        .addFile("file", "logo.jpg", is)
        .build()
        .execute();
    Logger.

info(delegate.body().

string());
```

8.设置网络代理

```java
    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1088));
    Authenticator.

setDefault(new Authenticator() {//如果没有设置账号密码，则可以注释掉这块
    private PasswordAuthentication authentication =
            new PasswordAuthentication("username", "password".toCharArray());
    @Override
    protected PasswordAuthentication getPasswordAuthentication () {
        return authentication;
    }
});
Response delegate = Httpz.
        newBuilder().
        proxy(proxy).
        build().
        get().
        url("http://ip111.cn/").
        build().
        execute();
    Logger.

info(delegate.string());
```

9.设置Http头部信息

```java
    String url = "https://www.baidu.com";
Response delegate = Httpz.
        get().
        addHeader("Referer", "http://news.baidu.com/").
        addHeader("cookie", "uin=test;skey=111111;").
        url(url).
        build().
        execute();
        System.out.

println(delegate.string());
```

9.设置https证书

```java
    SSLContext sslContext = getxxx();
Response delegate = Httpz
        .get()
        .sslContext(sslContext)
        .url(url)
        .build()
        .execute();
    System.out.

println(delegate.toString());
```

10.自动携带Cookie进行请求

```java
    private static class LocalCookieJar implements CookieJar {

    List<Cookie> cookies;

    @Override
    public List<Cookie> loadForRequest(UnoUrl arg0) {
        if (null != cookies) {
            return cookies;
        }
        return new ArrayList<>();
    }

    @Override
    public void saveFromResponse(UnoUrl arg0, List<Cookie> cookies) {
        this.cookies = cookies;
    }
}


LocalCookieJar cookie = new LocalCookieJar();
Httpz.Client client = Httpz.newBuilder()
        .followRedirects(false) //禁制Httpd的重定向操作，我们自己处理重定向
        .followSslRedirects(false)
        .cookieJar(cookie)   //为Httpd设置自动携带Cookie的功能
        .build();


String url = "https://www.baidu.com/";
    client.

get().

addHeader("Referer","https://www.baidu.com/").

url(url)
            .

build()
            .

execute();
            System.out.

println(cookie.cookies);
```

11.设置Content-Type为application/json

```java
    String url = "https://wx.qq.com";
Response delegate = Httpz.post().
        addHeader("Content-Type", "application/json").
        body("{\"username\":\"test\",\"password\":\"111111\"}").
        url(url).
        build().
        execute();
```

12.取消请求

```java
    RequestCall call = Httpz.get().
        url("https://www.baidu.com").
        build();
Response delegate = call.execute();
        call.

cancel();
    System.out.

println(delegate.string());
```

13.取消所有请求

```java
    Httpz.cancelAll();
```

14.按照TAG取消请求

```java
    Httpz.cancel(tag);
```