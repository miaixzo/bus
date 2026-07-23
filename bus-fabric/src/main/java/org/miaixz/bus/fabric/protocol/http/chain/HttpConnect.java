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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
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
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
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
import org.miaixz.bus.fabric.network.tls.TlsSocketChannel;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
import org.miaixz.bus.fabric.registry.route.Route;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP chain stage that leases or opens the route connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpConnect implements HttpStage, AutoCloseable {

    /** Debug state captured once; Logger level discovery performs caller inspection and is not a hot-path probe. */
    private static final boolean DEBUG_ENABLED = Logger.isDebugEnabled();

    /** Shared unregister action for synchronous exchanges using {@link Cancellation#none()}. */
    private static final Runnable NOOP_UNREGISTER = () -> {
    };

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

    /** Canonical direct-route destinations, avoiding repeated immutable option-key construction on pooled requests. */
    private final ConcurrentHashMap<Address, Destination> directDestinations;

    /** Canonical TCP addresses beneath direct HTTPS routes. */
    private final ConcurrentHashMap<Address, Address> directConnectAddresses;

    /**
     * Whether this stage owns and must close the pool.
     */
    private boolean ownsPool;

    /**
     * Whether this stage owns and must close the connector.
     */
    private boolean ownsConnector;

    /**
     * Creates a connect stage with a default socket connector.
     */
    public HttpConnect() {
        this(ConnectionPool.create(null), true);
    }

    /**
     * Creates the fully owned compatibility stage.
     *
     * @param pool  connection pool used by the stage
     * @param owned whether closing this stage also closes the pool
     */
    private HttpConnect(final ConnectionPool pool, final boolean owned) {
        this(pool, TlsContext.defaults(), TlsSettings.defaults(), null, DnsResolver.system(), pool.runtimeDispatcher());
        this.ownsPool = owned;
    }

    /**
     * Creates a connect stage with a shared connection pool.
     *
     * @param pool connection pool
     */
    public HttpConnect(final ConnectionPool pool) {
        this(pool, TlsContext.defaults(), TlsSettings.defaults(), null, DnsResolver.system(),
                require(pool, "Connection pool").runtimeDispatcher());
    }

    /**
     * Creates a connect stage with shared connection pool and TLS dependencies.
     *
     * @param pool        connection pool
     * @param tlsContext  TLS context
     * @param tlsSettings TLS settings
     */
    public HttpConnect(final ConnectionPool pool, final TlsContext tlsContext, final TlsSettings tlsSettings) {
        this(pool, tlsContext, tlsSettings, null, DnsResolver.system(),
                require(pool, "Connection pool").runtimeDispatcher());
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
        this(pool, tlsContext, tlsSettings, listener, DnsResolver.system(),
                require(pool, "Connection pool").runtimeDispatcher());
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
        this(pool, tlsContext, tlsSettings, listener, resolver, require(pool, "Connection pool").runtimeDispatcher());
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
        this.ownsConnector = true;
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
        this(pool, connector, tlsContext, tlsSettings, null, DnsResolver.system(),
                require(pool, "Connection pool").runtimeDispatcher());
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
        this(pool, connector, tlsContext, tlsSettings, listener, DnsResolver.system(),
                require(pool, "Connection pool").runtimeDispatcher());
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
        this(pool, connector, tlsContext, tlsSettings, listener, resolver,
                require(pool, "Connection pool").runtimeDispatcher());
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
        this.listener = safe(listener);
        this.resolver = require(resolver, "DNS resolver");
        this.dispatcher = require(dispatcher, "Dispatcher");
        this.directDestinations = new ConcurrentHashMap<>();
        this.directConnectAddresses = new ConcurrentHashMap<>();
    }

    /**
     * Acquires a connection, proceeds, and attaches release behavior to the response body.
     *
     * @param request request whose route requires a pooled connection
     * @param chain   remaining HTTP stages to invoke after acquisition
     * @return response
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        final Cancellation cancellation = next.cancellation();
        cancellation.throwIfCancelled();
        final boolean debug = DEBUG_ENABLED;
        if (debug) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP connect stage started: method={}, host={}, port={}, secure={}",
                    current.method().value(),
                    current.url().host(),
                    current.url().port(),
                    current.url().address().secure());
        }
        final ConnectionLease lease = acquire(current, cancellation);
        try {
            cancellation.throwIfCancelled();
            final HttpResponse response = next.withConnection(lease, lease.connection()).proceed(current);
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP connect stage response received: host={}, port={}, code={}",
                        current.url().host(),
                        current.url().port(),
                        response.code());
            }
            return track(lease, response);
        } catch (final RuntimeException e) {
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP connect stage failed: host={}, port={}, exception={}",
                        current.url().host(),
                        current.url().port(),
                        e.getClass().getSimpleName());
            }
            closeLease(lease, "Unable to close connection after HTTP chain failure");
            throw e;
        }
    }

    /**
     * Acquires a route lease from the connection pool.
     *
     * @param request request supplying the route and timeout policy
     * @return connection lease
     */
    public ConnectionLease acquire(final HttpRequest request) {
        return acquire(request, Cancellation.create());
    }

    /**
     * Acquires a route lease from the connection pool with a cancellation scope.
     *
     * @param request      request supplying the route and timeout policy
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
        final boolean debug = DEBUG_ENABLED;
        if (debug) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP connection lease acquisition started: host={}, port={}, secure={}, proxyMode={}, tunnel={}",
                    target.host(),
                    target.port(),
                    target.secure(),
                    proxyMode(proxy),
                    proxy.requiresTunnel(target));
        }
        final boolean transientConnection = "close".equalsIgnoreCase(current.headers().get(Http.Header.CONNECTION));
        final Supplier<Connection> factory = () -> open(
                destination,
                target,
                proxy,
                current.timeout(),
                scope,
                transientConnection);
        final ConnectionLease lease = transientConnection ? pool.acquireTransient(destination, factory, scope)
                : pool.acquire(destination, factory, scope);
        try {
            scope.throwIfCancelled();
        } catch (final RuntimeException e) {
            closeLease(lease, "Unable to close cancelled connection lease");
            throw e;
        }
        if (debug) {
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP connection lease acquired: host={}, port={}, healthy={}, proxyMode={}",
                    target.host(),
                    target.port(),
                    lease.connection().healthy(),
                    proxyMode(proxy));
        }
        return lease;
    }

    /**
     * Releases or closes a lease based on response consumption state.
     *
     * @param lease    connection lease associated with the response
     * @param response response whose body state determines reuse eligibility
     */
    public void release(final ConnectionLease lease, final HttpResponse response) {
        final ConnectionLease current = require(lease, "Connection lease");
        final HttpResponse target = require(response, "HTTP response");
        final Payload payload = target.body().payload();
        final ReleaseState state = payload instanceof LeasePayload tracked ? tracked.state() : null;
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
     * Closes only resources created by this stage. Borrowed pools/connectors remain caller-owned.
     */
    @Override
    public void close() {
        directDestinations.clear();
        RuntimeException failure = null;
        if (ownsConnector) {
            try {
                connector.close();
            } catch (final RuntimeException e) {
                failure = e;
            }
        }
        if (ownsPool) {
            try {
                pool.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
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
     * @param destination  connection destination
     * @param target       target address
     * @param proxy        proxy plan
     * @param timeout      maximum duration allowed for connection establishment
     * @param cancellation cancellation scope governing route establishment
     * @return network connection
     */
    private Connection open(
            final Destination destination,
            final Address target,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation,
            final boolean transientConnection) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final boolean tunnel = proxy.requiresTunnel(target);
        final Address connectAddress = connectAddress(target, proxy, connector.supports(Transport.TLS));
        final boolean debug = DEBUG_ENABLED;
        if (debug) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP route open started: targetHost={}, targetPort={}, connectHost={}, connectPort={}, "
                            + "proxyMode={}, tunnel={}, nativeTls={}",
                    target.host(),
                    target.port(),
                    connectAddress.host(),
                    connectAddress.port(),
                    proxyMode(proxy),
                    tunnel,
                    connector.supports(Transport.TLS));
        }
        final Connection raw = connector instanceof SocketConnector socket ? socket
                .open(connectAddress, timeout, transientConnection && target.secure() && !tunnel && !proxy.isSocks())
                : awaitConnection(connector.connect(connectAddress, timeout), timeout, scope);
        final Runnable unregisterRaw = scope.cancellable() ? scope.onCancel(raw::close) : NOOP_UNREGISTER;
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
                final Connection secured = tlsConnection(destination, raw, target, timeout, scope, transientConnection);
                if (debug) {
                    Logger.debug(
                            false,
                            "Fabric",
                            "HTTP route open completed with TLS wrapper: host={}, port={}",
                            target.host(),
                            target.port());
                }
                return secured;
            }
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP route open completed: host={}, port={}, secure={}",
                        target.host(),
                        target.port(),
                        target.secure());
            }
            return new RoutedConnection(destination, raw);
        } catch (final RuntimeException e) {
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP route open failed: host={}, port={}, exception={}",
                        target.host(),
                        target.port(),
                        e.getClass().getSimpleName());
            }
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
     * @param timeout      request timeout policy
     * @param cancellation cancellation scope
     * @return TLS connection
     */
    private Connection tlsConnection(
            final Destination destination,
            final Connection raw,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation,
            final boolean socketFastPath) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        if (socketFastPath && raw instanceof SocketConnection socket) {
            return tlsSocketConnection(destination, socket, target, timeout, scope);
        }
        final TlsEngine engine = TlsEngine
                .create(require(tlsContext, "TLS context"), target, require(tlsSettings, "TLS settings"));
        final Timeout currentTimeout = require(timeout, "Timeout");
        final TlsChannel tlsChannel = TlsChannel.wrap(raw.conduit(), engine, listener, dispatcher, currentTimeout);
        final Runnable unregisterTls = scope.cancellable() ? scope.onCancel(tlsChannel::close) : NOOP_UNREGISTER;
        try {
            final boolean debug = DEBUG_ENABLED;
            if (debug) {
                Logger.debug(
                        true,
                        "Fabric",
                        "HTTP TLS handshake started: host={}, port={}",
                        target.host(),
                        target.port());
            }
            final TlsHandshake handshake = await(
                    tlsChannel.handshake(),
                    currentTimeout.connect(),
                    "TLS handshake timed out",
                    scope);
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP TLS handshake completed: host={}, port={}",
                        target.host(),
                        target.port());
            }
            return new TlsRoutedConnection(destination, raw, tlsChannel, handshake,
                    negotiatedProtocol(engine.applicationProtocol()));
        } finally {
            unregisterTls.run();
        }
    }

    /** Uses the JDK's optimized blocking TLS socket for the built-in blocking HTTP transport. */
    private Connection tlsSocketConnection(
            final Destination destination,
            final SocketConnection raw,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        final Timeout currentTimeout = require(timeout, "Timeout");
        final TlsSocketChannel tls = TlsSocketChannel.wrap(
                require(tlsContext, "TLS context"),
                raw.socket(),
                target,
                require(tlsSettings, "TLS settings"),
                currentTimeout);
        final Runnable unregisterTls = cancellation.cancellable() ? cancellation.onCancel(tls::close) : NOOP_UNREGISTER;
        try {
            cancellation.throwIfCancelled();
            // Complete only the physical handshake here. Certificate-bearing public metadata is materialized
            // when the response exposes it, keeping route establishment on the shortest JSSE path.
            tls.handshakeSessionSynchronously();
            cancellation.throwIfCancelled();
            return new TlsSocketRoutedConnection(destination, raw, tls, negotiatedProtocol(tls.applicationProtocol()));
        } finally {
            unregisterTls.run();
        }
    }

    /**
     * Maps the wire-negotiated ALPN value and rejects every unknown non-empty protocol.
     *
     * @param applicationProtocol ALPN protocol selected by the TLS engine
     * @return established HTTP wire protocol
     */
    private static Protocol negotiatedProtocol(final String applicationProtocol) {
        if (applicationProtocol == null || applicationProtocol.isBlank()
                || Protocol.HTTP_1_1.name.equalsIgnoreCase(applicationProtocol)) {
            return Protocol.HTTP_1_1;
        }
        if ("h2".equalsIgnoreCase(applicationProtocol) || Protocol.HTTP_2.name.equalsIgnoreCase(applicationProtocol)) {
            return Protocol.HTTP_2;
        }
        throw new ProtocolException("Unsupported negotiated application protocol: " + applicationProtocol);
    }

    /**
     * Performs an HTTP CONNECT tunnel handshake through a proxy.
     *
     * @param connection   proxy connection
     * @param target       target address
     * @param proxy        proxy plan
     * @param timeout      maximum duration allowed for the proxy handshake
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
                "Fabric",
                "HTTP CONNECT tunnel started: targetHost={}, targetPort={}",
                target.host(),
                target.port());
        final String request = connectRequest(target, proxy.authorization());
        writeAll(
                connection.sink(),
                new Buffer().write(
                        ByteString.encodeString(request, org.miaixz.bus.core.lang.Charset.US_ASCII).toByteArray()),
                timeout.write(),
                scope);
        final String response = readHeader(connection.source(), timeout.read(), scope);
        if (!response.startsWith(Protocol.HTTP_1_1 + " 200 ") && !response.startsWith(Protocol.HTTP_1_0 + " 200 ")
                && !response.startsWith("HTTP/2 200 ")) {
            throw new ProtocolException("HTTP CONNECT tunnel failed");
        }
        Logger.debug(
                false,
                "Fabric",
                "HTTP CONNECT tunnel completed: targetHost={}, targetPort={}",
                target.host(),
                target.port());
    }

    /**
     * Performs a Builder.HTTP_CONNECT_SOCKS5 CONNECT handshake through a proxy.
     *
     * @param connection   proxy connection
     * @param target       target address
     * @param timeout      maximum duration allowed for the SOCKS5 handshake
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
                "Fabric",
                "SOCKS handshake started: targetHost={}, targetPort={}",
                target.host(),
                target.port());
        writeAll(
                connection.sink(),
                new Buffer().write(new byte[] { Builder.HTTP_CONNECT_SOCKS5, 0x01, Normal._0 }),
                timeout.write(),
                scope);
        final byte[] selection = readExact(
                connection.source(),
                2,
                timeout.read(),
                "SOCKS method selection timed out",
                scope);
        if (selection[0] != Builder.HTTP_CONNECT_SOCKS5 || selection[1] != Normal._0) {
            throw new ProtocolException("SOCKS proxy requires an unsupported authentication method");
        }
        writeAll(connection.sink(), new Buffer().write(socksConnectRequest(target)), timeout.write(), scope);
        final byte[] header = readExact(
                connection.source(),
                4,
                timeout.read(),
                "SOCKS connect response timed out",
                scope);
        if (header[0] != Builder.HTTP_CONNECT_SOCKS5) {
            throw new ProtocolException("Invalid SOCKS response version");
        }
        if (header[1] != 0x00) {
            throw new ProtocolException("SOCKS CONNECT failed with reply " + (header[1] & Builder.UNSIGNED_BYTE_MASK));
        }
        final int addressLength = switch (header[3]) {
            case Normal._1 -> 4;
            case Normal._3 -> readExact(
                    connection.source(),
                    1,
                    timeout.read(),
                    "SOCKS domain length timed out",
                    scope)[0] & Builder.UNSIGNED_BYTE_MASK;
            case Normal._4 -> 16;
            default -> throw new ProtocolException("Unsupported SOCKS address type");
        };
        readExact(connection.source(), addressLength + 2, timeout.read(), "SOCKS bind address timed out", scope);
        Logger.debug(
                false,
                "Fabric",
                "SOCKS handshake completed: targetHost={}, targetPort={}, addressType={}",
                target.host(),
                target.port(),
                header[3] & Builder.UNSIGNED_BYTE_MASK);
    }

    /**
     * Tracks a response so body completion releases the lease.
     *
     * @param lease    connection lease to release after body consumption
     * @param response response whose payload is wrapped with release tracking
     * @return tracked response
     */
    private HttpResponse track(final ConnectionLease lease, final HttpResponse response) {
        final HttpResponse source = require(response, "HTTP response");
        final boolean reusable = reusable(source);
        final TlsHandshake handshake = source.handshake() == null ? connectionHandshake(lease.connection()) : null;
        if (source.body().length() == Normal._0) {
            if (reusable && lease.connection().healthy()) {
                lease.release();
            } else {
                lease.close();
            }
            return source.withBody(source.body(), handshake);
        }
        final ReleaseState state = new ReleaseState(lease, reusable);
        final PayloadBody body = source.body().withTransportPayload(new LeasePayload(source.body().payload(), state));
        final HttpResponse tracked = source.withBody(body, handshake);
        if (DEBUG_ENABLED) {
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP response lease tracking installed: code={}, repeatable={}, healthy={}",
                    source.code(),
                    source.body().payload().repeatable(),
                    lease.connection().healthy());
        }
        return tracked;
    }

    /** Returns transport handshake metadata without exposing the concrete TLS conduit. */
    private static TlsHandshake connectionHandshake(final Connection connection) {
        if (connection instanceof TlsRoutedConnection tls)
            return tls.handshake();
        if (connection instanceof TlsSocketRoutedConnection tls)
            return tls.handshake();
        return null;
    }

    /**
     * Returns whether request and response connection semantics permit returning the physical connection to the pool.
     *
     * @param response completed response
     * @return true when neither side requested connection closure
     */
    private static boolean reusable(final HttpResponse response) {
        return !closeRequested(response.request().headers()) && !closeRequested(response.headers());
    }

    /**
     * Detects a Connection: close token in a header collection.
     *
     * @param headers headers to inspect
     * @return true when connection closure was requested
     */
    private static boolean closeRequested(final Headers headers) {
        for (int index = 0; index < headers.size(); index++) {
            if (Http.Header.CONNECTION.equalsIgnoreCase(headers.name(index))
                    && Http.Header.CONNECTION_CLOSE.equalsIgnoreCase(headers.value(index))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Releases an untracked response using conservative body state.
     *
     * @param lease    connection lease associated with the response
     * @param response untracked response used to determine safe reuse
     */
    private void releaseUntracked(final ConnectionLease lease, final HttpResponse response) {
        try {
            if (response.body().payload().repeatable() && lease.connection().healthy()) {
                if (DEBUG_ENABLED) {
                    Logger.debug(
                            false,
                            "Fabric",
                            "HTTP untracked lease released: code={}, repeatable={}, healthy={}",
                            response.code(),
                            true,
                            true);
                }
                lease.release();
            } else {
                if (DEBUG_ENABLED) {
                    Logger.debug(
                            false,
                            "Fabric",
                            "HTTP untracked lease closed: code={}, repeatable={}, healthy={}",
                            response.code(),
                            response.body().payload().repeatable(),
                            lease.connection().healthy());
                }
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
    private Destination destination(final Address target, final ProxyPlan proxy) {
        if (proxy.proxy().isEmpty() && !proxy.requiresTunnel(target)) {
            return directDestinations.computeIfAbsent(target, this::directDestination);
        }
        return buildDestination(target, proxy);
    }

    /** Builds and caches the overwhelmingly common direct-route identity. */
    private Destination directDestination(final Address target) {
        return buildDestination(target, ProxyPlan.direct());
    }

    /** Builds the immutable connection-pool key for one resolved route. */
    private Destination buildDestination(final Address target, final ProxyPlan proxy) {
        final Protocol requestProtocol = protocol(target);
        Options options = Options.of(Builder.OPTION_TLS, target.secure()).with(Builder.OPTION_SECURE, target.secure())
                .with(Builder.OPTION_MULTIPLEX, requestProtocol == Protocol.HTTP_2)
                .with(Builder.OPTION_PROTOCOL, requestProtocol.name)
                .with(
                        Builder.OPTION_ROUTE_PROXY,
                        proxy.proxy().map(Address::toUri).map(Object::toString).orElse(Builder.PROXY_PLAN_DIRECT_ID))
                .with(Builder.OPTION_ROUTE_TUNNEL, proxy.requiresTunnel(target));
        if (tlsContext != null) {
            options = options.with(Builder.OPTION_TLS_CONTEXT, tlsContext);
        }
        if (tlsSettings != null) {
            options = options.with(Builder.OPTION_TLS_SETTINGS, tlsSettings);
        }
        return Destination.of(requestProtocol, target, options);
    }

    /**
     * Selects the address to connect before optional tunnel and TLS wrapping.
     *
     * @param target    target address
     * @param proxy     proxy plan
     * @param nativeTls whether connector supports TLS directly
     * @return connect address
     */
    private Address connectAddress(final Address target, final ProxyPlan proxy, final boolean nativeTls) {
        return proxy.proxy().orElseGet(
                () -> target.secure() && !nativeTls
                        ? directConnectAddresses.computeIfAbsent(target, HttpConnect::tcpAddress)
                        : target);
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
     * @param address destination address whose scheme selects the pool protocol
     * @return protocol
     */
    private static Protocol protocol(final Address address) {
        return address.secure() ? Protocol.HTTPS : Protocol.HTTP;
    }

    /**
     * Reads proxy plan from the request, keeping tag-based routes as a secondary fallback.
     *
     * @param request request containing explicit or tag-based proxy configuration
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
            return Builder.PROXY_PLAN_DIRECT_ID;
        }
        if (proxy.isSocks()) {
            return "socks";
        }
        if (proxy.isHttp()) {
            return Protocol.HTTP.name;
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
            if (proxy.isHttp() && !Protocol.HTTP.name.equals(address.scheme())) {
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
        builder.append(Http.Method.CONNECT.value()).append(Symbol.C_SPACE).append(authority).append(Symbol.C_SPACE)
                .append(Protocol.HTTP_1_1).append(Symbol.CRLF);
        builder.append(Http.Header.HOST).append(Symbol.COLON).append(Symbol.SPACE).append(authority)
                .append(Symbol.CRLF);
        builder.append(Http.Header.PROXY_CONNECTION).append(Symbol.COLON).append(Symbol.SPACE)
                .append(Http.Header.CONNECTION_KEEP_ALIVE).append(Symbol.CRLF);
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
     * Creates a Builder.HTTP_CONNECT_SOCKS5 CONNECT request.
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
        buffer.put(Builder.HTTP_CONNECT_SOCKS5).put((byte) Normal._1).put((byte) Normal._0);
        if (ipv4 == null) {
            buffer.put((byte) Normal._3).put((byte) host.length);
        } else {
            buffer.put((byte) Normal._1);
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
     * @param address address whose host and port form the authority
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
     * @param sink         destination connection sink
     * @param source       buffer whose remaining bytes are written
     * @param timeout      maximum duration allowed for the write
     * @param cancellation cancellation scope
     */
    private static void writeAll(
            final Sink sink,
            final Buffer source,
            final Duration timeout,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final Sink current = require(sink, "Sink");
        final Buffer payload = require(source, "Write buffer");
        configureTimeout(current.timeout(), timeout);
        scope.throwIfCancelled();
        try {
            current.write(payload, payload.size());
        } catch (final IOException e) {
            throw new SocketException("HTTP CONNECT write failed", e);
        }
    }

    /**
     * Reads a proxy response header.
     *
     * @param source       proxy connection source
     * @param timeout      maximum duration allowed while reading headers
     * @param cancellation cancellation scope
     * @return response header
     */
    private static String readHeader(final Source source, final Duration timeout, final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final Source current = require(source, "Source");
        configureTimeout(current.timeout(), timeout);
        final StringBuilder header = new StringBuilder();
        while (header.length() < Builder.BYTES_64_KIB) {
            scope.throwIfCancelled();
            final Buffer buffer = new Buffer();
            final long read;
            try {
                read = current.read(buffer, Normal._1);
            } catch (final IOException e) {
                throw new SocketException("HTTP CONNECT read failed", e);
            }
            if (read < 0) {
                throw new SocketException("HTTP CONNECT response reached EOF");
            }
            if (read == 0) {
                if (!ThreadKit.sleep(Normal._1)) {
                    throw new SocketException("HTTP CONNECT read was interrupted");
                }
                continue;
            }
            while (buffer.size() > Normal._0) {
                header.append((char) (buffer.readByte() & Builder.UNSIGNED_BYTE_MASK));
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
     * @param source       proxy connection source
     * @param length       exact number of bytes required
     * @param timeout      maximum duration allowed for each read
     * @param message      timeout message
     * @param cancellation cancellation scope
     * @return bytes
     */
    private static byte[] readExact(
            final Source source,
            final int length,
            final Duration timeout,
            final String message,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final Source current = require(source, "Source");
        configureTimeout(current.timeout(), timeout);
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            scope.throwIfCancelled();
            final long read;
            try {
                read = current.read(buffer, length - buffer.size());
            } catch (final IOException e) {
                throw new SocketException(message, e);
            }
            if (read < 0) {
                throw new SocketException("SOCKS response reached EOF");
            }
            if (read == 0) {
                if (!ThreadKit.sleep(Normal._1)) {
                    throw new SocketException("SOCKS read was interrupted");
                }
            }
        }
        try {
            return buffer.readByteArray(length);
        } catch (final IOException e) {
            throw new SocketException("Unable to materialize SOCKS response", e);
        }
    }

    /**
     * Applies a fabric duration to a core.io timeout policy.
     *
     * @param ioTimeout core.io timeout
     * @param timeout   fabric duration
     */
    private static void configureTimeout(
            final org.miaixz.bus.core.io.timout.Timeout ioTimeout,
            final Duration timeout) {
        if (ioTimeout == null || timeout == null || timeout.isZero() || timeout.isNegative()) {
            return;
        }
        ioTimeout.timeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Waits for a connection future.
     *
     * @param future  asynchronous connection result to await
     * @param timeout maximum duration allowed for connection establishment
     * @return connection
     */
    private static Connection awaitConnection(final CompletableFuture<Connection> future, final Timeout timeout) {
        return awaitConnection(future, timeout, Cancellation.create());
    }

    /**
     * Waits for a connection future with cancellation support.
     *
     * @param future       asynchronous connection result to await
     * @param timeout      maximum duration allowed for connection establishment
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
     * @param future asynchronous TLS handshake result to await
     */
    private static void awaitTls(final CompletableFuture<?> future) {
        awaitTls(future, Cancellation.create());
    }

    /**
     * Waits for TLS handshake completion with cancellation support.
     *
     * @param future       asynchronous TLS handshake result to await
     * @param cancellation cancellation scope
     */
    private static void awaitTls(final CompletableFuture<?> future, final Cancellation cancellation) {
        await(future, Duration.ZERO, "TLS handshake timed out", cancellation);
    }

    /**
     * Waits for a future with bus exceptions.
     *
     * @param future  asynchronous result to await
     * @param timeout maximum duration allowed before failing
     * @param message timeout message
     * @param <T>     result type
     * @return the completed asynchronous result
     */
    private static <T> T await(final CompletableFuture<T> future, final Duration timeout, final String message) {
        return await(future, timeout, message, Cancellation.create());
    }

    /**
     * Waits for a future with bus exceptions and cancellation support.
     *
     * @param future       asynchronous result to await
     * @param timeout      maximum duration allowed before failing
     * @param message      timeout message
     * @param cancellation cancellation scope
     * @param <T>          result type
     * @return the completed asynchronous result
     */
    private static <T> T await(
            final CompletableFuture<T> future,
            final Duration timeout,
            final String message,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final Runnable unregister = scope.cancellable() ? scope.onCancel(() -> future.cancel(true)) : NOOP_UNREGISTER;
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
     * @param lease   failed lease to close
     * @param message diagnostic message used if closing fails
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
     * @param connection failed connection to close
     * @param message    diagnostic message used if closing fails
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
     * @param value raw stage name, or {@code null}
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
     * @param message failure message for a newly created internal exception
     * @param failure runtime failure to preserve or wrap
     * @return internal exception
     */
    private static InternalException internal(final String message, final RuntimeException failure) {
        return failure instanceof InternalException internal ? internal : new InternalException(message, failure);
    }

    /**
     * Protects listener callbacks from escaping.
     *
     * @param listener listener to protect, or {@code null} for a no-op listener
     * @return safe listener
     */
    private static Listener<Object> safe(final Listener<Object> listener) {
        return listener == null ? NoopListener.INSTANCE : new SafeListener(listener);
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name
     * @param <T>   value type
     * @return the validated reference
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Release state shared by tracked response payloads.
     */
    private static final class ReleaseState {

        /** Completion state updater. */
        private static final VarHandle STATE;

        /** Body fully consumed bit. */
        private static final int COMPLETE = 1;

        /** Body failed bit. */
        private static final int BROKEN = 1 << 1;

        /** Lease already released bit. */
        private static final int RELEASED = 1 << 2;

        static {
            try {
                STATE = MethodHandles.lookup().findVarHandle(ReleaseState.class, "state", int.class);
            } catch (final ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        /**
         * Lease to release.
         */
        private final ConnectionLease lease;

        /**
         * Whether protocol framing permits connection reuse after complete body consumption.
         */
        private final boolean reusable;

        /**
         * Body completion flag.
         */
        private volatile int state;

        /**
         * Creates a release state.
         *
         * @param lease connection lease owned by this release state
         */
        private ReleaseState(final ConnectionLease lease, final boolean reusable) {
            this.lease = require(lease, "Connection lease");
            this.reusable = reusable;
        }

        /**
         * Marks the body fully consumed.
         */
        private void complete() {
            STATE.getAndBitwiseOr(this, COMPLETE);
        }

        /**
         * Marks the body as broken.
         */
        private void broken() {
            STATE.getAndBitwiseOr(this, BROKEN);
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
            int observed;
            do {
                observed = state;
                if ((observed & RELEASED) != 0) {
                    return;
                }
            } while (!STATE.compareAndSet(this, observed, observed | RELEASED));
            final boolean complete = (observed & COMPLETE) != 0;
            final boolean broken = (observed & BROKEN) != 0;
            try {
                if (complete && !broken && reusable && lease.connection().healthy()) {
                    if (DEBUG_ENABLED) {
                        Logger.debug(
                                false,
                                "Fabric",
                                "HTTP tracked lease released: complete={}, broken={}, healthy={}",
                                complete,
                                broken,
                                lease.connection().healthy());
                    }
                    lease.release();
                } else {
                    if (DEBUG_ENABLED) {
                        Logger.debug(
                                false,
                                "Fabric",
                                "HTTP tracked lease closed: complete={}, broken={}, healthy={}",
                                complete,
                                broken,
                                lease.connection().healthy());
                    }
                    lease.close();
                }
            } catch (final RuntimeException e) {
                throw internal("Unable to release HTTP connection", e);
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
         * @param delegate payload whose lifecycle is tracked
         * @param state    release state
         */
        private LeasePayload(final Payload delegate, final ReleaseState state) {
            this.delegate = require(delegate, "Payload");
            this.state = require(state, "Release state");
        }

        /**
         * Returns the response-local release owner for explicit release validation.
         *
         * @return release state shared by all response body views
         */
        private ReleaseState state() {
            return state;
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

        /**
         * Opens a lease-aware source for the delegated payload.
         *
         * @return lease-aware source
         */
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
            return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
                final byte[] data = delegate.bytes(maxBytes);
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
         * @param charset charset used to decode the payload bytes
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads payload text with an explicit materialize threshold and releases the lease.
         *
         * @param charset  charset used to decode the payload bytes
         * @param maxBytes maximum bytes to materialize
         * @return text
         */
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
         * @param delegate stream source whose lifecycle is tracked
         * @param state    release state
         */
        private LeaseSource(final Source delegate, final ReleaseState state) {
            this.delegate = require(delegate, "Source");
            this.state = require(state, "Release state");
            this.closed = new AtomicBoolean();
        }

        /**
         * Reads from the delegated source and releases the connection at end of stream.
         *
         * @param sink      destination buffer
         * @param byteCount maximum bytes to read
         * @return bytes read, or -1 at end of stream
         * @throws IOException when the delegate read fails
         */
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

        /**
         * Returns the delegate timeout.
         *
         * @return timeout
         */
        @Override
        public org.miaixz.bus.core.io.timout.Timeout timeout() {
            return delegate.timeout();
        }

        /**
         * Closes the delegated source and releases or marks the connection as broken.
         *
         * @throws IOException when the delegate close fails
         */
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
         * Atomic updater for the lazily published protocol attachment.
         */
        private static final VarHandle PROTOCOL_ATTACHMENT;

        static {
            try {
                PROTOCOL_ATTACHMENT = MethodHandles.lookup()
                        .findVarHandle(RoutedConnection.class, "protocolAttachment", Object.class);
            } catch (final ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        /**
         * Route destination.
         */
        private final Destination destination;

        /**
         * Delegate connection.
         */
        private final Connection delegate;

        /**
         * Actual wire protocol.
         */
        private final Protocol protocol;

        /**
         * Connection-local multiplex session owner, present only for HTTP/2.
         */
        private final Connection.MultiplexAttachment attachment;

        /** Sequential protocol session retained for exclusive HTTP/1.1 leases. */
        private volatile Object protocolAttachment;

        /**
         * Creates a routed connection.
         *
         * @param destination route destination
         * @param delegate    physical connection reached through the route
         */
        RoutedConnection(final Destination destination, final Connection delegate) {
            this(destination, delegate,
                    destination.protocol() == Protocol.H2_PRIOR_KNOWLEDGE ? Protocol.HTTP_2 : delegate.protocol());
        }

        /**
         * Creates a routed connection with an authoritative wire protocol.
         *
         * @param destination route destination represented by the connection
         * @param delegate    physical connection reached through the route
         * @param protocol    negotiated HTTP wire protocol
         */
        RoutedConnection(final Destination destination, final Connection delegate, final Protocol protocol) {
            this.destination = require(destination, "Connection destination");
            this.delegate = require(delegate, "Network connection");
            this.protocol = require(protocol, "Established protocol");
            this.attachment = protocol == Protocol.HTTP_2 ? new RoutedMultiplexAttachment() : null;
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
         * Returns the protocol-layer source.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return delegate.source();
        }

        /**
         * Returns the protocol-layer sink.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return delegate.sink();
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
         * Returns the protocol negotiated or selected for this route.
         *
         * @return route protocol
         */
        @Override
        public Protocol protocol() {
            return protocol;
        }

        /**
         * Reports whether this route exposes multiplexed logical streams.
         *
         * @return {@code true} when a multiplex attachment is present
         */
        @Override
        public boolean multiplex() {
            return attachment != null;
        }

        /**
         * Returns currently available logical stream capacity.
         *
         * @return available stream count, or one for a non-multiplex route
         */
        @Override
        public int capacity() {
            return attachment == null ? Normal._1 : attachment.capacity();
        }

        /**
         * Reports whether the multiplex route is refusing new streams while existing work drains.
         *
         * @return draining flag
         */
        @Override
        public boolean draining() {
            return attachment != null && attachment.draining();
        }

        /**
         * Returns the connection-local multiplex publication attachment.
         *
         * @return attachment, or {@code null} for a non-multiplex route
         */
        @Override
        public Connection.MultiplexAttachment multiplexAttachment() {
            return attachment;
        }

        /**
         * Returns the connection-local sequential protocol session.
         *
         * @return attached session, or null before first use
         */
        @Override
        public Object protocolAttachment() {
            return protocolAttachment;
        }

        /**
         * Installs the connection-local sequential protocol session atomically.
         *
         * @param expected expected session
         * @param update   replacement session
         * @return true when installed
         */
        @Override
        public boolean compareAndSetProtocolAttachment(final Object expected, final Object update) {
            return PROTOCOL_ATTACHMENT.compareAndSet(this, expected, require(update, "Protocol attachment"));
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
         * Ensures the TLS and raw connection boundary is released once.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a TLS routed connection.
         *
         * @param destination route destination
         * @param raw         raw connection
         * @param tls         TLS channel
         * @param handshake   TLS handshake metadata
         * @param protocol    protocol negotiated by ALPN
         */
        private TlsRoutedConnection(final Destination destination, final Connection raw, final TlsChannel tls,
                final TlsHandshake handshake, final Protocol protocol) {
            super(destination, raw, protocol);
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
         * Returns the TLS plaintext conduit.
         *
         * @return TLS conduit
         */
        @Override
        public Conduit conduit() {
            return tls;
        }

        /**
         * Returns the TLS source view.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return tls.source();
        }

        /**
         * Returns the TLS sink view.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return tls.sink();
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
         * Returns whether the raw connection is idle.
         *
         * @return idle flag
         */
        @Override
        public boolean idle() {
            return raw.idle();
        }

        /**
         * Returns TLS lifecycle state.
         *
         * @return state
         */
        @Override
        public Status state() {
            return tls.state();
        }

        /**
         * Closes TLS resources.
         */
        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                tls.abort();
            } finally {
                raw.abort();
            }
        }

        /**
         * Aborts a TLS exchange that the HTTP layer has already classified as non-reusable.
         */
        @Override
        public void abort() {
            // A graceful close may already own the lifecycle flag while blocked in close_notify or a physical
            // write. Abort must still reach both transport layers so that blocked HTTP/2 reader/writer activities
            // are released; TlsChannel and the raw connection provide their own idempotent close boundaries.
            closed.set(true);
            try {
                tls.abort();
            } finally {
                raw.abort();
            }
        }

    }

    /** Routed connection using the JDK SSLSocket data path. */
    private static final class TlsSocketRoutedConnection extends RoutedConnection {

        /**
         * Raw transport connection that owns the underlying socket.
         */
        private final Connection raw;

        /**
         * TLS conduit layered over the raw connection.
         */
        private final TlsSocketChannel tls;

        private TlsSocketRoutedConnection(final Destination destination, final Connection raw,
                final TlsSocketChannel tls, final Protocol protocol) {
            super(destination, raw, protocol);
            this.raw = require(raw, "Raw connection");
            this.tls = require(tls, "TLS socket channel");
        }

        private TlsHandshake handshake() {
            return tls.handshakeMetadata();
        }

        @Override
        public Conduit conduit() {
            return tls;
        }

        @Override
        public Source source() {
            return tls.source();
        }

        @Override
        public Sink sink() {
            return tls.sink();
        }

        @Override
        public boolean healthy() {
            return tls.opened() && raw.healthy();
        }

        @Override
        public boolean idle() {
            return raw.idle();
        }

        @Override
        public Status state() {
            return raw.state();
        }

        @Override
        public void close() {
            try {
                tls.abort();
            } finally {
                raw.abort();
            }
        }

        @Override
        public void abort() {
            try {
                tls.abort();
            } finally {
                raw.abort();
            }
        }
    }

    /**
     * Atomic connection-local bridge between an HTTP/2 session and pool capacity observers.
     * <p>
     * The session is installed once with compare-and-set. Capacity and draining publications are volatile so pool
     * readers see current availability without taking the HTTP/2 connection lock.
     * </p>
     */
    private static final class RoutedMultiplexAttachment implements Connection.MultiplexAttachment {

        /**
         * Installed protocol session, or {@code null} before HTTP/2 connection creation.
         */
        private final AtomicReference<Object> session = new AtomicReference<>();

        /**
         * Listeners notified whenever capacity or draining state changes.
         */
        private final Set<Connection.CapacityListener> listeners = ConcurrentHashMap.newKeySet();

        /**
         * Last published logical stream capacity.
         */
        private volatile int capacity = Normal._100;

        /**
         * Whether the physical connection refuses new logical streams.
         */
        private volatile boolean draining;

        /**
         * Returns the currently installed protocol session.
         *
         * @return session, or {@code null} before installation
         */
        @Override
        public Object session() {
            return session.get();
        }

        /**
         * Atomically replaces the installed session when it matches the expected value.
         *
         * @param expected expected session
         * @param update   replacement session
         * @return whether the replacement succeeded
         */
        @Override
        public boolean compareAndSetSession(final Object expected, final Object update) {
            return session.compareAndSet(expected, update);
        }

        /**
         * Returns usable stream capacity, suppressing capacity while draining.
         *
         * @return available logical stream count
         */
        @Override
        public int capacity() {
            return draining ? Normal._0 : capacity;
        }

        /**
         * Reports whether new logical streams are disabled.
         *
         * @return draining flag
         */
        @Override
        public boolean draining() {
            return draining;
        }

        /**
         * Registers a capacity listener and immediately publishes the current state to it.
         *
         * @param listener listener to register
         * @return registration that removes the listener
         */
        @Override
        public Connection.Registration listen(final Connection.CapacityListener listener) {
            final Connection.CapacityListener checked = require(listener, "Capacity listener");
            listeners.add(checked);
            checked.changed(capacity(), draining);
            return () -> listeners.remove(checked);
        }

        /**
         * Publishes normalized capacity and draining state to every registered listener.
         *
         * @param capacity available logical stream count
         * @param draining whether new streams are disabled
         */
        @Override
        public void publish(final int capacity, final boolean draining) {
            this.capacity = Math.max(Normal._0, capacity);
            this.draining = draining;
            for (final Connection.CapacityListener listener : listeners) {
                listener.changed(capacity(), draining);
            }
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

        /** Last successful address per logical origin; failed direct attempts fall back to the full race. */
        private final ConcurrentHashMap<Address, InetAddress> preferredAddresses = new ConcurrentHashMap<>();

        /** Lock-free most-recent origin fast path ahead of the multi-origin map. */
        private volatile Address preferredAddress;

        /** Last successful candidate paired with {@link #preferredAddress}. */
        private volatile InetAddress preferredCandidate;

        /**
         * Creates a socket connector.
         *
         * @param listener   lifecycle listener
         * @param resolver   DNS resolver
         * @param dispatcher runtime dispatcher
         */
        private SocketConnector(final Listener<Object> listener, final DnsResolver resolver,
                final Dispatcher dispatcher) {
            this.listener = safe(listener);
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
            final String key = Protocol.HTTP.name + Symbol.COLON + "connect" + Symbol.COLON + address.host()
                    + Symbol.COLON + address.port();
            final CompletableFuture<Connection> result = new CompletableFuture<>();
            final Activity activity = Activity.of(key, () -> result.complete(open(address, timeout)));
            final DispatchHandle operation = dispatcher.background(key, this, activity);
            operation.future().whenComplete((ignored, cause) -> {
                if (cause != null && !result.isDone()) {
                    final Throwable failure = activity.failure();
                    result.completeExceptionally(failure == null ? cause : failure);
                }
            });
            result.whenComplete((value, cause) -> {
                if (result.isCancelled()) {
                    dispatcher.cancel(operation);
                }
            });
            return result;
        }

        /**
         * Returns supported transports.
         *
         * @param transport transport scheme to test
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
         * @param address remote address to connect
         * @param timeout maximum duration allowed for the blocking connect
         * @return connection
         */
        private Connection open(final Address address, final Timeout timeout) {
            return open(address, timeout, false);
        }

        /** Opens either the channel transport or the plain-socket shape used by one-shot JSSE routes. */
        private Connection open(final Address address, final Timeout timeout, final boolean socketStream) {
            if (closed.get()) {
                throw new StatefulException("HTTP socket connector is closed");
            }
            final DnsResult result = resolver.resolve(address.host());
            if (result.addresses().isEmpty()) {
                final SocketException failure = new SocketException("DNS returned no address for " + address.host());
                listener.failure(this, failure);
                throw failure;
            }
            final long deadline = timeout.connect().isZero() ? Long.MAX_VALUE
                    : System.nanoTime() + timeout.connect().toNanos();
            final Address lastAddress = preferredAddress;
            final InetAddress preferred = lastAddress == address || address.equals(lastAddress) ? preferredCandidate
                    : preferredAddresses.get(address);
            if (preferred != null && result.addresses().contains(preferred)) {
                try {
                    return connectCandidate(address, timeout, preferred, deadline, socketStream);
                } catch (final RuntimeException ignored) {
                    preferredAddresses.remove(address, preferred);
                    if (preferred == preferredCandidate) {
                        preferredAddress = null;
                        preferredCandidate = null;
                    }
                }
            }
            RuntimeException failure = null;
            for (int index = Normal._0; index < result.addresses().size(); index += Normal._2) {
                try {
                    final Connection connection = race(
                            address,
                            timeout,
                            result.addresses(),
                            index,
                            deadline,
                            socketStream);
                    return connection;
                } catch (final RuntimeException e) {
                    failure = e;
                }
            }
            listener.failure(this, failure);
            throw failure;
        }

        /**
         * Races at most two stable-order address candidates with a 250 ms stagger.
         *
         * @param address    unresolved destination retaining the logical host and port
         * @param timeout    connection timeout policy
         * @param candidates stable-order resolved address candidates
         * @param offset     index of the first candidate in this race
         * @param deadline   shared monotonic connection deadline
         * @return first successfully established connection
         */
        private Connection race(
                final Address address,
                final Timeout timeout,
                final List<InetAddress> candidates,
                final int offset,
                final long deadline,
                final boolean socketStream) {
            final int count = Math.min(Normal._2, candidates.size() - offset);
            if (count == Normal._1) {
                return connectCandidate(address, timeout, candidates.get(offset), deadline, socketStream);
            }
            final CompletableFuture<Connection> winner = new CompletableFuture<>();
            final AtomicInteger failures = new AtomicInteger();
            final AtomicReference<RuntimeException> lastFailure = new AtomicReference<>();
            launch(
                    address,
                    timeout,
                    candidates.get(offset),
                    deadline,
                    winner,
                    failures,
                    lastFailure,
                    count,
                    socketStream);
            if (count == Normal._2 && !winner.isDone()) {
                // Start the alternate immediately. The previous unconditional sleep
                // charged every successful localhost/cold-TLS connection 250 ms and
                // also delayed fallback after an immediate refusal. The first winner
                // remains authoritative and the late socket is closed by launch().
                launch(
                        address,
                        timeout,
                        candidates.get(offset + Normal._1),
                        deadline,
                        winner,
                        failures,
                        lastFailure,
                        count,
                        socketStream);
            }
            final long remaining = deadline == Long.MAX_VALUE ? Long.MAX_VALUE : deadline - System.nanoTime();
            if (remaining <= Normal._0) {
                throw new TimeoutException("Socket connect timed out");
            }
            try {
                return remaining == Long.MAX_VALUE ? winner.get() : winner.get(remaining, TimeUnit.NANOSECONDS);
            } catch (final java.util.concurrent.TimeoutException e) {
                throw new TimeoutException("Socket connect timed out", e);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SocketException("Socket connect race was interrupted", e);
            } catch (final ExecutionException e) {
                final Throwable cause = e.getCause();
                throw cause instanceof RuntimeException runtime ? runtime
                        : new SocketException("Socket connect failed", cause);
            }
        }

        /**
         * Launches one blocking candidate on a virtual thread and closes every late winner.
         *
         * @param address        unresolved destination retaining the logical port
         * @param timeout        connection timeout policy
         * @param candidate      resolved network address to try
         * @param deadline       shared monotonic connection deadline
         * @param winner         future completed by the first successful candidate
         * @param failures       number of failed candidates
         * @param lastFailure    most recent candidate failure
         * @param candidateCount total candidates participating in this race
         */
        private void launch(
                final Address address,
                final Timeout timeout,
                final InetAddress candidate,
                final long deadline,
                final CompletableFuture<Connection> winner,
                final AtomicInteger failures,
                final AtomicReference<RuntimeException> lastFailure,
                final int candidateCount,
                final boolean socketStream) {
            Thread.ofVirtual().name("fabric-http-connect").start(() -> {
                try {
                    final Connection connection = connectCandidate(address, timeout, candidate, deadline, socketStream);
                    if (!winner.complete(connection)) {
                        connection.close();
                    } else {
                        preferredAddresses.put(address, candidate);
                        preferredCandidate = candidate;
                        preferredAddress = address;
                    }
                } catch (final RuntimeException e) {
                    lastFailure.set(e);
                    if (failures.incrementAndGet() == candidateCount) {
                        winner.completeExceptionally(e);
                    }
                }
            });
        }

        /**
         * Connects one resolved address within the shared connect deadline.
         *
         * @param address   logical destination
         * @param timeout   timeout policy
         * @param candidate resolved address
         * @param deadline  shared connect deadline
         * @return connected socket connection
         */
        private Connection connectCandidate(
                final Address address,
                final Timeout timeout,
                final InetAddress candidate,
                final long deadline,
                final boolean socketStream) {
            if (socketStream) {
                return connectSecureCandidate(address, timeout, candidate, deadline);
            }
            SocketChannel channel = null;
            try {
                channel = SocketChannel.open();
                final long remaining = deadline == Long.MAX_VALUE ? Long.MAX_VALUE : deadline - System.nanoTime();
                if (remaining <= Normal._0) {
                    throw new TimeoutException("Socket connect timed out");
                }
                final Duration candidateTimeout = remaining == Long.MAX_VALUE ? timeout.connect()
                        : Duration.ofNanos(remaining);
                channel.socket()
                        .connect(new InetSocketAddress(candidate, address.port()), timeoutMillis(candidateTimeout));
                final Connection connection = new SocketConnection(address, channel, listener, dispatcher, timeout);
                channel = null;
                return connection;
            } catch (final SocketTimeoutException e) {
                throw new TimeoutException("Socket connect timed out", e);
            } catch (final IOException e) {
                throw new SocketException("Socket connect failed", e);
            } finally {
                IoKit.closeQuietly(channel);
            }
        }

        /** Connects direct HTTPS using the plain Socket shape expected by JSSE. */
        private Connection connectSecureCandidate(
                final Address address,
                final Timeout timeout,
                final InetAddress candidate,
                final long deadline) {
            Socket socket = null;
            try {
                socket = new Socket();
                final long remaining = deadline == Long.MAX_VALUE ? Long.MAX_VALUE : deadline - System.nanoTime();
                if (remaining <= Normal._0)
                    throw new TimeoutException("Socket connect timed out");
                final Duration candidateTimeout = remaining == Long.MAX_VALUE ? timeout.connect()
                        : Duration.ofNanos(remaining);
                socket.connect(new InetSocketAddress(candidate, address.port()), timeoutMillis(candidateTimeout));
                final Connection connection = new SocketConnection(address, socket, listener, dispatcher, timeout);
                socket = null;
                return connection;
            } catch (final SocketTimeoutException e) {
                throw new TimeoutException("Socket connect timed out", e);
            } catch (final IOException e) {
                throw new SocketException("Socket connect failed", e);
            } finally {
                IoKit.closeQuietly(socket);
            }
        }

        /**
         * Converts a timeout to socket milliseconds.
         *
         * @param timeout duration converted for socket APIs
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
        private final Address address;

        /** Lazily materialized only when a caller observes the raw, unrouted connection. */
        private volatile Destination destination;

        /**
         * Socket channel.
         */
        private final SocketChannel channel;

        /** Connected socket, either standalone or owned by {@link #channel}. */
        private final Socket socket;

        /**
         * Network conduit.
         */
        private final Conduit conduit;

        /** Lifecycle listener retained without allocating a full protocol resource scope. */
        private final Listener<Object> listener;

        /** One-way close guard for the transport socket. */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a socket connection.
         *
         * @param address    remote address represented by the connection
         * @param socket     connected channel backing the connection
         * @param listener   lifecycle listener
         * @param dispatcher runtime dispatcher
         * @param timeout    operation timeout policy
         */
        private SocketConnection(final Address address, final SocketChannel socket, final Listener<Object> listener,
                final Dispatcher dispatcher, final Timeout timeout) {
            this.address = require(address, "Address");
            this.channel = require(socket, "Socket channel");
            this.socket = socket.socket();
            this.conduit = new SocketConduit(socket, dispatcher, timeout);
            this.listener = safe(listener);
            this.listener.open(this);
        }

        /** Creates a direct plain-socket connection for JSSE layering. */
        private SocketConnection(final Address address, final Socket socket, final Listener<Object> listener,
                final Dispatcher dispatcher, final Timeout timeout) {
            this.address = require(address, "Address");
            this.channel = null;
            this.socket = require(socket, "Socket");
            this.conduit = new SocketStreamConduit(socket);
            this.listener = safe(listener);
            this.listener.open(this);
        }

        /** Returns the connected blocking socket used for TLS layering. */
        private java.net.Socket socket() {
            return socket;
        }

        /**
         * Returns destination.
         *
         * @return destination
         */
        @Override
        public Destination destination() {
            Destination current = destination;
            if (current == null) {
                current = Destination.of(address.protocol(), address, Options.empty());
                destination = current;
            }
            return current;
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
            return closed.get() ? Status.CLOSED : Status.OPENED;
        }

        /**
         * Returns the protocol-layer source.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return conduit.source();
        }

        /**
         * Returns the protocol-layer sink.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return conduit.sink();
        }

        /**
         * Returns whether healthy.
         *
         * @return healthy flag
         */
        @Override
        public boolean healthy() {
            return !closed.get() && socket.isConnected() && !socket.isClosed() && (channel == null || channel.isOpen());
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
            if (!closed.compareAndSet(false, true))
                return;
            try {
                conduit.close();
            } finally {
                listener.close(this);
            }
        }

    }

    /** Stream conduit used only until a direct HTTPS socket is layered with JSSE. */
    private static final class SocketStreamConduit implements Conduit {

        /**
         * Per-thread staging array reused by blocking stream reads and writes.
         */
        private static final ThreadLocal<byte[]> SCRATCH = ThreadLocal.withInitial(() -> new byte[Normal._8192]);

        /**
         * Connected socket borrowed from the owning raw connection.
         */
        private final Socket socket;

        /**
         * Input stream borrowed from the connected socket.
         */
        private final InputStream input;

        /**
         * Output stream borrowed from the connected socket.
         */
        private final OutputStream output;

        /**
         * Source view backed by this conduit.
         */
        private final Source source;

        /**
         * Sink view backed by this conduit.
         */
        private final Sink sink;

        private SocketStreamConduit(final Socket socket) {
            this.socket = require(socket, "Socket");
            try {
                this.input = socket.getInputStream();
                this.output = socket.getOutputStream();
            } catch (final IOException e) {
                throw new SocketException("Unable to open socket streams", e);
            }
            this.source = Conduit.super.source();
            this.sink = Conduit.super.sink();
        }

        @Override
        public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
            try {
                return CompletableFuture.completedFuture(readSynchronously(target, byteCount));
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        @Override
        public long readSynchronously(final Buffer target, final long byteCount) throws IOException {
            if (byteCount == Normal._0)
                return Normal._0;
            final byte[] scratch = SCRATCH.get();
            final int count = input.read(scratch, Normal._0, (int) Math.min(byteCount, scratch.length));
            if (count > Normal._0)
                target.write(scratch, Normal._0, count);
            return count;
        }

        @Override
        public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
            try {
                return CompletableFuture.completedFuture(writeSynchronously(source, byteCount));
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        @Override
        public long writeSynchronously(final Buffer source, final long byteCount) throws IOException {
            final byte[] scratch = SCRATCH.get();
            long remaining = byteCount;
            while (remaining > Normal._0) {
                final int count = (int) Math.min(remaining, scratch.length);
                source.read(scratch, Normal._0, count);
                output.write(scratch, Normal._0, count);
                remaining -= count;
            }
            return byteCount;
        }

        @Override
        public Source source() {
            return source;
        }

        @Override
        public Sink sink() {
            return sink;
        }

        @Override
        public boolean opened() {
            return !socket.isClosed();
        }

        @Override
        public void close() {
            try {
                socket.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close socket", e);
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
         * Operation timeout policy.
         */
        private final Timeout timeout;

        /**
         * Source view for protocol readers.
         */
        private final Source source;

        /**
         * Sink view for protocol writers.
         */
        private final Sink sink;

        /**
         * Reusable direct socket-read staging buffer owned by the single protocol reader.
         */
        private final ByteBuffer readBuffer;

        /**
         * Creates an adapter.
         *
         * @param socket     socket channel adapted to the network contract
         * @param dispatcher runtime dispatcher
         * @param timeout    operation timeout policy
         */
        private SocketConduit(final SocketChannel socket, final Dispatcher dispatcher, final Timeout timeout) {
            this.socket = require(socket, "Socket channel");
            this.dispatcher = require(dispatcher, "Dispatcher");
            this.timeout = require(timeout, "Timeout");
            this.readBuffer = ByteBuffer.allocateDirect(Builder.BYTES_64_KIB);
            this.source = new SocketSource();
            this.sink = new SocketSink();
        }

        /**
         * Reads directly into the caller-owned NIO buffer.
         *
         * @param target writable caller buffer receiving socket bytes
         * @return future completed with bytes read or {@code -1} at EOF
         */
        @Override
        public CompletableFuture<Integer> read(final ByteBuffer target) {
            final ByteBuffer checkedTarget = require(target, "Read target");
            if (!checkedTarget.hasRemaining()) {
                return CompletableFuture.completedFuture(Normal._0);
            }
            return direct(() -> {
                try {
                    return socket.read(checkedTarget);
                } catch (final IOException e) {
                    throw new SocketException("Socket read failed", e);
                }
            });
        }

        /**
         * Writes the caller-owned NIO buffer completely, preserving its position after every partial write.
         *
         * @param source caller buffer supplying socket bytes
         * @return future completed with the total number of bytes written
         */
        @Override
        public CompletableFuture<Integer> write(final ByteBuffer source) {
            final ByteBuffer checkedSource = require(source, "Write source");
            final int requested = checkedSource.remaining();
            if (requested == Normal._0) {
                return CompletableFuture.completedFuture(Normal._0);
            }
            return direct(() -> {
                int written = Normal._0;
                int zeroProgress = Normal._0;
                try {
                    while (checkedSource.hasRemaining()) {
                        final int count = socket.write(checkedSource);
                        if (count == Normal._0) {
                            if (++zeroProgress >= Normal._16) {
                                throw new SocketException("Socket write made no progress after 16 attempts");
                            }
                            Thread.onSpinWait();
                            continue;
                        }
                        zeroProgress = Normal._0;
                        written += count;
                    }
                    return written;
                } catch (final IOException e) {
                    throw new SocketException("Socket write failed", e);
                }
            });
        }

        /**
         * Reads directly from the blocking socket into the caller-owned NIO buffer.
         *
         * @param target writable destination
         * @return bytes read or EOF
         */
        @Override
        public int readSynchronously(final ByteBuffer target) {
            final ByteBuffer checkedTarget = require(target, "Read target");
            try {
                return socket.read(checkedTarget);
            } catch (final IOException e) {
                throw new SocketException("Socket read failed", e);
            }
        }

        /**
         * Writes the complete caller-owned NIO buffer directly to the blocking socket.
         *
         * @param source source buffer
         * @return number of bytes written
         */
        @Override
        public int writeSynchronously(final ByteBuffer source) {
            final ByteBuffer checkedSource = require(source, "Write source");
            final int requested = checkedSource.remaining();
            int written = Normal._0;
            int zeroProgress = Normal._0;
            try {
                while (checkedSource.hasRemaining()) {
                    final int count = socket.write(checkedSource);
                    if (count == Normal._0) {
                        if (++zeroProgress >= Normal._16) {
                            throw new SocketException("Socket write made no progress after 16 attempts");
                        }
                        Thread.onSpinWait();
                        continue;
                    }
                    zeroProgress = Normal._0;
                    written += count;
                }
                return written == requested ? written : requested;
            } catch (final IOException e) {
                throw new SocketException("Socket write failed", e);
            }
        }

        /**
         * Reads bytes into a core.io buffer.
         *
         * @param target    target buffer
         * @param byteCount maximum byte count
         * @return read future
         */
        @Override
        public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
            final Buffer checkedTarget = require(target, "Read target");
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Read byte count must not be negative"));
            if (byteCount == Normal._0) {
                return CompletableFuture.completedFuture(0L);
            }
            return direct(() -> readSynchronously(checkedTarget, byteCount));
        }

        /**
         * Performs one direct blocking socket read without creating a future or heap staging buffer.
         *
         * @param target    non-null core buffer receiving any bytes read
         * @param byteCount non-negative requested maximum; each channel read is additionally capped at 8192 bytes
         * @return number of bytes read, zero when the channel makes no progress, or -1 at end-of-stream
         */
        @Override
        public long readSynchronously(final Buffer target, final long byteCount) {
            final Buffer checkedTarget = require(target, "Read target");
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Read byte count must not be negative"));
            if (byteCount == Normal._0) {
                return Normal._0;
            }
            readBuffer.clear();
            readBuffer.limit(readCapacity(Math.min(byteCount, readBuffer.capacity())));
            try {
                final int read = socket.read(readBuffer);
                if (read > Normal._0) {
                    readBuffer.flip();
                    checkedTarget.write(readBuffer);
                }
                return read;
            } catch (final IOException e) {
                throw new SocketException("Socket read failed", e);
            }
        }

        /**
         * Writes bytes from a core.io buffer.
         *
         * @param source    source buffer
         * @param byteCount byte count to write
         * @return write future
         */
        @Override
        public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
            final Buffer checkedSource = require(source, "Write source");
            Assert.isTrue(byteCount >= Normal._0, () -> new ValidateException("Write byte count must not be negative"));
            Assert.isTrue(
                    byteCount <= checkedSource.size(),
                    () -> new ValidateException("Write byte count must not exceed source size"));
            if (byteCount == Normal._0) {
                return CompletableFuture.completedFuture(0L);
            }
            return direct(() -> writeSynchronously(checkedSource, byteCount));
        }

        /**
         * Performs direct gathering-compatible socket writes until the requested source prefix is consumed.
         *
         * @param source    non-null core buffer consumed as bytes are accepted by the channel
         * @param byteCount number of bytes to write, from zero through the current source size
         * @return requested byte count after all bytes have been written
         */
        @Override
        public long writeSynchronously(final Buffer source, final long byteCount) {
            final Buffer checkedSource = require(source, "Write source");
            Assert.isTrue(
                    byteCount >= Normal._0 && byteCount <= checkedSource.size(),
                    () -> new ValidateException("Write byte count must be between zero and source size"));
            long written = Normal._0;
            int zeroProgress = Normal._0;
            try {
                while (written < byteCount) {
                    final long remaining = byteCount - written;
                    final ByteBuffer view = checkedSource.nioBuffer(toIntSize(remaining));
                    final int count = socket.write(view);
                    if (count == Normal._0) {
                        if (++zeroProgress >= Normal._16) {
                            throw new SocketException("Socket write made no progress after 16 attempts");
                        }
                        Thread.onSpinWait();
                        continue;
                    }
                    zeroProgress = Normal._0;
                    checkedSource.skip(count);
                    written += count;
                }
                return written;
            } catch (final IOException e) {
                throw new SocketException("Socket write failed", e);
            }
        }

        /**
         * Executes one blocking-channel operation on its calling request thread. SocketChannel blocking operations are
         * interruptible, so cancellation still closes the request promptly without an extra dispatcher task, future
         * completion, and timer for every network read or write.
         *
         * @param supplier blocking socket operation executed by the calling request thread
         * @param <T>      operation result type
         * @return already completed or failed future containing the operation outcome
         */
        private static <T> CompletableFuture<T> direct(final Supplier<T> supplier) {
            try {
                return CompletableFuture.completedFuture(supplier.get());
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        /**
         * Runs a blocking socket operation on the dispatcher background channel.
         *
         * @param key      dispatch key
         * @param deadline operation deadline
         * @param supplier operation supplier
         * @param <T>      result type
         * @return operation future
         */
        private <T> CompletableFuture<T> background(
                final String key,
                final Duration deadline,
                final Supplier<T> supplier) {
            final CompletableFuture<T> result = new CompletableFuture<>();
            final Activity activity = Activity.of(key, () -> result.complete(supplier.get()));
            final DispatchHandle operation = dispatcher.background(key, this, activity);
            final DispatchHandle timer = deadline.isZero() ? null
                    : dispatcher.schedule(key + ":timeout", deadline, Activity.of(key + ":timeout", () -> {
                        if (result.completeExceptionally(new TimeoutException(key + " timed out"))) {
                            close();
                        }
                    }));
            operation.future().whenComplete((ignored, cause) -> {
                if (cause != null && !result.isDone()) {
                    final Throwable failure = activity.failure();
                    result.completeExceptionally(failure == null ? cause : failure);
                }
            });
            result.whenComplete((value, cause) -> {
                if (timer != null) {
                    dispatcher.cancel(timer);
                }
                if (result.isCancelled()) {
                    dispatcher.cancel(operation);
                    close();
                }
            });
            return result;
        }

        /**
         * Returns the core.io source view.
         *
         * @return source view
         */
        @Override
        public Source source() {
            return source;
        }

        /**
         * Returns the core.io sink view.
         *
         * @return sink view
         */
        @Override
        public Sink sink() {
            return sink;
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
         * Returns a bounded channel read capacity.
         *
         * @param byteCount requested byte count
         * @return read capacity
         */
        private static int readCapacity(final long byteCount) {
            return toIntSize(Math.min(byteCount, Normal._8192));
        }

        /**
         * Converts a long byte count to an int size accepted by JDK buffers.
         *
         * @param byteCount byte count
         * @return int size
         */
        private static int toIntSize(final long byteCount) {
            return (int) Math.min(byteCount, Integer.MAX_VALUE);
        }

        /**
         * Source backed by the socket conduit.
         */
        private final class SocketSource implements Source {

            /**
             * Reads bytes through the enclosing conduit.
             *
             * @param sink      target buffer
             * @param byteCount maximum byte count
             * @return read byte count
             */
            @Override
            public long read(final Buffer sink, final long byteCount) {
                return await(SocketConduit.this.read(sink, byteCount), Duration.ZERO, "Socket source read failed");
            }

            /**
             * Returns the no-op timeout.
             *
             * @return timeout
             */
            @Override
            public org.miaixz.bus.core.io.timout.Timeout timeout() {
                return org.miaixz.bus.core.io.timout.Timeout.NONE;
            }

            /**
             * Closes the enclosing conduit.
             */
            @Override
            public void close() {
                SocketConduit.this.close();
            }

        }

        /**
         * Sink backed by the socket conduit.
         */
        private final class SocketSink implements Sink {

            /**
             * Writes bytes through the enclosing conduit.
             *
             * @param source    source buffer
             * @param byteCount byte count
             */
            @Override
            public void write(final Buffer source, final long byteCount) {
                await(SocketConduit.this.write(source, byteCount), Duration.ZERO, "Socket sink write failed");
            }

            /**
             * Flushes this socket sink.
             */
            @Override
            public void flush() {
                // SocketChannel writes are flushed by the operating system.
            }

            /**
             * Returns the no-op timeout.
             *
             * @return timeout
             */
            @Override
            public org.miaixz.bus.core.io.timout.Timeout timeout() {
                return org.miaixz.bus.core.io.timout.Timeout.NONE;
            }

            /**
             * Closes the enclosing conduit.
             */
            @Override
            public void close() {
                SocketConduit.this.close();
            }
        }

    }

    /**
     * Safe listener wrapper.
     *
     * @param delegate listener delegate
     */
    private record SafeListener(Listener<Object> delegate) implements Listener<Object> {

        /**
         * Handles open events.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final Object source) {
            try {
                delegate.open(source);
            } catch (final RuntimeException ignored) {
                // Listener failures must not break HTTP connection lifecycle transitions.
            }
        }

        /**
         * Handles close events.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final Object source) {
            try {
                delegate.close(source);
            } catch (final RuntimeException ignored) {
                // Listener failures must not break HTTP connection lifecycle transitions.
            }
        }

        /**
         * Handles failure events.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final Object source, final Throwable cause) {
            try {
                delegate.failure(source, cause);
            } catch (final RuntimeException ignored) {
                // Listener failures must not break HTTP connection lifecycle transitions.
            }
        }

    }

    /**
     * Internal no-operation listener.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum NoopListener implements Listener<Object> {

        /**
         * Singleton no-operation listener.
         */
        INSTANCE

    }

}
