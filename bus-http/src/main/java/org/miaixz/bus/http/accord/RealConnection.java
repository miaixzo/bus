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
package org.miaixz.bus.http.accord;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.RevisedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.AnyHostnameVerifier;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.platform.Platform;
import org.miaixz.bus.http.metric.EventListener;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.NewChain;
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
 * A concrete connection to a target server (either directly or via a proxy). This class handles the low-level details
 * of establishing and maintaining the connection, including TLS handshakes and HTTP/2 multiplexing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealConnection extends Http2Connection.Listener implements Connection {

    private static final String NPE_THROW_WITH_NULL = "throw with null exception";
    private static final int MAX_TUNNEL_ATTEMPTS = 21;

    public final RealConnectionPool connectionPool;
    /**
     * The current streams being carried by this connection.
     */
    final List<Reference<Transmitter>> transmitters = new ArrayList<>();

    /**
     * The following fields are initialized by connect() and are never re-allocated.
     */
    private final Route route;
    /**
     * If true, no new streams can be created on this connection.
     */
    boolean noNewExchanges;
    /**
     * The number of times there was a problem establishing a stream that could be due to route chosen. Guarded by
     * {@link #connectionPool}.
     */
    int routeFailureCount;
    int successCount;
    /**
     * The nanotime timestamp when {@code allocations.size()} became zero.
     */
    long idleAtNanos = Long.MAX_VALUE;
    /**
     * The low-level TCP socket.
     */
    private Socket rawSocket;
    /**
     * The application-layer socket. This may be an {@link SSLSocket} layered over {@link #rawSocket}, or
     * {@link #rawSocket} itself if this connection is not using SSL.
     */
    private Socket socket;
    /**
     * The following fields are in the connected state and are guarded by connectionPool.
     */
    private Handshake handshake;
    private Protocol protocol;
    private Http2Connection http2Connection;
    private BufferSource source;
    private BufferSink sink;
    private int refusedStreamCount;
    /**
     * The maximum number of concurrent streams this connection can carry. New streams can be created on this connection
     * if {@code allocations.size() < allocationLimit}.
     */
    private int allocationLimit = 1;

    public RealConnection(RealConnectionPool connectionPool, Route route) {
        this.connectionPool = connectionPool;
        this.route = route;
    }

    /**
     * Creates a test connection for internal use.
     *
     * @param connectionPool The connection pool.
     * @param route          The route.
     * @param socket         The socket.
     * @param idleAtNanos    The idle timestamp.
     * @return A new {@code RealConnection} instance.
     */
    static RealConnection testConnection(
            RealConnectionPool connectionPool,
            Route route,
            Socket socket,
            long idleAtNanos) {
        RealConnection result = new RealConnection(connectionPool, route);
        result.socket = socket;
        result.idleAtNanos = idleAtNanos;
        return result;
    }

    /**
     * Prevents further exchanges from being created on this connection.
     */
    public void noNewExchanges() {
        assert (!Thread.holdsLock(connectionPool));
        synchronized (connectionPool) {
            noNewExchanges = true;
        }
    }

    /**
     * Establishes a connection to the target server.
     *
     * @param connectTimeout         The connect timeout in milliseconds.
     * @param readTimeout            The read timeout in milliseconds.
     * @param writeTimeout           The write timeout in milliseconds.
     * @param pingIntervalMillis     The ping interval in milliseconds for HTTP/2.
     * @param connectionRetryEnabled Whether connection retries are enabled.
     * @param call                   The call that initiated this connection.
     * @param eventListener          The event listener for connection events.
     * @throws IOException if an I/O error occurs during connection establishment.
     */
    public void connect(
            int connectTimeout,
            int readTimeout,
            int writeTimeout,
            int pingIntervalMillis,
            boolean connectionRetryEnabled,
            NewCall call,
            EventListener eventListener) throws IOException {
        if (protocol != null)
            throw new IllegalStateException("already connected");

        RouteException routeException = null;
        List<ConnectionSuite> connectionSuites = route.address().connectionSpecs();
        ConnectionSelector connectionSelector = new ConnectionSelector(connectionSuites);

        if (route.address().sslSocketFactory() == null) {
            if (!connectionSuites.contains(ConnectionSuite.CLEARTEXT)) {
                throw new RouteException(new UnknownServiceException("CLEARTEXT communication not enabled for client"));
            }
            String host = route.address().url().host();
            if (!Platform.get().isCleartextTrafficPermitted(host)) {
                throw new RouteException(new UnknownServiceException(
                        "CLEARTEXT communication to " + host + " not permitted by network security policy"));
            }
        } else {
            if (route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
                throw new RouteException(new UnknownServiceException("H2_PRIOR_KNOWLEDGE cannot be used with HTTPS"));
            }
        }

        while (true) {
            try {
                if (route.requiresTunnel()) {
                    connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);
                    if (rawSocket == null) {
                        // We failed to connect the tunnel, but closed our resources appropriately.
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

                if (routeException == null) {
                    routeException = new RouteException(e);
                } else {
                    routeException.addConnectException(e);
                }

                if (!connectionRetryEnabled || !connectionSelector.connectionFailed(e)) {
                    throw routeException;
                }
            }
        }

        if (route.requiresTunnel() && rawSocket == null) {
            ProtocolException exception = new ProtocolException(
                    "Too many tunnel connections attempted: " + MAX_TUNNEL_ATTEMPTS);
            throw new RouteException(exception);
        }

        if (http2Connection != null) {
            synchronized (connectionPool) {
                allocationLimit = http2Connection.maxConcurrentStreams();
            }
        }
    }

    /**
     * Completes all work necessary to build an HTTPS connection over a proxy tunnel. The challenge here is that the
     * proxy server may issue an authentication challenge and then close the connection.
     *
     * @param connectTimeout The connect timeout in milliseconds.
     * @param readTimeout    The read timeout in milliseconds.
     * @param writeTimeout   The write timeout in milliseconds.
     * @param call           The call that initiated this connection.
     * @param eventListener  The event listener for connection events.
     * @throws IOException if an I/O error occurs.
     */
    private void connectTunnel(
            int connectTimeout,
            int readTimeout,
            int writeTimeout,
            NewCall call,
            EventListener eventListener) throws IOException {
        Request tunnelRequest = createTunnelRequest();
        UnoUrl url = tunnelRequest.url();
        for (int i = 0; i < MAX_TUNNEL_ATTEMPTS; i++) {
            connectSocket(connectTimeout, readTimeout, call, eventListener);
            tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);

            // The tunnel was successfully created.
            if (tunnelRequest == null) {
                break;
            }

            // The proxy decided to close the connection after an authentication challenge. We need to create a new
            // connection, but this time with auth credentials.
            IoKit.close(rawSocket);
            rawSocket = null;
            sink = null;
            source = null;
            eventListener.connectEnd(call, route.socketAddress(), route.proxy(), null);
        }
    }

    /**
     * Completes all work necessary to build a full HTTP or HTTPS connection over the raw socket.
     *
     * @param connectTimeout The connect timeout in milliseconds.
     * @param readTimeout    The read timeout in milliseconds.
     * @param call           The call that initiated this connection.
     * @param eventListener  The event listener for connection events.
     * @throws IOException if an I/O error occurs.
     */
    private void connectSocket(int connectTimeout, int readTimeout, NewCall call, EventListener eventListener)
            throws IOException {
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

        // The following try/catch block is a pseudo-code to avoid Android 7.0 crashes.
        try {
            source = IoKit.buffer(IoKit.source(rawSocket));
            sink = IoKit.buffer(IoKit.sink(rawSocket));
        } catch (NullPointerException npe) {
            if (NPE_THROW_WITH_NULL.equals(npe.getMessage())) {
                throw new IOException(npe);
            }
        }
    }

    private void establishProtocol(
            ConnectionSelector connectionSelector,
            int pingIntervalMillis,
            NewCall call,
            EventListener eventListener) throws IOException {
        if (route.address().sslSocketFactory() == null) {
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
        // HTTP/2 connection timeouts are set per stream.
        socket.setSoTimeout(0);
        http2Connection = new Http2Connection.Builder(true).socket(socket, route.address().url().host(), source, sink)
                .listener(this).pingIntervalMillis(pingIntervalMillis).build();
        http2Connection.start();
    }

    private void connectTls(ConnectionSelector connectionSelector) throws IOException {
        Address address = route.address();
        SSLSocketFactory sslSocketFactory = address.sslSocketFactory();
        boolean success = false;
        SSLSocket sslSocket = null;
        try {
            // Create a wrapper over the connected socket.
            sslSocket = (SSLSocket) sslSocketFactory
                    .createSocket(rawSocket, address.url().host(), address.url().port(), true /* autoClose */);

            // Configure the socket's ciphers, TLS versions, and extensions.
            ConnectionSuite connectionSuite = connectionSelector.configureSecureSocket(sslSocket);
            if (connectionSuite.supportsTlsExtensions()) {
                Platform.get().configureTlsExtensions(sslSocket, address.url().host(), address.protocols());
            }

            // Force the handshake to occur, otherwise it may throw an exception later.
            sslSocket.startHandshake();
            // Establish the session information.
            SSLSession sslSocketSession = sslSocket.getSession();
            Handshake unverifiedHandshake = Handshake.get(sslSocketSession);

            // Verify that the socket's certificates are acceptable for the target host.
            if (!address.hostnameVerifier().verify(address.url().host(), sslSocketSession)) {
                List<Certificate> peerCertificates = unverifiedHandshake.peerCertificates();
                if (!peerCertificates.isEmpty()) {
                    X509Certificate cert = (X509Certificate) peerCertificates.get(0);
                    throw new SSLPeerUnverifiedException("Hostname " + address.url().host() + " not verified:"
                            + "\n    certificate: " + CertificatePinner.pin(cert) + "\n    DN: "
                            + cert.getSubjectX500Principal().getName() + "\n    subjectAltNames: "
                            + AnyHostnameVerifier.allSubjectAltNames(cert));
                } else {
                    throw new SSLPeerUnverifiedException(
                            "Hostname " + address.url().host() + " not verified (no certificates)");
                }
            }

            // Check that the provided certificates satisfy the certificate pinner.
            address.certificatePinner().check(address.url().host(), unverifiedHandshake.peerCertificates());

            // Success! Save the handshake and ALPN protocol.
            String maybeProtocol = connectionSuite.supportsTlsExtensions()
                    ? Platform.get().getSelectedProtocol(sslSocket)
                    : null;
            socket = sslSocket;
            source = IoKit.buffer(IoKit.source(socket));
            sink = IoKit.buffer(IoKit.sink(socket));
            handshake = unverifiedHandshake;
            protocol = null != maybeProtocol ? Protocol.get(maybeProtocol) : Protocol.HTTP_1_1;
            success = true;
        } catch (AssertionError e) {
            if (IoKit.isAndroidGetsocknameError(e))
                throw new IOException(e);
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
     * To establish an HTTPS connection through an HTTP proxy, send an unencrypted CONNECT request to create the proxy
     * connection. If the proxy requires authorization, this may need to be retried.
     *
     * @param readTimeout   The read timeout in milliseconds.
     * @param writeTimeout  The write timeout in milliseconds.
     * @param tunnelRequest The tunnel request.
     * @param url           The URL.
     * @return The next tunnel request if authentication is required, or null if the tunnel is established.
     * @throws IOException if an I/O error occurs.
     */
    private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest, UnoUrl url)
            throws IOException {
        // Create an SSL tunnel over the first message pair of each SSL + proxy connection.
        String requestLine = "CONNECT " + Builder.hostHeader(url, true) + " HTTP/1.1";
        while (true) {
            Http1Codec tunnelCodec = new Http1Codec(null, null, source, sink);
            source.timeout().timeout(readTimeout, TimeUnit.MILLISECONDS);
            sink.timeout().timeout(writeTimeout, TimeUnit.MILLISECONDS);
            tunnelCodec.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelCodec.finishRequest();
            Response response = tunnelCodec.readResponseHeaders(false).request(tunnelRequest).build();
            tunnelCodec.skipConnectBody(response);

            switch (response.code()) {
                case HTTP.HTTP_OK:
                    // The tunnel has been established. Prepare for HTTPS.
                    if (!source.getBuffer().exhausted() || !sink.buffer().exhausted()) {
                        throw new IOException("TLS tunnel buffered too many bytes!");
                    }
                    return null;

                case HTTP.HTTP_PROXY_AUTH:
                    tunnelRequest = route.address().proxyAuthenticator().authenticate(route, response);
                    if (null == tunnelRequest) {
                        throw new IOException("Failed to authenticate with proxy");
                    }

                    if ("close".equalsIgnoreCase(response.header(HTTP.CONNECTION))) {
                        return tunnelRequest;
                    }
                    break;

                default:
                    throw new IOException("Unexpected response code for CONNECT: " + response.code());
            }
        }
    }

    /**
     * Returns a request that creates a TLS tunnel through an HTTP proxy. All content in the tunnel request is sent
     * unencrypted to the proxy server, so the tunnel contains only a minimal set of headers. This avoids sending
     * potentially sensitive data (like HTTP cookies) to the proxy.
     *
     * @return The tunnel request.
     * @throws IOException if an I/O error occurs.
     */
    private Request createTunnelRequest() throws IOException {
        Request proxyConnectRequest = new Request.Builder().url(route.address().url()).method(HTTP.CONNECT, null)
                .header(HTTP.HOST, Builder.hostHeader(route.address().url(), true))
                .header(HTTP.PROXY_CONNECTION, HTTP.KEEP_ALIVE).header(HTTP.USER_AGENT, "Httpd/" + Version.all())
                .build();

        Response fakeAuthChallengeResponse = new Response.Builder().request(proxyConnectRequest)
                .protocol(Protocol.HTTP_1_1).code(HTTP.HTTP_PROXY_AUTH).message("Preemptive Authenticate")
                .body(Builder.EMPTY_RESPONSE).sentRequestAtMillis(-1L).receivedResponseAtMillis(-1L)
                .header(HTTP.PROXY_AUTHENTICATE, HTTP.HTTPD_PREEMPTIVE).build();

        Request authenticatedRequest = route.address().proxyAuthenticator()
                .authenticate(route, fakeAuthChallengeResponse);

        return authenticatedRequest != null ? authenticatedRequest : proxyConnectRequest;
    }

    /**
     * Returns true if this connection can carry a stream to {@code address}. If a non-null {@code route} is provided,
     * it is the resolved route for the connection.
     *
     * @param address The address to check eligibility for.
     * @param routes  The list of routes.
     * @return {@code true} if the connection is eligible.
     */
    boolean isEligible(Address address, List<Route> routes) {
        // If this connection doesn't accept new streams, we're done.
        if (transmitters.size() >= allocationLimit || noNewExchanges)
            return false;

        // If the address's non-host fields don't overlap, we're done.
        if (!Internal.instance.equalsNonHost(this.route.address(), address))
            return false;

        // If the host is an exact match, we're done: this connection can carry the address.
        if (address.url().host().equals(this.route().address().url().host())) {
            return true;
        }

        // 1. This connection must be HTTP/2.
        if (http2Connection == null)
            return false;

        // 2. These routes must share an IP address.
        if (routes == null || !routeMatchesAny(routes))
            return false;

        // 3. The server certificate of this connection must cover the new host.
        if (address.hostnameVerifier() != AnyHostnameVerifier.INSTANCE)
            return false;
        if (!supportsUrl(address.url()))
            return false;

        // 4. The certificate pinner must match the host.
        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if this connection's route has the same address as any of {@code routes}. This requires us to have a
     * DNS address for both hosts, which only happens after route planning. We can't coalesce connections that use a
     * proxy, since proxies don't tell us the origin server's IP address.
     *
     * @param candidates The list of candidate routes.
     * @return {@code true} if a route matches.
     */
    private boolean routeMatchesAny(List<Route> candidates) {
        for (int i = 0, size = candidates.size(); i < size; i++) {
            Route candidate = candidates.get(i);
            if (candidate.proxy().type() == Proxy.Type.DIRECT && route.proxy().type() == Proxy.Type.DIRECT
                    && route.socketAddress().equals(candidate.socketAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this connection can be used for {@code url}.
     *
     * @param url The URL to check.
     * @return {@code true} if the connection supports the URL.
     */
    public boolean supportsUrl(UnoUrl url) {
        // Port doesn't match.
        if (url.port() != route.address().url().port()) {
            return false;
        }

        // Host doesn't match, but if the certificate matches, we're still good.
        if (!url.host().equals(route.address().url().host())) {
            return handshake != null && AnyHostnameVerifier.INSTANCE
                    .verify(url.host(), (X509Certificate) handshake.peerCertificates().get(0));
        }

        return true;
    }

    /**
     * Creates a new HTTP codec for this connection.
     *
     * @param client The HTTP client.
     * @param chain  The interceptor chain.
     * @return A new HTTP codec.
     * @throws SocketException if a socket error occurs.
     */
    HttpCodec newCodec(Httpd client, NewChain chain) throws SocketException {
        if (http2Connection != null) {
            return new Http2Codec(client, this, chain, http2Connection);
        } else {
            socket.setSoTimeout(chain.readTimeoutMillis());
            source.timeout().timeout(chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
            sink.timeout().timeout(chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
            return new Http1Codec(client, this, source, sink);
        }
    }

    /**
     * Creates new WebSocket streams for this connection.
     *
     * @param exchange The exchange that initiated the WebSocket.
     * @return The WebSocket streams.
     * @throws SocketException if a socket error occurs.
     */
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

    /**
     * Cancels this connection.
     */
    public void cancel() {
        IoKit.close(rawSocket);
    }

    @Override
    public Socket socket() {
        return socket;
    }

    /**
     * Returns true if this connection is ready to host new streams.
     *
     * @param doExtensiveChecks Whether to perform extensive health checks.
     * @return {@code true} if the connection is healthy.
     */
    public boolean isHealthy(boolean doExtensiveChecks) {
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            return false;
        }

        if (http2Connection != null) {
            return http2Connection.isHealthy(System.nanoTime());
        }

        if (doExtensiveChecks) {
            try {
                int readTimeout = socket.getSoTimeout();
                try {
                    socket.setSoTimeout(1);
                    if (source.exhausted()) {
                        // Stream is exhausted; the socket is closed.
                        return false;
                    }
                    return true;
                } finally {
                    socket.setSoTimeout(readTimeout);
                }
            } catch (SocketTimeoutException ignored) {
                // Read timed out; the socket is good.
            } catch (IOException e) {
                // Unable to read; the socket is closed.
                return false;
            }
        }
        return true;
    }

    /**
     * Refuses incoming streams.
     *
     * @param stream The HTTP/2 stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void onStream(Http2Stream stream) throws IOException {
        stream.close(Http2ErrorCode.REFUSED_STREAM, null);
    }

    /**
     * When settings are received, adjust the allocation limit.
     *
     * @param connection The HTTP/2 connection.
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
     * Returns true if this is an HTTP/2 connection. Such connections can be used in multiple HTTP requests
     * simultaneously.
     *
     * @return {@code true} if the connection is multiplexed.
     */
    public boolean isMultiplexed() {
        return http2Connection != null;
    }

    /**
     * Track a failure using this connection. This may prevent both the connection and its route from being used for
     * future exchanges.
     *
     * @param e The exception that occurred.
     */
    void trackFailure(IOException e) {
        assert (!Thread.holdsLock(connectionPool));
        synchronized (connectionPool) {
            if (e instanceof StreamException) {
                Http2ErrorCode errorCode = ((StreamException) e).errorCode;
                if (errorCode == Http2ErrorCode.REFUSED_STREAM) {
                    // Retry REFUSED_STREAM errors once on the same connection.
                    refusedStreamCount++;
                    if (refusedStreamCount > 1) {
                        noNewExchanges = true;
                        routeFailureCount++;
                    }
                } else if (errorCode != Http2ErrorCode.CANCEL) {
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
        return "RealConnection{" + route.address().url().host() + Symbol.COLON + route.address().url().port()
                + ", proxy=" + route.proxy() + " hostAddress=" + route.socketAddress() + " cipherSuite="
                + (handshake != null ? handshake.cipherSuite() : "none") + " protocol=" + protocol
                + Symbol.C_BRACE_RIGHT;
    }

}
