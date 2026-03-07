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

import java.lang.reflect.Method;

import org.miaixz.bus.logger.Logger;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;

/**
 * Default implementation of {@link WorkflowPublisher}.
 * <p>
 * This publisher validates the publication definition, creates an untyped workflow stub, starts the workflow execution,
 * and returns the Temporal run identifier.
 */
public class DefaultWorkflowPublisher implements WorkflowPublisher {

    /**
     * Provider used to obtain workflow clients for target endpoints.
     */
    private final WorkflowClientProvider workflowClientProvider;

    /**
     * Factory used to create workflow options for each publication.
     */
    private final WorkflowOptionsFactory workflowOptionsFactory;

    /**
     * Creates a workflow publisher.
     *
     * @param workflowClientProvider the workflow client provider
     * @param workflowOptionsFactory the workflow options factory
     */
    public DefaultWorkflowPublisher(WorkflowClientProvider workflowClientProvider,
            WorkflowOptionsFactory workflowOptionsFactory) {
        this.workflowClientProvider = workflowClientProvider;
        this.workflowOptionsFactory = workflowOptionsFactory;
    }

    /**
     * Publishes a workflow execution using the specified definition and arguments.
     *
     * @param definition the workflow publication definition
     * @param args       the workflow arguments
     * @return the Temporal run identifier
     * @throws IllegalArgumentException if the definition is {@code null} or required definition properties are missing
     */
    @Override
    public String publish(WorkflowPublisherDefinition definition, Object... args) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        if (definition.getEndpoint() == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }
        if (definition.getTaskQueue() == null) {
            throw new IllegalArgumentException("temporal.task.queue must not be null");
        }
        if (definition.getWorkflowType() == null) {
            throw new IllegalArgumentException("temporal.workflow.type must not be null");
        }

        try {
            WorkflowClient client = workflowClientProvider.createWorkflowClient(definition.getEndpoint());
            WorkflowStub workflow = client.newUntypedWorkflowStub(
                    definition.getWorkflowType(),
                    workflowOptionsFactory.createWorkflowOptions(definition));
            Object execution = workflow.start(args);

            String workflowId = extractWorkflowId(execution);
            String runId = extractRunId(execution);

            Logger.info(
                    "[DefaultWorkflowPublisher] Published workflow successfully, type: {}, workflowId: {}, runId: {}",
                    definition.getWorkflowType(),
                    workflowId,
                    runId);
            return runId;
        } catch (Exception e) {
            Logger.error(
                    "[DefaultWorkflowPublisher] Failed to publish workflow, type: {}, endpoint: {}, queue: {}, error: {}",
                    definition.getWorkflowType(),
                    definition.getEndpoint(),
                    definition.getTaskQueue(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Extracts the workflow ID from a WorkflowExecution object using reflection.
     *
     * @param execution the workflow execution object
     * @return the workflow ID
     */
    private String extractWorkflowId(Object execution) {
        try {
            Method method = execution.getClass().getMethod("getWorkflowId");
            return (String) method.invoke(execution);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract workflow ID from execution", e);
        }
    }

    /**
     * Extracts the run ID from a WorkflowExecution object using reflection.
     *
     * @param execution the workflow execution object
     * @return the run ID
     */
    private String extractRunId(Object execution) {
        try {
            Method method = execution.getClass().getMethod("getRunId");
            return (String) method.invoke(execution);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract run ID from execution", e);
        }
    }

}
