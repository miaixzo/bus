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
package org.miaixz.bus.fabric.runtime.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle.State;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;

/**
 * Protocol-neutral server runtime for lifecycle, session registration, task ownership, and shutdown admission.
 *
 * @param <S> accepted session type
 * @author Kimi Liu
 */
public class ServerRuntime<S> {

    /**
     * Public server lifecycle.
     */
    private final LifecycleScope lifecycle;

    /**
     * Sessions registered after protocol setup succeeds.
     */
    private final Queue<S> sessions;

    /**
     * Accepted setup and accept-loop tasks.
     */
    private final Queue<DispatchHandle> handles;

    /**
     * One-way shutdown admission guard.
     */
    private final AtomicBoolean shuttingDown;

    /**
     * Creates a server runtime.
     *
     * @param lifecycle public lifecycle scope
     */
    public ServerRuntime(final LifecycleScope lifecycle) {
        this.lifecycle = require(lifecycle, "Lifecycle");
        this.sessions = new ConcurrentLinkedQueue<>();
        this.handles = new ConcurrentLinkedQueue<>();
        this.shuttingDown = new AtomicBoolean();
    }

    /**
     * Creates a protocol-neutral server runtime.
     *
     * @param source   server callback source
     * @param name     lifecycle name
     * @param listener server listener
     * @param observer observer
     * @param <T>      source type
     * @return server runtime
     */
    public static <S, T> ServerRuntime<S> create(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer) {
        return new ServerRuntime<>(LifecycleScope.resource(source, name, listener, observer));
    }

    /**
     * Returns the public lifecycle state.
     *
     * @return state
     */
    public State state() {
        return lifecycle.state();
    }

    /**
     * Marks the server accepting.
     *
     * @param source listener source
     * @return true when state changed
     */
    public boolean open(final Object source) {
        return lifecycle.open(source);
    }

    /**
     * Returns whether shutdown has started.
     *
     * @return true after shutdown admission closes
     */
    public boolean shuttingDown() {
        return shuttingDown.get();
    }

    /**
     * Atomically starts shutdown.
     *
     * @return true for the first shutdown caller
     */
    public boolean beginShutdown() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return false;
        }
        lifecycle.closing();
        return true;
    }

    /**
     * Registers a successfully established session.
     *
     * @param session session
     * @return same session
     */
    public S register(final S session) {
        final S current = require(session, "Session");
        if (shuttingDown.get()) {
            throw new IllegalStateException("Server is shutting down");
        }
        sessions.add(current);
        return current;
    }

    /**
     * Removes a terminal session.
     *
     * @param session session
     * @return true when registered
     */
    public boolean remove(final S session) {
        return sessions.remove(session);
    }

    /**
     * Returns an immutable session snapshot for shutdown traversal.
     *
     * @return session snapshot
     */
    public List<S> sessions() {
        return List.copyOf(new ArrayList<>(sessions));
    }

    /**
     * Returns whether registered sessions remain.
     *
     * @return true when non-empty
     */
    public boolean hasSessions() {
        return !sessions.isEmpty();
    }

    /**
     * Clears terminal session registrations.
     */
    public void clearSessions() {
        sessions.clear();
    }

    /**
     * Tracks an owned task until its completion.
     *
     * @param handle task handle
     * @return same handle
     */
    public DispatchHandle track(final DispatchHandle handle) {
        final DispatchHandle current = require(handle, "Dispatch handle");
        handles.add(current);
        current.future().whenComplete((ignored, cause) -> handles.remove(current));
        return current;
    }

    /**
     * Cancels all tracked tasks.
     */
    public void cancelTasks() {
        DispatchHandle handle;
        while ((handle = handles.poll()) != null) {
            handle.cancel();
        }
    }

    /**
     * Completes normal closure.
     *
     * @param source listener source
     * @return true when state changed
     */
    public boolean close(final Object source) {
        return lifecycle.close(source);
    }

    /**
     * Completes cancellation.
     *
     * @return true when state changed
     */
    public boolean cancel() {
        return lifecycle.cancel();
    }

    /**
     * Completes failure.
     *
     * @param cause failure
     * @return true when state changed
     */
    public boolean fail(final Throwable cause) {
        return lifecycle.fail(cause);
    }

    /**
     * Emits one server observation.
     *
     * @param marker marker
     * @param cause  cause
     */
    public void emit(final ObservationMarker marker, final Throwable cause) {
        lifecycle.emit(marker, cause);
    }

    /**
     * Validates required values.
     *
     * @param value value
     * @param name  diagnostic name
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
