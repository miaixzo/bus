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

import java.util.concurrent.CancellationException;

import org.miaixz.bus.core.Lifecycle.State;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Protocol-neutral lifecycle composition used by long-lived fabric sessions.
 * <p>
 * The component owns public lifecycle transitions and terminal notification while the protocol session retains only
 * wire state, frame processing, and protocol-specific cleanup. Cancellation is linked once and never closes the
 * borrowed Dispatcher or Clock.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SessionLifecycle {

    /**
     * Shared lifecycle state and listener coordinator.
     */
    private final LifecycleScope scope;

    /**
     * Borrowed cancellation controller linked to this lifecycle.
     */
    private final Cancellation cancellation;

    /**
     * Idempotent cancellation callback removal.
     */
    private final Runnable unregister;

    /**
     * Creates a linked session lifecycle.
     *
     * @param scope        lifecycle scope
     * @param cancellation borrowed session cancellation
     */
    private SessionLifecycle(final LifecycleScope scope, final Cancellation cancellation) {
        this.scope = require(scope, "Lifecycle scope");
        this.cancellation = require(cancellation, "Cancellation");
        this.unregister = cancellation.onCancel(() -> {
            final Throwable cause = cancellation.cause();
            scope.cancel(cause == null ? new CancellationException("Session cancelled") : cause);
        });
    }

    /**
     * Creates a session lifecycle with explicit runtime collaborators.
     *
     * @param source        session callback source
     * @param name          lifecycle name
     * @param listener      lifecycle listener
     * @param observer      event observer
     * @param openMarker    open marker
     * @param closeMarker   close marker
     * @param failureMarker failure marker
     * @param clock         runtime clock
     * @param cancellation  borrowed cancellation controller
     * @param <T>           session source type
     * @return linked session lifecycle
     */
    public static <T> SessionLifecycle create(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer,
            final ObservationMarker openMarker,
            final ObservationMarker closeMarker,
            final ObservationMarker failureMarker,
            final Clock clock,
            final Cancellation cancellation) {
        return new SessionLifecycle(
                LifecycleScope.session(source, name, listener, observer, openMarker, closeMarker, failureMarker, clock),
                cancellation);
    }

    /**
     * Returns the public lifecycle state.
     *
     * @return current state
     */
    public State state() {
        return scope.state();
    }

    /**
     * Marks the session available.
     *
     * @param source listener source
     * @return true when state changed
     */
    public boolean open(final Object source) {
        return scope.open(source);
    }

    /**
     * Starts graceful termination.
     *
     * @return true when state changed
     */
    public boolean closing() {
        return scope.closing();
    }

    /**
     * Completes normal session closure.
     *
     * @param source listener source
     * @return true when state changed
     */
    public boolean close(final Object source) {
        unregister.run();
        return scope.close(source);
    }

    /**
     * Completes cancellation.
     *
     * @param cause cancellation cause
     * @return true when state changed
     */
    public boolean cancel(final Throwable cause) {
        unregister.run();
        final Throwable current = require(cause, "Cancellation cause");
        final boolean changed = scope.cancel(current);
        cancellation.cancel(current);
        return changed;
    }

    /**
     * Completes failure.
     *
     * @param cause failure cause
     * @return true when state changed
     */
    public boolean fail(final Throwable cause) {
        unregister.run();
        return scope.fail(require(cause, "Failure cause"));
    }

    /**
     * Emits a lifecycle observation.
     *
     * @param marker marker
     * @param cause  optional cause
     */
    public void emit(final ObservationMarker marker, final Throwable cause) {
        scope.emit(marker, cause);
    }

    /**
     * Validates required collaborators.
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
