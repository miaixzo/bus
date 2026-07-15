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
package org.miaixz.bus.fabric.protocol.stomp;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tags.Tags;
import org.miaixz.bus.fabric.protocol.stomp.body.StompBody;
import org.miaixz.bus.fabric.protocol.stomp.broker.StompReceipt;
import org.miaixz.bus.fabric.protocol.stomp.broker.StompTopic;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompCodec;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompFrame;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.logger.Logger;

/**
 * Open STOMP session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompSession {

    /**
     * STOMP SEND command.
     */
    private static final String COMMAND_SEND = "SEND";

    /**
     * STOMP SUBSCRIBE command.
     */
    private static final String COMMAND_SUBSCRIBE = "SUBSCRIBE";

    /**
     * STOMP UNSUBSCRIBE command.
     */
    private static final String COMMAND_UNSUBSCRIBE = "UNSUBSCRIBE";

    /**
     * STOMP DISCONNECT command.
     */
    private static final String COMMAND_DISCONNECT = "DISCONNECT";

    /**
     * STOMP RECEIPT command.
     */
    private static final String COMMAND_RECEIPT = "RECEIPT";

    /**
     * STOMP ERROR command.
     */
    private static final String COMMAND_ERROR = "ERROR";

    /**
     * STOMP MESSAGE command.
     */
    private static final String COMMAND_MESSAGE = "MESSAGE";

    /**
     * STOMP ACK command.
     */
    private static final String COMMAND_ACK = "ACK";

    /**
     * STOMP NACK command.
     */
    private static final String COMMAND_NACK = "NACK";

    /**
     * STOMP destination header.
     */
    private static final String HEADER_DESTINATION = "destination";

    /**
     * STOMP content-length header.
     */
    private static final String HEADER_CONTENT_LENGTH = "content-length";

    /**
     * STOMP content-type header.
     */
    private static final String HEADER_CONTENT_TYPE = "content-type";

    /**
     * STOMP id header.
     */
    private static final String HEADER_ID = "id";

    /**
     * STOMP receipt-id header.
     */
    private static final String HEADER_RECEIPT_ID = "receipt-id";

    /**
     * STOMP message-id header.
     */
    private static final String HEADER_MESSAGE_ID = "message-id";

    /**
     * STOMP subscription header.
     */
    private static final String HEADER_SUBSCRIPTION = "subscription";

    /**
     * Guard tag for inbound frames.
     */
    private static final String TAG_READ = "stomp-read";

    /**
     * Guard tag for outbound frames.
     */
    private static final String TAG_WRITE = "stomp-write";

    /**
     * Fallback log value for sessions without an address.
     */
    private static final String UNKNOWN = "unknown";

    /**
     * Old topic destination prefix.
     */
    public static final String TOPIC_PREFIX = "/topic";

    /**
     * Old queue destination prefix.
     */
    public static final String QUEUE_PREFIX = "/queue";

    /**
     * Frame sender.
     */
    private final Function<Buffer, Call<Void>> sender;

    /**
     * Close hook.
     */
    private final Runnable closeHook;

    /**
     * Cancel hook.
     */
    private final Runnable cancelHook;

    /**
     * Outbound codec.
     */
    private final StompCodec codec;

    /**
     * Topic handlers keyed by subscription id.
     */
    private final Map<String, Subscription> topics;

    /**
     * Receipt registry.
     */
    private final StompReceipt receipts;

    /**
     * Default inbound message handler.
     */
    private final Consumer<StompMessage> handler;

    /**
     * Address used for guard and observe messages.
     */
    private final Address address;

    /**
     * Optional guard.
     */
    private final GuardRule guard;

    /**
     * Optional message filter.
     */
    private final Filter filter;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Lifecycle listener.
     */
    private final Listener<? super StompSession> listener;

    /**
     * Maximum bytes allowed when materializing session payloads.
     */
    private final long materializeMaxBytes;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Close notification guard.
     */
    private final AtomicBoolean closeNotified;

    /**
     * Creates an opened session.
     *
     * @param sender     sender
     * @param closeHook  close hook
     * @param cancelHook cancel hook
     * @param handler    default message handler
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
                 final Consumer<StompMessage> handler) {
        this(sender, closeHook, cancelHook, handler, null, null, EventObserver.noop(), null, Wiring.noop(),
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param sender     sender
     * @param closeHook  close hook
     * @param cancelHook cancel hook
     * @param handler    default message handler
     * @param address    session address
     * @param guard      optional guard
     * @param observer   observer
     * @param listener   lifecycle listener
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
                 final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
                 final EventObserver observer, final Listener<? super StompSession> listener) {
        this(sender, closeHook, cancelHook, handler, address, guard, observer, null, listener,
                Options.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param sender              sender
     * @param closeHook           close hook
     * @param cancelHook          cancel hook
     * @param handler             default message handler
     * @param address             session address
     * @param guard               optional guard
     * @param observer            observer
     * @param filter              optional filter
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
                 final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
                 final EventObserver observer, final Filter filter, final Listener<? super StompSession> listener,
                 final long materializeMaxBytes) {
        this.sender = require(sender, "STOMP sender");
        this.closeHook = closeHook == null ? () -> {
        } : closeHook;
        this.cancelHook = cancelHook == null ? () -> {
        } : cancelHook;
        this.codec = new StompCodec();
        this.topics = new LinkedHashMap<>();
        this.receipts = new StompReceipt();
        this.handler = handler == null ? message -> {
        } : handler;
        this.address = address;
        this.guard = guard;
        this.filter = filter;
        this.observer = EventObserver.safe(observer);
        this.listener = Wiring.safe(listener == null ? Wiring.noop() : listener, this.observer);
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        this.state = new AtomicReference<>(Status.OPENED);
        this.closeNotified = new AtomicBoolean();
    }

    /**
     * Sends a STOMP message.
     *
     * @param destination destination
     * @param payload     payload
     * @return send call
     */
    public Call<Void> send(final String destination, final Payload payload) {
        ensureOpen();
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        require(payload, "STOMP payload");
        final Payload outgoing = snapshot(payload);
        final Headers headers = Headers.builder().add(HEADER_DESTINATION, target)
                .add(HEADER_CONTENT_LENGTH, Long.toString(outgoing.length())).build();
        Logger.info(
                false,
                "Fabric",
                "STOMP send requested: scheme={}, host={}, port={}, destination={}, bytes={}",
                scheme(),
                host(),
                port(),
                target,
                outgoing.length());
        return write(StompFrame.of(COMMAND_SEND, headers, outgoing));
    }

    /**
     * Sends text to a destination.
     *
     * @param destination destination
     * @param data        text data
     * @return send call
     */
    public Call<Void> sendTo(final String destination, final String data) {
        return send(
                destination,
                Payload.of(data == null ? Normal.EMPTY : data, java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Sends payload to a destination.
     *
     * @param destination destination
     * @param payload     payload
     * @return send call
     */
    public Call<Void> sendTo(final String destination, final Payload payload) {
        return send(destination, payload);
    }

    /**
     * Sends a STOMP message body.
     *
     * @param destination destination
     * @param body        body
     * @return send call
     */
    public Call<Void> send(final String destination, final StompBody body) {
        ensureOpen();
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        require(body, "STOMP body");
        final Payload outgoing = snapshot(body.payload());
        final Headers headers = Headers.builder().add(HEADER_DESTINATION, target)
                .add(HEADER_CONTENT_TYPE, body.media().toString())
                .add(HEADER_CONTENT_LENGTH, Long.toString(outgoing.length())).build();
        Logger.info(
                false,
                "Fabric",
                "STOMP body send requested: scheme={}, host={}, port={}, destination={}, media={}, bytes={}",
                scheme(),
                host(),
                port(),
                target,
                body.media(),
                outgoing.length());
        return write(StompFrame.of(COMMAND_SEND, headers, outgoing));
    }

    /**
     * Subscribes to a destination.
     *
     * @param destination destination
     * @param handler     message handler
     * @return topic
     */
    public StompTopic subscribe(final String destination, final Consumer<StompMessage> handler) {
        return subscribe(destination, Headers.empty(), handler);
    }

    /**
     * Subscribes to a destination with extra SUBSCRIBE headers.
     *
     * @param destination destination
     * @param headers     extra headers
     * @param handler     message handler
     * @return topic
     */
    public StompTopic subscribe(final String destination, final Headers headers, final Consumer<StompMessage> handler) {
        ensureOpen();
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        Assert.notNull(handler, () -> new ValidateException("STOMP subscription handler must not be null"));
        final Headers extraHeaders = headers == null ? Headers.empty() : headers;
        final StompTopic topic = StompTopic.of(UUID.randomUUID().toString(), target);
        final Headers.Builder builder = Headers.builder().add(HEADER_ID, topic.id())
                .add(HEADER_DESTINATION, topic.destination());
        extraHeaders.asMap().forEach((name, values) -> {
            if (!HEADER_ID.equalsIgnoreCase(name) && !HEADER_DESTINATION.equalsIgnoreCase(name)) {
                values.forEach(value -> builder.add(name, value));
            }
        });
        synchronized (topics) {
            topics.put(topic.id(), new Subscription(topic, handler));
        }
        try {
            write(StompFrame.of(COMMAND_SUBSCRIBE, builder.build(), Payload.empty()));
            Logger.info(
                    false,
                    "Fabric",
                    "STOMP subscription created: scheme={}, host={}, port={}, destination={}, subscriptionId={}, activeSubscriptions={}",
                    scheme(),
                    host(),
                    port(),
                    topic.destination(),
                    topic.id(),
                    subscriptionCount());
        } catch (final RuntimeException e) {
            synchronized (topics) {
                topics.remove(topic.id());
            }
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "STOMP subscription failed: scheme={}, host={}, port={}, destination={}, exception={}",
                    scheme(),
                    host(),
                    port(),
                    topic.destination(),
                    e.getClass().getSimpleName());
            throw e;
        }
        return topic;
    }

    /**
     * Subscribes to a topic destination.
     *
     * @param destination topic suffix or absolute topic destination
     * @param handler     message handler
     * @return topic
     */
    public StompTopic topic(final String destination, final Consumer<StompMessage> handler) {
        return topic(destination, Headers.empty(), handler);
    }

    /**
     * Subscribes to a topic destination with extra headers.
     *
     * @param destination topic suffix or absolute topic destination
     * @param headers     extra headers
     * @param handler     message handler
     * @return topic
     */
    public StompTopic topic(final String destination, final Headers headers, final Consumer<StompMessage> handler) {
        return subscribeOnce(prefixed(TOPIC_PREFIX, destination), headers, handler);
    }

    /**
     * Subscribes to a queue destination.
     *
     * @param destination queue suffix or absolute queue destination
     * @param handler     message handler
     * @return topic
     */
    public StompTopic queue(final String destination, final Consumer<StompMessage> handler) {
        return queue(destination, Headers.empty(), handler);
    }

    /**
     * Subscribes to a queue destination with extra headers.
     *
     * @param destination queue suffix or absolute queue destination
     * @param headers     extra headers
     * @param handler     message handler
     * @return topic
     */
    public StompTopic queue(final String destination, final Headers headers, final Consumer<StompMessage> handler) {
        return subscribeOnce(prefixed(QUEUE_PREFIX, destination), headers, handler);
    }

    /**
     * Registers a receipt wait future.
     *
     * @param receiptId receipt id
     * @return receipt future
     */
    public CompletableFuture<Void> receipt(final String receiptId) {
        ensureOpen();
        final CompletableFuture<Void> future = new CompletableFuture<>();
        receipts.waitFor(receiptId, future);
        return future;
    }

    /**
     * Acknowledges a message.
     *
     * @param messageId message id
     * @return send call
     */
    public Call<Void> ack(final String messageId) {
        return acknowledge(COMMAND_ACK, messageId);
    }

    /**
     * Acknowledges a received message.
     *
     * @param message message
     * @return send call
     */
    public Call<Void> ack(final StompMessage message) {
        return acknowledge(COMMAND_ACK, message);
    }

    /**
     * Rejects a message.
     *
     * @param messageId message id
     * @return send call
     */
    public Call<Void> nack(final String messageId) {
        return acknowledge(COMMAND_NACK, messageId);
    }

    /**
     * Rejects a received message.
     *
     * @param message message
     * @return send call
     */
    public Call<Void> nack(final StompMessage message) {
        return acknowledge(COMMAND_NACK, message);
    }

    /**
     * Unsubscribes a topic.
     *
     * @param topic topic
     * @return true when removed
     */
    public boolean unsubscribe(final StompTopic topic) {
        Assert.notNull(topic, () -> new ValidateException("STOMP topic must not be null"));
        if (!opened()) {
            return false;
        }
        final Subscription removed;
        synchronized (topics) {
            removed = topics.remove(topic.id());
        }
        if (removed == null) {
            return false;
        }
        write(
                StompFrame.of(
                        COMMAND_UNSUBSCRIBE,
                        Headers.builder().add(HEADER_ID, topic.id()).build(),
                        Payload.empty()));
        Logger.info(
                false,
                "Fabric",
                "STOMP subscription removed: scheme={}, host={}, port={}, destination={}, subscriptionId={}, activeSubscriptions={}",
                scheme(),
                host(),
                port(),
                topic.destination(),
                topic.id(),
                subscriptionCount());
        return true;
    }

    /**
     * Unsubscribes by destination.
     *
     * @param destination destination
     * @return true when removed
     */
    public boolean unsubscribe(final String destination) {
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        final StompTopic topic;
        synchronized (topics) {
            topic = topics.values().stream().map(Subscription::topic)
                    .filter(current -> current.destination().equals(target)).findFirst().orElse(null);
        }
        return topic != null && unsubscribe(topic);
    }

    /**
     * Unsubscribes a topic destination.
     *
     * @param destination topic suffix or absolute topic destination
     * @return true when removed
     */
    public boolean untopic(final String destination) {
        return unsubscribe(prefixed(TOPIC_PREFIX, destination));
    }

    /**
     * Unsubscribes a queue destination.
     *
     * @param destination queue suffix or absolute queue destination
     * @return true when removed
     */
    public boolean unqueue(final String destination) {
        return unsubscribe(prefixed(QUEUE_PREFIX, destination));
    }

    /**
     * Closes this session.
     *
     * @return true when state changed
     */
    public boolean close() {
        if (transition(Status.CLOSED)) {
            final StatefulException closeCause = new StatefulException("STOMP session was closed");
            Logger.info(
                    true,
                    "Fabric",
                    "STOMP session close started: scheme={}, host={}, port={}",
                    scheme(),
                    host(),
                    port());
            try {
                write(StompFrame.of(COMMAND_DISCONNECT, Headers.empty(), Payload.empty()));
            } catch (final RuntimeException ignored) {
                // The close hook still owns transport cleanup after a best-effort DISCONNECT.
            } finally {
                cleanup(closeCause);
                closeHook.run();
            }
            notifyClosed();
            Logger.info(false, "Fabric", "STOMP session closed: scheme={}, host={}, port={}", scheme(), host(), port());
            return true;
        }
        return false;
    }

    /**
     * Cancels this session.
     *
     * @return true when state changed
     */
    public boolean cancel() {
        while (true) {
            final Status current = state.get();
            if (current.terminal()) {
                return false;
            }
            if (state.compareAndSet(current, Status.CANCELLED)) {
                final StatefulException cancelled = new StatefulException("STOMP session was cancelled");
                Logger.info(
                        true,
                        "Fabric",
                        "STOMP session cancel started: scheme={}, host={}, port={}",
                        scheme(),
                        host(),
                        port());
                cleanup(cancelled);
                cancelHook.run();
                emit(ObservationMarker.STOMP_FAILED, Payload.empty(), null);
                listener.failure(this, cancelled);
                Logger.info(
                        false,
                        "Fabric",
                        "STOMP session cancelled: scheme={}, host={}, port={}",
                        scheme(),
                        host(),
                        port());
                return true;
            }
        }
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns whether this session is opened.
     *
     * @return true when opened
     */
    boolean opened() {
        final Status current = state.get();
        return current == Status.OPENED || current == Status.RUNNING;
    }

    /**
     * Returns active subscription count.
     *
     * @return subscription count
     */
    int subscriptionCount() {
        synchronized (topics) {
            return topics.size();
        }
    }

    /**
     * Returns pending receipt count.
     *
     * @return receipt count
     */
    int receiptCount() {
        return receipts.size();
    }

    /**
     * Dispatches an inbound frame.
     *
     * @param frame frame
     */
    void dispatch(final StompFrame frame) {
        require(frame, "STOMP frame");
        if (COMMAND_RECEIPT.equals(frame.command())) {
            final String receiptId = frame.headers().get(HEADER_RECEIPT_ID);
            if (receiptId != null) {
                receipts.complete(receiptId);
                Logger.debug(
                        false,
                        "Fabric",
                        "STOMP receipt completed: scheme={}, host={}, port={}, receiptId={}",
                        scheme(),
                        host(),
                        port(),
                        receiptId);
            }
            return;
        }
        if (COMMAND_ERROR.equals(frame.command())) {
            final ProtocolException failure = new ProtocolException(
                    frame.body().text(java.nio.charset.StandardCharsets.UTF_8));
            fail(failure);
            Logger.warn(
                    false,
                    "Fabric",
                    failure,
                    "STOMP ERROR frame received: scheme={}, host={}, port={}",
                    scheme(),
                    host(),
                    port());
            throw failure;
        }
        if (!COMMAND_MESSAGE.equals(frame.command())) {
            return;
        }
        final StompFrame received = filter(frame, TAG_READ);
        final String destination = received.headers().get(HEADER_DESTINATION);
        final StompMessage message = StompMessage.of(destination, received.headers(), received.body());
        checkGuard(received, TAG_READ);
        emit(ObservationMarker.STOMP_MESSAGE, received.body(), null);
        Logger.debug(
                false,
                "Fabric",
                "STOMP message received: scheme={}, host={}, port={}, destination={}, bytes={}",
                scheme(),
                host(),
                port(),
                destination,
                received.body().length());
        accept(handler, message, received.body());
        final List<Subscription> snapshot;
        synchronized (topics) {
            snapshot = new ArrayList<>(topics.values());
        }
        for (final Subscription subscription : snapshot) {
            if (subscription.topic.matches(message)) {
                accept(subscription.handler, message, received.body());
            }
        }
    }

    /**
     * Transitions to a terminal state.
     *
     * @param next next terminal state
     * @return true when state changed
     */
    private boolean transition(final Status next) {
        while (true) {
            final Status current = state.get();
            if (current.terminal()) {
                return false;
            }
            if (state.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    /**
     * Fails the session and clears broker state.
     *
     * @param cause failure cause
     */
    private void fail(final RuntimeException cause) {
        if (transition(Status.FAILED)) {
            cleanup(cause);
            emit(ObservationMarker.STOMP_FAILED, Payload.empty(), cause);
            listener.failure(this, cause);
            Logger.warn(
                    false,
                    "Fabric",
                    cause,
                    "STOMP session failed: scheme={}, host={}, port={}, exception={}",
                    scheme(),
                    host(),
                    port(),
                    cause.getClass().getSimpleName());
        }
    }

    /**
     * Clears local broker state.
     *
     * @param cause terminal cause used for pending receipts
     */
    private void cleanup(final Throwable cause) {
        synchronized (topics) {
            topics.clear();
        }
        receipts.failAll(cause);
    }

    /**
     * Notifies close observers once.
     */
    private void notifyClosed() {
        if (closeNotified.compareAndSet(false, true)) {
            emit(ObservationMarker.STOMP_CLOSED, Payload.empty(), null);
            listener.close(this);
        }
    }

    /**
     * Invokes a message handler without allowing one callback to break delivery to other subscriptions.
     *
     * @param consumer consumer
     * @param message  message
     * @param body     event body
     */
    private void accept(final Consumer<StompMessage> consumer, final StompMessage message, final Payload body) {
        try {
            consumer.accept(message);
        } catch (final RuntimeException e) {
            emit(ObservationMarker.STOMP_FAILED, body, e);
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "STOMP message handler failed: scheme={}, host={}, port={}, destination={}, exception={}",
                    scheme(),
                    host(),
                    port(),
                    message.destination(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Sends an ACK or NACK frame.
     *
     * @param command   command
     * @param messageId message id
     * @return send call
     */
    private Call<Void> acknowledge(final String command, final String messageId) {
        ensureOpen();
        final String id = StompMessage.validateToken(messageId, "STOMP message id");
        Logger.debug(
                false,
                "Fabric",
                "STOMP acknowledgement requested: scheme={}, host={}, port={}, command={}",
                scheme(),
                host(),
                port(),
                command);
        return write(StompFrame.of(command, Headers.builder().add(HEADER_ID, id).build(), Payload.empty()));
    }

    /**
     * Sends an ACK or NACK frame for a received message.
     *
     * @param command command
     * @param message message
     * @return send call
     */
    private Call<Void> acknowledge(final String command, final StompMessage message) {
        ensureOpen();
        final StompMessage current = require(message, "STOMP message");
        final String id = current.headers().get(HEADER_MESSAGE_ID);
        final String subscription = current.headers().get(HEADER_SUBSCRIPTION);
        final Headers.Builder headers = Headers.builder()
                .add(HEADER_ID, StompMessage.validateToken(id, "STOMP message id"));
        if (subscription != null) {
            headers.add(HEADER_SUBSCRIPTION, StompMessage.validateToken(subscription, "STOMP subscription id"));
        }
        return write(StompFrame.of(command, headers.build(), Payload.empty()));
    }

    /**
     * Subscribes once by destination for topic and queue helpers.
     *
     * @param destination destination
     * @param headers     extra headers
     * @param handler     handler
     * @return topic
     */
    private StompTopic subscribeOnce(
            final String destination,
            final Headers headers,
            final Consumer<StompMessage> handler) {
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        synchronized (topics) {
            for (final Subscription subscription : topics.values()) {
                if (subscription.topic().destination().equals(target)) {
                    return subscription.topic();
                }
            }
        }
        return subscribe(target, headers, handler);
    }

    /**
     * Applies a destination prefix when absent.
     *
     * @param prefix      prefix
     * @param destination destination
     * @return normalized destination
     */
    private static String prefixed(final String prefix, final String destination) {
        final String value = StompMessage.validateToken(destination, "STOMP destination");
        if (value.equals(prefix) || value.startsWith(prefix + Symbol.SLASH)) {
            return value;
        }
        return value.charAt(Normal._0) == Symbol.C_SLASH ? prefix + value : prefix + Symbol.SLASH + value;
    }

    /**
     * Writes a frame.
     *
     * @param frame frame
     * @return send call
     */
    private Call<Void> write(final StompFrame frame) {
        final StompFrame outgoing = filter(frame, TAG_WRITE);
        checkGuard(outgoing, TAG_WRITE);
        final Buffer output = new Buffer();
        codec.encode(outgoing, output);
        final Call<Void> call = sender.apply(output);
        final ObservedCall observed = new ObservedCall(call, outgoing.body());
        observed.observeIfDone();
        Logger.debug(
                false,
                "Fabric",
                "STOMP frame written: scheme={}, host={}, port={}, command={}, bytes={}",
                scheme(),
                host(),
                port(),
                outgoing.command(),
                outgoing.body().length());
        return observed;
    }

    /**
     * Applies the optional message filter to a STOMP frame.
     *
     * @param frame frame
     * @param tag   direction tag
     * @return filtered frame
     */
    private StompFrame filter(final StompFrame frame, final String tag) {
        if (filter == null || address == null) {
            return frame;
        }
        final Message filtered = FilterChain
                .apply(Message.of(Protocol.STOMP, address, frame.headers(), frame.body(), tag), filter);
        return StompFrame.of(frame.command(), filtered.headers(), filtered.payload());
    }

    /**
     * Checks optional guard for a STOMP frame.
     *
     * @param frame frame
     * @param tag   direction tag
     */
    private void checkGuard(final StompFrame frame, final String tag) {
        if (guard != null && address != null) {
            Logger.debug(
                    true,
                    "Fabric",
                    "STOMP guard check started: scheme={}, host={}, port={}, command={}, tag={}",
                    scheme(),
                    host(),
                    port(),
                    frame.command(),
                    tag);
            guard.check(Message.of(Protocol.STOMP, address, frame.headers(), frame.body(), tag)).throwIfRejected();
            Logger.debug(
                    false,
                    "Fabric",
                    "STOMP guard check accepted: scheme={}, host={}, port={}, command={}, tag={}",
                    scheme(),
                    host(),
                    port(),
                    frame.command(),
                    tag);
        }
    }

    /**
     * Emits STOMP observation events.
     *
     * @param marker marker
     * @param body   body
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final Payload body, final Throwable cause) {
        if (address == null) {
            return;
        }
        final FabricEvent.Builder event = FabricEvent.builder(marker).tag(Tags.PROTOCOL, address.scheme())
                .tag(Tags.HOST, address.host()).tag(Tags.PORT, Integer.toString(address.port()));
        if (body != null && body.length() >= Normal.LONG_ZERO) {
            event.tag(Tags.BYTES, Long.toString(body.length()));
        }
        if (cause != null) {
            event.cause(cause);
        }
        observer.emit(event.build());
    }

    /**
     * Unwraps completion causes.
     *
     * @param cause completion cause
     * @return unwrapped cause
     */
    private static Throwable unwrap(final Throwable cause) {
        return cause instanceof java.util.concurrent.CompletionException completion ? completion.getCause() : cause;
    }

    /**
     * Creates a repeatable payload snapshot for one-shot stream sends.
     *
     * @param payload payload
     * @return payload snapshot
     */
    private Payload snapshot(final Payload payload) {
        final long length = payload.length();
        if (length > Integer.MAX_VALUE) {
            throw new ValidateException("STOMP payload is too large");
        }
        if (length >= Normal.LONG_ZERO && payload.repeatable()) {
            return payload;
        }
        return Payload.of(materialize(payload, "StompSession.snapshot(Payload)"));
    }

    /**
     * Materializes a payload through the configured session limit.
     *
     * @param payload   payload
     * @param operation operation name
     * @return payload bytes
     */
    private byte[] materialize(final Payload payload, final String operation) {
        try {
            return Payload.materialize(payload, materializeMaxBytes, operation);
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to materialize STOMP payload for " + operation, e);
        }
    }

    /**
     * Ensures the session is open.
     */
    private void ensureOpen() {
        if (!opened()) {
            throw new StatefulException("STOMP session is not open");
        }
    }

    /**
     * Returns the session scheme for logs.
     *
     * @return scheme or unknown
     */
    private String scheme() {
        return address == null ? UNKNOWN : address.scheme();
    }

    /**
     * Returns the session host for logs.
     *
     * @return host or unknown
     */
    private String host() {
        return address == null ? UNKNOWN : address.host();
    }

    /**
     * Returns the session port for logs.
     *
     * @return port or -1
     */
    private int port() {
        return address == null ? Normal.__1 : address.port();
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
     * Subscription holder.
     *
     * @param topic   topic
     * @param handler handler
     */
    private record Subscription(StompTopic topic, Consumer<StompMessage> handler) {

    }

    /**
     * Send call wrapper that records STOMP-level observation without exposing transport futures.
     */
    private final class ObservedCall implements Call<Void> {

        /**
         * Delegate send call.
         */
        private final Call<Void> delegate;

        /**
         * Frame body.
         */
        private final Payload body;

        /**
         * Observation guard.
         */
        private final AtomicBoolean observed;

        /**
         * Creates a call.
         *
         * @param delegate delegate call
         * @param body     frame body
         */
        private ObservedCall(final Call<Void> delegate, final Payload body) {
            this.delegate = require(delegate, "STOMP delegate call");
            this.body = body;
            this.observed = new AtomicBoolean();
        }

        /**
         * Executes the delegate send call and emits write observation.
         *
         * @return null
         */
        @Override
        public Void execute() {
            try {
                final Void value = delegate.execute();
                observe(null);
                return value;
            } catch (final RuntimeException e) {
                observe(e);
                throw e;
            }
        }

        /**
         * Enqueues the delegate send call and observes immediate completion when available.
         *
         * @return this call
         */
        @Override
        public Call<Void> enqueue() {
            delegate.enqueue();
            observeIfDone();
            return this;
        }

        /**
         * Awaits the delegate send call and emits write observation.
         *
         * @return null
         */
        @Override
        public Void await() {
            try {
                final Void value = delegate.await();
                observe(null);
                return value;
            } catch (final RuntimeException e) {
                observe(e);
                throw e;
            }
        }

        /**
         * Awaits the delegate send call within a timeout and emits write observation.
         *
         * @param timeout timeout
         * @return null
         */
        @Override
        public Void await(final Duration timeout) {
            try {
                final Void value = delegate.await(timeout);
                observe(null);
                return value;
            } catch (final RuntimeException e) {
                observe(e);
                throw e;
            }
        }

        /**
         * Cancels the delegate send call and emits cancellation observation.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancel() {
            final boolean cancelled = delegate.cancel();
            if (cancelled) {
                observe(new CancellationException("STOMP send was cancelled"));
            }
            return cancelled;
        }

        /**
         * Returns whether the delegate send call is cancelled.
         *
         * @return true when cancelled
         */
        @Override
        public boolean cancelled() {
            return delegate.cancelled();
        }

        /**
         * Returns whether the delegate send call is complete.
         *
         * @return true when complete
         */
        @Override
        public boolean done() {
            return delegate.done();
        }

        /**
         * Emits when the delegate has already completed.
         */
        private void observeIfDone() {
            if (!delegate.done()) {
                return;
            }
            try {
                delegate.await(Duration.ZERO);
                observe(null);
            } catch (final RuntimeException e) {
                observe(e);
            }
        }

        /**
         * Emits the terminal observation once.
         *
         * @param cause failure cause
         */
        private void observe(final Throwable cause) {
            if (observed.compareAndSet(false, true)) {
                emit(
                        cause == null ? ObservationMarker.STOMP_MESSAGE : ObservationMarker.STOMP_FAILED,
                        body,
                        unwrap(cause));
            }
        }

    }

}
