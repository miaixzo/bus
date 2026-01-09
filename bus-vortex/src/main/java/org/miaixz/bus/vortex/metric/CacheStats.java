/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
