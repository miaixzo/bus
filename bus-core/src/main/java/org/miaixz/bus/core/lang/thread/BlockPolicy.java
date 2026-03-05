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
package org.miaixz.bus.core.lang.thread;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * A {@link RejectedExecutionHandler} that blocks the calling thread until the task can be added to the queue. If the
 * blocking process is interrupted, an {@link InterruptedException} will be thrown. This policy is useful when a fixed
 * number of concurrent accesses to a third-party interface are desired within the thread pool, and tasks should not be
 * discarded when the queue is full (e.g., in database synchronization scenarios). Other built-in rejection policies can
 * be found in the {@link RejectPolicy} enumeration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BlockPolicy implements RejectedExecutionHandler {

    /**
     * Handler for tasks that are rejected when the thread pool is shut down, to prevent task loss. If the task needs to
     * be executed by the caller, you can use {@code new BlockPolicy(Runnable::run)}.
     */
    private final Consumer<Runnable> handlerwhenshutdown;

    /**
     * Constructs a new {@code BlockPolicy} with no specific handler for tasks rejected during shutdown.
     */
    public BlockPolicy() {
        this(null);
    }

    /**
     * Constructs a new {@code BlockPolicy} with a specified handler for tasks rejected during shutdown.
     *
     * @param handlerwhenshutdown The handler to execute rejected tasks when the thread pool is shut down.
     */
    public BlockPolicy(final Consumer<Runnable> handlerwhenshutdown) {
        this.handlerwhenshutdown = handlerwhenshutdown;
    }

    /**
     * Handles the rejected execution of a given runnable task. If the thread pool is not shut down, the task is put
     * into the queue, blocking if necessary. If the thread pool is shut down and a {@code handlerwhenshutdown} is
     * provided, it will be used to handle the task. Otherwise, the task is discarded if the thread pool is shut down
     * and no handler is specified.
     *
     * @param r The runnable task to be executed.
     * @param e The thread pool executor.
     * @throws RejectedExecutionException if the task is rejected due to an {@link InterruptedException} during queue
     *                                    insertion.
     */
    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            try {
                e.getQueue().put(r);
            } catch (final InterruptedException ex) {
                throw new RejectedExecutionException("Task " + r + " rejected from " + e);
            }
        } else if (null != handlerwhenshutdown) {
            handlerwhenshutdown.accept(r);
        }
    }

}
