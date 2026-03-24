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
package org.miaixz.bus.tempus.temporal.workflow.subscriber;

import io.temporal.worker.Worker;
import org.miaixz.bus.tempus.temporal.Binding;

/**
 * Describes how a Temporal worker subscriber should be created and configured.
 * <p>
 * Implementations provide connection settings, concurrency limits, and the workflow and activity registrations required
 * for a worker instance.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WorkflowSubscriberBinding extends Binding {

    /**
     * Returns whether the worker should be started.
     *
     * @return {@code true} if the worker is enabled; {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Returns the maximum concurrency used for workflow tasks and activities.
     *
     * @return the maximum concurrency
     */
    int getMaxConcurrent();

    /**
     * Registers workflow implementations and activity instances with the worker.
     *
     * @param worker the Temporal worker to configure
     */
    void registerWorkflowsAndActivities(Worker worker);

}
