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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Owns WebSocket ping scheduling and pong-deadline state.
 *
 * @author Kimi Liu
 */
final class WebSocketPing implements AutoCloseable {

    /**
     * Dispatcher used for the single scheduled tick.
     */
    private final Dispatcher dispatcher;

    /**
     * Dispatch registry key prefix.
     */
    private final String key;

    /**
     * Ping interval and pong deadline.
     */
    private final Duration interval;

    /**
     * Session cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Whether the owning session can currently send a ping.
     */
    private final BooleanSupplier active;

    /**
     * Callback that enqueues one automatic ping.
     */
    private final Runnable enqueue;

    /**
     * Callback that terminates the session when ping or pong processing fails.
     */
    private final Consumer<Throwable> failure;

    /**
     * Current scheduled tick.
     */
    private final AtomicReference<DispatchHandle> handle = new AtomicReference<>();

    /**
     * Whether a flushed ping is awaiting its pong.
     */
    private final AtomicBoolean awaitingPong = new AtomicBoolean();

    /**
     * Whether an automatic ping is queued but not yet flushed.
     */
    private final AtomicBoolean pending = new AtomicBoolean();

    /**
     * Sent ping count.
     */
    private final AtomicInteger sent = new AtomicInteger();

    /**
     * Received ping count.
     */
    private final AtomicInteger receivedPing = new AtomicInteger();

    /**
     * Received pong count.
     */
    private final AtomicInteger receivedPong = new AtomicInteger();

    /**
     * Creates a ping scheduler and pong-deadline tracker.
     *
     * @param dispatcher   dispatcher used for scheduling
     * @param key          dispatch registry key prefix
     * @param interval     ping interval
     * @param cancellation session cancellation
     * @param active       active-session predicate
     * @param enqueue      automatic-ping enqueue callback
     * @param failure      terminal failure callback
     */
    WebSocketPing(final Dispatcher dispatcher, final String key, final Duration interval,
            final Cancellation cancellation, final BooleanSupplier active, final Runnable enqueue,
            final Consumer<Throwable> failure) {
        this.dispatcher = dispatcher;
        this.key = key;
        this.interval = interval;
        this.cancellation = cancellation;
        this.active = active;
        this.enqueue = enqueue;
        this.failure = failure;
    }

    /**
     * Starts or reschedules the next tick.
     */
    void schedule() {
        if (dispatcher == null || interval.isZero() || cancellation.cancelled() || !active.getAsBoolean()) {
            return;
        }
        final DispatchHandle next = dispatcher
                .schedule(key + ":ping", interval, Activity.of("websocket:ping", this::tick, cancellation));
        final DispatchHandle previous = handle.getAndSet(next);
        if (previous != null && previous != next) {
            previous.cancel();
        }
    }

    /**
     * Records a received ping.
     */
    void receivedPing() {
        receivedPing.incrementAndGet();
    }

    /**
     * Records a received pong and satisfies the outstanding deadline.
     */
    void receivedPong() {
        receivedPong.incrementAndGet();
        awaitingPong.set(false);
    }

    /**
     * Records a flushed public ping.
     */
    void publicPingWritten() {
        sent.incrementAndGet();
        awaitingPong.set(true);
    }

    /**
     * Records a flushed automatic ping and schedules the following interval.
     */
    void automaticPingWritten() {
        publicPingWritten();
        pending.set(false);
        schedule();
    }

    /**
     * Returns the sent ping count.
     *
     * @return sent ping count
     */
    int sent() {
        return sent.get();
    }

    /**
     * Returns the received ping count.
     *
     * @return received ping count
     */
    int receivedPingCount() {
        return receivedPing.get();
    }

    /**
     * Returns the received pong count.
     *
     * @return received pong count
     */
    int receivedPongCount() {
        return receivedPong.get();
    }

    /**
     * Runs one ping tick.
     */
    private void tick() {
        handle.set(null);
        if (!active.getAsBoolean()) {
            return;
        }
        if (awaitingPong.get()) {
            failure.accept(new TimeoutException("WebSocket pong timeout"));
            return;
        }
        if (!pending.compareAndSet(false, true)) {
            schedule();
            return;
        }
        try {
            enqueue.run();
        } catch (final RuntimeException | Error e) {
            pending.set(false);
            failure.accept(e);
        }
    }

    /**
     * Cancels the scheduled tick and clears ping state.
     */
    @Override
    public void close() {
        awaitingPong.set(false);
        pending.set(false);
        final DispatchHandle current = handle.getAndSet(null);
        if (current != null) {
            current.cancel();
        }
    }

}
