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
     * Activity name for asynchronous socket opens.
     */

    /**
     * Socket protocol operation.
     */
    private final Function<Cancellation, SocketSession> operation;

    /**
     * Stable asynchronous dispatch key.
     */
    private final String key;

    /**
     * Opened session.
     */
    private final AtomicReference<SocketSession> session;

    /**
     * Creates a call.
     *
     * @param dispatcher dispatcher used by enqueue()
     * @param callback   callback managed by the call lifecycle
     * @param observer   lifecycle observer
     * @param operation  socket protocol operation
     * @param key        asynchronous dispatch key
     */
    private SocketCall(final Dispatcher dispatcher, final Callback<? super SocketSession> callback,
            final EventObserver observer, final Function<Cancellation, SocketSession> operation, final String key) {
        super(Builder.SOCKET_TAG_OPEN, dispatcher, observer, callback);
        this.operation = require(operation, "Socket operation");
        this.key = require(key, "Socket dispatch key");
        this.session = new AtomicReference<>();
    }

    /**
     * Creates a call.
     *
     * @param dispatcher dispatcher used by enqueue()
     * @param callback   callback managed by the call lifecycle
     * @param observer   lifecycle observer
     * @param operation  socket protocol operation
     * @param key        asynchronous dispatch key
     * @return call
     */
    public static SocketCall create(
            final Dispatcher dispatcher,
            final Callback<? super SocketSession> callback,
            final EventObserver observer,
            final Function<Cancellation, SocketSession> operation,
            final String key) {
        return new SocketCall(require(dispatcher, "Dispatcher"), callback,
                EventObserver.safe(require(observer, "Observer")), operation, key);
    }

    /**
     * Opens synchronously.
     *
     * @return session
     */
    public SocketSession open() {
        return execute();
    }

    /**
     * Performs the socket open operation.
     *
     * @return socket session
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
     * @param value produced session
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
     * @return dispatch key
     */
    @Override
    protected String dispatchKey() {
        return key;
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
