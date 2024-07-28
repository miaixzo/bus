/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.core.net.tls.TrustAnyHostnameVerifier;
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

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 发送HTTP请求辅助类 工厂的{@linkplain NewCall call}，可以用来发送HTTP请求并读取它们的响应 当您创建一个{@code Httpd}实例并将其用于所有HTTP调用时，体现Httpd的性能最佳
 * 这是因为每个客户机都拥有自己的连接池和线程池。重用连接和线程可以减少延迟并节省内存 相反，为每个请求创建一个客户机会浪费空闲池上的资源 Httpd还为HTTP/2连接使用守护进程线程。如果它们保持空闲，就会自动退出
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpd implements Cloneable, NewCall.Factory, WebSocket.Factory {

    static final List<Protocol> DEFAULT_PROTOCOLS = org.miaixz.bus.http.Builder.immutableList(Protocol.HTTP_2,
            Protocol.HTTP_1_1);

    static final List<ConnectionSuite> DEFAULT_CONNECTION_SPECS = org.miaixz.bus.http.Builder
            .immutableList(ConnectionSuite.MODERN_TLS, ConnectionSuite.CLEARTEXT);

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

    final Dispatcher dispatcher;
    final Proxy proxy;
    final List<Protocol> protocols;
    final List<ConnectionSuite> connectionSuites;
    /**
     * 返回一个不可变的拦截器列表，该列表观察每个调用的完整跨度: 从建立连接之前(如果有的话)到选择响应源之后(源服务器、缓存或两者都有)
     */
    final List<Interceptor> interceptors;
    /**
     * 返回观察单个网络请求和响应的不可变拦截器列表。这些拦截器必须 调用{@link NewChain#proceed} 只执行一次:网络拦截器短路或重复网络请求是错误的
     */
    final List<Interceptor> networkInterceptors;
    final EventListener.Factory eventListenerFactory;
    final ProxySelector proxySelector;
    final CookieJar cookieJar;
    final Cache cache;
    final InternalCache internalCache;
    final SocketFactory socketFactory;
    final SSLSocketFactory sslSocketFactory;
    final CertificateChainCleaner certificateChainCleaner;
    final javax.net.ssl.HostnameVerifier hostnameVerifier;
    final CertificatePinner certificatePinner;
    final Authenticator proxyAuthenticator;
    final Authenticator authenticator;
    final ConnectionPool connectionPool;
    final DnsX dns;
    final boolean followSslRedirects;
    final boolean followRedirects;
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

    public Httpd() {
        this(new Builder());
    }

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

        boolean isTLS = false;
        for (ConnectionSuite spec : connectionSuites) {
            isTLS = isTLS || spec.isTls();
        }

        if (null != builder.sslSocketFactory || !isTLS) {
            this.sslSocketFactory = builder.sslSocketFactory;
            this.certificateChainCleaner = builder.certificateChainCleaner;
        } else {
            X509TrustManager trustManager = SSLContextBuilder.newTrustManager();
            this.sslSocketFactory = SSLContextBuilder.newSslSocketFactory(trustManager);
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
        }

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

        if (interceptors.contains(null)) {
            throw new IllegalStateException("Null interceptor: " + interceptors);
        }
        if (networkInterceptors.contains(null)) {
            throw new IllegalStateException("Null network interceptor: " + networkInterceptors);
        }
    }

    /**
     * 准备{@code request}在将来的某个时间点执行
     *
     * @param request 请求信息
     * @return the object
     */
    @Override
    public NewCall newCall(Request request) {
        return RealCall.newRealCall(this, request, false);
    }

    /**
     * 使用{@code request}连接一个新的web套接字
     *
     * @param request  请求信息
     * @param listener 监听器
     * @return the object
     */
    @Override
    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), pingInterval);
        webSocket.connect(this);
        return webSocket;
    }

    public int callTimeoutMillis() {
        return callTimeout;
    }

    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    public int readTimeoutMillis() {
        return readTimeout;
    }

    public int writeTimeoutMillis() {
        return writeTimeout;
    }

    public int pingIntervalMillis() {
        return pingInterval;
    }

    public Proxy proxy() {
        return proxy;
    }

    public ProxySelector proxySelector() {
        return proxySelector;
    }

    public CookieJar cookieJar() {
        return cookieJar;
    }

    public Cache cache() {
        return cache;
    }

    InternalCache internalCache() {
        return null != cache ? cache.internalCache : internalCache;
    }

    public DnsX dns() {
        return dns;
    }

    public SocketFactory socketFactory() {
        return socketFactory;
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public javax.net.ssl.HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    public CertificatePinner certificatePinner() {
        return certificatePinner;
    }

    public Authenticator authenticator() {
        return authenticator;
    }

    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    public boolean followSslRedirects() {
        return followSslRedirects;
    }

    public boolean followRedirects() {
        return followRedirects;
    }

    public boolean retryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public List<Protocol> protocols() {
        return protocols;
    }

    public List<ConnectionSuite> connectionSpecs() {
        return connectionSuites;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public List<Interceptor> networkInterceptors() {
        return networkInterceptors;
    }

    public EventListener.Factory eventListenerFactory() {
        return eventListenerFactory;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        final List<Interceptor> interceptors = new ArrayList<>();
        final List<Interceptor> networkInterceptors = new ArrayList<>();
        Dispatcher dispatcher;
        Proxy proxy;
        List<Protocol> protocols;
        List<ConnectionSuite> connectionSuites;
        EventListener.Factory eventListenerFactory;
        ProxySelector proxySelector;
        CookieJar cookieJar;
        Cache cache;
        InternalCache internalCache;
        SocketFactory socketFactory;
        SSLSocketFactory sslSocketFactory;
        CertificateChainCleaner certificateChainCleaner;
        javax.net.ssl.HostnameVerifier hostnameVerifier;
        CertificatePinner certificatePinner;
        Authenticator proxyAuthenticator;
        Authenticator authenticator;
        ConnectionPool connectionPool;
        DnsX dns;
        boolean followSslRedirects;
        boolean followRedirects;
        boolean retryOnConnectionFailure;
        int callTimeout;
        int connectTimeout;
        int readTimeout;
        int writeTimeout;
        int pingInterval;

        public Builder() {
            dispatcher = new Dispatcher();
            protocols = DEFAULT_PROTOCOLS;
            connectionSuites = DEFAULT_CONNECTION_SPECS;
            eventListenerFactory = EventListener.factory(EventListener.NONE);
            proxySelector = ProxySelector.getDefault();
            if (null == proxySelector) {
                proxySelector = new NullProxySelector();
            }
            cookieJar = CookieJar.NO_COOKIES;
            socketFactory = SocketFactory.getDefault();
            hostnameVerifier = TrustAnyHostnameVerifier.INSTANCE;
            certificatePinner = CertificatePinner.DEFAULT;
            proxyAuthenticator = Authenticator.NONE;
            authenticator = Authenticator.NONE;
            connectionPool = new ConnectionPool();
            dns = DnsX.SYSTEM;
            followSslRedirects = true;
            followRedirects = true;
            retryOnConnectionFailure = true;
            callTimeout = 0;
            connectTimeout = 10_000;
            readTimeout = 10_000;
            writeTimeout = 10_000;
            pingInterval = 0;
        }

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
         * @param timeout 超时时间
         * @param unit    计算单位
         * @return 构造器
         */
        public Builder callTimeout(long timeout, TimeUnit unit) {
            callTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置完成调用的默认超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 持续时间
         * @return 构造器
         */
        public Builder callTimeout(Duration duration) {
            callTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置新连接的默认连接超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param timeout 超时时间
         * @param unit    计算单位
         * @return 构造器
         */
        public Builder connectTimeout(long timeout, TimeUnit unit) {
            connectTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置新连接的默认连接超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 持续时间
         * @return 构造器
         */
        public Builder connectTimeout(Duration duration) {
            connectTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置新连接的默认读取超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param timeout 超时时间
         * @param unit    计算单位
         * @return 构造器
         * @see Socket#setSoTimeout(int)
         * @see Source#timeout()
         */
        public Builder readTimeout(long timeout, TimeUnit unit) {
            readTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置新连接的默认读取超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 持续时间
         * @return 构造器
         * @see Socket#setSoTimeout(int)
         * @see Source#timeout()
         */
        public Builder readTimeout(Duration duration) {
            readTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置新连接的默认写超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param timeout 超时时间
         * @param unit    计算单位
         * @return 构造器
         * @see Sink#timeout()
         */
        public Builder writeTimeout(long timeout, TimeUnit unit) {
            writeTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * 设置新连接的默认写超时。值0表示没有超时，否则在转换为毫秒时，值必须在1和{@link Integer#MAX_VALUE}之间
         *
         * @param duration 持续时间
         * @return 构造器
         * @see Sink#timeout()
         */
        public Builder writeTimeout(Duration duration) {
            writeTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置此客户端发起的HTTP/2和web套接字ping之间的间隔。使用此命令可自动发送ping帧，直到连接失败或关闭 这将保持连接处于活动状态，并可能检测到连接失败.
         *
         * @param interval 间隔时间
         * @param unit     计算单位
         * @return 构造器
         */
        public Builder pingInterval(long interval, TimeUnit unit) {
            pingInterval = org.miaixz.bus.http.Builder.checkDuration("interval", interval, unit);
            return this;
        }

        /**
         * 设置此客户端发起的HTTP/2和web套接字ping之间的间隔。使用此命令可自动发送ping帧， 直到连接失败或关闭。这将保持连接处于活动状态，并可能检测到连接失败.
         *
         * @param duration 持续时间
         * @return 构造器
         */
        public Builder pingInterval(Duration duration) {
            pingInterval = org.miaixz.bus.http.Builder.checkDuration("timeout", duration.toMillis(),
                    TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置此客户端创建的连接将使用的HTTP代理。它优先于{@link #proxySelector}，
         * 后者仅在此代理为空(默认为空)时才被启用。要完全禁用代理使用，请调用{@code proxy(proxy . no_proxy)}
         *
         * @param proxy 代理
         * @return 构造器
         */
        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * 如果没有显式指定{@link #proxy proxy}，则设置要使用的代理选择策略。代理选择器可以返回多个代理;在这种情况下， 将依次对它们进行测试，直到建立成功的连接.
         *
         * @param proxySelector 代理选择器
         * @return 构造器
         */
        public Builder proxySelector(ProxySelector proxySelector) {
            if (null == proxySelector)
                throw new NullPointerException("proxySelector == null");
            this.proxySelector = proxySelector;
            return this;
        }

        /**
         * 设置可以接受来自传入HTTP响应的cookie并向传出HTTP请求提供cookie的处理程序.
         *
         * @param cookieJar cookie策略
         * @return 构造器
         */
        public Builder cookieJar(CookieJar cookieJar) {
            if (null == cookieJar)
                throw new NullPointerException("cookieJar == null");
            this.cookieJar = cookieJar;
            return this;
        }

        /**
         * 设置用于读写缓存的响应的响应缓存.
         *
         * @param cache 缓存支持
         * @return 构造器
         */
        public Builder cache(Cache cache) {
            this.cache = cache;
            this.internalCache = null;
            return this;
        }

        /**
         * 设置用于查找主机名的IP地址的DNS服务. 如果未设置，将使用{@link DnsX#SYSTEM system-wide default}DNS
         *
         * @param dns DNS服务
         * @return 构造器
         */
        public Builder dns(DnsX dns) {
            if (null == dns)
                throw new NullPointerException("dns == null");
            this.dns = dns;
            return this;
        }

        /**
         * 设置用于创建连接的套接字工厂。Httpd只使用无参数的{@link SocketFactory#createSocket()} 方法来创建未连接的套接字。重写这个方法，例如。，允许将套接字绑定到特定的本地地址
         * 如果未设置，将使用{@link SocketFactory#getDefault() system-wide default}socket工厂
         *
         * @param socketFactory socket工厂
         * @return 构造器
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
         * 设置用于保护HTTPS连接的套接字工厂。如果未设置，则使用系统默认值
         *
         * @param sslSocketFactory socket工厂
         * @return 构造器
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
         * @param sslSocketFactory ssl socket工厂
         * @param trustManager     X509证书身份验证
         * @return 构造器
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
         * @param hostnameVerifier 验证主机名接口
         * @return 构造器
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
         * @param certificatePinner 证书
         * @return 构造器
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
         * @param authenticator 验证器
         * @return 构造器
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
         * @param proxyAuthenticator 代理验证器
         * @return 构造器
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
         * @param connectionPool 连接池信息
         * @return 构造器
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
         * @param followProtocolRedirects 重定向
         * @return 构造器
         */
        public Builder followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        /**
         * 此客户端配置为遵循重定向。如果未设置，将遵循重定向.
         *
         * @param followRedirects 重定向
         * @return 构造器
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /**
         * 在遇到连接问题时，将此客户端配置为重试或不重试 将此设置为false，以避免在这样做会造成破坏时重试请求 在这种情况下，调用应用程序应该自己恢复连接故障.
         *
         * @param retryOnConnectionFailure 失败重试
         * @return 构造器
         */
        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        /**
         * 设置用于设置策略和执行异步请求的调度程序。不能为空.
         *
         * @param dispatcher 调度程序分配器
         * @return 构造器
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            if (null == dispatcher)
                throw new IllegalArgumentException("dispatcher == null");
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * 配置此客户端使用的协议以与远程服务器通信。默认情况下，该客户机将选择最有效的传输方式 退回到更普遍的协议。应用程序应该只调用这个方法来避免特定的兼容性问题，比如在启用HTTP/2时web服务器的行为不正确.
         *
         * @param protocols 使用的协议，按优先顺序排列。如果列表包含{@link Protocol#H2_PRIOR_KNOWLEDGE}， 那么它必须是唯一的协议，并且不支持HTTPS
         *                  url。否则列表必须包含{@link Protocol#HTTP_1_1}。 该列表不能包含null或{@link Protocol#HTTP_1_0}.
         * @return 构造器
         */
        public Builder protocols(List<Protocol> protocols) {
            // 创建列表的私有副本
            protocols = new ArrayList<>(protocols);

            // 验证该列表包含我们需要的所有内容，没有我们禁止的内容.
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

            // 删除不再支持的协议
            protocols.remove(Protocol.SPDY_3);

            // 指定为不可修改的列表。这是有效不可变的.
            this.protocols = Collections.unmodifiableList(protocols);
            return this;
        }

        public Builder connectionSpecs(List<ConnectionSuite> connectionSuites) {
            this.connectionSuites = org.miaixz.bus.http.Builder.immutableList(connectionSuites);
            return this;
        }

        /**
         * 返回一个可修改的拦截器列表，该列表观察每个调用的完整跨度: 从建立连接之前(如果有的话)到选择响应源之后(源服务器、缓存或两者都有).
         *
         * @return 构造器
         */
        public List<Interceptor> interceptors() {
            return interceptors;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            if (null == interceptor)
                throw new IllegalArgumentException("interceptor == null");
            interceptors.add(interceptor);
            return this;
        }

        /**
         * 返回观察单个网络请求和响应的可修改的拦截器列表。 这些拦截器必须调用{@link NewChain#proceed} 只执行一次:网络拦截器短路或重复网络请求是错误的
         *
         * @return 构造器
         */
        public List<Interceptor> networkInterceptors() {
            return networkInterceptors;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (null == interceptor)
                throw new IllegalArgumentException("interceptor == null");
            networkInterceptors.add(interceptor);
            return this;
        }

        /**
         * 配置单个客户机作用域侦听器，该侦听器将接收此客户机的所有分析事件.
         *
         * @param eventListener 监听器
         * @return 构造器
         */
        public Builder eventListener(EventListener eventListener) {
            if (null == eventListener)
                throw new NullPointerException("eventListener == null");
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        /**
         * 配置工厂以提供每个调用范围的侦听器，这些侦听器将接收此客户机的分析事件
         *
         * @param eventListenerFactory 监听工厂信息
         * @return 构造器
         */
        public Builder eventListenerFactory(EventListener.Factory eventListenerFactory) {
            if (null == eventListenerFactory) {
                throw new NullPointerException("eventListenerFactory == null");
            }
            this.eventListenerFactory = eventListenerFactory;
            return this;
        }

        public Httpd build() {
            return new Httpd(this);
        }
    }

}
