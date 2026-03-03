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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.vortex.magic.Metrics;
import reactor.core.publisher.Mono;

/**
 * A Service Provider Interface (SPI) for monitoring the performance of a running service process.
 * <p>
 * Implementations of this interface are responsible for fetching performance metrics, such as CPU and memory usage, for
 * a given service process. This allows the core logic to remain agnostic of the underlying monitoring mechanism (e.g.,
 * system commands, JMX, or a library like OSHI).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MetricsProvider {

    /**
     * Retrieves performance metrics for a given service process.
     *
     * @param serviceId A unique identifier for the service.
     * @return A {@code Mono} emitting the {@link Metrics} for the service. If the process is not found or metrics are
     *         unavailable, it is recommended to emit a {@code Mono} with a zero-value {@code ProcessMetrics} object
     *         rather than an empty or error signal, to simplify downstream processing.
     */
    Mono<Metrics> getMetrics(String serviceId);

}
