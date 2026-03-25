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
package org.miaixz.bus.tempus.temporal.worker;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Binding;

import io.temporal.client.WorkflowClient;

/**
 * Creates Temporal {@link WorkflowClient} instances for target endpoints.
 * <p>
 * Implementations may create fresh clients, reuse cached clients, or apply custom connection policies for different
 * Temporal clusters.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WorkflowClientProvider {

    /**
     * Returns a workflow client for the specified Temporal server endpoint.
     *
     * @param endpoint the Temporal server endpoint
     * @return the workflow client
     */
    WorkflowClient createWorkflowClient(String endpoint);

    /**
     * Returns a workflow client for the specified Temporal configuration.
     * <p>
     * Default implementation delegates to {@link #createWorkflowClient(String)}.
     *
     * @param binding temporal configuration
     * @return the workflow client
     */
    default WorkflowClient createWorkflowClient(Binding binding) {
        if (binding == null) {
            throw new IllegalArgumentException("binding must not be null");
        }
        if (binding.getEndpoint() == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }

        Logger.debug(
                "Creating workflow client, endpoint: {}, namespace: {}, identity: {}",
                binding.getEndpoint(),
                binding.getNamespace(),
                binding.getIdentity());

        try {
            WorkflowClient client = createWorkflowClient(binding.getEndpoint());
            Logger.debug("Created workflow client successfully, endpoint: {}", binding.getEndpoint());
            return client;
        } catch (Exception e) {
            Logger.error(
                    "Failed to create workflow client, endpoint: {}, namespace: {}, identity: {}, error: {}",
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

}
