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
package org.miaixz.bus.fabric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tags.Tags;

/**
 * Wires lifecycle listener callbacks, composition, dispatching, and failure protection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Wiring {

    /**
     * Hidden constructor for utility class.
     */
    private Wiring() {
        // No initialization required.
    }

    /**
     * Returns the shared no-operation lifecycle listener.
     *
     * @param <T> source type
     * @return no-operation listener
     */
    public static <T> Listener<T> noop() {
        final Listener<Object> listener = Instances.get(Wiring.class.getName() + ".noop", NoopListener::new);
        return (Listener<T>) listener;
    }

    /**
     * Returns the shared no-operation callback.
     *
     * @param <T> callback value type
     * @return no-operation callback
     */
    public static <T> Callback<T> callback() {
        final Callback<Object> callback = Instances.get(Wiring.class.getName() + ".callback", NoopCallback::new);
        return (Callback<T>) callback;
    }

    /**
     * Creates a listener that handles open events.
     *
     * @param open open consumer
     * @param <T>  source type
     * @return listener
     */
    public static <T> Listener<T> open(final Consumer<? super T> open) {
        return of(open, source -> {
        }, (source, cause) -> {
        });
    }

    /**
     * Creates a listener that handles close events.
     *
     * @param close close consumer
     * @param <T>   source type
     * @return listener
     */
    public static <T> Listener<T> close(final Consumer<? super T> close) {
        return of(source -> {
        }, close, (source, cause) -> {
        });
    }

    /**
     * Creates a listener that handles failure events.
     *
     * @param failure failure consumer
     * @param <T>     source type
     * @return listener
     */
    public static <T> Listener<T> failure(final BiConsumer<? super T, ? super Throwable> failure) {
        return of(source -> {
        }, source -> {
        }, failure);
    }

    /**
     * Creates a listener from function callbacks.
     *
     * @param open    open consumer
     * @param close   close consumer
     * @param failure failure consumer
     * @param <T>     source type
     * @return listener
     */
    public static <T> Listener<T> of(
            final Consumer<? super T> open,
            final Consumer<? super T> close,
            final BiConsumer<? super T, ? super Throwable> failure) {
        final Consumer<? super T> openConsumer = Assert
                .notNull(open, () -> new ValidateException("Open listener must not be null"));
        final Consumer<? super T> closeConsumer = Assert
                .notNull(close, () -> new ValidateException("Close listener must not be null"));
        final BiConsumer<? super T, ? super Throwable> failureConsumer = Assert
                .notNull(failure, () -> new ValidateException("Failure listener must not be null"));
        return new FunctionalListener<>(openConsumer, closeConsumer, failureConsumer);
    }

    /**
     * Combines listeners in order.
     *
     * @param listeners listeners
     * @param <T>       source type
     * @return composed listener
     */
    @SafeVarargs
    public static <T> Listener<T> compose(final Listener<? super T>... listeners) {
        if (listeners == null || listeners.length == 0) {
            return noop();
        }
        final List<Listener<? super T>> values = new ArrayList<>(listeners.length);
        for (final Listener<? super T> listener : listeners) {
            if (listener != null) {
                values.add(listener);
            }
        }
        return compose(values);
    }

    /**
     * Combines listeners in order.
     *
     * @param listeners listeners
     * @param <T>       source type
     * @return composed listener
     */
    public static <T> Listener<T> compose(final Collection<? extends Listener<? super T>> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            return noop();
        }
        final List<Listener<? super T>> values = new ArrayList<>(listeners.size());
        for (final Listener<? super T> listener : listeners) {
            if (listener != null) {
                values.add(listener);
            }
        }
        if (values.isEmpty()) {
            return noop();
        }
        if (values.size() == 1) {
            return new SingleListener<>(values.get(0));
        }
        return new CompositeListener<>(values);
    }

    /**
     * Runs listener callbacks on an executor.
     *
     * @param executor executor
     * @param listener listener
     * @param <T>      source type
     * @return asynchronous listener
     */
    public static <T> Listener<T> async(final Executor executor, final Listener<? super T> listener) {
        final Executor current = Assert
                .notNull(executor, () -> new ValidateException("Listener executor must not be null"));
        final Listener<? super T> target = listener == null ? noop() : safe(listener, EventObserver.noop());
        return new AsyncListener<>(current, target);
    }

    /**
     * Protects lifecycle callbacks from escaping listener failures.
     *
     * @param listener listener
     * @param observer observer that receives listener failure events
     * @param <T>      source type
     * @return safe listener
     */
    public static <T> Listener<T> safe(final Listener<? super T> listener, final EventObserver observer) {
        final Listener<? super T> target = listener == null ? noop() : listener;
        final EventObserver current = EventObserver.safe(observer);
        return new SafeListener<>(current, target);
    }

    /**
     * Protects call callbacks from escaping user callback failures.
     *
     * @param callback callback
     * @param observer observer that receives callback failure events
     * @param <T>      callback value type
     * @return safe callback
     */
    public static <T> Callback<T> safeCallback(final Callback<? super T> callback, final EventObserver observer) {
        final Callback<? super T> target = callback == null ? callback() : callback;
        final EventObserver current = EventObserver.safe(observer);
        return new SafeCallback<>(current, target);
    }

    /**
     * Shared listener that intentionally ignores all lifecycle events.
     *
     * @param <T> lifecycle source type
     */
    private static final class NoopListener<T> implements Listener<T> {
    }

    /**
     * Shared callback that intentionally ignores both success and failure.
     *
     * @param <T> callback value type
     */
    private static final class NoopCallback<T> implements Callback<T> {

        /**
         * Ignores a successful callback value.
         *
         * @param value successful value
         */
        @Override
        public void success(final T value) {
            // No-op callback intentionally ignores successful values.
        }

        /**
         * Ignores a callback failure cause.
         *
         * @param cause failure cause
         */
        @Override
        public void failure(final Throwable cause) {
            // No-op callback intentionally ignores failure causes.
        }
    }

    /**
     * Listener backed by caller supplied lifecycle functions.
     *
     * @param <T> lifecycle source type
     */
    private static final class FunctionalListener<T> implements Listener<T> {

        /**
         * Function called for open events.
         */
        private final Consumer<? super T> open;

        /**
         * Function called for close events.
         */
        private final Consumer<? super T> close;

        /**
         * Function called for failure events.
         */
        private final BiConsumer<? super T, ? super Throwable> failure;

        /**
         * Creates a listener backed by lifecycle functions.
         *
         * @param open    open function
         * @param close   close function
         * @param failure failure function
         */
        private FunctionalListener(final Consumer<? super T> open, final Consumer<? super T> close,
                                   final BiConsumer<? super T, ? super Throwable> failure) {
            this.open = open;
            this.close = close;
            this.failure = failure;
        }

        /**
         * Forwards an open event to the configured open function.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final T source) {
            open.accept(source);
        }

        /**
         * Forwards a close event to the configured close function.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final T source) {
            close.accept(source);
        }

        /**
         * Forwards a failure event to the configured failure function.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final T source, final Throwable cause) {
            failure.accept(source, cause);
        }
    }

    /**
     * Listener wrapper used when composition contains exactly one listener.
     *
     * @param <T> lifecycle source type
     */
    private static final class SingleListener<T> implements Listener<T> {

        /**
         * Wrapped listener.
         */
        private final Listener<? super T> listener;

        /**
         * Creates a single-listener wrapper.
         *
         * @param listener wrapped listener
         */
        private SingleListener(final Listener<? super T> listener) {
            this.listener = listener;
        }

        /**
         * Forwards an open event to the wrapped listener.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final T source) {
            listener.open(source);
        }

        /**
         * Forwards a close event to the wrapped listener.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final T source) {
            listener.close(source);
        }

        /**
         * Forwards a failure event to the wrapped listener.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final T source, final Throwable cause) {
            listener.failure(source, cause);
        }
    }

    /**
     * Listener that invokes several listeners in declaration order.
     *
     * @param <T> lifecycle source type
     */
    private static final class CompositeListener<T> implements Listener<T> {

        /**
         * Ordered listeners.
         */
        private final List<Listener<? super T>> listeners;

        /**
         * Creates an ordered listener composition.
         *
         * @param listeners ordered listeners
         */
        private CompositeListener(final List<Listener<? super T>> listeners) {
            this.listeners = List.copyOf(listeners);
        }

        /**
         * Invokes composed listeners for an open event in declaration order.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final T source) {
            each(listeners, listener -> listener.open(source));
        }

        /**
         * Invokes composed listeners for a close event in declaration order.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final T source) {
            each(listeners, listener -> listener.close(source));
        }

        /**
         * Invokes composed listeners for a failure event in declaration order.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final T source, final Throwable cause) {
            each(listeners, listener -> listener.failure(source, cause));
        }
    }

    /**
     * Listener that dispatches lifecycle callbacks through an executor.
     *
     * @param <T> lifecycle source type
     */
    private static final class AsyncListener<T> implements Listener<T> {

        /**
         * Executor receiving callback work.
         */
        private final Executor executor;

        /**
         * Listener invoked inside executor tasks.
         */
        private final Listener<? super T> listener;

        /**
         * Creates an asynchronous listener wrapper.
         *
         * @param executor executor
         * @param listener listener
         */
        private AsyncListener(final Executor executor, final Listener<? super T> listener) {
            this.executor = executor;
            this.listener = listener;
        }

        /**
         * Dispatches an open event through the configured executor.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final T source) {
            executor.execute(() -> listener.open(source));
        }

        /**
         * Dispatches a close event through the configured executor.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final T source) {
            executor.execute(() -> listener.close(source));
        }

        /**
         * Dispatches a failure event through the configured executor.
         *
         * @param source lifecycle source
         * @param cause  failure cause
         */
        @Override
        public void failure(final T source, final Throwable cause) {
            executor.execute(() -> listener.failure(source, cause));
        }
    }

    /**
     * Listener wrapper that reports callback failures instead of allowing them to escape.
     *
     * @param <T> lifecycle source type
     */
    private static final class SafeListener<T> implements Listener<T> {

        /**
         * Observer receiving listener failure events.
         */
        private final EventObserver observer;

        /**
         * Protected listener.
         */
        private final Listener<? super T> listener;

        /**
         * Creates a safe listener wrapper.
         *
         * @param observer observer
         * @param listener listener
         */
        private SafeListener(final EventObserver observer, final Listener<? super T> listener) {
            this.observer = observer;
            this.listener = listener;
        }

        /**
         * Invokes the wrapped open callback and reports listener failures.
         *
         * @param source lifecycle source
         */
        @Override
        public void open(final T source) {
            invoke(observer, "open", source, null, () -> listener.open(source));
        }

        /**
         * Invokes the wrapped close callback and reports listener failures.
         *
         * @param source lifecycle source
         */
        @Override
        public void close(final T source) {
            invoke(observer, "close", source, null, () -> listener.close(source));
        }

        /**
         * Invokes the wrapped failure callback and reports listener failures.
         *
         * @param source lifecycle source
         * @param cause  lifecycle failure cause
         */
        @Override
        public void failure(final T source, final Throwable cause) {
            invoke(observer, "failure", source, cause, () -> listener.failure(source, cause));
        }
    }

    /**
     * Callback wrapper that reports callback failures instead of allowing them to escape.
     *
     * @param <T> callback value type
     */
    private static final class SafeCallback<T> implements Callback<T> {

        /**
         * Observer receiving callback failure events.
         */
        private final EventObserver observer;

        /**
         * Protected callback.
         */
        private final Callback<? super T> callback;

        /**
         * Creates a safe callback wrapper.
         *
         * @param observer observer
         * @param callback callback
         */
        private SafeCallback(final EventObserver observer, final Callback<? super T> callback) {
            this.observer = observer;
            this.callback = callback;
        }

        /**
         * Invokes the wrapped success callback and reports callback failures.
         *
         * @param value successful value
         */
        @Override
        public void success(final T value) {
            invoke(observer, "callback.success", value, null, () -> callback.success(value));
        }

        /**
         * Invokes the wrapped failure callback and reports callback failures.
         *
         * @param cause callback failure cause
         */
        @Override
        public void failure(final Throwable cause) {
            invoke(observer, "callback.failure", cause, cause, () -> callback.failure(cause));
        }
    }

    /**
     * Invokes a listener callback and reports callback failures.
     *
     * @param observer observer
     * @param action   listener action
     * @param source   lifecycle source
     * @param cause    lifecycle failure cause
     * @param task     listener task
     */
    private static void invoke(
            final EventObserver observer,
            final String action,
            final Object source,
            final Throwable cause,
            final Runnable task) {
        try {
            task.run();
        } catch (final RuntimeException e) {
            report(observer, action, source, cause, e);
        }
    }

    /**
     * Reports listener callback failures to the observer.
     *
     * @param observer        observer
     * @param action          listener action
     * @param source          lifecycle source
     * @param lifecycleCause  lifecycle failure cause
     * @param listenerFailure listener failure
     */
    private static void report(
            final EventObserver observer,
            final String action,
            final Object source,
            final Throwable lifecycleCause,
            final RuntimeException listenerFailure) {
        try {
            final FabricEvent.Builder event = FabricEvent.builder(ObservationMarker.LISTENER_FAILED)
                    .tag(Tags.ACTION, action).tag(Tags.SOURCE, source == null ? "null" : source.getClass().getName());
            if (lifecycleCause != null) {
                event.tag(Tags.CAUSE, lifecycleCause.getClass().getName());
            }
            observer.emit(event.cause(listenerFailure).build());
        } catch (final RuntimeException ignored) {
            // Listener safety must never escape through observer failures.
        }
    }

    /**
     * Invokes all listeners and raises the first listener failure after all listeners run.
     *
     * @param listeners listeners
     * @param action    listener action
     * @param <T>       source type
     */
    private static <T> void each(
            final List<Listener<? super T>> listeners,
            final Consumer<Listener<? super T>> action) {
        RuntimeException failure = null;
        for (final Listener<? super T> listener : listeners) {
            try {
                action.accept(listener);
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

}
