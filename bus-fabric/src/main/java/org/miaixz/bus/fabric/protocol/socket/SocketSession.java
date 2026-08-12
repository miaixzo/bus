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

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Ingress;
import org.miaixz.bus.fabric.network.kcp.KcpNetwork;
import org.miaixz.bus.fabric.network.kcp.KcpPacket;
import org.miaixz.bus.fabric.network.tls.TlsChannel;
import org.miaixz.bus.fabric.network.tls.TlsEngine;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.udp.UdpSession;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.socket.body.SocketBody;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.protocol.socket.session.SocketLease;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.SessionLifecycle;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Open socket session.
 *
 * @author Kimi Liu
 */
public final class SocketSession implements Session {

    /**
     * Handler-thread marker used to route immediate replies through the active callback data plane.
     */
    private static final ThreadLocal<DataPlane> ACTIVE_DATA_PLANE = new ThreadLocal<>();

    /**
     * Remote address.
     */
    private final Address address;

    /**
     * Network connection.
     */
    private final Connection connection;

    /**
     * Active stream conduit, replaced atomically after a successful StartTLS handshake.
     */
    private volatile Conduit streamConduit;

    /**
     * TLS channel created by the one permitted upgrade attempt, or {@code null} before that attempt.
     */
    private volatile TlsChannel upgradeChannel;

    /**
     * One-shot TLS upgrade state.
     */
    private final AtomicReference<TlsUpgradeState> tlsUpgradeState;

    /**
     * Shared operation lock that excludes plaintext reads and writes while the TLS boundary changes.
     */
    private final ReentrantReadWriteLock transportLock;

    /**
     * Datagram session for UDP/KCP transports.
     */
    private final UdpSession datagram;

    /**
     * KCP packet endpoint when this session uses KCP over UDP.
     */
    private final KcpNetwork kcp;

    /**
     * Socket codec.
     */
    private final SocketCodec codec;

    /**
     * Message handler.
     */
    private final Handler handler;

    /**
     * Decoded frames waiting for receive calls.
     */
    private final ArrayDeque<PendingFrame> pendingFrames;

    /**
     * Allocation-light decoded payload owners used by exclusive server readers.
     */
    private final ArrayDeque<ByteString> decodedPayloads;

    /**
     * Attributes.
     */
    private final Map<String, Object> attributes;

    /**
     * Immutable session filter resolved once from the attributes.
     */
    private final Filter messageFilter;

    /**
     * Immutable session guard resolved once from the attributes.
     */
    private final GuardRule messageGuard;

    /**
     * Close owner.
     */
    private final AutoCloseable owner;

    /**
     * Maximum bytes allowed when materializing session payloads.
     */
    private final long materializeMaxBytes;

    /**
     * Socket tuning options.
     */
    private final SocketOptions socketOptions;

    /**
     * Dispatcher used by Calls, idle checks, and KCP work.
     */
    private final Dispatcher dispatcher;

    /**
     * Session clock.
     */
    private final Clock clock;

    /**
     * Session timeout policy.
     */
    private final Timeout timeout;

    /**
     * Cancellation shared by background session work.
     */
    private final Cancellation cancellation;

    /**
     * Event observer reused by session Calls.
     */
    private final EventObserver observer;

    /**
     * Whether observation decoration and traffic events are enabled.
     */
    private final boolean observationEnabled;

    /**
     * Per-emission wire byte count consumed by the observer decorator.
     */
    private final ThreadLocal<Long> trafficBytes;

    /**
     * Whether successful traffic must refresh an idle deadline.
     */
    private final boolean idleTrackingEnabled;

    /**
     * Whether the server-owned reader can invoke the existing receive core directly.
     */
    private final boolean directDataPlane;

    /**
     * Whether the stream receive path is owned by one server reader.
     */
    private final boolean exclusiveReader;

    /**
     * Reusable direct-call metadata for server-owned stream sends.
     */
    private final MonoCall.DirectTemplate<Payload, Void> directSend;

    /**
     * Retained input buffer for the exclusive blocking stream reader.
     */
    private final Buffer streamReadBuffer;

    /**
     * Retained output buffer for serialized server stream writes.
     */
    private final Buffer streamWriteBuffer;

    /**
     * Whether this session owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Active idle timeout handle.
     */
    private final AtomicReference<DispatchHandle> idleHandle;

    /**
     * Single KCP receive Pump handle.
     */
    private final AtomicReference<DispatchHandle> kcpHandle;

    /**
     * Active KCP retransmission handle.
     */
    private final AtomicReference<DispatchHandle> retransmitHandle;

    /**
     * Guard ensuring one terminal path owns cleanup and notification.
     */
    private final AtomicBoolean terminating;

    /**
     * Last activity time.
     */
    private volatile long lastActivityNanos;

    /**
     * Active completion-driven stream data plane.
     */
    private volatile DataPlane dataPlane;

    /**
     * Lifecycle scope.
     */
    private final SessionLifecycle scope;

    /**
     * Creates a current connection-backed socket session for framework integrations.
     *
     * @param address             peer address represented by the session
     * @param connection          connected stream transport backing the session
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     * @return socket session
     */
    public static SocketSession create(
            final Address address,
            final Connection connection,
            final SocketCodec codec,
            final Handler handler,
            final Map<String, Object> attributes,
            final AutoCloseable owner,
            final Listener<? super SocketSession> listener,
            final long materializeMaxBytes,
            final SocketOptions socketOptions) {
        return new SocketSession(address, connection, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions);
    }

    /**
     * Creates a server session whose stream receive path has one framework-owned reader.
     *
     * @param address             peer address represented by the session
     * @param connection          connected stream transport backing the session
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages
     * @param attributes          initial session attributes, or {@code null} for none
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     * @param dispatcher          shared dispatcher
     * @param clock               shared session clock
     * @param timeout             timeout policy governing session operations
     * @param cancellation        shared cancellation scope
     * @return opened server socket session
     */
    static SocketSession createServer(
            final Address address,
            final Connection connection,
            final SocketCodec codec,
            final Handler handler,
            final Map<String, Object> attributes,
            final Listener<? super SocketSession> listener,
            final long materializeMaxBytes,
            final SocketOptions socketOptions,
            final Dispatcher dispatcher,
            final Clock clock,
            final Timeout timeout,
            final Cancellation cancellation) {
        return new SocketSession(address, connection, null, null, codec, handler, attributes, null, listener,
                materializeMaxBytes, socketOptions, dispatcher, clock, timeout, cancellation, false, true);
    }

    /**
     * Creates an opened session.
     *
     * @param address    peer address represented by the session
     * @param connection connected stream transport backing the session
     * @param codec      codec used to encode and decode socket frames
     * @param handler    handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes initial session attributes, or {@code null} for none
     * @param owner      resource closed when the session terminates, or {@code null}
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
            final Map<String, Object> attributes, final AutoCloseable owner) {
        this(address, connection, codec, handler, attributes, owner, null);
    }

    /**
     * Creates an opened session.
     *
     * @param address    peer address represented by the session
     * @param connection connected stream transport backing the session
     * @param codec      codec used to encode and decode socket frames
     * @param handler    handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes initial session attributes, or {@code null} for none
     * @param owner      resource closed when the session terminates, or {@code null}
     * @param listener   lifecycle listener
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
            final Map<String, Object> attributes, final AutoCloseable owner,
            final Listener<? super SocketSession> listener) {
        this(address, connection, codec, handler, attributes, owner, listener, Normal.MEBI_64);
    }

    /**
     * Creates an opened session.
     *
     * @param address             peer address represented by the session
     * @param connection          connected stream transport backing the session
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
            final Map<String, Object> attributes, final AutoCloseable owner,
            final Listener<? super SocketSession> listener, final long materializeMaxBytes) {
        this(address, connection, codec, handler, attributes, owner, listener, materializeMaxBytes,
                SocketOptions.defaults());
    }

    /**
     * Creates an opened session.
     *
     * @param address             peer address represented by the session
     * @param connection          connected stream transport backing the session
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     */
    SocketSession(final Address address, final Connection connection, final SocketCodec codec, final Handler handler,
            final Map<String, Object> attributes, final AutoCloseable owner,
            final Listener<? super SocketSession> listener, final long materializeMaxBytes,
            final SocketOptions socketOptions) {
        this(address, connection, null, null, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions);
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address    peer address represented by the session
     * @param datagram   UDP transport backing the session
     * @param kcp        KCP packet endpoint or null for plain UDP
     * @param codec      codec used to encode and decode socket frames
     * @param handler    handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes initial session attributes, or {@code null} for none
     * @param owner      resource closed when the session terminates, or {@code null}
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
            final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner) {
        this(address, datagram, kcp, codec, handler, attributes, owner, null);
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address    peer address represented by the session
     * @param datagram   UDP transport backing the session
     * @param kcp        KCP packet endpoint or null for plain UDP
     * @param codec      codec used to encode and decode socket frames
     * @param handler    handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes initial session attributes, or {@code null} for none
     * @param owner      resource closed when the session terminates, or {@code null}
     * @param listener   lifecycle listener
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
            final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner,
            final Listener<? super SocketSession> listener) {
        this(address, datagram, kcp, codec, handler, attributes, owner, listener, Normal.MEBI_64);
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address             peer address represented by the session
     * @param datagram            UDP transport backing the session
     * @param kcp                 KCP packet endpoint or null for plain UDP
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
            final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner,
            final Listener<? super SocketSession> listener, final long materializeMaxBytes) {
        this(address, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                SocketOptions.defaults());
    }

    /**
     * Creates an opened datagram session.
     *
     * @param address             peer address represented by the session
     * @param datagram            UDP transport backing the session
     * @param kcp                 KCP packet endpoint or null for plain UDP
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     */
    SocketSession(final Address address, final UdpSession datagram, final KcpNetwork kcp, final SocketCodec codec,
            final Handler handler, final Map<String, Object> attributes, final AutoCloseable owner,
            final Listener<? super SocketSession> listener, final long materializeMaxBytes,
            final SocketOptions socketOptions) {
        this(address, null, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions);
    }

    /**
     * Creates an opened session.
     *
     * @param address    peer address represented by the session
     * @param connection connection or null for datagram
     * @param datagram   datagram session or null for connection
     * @param kcp        KCP packet endpoint or null
     * @param codec      codec used to encode and decode socket frames
     * @param handler    handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes initial session attributes, or {@code null} for none
     * @param owner      resource closed when the session terminates, or {@code null}
     * @param listener   lifecycle listener
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
            final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
            final AutoCloseable owner, final Listener<? super SocketSession> listener) {
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener, Normal.MEBI_64);
    }

    /**
     * Creates an opened session.
     *
     * @param address             peer address represented by the session
     * @param connection          connection or null for datagram
     * @param datagram            datagram session or null for connection
     * @param kcp                 KCP packet endpoint or null
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
            final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
            final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes) {
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                SocketOptions.defaults());
    }

    /**
     * Creates an opened session.
     *
     * @param address             peer address represented by the session
     * @param connection          connection or null for datagram
     * @param datagram            datagram session or null for connection
     * @param kcp                 KCP packet endpoint or null
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
            final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
            final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes,
            final SocketOptions socketOptions) {
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions, Dispatcher.create(), Clock.system(), Timeout.defaults(), Cancellation.create(), true,
                false);
    }

    /**
     * Creates an opened session with a shared runtime.
     *
     * @param address             peer address represented by the session
     * @param connection          connection or null for datagram
     * @param datagram            datagram session or null for connection
     * @param kcp                 KCP packet endpoint or null
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     * @param dispatcher          shared dispatcher
     * @param clock               shared clock
     * @param timeout             timeout policy governing session operations
     * @param cancellation        shared cancellation
     */
    SocketSession(final Address address, final Connection connection, final UdpSession datagram, final KcpNetwork kcp,
            final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
            final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes,
            final SocketOptions socketOptions, final Dispatcher dispatcher, final Clock clock, final Timeout timeout,
            final Cancellation cancellation) {
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener, materializeMaxBytes,
                socketOptions, dispatcher, clock, timeout, cancellation, false, false);
    }

    /**
     * Creates an opened session.
     *
     * @param address             peer address represented by the session
     * @param connection          connection or null for datagram
     * @param datagram            datagram session or null for connection
     * @param kcp                 KCP packet endpoint or null
     * @param codec               codec used to encode and decode socket frames
     * @param handler             handler receiving decoded inbound messages, or {@code null} for a no-op handler
     * @param attributes          initial session attributes, or {@code null} for none
     * @param owner               resource closed when the session terminates, or {@code null}
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param socketOptions       socket options
     * @param dispatcher          runtime dispatcher
     * @param clock               session clock
     * @param timeout             timeout policy governing session operations
     * @param cancellation        shared cancellation
     * @param ownsDispatcher      true when cleanup closes the dispatcher
     * @param exclusiveReader     true when one server reader owns stream receives
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
            final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
            final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes,
            final SocketOptions socketOptions, final Dispatcher dispatcher, final Clock clock, final Timeout timeout,
            final Cancellation cancellation, final boolean ownsDispatcher, final boolean exclusiveReader) {
        this.address = require(address, "Socket address");
        if (connection == null && datagram == null) {
            throw new ValidateException("Socket transport must not be null");
        }
        this.connection = connection;
        this.streamConduit = connection == null ? null : connection.conduit();
        this.tlsUpgradeState = new AtomicReference<>(connection == null ? TlsUpgradeState.UNAVAILABLE
                : this.streamConduit instanceof TlsChannel ? TlsUpgradeState.SECURE : TlsUpgradeState.PLAIN);
        this.transportLock = new ReentrantReadWriteLock(true);
        this.datagram = datagram;
        this.kcp = kcp;
        this.codec = require(codec, "Socket codec");
        this.handler = handler == null ? Demuxer.noop() : handler;
        this.pendingFrames = new ArrayDeque<>();
        this.decodedPayloads = new ArrayDeque<>();
        this.attributes = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
        final Object configuredFilter = this.attributes.get(Builder.ATTRIBUTE_FILTER);
        this.messageFilter = configuredFilter instanceof Filter current ? current : null;
        final Object configuredGuard = this.attributes.get(Builder.ATTRIBUTE_GUARD);
        this.messageGuard = configuredGuard instanceof GuardRule current ? current : null;
        this.owner = owner;
        final Object configuredObserver = this.attributes.get(Builder.ATTRIBUTE_OBSERVER);
        final EventObserver sink = configuredObserver instanceof EventObserver current ? EventObserver.safe(current)
                : EventObserver.noop();
        this.observationEnabled = sink != EventObserver.noop();
        this.trafficBytes = observationEnabled ? new ThreadLocal<>() : null;
        this.observer = observationEnabled ? event -> sink.emit(withTrafficBytes(event)) : EventObserver.noop();
        this.dispatcher = require(dispatcher, "Socket dispatcher");
        this.clock = require(clock, "Socket clock");
        this.timeout = require(timeout, "Socket timeout");
        if (connection != null) {
            // Stream sessions own these policies; datagram and KCP sessions retain their future-level timers.
            connection.source().timeout().timeout(this.timeout.read());
            connection.sink().timeout().timeout(this.timeout.write());
        }
        this.cancellation = require(cancellation, "Socket cancellation");
        this.ownsDispatcher = ownsDispatcher;
        this.exclusiveReader = exclusiveReader;
        this.directSend = exclusiveReader ? MonoCall
                .directTemplate("socket-session-send", "socket:session:send", dispatcher, this::sendNow, this::cancel)
                : null;
        this.socketOptions = socketOptions == null ? SocketOptions.defaults() : socketOptions;
        this.streamReadBuffer = connection == null || (!exclusiveReader && !this.socketOptions.retainReadBuffer())
                ? null
                : new Buffer();
        this.streamWriteBuffer = connection != null && exclusiveReader ? new Buffer() : null;
        this.scope = SessionLifecycle.create(
                this,
                "socket-session",
                listener,
                this.observer,
                ObservationMarker.SOCKET_OPEN,
                ObservationMarker.SOCKET_CLOSED,
                ObservationMarker.SOCKET_FAILED,
                this.clock,
                this.cancellation);
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        this.idleTrackingEnabled = !this.socketOptions.idleTimeout().isZero();
        this.directDataPlane = !observationEnabled && this.timeout.call().isZero();
        this.idleHandle = new AtomicReference<>();
        this.kcpHandle = new AtomicReference<>();
        this.retransmitHandle = new AtomicReference<>();
        this.terminating = new AtomicBoolean();
        this.lastActivityNanos = this.clock.nanos();
        this.scope.open(this);
        scheduleIdle();
        startKcpPump();
    }

    /**
     * Returns address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    public State state() {
        return scope.state();
    }

    /**
     * Sends payload.
     *
     * @param payload payload to encode and send
     * @return send call
     */
    public Call<Void> send(final Payload payload) {
        final Payload current = require(payload, "Socket payload");
        if (exclusiveReader && directDataPlane) {
            return directSend.call(current);
        }
        return MonoCall.<Void>create(
                "socket-session-send",
                "socket:session:send",
                dispatcher,
                observer,
                null,
                timeout,
                () -> sendNow(current),
                this::cancel);
    }

    /**
     * Writes immediately on the serialized server data plane.
     *
     * @param payload payload to send
     */
    public void write(final Payload payload) {
        sendNow(require(payload, "Socket payload"));
    }

    /**
     * Sends a socket body.
     *
     * @param body socket body to send
     * @return send call
     */
    public Call<Void> send(final SocketBody body) {
        return send(require(body, "Socket body").payload());
    }

    /**
     * Sends a shared frame.
     *
     * @param frame pre-encoded frame to send
     * @return send call
     */
    public Call<Void> send(final Frame frame) {
        return send(Payload.of(require(frame, "Frame").payload()));
    }

    /**
     * Receives the next message.
     *
     * @return receive call
     */
    public Call<Message> receive() {
        return MonoCall.create(
                "socket-session-receive",
                "socket:session:receive",
                dispatcher,
                observer,
                null,
                timeout,
                this::receiveNow,
                this::cancel);
    }

    /**
     * Upgrades this open plaintext TCP session to TLS exactly once.
     *
     * <p>
     * The upgrade requires exclusive ownership of the transport and rejects a datagram session, an existing TLS
     * boundary, an active read or write, a started completion data plane, or unconsumed plaintext. Every rejected,
     * cancelled, timed-out, or failed upgrade closes the session because the wire boundary can no longer be reused
     * safely.
     * </p>
     *
     * @param policy complete TLS context and handshake settings
     * @return single-execution call containing the completed TLS handshake
     */
    public Call<TlsHandshake> upgradeTls(final TlsPolicy policy) {
        final TlsPolicy current = require(policy, "TLS policy");
        return MonoCall.create(
                "socket-session-upgrade-tls",
                "socket:session:upgrade-tls",
                dispatcher,
                observer,
                null,
                timeout,
                () -> upgradeTlsNow(current),
                this::cancel);
    }

    /**
     * Reads one message for the server-owned reader.
     *
     * <p>
     * The fast case invokes the same receive core used by the public {@link Call}. Configurations that require Call
     * observation or a Call deadline retain the complete public execution path.
     * </p>
     *
     * @return received message
     */
    Message readDataPlane() {
        return directDataPlane ? receiveNow(exclusiveReader) : receive().execute();
    }

    /**
     * Closes this session.
     *
     * @return true when state changed
     */
    public boolean close() {
        return terminate(Termination.CLOSE, null);
    }

    /**
     * Cancels this session.
     *
     * @return true when state changed
     */
    public boolean cancel() {
        return terminate(Termination.CANCEL, new StatefulException("Socket session was cancelled"));
    }

    /**
     * Returns attributes snapshot.
     *
     * @return attributes
     */
    public Map<String, Object> attributes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    /**
     * Returns socket tuning options.
     *
     * @return socket options
     */
    public SocketOptions socketOptions() {
        return socketOptions;
    }

    /**
     * Runs one complete outbound socket pipeline.
     *
     * @param source application payload
     * @return null after a complete write
     */
    private Void sendNow(final Payload source) {
        final Lock operation = enterTransportOperation("Socket write cannot run during TLS upgrade");
        try {
            ensureOpen();
            final Payload outgoing;
            if (messageFilter == null && messageGuard == null) {
                outgoing = source;
            } else {
                final Message message = filter(source, "socket-write");
                checkGuard(message);
                outgoing = message.payload();
            }
            final ByteString bytes = snapshot(outgoing, "SocketSession.send(Payload)");
            final DataPlane activePlane = ACTIVE_DATA_PLANE.get();
            if (activePlane != null && activePlane.session() == this) {
                activePlane.write(bytes);
                return null;
            }
            if (connection != null) {
                writeEncodedStream(bytes);
                return null;
            }
            final Buffer encoded = new Buffer();
            synchronized (codec) {
                codec.encodeOwned(bytes, encoded);
            }
            final long wireBytes = encoded.size();
            Logger.debug(
                    true,
                    "Fabric",
                    "Socket send started: scheme={}, host={}, port={}, bytes={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    wireBytes);
            final byte[] datagramBytes = encoded.readByteArray();
            if (kcp == null) {
                sendPacket(Payload.of(datagramBytes));
            } else {
                sendKcpPackets(kcp.encode(Payload.of(datagramBytes)));
                scheduleRetransmission();
            }
            emit(ObservationMarker.SOCKET_WRITE, wireBytes, null);
            touch();
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket send completed: scheme={}, host={}, port={}, bytes={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    wireBytes);
            return null;
        } catch (final RuntimeException e) {
            operationFailed(e);
            throw e;
        } catch (final Error e) {
            operationFailed(e);
            throw e;
        } finally {
            operation.unlock();
        }
    }

    /**
     * Encodes and writes one owned stream payload, reusing the session buffer when serialization permits.
     *
     * @param payload immutable payload bytes
     */
    private void writeEncodedStream(final ByteString payload) {
        if (streamWriteBuffer == null) {
            final Buffer encoded = new Buffer();
            synchronized (codec) {
                codec.encodeOwned(payload, encoded);
            }
            writeEncodedBuffer(encoded);
            return;
        }
        synchronized (streamWriteBuffer) {
            codec.encodeOwned(payload, streamWriteBuffer);
            writeEncodedBuffer(streamWriteBuffer);
        }
    }

    /**
     * Writes an already encoded stream frame and publishes traffic events.
     *
     * @param encoded encoded frame bytes
     */
    private void writeEncodedBuffer(final Buffer encoded) {
        final long wireBytes = encoded.size();
        Logger.debug(
                true,
                "Fabric",
                "Socket send started: scheme={}, host={}, port={}, bytes={}",
                address.scheme(),
                address.host(),
                address.port(),
                wireBytes);
        writeStream(encoded, wireBytes);
        emit(ObservationMarker.SOCKET_WRITE, wireBytes, null);
        touch();
    }

    /**
     * Writes one encoded frame through the complete Conduit contract.
     *
     * @param encoded   encoded frame bytes to write
     * @param byteCount encoded byte count
     */
    private void writeStream(final Buffer encoded, final long byteCount) {
        final Conduit conduit = streamConduit();
        if (exclusiveReader && dataPlane == null && conduit instanceof Ingress) {
            try {
                final long written = conduit.writeSynchronously(encoded, byteCount);
                if (written != byteCount || encoded.size() != Normal._0) {
                    throw new SocketException("Socket Conduit did not fully consume the encoded frame");
                }
                return;
            } catch (final IOException e) {
                throw new SocketException("Unable to write socket frame", e);
            }
        }
        final long written = await(conduit.write(encoded, byteCount), timeout.write(), "Unable to write socket frame");
        if (written != byteCount || encoded.size() != Normal._0) {
            throw new SocketException("Socket Conduit did not fully consume the encoded frame");
        }
    }

    /**
     * Starts the optional completion-driven stream lane.
     *
     * @param target server handler
     */
    void startDataPlane(final Handler target) {
        startDataPlane(target, null);
    }

    /**
     * Starts the completion-driven stream lane with bytes consumed while probing transport metadata.
     *
     * @param target     server handler
     * @param prefetched prefetched application bytes, or {@code null}
     */
    void startDataPlane(final Handler target, final Buffer prefetched) {
        final Lock operation = enterTransportOperation("Socket data plane cannot start during TLS upgrade");
        try {
            if (connection == null || dataPlane != null) {
                throw new StatefulException("Socket completion data plane is unavailable");
            }
            final DataPlane created = new DataPlane(require(target, "Socket data-plane handler"), prefetched);
            dataPlane = created;
            if (created.input.size() == Normal._0) {
                created.read();
            } else {
                created.readCompleted(created.input.size(), null);
            }
        } finally {
            operation.unlock();
        }
    }

    /**
     * Sends one UDP datagram Call and verifies the accepted byte count.
     *
     * @param payload datagram payload
     */
    private void sendPacket(final Payload payload) {
        final int expected = Math.toIntExact(payload.length());
        final int written = datagram.sendDatagram(payload).execute();
        if (written != expected) {
            throw new SocketException("UDP session did not accept the complete socket datagram");
        }
    }

    /**
     * Sends all KCP packets currently released by the congestion window.
     *
     * @param packets outbound packets
     */
    private void sendKcpPackets(final List<KcpPacket> packets) {
        for (final KcpPacket packet : packets) {
            sendPacket(Payload.of(kcp.pack(packet)));
        }
    }

    /**
     * Runs one complete inbound socket pipeline.
     *
     * @return received message
     */
    private Message receiveNow() {
        return receiveNow(false);
    }

    /**
     * Runs the inbound pipeline, optionally returning the first decoded stream frame without queueing it.
     *
     * @param directFrame true only for the framework-owned exclusive server reader
     * @return received message
     */
    private Message receiveNow(final boolean directFrame) {
        final Lock operation = enterTransportOperation("Socket read cannot run during TLS upgrade");
        try {
            ensureOpen();
            PendingFrame pending = pollPending();
            if (pending == null) {
                if (connection != null) {
                    pending = readStreamFrames(directFrame);
                } else if (kcp == null) {
                    readPlainDatagram();
                } else {
                    awaitKcpFrame();
                }
                if (pending == null) {
                    pending = pollPending();
                }
            }
            if (pending == null) {
                throw new SocketException("Socket receive completed without a frame");
            }
            return deliver(pending);
        } catch (final RuntimeException e) {
            operationFailed(e);
            throw e;
        } catch (final Error e) {
            operationFailed(e);
            throw e;
        } finally {
            operation.unlock();
        }
    }

    /**
     * Reads stream chunks until the stateful codec produces at least one frame.
     *
     * @param directFrame true to return the first decoded frame directly
     * @return first decoded frame for direct mode, otherwise {@code null} after frames are queued
     */
    private PendingFrame readStreamFrames(final boolean directFrame) {
        long wireBytes = Normal._0;
        while (active()) {
            final Buffer input = streamReadBuffer == null ? new Buffer() : streamReadBuffer;
            final long read;
            final Conduit conduit = streamConduit();
            if (exclusiveReader && dataPlane == null && conduit instanceof Ingress) {
                try {
                    read = conduit.readSynchronously(input, socketOptions.readBufferSize());
                } catch (final IOException e) {
                    throw new SocketException("Unable to read socket frame", e);
                }
            } else {
                read = await(
                        conduit.read(input, socketOptions.readBufferSize()),
                        timeout.read(),
                        "Unable to read socket frame");
            }
            if (read < Normal._0) {
                throw new SocketException("Socket stream closed");
            }
            if (read == Normal._0) {
                if (!ThreadKit.sleep(Normal._1)) {
                    throw new CancellationException("Socket stream read interrupted");
                }
                continue;
            }
            wireBytes += read;
            if (directFrame) {
                if (decodeOwnedInto(input) > Normal._0) {
                    final ByteString first = decodedPayloads.removeFirst();
                    while (!decodedPayloads.isEmpty()) {
                        pendingFrames.addLast(new PendingFrame(decodedPayloads.removeFirst(), null, Normal.LONG_ZERO));
                    }
                    return new PendingFrame(first, null, wireBytes);
                }
                continue;
            }
            final List<Frame> frames = decode(input);
            if (!frames.isEmpty()) {
                enqueuePending(frames, null, wireBytes);
                return null;
            }
        }
        throw new StatefulException("Socket session closed while reading");
    }

    /**
     * Reads and decodes one plain UDP datagram.
     */
    private void readPlainDatagram() {
        final Message packet = datagram.receive().execute();
        final byte[] bytes = materialize(packet.payload(), "SocketSession.receive(UDP)");
        final List<Frame> frames = decode(new Buffer().write(bytes));
        if (frames.isEmpty()) {
            throw new SocketException("Socket datagram did not contain a complete frame");
        }
        enqueuePending(frames, packet.tag(), bytes.length);
    }

    /**
     * Waits for the KCP Pump to publish a complete decoded frame.
     */
    private void awaitKcpFrame() {
        while (active()) {
            if (hasPending()) {
                return;
            }
            cancellation.throwIfCancelled();
            if (!ThreadKit.sleep(Normal._1)) {
                throw new CancellationException("KCP receive interrupted");
            }
        }
        throw new StatefulException("Socket session closed while waiting for KCP");
    }

    /**
     * Decodes one input buffer under the stateful codec lock.
     *
     * @param input encoded input
     * @return decoded frames
     */
    private List<Frame> decode(final Buffer input) {
        synchronized (codec) {
            return codec.decodeFrames(input);
        }
    }

    /**
     * Decodes owned payloads into the reusable exclusive-reader queue.
     *
     * @param input newly received encoded bytes
     * @return number of complete payloads decoded
     */
    private int decodeOwnedInto(final Buffer input) {
        // Server data planes have exactly one serialized reader per session.
        return codec.decodeOwned(input, decodedPayloads);
    }

    /**
     * Delivers one decoded frame through the inbound Filter and Handler once.
     *
     * @param pending decoded frame awaiting delivery
     * @return delivered message
     */
    private Message deliver(final PendingFrame pending) {
        final Object tag = pending.tag() == null ? "socket-read" : pending.tag();
        final Message received = filter(Payload.owned(pending.payload()), tag);
        checkGuard(received);
        handler.message(this, received);
        if (pending.wireBytes() > Normal._0) {
            emit(ObservationMarker.SOCKET_READ, pending.wireBytes(), null);
        }
        touch();
        Logger.debug(
                false,
                "Fabric",
                "Socket receive completed: scheme={}, host={}, port={}, bytes={}",
                address.scheme(),
                address.host(),
                address.port(),
                pending.wireBytes());
        return received;
    }

    /**
     * Starts the single KCP receive Pump.
     */
    private void startKcpPump() {
        if (kcp == null) {
            return;
        }
        final DispatchHandle created = dispatcher
                .background("socket:kcp:pump", this, Activity.of("socket:kcp:pump", this::pumpKcp));
        if (!kcpHandle.compareAndSet(null, created)) {
            created.cancel();
            throw new StatefulException("KCP Pump can only be started once");
        }
        created.future().whenComplete((ignored, cause) -> kcpHandle.compareAndSet(created, null));
    }

    /**
     * Receives KCP datagrams, sends protocol outbound packets, and publishes complete frames.
     */
    private void pumpKcp() {
        try {
            while (active() && !cancellation.cancelled()) {
                final Message packetMessage = datagram.receive().execute();
                final KcpPacket packet = kcp.unpack(packetMessage.payload(), materializeMaxBytes);
                final KcpNetwork.Inbound inbound = kcp.receive(packet);
                sendKcpPackets(inbound.outbound());
                for (final Payload delivered : inbound.delivered()) {
                    final byte[] bytes = materialize(delivered, "SocketSession.kcpPump(Payload)");
                    final List<Frame> frames = decode(new Buffer().write(bytes));
                    if (frames.isEmpty()) {
                        throw new SocketException("KCP payload did not contain a complete socket frame");
                    }
                    enqueuePending(frames, packetMessage.tag(), bytes.length);
                }
                touch();
                scheduleRetransmission();
            }
        } catch (final RuntimeException e) {
            if (active()) {
                operationFailed(e);
            }
        } catch (final Error e) {
            if (active()) {
                operationFailed(e);
            }
            throw e;
        }
    }

    /**
     * Schedules one KCP retransmission pass while packets remain pending.
     */
    private void scheduleRetransmission() {
        if (kcp == null || kcp.pending() == Normal._0 || !active() || retransmitHandle.get() != null) {
            return;
        }
        final DispatchHandle created = dispatcher.schedule(
                "socket:kcp:retransmit",
                Builder.KCP_NETWORK_DEFAULT_RETRANSMIT_DELAY,
                Activity.of("socket:kcp:retransmit", this::retransmitKcp));
        if (!retransmitHandle.compareAndSet(null, created)) {
            created.cancel();
            return;
        }
        created.future().whenComplete((ignored, cause) -> retransmitHandle.compareAndSet(created, null));
    }

    /**
     * Sends due KCP packets and reschedules while the send window is non-empty.
     */
    private void retransmitKcp() {
        retransmitHandle.set(null);
        try {
            if (active()) {
                sendKcpPackets(kcp.retransmitDue());
                scheduleRetransmission();
            }
        } catch (final RuntimeException e) {
            operationFailed(e);
        } catch (final Error e) {
            operationFailed(e);
            throw e;
        }
    }

    /**
     * Builds a received message.
     *
     * @param payload received payload bytes
     * @param tag     direction or protocol tag attached to the message
     * @return message
     */
    private Message message(final Payload payload, final Object tag) {
        return Message
                .of(connection == null ? address.protocol() : Protocol.SOCKET, address, Headers.empty(), payload, tag);
    }

    /**
     * Checks the optional session guard against a socket message.
     *
     * @param message socket message to validate
     */
    private void checkGuard(final Message message) {
        if (messageGuard != null) {
            messageGuard.check(message).throwIfRejected();
        }
    }

    /**
     * Applies the optional session filter to a socket payload.
     *
     * @param payload socket payload to filter
     * @param tag     direction tag
     * @return filtered message
     */
    private Message filter(final Payload payload, final Object tag) {
        final Message message = message(payload, tag);
        return messageFilter == null ? message : FilterChain.apply(message, messageFilter);
    }

    /**
     * Emits a session observation event with a measured byte count.
     *
     * @param marker observation marker identifying the emitted event
     * @param bytes  measured bytes, or negative when absent
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final long bytes, final Throwable cause) {
        if (!observationEnabled) {
            return;
        }
        if (bytes < Normal._0) {
            scope.emit(marker, cause);
            return;
        }
        trafficBytes.set(bytes);
        try {
            scope.emit(marker, cause);
        } finally {
            trafficBytes.remove();
        }
    }

    /**
     * Adds the current encoded-wire byte count without replacing lifecycle tags or timestamps.
     *
     * @param event lifecycle event
     * @return event carrying the current wire byte count when present
     */
    private FabricEvent withTrafficBytes(final FabricEvent event) {
        final Long bytes = trafficBytes.get();
        if (bytes == null || (event.marker() != ObservationMarker.SOCKET_READ
                && event.marker() != ObservationMarker.SOCKET_WRITE)) {
            return event;
        }
        return new FabricEvent(event.marker(), event.time(), event.tags().with(Builder.TAG_BYTES, Long.toString(bytes)),
                event.cause());
    }

    /**
     * Ensures session is open.
     */
    private void ensureOpen() {
        if (!active()) {
            throw new StatefulException("Socket session is not open");
        }
        cancellation.throwIfCancelled();
    }

    /**
     * Acquires shared transport ownership for one ordinary stream or datagram operation.
     *
     * @param message failure message used when an upgrade owns the transport
     * @return acquired shared lock that the caller must release
     */
    private Lock enterTransportOperation(final String message) {
        final Lock operation = transportLock.readLock();
        if (!operation.tryLock()) {
            final StatefulException failure = new StatefulException(message);
            operationFailed(failure);
            throw failure;
        }
        if (tlsUpgradeState.get() == TlsUpgradeState.UPGRADING) {
            operation.unlock();
            final StatefulException failure = new StatefulException(message);
            operationFailed(failure);
            throw failure;
        }
        return operation;
    }

    /**
     * Performs the exclusive one-shot TLS upgrade.
     *
     * @param policy validated TLS policy
     * @return completed handshake metadata
     */
    private TlsHandshake upgradeTlsNow(final TlsPolicy policy) {
        final Lock upgrade = transportLock.writeLock();
        if (!upgrade.tryLock()) {
            throw rejectTlsUpgrade("TLS upgrade requires an idle transport");
        }
        TlsChannel channel = null;
        try {
            ensureOpen();
            if (!tlsUpgradeState.compareAndSet(TlsUpgradeState.PLAIN, TlsUpgradeState.UPGRADING)) {
                throw rejectTlsUpgrade("TLS upgrade is unavailable or has already been attempted");
            }
            requirePlaintextBoundary();
            final TlsEngine engine = exclusiveReader
                    ? TlsEngine.createServer(policy.context(), address, policy.settings())
                    : TlsEngine.create(policy.context(), address, policy.settings());
            channel = TlsChannel.wrap(streamConduit(), engine, null, dispatcher, timeout);
            upgradeChannel = channel;
            final TlsHandshake handshake = await(channel.handshake(), timeout.connect(), "Socket TLS upgrade failed");
            streamConduit = channel;
            tlsUpgradeState.set(TlsUpgradeState.SECURE);
            touch();
            return handshake;
        } catch (final RuntimeException | Error failure) {
            tlsUpgradeState.set(TlsUpgradeState.FAILED);
            if (channel != null && active()) {
                try {
                    channel.abort();
                } catch (final RuntimeException cleanup) {
                    failure.addSuppressed(cleanup);
                }
            }
            operationFailed(failure);
            throw failure;
        } finally {
            upgrade.unlock();
        }
    }

    /**
     * Rejects plaintext buffered beyond the protocol-approved StartTLS boundary.
     */
    private void requirePlaintextBoundary() {
        final boolean pending;
        synchronized (pendingFrames) {
            pending = !pendingFrames.isEmpty();
        }
        final boolean decoded;
        synchronized (decodedPayloads) {
            decoded = !decodedPayloads.isEmpty();
        }
        final boolean retainedRead = streamReadBuffer != null && streamReadBuffer.size() > Normal._0;
        final boolean retainedWrite = streamWriteBuffer != null && streamWriteBuffer.size() > Normal._0;
        if (dataPlane != null || pending || decoded || retainedRead || retainedWrite) {
            throw new StatefulException("TLS upgrade rejected unconsumed plaintext");
        }
    }

    /**
     * Returns the current stream conduit.
     *
     * @return active plaintext or TLS conduit
     */
    private Conduit streamConduit() {
        final Conduit current = streamConduit;
        if (current == null) {
            throw new StatefulException("Socket stream conduit is unavailable");
        }
        return current;
    }

    /**
     * Creates an upgrade rejection and closes the session before returning it.
     *
     * @param message rejection detail
     * @return terminal upgrade failure
     */
    private StatefulException rejectTlsUpgrade(final String message) {
        final StatefulException failure = new StatefulException(message);
        operationFailed(failure);
        return failure;
    }

    /**
     * Records socket activity.
     */
    private void touch() {
        if (!idleTrackingEnabled) {
            return;
        }
        lastActivityNanos = clock.nanos();
        scheduleIdle();
    }

    /**
     * Replaces the active idle deadline with one measured from the latest successful traffic.
     */
    private void scheduleIdle() {
        final Duration idle = socketOptions.idleTimeout();
        if (idle.isZero() || terminating.get() || !active()) {
            return;
        }
        scheduleIdle(idle, lastActivityNanos);
    }

    /**
     * Installs one idle deadline for an activity timestamp.
     *
     * @param delay         idle delay before checking the captured activity time
     * @param activityNanos activity timestamp guarded by the deadline
     */
    private void scheduleIdle(final Duration delay, final long activityNanos) {
        final DispatchHandle created = dispatcher.schedule(
                "socket:session:idle",
                delay,
                Activity.of("socket:session:idle", () -> idleExpired(activityNanos)));
        final DispatchHandle previous = idleHandle.getAndSet(created);
        if (previous != null) {
            previous.cancel();
        }
        created.future().whenComplete((ignored, cause) -> idleHandle.compareAndSet(created, null));
        if (terminating.get() || !active()) {
            cancelHandle(idleHandle);
        }
    }

    /**
     * Actively closes an idle session or rechecks a deadline reached early by a custom clock.
     *
     * @param activityNanos activity timestamp guarded by this check
     */
    private void idleExpired(final long activityNanos) {
        if (!active() || activityNanos != lastActivityNanos) {
            return;
        }
        final long idleNanos = durationNanos(socketOptions.idleTimeout());
        final long elapsed = Math.max(Normal.LONG_ZERO, clock.nanos() - activityNanos);
        if (elapsed < idleNanos) {
            scheduleIdle(Duration.ofNanos(idleNanos - elapsed), activityNanos);
            return;
        }
        Logger.debug(
                false,
                "Fabric",
                "Socket idle timeout reached: scheme={}, host={}, port={}, idleTimeout={}",
                address.scheme(),
                address.host(),
                address.port(),
                socketOptions.idleTimeout());
        terminate(Termination.CLOSE, null);
    }

    /**
     * Waits for one bottom-level operation using the configured stage timeout.
     *
     * @param future  bottom-level future
     * @param limit   timeout limit; zero means no explicit deadline
     * @param message failure context
     * @param <T>     result type
     * @return operation result
     */
    private <T> T await(final CompletableFuture<T> future, final Duration limit, final String message) {
        cancellation.throwIfCancelled();
        try {
            if (future.isDone()) {
                return future.join();
            }
            return limit.isZero() ? future.get() : future.get(limit.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException(message + ": interrupted", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new InternalException(message, cause);
        } catch (final CancellationException e) {
            throw e;
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new InternalException(message, cause);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException(message + ": timed out", e);
        } catch (final ArithmeticException e) {
            throw new ValidateException("Socket timeout is too large");
        }
    }

    /**
     * Terminates the session after cancelling background work and unblocking the active transport operation.
     *
     * @param termination requested terminal state
     * @param cause       terminal cause when cancelling or failing
     * @return true when this invocation owned termination
     */
    private boolean terminate(final Termination termination, final Throwable cause) {
        if (!terminating.compareAndSet(false, true)) {
            return false;
        }
        scope.closing();
        final Throwable terminalCause = cause == null ? new CancellationException("Socket session closed") : cause;
        cancellation.cancel(terminalCause);
        cancelHandle(kcpHandle);
        cancelHandle(retransmitHandle);
        cancelHandle(idleHandle);

        RuntimeException cleanupFailure = null;
        try {
            closeResources(termination == Termination.CLOSE && upgradeChannel == null);
        } catch (final RuntimeException e) {
            cleanupFailure = e;
        }
        if (ownsDispatcher) {
            try {
                ThreadKit.execute(this::closeDispatcher);
            } catch (final RuntimeException e) {
                cleanupFailure = append(cleanupFailure, e);
            }
        }

        final Termination effective = cleanupFailure != null && termination == Termination.CLOSE ? Termination.FAIL
                : termination;
        final Throwable effectiveCause = cause == null && cleanupFailure != null ? cleanupFailure : terminalCause;
        final boolean changed;
        if (effective == Termination.CLOSE) {
            changed = scope.close(this);
            notifyClosed();
        } else if (effective == Termination.CANCEL) {
            changed = scope.cancel(effectiveCause);
            notifyFailure(effectiveCause);
        } else {
            changed = scope.fail(effectiveCause);
            notifyFailure(effectiveCause);
        }
        if (cleanupFailure != null && cleanupFailure != effectiveCause) {
            effectiveCause.addSuppressed(cleanupFailure);
        }
        return changed;
    }

    /**
     * Fails the session without allowing cleanup errors to replace the operation failure.
     *
     * @param cause operation failure
     */
    private void operationFailed(final Throwable cause) {
        try {
            terminate(Termination.FAIL, cause);
        } catch (final RuntimeException cleanup) {
            cause.addSuppressed(cleanup);
        }
    }

    /**
     * Notifies the message handler about a terminal failure without destabilizing cleanup.
     *
     * @param cause terminal failure
     */
    private void notifyFailure(final Throwable cause) {
        try {
            handler.failure(this, cause);
        } catch (final RuntimeException e) {
            scope.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Notifies the message handler about normal close without destabilizing cleanup.
     */
    private void notifyClosed() {
        try {
            handler.closed(this);
        } catch (final RuntimeException e) {
            scope.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Closes an internally created dispatcher outside its currently completing activity.
     */
    private void closeDispatcher() {
        try {
            dispatcher.close();
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "Socket dispatcher close failed: scheme={}, host={}, port={}, exception={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Cancels and clears one owned dispatch handle.
     *
     * @param reference handle reference
     */
    private static void cancelHandle(final AtomicReference<DispatchHandle> reference) {
        final DispatchHandle handle = reference.getAndSet(null);
        if (handle != null) {
            handle.cancel();
        }
    }

    /**
     * Closes owned resources.
     *
     * @param reusable true when a connection lease may be returned to its pool
     */
    private void closeResources(final boolean reusable) {
        clearPendingFrames();
        RuntimeException failure = null;
        try {
            codec.reset();
        } catch (final RuntimeException e) {
            failure = e;
        }
        final TlsChannel tls = upgradeChannel;
        if (tls != null) {
            try {
                if (reusable) {
                    tls.close();
                } else {
                    tls.abort();
                }
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        if (owner instanceof SocketLease.Owner lease) {
            try {
                if (reusable) {
                    lease.release();
                } else {
                    lease.close();
                }
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        } else if (connection != null) {
            try {
                connection.close();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        if (datagram != null) {
            try {
                datagram.close();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        if (kcp != null) {
            try {
                kcp.close();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        if (owner != null && !(owner instanceof SocketLease.Owner)) {
            try {
                owner.close();
            } catch (final Exception e) {
                failure = append(failure, new InternalException("Unable to close socket owner", e));
            }
        }
        if (failure != null) {
            Logger.warn(
                    false,
                    "Fabric",
                    failure,
                    "Socket resource close failed: scheme={}, host={}, port={}, reusable={}, exception={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    reusable,
                    failure.getClass().getSimpleName());
            throw failure;
        }
    }

    /**
     * Caches decoded frames that do not yet have a matching receive Call.
     *
     * @param frames    decoded frames
     * @param tag       source tag
     * @param wireBytes encoded bytes consumed to produce the frames
     */
    private void enqueuePending(final List<Frame> frames, final Object tag, final long wireBytes) {
        synchronized (pendingFrames) {
            boolean first = true;
            for (final Frame frame : frames) {
                pendingFrames.addLast(new PendingFrame(frame.payload(), tag, first ? wireBytes : Normal.LONG_ZERO));
                first = false;
            }
            Logger.debug(
                    false,
                    "Fabric",
                    "Socket pending frames queued: scheme={}, host={}, port={}, queued={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    pendingFrames.size());
        }
    }

    /**
     * Removes the next decoded frame.
     *
     * @return pending frame or null
     */
    private PendingFrame pollPending() {
        synchronized (pendingFrames) {
            return pendingFrames.pollFirst();
        }
    }

    /**
     * Returns whether the KCP Pump has published a decoded frame.
     *
     * @return true when at least one frame is pending
     */
    private boolean hasPending() {
        synchronized (pendingFrames) {
            return !pendingFrames.isEmpty();
        }
    }

    /**
     * Clears locally decoded but undelivered frames.
     */
    private void clearPendingFrames() {
        synchronized (pendingFrames) {
            pendingFrames.clear();
        }
    }

    /**
     * Materializes a payload through the configured session limit.
     *
     * @param payload   payload to materialize
     * @param operation diagnostic operation name used when reporting limit failures
     * @return payload bytes
     */
    private byte[] materialize(final Payload payload, final String operation) {
        try {
            return Payload.materialize(payload, materializeMaxBytes, operation);
        } catch (final RuntimeException e) {
            throw new SocketException("Unable to materialize socket payload for " + operation, e);
        }
    }

    /**
     * Reuses a repeatable payload owner when its declared size is within the configured limit.
     *
     * @param payload   payload to snapshot
     * @param operation diagnostic operation name used when reporting limit failures
     * @return immutable payload owner
     */
    private ByteString snapshot(final Payload payload, final String operation) {
        final long length = payload.length();
        if (payload.repeatable() && length >= Normal.LONG_ZERO && length <= materializeMaxBytes
                && length <= Integer.MAX_VALUE) {
            final ByteString owned = payload.ownedBytes();
            if (owned.size() == length) {
                return owned;
            }
        }
        return ByteString.of(materialize(payload, operation));
    }

    /**
     * Converts a duration to nanoseconds while treating an overflowing positive duration as effectively unbounded.
     *
     * @param duration duration to convert
     * @return duration nanoseconds
     */
    private static long durationNanos(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Appends a cleanup failure without discarding the first failure.
     *
     * @param current first recorded failure, or {@code null}
     * @param next    additional cleanup failure to append
     * @return aggregate failure
     */
    private static RuntimeException append(final RuntimeException current, final RuntimeException next) {
        if (current == null) {
            return next;
        }
        if (current != next) {
            current.addSuppressed(next);
        }
        return current;
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Decoded frame plus its original transport metadata and encoded byte count.
     *
     * @param payload   decoded frame payload
     * @param tag       transport tag
     * @param wireBytes encoded bytes attributed to this frame
     */
    private record PendingFrame(ByteString payload, Object tag, long wireBytes) {

        /**
         * Creates a validated pending frame.
         *
         * @param payload   decoded frame payload
         * @param tag       transport tag
         * @param wireBytes encoded bytes attributed to this frame
         */
        private PendingFrame {
            payload = require(payload, "Pending socket frame payload");
            if (wireBytes < Normal.LONG_ZERO) {
                throw new ValidateException("Pending socket wire bytes must be non-negative");
            }
        }

    }

    /**
     * Per-session callback lane that reuses the normal codec, message pipeline, connection and lifecycle.
     */
    private final class DataPlane {

        /**
         * Server handler receiving decoded messages and terminal failures.
         */
        private final Handler target;

        /**
         * Retained inbound bytes, including application bytes prefetched during PROXY probing.
         */
        private final Buffer input = new Buffer();

        /**
         * Retained output buffer used when no earlier write is pending.
         */
        private final Buffer output = new Buffer();

        /**
         * Reusable read completion callback.
         */
        private final BiConsumerX<Long, Throwable> readCompletion = this::readCompleted;

        /**
         * Reusable write completion callback.
         */
        private final BiConsumerX<Long, Throwable> writeCompletion = this::writeCompleted;

        /**
         * Lazily allocated expected sizes for writes following the active write.
         */
        private ArrayDeque<Long> additionalWrites;

        /**
         * Expected byte count of the active write.
         */
        private long expectedWrite;

        /**
         * Number of writes submitted but not yet completed.
         */
        private int pendingWrites;

        /**
         * Whether the current inbound handler dispatch has returned.
         */
        private boolean dispatchComplete;

        /**
         * Whether this data plane has entered its terminal failure path.
         */
        private boolean failed;

        /**
         * Creates a serialized data plane.
         *
         * @param target     server handler
         * @param prefetched application bytes consumed during transport metadata probing
         */
        private DataPlane(final Handler target, final Buffer prefetched) {
            this.target = target;
            if (prefetched != null && prefetched.size() > Normal._0) {
                input.write(prefetched, prefetched.size());
            }
        }

        /**
         * Returns the enclosing session for thread-local ownership checks.
         *
         * @return enclosing socket session
         */
        private SocketSession session() {
            return SocketSession.this;
        }

        /**
         * Submits the next callback read while the session remains active.
         */
        private void read() {
            if (active()) {
                streamConduit().read(input, socketOptions.readBufferSize(), readCompletion);
            }
        }

        /**
         * Decodes and dispatches one callback read completion.
         *
         * @param count completed byte count
         * @param cause terminal read failure
         */
        private void readCompleted(final Long count, final Throwable cause) {
            if (cause != null || count == null || count < Normal._0) {
                fail(cause == null ? new SocketException("Socket stream closed") : cause);
                return;
            }
            if (count == Normal._0) {
                read();
                return;
            }
            try {
                if (decodeOwnedInto(input) == Normal._0) {
                    read();
                    return;
                }
                synchronized (this) {
                    dispatchComplete = false;
                }
                ACTIVE_DATA_PLANE.set(this);
                try {
                    boolean first = true;
                    while (!decodedPayloads.isEmpty()) {
                        final ByteString frame = decodedPayloads.removeFirst();
                        final Message message = deliver(
                                new PendingFrame(frame, null, first ? count : Normal.LONG_ZERO));
                        target.message(SocketSession.this, message);
                        first = false;
                    }
                } finally {
                    ACTIVE_DATA_PLANE.remove();
                }
                synchronized (this) {
                    dispatchComplete = true;
                    if (pendingWrites != Normal._0 || failed) {
                        return;
                    }
                }
                read();
            } catch (final RuntimeException | Error error) {
                fail(error);
            }
        }

        /**
         * Encodes and submits one serialized handler response.
         *
         * @param payload immutable response payload
         */
        private void write(final ByteString payload) {
            final Buffer encoded;
            synchronized (this) {
                encoded = pendingWrites == Normal._0 ? output : new Buffer();
            }
            // The handler and its writes run on this session's serialized completion lane.
            codec.encodeOwned(payload, encoded);
            final long expected = encoded.size();
            synchronized (this) {
                if (failed) {
                    throw new StatefulException("Socket completion data plane is failed");
                }
                if (pendingWrites == Normal._0) {
                    expectedWrite = expected;
                } else {
                    if (additionalWrites == null) {
                        additionalWrites = new ArrayDeque<>();
                    }
                    additionalWrites.addLast(expected);
                }
                pendingWrites++;
            }
            streamConduit().write(encoded, expected, writeCompletion);
        }

        /**
         * Verifies one callback write completion and resumes reads when all responses finish.
         *
         * @param count completed byte count
         * @param cause terminal write failure
         */
        private void writeCompleted(final Long count, final Throwable cause) {
            final long expected;
            final boolean resume;
            synchronized (this) {
                expected = expectedWrite;
                if (cause == null && count != null && count.longValue() == expected) {
                    pendingWrites--;
                    expectedWrite = pendingWrites == Normal._0 ? Normal.LONG_ZERO : additionalWrites.removeFirst();
                    resume = dispatchComplete && pendingWrites == Normal._0;
                } else {
                    resume = false;
                }
            }
            if (cause != null || count == null || count.longValue() != expected) {
                fail(cause == null ? new SocketException("Socket stream write was incomplete") : cause);
                return;
            }
            emit(ObservationMarker.SOCKET_WRITE, expected, null);
            touch();
            if (resume) {
                read();
            }
        }

        /**
         * Terminates the data plane and notifies the handler exactly once.
         *
         * @param cause terminal transport or handler failure
         */
        private void fail(final Throwable cause) {
            synchronized (this) {
                if (failed) {
                    return;
                }
                failed = true;
            }
            final Throwable failure = cause == null ? new SocketException("Socket completion data plane failed")
                    : cause;
            operationFailed(failure);
            try {
                target.failure(SocketSession.this, failure);
            } catch (final RuntimeException ignored) {
                // The transport failure remains authoritative.
            }
        }

    }

    /**
     * One-shot state of the optional in-session TLS transition.
     */
    private enum TlsUpgradeState {

        /**
         * Plaintext stream is eligible for its one permitted upgrade attempt.
         */
        PLAIN,

        /**
         * TLS handshake exclusively owns the stream conduit.
         */
        UPGRADING,

        /**
         * TLS protects every subsequent stream operation.
         */
        SECURE,

        /**
         * Datagram transport does not support an in-session TLS transition.
         */
        UNAVAILABLE,

        /**
         * The sole upgrade attempt failed and the session is terminal.
         */
        FAILED

    }

    /**
     * Session terminal path selected by the owner of the termination guard.
     */
    private enum Termination {

        /**
         * Normal close with reusable connection ownership.
         */
        CLOSE,

        /**
         * Explicit cancellation with non-reusable ownership.
         */
        CANCEL,

        /**
         * Failure with non-reusable ownership.
         */
        FAIL

    }

}
