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
import org.miaixz.bus.vortex.guard.AdmissionGate;
import org.miaixz.bus.vortex.guard.AsyncByteBudget;
import org.miaixz.bus.vortex.guard.MemoryPressure;
import org.miaixz.bus.vortex.magic.Performance;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufAllocatorMetricProvider;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

/**
 * Global holder for performance configuration and resources in the Vortex gateway (Singleton).
 * <p>
 * This class acts as a centralized registry for all performance-related settings and resources, providing a single
 * source of truth for components that need to access these configurations. It delegates to {@link Instances} for
 * singleton management.
 * <p>
 * <b>Managed Resources:</b>
 * <ul>
 * <li>{@link Performance} - request, connection, buffering and streaming limits</li>
 * <li>{@link ConnectionProvider} - shared outbound HTTP connection pool</li>
 * <li>{@link LoopResources} - shared outbound HTTP event loops</li>
 * <li>{@link ByteBufAllocator} - unified pooled allocator for inbound and outbound channels</li>
 * <li>{@link AdmissionGate} - request and streaming concurrency ownership</li>
 * <li>{@link AsyncByteBudget} - independent request, response and transformation byte budgets</li>
 * <li>{@link MemoryPressure} - sampled heap and direct-memory pressure state</li>
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
     * The key used to store the outbound LoopResources in {@link Instances}.
     */
    public static final String LOOP_RESOURCES_KEY = "vortex:loop-resources";

    /**
     * Key for the unified allocator used by inbound and outbound Reactor Netty channels.
     */
    public static final String ALLOCATOR_KEY = "vortex:allocator";

    /**
     * Key for the process-wide buffered-request byte budget.
     */
    public static final String REQUEST_BUFFER_BUDGET_KEY = "vortex:request-buffer-budget";

    /**
     * Key for the process-wide buffered-response byte budget.
     */
    public static final String RESPONSE_BUFFER_BUDGET_KEY = "vortex:response-buffer-budget";

    /**
     * Key for the process-wide response-transformation byte budget.
     */
    public static final String TRANSFORM_BUFFER_BUDGET_KEY = "vortex:transform-buffer-budget";

    /**
     * Key for hierarchical request and streaming admission.
     */
    public static final String ADMISSION_GATE_KEY = "vortex:admission-gate";

    /**
     * Key for the sampled heap and direct-memory pressure state.
     */
    public static final String MEMORY_PRESSURE_KEY = "vortex:memory-pressure";

    /**
     * Marker key to track initialization status.
     */
    private static final String INIT_MARKER_KEY = "vortex:performance:initialized";

    /**
     * Keeps Vortex shared-resource access on the static holder API.
     */
    private Holder() {
        throw new UnsupportedOperationException("Holder class cannot be instantiated");
    }

    /**
     * Initializes the global performance configuration.
     * <p>
     * This method must run once during startup before any lazily created shared resource is requested. Replacing the
     * configuration after resources have been created does not resize those existing resources.
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
        Logger.info(true, "Vortex", "- Max Request Size: {} MB", performance.getMaxRequestSize() / (1024 * 1024));
        Logger.info(
                true,
                "Vortex",
                "- Max Multipart Request Size: {} MB",
                performance.getMaxMultipartRequestSize() / (1024 * 1024));
        Logger.info(true, "Vortex", "- Max Connections: {}", performance.getMaxConnections());
        Logger.info(true, "Vortex", "- Max In-Flight Requests: {}", performance.getMaxInFlightRequests());
        Logger.info(true, "Vortex", "- Max Streaming Requests: {}", performance.getMaxTotalStreamingRequests());
        Logger.info(true, "Vortex", "- Max Downloads: {}", performance.getMaxDownloadRequests());
        Logger.info(true, "Vortex", "- Max Realtime Streams: {}", performance.getMaxRealtimeStreamingRequests());
        Logger.info(
                true,
                "Vortex",
                "- Request Buffer Budget: {} MB",
                performance.getRequestBufferBudgetBytes() / (1024 * 1024));
        Logger.info(
                true,
                "Vortex",
                "- Response Buffer Budget: {} MB",
                performance.getResponseBufferBudgetBytes() / (1024 * 1024));
        Logger.info(
                true,
                "Vortex",
                "- Transform Buffer Budget: {} MB",
                performance.getTransformBufferBudgetBytes() / (1024 * 1024));
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
     * Initializes outbound HTTP event loops.
     * <p>
     * Vortex outbound routing uses these resources with Reactor Netty native transport disabled so DNS and socket
     * channels stay on NIO in native images while preserving an explicit event-loop lifecycle.
     *
     * @return The configured LoopResources
     */
    public static LoopResources loopResources() {
        return Instances.get(LOOP_RESOURCES_KEY, () -> {
            Logger.info(true, "Vortex", "HTTP event loops initialized");
            Logger.info(true, "Vortex", "  - Transport Preference: nio");
            Logger.info(true, "Vortex", "  - Worker Count: {}", LoopResources.DEFAULT_IO_WORKER_COUNT);
            Logger.info(true, "Vortex", "  - Select Count: {}", LoopResources.DEFAULT_IO_SELECT_COUNT);
            return LoopResources.create(
                    "vortex-http",
                    LoopResources.DEFAULT_IO_SELECT_COUNT,
                    LoopResources.DEFAULT_IO_WORKER_COUNT,
                    true,
                    true);
        });
    }

    /**
     * Returns the only allocator used by Vortex network channels and buffer factories.
     *
     * @return shared pooled direct allocator with built-in metrics
     */
    public static ByteBufAllocator allocator() {
        return Instances.get(ALLOCATOR_KEY, () -> new PooledByteBufAllocator(true));
    }

    /**
     * Returns hierarchical request and streaming admission initialized from the startup profile.
     *
     * @return process-wide admission gate
     */
    public static AdmissionGate admissionGate() {
        Performance performance = get();
        return Instances.get(
                ADMISSION_GATE_KEY,
                () -> new AdmissionGate(performance.getMaxInFlightRequests(),
                        performance.getMaxTotalStreamingRequests(), performance.getMaxDownloadRequests(),
                        performance.getMaxRealtimeStreamingRequests()));
    }

    /**
     * Returns the process-wide logical-byte budget for buffered request bodies.
     *
     * @return buffered-request byte budget
     */
    public static AsyncByteBudget requestBufferBudget() {
        return Instances.get(REQUEST_BUFFER_BUDGET_KEY, () -> new AsyncByteBudget(get().getRequestBufferBudgetBytes()));
    }

    /**
     * Returns the process-wide logical-byte budget for buffered downstream responses.
     *
     * @return buffered-response byte budget
     */
    public static AsyncByteBudget responseBufferBudget() {
        return Instances
                .get(RESPONSE_BUFFER_BUDGET_KEY, () -> new AsyncByteBudget(get().getResponseBufferBudgetBytes()));
    }

    /**
     * Returns the process-wide logical-byte budget for response transformations.
     *
     * @return response-transformation byte budget
     */
    public static AsyncByteBudget transformBufferBudget() {
        return Instances
                .get(TRANSFORM_BUFFER_BUDGET_KEY, () -> new AsyncByteBudget(get().getTransformBufferBudgetBytes()));
    }

    /**
     * Returns the lazily started heap and direct-memory pressure state.
     *
     * @return process-wide memory pressure
     */
    public static MemoryPressure memoryPressure() {
        return Instances.get(
                MEMORY_PRESSURE_KEY,
                () -> new MemoryPressure(Holder::usedDirectMemoryBytes, PlatformDependent.maxDirectMemory(),
                        get().getDirectMemoryHighWatermark(), get().getDirectMemoryLowWatermark(),
                        get().getMemorySampleIntervalMillis()));
    }

    /**
     * Returns direct bytes currently allocated by the unified Netty allocator.
     *
     * @return allocated direct bytes
     */
    public static long usedDirectMemoryBytes() {
        return ((ByteBufAllocatorMetricProvider) allocator()).metric().usedDirectMemory();
    }

    /**
     * Resolves the pending connection acquisition timeout.
     *
     * @param performance performance configuration
     * @return timeout in seconds
     */
    private static int pendingAcquireTimeoutSeconds(Performance performance) {
        return performance.getPendingAcquireTimeoutSeconds() > 0 ? performance.getPendingAcquireTimeoutSeconds() : 5;
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
        return Math.max(1, performance.getMaxConnections());
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
     * Gets the LoopResources if it has been initialized, without creating a new one.
     *
     * @return The LoopResources if initialized, or {@code null} if never created
     */
    public static LoopResources getLoopResourcesIfPresent() {
        return Instances.get(LOOP_RESOURCES_KEY, () -> null);
    }

    /**
     * Stops the memory sampler and closes every byte budget during gateway shutdown.
     * <p>
     * Closing a budget fails queued acquisitions; already granted leases can still return their ownership safely.
     */
    public static void closeCapacityResources() {
        MemoryPressure pressure = Instances.get(MEMORY_PRESSURE_KEY, () -> null);
        if (pressure != null) {
            pressure.close();
        }
        AsyncByteBudget request = Instances.get(REQUEST_BUFFER_BUDGET_KEY, () -> null);
        AsyncByteBudget response = Instances.get(RESPONSE_BUFFER_BUDGET_KEY, () -> null);
        AsyncByteBudget transform = Instances.get(TRANSFORM_BUFFER_BUDGET_KEY, () -> null);
        if (request != null) {
            request.close();
        }
        if (response != null) {
            response.close();
        }
        if (transform != null) {
            transform.close();
        }
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
