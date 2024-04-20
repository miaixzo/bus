/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.http.accord;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.exception.RevisedException;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.tls.HostnameVerifier;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.platform.Platform;
import org.miaixz.bus.http.metric.EventListener;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.http.*;
import org.miaixz.bus.http.secure.CertificatePinner;
import org.miaixz.bus.http.socket.Handshake;
import org.miaixz.bus.http.socket.RealWebSocket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 连接提供
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealConnection extends Http2Connection.Listener implements Connection {

    private static final String NPE_THROW_WITH_NULL = "throw with null exception";
    private static final int MAX_TUNNEL_ATTEMPTS = 21;

    public final RealConnectionPool connectionPool;
    /**
     * 由该连接传送的当前流
     */
    final List<Reference<Transmitter>> transmitters = new ArrayList<>();

    /**
     * 下面的字段由connect()初始化，并且从不重新分配
     */
    private final Route route;
    /**
     * 如果为真，则不能在此连接上创建新的流
     */
    boolean noNewExchanges;
    /**
     * The number of times there was a problem establishing a stream that could be due to route
     * chosen. Guarded by {@link #connectionPool}.
     */
    int routeFailureCount;
    int successCount;
    /**
     * 当{@code allocations.size()}达到0时的Nanotime时间戳
     */
    long idleAtNanos = Long.MAX_VALUE;
    /**
     * 低级TCP套接字
     */
    private Socket rawSocket;
    /**
     * 应用层套接字，如果该连接不使用SSL，则可以使用位于
     * {@link #rawSocket}之上的{@link SSLSocket}或{@link #rawSocket}本身
     */
    private Socket socket;
    /**
     * 下面的字段处于连接状态，并由connectionPool保护
     */
    private Handshake handshake;
    private Protocol protocol;
    private Http2Connection http2Connection;
    private BufferSource source;
    private BufferSink sink;
    private int refusedStreamCount;
    /**
     * 此连接可承载的并发流的最大数目如果
     * {@code allocations.size() < allocationLimit}
     * 则可以在此连接上创建新的流
     */
    private int allocationLimit = 1;

    public RealConnection(RealConnectionPool connectionPool, Route route) {
        this.connectionPool = connectionPool;
        this.route = route;
    }

    static RealConnection testConnection(
            RealConnectionPool connectionPool, Route route, Socket socket, long idleAtNanos) {
        RealConnection result = new RealConnection(connectionPool, route);
        result.socket = socket;
        result.idleAtNanos = idleAtNanos;
        return result;
    }

    /**
     * Prevent further exchanges from being created on this connection.
     */
    public void noNewExchanges() {
        assert (!Thread.holdsLock(connectionPool));
        synchronized (connectionPool) {
            noNewExchanges = true;
        }
    }

    public void connect(int connectTimeout, int readTimeout, int writeTimeout,
                        int pingIntervalMillis, boolean connectionRetryEnabled, NewCall call,
                        EventListener eventListener) {
        if (protocol != null) throw new IllegalStateException("already connected");

        RouteException routeException = null;
        List<ConnectionSuite> connectionSuites = route.address().connectionSpecs();
        ConnectionSelector connectionSelector = new ConnectionSelector(connectionSuites);

        if (route.address().sslSocketFactory() == null) {
            if (!connectionSuites.contains(ConnectionSuite.CLEARTEXT)) {
                throw new RouteException(new UnknownServiceException(
                        "CLEARTEXT communication not enabled for client"));
            }
            String host = route.address().url().host();
            if (!Platform.get().isCleartextTrafficPermitted(host)) {
                throw new RouteException(new UnknownServiceException(
                        "CLEARTEXT communication to " + host + " not permitted by network security policy"));
            }
        } else {
            if (route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
                throw new RouteException(new UnknownServiceException(
                        "H2_PRIOR_KNOWLEDGE cannot be used with HTTPS"));
            }
        }

        while (true) {
            try {
                if (route.requiresTunnel()) {
                    connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);
                    if (null == rawSocket) {
                        // 我们无法连接隧道，但适当地关闭了我们的资源
                        break;
                    }
                } else {
                    connectSocket(connectTimeout, readTimeout, call, eventListener);
                }
                establishProtocol(connectionSelector, pingIntervalMillis, call, eventListener);
                eventListener.connectEnd(call, route.socketAddress(), route.proxy(), protocol);
                break;
            } catch (IOException e) {
                IoKit.close(socket);
                IoKit.close(rawSocket);
                socket = null;
                rawSocket = null;
                source = null;
                sink = null;
                handshake = null;
                protocol = null;
                http2Connection = null;

                eventListener.connectFailed(call, route.socketAddress(), route.proxy(), null, e);

                if (null == routeException) {
                    routeException = new RouteException(e);
                } else {
                    routeException.addConnectException(e);
                }

                if (!connectionRetryEnabled || !connectionSelector.connectionFailed(e)) {
                    throw routeException;
                }
            }
        }

        if (route.requiresTunnel() && null == rawSocket) {
            ProtocolException exception = new ProtocolException("Too many tunnel connections attempted: "
                    + MAX_TUNNEL_ATTEMPTS);
            throw new RouteException(exception);
        }

        if (null != http2Connection) {
            synchronized (connectionPool) {
                allocationLimit = http2Connection.maxConcurrentStreams();
            }
        }
    }

    /**
     * 完成在代理通道上构建HTTPS连接的所有工作。
     * 这里的问题是，代理服务器可以发出验证请求，然后关闭连接
     *
     * @param connectTimeout 连接超时时间
     * @param readTimeout    读取超时时间
     * @param writeTimeout   写入超时时间
     * @param call           调用者信息
     * @param eventListener  监听器
     * @throws IOException 异常
     */
    private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, NewCall call,
                               EventListener eventListener) throws IOException {
        Request tunnelRequest = createTunnelRequest();
        UnoUrl url = tunnelRequest.url();
        for (int i = 0; i < MAX_TUNNEL_ATTEMPTS; i++) {
            connectSocket(connectTimeout, readTimeout, call, eventListener);
            tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);

            // 通道成功创建
            if (null == tunnelRequest) {
                break;
            }

            // 代理在验证请求后决定关闭连接。我们需要创建一个新的连接，但这次是使用auth凭据
            IoKit.close(rawSocket);
            rawSocket = null;
            sink = null;
            source = null;
            eventListener.connectEnd(call, route.socketAddress(), route.proxy(), null);
        }
    }

    /**
     * 在原始套接字上构建完整的HTTP或HTTPS连接所需的所有工作
     *
     * @param connectTimeout 连接超时时间
     * @param readTimeout    读取超时时间
     * @param call           调用者信息
     * @param eventListener  监听器
     * @throws IOException 异常
     */
    private void connectSocket(int connectTimeout, int readTimeout, NewCall call,
                               EventListener eventListener) throws IOException {
        Proxy proxy = route.proxy();
        Address address = route.address();

        rawSocket = proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP
                ? address.socketFactory().createSocket()
                : new Socket(proxy);

        eventListener.connectStart(call, route.socketAddress(), proxy);
        rawSocket.setSoTimeout(readTimeout);
        try {
            Platform.get().connectSocket(rawSocket, route.socketAddress(), connectTimeout);
        } catch (ConnectException e) {
            ConnectException ce = new ConnectException("Failed to connect to " + route.socketAddress());
            ce.initCause(e);
            throw ce;
        }

        // 下面的try/catch块是一种避免Android 7.0崩溃的伪代码
        try {
            source = IoKit.buffer(IoKit.source(rawSocket));
            sink = IoKit.buffer(IoKit.sink(rawSocket));
        } catch (NullPointerException npe) {
            if (NPE_THROW_WITH_NULL.equals(npe.getMessage())) {
                throw new IOException(npe);
            }
        }
    }

    private void establishProtocol(ConnectionSelector connectionSelector,
                                   int pingIntervalMillis, NewCall call, EventListener eventListener) throws IOException {
        if (null == route.address().sslSocketFactory()) {
            if (route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
                socket = rawSocket;
                protocol = Protocol.H2_PRIOR_KNOWLEDGE;
                startHttp2(pingIntervalMillis);
                return;
            }

            socket = rawSocket;
            protocol = Protocol.HTTP_1_1;
            return;
        }

        eventListener.secureConnectStart(call);
        connectTls(connectionSelector);
        eventListener.secureConnectEnd(call, handshake);

        if (protocol == Protocol.HTTP_2) {
            startHttp2(pingIntervalMillis);
        }
    }

    private void startHttp2(int pingIntervalMillis) throws IOException {
        // HTTP/2连接超时是按流设置的
        socket.setSoTimeout(0);
        http2Connection = new Http2Connection.Builder(true)
                .socket(socket, route.address().url().host(), source, sink)
                .listener(this)
                .pingIntervalMillis(pingIntervalMillis)
                .build();
        http2Connection.start();
    }

    private void connectTls(ConnectionSelector connectionSelector) throws IOException {
        Address address = route.address();
        SSLSocketFactory sslSocketFactory = address.sslSocketFactory();
        boolean success = false;
        SSLSocket sslSocket = null;
        try {
            // 在连接的套接字上创建包装器
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    rawSocket, address.url().host(), address.url().port(), true /* autoClose */);

            // 配置套接字的密码、TLS版本和扩展
            ConnectionSuite connectionSuite = connectionSelector.configureSecureSocket(sslSocket);
            if (connectionSuite.supportsTlsExtensions()) {
                Platform.get().configureTlsExtensions(
                        sslSocket, address.url().host(), address.protocols());
            }

            // 强制握手，否则抛出异常
            sslSocket.startHandshake();
            // 建立会话信息
            SSLSession sslSocketSession = sslSocket.getSession();
            Handshake unverifiedHandshake = Handshake.get(sslSocketSession);

            // 验证套接字的证书对于目标主机是可接受的
            if (!address.hostnameVerifier().verify(address.url().host(), sslSocketSession)) {
                List<Certificate> peerCertificates = unverifiedHandshake.peerCertificates();
                if (!peerCertificates.isEmpty()) {
                    X509Certificate cert = (X509Certificate) peerCertificates.get(0);
                    throw new SSLPeerUnverifiedException(
                            "Hostname " + address.url().host() + " not verified:"
                                    + "\n    certificate: " + CertificatePinner.pin(cert)
                                    + "\n    DN: " + cert.getSubjectX500Principal().getName()
                                    + "\n    subjectAltNames: " + HostnameVerifier.allSubjectAltNames(cert));
                } else {
                    throw new SSLPeerUnverifiedException(
                            "Hostname " + address.url().host() + " not verified (no certificates)");
                }
            }

            // 检查所提供的证书是否满足
            address.certificatePinner().check(address.url().host(),
                    unverifiedHandshake.peerCertificates());

            // 成功!保存握手和ALPN协议
            String maybeProtocol = connectionSuite.supportsTlsExtensions()
                    ? Platform.get().getSelectedProtocol(sslSocket)
                    : null;
            socket = sslSocket;
            source = IoKit.buffer(IoKit.source(socket));
            sink = IoKit.buffer(IoKit.sink(socket));
            handshake = unverifiedHandshake;
            protocol = null != maybeProtocol
                    ? Protocol.get(maybeProtocol)
                    : Protocol.HTTP_1_1;
            success = true;
        } catch (AssertionError e) {
            if (Builder.isAndroidGetsocknameError(e)) throw new IOException(e);
            throw e;
        } finally {
            if (null != sslSocket) {
                Platform.get().afterHandshake(sslSocket);
            }
            if (!success) {
                IoKit.close(sslSocket);
            }
        }
    }

    /**
     * 要通过HTTP代理建立HTTPS连接，请发送未加密的连接
     * 请求以创建代理连接。如果代理需要授权，则可能需要重试
     *
     * @param readTimeout   读取超时时间
     * @param writeTimeout  写入超时时间
     * @param tunnelRequest 请求信息
     * @param url           请求url
     * @throws IOException 异常
     */
    private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest,
                                 UnoUrl url) throws IOException {
        // 在每个SSL +代理连接的第一个消息对上创建SSL隧道
        String requestLine = "CONNECT " + Builder.hostHeader(url, true) + " HTTP/1.1";
        while (true) {
            Http1Codec tunnelCodec = new Http1Codec(null, null, source, sink);
            source.timeout().timeout(readTimeout, TimeUnit.MILLISECONDS);
            sink.timeout().timeout(writeTimeout, TimeUnit.MILLISECONDS);
            tunnelCodec.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelCodec.finishRequest();
            Response response = tunnelCodec.readResponseHeaders(false)
                    .request(tunnelRequest)
                    .build();
            tunnelCodec.skipConnectBody(response);

            switch (response.code()) {
                case Http.HTTP_OK:
                    if (!source.getBuffer().exhausted() || !sink.buffer().exhausted()) {
                        throw new IOException("TLS tunnel buffered too many bytes!");
                    }
                    return null;

                case Http.HTTP_PROXY_AUTH:
                    tunnelRequest = route.address().proxyAuthenticator().authenticate(route, response);
                    if (null == tunnelRequest) {
                        throw new IOException("Failed to authenticate with proxy");
                    }

                    if ("close".equalsIgnoreCase(response.header(Header.CONNECTION))) {
                        return tunnelRequest;
                    }
                    break;

                default:
                    throw new IOException("Unexpected response code for CONNECT: " + response.code());
            }
        }
    }

    /**
     * 返回通过HTTP代理创建TLS隧道的请求。隧道请求中的所有内容
     * 都以未加密的方式发送到代理服务器，因此隧道只包含最小的报头集。
     * 这避免了向代理发送潜在的敏感数据(如HTTP cookie)
     *
     * @return the request
     * @throws IOException 异常
     */
    private Request createTunnelRequest() throws IOException {
        Request proxyConnectRequest = new Request.Builder()
                .url(route.address().url())
                .method(Http.CONNECT, null)
                .header(Header.HOST, Builder.hostHeader(route.address().url(), true))
                .header(Header.PROXY_CONNECTION, Header.KEEP_ALIVE)
                .header(Header.USER_AGENT, "Httpd/" + Version.all())
                .build();

        Response fakeAuthChallengeResponse = new Response.Builder()
                .request(proxyConnectRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(Http.HTTP_PROXY_AUTH)
                .message("Preemptive Authenticate")
                .body(Builder.EMPTY_RESPONSE)
                .sentRequestAtMillis(-1L)
                .receivedResponseAtMillis(-1L)
                .header(Header.PROXY_AUTHENTICATE, Header.HTTPD_PREEMPTIVE)
                .build();

        Request authenticatedRequest = route.address().proxyAuthenticator()
                .authenticate(route, fakeAuthChallengeResponse);

        return null != authenticatedRequest
                ? authenticatedRequest
                : proxyConnectRequest;
    }

    /**
     * 如果此连接可以将流分配到{@code address}，则返回true。如果非空{@code route}是连接的解析路由
     *
     * @param address 地址信息
     * @param routes  路由
     * @return the true/false
     */
    boolean isEligible(Address address, List<Route> routes) {
        // 如果这个连接不接受新的流，我们就完成了
        if (transmitters.size() >= allocationLimit || noNewExchanges) return false;

        // 如果地址的非主机字段没有重叠，我们就完成了
        if (!Internal.instance.equalsNonHost(this.route.address(), address)) return false;

        // 如果主机完全匹配，就完成了:这个连接可以携带地址
        if (address.url().host().equals(this.route().address().url().host())) {
            return true;
        }

        // 1. 这个连接必须是 HTTP/2
        if (null == http2Connection) {
            return false;
        }

        // 2. 这些路由必须共享一个IP地址
        if (routes == null || !routeMatchesAny(routes)) return false;

        // 3. 此连接的服务器证书必须覆盖新主机
        if (address.hostnameVerifier() != HostnameVerifier.INSTANCE) return false;
        if (!supportsUrl(address.url())) return false;

        // 4. 证书固定必须与主机匹配
        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if this connection's route has the same address as any of {@code routes}. This
     * requires us to have a DNS address for both hosts, which only happens after route planning. We
     * can't coalesce connections that use a proxy, since proxies don't tell us the origin server's IP
     * address.
     */
    private boolean routeMatchesAny(List<Route> candidates) {
        for (int i = 0, size = candidates.size(); i < size; i++) {
            Route candidate = candidates.get(i);
            if (candidate.proxy().type() == Proxy.Type.DIRECT
                    && route.proxy().type() == Proxy.Type.DIRECT
                    && route.socketAddress().equals(candidate.socketAddress())) {
                return true;
            }
        }
        return false;
    }

    public boolean supportsUrl(UnoUrl url) {
        // 端口不匹配
        if (url.port() != route.address().url().port()) {
            return false;
        }

        // 主机不匹配,但是如果证书匹配，仍然是好的。
        if (!url.host().equals(route.address().url().host())) {
            // We have a host mismatch. But if the certificate matches, we're still good.
            return null != handshake && HostnameVerifier.INSTANCE.verify(
                    url.host(), (X509Certificate) handshake.peerCertificates().get(0));
        }

        return true;
    }

    HttpCodec newCodec(Httpd client, Interceptor.Chain chain) throws SocketException {
        if (http2Connection != null) {
            return new Http2Codec(client, this, chain, http2Connection);
        } else {
            socket.setSoTimeout(chain.readTimeoutMillis());
            source.timeout().timeout(chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
            sink.timeout().timeout(chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
            return new Http1Codec(client, this, source, sink);
        }
    }

    RealWebSocket.Streams newWebSocketStreams(Exchange exchange) throws SocketException {
        socket.setSoTimeout(0);
        noNewExchanges();
        return new RealWebSocket.Streams(true, source, sink) {
            @Override
            public void close() {
                exchange.bodyComplete(-1L, true, true, null);
            }
        };
    }

    @Override
    public Route route() {
        return route;
    }

    public void cancel() {
        IoKit.close(rawSocket);
    }

    @Override
    public Socket socket() {
        return socket;
    }

    /**
     * 如果此连接准备托管新流，则返回true
     *
     * @param doExtensiveChecks 是否检查
     * @return the true/false
     */
    public boolean isHealthy(boolean doExtensiveChecks) {
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            return false;
        }

        if (null != http2Connection) {
            return http2Connection.isHealthy(System.nanoTime());
        }

        if (doExtensiveChecks) {
            try {
                int readTimeout = socket.getSoTimeout();
                try {
                    socket.setSoTimeout(1);
                    if (source.exhausted()) {
                        // Stream耗尽;关闭套接字
                        return false;
                    }
                    return true;
                } finally {
                    socket.setSoTimeout(readTimeout);
                }
            } catch (SocketTimeoutException ignored) {
                // 读取超时;套接字是好的
            } catch (IOException e) {
                // 不能读取;套接字关闭
                return false;
            }
        }
        return true;
    }

    /**
     * Refuse incoming streams.
     */
    @Override
    public void onStream(Http2Stream stream) throws IOException {
        stream.close(ErrorCode.REFUSED_STREAM, null);
    }

    /**
     * When settings are received, adjust the allocation limit.
     */
    @Override
    public void onSettings(Http2Connection connection) {
        synchronized (connectionPool) {
            allocationLimit = connection.maxConcurrentStreams();
        }
    }

    @Override
    public Handshake handshake() {
        return handshake;
    }

    /**
     * Returns true if this is an HTTP/2 connection. Such connections can be used in multiple HTTP
     * requests simultaneously.
     */
    public boolean isMultiplexed() {
        return http2Connection != null;
    }

    /**
     * Track a failure using this connection. This may prevent both the connection and its route from
     * being used for future exchanges.
     */
    void trackFailure(IOException e) {
        assert (!Thread.holdsLock(connectionPool));
        synchronized (connectionPool) {
            if (e instanceof StreamException) {
                ErrorCode errorCode = ((StreamException) e).errorCode;
                if (errorCode == ErrorCode.REFUSED_STREAM) {
                    // Retry REFUSED_STREAM errors once on the same connection.
                    refusedStreamCount++;
                    if (refusedStreamCount > 1) {
                        noNewExchanges = true;
                        routeFailureCount++;
                    }
                } else if (errorCode != ErrorCode.CANCEL) {
                    // Keep the connection for CANCEL errors. Everything else wants a fresh connection.
                    noNewExchanges = true;
                    routeFailureCount++;
                }
            } else if (!isMultiplexed() || e instanceof RevisedException) {
                noNewExchanges = true;

                // If this route hasn't completed a call, avoid it for new connections.
                if (successCount == 0) {
                    if (e != null) {
                        connectionPool.connectFailed(route, e);
                    }
                    routeFailureCount++;
                }
            }
        }
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return "RealConnection{"
                + route.address().url().host() + Symbol.COLON + route.address().url().port()
                + ", proxy="
                + route.proxy()
                + " hostAddress="
                + route.socketAddress()
                + " cipherSuite="
                + (null != handshake ? handshake.cipherSuite() : "none")
                + " protocol="
                + protocol
                + Symbol.C_BRACE_RIGHT;
    }

}
