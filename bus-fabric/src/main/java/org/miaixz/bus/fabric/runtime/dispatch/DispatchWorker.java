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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.runtime.Activity;

/**
 * Executor wrapper for dispatched activities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchWorker implements AutoCloseable {

    /**
     * Executor service.
     */
    private final ExecutorService executor;

    /**
     * Whether this worker owns the executor.
     */
    private final boolean ownsExecutor;

    /**
     * Closed flag.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a worker.
     *
     * @param executor     executor
     * @param ownsExecutor true when close should stop the executor
     */
    private DispatchWorker(final ExecutorService executor, final boolean ownsExecutor) {
        this.executor = require(executor, "Executor");
        this.ownsExecutor = ownsExecutor;
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a default worker.
     *
     * @return dispatch worker
     */
    public static DispatchWorker create() {
        try {
            return new DispatchWorker(ThreadKit.newExecutor(), true);
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to create dispatch worker", e);
        }
    }

    /**
     * Wraps an external executor.
     *
     * @param executor executor
     * @return dispatch worker
     */
    public static DispatchWorker of(final ExecutorService executor) {
        return new DispatchWorker(executor, false);
    }

    /**
     * Executes an activity asynchronously.
     *
     * @param activity activity
     * @return execution future
     */
    public CompletableFuture<Void> execute(final Activity activity) {
        require(activity, "Activity");
        if (closed.get()) {
            throw new StatefulException("Dispatch worker is closed");
        }
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            executor.execute(() -> run(activity, future));
        } catch (final RejectedExecutionException e) {
            throw new StatefulException("Dispatch worker rejected activity", e);
        }
        return future;
    }

    /**
     * Closes owned worker resources.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true) || !ownsExecutor) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            throw new InternalException("Interrupted while closing dispatch worker", e);
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to close dispatch worker", e);
        }
    }

    /**
     * Runs activity and completes the supplied future.
     *
     * @param activity activity
     * @param future   execution future
     */
    private static void run(final Activity activity, final CompletableFuture<Void> future) {
        try {
            activity.run();
            future.complete(null);
        } catch (final RuntimeException e) {
            final Throwable failure = activity.failure();
            future.completeExceptionally(failure == null ? e : failure);
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
