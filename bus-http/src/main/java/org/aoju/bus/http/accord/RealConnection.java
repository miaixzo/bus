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
package org.aoju.bus.http.accord;

import org.aoju.bus.core.io.segment.BufferSink;
import org.aoju.bus.core.io.segment.BufferSource;
import org.aoju.bus.core.io.segment.Source;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.accord.platform.Platform;
import org.aoju.bus.http.internal.http.HttpCodec;
import org.aoju.bus.http.internal.http.HttpHeaders;
import org.aoju.bus.http.internal.http.first.Http1Codec;
import org.aoju.bus.http.internal.http.second.ErrorCode;
import org.aoju.bus.http.internal.http.second.Http2Codec;
import org.aoju.bus.http.internal.http.second.Http2Connection;
import org.aoju.bus.http.internal.http.second.Http2Stream;
import org.aoju.bus.http.offers.CertificatePinner;
import org.aoju.bus.http.offers.EventListener;
import org.aoju.bus.http.offers.Handshake;
import org.aoju.bus.http.offers.Interceptor;
import org.aoju.bus.http.secure.OkHostnameVerifier;
import org.aoju.bus.http.socket.RealWebSocket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.Proxy;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PROXY_AUTH;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
public final class RealConnection extends Http2Connection.Listener implements Connection {

    private static final String NPE_THROW_WITH_NULL = "throw with null exception";
    private static final int MAX_TUNNEL_ATTEMPTS = 21;
    /**
     * Current streams carried by this connection.
     */
    public final List<Reference<StreamAllocation>> allocations = new ArrayList<>();
    private final ConnectionPool connectionPool;

    // The fields below are initialized by connect() and never reassigned.
    private final Route route;
    /**
     * If true, no new streams can be created on this connection. Once true this is always true.
     */
    public boolean noNewStreams;
    public int successCount;
    /**
     * The maximum number of concurrent streams that can be carried by this connection. If {@code
     * allocations.size() < allocationLimit} then new streams can be created on this connection.
     */
    public int allocationLimit = 1;
    /**
     * Nanotime timestamp when {@code allocations.size()} reached zero.
     */
    public long idleAtNanos = Long.MAX_VALUE;
    /**
     * The low-level TCP socket.
     */
    private Socket rawSocket;
    /**
     * The application layer socket. Either an {@link SSLSocket} layered over {@link #rawSocket}, or
     * {@link #rawSocket} itself if this connection does not use SSL.
     */
    private Socket socket;

    // The fields below track connection state and are guarded by connectionPool.
    private Handshake handshake;
    private Protocol protocol;
    private Http2Connection http2Connection;
    private BufferSource source;
    private BufferSink sink;

    public RealConnection(ConnectionPool connectionPool, Route route) {
        this.connectionPool = connectionPool;
        this.route = route;
    }

    public static RealConnection testConnection(
            ConnectionPool connectionPool, Route route, Socket socket, long idleAtNanos) {
        RealConnection result = new RealConnection(connectionPool, route);
        result.socket = socket;
        result.idleAtNanos = idleAtNanos;
        return result;
    }

    public void connect(int connectTimeout, int readTimeout, int writeTimeout,
                        int pingIntervalMillis, boolean connectionRetryEnabled, Call call,
                        EventListener eventListener) {
        if (protocol != null) throw new IllegalStateException("already connected");

        RouteException routeException = null;
        List<ConnectionSpec> connectionSpecs = route.address().connectionSpecs();
        ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);

        if (route.address().sslSocketFactory() == null) {
            if (!connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
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
                    if (rawSocket == null) {
                        // We were unable to connect the tunnel but properly closed down our resources.
                        break;
                    }
                } else {
                    connectSocket(connectTimeout, readTimeout, call, eventListener);
                }
                establishProtocol(connectionSpecSelector, pingIntervalMillis, call, eventListener);
                eventListener.connectEnd(call, route.socketAddress(), route.proxy(), protocol);
                break;
            } catch (IOException e) {
                Internal.closeQuietly(socket);
                Internal.closeQuietly(rawSocket);
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

                if (!connectionRetryEnabled || !connectionSpecSelector.connectionFailed(e)) {
                    throw routeException;
                }
            }
        }

        if (route.requiresTunnel() && rawSocket == null) {
            ProtocolException exception = new ProtocolException("Too many tunnel connections attempted: "
                    + MAX_TUNNEL_ATTEMPTS);
            throw new RouteException(exception);
        }

        if (http2Connection != null) {
            synchronized (connectionPool) {
                allocationLimit = http2Connection.maxConcurrentStreams();
            }
        }
    }

    private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, Call call,
                               EventListener eventListener) throws IOException {
        Request tunnelRequest = createTunnelRequest();
        Url url = tunnelRequest.url();
        for (int i = 0; i < MAX_TUNNEL_ATTEMPTS; i++) {
            connectSocket(connectTimeout, readTimeout, call, eventListener);
            tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);

            if (tunnelRequest == null) break;

            Internal.closeQuietly(rawSocket);
            rawSocket = null;
            sink = null;
            source = null;
            eventListener.connectEnd(call, route.socketAddress(), route.proxy(), null);
        }
    }

    private void connectSocket(int connectTimeout, int readTimeout, Call call,
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

        try {
            source = IoUtils.buffer(IoUtils.source(rawSocket));
            sink = IoUtils.buffer(IoUtils.sink(rawSocket));
        } catch (NullPointerException npe) {
            if (NPE_THROW_WITH_NULL.equals(npe.getMessage())) {
                throw new IOException(npe);
            }
        }
    }

    private void establishProtocol(ConnectionSpecSelector connectionSpecSelector,
                                   int pingIntervalMillis, Call call, EventListener eventListener) throws IOException {
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
        connectTls(connectionSpecSelector);
        eventListener.secureConnectEnd(call, handshake);

        if (protocol == Protocol.HTTP_2) {
            startHttp2(pingIntervalMillis);
        }
    }

    private void startHttp2(int pingIntervalMillis) throws IOException {
        socket.setSoTimeout(0); // HTTP/2 connection timeouts are set per-stream.
        http2Connection = new Http2Connection.Builder(true)
                .socket(socket, route.address().url().host(), source, sink)
                .listener(this)
                .pingIntervalMillis(pingIntervalMillis)
                .build();
        http2Connection.start();
    }

    private void connectTls(ConnectionSpecSelector connectionSpecSelector) throws IOException {
        Address address = route.address();
        SSLSocketFactory sslSocketFactory = address.sslSocketFactory();
        boolean success = false;
        SSLSocket sslSocket = null;
        try {
            // Create the wrapper over the connected socket.
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    rawSocket, address.url().host(), address.url().port(), true /* autoClose */);

            // Configure the socket's ciphers, TLS versions, and extensions.
            ConnectionSpec connectionSpec = connectionSpecSelector.configureSecureSocket(sslSocket);
            if (connectionSpec.supportsTlsExtensions()) {
                Platform.get().configureTlsExtensions(
                        sslSocket, address.url().host(), address.protocols());
            }

            // Force handshake. This can throw!
            sslSocket.startHandshake();
            // block for session establishment
            SSLSession sslSocketSession = sslSocket.getSession();
            Handshake unverifiedHandshake = Handshake.get(sslSocketSession);

            // Verify that the socket's certificates are acceptable for the target host.
            if (!address.hostnameVerifier().verify(address.url().host(), sslSocketSession)) {
                X509Certificate cert = (X509Certificate) unverifiedHandshake.peerCertificates().get(0);
                throw new SSLPeerUnverifiedException("Hostname " + address.url().host() + " not verified:"
                        + "\n    certificate: " + CertificatePinner.pin(cert)
                        + "\n    DN: " + cert.getSubjectDN().getName()
                        + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(cert));
            }

            // Check that the certificate pinner is satisfied by the certificates presented.
            address.certificatePinner().check(address.url().host(),
                    unverifiedHandshake.peerCertificates());

            // Success! Save the handshake and the ALPN protocol.
            String maybeProtocol = connectionSpec.supportsTlsExtensions()
                    ? Platform.get().getSelectedProtocol(sslSocket)
                    : null;
            socket = sslSocket;
            source = IoUtils.buffer(IoUtils.source(socket));
            sink = IoUtils.buffer(IoUtils.sink(socket));
            handshake = unverifiedHandshake;
            protocol = maybeProtocol != null
                    ? Protocol.get(maybeProtocol)
                    : Protocol.HTTP_1_1;
            success = true;
        } catch (AssertionError e) {
            if (Internal.isAndroidGetsocknameError(e)) throw new IOException(e);
            throw e;
        } finally {
            if (sslSocket != null) {
                Platform.get().afterHandshake(sslSocket);
            }
            if (!success) {
                Internal.closeQuietly(sslSocket);
            }
        }
    }

    private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest,
                                 Url url) throws IOException {
        // Make an SSL Tunnel on the first message pair of each SSL + proxy connection.
        String requestLine = "CONNECT " + Internal.hostHeader(url, true) + " HTTP/1.1";
        while (true) {
            Http1Codec tunnelConnection = new Http1Codec(null, null, source, sink);
            source.timeout().timeout(readTimeout, MILLISECONDS);
            sink.timeout().timeout(writeTimeout, MILLISECONDS);
            tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelConnection.finishRequest();
            Response response = tunnelConnection.readResponseHeaders(false)
                    .request(tunnelRequest)
                    .build();
            // The response body from a CONNECT should be empty, but if it is not then we should consume
            // it before proceeding.
            long contentLength = HttpHeaders.contentLength(response);
            if (contentLength == -1L) {
                contentLength = 0L;
            }
            Source body = tunnelConnection.newFixedLengthSource(contentLength);
            Internal.skipAll(body, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            body.close();

            switch (response.code()) {
                case HTTP_OK:
                    // Assume the server won't send a TLS ServerHello until we send a TLS ClientHello. If
                    // that happens, then we will have buffered bytes that are needed by the SSLSocket!
                    // This check is imperfect: it doesn't tell us whether a handshake will succeed, just
                    // that it will almost certainly fail because the proxy has sent unexpected data.
                    if (!source.buffer().exhausted() || !sink.buffer().exhausted()) {
                        throw new IOException("TLS tunnel buffered too many bytes!");
                    }
                    return null;

                case HTTP_PROXY_AUTH:
                    tunnelRequest = route.address().proxyAuthenticator().authenticate(route, response);
                    if (tunnelRequest == null) throw new IOException("Failed to authenticate with proxy");

                    if ("close".equalsIgnoreCase(response.header("Connection"))) {
                        return tunnelRequest;
                    }
                    break;

                default:
                    throw new IOException(
                            "Unexpected response code for CONNECT: " + response.code());
            }
        }
    }

    private Request createTunnelRequest() throws IOException {
        Request proxyConnectRequest = new Request.Builder()
                .url(route.address().url())
                .method("CONNECT", null)
                .header("Host", Internal.hostHeader(route.address().url(), true))
                .header("Proxy-Connection", "Keep-Alive") // For HTTP/1.0 proxies like Squid.
                .header("User-Agent", Version.userAgent())
                .build();

        Response fakeAuthChallengeResponse = new Response.Builder()
                .request(proxyConnectRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_PROXY_AUTH)
                .message("Preemptive Authenticate")
                .body(Internal.EMPTY_RESPONSE)
                .sentRequestAtMillis(-1L)
                .receivedResponseAtMillis(-1L)
                .header("Proxy-Authenticate", "httpClient-Preemptive")
                .build();

        Request authenticatedRequest = route.address().proxyAuthenticator()
                .authenticate(route, fakeAuthChallengeResponse);

        return authenticatedRequest != null
                ? authenticatedRequest
                : proxyConnectRequest;
    }

    public boolean isEligible(Address address, Route route) {
        if (allocations.size() >= allocationLimit || noNewStreams) return false;

        if (!Internal.instance.equalsNonHost(this.route.address(), address)) return false;

        if (address.url().host().equals(this.route().address().url().host())) {
            return true;
        }

        if (http2Connection == null) return false;

        if (route == null) return false;
        if (route.proxy().type() != Proxy.Type.DIRECT) return false;
        if (this.route.proxy().type() != Proxy.Type.DIRECT) return false;
        if (!this.route.socketAddress().equals(route.socketAddress())) return false;

        if (route.address().hostnameVerifier() != OkHostnameVerifier.INSTANCE) return false;
        if (!supportsUrl(address.url())) return false;

        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }

        return true;
    }

    public boolean supportsUrl(Url url) {
        if (url.port() != route.address().url().port()) {
            return false; // Port mismatch.
        }

        if (!url.host().equals(route.address().url().host())) {
            // We have a host mismatch. But if the certificate matches, we're still good.
            return handshake != null && OkHostnameVerifier.INSTANCE.verify(
                    url.host(), (X509Certificate) handshake.peerCertificates().get(0));
        }

        return true; // Success. The URL is supported.
    }

    public HttpCodec newCodec(Client client, Interceptor.Chain chain,
                              StreamAllocation streamAllocation) throws SocketException {
        if (http2Connection != null) {
            return new Http2Codec(client, chain, streamAllocation, http2Connection);
        } else {
            socket.setSoTimeout(chain.readTimeoutMillis());
            source.timeout().timeout(chain.readTimeoutMillis(), MILLISECONDS);
            sink.timeout().timeout(chain.writeTimeoutMillis(), MILLISECONDS);
            return new Http1Codec(client, streamAllocation, source, sink);
        }
    }

    public RealWebSocket.Streams newWebSocketStreams(final StreamAllocation streamAllocation) {
        return new RealWebSocket.Streams(true, source, sink) {
            @Override
            public void close() throws IOException {
                streamAllocation.streamFinished(true, streamAllocation.codec(), -1L, null);
            }
        };
    }

    @Override
    public Route route() {
        return route;
    }

    public void cancel() {
        // Close the raw socket so we don't end up doing synchronous I/O.
        Internal.closeQuietly(rawSocket);
    }

    @Override
    public Socket socket() {
        return socket;
    }

    public boolean isHealthy(boolean doExtensiveChecks) {
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            return false;
        }

        if (http2Connection != null) {
            return !http2Connection.isShutdown();
        }

        if (doExtensiveChecks) {
            try {
                int readTimeout = socket.getSoTimeout();
                try {
                    socket.setSoTimeout(1);
                    if (source.exhausted()) {
                        return false; // Stream is exhausted; socket is closed.
                    }
                    return true;
                } finally {
                    socket.setSoTimeout(readTimeout);
                }
            } catch (SocketTimeoutException ignored) {
                // Read timed out; socket is good.
            } catch (IOException e) {
                return false; // Couldn't read; socket is closed.
            }
        }

        return true;
    }

    @Override
    public void onStream(Http2Stream stream) throws IOException {
        stream.close(ErrorCode.REFUSED_STREAM);
    }

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

    public boolean isMultiplexed() {
        return http2Connection != null;
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return "Connection{"
                + route.address().url().host() + ":" + route.address().url().port()
                + ", proxy="
                + route.proxy()
                + " hostAddress="
                + route.socketAddress()
                + " cipherSuite="
                + (handshake != null ? handshake.cipherSuite() : "none")
                + " protocol="
                + protocol
                + '}';
    }

}
