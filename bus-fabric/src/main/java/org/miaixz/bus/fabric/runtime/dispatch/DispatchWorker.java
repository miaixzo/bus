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
     * Executor service.
     */
    private final ExecutorService executor;

    /**
     * Whether this worker owns executor shutdown.
     */
    private final boolean ownsExecutor;

    /**
     * Closed flag.
     */
    private final AtomicBoolean closed;

    /**
     * Entries submitted to the executor and not yet terminated.
     */
    private final Set<Object> active;

    /**
     * Creates a worker.
     *
     * @param executor owned executor
     */
    private DispatchWorker(final ExecutorService executor, final boolean ownsExecutor) {
        this.executor = require(executor, "Executor");
        this.ownsExecutor = ownsExecutor;
        this.closed = new AtomicBoolean();
        this.active = ConcurrentHashMap.newKeySet();
    }

    /**
     * Creates a default worker.
     *
     * @return dispatch worker
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
     * Takes ownership of an external platform executor.
     *
     * @param executor executor
     * @return dispatch worker
     */
    public static DispatchWorker of(final ExecutorService executor) {
        return new DispatchWorker(executor, false);
    }

    /**
     * Executes one short-task entry asynchronously through its authoritative handle.
     *
     * @param entry queue entry
     * @return the handle future
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
     * Executes one compatibility activity without acquiring queue permits.
     *
     * @param activity activity
     * @return completion future
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
     * @param entries promoted entries
     */
    void executeBatch(final List<DispatchQueue.Entry> entries) {
        final List<DispatchQueue.Entry> current = require(entries, "Dispatch entries");
        if (current.size() > MAX_BATCH) {
            throw new ValidateException("Dispatch batch must not exceed " + MAX_BATCH);
        }
        for (final DispatchQueue.Entry entry : current) {
            try {
                execute(entry);
            } catch (final RuntimeException ignored) {
                // The entry handle already owns the isolated terminal failure; continue the batch.
            }
        }
    }

    /**
     * Closes owned worker resources.
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
     * @param entry promoted entry
     */
    private void run(final DispatchQueue.Entry entry) {
        final DispatchHandle handle = entry.handle();
        try {
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
     * @param handle handle
     * @param cause  original failure
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
