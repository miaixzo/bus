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
package org.miaixz.bus.fabric.protocol.sse.calls;

import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.sse.SseSession;
import org.miaixz.bus.fabric.protocol.sse.SseX;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Single-use SSE open call backed by the shared protocol call lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseCall extends MonoCall<SseSession> {

    /**
     * Dispatcher activity name for opening the SSE stream.
     */
    private static final String ACTIVITY_OPEN = "sse-open";

    /**
     * Source exchange.
     */
    private final SseX exchange;

    /**
     * Opened session.
     */
    private final AtomicReference<SseSession> session;

    /**
     * Creates a call.
     *
     * @param exchange exchange
     */
    private SseCall(final SseX exchange) {
        this(exchange, null);
    }

    /**
     * Creates a call.
     *
     * @param exchange   exchange
     * @param dispatcher dispatcher used by enqueue()
     */
    private SseCall(final SseX exchange, final Dispatcher dispatcher) {
        super(ACTIVITY_OPEN, dispatcher, EventObserver.noop());
        this.exchange = require(exchange, "SSE exchange");
        this.session = new AtomicReference<>();
    }

    /**
     * Creates a call.
     *
     * @param exchange exchange
     * @return call
     */
    public static SseCall create(final SseX exchange) {
        return new SseCall(exchange);
    }

    /**
     * Creates a call.
     *
     * @param exchange   exchange
     * @param dispatcher dispatcher used by enqueue()
     * @return call
     */
    public static SseCall create(final SseX exchange, final Dispatcher dispatcher) {
        return new SseCall(exchange, require(dispatcher, "Dispatcher"));
    }

    /**
     * Opens synchronously.
     *
     * @return session
     */
    public SseSession open() {
        return execute();
    }

    /**
     * Performs the SSE open operation.
     *
     * @return SSE session
     */
    @Override
    protected SseSession perform() {
        final SseSession opened = exchange.open();
        session.set(opened);
        return opened;
    }

    /**
     * Cancels the opened session when present.
     */
    @Override
    protected void cancelRunning() {
        final SseSession current = session.get();
        if (current != null) {
            current.cancel();
        }
    }

    /**
     * Cancels a session produced after cancellation.
     *
     * @param value produced session
     */
    @Override
    protected void closeAfterCancelled(final SseSession value) {
        if (value != null) {
            value.cancel();
        }
    }

    /**
     * Returns the dispatch key.
     *
     * @return dispatch key
     */
    @Override
    protected String dispatchKey() {
        return exchange.dispatchKey();
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
