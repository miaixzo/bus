/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.storage;

import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;
import software.amazon.awssdk.utils.AttributeMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * S3 HTTP客户端实现
 * <p>
 * 通用S3协议兼容，不依赖特定存储服务类型
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClientX implements SdkHttpClient {

    /**
     * HTTP客户端实例
     */
    private final Httpd httpd;
    /**
     * 线程池执行器
     */
    private final ExecutorService executor;

    /**
     * 构造函数
     *
     * @param clientBuilder 构建器
     */
    private ClientX(ClientBuilder clientBuilder) {
        this.httpd = clientBuilder.httpdBuilder.build();
        this.executor = clientBuilder.executor != null ? clientBuilder.executor : Executors.newCachedThreadPool();
    }

    /**
     * 准备执行请求
     *
     * @param request HTTP执行请求
     * @return 可执行HTTP请求
     */
    @Override
    public ExecutableHttpRequest prepareRequest(HttpExecuteRequest request) {
        return new HttpExecutableHttpRequest(request);
    }

    /**
     * 获取客户端名称
     *
     * @return 客户端名称
     */
    @Override
    public String clientName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 关闭客户端，释放资源
     */
    @Override
    public void close() {
        // 清理资源
        httpd.dispatcher().executorService().shutdown();
        httpd.connectionPool().evictAll();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 执行异步请求
     * <p>
     * 注意：这不是重写接口方法，而是自定义的异步请求处理方法
     * </p>
     *
     * @param asyncRequest 异步执行请求
     * @return 异步执行结果
     */
    public CompletableFuture<Void> executeAsync(AsyncExecuteRequest asyncRequest) {
        SdkHttpRequest sdkRequest = asyncRequest.request();
        // 构建请求
        Request.Builder requestBuilder = new Request.Builder().url(sdkRequest.getUri().toString())
                .method(sdkRequest.method().name(), createRequestBodyFromAsync(asyncRequest));

        // 添加请求头
        sdkRequest.headers().forEach((name, values) -> {
            for (String value : values) {
                requestBuilder.addHeader(name, value);
            }
        });

        CompletableFuture<Void> future = new CompletableFuture<>();

        // 异步执行请求
        httpd.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(NewCall call, IOException e) {
                // 通知处理器错误
                asyncRequest.responseHandler().onError(e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(NewCall call, Response response) {
                try {
                    // 构建AWS SDK响应
                    SdkHttpResponse sdkResponse = buildSdkHttpResponse(response);

                    // 通知响应处理器头部信息
                    asyncRequest.responseHandler().onHeaders(sdkResponse);

                    if (response.body() != null) {
                        // 创建ByteBuffer发布者
                        Publisher<ByteBuffer> publisher = new ByteBufferPublisher(response.body().byteStream());
                        asyncRequest.responseHandler().onStream(publisher);
                    }

                    // 请求处理完成
                    future.complete(null);
                } catch (Exception e) {
                    // 通知处理器错误
                    asyncRequest.responseHandler().onError(e);
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    /**
     * 构建AWS SDK HTTP响应
     *
     * @param response HTTP响应
     * @return AWS SDK HTTP响应
     */
    private SdkHttpResponse buildSdkHttpResponse(Response response) {
        // 使用SdkHttpResponse.builder()创建响应构建器
        SdkHttpResponse.Builder responseBuilder = SdkHttpResponse.builder().statusCode(response.code());

        // 设置状态文本（如果存在）
        Optional<String> statusText = Optional.ofNullable(response.message());
        statusText.ifPresent(responseBuilder::statusText);

        // 添加响应头
        for (Map.Entry<String, java.util.List<String>> entry : response.headers().toMultimap().entrySet()) {
            responseBuilder.putHeader(entry.getKey(), entry.getValue());
        }

        return responseBuilder.build();
    }

    /**
     * 创建请求体（异步请求）
     *
     * @param asyncRequest 异步执行请求
     * @return 请求体，如果没有请求体则返回null
     */
    private RequestBody createRequestBodyFromAsync(AsyncExecuteRequest asyncRequest) {
        // 使用asyncRequest.request()获取SdkHttpRequest
        SdkHttpRequest sdkRequest = asyncRequest.request();

        // 检查是否有请求内容发布者
        SdkHttpContentPublisher contentPublisher = asyncRequest.requestContentPublisher();

        if (contentPublisher != null) {
            // 创建自定义异步RequestBody
            return new AsyncContentPublisherWrapper(contentPublisher, sdkRequest);
        }

        // 如果没有请求体，返回null
        return null;
    }

    /**
     * 可执行请求实现
     */
    private class HttpExecutableHttpRequest implements ExecutableHttpRequest {
        /** HTTP调用实例 */
        private final NewCall call;
        /** 请求是否已中止的标志 */
        private final AtomicBoolean aborted = new AtomicBoolean(false);

        /**
         * 构造函数
         *
         * @param request HTTP执行请求
         */
        HttpExecutableHttpRequest(HttpExecuteRequest request) {
            // 预构建请求但不立即执行
            SdkHttpRequest sdkRequest = request.httpRequest();
            Request.Builder requestBuilder = new Request.Builder().url(sdkRequest.getUri().toString())
                    .method(sdkRequest.method().name(), createRequestBody(request));

            // 添加请求头
            sdkRequest.headers().forEach((name, values) -> {
                for (String value : values) {
                    requestBuilder.addHeader(name, value);
                }
            });

            this.call = httpd.newCall(requestBuilder.build());
        }

        /**
         * 执行请求
         *
         * @return HTTP执行响应
         * @throws IOException 如果请求执行失败
         */
        @Override
        public HttpExecuteResponse call() throws IOException {
            if (aborted.get()) {
                throw new IOException("Request has been aborted");
            }

            Response response = call.execute();

            // 构建AWS SDK响应
            SdkHttpResponse sdkResponse = buildSdkHttpResponse(response);

            // 创建响应体
            AbortableInputStream responseBody = response.body() != null
                    ? AbortableInputStream.create(response.body().byteStream())
                    : null;

            // 返回HttpExecuteResponse
            return HttpExecuteResponse.builder().response(sdkResponse).responseBody(responseBody).build();
        }

        /**
         * 中止请求
         */
        @Override
        public void abort() {
            if (aborted.compareAndSet(false, true)) {
                call.cancel();
            }
        }

        /**
         * 创建请求体
         *
         * @param request HTTP执行请求
         * @return 请求体，如果没有请求体则返回null
         */
        private RequestBody createRequestBody(HttpExecuteRequest request) {
            // 使用request.contentStreamProvider()获取请求内容
            Optional<ContentStreamProvider> contentStreamProviderOpt = request.contentStreamProvider();
            if (contentStreamProviderOpt.isPresent()) {
                ContentStreamProvider contentStreamProvider = contentStreamProviderOpt.get();
                // 创建自定义RequestBody，确保正确处理内容长度
                return new CustomRequestBody(contentStreamProvider, request.httpRequest());
            }
            return null;
        }
    }

    /**
     * 自定义RequestBody，确保正确处理内容长度和Content-Type
     * <p>
     * 优化性能：减少内存分配，使用流式处理
     * </p>
     */
    private static class CustomRequestBody extends RequestBody {
        /** 内容流提供者 */
        private final ContentStreamProvider contentStreamProvider;
        /** HTTP请求 */
        private final SdkHttpRequest httpRequest;
        /** 内容长度，延迟计算 */
        private volatile Long contentLength;

        /**
         * 构造函数
         *
         * @param contentStreamProvider 内容流提供者
         * @param httpRequest           HTTP请求
         */
        CustomRequestBody(ContentStreamProvider contentStreamProvider, SdkHttpRequest httpRequest) {
            this.contentStreamProvider = contentStreamProvider;
            this.httpRequest = httpRequest;
            // 延迟计算内容长度，只在需要时计算
        }

        /**
         * 获取内容类型
         *
         * @return 内容类型
         */
        @Override
        public MediaType contentType() {
            // 从请求头获取Content-Type，如果没有则使用默认值
            Optional<String> contentTypeOpt = httpRequest.firstMatchingHeader("Content-Type");
            return contentTypeOpt.map(MediaType::valueOf).orElse(MediaType.valueOf("application/octet-stream"));
        }

        /**
         * 将请求体写入输出流
         *
         * @param sink 输出流
         * @throws IOException 如果写入失败
         */
        @Override
        public void writeTo(BufferSink sink) throws IOException {
            // 使用流式处理，避免将整个文件加载到内存
            try (InputStream inputStream = contentStreamProvider.newStream()) {
                byte[] buffer = new byte[8192]; // 8KB缓冲区
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }

        /**
         * 获取内容长度
         *
         * @return 内容长度
         * @throws IOException 如果获取内容长度失败
         */
        @Override
        public long length() throws IOException {
            if (contentLength == null) {
                synchronized (this) {
                    if (contentLength == null) {
                        // 只计算一次，然后缓存结果
                        contentLength = calculateContentLength();
                    }
                }
            }
            return contentLength;
        }

        /**
         * 计算内容长度
         *
         * @return 内容长度
         * @throws IOException 如果计算失败
         */
        private long calculateContentLength() throws IOException {
            // 尝试从请求头获取内容长度
            Optional<String> contentLengthOpt = httpRequest.firstMatchingHeader("Content-Length");
            if (contentLengthOpt.isPresent()) {
                try {
                    return Long.parseLong(contentLengthOpt.get());
                } catch (NumberFormatException e) {
                    // 如果解析失败，继续计算
                }
            }

            // 如果无法从请求头获取，则计算实际内容长度
            try (InputStream inputStream = contentStreamProvider.newStream()) {
                long length = 0;
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    length += bytesRead;
                }
                return length;
            }
        }
    }

    /**
     * 异步内容发布者包装器，将AWS SDK的SdkHttpContentPublisher转换为OkHttp的RequestBody
     */
    private static class AsyncContentPublisherWrapper extends RequestBody {
        /** 内容发布者 */
        private final SdkHttpContentPublisher contentPublisher;
        /** HTTP请求 */
        private final SdkHttpRequest httpRequest;
        /** 内容长度，延迟计算 */
        private volatile Long contentLength;

        /**
         * 构造函数
         *
         * @param contentPublisher 内容发布者
         * @param httpRequest      HTTP请求
         */
        AsyncContentPublisherWrapper(SdkHttpContentPublisher contentPublisher, SdkHttpRequest httpRequest) {
            this.contentPublisher = contentPublisher;
            this.httpRequest = httpRequest;
        }

        /**
         * 获取内容类型
         *
         * @return 内容类型
         */
        @Override
        public MediaType contentType() {
            // 从请求头获取Content-Type，如果没有则使用默认值
            Optional<String> contentTypeOpt = httpRequest.firstMatchingHeader("Content-Type");
            return contentTypeOpt.map(MediaType::valueOf).orElse(MediaType.valueOf("application/octet-stream"));
        }

        /**
         * 将请求体写入输出流
         *
         * @param sink 输出流
         * @throws IOException 如果写入失败
         */
        @Override
        public void writeTo(BufferSink sink) throws IOException {
            try {
                // 将SdkHttpContentPublisher的内容写入BufferSink
                CompletableFuture<Void> future = new CompletableFuture<>();

                // 创建订阅者来接收数据
                Subscriber<ByteBuffer> subscriber = new Subscriber<>() {
                    /**
                     * 订阅关系
                     */
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        // 请求第一个数据块
                        s.request(1);
                    }

                    @Override
                    public void onNext(ByteBuffer buffer) {
                        try {
                            // 将ByteBuffer写入sink
                            if (buffer.hasArray()) {
                                sink.write(buffer.array(), buffer.arrayOffset(), buffer.remaining());
                            } else {
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                sink.write(bytes);
                            }
                            // 请求下一个数据块
                            subscription.request(1);
                        } catch (IOException e) {
                            subscription.cancel();
                            future.completeExceptionally(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }
                };

                // 订阅SdkHttpContentPublisher的发布者
                contentPublisher.subscribe(subscriber);

                // 等待写入完成
                future.get();
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("Failed to write async request body", e);
            }
        }

        /**
         * 获取内容长度
         *
         * @return 内容长度
         * @throws IOException 如果获取内容长度失败
         */
        @Override
        public long length() throws IOException {
            if (contentLength == null) {
                synchronized (this) {
                    if (contentLength == null) {
                        // 尝试从请求头获取内容长度
                        Optional<String> contentLengthOpt = httpRequest.firstMatchingHeader("Content-Length");
                        if (contentLengthOpt.isPresent()) {
                            try {
                                contentLength = Long.parseLong(contentLengthOpt.get());
                                return contentLength;
                            } catch (NumberFormatException e) {
                                // 如果解析失败，继续计算
                            }
                        }

                        // 如果无法从请求头获取，尝试从SdkHttpContentPublisher获取
                        try {
                            Optional<Long> publisherContentLength = contentPublisher.contentLength();
                            if (publisherContentLength.isPresent()) {
                                contentLength = publisherContentLength.get();
                                return contentLength;
                            }
                        } catch (Exception e) {
                            // 如果获取失败，返回-1表示未知长度
                            contentLength = -1L;
                        }
                    }
                }
            }
            return contentLength != null ? contentLength : -1L;
        }
    }

    /**
     * ByteBuffer发布者实现，用于将InputStream转换为Publisher<ByteBuffer>
     * <p>
     * 优化性能：减少内存分配，使用更高效的缓冲区管理
     * </p>
     */
    private static class ByteBufferPublisher implements Publisher<ByteBuffer> {
        /** 输入流 */
        private final InputStream inputStream;
        /** 缓冲区大小 */
        private final int bufferSize;
        /** 执行器服务 */
        private final ExecutorService executorService;

        /**
         * 构造函数，使用默认缓冲区大小
         *
         * @param inputStream 输入流
         */
        ByteBufferPublisher(InputStream inputStream) {
            this(inputStream, 8192);
        }

        /**
         * 构造函数
         *
         * @param inputStream 输入流
         * @param bufferSize  缓冲区大小
         */
        ByteBufferPublisher(InputStream inputStream, int bufferSize) {
            this.inputStream = inputStream;
            this.bufferSize = bufferSize;
            this.executorService = Executors.newSingleThreadExecutor();
        }

        /**
         * 订阅发布者
         *
         * @param subscriber 订阅者
         */
        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            subscriber.onSubscribe(new Subscription() {
                /** 是否已取消 */
                private volatile boolean cancelled = false;
                /** 已请求数据量 */
                private volatile long requested = 0;
                /** 锁对象，用于同步请求计数 */
                private final Object lock = new Object();

                @Override
                public void request(long n) {
                    if (n <= 0 || cancelled) {
                        return;
                    }

                    synchronized (lock) {
                        requested += n;
                        if (requested > 0) {
                            // 使用单独的线程执行数据发送，避免阻塞调用线程
                            executorService.submit(this::emitData);
                        }
                    }
                }

                @Override
                public void cancel() {
                    if (!cancelled) {
                        cancelled = true;
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // 忽略关闭异常
                        }
                        executorService.shutdown();
                    }
                }

                /**
                 * 发送数据
                 */
                private void emitData() {
                    try {
                        byte[] buffer = new byte[bufferSize];
                        while (!cancelled && requested > 0) {
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead == -1) {
                                // 流结束，通知完成
                                subscriber.onComplete();
                                executorService.shutdown();
                                return;
                            }

                            synchronized (lock) {
                                if (requested > 0) {
                                    requested--;
                                    subscriber.onNext(ByteBuffer.wrap(buffer, 0, bytesRead));
                                } else {
                                    // 如果没有请求更多数据，停止发送
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        subscriber.onError(e);
                        executorService.shutdown();
                    }
                }
            });
        }
    }

    /**
     * 构建器模式
     */
    public static class ClientBuilder implements SdkHttpClient.Builder<ClientBuilder> {
        /** HTTP客户端构建器 */
        private Httpd.Builder httpdBuilder;
        /** 回调执行器 */
        private ExecutorService executor;

        /**
         * 构造函数
         */
        public ClientBuilder() {
            this.httpdBuilder = new Httpd.Builder().connectTimeout(Duration.ofSeconds(10))
                    .readTimeout(Duration.ofSeconds(30)).writeTimeout(Duration.ofSeconds(30))
                    .retryOnConnectionFailure(true)
                    // 优化连接池配置
                    .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
                    // 添加通用S3协议兼容性配置
                    .addInterceptor(new S3CompatibilityInterceptor());
        }

        /**
         * 设置连接超时时间
         *
         * @param timeout 超时时间
         * @return 构建器
         */
        public ClientBuilder connectTimeout(Duration timeout) {
            httpdBuilder.connectTimeout(timeout);
            return this;
        }

        /**
         * 设置读取超时时间
         *
         * @param timeout 超时时间
         * @return 构建器
         */
        public ClientBuilder readTimeout(Duration timeout) {
            httpdBuilder.readTimeout(timeout);
            return this;
        }

        /**
         * 设置写入超时时间
         *
         * @param timeout 超时时间
         * @return 构建器
         */
        public ClientBuilder writeTimeout(Duration timeout) {
            httpdBuilder.writeTimeout(timeout);
            return this;
        }

        /**
         * 设置调用超时时间
         *
         * @param timeout 超时时间
         * @return 构建器
         */
        public ClientBuilder callTimeout(Duration timeout) {
            httpdBuilder.callTimeout(timeout);
            return this;
        }

        /**
         * 添加拦截器
         *
         * @param interceptor 拦截器
         * @return 构建器
         */
        public ClientBuilder addInterceptor(Interceptor interceptor) {
            httpdBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * 添加网络拦截器
         *
         * @param interceptor 拦截器
         * @return 构建器
         */
        public ClientBuilder addNetworkInterceptor(Interceptor interceptor) {
            httpdBuilder.addNetworkInterceptor(interceptor);
            return this;
        }

        /**
         * 设置连接池
         *
         * @param connectionPool 连接池
         * @return 构建器
         */
        public ClientBuilder connectionPool(ConnectionPool connectionPool) {
            httpdBuilder.connectionPool(connectionPool);
            return this;
        }

        /**
         * 设置连接失败时是否重试
         *
         * @param retry 是否重试
         * @return 构建器
         */
        public ClientBuilder retryOnConnectionFailure(boolean retry) {
            httpdBuilder.retryOnConnectionFailure(retry);
            return this;
        }

        /**
         * 设置回调执行器
         *
         * @param callbackExecutor 回调执行器
         * @return 构建器
         */
        public ClientBuilder callbackExecutor(ExecutorService callbackExecutor) {
            this.executor = callbackExecutor;
            return this;
        }

        /**
         * 使用默认属性构建客户端
         *
         * @param attributeMap 属性映射
         * @return 存储客户端
         */
        @Override
        public ClientX buildWithDefaults(AttributeMap attributeMap) {
            return new ClientX(this);
        }

        /**
         * 构建客户端
         *
         * @return 存储客户端
         */
        // 添加一个便捷的build方法
        public ClientX build() {
            return new ClientX(this);
        }
    }

    /**
     * S3协议兼容性拦截器
     * <p>
     * 通用S3协议兼容，不依赖特定存储服务类型检测
     * </p>
     */
    private static class S3CompatibilityInterceptor implements Interceptor {
        /**
         * 拦截请求，添加S3协议兼容性头信息
         *
         * @param chain 请求链
         * @return 响应
         * @throws IOException 如果请求处理失败
         */
        @Override
        public Response intercept(NewChain chain) throws IOException {
            Request request = chain.request();

            // 获取原始请求构建器
            Request.Builder requestBuilder = request.newBuilder();

            // 确保Content-Type头存在
            if (request.header("Content-Type") == null) {
                requestBuilder.addHeader("Content-Type", "application/octet-stream");
            }

            // 确保Content-Length头存在
            if (request.body() != null && request.header("Content-Length") == null) {
                try {
                    // 对于自定义RequestBody，我们已经实现了length()方法
                    long length = request.body().length();
                    if (length != -1) {
                        requestBuilder.addHeader("Content-Length", String.valueOf(length));
                    }
                } catch (IOException e) {
                    // 如果无法获取内容长度，忽略错误
                }
            }

            // 添加通用的S3协议头信息
            addS3ProtocolHeaders(requestBuilder, request);

            // 构建新请求
            Request newRequest = requestBuilder.build();

            // 继续请求链
            return chain.proceed(newRequest);
        }

        /**
         * 添加通用的S3协议头信息
         *
         * @param requestBuilder 请求构建器
         * @param request        原始请求
         */
        private void addS3ProtocolHeaders(Request.Builder requestBuilder, Request request) {
            // 确保Host头正确
            if (request.header("Host") == null) {
                requestBuilder.addHeader("Host", request.url().host());
            }

            // 确保Date头存在
            if (request.header("Date") == null) {
                requestBuilder.addHeader("Date",
                        DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
            }

            // 添加通用的内容SHA256头信息
            // 这里使用AWS S3格式作为默认，因为大多数S3兼容服务都支持这种格式
            if (request.header("x-amz-content-sha256") == null) {
                requestBuilder.addHeader("x-amz-content-sha256", "UNSIGNED-PAYLOAD");
            }

            // 检查是否需要覆盖文件
            boolean shouldOverwrite = shouldAllowOverwrite(request);

            // 如果不允许覆盖，添加禁止覆盖头
            // 注意：不是所有S3兼容服务都支持这个头，但对于那些支持的服务，这是有用的
            if (!shouldOverwrite && request.header("x-oss-forbid-overwrite") == null) {
                requestBuilder.addHeader("x-oss-forbid-overwrite", "true");
            }
        }

        /**
         * 判断是否允许覆盖文件
         *
         * @param request HTTP请求
         * @return 是否允许覆盖
         */
        private boolean shouldAllowOverwrite(Request request) {
            // 检查请求方法，如果是PUT请求，通常表示上传或更新操作，应该允许覆盖
            if ("PUT".equals(request.method())) {
                return true;
            }

            // 检查URL中是否包含特定的参数或路径模式
            String url = request.url().toString();

            // 如果URL中包含"overwrite=true"参数，允许覆盖
            if (url.contains("overwrite=true")) {
                return true;
            }

            // 如果URL中包含特定的路径模式，允许覆盖
            if (url.contains("/update/") || url.contains("/replace/")) {
                return true;
            }

            // 默认不允许覆盖
            return false;
        }
    }

}