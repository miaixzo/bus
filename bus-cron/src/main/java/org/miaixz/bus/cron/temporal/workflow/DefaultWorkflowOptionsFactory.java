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
package org.miaixz.bus.cron.temporal.workflow;

import java.time.Duration;

import io.temporal.client.WorkflowOptions;

/**
 * Default implementation of {@link WorkflowOptionsFactory}.
 * <p>
 * This factory builds Temporal workflow options from a publication definition and delegates workflow identifier
 * generation to a {@link WorkflowIdGenerator}.
 */
public class DefaultWorkflowOptionsFactory implements WorkflowOptionsFactory {

    /**
     * Strategy used to generate workflow identifiers.
     */
    private final WorkflowIdGenerator workflowIdGenerator;

    /**
     * Creates a workflow options factory.
     *
     * @param workflowIdGenerator the workflow identifier generator
     */
    public DefaultWorkflowOptionsFactory(WorkflowIdGenerator workflowIdGenerator) {
        this.workflowIdGenerator = workflowIdGenerator;
    }

    /**
     * Creates workflow options for the specified publication definition.
     *
     * @param definition the workflow publication definition
     * @return the created workflow options
     */
    @Override
    public WorkflowOptions createWorkflowOptions(WorkflowPublisherDefinition definition) {
        return WorkflowOptions.newBuilder().setTaskQueue(definition.getTaskQueue())
                .setWorkflowId(workflowIdGenerator.generateWorkflowId(definition))
                .setWorkflowExecutionTimeout(Duration.ofDays(1)).setWorkflowRunTimeout(Duration.ofHours(1))
                .setWorkflowTaskTimeout(Duration.ofMinutes(1)).build();
    }

}
