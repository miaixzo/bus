/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Enumeration of thread rejection policies. These policies define how a {@link ThreadPoolExecutor} handles tasks that
 * cannot be executed, typically when the thread pool's capacity (including its work queue) is exhausted. The policies
 * correspond to the predefined strategies in {@link ThreadPoolExecutor} and a custom {@link BlockPolicy}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum RejectPolicy {

    /**
     * A handler for rejected tasks that throws a {@code RejectedExecutionException}.
     */
    ABORT(new ThreadPoolExecutor.AbortPolicy()),
    /**
     * A handler for rejected tasks that silently discards the rejected task.
     */
    DISCARD(new ThreadPoolExecutor.DiscardPolicy()),
    /**
     * A handler for rejected tasks that discards the oldest unexecuted task in the work queue and then retries to
     * submit the new task. If the executor is shut down, the new task is discarded.
     */
    DISCARD_OLDEST(new ThreadPoolExecutor.DiscardOldestPolicy()),
    /**
     * A handler for rejected tasks that runs the rejected task directly in the calling thread of the {@code execute}
     * method. This provides a simple feedback mechanism that slows down the submission of new tasks.
     */
    CALLER_RUNS(new ThreadPoolExecutor.CallerRunsPolicy()),
    /**
     * A handler for rejected tasks that blocks the calling thread until the task can be added to the queue. This policy
     * is suitable when a fixed number of concurrent accesses is desired and tasks should not be discarded.
     */
    BLOCK(new BlockPolicy());

    /**
     * The {@link RejectedExecutionHandler} instance associated with this policy.
     */
    private final RejectedExecutionHandler value;

    /**
     * Constructs a {@code RejectPolicy} enum constant with the specified {@link RejectedExecutionHandler}.
     *
     * @param handler The {@link RejectedExecutionHandler} instance.
     */
    RejectPolicy(final RejectedExecutionHandler handler) {
        this.value = handler;
    }

    /**
     * Retrieves the {@link RejectedExecutionHandler} instance associated with this policy.
     *
     * @return The {@link RejectedExecutionHandler} instance.
     */
    public RejectedExecutionHandler getValue() {
        return this.value;
    }

}
