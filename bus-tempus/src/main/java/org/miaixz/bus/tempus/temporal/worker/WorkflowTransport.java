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
 * Handles Temporal workflow transport operations for configured endpoints.
 * <p>
 * Implementations are responsible for translating opaque transport handles into clients, health probes, transport
 * states, and shutdown behavior.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WorkflowTransport {

    /**
     * Creates a transport handle from Temporal binding configuration.
     * <p>
     * This is the main transport creation entry point. The binding may carry transport metadata beyond endpoint
     * settings.
     *
     * @param binding Temporal binding configuration
     * @return opaque transport handle
     */
    Object create(Binding binding);

    /**
     * Creates a workflow client from a transport handle and Temporal binding configuration.
     *
     * @param handle  opaque transport handle
     * @param binding Temporal binding configuration
     * @return workflow client
     */
    WorkflowClient client(Object handle, Binding binding);

    /**
     * Creates a workflow client from a transport handle.
     * <p>
     * This lower-level path does not carry binding-level client options and is only used when the caller has already
     * prepared a transport handle.
     *
     * @param handle opaque transport handle
     * @return workflow client
     */
    WorkflowClient client(Object handle);

    /**
     * Gets the current transport state.
     *
     * @param handle opaque transport handle
     * @return current transport state
     */
    String state(Object handle);

    /**
     * Probes the Temporal service with a lightweight system information request.
     *
     * @param handle         opaque transport handle
     * @param timeoutSeconds probe timeout in seconds
     * @return {@code true} when the service responds successfully
     */
    boolean health(Object handle, long timeoutSeconds);

    /**
     * Shuts down the specified transport handle.
     *
     * @param handle opaque transport handle
     */
    void shutdown(Object handle);

}
