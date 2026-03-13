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
 * Base configuration interface for Temporal components.
 * <p>
 * This interface defines common connection and routing properties shared by both publishers and subscribers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Binding {

    /**
     * Returns the Temporal server endpoint.
     *
     * @return the Temporal server endpoint
     */
    String getEndpoint();

    /**
     * Returns the task queue name.
     *
     * @return the task queue name
     */
    String getTaskQueue();

    /**
     * Returns the Temporal namespace.
     *
     * @return the namespace, or {@code null} to use Temporal defaults
     */
    default String getNamespace() {
        return null;
    }

    /**
     * Returns the client identity used for Temporal connections.
     *
     * @return the identity, or {@code null} to use Temporal defaults
     */
    default String getIdentity() {
        return null;
    }

}
