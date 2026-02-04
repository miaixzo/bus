/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
