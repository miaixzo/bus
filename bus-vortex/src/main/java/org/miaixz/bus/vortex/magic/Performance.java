/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.magic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Centralized performance configuration holder for the Vortex gateway.
 * <p>
 * This class acts as a global registry for all performance-related settings, providing a single source of truth for
 * components that need to access these configurations. It is designed to be framework-agnostic and can be instantiated
 * from any configuration source (Spring Boot properties, system properties, environment variables, etc.).
 * <p>
 * Performance optimizations controlled by this configuration:
 * <ul>
 * <li>Request body size limits for DoS prevention</li>
 * <li>Streaming thresholds for memory optimization</li>
 * <li>Connection pool sizing for HTTP clients</li>
 * <li>Cache size limits for MQ producers</li>
 * <li>Registry L2 cache configuration (Caffeine)</li>
 * <li>Cluster synchronization configuration</li>
 * </ul>
 * <p>
 * <b>Default Values:</b>
 * <ul>
 * <li>streamingRequestThreshold: 10 MB</li>
 * <li>maxRequestSize: 100 MB</li>
 * <li>maxMultipartRequestSize: 512 MB</li>
 * <li>maxConnections: 500</li>
 * <li>maxProducerCacheSize: 100</li>
 * <li>registryL2CacheSize: 10,000 (assets)</li>
 * <li>registryL2CacheExpireMs: 300,000 (5 minutes)</li>
 * <li>clusterSyncIntervalSeconds: 60 (1 minute)</li>
 * <li>clusterFullSyncOnStartup: true</li>
 * <li>clusterStartupDelaySeconds: 10</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    /**
     * The threshold in bytes for enabling streaming request body processing.
     * <p>
     * Request bodies smaller than this threshold will be cached in memory for faster processing. Request bodies larger
     * than this threshold will use streaming processing to avoid high memory pressure.
     */
    @Builder.Default
    private long streamingRequestThreshold = 10 * 1024 * 1024;

    /**
     * Maximum size in bytes for non-multipart request bodies (JSON, form-urlencoded).
     * <p>
     * Acts as a DoS prevention limit. Requests exceeding this size will be rejected.
     */
    @Builder.Default
    private long maxRequestSize = 100 * 1024 * 1024;

    /**
     * Maximum size in bytes for multipart/form-data requests (file uploads).
     */
    @Builder.Default
    private long maxMultipartRequestSize = 512 * 1024 * 1024;

    /**
     * Maximum number of HTTP connections in the connection pool.
     */
    @Builder.Default
    private int maxConnections = 500;

    /**
     * Maximum number of MQ producer instances to cache.
     */
    @Builder.Default
    private int maxProducerCacheSize = 100;

    /**
     * Registry L2 cache maximum size (number of assets).
     * <p>
     * Used by AbstractRegistry's Caffeine cache for the second-level cache layer. When the cache exceeds this size,
     * least-recently-used entries will be evicted.
     * </p>
     */
    @Builder.Default
    private long cacheSize = 10_000L;

    /**
     * Registry L2 cache expiration time in milliseconds.
     * <p>
     * Used by AbstractRegistry's Caffeine cache for time-based eviction. Entries that haven't been accessed within this
     * duration will be automatically evicted. Default: 300,000ms (5 minutes).
     * </p>
     */
    @Builder.Default
    private long cacheExpireMs = 300_000L;

    /**
     * Cluster synchronization interval in seconds.
     * <p>
     * Used by ClusterSynchronizer implementations to determine how often to poll for changes. Default: 60 seconds (1
     * minute).
     * </p>
     */
    @Builder.Default
    private int syncIntervalSeconds = 60;

    /**
     * Whether to perform full synchronization on startup.
     * <p>
     * If true, the application will fetch all data from the source on startup. Default: true.
     * </p>
     */
    @Builder.Default
    private boolean fullSyncOnStartup = true;

    /**
     * Startup delay before synchronization begins (in seconds).
     * <p>
     * Allows the application to initialize before starting cluster synchronization. Default: 10 seconds.
     * </p>
     */
    @Builder.Default
    private int startupDelaySeconds = 10;

}
