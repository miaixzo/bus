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
package org.miaixz.bus.fabric.protocol.socket.calls;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Single-use socket open call backed by the shared protocol call lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketCall extends MonoCall<SocketSession> {

    /**
     * Single socket-open operation receiving the call lifecycle's cancellation scope.
     */
    private final Function<Cancellation, SocketSession> operation;

    /**
     * Dispatch key returned when this call is submitted asynchronously.
     */
    private final String key;

    /**
     * Session produced by the operation and available to running-call cancellation.
     */
    private final AtomicReference<SocketSession> session;

    /**
     * Creates an unsubmitted socket-open call.
     *
     * @param dispatcher dispatcher used by no-argument {@code enqueue()}
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param observer   observer receiving call lifecycle events
     * @param timeout    complete protocol timeout policy
     * @param operation  function that opens a session within the lifecycle cancellation scope
     * @param key        dispatch key used for asynchronous submission
     */
    private SocketCall(final Dispatcher dispatcher, final Callback<? super SocketSession> callback,
            final EventObserver observer, final Timeout timeout, final Function<Cancellation, SocketSession> operation,
            final String key) {
        super(Builder.SOCKET_TAG_OPEN, dispatcher, observer, callback, timeout);
        this.operation = require(operation, "Socket operation");
        this.key = require(key, "Socket dispatch key");
        this.session = new AtomicReference<>();
    }

    /**
     * Creates a call with the shared default timeout policy.
     *
     * @param dispatcher dispatcher used by no-argument {@code enqueue()}
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param observer   observer receiving call lifecycle events
     * @param operation  function that opens a session within the lifecycle cancellation scope
     * @param key        dispatch key used for asynchronous submission
     * @return new single-use, unsubmitted socket-open call
     */
    public static SocketCall create(
            final Dispatcher dispatcher,
            final Callback<? super SocketSession> callback,
            final EventObserver observer,
            final Function<Cancellation, SocketSession> operation,
            final String key) {
        return create(dispatcher, callback, observer, Timeout.defaults(), operation, key);
    }

    /**
     * Creates a call with a complete timeout policy.
     *
     * @param dispatcher dispatcher used by no-argument {@code enqueue()}
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param observer   observer receiving call lifecycle events
     * @param timeout    complete protocol timeout policy
     * @param operation  function that opens a session within the lifecycle cancellation scope
     * @param key        dispatch key used for asynchronous submission
     * @return new single-use, unsubmitted socket-open call
     * @throws ValidateException if {@code dispatcher}, {@code observer}, {@code operation}, or {@code key} is
     *                           {@code null}
     */
    public static SocketCall create(
            final Dispatcher dispatcher,
            final Callback<? super SocketSession> callback,
            final EventObserver observer,
            final Timeout timeout,
            final Function<Cancellation, SocketSession> operation,
            final String key) {
        return new SocketCall(require(dispatcher, "Dispatcher"), callback,
                EventObserver.safe(require(observer, "Observer")), require(timeout, "Timeout"), operation, key);
    }

    /**
     * Claims the call's single submission path and opens the socket session synchronously.
     *
     * @return session returned by the configured operation, which may be {@code null}
     */
    public SocketSession open() {
        return execute();
    }

    /**
     * Performs the socket open operation.
     *
     * @return result returned by the socket-open function
     */
    @Override
    protected SocketSession perform() {
        final SocketSession opened = operation.apply(cancellation());
        session.set(opened);
        return opened;
    }

    /**
     * Cancels the opened session when present.
     */
    @Override
    protected void cancelRunning() {
        final SocketSession current = session.get();
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
    protected void closeAfterCancelled(final SocketSession value) {
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
