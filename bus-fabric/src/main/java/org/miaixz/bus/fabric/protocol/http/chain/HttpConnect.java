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
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.lang.exception.ConnectionException;
import org.miaixz.bus.core.lang.exception.ConnectionException.Delivery;
import org.miaixz.bus.core.lang.exception.ConnectionException.Phase;
import org.miaixz.bus.core.lang.exception.ConnectionException.Scope;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.network.*;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.dns.DnsResult;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.proxy.StreamProxyConnector;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.TlsSocketChannel;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
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

    /**
     * Debug state captured once; Logger level discovery performs caller inspection and is not a hot-path probe.
     */
    private static final boolean DEBUG_ENABLED = Logger.isDebugEnabled();

    /**
     * Shared unregister action for synchronous exchanges using {@link Cancellation#none()}.
     */
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

    /**
     * Pure route planner.
     */
    private final HttpRoutePlanner routePlanner;

    /** Shared transport-level HTTP CONNECT and SOCKS5 handshake component. */
    private final StreamProxyConnector proxyConnector;

    /**
     * TLS upgrade component.
     */
    private final HttpTlsConnector tlsConnector;

    /**
     * Connection-pool acquisition component.
     */
    private final HttpConnectionAcquirer acquirer;

    /**
     * Canonical direct-route plans, avoiding construction of discarded destinations and option snapshots on every
     * pooled request.
     */
    private final ConcurrentHashMap<Address, HttpRoutePlanner.Plan> directRoutes;

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
        this.routePlanner = new HttpRoutePlanner(tlsContext, tlsSettings);
        this.proxyConnector = new StreamProxyConnector();
        this.tlsConnector = new HttpTlsConnector(tlsContext, tlsSettings, this.listener, this.dispatcher);
        this.acquirer = new HttpConnectionAcquirer(this.pool);
        this.directRoutes = new ConcurrentHashMap<>();
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
        final ConnectionLease lease;
        try {
            lease = acquireResolved(current, cancellation);
        } catch (final ConnectionException | ProtocolException | AuthorizedException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ConnectionException(Phase.TRANSPORT_CONNECT, Scope.ROUTE, Delivery.NOT_STARTED,
                    current.proxy().id(), "Unable to establish HTTP route", e);
        }
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
        return acquireResolved(current, scope);
    }

    /**
     * Acquires a lease for one request whose proxy policy has already been resolved to a physical route.
     *
     * @param current request carrying a direct, HTTP, or SOCKS proxy plan
     * @param scope   cancellation scope governing acquisition and route establishment
     * @return connection lease for the resolved destination
     */
    private ConnectionLease acquireResolved(final HttpRequest current, final Cancellation scope) {
        final Address target = current.url().address();
        final ProxyPlan proxy = routePlanner.proxy(current);
        final HttpRoutePlanner.Plan route = proxy.isDirect() ? directRoutes
                .computeIfAbsent(target, ignored -> routePlanner.plan(target, proxy, connector.supports(Transport.TLS)))
                : routePlanner.plan(target, proxy, connector.supports(Transport.TLS));
        final Destination destination = route.destination();
        final boolean debug = DEBUG_ENABLED;
        if (debug) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP connection lease acquisition started: host={}, port={}, secure={}, proxyMode={}, tunnel={}",
                    target.host(),
                    target.port(),
                    target.secure(),
                    route.proxyMode(),
                    route.tunnel());
        }
        final boolean transientConnection = Http.Header.CONNECTION_CLOSE
                .equalsIgnoreCase(current.headers().get(Http.Header.CONNECTION));
        final Supplier<Connection> factory = () -> open(
                route,
                target,
                proxy,
                current.timeout(),
                scope,
                transientConnection);
        final ConnectionLease lease = acquirer.acquire(destination, factory, scope, transientConnection);
        if (debug) {
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP connection lease acquired: host={}, port={}, healthy={}, proxyMode={}",
                    target.host(),
                    target.port(),
                    lease.connection().healthy(),
                    route.proxyMode());
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
        final HttpConnectionLease state = HttpConnectionLease.from(payload);
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
        directRoutes.clear();
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
     * @param route               immutable route plan
     * @param target              target address
     * @param proxy               proxy plan
     * @param timeout             maximum duration allowed for connection establishment
     * @param cancellation        cancellation scope governing route establishment
     * @param transientConnection whether the physical connection must bypass reusable pooling
     * @return network connection
     */
    private Connection open(
            final HttpRoutePlanner.Plan route,
            final Address target,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation,
            final boolean transientConnection) {
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final boolean tunnel = route.tunnel();
        final Address connectAddress = route.connectAddress();
        final Destination destination = route.destination();
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
                    route.proxyMode(),
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
                proxyConnector.connect(raw, target, proxy, timeout, scope);
            }
            if (tunnel) {
                proxyConnector.connect(raw, target, proxy, timeout, scope);
            }
            scope.throwIfCancelled();
            if (target.secure() && (tunnel || !connector.supports(Transport.TLS))) {
                final Connection secured;
                try {
                    secured = tlsConnection(destination, raw, target, timeout, scope, transientConnection);
                } catch (final ConnectionException | ProtocolException | AuthorizedException e) {
                    throw e;
                } catch (final RuntimeException e) {
                    throw new ConnectionException(Phase.SECURITY_HANDSHAKE, Scope.TARGET, Delivery.NOT_STARTED,
                            proxy.id(), "Unable to establish HTTP TLS session", e);
                }
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
     * @param destination    connection destination
     * @param raw            raw connection
     * @param target         target address
     * @param timeout        request timeout policy
     * @param cancellation   cancellation scope
     * @param socketFastPath whether the built-in blocking socket can use the optimized JSSE path
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
        final HttpTlsConnector.ChannelUpgrade upgrade = tlsConnector.channel(raw, target, timeout, scope);
        return new TlsRoutedConnection(destination, raw, upgrade.channel(), upgrade.handshake(), upgrade.protocol());
    }

    /**
     * Uses the JDK's optimized blocking TLS socket for the built-in blocking HTTP transport.
     *
     * @param destination  logical destination and pool identity
     * @param raw          connected plain socket transport to upgrade
     * @param target       secure target used for peer identity and SNI
     * @param timeout      TLS handshake timeout policy
     * @param cancellation cancellation scope governing the upgrade
     * @return TLS-routed connection backed by the upgraded socket
     */
    private Connection tlsSocketConnection(
            final Destination destination,
            final SocketConnection raw,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        final HttpTlsConnector.SocketUpgrade upgrade = tlsConnector.socket(raw.socket(), target, timeout, cancellation);
        return new TlsSocketRoutedConnection(destination, raw, upgrade.channel(), upgrade.protocol());
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
        if (Protocol.HTTP_2.name.equalsIgnoreCase(applicationProtocol)) {
            return Protocol.HTTP_2;
        }
        throw new ProtocolException("Unsupported negotiated application protocol: " + applicationProtocol);
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
        final HttpConnectionLease state = new HttpConnectionLease(lease, reusable);
        final PayloadBody body = source.body().withTransportPayload(state.wrap(source.body().payload()));
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

    /**
     * Returns transport handshake metadata without exposing the concrete TLS conduit.
     *
     * @param connection established direct or TLS-routed connection
     * @return TLS handshake metadata, or {@code null} for a plain connection
     */
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
        } catch (final CancellationException e) {
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

        /**
         * Sequential protocol session retained for exclusive HTTP/1.1 leases.
         */
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
        public State state() {
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
         * Returns whether both route state and the physical delegate permit another logical lease.
         *
         * @return true when the routed connection is reusable
         */
        @Override
        public boolean reusable() {
            return delegate.reusable() && !draining() && capacity() > Normal._0;
        }

        /**
         * Actively validates the exclusively idle physical delegate while retaining route admission checks.
         *
         * @return true when both route and transport remain reusable
         */
        @Override
        public boolean validateIdle() {
            return reusable() && delegate.validateIdle();
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
        public State state() {
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

    /**
     * Routed connection using the JDK SSLSocket data path.
     */
    private static final class TlsSocketRoutedConnection extends RoutedConnection {

        /**
         * Raw transport connection that owns the underlying socket.
         */
        private final Connection raw;

        /**
         * TLS conduit layered over the raw connection.
         */
        private final TlsSocketChannel tls;

        /**
         * Creates a routed connection backed by a blocking JSSE socket.
         *
         * @param destination routed destination
         * @param raw         raw connection that owns the socket
         * @param tls         TLS socket conduit
         * @param protocol    negotiated application protocol
         */
        private TlsSocketRoutedConnection(final Destination destination, final Connection raw,
                final TlsSocketChannel tls, final Protocol protocol) {
            super(destination, raw, protocol);
            this.raw = require(raw, "Raw connection");
            this.tls = require(tls, "TLS socket channel");
        }

        /**
         * Returns metadata from the completed TLS handshake.
         *
         * @return negotiated TLS handshake metadata
         */
        private TlsHandshake handshake() {
            return tls.handshakeMetadata();
        }

        /**
         * Returns the TLS conduit used for application data.
         *
         * @return TLS socket conduit
         */
        @Override
        public Conduit conduit() {
            return tls;
        }

        /**
         * Returns the TLS-decoded source.
         *
         * @return application-data source
         */
        @Override
        public Source source() {
            return tls.source();
        }

        /**
         * Returns the TLS-encoding sink.
         *
         * @return application-data sink
         */
        @Override
        public Sink sink() {
            return tls.sink();
        }

        /**
         * Reports whether both the TLS conduit and raw connection remain usable.
         *
         * @return {@code true} when the routed connection is healthy
         */
        @Override
        public boolean healthy() {
            return tls.opened() && raw.healthy();
        }

        /**
         * Reports whether the raw connection is currently idle.
         *
         * @return {@code true} when no operation is active
         */
        @Override
        public boolean idle() {
            return raw.idle();
        }

        /**
         * Returns the lifecycle state of the raw connection.
         *
         * @return current connection status
         */
        @Override
        public State state() {
            return raw.state();
        }

        /**
         * Closes the TLS conduit and its owning raw connection.
         */
        @Override
        public void close() {
            try {
                tls.abort();
            } finally {
                raw.abort();
            }
        }

        /**
         * Aborts the TLS conduit and its owning raw connection.
         */
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

        /**
         * Dispatcher-owned address candidate race.
         */
        private final HappyEyeballsConnector happyEyeballs;

        /**
         * Last successful address per logical origin; failed direct attempts fall back to the full race.
         */
        private final ConcurrentHashMap<Address, InetAddress> preferredAddresses = new ConcurrentHashMap<>();

        /**
         * Lock-free most-recent origin fast path ahead of the multi-origin map.
         */
        private volatile Address preferredAddress;

        /**
         * Last successful candidate paired with {@link #preferredAddress}.
         */
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
            this.happyEyeballs = new HappyEyeballsConnector(this.dispatcher);
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

        /**
         * Opens either the channel transport or the plain-socket shape used by one-shot JSSE routes.
         *
         * @param address      unresolved logical destination whose host is resolved by this connector
         * @param timeout      shared connection timeout policy
         * @param socketStream whether to create a blocking Socket transport for direct JSSE layering
         * @return first successfully connected transport
         */
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
         * @param address      unresolved destination retaining the logical host and port
         * @param timeout      connection timeout policy
         * @param candidates   stable-order resolved address candidates
         * @param offset       index of the first candidate in this race
         * @param deadline     shared monotonic connection deadline
         * @param socketStream whether candidates must use the blocking Socket transport
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
            final HappyEyeballsConnector.Result result = happyEyeballs.race(
                    candidates,
                    offset,
                    count,
                    deadline,
                    (candidate, sharedDeadline) -> connectCandidate(
                            address,
                            timeout,
                            candidate,
                            sharedDeadline,
                            socketStream));
            preferredAddresses.put(address, result.candidate());
            preferredCandidate = result.candidate();
            preferredAddress = address;
            return result.connection();
        }

        /**
         * Connects one resolved address within the shared connect deadline.
         *
         * @param address      logical destination
         * @param timeout      timeout policy
         * @param candidate    resolved address
         * @param deadline     shared connect deadline
         * @param socketStream whether to create a blocking Socket instead of a SocketChannel
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

        /**
         * Connects direct HTTPS using the plain Socket shape expected by JSSE.
         *
         * @param address   logical secure destination
         * @param timeout   shared connection timeout policy
         * @param candidate resolved network address
         * @param deadline  shared monotonic connection deadline
         * @return connected plain-socket transport ready for TLS layering
         */
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

        /**
         * Lazily materialized only when a caller observes the raw, unrouted connection.
         */
        private volatile Destination destination;

        /**
         * Socket channel.
         */
        private final SocketChannel channel;

        /**
         * Connected socket, either standalone or owned by {@link #channel}.
         */
        private final Socket socket;

        /**
         * Network conduit.
         */
        private final Conduit conduit;

        /**
         * Reused one-byte buffer for exclusive idle-channel validation.
         */
        private final ByteBuffer staleProbe;

        /**
         * Lifecycle listener retained without allocating a full protocol resource scope.
         */
        private final Listener<Object> listener;

        /**
         * One-way close guard for the transport socket.
         */
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
            this.staleProbe = ByteBuffer.allocate(Normal._1);
            this.listener = safe(listener);
            this.listener.open(this);
        }

        /**
         * Creates a direct plain-socket connection for JSSE layering.
         *
         * @param address    logical remote address
         * @param socket     connected blocking socket owned by the new connection
         * @param listener   lifecycle listener notified when the connection opens and closes
         * @param dispatcher runtime dispatcher retained for constructor symmetry with channel connections
         * @param timeout    operation timeout policy retained for constructor symmetry with channel connections
         */
        private SocketConnection(final Address address, final Socket socket, final Listener<Object> listener,
                final Dispatcher dispatcher, final Timeout timeout) {
            this.address = require(address, "Address");
            this.channel = null;
            this.socket = require(socket, "Socket");
            this.conduit = new SocketStreamConduit(socket);
            this.staleProbe = null;
            this.listener = safe(listener);
            this.listener.open(this);
        }

        /**
         * Returns the connected blocking socket used for TLS layering.
         *
         * @return connected socket owned by this connection
         */
        private Socket socket() {
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
        public State state() {
            return closed.get() ? State.CLOSED : State.RUNNING;
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
         * Checks an exclusive idle channel for a peer close without waiting for application data.
         *
         * @return true when the channel has no pending data and has not reached EOF
         */
        @Override
        public boolean validateIdle() {
            if (!reusable() || channel == null) {
                return reusable();
            }
            synchronized (channel.blockingLock()) {
                boolean valid = false;
                final boolean blocking = channel.isBlocking();
                try {
                    if (blocking) {
                        channel.configureBlocking(false);
                    }
                    staleProbe.clear();
                    valid = channel.read(staleProbe) == Normal._0;
                } catch (final IOException | RuntimeException ignored) {
                    valid = false;
                } finally {
                    if (blocking && channel.isOpen()) {
                        try {
                            channel.configureBlocking(true);
                        } catch (final IOException | RuntimeException ignored) {
                            valid = false;
                        }
                    }
                }
                return valid;
            }
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

    /**
     * Stream conduit used only until a direct HTTPS socket is layered with JSSE.
     */
    private static final class SocketStreamConduit implements Conduit {

        /**
         * Maximum raw-socket staging arrays retained across short-lived connections.
         */
        private static final int SCRATCH_CAPACITY = Normal._256;

        /**
         * Bounded operation-owned staging arrays; direct TLS routes normally never need to borrow one.
         */
        private static final ArrayBlockingQueue<byte[]> SCRATCH = new ArrayBlockingQueue<>(SCRATCH_CAPACITY);

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

        /**
         * Creates a stream conduit over a connected socket.
         *
         * @param socket connected TLS application-data socket
         */
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

        /**
         * Reads bytes from the socket and exposes the completed result as a future.
         *
         * @param target    destination buffer
         * @param byteCount maximum number of bytes to read
         * @return completed read result or failed future
         */
        @Override
        public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
            try {
                return CompletableFuture.completedFuture(readSynchronously(target, byteCount));
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        /**
         * Reads bytes synchronously from the socket input stream.
         *
         * @param target    destination buffer
         * @param byteCount maximum number of bytes to read
         * @return bytes read, zero for an empty request, or {@code -1} at end of stream
         * @throws IOException if the input stream cannot be read
         */
        @Override
        public long readSynchronously(final Buffer target, final long byteCount) throws IOException {
            if (byteCount == Normal._0)
                return Normal._0;
            final byte[] scratch = borrowScratch();
            try {
                final int count = input.read(scratch, Normal._0, (int) Math.min(byteCount, scratch.length));
                if (count > Normal._0)
                    target.write(scratch, Normal._0, count);
                return count;
            } finally {
                releaseScratch(scratch);
            }
        }

        /**
         * Writes bytes to the socket and exposes the completed result as a future.
         *
         * @param source    source buffer
         * @param byteCount number of bytes to write
         * @return completed write result or failed future
         */
        @Override
        public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
            try {
                return CompletableFuture.completedFuture(writeSynchronously(source, byteCount));
            } catch (final Throwable failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        /**
         * Writes the requested bytes synchronously to the socket output stream.
         *
         * @param source    source buffer
         * @param byteCount number of bytes to write
         * @return number of bytes written
         * @throws IOException if the output stream cannot be written
         */
        @Override
        public long writeSynchronously(final Buffer source, final long byteCount) throws IOException {
            final byte[] scratch = borrowScratch();
            try {
                long remaining = byteCount;
                while (remaining > Normal._0) {
                    final int count = (int) Math.min(remaining, scratch.length);
                    source.read(scratch, Normal._0, count);
                    output.write(scratch, Normal._0, count);
                    remaining -= count;
                }
                return byteCount;
            } finally {
                releaseScratch(scratch);
            }
        }

        /**
         * Borrows one exclusive staging array without blocking socket I/O.
         *
         * @return reusable or transient staging array
         */
        private static byte[] borrowScratch() {
            final byte[] scratch = SCRATCH.poll();
            return scratch == null ? new byte[Normal._8192] : scratch;
        }

        /**
         * Returns a staging array to the bounded pool.
         *
         * @param scratch staging array no longer used by the caller
         */
        private static void releaseScratch(final byte[] scratch) {
            SCRATCH.offer(scratch);
        }

        /**
         * Returns the reusable source view.
         *
         * @return conduit source
         */
        @Override
        public Source source() {
            return source;
        }

        /**
         * Returns the reusable sink view.
         *
         * @return conduit sink
         */
        @Override
        public Sink sink() {
            return sink;
        }

        /**
         * Reports whether the socket remains open.
         *
         * @return {@code true} while the socket is not closed
         */
        @Override
        public boolean opened() {
            return !socket.isClosed();
        }

        /**
         * Closes the underlying socket.
         */
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
