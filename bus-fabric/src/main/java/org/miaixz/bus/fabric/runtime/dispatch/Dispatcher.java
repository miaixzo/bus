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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Dispatcher contract for asynchronous activities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Dispatcher extends AutoCloseable {

    /**
     * Creates a dispatcher.
     *
     * @return dispatcher
     */
    static Dispatcher create() {
        return new DefaultDispatcher(new DispatchQueue(DispatchLimit.defaults()), DispatchWorker.create(),
                ThreadKit.newScheduledExecutor(Normal._1));
    }

    /**
     * Runs a runnable asynchronously.
     *
     * @param key      dispatch key
     * @param runnable runnable task
     * @return execution future
     */
    CompletableFuture<Void> run(String key, Runnable runnable);

    /**
     * Supplies a value asynchronously.
     *
     * @param key      dispatch key
     * @param supplier supplier task
     * @param <T>      value type
     * @return execution future
     */
    <T> CompletableFuture<T> supply(String key, Supplier<T> supplier);

    /**
     * Enqueues an activity for dispatch.
     *
     * @param key      dispatch key
     * @param activity activity
     * @return dispatch handle
     */
    DispatchHandle enqueue(String key, Activity activity);

    /**
     * Schedules an activity after a delay.
     *
     * @param key      dispatch key
     * @param delay    delay duration
     * @param activity activity
     * @return dispatch handle
     */
    DispatchHandle schedule(String key, Duration delay, Activity activity);

    /**
     * Cancels an enqueued handle.
     *
     * @param handle dispatch handle
     * @return true when cancellation changed the handle state
     */
    boolean cancel(DispatchHandle handle);

    /**
     * Cancels work by tag.
     *
     * @param tag tag
     * @return true when work was cancelled
     */
    boolean cancel(Object tag);

    /**
     * Returns queued activity snapshots.
     *
     * @return queued activities
     */
    List<Activity> queued();

    /**
     * Returns running activity snapshots.
     *
     * @return running activities
     */
    List<Activity> running();

    /**
     * Registers a callback that runs when dispatch becomes idle.
     *
     * @param callback idle callback
     */
    void idle(Runnable callback);

    /**
     * Closes dispatcher resources.
     */
    @Override
    void close();

}

/**
 * Default asynchronous dispatcher backed by a queue and worker.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class DefaultDispatcher implements Dispatcher {

    /**
     * Dispatch queue.
     */
    private final DispatchQueue queue;

    /**
     * Worker executor wrapper.
     */
    private final DispatchWorker worker;

    /**
     * Scheduler for delayed dispatch.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Dispatcher lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Idle callbacks awaiting an empty queue.
     */
    private final List<Runnable> idleCallbacks;

    /**
     * Delayed tasks waiting to enter the dispatch queue.
     */
    private final List<DelayedDispatch> delayed;

    /**
     * Creates a dispatcher.
     *
     * @param queue     dispatch queue
     * @param worker    dispatch worker
     * @param scheduler delay scheduler
     */
    DefaultDispatcher(final DispatchQueue queue, final DispatchWorker worker,
            final ScheduledExecutorService scheduler) {
        this.queue = require(queue, "Dispatch queue");
        this.worker = require(worker, "Dispatch worker");
        this.scheduler = require(scheduler, "Dispatch scheduler");
        this.scope = LifecycleScope.resource(this, "dispatcher", null, EventObserver.noop());
        this.scope.open(this);
        this.idleCallbacks = new ArrayList<>();
        this.delayed = new ArrayList<>();
    }

    /**
     * Returns the dispatcher lifecycle state.
     *
     * @return lifecycle state
     */
    public Status state() {
        return scope.state();
    }

    /**
     * Enqueues a runnable activity and returns its completion future.
     *
     * @param key      dispatch key
     * @param runnable runnable task
     * @return completion future
     */
    @Override
    public CompletableFuture<Void> run(final String key, final Runnable runnable) {
        require(runnable, "Runnable");
        return enqueue(key, Activity.of(key, runnable)).future();
    }

    /**
     * Enqueues a supplier activity and returns a typed completion future.
     *
     * @param key      dispatch key
     * @param supplier supplier task
     * @param <T>      result type
     * @return typed completion future
     */
    @Override
    public <T> CompletableFuture<T> supply(final String key, final Supplier<T> supplier) {
        require(supplier, "Supplier");
        final CompletableFuture<T> result = new CompletableFuture<>();
        final DispatchHandle handle = enqueue(key, Activity.of(key, () -> {
            try {
                result.complete(supplier.get());
            } catch (final RuntimeException e) {
                result.completeExceptionally(e);
                throw e;
            }
        }));
        handle.future().whenComplete((ignored, cause) -> {
            if (cause != null && !result.isDone()) {
                result.completeExceptionally(cause);
            } else if (handle.cancelled() && !result.isDone()) {
                result.cancel(false);
            }
        });
        result.whenComplete((value, cause) -> {
            if (result.isCancelled()) {
                cancel(handle);
            }
        });
        return result;
    }

    /**
     * Enqueues an activity for dispatch respecting queue limits.
     *
     * @param key      dispatch key
     * @param activity activity
     * @return dispatch handle
     */
    @Override
    public DispatchHandle enqueue(final String key, final Activity activity) {
        final Activity current = require(activity, "Dispatch activity");
        return enqueue(DispatchHandle.of(key, current.name(), current));
    }

    /**
     * Schedules an activity after a non-negative delay.
     *
     * @param key      dispatch key
     * @param delay    delay duration
     * @param activity activity
     * @return dispatch handle
     */
    @Override
    public DispatchHandle schedule(final String key, final Duration delay, final Activity activity) {
        final Duration currentDelay = require(delay, "Dispatch delay");
        final Activity currentActivity = require(activity, "Dispatch activity");
        Assert.isFalse(currentDelay.isNegative(), () -> new ValidateException("Dispatch delay must not be negative"));
        ensureOpen();
        final DispatchHandle handle = DispatchHandle.of(key, currentActivity.name(), currentActivity);
        if (currentDelay.isZero()) {
            return enqueue(handle);
        }
        final DelayedDispatch task = new DelayedDispatch(handle);
        try {
            synchronized (delayed) {
                delayed.add(task);
                task.future = scheduler.schedule(() -> runDelayed(task), currentDelay.toNanos(), TimeUnit.NANOSECONDS);
            }
        } catch (final RuntimeException e) {
            removeDelayed(task);
            handle.fail(e);
            throw new StatefulException("Dispatch scheduler rejected activity", e);
        }
        handle.future().whenComplete((ignored, cause) -> {
            if (handle.cancelled()) {
                cancelDelayed(task);
            }
        });
        return handle;
    }

    /**
     * Cancels a known dispatch handle.
     *
     * @param handle dispatch handle
     * @return true when the handle was cancelled
     */
    @Override
    public boolean cancel(final DispatchHandle handle) {
        final boolean cancelled = cancelKnown(require(handle, "Dispatch handle"));
        if (cancelled) {
            promoteQueued();
            drainIdleCallbacks();
        }
        return cancelled;
    }

    /**
     * Cancels all dispatch handles matching a tag.
     *
     * @param tag dispatch tag
     * @return true when at least one handle was cancelled
     */
    @Override
    public boolean cancel(final Object tag) {
        require(tag, "Tag");
        boolean cancelled = false;
        for (final DispatchHandle handle : handles()) {
            if (tag.equals(handle.tag())) {
                cancelled |= cancelKnown(handle);
            }
        }
        if (cancelled) {
            promoteQueued();
            drainIdleCallbacks();
        }
        return cancelled;
    }

    /**
     * Returns queued activities.
     *
     * @return queued activities
     */
    @Override
    public List<Activity> queued() {
        return activities(queue.queued());
    }

    /**
     * Returns running activities.
     *
     * @return running activities
     */
    @Override
    public List<Activity> running() {
        return activities(queue.running());
    }

    /**
     * Runs a callback once the dispatcher becomes idle.
     *
     * @param callback idle callback
     */
    @Override
    public void idle(final Runnable callback) {
        require(callback, "Idle callback");
        boolean runNow = false;
        synchronized (idleCallbacks) {
            if (idleNow()) {
                runNow = true;
            } else {
                idleCallbacks.add(callback);
            }
        }
        if (runNow) {
            callback.run();
        } else {
            drainIdleCallbacks();
        }
    }

    /**
     * Closes this dispatcher and cancels queued, running, and delayed work.
     */
    @Override
    public void close() {
        if (!scope.closing()) {
            return;
        }
        RuntimeException failure = null;
        for (final DispatchHandle handle : handles()) {
            try {
                cancelKnown(handle);
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        try {
            closeScheduler();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
        try {
            worker.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
        scope.close(this);
        drainIdleCallbacks();
        if (failure != null) {
            throw new InternalException("Unable to close dispatcher", failure);
        }
    }

    /**
     * Adds a handle to the queue and starts work when capacity is available.
     *
     * @param handle dispatch handle
     * @return dispatch handle
     */
    private DispatchHandle enqueue(final DispatchHandle handle) {
        ensureOpen();
        if (!queue.enqueue(handle)) {
            throw new StatefulException("Dispatch handle cannot be queued");
        }
        promoteQueued();
        return handle;
    }

    /**
     * Starts all handles promoted by the queue.
     */
    private void promoteQueued() {
        final List<DispatchHandle> promoted = queue.promote();
        for (final DispatchHandle handle : promoted) {
            try {
                final CompletableFuture<Void> future = worker.execute(handle.activity());
                future.whenComplete((ignored, cause) -> complete(handle, cause));
            } catch (final RuntimeException e) {
                failPromoted(handle, e);
            }
        }
    }

    /**
     * Moves a delayed task into the dispatch queue.
     *
     * @param task delayed task
     */
    private void runDelayed(final DelayedDispatch task) {
        removeDelayed(task);
        if (task.handle.cancelled()) {
            drainIdleCallbacks();
            return;
        }
        try {
            enqueue(task.handle);
        } catch (final RuntimeException e) {
            task.handle.fail(e);
            drainIdleCallbacks();
        }
    }

    /**
     * Cancels a queued, running, or delayed handle known to this dispatcher.
     *
     * @param handle dispatch handle
     * @return true when cancellation changed state
     */
    private boolean cancelKnown(final DispatchHandle handle) {
        boolean cancelled = queue.cancel(handle);
        cancelled |= cancelDelayed(handle);
        return cancelled;
    }

    /**
     * Cancels a delayed handle before it enters the queue.
     *
     * @param handle dispatch handle
     * @return true when delayed work was cancelled
     */
    private boolean cancelDelayed(final DispatchHandle handle) {
        synchronized (delayed) {
            for (int i = Normal._0; i < delayed.size(); i++) {
                final DelayedDispatch task = delayed.get(i);
                if (task.handle == handle) {
                    delayed.remove(i);
                    if (task.future != null) {
                        task.future.cancel(false);
                    }
                    return task.handle.cancel();
                }
            }
        }
        return false;
    }

    /**
     * Removes and cancels a delayed task record.
     *
     * @param task delayed task
     */
    private void cancelDelayed(final DelayedDispatch task) {
        synchronized (delayed) {
            if (delayed.remove(task) && task.future != null) {
                task.future.cancel(false);
            }
        }
    }

    /**
     * Removes a delayed task record.
     *
     * @param task delayed task
     */
    private void removeDelayed(final DelayedDispatch task) {
        synchronized (delayed) {
            delayed.remove(task);
        }
    }

    /**
     * Closes delayed dispatch resources.
     */
    private void closeScheduler() {
        synchronized (delayed) {
            for (final DelayedDispatch task : new ArrayList<>(delayed)) {
                if (task.future != null) {
                    task.future.cancel(false);
                }
                task.handle.cancel();
            }
            delayed.clear();
        }
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(Normal._5, TimeUnit.SECONDS)) {
                throw new StatefulException("Dispatch scheduler did not stop in time");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StatefulException("Interrupted while closing dispatch scheduler", e);
        }
    }

    /**
     * Completes a promoted handle after worker execution.
     *
     * @param handle handle
     * @param cause  failure cause
     */
    private void complete(final DispatchHandle handle, final Throwable cause) {
        try {
            if (!handle.cancelled()) {
                if (cause == null) {
                    handle.complete();
                } else {
                    handle.fail(cause);
                }
            }
        } finally {
            queue.finish(handle);
            promoteQueued();
            drainIdleCallbacks();
        }
    }

    /**
     * Records a worker submission failure.
     *
     * @param handle handle
     * @param cause  failure cause
     */
    private void failPromoted(final DispatchHandle handle, final RuntimeException cause) {
        try {
            handle.fail(cause);
        } finally {
            queue.finish(handle);
            drainIdleCallbacks();
        }
    }

    /**
     * Runs pending idle callbacks when the queue is empty.
     */
    private void drainIdleCallbacks() {
        final List<Runnable> callbacks;
        synchronized (idleCallbacks) {
            if (!idleNow() || idleCallbacks.isEmpty()) {
                return;
            }
            callbacks = List.copyOf(idleCallbacks);
            idleCallbacks.clear();
        }
        for (final Runnable callback : callbacks) {
            callback.run();
        }
    }

    /**
     * Returns all known handles.
     *
     * @return handle snapshot
     */
    private List<DispatchHandle> handles() {
        final List<DispatchHandle> handles = new ArrayList<>();
        handles.addAll(queue.queued());
        handles.addAll(queue.running());
        synchronized (delayed) {
            for (final DelayedDispatch task : delayed) {
                handles.add(task.handle);
            }
        }
        return List.copyOf(handles);
    }

    /**
     * Converts handles to activities.
     *
     * @param handles handles
     * @return immutable activity list
     */
    private static List<Activity> activities(final List<DispatchHandle> handles) {
        return handles.stream().map(DispatchHandle::activity).toList();
    }

    /**
     * Returns true when no queued, running, or delayed work remains.
     *
     * @return true when dispatcher is idle
     */
    private boolean idleNow() {
        synchronized (delayed) {
            if (!delayed.isEmpty()) {
                return false;
            }
        }
        return queue.idle();
    }

    /**
     * Ensures this dispatcher is open.
     */
    private void ensureOpen() {
        if (scope.state() != Status.OPENED) {
            throw new StatefulException("Dispatcher is closed");
        }
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

    /**
     * Delayed dispatch record.
     */
    private static final class DelayedDispatch {

        /**
         * Delayed handle.
         */
        private final DispatchHandle handle;

        /**
         * Scheduled future.
         */
        private ScheduledFuture<?> future;

        /**
         * Creates a delayed dispatch record.
         *
         * @param handle dispatch handle
         */
        private DelayedDispatch(final DispatchHandle handle) {
            this.handle = handle;
        }

    }

}
