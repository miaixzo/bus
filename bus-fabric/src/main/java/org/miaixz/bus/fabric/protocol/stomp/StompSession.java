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

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
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
import org.miaixz.bus.fabric.observe.tag.Tags;
import org.miaixz.bus.fabric.protocol.stomp.body.StompBody;
import org.miaixz.bus.fabric.protocol.stomp.broker.StompReceipt;
import org.miaixz.bus.fabric.protocol.stomp.broker.StompTopic;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompCodec;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompFrame;
import org.miaixz.bus.logger.Logger;

/**
 * Open STOMP session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompSession {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

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
    private final Function<ByteBuffer, Call<Void>> sender;

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
    StompSession(final Function<ByteBuffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler) {
        this(sender, closeHook, cancelHook, handler, null, null, EventObserver.noop(), Wiring.noop(),
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
    StompSession(final Function<ByteBuffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
            final EventObserver observer, final Listener<? super StompSession> listener) {
        this(sender, closeHook, cancelHook, handler, address, guard, observer, listener,
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
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    StompSession(final Function<ByteBuffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
            final EventObserver observer, final Listener<? super StompSession> listener,
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
        final Headers headers = Headers.builder().add("destination", target)
                .add("content-length", Long.toString(outgoing.length())).build();
        Logger.info(
                false,
                LOG_TAG,
                "STOMP send requested: scheme={}, host={}, port={}, destination={}, bytes={}",
                scheme(),
                host(),
                port(),
                target,
                outgoing.length());
        return write(StompFrame.of("SEND", headers, outgoing));
    }

    /**
     * Sends text to a destination.
     *
     * @param destination destination
     * @param data        text data
     * @return send call
     */
    public Call<Void> sendTo(final String destination, final String data) {
        return send(destination, Payload.of(data == null ? "" : data, java.nio.charset.StandardCharsets.UTF_8));
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
        final Headers headers = Headers.builder().add("destination", target)
                .add("content-type", body.media().toString()).add("content-length", Long.toString(outgoing.length()))
                .build();
        Logger.info(
                false,
                LOG_TAG,
                "STOMP body send requested: scheme={}, host={}, port={}, destination={}, media={}, bytes={}",
                scheme(),
                host(),
                port(),
                target,
                body.media(),
                outgoing.length());
        return write(StompFrame.of("SEND", headers, outgoing));
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
        if (handler == null) {
            throw new ValidateException("STOMP subscription handler must not be null");
        }
        final Headers extraHeaders = headers == null ? Headers.empty() : headers;
        final StompTopic topic = StompTopic.of(UUID.randomUUID().toString(), target);
        final Headers.Builder builder = Headers.builder().add("id", topic.id()).add("destination", topic.destination());
        extraHeaders.asMap().forEach((name, values) -> {
            if (!"id".equalsIgnoreCase(name) && !"destination".equalsIgnoreCase(name)) {
                values.forEach(value -> builder.add(name, value));
            }
        });
        synchronized (topics) {
            topics.put(topic.id(), new Subscription(topic, handler));
        }
        try {
            write(StompFrame.of("SUBSCRIBE", builder.build(), Payload.empty()));
            Logger.info(
                    false,
                    LOG_TAG,
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
                    LOG_TAG,
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
        return acknowledge("ACK", messageId);
    }

    /**
     * Acknowledges a received message.
     *
     * @param message message
     * @return send call
     */
    public Call<Void> ack(final StompMessage message) {
        return acknowledge("ACK", message);
    }

    /**
     * Rejects a message.
     *
     * @param messageId message id
     * @return send call
     */
    public Call<Void> nack(final String messageId) {
        return acknowledge("NACK", messageId);
    }

    /**
     * Rejects a received message.
     *
     * @param message message
     * @return send call
     */
    public Call<Void> nack(final StompMessage message) {
        return acknowledge("NACK", message);
    }

    /**
     * Unsubscribes a topic.
     *
     * @param topic topic
     * @return true when removed
     */
    public boolean unsubscribe(final StompTopic topic) {
        if (topic == null) {
            throw new ValidateException("STOMP topic must not be null");
        }
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
        write(StompFrame.of("UNSUBSCRIBE", Headers.builder().add("id", topic.id()).build(), Payload.empty()));
        Logger.info(
                false,
                LOG_TAG,
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
                    LOG_TAG,
                    "STOMP session close started: scheme={}, host={}, port={}",
                    scheme(),
                    host(),
                    port());
            try {
                write(StompFrame.of("DISCONNECT", Headers.empty(), Payload.empty()));
            } catch (final RuntimeException ignored) {
                // The close hook still owns transport cleanup after a best-effort DISCONNECT.
            } finally {
                cleanup(closeCause);
                closeHook.run();
            }
            notifyClosed();
            Logger.info(false, LOG_TAG, "STOMP session closed: scheme={}, host={}, port={}", scheme(), host(), port());
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
                        LOG_TAG,
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
                        LOG_TAG,
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
        if ("RECEIPT".equals(frame.command())) {
            final String receiptId = frame.headers().get("receipt-id");
            if (receiptId != null) {
                receipts.complete(receiptId);
                Logger.debug(
                        false,
                        LOG_TAG,
                        "STOMP receipt completed: scheme={}, host={}, port={}, receiptId={}",
                        scheme(),
                        host(),
                        port(),
                        receiptId);
            }
            return;
        }
        if ("ERROR".equals(frame.command())) {
            final ProtocolException failure = new ProtocolException(
                    frame.body().text(java.nio.charset.StandardCharsets.UTF_8));
            fail(failure);
            Logger.warn(
                    false,
                    LOG_TAG,
                    failure,
                    "STOMP ERROR frame received: scheme={}, host={}, port={}",
                    scheme(),
                    host(),
                    port());
            throw failure;
        }
        if (!"MESSAGE".equals(frame.command())) {
            return;
        }
        final String destination = frame.headers().get("destination");
        final StompMessage message = StompMessage.of(destination, frame.headers(), frame.body());
        checkGuard(frame, "stomp-read");
        emit(ObservationMarker.STOMP_MESSAGE, frame.body(), null);
        Logger.debug(
                false,
                LOG_TAG,
                "STOMP message received: scheme={}, host={}, port={}, destination={}, bytes={}",
                scheme(),
                host(),
                port(),
                destination,
                frame.body().length());
        accept(handler, message, frame.body());
        final List<Subscription> snapshot;
        synchronized (topics) {
            snapshot = new ArrayList<>(topics.values());
        }
        for (final Subscription subscription : snapshot) {
            if (subscription.topic.matches(message)) {
                accept(subscription.handler, message, frame.body());
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
                    LOG_TAG,
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
                    LOG_TAG,
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
                LOG_TAG,
                "STOMP acknowledgement requested: scheme={}, host={}, port={}, command={}",
                scheme(),
                host(),
                port(),
                command);
        return write(StompFrame.of(command, Headers.builder().add("id", id).build(), Payload.empty()));
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
        final String id = current.headers().get("message-id");
        final String subscription = current.headers().get("subscription");
        final Headers.Builder headers = Headers.builder().add("id", StompMessage.validateToken(id, "STOMP message id"));
        if (subscription != null) {
            headers.add("subscription", StompMessage.validateToken(subscription, "STOMP subscription id"));
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
        if (value.equals(prefix) || value.startsWith(prefix + "/")) {
            return value;
        }
        return value.charAt(0) == '/' ? prefix + value : prefix + '/' + value;
    }

    /**
     * Writes a frame.
     *
     * @param frame frame
     * @return send call
     */
    private Call<Void> write(final StompFrame frame) {
        checkGuard(frame, "stomp-write");
        final Call<Void> call = sender.apply(codec.encode(frame));
        final ObservedCall observed = new ObservedCall(call, frame.body());
        observed.observeIfDone();
        Logger.debug(
                false,
                LOG_TAG,
                "STOMP frame written: scheme={}, host={}, port={}, command={}, bytes={}",
                scheme(),
                host(),
                port(),
                frame.command(),
                frame.body().length());
        return observed;
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
                    LOG_TAG,
                    "STOMP guard check started: scheme={}, host={}, port={}, command={}, tag={}",
                    scheme(),
                    host(),
                    port(),
                    frame.command(),
                    tag);
            guard.check(Message.of(address.protocol(), address, frame.headers(), frame.body(), tag)).throwIfRejected();
            Logger.debug(
                    false,
                    LOG_TAG,
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
        if (body != null && body.length() >= 0) {
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
        if (length >= 0 && payload.repeatable()) {
            return payload;
        }
        return Payload.of(payload.bytes(materializeMaxBytes));
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
        return address == null ? "unknown" : address.scheme();
    }

    /**
     * Returns the session host for logs.
     *
     * @return host or unknown
     */
    private String host() {
        return address == null ? "unknown" : address.host();
    }

    /**
     * Returns the session port for logs.
     *
     * @return port or -1
     */
    private int port() {
        return address == null ? -1 : address.port();
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

        @Override
        public Call<Void> enqueue() {
            delegate.enqueue();
            observeIfDone();
            return this;
        }

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

        @Override
        public boolean cancel() {
            final boolean cancelled = delegate.cancel();
            if (cancelled) {
                observe(new CancellationException("STOMP send was cancelled"));
            }
            return cancelled;
        }

        @Override
        public boolean cancelled() {
            return delegate.cancelled();
        }

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
