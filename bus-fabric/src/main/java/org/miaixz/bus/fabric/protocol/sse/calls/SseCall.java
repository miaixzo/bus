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
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.sse.SseSession;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Single-use SSE open call backed by the shared protocol call lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseCall extends MonoCall<SseSession> {

    /**
     * Single SSE-open operation receiving the call lifecycle's cancellation scope.
     */
    private final Function<Cancellation, SseSession> operation;

    /**
     * Dispatch key returned when this call is submitted asynchronously.
     */
    private final String key;

    /**
     * Session produced by the operation and available to running-call cancellation.
     */
    private final AtomicReference<SseSession> session;

    /**
     * Creates an unsubmitted SSE-open call.
     *
     * @param dispatcher dispatcher used by no-argument {@code enqueue()}
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param observer   observer receiving call lifecycle events
     * @param operation  function that opens a session within the lifecycle cancellation scope
     * @param key        dispatch key used for asynchronous submission
     */
    private SseCall(final Dispatcher dispatcher, final Callback<? super SseSession> callback,
            final EventObserver observer, final Function<Cancellation, SseSession> operation, final String key) {
        super(Builder.SSE_TAG_OPEN, dispatcher, observer, callback);
        this.operation = require(operation, "SSE operation");
        this.key = require(key, "SSE dispatch key");
        this.session = new AtomicReference<>();
    }

    /**
     * Creates a call.
     *
     * @param dispatcher dispatcher used by no-argument {@code enqueue()}
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param observer   observer receiving call lifecycle events
     * @param operation  function that opens a session within the lifecycle cancellation scope
     * @param key        dispatch key used for asynchronous submission
     * @return new single-use, unsubmitted SSE-open call
     * @throws ValidateException if {@code dispatcher}, {@code observer}, {@code operation}, or {@code key} is
     *                           {@code null}
     */
    public static SseCall create(
            final Dispatcher dispatcher,
            final Callback<? super SseSession> callback,
            final EventObserver observer,
            final Function<Cancellation, SseSession> operation,
            final String key) {
        return new SseCall(require(dispatcher, "Dispatcher"), callback,
                EventObserver.safe(require(observer, "Observer")), operation, key);
    }

    /**
     * Claims the call's single submission path and opens the SSE session synchronously.
     *
     * @return session returned by the configured operation, which may be {@code null}
     */
    public SseSession open() {
        return execute();
    }

    /**
     * Performs the SSE open operation.
     *
     * @return result returned by the SSE-open function
     */
    @Override
    protected SseSession perform() {
        final SseSession opened = operation.apply(cancellation());
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
     * @param value session produced after cancellation, or {@code null}
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
     * @return configured asynchronous dispatch key
     */
    @Override
    protected String dispatchKey() {
        return key;
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
