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

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.AnyHostnameVerifier;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.accord.*;
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
 * The core client for making HTTP requests and reading their responses. This class is designed to be efficient by
 * reusing connections with a connection pool. It is recommended to create a single, shared instance of {@code Httpd}
 * and reuse it for all HTTP calls.
 *
 * <p>
 * This class supports both synchronous and asynchronous calls. Asynchronous calls are executed on a background thread
 * pool, which is managed by a {@link Dispatcher}.
 * <p>
 * This class also supports advanced features like HTTP/2 and WebSockets.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpd implements Cloneable, NewCall.Factory, WebSocket.Factory {

    /**
     * The default supported protocols, including HTTP/2 and HTTP/1.1.
     */
    static final List<Protocol> DEFAULT_PROTOCOLS = org.miaixz.bus.http.Builder
            .immutableList(Protocol.HTTP_2, Protocol.HTTP_1_1);

    /**
     * The default connection specifications, including modern TLS and cleartext.
     */
    static final List<ConnectionSuite> DEFAULT_CONNECTION_SPECS = org.miaixz.bus.http.Builder
            .immutableList(ConnectionSuite.MODERN_TLS, ConnectionSuite.CLEARTEXT);

    /**
     * Initializes the internal API instance for package-private access.
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
     * The dispatcher that manages the execution of asynchronous requests.
     */
    final Dispatcher dispatcher;
    /**
     * The proxy to use for network connections. If null, a proxy will be selected by the proxySelector.
     */
    final Proxy proxy;
    /**
     * The list of protocols to negotiate when connecting to a remote server.
     */
    final List<Protocol> protocols;
    /**
     * The list of connection specs to use when establishing a connection.
     */
    final List<ConnectionSuite> connectionSuites;
    /**
     * An immutable list of interceptors that observe the full span of each call.
     */
    final List<Interceptor> interceptors;
    /**
     * An immutable list of interceptors that observe a single network request and response.
     */
    final List<Interceptor> networkInterceptors;
    /**
     * The factory for creating event listeners for monitoring request lifecycle events.
     */
    final EventListener.Factory eventListenerFactory;
    /**
     * The selector for choosing a proxy for a given URI.
     */
    final ProxySelector proxySelector;
    /**
     * The cookie jar for managing HTTP cookies.
     */
    final CookieJar cookieJar;
    /**
     * The cache for storing and retrieving responses.
     */
    final Cache cache;
    /**
     * The internal cache interface for custom caching implementations.
     */
    final InternalCache internalCache;
    /**
     * The socket factory for creating plain TCP connections.
     */
    final SocketFactory socketFactory;
    /**
     * The SSL socket factory for creating HTTPS connections.
     */
    final SSLSocketFactory sslSocketFactory;
    /**
     * The cleaner for normalizing certificate chains.
     */
    final CertificateChainCleaner certificateChainCleaner;
    /**
     * The verifier for checking hostnames in HTTPS connections.
     */
    final javax.net.ssl.HostnameVerifier hostnameVerifier;
    /**
     * The pinner for restricting which certificates are trusted.
     */
    final CertificatePinner certificatePinner;
    /**
     * The authenticator for handling challenges from proxy servers.
     */
    final Authenticator proxyAuthenticator;
    /**
     * The authenticator for handling challenges from origin servers.
     */
    final Authenticator authenticator;
    /**
     * The connection pool for managing and reusing HTTP and HTTPS connections.
     */
    final ConnectionPool connectionPool;
    /**
     * The DNS service for resolving hostnames to IP addresses.
     */
    final DnsX dns;
    /**
     * Whether to follow redirects from HTTPS to HTTP and vice versa.
     */
    final boolean followSslRedirects;
    /**
     * Whether to follow HTTP redirects.
     */
    final boolean followRedirects;
    /**
     * Whether to retry the request when a connection failure occurs.
     */
    final boolean retryOnConnectionFailure;
    /**
     * The default timeout for the entire call, in milliseconds.
     */
    final int callTimeout;
    /**
     * The default timeout for establishing a new connection, in milliseconds.
     */
    final int connectTimeout;
    /**
     * The default timeout for reading data from a connection, in milliseconds.
     */
    final int readTimeout;
    /**
     * The default timeout for writing data to a connection, in milliseconds.
     */
    final int writeTimeout;
    /**
     * The interval for sending WebSocket pings, in milliseconds.
     */
    final int pingInterval;

    /**
     * Constructs a new {@code Httpd} instance with default settings.
     */
    public Httpd() {
        this(new Builder());
    }

    /**
     * Constructs a new {@code Httpd} instance configured by the given {@link Builder}.
     *
     * @param builder The builder instance containing all configuration parameters.
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
     * Prepares the {@code request} to be executed at some point in the future.
     *
     * @param request The HTTP request to execute.
     * @return a new {@link NewCall} instance that can be used to execute the request.
     */
    @Override
    public NewCall newCall(Request request) {
        return RealCall.newRealCall(this, request, false /* for web socket */);
    }

    /**
     * Uses {@code request} to connect a new web socket.
     *
     * @param request  The WebSocket request object.
     * @param listener The WebSocket event listener.
     * @return a new {@link WebSocket} instance.
     */
    @Override
    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), pingInterval);
        webSocket.connect(this);
        return webSocket;
    }

    /**
     * Returns the default timeout for complete calls in milliseconds.
     *
     * @return the call timeout in milliseconds.
     */
    public int callTimeoutMillis() {
        return callTimeout;
    }

    /**
     * Returns the default timeout for establishing a new connection in milliseconds.
     *
     * @return the connect timeout in milliseconds.
     */
    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    /**
     * Returns the default timeout for reading data from a connection in milliseconds.
     *
     * @return the read timeout in milliseconds.
     */
    public int readTimeoutMillis() {
        return readTimeout;
    }

    /**
     * Returns the default timeout for writing data to a connection in milliseconds.
     *
     * @return the write timeout in milliseconds.
     */
    public int writeTimeoutMillis() {
        return writeTimeout;
    }

    /**
     * Returns the WebSocket ping interval in milliseconds.
     *
     * @return the ping interval in milliseconds.
     */
    public int pingIntervalMillis() {
        return pingInterval;
    }

    /**
     * Returns the configured HTTP proxy, or null if no proxy is configured.
     *
     * @return the proxy instance, which may be null.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Returns the proxy selector used to choose a proxy for a given URI.
     *
     * @return the proxy selector.
     */
    public ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * Returns the cookie jar used to manage HTTP cookies.
     *
     * @return the {@link CookieJar} instance.
     */
    public CookieJar cookieJar() {
        return cookieJar;
    }

    /**
     * Returns the cache used to store responses.
     *
     * @return the {@link Cache} instance, which may be null.
     */
    public Cache cache() {
        return cache;
    }

    /**
     * Returns the internal cache interface.
     *
     * @return the {@link InternalCache} instance, which may be null.
     */
    InternalCache internalCache() {
        return null != cache ? cache.internalCache : internalCache;
    }

    /**
     * Returns the DNS service used to resolve hostnames.
     *
     * @return the {@link DnsX} instance.
     */
    public DnsX dns() {
        return dns;
    }

    /**
     * Returns the socket factory used for creating plain TCP connections.
     *
     * @return the {@link SocketFactory} instance.
     */
    public SocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * Returns the SSL socket factory used for creating HTTPS connections.
     *
     * @return the {@link SSLSocketFactory} instance.
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Returns the hostname verifier used for HTTPS connections.
     *
     * @return the {@link javax.net.ssl.HostnameVerifier} instance.
     */
    public javax.net.ssl.HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Returns the certificate pinner used to constrain trusted certificates.
     *
     * @return the {@link CertificatePinner} instance.
     */
    public CertificatePinner certificatePinner() {
        return certificatePinner;
    }

    /**
     * Returns the authenticator for handling challenges from origin servers.
     *
     * @return the {@link Authenticator} instance.
     */
    public Authenticator authenticator() {
        return authenticator;
    }

    /**
     * Returns the authenticator for handling challenges from proxy servers.
     *
     * @return the {@link Authenticator} instance.
     */
    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    /**
     * Returns the connection pool for managing and reusing connections.
     *
     * @return the {@link ConnectionPool} instance.
     */
    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    /**
     * Returns true if this client follows redirects from HTTPS to HTTP and vice versa.
     *
     * @return true if SSL redirects are followed.
     */
    public boolean followSslRedirects() {
        return followSslRedirects;
    }

    /**
     * Returns true if this client follows HTTP redirects.
     *
     * @return true if redirects are followed.
     */
    public boolean followRedirects() {
        return followRedirects;
    }

    /**
     * Returns true if this client retries requests on connection failures.
     *
     * @return true if retry on connection failure is enabled.
     */
    public boolean retryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    /**
     * Returns the dispatcher that manages asynchronous requests.
     *
     * @return the {@link Dispatcher} instance.
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * Returns the list of protocols supported by this client.
     *
     * @return an immutable list of protocols.
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    /**
     * Returns the list of connection specs supported by this client.
     *
     * @return an immutable list of connection specs.
     */
    public List<ConnectionSuite> connectionSpecs() {
        return connectionSuites;
    }

    /**
     * Returns the list of application interceptors.
     *
     * @return an immutable list of interceptors.
     */
    public List<Interceptor> interceptors() {
        return interceptors;
    }

    /**
     * Returns the list of network interceptors.
     *
     * @return an immutable list of network interceptors.
     */
    public List<Interceptor> networkInterceptors() {
        return networkInterceptors;
    }

    /**
     * Returns the factory for creating event listeners.
     *
     * @return the {@link EventListener.Factory} instance.
     */
    public EventListener.Factory eventListenerFactory() {
        return eventListenerFactory;
    }

    /**
     * Returns a new builder that is a copy of this client's configuration.
     *
     * @return a new {@link Builder} instance.
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * A builder for configuring and creating {@link Httpd} instances.
     */
    public static class Builder {

        /**
         * The list of application interceptors.
         */
        final List<Interceptor> interceptors = new ArrayList<>();
        /**
         * The list of network interceptors.
         */
        final List<Interceptor> networkInterceptors = new ArrayList<>();
        /**
         * The dispatcher for managing asynchronous requests.
         */
        Dispatcher dispatcher;
        /**
         * The HTTP proxy configuration.
         */
        Proxy proxy;
        /**
         * The list of supported protocols.
         */
        List<Protocol> protocols;
        /**
         * The list of supported connection specifications.
         */
        List<ConnectionSuite> connectionSuites;
        /**
         * The factory for creating event listeners.
         */
        EventListener.Factory eventListenerFactory;
        /**
         * The selector for choosing a proxy.
         */
        ProxySelector proxySelector;
        /**
         * The manager for handling cookies.
         */
        CookieJar cookieJar;
        /**
         * The cache instance for storing responses.
         */
        Cache cache;
        /**
         * The internal cache interface.
         */
        InternalCache internalCache;
        /**
         * The factory for creating sockets.
         */
        SocketFactory socketFactory;
        /**
         * The factory for creating SSL sockets.
         */
        SSLSocketFactory sslSocketFactory;
        /**
         * The cleaner for certificate chains.
         */
        CertificateChainCleaner certificateChainCleaner;
        /**
         * The verifier for hostnames.
         */
        javax.net.ssl.HostnameVerifier hostnameVerifier;
        /**
         * The pinner for certificates.
         */
        CertificatePinner certificatePinner;
        /**
         * The authenticator for proxy servers.
         */
        Authenticator proxyAuthenticator;
        /**
         * The authenticator for origin servers.
         */
        Authenticator authenticator;
        /**
         * The connection pool for reusing connections.
         */
        ConnectionPool connectionPool;
        /**
         * The DNS service.
         */
        DnsX dns;
        /**
         * Whether to follow SSL redirects.
         */
        boolean followSslRedirects;
        /**
         * Whether to follow HTTP redirects.
         */
        boolean followRedirects;
        /**
         * Whether to retry on connection failure.
         */
        boolean retryOnConnectionFailure;
        /**
         * The call timeout in milliseconds.
         */
        int callTimeout;
        /**
         * The connection timeout in milliseconds.
         */
        int connectTimeout;
        /**
         * The read timeout in milliseconds.
         */
        int readTimeout;
        /**
         * The write timeout in milliseconds.
         */
        int writeTimeout;
        /**
         * The WebSocket ping interval in milliseconds.
         */
        int pingInterval;

        /**
         * Default constructor which initializes with default settings.
         */
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
            connectTimeout = 10_000;
            readTimeout = 10_000;
            writeTimeout = 10_000;
            pingInterval = 0;
        }

        /**
         * Constructor that initializes the builder with the settings of an existing {@link Httpd} instance.
         *
         * @param httpd The {@link Httpd} instance to copy settings from.
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
         * Sets the default timeout for complete calls. A value of 0 means no timeout.
         *
         * @param timeout the timeout value.
         * @param unit    the time unit.
         * @return this builder instance.
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder callTimeout(long timeout, TimeUnit unit) {
            callTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * Sets the default timeout for complete calls. A value of 0 means no timeout.
         *
         * @param duration the timeout duration.
         * @return this builder instance.
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder callTimeout(Duration duration) {
            callTimeout = org.miaixz.bus.http.Builder
                    .checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the default connect timeout for new connections. A value of 0 means no timeout.
         *
         * @param timeout the timeout value.
         * @param unit    the time unit.
         * @return this builder instance.
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder connectTimeout(long timeout, TimeUnit unit) {
            connectTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * Sets the default connect timeout for new connections. A value of 0 means no timeout.
         *
         * @param duration the timeout duration.
         * @return this builder instance.
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder connectTimeout(Duration duration) {
            connectTimeout = org.miaixz.bus.http.Builder
                    .checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout.
         *
         * @param timeout the timeout value.
         * @param unit    the time unit.
         * @return this builder instance.
         * @see Socket#setSoTimeout(int)
         * @see Source#timeout()
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder readTimeout(long timeout, TimeUnit unit) {
            readTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout.
         *
         * @param duration the timeout duration.
         * @return this builder instance.
         * @see Socket#setSoTimeout(int)
         * @see Source#timeout()
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder readTimeout(Duration duration) {
            readTimeout = org.miaixz.bus.http.Builder
                    .checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the default write timeout for new connections. A value of 0 means no timeout.
         *
         * @param timeout the timeout value.
         * @param unit    the time unit.
         * @return this builder instance.
         * @see Sink#timeout()
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder writeTimeout(long timeout, TimeUnit unit) {
            writeTimeout = org.miaixz.bus.http.Builder.checkDuration("timeout", timeout, unit);
            return this;
        }

        /**
         * Sets the default write timeout for new connections. A value of 0 means no timeout.
         *
         * @param duration the timeout duration.
         * @return this builder instance.
         * @see Sink#timeout()
         * @throws IllegalArgumentException if the timeout value is invalid.
         */
        public Builder writeTimeout(Duration duration) {
            writeTimeout = org.miaixz.bus.http.Builder
                    .checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the WebSocket ping interval. A value of 0 disables pings.
         *
         * @param interval the interval value.
         * @param unit     the time unit.
         * @return this builder instance.
         * @throws IllegalArgumentException if the interval value is invalid.
         */
        public Builder pingInterval(long interval, TimeUnit unit) {
            pingInterval = org.miaixz.bus.http.Builder.checkDuration("interval", interval, unit);
            return this;
        }

        /**
         * Sets the WebSocket ping interval. A value of 0 disables pings.
         *
         * @param duration the interval duration.
         * @return this builder instance.
         * @throws IllegalArgumentException if the interval value is invalid.
         */
        public Builder pingInterval(Duration duration) {
            pingInterval = org.miaixz.bus.http.Builder
                    .checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the HTTP proxy that will be used by connections created by this client.
         *
         * @param proxy the HTTP proxy.
         * @return this builder instance.
         */
        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * Sets the proxy selector that will be used to select the HTTP proxy for each request.
         *
         * @param proxySelector the proxy selector.
         * @return this builder instance.
         * @throws NullPointerException if proxySelector is null.
         */
        public Builder proxySelector(ProxySelector proxySelector) {
            if (null == proxySelector)
                throw new NullPointerException("proxySelector == null");
            this.proxySelector = proxySelector;
            return this;
        }

        /**
         * Sets the cookie jar that will be used to manage cookies for all HTTP requests.
         *
         * @param cookieJar the cookie jar.
         * @return this builder instance.
         * @throws NullPointerException if cookieJar is null.
         */
        public Builder cookieJar(CookieJar cookieJar) {
            if (null == cookieJar)
                throw new NullPointerException("cookieJar == null");
            this.cookieJar = cookieJar;
            return this;
        }

        /**
         * Sets the cache to be used to read and write cached responses.
         *
         * @param cache the cache instance.
         * @return this builder instance.
         */
        public Builder cache(Cache cache) {
            this.cache = cache;
            this.internalCache = null;
            return this;
        }

        /**
         * Sets the DNS service used to lookup IP addresses for hostnames.
         *
         * @param dns the DNS service.
         * @return this builder instance.
         * @throws NullPointerException if dns is null.
         */
        public Builder dns(DnsX dns) {
            if (null == dns)
                throw new NullPointerException("dns == null");
            this.dns = dns;
            return this;
        }

        /**
         * Sets the socket factory used to create connections.
         *
         * @param socketFactory the socket factory.
         * @return this builder instance.
         * @throws NullPointerException     if socketFactory is null.
         * @throws IllegalArgumentException if socketFactory is an instance of {@link SSLSocketFactory}.
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
         * Sets the SSL socket factory used to create HTTPS connections.
         *
         * @param sslSocketFactory the SSL socket factory.
         * @return this builder instance.
         * @throws NullPointerException if sslSocketFactory is null.
         */
        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (null == sslSocketFactory)
                throw new NullPointerException("sslSocketFactory == null");
            this.sslSocketFactory = sslSocketFactory;
            this.certificateChainCleaner = Platform.get().buildCertificateChainCleaner(sslSocketFactory);
            return this;
        }

        /**
         * Sets the SSL socket factory and trust manager used for HTTPS connections.
         *
         * <p>
         * Most applications should not call this method, and instead use the system defaults.
         *
         * @param sslSocketFactory the SSL socket factory.
         * @param trustManager     the X.509 trust manager.
         * @return this builder instance.
         * @throws NullPointerException if sslSocketFactory or trustManager is null.
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
         * Sets the verifier used to confirm that response certificates apply to the requested hostnames for HTTPS
         * connections.
         *
         * @param hostnameVerifier the hostname verifier.
         * @return this builder instance.
         * @throws NullPointerException if hostnameVerifier is null.
         */
        public Builder hostnameVerifier(javax.net.ssl.HostnameVerifier hostnameVerifier) {
            if (null == hostnameVerifier)
                throw new NullPointerException("hostnameVerifier == null");
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * Sets the certificate pinner that constrains which certificates are trusted.
         *
         * @param certificatePinner the certificate pinner.
         * @return this builder instance.
         * @throws NullPointerException if certificatePinner is null.
         */
        public Builder certificatePinner(CertificatePinner certificatePinner) {
            if (null == certificatePinner)
                throw new NullPointerException("certificatePinner == null");
            this.certificatePinner = certificatePinner;
            return this;
        }

        /**
         * Sets the authenticator used to respond to challenges from origin servers.
         *
         * @param authenticator the authenticator.
         * @return this builder instance.
         * @throws NullPointerException if authenticator is null.
         */
        public Builder authenticator(Authenticator authenticator) {
            if (null == authenticator)
                throw new NullPointerException("authenticator == null");
            this.authenticator = authenticator;
            return this;
        }

        /**
         * Sets the authenticator used to respond to challenges from proxy servers.
         *
         * @param proxyAuthenticator the proxy authenticator.
         * @return this builder instance.
         * @throws NullPointerException if proxyAuthenticator is null.
         */
        public Builder proxyAuthenticator(Authenticator proxyAuthenticator) {
            if (null == proxyAuthenticator)
                throw new NullPointerException("proxyAuthenticator == null");
            this.proxyAuthenticator = proxyAuthenticator;
            return this;
        }

        /**
         * Sets the connection pool used to recycle HTTP and HTTPS connections.
         *
         * @param connectionPool the connection pool.
         * @return this builder instance.
         * @throws NullPointerException if connectionPool is null.
         */
        public Builder connectionPool(ConnectionPool connectionPool) {
            if (null == connectionPool)
                throw new NullPointerException("connectionPool == null");
            this.connectionPool = connectionPool;
            return this;
        }

        /**
         * Configures this client to follow redirects from HTTPS to HTTP and from HTTP to HTTPS.
         *
         * @param followProtocolRedirects whether to follow SSL redirects.
         * @return this builder instance.
         */
        public Builder followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        /**
         * Configures this client to follow redirects.
         *
         * @param followRedirects whether to follow HTTP redirects.
         * @return this builder instance.
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /**
         * Configures this client to retry or not to retry when a connectivity problem is encountered.
         *
         * @param retryOnConnectionFailure whether to retry on connection failure.
         * @return this builder instance.
         */
        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        /**
         * Sets the dispatcher used to execute asynchronous requests.
         *
         * @param dispatcher the dispatcher.
         * @return this builder instance.
         * @throws IllegalArgumentException if dispatcher is null.
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            if (null == dispatcher)
                throw new IllegalArgumentException("dispatcher == null");
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * Sets the protocols used for requests.
         *
         * @param protocols the list of protocols.
         * @return this builder instance.
         * @throws IllegalArgumentException if the protocols list is invalid.
         */
        public Builder protocols(List<Protocol> protocols) {
            protocols = new ArrayList<>(protocols);

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

            protocols.remove(Protocol.SPDY_3);

            this.protocols = Collections.unmodifiableList(protocols);
            return this;
        }

        /**
         * Sets the connection specs for this client.
         *
         * @param connectionSuites the list of connection specs.
         * @return this builder instance.
         */
        public Builder connectionSpecs(List<ConnectionSuite> connectionSuites) {
            this.connectionSuites = org.miaixz.bus.http.Builder.immutableList(connectionSuites);
            return this;
        }

        /**
         * Returns a mutable list of the application interceptors that observe the full span of each call.
         *
         * @return the list of application interceptors.
         */
        public List<Interceptor> interceptors() {
            return interceptors;
        }

        /**
         * Adds an application interceptor.
         *
         * @param interceptor the application interceptor.
         * @return this builder instance.
         * @throws IllegalArgumentException if interceptor is null.
         */
        public Builder addInterceptor(Interceptor interceptor) {
            if (null == interceptor)
                throw new IllegalArgumentException("interceptor == null");
            interceptors.add(interceptor);
            return this;
        }

        /**
         * Returns a mutable list of the network interceptors that observe a single network request and response.
         *
         * @return the list of network interceptors.
         */
        public List<Interceptor> networkInterceptors() {
            return networkInterceptors;
        }

        /**
         * Adds a network interceptor.
         *
         * @param interceptor the network interceptor.
         * @return this builder instance.
         * @throws IllegalArgumentException if interceptor is null.
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (null == interceptor)
                throw new IllegalArgumentException("interceptor == null");
            networkInterceptors.add(interceptor);
            return this;
        }

        /**
         * Sets the event listener for monitoring request lifecycle events.
         *
         * @param eventListener the event listener.
         * @return this builder instance.
         * @throws NullPointerException if eventListener is null.
         */
        public Builder eventListener(EventListener eventListener) {
            if (null == eventListener)
                throw new NullPointerException("eventListener == null");
            this.eventListenerFactory = EventListener.factory(eventListener);
            return this;
        }

        /**
         * Sets the factory for creating event listeners.
         *
         * @param eventListenerFactory the event listener factory.
         * @return this builder instance.
         * @throws NullPointerException if eventListenerFactory is null.
         */
        public Builder eventListenerFactory(EventListener.Factory eventListenerFactory) {
            if (null == eventListenerFactory) {
                throw new NullPointerException("eventListenerFactory == null");
            }
            this.eventListenerFactory = eventListenerFactory;
            return this;
        }

        /**
         * Builds a new {@link Httpd} instance with the configured settings.
         *
         * @return a new {@link Httpd} instance.
         */
        public Httpd build() {
            return new Httpd(this);
        }
    }

}
