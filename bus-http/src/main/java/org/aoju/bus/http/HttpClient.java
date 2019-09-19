/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http;

import org.aoju.bus.http.internal.Internal;
import org.aoju.bus.http.internal.cache.InternalCache;
import org.aoju.bus.http.internal.connection.RealConnection;
import org.aoju.bus.http.internal.connection.RouteDatabase;
import org.aoju.bus.http.internal.connection.StreamAllocation;
import org.aoju.bus.http.internal.platform.Platform;
import org.aoju.bus.http.internal.proxy.NullProxySelector;
import org.aoju.bus.http.internal.tls.CertificateChainCleaner;
import org.aoju.bus.http.internal.tls.OkHostnameVerifier;
import org.aoju.bus.http.internal.ws.RealWebSocket;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Factory for {@linkplain Call calls}, which can be used to send HTTP requests and read their
 * responses.
 *
 * <h3>OkHttpClients should be shared</h3>
 *
 * <p>OkHttp performs best when you create a single {@code HttpClient} instance and reuse it for
 * all of your HTTP calls. This is because each client holds its own connection pool and thread
 * pools. Reusing connections and threads reduces latency and saves memory. Conversely, creating a
 * client for each request wastes resources on idle pools.
 *
 * <p>Use {@code new HttpClient()} to create a shared instance with the default settings:
 * <pre>   {@code
 *
 *   // The singleton HTTP client.
 *   public final HttpClient client = new HttpClient();
 * }</pre>
 *
 * <p>Or use {@code new HttpClient.Builder()} to create a shared instance with custom settings:
 * <pre>   {@code
 *
 *   // The singleton HTTP client.
 *   public final HttpClient client = new HttpClient.Builder()
 *       .addInterceptor(new HttpLoggingInterceptor())
 *       .cache(new Cache(cacheDir, cacheSize))
 *       .build();
 * }</pre>
 *
 * <h3>Customize your client with newBuilder()</h3>
 *
 * <p>You can customize a shared HttpClient instance with {@link #newBuilder()}. This builds a
 * client that shares the same connection pool, thread pools, and configuration. Use the builder
 * methods to configure the derived client for a specific purpose.
 *
 * <p>This example shows a call with a short 500 millisecond timeout: <pre>   {@code
 *
 *   HttpClient eagerClient = client.newBuilder()
 *       .readTimeout(500, TimeUnit.MILLISECONDS)
 *       .build();
 *   Response response = eagerClient.newCall(request).execute();
 * }</pre>
 *
 * <h3>Shutdown isn't necessary</h3>
 *
 * <p>The threads and connections that are held will be released automatically if they remain idle.
 * But if you are writing a application that needs to aggressively release unused resources you may
 * do so.
 *
 * <p>Shutdown the dispatcher's executor service with {@link ExecutorService#shutdown shutdown()}.
 * This will also cause future calls to the client to be rejected. <pre>   {@code
 *
 *     client.dispatcher().executorService().shutdown();
 * }</pre>
 *
 * <p>Clear the connection pool with {@link ConnectionPool#evictAll() evictAll()}. Note that the
 * connection pool's daemon thread may not exit immediately. <pre>   {@code
 *
 *     client.connectionPool().evictAll();
 * }</pre>
 *
 * <p>If your client has a cache, call {@link Cache#close close()}. Note that it is an error to
 * create calls against a cache that is closed, and doing so will cause the call to crash.
 * <pre>   {@code
 *
 *     client.cache().close();
 * }</pre>
 *
 * <p>OkHttp also uses daemon threads for HTTP/2 connections. These will exit automatically if they
 * remain idle.
 *
 * @author Kimi Liu
 * @version 3.5.1
 * @since JDK 1.8
 */
public class HttpClient implements Cloneable, Call.Factory, WebSocket.Factory {

    static final List<Protocol> DEFAULT_PROTOCOLS = Internal.immutableList(
            Protocol.HTTP_2, Protocol.HTTP_1_1);

    static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Internal.immutableList(
            ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);

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
            public void setCache(HttpClient.Builder builder, InternalCache internalCache) {
                builder.setInternalCache(internalCache);
            }

            @Override
            public boolean connectionBecameIdle(
                    ConnectionPool pool, RealConnection connection) {
                return pool.connectionBecameIdle(connection);
            }

            @Override
            public RealConnection get(ConnectionPool pool, Address address,
                                      StreamAllocation streamAllocation, Route route) {
                return pool.get(address, streamAllocation, route);
            }

            @Override
            public boolean equalsNonHost(Address a, Address b) {
                return a.equalsNonHost(b);
            }

            @Override
            public Socket deduplicate(
                    ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
                return pool.deduplicate(address, streamAllocation);
            }

            @Override
            public void put(ConnectionPool pool, RealConnection connection) {
                pool.put(connection);
            }

            @Override
            public RouteDatabase routeDatabase(ConnectionPool connectionPool) {
                return connectionPool.routeDatabase;
            }

            @Override
            public int code(Response.Builder responseBuilder) {
                return responseBuilder.code;
            }

            @Override
            public void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
                tlsConfiguration.apply(sslSocket, isFallback);
            }

            @Override
            public boolean isInvalidHttpUrlHost(IllegalArgumentException e) {
                return e.getMessage().startsWith(HttpUrl.Builder.INVALID_HOST);
            }

            @Override
            public StreamAllocation streamAllocation(Call call) {
                return ((RealCall) call).streamAllocation();
            }

            @Override
            public IOException timeoutExit(Call call, IOException e) {
                return ((RealCall) call).timeoutExit(e);
            }

            @Override
            public Call newWebSocketCall(HttpClient client, Request originalRequest) {
                return RealCall.newRealCall(client, originalRequest, true);
            }
        };
    }

    final Dispatcher dispatcher;
    final Proxy proxy;
    final List<Protocol> protocols;
    final List<ConnectionSpec> connectionSpecs;
    final List<Interceptor> interceptors;
    final List<Interceptor> networkInterceptors;
    final EventListener.Factory eventListenerFactory;
    final ProxySelector proxySelector;
    final CookieJar cookieJar;
    final Cache cache;
    final InternalCache internalCache;
    final SocketFactory socketFactory;
    final SSLSocketFactory sslSocketFactory;
    final CertificateChainCleaner certificateChainCleaner;
    final HostnameVerifier hostnameVerifier;
    final CertificatePinner certificatePinner;
    final Authenticator proxyAuthenticator;
    final Authenticator authenticator;
    final ConnectionPool connectionPool;
    final Dns dns;
    final boolean followSslRedirects;
    final boolean followRedirects;
    final boolean retryOnConnectionFailure;
    final int callTimeout;
    final int connectTimeout;
    final int readTimeout;
    final int writeTimeout;
    final int pingInterval;

    public HttpClient() {
        this(new Builder());
    }

    HttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.proxy = builder.proxy;
        this.protocols = builder.protocols;
        this.connectionSpecs = builder.connectionSpecs;
        this.interceptors = Internal.immutableList(builder.interceptors);
        this.networkInterceptors = Internal.immutableList(builder.networkInterceptors);
        this.eventListenerFactory = builder.eventListenerFactory;
        this.proxySelector = builder.proxySelector;
        this.cookieJar = builder.cookieJar;
        this.cache = builder.cache;
        this.internalCache = builder.internalCache;
        this.socketFactory = builder.socketFactory;

        boolean isTLS = false;
        for (ConnectionSpec spec : connectionSpecs) {
            isTLS = isTLS || spec.isTls();
        }

        if (builder.sslSocketFactory != null || !isTLS) {
            this.sslSocketFactory = builder.sslSocketFactory;
            this.certificateChainCleaner = builder.certificateChainCleaner;
        } else {
            X509TrustManager trustManager = Internal.platformTrustManager();
            this.sslSocketFactory = newSslSocketFactory(trustManager);
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
        }

        if (sslSocketFactory != null) {
            Platform.get().configureSslSocketFactory(sslSocketFactory);
        }

        this.hostnameVerifier = builder.hostnameVerifier;
        this.certificatePinner = builder.certificatePinner.withCertificateChainCleaner(
                certificateChainCleaner);
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
            throw new IllegalStateException("Null intercept: " + interceptors);
        }
        if (networkInterceptors.contains(null)) {
            throw new IllegalStateException("Null network intercept: " + networkInterceptors);
        }
    }

    private static SSLSocketFactory newSslSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = Platform.get().getSSLContext();
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw Internal.assertionError("No System TLS", e); // The system has no TLS. Just give up.
        }
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
        return cache != null ? cache.internalCache : internalCache;
    }

    public Dns dns() {
        return dns;
    }

    public SocketFactory socketFactory() {
        return socketFactory;
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public HostnameVerifier hostnameVerifier() {
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

    public List<ConnectionSpec> connectionSpecs() {
        return connectionSpecs;
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

    @Override
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request, false /* for web socket */);
    }

    @Override
    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), pingInterval);
        webSocket.connect(this);
        return webSocket;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        final List<Interceptor> interceptors = new ArrayList<>();
        final List<Interceptor> networkInterceptors = new ArrayList<>();
        Dispatcher dispatcher;
        Proxy proxy;
        List<Protocol> protocols;
        List<ConnectionSpec> connectionSpecs;
        EventListener.Factory eventListenerFactory;
        ProxySelector proxySelector;
        CookieJar cookieJar;
        Cache cache;
        InternalCache internalCache;
        SocketFactory socketFactory;
        SSLSocketFactory sslSocketFactory;
        CertificateChainCleaner certificateChainCleaner;
        HostnameVerifier hostnameVerifier;
        CertificatePinner certificatePinner;
        Authenticator proxyAuthenticator;
        Authenticator authenticator;
        ConnectionPool connectionPool;
        Dns dns;
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
            connectionSpecs = DEFAULT_CONNECTION_SPECS;
            eventListenerFactory = EventListener.factory(EventListener.NONE);
            proxySelector = ProxySelector.getDefault();
            if (proxySelector == null) {
                proxySelector = new NullProxySelector();
            }
            cookieJar = CookieJar.NO_COOKIES;
            socketFactory = SocketFactory.getDefault();
            hostnameVerifier = OkHostnameVerifier.INSTANCE;
            certificatePinner = CertificatePinner.DEFAULT;
            proxyAuthenticator = Authenticator.NONE;
            authenticator = Authenticator.NONE;
            connectionPool = new ConnectionPool();
            dns = Dns.SYSTEM;
            followSslRedirects = true;
            followRedirects = true;
            retryOnConnectionFailure = true;
            callTimeout = 0;
            connectTimeout = 30_000;
            readTimeout = 30_000;
            writeTimeout = 30_000;
            pingInterval = 0;
        }

        Builder(HttpClient httpClient) {
            this.dispatcher = httpClient.dispatcher;
            this.proxy = httpClient.proxy;
            this.protocols = httpClient.protocols;
            this.connectionSpecs = httpClient.connectionSpecs;
            this.interceptors.addAll(httpClient.interceptors);
            this.networkInterceptors.addAll(httpClient.networkInterceptors);
            this.eventListenerFactory = httpClient.eventListenerFactory;
            this.proxySelector = httpClient.proxySelector;
            this.cookieJar = httpClient.cookieJar;
            this.internalCache = httpClient.internalCache;
            this.cache = httpClient.cache;
            this.socketFactory = httpClient.socketFactory;
            this.sslSocketFactory = httpClient.sslSocketFactory;
            this.certificateChainCleaner = httpClient.certificateChainCleaner;
            this.hostnameVerifier = httpClient.hostnameVerifier;
            this.certificatePinner = httpClient.certificatePinner;
            this.proxyAuthenticator = httpClient.proxyAuthenticator;
            this.authenticator = httpClient.authenticator;
            this.connectionPool = httpClient.connectionPool;
            this.dns = httpClient.dns;
            this.followSslRedirects = httpClient.followSslRedirects;
            this.followRedirects = httpClient.followRedirects;
            this.retryOnConnectionFailure = httpClient.retryOnConnectionFailure;
            this.callTimeout = httpClient.callTimeout;
            this.connectTimeout = httpClient.connectTimeout;
            this.readTimeout = httpClient.readTimeout;
            this.writeTimeout = httpClient.writeTimeout;
            this.pingInterval = httpClient.pingInterval;
        }


        public Builder callTimeout(long timeout, TimeUnit unit) {
            callTimeout = Internal.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder callTimeout(Duration duration) {
            callTimeout = Internal.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder connectTimeout(long timeout, TimeUnit unit) {
            connectTimeout = Internal.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder connectTimeout(Duration duration) {
            connectTimeout = Internal.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder readTimeout(long timeout, TimeUnit unit) {
            readTimeout = Internal.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder readTimeout(Duration duration) {
            readTimeout = Internal.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder writeTimeout(long timeout, TimeUnit unit) {
            writeTimeout = Internal.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder writeTimeout(Duration duration) {
            writeTimeout = Internal.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder pingInterval(long interval, TimeUnit unit) {
            pingInterval = Internal.checkDuration("interval", interval, unit);
            return this;
        }

        public Builder pingInterval(Duration duration) {
            pingInterval = Internal.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxySelector(ProxySelector proxySelector) {
            if (proxySelector == null) throw new NullPointerException("proxySelector == null");
            this.proxySelector = proxySelector;
            return this;
        }

        public Builder cookieJar(CookieJar cookieJar) {
            if (cookieJar == null) throw new NullPointerException("cookieJar == null");
            this.cookieJar = cookieJar;
            return this;
        }

        void setInternalCache(InternalCache internalCache) {
            this.internalCache = internalCache;
            this.cache = null;
        }

        public Builder cache(Cache cache) {
            this.cache = cache;
            this.internalCache = null;
            return this;
        }

        public Builder dns(Dns dns) {
            if (dns == null) throw new NullPointerException("dns == null");
            this.dns = dns;
            return this;
        }

        public Builder socketFactory(SocketFactory socketFactory) {
            if (socketFactory == null) throw new NullPointerException("socketFactory == null");
            this.socketFactory = socketFactory;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (sslSocketFactory == null) throw new NullPointerException("sslSocketFactory == null");
            this.sslSocketFactory = sslSocketFactory;
            this.certificateChainCleaner = Platform.get().buildCertificateChainCleaner(sslSocketFactory);
            return this;
        }

        public Builder sslSocketFactory(
                SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
            if (sslSocketFactory == null) throw new NullPointerException("sslSocketFactory == null");
            if (trustManager == null) throw new NullPointerException("trustManager == null");
            this.sslSocketFactory = sslSocketFactory;
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            if (hostnameVerifier == null) throw new NullPointerException("hostnameVerifier == null");
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder certificatePinner(CertificatePinner certificatePinner) {
            if (certificatePinner == null) throw new NullPointerException("certificatePinner == null");
            this.certificatePinner = certificatePinner;
            return this;
        }

        public Builder authenticator(Authenticator authenticator) {
            if (authenticator == null) throw new NullPointerException("authenticator == null");
            this.authenticator = authenticator;
            return this;
        }

        public Builder proxyAuthenticator(Authenticator proxyAuthenticator) {
            if (proxyAuthenticator == null) throw new NullPointerException("proxyAuthenticator == null");
            this.proxyAuthenticator = proxyAuthenticator;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            if (connectionPool == null) throw new NullPointerException("connectionPool == null");
            this.connectionPool = connectionPool;
            return this;
        }

        public Builder followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Builder dispatcher(Dispatcher dispatcher) {
            if (dispatcher == null) throw new IllegalArgumentException("dispatcher == null");
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * Configure the protocols used by this client to communicate with remote servers. By default
         * this client will prefer the most efficient transport available, falling back to more
         * ubiquitous protocols. Applications should only call this method to avoid specific
         * compatibility problems, such as web servers that behave incorrectly when HTTP/2 is enabled.
         *
         * <p>The following protocols are currently supported:
         *
         * <ul>
         * <li><a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">http/1.1</a>
         * <li><a href="https://tools.ietf.org/html/rfc7540">h2</a>
         * <li><a href="https://tools.ietf.org/html/rfc7540#section-3.4">h2 with prior knowledge
         * (cleartext only)</a>
         * </ul>
         *
         * <p><strong>This is an evolving set.</strong> Future releases include support for transitional
         * protocols. The http/1.1 transport will never be dropped.
         *
         * <p>If multiple protocols are specified, <a
         * href="http://tools.ietf.org/html/draft-ietf-tls-applayerprotoneg">ALPN</a> will be used to
         * negotiate a transport. Protocol negotiation is only attempted for HTTPS URLs.
         *
         * <p>{@link Protocol#HTTP_1_0} is not supported in this set. Requests are initiated with {@code
         * HTTP/1.1}. If the server responds with {@code HTTP/1.0}, that will be exposed by {@link
         * Response#protocol()}.
         *
         * @param protocols the protocols to use, in order of preference. If the list contains {@link
         *                  Protocol#H2_PRIOR_KNOWLEDGE} then that must be the only protocol and HTTPS URLs will not
         *                  be supported. Otherwise the list must contain {@link Protocol#HTTP_1_1}. The list must
         *                  not contain null or {@link Protocol#HTTP_1_0}.
         * @return Builder
         */
        public Builder protocols(List<Protocol> protocols) {
            // Create a private copy of the list.
            protocols = new ArrayList<>(protocols);

            // Validate that the list has everything we require and nothing we forbid.
            if (!protocols.contains(Protocol.H2_PRIOR_KNOWLEDGE)
                    && !protocols.contains(Protocol.HTTP_1_1)) {
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

            // Remove protocols that we no longer support.
            protocols.remove(Protocol.SPDY_3);

            // Assign as an unmodifiable list. This is effectively immutable.
            this.protocols = Collections.unmodifiableList(protocols);
            return this;
        }

        public Builder connectionSpecs(List<ConnectionSpec> connectionSpecs) {
            this.connectionSpecs = Internal.immutableList(connectionSpecs);
            return this;
        }

        public List<Interceptor> interceptors() {
            return interceptors;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor == null) throw new IllegalArgumentException("intercept == null");
            interceptors.add(interceptor);
            return this;
        }

        public List<Interceptor> networkInterceptors() {
            return networkInterceptors;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (interceptor == null) throw new IllegalArgumentException("intercept == null");
            networkInterceptors.add(interceptor);
            return this;
        }

        public Builder eventListener(EventListener eventListener) {
            if (eventListener == null) throw new NullPointerException("eventListener == null");
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        public Builder eventListenerFactory(EventListener.Factory eventListenerFactory) {
            if (eventListenerFactory == null) {
                throw new NullPointerException("eventListenerFactory == null");
            }
            this.eventListenerFactory = eventListenerFactory;
            return this;
        }

        public HttpClient build() {
            return new HttpClient(this);
        }
    }

}
