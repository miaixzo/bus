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
package org.miaixz.bus.fabric.protocol.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
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
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.websocket.body.WebSocketBody;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketFrame;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketReader;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketWriter;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Open WebSocket session that owns frame ordering, message aggregation, control handling, and terminal notification.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketSession implements Session {

    /**
     * Milliseconds between non-blocking completion checks.
     */
    private static final long ENTRY_WAIT_MILLIS = Normal._1;

    /**
     * Session address.
     */
    private final Address address;

    /**
     * Endpoint role.
     */
    private final WebSocketRole role;

    /**
     * Single-frame writer.
     */
    private final WebSocketWriter writer;

    /**
     * Single-frame reader.
     */
    private final WebSocketReader reader;

    /**
     * Output sink flushed after each complete outbound entry.
     */
    private final Sink output;

    /**
     * Native connection lease.
     */
    private final ConnectionLease lease;

    /**
     * Optional owner closed with native resources.
     */
    private final AutoCloseable owner;

    /**
     * User message and terminal handler.
     */
    private final Handler handler;

    /**
     * Session attributes.
     */
    private final Map<String, Object> attributes;

    /**
     * Dispatcher used by the reader, drain, ping, and close deadline.
     */
    private final Dispatcher dispatcher;

    /**
     * Whether this session owns its compatibility dispatcher.
     */
    private final boolean ownsDispatcher;

    /**
     * Session dispatch key.
     */
    private final String dispatchKey;

    /**
     * Session clock.
     */
    private final Clock clock;

    /**
     * Close handshake timeout.
     */
    private final Duration closeTimeout;

    /**
     * Automatic ping interval.
     */
    private final Duration pingInterval;

    /**
     * Shared cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Cancellation callback removal handle.
     */
    private final AtomicReference<Runnable> cancellationRegistration;

    /**
     * Native reader dispatch handle.
     */
    private final AtomicReference<DispatchHandle> readerHandle;

    /**
     * Outbound drain dispatch handle.
     */
    private final AtomicReference<DispatchHandle> drainHandle;

    /**
     * Automatic ping dispatch handle.
     */
    private final AtomicReference<DispatchHandle> pingHandle;

    /**
     * Close-timeout dispatch handle.
     */
    private final AtomicReference<DispatchHandle> closeTimeoutHandle;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Optional guard.
     */
    private final GuardRule guard;

    /**
     * Optional message filter.
     */
    private final Filter filter;

    /**
     * Event observer enriched with complete WebSocket wire byte counts.
     */
    private final EventObserver observer;

    /**
     * Per-emission complete WebSocket wire byte count.
     */
    private final ThreadLocal<Long> trafficBytes;

    /**
     * Maximum bytes allowed when materializing session payloads.
     */
    private final long materializeMaxBytes;

    /**
     * Outbound queue monitor.
     */
    private final Object outboundLock;

    /**
     * Ordered outbound entries not yet active.
     */
    private final ArrayDeque<OutboundEntry> outbound;

    /**
     * Complete reserved wire bytes across queued and active entries.
     */
    private long queuedBytes;

    /**
     * Guard allowing at most one active background drain.
     */
    private final AtomicBoolean draining;

    /**
     * Currently active outbound entry.
     */
    private final AtomicReference<OutboundEntry> activeEntry;

    /**
     * Guard allowing exactly one terminal notification path.
     */
    private final AtomicBoolean terminalNotified;

    /**
     * Guard allowing exactly one native resource cleanup.
     */
    private final AtomicBoolean resourcesClosed;

    /**
     * Guard allowing exactly one close entry.
     */
    private final AtomicBoolean closeQueued;

    /**
     * Whether a close frame was physically flushed.
     */
    private final AtomicBoolean closeWritten;

    /**
     * Whether a peer close frame was received.
     */
    private final AtomicBoolean peerCloseReceived;

    /**
     * Failure delivered after the best-effort protocol close frame.
     */
    private final AtomicReference<Throwable> failureAfterClose;

    /**
     * Automatic ping awaiting pong flag.
     */
    private final AtomicBoolean awaitingPong;

    /**
     * Whether one automatic ping is queued but not yet flushed.
     */
    private final AtomicBoolean automaticPingPending;

    /**
     * Sent ping count.
     */
    private final AtomicInteger sentPingCount;

    /**
     * Received ping count.
     */
    private final AtomicInteger receivedPingCount;

    /**
     * Received pong count.
     */
    private final AtomicInteger receivedPongCount;

    /**
     * Creates a transport-less session for validated upgrade snapshots.
     *
     * @param address session address
     */
    WebSocketSession(final Address address) {
        this(address, null, null, null, null, null, null, null, false, defaultDispatchKey(address), Clock.system(),
                Duration.ZERO, Builder.DURATION_60_SECONDS, null, WebSocketRole.CLIENT,
                defaultAttributes(EventObserver.noop()), null, EventObserver.noop(), null, Cancellation.create(),
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened compatibility session with an owned dispatcher.
     *
     * @param address session address
     * @param writer  native writer
     * @param reader  native reader
     * @param lease   native lease
     * @param handler native handler
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler) {
        this(address, writer, reader, null, lease, null, handler, Dispatcher.create(), true,
                defaultDispatchKey(address), Clock.system(), Duration.ZERO, Builder.DURATION_60_SECONDS, null,
                WebSocketRole.CLIENT, defaultAttributes(EventObserver.noop()), null, EventObserver.noop(), null,
                Cancellation.create(), Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened compatibility session.
     *
     * @param address     session address
     * @param writer      native writer
     * @param reader      native reader
     * @param lease       native lease
     * @param handler     native handler
     * @param dispatcher  dispatcher for background work
     * @param dispatchKey dispatch key
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler, final Dispatcher dispatcher, final String dispatchKey) {
        this(address, writer, reader, null, lease, null, handler, dispatcher, false, dispatchKey, Clock.system(),
                Duration.ZERO, Builder.DURATION_60_SECONDS, null, WebSocketRole.CLIENT,
                defaultAttributes(EventObserver.noop()), null, EventObserver.noop(), null, Cancellation.create(),
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened compatibility session with lifecycle collaborators.
     *
     * @param address     session address
     * @param writer      native writer
     * @param reader      native reader
     * @param lease       native lease
     * @param handler     native handler
     * @param dispatcher  dispatcher for background work
     * @param dispatchKey dispatch key
     * @param ping        ping interval
     * @param guard       optional guard
     * @param observer    observer
     * @param listener    lifecycle listener
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler, final Dispatcher dispatcher, final String dispatchKey,
            final Duration ping, final GuardRule guard, final EventObserver observer,
            final Listener<? super WebSocketSession> listener) {
        this(address, writer, reader, null, lease, null, handler, dispatcher, false, dispatchKey, Clock.system(), ping,
                Builder.DURATION_60_SECONDS, guard, WebSocketRole.CLIENT, defaultAttributes(observer), null, observer,
                listener, Cancellation.create(), Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened compatibility session with filtering.
     *
     * @param address             session address
     * @param writer              native writer
     * @param reader              native reader
     * @param lease               native lease
     * @param handler             native handler
     * @param dispatcher          dispatcher for background work
     * @param dispatchKey         dispatch key
     * @param ping                ping interval
     * @param guard               optional guard
     * @param filter              optional filter
     * @param observer            observer
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler, final Dispatcher dispatcher, final String dispatchKey,
            final Duration ping, final GuardRule guard, final Filter filter, final EventObserver observer,
            final Listener<? super WebSocketSession> listener, final long materializeMaxBytes) {
        this(address, writer, reader, null, lease, null, handler, dispatcher, false, dispatchKey, Clock.system(), ping,
                Builder.DURATION_60_SECONDS, guard, WebSocketRole.CLIENT, defaultAttributes(observer), filter, observer,
                listener, Cancellation.create(), materializeMaxBytes);
    }

    /**
     * Creates an opened compatibility session with an explicit endpoint role and owner.
     *
     * @param address             session address
     * @param writer              native writer
     * @param reader              native reader
     * @param lease               native lease
     * @param handler             native handler
     * @param dispatcher          dispatcher for background work
     * @param dispatchKey         dispatch key
     * @param ping                ping interval
     * @param guard               optional guard
     * @param role                endpoint role
     * @param attributes          attributes
     * @param owner               owner closed with native resources
     * @param filter              optional filter
     * @param observer            observer
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler, final Dispatcher dispatcher, final String dispatchKey,
            final Duration ping, final GuardRule guard, final WebSocketRole role, final Map<String, Object> attributes,
            final AutoCloseable owner, final Filter filter, final EventObserver observer,
            final Listener<? super WebSocketSession> listener, final long materializeMaxBytes) {
        this(address, writer, reader, null, lease, owner, handler, dispatcher, false, dispatchKey, Clock.system(), ping,
                Builder.DURATION_60_SECONDS, guard, role, attributes, filter, observer, listener, Cancellation.create(),
                materializeMaxBytes);
    }

    /**
     * Creates the final source-and-sink owned session used by WebSocket runners.
     *
     * @param address      session address
     * @param source       underlying source
     * @param sink         underlying sink
     * @param lease        native lease
     * @param handler      native handler
     * @param context      runtime context
     * @param timeout      timeout policy
     * @param dispatchKey  dispatch key
     * @param guard        optional guard
     * @param role         endpoint role
     * @param attributes   attributes
     * @param owner        owner closed with native resources
     * @param filter       optional filter
     * @param observer     observer
     * @param listener     lifecycle listener
     * @param cancellation shared cancellation scope
     */
    WebSocketSession(final Address address, final Source source, final Sink sink, final ConnectionLease lease,
            final Handler handler, final Context context, final Timeout timeout, final String dispatchKey,
            final GuardRule guard, final WebSocketRole role, final Map<String, Object> attributes,
            final AutoCloseable owner, final Filter filter, final EventObserver observer,
            final Listener<? super WebSocketSession> listener, final Cancellation cancellation) {
        this(address,
                new WebSocketWriter(require(sink, "WebSocket sink"), require(role, "WebSocket role").writerMask()),
                new WebSocketReader(require(source, "WebSocket source"), role.readerExpectMasked()), sink, lease, owner,
                handler, require(context, "Context").reactor().dispatcher(), false, dispatchKey, context.clock(),
                require(timeout, "WebSocket timeout").ping(), timeout.close(), guard, role, attributes, filter,
                observer, listener, cancellation, context.options().materializeMaxBytes());
    }

    /**
     * Creates the fully specified session and starts its owned background activities.
     *
     * @param address             session address
     * @param writer              single-frame writer
     * @param reader              single-frame reader
     * @param output              output sink, when owned directly
     * @param lease               native lease
     * @param owner               optional owner
     * @param handler             message handler
     * @param dispatcher          dispatcher
     * @param ownsDispatcher      dispatcher ownership flag
     * @param dispatchKey         dispatch key
     * @param clock               session clock
     * @param ping                ping interval
     * @param closeTimeout        close timeout
     * @param guard               optional guard
     * @param role                endpoint role
     * @param attributes          attributes
     * @param filter              optional filter
     * @param observer            observer
     * @param listener            lifecycle listener
     * @param cancellation        shared cancellation scope
     * @param materializeMaxBytes materialize byte threshold
     */
    private WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final Sink output, final ConnectionLease lease, final AutoCloseable owner, final Handler handler,
            final Dispatcher dispatcher, final boolean ownsDispatcher, final String dispatchKey, final Clock clock,
            final Duration ping, final Duration closeTimeout, final GuardRule guard, final WebSocketRole role,
            final Map<String, Object> attributes, final Filter filter, final EventObserver observer,
            final Listener<? super WebSocketSession> listener, final Cancellation cancellation,
            final long materializeMaxBytes) {
        this.address = require(address, "WebSocket address");
        Assert.isFalse(reader != null && handler == null, () -> new ValidateException("Handler must not be null"));
        Assert.isFalse(
                (reader != null || writer != null) && dispatcher == null,
                () -> new ValidateException("Dispatcher must not be null"));
        this.role = require(role, "WebSocket role");
        this.writer = writer;
        this.reader = reader;
        this.output = output;
        this.lease = lease;
        this.owner = owner;
        this.handler = handler;
        this.dispatcher = dispatcher;
        this.ownsDispatcher = ownsDispatcher;
        this.dispatchKey = validateDispatchKey(dispatchKey, writer != null || reader != null);
        this.clock = require(clock, "WebSocket clock");
        this.pingInterval = validateDuration(ping, "WebSocket ping interval");
        this.closeTimeout = validateDuration(closeTimeout, "WebSocket close timeout");
        this.guard = guard;
        this.filter = filter;
        final EventObserver sink = EventObserver.safe(observer);
        this.trafficBytes = new ThreadLocal<>();
        this.observer = event -> sink.emit(withTrafficBytes(event));
        this.attributes = attributes(attributes, this.observer);
        this.cancellation = require(cancellation, "WebSocket cancellation");
        this.cancellationRegistration = new AtomicReference<>(WebSocketSession::noop);
        this.readerHandle = new AtomicReference<>();
        this.drainHandle = new AtomicReference<>();
        this.pingHandle = new AtomicReference<>();
        this.closeTimeoutHandle = new AtomicReference<>();
        this.outboundLock = new Object();
        this.outbound = new ArrayDeque<>();
        this.draining = new AtomicBoolean();
        this.activeEntry = new AtomicReference<>();
        this.terminalNotified = new AtomicBoolean();
        this.resourcesClosed = new AtomicBoolean();
        this.closeQueued = new AtomicBoolean();
        this.closeWritten = new AtomicBoolean();
        this.peerCloseReceived = new AtomicBoolean();
        this.failureAfterClose = new AtomicReference<>();
        this.awaitingPong = new AtomicBoolean();
        this.automaticPingPending = new AtomicBoolean();
        this.sentPingCount = new AtomicInteger();
        this.receivedPingCount = new AtomicInteger();
        this.receivedPongCount = new AtomicInteger();
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        this.scope = LifecycleScope.session(
                this,
                "websocket-session",
                listener,
                this.observer,
                ObservationMarker.WEBSOCKET_OPEN,
                ObservationMarker.WEBSOCKET_CLOSED,
                ObservationMarker.WEBSOCKET_FAILED,
                this.clock);
        this.scope.open(this);
        this.cancellationRegistration.set(this.cancellation.onCancel(this::cancel));
        if (!terminalNotified.get() && reader != null) {
            startReader();
        }
        if (!terminalNotified.get()) {
            schedulePing();
        }
    }

    /**
     * Returns the address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the endpoint role.
     *
     * @return endpoint role
     */
    public WebSocketRole role() {
        return role;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    public Status state() {
        return scope.state();
    }

    /**
     * Returns complete reserved outbound wire bytes.
     *
     * @return queued bytes
     */
    public long queueSize() {
        synchronized (outboundLock) {
            return queuedBytes;
        }
    }

    /**
     * Returns the close timeout used by this session.
     *
     * @return close timeout
     */
    public Duration closeTimeout() {
        return closeTimeout;
    }

    /**
     * Returns sent ping count.
     *
     * @return sent ping count
     */
    public int sentPingCount() {
        return sentPingCount.get();
    }

    /**
     * Returns received ping count.
     *
     * @return received ping count
     */
    public int receivedPingCount() {
        return receivedPingCount.get();
    }

    /**
     * Returns received pong count.
     *
     * @return received pong count
     */
    public int receivedPongCount() {
        return receivedPongCount.get();
    }

    /**
     * Creates a lazy binary send Call.
     *
     * @param payload payload
     * @return lazy send Call
     */
    @Override
    public Call<Void> send(final Payload payload) {
        final Payload checked = require(payload, "WebSocket payload");
        return outboundCall("websocket-send", EntryKind.APPLICATION, () -> binaryFrame(checked));
    }

    /**
     * Creates a lazy text or binary body send Call.
     *
     * @param body body
     * @return lazy send Call
     */
    public Call<Void> send(final WebSocketBody body) {
        final WebSocketBody checked = require(body, "WebSocket body");
        return outboundCall("websocket-send", EntryKind.APPLICATION, () -> bodyFrame(checked));
    }

    /**
     * Creates a lazy text send Call.
     *
     * @param text text
     * @return lazy send Call
     */
    public Call<Void> send(final String text) {
        final String checked = require(text, "WebSocket text");
        return outboundCall("websocket-send", EntryKind.APPLICATION, () -> textFrame(checked));
    }

    /**
     * Creates a lazy binary send Call.
     *
     * @param bytes binary bytes
     * @return lazy send Call
     */
    public Call<Void> send(final ByteString bytes) {
        final ByteString checked = require(bytes, "WebSocket binary payload");
        return outboundCall(
                "websocket-send",
                EntryKind.APPLICATION,
                () -> binaryFrame(Payload.of(ByteString.of(checked.toByteArray()))));
    }

    /**
     * Creates a lazy ping Call.
     *
     * @param payload ping payload
     * @return lazy ping Call
     */
    public Call<Void> ping(final ByteString payload) {
        final ByteString checked = require(payload, "WebSocket ping payload");
        return outboundCall(Builder.WEBSOCKET_PING, EntryKind.PING, () -> pingFrame(checked));
    }

    /**
     * Starts a normal close handshake.
     *
     * @return true when this invocation moved the session to closing
     */
    @Override
    public boolean close() {
        return close(Builder._1000, Normal.EMPTY);
    }

    /**
     * Starts a close handshake with an explicit close description.
     *
     * @param code   close code
     * @param reason close reason
     * @return true when this invocation moved the session to closing
     */
    public boolean close(final int code, final String reason) {
        final WebSocketClose close = WebSocketClose.of(code, reason);
        if (!beginClosing()) {
            return false;
        }
        Logger.info(
                true,
                "Fabric",
                "WebSocket session close started: scheme={}, host={}, port={}, code={}",
                address.scheme(),
                address.host(),
                address.port(),
                close.code());
        if (writer == null) {
            terminate(Termination.CLOSE, null);
            return true;
        }
        try {
            enqueueClose(WebSocketFrame.close(close.code(), close.reason()));
        } catch (final RuntimeException e) {
            terminate(Termination.FAIL, e);
        }
        return true;
    }

    /**
     * Cancels this session and all active native work.
     *
     * @return true when this invocation selected the terminal path
     */
    @Override
    public boolean cancel() {
        return terminate(Termination.CANCEL, new CancellationException("WebSocket session was cancelled"));
    }

    /**
     * Returns immutable session attributes.
     *
     * @return session attributes
     */
    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }

    /**
     * Creates a lazy outbound Call.
     *
     * @param name    Call name
     * @param kind    entry kind
     * @param factory frame factory executed after Call start
     * @return lazy outbound Call
     */
    private Call<Void> outboundCall(final String name, final EntryKind kind, final Supplier<WebSocketFrame> factory) {
        return new OutboundCall(name, kind, factory);
    }

    /**
     * Builds a filtered binary frame after a Call starts.
     *
     * @param payload source payload
     * @return binary frame
     */
    private WebSocketFrame binaryFrame(final Payload payload) {
        final Message outgoing = filter(payload, Builder.WEBSOCKET_WRITE);
        checkGuard(outgoing);
        return WebSocketFrame
                .binary(materialize(outgoing.payload(), Builder.WEB_SOCKET_SESSION_MATERIALIZE_SEND_PAYLOAD));
    }

    /**
     * Builds a filtered body frame after a Call starts.
     *
     * @param body source body
     * @return text or binary frame
     */
    private WebSocketFrame bodyFrame(final WebSocketBody body) {
        final Message outgoing = filter(body.payload(), Builder.WEBSOCKET_WRITE);
        checkGuard(outgoing);
        final ByteString bytes = materialize(outgoing.payload(), "WebSocketSession.send(WebSocketBody)");
        return body.binaryMessage() ? WebSocketFrame.binary(bytes) : WebSocketFrame.text(bytes);
    }

    /**
     * Builds a filtered text frame after a Call starts.
     *
     * @param text source text
     * @return text frame
     */
    private WebSocketFrame textFrame(final String text) {
        final ByteString source = validateSendText(text);
        final Message outgoing = filter(Payload.of(source), Builder.WEBSOCKET_WRITE);
        checkGuard(outgoing);
        return WebSocketFrame.text(materialize(outgoing.payload(), "WebSocketSession.send(String)"));
    }

    /**
     * Builds a filtered ping frame after a Call starts.
     *
     * @param payload source ping payload
     * @return ping frame
     */
    private WebSocketFrame pingFrame(final ByteString payload) {
        if (payload.size() > Builder._125) {
            throw new ValidateException("WebSocket ping payload is too large");
        }
        final Message outgoing = filter(Payload.of(ByteString.of(payload.toByteArray())), Builder.WEBSOCKET_PING);
        checkGuard(outgoing);
        final ByteString filtered = materialize(outgoing.payload(), "WebSocketSession.ping");
        if (filtered.size() > Builder._125) {
            throw new ValidateException("WebSocket ping payload is too large");
        }
        return new WebSocketFrame(Builder.WEBSOCKET_OPCODE_PING, true, filtered, true);
    }

    /**
     * Materializes a payload without exceeding either the configured limit or the fixed message limit.
     *
     * @param payload   payload
     * @param operation operation name
     * @return immutable bytes
     */
    private ByteString materialize(final Payload payload, final String operation) {
        final long declared = payload.length();
        if (declared > Builder.WEB_SOCKET_SESSION_MAX_MESSAGE_BYTES) {
            throw new ValidateException("WebSocket message is too large");
        }
        final long limit = Math.min(materializeMaxBytes, Builder.WEB_SOCKET_SESSION_MAX_MESSAGE_BYTES);
        try {
            return ByteString.of(Payload.materialize(payload, limit, operation));
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to materialize WebSocket payload for " + operation, e);
        }
    }

    /**
     * Enqueues an entry while reserving its complete wire bytes.
     *
     * @param entry entry
     */
    private void enqueue(final OutboundEntry entry) {
        ensureWritable(entry.kind());
        synchronized (outboundLock) {
            if (terminalNotified.get()) {
                throw new StatefulException("WebSocket session is terminated");
            }
            final long next = queuedBytes + entry.wireBytes();
            if (next < queuedBytes || next > Builder.WEB_SOCKET_SESSION_MAX_QUEUED_BYTES) {
                throw new StatefulException("WebSocket write queue is full");
            }
            if (!entry.reserve()) {
                throw new StatefulException("WebSocket entry was already reserved");
            }
            queuedBytes = next;
            insert(entry);
        }
        startDrain();
    }

    /**
     * Inserts one entry according to WebSocket application and control ordering.
     *
     * @param entry entry
     */
    private void insert(final OutboundEntry entry) {
        if (entry.kind() == EntryKind.APPLICATION) {
            outbound.addLast(entry);
            return;
        }
        if (entry.kind() == EntryKind.CLOSE) {
            outbound.addFirst(entry);
            return;
        }
        final ArrayDeque<OutboundEntry> ordered = new ArrayDeque<>(outbound.size() + Normal._1);
        while (!outbound.isEmpty() && outbound.peekFirst().kind() != EntryKind.APPLICATION) {
            ordered.addLast(outbound.removeFirst());
        }
        ordered.addLast(entry);
        ordered.addAll(outbound);
        outbound.clear();
        outbound.addAll(ordered);
    }

    /**
     * Enqueues the single internal close entry.
     *
     * @param frame close frame
     */
    private void enqueueClose(final WebSocketFrame frame) {
        if (!closeQueued.compareAndSet(false, true)) {
            return;
        }
        try {
            enqueue(new OutboundEntry(frame, EntryKind.CLOSE, wireBytes(frame, role.writerMask())));
        } catch (final RuntimeException e) {
            closeQueued.set(false);
            throw e;
        }
    }

    /**
     * Enqueues an internal pong entry for a received ping.
     *
     * @param payload ping payload
     */
    private void enqueuePong(final ByteString payload) {
        if (scope.state() != Status.OPENED) {
            return;
        }
        final WebSocketFrame frame = new WebSocketFrame(Builder.WEBSOCKET_OPCODE_PONG, true, payload, true);
        try {
            enqueue(new OutboundEntry(frame, EntryKind.PONG, wireBytes(frame, role.writerMask())));
        } catch (final StatefulException e) {
            if (scope.state() == Status.OPENED && !terminalNotified.get()) {
                throw e;
            }
        }
    }

    /**
     * Starts at most one background drain.
     */
    private void startDrain() {
        if (writer == null || terminalNotified.get() || closeWritten.get() || !hasQueued()
                || !draining.compareAndSet(false, true)) {
            return;
        }
        try {
            final DispatchHandle created = dispatcher.background(
                    dispatchKey + ":drain",
                    this,
                    Activity.of("websocket-drain", this::drain, cancellation));
            drainHandle.set(created);
            created.future().whenComplete((ignored, cause) -> drainFinished(created, cause));
        } catch (final RuntimeException | Error e) {
            draining.set(false);
            terminate(Termination.FAIL, e);
            throw e;
        }
    }

    /**
     * Handles background drain completion and closes the enqueue race.
     *
     * @param completed completed handle
     * @param cause     completion cause
     */
    private void drainFinished(final DispatchHandle completed, final Throwable cause) {
        drainHandle.compareAndSet(completed, null);
        draining.set(false);
        if (cause != null && !terminalNotified.get() && !completed.cancelled()) {
            terminate(Termination.FAIL, cause);
            return;
        }
        if (!terminalNotified.get() && hasQueued()) {
            startDrain();
        }
    }

    /**
     * Serially writes and flushes outbound entries.
     */
    private void drain() {
        while (!terminalNotified.get()) {
            final OutboundEntry entry = nextEntry();
            if (entry == null) {
                return;
            }
            try {
                final long actualBytes = writer.write(entry.frame());
                flushOutput();
                finishReservation(entry);
                if (!entry.succeed()) {
                    continue;
                }
                emit(ObservationMarker.WEBSOCKET_MESSAGE, actualBytes, null);
                if (entry.kind() == EntryKind.PING || entry.kind() == EntryKind.AUTOMATIC_PING) {
                    sentPingCount.incrementAndGet();
                    awaitingPong.set(true);
                }
                if (entry.kind() == EntryKind.AUTOMATIC_PING) {
                    automaticPingPending.set(false);
                    schedulePing();
                }
                Logger.debug(
                        false,
                        "Fabric",
                        "WebSocket frame sent: scheme={}, host={}, port={}, opcode={}, bytes={}",
                        address.scheme(),
                        address.host(),
                        address.port(),
                        entry.frame().opcode(),
                        actualBytes);
                if (entry.kind() == EntryKind.CLOSE) {
                    closeWritten.set(true);
                    afterCloseWritten();
                    return;
                }
            } catch (final RuntimeException | Error e) {
                finishReservation(entry);
                entry.fail(e);
                terminate(Termination.FAIL, e);
                if (e instanceof Error error) {
                    throw error;
                }
                return;
            } finally {
                activeEntry.compareAndSet(entry, null);
            }
        }
    }

    /**
     * Removes and activates the next queued entry.
     *
     * @return active entry or null
     */
    private OutboundEntry nextEntry() {
        synchronized (outboundLock) {
            OutboundEntry entry;
            while ((entry = outbound.pollFirst()) != null) {
                if (entry.activate()) {
                    activeEntry.set(entry);
                    return entry;
                }
                releaseReservation(entry);
            }
            return null;
        }
    }

    /**
     * Flushes the directly owned output after one complete entry.
     */
    private void flushOutput() {
        if (output == null) {
            return;
        }
        try {
            output.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to flush WebSocket entry", e);
        }
    }

    /**
     * Releases the active entry reservation before completing its Call.
     *
     * @param entry active entry
     */
    private void finishReservation(final OutboundEntry entry) {
        releaseReservation(entry);
    }

    /**
     * Releases complete reserved wire bytes exactly once.
     *
     * @param entry entry
     */
    private void releaseReservation(final OutboundEntry entry) {
        synchronized (outboundLock) {
            if (!entry.release()) {
                return;
            }
            queuedBytes -= entry.wireBytes();
            if (queuedBytes < Normal.LONG_ZERO) {
                queuedBytes = Normal.LONG_ZERO;
            }
        }
    }

    /**
     * Cancels one outbound entry according to whether it is queued or active.
     *
     * @param entry entry
     */
    private void cancelEntry(final OutboundEntry entry) {
        if (entry == null || entry.terminal()) {
            return;
        }
        if (activeEntry.get() == entry) {
            terminate(Termination.CANCEL, new CancellationException("Active WebSocket entry was cancelled"));
            return;
        }
        synchronized (outboundLock) {
            if (outbound.remove(entry)) {
                releaseReservation(entry);
                entry.cancel(new CancellationException("Queued WebSocket entry was cancelled"));
            }
        }
    }

    /**
     * Waits for an entry using the shared thread utility.
     *
     * @param entry entry
     */
    private void awaitEntry(final OutboundEntry entry) {
        while (!entry.terminal()) {
            if (!ThreadKit.sleep(ENTRY_WAIT_MILLIS)) {
                throw new CancellationException("Interrupted while waiting for WebSocket entry");
            }
        }
        final Throwable cause = entry.cause();
        if (entry.state() == EntryState.CANCELLED) {
            final CancellationException cancelled = new CancellationException("WebSocket entry was cancelled");
            if (cause != null) {
                cancelled.initCause(cause);
            }
            throw cancelled;
        }
        if (entry.state() == EntryState.FAILED) {
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new InternalException("WebSocket entry failed", cause);
        }
    }

    /**
     * Returns whether at least one entry is queued.
     *
     * @return true when queued
     */
    private boolean hasQueued() {
        synchronized (outboundLock) {
            return !outbound.isEmpty();
        }
    }

    /**
     * Starts the single background reader loop.
     */
    private void startReader() {
        final DispatchHandle created = dispatcher.background(
                dispatchKey + ":reader",
                this,
                Activity.of(Builder.WEBSOCKET_READ, this::readFrames, cancellation));
        if (!readerHandle.compareAndSet(null, created)) {
            created.cancel();
            throw new StatefulException("WebSocket reader can only be started once");
        }
        created.future().whenComplete((ignored, cause) -> readerHandle.compareAndSet(created, null));
    }

    /**
     * Reads frames, aggregates data messages, and handles control frames.
     */
    private void readFrames() {
        Buffer fragments = null;
        int fragmentOpcode = Normal.__1;
        long fragmentBytes = Normal.LONG_ZERO;
        try {
            while (!terminalNotified.get() && !cancellation.cancelled()) {
                final WebSocketFrame frame = reader.next();
                emit(ObservationMarker.WEBSOCKET_MESSAGE, wireBytes(frame, role.readerExpectMasked()), null);
                final int opcode = frame.opcode();
                if (opcode == Normal._8) {
                    peerClose(frame);
                    return;
                }
                if (opcode == Builder.WEBSOCKET_OPCODE_PING) {
                    receivedPingCount.incrementAndGet();
                    enqueuePong(frame.payload());
                    continue;
                }
                if (opcode == Builder.WEBSOCKET_OPCODE_PONG) {
                    receivedPongCount.incrementAndGet();
                    awaitingPong.set(false);
                    continue;
                }
                if (opcode == Normal._1 || opcode == Builder.WEBSOCKET_OPCODE_BINARY) {
                    if (fragments != null) {
                        throw new ProtocolException("WebSocket fragmented message is already open");
                    }
                    if (frame.fin()) {
                        deliver(opcode, frame.payload());
                    } else {
                        fragments = new Buffer();
                        fragmentOpcode = opcode;
                        fragmentBytes = appendFragment(fragments, Normal.LONG_ZERO, frame.payload());
                    }
                    continue;
                }
                if (opcode != Normal._0 || fragments == null) {
                    throw new ProtocolException("WebSocket continuation has no initial frame");
                }
                fragmentBytes = appendFragment(fragments, fragmentBytes, frame.payload());
                if (frame.fin()) {
                    deliver(fragmentOpcode, fragments.readByteString());
                    fragments = null;
                    fragmentOpcode = Normal.__1;
                    fragmentBytes = Normal.LONG_ZERO;
                }
            }
        } catch (final CancellationException e) {
            if (!terminalNotified.get()) {
                terminate(Termination.CANCEL, e);
            }
        } catch (final RuntimeException | Error e) {
            if (!terminalNotified.get()) {
                readerFailure(e);
            }
            if (e instanceof Error error) {
                throw error;
            }
        }
    }

    /**
     * Appends a fragment while enforcing the complete aggregate limit.
     *
     * @param aggregate aggregate buffer
     * @param current   current aggregate bytes
     * @param fragment  fragment bytes
     * @return updated aggregate bytes
     */
    private long appendFragment(final Buffer aggregate, final long current, final ByteString fragment) {
        final long next = current + fragment.size();
        if (next < current || next > Builder.WEB_SOCKET_SESSION_MAX_MESSAGE_BYTES) {
            throw new ProtocolException("WebSocket aggregated message is too large");
        }
        aggregate.write(fragment);
        return next;
    }

    /**
     * Delivers one complete data message through the inbound filter and guard.
     *
     * @param opcode  initial data opcode
     * @param payload complete payload
     */
    private void deliver(final int opcode, final ByteString payload) {
        if (payload.size() > Builder.WEB_SOCKET_SESSION_MAX_MESSAGE_BYTES) {
            throw new ProtocolException("WebSocket aggregated message is too large");
        }
        final Payload source;
        if (opcode == Normal._1) {
            source = Payload.of(decodeUtf8(payload), StandardCharsets.UTF_8);
        } else if (opcode == Builder.WEBSOCKET_OPCODE_BINARY) {
            source = Payload.of(payload);
        } else {
            throw new ProtocolException("WebSocket message opcode is invalid");
        }
        final Message received = filter(source, Builder.WEBSOCKET_READ);
        checkGuard(received);
        handler.message(this, received);
        Logger.debug(
                false,
                "Fabric",
                "WebSocket message received: scheme={}, host={}, port={}, bytes={}",
                address.scheme(),
                address.host(),
                address.port(),
                payload.size());
    }

    /**
     * Handles a peer close and enqueues the single direct close reply when needed.
     *
     * @param frame peer close frame
     */
    private void peerClose(final WebSocketFrame frame) {
        final WebSocketClose close = parseClose(frame);
        peerCloseReceived.set(true);
        beginClosing();
        Logger.info(
                true,
                "Fabric",
                "WebSocket peer close received: scheme={}, host={}, port={}, code={}",
                address.scheme(),
                address.host(),
                address.port(),
                close.code());
        if (closeWritten.get()) {
            terminate(Termination.CLOSE, null);
            return;
        }
        try {
            enqueueClose(new WebSocketFrame(Normal._8, true, frame.payload(), true));
        } catch (final RuntimeException e) {
            terminate(Termination.FAIL, e);
        }
    }

    /**
     * Maps a reader failure to a best-effort protocol close followed by one failure notification.
     *
     * @param cause reader failure
     */
    private void readerFailure(final Throwable cause) {
        failureAfterClose.compareAndSet(null, cause);
        beginClosing();
        if (writer == null || closeWritten.get()) {
            terminate(Termination.FAIL, cause);
            return;
        }
        final int code = failureCloseCode(cause);
        final String reason = switch (code) {
            case Builder.WEBSOCKET_CLOSE_INVALID_PAYLOAD -> "invalid payload";
            case Builder.WEBSOCKET_CLOSE_MESSAGE_TOO_LARGE -> "message too large";
            case Builder.WEBSOCKET_CLOSE_INTERNAL_ERROR -> "internal error";
            default -> "protocol error";
        };
        try {
            enqueueClose(WebSocketFrame.close(code, reason));
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
            terminate(Termination.FAIL, cause);
        }
    }

    /**
     * Completes the terminal action selected by a flushed close entry.
     */
    private void afterCloseWritten() {
        final Throwable failure = failureAfterClose.get();
        if (failure != null) {
            terminate(Termination.FAIL, failure);
        } else if (peerCloseReceived.get() || reader == null) {
            terminate(Termination.CLOSE, null);
        } else {
            scheduleCloseTimeout();
        }
    }

    /**
     * Schedules forced cancellation after the close handshake deadline.
     */
    private void scheduleCloseTimeout() {
        if (dispatcher == null || terminalNotified.get()) {
            return;
        }
        final DispatchHandle next = dispatcher.schedule(
                dispatchKey + ":close-timeout",
                closeTimeout,
                Activity.of(Builder.WEBSOCKET_ACTIVITY_CLOSE_TIMEOUT, () -> {
                    if (!terminalNotified.get() && scope.state() == Status.CLOSING) {
                        terminate(Termination.CANCEL, new TimeoutException("WebSocket close handshake timed out"));
                    }
                }, cancellation));
        final DispatchHandle previous = closeTimeoutHandle.getAndSet(next);
        if (previous != null) {
            previous.cancel();
        }
    }

    /**
     * Schedules the next automatic ping tick.
     */
    private void schedulePing() {
        if (pingInterval.isZero() || writer == null || dispatcher == null || terminalNotified.get()) {
            return;
        }
        final DispatchHandle next = dispatcher.schedule(
                dispatchKey + ":ping",
                pingInterval,
                Activity.of(Builder.WEBSOCKET_PING, this::automaticPing, cancellation));
        final DispatchHandle previous = pingHandle.getAndSet(next);
        if (previous != null && previous != next) {
            previous.cancel();
        }
    }

    /**
     * Enqueues one internal automatic ping or fails an unanswered ping deadline.
     */
    private void automaticPing() {
        pingHandle.set(null);
        if (terminalNotified.get() || scope.state() != Status.OPENED) {
            return;
        }
        if (awaitingPong.get()) {
            terminate(Termination.FAIL, new TimeoutException("WebSocket pong timeout"));
            return;
        }
        if (!automaticPingPending.compareAndSet(false, true)) {
            schedulePing();
            return;
        }
        try {
            final WebSocketFrame frame = new WebSocketFrame(Builder.WEBSOCKET_OPCODE_PING, true, ByteString.EMPTY,
                    true);
            enqueue(new OutboundEntry(frame, EntryKind.AUTOMATIC_PING, wireBytes(frame, role.writerMask())));
        } catch (final RuntimeException e) {
            automaticPingPending.set(false);
            terminate(Termination.FAIL, e);
        }
    }

    /**
     * Selects and executes the unique session terminal path.
     *
     * @param termination terminal kind
     * @param cause       optional cause
     * @return true when this invocation selected the terminal path
     */
    private boolean terminate(final Termination termination, final Throwable cause) {
        if (!terminalNotified.compareAndSet(false, true)) {
            return false;
        }
        final Throwable terminalCause = cause == null && termination != Termination.CLOSE
                ? new StatefulException("WebSocket session terminated")
                : cause;
        final Runnable unregister = cancellationRegistration.getAndSet(WebSocketSession::noop);
        unregister.run();
        if (!cancellation.cancelled()) {
            cancellation.cancel(
                    terminalCause == null ? new CancellationException("WebSocket session closed") : terminalCause);
        }
        awaitingPong.set(false);
        automaticPingPending.set(false);
        cancelHandle(closeTimeoutHandle);
        cancelHandle(pingHandle);
        cancelHandle(readerHandle);
        cancelHandle(drainHandle);
        completePending(termination, terminalCause);
        final RuntimeException cleanupFailure = closeNativeResources();
        final Throwable notifiedCause = combine(terminalCause, cleanupFailure);
        switch (termination) {
            case CLOSE -> scope.close(this);
            case CANCEL -> scope.cancel(
                    notifiedCause == null ? new CancellationException("WebSocket session cancelled") : notifiedCause);
            case FAIL -> scope
                    .fail(notifiedCause == null ? new StatefulException("WebSocket session failed") : notifiedCause);
        }
        notifyHandler(termination, notifiedCause);
        Logger.info(
                false,
                "Fabric",
                "WebSocket session terminated: scheme={}, host={}, port={}, state={}",
                address.scheme(),
                address.host(),
                address.port(),
                scope.state());
        return true;
    }

    /**
     * Completes all active and queued entries exactly once during terminal cleanup.
     *
     * @param termination terminal kind
     * @param cause       terminal cause
     */
    private void completePending(final Termination termination, final Throwable cause) {
        final Throwable failure = cause == null ? new StatefulException("WebSocket session closed") : cause;
        synchronized (outboundLock) {
            OutboundEntry entry;
            while ((entry = outbound.pollFirst()) != null) {
                releaseReservation(entry);
                completeEntry(entry, termination, failure);
            }
            final OutboundEntry active = activeEntry.getAndSet(null);
            if (active != null) {
                releaseReservation(active);
                completeEntry(active, termination, failure);
            }
        }
    }

    /**
     * Completes one entry according to the selected terminal kind.
     *
     * @param entry       entry
     * @param termination terminal kind
     * @param cause       completion cause
     */
    private static void completeEntry(final OutboundEntry entry, final Termination termination, final Throwable cause) {
        if (termination == Termination.CANCEL) {
            entry.cancel(cause);
        } else {
            entry.fail(cause);
        }
    }

    /**
     * Closes all native resources once and returns the first cleanup failure.
     *
     * @return first cleanup failure or null
     */
    private RuntimeException closeNativeResources() {
        if (!resourcesClosed.compareAndSet(false, true)) {
            return null;
        }
        RuntimeException failure = null;
        failure = closeLease(lease, failure);
        failure = closeResource(owner, failure, "WebSocket owner");
        if (ownsDispatcher) {
            failure = closeResource(dispatcher, failure, "WebSocket dispatcher");
        }
        return failure;
    }

    /**
     * Closes one resource while retaining the first failure.
     *
     * @param resource resource
     * @param failure  current first failure
     * @param name     resource name
     * @return first failure
     */
    private static RuntimeException closeResource(
            final AutoCloseable resource,
            final RuntimeException failure,
            final String name) {
        RuntimeException current = failure;
        if (resource == null) {
            return current;
        }
        try {
            resource.close();
        } catch (final Exception e) {
            final RuntimeException next = e instanceof RuntimeException runtime ? runtime
                    : new InternalException("Unable to close " + name, e);
            if (current == null) {
                current = next;
            } else if (current != next) {
                current.addSuppressed(next);
            }
        }
        return current;
    }

    /**
     * Closes a connection lease while retaining the first cleanup failure.
     *
     * @param resource connection lease
     * @param failure  current first failure
     * @return first failure
     */
    private static RuntimeException closeLease(final ConnectionLease resource, final RuntimeException failure) {
        RuntimeException current = failure;
        if (resource == null) {
            return current;
        }
        try {
            resource.close();
        } catch (final RuntimeException e) {
            if (current == null) {
                current = e;
            } else if (current != e) {
                current.addSuppressed(e);
            }
        }
        return current;
    }

    /**
     * Cancels and clears one dispatch handle reference.
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
     * Notifies the user handler exactly once for the selected terminal kind.
     *
     * @param termination terminal kind
     * @param cause       optional cause
     */
    private void notifyHandler(final Termination termination, final Throwable cause) {
        if (handler == null) {
            return;
        }
        try {
            if (termination == Termination.CLOSE) {
                handler.closed(this);
            } else {
                handler.failure(this, cause == null ? new StatefulException("WebSocket session terminated") : cause);
            }
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "WebSocket terminal handler failed: exception={}",
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Applies the optional message filter.
     *
     * @param payload payload
     * @param tag     direction tag
     * @return filtered message
     */
    private Message filter(final Payload payload, final String tag) {
        final Message message = Message.of(address.protocol(), address, Headers.empty(), payload, tag);
        return filter == null ? message : FilterChain.apply(message, filter);
    }

    /**
     * Applies the optional guard.
     *
     * @param message message
     */
    private void checkGuard(final Message message) {
        if (guard != null) {
            guard.check(message).throwIfRejected();
        }
    }

    /**
     * Emits a WebSocket event with the complete physical wire byte count.
     *
     * @param marker marker
     * @param bytes  complete wire bytes
     * @param cause  optional cause
     */
    private void emit(final ObservationMarker marker, final long bytes, final Throwable cause) {
        trafficBytes.set(bytes);
        try {
            scope.emit(marker, cause);
        } finally {
            trafficBytes.remove();
        }
    }

    /**
     * Adds complete WebSocket wire bytes while preserving lifecycle event metadata.
     *
     * @param event lifecycle event
     * @return event enriched with wire bytes when applicable
     */
    private FabricEvent withTrafficBytes(final FabricEvent event) {
        final Long bytes = trafficBytes.get();
        if (bytes == null || event.marker() != ObservationMarker.WEBSOCKET_MESSAGE) {
            return event;
        }
        return new FabricEvent(event.marker(), event.time(), event.tags().with(Builder.TAG_BYTES, Long.toString(bytes)),
                event.cause());
    }

    /**
     * Ensures an entry is legal for the current session state.
     *
     * @param kind entry kind
     */
    private void ensureWritable(final EntryKind kind) {
        if (writer == null) {
            throw new StatefulException("WebSocket session has no transport");
        }
        if (terminalNotified.get()) {
            throw new StatefulException("WebSocket session is terminated");
        }
        if (kind != EntryKind.CLOSE && scope.state() != Status.OPENED) {
            throw new StatefulException("WebSocket session is not open");
        }
        cancellation.throwIfCancelled();
    }

    /**
     * Moves the session to closing when legal.
     *
     * @return true when state changed
     */
    private boolean beginClosing() {
        return !terminalNotified.get() && scope.closing();
    }

    /**
     * Calculates complete frame wire bytes including header and optional mask.
     *
     * @param frame  frame
     * @param masked mask flag
     * @return complete wire bytes
     */
    private static long wireBytes(final WebSocketFrame frame, final boolean masked) {
        final long payloadBytes = frame.payload().size();
        final long lengthBytes = payloadBytes <= Builder._125 ? Normal.LONG_ZERO
                : payloadBytes <= Normal._65535 ? Short.BYTES : Long.BYTES;
        return Normal._2 + lengthBytes + (masked ? Normal._4 : Normal._0) + payloadBytes;
    }

    /**
     * Parses one already validated close frame.
     *
     * @param frame close frame
     * @return close description
     */
    private static WebSocketClose parseClose(final WebSocketFrame frame) {
        final byte[] payload = frame.payload().toByteArray();
        if (payload.length == Normal._0) {
            return WebSocketClose.of(Builder._1000, Normal.EMPTY);
        }
        if (payload.length == Normal._1) {
            throw new ProtocolException("Invalid WebSocket close payload");
        }
        final int code = (payload[Normal._0] & Builder.UNSIGNED_BYTE_MASK) << Byte.SIZE
                | payload[Normal._1] & Builder.UNSIGNED_BYTE_MASK;
        final String reason = decodeUtf8(payload, Short.BYTES, payload.length - Short.BYTES);
        try {
            return WebSocketClose.of(code, reason);
        } catch (final ValidateException e) {
            throw new ProtocolException("Invalid WebSocket close frame", e);
        }
    }

    /**
     * Strictly decodes complete text message bytes.
     *
     * @param value bytes
     * @return decoded text
     */
    private static String decodeUtf8(final ByteString value) {
        return decodeUtf8(value.toByteArray(), Normal._0, value.size());
    }

    /**
     * Strictly decodes a UTF-8 byte range.
     *
     * @param value  bytes
     * @param offset range offset
     * @param length range length
     * @return decoded text
     */
    private static String decodeUtf8(final byte[] value, final int offset, final int length) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(value, offset, length))
                    .toString();
        } catch (final CharacterCodingException e) {
            throw new ValidateException("WebSocket text must be valid UTF-8", e);
        }
    }

    /**
     * Selects a close code for a reader failure.
     *
     * @param cause failure
     * @return close code
     */
    private static int failureCloseCode(final Throwable cause) {
        final String message = cause.getMessage() == null ? Normal.EMPTY : cause.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("too large") || message.contains("size")) {
            return Builder.WEBSOCKET_CLOSE_MESSAGE_TOO_LARGE;
        }
        if (message.contains("utf-8")) {
            return Builder.WEBSOCKET_CLOSE_INVALID_PAYLOAD;
        }
        if (cause instanceof ProtocolException || cause instanceof ValidateException) {
            return Builder.WEBSOCKET_CLOSE_PROTOCOL_ERROR;
        }
        return Builder.WEBSOCKET_CLOSE_INTERNAL_ERROR;
    }

    /**
     * Validates text accepted by the String send overload.
     *
     * @param text text
     * @return encoded text
     */
    private static ByteString validateSendText(final String text) {
        if (StringKit.isBlank(text) || StringKit.containsAny(text, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket text must be non-blank and single-line");
        }
        return ByteString.encodeUtf8(text);
    }

    /**
     * Validates a non-negative duration.
     *
     * @param duration duration
     * @param name     field name
     * @return validated duration
     */
    private static Duration validateDuration(final Duration duration, final String name) {
        final Duration checked = require(duration, name);
        if (checked.isNegative()) {
            throw new ValidateException(name + " must not be negative");
        }
        return checked;
    }

    /**
     * Validates a dispatch key only for sessions with native transport.
     *
     * @param value    dispatch key
     * @param required whether a key is required
     * @return validated key or null
     */
    private static String validateDispatchKey(final String value, final boolean required) {
        if (!required && value == null) {
            return null;
        }
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket dispatch key must be non-blank and single-line");
        }
        return value.trim();
    }

    /**
     * Creates a compatibility dispatch key.
     *
     * @param address session address
     * @return dispatch key
     */
    private static String defaultDispatchKey(final Address address) {
        final Address checked = require(address, "WebSocket address");
        return "websocket:" + checked.host() + Symbol.COLON + checked.port();
    }

    /**
     * Creates default session attributes.
     *
     * @param observer observer
     * @return default attributes
     */
    private static Map<String, Object> defaultAttributes(final EventObserver observer) {
        return Map.of(Builder.ATTRIBUTE_OBSERVER, EventObserver.safe(observer));
    }

    /**
     * Copies session attributes and installs the session observer.
     *
     * @param source   source attributes
     * @param observer observer
     * @return immutable attributes
     */
    private static Map<String, Object> attributes(final Map<String, Object> source, final EventObserver observer) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (source != null) {
            source.forEach((key, value) -> {
                if (key != null && value != null) {
                    result.put(key, value);
                }
            });
        }
        result.put(Builder.ATTRIBUTE_OBSERVER, EventObserver.safe(observer));
        return Map.copyOf(result);
    }

    /**
     * Combines a terminal cause with an optional cleanup failure.
     *
     * @param cause   terminal cause
     * @param cleanup cleanup failure
     * @return combined cause
     */
    private static Throwable combine(final Throwable cause, final RuntimeException cleanup) {
        if (cause == null) {
            return cleanup;
        }
        if (cleanup != null && cleanup != cause) {
            cause.addSuppressed(cleanup);
        }
        return cause;
    }

    /**
     * No-operation callback used as an idempotent cancellation-registration sentinel.
     */
    private static void noop() {
        // No operation.
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
     * Lazy outbound Call that links Call cancellation to its owned queue entry.
     */
    private final class OutboundCall extends MonoCall<Void> {

        /**
         * Outbound entry kind.
         */
        private final EntryKind kind;

        /**
         * Frame factory run only after Call start.
         */
        private final Supplier<WebSocketFrame> factory;

        /**
         * Entry created by the running Call.
         */
        private final AtomicReference<OutboundEntry> entry;

        /**
         * Creates a lazy outbound Call.
         *
         * @param name    Call name
         * @param kind    entry kind
         * @param factory frame factory
         */
        private OutboundCall(final String name, final EntryKind kind, final Supplier<WebSocketFrame> factory) {
            super(name, dispatcher, observer);
            this.kind = require(kind, "WebSocket entry kind");
            this.factory = require(factory, "WebSocket frame factory");
            this.entry = new AtomicReference<>();
        }

        /**
         * Filters, encodes, reserves, enqueues, and waits after the Call starts.
         *
         * @return null after the entry is flushed
         */
        @Override
        protected Void perform() {
            ensureWritable(kind);
            cancellation().throwIfCancelled();
            final WebSocketFrame frame = factory.get();
            final OutboundEntry created = new OutboundEntry(frame, kind, wireBytes(frame, role.writerMask()));
            entry.set(created);
            cancellation().throwIfCancelled();
            WebSocketSession.this.enqueue(created);
            awaitEntry(created);
            return null;
        }

        /**
         * Returns the outbound dispatch key.
         *
         * @return dispatch key
         */
        @Override
        protected String dispatchKey() {
            return dispatchKey + ":call";
        }

        /**
         * Cancels the linked queued or active entry.
         */
        @Override
        protected void cancelRunning() {
            cancelEntry(entry.get());
        }

    }

    /**
     * One session-owned outbound frame and its exactly-once completion state.
     */
    private static final class OutboundEntry {

        /**
         * Frame to encode.
         */
        private final WebSocketFrame frame;

        /**
         * Entry ordering kind.
         */
        private final EntryKind kind;

        /**
         * Complete estimated wire bytes reserved in the queue.
         */
        private final long wireBytes;

        /**
         * Completion state.
         */
        private final AtomicReference<EntryState> state;

        /**
         * Terminal cause.
         */
        private final AtomicReference<Throwable> cause;

        /**
         * Reservation ownership guard.
         */
        private final AtomicBoolean reserved;

        /**
         * Creates an outbound entry.
         *
         * @param frame     frame
         * @param kind      ordering kind
         * @param wireBytes complete wire bytes
         */
        private OutboundEntry(final WebSocketFrame frame, final EntryKind kind, final long wireBytes) {
            this.frame = require(frame, "WebSocket frame");
            this.kind = require(kind, "WebSocket entry kind");
            if (wireBytes < Normal._2 || wireBytes > Builder.WEB_SOCKET_SESSION_MAX_MESSAGE_BYTES + Normal._14) {
                throw new ValidateException("WebSocket entry wire size is invalid");
            }
            this.wireBytes = wireBytes;
            this.state = new AtomicReference<>(EntryState.QUEUED);
            this.cause = new AtomicReference<>();
            this.reserved = new AtomicBoolean();
        }

        /**
         * Returns the frame.
         *
         * @return frame
         */
        private WebSocketFrame frame() {
            return frame;
        }

        /**
         * Returns the ordering kind.
         *
         * @return kind
         */
        private EntryKind kind() {
            return kind;
        }

        /**
         * Returns complete wire bytes.
         *
         * @return wire bytes
         */
        private long wireBytes() {
            return wireBytes;
        }

        /**
         * Returns current entry state.
         *
         * @return state
         */
        private EntryState state() {
            return state.get();
        }

        /**
         * Returns the terminal cause.
         *
         * @return cause or null
         */
        private Throwable cause() {
            return cause.get();
        }

        /**
         * Claims queue-byte reservation ownership.
         *
         * @return true when claimed
         */
        private boolean reserve() {
            return reserved.compareAndSet(false, true);
        }

        /**
         * Releases queue-byte reservation ownership.
         *
         * @return true when released
         */
        private boolean release() {
            return reserved.compareAndSet(true, false);
        }

        /**
         * Marks this entry active.
         *
         * @return true when activated
         */
        private boolean activate() {
            return state.compareAndSet(EntryState.QUEUED, EntryState.ACTIVE);
        }

        /**
         * Completes this entry successfully.
         *
         * @return true when completed
         */
        private synchronized boolean succeed() {
            return state.compareAndSet(EntryState.ACTIVE, EntryState.SUCCEEDED);
        }

        /**
         * Completes this entry with failure.
         *
         * @param failure failure
         * @return true when completed
         */
        private boolean fail(final Throwable failure) {
            return complete(EntryState.FAILED, failure);
        }

        /**
         * Completes this entry with cancellation.
         *
         * @param cancellation cancellation cause
         * @return true when completed
         */
        private boolean cancel(final Throwable cancellation) {
            return complete(EntryState.CANCELLED, cancellation);
        }

        /**
         * Completes a non-terminal entry once.
         *
         * @param target  terminal state
         * @param failure terminal cause
         * @return true when completed
         */
        private synchronized boolean complete(final EntryState target, final Throwable failure) {
            if (state.get().terminal()) {
                return false;
            }
            cause.set(failure);
            state.set(target);
            return true;
        }

        /**
         * Returns whether this entry is terminal.
         *
         * @return true when terminal
         */
        private boolean terminal() {
            return state.get().terminal();
        }

    }

    /**
     * Outbound ordering class.
     */
    private enum EntryKind {

        /**
         * User application message kept in FIFO order.
         */
        APPLICATION,

        /**
         * Public or automatic ping ordered before queued application messages.
         */
        PING,

        /**
         * Internal automatic ping ordered before queued application messages.
         */
        AUTOMATIC_PING,

        /**
         * Internal pong ordered before queued application messages.
         */
        PONG,

        /**
         * Internal close ordered immediately after the active entry.
         */
        CLOSE

    }

    /**
     * Outbound entry completion state.
     */
    private enum EntryState {

        /**
         * Reserved and queued.
         */
        QUEUED,

        /**
         * Currently being written.
         */
        ACTIVE,

        /**
         * Successfully flushed.
         */
        SUCCEEDED,

        /**
         * Failed.
         */
        FAILED,

        /**
         * Cancelled.
         */
        CANCELLED;

        /**
         * Returns whether this state is terminal.
         *
         * @return true when terminal
         */
        private boolean terminal() {
            return this == SUCCEEDED || this == FAILED || this == CANCELLED;
        }

    }

    /**
     * Session terminal path selected by the exactly-once guard owner.
     */
    private enum Termination {

        /**
         * Normal local or peer close.
         */
        CLOSE,

        /**
         * Explicit or deadline cancellation.
         */
        CANCEL,

        /**
         * Protocol, reader, writer, or handler failure.
         */
        FAIL

    }

}
