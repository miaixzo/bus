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
     * Returns a workflow client for the specified Temporal configuration.
     * <p>
     * This is the primary entry point because a binding can carry namespace and identity information in addition to the
     * endpoint itself.
     *
     * @param binding temporal configuration
     * @return the workflow client
     */
    WorkflowClient createWorkflowClient(Binding binding);

}
