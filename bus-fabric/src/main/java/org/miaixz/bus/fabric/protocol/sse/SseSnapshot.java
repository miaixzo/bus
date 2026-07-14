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
package org.miaixz.bus.fabric.protocol.sse;

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.sse.event.SseRetry;

/**
 * Immutable execution snapshot for an SSE exchange.
 *
 * @param context         runtime services used by the SSE exchange
 * @param uri             original target URI requested by the caller
 * @param address         normalized HTTP address for the event stream
 * @param headers         request headers sent when opening the stream
 * @param timeout         connect and read timeout policy
 * @param retry           per-session retry policy copied from the builder
 * @param lastEventId     last event id sent during reconnect, or {@code null}
 * @param autoReconnect   whether the runner should reopen the stream after retryable disconnects
 * @param responseHandler callback receiving the opening HTTP status and headers
 * @param guard           optional policy guard for stream messages
 * @param filter          optional message filter for stream open and events
 * @param observer        observer receiving SSE lifecycle events
 * @param callback        callback receiving the opened SSE session or failure
 * @param handler         application event handler
 * @param listener        session lifecycle listener
 * @author Kimi Liu
 * @since Java 21+
 */
record SseSnapshot(Context context, URI uri, Address address, Headers headers, Timeout timeout, SseRetry retry,
        String lastEventId, boolean autoReconnect, BiConsumer<Integer, Headers> responseHandler, GuardRule guard,
        Filter filter, EventObserver observer, Callback<SseSession> callback, Consumer<SseEvent> handler,
        Listener<? super SseSession> listener) {

    /**
     * Creates a validated snapshot.
     */
    SseSnapshot {
        context = require(context, "Context");
        uri = require(uri, "Target URI");
        address = require(address, "Address");
        headers = require(headers, "Headers");
        timeout = require(timeout, "Timeout");
        retry = copyRetry(require(retry, "SSE retry"));
        responseHandler = require(responseHandler, "SSE response handler");
        observer = EventObserver.safe(require(observer, "Observer"));
        callback = Wiring.safeCallback(require(callback, "Callback"), observer);
        handler = require(handler, "SSE handler");
        listener = Wiring.safe(require(listener, "Listener"), observer);
    }

    /**
     * Copies the current retry delay into a session-local policy.
     *
     * @param retry retry source
     * @return copied retry policy
     */
    private static SseRetry copyRetry(final SseRetry retry) {
        final SseRetry copy = SseRetry.defaults();
        copy.update(retry.current());
        return copy;
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

}
