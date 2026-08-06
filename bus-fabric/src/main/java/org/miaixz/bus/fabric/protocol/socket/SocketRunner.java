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
package org.miaixz.bus.fabric.protocol.socket;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.aio.AioGroup;
import org.miaixz.bus.fabric.network.aio.AioNetwork;
import org.miaixz.bus.fabric.network.kcp.KcpNetwork;
import org.miaixz.bus.fabric.network.kcp.KcpPolicy;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.network.proxy.ProxyPolicyResolver;
import org.miaixz.bus.fabric.network.proxy.ProxySelection;
import org.miaixz.bus.fabric.network.proxy.Socks5UdpCodec;
import org.miaixz.bus.fabric.network.proxy.StreamProxyConnector;
import org.miaixz.bus.fabric.network.tcp.TcpNetwork;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;
import org.miaixz.bus.fabric.network.udp.UdpSession;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.protocol.socket.session.SocketLease;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Opens socket sessions from an immutable socket exchange specification.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SocketRunner {

    /**
     * Immutable exchange configuration and borrowed runtime services.
     */
    private final SocketSpec spec;

    /**
     * Identifier reused by every event emitted while opening this session.
     */
    private final String operationId;

    /**
     * Creates a runner.
     *
     * @param spec immutable socket exchange specification
     * @throws ValidateException if {@code spec} is {@code null}
     */
    SocketRunner(final SocketSpec spec) {
        this.spec = require(spec, "Socket exchange specification");
        this.operationId = ID.objectId();
    }

    /**
     * Opens a socket session.
     *
     * @return opened session using the transport selected from the target scheme
     */
    SocketSession open() {
        return open(Cancellation.create());
    }

    /**
     * Opens a socket session within a cancellation scope.
     *
     * @param cancellation scope shared by transport creation, connection setup, and the returned session
     * @return opened TCP, TLS, UDP, or KCP session
     * @throws CancellationException if opening is cancelled
     * @throws ProtocolException     if the address scheme does not select a supported socket transport
     * @throws ValidateException     if {@code cancellation} is {@code null}
     */
    SocketSession open(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        Logger.info(
                true,
                "Fabric",
                "Socket open started: scheme={}, host={}, port={}, pooled={}",
                spec.address().scheme(),
                spec.address().host(),
                spec.address().port(),
                spec.pooled());
        try {
            currentCancellation.throwIfCancelled();
            final Message opening = prepareOpen();
            currentCancellation.throwIfCancelled();
            checkGuard(opening);
            currentCancellation.throwIfCancelled();
            final Transport transport = Transport.fromScheme(spec.address().scheme());
            final ProxySelection selection = new ProxyPolicyResolver()
                    .resolve(spec.proxy(), spec.context().options(), spec.address());
            SocketSession session = null;
            ConnectionException failure = null;
            for (final ProxyPlan proxy : selection.candidates()) {
                try {
                    session = open(opening, currentCancellation, transport, proxy);
                    break;
                } catch (final ConnectionException e) {
                    if (!e.canSwitchRoute()) {
                        throw e;
                    }
                    selection.connectFailed(proxy, e);
                    if (failure != null && failure != e) {
                        e.addSuppressed(failure);
                    }
                    failure = e;
                } catch (final AuthorizedException e) {
                    throw e;
                } catch (final SocketException | TimeoutException e) {
                    final ConnectionException routeFailure = connectionFailure(
                            proxy,
                            ConnectionException.Phase.TRANSPORT_CONNECT,
                            ConnectionException.Scope.ROUTE,
                            "Unable to establish socket route",
                            e);
                    selection.connectFailed(proxy, routeFailure);
                    if (failure != null) {
                        routeFailure.addSuppressed(failure);
                    }
                    failure = routeFailure;
                }
            }
            if (session == null) {
                throw require(failure, "Proxy candidate failure");
            }
            final Runnable unregisterCancellation = currentCancellation.onCancel(session::cancel);
            try {
                currentCancellation.throwIfCancelled();
                Logger.info(
                        false,
                        "Fabric",
                        "Socket open completed: scheme={}, host={}, port={}, transport={}, pooled={}",
                        spec.address().scheme(),
                        spec.address().host(),
                        spec.address().port(),
                        transport,
                        spec.pooled());
                return session;
            } finally {
                unregisterCancellation.run();
            }
        } catch (final CancellationException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "Socket open cancelled: scheme={}, host={}, port={}, pooled={}",
                    spec.address().scheme(),
                    spec.address().host(),
                    spec.address().port(),
                    spec.pooled());
            throw e;
        } catch (final RuntimeException e) {
            Logger.error(
                    false,
                    "Fabric",
                    e,
                    "Socket open failed: scheme={}, host={}, port={}, pooled={}, exception={}",
                    spec.address().scheme(),
                    spec.address().host(),
                    spec.address().port(),
                    spec.pooled(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Opens one already resolved physical proxy route.
     *
     * @param opening      filtered opening message used to initialize session attributes
     * @param cancellation cancellation scope governing transport and session establishment
     * @param transport    target transport selected from the logical address scheme
     * @param proxy        resolved direct, HTTP, or SOCKS route candidate
     * @return opened socket session
     * @throws ProtocolException if the selected transport and route combination is unsupported
     */
    private SocketSession open(
            final Message opening,
            final Cancellation cancellation,
            final Transport transport,
            final ProxyPlan proxy) {
        return switch (transport) {
            case TCP -> openTcp(opening, cancellation, proxy);
            case TLS -> openTls(opening, cancellation, proxy);
            case UDP -> openUdp(opening, cancellation, proxy);
            case KCP -> openKcp(opening, cancellation, proxy);
            default -> throw new ProtocolException("Socket exchange does not support transport: " + transport);
        };
    }

    /**
     * Creates a structured connection failure before the socket session can deliver application data.
     *
     * @param proxy   resolved route used by the failed attempt
     * @param phase   lifecycle phase in which the attempt failed
     * @param scope   route, target, or configuration component responsible for the failure
     * @param message human-readable failure description
     * @param cause   underlying transport or handshake failure
     * @return structured connection failure with a non-started delivery state
     */
    private static ConnectionException connectionFailure(
            final ProxyPlan proxy,
            final ConnectionException.Phase phase,
            final ConnectionException.Scope scope,
            final String message,
            final Throwable cause) {
        return new ConnectionException(phase, scope, ConnectionException.Delivery.NOT_STARTED, proxy.id(), message,
                cause);
    }

    /**
     * Opens a TCP session.
     *
     * @param opening      filtered opening message
     * @param cancellation cancellation scope governing connection setup
     * @param proxy        resolved direct, HTTP, or SOCKS stream route
     * @return session
     */
    private SocketSession openTcp(final Message opening, final Cancellation cancellation, final ProxyPlan proxy) {
        if (spec.pooled() && proxy.isDirect()) {
            return openPooledTcp(opening, cancellation);
        }
        Logger.debug(
                true,
                "Fabric",
                "Socket TCP connect started: host={}, port={}",
                spec.address().host(),
                spec.address().port());
        AioNetwork aio = null;
        try {
            aio = AioNetwork.create(
                    spec.context().listener(),
                    spec.context().reactor().resolver(),
                    spec.context().reactor().dispatcher(),
                    spec.socketOptions());
            final TcpNetwork network = TcpNetwork.create(aio);
            final Address connectAddress = proxy.proxy().orElse(spec.address());
            final Connection connection = await(network.connect(connectAddress, spec.timeout()), cancellation);
            new StreamProxyConnector().connect(connection, spec.address(), proxy, spec.timeout(), cancellation);
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket TCP connect completed: host={}, port={}",
                    spec.address().host(),
                    spec.address().port());
            return session(connection, null, null, opening, network, cancellation);
        } catch (final RuntimeException e) {
            if (aio != null) {
                aio.close();
            }
            throw e;
        }
    }

    /**
     * Opens a TLS-over-TCP session.
     *
     * @param opening      filtered opening message
     * @param cancellation cancellation scope governing TLS setup
     * @param proxy        resolved direct, HTTP, or SOCKS stream route
     * @return session
     */
    private SocketSession openTls(final Message opening, final Cancellation cancellation, final ProxyPlan proxy) {
        Logger.debug(
                true,
                "Fabric",
                "Socket TLS connect started: host={}, port={}",
                spec.address().host(),
                spec.address().port());
        AioNetwork aio = null;
        try {
            aio = AioNetwork.create(
                    spec.context().listener(),
                    spec.context().reactor().resolver(),
                    spec.context().reactor().dispatcher(),
                    spec.socketOptions());
            final TcpNetwork network = TcpNetwork.create(aio);
            final Address connectAddress = proxy.proxy().orElse(spec.address());
            final Connection raw = await(network.connect(connectAddress, spec.timeout()), cancellation);
            new StreamProxyConnector().connect(raw, spec.address(), proxy, spec.timeout(), cancellation);
            final TlsChannel tls = TlsChannel.wrap(
                    raw.conduit(),
                    TlsEngine.create(spec.tlsContext(), spec.address(), spec.tlsSettings()),
                    spec.context().listener(),
                    spec.context().reactor().dispatcher(),
                    spec.timeout());
            final Runnable unregister = cancellation.onCancel(() -> closeTls(raw, tls));
            try {
                await(tls.handshake(), cancellation);
                Logger.debug(
                        false,
                        "Fabric",
                        "Socket TLS handshake completed: host={}, port={}",
                        spec.address().host(),
                        spec.address().port());
                return session(new TlsSocketConnection(raw, tls), null, null, opening, network, cancellation);
            } catch (final ConnectionException e) {
                closeTls(raw, tls);
                throw e;
            } catch (final SocketException | TimeoutException e) {
                closeTls(raw, tls);
                throw connectionFailure(
                        proxy,
                        ConnectionException.Phase.SECURITY_HANDSHAKE,
                        ConnectionException.Scope.TARGET,
                        "Unable to establish socket TLS session",
                        e);
            } catch (final RuntimeException e) {
                closeTls(raw, tls);
                throw e;
            } finally {
                unregister.run();
            }
        } catch (final RuntimeException e) {
            if (aio != null) {
                aio.close();
            }
            throw e;
        }
    }

    /**
     * Opens a pooled TCP session.
     *
     * @param opening      filtered opening message
     * @param cancellation cancellation scope governing pooled acquisition
     * @return session
     */
    private SocketSession openPooledTcp(final Message opening, final Cancellation cancellation) {
        cancellation.throwIfCancelled();
        Logger.debug(
                true,
                "Fabric",
                "Socket pooled TCP lease started: host={}, port={}",
                spec.address().host(),
                spec.address().port());
        final Destination destination = Destination
                .of(spec.address().protocol(), spec.address(), spec.socketOptions().options());
        final SocketSession session = SocketLease.acquire(
                spec.context().reactor().directory().connectionPool(),
                destination,
                spec.timeout(),
                spec.context().listener(),
                spec.context().reactor().resolver(),
                spec.context().reactor().dispatcher(),
                spec.frameCodec(),
                spec.handler(),
                attributes(opening),
                spec.listener(),
                spec.context().options().materializeMaxBytes()).session();
        cancellation.throwIfCancelled();
        Logger.debug(
                false,
                "Fabric",
                "Socket pooled TCP lease completed: host={}, port={}",
                spec.address().host(),
                spec.address().port());
        return session;
    }

    /**
     * Opens a UDP session.
     *
     * @param opening      filtered opening message
     * @param cancellation cancellation scope governing UDP setup
     * @param proxy        resolved direct or SOCKS datagram route
     * @return session
     * @throws ProtocolException if an HTTP proxy is selected for UDP
     */
    private SocketSession openUdp(final Message opening, final Cancellation cancellation, final ProxyPlan proxy) {
        if (proxy.isHttp()) {
            throw new ProtocolException("HTTP proxies do not support UDP transport");
        }
        Logger.debug(
                true,
                "Fabric",
                "Socket UDP connect started: host={}, port={}",
                spec.address().host(),
                spec.address().port());
        final DatagramOwner owner = proxy.isSocks() ? socksDatagramOwner(proxy, cancellation)
                : DatagramOwner.open(spec.context().listener(), spec.context().reactor().dispatcher());
        try {
            final UdpSession session = owner.udp().connect(spec.address());
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket UDP connect completed: host={}, port={}",
                    spec.address().host(),
                    spec.address().port());
            return session(null, session, null, opening, owner, cancellation);
        } catch (final RuntimeException e) {
            owner.close();
            throw e;
        }
    }

    /**
     * Opens a KCP-over-UDP session.
     *
     * @param opening      filtered opening message
     * @param cancellation cancellation scope governing KCP setup
     * @param proxy        resolved direct or SOCKS datagram route
     * @return session
     * @throws ProtocolException if an HTTP proxy is selected for KCP
     */
    private SocketSession openKcp(final Message opening, final Cancellation cancellation, final ProxyPlan proxy) {
        if (proxy.isHttp()) {
            throw new ProtocolException("HTTP proxies do not support KCP transport");
        }
        Logger.debug(
                true,
                "Fabric",
                "Socket KCP open started: host={}, port={}",
                spec.address().host(),
                spec.address().port());
        final DatagramOwner owner = proxy.isSocks() ? socksDatagramOwner(proxy, cancellation)
                : DatagramOwner.open(spec.context().listener(), spec.context().reactor().dispatcher());
        try {
            final KcpPolicy configured = spec.context().options().get(KcpPolicy.OPTION);
            final KcpPolicy policy = configured == null
                    ? KcpPolicy.builder().wireVersion(spec.socketOptions().kcpWireVersion()).build()
                    : configured;
            final KcpNetwork kcp = KcpNetwork.create(owner.udp(), spec.context().clock(), policy);
            final UdpSession session = kcp.open(spec.address());
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket KCP open completed: host={}, port={}",
                    spec.address().host(),
                    spec.address().port());
            return session(null, session, kcp, opening, owner, cancellation);
        } catch (final RuntimeException e) {
            owner.close();
            throw e;
        }
    }

    /**
     * Opens the SOCKS5 control connection and configures its UDP relay on a private datagram owner.
     *
     * @param proxy        resolved SOCKS5 route used for the control connection
     * @param cancellation cancellation scope governing connection and UDP ASSOCIATE negotiation
     * @return private datagram owner whose close path also closes the SOCKS control transport
     */
    private DatagramOwner socksDatagramOwner(final ProxyPlan proxy, final Cancellation cancellation) {
        final DatagramOwner owner = DatagramOwner
                .open(spec.context().listener(), spec.context().reactor().dispatcher());
        AioNetwork aio = null;
        Connection control = null;
        try {
            aio = AioNetwork.create(
                    spec.context().listener(),
                    spec.context().reactor().resolver(),
                    spec.context().reactor().dispatcher(),
                    spec.socketOptions());
            final TcpNetwork network = TcpNetwork.create(aio);
            control = await(network.connect(proxy.proxy().orElseThrow(), spec.timeout()), cancellation);
            final Connection ownedControl = control;
            final AioNetwork ownedAio = aio;
            final Address relay = new StreamProxyConnector().udpAssociate(control, proxy, spec.timeout(), cancellation);
            owner.udp().relay(new Socks5UdpCodec(relay), () -> {
                ownedControl.close();
                ownedAio.close();
            });
            return owner;
        } catch (final RuntimeException e) {
            if (control != null) {
                control.close();
            }
            if (aio != null) {
                aio.close();
            }
            owner.close();
            throw e;
        }
    }

    /**
     * Waits for connect completion.
     *
     * @param future       non-null asynchronous transport operation
     * @param cancellation cancellation scope allowed to abort the wait
     * @return non-null operation result
     * @throws TimeoutException      if the configured connect timeout expires
     * @throws CancellationException if the cancellation scope is cancelled
     */
    private <T> T await(final CompletableFuture<T> future, final Cancellation cancellation) {
        final CompletableFuture<T> operation = require(future, "Socket operation");
        final Cancellation scope = require(cancellation, "Cancellation");
        final Duration connectTimeout = spec.timeout().connect();
        final long started = System.nanoTime();
        final long deadline = connectTimeout.isZero() ? Long.MAX_VALUE : connectTimeout.toNanos();
        final Runnable unregister = scope.onCancel(() -> operation.cancel(true));
        try {
            while (!operation.isDone()) {
                scope.throwIfCancelled();
                if (System.nanoTime() - started >= deadline) {
                    operation.cancel(true);
                    throw new TimeoutException("Socket open timed out");
                }
                if (!ThreadKit.sleep(Normal._1)) {
                    operation.cancel(true);
                    throw new InternalException("Interrupted while opening socket");
                }
            }
            scope.throwIfCancelled();
            return operation.join();
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("Unable to open socket", cause);
        } finally {
            unregister.run();
        }
    }

    /**
     * Creates a session that borrows the Context runtime and shared cancellation.
     *
     * @param connection   stream connection, or {@code null} for datagram transports
     * @param datagram     UDP session, or {@code null} for stream transports
     * @param kcp          KCP endpoint, or {@code null} except for KCP sessions
     * @param opening      filtered opening message
     * @param owner        resource owner closed with the session
     * @param cancellation cancellation scope shared with the session
     * @return opened session
     */
    private SocketSession session(
            final Connection connection,
            final UdpSession datagram,
            final KcpNetwork kcp,
            final Message opening,
            final AutoCloseable owner,
            final Cancellation cancellation) {
        return new SocketSession(spec.address(), connection, datagram, kcp, SocketCodec.forSession(spec.frameCodec()),
                spec.handler(), attributes(opening), owner, spec.listener(),
                spec.context().options().materializeMaxBytes(), spec.socketOptions(),
                spec.context().reactor().dispatcher(), spec.context().clock(), spec.timeout(), cancellation);
    }

    /**
     * Closes the TLS channel and always attempts to close the raw connection afterward.
     *
     * @param raw raw connection
     * @param tls TLS channel
     */
    private static void closeTls(final Connection raw, final TlsChannel tls) {
        try {
            tls.close();
        } finally {
            raw.close();
        }
    }

    /**
     * Prepares the socket opening message.
     *
     * @return filtered opening message
     */
    private Message prepareOpen() {
        return FilterChain.apply(
                Message.of(
                        spec.address().protocol(),
                        spec.address(),
                        spec.headers(),
                        Payload.empty(),
                        Builder.SOCKET_TAG_OPEN),
                spec.context().filter(),
                spec.filter());
    }

    /**
     * Checks optional guard.
     *
     * @param opening filtered opening message
     */
    private void checkGuard(final Message opening) {
        if (spec.guard() == null) {
            return;
        }
        Logger.debug(
                true,
                "Fabric",
                "Socket guard check started: scheme={}, host={}, port={}",
                spec.address().scheme(),
                spec.address().host(),
                spec.address().port());
        spec.guard().check(opening).throwIfRejected();
        Logger.debug(
                false,
                "Fabric",
                "Socket guard check accepted: scheme={}, host={}, port={}",
                spec.address().scheme(),
                spec.address().host(),
                spec.address().port());
    }

    /**
     * Creates session attributes.
     *
     * @param opening filtered opening message
     * @return mutable insertion-ordered attributes consumed by the new session
     */
    private Map<String, Object> attributes(final Message opening) {
        final LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(Builder.ATTRIBUTE_HEADERS, require(opening, "Opening message").headers().asMap());
        attributes.put(Builder.ATTRIBUTE_OBSERVER, spec.observer());
        attributes.put(Builder.ATTRIBUTE_SOCKET_OPTIONS, spec.socketOptions());
        if (spec.guard() != null) {
            attributes.put(Builder.ATTRIBUTE_GUARD, spec.guard());
        }
        final Object filter = FilterChain.compose(spec.context().filter(), spec.filter());
        if (filter != null) {
            attributes.put(Builder.ATTRIBUTE_FILTER, filter);
        }
        if (spec.proxyHeader() != null) {
            attributes.put(Builder.ATTRIBUTE_PROXY_HEADER, spec.proxyHeader());
            emit(ObservationMarker.PROXY_PARSED, null);
        }
        return attributes;
    }

    /**
     * Emits an observation event.
     *
     * @param marker socket lifecycle marker to publish
     * @param cause  failure attached to the event, or {@code null}
     */
    private void emit(final ObservationMarker marker, final Throwable cause) {
        final FabricEvent.Builder event = FabricEvent.builder(marker, spec.context().clock())
                .tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.TAG_PROTOCOL, spec.address().scheme())
                .tag(Builder.HOST, spec.address().host())
                .tag(Builder.TAG_PORT, Integer.toString(spec.address().port()));
        if (cause != null) {
            event.cause(cause);
        }
        spec.observer().emit(event.build());
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * TLS connection wrapper that exposes plaintext socket reads and writes.
     */
    private static final class TlsSocketConnection implements Connection {

        /**
         * Raw TCP connection.
         */
        private final Connection raw;

        /**
         * TLS channel over the raw connection conduit.
         */
        private final TlsChannel tls;

        /**
         * Ensures the TLS and raw connection boundary closes once.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a TLS socket connection.
         *
         * @param raw underlying TCP connection owned by the wrapper
         * @param tls handshaken TLS channel layered over the raw conduit
         */
        private TlsSocketConnection(final Connection raw, final TlsChannel tls) {
            this.raw = require(raw, "Raw connection");
            this.tls = require(tls, "TLS channel");
        }

        /**
         * Returns the raw connection destination.
         *
         * @return destination metadata of the underlying TCP connection
         */
        @Override
        public Destination destination() {
            return raw.destination();
        }

        /**
         * Returns the TLS plaintext conduit.
         *
         * @return plaintext conduit backed by the TLS channel
         */
        @Override
        public Conduit conduit() {
            return tls;
        }

        /**
         * Returns the TLS channel lifecycle state.
         *
         * @return current TLS channel state
         */
        @Override
        public Lifecycle.State state() {
            return tls.state();
        }

        /**
         * Returns the TLS source view.
         *
         * @return plaintext source view of the TLS channel
         */
        @Override
        public Source source() {
            return tls.source();
        }

        /**
         * Returns the TLS sink view.
         *
         * @return plaintext sink view of the TLS channel
         */
        @Override
        public Sink sink() {
            return tls.sink();
        }

        /**
         * Returns whether TLS and the raw connection are healthy.
         *
         * @return {@code true} when TLS remains open and the raw TCP connection reports healthy
         */
        @Override
        public boolean healthy() {
            return tls.opened() && raw.healthy();
        }

        /**
         * Returns whether the raw connection is idle.
         *
         * @return idleness reported by the underlying TCP connection
         */
        @Override
        public boolean idle() {
            return raw.idle();
        }

        /**
         * Closes the TLS channel and then the raw connection.
         */
        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            RuntimeException failure = null;
            try {
                tls.close();
            } catch (final RuntimeException e) {
                failure = e;
            }
            try {
                raw.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

    /**
     * Owner for datagram transports created for one socket runner.
     *
     * @param group AIO group backing UDP channel operations
     * @param udp   UDP network closed with the group
     */
    private record DatagramOwner(AioGroup group, UdpNetwork udp) implements AutoCloseable {

        /**
         * Opens a UDP owner.
         *
         * @param listener   optional lifecycle listener for the UDP network
         * @param dispatcher borrowed dispatcher used by the single-thread AIO group
         * @return owner containing a new AIO group and UDP network
         */
        private static DatagramOwner open(final Listener<Object> listener, final Dispatcher dispatcher) {
            AioGroup group = null;
            try {
                group = AioGroup.create(Normal._1, dispatcher);
                return new DatagramOwner(group, UdpNetwork.create(group, listener));
            } catch (final RuntimeException e) {
                if (group != null) {
                    group.shutdown();
                }
                throw e;
            }
        }

        /**
         * Closes the owned UDP network and AIO group.
         */
        @Override
        public void close() {
            RuntimeException failure = null;
            try {
                udp.close();
            } catch (final RuntimeException e) {
                failure = e;
            }
            try {
                group.shutdown();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

}
