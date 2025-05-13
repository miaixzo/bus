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
package org.miaixz.bus.http;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.AnyHostnameVerifier;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.accord.RealConnectionPool;
import org.miaixz.bus.http.accord.platform.Platform;
import org.miaixz.bus.http.cache.Cache;
import org.miaixz.bus.http.cache.InternalCache;
import org.miaixz.bus.http.metric.*;
import org.miaixz.bus.http.metric.proxy.NullProxySelector;
import org.miaixz.bus.http.secure.Authenticator;
import org.miaixz.bus.http.secure.CertificateChainCleaner;
import org.miaixz.bus.http.secure.CertificatePinner;
import org.miaixz.bus.http.socket.RealWebSocket;
import org.miaixz.bus.http.socket.WebSocket;
import org.miaixz.bus.http.socket.WebSocketListener;

/**
 * HTTP 请求核心客户端，负责发送 HTTP 请求并读取响应。 通过连接池和线程池优化性能，支持 HTTP/2 和 WebSocket 连接。 建议复用单一 {@code Httpd} 实例以减少资源开销。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpd implements Cloneable, NewCall.Factory, WebSocket.Factory {

    /**
     * 默认支持的协议（HTTP/2 和 HTTP/1.1）
     */
    static final List<Protocol> DEFAULT_PROTOCOLS = org.miaixz.bus.http.Builder.immutableList(Protocol.HTTP_2,
            Protocol.HTTP_1_1);

    /**
     * 默认连接套件（现代 TLS 和明文）
     */
    static final List<ConnectionSuite> DEFAULT_CONNECTION_SPECS = org.miaixz.bus.http.Builder
            .immutableList(ConnectionSuite.MODERN_TLS, ConnectionSuite.CLEARTEXT);

    /**
     * 初始化 Internal 实例，提供内部方法实现
     */
    static {
        Internal.instance = new Internal() {
            @Override
            public void addLenient(Headers.Builder builder, String line) {
                builder.addLenient(line);
            }

            @Override
            public void addLenient(Headers.Builder builder, String name, String value) {
                builder.addLenient(name, value);
            }

            @Override
            public RealConnectionPool realConnectionPool(ConnectionPool connectionPool) {
                return connectionPool.delegate;
            }

            @Override
            public boolean equalsNonHost(Address a, Address b) {
                return a.equalsNonHost(b);
            }

            @Override
            public int code(Response.Builder responseBuilder) {
                return responseBuilder.code;
            }

            @Override
            public void apply(ConnectionSuite tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
                tlsConfiguration.apply(sslSocket, isFallback);
            }

            @Override
            public NewCall newWebSocketCall(Httpd client, Request originalRequest) {
                return RealCall.newRealCall(client, originalRequest, true);
            }

            @Override
            public void initExchange(Response.Builder responseBuilder, Exchange exchange) {
                responseBuilder.initExchange(exchange);
            }

            @Override
            public Exchange exchange(Response response) {
                return response.exchange;
            }
        };
    }

    /**
     * 请求调度器，管理异步请求的执行
     */
    final Dispatcher dispatcher;
    /**
     * 代理配置（优先于代理选择器）
     */
    final Proxy proxy;
    /**
     * 支持的协议列表（HTTP/2, HTTP/1.1 等）
     */
    final List<Protocol> protocols;
    /**
     * 支持的连接套件（TLS 版本和明文）
     */
    final List<ConnectionSuite> connectionSuites;
    /**
     * 返回一个不可变的拦截器列表，该列表观察每个调用的完整跨度: 从建立连接之前(如果有的话)到选择响应源之后(源服务器、缓存或两者都有)
     */
    final List<Interceptor> interceptors;
    /**
     * 返回观察单个网络请求和响应的不可变拦截器列表。这些拦截器必须 调用{@link NewChain#proceed} 只执行一次:网络拦截器短路或重复网络请求是错误的
     */
    final List<Interceptor> networkInterceptors;
    /**
     * 事件监听器工厂，用于监控请求生命周期
     */
    final EventListener.Factory eventListenerFactory;
    /**
     * 代理选择器，用于动态选择代理
     */
    final ProxySelector proxySelector;
    /**
     * Cookie 管理器，处理请求和响应的 Cookie
     */
    final CookieJar cookieJar;
    /**
     * 缓存实例，用于存储响应
     */
    final Cache cache;
    /**
     * 内部缓存接口，提供自定义缓存实现
     */
    final InternalCache internalCache;
    /**
     * 套接字工厂，用于创建普通 TCP 连接
     */
    final SocketFactory socketFactory;
    /**
     * SSL 套接字工厂，用于 HTTPS 连接
     */
    final SSLSocketFactory sslSocketFactory;
    /**
     * 证书链清理器，用于规范化证书链
     */
    final CertificateChainCleaner certificateChainCleaner;
    /**
     * 主机名验证器，用于 HTTPS 主机名验证
     */
    final javax.net.ssl.HostnameVerifier hostnameVerifier;
    /**
     * 证书固定器，限制受信任的证书
     */
    final CertificatePinner certificatePinner;
    /**
     * 代理身份验证器，处理代理服务器认证
     */
    final Authenticator proxyAuthenticator;
    /**
     * 服务器身份验证器，处理源服务器认证
     */
    final Authenticator authenticator;
    /**
     * 连接池，管理 HTTP 和 HTTPS 连接
     */
    final ConnectionPool connectionPool;
    /**
     * DNS 服务，解析主机名到 IP 地址
     */
    final DnsX dns;
    /**
     * 是否跟随 SSL 重定向（HTTPS 到 HTTP 或反之）
     */
    final boolean followSslRedirects;
    /**
     * 是否跟随 HTTP 重定向
     */
    final boolean followRedirects;
    /**
     * 是否在连接失败时重试
     */
    final boolean retryOnConnectionFailure;
    /**
     * 默认调用超时(毫秒).
     */
    final int callTimeout;
    /**
     * 默认连接超时(毫秒).
     */
    final int connectTimeout;
    /**
     * 默认读超时(毫秒).
     */
    final int readTimeout;
    /**
     * 默认写超时(毫秒).
     */
    final int writeTimeout;
    /**
     * Web socket ping间隔(毫秒)
     */
    final int pingInterval;

    /**
     * 默认构造函数，使用默认配置创建 Httpd 实例。
     */
    public Httpd() {
        this(new Builder());
    }

    /**
     * 构造函数，根据提供的 Builder 配置创建 Httpd 实例。
     *
     * @param builder Builder 实例，包含所有配置参数
     */
    public Httpd(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.proxy = builder.proxy;
        this.protocols = builder.protocols;
        this.connectionSuites = builder.connectionSuites;
        this.interceptors = org.miaixz.bus.http.Builder.immutableList(builder.interceptors);
        this.networkInterceptors = org.miaixz.bus.http.Builder.immutableList(builder.networkInterceptors);
        this.eventListenerFactory = builder.eventListenerFactory;
        this.proxySelector = builder.proxySelector;
        this.cookieJar = builder.cookieJar;
        this.cache = builder.cache;
        this.internalCache = builder.internalCache;
        this.socketFactory = builder.socketFactory;

        // 检查是否需要 TLS 配置
        boolean isTLS = false;
        for (ConnectionSuite spec : connectionSuites) {
            isTLS = isTLS || spec.isTls();
        }

        // 配置 SSL 套接字工厂和证书链清理器
        if (null != builder.sslSocketFactory || !isTLS) {
            this.sslSocketFactory = builder.sslSocketFactory;
            this.certificateChainCleaner = builder.certificateChainCleaner;
        } else {
            X509TrustManager trustManager = SSLContextBuilder.newTrustManager();
            this.sslSocketFactory = SSLContextBuilder.newSslSocketFactory(trustManager);
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
        }

        // 配置 SSL 套接字工厂的平台优化
        if (null != sslSocketFactory) {
            Platform.get().configureSslSocketFactory(sslSocketFactory);
        }

        this.hostnameVerifier = builder.hostnameVerifier;
        this.certificatePinner = builder.certificatePinner.withCertificateChainCleaner(certificateChainCleaner);
        this.proxyAuthenticator = builder.proxyAuthenticator;
        this.authenticator = builder.authenticator;
        this.connectionPool = builder.connectionPool;
        this.dns = builder.dns;
        this.followSslRedirects = builder.followSslRedirects;
        this.followRedirects = builder.followRedirects;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
        this.callTimeout = builder.callTimeout;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.pingInterval = builder.pingInterval;

        // 验证拦截器列表不包含 null
        if (interceptors.contains(null)) {
            throw new IllegalStateException("Null interceptor: " + interceptors);
        }
        if (networkInterceptors.contains(null)) {
            throw new IllegalStateException("Null network interceptor: " + networkInterceptors);
        }
    }

    /**
     * 创建新的 HTTP 调用，用于执行指定的请求。
     *
     * @param request HTTP 请求对象
     * @return NewCall 实例，用于执行请求
     */
    @Override
    public NewCall newCall(Request request) {
        return RealCall.newRealCall(this, request, false); // 创建普通 HTTP 调用
    }

    /**
     * 创建新的 WebSocket 连接。
     *
     * @param request  WebSocket 请求对象
     * @param listener WebSocket 事件监听器
     * @return WebSocket 实例
     */
    @Override
    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), pingInterval);
        webSocket.connect(this); // 发起 WebSocket 连接
        return webSocket;
    }

    /**
     * 获取调用超时时间（毫秒）。
     *
     * @return 调用超时时间
     */
    public int callTimeoutMillis() {
        return callTimeout;
    }

    /**
     * 获取连接超时时间（毫秒）。
     *
     * @return 连接超时时间
     */
    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    /**
     * 获取读取超时时间（毫秒）。
     *
     * @return 读取超时时间
     */
    public int readTimeoutMillis() {
        return readTimeout;
    }

    /**
     * 获取写入超时时间（毫秒）。
     *
     * @return 写入超时时间
     */
    public int writeTimeoutMillis() {
        return writeTimeout;
    }

    /**
     * 获取 WebSocket ping 间隔（毫秒）。
     *
     * @return ping 间隔
     */
    public int pingIntervalMillis() {
        return pingInterval;
    }

    /**
     * 获取配置的代理。
     *
     * @return 代理实例，可能为 null
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * 获取代理选择器。
     *
     * @return 代理选择器
     */
    public ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * 获取 Cookie 管理器。
     *
     * @return CookieJar 实例
     */
    public CookieJar cookieJar() {
        return cookieJar;
    }

    /**
     * 获取缓存实例。
     *
     * @return Cache 实例，可能为 null
     */
    public Cache cache() {
        return cache;
    }

    /**
     * 获取内部缓存接口。
     *
     * @return InternalCache 实例，可能为 null
     */
    InternalCache internalCache() {
        return null != cache ? cache.internalCache : internalCache;
    }

    /**
     * 获取 DNS 服务。
     *
     * @return DnsX 实例
     */
    public DnsX dns() {
        return dns;
    }

    /**
     * 获取套接字工厂。
     *
     * @return SocketFactory 实例
     */
    public SocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * 获取 SSL 套接字工厂。
     *
     * @return SSLSocketFactory 实例
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * 获取主机名验证器。
     *
     * @return HostnameVerifier 实例
     */
    public javax.net.ssl.HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * 获取证书固定器。
     *
     * @return CertificatePinner 实例
     */
    public CertificatePinner certificatePinner() {
        return certificatePinner;
    }

    /**
     * 获取服务器身份验证器。
     *
     * @return Authenticator 实例
     */
    public Authenticator authenticator() {
        return authenticator;
    }

    /**
     * 获取代理身份验证器。
     *
     * @return Authenticator 实例
     */
    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    /**
     * 获取连接池。
     *
     * @return ConnectionPool 实例
     */
    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    /**
     * 检查是否跟随 SSL 重定向。
     *
     * @return true 如果跟随 SSL 重定向
     */
    public boolean followSslRedirects() {
        return followSslRedirects;
    }

    /**
     * 检查是否跟随 HTTP 重定向。
     *
     * @return true 如果跟随 HTTP 重定向
     */
    public boolean followRedirects() {
        return followRedirects;
    }

    /**
     * 检查是否在连接失败时重试。
     *
     * @return true 如果启用重试
     */
    public boolean retryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    /**
     * 获取请求调度器。
     *
     * @return Dispatcher 实例
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * 获取支持的协议列表。
     *
     * @return 不可修改的协议列表
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    /**
     * 获取支持的连接套件列表。
     *
     * @return 不可修改的连接套件列表
     */
    public List<ConnectionSuite> connectionSpecs() {
        return connectionSuites;
    }

    /**
     * 获取拦截器列表。
     *
     * @return 不可修改的拦截器列表
     */
    public List<Interceptor> interceptors() {
        return interceptors;
    }

    /**
     * 获取网络拦截器列表。
     *
     * @return 不可修改的网络拦截器列表
     */
    public List<Interceptor> networkInterceptors() {
        return networkInterceptors;
    }

    /**
     * 获取事件监听器工厂。
     *
     * @return EventListener.Factory 实例
     */
    public EventListener.Factory eventListenerFactory() {
        return eventListenerFactory;
    }

    /**
     * 创建新的 Builder 实例，基于当前 Httpd 配置。
     *
     * @return Builder 实例
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Httpd 配置构建器，用于创建和配置 Httpd 实例。
     */
    public static class Builder {

        /**
         * 拦截器列表
         */
        final List<Interceptor> interceptors = new ArrayList<>();
        /**
         * 网络拦截器列表
         */
        final List<Interceptor> networkInterceptors = new ArrayList<>();
        /**
         * 请求调度器
         */
        Dispatcher dispatcher;
        /**
         * 代理配置
         */
        Proxy proxy;
        /**
         * 支持的协议
         */
        List<Protocol> protocols;
        /**
         * 支持的连接套件
         */
        List<ConnectionSuite> connectionSuites;
        /**
         * 事件监听器工厂
         */
        EventListener.Factory eventListenerFactory;
        /**
         * 代理选择器
         */
        ProxySelector proxySelector;
        /**
         * Cookie 管理器
         */
        CookieJar cookieJar;
        /**
         * 缓存实例
         */
        Cache cache;
        /**
         * 内部缓存接口
         */
        InternalCache internalCache;
        /**
         * 套接字工厂
         */
        SocketFactory socketFactory;
        /**
         * SSL 套接字工厂
         */
        SSLSocketFactory sslSocketFactory;
        /**
         * 证书链清理器
         */
        CertificateChainCleaner certificateChainCleaner;
        /**
         * 主机名验证器
         */
        javax.net.ssl.HostnameVerifier hostnameVerifier;
        /**
         * 证书固定器
         */
        CertificatePinner certificatePinner;
        /**
         * 代理身份验证器
         */
        Authenticator proxyAuthenticator;
        /**
         * 服务器身份验证器
         */
        Authenticator authenticator;
        /**
         * 连接池
         */
        ConnectionPool connectionPool;
        /**
         * DNS 服务
         */
        DnsX dns;
        /**
         * 是否跟随 SSL 重定向
         */
        boolean followSslRedirects;
        /**
         * 是否跟随 HTTP 重定向
         */
        boolean followRedirects;
        /**
         * 是否在连接失败时重试
         */
        boolean retryOnConnectionFailure;
        /**
         * 调用超时（毫秒）
         */
        int callTimeout;
        /**
         * 连接超时（毫秒）
         */
        int connectTimeout;
        /**
         * 读取超时（毫秒）
         */
        int readTimeout;
        /**
         * 写入超时（毫秒）
         */
        int writeTimeout;
        /**
         * WebSocket ping 间隔（毫秒）
         */
        int pingInterval;

        /**
         * 默认构造函数，初始化默认配置。
         */
        public Builder() {
            dispatcher = new Dispatcher();
            protocols = DEFAULT_PROTOCOLS;
            connectionSuites = DEFAULT_CONNECTION_SPECS;
            eventListenerFactory = EventListener.factory(EventListener.NONE);
            proxySelector = ProxySelector.getDefault();
            if (null == proxySelector) {
                proxySelector = new NullProxySelector(); // 使用空代理选择器
            }
            cookieJar = CookieJar.NO_COOKIES;
            socketFactory = SocketFactory.getDefault();
            hostnameVerifier = AnyHostnameVerifier.INSTANCE;
            certificatePinner = CertificatePinner.DEFAULT;
            proxyAuthenticator = Authenticator.NONE;
            authenticator = Authenticator.NONE;
            connectionPool = new ConnectionPool();
            dns = DnsX.SYSTEM;
            followSslRedirects = true;
            followRedirects = true;
            retryOnConnectionFailure = true;
            callTimeout = 0;
            connectTimeout = 10_000; // 默认 10 秒
            readTimeout = 10_000; // 默认 10 秒
            writeTimeout = 10_000; // 默认 10 秒
            pingInterval = 0;
        }

        /**
         * 构造函数，基于现有 Httpd 实例初始化配置。
         *
         * @param httpd Httpd 实例
         */
        Builder(Httpd httpd) {
            this.dispatcher = httpd.dispatcher;
            this.proxy = httpd.proxy;
            this.protocols = httpd.protocols;
            this.connectionSuites = httpd.connectionSuites;
            this.interceptors.addAll(httpd.interceptors);
            this.networkInterceptors.addAll(httpd.networkInterceptors);
            this.eventListenerFactory = httpd.eventListenerFactory;
            this.proxySelector = httpd.proxySelector;
            this.cookieJar = httpd.cookieJar;
            this.internalCache = httpd.internalCache;
            this.cache = httpd.cache;
            this.socketFactory = httpd.socketFactory;
            this.sslSocketFactory = httpd.sslSocketFactory;
            this.certificateChainCleaner = httpd.certificateChainCleaner;
            this.hostnameVerifier = httpd.hostnameVerifier;
            this.certificatePinner = httpd.certificatePinner;
            this.proxyAuthenticator = httpd.proxyAuthenticator;
            this.authenticator = httpd.authenticator;
            this.connectionPool = httpd.connectionPool;
            this.dns = httpd.dns;
            this.followSslRedirects = httpd.followSslRedirects;
            this.followRedirects = httpd.followRedirects;
            this.retryOnConnectionFailure = httpd.retryOnConnectionFailure;
            this.callTimeout = httpd.callTimeout;
            this.connectTimeout = httpd.connectTimeout;
            this.readTimeout = httpd.readTimeout;
            this.writeTimeout = httpd.writeTimeout;
            this.pingInterval = httpd.pingInterval;
        }

        /**
         * 设置完成调用的默认超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param timeout 超时时间（0 表示无超时）
         * @param unit    时间单位
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder callTimeout(long timeout, TimeUnit unit) {
            callTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置完成调用的默认超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 超时时间（0 表示无超时）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder callTimeout(Duration duration) {
            callTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置新连接的默认连接超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param timeout 超时时间（0 表示无超时）
         * @param unit    时间单位
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder connectTimeout(long timeout, TimeUnit unit) {
            connectTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置新连接的默认连接超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 超时时间（0 表示无超时）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder connectTimeout(Duration duration) {
            connectTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置新连接的默认读取超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param timeout 超时时间（0 表示无超时）
         * @param unit    时间单位
         * @return 当前 Builder 实例
         * @see Socket#setSoTimeout(int)
         * @see Source#timeout()
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder readTimeout(long timeout, TimeUnit unit) {
            readTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置新连接的默认读取超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 超时时间（0 表示无超时）
         * @return 当前 Builder 实例
         * @see Socket#setSoTimeout(int)
         * @see Source#timeout()
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder readTimeout(Duration duration) {
            readTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置写入超时时间。
         *
         * @param timeout 超时时间（0 表示无超时）
         * @param unit    时间单位
         * @return 当前 Builder 实例
         * @see Sink#timeout()
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder writeTimeout(long timeout, TimeUnit unit) {
            writeTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置写入超时时间。
         *
         * @param duration 超时时间（0 表示无超时）
         * @return 当前 Builder 实例
         * @see Sink#timeout()
         * @throws IllegalArgumentException 如果超时值无效
         */
        public Builder writeTimeout(Duration duration) {
            writeTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置 WebSocket ping 间隔。
         *
         * @param interval 间隔时间（0 表示无 ping）
         * @param unit     时间单位
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果间隔值无效
         */
        public Builder pingInterval(long interval, TimeUnit unit) {
            pingInterval = org.miaixz.bus.http.Builder.checkDuration("interval", interval, unit);
            return this;
        }

        /**
         * 设置 WebSocket ping 间隔。
         *
         * @param duration 间隔时间（0 表示无 ping）
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果间隔值无效
         */
        public Builder pingInterval(Duration duration) {
            pingInterval = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置 HTTP 代理。
         *
         * @param proxy 代理实例
         * @return 当前 Builder 实例
         */
        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * 设置代理选择器。
         *
         * @param proxySelector 代理选择器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 proxySelector 为 null
         */
        public Builder proxySelector(ProxySelector proxySelector) {
            if (null == proxySelector)
                throw new NullPointerException("proxySelector == null");
            this.proxySelector = proxySelector;
            return this;
        }

        /**
         * 设置 Cookie 管理器。
         *
         * @param cookieJar Cookie 管理器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 cookieJar 为 null
         */
        public Builder cookieJar(CookieJar cookieJar) {
            if (null == cookieJar)
                throw new NullPointerException("cookieJar == null");
            this.cookieJar = cookieJar;
            return this;
        }

        /**
         * 设置缓存实例。
         *
         * @param cache 缓存实例
         * @return 当前 Builder 实例
         */
        public Builder cache(Cache cache) {
            this.cache = cache;
            this.internalCache = null;
            return this;
        }

        /**
         * 设置 DNS 服务。
         *
         * @param dns DNS 服务
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 dns 为 null
         */
        public Builder dns(DnsX dns) {
            if (null == dns)
                throw new NullPointerException("dns == null");
            this.dns = dns;
            return this;
        }

        /**
         * 设置套接字工厂。
         *
         * @param socketFactory 套接字工厂
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 socketFactory 为 null
         * @throws IllegalArgumentException 如果 socketFactory 是 SSLSocketFactory
         */
        public Builder socketFactory(SocketFactory socketFactory) {
            if (socketFactory == null)
                throw new NullPointerException("socketFactory == null");
            if (socketFactory instanceof SSLSocketFactory) {
                throw new IllegalArgumentException("socketFactory instanceof SSLSocketFactory");
            }
            this.socketFactory = socketFactory;
            return this;
        }

        /**
         * 设置 SSL 套接字工厂。
         *
         * @param sslSocketFactory SSL 套接字工厂
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 sslSocketFactory 为 null
         */
        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (null == sslSocketFactory)
                throw new NullPointerException("sslSocketFactory == null");
            this.sslSocketFactory = sslSocketFactory;
            this.certificateChainCleaner = Platform.get().buildCertificateChainCleaner(sslSocketFactory);
            return this;
        }

        /**
         * 设置用于保护HTTPS连接的套接字工厂和信任管理器。如果未设置，则使用系统默认值 大多数应用程序不应该调用这个方法，而应该使用系统默认值。这些类包含特殊的优化，如果实现被修饰，这些优化可能会丢失
         *
         * <pre>
         * {@code
         *
         * TrustManagerFactory trustManagerFactory = TrustManagerFactory
         *         .getInstance(TrustManagerFactory.getDefaultAlgorithm());
         * trustManagerFactory.init((KeyStore) null);
         * TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
         * if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
         *     throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
         * }
         * X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
         *
         * SSLContext sslContext = SSLContext.getInstance("TLS");
         * sslContext.init(null, new TrustManager[] { trustManager }, null);
         * SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
         *
         * Httpd client = new Httpd.Builder().sslSocketFactory(sslSocketFactory, trustManager).build();
         * }
         * </pre>
         *
         * @param sslSocketFactory SSL 套接字工厂
         * 
         * @param trustManager     X509 信任管理器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 sslSocketFactory 或 trustManager 为 null
         */
        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
            if (sslSocketFactory == null)
                throw new NullPointerException("sslSocketFactory == null");
            if (trustManager == null)
                throw new NullPointerException("trustManager == null");
            this.sslSocketFactory = sslSocketFactory;
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
            return this;
        }

        /**
         * 设置用于确认响应证书适用于HTTPS连接请求的主机名的验证程序. 如果未设置，将使用默认的主机名验证器
         *
         * @param hostnameVerifier 主机名验证器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 hostnameVerifier 为 null
         */
        public Builder hostnameVerifier(javax.net.ssl.HostnameVerifier hostnameVerifier) {
            if (null == hostnameVerifier)
                throw new NullPointerException("hostnameVerifier == null");
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * 设置限制哪些证书受信任的证书pinner。默认情况下，HTTPS连接仅 依赖于{@link #sslSocketFactory SSL套接字工厂}来建立信任。 固定证书避免了信任证书颁发机构的需要。
         *
         * @param certificatePinner 证书固定器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 certificatePinner 为 null
         */
        public Builder certificatePinner(CertificatePinner certificatePinner) {
            if (null == certificatePinner)
                throw new NullPointerException("certificatePinner == null");
            this.certificatePinner = certificatePinner;
            return this;
        }

        /**
         * 设置用于响应来自源服务器的挑战的验证器。使用{@link #proxyAuthenticator}设置代理服务器的身份验证器.
         *
         * @param authenticator 身份验证器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 authenticator 为 null
         */
        public Builder authenticator(Authenticator authenticator) {
            if (null == authenticator)
                throw new NullPointerException("authenticator == null");
            this.authenticator = authenticator;
            return this;
        }

        /**
         * 设置用于响应来自代理服务器的挑战的验证器。使用{@link #authenticator}设置源服务器的身份验证器 果未设置，将尝试{@linkplain Authenticator#NONE no
         * authentication will be attempted}
         *
         * @param proxyAuthenticator 代理身份验证器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 proxyAuthenticator 为 null
         */
        public Builder proxyAuthenticator(Authenticator proxyAuthenticator) {
            if (null == proxyAuthenticator)
                throw new NullPointerException("proxyAuthenticator == null");
            this.proxyAuthenticator = proxyAuthenticator;
            return this;
        }

        /**
         * 设置用于回收HTTP和HTTPS连接的连接池. 如果未设置，将使用新的连接池
         *
         * @param connectionPool 连接池
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 connectionPool 为 null
         */
        public Builder connectionPool(ConnectionPool connectionPool) {
            if (null == connectionPool)
                throw new NullPointerException("connectionPool == null");
            this.connectionPool = connectionPool;
            return this;
        }

        /**
         * 让这个客户从HTTPS到HTTPS跟踪和从HTTPS到HTTPS. 如果未设置，将遵循协议重定向。这与内置的{@code HttpURLConnection}的默认设置不同
         *
         * @param followProtocolRedirects 是否跟随 SSL 重定向
         * @return 当前 Builder 实例
         */
        public Builder followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        /**
         * 此客户端配置为遵循重定向。如果未设置，将遵循重定向.
         *
         * @param followRedirects 是否跟随 HTTP 重定向
         * @return 当前 Builder 实例
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /**
         * 在遇到连接问题时，将此客户端配置为重试或不重试 将此设置为false，以避免在这样做会造成破坏时重试请求 在这种情况下，调用应用程序应该自己恢复连接故障.
         *
         * @param retryOnConnectionFailure 是否重试
         * @return 当前 Builder 实例
         */
        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        /**
         * 设置请求调度器。
         *
         * @param dispatcher 请求调度器
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 dispatcher 为 null
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            if (null == dispatcher)
                throw new IllegalArgumentException("dispatcher == null");
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * 设置支持的协议。
         *
         * @param protocols 协议列表
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果协议列表无效
         */
        public Builder protocols(List<Protocol> protocols) {
            // 创建协议列表副本
            protocols = new ArrayList<>(protocols);

            // 验证协议列表
            if (!protocols.contains(Protocol.H2_PRIOR_KNOWLEDGE) && !protocols.contains(Protocol.HTTP_1_1)) {
                throw new IllegalArgumentException(
                        "protocols must contain h2_prior_knowledge or http/1.1: " + protocols);
            }
            if (protocols.contains(Protocol.H2_PRIOR_KNOWLEDGE) && protocols.size() > 1) {
                throw new IllegalArgumentException(
                        "protocols containing h2_prior_knowledge cannot use other protocols: " + protocols);
            }
            if (protocols.contains(Protocol.HTTP_1_0)) {
                throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols);
            }
            if (protocols.contains(null)) {
                throw new IllegalArgumentException("protocols must not contain null");
            }

            // 移除不再支持的 SPDY/3
            protocols.remove(Protocol.SPDY_3);

            // 设置不可修改的协议列表
            this.protocols = Collections.unmodifiableList(protocols);
            return this;
        }

        /**
         * 设置支持的连接套件。
         *
         * @param connectionSuites 连接套件列表
         * @return 当前 Builder 实例
         */
        public Builder connectionSpecs(List<ConnectionSuite> connectionSuites) {
            this.connectionSuites = org.miaixz.bus.http.Builder.immutableList(connectionSuites);
            return this;
        }

        /**
         * 获取拦截器列表。
         *
         * @return 可修改的拦截器列表
         */
        public List<Interceptor> interceptors() {
            return interceptors;
        }

        /**
         * 添加拦截器。
         *
         * @param interceptor 拦截器
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 interceptor 为 null
         */
        public Builder addInterceptor(Interceptor interceptor) {
            if (null == interceptor)
                throw new IllegalArgumentException("interceptor == null");
            interceptors.add(interceptor);
            return this;
        }

        /**
         * 获取网络拦截器列表。
         *
         * @return 可修改的网络拦截器列表
         */
        public List<Interceptor> networkInterceptors() {
            return networkInterceptors;
        }

        /**
         * 添加网络拦截器。
         *
         * @param interceptor 网络拦截器
         * @return 当前 Builder 实例
         * @throws IllegalArgumentException 如果 interceptor 为 null
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (null == interceptor)
                throw new IllegalArgumentException("interceptor == null");
            networkInterceptors.add(interceptor);
            return this;
        }

        /**
         * 设置事件监听器。
         *
         * @param eventListener 事件监听器
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 eventListener 为 null
         */
        public Builder eventListener(EventListener eventListener) {
            if (null == eventListener)
                throw new NullPointerException("eventListener == null");
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        /**
         * 设置事件监听器工厂。
         *
         * @param eventListenerFactory 事件监听器工厂
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 eventListenerFactory 为 null
         */
        public Builder eventListenerFactory(EventListener.Factory eventListenerFactory) {
            if (null == eventListenerFactory) {
                throw new NullPointerException("eventListenerFactory == null");
            }
            this.eventListenerFactory = eventListenerFactory;
            return this;
        }

        /**
         * 构建 Httpd 实例。
         *
         * @return Httpd 实例
         */
        public Httpd build() {
            return new Httpd(this);
        }
    }

}