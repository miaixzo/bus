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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Enumeration of thread rejection policies. These policies define how a {@link ThreadPoolExecutor} handles tasks that
 * cannot be executed, typically when the thread pool's capacity (including its work queue) is exhausted. The policies
 * correspond to the predefined strategies in {@link ThreadPoolExecutor} and a custom {@link BlockPolicy}.
 *
 * @author Kimi Liu
 * @since Java 21+
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
