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
package org.miaixz.bus.cron.tempus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * An interface for executing Temporal workflows, providing the basic contract for interacting with a Temporal server.
 * <p>
 * Implementations of this interface are responsible for the concrete communication logic with the Temporal server,
 * including starting workflows and passing arguments.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Temporal {

    /**
     * Executes a Temporal workflow task.
     * <p>
     * This method initiates a workflow task via Temporal's gRPC, supporting both HTTP and HTTPS protocols, and returns
     * the workflow's Run ID. The workflow arguments are encapsulated in a {@link Message} object and converted to JSON
     * format for transmission.
     * </p>
     *
     * @param endpoint The endpoint address of the Temporal server's gRPC gateway (e.g.,
     *                 "https://temporal.example.com:8080"). This must be a non-null and valid address.
     * @param queue    The name of the task queue to which the workflow will be dispatched. This must match the task
     *                 queue that the target Worker is listening on (e.g., "cron-queue"). Must be a non-empty string.
     * @param type     The name of the workflow type to be executed. This corresponds to the workflow implementation
     *                 registered with the Worker (e.g., "org.example.Workflow"). Must be a non-empty string.
     * @param args     The arguments for the workflow execution. This can be any serializable Java object (POJO, Map,
     *                 etc.) and will be converted to JSON format. It is typically wrapped using
     *                 {@code Message.builder().data(args).build()}. Must not be null.
     * @return The unique Run ID of the workflow execution, which can be used to query the workflow's status, history,
     *         or result. Returns a non-empty string on successful start.
     * @throws IllegalArgumentException if any of the required parameters are null or invalid.
     * @throws RuntimeException         if the workflow fails to start for any reason (e.g., network issues, server
     *                                  errors).
     */
    String execute(String endpoint, String queue, String type, Object args);

    /**
     * A wrapper class for workflow execution arguments.
     * <p>
     * This class encapsulates the parameters required for a workflow execution and is serializable for network
     * transmission. It is designed to be created using its builder.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class Message implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852290719686L;

        /**
         * The workflow execution arguments.
         * <p>
         * This can be any serializable Java object (POJO, Map, etc.) and will be converted to JSON format before being
         * passed to the workflow.
         * </p>
         */
        public Object data;
    }

}
