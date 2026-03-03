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
package org.miaixz.bus.cron.listener;

import org.miaixz.bus.cron.Executor;

/**
 * A simple no-op implementation of {@link TaskListener}. Extend this class and override only the methods you need.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleTaskListener implements TaskListener {

    /**
     * Triggered when a task is about to start.
     * <p>
     * Description inherited from parent interface.
     *
     * @param executor The {@link Executor} for the task that is starting.
     */
    @Override
    public void onStart(final Executor executor) {
        // Do nothing
    }

    /**
     * Triggered when a task has completed successfully.
     * <p>
     * Description inherited from parent interface.
     *
     * @param executor The {@link Executor} for the task that has succeeded.
     */
    @Override
    public void onSucceeded(final Executor executor) {
        // Do nothing
    }

    /**
     * Triggered when a task fails to execute.
     * <p>
     * Description inherited from parent interface.
     *
     * @param executor  The {@link Executor} for the task that has failed.
     * @param exception The exception that caused the failure.
     */
    @Override
    public void onFailed(final Executor executor, final Throwable exception) {
        // Do nothing
    }

}
