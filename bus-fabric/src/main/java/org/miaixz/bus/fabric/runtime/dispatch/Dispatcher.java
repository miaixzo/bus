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
package org.miaixz.bus.fabric.runtime.dispatch;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.runtime.Activity;

/**
 * Dispatcher contract for short, delayed, and long-running background activities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Dispatcher extends AutoCloseable {

    /**
     * Creates a dispatcher with a no-operation observer.
     *
     * @return dispatcher
     */
    static Dispatcher create() {
        return create(EventObserver.noop());
    }

    /**
     * Creates a dispatcher with an event observer.
     *
     * @param observer event observer
     * @return dispatcher
     */
    static Dispatcher create(final EventObserver observer) {
        return DefaultDispatcher.create(EventObserver.safe(observer));
    }

    /**
     * Runs a runnable asynchronously as a short task.
     *
     * @param key      dispatch key
     * @param runnable runnable task
     * @return completion future
     */
    default CompletableFuture<Void> run(final String key, final Runnable runnable) {
        return submit(key, Activity.of(key, runnable)).future();
    }

    /**
     * Supplies a value asynchronously as a short task.
     *
     * @param key      dispatch key
     * @param supplier supplier task
     * @param <T>      result type
     * @return result future
     */
    default <T> CompletableFuture<T> supply(final String key, final Supplier<T> supplier) {
        final CompletableFuture<T> result = new CompletableFuture<>();
        final DispatchHandle handle = submit(key, Activity.of(key, () -> result.complete(supplier.get())));
        handle.future().whenComplete((ignored, cause) -> {
            if (cause != null && !result.isDone()) {
                result.completeExceptionally(cause);
            }
        });
        result.whenComplete((ignored, cause) -> {
            if (result.isCancelled()) {
                handle.cancel();
            }
        });
        return result;
    }

    /**
     * Supplies a value on the separately bounded blocking-background channel.
     *
     * @param key      dispatch key
     * @param tag      cancellation tag
     * @param supplier blocking framework supplier
     * @param <T>      result type
     * @return independently cancellable result
     */
    default <T> CompletableFuture<T> backgroundSupply(final String key, final Object tag, final Supplier<T> supplier) {
        final CompletableFuture<T> result = new CompletableFuture<>();
        final DispatchHandle handle = background(key, tag, Activity.of(key, () -> result.complete(supplier.get())));
        handle.future().whenComplete((ignored, cause) -> {
            if (cause != null && !result.isDone()) {
                result.completeExceptionally(cause);
            }
        });
        result.whenComplete((ignored, cause) -> {
            if (result.isCancelled()) {
                handle.cancel();
            }
        });
        return result;
    }

    /**
     * Enqueues a short activity using its name as cancellation tag.
     *
     * @param key      dispatch key
     * @param activity short activity to enqueue
     * @return dispatch handle
     */
    default DispatchHandle enqueue(final String key, final Activity activity) {
        return submit(key, activity);
    }

    /**
     * Submits a short activity using its name as the cancellation tag.
     *
     * @param key      dispatch key
     * @param activity activity to submit
     * @return handle used to observe or cancel the submitted activity
     */
    default DispatchHandle submit(final String key, final Activity activity) {
        return enqueue(key, activity);
    }

    /**
     * Enqueues a tagged short activity.
     *
     * @param key      dispatch key
     * @param tag      cancellation tag
     * @param activity short activity to enqueue
     * @return dispatch handle
     */
    default DispatchHandle enqueue(final String key, final Object tag, final Activity activity) {
        return submit(key, activity);
    }

    /**
     * Starts a tagged long-running activity outside the short-task queue.
     *
     * @param key      dispatch key
     * @param tag      cancellation tag
     * @param activity long-running activity to start
     * @return dispatch handle
     */
    default DispatchHandle background(final String key, final Object tag, final Activity activity) {
        return enqueue(key, tag, activity);
    }

    /**
     * Schedules a short activity after a delay.
     *
     * @param key      dispatch key
     * @param delay    duration to wait before enqueueing
     * @param activity short activity to schedule
     * @return dispatch handle
     */
    default DispatchHandle schedule(final String key, final Duration delay, final Activity activity) {
        return enqueue(key, activity);
    }

    /**
     * Cancels one known handle.
     *
     * @param handle dispatch handle to cancel
     * @return true when cancellation changed work state
     */
    boolean cancel(DispatchHandle handle);

    /**
     * Cancels all known handles matching a tag.
     *
     * @param tag cancellation tag identifying matching work
     * @return true when at least one task changed
     */
    boolean cancel(Object tag);

    /**
     * Returns queued short, delayed, and not-yet-started background activities.
     *
     * @return queued activities
     */
    List<Activity> queued();

    /**
     * Returns running short and background activities.
     *
     * @return running activities
     */
    List<Activity> running();

    /**
     * Registers a callback to run once all dispatcher channels become idle.
     *
     * @param callback action to invoke after all channels become idle
     */
    void idle(Runnable callback);

    /**
     * Closes all dispatcher channels.
     */
    @Override
    void close();

}
