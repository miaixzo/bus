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
 * <li>maxMultipartRequestSize: 1024 MB</li>
 * <li>maxConnections: 5000</li>
 * <li>maxProducerCacheSize: 100</li>
 * <li>registryL2CacheSize: 10,000 (assets)</li>
 * <li>registryL2CacheExpireMs: 300,000 (5 minutes)</li>
 * <li>clusterSyncIntervalSeconds: 60 (1 minute)</li>
 * <li>clusterFullSyncOnStartup: true</li>
 * <li>clusterStartupDelaySeconds: 10</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
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
    private long maxMultipartRequestSize = 1024 * 1024 * 1024;

    /**
     * Maximum number of HTTP connections in the connection pool.
     */
    @Builder.Default
    private int maxConnections = 5000;

    /**
     * Maximum number of MQ producer instances to cache.
     */
    @Builder.Default
    private int maxProducerCacheSize = 1000;

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

    /**
     * The maximum allowed time difference (in minutes) between the client timestamp and the server time.
     * <p>
     * Requests with a timestamp outside this window will be rejected to prevent replay attacks. Default: 30 minutes.
     * </p>
     */
    @Builder.Default
    private int timestampToleranceMinutes = 30;

}
