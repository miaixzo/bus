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
package org.miaixz.bus.tempus.listener;

import org.miaixz.bus.tempus.Executor;

/**
 * An interface for listening to cron task execution events. By implementing this interface, you can monitor various
 * stages of a task's lifecycle.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface TaskListener {

    /**
     * Triggered when a task is about to start.
     *
     * @param executor The {@link Executor} for the task that is starting.
     */
    void onStart(Executor executor);

    /**
     * Triggered when a task has completed successfully.
     *
     * @param executor The {@link Executor} for the task that has succeeded.
     */
    void onSucceeded(Executor executor);

    /**
     * Triggered when a task fails to execute.
     *
     * @param executor  The {@link Executor} for the task that has failed.
     * @param exception The exception that caused the failure.
     */
    void onFailed(Executor executor, Throwable exception);

}
