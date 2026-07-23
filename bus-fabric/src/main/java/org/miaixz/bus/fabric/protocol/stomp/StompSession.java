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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Filter;
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
import org.miaixz.bus.fabric.protocol.stomp.body.StompBody;
import org.miaixz.bus.fabric.protocol.stomp.broker.StompReceipt;
import org.miaixz.bus.fabric.protocol.stomp.broker.StompTopic;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompCodec;
import org.miaixz.bus.fabric.protocol.stomp.frame.StompFrame;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Open STOMP session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompSession implements Session {

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
     * Per-emission logical STOMP frame byte count.
     */
    private final ThreadLocal<Long> logicalBytes;

    /**
     * Dispatcher used by Calls and heartbeat deadlines.
     */
    private final Dispatcher dispatcher;

    /**
     * Clock used by heartbeat activity tracking.
     */
    private final Clock clock;

    /**
     * Session cancellation shared with receipt waits.
     */
    private final Cancellation cancellation;

    /**
     * Complete immutable transport timeout policy.
     */
    private final Timeout timeout;

    /**
     * Immutable negotiated STOMP state.
     */
    private final StompState state;

    /**
     * Whether this session owns its compatibility dispatcher.
     */
    private final boolean ownsDispatcher;

    /**
     * Active outbound heartbeat handle.
     */
    private final AtomicReference<DispatchHandle> outboundHeartbeatHandle;

    /**
     * Active inbound deadline handle.
     */
    private final AtomicReference<DispatchHandle> inboundDeadlineHandle;

    /**
     * Guard allowing one terminal path to own cleanup.
     */
    private final AtomicBoolean terminating;

    /**
     * Last successfully written frame or heartbeat timestamp.
     */
    private volatile long lastWriteNanos;

    /**
     * Last received frame or heartbeat timestamp.
     */
    private volatile long lastReadNanos;

    /**
     * Maximum bytes allowed when materializing session payloads.
     */
    private final long materializeMaxBytes;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Creates an opened session.
     *
     * @param sender     transport action used to write encoded STOMP frames
     * @param closeHook  close hook
     * @param cancelHook cancel hook
     * @param handler    default message handler
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler) {
        this(sender, closeHook, cancelHook, handler, null, null, EventObserver.noop(), null, null,
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param sender     transport action used to write encoded STOMP frames
     * @param closeHook  close hook
     * @param cancelHook cancel hook
     * @param handler    default message handler
     * @param address    session address
     * @param guard      optional guard
     * @param observer   observer receiving STOMP lifecycle events
     * @param listener   lifecycle listener
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
            final EventObserver observer, final Listener<? super StompSession> listener) {
        this(sender, closeHook, cancelHook, handler, address, guard, observer, null, listener,
                Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates an opened session.
     *
     * @param sender              transport action used to write encoded STOMP frames
     * @param closeHook           close hook
     * @param cancelHook          cancel hook
     * @param handler             default message handler
     * @param address             session address
     * @param guard               optional guard
     * @param observer            observer receiving STOMP lifecycle events
     * @param filter              optional filter
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
            final EventObserver observer, final Filter filter, final Listener<? super StompSession> listener,
            final long materializeMaxBytes) {
        this(sender, closeHook, cancelHook, handler, address, guard, observer, filter, listener, materializeMaxBytes,
                Dispatcher.create(), Clock.system(), Cancellation.create(), Timeout.defaults(), StompState.disabled(),
                true);
    }

    /**
     * Creates an opened session with negotiated heartbeat settings and shared runtime services.
     *
     * @param sender              transport action used to write encoded STOMP frames
     * @param closeHook           close hook
     * @param cancelHook          cancel hook
     * @param handler             default message handler
     * @param address             session address
     * @param guard               optional guard
     * @param observer            observer receiving STOMP lifecycle events
     * @param filter              optional filter
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param dispatcher          shared dispatcher
     * @param clock               shared clock
     * @param cancellation        session cancellation
     * @param timeout             complete transport timeout policy
     * @param state               negotiated STOMP state
     */
    StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
            final EventObserver observer, final Filter filter, final Listener<? super StompSession> listener,
            final long materializeMaxBytes, final Dispatcher dispatcher, final Clock clock,
            final Cancellation cancellation, final Timeout timeout, final StompState state) {
        this(sender, closeHook, cancelHook, handler, address, guard, observer, filter, listener, materializeMaxBytes,
                dispatcher, clock, cancellation, timeout, state, false);
    }

    /**
     * Creates an opened session.
     *
     * @param sender              transport action used to write encoded STOMP frames
     * @param closeHook           close hook
     * @param cancelHook          cancel hook
     * @param handler             default message handler
     * @param address             session address
     * @param guard               optional guard
     * @param observer            observer receiving STOMP lifecycle events
     * @param filter              optional filter
     * @param listener            lifecycle listener
     * @param materializeMaxBytes materialize byte threshold
     * @param dispatcher          runtime dispatcher
     * @param clock               session clock
     * @param cancellation        session cancellation
     * @param timeout             complete transport timeout policy
     * @param state               negotiated STOMP state
     * @param ownsDispatcher      true when this session owns the dispatcher
     */
    private StompSession(final Function<Buffer, Call<Void>> sender, final Runnable closeHook, final Runnable cancelHook,
            final Consumer<StompMessage> handler, final Address address, final GuardRule guard,
            final EventObserver observer, final Filter filter, final Listener<? super StompSession> listener,
            final long materializeMaxBytes, final Dispatcher dispatcher, final Clock clock,
            final Cancellation cancellation, final Timeout timeout, final StompState state,
            final boolean ownsDispatcher) {
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
        final EventObserver sink = EventObserver.safe(observer);
        this.logicalBytes = new ThreadLocal<>();
        this.observer = event -> sink.emit(withLogicalBytes(event));
        this.dispatcher = require(dispatcher, "STOMP dispatcher");
        this.clock = require(clock, "STOMP clock");
        this.cancellation = require(cancellation, "STOMP cancellation");
        this.timeout = require(timeout, "STOMP timeout");
        this.state = require(state, "STOMP state");
        this.ownsDispatcher = ownsDispatcher;
        this.scope = LifecycleScope.session(
                this,
                "stomp-session",
                listener,
                this.observer,
                ObservationMarker.STOMP_OPEN,
                ObservationMarker.STOMP_CLOSED,
                ObservationMarker.STOMP_FAILED,
                this.clock);
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
        this.outboundHeartbeatHandle = new AtomicReference<>();
        this.inboundDeadlineHandle = new AtomicReference<>();
        this.terminating = new AtomicBoolean();
        this.lastWriteNanos = this.clock.nanos();
        this.lastReadNanos = this.lastWriteNanos;
        this.scope.open(this);
        scheduleOutboundHeartbeat();
        scheduleInboundDeadline();
    }

    /**
     * Returns the session address.
     *
     * @return session address
     */
    @Override
    public Address address() {
        return address;
    }

    /**
     * Sends a payload to the default destination.
     *
     * @param payload message payload sent to the session default destination
     * @return send call
     */
    @Override
    public Call<Void> send(final Payload payload) {
        return send(defaultDestination(), payload);
    }

    /**
     * Returns session attributes.
     *
     * @return attributes
     */
    @Override
    public Map<String, Object> attributes() {
        return Map.of(Builder.ATTRIBUTE_OBSERVER, observer);
    }

    /**
     * Sends a STOMP message.
     *
     * @param destination STOMP destination header value
     * @param payload     message payload to send
     * @return send call
     */
    public Call<Void> send(final String destination, final Payload payload) {
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        final Payload source = require(payload, "STOMP payload");
        return write(() -> {
            final Payload outgoing = snapshot(source);
            final Headers headers = Headers.builder().add(Builder.STOMP_HEADER_DESTINATION, target)
                    .add(Http.Header.CONTENT_LENGTH.toLowerCase(Locale.ROOT), Long.toString(outgoing.length())).build();
            Logger.info(
                    false,
                    "Fabric",
                    "STOMP send requested: scheme={}, host={}, port={}, destination={}, bytes={}",
                    scheme(),
                    host(),
                    port(),
                    target,
                    outgoing.length());
            return StompFrame.of(Builder.STOMP_COMMAND_SEND, headers, outgoing);
        });
    }

    /**
     * Sends text to a destination.
     *
     * @param destination STOMP destination header value
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
     * @param destination STOMP destination header value
     * @param payload     message payload to send
     * @return send call
     */
    public Call<Void> sendTo(final String destination, final Payload payload) {
        return send(destination, payload);
    }

    /**
     * Sends a STOMP message body.
     *
     * @param destination STOMP destination header value
     * @param body        body used to construct the SEND frame
     * @return send call
     */
    public Call<Void> send(final String destination, final StompBody body) {
        final String target = StompMessage.validateToken(destination, "STOMP destination");
        final StompBody source = require(body, "STOMP body");
        return write(() -> {
            final Payload outgoing = snapshot(source.payload());
            final Headers headers = Headers.builder().add(Builder.STOMP_HEADER_DESTINATION, target)
                    .add(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT), source.media().toString())
                    .add(Http.Header.CONTENT_LENGTH.toLowerCase(Locale.ROOT), Long.toString(outgoing.length())).build();
            Logger.info(
                    false,
                    "Fabric",
                    "STOMP body send requested: scheme={}, host={}, port={}, destination={}, media={}, bytes={}",
                    scheme(),
                    host(),
                    port(),
                    target,
                    source.media(),
                    outgoing.length());
            return StompFrame.of(Builder.STOMP_COMMAND_SEND, headers, outgoing);
        });
    }

    /**
     * Subscribes to a destination.
     *
     * @param destination STOMP destination to subscribe to
     * @param handler     message handler
     * @return topic
     */
    public StompTopic subscribe(final String destination, final Consumer<StompMessage> handler) {
        return subscribe(destination, Headers.empty(), handler);
    }

    /**
     * Subscribes to a destination with extra SUBSCRIBE headers.
     *
     * @param destination STOMP destination to subscribe to
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
        final Headers.Builder builder = Headers.builder().add(Builder.STOMP_HEADER_ID, topic.id())
                .add(Builder.STOMP_HEADER_DESTINATION, topic.destination());
        extraHeaders.asMap().forEach((name, values) -> {
            if (!Builder.STOMP_HEADER_ID.equalsIgnoreCase(name)
                    && !Builder.STOMP_HEADER_DESTINATION.equalsIgnoreCase(name)) {
                values.forEach(value -> builder.add(name, value));
            }
        });
        try {
            write(StompFrame.of(Builder.STOMP_COMMAND_SUBSCRIBE, builder.build(), Payload.empty())).execute();
            synchronized (topics) {
                topics.put(topic.id(), new Subscription(topic, handler));
            }
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
        return subscribeOnce(prefixed(Builder.STOMP_TOPIC_PREFIX, destination), headers, handler);
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
        return subscribeOnce(prefixed(Builder.STOMP_QUEUE_PREFIX, destination), headers, handler);
    }

    /**
     * Creates a lazy receipt wait Call.
     *
     * @param receiptId receipt id
     * @return receipt wait Call
     */
    public Call<Void> receipt(final String receiptId) {
        return new ReceiptCall(StompMessage.validateToken(receiptId, "STOMP receipt id"));
    }

    /**
     * Acknowledges a message.
     *
     * @param messageId message id
     * @return send call
     */
    public Call<Void> ack(final String messageId) {
        return acknowledge(Builder.STOMP_COMMAND_ACK, messageId);
    }

    /**
     * Acknowledges a received message.
     *
     * @param message received message whose acknowledgement id is used
     * @return send call
     */
    public Call<Void> ack(final StompMessage message) {
        return acknowledge(Builder.STOMP_COMMAND_ACK, message);
    }

    /**
     * Rejects a message.
     *
     * @param messageId message id
     * @return send call
     */
    public Call<Void> nack(final String messageId) {
        return acknowledge(Builder.STOMP_COMMAND_NACK, messageId);
    }

    /**
     * Rejects a received message.
     *
     * @param message received message whose acknowledgement id is rejected
     * @return send call
     */
    public Call<Void> nack(final StompMessage message) {
        return acknowledge(Builder.STOMP_COMMAND_NACK, message);
    }

    /**
     * Unsubscribes a topic.
     *
     * @param topic subscription handle to remove
     * @return true when removed
     */
    public boolean unsubscribe(final StompTopic topic) {
        Assert.notNull(topic, () -> new ValidateException("STOMP topic must not be null"));
        if (!opened()) {
            return false;
        }
        final Subscription removed;
        synchronized (topics) {
            removed = topics.get(topic.id());
            if (removed == null) {
                return false;
            }
            write(
                    StompFrame.of(
                            Builder.STOMP_COMMAND_UNSUBSCRIBE,
                            Headers.builder().add(Builder.STOMP_HEADER_ID, topic.id()).build(),
                            Payload.empty())).execute();
            topics.remove(topic.id(), removed);
        }
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
     * @param destination subscribed destination to remove
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
        return unsubscribe(prefixed(Builder.STOMP_TOPIC_PREFIX, destination));
    }

    /**
     * Unsubscribes a queue destination.
     *
     * @param destination queue suffix or absolute queue destination
     * @return true when removed
     */
    public boolean unqueue(final String destination) {
        return unsubscribe(prefixed(Builder.STOMP_QUEUE_PREFIX, destination));
    }

    /**
     * Closes this session.
     *
     * @return true when state changed
     */
    public boolean close() {
        return terminate(Termination.CLOSE, new StatefulException("STOMP session was closed"));
    }

    /**
     * Cancels this session.
     *
     * @return true when state changed
     */
    public boolean cancel() {
        return terminate(Termination.CANCEL, new StatefulException("STOMP session was cancelled"));
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    @Override
    public Status state() {
        return scope.state();
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
     * @param frame decoded inbound STOMP frame to dispatch
     */
    void dispatch(final StompFrame frame) {
        final StompFrame current = require(frame, "STOMP frame");
        if (!opened()) {
            return;
        }
        touchRead();
        if (current == StompFrame.heartbeat()) {
            emit(ObservationMarker.STOMP_MESSAGE, Normal._1, null);
            return;
        }
        final long frameBytes = encodedBytes(current);
        if (Builder.STOMP_COMMAND_RECEIPT.equals(current.command())) {
            final String receiptId = current.headers().get(Builder.STOMP_HEADER_RECEIPT_ID);
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
            emit(ObservationMarker.STOMP_MESSAGE, frameBytes, null);
            return;
        }
        if (Builder.STOMP_COMMAND_ERROR.equals(current.command())) {
            final ProtocolException failure = new ProtocolException(
                    current.body().text(java.nio.charset.StandardCharsets.UTF_8));
            emit(ObservationMarker.STOMP_MESSAGE, frameBytes, null);
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
        if (!Builder.STOMP_COMMAND_MESSAGE.equals(current.command())) {
            emit(ObservationMarker.STOMP_MESSAGE, frameBytes, null);
            return;
        }
        final StompFrame received = filter(current, Builder.STOMP_TAG_READ);
        final String destination = received.headers().get(Builder.STOMP_HEADER_DESTINATION);
        final StompMessage message = StompMessage.of(destination, received.headers(), received.body());
        checkGuard(received, Builder.STOMP_TAG_READ);
        emit(ObservationMarker.STOMP_MESSAGE, frameBytes, null);
        Logger.debug(
                false,
                "Fabric",
                "STOMP message received: scheme={}, host={}, port={}, destination={}, bytes={}",
                scheme(),
                host(),
                port(),
                destination,
                received.body().length());
        accept(handler, message);
        final List<Subscription> snapshot;
        synchronized (topics) {
            snapshot = new ArrayList<>(topics.values());
        }
        for (final Subscription subscription : snapshot) {
            if (subscription.topic.matches(message)) {
                accept(subscription.handler, message);
            }
        }
    }

    /**
     * Fails the session and clears broker state.
     *
     * @param cause failure cause
     */
    private void fail(final RuntimeException cause) {
        terminate(Termination.FAIL, cause);
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
        synchronized (codec) {
            codec.reset();
        }
    }

    /**
     * Invokes a message handler without allowing one callback to break delivery to other subscriptions.
     *
     * @param consumer subscription callback to invoke safely
     * @param message  decoded message delivered to the callback
     */
    private void accept(final Consumer<StompMessage> consumer, final StompMessage message) {
        try {
            consumer.accept(message);
        } catch (final RuntimeException e) {
            scope.emit(ObservationMarker.STOMP_FAILED, e);
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
     * @param command   ACK or NACK command to send
     * @param messageId message id
     * @return send call
     */
    private Call<Void> acknowledge(final String command, final String messageId) {
        final String id = StompMessage.validateToken(messageId, "STOMP message id");
        Logger.debug(
                false,
                "Fabric",
                "STOMP acknowledgement requested: scheme={}, host={}, port={}, command={}",
                scheme(),
                host(),
                port(),
                command);
        return write(
                StompFrame.of(command, Headers.builder().add(Builder.STOMP_HEADER_ID, id).build(), Payload.empty()));
    }

    /**
     * Sends an ACK or NACK frame for a received message.
     *
     * @param command ACK or NACK command to send
     * @param message received message supplying acknowledgement metadata
     * @return send call
     */
    private Call<Void> acknowledge(final String command, final StompMessage message) {
        final StompMessage current = require(message, "STOMP message");
        final String id = current.headers().get(Builder.STOMP_HEADER_MESSAGE_ID);
        final String subscription = current.headers().get(Builder.STOMP_HEADER_SUBSCRIPTION);
        final Headers.Builder headers = Headers.builder()
                .add(Builder.STOMP_HEADER_ID, StompMessage.validateToken(id, "STOMP message id"));
        if (subscription != null) {
            headers.add(
                    Builder.STOMP_HEADER_SUBSCRIPTION,
                    StompMessage.validateToken(subscription, "STOMP subscription id"));
        }
        return write(StompFrame.of(command, headers.build(), Payload.empty()));
    }

    /**
     * Returns the default destination derived from the session address.
     *
     * @return default destination
     */
    private String defaultDestination() {
        if (address == null || address.path() == null || Symbol.SLASH.equals(address.path())) {
            throw new ValidateException("STOMP default destination must be configured");
        }
        return address.path();
    }

    /**
     * Subscribes once by destination for topic and queue helpers.
     *
     * @param destination destination to normalize and subscribe to
     * @param headers     extra headers
     * @param handler     callback receiving messages for the subscription
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
            return subscribe(target, headers, handler);
        }
    }

    /**
     * Applies a destination prefix when absent.
     *
     * @param prefix      STOMP destination namespace prefix
     * @param destination destination to normalize
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
     * @param frame STOMP frame to encode and write
     * @return send call
     */
    private Call<Void> write(final StompFrame frame) {
        final StompFrame current = require(frame, "STOMP frame");
        return write(() -> current);
    }

    /**
     * Creates a lazy frame write Call from a deferred frame factory.
     *
     * @param frameSupplier frame factory
     * @return send Call
     */
    private Call<Void> write(final Supplier<StompFrame> frameSupplier) {
        final Supplier<StompFrame> current = require(frameSupplier, "STOMP frame supplier");
        return MonoCall.<Void>create(
                "stomp-session-write",
                "stomp:session:write",
                dispatcher,
                observer,
                null,
                () -> writeNow(require(current.get(), "STOMP frame"), false),
                this::cancel);
    }

    /**
     * Filters, encodes, and sends one complete normal STOMP frame.
     *
     * @param frame         complete STOMP frame to filter, encode, and write
     * @param terminalWrite true for best-effort DISCONNECT during close
     * @return null after successful transport completion
     */
    private Void writeNow(final StompFrame frame, final boolean terminalWrite) {
        try {
            ensureWritable(terminalWrite);
            final StompFrame outgoing = filter(frame, Builder.STOMP_TAG_WRITE);
            checkGuard(outgoing, Builder.STOMP_TAG_WRITE);
            final Buffer output = new Buffer();
            synchronized (codec) {
                codec.encode(outgoing, output);
            }
            final long frameBytes = output.size();
            require(sender.apply(output), "STOMP sender Call").execute();
            emit(ObservationMarker.STOMP_MESSAGE, frameBytes, null);
            touchWrite();
            Logger.debug(
                    false,
                    "Fabric",
                    "STOMP frame written: scheme={}, host={}, port={}, command={}, bytes={}",
                    scheme(),
                    host(),
                    port(),
                    outgoing.command(),
                    frameBytes);
            return null;
        } catch (final RuntimeException e) {
            if (!terminalWrite) {
                operationFailed(e);
            }
            throw e;
        } catch (final Error e) {
            if (!terminalWrite) {
                operationFailed(e);
            }
            throw e;
        }
    }

    /**
     * Applies the optional message filter to a STOMP frame.
     *
     * @param frame STOMP frame represented as a protocol-neutral message
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
     * @param frame STOMP frame to validate
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
     * @param marker observation marker identifying the STOMP event
     * @param bytes  complete encoded STOMP frame bytes
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final long bytes, final Throwable cause) {
        if (address == null) {
            return;
        }
        logicalBytes.set(bytes);
        try {
            scope.emit(marker, cause);
        } finally {
            logicalBytes.remove();
        }
    }

    /**
     * Adds logical STOMP bytes while retaining lifecycle operation tags and timestamp.
     *
     * @param event lifecycle event
     * @return event with logical frame bytes when present
     */
    private FabricEvent withLogicalBytes(final FabricEvent event) {
        final Long bytes = logicalBytes.get();
        if (bytes == null || event.marker() != ObservationMarker.STOMP_MESSAGE) {
            return event;
        }
        return new FabricEvent(event.marker(), event.time(), event.tags().with(Builder.TAG_BYTES, Long.toString(bytes)),
                event.cause());
    }

    /**
     * Returns the complete encoded size of one normal inbound frame.
     *
     * @param frame decoded inbound frame whose wire size is computed
     * @return encoded frame bytes
     */
    private long encodedBytes(final StompFrame frame) {
        final Buffer output = new Buffer();
        synchronized (codec) {
            codec.encode(frame, output);
        }
        return output.size();
    }

    /**
     * Creates a repeatable payload snapshot for one-shot stream sends.
     *
     * @param payload potentially one-shot payload to snapshot
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
     * @param payload   payload to materialize
     * @param operation diagnostic operation name used for limit failures
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
     * Records a successful normal frame or heartbeat write and replaces the outbound deadline.
     */
    private void touchWrite() {
        lastWriteNanos = clock.nanos();
        scheduleOutboundHeartbeat();
    }

    /**
     * Records any inbound frame or heartbeat and replaces the inbound deadline.
     */
    private void touchRead() {
        lastReadNanos = clock.nanos();
        scheduleInboundDeadline();
    }

    /**
     * Schedules the next outbound heartbeat relative to the latest successful write.
     */
    private void scheduleOutboundHeartbeat() {
        if (state.outboundHeartbeat().isZero() || terminating.get() || !opened()) {
            return;
        }
        scheduleOutboundHeartbeat(state.outboundHeartbeat(), lastWriteNanos);
    }

    /**
     * Installs one outbound heartbeat check.
     *
     * @param delay         delay before checking
     * @param activityNanos write timestamp guarded by the check
     */
    private void scheduleOutboundHeartbeat(final Duration delay, final long activityNanos) {
        final DispatchHandle created = dispatcher.schedule(
                "stomp:heartbeat:write",
                delay,
                Activity.of("stomp:heartbeat:write", () -> outboundHeartbeatExpired(activityNanos)));
        replaceHandle(outboundHeartbeatHandle, created);
        if (terminating.get() || !opened()) {
            cancelHandle(outboundHeartbeatHandle);
        }
    }

    /**
     * Sends one LF heartbeat when no normal frame was written before the negotiated deadline.
     *
     * @param activityNanos write timestamp guarded by the check
     */
    private void outboundHeartbeatExpired(final long activityNanos) {
        if (!opened() || terminating.get() || activityNanos != lastWriteNanos) {
            return;
        }
        final long intervalNanos = state.outboundHeartbeatNanos();
        final long elapsed = elapsedSince(activityNanos);
        if (elapsed < intervalNanos) {
            scheduleOutboundHeartbeat(Duration.ofNanos(intervalNanos - elapsed), activityNanos);
            return;
        }
        try {
            final Buffer heartbeat = new Buffer().writeByte(Symbol.C_LF);
            require(sender.apply(heartbeat), "STOMP heartbeat sender Call").execute();
            emit(ObservationMarker.STOMP_MESSAGE, Normal._1, null);
            touchWrite();
        } catch (final RuntimeException e) {
            operationFailed(e);
        } catch (final Error e) {
            operationFailed(e);
            throw e;
        }
    }

    /**
     * Schedules the next inbound heartbeat deadline relative to the latest received byte.
     */
    private void scheduleInboundDeadline() {
        if (state.inboundHeartbeatDeadline().isZero() || terminating.get() || !opened()) {
            return;
        }
        scheduleInboundDeadline(state.inboundHeartbeatDeadline(), lastReadNanos);
    }

    /**
     * Installs one inbound heartbeat deadline check.
     *
     * @param delay         delay before checking
     * @param activityNanos read timestamp guarded by the check
     */
    private void scheduleInboundDeadline(final Duration delay, final long activityNanos) {
        final DispatchHandle created = dispatcher.schedule(
                "stomp:heartbeat:read",
                delay,
                Activity.of("stomp:heartbeat:read", () -> inboundDeadlineExpired(activityNanos)));
        replaceHandle(inboundDeadlineHandle, created);
        if (terminating.get() || !opened()) {
            cancelHandle(inboundDeadlineHandle);
        }
    }

    /**
     * Fails the session when the negotiated inbound deadline is exceeded.
     *
     * @param activityNanos read timestamp guarded by the check
     */
    private void inboundDeadlineExpired(final long activityNanos) {
        if (!opened() || terminating.get() || activityNanos != lastReadNanos) {
            return;
        }
        final long deadlineNanos = state.inboundHeartbeatDeadlineNanos();
        final long elapsed = elapsedSince(activityNanos);
        if (elapsed < deadlineNanos) {
            scheduleInboundDeadline(Duration.ofNanos(deadlineNanos - elapsed), activityNanos);
            return;
        }
        fail(new TimeoutException("STOMP inbound heartbeat deadline exceeded"));
    }

    /**
     * Atomically replaces a heartbeat handle and clears it after terminal completion.
     *
     * @param reference owned handle reference
     * @param created   replacement handle
     */
    private static void replaceHandle(final AtomicReference<DispatchHandle> reference, final DispatchHandle created) {
        final DispatchHandle previous = reference.getAndSet(created);
        if (previous != null) {
            previous.cancel();
        }
        created.future().whenComplete((ignored, cause) -> reference.compareAndSet(created, null));
    }

    /**
     * Terminates the session after cancelling both heartbeat handles exactly once.
     *
     * @param termination requested terminal path
     * @param cause       terminal cause
     * @return true when this invocation owned termination
     */
    private boolean terminate(final Termination termination, final Throwable cause) {
        if (!terminating.compareAndSet(false, true)) {
            return false;
        }
        cancelHandle(outboundHeartbeatHandle);
        cancelHandle(inboundDeadlineHandle);
        if (termination == Termination.CLOSE && opened()) {
            try {
                writeNow(StompFrame.of(Builder.STOMP_COMMAND_DISCONNECT, Headers.empty(), Payload.empty()), true);
            } catch (final RuntimeException ignored) {
                // DISCONNECT is best-effort; transport cleanup still owns close completion.
            }
        }
        scope.closing();
        cancellation.cancel(cause);
        cleanup(cause);
        if (termination == Termination.CLOSE) {
            runHook(closeHook, "close");
        } else {
            runHook(cancelHook, termination == Termination.CANCEL ? "cancel" : "failure");
        }
        final boolean changed = switch (termination) {
            case CLOSE -> scope.close(this);
            case CANCEL -> scope.cancel(cause);
            case FAIL -> scope.fail(cause);
        };
        if (ownsDispatcher) {
            try {
                ThreadKit.execute(this::closeDispatcher);
            } catch (final RuntimeException e) {
                scope.emit(ObservationMarker.LISTENER_FAILED, e);
            }
        }
        Logger.info(
                false,
                "Fabric",
                "STOMP session terminated: scheme={}, host={}, port={}, state={}",
                scheme(),
                host(),
                port(),
                scope.state());
        return changed;
    }

    /**
     * Fails the session without replacing an operation failure with cleanup behavior.
     *
     * @param cause operation failure
     */
    private void operationFailed(final Throwable cause) {
        try {
            terminate(Termination.FAIL, cause);
        } catch (final RuntimeException cleanupFailure) {
            cause.addSuppressed(cleanupFailure);
        }
    }

    /**
     * Runs one transport hook without interrupting lifecycle cleanup.
     *
     * @param hook  transport cleanup action to invoke safely
     * @param phase terminal phase
     */
    private void runHook(final Runnable hook, final String phase) {
        try {
            hook.run();
        } catch (final RuntimeException e) {
            scope.emit(ObservationMarker.LISTENER_FAILED, e);
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "STOMP transport hook failed: phase={}, exception={}",
                    phase,
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Closes an internally created compatibility dispatcher outside its active Call.
     */
    private void closeDispatcher() {
        try {
            dispatcher.close();
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "STOMP dispatcher close failed: exception={}",
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Cancels and clears one owned heartbeat handle.
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
     * Returns monotonic elapsed nanoseconds without exposing negative clock movement.
     *
     * @param activityNanos activity timestamp
     * @return elapsed nanoseconds
     */
    private long elapsedSince(final long activityNanos) {
        return Math.max(Normal.LONG_ZERO, clock.nanos() - activityNanos);
    }

    /**
     * Ensures the session is open.
     */
    private void ensureOpen() {
        if (!opened() || terminating.get()) {
            throw new StatefulException("STOMP session is not open");
        }
        cancellation.throwIfCancelled();
    }

    /**
     * Ensures a normal Call may write, while allowing the owned DISCONNECT after termination is claimed.
     *
     * @param terminalWrite true for the close-owned DISCONNECT
     */
    private void ensureWritable(final boolean terminalWrite) {
        if (!opened() || !terminalWrite && terminating.get()) {
            throw new StatefulException("STOMP session is not open");
        }
        if (!terminalWrite) {
            cancellation.throwIfCancelled();
        }
    }

    /**
     * Returns the session scheme for logs.
     *
     * @return scheme or unknown
     */
    private String scheme() {
        return address == null ? Normal.UNKNOWN : address.scheme();
    }

    /**
     * Returns the session host for logs.
     *
     * @return host or unknown
     */
    private String host() {
        return address == null ? Normal.UNKNOWN : address.host();
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
     * @param value reference to validate
     * @param name  field name
     * @param <T>   value type
     * @return the validated reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Subscription holder.
     *
     * @param topic   subscription metadata and identifier
     * @param handler callback receiving matching messages
     */
    private record Subscription(StompTopic topic, Consumer<StompMessage> handler) {

    }

    /**
     * Lazy receipt wait Call backed by the session-owned receipt registry.
     */
    private final class ReceiptCall extends MonoCall<Void> {

        /**
         * Receipt identifier.
         */
        private final String receiptId;

        /**
         * Creates a lazy receipt wait Call.
         *
         * @param receiptId receipt identifier
         */
        private ReceiptCall(final String receiptId) {
            super("stomp-receipt", dispatcher, observer, timeout);
            this.receiptId = receiptId;
        }

        /**
         * Registers and waits for the matching receipt after the Call starts.
         *
         * @return null after receipt completion
         */
        @Override
        protected Void perform() {
            ensureOpen();
            receipts.await(receiptId, cancellation());
            return null;
        }

        /**
         * Returns the receipt dispatch key.
         *
         * @return dispatch key
         */
        @Override
        protected String dispatchKey() {
            return "stomp:receipt";
        }

    }

    /**
     * Session terminal path selected by the termination guard owner.
     */
    private enum Termination {

        /**
         * Normal close after best-effort DISCONNECT.
         */
        CLOSE,

        /**
         * Explicit cancellation.
         */
        CANCEL,

        /**
         * Protocol, transport, or heartbeat failure.
         */
        FAIL

    }

}
