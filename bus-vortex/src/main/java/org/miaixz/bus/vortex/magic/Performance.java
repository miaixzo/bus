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

import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
 * <li>Streaming and download concurrency isolation</li>
 * <li>Connection pool sizing for HTTP clients</li>
 * <li>Cache size limits for MQ producers</li>
 * <li>Registry L2 cache configuration (Caffeine)</li>
 * <li>Cluster synchronization configuration</li>
 * <li>Null-like request parameter sanitization</li>
 * </ul>
 * <p>
 * <b>Default Values:</b>
 * <ul>
 * <li>maxRequestSize: 100 MB</li>
 * <li>maxMultipartRequestSize: 1024 MB</li>
 * <li>multipartMemoryThresholdBytes: 64 KB per part</li>
 * <li>maxBufferedRequestSize: 1 MB</li>
 * <li>maxBufferedResponseSize: 32 MB</li>
 * <li>requestBufferBudgetBytes: 64 MB</li>
 * <li>responseBufferBudgetBytes: 160 MB</li>
 * <li>transformBufferBudgetBytes: 32 MB</li>
 * <li>maxTransformResponseSize: 8 MB</li>
 * <li>bufferAcquireTimeoutSeconds: 30 seconds</li>
 * <li>maxInFlightRequests: 5000</li>
 * <li>maxTotalStreamingRequests: 4000</li>
 * <li>maxDownloadRequests: 4000</li>
 * <li>maxRealtimeStreamingRequests: 1000</li>
 * <li>directMemoryHighWatermark: 0.70</li>
 * <li>directMemoryLowWatermark: 0.50</li>
 * <li>memorySampleIntervalMillis: 100 milliseconds</li>
 * <li>writeBufferLowWatermarkBytes: 32 KB</li>
 * <li>writeBufferHighWatermarkBytes: 64 KB</li>
 * <li>downloadNoProgressTimeoutSeconds: 60 seconds</li>
 * <li>downloadMinimumBytesPerSecond: 32 KB/s</li>
 * <li>maxConnections: 5000</li>
 * <li>pendingAcquireTimeoutSeconds: 5 seconds</li>
 * <li>pendingAcquireMaxCount: 5000</li>
 * <li>outboundRetryBackoffMillis: 100 milliseconds</li>
 * <li>outboundRetryMaxBackoffMillis: 5000 milliseconds</li>
 * <li>outboundMaxIdleSeconds: 20 seconds</li>
 * <li>outboundMaxLifeMinutes: 5 minutes</li>
 * <li>outboundEvictSeconds: 30 seconds</li>
 * <li>maxProducerCacheSize: 1000</li>
 * <li>registryL2CacheSize: 10,000 (assets)</li>
 * <li>registryL2CacheExpireMs: 300,000 (5 minutes)</li>
 * <li>clusterSyncIntervalSeconds: 60 (1 minute)</li>
 * <li>clusterFullSyncOnStartup: true</li>
 * <li>clusterStartupDelaySeconds: 10</li>
 * <li>sanitizeNullLikeParameters: true</li>
 * </ul>
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Performance {

    /**
     * Creates a performance configuration with default values.
     */
    public Performance() {
        // No initialization required.
    }

    /**
     * Transport-level maximum size in bytes for non-multipart request bodies.
     * <p>
     * This is the outer DoS limit. Operations that must materialize a body are further restricted by
     * {@link #maxBufferedRequestSize}.
     */
    @Builder.Default
    private long maxRequestSize = 100 * 1024 * 1024;

    /**
     * Maximum accepted size in bytes for one multipart/form-data request, including file uploads.
     */
    @Builder.Default
    private long maxMultipartRequestSize = 1024 * 1024 * 1024;

    /**
     * Maximum bytes retained for one multipart part before the part reader spills it to disk.
     */
    @Builder.Default
    private int multipartMemoryThresholdBytes = 64 * 1024;

    /**
     * Maximum request bytes allowed to be materialized for parsing, signing or replay.
     */
    @Builder.Default
    private long maxBufferedRequestSize = 1024 * 1024;

    /**
     * Maximum downstream response bytes allowed to be materialized by an atomic route.
     */
    @Builder.Default
    private long maxBufferedResponseSize = 32 * 1024 * 1024;

    /**
     * Process-wide logical-byte budget for simultaneously materialized request bodies.
     */
    @Builder.Default
    private long requestBufferBudgetBytes = 64 * 1024 * 1024;

    /**
     * Process-wide logical-byte budget for buffered downstream responses.
     */
    @Builder.Default
    private long responseBufferBudgetBytes = 160 * 1024 * 1024;

    /**
     * Process-wide logical-byte budget for response transformations.
     */
    @Builder.Default
    private long transformBufferBudgetBytes = 32 * 1024 * 1024;

    /**
     * Maximum input or output size accepted by a bounded response transformation.
     */
    @Builder.Default
    private long maxTransformResponseSize = 8 * 1024 * 1024;

    /**
     * Maximum time in seconds that buffered work may wait asynchronously for byte-budget capacity.
     */
    @Builder.Default
    private int bufferAcquireTimeoutSeconds = 30;

    /**
     * Maximum complete reactive request lifecycles admitted to the gateway at one time.
     */
    @Builder.Default
    private int maxInFlightRequests = 5000;

    /**
     * Maximum downloads and realtime streaming responses active concurrently.
     */
    @Builder.Default
    private int maxTotalStreamingRequests = 4000;

    /**
     * Maximum concurrently active file downloads.
     */
    @Builder.Default
    private int maxDownloadRequests = 4000;

    /**
     * Maximum concurrently active SSE, LLM-streaming and similar latency-sensitive responses.
     */
    @Builder.Default
    private int maxRealtimeStreamingRequests = 1000;

    /**
     * Direct-memory ratio at which the gateway rejects new realtime streams.
     * <p>
     * Downloads are rejected ten percentage points earlier, while emergency rejection begins ten percentage points
     * later. Validation keeps the emergency threshold below the direct-memory maximum.
     */
    @Builder.Default
    private double directMemoryHighWatermark = 0.70d;

    /**
     * Direct-memory ratio below which throttled traffic may return to normal admission.
     * <p>
     * Recovery also requires heap usage below the pressure state's recovery threshold.
     */
    @Builder.Default
    private double directMemoryLowWatermark = 0.50d;

    /**
     * Sampling interval for heap and direct-memory pressure.
     */
    @Builder.Default
    private int memorySampleIntervalMillis = 100;

    /**
     * Netty channel writable low watermark.
     */
    @Builder.Default
    private int writeBufferLowWatermarkBytes = 32 * 1024;

    /**
     * Netty channel writable high watermark.
     */
    @Builder.Default
    private int writeBufferHighWatermarkBytes = 64 * 1024;

    /**
     * Maximum number of seconds a download may emit no data before it is terminated.
     */
    @Builder.Default
    private int downloadNoProgressTimeoutSeconds = 60;

    /**
     * Minimum average download rate, evaluated after the no-progress interval, in bytes per second.
     */
    @Builder.Default
    private long downloadMinimumBytesPerSecond = 32 * 1024;

    /**
     * Maximum number of HTTP connections in the connection pool.
     */
    @Builder.Default
    private int maxConnections = 5000;

    /**
     * Maximum time in seconds to wait for a pooled HTTP connection.
     * <p>
     * Requests waiting longer than this value fail instead of staying queued indefinitely. Starter validation requires
     * a positive value; direct runtime use falls back to 5 seconds when no valid value was supplied.
     */
    @Builder.Default
    private int pendingAcquireTimeoutSeconds = 5;

    /**
     * Maximum number of pending HTTP connection acquisition requests.
     * <p>
     * Values greater than zero are used directly. Values less than or equal to zero are resolved to the configured
     * connection count.
     */
    @Builder.Default
    private int pendingAcquireMaxCount = 5000;

    /**
     * Maximum retries for safe, idempotent outbound methods.
     */
    @Builder.Default
    private int maxRetries = 2;

    /**
     * Initial retry backoff in milliseconds.
     */
    @Builder.Default
    private int outboundRetryBackoffMillis = 100;

    /**
     * Maximum retry backoff in milliseconds.
     */
    @Builder.Default
    private int outboundRetryMaxBackoffMillis = 5000;

    /**
     * Maximum idle time for pooled outbound HTTP connections in seconds.
     */
    @Builder.Default
    private int outboundMaxIdleSeconds = 20;

    /**
     * Maximum life time for pooled outbound HTTP connections in minutes.
     */
    @Builder.Default
    private int outboundMaxLifeMinutes = 5;

    /**
     * Background eviction interval for pooled outbound HTTP connections in seconds.
     */
    @Builder.Default
    private int outboundEvictSeconds = 30;

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

    /**
     * Whether null-like request parameter values should be removed before they are stored and forwarded.
     * <p>
     * When enabled, parameters whose values are {@code null}, {@code "null"}, or {@code "undefined"} are discarded
     * throughout request ingestion, context enrichment, and outbound forwarding. Default: true.
     * </p>
     */
    @Builder.Default
    private boolean sanitizeNullLikeParameters = true;

    /**
     * Trusted origins accepted by the MCP Streamable HTTP ingress. When empty, MCP requests only trust same-host
     * origins.
     */
    @Builder.Default
    private List<String> mcpTrustedOrigins = List.of();

}
