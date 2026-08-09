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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Owns the single WebSocket close-handshake deadline.
 *
 * @author Kimi Liu
 */
final class WebSocketDeadline implements AutoCloseable {

    /**
     * Dispatcher used for the deadline.
     */
    private final Dispatcher dispatcher;

    /**
     * Dispatch registry key prefix.
     */
    private final String key;

    /**
     * Close-handshake timeout.
     */
    private final Duration timeout;

    /**
     * Session cancellation.
     */
    private final Cancellation cancellation;

    /**
     * Predicate indicating that the close handshake still awaits completion.
     */
    private final BooleanSupplier waiting;

    /**
     * Timeout action.
     */
    private final Runnable timeoutAction;

    /**
     * Current deadline handle.
     */
    private final AtomicReference<DispatchHandle> handle = new AtomicReference<>();

    /**
     * Creates a close-handshake deadline.
     *
     * @param dispatcher    dispatcher used for scheduling
     * @param key           dispatch key prefix
     * @param timeout       close timeout
     * @param cancellation  session cancellation
     * @param waiting       pending-handshake predicate
     * @param timeoutAction timeout action
     */
    WebSocketDeadline(final Dispatcher dispatcher, final String key, final Duration timeout,
            final Cancellation cancellation, final BooleanSupplier waiting, final Runnable timeoutAction) {
        this.dispatcher = dispatcher;
        this.key = key;
        this.timeout = timeout;
        this.cancellation = cancellation;
        this.waiting = waiting;
        this.timeoutAction = timeoutAction;
    }

    /**
     * Arms or replaces the close-handshake deadline.
     */
    void schedule() {
        if (dispatcher == null || cancellation.cancelled() || !waiting.getAsBoolean()) {
            return;
        }
        final DispatchHandle next = dispatcher
                .schedule(key + ":close-timeout", timeout, Activity.of("websocket:close-timeout", () -> {
                    handle.set(null);
                    if (waiting.getAsBoolean()) {
                        timeoutAction.run();
                    }
                }, cancellation));
        final DispatchHandle previous = handle.getAndSet(next);
        if (previous != null && previous != next) {
            previous.cancel();
        }
    }

    /**
     * Cancels the close deadline.
     */
    @Override
    public void close() {
        final DispatchHandle current = handle.getAndSet(null);
        if (current != null) {
            current.cancel();
        }
    }

}
