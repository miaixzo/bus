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
package org.miaixz.bus.tempus.temporal;

/**
 * Defines the top-level contract for publishing Temporal workflows.
 * <p>
 * This interface represents the framework entry point for starting workflow executions from application code.
 * Implementations are responsible for creating workflow clients, configuring workflow options, and initiating workflow
 * executions on the Temporal server. Configuration such as endpoint, task queue, and workflow type should be provided
 * during publisher instantiation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Publisher extends AutoCloseable {

    /**
     * Publishes a workflow execution with the specified arguments.
     *
     * @param args the workflow arguments
     * @return the Temporal run identifier
     */
    String publish(Object... args);

    /**
     * Closes this publisher and releases any resources.
     * <p>
     * Default implementations should remain no-ops because the interface does not own concrete resources.
     */
    @Override
    default void close() {

    }

}
