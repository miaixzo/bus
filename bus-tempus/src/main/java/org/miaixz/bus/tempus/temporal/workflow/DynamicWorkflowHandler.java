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

import java.util.Objects;
import java.util.function.Function;

import io.temporal.common.converter.EncodedValues;
import io.temporal.workflow.DynamicSignalHandler;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.Workflow;

/**
 * Dynamic workflow handler used by {@link NativeWorkflowAdapter}.
 * <p>
 * The handler is registered with Temporal as a dynamic workflow implementation. At execution time it verifies that the
 * workflow type selected by Temporal matches the expected binding and then delegates the single request argument to the
 * supplied invocation function.
 *
 * @param <R> workflow request type
 * @author Kimi Liu
 * @since Java 21+
 */
public class DynamicWorkflowHandler<R> implements DynamicWorkflow, DynamicSignalHandler {

    /**
     * Expected Temporal workflow type for this handler.
     */
    private final String workflowType;

    /**
     * Java type used to deserialize the first workflow argument.
     */
    private final Class<R> requestType;

    /**
     * Workflow invocation function created inside Temporal workflow context.
     */
    private final Function<R, ?> invocation;

    /**
     * Creates a dynamic workflow handler for one workflow type.
     *
     * @param workflowType expected Temporal workflow type
     * @param requestType  Java request payload type
     * @param invocation   workflow invocation function
     * @throws NullPointerException if {@code invocation} is {@code null}
     */
    DynamicWorkflowHandler(String workflowType, Class<R> requestType, Function<R, ?> invocation) {
        this.workflowType = workflowType;
        this.requestType = requestType;
        this.invocation = Objects.requireNonNull(invocation, "invocation must not be null");
    }

    /**
     * Executes the dynamic workflow request.
     * <p>
     * Temporal passes workflow arguments through {@link EncodedValues}. This method decodes the first argument as the
     * configured request type and returns the invocation result to Temporal.
     *
     * @param args encoded workflow arguments
     * @return workflow execution result
     * @throws IllegalArgumentException if Temporal dispatches a workflow type other than the configured type
     */
    @Override
    public Object execute(EncodedValues args) {
        String currentWorkflowType = Workflow.getInfo().getWorkflowType();
        if (!workflowType.equals(currentWorkflowType)) {
            throw new IllegalArgumentException("Unsupported workflow type: " + currentWorkflowType);
        }
        R request = args.get(0, requestType);
        return invocation.apply(request);
    }

    /**
     * Handles dynamic workflow signals.
     * <p>
     * The current native workflow bridge only supports request/response workflow execution. Signals are rejected
     * explicitly so unsupported usage fails with a clear message.
     *
     * @param signalName Temporal signal name
     * @param args       encoded signal arguments
     * @throws UnsupportedOperationException always, because this handler does not support signals
     */
    @Override
    public void handle(String signalName, EncodedValues args) {
        throw new UnsupportedOperationException("Unsupported workflow signal: " + signalName);
    }

}
