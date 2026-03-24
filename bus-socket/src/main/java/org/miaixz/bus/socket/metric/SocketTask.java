/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
package org.miaixz.bus.socket.metric;

/**
 * Represents a task that can be scheduled and managed by a timer. This interface provides methods to check the status
 * of the task and to cancel it.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SocketTask {

    /**
     * Checks if the scheduled task has completed its execution.
     *
     * @return {@code true} if the task has completed, {@code false} otherwise
     */
    boolean isDone();

    /**
     * Checks if the scheduled task has been cancelled.
     *
     * @return {@code true} if the task has been cancelled, {@code false} otherwise
     */
    boolean isCancelled();

    /**
     * Cancels the scheduled task. After calling this method, the task should not be executed.
     */
    void cancel();

}
