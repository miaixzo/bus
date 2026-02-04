/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.metric;

import lombok.*;
import org.miaixz.bus.vortex.Monitor;

/**
 * Cache statistics data structure for tracking cache access metrics.
 * <p>
 * A generic cache statistics container that tracks cache access patterns. Suitable for both L1 cache
 * (ConcurrentHashMap) and L2 cache (Caffeine) statistics.
 * </p>
 *
 * <p>
 * <b>Responsibility:</b>
 * </p>
 * <ul>
 * <li>Located in the {@code metrics} package as part of performance metrics collection</li>
 * <li>Coexists with {@link Monitor} and {@link org.miaixz.bus.vortex.magic.Metrics}</li>
 * <li>Focuses on being a data carrier without involving cache implementation details</li>
 * </ul>
 *
 * <p>
 * <b>Use Cases:</b>
 * </p>
 * <ul>
 * <li>Two-level cache statistics for {@link org.miaixz.bus.vortex.registry.AbstractRegistry}</li>
 * <li>Statistics for other cache implementations</li>
 * </ul>
 *
 * <p>
 * <b>Metrics:</b>
 * </p>
 * <ul>
 * <li>{@code hitCount}: Total number of cache hits</li>
 * <li>{@code missCount}: Total number of cache misses</li>
 * <li>{@code hitRate}: Cache hit rate (0.0 - 1.0)</li>
 * <li>{@code cacheSize}: Current size of L1 cache</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStats {

    /**
     * Total number of cache hits.
     */
    private long hitCount;

    /**
     * Total number of cache misses.
     */
    private long missCount;

    /**
     * Cache hit rate (0.0 - 1.0).
     */
    private double hitRate;

    /**
     * Current size of L1 cache (ConcurrentHashMap).
     */
    private long cacheSize;

}
