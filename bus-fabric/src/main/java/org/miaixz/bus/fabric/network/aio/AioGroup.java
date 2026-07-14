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
package org.miaixz.bus.fabric.network.aio;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * AIO channel group and worker registry.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioGroup {

    /**
     * Maximum read worker count.
     */
    private static final int MAX_READ_WORKERS = Normal._256;

    /**
     * JDK channel group.
     */
    final AsynchronousChannelGroup channelGroup;

    /**
     * Read workers.
     */
    private final AioWorker[] readWorkers;

    /**
     * Write worker.
     */
    private final AioWorker write;

    /**
     * Common worker.
     */
    private final AioWorker common;

    /**
     * Runtime dispatcher for blocking AIO future waits.
     */
    private final Dispatcher dispatcher;

    /**
     * Whether this group owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Next read index.
     */
    private final AtomicInteger nextRead;

    /**
     * Started flag.
     */
    private final AtomicBoolean started;

    /**
     * Shutdown flag.
     */
    private final AtomicBoolean shutdown;

    /**
     * Creates a group.
     *
     * @param channelGroup   JDK group
     * @param readWorkers    read workers
     * @param write          write worker
     * @param common         common worker
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when shutdown closes dispatcher
     */
    private AioGroup(final AsynchronousChannelGroup channelGroup, final AioWorker[] readWorkers, final AioWorker write,
            final AioWorker common, final Dispatcher dispatcher, final boolean ownsDispatcher) {
        this.channelGroup = Assert
                .notNull(channelGroup, () -> new ValidateException("AIO channel group must not be null"));
        this.readWorkers = Assert.notNull(readWorkers, () -> new ValidateException("Read workers must not be null"));
        this.write = Assert.notNull(write, () -> new ValidateException("Write worker must not be null"));
        this.common = Assert.notNull(common, () -> new ValidateException("Common worker must not be null"));
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        this.nextRead = new AtomicInteger();
        this.started = new AtomicBoolean();
        this.shutdown = new AtomicBoolean();
    }

    /**
     * Creates a worker group.
     *
     * @param readWorkers read worker count
     * @return group
     */
    public static AioGroup create(final int readWorkers) {
        return create(readWorkers, Dispatcher.create(), true);
    }

    /**
     * Creates a worker group with a shared dispatcher.
     *
     * @param readWorkers read worker count
     * @param dispatcher  shared dispatcher
     * @return group
     */
    public static AioGroup create(final int readWorkers, final Dispatcher dispatcher) {
        return create(readWorkers, dispatcher, false);
    }

    /**
     * Creates a worker group.
     *
     * @param readWorkers    read worker count
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when group owns dispatcher lifecycle
     * @return group
     */
    private static AioGroup create(final int readWorkers, final Dispatcher dispatcher, final boolean ownsDispatcher) {
        final int checkedReadWorkers = Assert.checkBetween(
                readWorkers,
                Normal._1,
                MAX_READ_WORKERS,
                () -> new ValidateException("AIO read worker count out of range"));
        final Dispatcher checkedDispatcher = Assert
                .notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null"));
        try {
            final AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    checkedReadWorkers,
                    task -> ThreadKit.newThread(task, "fabric-aio-channel", true));
            final AioWorker[] reads = new AioWorker[checkedReadWorkers];
            for (int i = Normal._0; i < checkedReadWorkers; i++) {
                reads[i] = new AioWorker("fabric-aio-read-" + i);
            }
            return new AioGroup(channelGroup, reads, new AioWorker("fabric-aio-write"),
                    new AioWorker("fabric-aio-common"), checkedDispatcher, ownsDispatcher);
        } catch (final IOException | RuntimeException e) {
            throw new InternalException("Unable to create AIO group", e);
        }
    }

    /**
     * Returns the next read worker.
     *
     * @return read worker
     */
    public AioWorker nextRead() {
        final int index = Math.floorMod(nextRead.getAndIncrement(), readWorkers.length);
        return readWorkers[index];
    }

    /**
     * Returns configured read worker count.
     *
     * @return read worker count
     */
    public int readWorkerCount() {
        return readWorkers.length;
    }

    /**
     * Returns the write worker.
     *
     * @return write worker
     */
    public AioWorker write() {
        return write;
    }

    /**
     * Returns the common worker.
     *
     * @return common worker
     */
    public AioWorker common() {
        return common;
    }

    /**
     * Returns the runtime dispatcher.
     *
     * @return dispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * Starts all workers.
     */
    public void start() {
        if (started.compareAndSet(false, true)) {
            for (final AioWorker worker : readWorkers) {
                worker.start();
            }
            write.start();
            common.start();
        }
    }

    /**
     * Shuts down all workers.
     */
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            for (final AioWorker worker : readWorkers) {
                worker.shutdown();
            }
            write.shutdown();
            common.shutdown();
            try {
                channelGroup.shutdownNow();
            } catch (final IOException e) {
                throw new InternalException("Unable to shut down AIO channel group", e);
            }
            if (ownsDispatcher) {
                dispatcher.close();
            }
        }
    }

    /**
     * Waits for worker termination.
     *
     * @param timeout timeout
     * @return true when terminated
     */
    public boolean awaitTermination(final Duration timeout) {
        final Duration checkedTimeout = Assert
                .notNull(timeout, () -> new ValidateException("Termination timeout must be non-null and non-negative"));
        Assert.isTrue(
                !checkedTimeout.isNegative(),
                () -> new ValidateException("Termination timeout must be non-null and non-negative"));
        try {
            final boolean groupTerminated = channelGroup
                    .awaitTermination(checkedTimeout.toNanos(), TimeUnit.NANOSECONDS);
            return groupTerminated && stopped();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while awaiting AIO termination", e);
        }
    }

    /**
     * Returns whether workers are stopped.
     *
     * @return true when stopped
     */
    private boolean stopped() {
        for (final AioWorker worker : readWorkers) {
            if (worker.running()) {
                return false;
            }
        }
        return !write.running() && !common.running();
    }

}
