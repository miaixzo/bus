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
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.kcp.KcpNetwork;
import org.miaixz.bus.fabric.network.kcp.KcpPacket;
import org.miaixz.bus.fabric.network.udp.UdpSession;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Demuxer;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.socket.body.SocketBody;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketFrame;
import org.miaixz.bus.fabric.protocol.socket.session.SocketLease;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Open socket session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketSession implements Session {

    /**
     * Remote address.
     */
    private final Address address;

    /**
     * Network connection.
     */
    private final Connection connection;

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
     * Attributes.
     */
    private final Map<String, Object> attributes;

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
     * Per-emission wire byte count consumed by the observer decorator.
     */
    private final ThreadLocal<Long> trafficBytes;

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
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

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
        this(address, connection, codec, handler, attributes, owner, listener, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
        this(address, datagram, kcp, codec, handler, attributes, owner, listener,
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
        this(address, connection, datagram, kcp, codec, handler, attributes, owner, listener,
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
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
                socketOptions, Dispatcher.create(), Clock.system(), Timeout.defaults(), Cancellation.create(), true);
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
                socketOptions, dispatcher, clock, timeout, cancellation, false);
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
     */
    private SocketSession(final Address address, final Connection connection, final UdpSession datagram,
            final KcpNetwork kcp, final SocketCodec codec, final Handler handler, final Map<String, Object> attributes,
            final AutoCloseable owner, final Listener<? super SocketSession> listener, final long materializeMaxBytes,
            final SocketOptions socketOptions, final Dispatcher dispatcher, final Clock clock, final Timeout timeout,
            final Cancellation cancellation, final boolean ownsDispatcher) {
        this.address = require(address, "Socket address");
        if (connection == null && datagram == null) {
            throw new ValidateException("Socket transport must not be null");
        }
        this.connection = connection;
        this.datagram = datagram;
        this.kcp = kcp;
        this.codec = require(codec, "Socket codec");
        this.handler = handler == null ? Demuxer.noop() : handler;
        this.pendingFrames = new ArrayDeque<>();
        this.attributes = new LinkedHashMap<>(attributes == null ? Map.of() : attributes);
        this.owner = owner;
        final Object configuredObserver = this.attributes.get(Builder.ATTRIBUTE_OBSERVER);
        final EventObserver sink = configuredObserver instanceof EventObserver current ? EventObserver.safe(current)
                : EventObserver.noop();
        this.trafficBytes = new ThreadLocal<>();
        this.observer = event -> sink.emit(withTrafficBytes(event));
        this.dispatcher = require(dispatcher, "Socket dispatcher");
        this.clock = require(clock, "Socket clock");
        this.timeout = require(timeout, "Socket timeout");
        this.cancellation = require(cancellation, "Socket cancellation");
        this.ownsDispatcher = ownsDispatcher;
        this.scope = LifecycleScope.session(
                this,
                "socket-session",
                listener,
                this.observer,
                ObservationMarker.SOCKET_OPEN,
                ObservationMarker.SOCKET_CLOSED,
                ObservationMarker.SOCKET_FAILED,
                this.clock);
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        this.socketOptions = socketOptions == null ? SocketOptions.defaults() : socketOptions;
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
    public Status state() {
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
        return MonoCall.<Void>create(
                "socket-session-send",
                "socket:session:send",
                dispatcher,
                observer,
                null,
                () -> sendNow(current),
                this::cancel);
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
                this::receiveNow,
                this::cancel);
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
        try {
            ensureOpen();
            final Message outgoing = filter(source, "socket-write");
            checkGuard(outgoing);
            final byte[] bytes = materialize(outgoing.payload(), "SocketSession.send(Payload)");
            final Buffer encoded = new Buffer();
            synchronized (codec) {
                codec.encode(SocketFrame.of(ByteString.of(bytes)), encoded);
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
            if (connection != null) {
                writeStream(encoded, wireBytes);
            } else {
                final byte[] datagramBytes = encoded.readByteArray();
                if (kcp == null) {
                    sendPacket(Payload.of(datagramBytes));
                } else {
                    sendKcpPackets(kcp.encode(Payload.of(datagramBytes)));
                    scheduleRetransmission();
                }
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
        }
    }

    /**
     * Writes one encoded frame through the complete Conduit contract.
     *
     * @param encoded   encoded frame bytes to write
     * @param byteCount encoded byte count
     */
    private void writeStream(final Buffer encoded, final long byteCount) {
        final long written = await(
                connection.conduit().write(encoded, byteCount),
                timeout.write(),
                "Unable to write socket frame");
        if (written != byteCount || encoded.size() != Normal._0) {
            throw new SocketException("Socket Conduit did not fully consume the encoded frame");
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
        try {
            ensureOpen();
            PendingFrame pending = pollPending();
            if (pending == null) {
                if (connection != null) {
                    readStreamFrames();
                } else if (kcp == null) {
                    readPlainDatagram();
                } else {
                    awaitKcpFrame();
                }
                pending = pollPending();
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
        }
    }

    /**
     * Reads stream chunks until the stateful codec produces at least one frame.
     */
    private void readStreamFrames() {
        long wireBytes = Normal._0;
        while (opened()) {
            final Buffer input = new Buffer();
            final long read = await(
                    connection.conduit().read(input, socketOptions.readBufferSize()),
                    timeout.read(),
                    "Unable to read socket frame");
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
            final List<SocketFrame> frames = decode(input);
            if (!frames.isEmpty()) {
                enqueuePending(frames, null, wireBytes);
                return;
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
        final List<SocketFrame> frames = decode(new Buffer().write(bytes));
        if (frames.isEmpty()) {
            throw new SocketException("Socket datagram did not contain a complete frame");
        }
        enqueuePending(frames, packet.tag(), bytes.length);
    }

    /**
     * Waits for the KCP Pump to publish a complete decoded frame.
     */
    private void awaitKcpFrame() {
        while (opened()) {
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
    private List<SocketFrame> decode(final Buffer input) {
        synchronized (codec) {
            return codec.decode(input);
        }
    }

    /**
     * Delivers one decoded frame through the inbound Filter and Handler once.
     *
     * @param pending decoded frame awaiting delivery
     * @return delivered message
     */
    private Message deliver(final PendingFrame pending) {
        final Object tag = pending.tag() == null ? "socket-read" : pending.tag();
        final Message received = filter(Payload.of(pending.frame().payload()), tag);
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
            while (opened() && !cancellation.cancelled()) {
                final Message packetMessage = datagram.receive().execute();
                final KcpPacket packet = kcp.unpack(packetMessage.payload(), materializeMaxBytes);
                final KcpNetwork.Inbound inbound = kcp.receive(packet);
                sendKcpPackets(inbound.outbound());
                for (final Payload delivered : inbound.delivered()) {
                    final byte[] bytes = materialize(delivered, "SocketSession.kcpPump(Payload)");
                    final List<SocketFrame> frames = decode(new Buffer().write(bytes));
                    if (frames.isEmpty()) {
                        throw new SocketException("KCP payload did not contain a complete socket frame");
                    }
                    enqueuePending(frames, packetMessage.tag(), bytes.length);
                }
                touch();
                scheduleRetransmission();
            }
        } catch (final RuntimeException e) {
            if (opened()) {
                operationFailed(e);
            }
        } catch (final Error e) {
            if (opened()) {
                operationFailed(e);
            }
            throw e;
        }
    }

    /**
     * Schedules one KCP retransmission pass while packets remain pending.
     */
    private void scheduleRetransmission() {
        if (kcp == null || kcp.pending() == Normal._0 || !opened() || retransmitHandle.get() != null) {
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
            if (opened()) {
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
        final Object value = attributes.get(Builder.ATTRIBUTE_GUARD);
        if (value instanceof GuardRule current) {
            current.check(message).throwIfRejected();
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
        final Object value = attributes.get(Builder.ATTRIBUTE_FILTER);
        return value instanceof Filter current ? FilterChain.apply(message, current) : message;
    }

    /**
     * Emits a session observation event with a measured byte count.
     *
     * @param marker observation marker identifying the emitted event
     * @param bytes  measured bytes, or negative when absent
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final long bytes, final Throwable cause) {
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
        if (!opened()) {
            throw new StatefulException("Socket session is not open");
        }
        cancellation.throwIfCancelled();
    }

    /**
     * Records socket activity.
     */
    private void touch() {
        lastActivityNanos = clock.nanos();
        scheduleIdle();
    }

    /**
     * Replaces the active idle deadline with one measured from the latest successful traffic.
     */
    private void scheduleIdle() {
        final Duration idle = socketOptions.idleTimeout();
        if (idle.isZero() || terminating.get() || !opened()) {
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
        if (terminating.get() || !opened()) {
            cancelHandle(idleHandle);
        }
    }

    /**
     * Actively closes an idle session or rechecks a deadline reached early by a custom clock.
     *
     * @param activityNanos activity timestamp guarded by this check
     */
    private void idleExpired(final long activityNanos) {
        if (!opened() || activityNanos != lastActivityNanos) {
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
            closeResources(termination == Termination.CLOSE);
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
    private void enqueuePending(final List<SocketFrame> frames, final Object tag, final long wireBytes) {
        synchronized (pendingFrames) {
            boolean first = true;
            for (final SocketFrame frame : frames) {
                pendingFrames.addLast(new PendingFrame(frame, tag, first ? wireBytes : Normal.LONG_ZERO));
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
     * @param frame     decoded frame
     * @param tag       transport tag
     * @param wireBytes encoded bytes attributed to this frame
     */
    private record PendingFrame(SocketFrame frame, Object tag, long wireBytes) {

        /**
         * Creates a validated pending frame.
         *
         * @param frame     decoded frame
         * @param tag       transport tag
         * @param wireBytes encoded bytes attributed to this frame
         */
        private PendingFrame {
            frame = require(frame, "Pending socket frame");
            if (wireBytes < Normal.LONG_ZERO) {
                throw new ValidateException("Pending socket wire bytes must be non-negative");
            }
        }

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
