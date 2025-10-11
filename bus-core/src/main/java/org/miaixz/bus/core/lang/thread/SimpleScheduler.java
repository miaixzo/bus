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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.miaixz.bus.core.xyz.RuntimeKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * A simple single-threaded task scheduler. It allows for scheduling tasks that can produce a result which can be
 * retrieved via {@link #getResult()}.
 *
 * @param <T> The type of the result returned by the scheduled job.
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleScheduler<T> {

    /**
     * The job to be scheduled and executed.
     */
    private final Job<T> job;

    /**
     * Constructs a {@code SimpleScheduler} with a specified job and a fixed execution period. The task will start
     * immediately with no initial delay and run in fixed-rate mode.
     *
     * @param job    The {@link Job} to be scheduled.
     * @param period The period between successive executions of the task, in milliseconds.
     */
    public SimpleScheduler(final Job<T> job, final long period) {
        this(job, 0, period, true);
    }

    /**
     * Constructs a {@code SimpleScheduler} with a specified job, initial delay, period, and scheduling mode.
     *
     * @param job                   The {@link Job} to be scheduled.
     * @param initialDelay          The time to delay first execution, in milliseconds.
     * @param period                The period between successive executions, in milliseconds.
     * @param fixedRateOrFixedDelay {@code true} for fixed-rate execution (tasks start at fixed intervals),
     *                              {@code false} for fixed-delay execution (tasks start after the previous one finishes
     *                              plus the delay).
     */
    public SimpleScheduler(final Job<T> job, final long initialDelay, final long period,
            final boolean fixedRateOrFixedDelay) {
        this.job = job;

        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // Start the scheduled task
        ThreadKit.schedule(scheduler, job, initialDelay, period, fixedRateOrFixedDelay);
        // Ensure the scheduled task shuts down when the program ends
        RuntimeKit.addShutdownHook(scheduler::shutdown);
    }

    /**
     * Retrieves the current result of the executed job.
     *
     * @return The result of the job.
     */
    public T getResult() {
        return this.job.getResult();
    }

    /**
     * Represents a scheduled task that can produce a result. Implementations of this interface define the task's logic
     * in the {@link #run()} method and provide a way to retrieve the current result via {@link #getResult()}.
     *
     * @param <T> The type of the result produced by the job.
     */
    public interface Job<T> extends Runnable {

        /**
         * Retrieves the current execution result of the job.
         *
         * @return The execution result.
         */
        T getResult();
    }

}
