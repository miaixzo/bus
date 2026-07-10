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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.network.aio.AioGroup;
import org.miaixz.bus.fabric.network.aio.AioNetwork;
import org.miaixz.bus.fabric.network.kcp.KcpNetwork;
import org.miaixz.bus.fabric.network.tcp.TcpNetwork;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;
import org.miaixz.bus.fabric.network.udp.UdpSession;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tag.Tags;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.protocol.socket.session.SocketLease;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.logger.Logger;

/**
 * Opens socket sessions from an immutable socket exchange snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SocketRunner {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Execution snapshot.
     */
    private final SocketSnapshot snapshot;

    /**
     * Creates a runner.
     *
     * @param snapshot execution snapshot
     */
    SocketRunner(final SocketSnapshot snapshot) {
        this.snapshot = require(snapshot, "Socket exchange snapshot");
    }

    /**
     * Opens a socket session.
     *
     * @return session
     */
    SocketSession open() {
        Logger.info(
                true,
                LOG_TAG,
                "Socket open started: scheme={}, host={}, port={}, pooled={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port(),
                snapshot.pooled());
        try {
            checkGuard();
            final Transport transport = Transport.fromScheme(snapshot.address().scheme());
            final SocketSession session = switch (transport) {
                case TCP -> openTcp();
                case TLS -> openTls();
                case UDP -> openUdp();
                case KCP -> openKcp();
                default -> throw new ProtocolException("Socket exchange does not support transport: " + transport);
            };
            emit(ObservationMarker.SOCKET_OPEN, null);
            snapshot.listener().open(session);
            snapshot.callback().success(session);
            Logger.info(
                    false,
                    LOG_TAG,
                    "Socket open completed: scheme={}, host={}, port={}, transport={}, pooled={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port(),
                    transport,
                    snapshot.pooled());
            return session;
        } catch (final RuntimeException e) {
            emit(ObservationMarker.SOCKET_FAILED, e);
            snapshot.callback().failure(e);
            Logger.error(
                    false,
                    LOG_TAG,
                    e,
                    "Socket open failed: scheme={}, host={}, port={}, pooled={}, exception={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port(),
                    snapshot.pooled(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Opens a TCP session.
     *
     * @return session
     */
    private SocketSession openTcp() {
        if (snapshot.pooled()) {
            return openPooledTcp();
        }
        Logger.debug(
                true,
                LOG_TAG,
                "Socket TCP connect started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        AioNetwork aio = null;
        try {
            aio = AioNetwork.create(
                    snapshot.context().listener(),
                    snapshot.context().resolver(),
                    snapshot.context().reactor().dispatcher(),
                    snapshot.socketOptions());
            final TcpNetwork network = TcpNetwork.create(aio);
            final Connection connection = await(network.connect(snapshot.address(), snapshot.timeout()));
            Logger.debug(
                    false,
                    LOG_TAG,
                    "Socket TCP connect completed: host={}, port={}",
                    snapshot.address().host(),
                    snapshot.address().port());
            return new SocketSession(snapshot.address(), connection, SocketCodec.of(snapshot.frameCodec()),
                    snapshot.handler(), attributes(), network, snapshot.listener(),
                    snapshot.context().options().materializeMaxBytes(), snapshot.socketOptions());
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
     * @return session
     */
    private SocketSession openTls() {
        Logger.debug(
                true,
                LOG_TAG,
                "Socket TLS connect started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        AioNetwork aio = null;
        try {
            aio = AioNetwork.create(
                    snapshot.context().listener(),
                    snapshot.context().resolver(),
                    snapshot.context().reactor().dispatcher(),
                    snapshot.socketOptions());
            final TcpNetwork network = TcpNetwork.create(aio);
            final Connection raw = await(network.connect(snapshot.address(), snapshot.timeout()));
            final TlsChannel tls = TlsChannel.wrap(
                    raw.conduit(),
                    TlsEngine.create(tlsContext(), snapshot.address(), tlsSettings()),
                    snapshot.context().listener(),
                    snapshot.context().reactor().dispatcher());
            try {
                await(tls.handshake());
                Logger.debug(
                        false,
                        LOG_TAG,
                        "Socket TLS handshake completed: host={}, port={}",
                        snapshot.address().host(),
                        snapshot.address().port());
                return new SocketSession(snapshot.address(), new TlsSocketConnection(raw, tls),
                        SocketCodec.of(snapshot.frameCodec()), snapshot.handler(), attributes(), network,
                        snapshot.listener(), snapshot.context().options().materializeMaxBytes(),
                        snapshot.socketOptions());
            } catch (final RuntimeException e) {
                tls.close();
                throw e;
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
     * @return session
     */
    private SocketSession openPooledTcp() {
        Logger.debug(
                true,
                LOG_TAG,
                "Socket pooled TCP lease started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        final Destination destination = Destination
                .of(snapshot.address().protocol(), snapshot.address(), snapshot.socketOptions().toOptions());
        final SocketSession session = SocketLease.acquire(
                snapshot.context().directory().connectionPool(),
                destination,
                snapshot.timeout(),
                snapshot.context().listener(),
                snapshot.context().resolver(),
                snapshot.context().reactor().dispatcher(),
                snapshot.frameCodec(),
                snapshot.handler(),
                attributes(),
                snapshot.listener(),
                snapshot.context().options().materializeMaxBytes()).session();
        Logger.debug(
                false,
                LOG_TAG,
                "Socket pooled TCP lease completed: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        return session;
    }

    /**
     * Opens a UDP session.
     *
     * @return session
     */
    private SocketSession openUdp() {
        Logger.debug(
                true,
                LOG_TAG,
                "Socket UDP connect started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        final DatagramOwner owner = DatagramOwner
                .open(snapshot.context().listener(), snapshot.context().reactor().dispatcher());
        try {
            final UdpSession session = owner.udp().connect(snapshot.address());
            Logger.debug(
                    false,
                    LOG_TAG,
                    "Socket UDP connect completed: host={}, port={}",
                    snapshot.address().host(),
                    snapshot.address().port());
            return new SocketSession(snapshot.address(), session, null, SocketCodec.of(snapshot.frameCodec()),
                    snapshot.handler(), attributes(), owner, snapshot.listener(),
                    snapshot.context().options().materializeMaxBytes(), snapshot.socketOptions());
        } catch (final RuntimeException e) {
            owner.close();
            throw e;
        }
    }

    /**
     * Opens a KCP-over-UDP session.
     *
     * @return session
     */
    private SocketSession openKcp() {
        Logger.debug(
                true,
                LOG_TAG,
                "Socket KCP open started: host={}, port={}",
                snapshot.address().host(),
                snapshot.address().port());
        final DatagramOwner owner = DatagramOwner
                .open(snapshot.context().listener(), snapshot.context().reactor().dispatcher());
        try {
            final KcpNetwork kcp = KcpNetwork.create(owner.udp());
            final UdpSession session = kcp.open(snapshot.address());
            Logger.debug(
                    false,
                    LOG_TAG,
                    "Socket KCP open completed: host={}, port={}",
                    snapshot.address().host(),
                    snapshot.address().port());
            return new SocketSession(snapshot.address(), session, kcp, SocketCodec.of(snapshot.frameCodec()),
                    snapshot.handler(), attributes(), owner, snapshot.listener(),
                    snapshot.context().options().materializeMaxBytes(), snapshot.socketOptions());
        } catch (final RuntimeException e) {
            owner.close();
            throw e;
        }
    }

    /**
     * Waits for connect completion.
     *
     * @param future future
     * @return connection
     */
    private <T> T await(final CompletableFuture<T> future) {
        final Duration connectTimeout = snapshot.timeout().connect();
        try {
            if (connectTimeout.isZero()) {
                return future.get();
            }
            return future.get(connectTimeout.toNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while opening socket", e);
        } catch (final ExecutionException e) {
            throw new SocketException("Unable to open socket", e.getCause());
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("Socket open timed out", e);
        }
    }

    /**
     * Checks optional guard.
     */
    private void checkGuard() {
        if (snapshot.guard() == null) {
            return;
        }
        Logger.debug(
                true,
                LOG_TAG,
                "Socket guard check started: scheme={}, host={}, port={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port());
        snapshot.guard()
                .check(
                        Message.of(
                                snapshot.address().protocol(),
                                snapshot.address(),
                                snapshot.headers(),
                                Payload.empty(),
                                null))
                .throwIfRejected();
        Logger.debug(
                false,
                LOG_TAG,
                "Socket guard check accepted: scheme={}, host={}, port={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port());
    }

    /**
     * Creates session attributes.
     *
     * @return attributes
     */
    private Map<String, Object> attributes() {
        final LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(SocketSession.ATTRIBUTE_HEADERS, snapshot.headers().asMap());
        attributes.put(SocketSession.ATTRIBUTE_OBSERVER, snapshot.observer());
        attributes.put(SocketSession.ATTRIBUTE_SOCKET_OPTIONS, snapshot.socketOptions());
        if (snapshot.guard() != null) {
            attributes.put(SocketSession.ATTRIBUTE_GUARD, snapshot.guard());
        }
        if (snapshot.proxyHeader() != null) {
            attributes.put(SocketSession.ATTRIBUTE_PROXY_HEADER, snapshot.proxyHeader());
            emit(ObservationMarker.PROXY_PARSED, null);
        }
        return attributes;
    }

    /**
     * Returns configured TLS context.
     *
     * @return TLS context
     */
    private TlsContext tlsContext() {
        if (snapshot.context().options().contains("socket.tlsContext")) {
            return snapshot.context().options().get("socket.tlsContext", TlsContext.class);
        }
        if (snapshot.context().options().contains("tlsContext")) {
            return snapshot.context().options().get("tlsContext", TlsContext.class);
        }
        return TlsContext.defaults();
    }

    /**
     * Returns configured TLS settings.
     *
     * @return TLS settings
     */
    private TlsSettings tlsSettings() {
        if (snapshot.context().options().contains("socket.tlsSettings")) {
            return snapshot.context().options().get("socket.tlsSettings", TlsSettings.class);
        }
        if (snapshot.context().options().contains("tlsSettings")) {
            return snapshot.context().options().get("tlsSettings", TlsSettings.class);
        }
        return TlsSettings.defaults();
    }

    /**
     * Emits an observation event.
     *
     * @param marker marker
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final Throwable cause) {
        final FabricEvent.Builder event = FabricEvent.builder(marker).tag(Tags.PROTOCOL, snapshot.address().scheme())
                .tag(Tags.HOST, snapshot.address().host()).tag(Tags.PORT, Integer.toString(snapshot.address().port()));
        if (cause != null) {
            event.cause(cause);
        }
        snapshot.observer().emit(event.build());
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
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
         * Creates a TLS socket connection.
         *
         * @param raw raw connection
         * @param tls TLS channel
         */
        private TlsSocketConnection(final Connection raw, final TlsChannel tls) {
            this.raw = require(raw, "Raw connection");
            this.tls = require(tls, "TLS channel");
        }

        @Override
        public Destination destination() {
            return raw.destination();
        }

        @Override
        public org.miaixz.bus.fabric.network.Conduit conduit() {
            return raw.conduit();
        }

        @Override
        public org.miaixz.bus.fabric.Status state() {
            return raw.state();
        }

        @Override
        public CompletableFuture<Integer> read(final java.nio.ByteBuffer buffer) {
            return tls.read(buffer);
        }

        @Override
        public CompletableFuture<Integer> write(final java.nio.ByteBuffer buffer) {
            return tls.write(buffer);
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
        public void close() {
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
         * @param listener   listener
         * @param dispatcher dispatcher
         * @return owner
         */
        private static DatagramOwner open(
                final org.miaixz.bus.fabric.Listener<Object> listener,
                final Dispatcher dispatcher) {
            AioGroup group = null;
            try {
                group = AioGroup.create(1, dispatcher);
                return new DatagramOwner(group, UdpNetwork.create(group, listener));
            } catch (final RuntimeException e) {
                if (group != null) {
                    group.shutdown();
                }
                throw e;
            }
        }

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
