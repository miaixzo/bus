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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

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
        return result;
    }

    /**
     * Enqueues a short activity using its name as cancellation tag.
     *
     * @param key      dispatch key
     * @param activity activity
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
     * @param activity activity
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
     * @param activity activity
     * @return dispatch handle
     */
    default DispatchHandle background(final String key, final Object tag, final Activity activity) {
        return enqueue(key, tag, activity);
    }

    /**
     * Schedules a short activity after a delay.
     *
     * @param key      dispatch key
     * @param delay    delay
     * @param activity activity
     * @return dispatch handle
     */
    default DispatchHandle schedule(final String key, final Duration delay, final Activity activity) {
        return enqueue(key, activity);
    }

    /**
     * Cancels one known handle.
     *
     * @param handle handle
     * @return true when cancellation changed work state
     */
    boolean cancel(DispatchHandle handle);

    /**
     * Cancels all known handles matching a tag.
     *
     * @param tag tag
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
     * @param callback callback
     */
    void idle(Runnable callback);

    /**
     * Closes all dispatcher channels.
     */
    @Override
    void close();

}

/**
 * Default dispatcher with one registry shared by short, delayed, and background channels.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class DefaultDispatcher implements Dispatcher {

    /**
     * Maximum background activities that have not terminated.
     */
    private static final int MAX_BACKGROUND = 1_024;

    /**
     * Maximum resource shutdown wait in nanoseconds.
     */
    private static final long CLOSE_WAIT_NANOS = TimeUnit.SECONDS.toNanos(Normal._5);

    /**
     * Short-task queue.
     */
    private final DispatchQueue queue;

    /**
     * Short-task worker.
     */
    private final DispatchWorker worker;

    /**
     * Delayed-task scheduler.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Long-running virtual-thread executor.
     */
    private final ExecutorService backgroundExecutor;

    /**
     * Safe event observer.
     */
    private final EventObserver observer;

    /**
     * Dispatcher lifecycle.
     */
    private final LifecycleScope scope;

    /**
     * Submission and close serialization lock.
     */
    private final Object submissionLock;

    /**
     * Idle callbacks awaiting an empty registry.
     */
    private final List<Runnable> idleCallbacks;

    /**
     * Delayed task records.
     */
    private final List<DelayedDispatch> delayed;

    /**
     * Background task records retained until native termination.
     */
    private final Map<DispatchHandle, BackgroundDispatch> background;

    /**
     * Authoritative registry shared by every dispatcher channel.
     */
    private final Map<DispatchHandle, Channel> registry;

    /**
     * Creates a production dispatcher.
     *
     * @param observer observer
     * @return dispatcher
     */
    static DefaultDispatcher create(final EventObserver observer) {
        return new DefaultDispatcher(new DispatchQueue(DispatchLimit.defaults()), DispatchWorker.create(),
                ThreadKit.newScheduledExecutor(Normal._1), createBackgroundExecutor(), observer);
    }

    /**
     * Creates a compatibility dispatcher with owned background resources.
     *
     * @param queue     queue
     * @param worker    worker
     * @param scheduler scheduler
     */
    DefaultDispatcher(final DispatchQueue queue, final DispatchWorker worker,
            final ScheduledExecutorService scheduler) {
        this(queue, worker, scheduler, createBackgroundExecutor(), EventObserver.noop());
    }

    /**
     * Creates a fully specified dispatcher.
     *
     * @param queue              queue
     * @param worker             worker
     * @param scheduler          scheduler
     * @param backgroundExecutor background executor
     * @param observer           observer
     */
    DefaultDispatcher(final DispatchQueue queue, final DispatchWorker worker, final ScheduledExecutorService scheduler,
            final ExecutorService backgroundExecutor, final EventObserver observer) {
        this.queue = require(queue, "Dispatch queue");
        this.worker = require(worker, "Dispatch worker");
        this.scheduler = require(scheduler, "Dispatch scheduler");
        this.backgroundExecutor = require(backgroundExecutor, "Background executor");
        this.observer = EventObserver.safe(observer);
        this.scope = LifecycleScope.resource(this, "dispatcher", null, this.observer);
        this.submissionLock = new Object();
        this.idleCallbacks = new ArrayList<>();
        this.delayed = new ArrayList<>();
        this.background = new LinkedHashMap<>();
        this.registry = new LinkedHashMap<>();
        this.scope.open(this);
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    public Status state() {
        return scope.state();
    }

    /**
     * Runs one short runnable.
     *
     * @param key      key
     * @param runnable runnable
     * @return completion future
     */
    @Override
    public CompletableFuture<Void> run(final String key, final Runnable runnable) {
        final Runnable current = require(runnable, "Runnable");
        return enqueue(key, Activity.of(key, current)).future();
    }

    /**
     * Runs one short supplier.
     *
     * @param key      key
     * @param supplier supplier
     * @param <T>      result type
     * @return result future
     */
    @Override
    public <T> CompletableFuture<T> supply(final String key, final Supplier<T> supplier) {
        final Supplier<T> current = require(supplier, "Supplier");
        final CompletableFuture<T> result = new CompletableFuture<>();
        final DispatchHandle handle = enqueue(key, Activity.of(key, () -> result.complete(current.get())));
        handle.future().whenComplete((ignored, cause) -> {
            if (handle.cancelled()) {
                result.cancel(false);
            } else if (cause != null && !result.isDone()) {
                result.completeExceptionally(unwrap(cause));
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
     * Runs one framework-owned blocking supplier on the background channel.
     */
    @Override
    public <T> CompletableFuture<T> backgroundSupply(final String key, final Object tag, final Supplier<T> supplier) {
        final Supplier<T> current = require(supplier, "Background supplier");
        final CompletableFuture<T> result = new CompletableFuture<>();
        final DispatchHandle handle = background(key, tag, Activity.of(key, () -> result.complete(current.get())));
        handle.future().whenComplete((ignored, cause) -> {
            if (handle.cancelled()) {
                result.cancel(false);
            } else if (cause != null && !result.isDone()) {
                result.completeExceptionally(unwrap(cause));
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
     * Enqueues a short activity with its name as tag.
     *
     * @param key      key
     * @param activity activity
     * @return handle
     */
    @Override
    public DispatchHandle enqueue(final String key, final Activity activity) {
        final Activity current = require(activity, "Dispatch activity");
        return enqueue(key, current.name(), current);
    }

    /**
     * Enqueues a tagged short activity.
     *
     * @param key      key
     * @param tag      tag
     * @param activity activity
     * @return handle
     */
    @Override
    public DispatchHandle enqueue(final String key, final Object tag, final Activity activity) {
        final Activity current = require(activity, "Dispatch activity");
        final DispatchHandle handle = DispatchHandle.of(key, require(tag, "Tag"), current);
        synchronized (submissionLock) {
            ensureOpen();
            register(handle, Channel.SHORT);
            if (!queue.enqueue(tag, this, current, current.cancellation(), handle)) {
                handle.cancel();
                throw new StatefulException("Dispatch handle cannot be queued");
            }
        }
        promoteQueued();
        return handle;
    }

    /**
     * Starts a tagged background activity.
     *
     * @param key      key
     * @param tag      tag
     * @param activity activity
     * @return handle
     */
    @Override
    public DispatchHandle background(final String key, final Object tag, final Activity activity) {
        final Activity current = require(activity, "Background activity");
        final DispatchHandle handle = DispatchHandle.of(key, require(tag, "Tag"), current);
        final BackgroundDispatch task = new BackgroundDispatch(handle);
        synchronized (submissionLock) {
            ensureOpen();
            synchronized (background) {
                if (background.size() >= MAX_BACKGROUND) {
                    throw new StatefulException("Background dispatch limit is 1024");
                }
                register(handle, Channel.BACKGROUND);
                background.put(handle, task);
            }
            try {
                task.future = backgroundExecutor.submit(() -> runBackground(task));
            } catch (final RuntimeException | Error e) {
                task.terminated.set(true);
                failQueued(handle, e);
                releaseBackground(task);
                throw e;
            }
        }
        return handle;
    }

    /**
     * Schedules a short activity.
     *
     * @param key      key
     * @param delay    delay
     * @param activity activity
     * @return handle
     */
    @Override
    public DispatchHandle schedule(final String key, final Duration delay, final Activity activity) {
        final Duration currentDelay = require(delay, "Dispatch delay");
        final Activity current = require(activity, "Dispatch activity");
        Assert.isFalse(currentDelay.isNegative(), () -> new ValidateException("Dispatch delay must not be negative"));
        if (currentDelay.isZero()) {
            return enqueue(key, current);
        }
        final DispatchHandle handle = DispatchHandle.of(key, current.name(), current);
        final DelayedDispatch task = new DelayedDispatch(handle);
        synchronized (submissionLock) {
            ensureOpen();
            register(handle, Channel.DELAYED);
            synchronized (delayed) {
                delayed.add(task);
            }
            try {
                task.future = scheduler.schedule(() -> runDelayed(task), currentDelay.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final RuntimeException | Error e) {
                removeDelayed(task, true);
                failQueued(handle, e);
                throw e;
            }
        }
        return handle;
    }

    /**
     * Cancels one known handle.
     *
     * @param handle handle
     * @return true when changed
     */
    @Override
    public boolean cancel(final DispatchHandle handle) {
        final boolean changed = cancelKnown(require(handle, "Dispatch handle"));
        if (changed) {
            promoteQueued();
            drainIdleCallbacks();
        }
        return changed;
    }

    /**
     * Cancels every known handle matching a tag.
     *
     * @param tag tag
     * @return true when changed
     */
    @Override
    public boolean cancel(final Object tag) {
        final Object current = require(tag, "Tag");
        boolean changed = false;
        for (final DispatchHandle handle : handles()) {
            if (current.equals(handle.tag())) {
                changed |= cancelKnown(handle);
            }
        }
        if (changed) {
            promoteQueued();
            drainIdleCallbacks();
        }
        return changed;
    }

    /**
     * Returns queued activities across every channel.
     *
     * @return queued activities
     */
    @Override
    public List<Activity> queued() {
        return activities(Status.QUEUED);
    }

    /**
     * Returns running activities across every channel.
     *
     * @return running activities
     */
    @Override
    public List<Activity> running() {
        return activities(Status.RUNNING);
    }

    /**
     * Registers an isolated idle callback.
     *
     * @param callback callback
     */
    @Override
    public void idle(final Runnable callback) {
        final Runnable current = require(callback, "Idle callback");
        boolean now;
        synchronized (idleCallbacks) {
            now = idleNow();
            if (!now) {
                idleCallbacks.add(current);
            }
        }
        if (now) {
            runIdleCallback(current);
        } else {
            drainIdleCallbacks();
        }
    }

    /**
     * Closes channels in submission, cancellation, scheduler, short-worker, and background order.
     */
    @Override
    public void close() {
        synchronized (submissionLock) {
            if (!scope.closing()) {
                return;
            }
        }
        RuntimeException failure = null;
        for (final DispatchHandle handle : handles()) {
            try {
                cancelKnown(handle);
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        queue.close();
        failure = closeScheduler(failure);
        try {
            worker.close();
        } catch (final RuntimeException e) {
            failure = append(failure, e);
        }
        failure = closeBackground(failure);
        synchronized (registry) {
            registry.clear();
        }
        scope.close(this);
        drainIdleCallbacks();
        if (failure != null) {
            throw new InternalException("Unable to close dispatcher", failure);
        }
    }

    /**
     * Registers one handle and its terminal cleanup callback.
     *
     * @param handle  handle
     * @param channel channel
     */
    private void register(final DispatchHandle handle, final Channel channel) {
        synchronized (registry) {
            if (registry.putIfAbsent(handle, channel) != null) {
                throw new StatefulException("Dispatch handle is already registered");
            }
        }
        handle.future().whenComplete((ignored, cause) -> terminal(handle));
    }

    /**
     * Promotes all short tasks currently allowed by queue limits.
     */
    private void promoteQueued() {
        final List<DispatchQueue.Entry> entries = queue.promoteEntries();
        if (entries.size() > 1) {
            worker.executeBatch(entries);
            return;
        }
        for (final DispatchQueue.Entry entry : entries) {
            try {
                worker.execute(entry);
            } catch (final RuntimeException | Error ignored) {
                queue.finish(entry.handle());
            }
        }
    }

    /**
     * Moves a delayed record into the short-task channel.
     *
     * @param task delayed task
     */
    private void runDelayed(final DelayedDispatch task) {
        synchronized (submissionLock) {
            removeDelayed(task, false);
            synchronized (registry) {
                if (registry.get(task.handle) != Channel.DELAYED || scope.state() != Status.OPENED) {
                    task.handle.cancel();
                    return;
                }
                registry.put(task.handle, Channel.SHORT);
            }
            final Activity activity = task.handle.activity();
            if (!queue.enqueue(task.handle.tag(), this, activity, activity.cancellation(), task.handle)) {
                task.handle.cancel();
                return;
            }
        }
        promoteQueued();
    }

    /**
     * Runs one background activity with the same handle state machine as short tasks.
     *
     * @param task background task
     */
    private void runBackground(final BackgroundDispatch task) {
        task.started.set(true);
        final DispatchHandle handle = task.handle;
        try {
            if (!handle.markRunning()) {
                return;
            }
            handle.activity().run();
            if (handle.activity().state() == Status.CANCELLED || handle.activity().cancellation().cancelled()) {
                handle.cancel();
            } else if (handle.state() == Status.RUNNING) {
                handle.complete();
            }
        } catch (final Throwable cause) {
            final Throwable recorded = handle.activity().failure();
            final Throwable original = recorded == null ? cause : recorded;
            if (handle.state() == Status.CANCELLED || handle.activity().state() == Status.CANCELLED
                    || original instanceof CancellationException) {
                handle.cancel();
            } else {
                failRunning(handle, original);
            }
        } finally {
            task.terminated.set(true);
            releaseBackground(task);
        }
    }

    /**
     * Cancels a handle according to its registered channel.
     *
     * @param handle handle
     * @return true when state or native work changed
     */
    private boolean cancelKnown(final DispatchHandle handle) {
        final Channel channel;
        synchronized (registry) {
            channel = registry.get(handle);
        }
        if (channel == null) {
            return false;
        }
        return switch (channel) {
            case SHORT -> queue.cancel(handle);
            case DELAYED -> cancelDelayed(handle);
            case BACKGROUND -> cancelBackground(handle);
        };
    }

    /**
     * Cancels one delayed record before its handle.
     *
     * @param handle handle
     * @return true when changed
     */
    private boolean cancelDelayed(final DispatchHandle handle) {
        DelayedDispatch found = null;
        synchronized (delayed) {
            for (final DelayedDispatch task : delayed) {
                if (task.handle == handle) {
                    found = task;
                    break;
                }
            }
            if (found != null) {
                delayed.remove(found);
            }
        }
        if (found != null && found.future != null) {
            found.future.cancel(false);
        }
        return handle.cancel();
    }

    /**
     * Cancels one background handle and interrupts its native future.
     *
     * @param handle handle
     * @return true when changed
     */
    private boolean cancelBackground(final DispatchHandle handle) {
        final BackgroundDispatch task;
        synchronized (background) {
            task = background.get(handle);
        }
        final boolean changed = handle.cancel();
        boolean nativeChanged = false;
        if (task != null && task.future != null) {
            nativeChanged = task.future.cancel(true);
            if (nativeChanged && !task.started.get()) {
                task.terminated.set(true);
                releaseBackground(task);
            }
        }
        return changed || nativeChanged;
    }

    /**
     * Handles a canonical future terminal event.
     *
     * @param handle handle
     */
    private void terminal(final DispatchHandle handle) {
        final Channel channel;
        synchronized (registry) {
            channel = registry.get(handle);
            if (channel == null) {
                return;
            }
            if (channel != Channel.BACKGROUND) {
                registry.remove(handle);
            }
        }
        if (channel == Channel.SHORT) {
            queue.cancel(handle);
            promoteQueued();
        } else if (channel == Channel.DELAYED) {
            removeDelayed(handle);
        } else {
            final BackgroundDispatch task;
            synchronized (background) {
                task = background.get(handle);
            }
            if (task != null && task.terminated.get()) {
                releaseBackground(task);
            }
        }
        drainIdleCallbacks();
    }

    /**
     * Releases one terminated background record and common registration.
     *
     * @param task task
     */
    private void releaseBackground(final BackgroundDispatch task) {
        if (!task.terminated.get()) {
            return;
        }
        synchronized (background) {
            background.remove(task.handle, task);
        }
        synchronized (registry) {
            registry.remove(task.handle, Channel.BACKGROUND);
        }
        drainIdleCallbacks();
    }

    /**
     * Removes one delayed record and optionally cancels its native future.
     *
     * @param task         task
     * @param cancelFuture true to cancel native scheduling
     */
    private void removeDelayed(final DelayedDispatch task, final boolean cancelFuture) {
        synchronized (delayed) {
            delayed.remove(task);
        }
        if (cancelFuture && task.future != null) {
            task.future.cancel(false);
        }
    }

    /**
     * Removes one delayed record by handle.
     *
     * @param handle handle
     */
    private void removeDelayed(final DispatchHandle handle) {
        synchronized (delayed) {
            delayed.removeIf(task -> {
                if (task.handle != handle) {
                    return false;
                }
                if (task.future != null) {
                    task.future.cancel(false);
                }
                return true;
            });
        }
    }

    /**
     * Fails a queued handle through the required running transition.
     *
     * @param handle handle
     * @param cause  original failure
     */
    private static void failQueued(final DispatchHandle handle, final Throwable cause) {
        if (handle.markRunning()) {
            handle.fail(cause);
        }
    }

    /**
     * Fails a running handle while tolerating a concurrent terminal winner.
     *
     * @param handle handle
     * @param cause  original failure
     */
    private static void failRunning(final DispatchHandle handle, final Throwable cause) {
        if (handle.state() != Status.RUNNING) {
            return;
        }
        try {
            handle.fail(cause);
        } catch (final StatefulException ignored) {
            // Another terminal transition won.
        }
    }

    /**
     * Closes the delayed scheduler with ThreadKit-based waiting.
     *
     * @param failure current failure
     * @return aggregated failure
     */
    private RuntimeException closeScheduler(final RuntimeException failure) {
        RuntimeException current = failure;
        scheduler.shutdownNow();
        try {
            awaitTermination(scheduler, "Dispatch scheduler");
        } catch (final RuntimeException e) {
            current = append(current, e);
        }
        synchronized (delayed) {
            delayed.clear();
        }
        return current;
    }

    /**
     * Closes the background executor after cancelling all retained native futures.
     *
     * @param failure current failure
     * @return aggregated failure
     */
    private RuntimeException closeBackground(final RuntimeException failure) {
        RuntimeException current = failure;
        synchronized (background) {
            for (final BackgroundDispatch task : new ArrayList<>(background.values())) {
                if (task.future != null) {
                    task.future.cancel(true);
                }
            }
        }
        backgroundExecutor.shutdownNow();
        try {
            awaitTermination(backgroundExecutor, "Background executor");
        } catch (final RuntimeException e) {
            current = append(current, e);
        }
        synchronized (background) {
            background.clear();
        }
        return current;
    }

    /**
     * Waits for one executor without direct thread sleeping.
     *
     * @param executor executor
     * @param name     resource name
     */
    private static void awaitTermination(final ExecutorService executor, final String name) {
        final long start = System.nanoTime();
        while (!executor.isTerminated()) {
            if (System.nanoTime() - start >= CLOSE_WAIT_NANOS) {
                throw new StatefulException(name + " did not stop in time");
            }
            if (!ThreadKit.sleep(Normal._1)) {
                throw new StatefulException("Interrupted while closing " + name.toLowerCase());
            }
        }
    }

    /**
     * Drains idle callbacks after all channel registrations disappear.
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
            runIdleCallback(callback);
        }
    }

    /**
     * Runs one idle callback with observer-backed failure isolation.
     *
     * @param callback callback
     */
    private void runIdleCallback(final Runnable callback) {
        try {
            callback.run();
        } catch (final RuntimeException | Error e) {
            scope.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Returns all common registry handles.
     *
     * @return handle snapshot
     */
    private List<DispatchHandle> handles() {
        synchronized (registry) {
            return List.copyOf(registry.keySet());
        }
    }

    /**
     * Returns activities in one handle state.
     *
     * @param state state
     * @return activities
     */
    private List<Activity> activities(final Status state) {
        final List<Activity> activities = new ArrayList<>();
        for (final DispatchHandle handle : handles()) {
            if (handle.state() == state) {
                activities.add(handle.activity());
            }
        }
        return List.copyOf(activities);
    }

    /**
     * Returns whether all dispatcher channels are idle.
     *
     * @return true when idle
     */
    private boolean idleNow() {
        synchronized (registry) {
            return registry.isEmpty();
        }
    }

    /**
     * Ensures submissions are still accepted.
     */
    private void ensureOpen() {
        if (scope.state() != Status.OPENED) {
            throw new StatefulException("Dispatcher is closed");
        }
    }

    /**
     * Creates the dedicated virtual-thread background executor.
     *
     * @return executor
     */
    private static ExecutorService createBackgroundExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("fabric-background-", Normal._0).factory());
    }

    /**
     * Unwraps one completion wrapper.
     *
     * @param cause cause
     * @return original cause
     */
    private static Throwable unwrap(final Throwable cause) {
        return cause instanceof java.util.concurrent.CompletionException completion && completion.getCause() != null
                ? completion.getCause()
                : cause;
    }

    /**
     * Aggregates cleanup failures.
     *
     * @param failure current failure
     * @param next    next failure
     * @return primary failure
     */
    private static RuntimeException append(final RuntimeException failure, final RuntimeException next) {
        if (failure == null) {
            return next;
        }
        if (failure != next) {
            failure.addSuppressed(next);
        }
        return failure;
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
     * Dispatcher channel kind stored in the common registry.
     */
    private enum Channel {

        /**
         * Short queue and worker.
         */
        SHORT,

        /**
         * Delayed scheduler.
         */
        DELAYED,

        /**
         * Long-running virtual thread.
         */
        BACKGROUND

    }

    /**
     * Delayed native resource record.
     */
    private static final class DelayedDispatch {

        /**
         * Dispatch handle.
         */
        private final DispatchHandle handle;

        /**
         * Native scheduled future.
         */
        private volatile ScheduledFuture<?> future;

        /**
         * Creates a delayed record.
         *
         * @param handle handle
         */
        private DelayedDispatch(final DispatchHandle handle) {
            this.handle = handle;
        }

    }

    /**
     * Background native resource record retained until its virtual thread exits.
     */
    private static final class BackgroundDispatch {

        /**
         * Dispatch handle.
         */
        private final DispatchHandle handle;

        /**
         * Native start flag.
         */
        private final AtomicBoolean started;

        /**
         * Native termination flag.
         */
        private final AtomicBoolean terminated;

        /**
         * Native future.
         */
        private volatile Future<?> future;

        /**
         * Creates a background record.
         *
         * @param handle handle
         */
        private BackgroundDispatch(final DispatchHandle handle) {
            this.handle = handle;
            this.started = new AtomicBoolean();
            this.terminated = new AtomicBoolean();
        }

    }

}
