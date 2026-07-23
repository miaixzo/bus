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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.fabric.runtime.resource.ResourceScope;

/**
 * Internal lifecycle scope that owns state, cancellation, resources, listener callbacks, and observation events.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LifecycleScope {

    /**
     * Current lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Cancellation scope triggered by cancelled and failed terminal outcomes.
     */
    private final Cancellation cancellation;

    /**
     * Owned resource scope.
     */
    private final ResourceScope resources;

    /**
     * One-way guard ensuring resource cleanup, terminal emission, and listener notification run once.
     */
    private final AtomicBoolean terminal;

    /**
     * Default source used for listener callbacks and observation source tags; may be {@code null}.
     */
    private final Object source;

    /**
     * Non-blank name attached to every emitted event.
     */
    private final String name;

    /**
     * Listener or no-op substitute whose runtime failures are converted to observation events.
     */
    private final Listener<Object> listener;

    /**
     * Observer wrapper that contains observer failures.
     */
    private final EventObserver observer;

    /**
     * Clock used to timestamp emitted lifecycle events.
     */
    private final Clock clock;

    /**
     * Identifier shared by every event in this lifecycle.
     */
    private final String operationId;

    /**
     * Start event marker.
     */
    private final ObservationMarker startMarker;

    /**
     * Open event marker.
     */
    private final ObservationMarker openMarker;

    /**
     * Close or success event marker.
     */
    private final ObservationMarker closeMarker;

    /**
     * Cancellation event marker.
     */
    private final ObservationMarker cancelMarker;

    /**
     * Failure event marker.
     */
    private final ObservationMarker failureMarker;

    /**
     * Creates a queued lifecycle scope with new cancellation and resource scopes.
     *
     * @param source        default callback and event source, or {@code null}
     * @param name          non-blank lifecycle name attached to events
     * @param listener      lifecycle listener, or {@code null} to install a no-op listener
     * @param observer      event observer, or {@code null} to install a no-op observer
     * @param clock         clock used to timestamp events
     * @param startMarker   marker emitted after a successful running transition, or {@code null}
     * @param openMarker    marker emitted after a successful opened transition, or {@code null}
     * @param closeMarker   marker emitted for successful completion or close, or {@code null}
     * @param cancelMarker  marker emitted for cancellation, or {@code null}
     * @param failureMarker marker emitted for failure, or {@code null}
     */
    private LifecycleScope(final Object source, final String name, final Listener<Object> listener,
            final EventObserver observer, final Clock clock, final ObservationMarker startMarker,
            final ObservationMarker openMarker, final ObservationMarker closeMarker,
            final ObservationMarker cancelMarker, final ObservationMarker failureMarker) {
        this.state = new AtomicReference<>(Status.QUEUED);
        this.cancellation = Cancellation.create();
        this.resources = ResourceScope.create();
        this.terminal = new AtomicBoolean();
        this.source = source;
        this.name = Assert.notBlank(name, () -> new ValidateException("Lifecycle name must not be blank"));
        this.listener = listener == null ? noopListener() : listener;
        this.observer = EventObserver.safe(observer);
        this.clock = Assert.notNull(clock, () -> new ValidateException("Lifecycle clock must not be null"));
        this.operationId = ID.objectId();
        this.startMarker = startMarker;
        this.openMarker = openMarker;
        this.closeMarker = closeMarker;
        this.cancelMarker = cancelMarker;
        this.failureMarker = failureMarker;
    }

    /**
     * Creates a call lifecycle scope.
     *
     * @param name     non-blank call lifecycle name
     * @param observer event observer, or {@code null} to disable observation
     * @return queued call scope using the system clock and standard call markers
     * @throws ValidateException if {@code name} is blank
     */
    public static LifecycleScope call(final String name, final EventObserver observer) {
        return call(name, observer, Clock.system());
    }

    /**
     * Creates a call lifecycle scope with an explicit clock.
     *
     * @param name     non-blank call lifecycle name
     * @param observer event observer, or {@code null} to disable observation
     * @param clock    clock used to timestamp events
     * @return queued call scope using standard call markers
     * @throws ValidateException if {@code name} is blank or {@code clock} is {@code null}
     */
    public static LifecycleScope call(final String name, final EventObserver observer, final Clock clock) {
        return new LifecycleScope(null, name, noopListener(), observer, clock, ObservationMarker.CALL_START, null,
                ObservationMarker.CALL_SUCCESS, ObservationMarker.CALL_CANCELLED, ObservationMarker.CALL_FAILED);
    }

    /**
     * Creates a session lifecycle scope.
     *
     * @param source        default session callback source, which may be {@code null}
     * @param name          non-blank session lifecycle name
     * @param listener      session listener, or {@code null} to disable callbacks
     * @param observer      event observer, or {@code null} to disable observation
     * @param openMarker    session-open marker, or {@code null}
     * @param closeMarker   session-close marker, or {@code null}
     * @param failureMarker session-failure marker, or {@code null}
     * @param <T>           callback source type
     * @return queued session scope using the system clock and a protocol-derived cancellation marker
     * @throws ValidateException if {@code name} is blank
     */
    public static <T> LifecycleScope session(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer,
            final ObservationMarker openMarker,
            final ObservationMarker closeMarker,
            final ObservationMarker failureMarker) {
        return session(source, name, listener, observer, openMarker, closeMarker, failureMarker, Clock.system());
    }

    /**
     * Creates a session lifecycle scope with an explicit clock.
     *
     * @param source        default session callback source, which may be {@code null}
     * @param name          non-blank session lifecycle name
     * @param listener      session listener, or {@code null} to disable callbacks
     * @param observer      event observer, or {@code null} to disable observation
     * @param openMarker    session-open marker, or {@code null}
     * @param closeMarker   session-close marker, or {@code null}
     * @param failureMarker session-failure marker, or {@code null}
     * @param clock         clock used to timestamp events
     * @param <T>           callback source type
     * @return queued session scope with a protocol-derived cancellation marker
     * @throws ValidateException if {@code name} is blank or {@code clock} is {@code null}
     */
    public static <T> LifecycleScope session(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer,
            final ObservationMarker openMarker,
            final ObservationMarker closeMarker,
            final ObservationMarker failureMarker,
            final Clock clock) {
        return new LifecycleScope(source, name, cast(listener), observer, clock, null, openMarker, closeMarker,
                cancellationMarker(openMarker, failureMarker), failureMarker);
    }

    /**
     * Creates a resource lifecycle scope.
     *
     * @param source   default resource callback source, which may be {@code null}
     * @param name     non-blank resource lifecycle name
     * @param listener resource listener, or {@code null} to disable callbacks
     * @param observer event observer, or {@code null} to disable observation
     * @param <T>      callback source type
     * @return queued marker-free resource scope using the system clock
     * @throws ValidateException if {@code name} is blank
     */
    public static <T> LifecycleScope resource(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer) {
        return resource(source, name, listener, observer, Clock.system());
    }

    /**
     * Creates a resource lifecycle scope with an explicit clock.
     *
     * @param source   default resource callback source, which may be {@code null}
     * @param name     non-blank resource lifecycle name
     * @param listener resource listener, or {@code null} to disable callbacks
     * @param observer event observer, or {@code null} to disable observation
     * @param clock    clock used to timestamp explicitly emitted markers
     * @param <T>      callback source type
     * @return queued marker-free resource scope
     * @throws ValidateException if {@code name} is blank or {@code clock} is {@code null}
     */
    public static <T> LifecycleScope resource(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer,
            final Clock clock) {
        return new LifecycleScope(source, name, cast(listener), observer, clock, null, null, null, null, null);
    }

    /**
     * Returns the current lifecycle state.
     *
     * @return current authoritative lifecycle status
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns the cancellation scope.
     *
     * @return cancellation scope owned by this lifecycle
     */
    public Cancellation cancellation() {
        return cancellation;
    }

    /**
     * Registers a resource for reverse-order terminal cleanup.
     *
     * @param resource non-null closeable resource to own by identity
     * @param <T>      closeable resource type
     * @return the same resource reference
     * @throws ValidateException                                    if {@code resource} is {@code null}
     * @throws org.miaixz.bus.core.lang.exception.InternalException if the scope is closed and the rejected resource
     *                                                              cannot be closed
     * @throws org.miaixz.bus.core.lang.exception.StatefulException if terminal cleanup already closed the resource
     *                                                              scope
     */
    public <T extends AutoCloseable> T own(final T resource) {
        return resources.add(Assert.notNull(resource, () -> new ValidateException("Resource must not be null")));
    }

    /**
     * Moves the lifecycle to running and emits the configured start marker when the transition succeeds.
     *
     * @return {@code true} when the state changed to running
     */
    public boolean start() {
        final boolean changed = transit(Status.RUNNING);
        if (changed) {
            emit(startMarker, null);
        }
        return changed;
    }

    /**
     * Moves the lifecycle to opened using the configured source.
     *
     * @return {@code true} when the state changed to opened
     */
    public boolean open() {
        return open(source);
    }

    /**
     * Moves the lifecycle to opened.
     *
     * @param openedSource source passed to the open listener, or {@code null} to use the configured source
     * @return {@code true} when the state changed to opened
     */
    public boolean open(final Object openedSource) {
        final boolean changed = transit(Status.OPENED);
        if (changed) {
            emit(openMarker, null);
            notifyOpen(select(openedSource));
        }
        return changed;
    }

    /**
     * Moves a call-style lifecycle to done and runs successful terminal cleanup and close notification once.
     *
     * @return {@code true} when the state changed to done
     */
    public boolean complete() {
        final boolean changed = transit(Status.DONE);
        if (changed) {
            terminal(closeMarker, null, false, false, source);
        }
        return changed;
    }

    /**
     * Moves this lifecycle to closing.
     *
     * @return {@code true} when the state changed to closing
     */
    public boolean closing() {
        return transit(Status.CLOSING);
    }

    /**
     * Closes this lifecycle using the configured source.
     *
     * @return {@code true} when the state changed to closed
     */
    public boolean close() {
        return close(source);
    }

    /**
     * Closes this lifecycle.
     *
     * @param closedSource source passed to the close listener, or {@code null} to use the configured source
     * @return {@code true} when the state changed to closed
     */
    public boolean close(final Object closedSource) {
        closing();
        final boolean changed = transit(Status.CLOSED);
        if (changed) {
            terminal(closeMarker, null, false, false, select(closedSource));
        }
        return changed;
    }

    /**
     * Cancels this lifecycle with a newly created {@link CancellationException}.
     *
     * @return {@code true} when the state changed to cancelled
     */
    public boolean cancel() {
        return cancel(new CancellationException("Lifecycle cancelled"));
    }

    /**
     * Cancels this lifecycle.
     *
     * @param cause non-null cancellation cause recorded by the cancellation scope and event
     * @return {@code true} when the state changed to cancelled
     * @throws ValidateException if {@code cause} is {@code null}
     */
    public boolean cancel(final Throwable cause) {
        final Throwable current = Assert
                .notNull(cause, () -> new ValidateException("Cancellation cause must not be null"));
        final boolean changed = transit(Status.CANCELLED);
        if (changed) {
            terminal(cancelMarker, current, true, false, source);
        }
        return changed;
    }

    /**
     * Fails this lifecycle.
     *
     * @param cause non-null failure reported to observation and the listener
     * @return {@code true} when the state changed to failed
     * @throws ValidateException if {@code cause} is {@code null}
     */
    public boolean fail(final Throwable cause) {
        final Throwable current = Assert.notNull(cause, () -> new ValidateException("Failure cause must not be null"));
        final boolean changed = transit(Status.FAILED);
        if (changed) {
            terminal(failureMarker, current, true, true, source);
        }
        return changed;
    }

    /**
     * Emits an observation marker.
     *
     * @param marker marker to emit, or {@code null} for no operation
     */
    public void emit(final ObservationMarker marker) {
        emit(marker, null);
    }

    /**
     * Emits an observation marker.
     *
     * @param marker marker to emit, or {@code null} for no operation
     * @param cause  optional cause attached to the event
     */
    public void emit(final ObservationMarker marker, final Throwable cause) {
        if (marker == null) {
            return;
        }
        observer.emit(
                FabricEvent.builder(marker, clock).tag(Builder.TAG_OPERATION_ID, operationId)
                        .tag(Builder.LIFECYCLE_SCOPE_NAME, name).tag(Builder.TAG_SOURCE, sourceName(source))
                        .cause(cause).build());
    }

    /**
     * Moves the state with validation.
     *
     * @param next target status to compare against the current status rules
     * @return {@code true} when the atomic state changed; {@code false} for identical or disallowed transitions
     * @throws ValidateException if {@code next} is {@code null}
     */
    private boolean transit(final Status next) {
        while (true) {
            final Status current = state.get();
            if (current == next || !current.canTransit(next)) {
                return false;
            }
            if (state.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    /**
     * Cancels the shared scope when requested, closes owned resources, emits the terminal event, and invokes exactly
     * one terminal listener callback.
     *
     * @param marker      terminal marker to emit, or {@code null}
     * @param cause       terminal cause attached to cancellation and observation, or {@code null}
     * @param cancelScope whether to cancel the cancellation scope
     * @param failed      whether to notify listener failure
     * @param eventSource source passed to the terminal listener callback
     */
    private void terminal(
            final ObservationMarker marker,
            final Throwable cause,
            final boolean cancelScope,
            final boolean failed,
            final Object eventSource) {
        if (!terminal.compareAndSet(false, true)) {
            return;
        }
        if (cancelScope) {
            cancellation.cancel(cause);
        }
        try {
            resources.close();
        } catch (final RuntimeException e) {
            emit(ObservationMarker.LISTENER_FAILED, e);
        }
        emit(marker, cause);
        if (failed) {
            notifyFailure(eventSource, cause);
        } else {
            notifyClose(eventSource);
        }
    }

    /**
     * Invokes the open listener and converts a runtime callback failure into a listener-failed event.
     *
     * @param eventSource source passed to the listener
     */
    private void notifyOpen(final Object eventSource) {
        try {
            listener.open(eventSource);
        } catch (final RuntimeException e) {
            listenerFailed("open", e);
        }
    }

    /**
     * Invokes the close listener and converts a runtime callback failure into a listener-failed event.
     *
     * @param eventSource source passed to the listener
     */
    private void notifyClose(final Object eventSource) {
        try {
            listener.close(eventSource);
        } catch (final RuntimeException e) {
            listenerFailed("close", e);
        }
    }

    /**
     * Invokes the failure listener and converts a runtime callback failure into a listener-failed event.
     *
     * @param eventSource source passed to the listener
     * @param cause       lifecycle failure passed to the listener
     */
    private void notifyFailure(final Object eventSource, final Throwable cause) {
        try {
            listener.failure(eventSource, cause);
        } catch (final RuntimeException e) {
            listenerFailed("failure", e);
        }
    }

    /**
     * Emits a listener failure event.
     *
     * @param action listener callback name attached to the event
     * @param cause  runtime failure thrown by the listener
     */
    private void listenerFailed(final String action, final RuntimeException cause) {
        observer.emit(
                FabricEvent.builder(ObservationMarker.LISTENER_FAILED, clock).tag(Builder.TAG_OPERATION_ID, operationId)
                        .tag(Builder.LIFECYCLE_SCOPE_NAME, name).tag(Builder.TAG_ACTION, action)
                        .tag(Builder.TAG_SOURCE, sourceName(source)).cause(cause).build());
    }

    /**
     * Selects an event source.
     *
     * @param candidate callback source supplied by the transition, or {@code null}
     * @return candidate when non-null; otherwise the configured lifecycle source
     */
    private Object select(final Object candidate) {
        return candidate == null ? source : candidate;
    }

    /**
     * Selects the protocol-specific cancellation marker for a session.
     *
     * @param openMarker    session-open marker used to identify the protocol family, or {@code null}
     * @param failureMarker session-failure marker used as a second family hint and fallback, or {@code null}
     * @return cancellation marker or the failure marker when no dedicated marker exists
     */
    private static ObservationMarker cancellationMarker(
            final ObservationMarker openMarker,
            final ObservationMarker failureMarker) {
        if (openMarker == ObservationMarker.SOCKET_OPEN || failureMarker == ObservationMarker.SOCKET_FAILED) {
            return ObservationMarker.SOCKET_CANCELLED;
        }
        if (openMarker == ObservationMarker.WEBSOCKET_OPEN || failureMarker == ObservationMarker.WEBSOCKET_FAILED) {
            return ObservationMarker.WEBSOCKET_CANCELLED;
        }
        if (openMarker == ObservationMarker.SSE_OPEN || failureMarker == ObservationMarker.SSE_FAILED) {
            return ObservationMarker.SSE_CANCELLED;
        }
        if (openMarker == ObservationMarker.STOMP_OPEN || failureMarker == ObservationMarker.STOMP_FAILED) {
            return ObservationMarker.STOMP_CANCELLED;
        }
        if (openMarker == ObservationMarker.TLS_HANDSHAKE || failureMarker == ObservationMarker.TLS_FAILED) {
            return ObservationMarker.TLS_CANCELLED;
        }
        return failureMarker;
    }

    /**
     * Returns a safe source name.
     *
     * @param value source object to describe, or {@code null}
     * @return fully qualified source class name, or {@code unknown} for a null source
     */
    private static String sourceName(final Object value) {
        return value == null ? "unknown" : value.getClass().getName();
    }

    /**
     * Returns the no-op listener.
     *
     * @return singleton no-operation listener
     */
    private static Listener<Object> noopListener() {
        return NoopListener.INSTANCE;
    }

    /**
     * Casts a listener to the internal object listener type.
     *
     * @param listener typed listener to adapt, or {@code null}
     * @param <T>      callback source type accepted by the listener
     * @return the same listener cast to the internal object type, or the no-op singleton when null
     */
    private static <T> Listener<Object> cast(final Listener<? super T> listener) {
        return listener == null ? noopListener() : (Listener<Object>) listener;
    }

    /**
     * Internal no-operation listener.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum NoopListener implements Listener<Object> {

        /**
         * Singleton no-op listener.
         */
        INSTANCE

    }

}
