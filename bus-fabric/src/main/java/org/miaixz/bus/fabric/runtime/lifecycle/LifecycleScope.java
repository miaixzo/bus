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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tags.Tags;
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
     * Lifecycle name tag key.
     */
    private static final String NAME = "name";

    /**
     * Current lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Owned resource scope.
     */
    private final ResourceScope resources;

    /**
     * Terminal callback guard.
     */
    private final AtomicBoolean terminal;

    /**
     * Lifecycle source object.
     */
    private final Object source;

    /**
     * Lifecycle name.
     */
    private final String name;

    /**
     * Safe listener wrapper.
     */
    private final Listener<Object> listener;

    /**
     * Safe event observer.
     */
    private final EventObserver observer;

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
     * Creates a lifecycle scope.
     *
     * @param source        lifecycle source
     * @param name          lifecycle name
     * @param listener      lifecycle listener
     * @param observer      event observer
     * @param startMarker   start event marker
     * @param openMarker    open event marker
     * @param closeMarker   close event marker
     * @param cancelMarker  cancellation event marker
     * @param failureMarker failure event marker
     */
    private LifecycleScope(
            final Object source,
            final String name,
            final Listener<Object> listener,
            final EventObserver observer,
            final ObservationMarker startMarker,
            final ObservationMarker openMarker,
            final ObservationMarker closeMarker,
            final ObservationMarker cancelMarker,
            final ObservationMarker failureMarker) {
        this.state = new AtomicReference<>(Status.QUEUED);
        this.cancellation = Cancellation.create();
        this.resources = ResourceScope.create();
        this.terminal = new AtomicBoolean();
        this.source = source;
        this.name = Assert.notBlank(name, () -> new ValidateException("Lifecycle name must not be blank"));
        this.listener = listener == null ? noopListener() : listener;
        this.observer = EventObserver.safe(observer);
        this.startMarker = startMarker;
        this.openMarker = openMarker;
        this.closeMarker = closeMarker;
        this.cancelMarker = cancelMarker;
        this.failureMarker = failureMarker;
    }

    /**
     * Creates a call lifecycle scope.
     *
     * @param name     call name
     * @param observer event observer
     * @return lifecycle scope
     */
    public static LifecycleScope call(final String name, final EventObserver observer) {
        return new LifecycleScope(
                null,
                name,
                noopListener(),
                observer,
                ObservationMarker.CALL_START,
                null,
                ObservationMarker.CALL_SUCCESS,
                ObservationMarker.CALL_CANCELLED,
                ObservationMarker.CALL_FAILED);
    }

    /**
     * Creates a session lifecycle scope.
     *
     * @param source        session source
     * @param name          session name
     * @param listener      session listener
     * @param observer      event observer
     * @param openMarker    open event marker
     * @param closeMarker   close event marker
     * @param failureMarker failure event marker
     * @param <T>           source type
     * @return lifecycle scope
     */
    public static <T> LifecycleScope session(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer,
            final ObservationMarker openMarker,
            final ObservationMarker closeMarker,
            final ObservationMarker failureMarker) {
        return new LifecycleScope(
                source,
                name,
                cast(listener),
                observer,
                null,
                openMarker,
                closeMarker,
                failureMarker,
                failureMarker);
    }

    /**
     * Creates a resource lifecycle scope.
     *
     * @param source   resource source
     * @param name     resource name
     * @param listener resource listener
     * @param observer event observer
     * @param <T>      source type
     * @return lifecycle scope
     */
    public static <T> LifecycleScope resource(
            final T source,
            final String name,
            final Listener<? super T> listener,
            final EventObserver observer) {
        return new LifecycleScope(source, name, cast(listener), observer, null, null, null, null, null);
    }

    /**
     * Returns the current lifecycle state.
     *
     * @return lifecycle state
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns the cancellation scope.
     *
     * @return cancellation scope
     */
    public Cancellation cancellation() {
        return cancellation;
    }

    /**
     * Owns a closeable resource.
     *
     * @param resource resource
     * @param <T>      resource type
     * @return original resource
     */
    public <T extends AutoCloseable> T own(final T resource) {
        final T current = resources.add(Assert.notNull(resource, () -> new ValidateException("Resource must not be null")));
        cancellation.closeOnCancel(current);
        return current;
    }

    /**
     * Moves the lifecycle to running.
     *
     * @return true when the state changed
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
     * @return true when the state changed
     */
    public boolean open() {
        return open(source);
    }

    /**
     * Moves the lifecycle to opened.
     *
     * @param openedSource opened source
     * @return true when the state changed
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
     * Completes this lifecycle successfully.
     *
     * @return true when the state changed
     */
    public boolean complete() {
        final boolean changed = transit(Status.DONE);
        if (changed) {
            terminal(closeMarker, null, false, source);
        }
        return changed;
    }

    /**
     * Moves this lifecycle to closing.
     *
     * @return true when the state changed
     */
    public boolean closing() {
        return transit(Status.CLOSING);
    }

    /**
     * Closes this lifecycle using the configured source.
     *
     * @return true when the state changed
     */
    public boolean close() {
        return close(source);
    }

    /**
     * Closes this lifecycle.
     *
     * @param closedSource closed source
     * @return true when the state changed
     */
    public boolean close(final Object closedSource) {
        closing();
        final boolean changed = transit(Status.CLOSED);
        if (changed) {
            terminal(closeMarker, null, false, select(closedSource));
        }
        return changed;
    }

    /**
     * Cancels this lifecycle.
     *
     * @return true when the state changed
     */
    public boolean cancel() {
        return cancel(new CancellationException("Lifecycle cancelled"));
    }

    /**
     * Cancels this lifecycle.
     *
     * @param cause cancellation cause
     * @return true when the state changed
     */
    public boolean cancel(final Throwable cause) {
        final Throwable current = Assert.notNull(cause, () -> new ValidateException("Cancellation cause must not be null"));
        cancellation.cancel(current);
        final boolean changed = transit(Status.CANCELLED);
        if (changed) {
            terminal(cancelMarker, current, true, source);
        }
        return changed;
    }

    /**
     * Fails this lifecycle.
     *
     * @param cause failure cause
     * @return true when the state changed
     */
    public boolean fail(final Throwable cause) {
        final Throwable current = Assert.notNull(cause, () -> new ValidateException("Failure cause must not be null"));
        cancellation.cancel(current);
        final boolean changed = transit(Status.FAILED);
        if (changed) {
            terminal(failureMarker, current, true, source);
        }
        return changed;
    }

    /**
     * Emits an observation marker.
     *
     * @param marker marker
     */
    public void emit(final ObservationMarker marker) {
        emit(marker, null);
    }

    /**
     * Emits an observation marker.
     *
     * @param marker marker
     * @param cause  failure cause
     */
    public void emit(final ObservationMarker marker, final Throwable cause) {
        if (marker == null) {
            return;
        }
        observer.emit(FabricEvent.builder(marker)
                .tag(NAME, name)
                .tag(Tags.SOURCE, sourceName(source))
                .cause(cause)
                .build());
    }

    /**
     * Moves the state with validation.
     *
     * @param next next state
     * @return true when changed
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
     * Runs terminal cleanup and callbacks once.
     *
     * @param marker       terminal marker
     * @param cause        failure cause
     * @param failed       failure flag
     * @param eventSource  event source
     */
    private void terminal(
            final ObservationMarker marker,
            final Throwable cause,
            final boolean failed,
            final Object eventSource) {
        if (!terminal.compareAndSet(false, true)) {
            return;
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
     * Notifies listener open.
     *
     * @param eventSource event source
     */
    private void notifyOpen(final Object eventSource) {
        try {
            listener.open(eventSource);
        } catch (final RuntimeException e) {
            listenerFailed("open", e);
        }
    }

    /**
     * Notifies listener close.
     *
     * @param eventSource event source
     */
    private void notifyClose(final Object eventSource) {
        try {
            listener.close(eventSource);
        } catch (final RuntimeException e) {
            listenerFailed("close", e);
        }
    }

    /**
     * Notifies listener failure.
     *
     * @param eventSource event source
     * @param cause       failure cause
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
     * @param action listener action
     * @param cause  failure cause
     */
    private void listenerFailed(final String action, final RuntimeException cause) {
        observer.emit(FabricEvent.builder(ObservationMarker.LISTENER_FAILED)
                .tag(NAME, name)
                .tag(Tags.ACTION, action)
                .tag(Tags.SOURCE, sourceName(source))
                .cause(cause)
                .build());
    }

    /**
     * Selects an event source.
     *
     * @param candidate candidate source
     * @return selected source
     */
    private Object select(final Object candidate) {
        return candidate == null ? source : candidate;
    }

    /**
     * Returns a safe source name.
     *
     * @param value source value
     * @return source name
     */
    private static String sourceName(final Object value) {
        return value == null ? "unknown" : value.getClass().getName();
    }

    /**
     * Returns the no-op listener.
     *
     * @return listener
     */
    private static Listener<Object> noopListener() {
        return NoopListener.INSTANCE;
    }

    /**
     * Casts a listener to the internal object listener type.
     *
     * @param listener listener
     * @param <T>      source type
     * @return object listener
     */
    @SuppressWarnings("unchecked")
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
