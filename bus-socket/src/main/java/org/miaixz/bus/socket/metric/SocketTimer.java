/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.metric;

import java.util.concurrent.TimeUnit;

/**
 * Interface for a timer that schedules tasks.
 * <p>
 * This interface defines methods for scheduling one-time tasks and tasks with a fixed delay.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface SocketTimer {

    /**
     * Schedules a one-time task to be executed after the specified delay.
     *
     * @param runnable the task to execute
     * @param delay    the delay before the task is executed
     * @param unit     the time unit of the delay parameter
     * @return a {@link SocketTask} representing the scheduled task
     */
    SocketTask schedule(final Runnable runnable, final long delay, final TimeUnit unit);

    /**
     * Shuts down the timer, preventing new tasks from being scheduled and attempting to stop currently running tasks.
     */
    void shutdown();

    /**
     * Schedules a task to be executed repeatedly with a fixed delay between the termination of one execution and the
     * commencement of the next.
     *
     * @param runnable the task to execute
     * @param delay    the delay between executions
     * @param unit     the time unit of the delay parameter
     * @return a {@link SocketTask} representing the scheduled task
     */
    SocketTask scheduleWithFixedDelay(Runnable runnable, long delay, TimeUnit unit);

}
