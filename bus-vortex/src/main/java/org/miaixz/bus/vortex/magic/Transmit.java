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
package org.miaixz.bus.vortex.magic;

import org.miaixz.bus.core.lang.EnumValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A data transfer object (DTO) that provides a consolidated view of a single managed service, including its status and
 * performance metrics.
 * <p>
 * This object is typically constructed by the {@link org.miaixz.bus.vortex.registry.ServerRegistry} and returned by
 * management APIs to be consumed by a UI dashboard.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Data
@Builder
@AllArgsConstructor
public class Transmit {

    /**
     * Creates an empty service transmission snapshot.
     */
    public Transmit() {
    }

    /**
     * The unique name or ID of the service, derived from {@link org.miaixz.bus.cortex.Assets#getName()}.
     */
    private String name;

    /**
     * The latest performance metrics for the service process (e.g., CPU and memory usage).
     */
    private Metrics metrics;

    /**
     * The current lifecycle status of the service process (e.g., RUNNING, STOPPED).
     */
    private EnumValue.Lifecycle lifecycle;

}
