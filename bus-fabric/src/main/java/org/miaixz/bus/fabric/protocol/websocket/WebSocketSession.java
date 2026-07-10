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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tag.Tags;
import org.miaixz.bus.fabric.protocol.websocket.body.WebSocketBody;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketFrame;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketReader;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketWriter;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.logger.Logger;

/**
 * Open WebSocket session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketSession implements Session {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Maximum queued application payload bytes.
     */
    public static final long MAX_QUEUE_SIZE = 16L * 1024L * 1024L;

    /**
     * Time to wait for peer close after sending a close frame.
     */
    public static final Duration CANCEL_AFTER_CLOSE = Duration.ofSeconds(60);

    /**
     * Session address.
     */
    private final Address address;

    /**
     * Native frame writer.
     */
    private final WebSocketWriter writer;

    /**
     * Native frame reader.
     */
    private final WebSocketReader reader;

    /**
     * Native connection lease.
     */
    private final ConnectionLease lease;

    /**
     * Native reader dispatch handle.
     */
    private final AtomicReference<DispatchHandle> readerHandle;

    /**
     * Automatic ping dispatch handle.
     */
    private final AtomicReference<DispatchHandle> pingHandle;

    /**
     * Close-timeout dispatch handle.
     */
    private final AtomicReference<DispatchHandle> closeTimeoutHandle;

    /**
     * Dispatcher for close-timeout scheduling.
     */
    private final Dispatcher dispatcher;

    /**
     * Session dispatch key.
     */
    private final String dispatchKey;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Close callback guard.
     */
    private final AtomicBoolean closeNotified;

    /**
     * Automatic ping awaiting pong flag.
     */
    private final AtomicBoolean awaitingPong;

    /**
     * Sent ping count.
     */
    private final java.util.concurrent.atomic.AtomicInteger sentPingCount;

    /**
     * Received ping count.
     */
    private final java.util.concurrent.atomic.AtomicInteger receivedPingCount;

    /**
     * Received pong count.
     */
    private final java.util.concurrent.atomic.AtomicInteger receivedPongCount;

    /**
     * Optional guard.
     */
    private final GuardRule guard;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Lifecycle listener.
     */
    private final Listener<? super WebSocketSession> listener;

    /**
     * Maximum bytes allowed when materializing session payloads.
     */
    private final long materializeMaxBytes;

    /**
     * Creates a transport-less session for validated upgrade snapshots.
     *
     * @param address session address
     */
    WebSocketSession(final Address address) {
        this(address, null, null, null, null, null, null, Duration.ZERO, null, EventObserver.noop(), Wiring.noop(),
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param address session address
     * @param writer  native writer
     * @param reader  native reader
     * @param lease   native lease
     * @param handler native handler
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler) {
        this(address, writer, reader, lease, handler, null, null, Duration.ZERO, null, EventObserver.noop(),
                Wiring.noop(), Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param address     session address
     * @param writer      native writer
     * @param reader      native reader
     * @param lease       native lease
     * @param handler     native handler
     * @param dispatcher  dispatcher for reader loop
     * @param dispatchKey dispatch key
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler, final Dispatcher dispatcher, final String dispatchKey) {
        this(address, writer, reader, lease, handler, dispatcher, dispatchKey, Duration.ZERO, null,
                EventObserver.noop(), Wiring.noop(), Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param address     session address
     * @param writer      native writer
     * @param reader      native reader
     * @param lease       native lease
     * @param handler     native handler
     * @param dispatcher  dispatcher for reader loop
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
        this(address, writer, reader, lease, handler, dispatcher, dispatchKey, ping, guard, observer, listener,
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param address             session address
     * @param writer              native writer
     * @param reader              native reader
     * @param lease               native lease
     * @param handler             native handler
     * @param dispatcher          dispatcher for reader loop
     * @param dispatchKey         dispatch key
     * @param ping                ping interval
     * @param guard               optional guard
     * @param observer            observer
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    WebSocketSession(final Address address, final WebSocketWriter writer, final WebSocketReader reader,
            final ConnectionLease lease, final Handler handler, final Dispatcher dispatcher, final String dispatchKey,
            final Duration ping, final GuardRule guard, final EventObserver observer,
            final Listener<? super WebSocketSession> listener, final long materializeMaxBytes) {
        if (address == null) {
            throw new ValidateException("WebSocket address must not be null");
        }
        if (reader != null && handler == null) {
            throw new ValidateException("Handler must not be null");
        }
        if (reader != null && dispatcher == null) {
            throw new ValidateException("Dispatcher must not be null");
        }
        this.address = address;
        this.writer = writer;
        this.reader = reader;
        this.lease = lease;
        this.state = new AtomicReference<>(Status.OPENED);
        this.closeNotified = new AtomicBoolean();
        this.awaitingPong = new AtomicBoolean();
        this.sentPingCount = new java.util.concurrent.atomic.AtomicInteger();
        this.receivedPingCount = new java.util.concurrent.atomic.AtomicInteger();
        this.receivedPongCount = new java.util.concurrent.atomic.AtomicInteger();
        this.readerHandle = new AtomicReference<>();
        this.pingHandle = new AtomicReference<>();
        this.closeTimeoutHandle = new AtomicReference<>();
        this.dispatcher = dispatcher;
        this.dispatchKey = dispatchKey;
        this.guard = guard;
        this.observer = EventObserver.safe(observer);
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, this.observer);
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        if (reader != null) {
            readerHandle.set(startReader(handler, dispatcher, dispatchKey));
        }
        pingHandle.set(startPing(validatePing(ping), dispatcher, dispatchKey));
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
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns currently queued outbound payload bytes.
     *
     * @return queued bytes
     */
    public long queueSize() {
        return writer == null ? 0L : writer.queuedBytes();
    }

    /**
     * Returns the close timeout used for live sessions.
     *
     * @return close timeout
     */
    public Duration closeTimeout() {
        return CANCEL_AFTER_CLOSE;
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
     * Sends a generic payload as a binary message.
     *
     * @param payload payload
     * @return send call
     */
    @Override
    public Call<Void> send(final Payload payload) {
        if (payload == null) {
            throw new ValidateException("WebSocket payload must not be null");
        }
        return send(ByteBuffer.wrap(payload.bytes(materializeMaxBytes)));
    }

    /**
     * Sends a WebSocket body using its text or binary message kind.
     *
     * @param body body
     * @return send call
     */
    public Call<Void> send(final WebSocketBody body) {
        if (body == null) {
            throw new ValidateException("WebSocket body must not be null");
        }
        ensureOpened();
        if (writer == null) {
            throw new StatefulException("WebSocket session has no transport");
        }
        final Payload payload = body.payload();
        checkGuard(payload, "websocket-write");
        checkQueueLimit(payload.length());
        return body.textMessage() ? writeNative(() -> writer.write(WebSocketFrame.text(body.textValue())), payload)
                : writeNative(() -> writer.write(WebSocketFrame.binary(body.binaryValue())), payload);
    }

    /**
     * Sends a text message.
     *
     * @param text text
     * @return send call
     */
    public Call<Void> send(final String text) {
        final String value = validateSendText(text);
        ensureOpened();
        final Payload payload = Payload.of(value, StandardCharsets.UTF_8);
        checkGuard(payload, "websocket-write");
        if (writer != null) {
            checkQueueLimit(payload.length());
            return writeNative(() -> writer.write(WebSocketFrame.text(value)), payload);
        }
        throw new StatefulException("WebSocket session has no transport");
    }

    /**
     * Sends a binary message.
     *
     * @param bytes binary bytes
     * @return send call
     */
    @Override
    public Call<Void> send(final ByteBuffer bytes) {
        if (bytes == null) {
            throw new ValidateException("WebSocket binary payload must not be null");
        }
        ensureOpened();
        final ByteBuffer duplicate = bytes.duplicate();
        final ByteBuffer copy = ByteBuffer.allocate(duplicate.remaining());
        copy.put(duplicate).flip();
        final Payload payload = Payload.of(bytes(copy.asReadOnlyBuffer()));
        checkGuard(payload, "websocket-write");
        if (writer != null) {
            checkQueueLimit(payload.length());
            return writeNative(() -> writer.write(WebSocketFrame.binary(copy.asReadOnlyBuffer())), payload);
        }
        throw new StatefulException("WebSocket session has no transport");
    }

    /**
     * Sends a ping.
     *
     * @param payload ping payload
     * @return send call
     */
    public Call<Void> ping(final ByteBuffer payload) {
        if (payload == null) {
            throw new ValidateException("WebSocket ping payload must not be null");
        }
        if (payload.remaining() > 125) {
            throw new ValidateException("WebSocket ping payload is too large");
        }
        ensureOpened();
        final ByteBuffer duplicate = payload.duplicate();
        final ByteBuffer copy = ByteBuffer.allocate(duplicate.remaining());
        copy.put(duplicate).flip();
        final Payload body = Payload.of(bytes(copy.asReadOnlyBuffer()));
        checkGuard(body, "websocket-ping");
        if (writer != null) {
            sentPingCount.incrementAndGet();
            return writeNative(() -> writer.ping(copy.asReadOnlyBuffer()), body);
        }
        throw new StatefulException("WebSocket session has no transport");
    }

    /**
     * Closes the session normally.
     *
     * @return true when state changed
     */
    @Override
    public boolean close() {
        return close(1000, Normal.EMPTY);
    }

    /**
     * Closes the session.
     *
     * @param code   close code
     * @param reason close reason
     * @return true when state changed
     */
    public boolean close(final int code, final String reason) {
        final WebSocketClose close = WebSocketClose.of(code, reason);
        if (beginClosing()) {
            Logger.info(
                    true,
                    LOG_TAG,
                    "WebSocket session close started: scheme={}, host={}, port={}, code={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    close.code());
            RuntimeException failure = null;
            if (writer != null) {
                try {
                    writer.writeClose(close.code(), close.reason());
                } catch (final RuntimeException e) {
                    failure = e;
                }
            }
            if (reader != null && dispatcher != null && StringKit.isNotBlank(dispatchKey)) {
                scheduleCloseTimeout();
            } else {
                completeClose(null);
                try {
                    closeNative();
                } catch (final RuntimeException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (failure != null) {
                Logger.warn(
                        false,
                        LOG_TAG,
                        failure,
                        "WebSocket session close failed: scheme={}, host={}, port={}, code={}, exception={}",
                        address.scheme(),
                        address.host(),
                        address.port(),
                        close.code(),
                        failure.getClass().getSimpleName());
                throw failure;
            }
            Logger.info(
                    false,
                    LOG_TAG,
                    "WebSocket session close requested: scheme={}, host={}, port={}, code={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    close.code());
            return true;
        }
        return false;
    }

    /**
     * Cancels the session.
     *
     * @return true when state changed
     */
    @Override
    public boolean cancel() {
        while (true) {
            final Status current = state.get();
            if (current == Status.CANCELLED || current == Status.CLOSED || current == Status.DONE) {
                return false;
            }
            if (state.compareAndSet(current, Status.CANCELLED)) {
                Logger.info(
                        true,
                        LOG_TAG,
                        "WebSocket session cancel started: scheme={}, host={}, port={}",
                        address.scheme(),
                        address.host(),
                        address.port());
                awaitingPong.set(false);
                closeNative();
                emit(ObservationMarker.WEBSOCKET_FAILED, Payload.empty(), null);
                listener.failure(this, new StatefulException("WebSocket session was cancelled"));
                Logger.info(
                        false,
                        LOG_TAG,
                        "WebSocket session cancelled: scheme={}, host={}, port={}",
                        address.scheme(),
                        address.host(),
                        address.port());
                return true;
            }
        }
    }

    /**
     * Returns whether the session is open.
     *
     * @return true when opened
     */
    @Override
    public boolean opened() {
        final Status current = state.get();
        return current == Status.OPENED || current == Status.RUNNING;
    }

    /**
     * Returns session attributes.
     *
     * @return empty attribute snapshot
     */
    @Override
    public Map<String, Object> attributes() {
        return Map.of("observer", observer);
    }

    /**
     * Writes a native frame and exposes the completed call.
     *
     * @param action write action
     * @return send call
     */
    private Call<Void> writeNative(final Runnable action, final Payload payload) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            action.run();
            future.complete(null);
            emit(ObservationMarker.WEBSOCKET_MESSAGE, payload, null);
            Logger.debug(
                    false,
                    LOG_TAG,
                    "WebSocket message sent: scheme={}, host={}, port={}, bytes={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    payload.length());
        } catch (final RuntimeException e) {
            future.completeExceptionally(e);
            emit(ObservationMarker.WEBSOCKET_FAILED, payload, e);
            Logger.warn(
                    false,
                    LOG_TAG,
                    e,
                    "WebSocket message send failed: scheme={}, host={}, port={}, bytes={}, exception={}",
                    address.scheme(),
                    address.host(),
                    address.port(),
                    payload.length(),
                    e.getClass().getSimpleName());
            throw e;
        }
        return new SessionCall(future, future);
    }

    /**
     * Checks the optional guard.
     *
     * @param payload payload
     * @param tag     direction tag
     */
    private void checkGuard(final Payload payload, final String tag) {
        if (guard != null) {
            guard.check(Message.of(address.protocol(), address, Headers.empty(), payload, tag)).throwIfRejected();
        }
    }

    /**
     * Emits a WebSocket event.
     *
     * @param marker  marker
     * @param payload payload
     * @param cause   failure cause
     */
    private void emit(final ObservationMarker marker, final Payload payload, final Throwable cause) {
        final FabricEvent.Builder event = FabricEvent.builder(marker).tag(Tags.PROTOCOL, address.scheme())
                .tag(Tags.HOST, address.host()).tag(Tags.PORT, Integer.toString(address.port()));
        if (payload != null && payload.length() >= 0) {
            event.tag(Tags.BYTES, Long.toString(payload.length()));
        }
        event.tag("queueBytes", Long.toString(queueSize())).tag("sentPingCount", Integer.toString(sentPingCount()))
                .tag("receivedPingCount", Integer.toString(receivedPingCount()))
                .tag("receivedPongCount", Integer.toString(receivedPongCount()));
        if (cause != null) {
            event.cause(cause);
        }
        observer.emit(event.build());
    }

    /**
     * Enforces the outbound queue limit.
     *
     * @param length message length
     */
    private void checkQueueLimit(final long length) {
        if (length < 0) {
            return;
        }
        if (length > MAX_QUEUE_SIZE || queueSize() + length > MAX_QUEUE_SIZE) {
            if (opened()) {
                close(1001, "queue full");
            }
            throw new StatefulException("WebSocket write queue is full");
        }
    }

    /**
     * Schedules cancel after a graceful close timeout.
     */
    private void scheduleCloseTimeout() {
        final DispatchHandle next = dispatcher
                .schedule(dispatchKey, CANCEL_AFTER_CLOSE, Activity.of("websocket-close-timeout", () -> {
                    if (state.get() == Status.CLOSING) {
                        cancel();
                    }
                }));
        final DispatchHandle previous = closeTimeoutHandle.getAndSet(next);
        if (previous != null) {
            previous.cancel();
        }
    }

    /**
     * Copies buffer bytes.
     *
     * @param buffer buffer
     * @return bytes
     */
    private static byte[] bytes(final ByteBuffer buffer) {
        final ByteBuffer view = buffer.duplicate();
        final byte[] data = new byte[view.remaining()];
        view.get(data);
        return data;
    }

    /**
     * Validates text for send.
     *
     * @param text text
     * @return text
     */
    private static String validateSendText(final String text) {
        if (StringKit.isBlank(text) || StringKit.containsAny(text, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket text must be non-blank and single-line");
        }
        text.getBytes(StandardCharsets.UTF_8);
        return text;
    }

    /**
     * Validates a ping interval.
     *
     * @param interval interval
     * @return interval
     */
    private static Duration validatePing(final Duration interval) {
        if (interval == null || interval.isNegative()) {
            throw new ValidateException("WebSocket ping interval must be non-null and non-negative");
        }
        return interval;
    }

    /**
     * Ensures the session is open.
     */
    private void ensureOpened() {
        if (!opened()) {
            throw new StatefulException("WebSocket session is not open");
        }
    }

    /**
     * Starts the native reader activity.
     *
     * @param handler     handler
     * @param dispatcher  dispatcher
     * @param dispatchKey dispatch key
     * @return dispatch handle
     */
    private DispatchHandle startReader(final Handler handler, final Dispatcher dispatcher, final String dispatchKey) {
        if (StringKit.isBlank(dispatchKey) || StringKit.containsAny(dispatchKey, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket dispatch key must be non-blank and single-line");
        }
        final Activity activity = Activity.of("websocket-read", () -> {
            final AtomicBoolean delivered = new AtomicBoolean();
            try {
                reader.readLoop(WebSocketSession.this, new Handler() {

                    @Override
                    public void message(final Session session, final org.miaixz.bus.fabric.Message message) {
                        delivered.set(true);
                        checkGuard(message.payload(), "websocket-read");
                        emit(ObservationMarker.WEBSOCKET_MESSAGE, message.payload(), null);
                        Logger.debug(
                                false,
                                LOG_TAG,
                                "WebSocket message received: scheme={}, host={}, port={}, bytes={}",
                                address.scheme(),
                                address.host(),
                                address.port(),
                                message.payload().length());
                        handler.message(session, message);
                    }

                    @Override
                    public void closed(final Session session) {
                        handler.closed(session);
                    }

                    @Override
                    public void failure(final Session session, final Throwable cause) {
                        handler.failure(session, cause);
                    }
                }, new WebSocketReader.Control() {

                    @Override
                    public void ping(final Session session, final ByteBuffer payload) {
                        receivedPingCount.incrementAndGet();
                        Logger.debug(
                                false,
                                LOG_TAG,
                                "WebSocket ping received: scheme={}, host={}, port={}, bytes={}",
                                address.scheme(),
                                address.host(),
                                address.port(),
                                payload.remaining());
                        if (writer != null && opened()) {
                            writer.pong(payload);
                        }
                    }

                    @Override
                    public void pong(final Session session, final ByteBuffer payload) {
                        receivedPongCount.incrementAndGet();
                        awaitingPong.set(false);
                        Logger.debug(
                                false,
                                LOG_TAG,
                                "WebSocket pong received: scheme={}, host={}, port={}, bytes={}",
                                address.scheme(),
                                address.host(),
                                address.port(),
                                payload.remaining());
                    }

                    @Override
                    public void close(final Session session, final WebSocketClose close) {
                        closeFromPeer(close);
                    }

                });
                completeClose(handler);
            } catch (final RuntimeException e) {
                if (!delivered.get() && !state.get().terminal()) {
                    state.set(Status.FAILED);
                    emit(ObservationMarker.WEBSOCKET_FAILED, Payload.empty(), e);
                    handler.failure(WebSocketSession.this, e);
                    listener.failure(WebSocketSession.this, e);
                }
            } finally {
                closeNative();
            }
        });
        return dispatcher.enqueue(dispatchKey, activity);
    }

    /**
     * Starts automatic ping scheduling.
     *
     * @param interval    ping interval
     * @param dispatcher  runtime dispatcher
     * @param dispatchKey dispatch key
     * @return dispatch handle or null
     */
    private DispatchHandle startPing(final Duration interval, final Dispatcher dispatcher, final String dispatchKey) {
        if (interval.isZero() || writer == null || !opened()) {
            return null;
        }
        if (dispatcher == null) {
            throw new ValidateException("Dispatcher must not be null when WebSocket ping is enabled");
        }
        return dispatcher.schedule(dispatchKey, interval, Activity.of("websocket-ping", () -> {
            scheduledPing();
            if (opened()) {
                final DispatchHandle current = pingHandle.get();
                if (current != null) {
                    final DispatchHandle next = startPing(interval, dispatcher, dispatchKey);
                    if (!pingHandle.compareAndSet(current, next) && next != null) {
                        next.cancel();
                    }
                }
            }
        }));
    }

    /**
     * Sends a scheduled ping.
     */
    private void scheduledPing() {
        if (!opened()) {
            return;
        }
        try {
            if (!awaitingPong.compareAndSet(false, true)) {
                final TimeoutException timeout = new TimeoutException("WebSocket pong timeout");
                state.set(Status.FAILED);
                emit(ObservationMarker.WEBSOCKET_FAILED, Payload.empty(), timeout);
                Logger.debug(
                        false,
                        LOG_TAG,
                        "WebSocket scheduled ping timed out: scheme={}, host={}, port={}",
                        address.scheme(),
                        address.host(),
                        address.port());
                listener.failure(this, timeout);
                closeNative();
                return;
            }
            Logger.debug(
                    true,
                    LOG_TAG,
                    "WebSocket scheduled ping started: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
            ping(ByteBuffer.allocate(0));
        } catch (final RuntimeException e) {
            awaitingPong.set(false);
            if (!state.get().terminal()) {
                state.set(Status.FAILED);
                emit(ObservationMarker.WEBSOCKET_FAILED, Payload.empty(), e);
                listener.failure(this, e);
            }
            closeNative();
        }
    }

    /**
     * Closes native resources.
     */
    private void closeNative() {
        RuntimeException failure = null;
        awaitingPong.set(false);
        final DispatchHandle ping = pingHandle.getAndSet(null);
        if (ping != null) {
            ping.cancel();
        }
        final DispatchHandle closeTimeout = closeTimeoutHandle.getAndSet(null);
        if (closeTimeout != null) {
            closeTimeout.cancel();
        }
        final DispatchHandle handle = readerHandle.getAndSet(null);
        if (handle != null) {
            handle.cancel();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (final RuntimeException e) {
                failure = e;
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (final RuntimeException e) {
                failure = failure == null ? e : failure;
            }
        }
        if (lease != null) {
            try {
                lease.close();
            } catch (final RuntimeException e) {
                failure = failure == null ? e : failure;
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Begins a close transition.
     *
     * @return true when this caller owns the close transition
     */
    private boolean beginClosing() {
        while (true) {
            final Status current = state.get();
            if (current == Status.CLOSING || current.terminal()) {
                return false;
            }
            if (state.compareAndSet(current, Status.CLOSING)) {
                return true;
            }
        }
    }

    /**
     * Replies to a peer close frame.
     *
     * @param close peer close description
     */
    private void closeFromPeer(final WebSocketClose close) {
        Logger.info(
                true,
                LOG_TAG,
                "WebSocket peer close received: scheme={}, host={}, port={}, code={}",
                address.scheme(),
                address.host(),
                address.port(),
                close.code());
        if (beginClosing() && writer != null) {
            writer.writeClose(close.code(), close.reason());
        }
        completeClose(null);
        closeNative();
    }

    /**
     * Completes close state and notifies observers once.
     *
     * @param handler optional session handler
     */
    private void completeClose(final Handler handler) {
        state.set(Status.CLOSED);
        awaitingPong.set(false);
        if (closeNotified.compareAndSet(false, true)) {
            emit(ObservationMarker.WEBSOCKET_CLOSED, Payload.empty(), null);
            if (handler != null) {
                handler.closed(this);
            }
            listener.close(this);
            Logger.info(
                    false,
                    LOG_TAG,
                    "WebSocket session closed: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
        }
    }

    /**
     * Future-backed send call.
     */
    private static final class SessionCall implements Call<Void> {

        /**
         * Result future.
         */
        private final CompletableFuture<Void> future;

        /**
         * Source future.
         */
        private final CompletableFuture<?> source;

        /**
         * Creates a call.
         *
         * @param future result future
         * @param source source future
         */
        private SessionCall(final CompletableFuture<Void> future, final CompletableFuture<?> source) {
            this.future = future;
            this.source = source;
        }

        /**
         * Waits for the already-started send to complete.
         *
         * @return null
         */
        @Override
        public Void execute() {
            return await();
        }

        /**
         * Returns this already-started send call.
         *
         * @return this call
         */
        @Override
        public Call<Void> enqueue() {
            return this;
        }

        /**
         * Waits for completion.
         *
         * @return null
         */
        @Override
        public Void await() {
            try {
                return future.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for WebSocket send", e);
            } catch (final ExecutionException e) {
                throw new InternalException("WebSocket send failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("WebSocket send was cancelled", e);
            }
        }

        /**
         * Waits for completion within a timeout.
         *
         * @param timeout timeout
         * @return null
         */
        @Override
        public Void await(final Duration timeout) {
            validateTimeout(timeout);
            if (timeout.isZero()) {
                if (!future.isDone()) {
                    cancel();
                    throw new TimeoutException("WebSocket send timed out");
                }
                return await();
            }
            try {
                return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InternalException("Interrupted while waiting for WebSocket send", e);
            } catch (final ExecutionException e) {
                throw new InternalException("WebSocket send failed", e.getCause());
            } catch (final CancellationException e) {
                throw new InternalException("WebSocket send was cancelled", e);
            } catch (final java.util.concurrent.TimeoutException e) {
                cancel();
                throw new TimeoutException("WebSocket send timed out", e);
            } catch (final ArithmeticException e) {
                throw new ValidateException("Timeout is too large");
            }
        }

        @Override
        public boolean cancel() {
            final boolean cancelled = source.cancel(false);
            future.cancel(false);
            return cancelled || future.isCancelled();
        }

        @Override
        public boolean cancelled() {
            return future.isCancelled() || source.isCancelled();
        }

        @Override
        public boolean done() {
            return future.isDone();
        }

        /**
         * Validates timeout.
         *
         * @param timeout timeout
         */
        private static void validateTimeout(final Duration timeout) {
            if (timeout == null || timeout.isNegative()) {
                throw new ValidateException("Timeout must be non-null and non-negative");
            }
        }

    }

}
