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

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Supplier;

import org.miaixz.bus.core.xyz.RetryKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Publisher;
import org.miaixz.bus.tempus.temporal.worker.CachingWorkflowClientProvider;
import org.miaixz.bus.tempus.temporal.worker.WorkflowClientProvider;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsFactory;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsSpec;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;

/**
 * Manages workflow publication lifecycle for Temporal workflows.
 * <p>
 * This manager creates an untyped workflow stub using the pre-configured binding, starts the workflow execution, and
 * returns the Temporal run identifier. The workflow configuration is provided during instantiation rather than per-call
 * to align with the subscriber pattern.
 * <p>
 * This class implements {@link AutoCloseable} to support try-with-resources for proper resource cleanup.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WorkflowPublisherManager implements Publisher {

    /**
     * Factory used to create workflow options for each publication.
     */
    private final WorkflowOptionsFactory factory;

    /**
     * Provider used to obtain workflow clients for target endpoints.
     */
    private final WorkflowClientProvider provider;

    /**
     * Pre-configured workflow publication binding.
     */
    private final WorkflowPublisherBinding binding;

    /**
     * Creates a workflow publisher manager with the specified configuration.
     *
     * @param provider the workflow client provider
     * @param factory  the workflow options factory
     * @param binding  the workflow publication binding
     * @throws IllegalArgumentException if {@code binding} is {@code null} or required binding properties are missing
     */
    public WorkflowPublisherManager(WorkflowClientProvider provider, WorkflowOptionsFactory factory,
            WorkflowPublisherBinding binding) {
        if (binding == null) {
            throw new IllegalArgumentException("binding must not be null");
        }
        if (binding.getEndpoint() == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }
        if (binding.getTaskQueue() == null) {
            throw new IllegalArgumentException("temporal.task.queue must not be null");
        }
        if (binding.getWorkflowType() == null) {
            throw new IllegalArgumentException("temporal.workflow.type must not be null");
        }

        this.provider = provider;
        this.factory = factory;
        this.binding = binding;
    }

    /**
     * Publishes a workflow execution with the specified arguments.
     * <p>
     * On transient connection errors, the cached client is invalidated and the publish is retried once.
     *
     * @param args the workflow arguments
     * @return the Temporal run identifier
     */
    @Override
    public String publish(Object... args) {
        Logger.debug(
                "Publishing workflow, type: {}, endpoint: {}, queue: {}, args: {}",
                binding.getWorkflowType(),
                binding.getEndpoint(),
                binding.getTaskQueue(),
                args != null ? args.length : 0);

        Supplier<String> recover = () -> {
            throw new RuntimeException("Publish failed after retry, type: " + binding.getWorkflowType());
        };
        return RetryKit.ofPredicate(() -> doPublish(args), 1, Duration.ZERO, recover, (result, ex) -> {
            if (ex != null && isConnectionError(ex)) {
                Logger.warn(
                        "Connection error detected, invalidating cached client and retrying. type: {}, endpoint: {}, error: {}",
                        binding.getWorkflowType(),
                        binding.getEndpoint(),
                        ex.getMessage());
                if (provider instanceof CachingWorkflowClientProvider c) {
                    c.invalidate(binding.getEndpoint());
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Executes a single publish attempt.
     *
     * @param args the workflow arguments
     * @return the Temporal run identifier
     */
    private String doPublish(Object[] args) {
        try {
            WorkflowClient client = provider.createWorkflowClient(binding);
            Logger.debug("Created workflow client for endpoint: {}", binding.getEndpoint());

            WorkflowStub workflow = client.newUntypedWorkflowStub(
                    binding.getWorkflowType(),
                    factory.createWorkflowOptions(
                            WorkflowOptionsSpec.of(binding.getTaskQueue(), binding.getWorkflowType())));
            Logger.debug("Created workflow stub for type: {}", binding.getWorkflowType());

            Object execution = workflow.start(args);

            String workflowId = extractString(execution, "getWorkflowId");
            String runId = extractString(execution, "getRunId");

            Logger.info(
                    "Published workflow successfully, type: {}, workflowId: {}, runId: {}",
                    binding.getWorkflowType(),
                    workflowId,
                    runId);
            return runId;
        } catch (Exception e) {
            Logger.error(
                    "Failed to publish workflow, type: {}, endpoint: {}, queue: {}, error: {}",
                    binding.getWorkflowType(),
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Returns {@code true} if the exception indicates a transient connection error.
     *
     * @param ex the throwable to inspect
     * @return {@code true} if the error is connection-related
     */
    private boolean isConnectionError(Throwable ex) {
        String msg = ex.getMessage();
        if (msg != null
                && (msg.contains("UNAVAILABLE") || msg.contains("DEADLINE_EXCEEDED") || msg.contains("CANCELLED"))) {
            return true;
        }
        return ex.getCause() instanceof java.io.IOException;
    }

    private String extractString(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            return (String) m.invoke(obj);
        } catch (Exception e) {
            Logger.warn("Failed to extract {} from {}: {}", methodName, obj.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

}
