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

import org.miaixz.bus.core.lang.Assert;

/**
 * Specification used when creating {@link io.temporal.client.WorkflowOptions}.
 * <p>
 * This type exists to avoid signature churn as additional workflow option fields become relevant.
 *
 * @param taskQueue    the task queue name
 * @param workflowType the workflow type name
 * @param workflowId   an optional workflow id to use
 * @param stableKey    an optional stable key used for deterministic workflow id generation
 * @author Kimi Liu
 * @since Java 21+
 */
public record WorkflowOptionsSpec(String taskQueue, String workflowType, String workflowId, String stableKey) {

    /**
     * Validates required fields.
     *
     * @throws IllegalArgumentException if {@code taskQueue} or {@code workflowType} is {@code null}
     */
    public WorkflowOptionsSpec {
        Assert.notNull(taskQueue, "taskQueue must not be null");
        Assert.notNull(workflowType, "workflowType must not be null");
    }

    /**
     * Creates a minimal specification with only the required fields.
     *
     * @param taskQueue    the task queue name
     * @param workflowType the workflow type name
     * @return the workflow options specification
     */
    public static WorkflowOptionsSpec of(String taskQueue, String workflowType) {
        return new WorkflowOptionsSpec(taskQueue, workflowType, null, null);
    }

}
