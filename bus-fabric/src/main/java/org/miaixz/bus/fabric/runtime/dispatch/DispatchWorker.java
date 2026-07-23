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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.Activity;

/**
 * Worker executor that accepts only short-task entries promoted by {@link DispatchQueue}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchWorker implements AutoCloseable {

    /**
     * Maximum entries accepted by one dispatcher-to-worker batch.
     */
    private static final int MAX_BATCH = 64;

    /**
     * Executor used for asynchronous activity execution.
     */
    private final ExecutorService executor;

    /**
     * Whether this worker owns executor shutdown.
     */
    private final boolean ownsExecutor;

    /**
     * One-way flag that prevents further submissions after closure begins.
     */
    private final AtomicBoolean closed;

    /**
     * Queue entries and compatibility activities submitted to the executor but not yet terminated.
     */
    private final Set<Object> active;

    /**
     * Creates a worker backed by the supplied executor.
     *
     * @param executor     executor used for activity submissions
     * @param ownsExecutor whether closing the worker also closes the executor
     */
    private DispatchWorker(final ExecutorService executor, final boolean ownsExecutor) {
        this.executor = require(executor, "Executor");
        this.ownsExecutor = ownsExecutor;
        this.closed = new AtomicBoolean();
        this.active = ConcurrentHashMap.newKeySet();
    }

    /**
     * Creates a worker that owns a virtual-thread-per-task executor.
     *
     * @return worker backed by a newly created virtual-thread executor
     * @throws InternalException if the executor cannot be created
     */
    public static DispatchWorker create() {
        try {
            return new DispatchWorker(
                    Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("fabric-dispatch-", 0L).factory()),
                    true);
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to create dispatch worker", e);
        }
    }

    /**
     * Creates a worker that uses, but does not own, an external executor.
     *
     * @param executor executor used for activity submissions
     * @return worker backed by the supplied executor
     * @throws ValidateException if {@code executor} is {@code null}
     */
    public static DispatchWorker of(final ExecutorService executor) {
        return new DispatchWorker(executor, false);
    }

    /**
     * Executes one short-task entry asynchronously through its authoritative handle.
     *
     * @param entry reserved queue entry to submit
     * @return authoritative handle future, including when the handle could not transition to running
     * @throws StatefulException          if the worker is closed
     * @throws RejectedExecutionException if the executor rejects the submission
     */
    CompletableFuture<Void> execute(final DispatchQueue.Entry entry) {
        final DispatchQueue.Entry current = require(entry, "Dispatch entry");
        final DispatchHandle handle = current.handle();
        if (closed.get()) {
            handle.cancel();
            throw new StatefulException("Dispatch worker is closed");
        }
        if (!handle.markRunning()) {
            return handle.future();
        }
        active.add(current);
        if (closed.get()) {
            active.remove(current);
            handle.cancel();
            return handle.future();
        }
        try {
            executor.execute(() -> run(current));
        } catch (final RejectedExecutionException e) {
            active.remove(current);
            fail(handle, e);
            throw e;
        } catch (final RuntimeException | Error e) {
            active.remove(current);
            fail(handle, e);
            throw e;
        }
        return handle.future();
    }

    /**
     * Executes one compatibility activity without acquiring queue permits or creating a dispatch handle.
     *
     * @param activity object that must be an {@link Activity}
     * @return future completed from the activity's terminal outcome
     * @throws ValidateException if {@code activity} is not an {@link Activity}
     * @throws StatefulException if the worker is closed or the executor rejects the activity
     */
    public CompletableFuture<Void> execute(final Object activity) {
        if (!(activity instanceof Activity current)) {
            throw new ValidateException("Dispatch activity must not be null");
        }
        if (closed.get()) {
            current.cancel();
            throw new StatefulException("Dispatch worker is closed");
        }
        final CompletableFuture<Void> future = new CompletableFuture<>();
        active.add(current);
        try {
            executor.execute(() -> {
                try {
                    current.run();
                    if (current.cancelled()) {
                        future.cancel(false);
                    } else {
                        future.complete(null);
                    }
                } catch (final Throwable cause) {
                    final Throwable failure = current.failure();
                    future.completeExceptionally(failure == null ? cause : failure);
                } finally {
                    active.remove(current);
                }
            });
        } catch (final RejectedExecutionException failure) {
            active.remove(current);
            future.completeExceptionally(failure);
            throw new StatefulException("Dispatch executor rejected activity", failure);
        }
        return future;
    }

    /**
     * Submits one bounded promotion batch while preserving independent executor submissions and failure isolation.
     *
     * @param entries reserved entries to validate, transition to running, and submit
     * @throws ValidateException if the list is {@code null}, contains a {@code null} entry, or exceeds the batch limit
     * @throws StatefulException if the worker is closed
     */
    void executeBatch(final List<DispatchQueue.Entry> entries) {
        final List<DispatchQueue.Entry> current = require(entries, "Dispatch entries");
        if (current.size() > MAX_BATCH) {
            throw new ValidateException("Dispatch batch must not exceed " + MAX_BATCH);
        }
        if (closed.get()) {
            for (final DispatchQueue.Entry entry : current) {
                entry.handle().cancel();
            }
            throw new StatefulException("Dispatch worker is closed");
        }
        final ArrayList<DispatchQueue.Entry> accepted = new ArrayList<>(current.size());
        for (final DispatchQueue.Entry entry : current) {
            final DispatchQueue.Entry checked = require(entry, "Dispatch entry");
            if (checked.handle().markRunning()) {
                active.add(checked);
                accepted.add(checked);
            }
        }
        if (accepted.isEmpty()) {
            return;
        }
        try {
            executor.execute(() -> runBatch(accepted));
        } catch (final RuntimeException | Error e) {
            for (final DispatchQueue.Entry entry : accepted) {
                active.remove(entry);
                fail(entry.handle(), e);
            }
            throw e;
        }
    }

    /**
     * Executes one bounded batch while retaining per-entry terminal isolation.
     *
     * @param entries entries whose handles have transitioned to running
     */
    private void runBatch(final List<DispatchQueue.Entry> entries) {
        for (final DispatchQueue.Entry entry : entries) {
            run(entry);
        }
    }

    /**
     * Prevents new submissions, cancels active work, and shuts down the executor when this worker owns it.
     *
     * @throws StatefulException if the owned executor does not terminate before the shutdown deadline
     * @throws InternalException if shutdown waiting is interrupted
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (final Object task : List.copyOf(active)) {
            if (task instanceof DispatchQueue.Entry entry) {
                entry.handle().cancel();
            } else if (task instanceof Activity activity) {
                activity.cancel();
            }
        }
        if (!ownsExecutor) {
            active.clear();
            return;
        }
        executor.shutdownNow();
        final long deadline = System.nanoTime() + Normal._5 * 1_000_000_000L;
        while (!executor.isTerminated()) {
            if (System.nanoTime() >= deadline) {
                throw new StatefulException("Dispatch worker did not stop in time");
            }
            if (!ThreadKit.sleep(Normal._1)) {
                throw new InternalException("Interrupted while closing dispatch worker");
            }
        }
        active.clear();
    }

    /**
     * Runs one promoted entry and records exactly one handle terminal state.
     *
     * @param entry submitted entry whose handle is expected to be running
     */
    private void run(final DispatchQueue.Entry entry) {
        final DispatchHandle handle = entry.handle();
        try {
            if (handle.state() != Status.RUNNING) {
                return;
            }
            entry.activity().run();
            if (entry.cancellation().cancelled()) {
                handle.cancel();
            } else if (handle.state() == Status.RUNNING) {
                handle.complete();
            }
        } catch (final Throwable cause) {
            final Throwable failure = entry.activity().failure();
            final Throwable original = failure == null ? cause : failure;
            if (handle.state() == Status.CANCELLED || entry.activity().state() == Status.CANCELLED
                    || original instanceof CancellationException) {
                handle.cancel();
            } else {
                fail(handle, original);
            }
        } finally {
            active.remove(entry);
        }
    }

    /**
     * Fails a running handle while tolerating a concurrent terminal winner.
     *
     * @param handle running handle to fail
     * @param cause  failure reported by the activity or executor
     */
    private static void fail(final DispatchHandle handle, final Throwable cause) {
        if (handle.state() != Status.RUNNING) {
            return;
        }
        try {
            handle.fail(cause);
        } catch (final StatefulException ignored) {
            // A concurrent cancellation or completion already owns the terminal state.
        }
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
