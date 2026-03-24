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
package org.miaixz.bus.tempus.temporal.workflow;

import io.temporal.client.WorkflowOptions;

/**
 * Creates {@link WorkflowOptions} for workflow publications.
 * <p>
 * This abstraction allows applications to centralize timeout, task queue, and workflow identifier policies for
 * published workflows. The factory accepts basic workflow parameters that can be provided by both publishers and
 * subscribers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WorkflowOptionsFactory {

    /**
     * Creates workflow options for the specified workflow parameters.
     *
     * @param taskQueue    the task queue name
     * @param workflowType the workflow type name
     * @return the workflow options
     */
    WorkflowOptions createWorkflowOptions(String taskQueue, String workflowType);

    /**
     * Creates workflow options for the specified specification.
     * <p>
     * This overload exists to avoid signature churn as additional workflow fields become relevant.
     *
     * @param spec the workflow options specification
     * @return the workflow options
     */
    default WorkflowOptions createWorkflowOptions(WorkflowOptionsSpec spec) {
        return createWorkflowOptions(spec.taskQueue(), spec.workflowType());
    }

}
