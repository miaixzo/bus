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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Publisher;
import org.miaixz.bus.tempus.temporal.worker.WorkflowConnector;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowBindingOptions;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsFactory;

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
     * Connector used to obtain workflow clients for target endpoints.
     */
    private final WorkflowConnector connector;

    /**
     * Pre-configured workflow publication binding.
     */
    private final WorkflowPublisherBinding binding;

    /**
     * Unified workflow binding options.
     */
    private final WorkflowBindingOptions options;

    /**
     * Creates a workflow publisher manager with the specified configuration.
     *
     * @param connector the workflow connector
     * @param factory   the workflow options factory
     * @param binding   the workflow publication binding
     * @param options   unified workflow binding options
     * @throws IllegalArgumentException if {@code binding} is {@code null} or required binding properties are missing
     */
    public WorkflowPublisherManager(WorkflowConnector connector, WorkflowOptionsFactory factory,
            WorkflowPublisherBinding binding, WorkflowBindingOptions options) {
        Assert.notNull(binding, "binding must not be null");
        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");
        Assert.notNull(binding.getTaskQueue(), "temporal.task.queue must not be null");
        Assert.notNull(binding.getWorkflowType(), "temporal.workflow.type must not be null");

        this.connector = connector;
        this.factory = factory;
        this.binding = binding;
        this.options = completeOptions(options, binding);
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
        Logger.info(
                true,
                "Tempus",
                "Workflow publication started: workflowType={}, endpoint={}, taskQueue={}, argCount={}",
                binding.getWorkflowType(),
                binding.getEndpoint(),
                binding.getTaskQueue(),
                args != null ? args.length : 0);
        Throwable lastError = null;
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Logger.debug(
                        true,
                        "Tempus",
                        "Workflow publication attempt started: workflowType={}, endpoint={}, attempt={}, maxAttempts={}",
                        binding.getWorkflowType(),
                        binding.getEndpoint(),
                        attempt,
                        maxAttempts);
                String runId = doPublish(args);
                Logger.info(
                        false,
                        "Tempus",
                        "Workflow publication completed: workflowType={}, endpoint={}, attempt={}, runId={}",
                        binding.getWorkflowType(),
                        binding.getEndpoint(),
                        attempt,
                        runId);
                return runId;
            } catch (Exception ex) {
                lastError = ex;
                boolean retryable = attempt < maxAttempts && isConnectionError(ex);
                if (!retryable) {
                    break;
                }
                Logger.warn(
                        false,
                        "Tempus",
                        ex,
                        "Workflow publication retry scheduled: workflowType={}, endpoint={}, attempt={}, exception={}",
                        binding.getWorkflowType(),
                        binding.getEndpoint(),
                        attempt,
                        ex.getClass().getSimpleName());
                connector.invalidate(binding.getEndpoint());
            }
        }
        Logger.error(
                false,
                "Tempus",
                lastError,
                "Workflow publication failed after retry: workflowType={}, endpoint={}, taskQueue={}, maxAttempts={}, exception={}",
                binding.getWorkflowType(),
                binding.getEndpoint(),
                binding.getTaskQueue(),
                maxAttempts,
                lastError == null ? null : lastError.getClass().getSimpleName());
        throw new RuntimeException("Publish failed after retry, type: " + binding.getWorkflowType(), lastError);
    }

    /**
     * Executes a single publish attempt.
     *
     * @param args the workflow arguments
     * @return the Temporal run identifier
     */
    private String doPublish(Object[] args) {
        try {
            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow publish attempt details started: workflowType={}, endpoint={}, taskQueue={}, argCount={}",
                    binding.getWorkflowType(),
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    args == null ? 0 : args.length);
            WorkflowClient client = connector.client(binding);
            Logger.debug(false, "Tempus", "Workflow client created: endpoint={}", binding.getEndpoint());

            WorkflowStub workflow = client
                    .newUntypedWorkflowStub(binding.getWorkflowType(), factory.createWorkflowOptions(options));
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow stub created: workflowType={}, taskQueue={}",
                    binding.getWorkflowType(),
                    binding.getTaskQueue());

            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow start request dispatched: workflowType={}, taskQueue={}, argCount={}",
                    binding.getWorkflowType(),
                    binding.getTaskQueue(),
                    args == null ? 0 : args.length);
            Object execution = workflow.start(args);

            String workflowId = extractString(execution, "getWorkflowId");
            String runId = extractString(execution, "getRunId");
            Assert.notNull(
                    runId,
                    "Failed to resolve Temporal runId after workflow start, type: %s, workflowId: %s",
                    binding.getWorkflowType(),
                    workflowId);

            Logger.info(
                    false,
                    "Tempus",
                    "Workflow start response received: workflowType={}, workflowId={}, runId={}",
                    binding.getWorkflowType(),
                    workflowId,
                    runId);
            return runId;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow publish attempt failed: workflowType={}, endpoint={}, taskQueue={}, exception={}",
                    binding.getWorkflowType(),
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    e.getClass().getSimpleName());
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
        Throwable current = ex;
        while (current != null) {
            String grpcStatusCode = extractGrpcStatusCode(current);
            if ("UNAVAILABLE".equals(grpcStatusCode) || "DEADLINE_EXCEEDED".equals(grpcStatusCode)
                    || "CANCELLED".equals(grpcStatusCode)) {
                return true;
            }
            String msg = current.getMessage();
            if (StringKit.hasText(msg)
                    && (msg.contains("UNAVAILABLE") || msg.contains("DEADLINE_EXCEEDED") || msg.contains("CANCELLED")
                            || msg.contains("connection reset") || msg.contains("connection refused"))) {
                return true;
            }
            if (current instanceof java.io.IOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Extracts the gRPC status code from a throwable without introducing a direct gRPC compile-time dependency.
     *
     * @param throwable the throwable to inspect
     * @return the gRPC status code name, or {@code null} when it cannot be resolved
     */
    private String extractGrpcStatusCode(Throwable throwable) {
        Class<?> throwableType = throwable.getClass();
        Package throwablePackage = throwableType.getPackage();
        String packageName = throwablePackage == null ? null : throwablePackage.getName();
        if (!"StatusRuntimeException".equals(throwableType.getSimpleName()) || !("io" + ".grpc").equals(packageName)) {
            return null;
        }
        try {
            Object status = MethodKit.invoke(throwable, "getStatus");
            if (status == null) {
                return null;
            }
            Object code = MethodKit.invoke(status, "getCode");
            return code == null ? null : String.valueOf(code);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Extracts a string value from a Temporal SDK object by invoking a no-argument accessor method.
     *
     * @param obj        the source object
     * @param methodName the accessor method name
     * @return the extracted string value, or {@code null} when extraction fails
     */
    private String extractString(Object obj, String methodName) {
        try {
            return MethodKit.invoke(obj, methodName);
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Workflow execution field extraction failed: method={}, sourceType={}, exception={}",
                    methodName,
                    obj == null ? null : obj.getClass().getSimpleName(),
                    e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Completes workflow binding options with publisher binding defaults.
     *
     * @param source  source workflow binding options
     * @param binding workflow binding
     * @return completed workflow binding options
     */
    private static WorkflowBindingOptions completeOptions(
            WorkflowBindingOptions source,
            WorkflowPublisherBinding binding) {
        WorkflowBindingOptions target = source == null ? WorkflowBindingOptions.defaults() : source;
        if (!StringKit.hasText(target.getTaskQueue())) {
            target.setTaskQueue(binding.getTaskQueue());
        }
        if (!StringKit.hasText(target.getWorkflowType())) {
            target.setWorkflowType(binding.getWorkflowType());
        }
        return target;
    }

}
