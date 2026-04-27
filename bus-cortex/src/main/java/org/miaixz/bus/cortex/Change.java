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
package org.miaixz.bus.cortex;

/**
 * Common metadata contract shared by Cortex change notifications.
 * <p>
 * Implementations such as {@link Watch} and {@link org.miaixz.bus.cortex.registry.RegistryChange} expose their concrete
 * payload shape separately while sharing the same namespace, type, ordering and timestamp metadata.
 *
 * @param <T> domain value type described by the concrete change implementation
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Change<T> {

    /**
     * Returns the namespace affected by this change notification.
     *
     * @return affected namespace
     */
    String getNamespace_id();

    /**
     * Returns the asset type affected by this change notification when known.
     *
     * @return affected asset type, or {@code null} when the change spans multiple types
     */
    Type getType();

    /**
     * Returns the ordering sequence assigned to this change.
     *
     * @return change ordering sequence
     */
    long getSequence();

    /**
     * Returns the creation time of this change in epoch milliseconds.
     *
     * @return change creation timestamp
     */
    long getTimestamp();

}
