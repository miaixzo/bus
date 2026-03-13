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
package org.miaixz.bus.tempus.temporal.workflow.publisher;

import java.time.Duration;

import org.miaixz.bus.tempus.temporal.workflow.WorkflowIdGenerator;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsSpec;

import io.temporal.client.WorkflowOptions;

/**
 * Default implementation of {@link WorkflowOptionsFactory}.
 * <p>
 * This factory builds Temporal workflow options from a publication binding and delegates workflow identifier generation
 * to a {@link WorkflowIdGenerator}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WorkflowPublisherOptionsFactory implements WorkflowOptionsFactory {

    /**
     * Strategy used to generate workflow identifiers.
     */
    private final WorkflowIdGenerator generator;

    /**
     * Creates a workflow options factory.
     *
     * @param generator the workflow identifier generator
     */
    public WorkflowPublisherOptionsFactory(WorkflowIdGenerator generator) {
        this.generator = generator;
    }

    /**
     * Creates workflow options for the specified workflow parameters.
     *
     * @param taskQueue    the task queue name
     * @param workflowType the workflow type name
     * @return the created workflow options
     */
    @Override
    public WorkflowOptions createWorkflowOptions(String taskQueue, String workflowType) {
        return WorkflowOptions.newBuilder().setTaskQueue(taskQueue).setWorkflowId(generator.workflowId(workflowType))
                .setWorkflowExecutionTimeout(Duration.ofDays(1)).setWorkflowRunTimeout(Duration.ofHours(1))
                .setWorkflowTaskTimeout(Duration.ofMinutes(1)).build();
    }

    /**
     * Creates workflow options for the specified workflow options specification.
     * <p>
     * When {@link WorkflowOptionsSpec#workflowId()} is blank, this implementation generates a workflow id using the
     * configured {@link WorkflowIdGenerator} and the optional stable key.
     *
     * @param spec the workflow options specification
     * @return the created workflow options
     */
    @Override
    public WorkflowOptions createWorkflowOptions(WorkflowOptionsSpec spec) {
        String workflowId = spec.workflowId();
        if (workflowId == null || workflowId.isBlank()) {
            workflowId = generator.workflowId(spec.workflowType(), spec.stableKey());
        }

        return WorkflowOptions.newBuilder().setTaskQueue(spec.taskQueue()).setWorkflowId(workflowId)
                .setWorkflowExecutionTimeout(Duration.ofDays(1)).setWorkflowRunTimeout(Duration.ofHours(1))
                .setWorkflowTaskTimeout(Duration.ofMinutes(1)).build();
    }

}
