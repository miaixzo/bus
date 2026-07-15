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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Builder;

/**
 * Single-threaded AIO worker with lock-free registration and task queues.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioWorker {

    /**
     * Worker name.
     */
    private final String name;

    /**
     * Registered channels.
     */
    private final Queue<AioChannel> channels;

    /**
     * Task queue.
     */
    private final Queue<Runnable> tasks;

    /**
     * Running flag.
     */
    private final AtomicBoolean running;

    /**
     * Closed flag.
     */
    private final AtomicBoolean closed;

    /**
     * Worker thread.
     */
    private volatile Thread thread;

    /**
     * Creates a worker.
     *
     * @param name worker name
     */
    AioWorker(final String name) {
        this.name = Assert.notBlank(name, () -> new ValidateException("AIO worker name must not be blank"));
        this.channels = new ConcurrentLinkedQueue<>();
        this.tasks = new ConcurrentLinkedQueue<>();
        this.running = new AtomicBoolean();
        this.closed = new AtomicBoolean();
    }

    /**
     * Starts the worker loop.
     */
    public void start() {
        if (closed.get()) {
            throw new StatefulException("AIO worker is closed");
        }
        if (running.compareAndSet(false, true)) {
            try {
                final Thread created = ThreadKit.newThread(this::loop, name, true);
                thread = created;
                created.start();
            } catch (final RuntimeException e) {
                running.set(false);
                throw new InternalException("Unable to start AIO worker", e);
            }
        }
    }

    /**
     * Registers a channel.
     *
     * @param channel channel
     */
    public void register(final AioChannel channel) {
        final AioChannel checkedChannel = Assert
                .notNull(channel, () -> new ValidateException("AIO channel must not be null"));
        if (closed.get()) {
            throw new StatefulException("AIO worker is closed");
        }
        channels.add(checkedChannel);
        wakeup();
    }

    /**
     * Executes a task on the worker.
     *
     * @param task task
     */
    public void execute(final Runnable task) {
        final Runnable checkedTask = Assert.notNull(task, () -> new ValidateException("AIO task must not be null"));
        if (closed.get()) {
            throw new StatefulException("AIO worker is closed");
        }
        tasks.add(checkedTask);
        wakeup();
    }

    /**
     * Wakes the worker loop.
     */
    public void wakeup() {
        final Thread current = thread;
        if (current != null) {
            LockSupport.unpark(current);
        }
    }

    /**
     * Shuts down this worker.
     */
    public void shutdown() {
        if (closed.compareAndSet(false, true)) {
            running.set(false);
            wakeup();
            final Thread current = thread;
            if (current != null && current != Thread.currentThread()) {
                try {
                    current.join(Builder.AIO_WORKER_SHUTDOWN_WAIT_MILLIS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InternalException("Interrupted while stopping AIO worker", e);
                }
            }
            closeChannels();
        }
    }

    /**
     * Returns whether this worker is running.
     *
     * @return true when running
     */
    public boolean running() {
        return running.get() && !closed.get();
    }

    /**
     * Runs the worker loop.
     */
    private void loop() {
        while (running.get() || !tasks.isEmpty()) {
            drainTasks();
            LockSupport.parkNanos(Builder.AIO_WORKER_IDLE_PARK_NANOS);
        }
    }

    /**
     * Drains queued tasks.
     */
    private void drainTasks() {
        Runnable task = tasks.poll();
        while (task != null) {
            try {
                task.run();
            } catch (final RuntimeException ignored) {
                // Worker tasks are isolated so the event loop can continue.
            }
            task = tasks.poll();
        }
    }

    /**
     * Closes registered channels.
     */
    private void closeChannels() {
        AioChannel channel = channels.poll();
        while (channel != null) {
            channel.close();
            channel = channels.poll();
        }
    }

}
