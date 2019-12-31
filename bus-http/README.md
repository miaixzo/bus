##  介绍
HTTP是现代应用常用的一种交换数据和媒体的网络方式，高效地使用HTTP能让资源加载更快，节省带宽。高效的HTTP客户端，它有以下默认特性：

支持HTTP/2，允许所有同一个主机地址的请求共享同一个socket连接
连接池减少请求延时
透明的GZIP压缩减少响应数据的大小
缓存响应内容，避免一些完全重复的请求
当网络出现问题的时候依然坚守自己的职责，它会自动恢复一般的连接问题，如果你的服务有多个IP地址，当第一个IP请求失败时，会交替尝试你配置的其他IP，使用现代TLS技术(SNI, ALPN)初始化新的连接，当握手失败时会回退到TLS 1.0。
 
### Httpd 使用

1.1. 异步GET请求
-new Httpd;
-构造Request对象；
-通过前两步中的对象构建Call对象；
-通过Call#enqueue(Callback)方法来提交异步请求；
 
``` 
    String url = "http://wwww.baidu.com";
    Httpd httpd = new Httpd();
    final Request request = new Request.Builder()
            .url(url)
            .get()//默认就是GET请求，可以不写
            .build();
    NewCall call = httpd.newCall(request);
    call.enqueue(new Callback() {
        @Override
        public void onFailure(NewCall call, IOException e) {
            Logger.info("onFailure: ");
        }

        @Override
        public void onResponse(NewCall call, Response response) throws IOException {
            Logger.info("onResponse: " + response.body().string());
        }
    });
```

1.2. 同步GET请求
前面几个步骤和异步方式一样，只是最后一部是通过 NewCall#execute() 来提交请求，注意这种方式会阻塞调用线程，所以在Android中应放在子线程中执行，否则有可能引起ANR异常，Android3.0 以后已经不允许在主线程访问网络。
 
``` 
    String url = "http://wwww.baidu.com";
    Httpd httpd = new Httpd();
    final Request request = new Request.Builder()
            .url(url)
            .build();
    final NewCall call = httpd.newCall(request);
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Response response = call.execute();
                Logger.info("run: " + response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }).start();
``` 

2.1. POST方式提交String
这种方式与前面的区别就是在构造Request对象时，需要多构造一个RequestBody对象，用它来携带我们要提交的数据。在构造 RequestBody 需要指定MediaType，用于描述请求/响应 body 的内容类型，关于 MediaType 的更多信息可以查看 RFC 2045，RequstBody的几种构造方式：
 
 ``` 
    MediaType mediaType = MediaType.valueOf("text/x-markdown; charset=utf-8");
    String requestBody = "I am Jdqm.";
    Request request = new Request.Builder()
           .url("https://api.github.com/markdown/raw")
           .post(RequestBody.create(mediaType, requestBody))
           .build();
    Httpd httpd = new Httpd();
    httpd.newCall(request).enqueue(new Callback() {
       @Override
       public void onFailure(NewCall call, IOException e) {
           Logger.info("onFailure: " + e.getMessage());
       }
    
       @Override
       public void onResponse(NewCall call, Response response) throws IOException {
           Logger.info(response.protocol() + " " + response.code() + " " + response.message());
           Headers headers = response.headers();
           for (int i = 0; i < headers.size(); i++) {
               Logger.info(headers.name(i) + ":" + headers.value(i));
           }
           Logger.info("onResponse: " + response.body().string());
       }
    });
 ``` 
响应内容
 ```
    http/1.1 200 OK 
    Date:Sat, 10 Mar 2018 05:23:20 GMT 
    Content-Type:text/html;charset=utf-8
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
 ```
    RequestBody requestBody = new RequestBody() {
    
        @Override
        public MediaType contentType() {
            return MediaType.valueOf("text/x-markdown; charset=utf-8");
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
    httpd.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(NewCall call, IOException e) {
            Logger.info("onFailure: " + e.getMessage());
        }
    
        @Override
        public void onResponse(NewCall call, Response response) throws IOException {
            Logger.info(response.protocol() + " " + response.code() + " " + response.message());
            Headers headers = response.headers();
            for (int i = 0; i < headers.size(); i++) {
                Logger.info(headers.name(i) + ":" + headers.value(i));
            }
            Logger.info("onResponse: " + response.body().string());
        }
    });
 ```

2.3. POST提交文件
 ```
    MediaType mediaType = MediaType.valueOf("text/x-markdown; charset=utf-8");
    Httpd httpd = new Httpd();
    File file = new File("test.md");
    Request request = new Request.Builder()
           .url("https://api.github.com/markdown/raw")
           .post(RequestBody.create(mediaType, file))
           .build();
    httpd.newCall(request).enqueue(new Callback() {
       @Override
       public void onFailure(NewCall call, IOException e) {
           Logger.info("onFailure: " + e.getMessage());
       }
    
       @Override
       public void onResponse(NewCall call, Response response) throws IOException {
           Logger.info(response.protocol() + " " + response.code() + " " + response.message());
           Headers headers = response.headers();
           for (int i = 0; i < headers.size(); i++) {
               Logger.info(headers.name(i) + ":" + headers.value(i));
           }
           Logger.info("onResponse: " + response.body().string());
       }
    });
 ```

2.4. POST方式提交表单

 ```
    Httpd httpd = new Httpd();
    RequestBody requestBody = new FormBody.Builder()
            .add("search", "Jurassic Park")
            .build();
    Request request = new Request.Builder()
            .url("https://en.wikipedia.org/w/index.php")
            .post(requestBody)
            .build();
    
    httpd.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(NewCall call, IOException e) {
            Logger.info("onFailure: " + e.getMessage());
        }
    
        @Override
        public void onResponse(NewCall call, Response response) throws IOException {
            Logger.info(response.protocol() + " " + response.code() + " " + response.message());
            Headers headers = response.headers();
            for (int i = 0; i < headers.size(); i++) {
                Logger.info(headers.name(i) + ":" + headers.value(i));
            }
            Logger.info("onResponse: " + response.body().string());
        }
    });
 ```

2.5. POST方式提交分块请求
MultipartBody 可以构建复杂的请求体，与HTML文件上传形式兼容。多块请求体中每块请求都是一个请求体，可以定义自己的请求头。这些请求头可以用来描述这块请求，例如它的 Content-Disposition 。如果 Content-Length 和 Content-Type 可用的话，他们会被自动添加到请求头中
 ``` 
    Httpd client = new Httpd();
    MultipartBody body = new MultipartBody.Builder("AaB03x")
            .setType(MediaType.MULTIPART_FORM_DATA_TYPE)
            .addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"title\""),
                    RequestBody.create(null, "Square Logo"))
            .addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"image\""),
                    RequestBody.create( MediaType.valueOf("image/png"), new File("website/static/logo-square.png")))
            .build();
    
    Request request = new Request.Builder()
            .header("Authorization", "Client-ID " + "...")
            .url("https://api.imgur.com/3/image")
            .post(body)
            .build();
    
    NewCall call = client.newCall(request);
    call.enqueue(new Callback() {
        @Override
        public void onFailure(NewCall call, IOException e) {
    
        }
    
        @Override
        public void onResponse(NewCall call, Response response) throws IOException {
            Logger.info(response.body().string());
    
        }
    
    });
 ```

3.1. 拦截器
 Httpd的拦截器链可谓是其整个框架的精髓，用户可传入的 interceptor 分为两类：
 ①一类是全局的 interceptor，该类 interceptor 在整个拦截器链中最早被调用，通过 Httpd.Builder#addInterceptor(Interceptor) 传入；
 ②另外一类是非网页请求的 interceptor ，这类拦截器只会在非网页请求中被调用，并且是在组装完请求之后，真正发起网络请求前被调用，所有的 interceptor 被保存在 List<Interceptor> interceptors 集合中，按照添加顺序来逐个调用，具体可参考 RealCall#getResponseWithInterceptorChain() 方法。通过 Httpd.Builder#addNetworkInterceptor(Interceptor) 传入；

 这里举一个简单的例子，例如有这样一个需求，我要监控App通过 OkHttp 发出的所有原始请求，以及整个请求所耗费的时间，针对这样的需求就可以使用第一类全局的 interceptor 在拦截器链头去做。
 ```
    public class LoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long startTime = System.nanoTime();
            Logger.info(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long endTime = System.nanoTime();
            Logger.info(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (endTime - startTime) / 1e6d, response.headers()));

            return response;
        }
    }
 ```

 ```
    Httpd httpd = new Httpd.Builder()
            .addInterceptor(new LoggingInterceptor())
            .build();
    Request request = new Request.Builder()
            .url("http://www.publicobject.com/helloworld.txt")
            .header("User-Agent", "OkHttp Example")
            .build();
    httpd.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(NewCall call, IOException e) {
            Logger.info("onFailure: " + e.getMessage());
        }
    
        @Override
        public void onResponse(NewCall call, Response response) throws IOException {
            ResponseBody body = response.body();
            if (body != null) {
                Logger.info("onResponse: " + response.body().string());
                body.close();
            }
        }
    });
 ```
针对这个请求，打印出来的结果
 ```
    Sending request http://www.publicobject.com/helloworld.txt on null
    User-Agent: OkHttp Example
            
    Received response for https://publicobject.com/helloworld.txt in 1265.9ms
    Server: nginx/1.10.0 (Ubuntu)
    Date: Wed, 28 Mar 2018 08:19:48 GMT
    Content-Type: text/plain
    Content-Length: 1759
    Last-Modified: Tue, 27 May 2014 02:35:47 GMT
    Connection: keep-alive
    ETag: "5383fa03-6df"
    Accept-Ranges: bytes
 ```

注意到一点是这个请求做了重定向，原始的 request url 是 http://www.publicobject.com/helloworld.tx，而响应的 request url 是 https://publicobject.com/helloworld.txt，这说明一定发生了重定向，但是做了几次重定向其实我们这里是不知道的，要知道这些的话，可以使用 addNetworkInterceptor()去做。更多的关于 interceptor的使用以及它们各自的优缺点
 
 
## 其他
 1. 推荐让 Httpd 保持单例，用同一个 Httpd 实例来执行你的所有请求，因为每一个 Httpd 实例都拥有自己的连接池和线程池，重用这些资源可以减少延时和节省资源，如果为每个请求创建一个 Httpd 实例，显然就是一种资源的浪费。当然，也可以使用如下的方式来创建一个新的 Httpd 实例，它们共享连接池、线程池和配置信息。
 ```
    Httpd client = Httpd.newBuilder()
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build();
    Response response = client.newCall(request).execute();
 ```
 2. 每一个Call（其实现是RealCall）只能执行一次，否则会报异常，具体参见 RealCall#execute()
  
  
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
```
    String url = "https://www.baidu.com";
    String resp = Httpz.get().url(url).build().execute().string();
```

2.异步Get请求(访问百度首页)
```
    Httpz.get().url("https://www.baidu.com").build().
            executeAsync(new StringCallback() {
                @Override
                public void onFailure(NewCall call, Exception e, int id) {
                    Logger.error(e.getMessage(), e);
                }
    
                @Override
                public void onSuccess(NewCall call, String response, int id) {
                    Logger.info("response:{}", response);
                }
            });
```

3.百度搜索关键字'微信机器人'
```
    Httpz.get().
            url("http://www.baidu.com/s").
            addParams("wd", "微信机器人").
            addParams("tn", "baidu").
            build().
            execute().
            string();
```

4.异步下载一张百度图片，有下载进度,保存为/tmp/tmp.jpg
```
    String savePath = "tmp.jpg";
    String imageUrl = "http://e.hiphotos.baidu.com/image/pic/item/faedab64034f78f0b31a05a671310a55b3191c55.jpg";
    Httpz.newBuilder().addNetworkInterceptor(new FileInterceptor() {
        @Override
        public void updateProgress(long downloadLenth, long totalLength, boolean isFinish) {
            Logger.info("updateProgress downloadLenth:" + downloadLenth +
                    ",totalLength:" + totalLength + ",isFinish:" + isFinish);
        }
    }).build().
            get().
            url(imageUrl).
            build().
            executeAsync(new FileCallback(savePath) {//save file to /tmp/tmp.jpg
                @Override
                public void onFailure(NewCall call, Exception e, int id) {
                    Logger.error(e.getMessage(), e);
                }
    
                @Override
                public void onSuccess(NewCall call, File file, int id) {
                    Logger.info("filePath:" + file.getAbsolutePath());
                }
    
                @Override
                public void onSuccess(NewCall call, InputStream fileStream, int id) {
                    Logger.info("onSuccessWithInputStream");
                }
            });

```

5.同步下载文件
```
    String savePath = "tmp.jpg";
    String imageUrl = "http://e.hiphotos.baidu.com/image/pic/item/faedab64034f78f0b31a05a671310a55b3191c55.jpg";
    InputStream is = Httpz.get().url(imageUrl).build().execute().byteStream();
    ...
```
	
6.上传文件
```
    String url = "https://www.xxx.com";
    byte[] imageContent = FileUtils.readBytes("/tmp/test.png");
    Response response = FastHttpClient.post()
            .url(url)
            .addFile("file", "b.jpg", imageContent)
            .build()
            .execute();
    System.out.println(response.body().string());
```

7.上传文件(通过文件流)
```
    InputStream is = new FileInputStream("/tmp/logo.jpg");
    HttpResponse response = Httpz.newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
            .post()
            .url("上传地址")
            .addFile("file", "logo.jpg", is)
            .build()
            .execute();
    Logger.info(response.body().string());
```

8.设置网络代理
```
    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1088));
    Authenticator.setDefault(new Authenticator(){//如果没有设置账号密码，则可以注释掉这块
        private PasswordAuthentication authentication =
                new PasswordAuthentication("username","password".toCharArray());
        @Override
        protected PasswordAuthentication getPasswordAuthentication(){
            return authentication;
        }
    });
    HttpResponse response = Httpz.
            newBuilder().
            proxy(proxy).
            build().
            get().
            url("http://ip111.cn/").
            build().
            execute();
    Logger.info(response.string());
```

9.设置Http头部信息
```
    String url="https://www.baidu.com";
    HttpResponse response=Httpz.
            get().
            addHeader("Referer","http://news.baidu.com/").
            addHeader("cookie", "uin=test;skey=111111;").
            url(url).
            build().
            execute();
    System.out.println(response.string());
```

9.设置https证书
```
    SSLContext sslContext = getxxx();
    Response response = Httpz
            .get()
            .sslContext(sslContext)
            .url(url)
            .build()
            .execute();
    System.out.println(response.toString());
```

10.自动携带Cookie进行请求
```
    private static class LocalCookieJar implements CookieJar {

        List<Cookie> cookies;

        @Override
        public List<Cookie> loadForRequest(UnoUrl arg0) {
            if (cookies != null) {
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
    client.get().addHeader("Referer", "https://www.baidu.com/").url(url)
            .build()
            .execute();
    System.out.println(cookie.cookies);

```

11.设置Content-Type为application/json
```
    String url="https://wx.qq.com";
    HttpResponse response=Httpz.post().
            addHeader("Content-Type","application/json").
            body("{\"username\":\"test\",\"password\":\"111111\"}").
            url(url).
            build().
            execute();

```

12.取消请求
```
    RequestCall call = Httpz.get().
            url("https://www.baidu.com").
            build();
    HttpResponse response = call.execute();
    call.cancel();
    System.out.println(response.string());
```