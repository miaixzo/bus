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
package org.miaixz.bus.vortex;

import java.time.Duration;

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
 * @since Java 17+
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

        // Use Instances.put() for thread-safe singleton registration
        // ConcurrentHashMap ensures thread-safety for concurrent writes
        Instances.put(PERFORMANCE_KEY, performance);
        Instances.put(INIT_MARKER_KEY, Boolean.TRUE);
        Logger.info(true, "Holder", "Performance configuration initialized:");
        Logger.info(
                true,
                "Holder",
                "  - Streaming Request Threshold: {} MB",
                performance.getStreamingRequestThreshold() / (1024 * 1024));
        Logger.info(true, "Holder", "  - Max Request Size: {} MB", performance.getMaxRequestSize() / (1024 * 1024));
        Logger.info(
                true,
                "Holder",
                "  - Max Multipart Request Size: {} MB",
                performance.getMaxMultipartRequestSize() / (1024 * 1024));
        Logger.info(true, "Holder", "  - Max Connections: {}", performance.getMaxConnections());
        Logger.info(true, "Holder", "  - Max Producer Cache Size: {}", performance.getMaxProducerCacheSize());
        Logger.info(true, "Holder", "  - L2 Cache Size: {}", performance.getCacheSize());
        Logger.info(true, "Holder", "  - L2 Cache Expire: {} ms", performance.getCacheExpireMs());
        Logger.info(true, "Holder", "  - Sync Interval: {} seconds", performance.getSyncIntervalSeconds());
        Logger.info(true, "Holder", "  - Full Sync On Startup: {}", performance.isFullSyncOnStartup());
        Logger.info(true, "Holder", "  - Startup Delay: {} seconds", performance.getStartupDelaySeconds());
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
            Logger.info(true, "Holder", "ConnectionProvider initialized:");
            Logger.info(true, "Holder", "  - Pool Name: vortex-http-pool");
            Logger.info(true, "Holder", "  - Max Connections: {}", perf.getMaxConnections());
            return ConnectionProvider.builder("vortex-http-pool").maxConnections(perf.getMaxConnections())
                    .pendingAcquireTimeout(Duration.ofSeconds(45)).pendingAcquireMaxCount(-1) // Unlimited pending
                                                                                              // acquires
                    .maxIdleTime(Duration.ofSeconds(20)).maxLifeTime(Duration.ofMinutes(5)).build();
        });
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
        // If key exists, return the ConnectionProvider without calling supplier
        // If key doesn't exist, supplier returns null, which is NOT stored in the map
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
        // Use Instances.get() with supplier to provide default configuration
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

}
