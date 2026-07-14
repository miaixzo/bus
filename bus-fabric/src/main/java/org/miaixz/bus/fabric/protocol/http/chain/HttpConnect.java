/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol.http.chain;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Connector;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.dns.DnsResult;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
import org.miaixz.bus.fabric.registry.route.Route;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP chain stage that leases or opens the route connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpConnect implements HttpStage {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Maximum proxy response header size.
     */
    private static final int MAX_PROXY_HEADER = Normal._64 * Normal._1024;

    /**
     * SOCKS protocol version supported by this connector.
     */
    private static final byte SOCKS5 = 0x05;

    /**
     * SOCKS no-authentication method.
     */
    private static final byte SOCKS_NO_AUTH = 0x00;

    /**
     * SOCKS CONNECT command.
     */
    private static final byte SOCKS_CONNECT = 0x01;

    /**
     * SOCKS IPv4 address type.
     */
    private static final byte SOCKS_ATYP_IPV4 = 0x01;

    /**
     * SOCKS domain address type.
     */
    private static final byte SOCKS_ATYP_DOMAIN = 0x03;

    /**
     * SOCKS IPv6 address type.
     */
    private static final byte SOCKS_ATYP_IPV6 = 0x04;

    /**
     * Response release states keyed by tracked responses.
     */
    private static final Map<HttpResponse, ReleaseState> RELEASES = Collections
            .synchronizedMap(new IdentityHashMap<>());

    /**
     * Stage name.
     */
    private final String name;

    /**
     * Connection pool.
     */
    private final ConnectionPool pool;

    /**
     * Network connector.
     */
    private final Connector connector;

    /**
     * TLS context.
     */
    private final TlsContext tlsContext;

    /**
     * TLS settings.
     */
    private final TlsSettings tlsSettings;

    /**
     * Lifecycle listener.
     */
    private final Listener<Object> listener;

    /**
     * DNS resolver used when a new TCP connection is opened.
     */
    private final DnsResolver resolver;

    /**
     * Runtime dispatcher for connection and socket operations.
     */
    private final Dispatcher dispatcher;

    /**
     * Creates a connect stage with a default socket connector.
     */
    public HttpConnect() {
        this(ConnectionPool.create(null), TlsContext.defaults(), TlsSettings.defaults(), Wiring.noop(),
                DnsResolver.system(), Dispatcher.create());
    }

    /**
     * Creates a connect stage with a shared connection pool.
     *
     * @param pool connection pool
     */
    public HttpConnect(final ConnectionPool pool) {
        this(pool, TlsContext.defaults(), TlsSettings.defaults(), Wiring.noop(), DnsResolver.system(),
                Dispatcher.create());
    }

    /**
     * Creates a connect stage with shared connection pool and TLS dependencies.
     *
     * @param pool        connection pool
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     */
    public HttpConnect(final ConnectionPool pool, final TlsContext tlsContext, final TlsSettings tlsSettings) {
        this(pool, tlsContext, tlsSettings, Wiring.noop(), DnsResolver.system(), Dispatcher.create());
    }

    /**
     * Creates a connect stage with shared connection pool, TLS dependencies, and lifecycle listener.
     *
     * @param pool        connection pool
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     * @param listener    lifecycle listener
     */
    public HttpConnect(final ConnectionPool pool, final TlsContext tlsContext, final TlsSettings tlsSettings,
            final Listener<Object> listener) {
        this(pool, tlsContext, tlsSettings, listener, DnsResolver.system(), Dispatcher.create());
    }

    /**
     * Creates a connect stage with shared connection pool, TLS dependencies, lifecycle listener, and DNS resolver.
     *
     * @param pool        connection pool
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     * @param listener    lifecycle listener
     * @param resolver    DNS resolver
     */
    public HttpConnect(final ConnectionPool pool, final TlsContext tlsContext, final TlsSettings tlsSettings,
            final Listener<Object> listener, final DnsResolver resolver) {
        this(pool, tlsContext, tlsSettings, listener, resolver, Dispatcher.create());
    }

    /**
     * Creates a connect stage with shared connection pool, TLS dependencies, lifecycle listener, DNS resolver, and
     * dispatcher.
     *
     * @param pool        connection pool
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     * @param listener    lifecycle listener
     * @param resolver    DNS resolver
     * @param dispatcher  runtime dispatcher
     */
    public HttpConnect(final ConnectionPool pool, final TlsContext tlsContext, final TlsSettings tlsSettings,
            final Listener<Object> listener, final DnsResolver resolver, final Dispatcher dispatcher) {
        this(pool, new SocketConnector(listener, resolver, dispatcher), tlsContext, tlsSettings, listener, resolver,
                dispatcher);
    }

    /**
     * Creates a connect stage with explicit dependencies.
     *
     * @param pool        connection pool
     * @param connector   network connector
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     */
    HttpConnect(final ConnectionPool pool, final Connector connector, final TlsContext tlsContext,
            final TlsSettings tlsSettings) {
        this(pool, connector, tlsContext, tlsSettings, Wiring.noop(), DnsResolver.system(), Dispatcher.create());
    }

    /**
     * Creates a connect stage with explicit dependencies.
     *
     * @param pool        connection pool
     * @param connector   network connector
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     * @param listener    lifecycle listener
     */
    HttpConnect(final ConnectionPool pool, final Connector connector, final TlsContext tlsContext,
            final TlsSettings tlsSettings, final Listener<Object> listener) {
        this(pool, connector, tlsContext, tlsSettings, listener, DnsResolver.system(), Dispatcher.create());
    }

    /**
     * Creates a connect stage with explicit dependencies.
     *
     * @param pool        connection pool
     * @param connector   network connector
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     * @param listener    lifecycle listener
     * @param resolver    DNS resolver
     */
    HttpConnect(final ConnectionPool pool, final Connector connector, final TlsContext tlsContext,
            final TlsSettings tlsSettings, final Listener<Object> listener, final DnsResolver resolver) {
        this(pool, connector, tlsContext, tlsSettings, listener, resolver, Dispatcher.create());
    }

    /**
     * Creates a connect stage with explicit dependencies.
     *
     * @param pool        connection pool
     * @param connector   network connector
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     * @param listener    lifecycle listener
     * @param resolver    DNS resolver
     * @param dispatcher  runtime dispatcher
     */
    HttpConnect(final ConnectionPool pool, final Connector connector, final TlsContext tlsContext,
            final TlsSettings tlsSettings, final Listener<Object> listener, final DnsResolver resolver,
            final Dispatcher dispatcher) {
        this.name = normalizeName("http-connect");
        this.pool = require(pool, "Connection pool");
        this.connector = require(connector, "Network connector");
        this.tlsContext = tlsContext;
        this.tlsSettings = tlsSettings;
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        this.resolver = require(resolver, "DNS resolver");
        this.dispatcher = require(dispatcher, "Dispatcher");
    }

    /**
     * Acquires a connection, proceeds, and attaches release behavior to the response body.
     *
     * @param request request
     * @param chain   chain
     * @return response
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        final Cancellation cancellation = next.cancellation();
        cancellation.throwIfCancelled();
        Logger.debug(
                true,
                LOG_TAG,
                "HTTP connect stage started: method={}, host={}, port={}, secure={}",
                current.method().value(),
                current.url().host(),
                current.url().port(),
                current.url().address().secure());
        final ConnectionLease lease = acquire(current, cancellation);
        try {
            cancellation.throwIfCancelled();
            final HttpResponse response = next.withConnection(lease, lease.connection()).proceed(current);
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP connect stage response received: host={}, port={}, code={}",
                    current.url().host(),
                    current.url().port(),
                    response.code());
            return track(lease, response);
        } catch (final RuntimeException e) {
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP connect stage failed: host={}, port={}, exception={}",
                    current.url().host(),
                    current.url().port(),
                    e.getClass().getSimpleName());
            closeLease(lease, "Unable to close connection after HTTP chain failure");
            throw e;
        }
    }

    /**
     * Acquires a route lease from the connection pool.
     *
     * @param request request
     * @return connection lease
     */
    public ConnectionLease acquire(final HttpRequest request) {
        return acquire(request, Cancellation.create());
    }

    /**
     * Acquires a route lease from the connection pool with a cancellation scope.
     *
     * @param request      request
     * @param cancellation cancellation scope
     * @return connection lease
     */
    public ConnectionLease acquire(final HttpRequest request, final Cancellation cancellation) {
        final HttpRequest current = require(request, "HTTP request");
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final Address target = current.url().address();
        final ProxyPlan proxy = proxy(current);
        validateProxy(proxy);
        final Destination destination = destination(target, proxy);
        Logger.debug(
                true,
                LOG_TAG,
                "HTTP connection lease acquisition started: host={}, port={}, secure={}, proxyMode={}, tunnel={}",
                target.host(),
                target.port(),
                target.secure(),
                proxyMode(proxy),
                proxy.requiresTunnel(target));
        final ConnectionLease lease = pool
                .acquire(destination, () -> open(destination, target, proxy, current.timeout(), scope), scope);
        scope.onCancel(lease::close);
        scope.throwIfCancelled();
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP connection lease acquired: host={}, port={}, healthy={}, proxyMode={}",
                target.host(),
                target.port(),
                lease.connection().healthy(),
                proxyMode(proxy));
        return lease;
    }

    /**
     * Releases or closes a lease based on response consumption state.
     *
     * @param lease    lease
     * @param response response
     */
    public void release(final ConnectionLease lease, final HttpResponse response) {
        final ConnectionLease current = require(lease, "Connection lease");
        final HttpResponse target = require(response, "HTTP response");
        final ReleaseState state = RELEASES.get(target);
        if (state != null) {
            if (!state.matches(current)) {
                throw new ValidateException("Connection lease does not match HTTP response");
            }
            state.release();
            return;
        }
        releaseUntracked(current, target);
    }

    /**
     * Returns stage name.
     *
     * @return stage name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Returns whether this stage touches the network.
     *
     * @return true
     */
    @Override
    public boolean network() {
        return true;
    }

    /**
     * Opens a network route for a connection destination.
     *
     * @param destination connection destination
     * @param target      target address
     * @param proxy       proxy plan
     * @param timeout     timeout
     * @return network connection
     */
    private Connection open(
            final Destination destination,
            final Address target,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final boolean tunnel = proxy.requiresTunnel(target);
        final Address connectAddress = connectAddress(target, proxy, connector.supports(Transport.TLS));
        Logger.debug(
                true,
                LOG_TAG,
                "HTTP route open started: targetHost={}, targetPort={}, connectHost={}, connectPort={}, "
                        + "proxyMode={}, tunnel={}, nativeTls={}",
                target.host(),
                target.port(),
                connectAddress.host(),
                connectAddress.port(),
                proxyMode(proxy),
                tunnel,
                connector.supports(Transport.TLS));
        final Connection raw = awaitConnection(connector.connect(connectAddress, timeout), timeout, scope);
        final Runnable unregisterRaw = scope.onCancel(raw::close);
        try {
            scope.throwIfCancelled();
            if (proxy.isSocks()) {
                socks(raw, target, timeout, scope);
            }
            if (tunnel) {
                tunnel(raw, target, proxy, timeout, scope);
            }
            scope.throwIfCancelled();
            if (target.secure() && (tunnel || !connector.supports(Transport.TLS))) {
                final Connection secured = tlsConnection(destination, raw, target, scope);
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP route open completed with TLS wrapper: host={}, port={}",
                        target.host(),
                        target.port());
                return secured;
            }
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP route open completed: host={}, port={}, secure={}",
                    target.host(),
                    target.port(),
                    target.secure());
            return new RoutedConnection(destination, raw);
        } catch (final RuntimeException e) {
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP route open failed: host={}, port={}, exception={}",
                    target.host(),
                    target.port(),
                    e.getClass().getSimpleName());
            closeConnection(raw, "Unable to close failed HTTP route");
            throw e;
        } finally {
            unregisterRaw.run();
        }
    }

    /**
     * Creates a TLS-routed connection.
     *
     * @param destination  connection destination
     * @param raw          raw connection
     * @param target       target address
     * @param cancellation cancellation scope
     * @return TLS connection
     */
    private Connection tlsConnection(
            final Destination destination,
            final Connection raw,
            final Address target,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final TlsEngine engine = TlsEngine
                .create(require(tlsContext, "TLS context"), target, require(tlsSettings, "TLS settings"));
        final TlsChannel tlsChannel = TlsChannel.wrap(raw.conduit(), engine, listener, dispatcher);
        final Runnable unregisterTls = scope.onCancel(tlsChannel::close);
        try {
            Logger.debug(true, LOG_TAG, "HTTP TLS handshake started: host={}, port={}", target.host(), target.port());
            final TlsHandshake handshake = await(
                    tlsChannel.handshake(),
                    Duration.ZERO,
                    "TLS handshake timed out",
                    scope);
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP TLS handshake completed: host={}, port={}",
                    target.host(),
                    target.port());
            return new TlsRoutedConnection(destination, raw, tlsChannel, handshake);
        } finally {
            unregisterTls.run();
        }
    }

    /**
     * Performs an HTTP CONNECT tunnel handshake through a proxy.
     *
     * @param connection   proxy connection
     * @param target       target address
     * @param proxy        proxy plan
     * @param timeout      timeout
     * @param cancellation cancellation scope
     */
    private void tunnel(
            final Connection connection,
            final Address target,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        Logger.debug(
                true,
                LOG_TAG,
                "HTTP CONNECT tunnel started: targetHost={}, targetPort={}",
                target.host(),
                target.port());
        final String request = connectRequest(target, proxy.authorization());
        writeAll(
                connection,
                ByteString.encodeString(request, org.miaixz.bus.core.lang.Charset.US_ASCII).asByteBuffer(),
                timeout.write(),
                scope);
        final String response = readHeader(connection, timeout.read(), scope);
        if (!response.startsWith(Protocol.HTTP_1_1 + " 200 ") && !response.startsWith(Protocol.HTTP_1_0 + " 200 ")
                && !response.startsWith("HTTP/2 200 ")) {
            throw new ProtocolException("HTTP CONNECT tunnel failed");
        }
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP CONNECT tunnel completed: targetHost={}, targetPort={}",
                target.host(),
                target.port());
    }

    /**
     * Performs a SOCKS5 CONNECT handshake through a proxy.
     *
     * @param connection   proxy connection
     * @param target       target address
     * @param timeout      timeout
     * @param cancellation cancellation scope
     */
    private void socks(
            final Connection connection,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        Logger.debug(
                true,
                LOG_TAG,
                "SOCKS handshake started: targetHost={}, targetPort={}",
                target.host(),
                target.port());
        writeAll(connection, ByteBuffer.wrap(new byte[] { SOCKS5, 0x01, SOCKS_NO_AUTH }), timeout.write(), scope);
        final byte[] selection = readExact(connection, 2, timeout.read(), "SOCKS method selection timed out", scope);
        if (selection[0] != SOCKS5 || selection[1] != SOCKS_NO_AUTH) {
            throw new ProtocolException("SOCKS proxy requires an unsupported authentication method");
        }
        writeAll(connection, ByteBuffer.wrap(socksConnectRequest(target)), timeout.write(), scope);
        final byte[] header = readExact(connection, 4, timeout.read(), "SOCKS connect response timed out", scope);
        if (header[0] != SOCKS5) {
            throw new ProtocolException("Invalid SOCKS response version");
        }
        if (header[1] != 0x00) {
            throw new ProtocolException("SOCKS CONNECT failed with reply " + (header[1] & 0xff));
        }
        final int addressLength = switch (header[3]) {
            case SOCKS_ATYP_IPV4 -> 4;
            case SOCKS_ATYP_DOMAIN -> readExact(
                    connection,
                    1,
                    timeout.read(),
                    "SOCKS domain length timed out",
                    scope)[0] & 0xff;
            case SOCKS_ATYP_IPV6 -> 16;
            default -> throw new ProtocolException("Unsupported SOCKS address type");
        };
        readExact(connection, addressLength + 2, timeout.read(), "SOCKS bind address timed out", scope);
        Logger.debug(
                false,
                LOG_TAG,
                "SOCKS handshake completed: targetHost={}, targetPort={}, addressType={}",
                target.host(),
                target.port(),
                header[3] & 0xff);
    }

    /**
     * Tracks a response so body completion releases the lease.
     *
     * @param lease    lease
     * @param response response
     * @return tracked response
     */
    private HttpResponse track(final ConnectionLease lease, final HttpResponse response) {
        final HttpResponse source = require(response, "HTTP response");
        final ReleaseState state = new ReleaseState(lease);
        final PayloadBody body = PayloadBody.of(new LeasePayload(source.body().payload(), state), source.body().media());
        final HttpResponse.Builder builder = source.toBuilder().body(body);
        if (source.handshake() == null && lease.connection() instanceof TlsRoutedConnection tls) {
            builder.handshake(tls.handshake());
        }
        final HttpResponse tracked = builder.build();
        state.response(tracked);
        RELEASES.put(tracked, state);
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP response lease tracking installed: code={}, repeatable={}, healthy={}",
                source.code(),
                source.body().payload().repeatable(),
                lease.connection().healthy());
        return tracked;
    }

    /**
     * Releases an untracked response using conservative body state.
     *
     * @param lease    lease
     * @param response response
     */
    private void releaseUntracked(final ConnectionLease lease, final HttpResponse response) {
        try {
            if (response.body().payload().repeatable() && lease.connection().healthy()) {
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP untracked lease released: code={}, repeatable={}, healthy={}",
                        response.code(),
                        true,
                        true);
                lease.release();
            } else {
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP untracked lease closed: code={}, repeatable={}, healthy={}",
                        response.code(),
                        response.body().payload().repeatable(),
                        lease.connection().healthy());
                lease.close();
            }
        } catch (final RuntimeException e) {
            throw internal("Unable to release HTTP connection", e);
        }
    }

    /**
     * Builds a destination for pooling.
     *
     * @param target target address
     * @param proxy  proxy plan
     * @return connection destination
     */
    private static Destination destination(final Address target, final ProxyPlan proxy) {
        final LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("tls", target.secure());
        options.put("proxy", proxy.proxy().map(Address::toUri).map(Object::toString).orElse("direct"));
        options.put("tunnel", proxy.requiresTunnel(target));
        return Destination.of(protocol(target), target, Options.from(options));
    }

    /**
     * Selects the address to connect before optional tunnel and TLS wrapping.
     *
     * @param target    target address
     * @param proxy     proxy plan
     * @param nativeTls whether connector supports TLS directly
     * @return connect address
     */
    private static Address connectAddress(final Address target, final ProxyPlan proxy, final boolean nativeTls) {
        return proxy.proxy().orElseGet(() -> target.secure() && !nativeTls ? tcpAddress(target) : target);
    }

    /**
     * Converts a secure target to its underlying TCP address.
     *
     * @param target target address
     * @return TCP address
     */
    private static Address tcpAddress(final Address target) {
        return new Address(Protocol.TCP.toString(), target.host(), target.port(), target.path());
    }

    /**
     * Selects the pooling protocol.
     *
     * @param address address
     * @return protocol
     */
    private static Protocol protocol(final Address address) {
        return address.secure() ? Protocol.HTTPS : Protocol.HTTP;
    }

    /**
     * Reads proxy plan from the request, keeping tag-based routes as a secondary fallback.
     *
     * @param request request
     * @return proxy plan
     */
    private static ProxyPlan proxy(final HttpRequest request) {
        final ProxyPlan configured = request.proxy();
        if (!configured.isDirect()) {
            return configured;
        }
        final Object tag = request.tag();
        if (tag instanceof ProxyPlan plan) {
            return plan;
        }
        if (tag instanceof Route route) {
            return route.proxy();
        }
        return configured;
    }

    /**
     * Returns a short proxy mode label for logs.
     *
     * @param proxy proxy plan
     * @return proxy mode
     */
    private static String proxyMode(final ProxyPlan proxy) {
        if (proxy.isDirect()) {
            return "direct";
        }
        if (proxy.isSocks()) {
            return "socks";
        }
        if (proxy.isHttp()) {
            return Protocol.HTTP.toString();
        }
        return "custom";
    }

    /**
     * Validates proxy plans that this stage can open.
     *
     * @param proxy proxy plan
     */
    private static void validateProxy(final ProxyPlan proxy) {
        proxy.proxy().ifPresent(address -> {
            if (proxy.isHttp() && !Protocol.HTTP.toString().equals(address.scheme())) {
                throw new ProtocolException("Unsupported HTTP proxy transport");
            }
            if (proxy.isSocks() && (address.secure() || !Transport.fromScheme(address.scheme()).connectionOriented())) {
                throw new ProtocolException("Unsupported SOCKS proxy transport");
            }
        });
    }

    /**
     * Creates an HTTP CONNECT request.
     *
     * @param target        target address
     * @param authorization authorization headers
     * @return request text
     */
    private static String connectRequest(final Address target, final Headers authorization) {
        final String authority = authority(target);
        final StringBuilder builder = new StringBuilder();
        builder.append(HTTP.CONNECT).append(Symbol.C_SPACE).append(authority).append(Symbol.C_SPACE)
                .append(Protocol.HTTP_1_1).append(Symbol.CRLF);
        builder.append(HTTP.HOST).append(Symbol.COLON).append(Symbol.SPACE).append(authority).append(Symbol.CRLF);
        builder.append(HTTP.PROXY_CONNECTION).append(Symbol.COLON).append(Symbol.SPACE)
                .append(HTTP.CONNECTION_KEEP_ALIVE).append(Symbol.CRLF);
        for (final Map.Entry<String, List<String>> entry : authorization.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                builder.append(entry.getKey()).append(Symbol.COLON).append(Symbol.SPACE).append(value)
                        .append(Symbol.CRLF);
            }
        }
        builder.append(Symbol.CRLF);
        return builder.toString();
    }

    /**
     * Creates a SOCKS5 CONNECT request.
     *
     * @param target target address
     * @return request bytes
     */
    private static byte[] socksConnectRequest(final Address target) {
        final byte[] ipv4 = ipv4(target.host());
        final byte[] host = ipv4 == null
                ? ByteString.encodeString(target.host(), org.miaixz.bus.core.lang.Charset.UTF_8).toByteArray()
                : ipv4;
        if (host.length > 255) {
            throw new ProtocolException("SOCKS target host is too long");
        }
        final int capacity = ipv4 == null ? 7 + host.length : 6 + host.length;
        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(SOCKS5).put(SOCKS_CONNECT).put((byte) 0x00);
        if (ipv4 == null) {
            buffer.put(SOCKS_ATYP_DOMAIN).put((byte) host.length);
        } else {
            buffer.put(SOCKS_ATYP_IPV4);
        }
        buffer.put(host).putShort((short) target.port());
        return buffer.array();
    }

    /**
     * Parses an IPv4 literal without resolving DNS.
     *
     * @param host host text
     * @return four bytes or null when not IPv4
     */
    private static byte[] ipv4(final String host) {
        try {
            return ByteBuffer.allocate(Integer.BYTES).putInt((int) NetKit.ipv4ToLong(host)).array();
        } catch (final RuntimeException e) {
            return null;
        }
    }

    /**
     * Formats host and port for HTTP authority use.
     *
     * @param address address
     * @return authority
     */
    private static String authority(final Address address) {
        final String host = address.host().indexOf(Symbol.C_COLON) >= 0
                ? Symbol.BRACKET_LEFT + address.host() + Symbol.BRACKET_RIGHT
                : address.host();
        return host + Symbol.C_COLON + address.port();
    }

    /**
     * Writes a whole buffer.
     *
     * @param connection   connection
     * @param source       source
     * @param timeout      timeout
     * @param cancellation cancellation scope
     */
    private static void writeAll(
            final Connection connection,
            final ByteBuffer source,
            final Duration timeout,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        while (source.hasRemaining()) {
            scope.throwIfCancelled();
            final int position = source.position();
            final int written = await(connection.write(source), timeout, "HTTP CONNECT write timed out", scope);
            if (written < 0) {
                throw new SocketException("HTTP CONNECT write reached EOF");
            }
            if (written == 0) {
                Thread.yield();
            } else {
                source.position(position + written);
            }
        }
    }

    /**
     * Reads a proxy response header.
     *
     * @param connection   connection
     * @param timeout      timeout
     * @param cancellation cancellation scope
     * @return response header
     */
    private static String readHeader(
            final Connection connection,
            final Duration timeout,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final ByteBuffer buffer = ByteBuffer.allocate(Normal._1);
        final StringBuilder header = new StringBuilder();
        while (header.length() < MAX_PROXY_HEADER) {
            scope.throwIfCancelled();
            buffer.clear();
            final int position = buffer.position();
            final int read = await(connection.read(buffer), timeout, "HTTP CONNECT read timed out", scope);
            if (read < 0) {
                throw new SocketException("HTTP CONNECT response reached EOF");
            }
            if (read == 0) {
                Thread.yield();
                continue;
            }
            buffer.position(position + read);
            buffer.flip();
            while (buffer.hasRemaining()) {
                header.append((char) (buffer.get() & 0xff));
            }
            if (header.indexOf(Symbol.CRLF + Symbol.CRLF) >= Normal._0) {
                return header.toString();
            }
        }
        throw new ProtocolException("HTTP CONNECT response header is too large");
    }

    /**
     * Reads exactly the requested number of bytes.
     *
     * @param connection   connection
     * @param length       length
     * @param timeout      timeout
     * @param message      timeout message
     * @param cancellation cancellation scope
     * @return bytes
     */
    private static byte[] readExact(
            final Connection connection,
            final int length,
            final Duration timeout,
            final String message,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final ByteBuffer buffer = ByteBuffer.allocate(length);
        while (buffer.hasRemaining()) {
            scope.throwIfCancelled();
            final int position = buffer.position();
            final int read = await(connection.read(buffer), timeout, message, scope);
            if (read < 0) {
                throw new SocketException("SOCKS response reached EOF");
            }
            if (read == 0) {
                Thread.yield();
            } else {
                buffer.position(position + read);
            }
        }
        return buffer.array();
    }

    /**
     * Waits for a connection future.
     *
     * @param future  future
     * @param timeout timeout
     * @return connection
     */
    private static Connection awaitConnection(final CompletableFuture<Connection> future, final Timeout timeout) {
        return awaitConnection(future, timeout, Cancellation.create());
    }

    /**
     * Waits for a connection future with cancellation support.
     *
     * @param future       future
     * @param timeout      timeout
     * @param cancellation cancellation scope
     * @return connection
     */
    private static Connection awaitConnection(
            final CompletableFuture<Connection> future,
            final Timeout timeout,
            final Cancellation cancellation) {
        return require(await(future, timeout.connect(), "Connection timed out", cancellation), "Network connection");
    }

    /**
     * Waits for TLS handshake completion.
     *
     * @param future future
     */
    private static void awaitTls(final CompletableFuture<?> future) {
        awaitTls(future, Cancellation.create());
    }

    /**
     * Waits for TLS handshake completion with cancellation support.
     *
     * @param future       future
     * @param cancellation cancellation scope
     */
    private static void awaitTls(final CompletableFuture<?> future, final Cancellation cancellation) {
        await(future, Duration.ZERO, "TLS handshake timed out", cancellation);
    }

    /**
     * Waits for a future with bus exceptions.
     *
     * @param future  future
     * @param timeout timeout
     * @param message timeout message
     * @param <T>     result type
     * @return result
     */
    private static <T> T await(final CompletableFuture<T> future, final Duration timeout, final String message) {
        return await(future, timeout, message, Cancellation.create());
    }

    /**
     * Waits for a future with bus exceptions and cancellation support.
     *
     * @param future       future
     * @param timeout      timeout
     * @param message      timeout message
     * @param cancellation cancellation scope
     * @param <T>          result type
     * @return result
     */
    private static <T> T await(
            final CompletableFuture<T> future,
            final Duration timeout,
            final String message,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final Runnable unregister = scope.onCancel(() -> future.cancel(true));
        try {
            final T result = timeout.isZero() ? future.get()
                    : future.get(Math.max(1L, timeout.toMillis()), TimeUnit.MILLISECONDS);
            scope.throwIfCancelled();
            return result;
        } catch (final java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException(message, e);
        } catch (final java.util.concurrent.CancellationException e) {
            scope.throwIfCancelled();
            throw e;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP connection", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("HTTP connection failed", cause);
        } finally {
            unregister.run();
        }
    }

    /**
     * Closes a lease after failure.
     *
     * @param lease   lease
     * @param message message
     */
    private static void closeLease(final ConnectionLease lease, final String message) {
        try {
            lease.close();
        } catch (final RuntimeException e) {
            throw internal(message, e);
        }
    }

    /**
     * Closes a connection after failure.
     *
     * @param connection connection
     * @param message    message
     */
    private static void closeConnection(final Connection connection, final String message) {
        try {
            connection.close();
        } catch (final RuntimeException e) {
            throw internal(message, e);
        }
    }

    /**
     * Normalizes a stage name.
     *
     * @param value value
     * @return normalized name
     */
    private static String normalizeName(final String value) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("HTTP connect name must be non-blank and single-line"));
        return StringKit.trim(value).toLowerCase(Locale.ROOT);
    }

    /**
     * Wraps a runtime failure as an internal failure when needed.
     *
     * @param message message
     * @param failure failure
     * @return internal exception
     */
    private static InternalException internal(final String message, final RuntimeException failure) {
        return failure instanceof InternalException internal ? internal : new InternalException(message, failure);
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Release state shared by tracked response payloads.
     */
    private static final class ReleaseState {

        /**
         * Lease to release.
         */
        private final ConnectionLease lease;

        /**
         * Body completion flag.
         */
        private final AtomicBoolean complete;

        /**
         * Body failure flag.
         */
        private final AtomicBoolean broken;

        /**
         * Release completion flag.
         */
        private final AtomicBoolean released;

        /**
         * Tracked response.
         */
        private volatile HttpResponse response;

        /**
         * Creates a release state.
         *
         * @param lease lease
         */
        private ReleaseState(final ConnectionLease lease) {
            this.lease = require(lease, "Connection lease");
            this.complete = new AtomicBoolean();
            this.broken = new AtomicBoolean();
            this.released = new AtomicBoolean();
        }

        /**
         * Stores the tracked response.
         *
         * @param response response
         */
        private void response(final HttpResponse response) {
            this.response = require(response, "HTTP response");
        }

        /**
         * Marks the body fully consumed.
         */
        private void complete() {
            complete.set(true);
        }

        /**
         * Marks the body as broken.
         */
        private void broken() {
            broken.set(true);
        }

        /**
         * Returns whether this state belongs to a lease.
         *
         * @param current lease
         * @return true when matching
         */
        private boolean matches(final ConnectionLease current) {
            return lease == current;
        }

        /**
         * Releases or closes the lease once.
         */
        private void release() {
            if (!released.compareAndSet(false, true)) {
                return;
            }
            try {
                if (complete.get() && !broken.get() && lease.connection().healthy()) {
                    Logger.debug(
                            false,
                            LOG_TAG,
                            "HTTP tracked lease released: complete={}, broken={}, healthy={}",
                            complete.get(),
                            broken.get(),
                            lease.connection().healthy());
                    lease.release();
                } else {
                    Logger.debug(
                            false,
                            LOG_TAG,
                            "HTTP tracked lease closed: complete={}, broken={}, healthy={}",
                            complete.get(),
                            broken.get(),
                            lease.connection().healthy());
                    lease.close();
                }
            } catch (final RuntimeException e) {
                throw internal("Unable to release HTTP connection", e);
            } finally {
                final HttpResponse tracked = response;
                if (tracked != null) {
                    RELEASES.remove(tracked);
                }
            }
        }

    }

    /**
     * Payload wrapper that releases the lease when consumed or closed.
     */
    private static final class LeasePayload implements Payload, AutoCloseable {

        /**
         * Delegate payload.
         */
        private final Payload delegate;

        /**
         * Release state.
         */
        private final ReleaseState state;

        /**
         * Creates a lease payload.
         *
         * @param delegate delegate
         * @param state    release state
         */
        private LeasePayload(final Payload delegate, final ReleaseState state) {
            this.delegate = require(delegate, "Payload");
            this.state = require(state, "Release state");
        }

        /**
         * Returns payload length.
         *
         * @return length
         */
        @Override
        public long length() {
            return delegate.length();
        }

        @Override
        public Source source() {
            return new LeaseSource(delegate.source(), state);
        }

        /**
         * Reads payload bytes and releases reusable connections.
         *
         * @return bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads payload bytes and releases reusable connections.
         *
         * @param maxBytes maximum bytes to materialize
         * @return bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            try {
                final byte[] data = Payload.materialize(this, maxBytes, "HttpConnect.LeasePayload.bytes(long)");
                state.complete();
                state.release();
                return data;
            } catch (final RuntimeException e) {
                state.broken();
                state.release();
                throw e;
            }
        }

        /**
         * Reads payload text.
         *
         * @param charset charset
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public String text(final Charset charset, final long maxBytes) {
            return new String(bytes(maxBytes),
                    Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
        }

        /**
         * Returns repeatability.
         *
         * @return repeatability
         */
        @Override
        public boolean repeatable() {
            return delegate.repeatable();
        }

        /**
         * Closes the payload and releases the lease when needed.
         *
         * @throws Exception when the delegate fails to close
         */
        @Override
        public void close() throws Exception {
            try {
                if (delegate instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            } catch (final Exception e) {
                state.broken();
                state.release();
                throw e;
            }
            state.release();
        }

    }

    /**
     * Source that tracks EOF and close semantics.
     */
    private static final class LeaseSource implements Source {

        /**
         * Delegate source.
         */
        private final Source delegate;

        /**
         * Release state.
         */
        private final ReleaseState state;

        /**
         * Close flag.
         */
        private final AtomicBoolean closed;

        /**
         * Creates a lease source.
         *
         * @param delegate delegate
         * @param state    release state
         */
        private LeaseSource(final Source delegate, final ReleaseState state) {
            this.delegate = require(delegate, "Source");
            this.state = require(state, "Release state");
            this.closed = new AtomicBoolean();
        }

        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            try {
                final long read = delegate.read(sink, byteCount);
                if (read < 0) {
                    state.complete();
                    state.release();
                }
                return read;
            } catch (final IOException e) {
                state.broken();
                state.release();
                throw e;
            }
        }

        @Override
        public org.miaixz.bus.core.io.timout.Timeout timeout() {
            return delegate.timeout();
        }

        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                delegate.close();
            } catch (final IOException e) {
                state.broken();
                state.release();
                throw e;
            }
            state.release();
        }

    }

    /**
     * Connection wrapper that exposes the HTTP route destination.
     */
    private static class RoutedConnection implements Connection {

        /**
         * Route destination.
         */
        private final Destination destination;

        /**
         * Delegate connection.
         */
        private final Connection delegate;

        /**
         * Creates a routed connection.
         *
         * @param destination route destination
         * @param delegate    delegate
         */
        RoutedConnection(final Destination destination, final Connection delegate) {
            this.destination = require(destination, "Connection destination");
            this.delegate = require(delegate, "Network connection");
        }

        /**
         * Returns route destination.
         *
         * @return destination
         */
        @Override
        public Destination destination() {
            return destination;
        }

        /**
         * Returns the network conduit.
         *
         * @return conduit
         */
        @Override
        public Conduit conduit() {
            return delegate.conduit();
        }

        /**
         * Returns state.
         *
         * @return state
         */
        @Override
        public Status state() {
            return delegate.state();
        }

        /**
         * Reads bytes.
         *
         * @param buffer target buffer
         * @return read future
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer buffer) {
            return delegate.read(buffer);
        }

        /**
         * Writes bytes.
         *
         * @param buffer source buffer
         * @return write future
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer buffer) {
            return delegate.write(buffer);
        }

        /**
         * Returns whether healthy.
         *
         * @return healthy flag
         */
        @Override
        public boolean healthy() {
            return delegate.healthy();
        }

        /**
         * Returns whether idle.
         *
         * @return idle flag
         */
        @Override
        public boolean idle() {
            return delegate.idle();
        }

        /**
         * Closes the delegate.
         */
        @Override
        public void close() {
            delegate.close();
        }

    }

    /**
     * Routed connection that delegates IO through TLS.
     */
    private static final class TlsRoutedConnection extends RoutedConnection {

        /**
         * Raw connection.
         */
        private final Connection raw;

        /**
         * TLS channel.
         */
        private final TlsChannel tls;

        /**
         * TLS handshake metadata.
         */
        private final TlsHandshake handshake;

        /**
         * Creates a TLS routed connection.
         *
         * @param destination route destination
         * @param raw         raw connection
         * @param tls         TLS channel
         * @param handshake   TLS handshake metadata
         */
        private TlsRoutedConnection(final Destination destination, final Connection raw, final TlsChannel tls,
                final TlsHandshake handshake) {
            super(destination, raw);
            this.raw = require(raw, "Raw connection");
            this.tls = require(tls, "TLS channel");
            this.handshake = require(handshake, "TLS handshake");
        }

        /**
         * Returns TLS handshake metadata.
         *
         * @return handshake
         */
        private TlsHandshake handshake() {
            return handshake;
        }

        /**
         * Reads plain TLS bytes.
         *
         * @param buffer target buffer
         * @return read future
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer buffer) {
            return tls.read(buffer);
        }

        /**
         * Writes plain TLS bytes.
         *
         * @param buffer source buffer
         * @return write future
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer buffer) {
            return tls.write(buffer);
        }

        /**
         * Returns whether healthy.
         *
         * @return healthy flag
         */
        @Override
        public boolean healthy() {
            return tls.opened() && raw.healthy();
        }

        /**
         * Closes TLS resources.
         */
        @Override
        public void close() {
            tls.close();
            raw.close();
        }

    }

    /**
     * Default socket connector used by the public constructor.
     */
    private static final class SocketConnector implements Connector {

        /**
         * Close flag.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Lifecycle listener.
         */
        private final Listener<Object> listener;

        /**
         * DNS resolver.
         */
        private final DnsResolver resolver;

        /**
         * Runtime dispatcher.
         */
        private final Dispatcher dispatcher;

        /**
         * Creates a socket connector.
         *
         * @param listener lifecycle listener
         */
        private SocketConnector(final Listener<Object> listener) {
            this(listener, DnsResolver.system(), Dispatcher.create());
        }

        /**
         * Creates a socket connector.
         *
         * @param listener lifecycle listener
         * @param resolver DNS resolver
         */
        private SocketConnector(final Listener<Object> listener, final DnsResolver resolver) {
            this(listener, resolver, Dispatcher.create());
        }

        /**
         * Creates a socket connector.
         *
         * @param listener   lifecycle listener
         * @param resolver   DNS resolver
         * @param dispatcher runtime dispatcher
         */
        private SocketConnector(final Listener<Object> listener, final DnsResolver resolver,
                final Dispatcher dispatcher) {
            this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
            this.resolver = require(resolver, "DNS resolver");
            this.dispatcher = require(dispatcher, "Dispatcher");
        }

        /**
         * Opens a socket connection.
         *
         * @param address target address
         * @param timeout timeout policy
         * @return connection future
         */
        @Override
        public CompletableFuture<Connection> connect(final Address address, final Timeout timeout) {
            require(address, "Address");
            require(timeout, "Timeout");
            if (!supports(Transport.fromScheme(address.scheme()))) {
                return CompletableFuture.failedFuture(new ProtocolException("Unsupported HTTP connect transport"));
            }
            return dispatcher
                    .supply("http:connect:" + address.host() + ":" + address.port(), () -> open(address, timeout));
        }

        /**
         * Returns supported transports.
         *
         * @param transport transport
         * @return true when supported
         */
        @Override
        public boolean supports(final Transport transport) {
            require(transport, "Network transport");
            return transport == Transport.TCP;
        }

        /**
         * Closes this connector.
         */
        @Override
        public void close() {
            closed.set(true);
        }

        /**
         * Opens a blocking socket channel.
         *
         * @param address address
         * @param timeout timeout
         * @return connection
         */
        private Connection open(final Address address, final Timeout timeout) {
            if (closed.get()) {
                throw new StatefulException("HTTP socket connector is closed");
            }
            final DnsResult result = resolver.resolve(address.host());
            if (result.addresses().isEmpty()) {
                final SocketException failure = new SocketException("DNS returned no address for " + address.host());
                listener.failure(this, failure);
                throw failure;
            }
            RuntimeException failure = null;
            for (final InetAddress candidate : result.addresses()) {
                SocketChannel channel = null;
                try {
                    channel = SocketChannel.open();
                    channel.socket().connect(
                            new InetSocketAddress(candidate, address.port()),
                            timeoutMillis(timeout.connect()));
                    final Connection connection = new SocketConnection(address, channel, listener, dispatcher);
                    listener.open(connection);
                    return connection;
                } catch (final SocketTimeoutException e) {
                    IoKit.closeQuietly(channel);
                    failure = new TimeoutException("Socket connect timed out", e);
                } catch (final IOException e) {
                    IoKit.closeQuietly(channel);
                    failure = new SocketException("Socket connect failed", e);
                }
            }
            listener.failure(this, failure);
            throw failure;
        }

        /**
         * Converts a timeout to socket milliseconds.
         *
         * @param timeout timeout
         * @return milliseconds
         */
        private static int timeoutMillis(final Duration timeout) {
            if (timeout.isZero()) {
                return 0;
            }
            return Math.toIntExact(Math.min(Integer.MAX_VALUE, Math.max(1L, timeout.toMillis())));
        }

    }

    /**
     * Socket-backed network connection.
     */
    private static final class SocketConnection implements Connection {

        /**
         * Connection destination.
         */
        private final Destination destination;

        /**
         * Socket channel.
         */
        private final SocketChannel socket;

        /**
         * Network conduit.
         */
        private final Conduit conduit;

        /**
         * State.
         */
        private volatile Status state = Status.OPENED;

        /**
         * Lifecycle listener.
         */
        private final Listener<Object> listener;

        /**
         * Creates a socket connection.
         *
         * @param address    address
         * @param socket     socket channel
         * @param listener   lifecycle listener
         * @param dispatcher runtime dispatcher
         */
        private SocketConnection(final Address address, final SocketChannel socket, final Listener<Object> listener,
                final Dispatcher dispatcher) {
            this.destination = Destination.of(address.protocol(), address, Options.empty());
            this.socket = require(socket, "Socket channel");
            this.conduit = new SocketConduit(socket, dispatcher);
            this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, null);
        }

        /**
         * Returns destination.
         *
         * @return destination
         */
        @Override
        public Destination destination() {
            return destination;
        }

        /**
         * Returns conduit.
         *
         * @return conduit
         */
        @Override
        public Conduit conduit() {
            return conduit;
        }

        /**
         * Returns state.
         *
         * @return state
         */
        @Override
        public Status state() {
            return state;
        }

        /**
         * Reads bytes.
         *
         * @param buffer target buffer
         * @return read future
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer buffer) {
            return conduit.read(buffer);
        }

        /**
         * Writes bytes.
         *
         * @param buffer source buffer
         * @return write future
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer buffer) {
            return conduit.write(buffer);
        }

        /**
         * Returns whether healthy.
         *
         * @return healthy flag
         */
        @Override
        public boolean healthy() {
            return state == Status.OPENED && socket.isConnected() && socket.isOpen();
        }

        /**
         * Returns whether idle.
         *
         * @return idle flag
         */
        @Override
        public boolean idle() {
            return healthy();
        }

        /**
         * Closes the socket.
         */
        @Override
        public void close() {
            if (state != Status.CLOSED) {
                state = Status.CLOSED;
                conduit.close();
                listener.close(this);
            }
        }

    }

    /**
     * Socket conduit adapter.
     */
    private static final class SocketConduit implements Conduit {

        /**
         * Socket channel.
         */
        private final SocketChannel socket;

        /**
         * Runtime dispatcher.
         */
        private final Dispatcher dispatcher;

        /**
         * Creates an adapter.
         *
         * @param socket     socket
         * @param dispatcher runtime dispatcher
         */
        private SocketConduit(final SocketChannel socket, final Dispatcher dispatcher) {
            this.socket = require(socket, "Socket channel");
            this.dispatcher = require(dispatcher, "Dispatcher");
        }

        /**
         * Reads bytes.
         *
         * @param target target buffer
         * @return read future
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer target) {
            require(target, "Read target");
            return dispatcher.supply("http:socket:read", () -> {
                try {
                    return socket.read(target);
                } catch (final IOException e) {
                    throw new SocketException("Socket read failed", e);
                }
            });
        }

        /**
         * Reads bytes with a handler.
         *
         * @param target  target buffer
         * @param handler completion handler
         */
        @Override
        public void read(final ByteBuffer target, final CompletionHandler<Integer, ByteBuffer> handler) {
            complete(read(target), target, handler);
        }

        /**
         * Writes bytes.
         *
         * @param source source buffer
         * @return write future
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer source) {
            require(source, "Write source");
            return dispatcher.supply("http:socket:write", () -> {
                try {
                    return socket.write(source);
                } catch (final IOException e) {
                    throw new SocketException("Socket write failed", e);
                }
            });
        }

        /**
         * Writes bytes with a handler.
         *
         * @param source  source buffer
         * @param handler completion handler
         */
        @Override
        public void write(final ByteBuffer source, final CompletionHandler<Integer, ByteBuffer> handler) {
            complete(write(source), source, handler);
        }

        /**
         * Returns open state.
         *
         * @return open flag
         */
        @Override
        public boolean opened() {
            return socket.isOpen();
        }

        /**
         * Closes socket channel.
         */
        @Override
        public void close() {
            try {
                socket.close();
            } catch (final IOException e) {
                throw new SocketException("Socket close failed", e);
            }
        }

        /**
         * Bridges a future to a completion handler.
         *
         * @param future     future
         * @param attachment attachment
         * @param handler    handler
         */
        private static void complete(
                final CompletableFuture<Integer> future,
                final ByteBuffer attachment,
                final CompletionHandler<Integer, ByteBuffer> handler) {
            require(handler, "Completion handler");
            future.whenComplete((value, failure) -> {
                if (failure == null) {
                    handler.completed(value, attachment);
                } else {
                    handler.failed(failure, attachment);
                }
            });
        }

    }

}
