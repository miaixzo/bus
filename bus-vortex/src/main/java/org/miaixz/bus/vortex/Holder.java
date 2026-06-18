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
package org.miaixz.bus.vortex;

import java.time.Duration;
import java.util.List;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.magic.Performance;

import reactor.netty.resources.ConnectionProvider;

/**
 * Global holder for performance configuration and resources in the Vortex gateway (Singleton).
 * <p>
 * This class acts as a centralized registry for all performance-related settings and resources, providing a single
 * source of truth for components that need to access these configurations. It delegates to {@link Instances} for
 * singleton management.
 * <p>
 * <b>Managed Resources:</b>
 * <ul>
 * <li>{@link Performance} - Performance configuration (request limits, streaming thresholds, etc.)</li>
 * <li>{@link ConnectionProvider} - HTTP connection pool for REST requests</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe as it delegates to {@link Instances} which provides thread-safe
 * singleton management via {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Holder {

    /**
     * The key used to store the Performance configuration in {@link Instances}.
     */
    private static final String PERFORMANCE_KEY = "vortex:performance";

    /**
     * The key used to store the ConnectionProvider in {@link Instances}.
     */
    public static final String CONNECTION_PROVIDER_KEY = "vortex:connection-provider";

    /**
     * Marker key to track initialization status.
     */
    private static final String INIT_MARKER_KEY = "vortex:performance:initialized";

    /**
     * Private constructor to prevent instantiation.
     * <p>
     * This is a utility class with only static members. Attempting to instantiate will throw an exception.
     */
    private Holder() {
        throw new UnsupportedOperationException("Holder class cannot be instantiated");
    }

    /**
     * Initializes the global performance configuration.
     * <p>
     * <b>Thread Safety:</b> This method is thread-safe. Multiple concurrent calls will use the last configuration
     * provided (ConcurrentHashMap ensures consistency).
     * <p>
     * <b>Example:</b>
     *
     * <pre>{@code
     * &#64;Bean
     * public Vortex vortex(...) {
     *     Performance perf = properties.getPerformance();
     *     Holder.of(perf);  // Initialize once at startup
     *     // ... rest of initialization
     * }
     * }</pre>
     *
     * @param performance The performance configuration to set (must not be null)
     * @throws IllegalArgumentException if performance is null
     */
    public static void of(Performance performance) {
        if (performance == null) {
            throw new IllegalArgumentException("Performance configuration cannot be null");
        }

        Instances.put(PERFORMANCE_KEY, performance);
        Instances.put(INIT_MARKER_KEY, Boolean.TRUE);
        Logger.info(true, "Vortex", "Performance profile applied");
        Logger.info(
                true,
                "Vortex",
                "- Streaming Request Threshold: {} MB",
                performance.getStreamingRequestThreshold() / (1024 * 1024));
        Logger.info(true, "Vortex", "- Max Request Size: {} MB", performance.getMaxRequestSize() / (1024 * 1024));
        Logger.info(
                true,
                "Vortex",
                "- Max Multipart Request Size: {} MB",
                performance.getMaxMultipartRequestSize() / (1024 * 1024));
        Logger.info(true, "Vortex", "- Max Connections: {}", performance.getMaxConnections());
        Logger.info(true, "Vortex", "- Pending Acquire Timeout: {} seconds", pendingAcquireTimeoutSeconds(performance));
        Logger.info(true, "Vortex", "- Pending Acquire Max Count: {}", pendingAcquireMaxCount(performance));
        Logger.info(true, "Vortex", "- Outbound Max Idle: {} seconds", outboundMaxIdleSeconds(performance));
        Logger.info(true, "Vortex", "- Outbound Max Life: {} minutes", outboundMaxLifeMinutes(performance));
        Logger.info(true, "Vortex", "- Outbound Evict: {} seconds", outboundEvictSeconds(performance));
        Logger.info(true, "Vortex", "- Max Producer Cache Size: {}", performance.getMaxProducerCacheSize());
        Logger.info(true, "Vortex", "- L2 Cache Size: {}", performance.getCacheSize());
        Logger.info(true, "Vortex", "- L2 Cache Expire: {} ms", performance.getCacheExpireMs());
        Logger.info(true, "Vortex", "- Sync Interval: {} seconds", performance.getSyncIntervalSeconds());
        Logger.info(true, "Vortex", "- Full Sync On Startup: {}", performance.isFullSyncOnStartup());
        Logger.info(true, "Vortex", "- Startup Delay: {} seconds", performance.getStartupDelaySeconds());
        Logger.info(true, "Vortex", "- Sanitize Null-like Parameters: {}", performance.isSanitizeNullLikeParameters());
    }

    /**
     * Initializes the HTTP connection pool.
     * <p>
     * Creates a shared {@link ConnectionProvider} with configuration from the {@link Performance} settings. This
     * connection pool is used by all REST requests to downstream services.
     *
     * @return The configured ConnectionProvider
     */
    public static ConnectionProvider connectionProvider() {
        return Instances.get(CONNECTION_PROVIDER_KEY, () -> {
            Performance perf = get();
            int pendingAcquireMaxCount = pendingAcquireMaxCount(perf);
            int pendingAcquireTimeoutSeconds = pendingAcquireTimeoutSeconds(perf);
            int maxIdleSeconds = outboundMaxIdleSeconds(perf);
            int maxLifeMinutes = outboundMaxLifeMinutes(perf);
            int evictSeconds = outboundEvictSeconds(perf);
            Logger.info(true, "Vortex", "HTTP connection pool initialized");
            Logger.info(true, "Vortex", "  - Pool Name: vortex-http-pool");
            Logger.info(true, "Vortex", "  - Max Connections: {}", perf.getMaxConnections());
            Logger.info(true, "Vortex", "  - Pending Acquire Timeout: {} seconds", pendingAcquireTimeoutSeconds);
            Logger.info(true, "Vortex", "  - Pending Acquire Max Count: {}", pendingAcquireMaxCount);
            Logger.info(true, "Vortex", "  - Max Idle Time: {} seconds", maxIdleSeconds);
            Logger.info(true, "Vortex", "  - Max Life Time: {} minutes", maxLifeMinutes);
            Logger.info(true, "Vortex", "  - Evict In Background: {} seconds", evictSeconds);
            return ConnectionProvider.builder("vortex-http-pool").maxConnections(perf.getMaxConnections())
                    .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
                    .pendingAcquireMaxCount(pendingAcquireMaxCount).maxIdleTime(Duration.ofSeconds(maxIdleSeconds))
                    .maxLifeTime(Duration.ofMinutes(maxLifeMinutes)).evictInBackground(Duration.ofSeconds(evictSeconds))
                    .build();
        });
    }

    /**
     * Resolves the pending connection acquisition timeout.
     *
     * @param performance performance configuration
     * @return timeout in seconds
     */
    private static int pendingAcquireTimeoutSeconds(Performance performance) {
        return performance.getPendingAcquireTimeoutSeconds() > 0 ? performance.getPendingAcquireTimeoutSeconds() : 45;
    }

    /**
     * Resolves the bounded pending connection acquisition queue size.
     *
     * @param performance performance configuration
     * @return pending acquisition queue size
     */
    private static int pendingAcquireMaxCount(Performance performance) {
        if (performance.getPendingAcquireMaxCount() > 0) {
            return performance.getPendingAcquireMaxCount();
        }
        long derived = Math.max(1L, performance.getMaxConnections()) * 2L;
        return derived > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) derived;
    }

    /**
     * Resolves outbound retry initial backoff in milliseconds.
     *
     * @return retry initial backoff
     */
    public static int outboundRetryBackoffMillis() {
        return outboundRetryBackoffMillis(get());
    }

    /**
     * Resolves outbound retry maximum backoff in milliseconds.
     *
     * @return retry maximum backoff
     */
    public static int outboundRetryMaxBackoffMillis() {
        return outboundRetryMaxBackoffMillis(get());
    }

    /**
     * Resolves outbound max idle time in seconds.
     *
     * @return max idle time
     */
    public static int outboundMaxIdleSeconds() {
        return outboundMaxIdleSeconds(get());
    }

    /**
     * Resolves outbound max life time in minutes.
     *
     * @return max life time
     */
    public static int outboundMaxLifeMinutes() {
        return outboundMaxLifeMinutes(get());
    }

    /**
     * Resolves outbound background eviction interval in seconds.
     *
     * @return eviction interval
     */
    public static int outboundEvictSeconds() {
        return outboundEvictSeconds(get());
    }

    private static int outboundRetryBackoffMillis(Performance performance) {
        return performance.getOutboundRetryBackoffMillis() > 0 ? performance.getOutboundRetryBackoffMillis() : 100;
    }

    private static int outboundRetryMaxBackoffMillis(Performance performance) {
        int maxBackoff = performance.getOutboundRetryMaxBackoffMillis();
        int backoff = outboundRetryBackoffMillis(performance);
        return maxBackoff >= backoff ? maxBackoff : 5000;
    }

    private static int outboundMaxIdleSeconds(Performance performance) {
        return performance.getOutboundMaxIdleSeconds() > 0 ? performance.getOutboundMaxIdleSeconds() : 20;
    }

    private static int outboundMaxLifeMinutes(Performance performance) {
        return performance.getOutboundMaxLifeMinutes() > 0 ? performance.getOutboundMaxLifeMinutes() : 5;
    }

    private static int outboundEvictSeconds(Performance performance) {
        return performance.getOutboundEvictSeconds() > 0 ? performance.getOutboundEvictSeconds() : 30;
    }

    /**
     * Gets the ConnectionProvider if it has been initialized, without creating a new one.
     * <p>
     * This is used during shutdown to safely access the ConnectionProvider. Returns null if the ConnectionProvider was
     * never created (e.g., the application never handled any HTTP requests).
     * <p>
     * Implementation note: Uses {@link Instances#get(String, SupplierX)} with a null-returning supplier. Since
     * {@code ConcurrentHashMap.computeIfAbsent()} does not allow null values, if the key doesn't exist, null is
     * returned without being stored in the pool.
     *
     * @return The ConnectionProvider if initialized, or {@code null} if never created
     */
    public static ConnectionProvider getConnectionProviderIfPresent() {
        return Instances.get(CONNECTION_PROVIDER_KEY, () -> null);
    }

    /**
     * Gets the global performance configuration.
     * <p>
     * Returns the configuration instance initialized at startup. If not explicitly initialized, returns the default
     * configuration with default values.
     *
     * @return The current performance configuration (never null)
     */
    public static Performance get() {
        return Instances.get(PERFORMANCE_KEY, () -> Performance.builder().build());
    }

    /**
     * Checks whether the holder has been explicitly initialized.
     * <p>
     * This can be useful for debugging or testing purposes.
     *
     * @return {@code true} if {@link #of(Performance)} has been called, {@code false} otherwise
     */
    public static boolean isInitialized() {
        return Instances.get(INIT_MARKER_KEY, () -> Boolean.FALSE);
    }

    /**
     * Gets the streaming request threshold in bytes.
     * <p>
     * Request bodies smaller than this threshold will be cached in memory for faster processing. Request bodies larger
     * than this threshold will use streaming processing.
     *
     * @return The streaming request threshold in bytes
     */
    public static long getStreamingRequestThreshold() {
        return get().getStreamingRequestThreshold();
    }

    /**
     * Gets the maximum allowed size for non-multipart request bodies in bytes.
     * <p>
     * Acts as a DoS prevention limit. Requests exceeding this size will be rejected.
     *
     * @return The maximum request size in bytes
     */
    public static long getMaxRequestSize() {
        return get().getMaxRequestSize();
    }

    /**
     * Gets the maximum allowed size for multipart/form-data requests in bytes.
     *
     * @return The maximum multipart request size in bytes
     */
    public static long getMaxMultipartRequestSize() {
        return get().getMaxMultipartRequestSize();
    }

    /**
     * Gets the maximum number of HTTP connections in the connection pool.
     *
     * @return The maximum number of connections
     */
    public static int getMaxConnections() {
        return get().getMaxConnections();
    }

    /**
     * Gets the maximum number of MQ producer instances to cache.
     *
     * @return The maximum producer cache size
     */
    public static int getMaxProducerCacheSize() {
        return get().getMaxProducerCacheSize();
    }

    /**
     * Gets the L2 cache maximum size (number of assets).
     * <p>
     * Used by AbstractRegistry's Caffeine cache for the second-level cache layer.
     *
     * @return The L2 cache maximum size
     */
    public static long getCacheSize() {
        return get().getCacheSize();
    }

    /**
     * Gets the L2 cache expiration time in milliseconds.
     * <p>
     * Used by AbstractRegistry's Caffeine cache for time-based eviction.
     *
     * @return The L2 cache expiration time in milliseconds
     */
    public static long getCacheExpireMs() {
        return get().getCacheExpireMs();
    }

    /**
     * Checks whether null-like parameter sanitization is enabled.
     *
     * @return {@code true} when null-like values should be removed from request parameters
     */
    public static boolean isSanitizeNullLikeParameters() {
        return get().isSanitizeNullLikeParameters();
    }

    /**
     * Gets the synchronization interval in seconds.
     * <p>
     * Used by ClusterSynchronizer implementations to determine how often to poll for changes.
     *
     * @return The synchronization interval in seconds
     */
    public static int getSyncIntervalSeconds() {
        return get().getSyncIntervalSeconds();
    }

    /**
     * Gets whether to perform full synchronization on startup.
     * <p>
     * If true, the application will fetch all data from the source on startup.
     *
     * @return {@code true} if full sync on startup is enabled
     */
    public static boolean isFullSyncOnStartup() {
        return get().isFullSyncOnStartup();
    }

    /**
     * Gets the startup delay before synchronization begins (in seconds).
     * <p>
     * Allows the application to initialize before starting cluster synchronization.
     *
     * @return The startup delay in seconds
     */
    public static int getStartupDelaySeconds() {
        return get().getStartupDelaySeconds();
    }

    /**
     * Gets the maximum allowed time difference (in minutes) between the client timestamp and the server time.
     *
     * @return The timestamp tolerance in minutes
     */
    public static int getTimestampToleranceMinutes() {
        return get().getTimestampToleranceMinutes();
    }

    /**
     * Gets the trusted origin list for MCP Streamable HTTP ingress requests.
     *
     * @return configured trusted origins, or an empty list for same-host only validation
     */
    public static List<String> getMcpTrustedOrigins() {
        List<String> trustedOrigins = get().getMcpTrustedOrigins();
        return trustedOrigins == null ? List.of() : trustedOrigins;
    }

}
