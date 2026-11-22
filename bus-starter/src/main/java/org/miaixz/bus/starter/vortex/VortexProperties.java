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
package org.miaixz.bus.starter.vortex;

import java.net.URI;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.magic.Performance;

/**
 * Configuration properties for the Vortex routing gateway.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(GeniusBuilder.VORTEX)
public class VortexProperties {

    /**
     * Whether Vortex integration is enabled.
     */
    private boolean enabled;

    /**
     * The service port, specifying the port number the server listens on.
     */
    private int port = 8765;

    /**
     * Bind address for the private SDC listener.
     */
    private String internalHost = "127.0.0.1";

    /**
     * Private SDC listening port. Zero disables the listener.
     */
    private int internalPort;

    /**
     * Exact path reserved for private SDC requests.
     */
    private String internalPath;

    /**
     * The service path, specifying the access path for the server.
     */
    private String path;

    /**
     * A condition to enable or disable custom Spring MVC configuration handling.
     */
    private boolean condition;

    /**
     * Rate limiting configuration, initialized by default.
     */
    private Args.Limit limit = Args.Limit.builder().build();

    /**
     * Capacity, memory, streaming, download and outbound connection settings.
     * <p>
     * The defaults admit 5000 complete request lifecycles while bounding every body category independently.
     */
    private Performance performance = Performance.builder().build();

    /**
     * Asset registry runtime configuration.
     */
    private Assets assets = new Assets();

    /**
     * Optional SDC startup route configuration.
     */
    private final Sdc sdc = new Sdc();

    /**
     * Initializes Vortex properties with the documented transport and asset refresh defaults.
     */
    public VortexProperties() {
        // No initialization required.
    }

    /**
     * Validates bound values and cross-field capacity invariants before shared runtime resources are created.
     * <p>
     * In addition to positive ranges, this ensures child streaming limits fit under their parent limit, each byte
     * budget can hold one maximum body, and all logical byte budgets together remain within one-sixteenth of the JVM
     * maximum heap.
     */
    public void validate() {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("bus.vortex.port must be in 1..65535");
        }
        validateSdc();
        if (performance == null || performance.getMaxRequestSize() <= 0 || performance.getMaxMultipartRequestSize() <= 0
                || performance.getMultipartMemoryThresholdBytes() <= 0
                || performance.getMultipartMemoryThresholdBytes() > performance.getMaxBufferedRequestSize()
                || performance.getMaxBufferedRequestSize() <= 0 || performance.getMaxBufferedResponseSize() <= 0
                || performance.getMaxTransformResponseSize() <= 0
                || performance.getRequestBufferBudgetBytes() < performance.getMaxBufferedRequestSize()
                || performance.getResponseBufferBudgetBytes() < performance.getMaxBufferedResponseSize()
                || performance.getTransformBufferBudgetBytes() < performance.getMaxTransformResponseSize()
                || performance.getMaxBufferedRequestSize() > performance.getMaxRequestSize()
                || performance.getMaxInFlightRequests() <= 0 || performance.getMaxTotalStreamingRequests() <= 0
                || performance.getMaxTotalStreamingRequests() > performance.getMaxInFlightRequests()
                || performance.getMaxDownloadRequests() <= 0
                || performance.getMaxDownloadRequests() > performance.getMaxTotalStreamingRequests()
                || performance.getMaxRealtimeStreamingRequests() <= 0
                || performance.getMaxRealtimeStreamingRequests() > performance.getMaxTotalStreamingRequests()
                || performance.getBufferAcquireTimeoutSeconds() <= 0 || performance.getMemorySampleIntervalMillis() <= 0
                || performance.getWriteBufferLowWatermarkBytes() <= 0
                || performance.getWriteBufferHighWatermarkBytes() <= performance.getWriteBufferLowWatermarkBytes()
                || performance.getDownloadNoProgressTimeoutSeconds() <= 0
                || performance.getDownloadMinimumBytesPerSecond() <= 0
                || performance.getDirectMemoryHighWatermark() <= 0
                || performance.getDirectMemoryHighWatermark() >= 0.90d || performance.getDirectMemoryLowWatermark() < 0
                || performance.getDirectMemoryLowWatermark() >= performance.getDirectMemoryHighWatermark()
                || performance.getMaxConnections() <= 0 || performance.getPendingAcquireTimeoutSeconds() <= 0
                || performance.getPendingAcquireMaxCount() < 0 || performance.getOutboundRetryBackoffMillis() <= 0
                || performance.getMaxRetries() < 0 || performance.getMaxRetries() > 2
                || performance.getOutboundRetryMaxBackoffMillis() < performance.getOutboundRetryBackoffMillis()
                || performance.getOutboundMaxIdleSeconds() <= 0 || performance.getOutboundMaxLifeMinutes() <= 0
                || performance.getOutboundEvictSeconds() <= 0 || performance.getMaxProducerCacheSize() <= 0
                || performance.getCacheSize() <= 0 || performance.getCacheExpireMs() <= 0
                || performance.getSyncIntervalSeconds() <= 0 || performance.getStartupDelaySeconds() < 0
                || performance.getTimestampToleranceMinutes() <= 0) {
            throw new IllegalArgumentException("bus.vortex performance limits and timeouts are invalid");
        }
        long logicalBufferedBytes = Math.addExact(
                Math.addExact(performance.getRequestBufferBudgetBytes(), performance.getResponseBufferBudgetBytes()),
                performance.getTransformBufferBudgetBytes());
        long heapSafeBufferedBytes = Runtime.getRuntime().maxMemory() / 16L;
        if (logicalBufferedBytes > heapSafeBufferedBytes) {
            throw new IllegalArgumentException(
                    "bus.vortex logical buffered-byte budgets must not exceed 1/16 of the JVM maximum heap ("
                            + heapSafeBufferedBytes + " bytes)");
        }
        if (assets == null || assets.getIncrementalRefreshIntervalSeconds() <= 0
                || assets.getFullCalibrationIntervalSeconds() <= 0 || assets.getModifiedOverlapMs() <= 0
                || assets.getRefreshStartupDelaySeconds() < 0) {
            throw new IllegalArgumentException("bus.vortex.assets refresh limits are invalid");
        }
    }

    /**
     * Validates the private listener and any configured bootstrap upstream.
     */
    private void validateSdc() {
        if (internalPort < 0 || internalPort > 65535 || internalPort == port) {
            throw new IllegalArgumentException("bus.vortex.internal-port must be zero or a distinct port in 1..65535");
        }
        if (internalPort > 0 && (internalHost == null || internalHost.isBlank() || internalPath == null)) {
            throw new IllegalArgumentException("bus.vortex.internal-host and internal-path are required for private SDC");
        }
        if (internalPath != null && (!isLiteralPath(internalPath)
                || !internalPath.startsWith(Args.SDC_PATH_PREFIX + "/"))) {
            throw new IllegalArgumentException("bus.vortex.internal-path must be a literal path under /router/sdc");
        }
        Sdc.Bootstrap bootstrap = sdc.getBootstrap();
        if (bootstrap == null) {
            return;
        }
        if (bootstrap.getMethod() == null || bootstrap.getMethod().isBlank()) {
            throw new IllegalArgumentException("bus.vortex.sdc.bootstrap.method is required");
        }
        URI endpoint = bootstrap.getEndpoint();
        String operation = bootstrap.getOperationPath();
        if (endpoint == null || endpoint.getHost() == null || endpoint.getUserInfo() != null
                || endpoint.getRawQuery() != null || endpoint.getRawFragment() != null
                || !("http".equals(endpoint.getScheme()) || "https".equals(endpoint.getScheme()))
                || endpoint.getPort() == 0 || endpoint.getPort() > 65535
                || !isLiteralPath(operation) || !isLiteralPath(endpoint.getRawPath())
                || !endpoint.getRawPath().endsWith(operation)) {
            throw new IllegalArgumentException(
                    "bus.vortex.sdc.bootstrap.endpoint must be an HTTP(S) URL ending in operation-path");
        }
    }

    /**
     * Checks for an exact path with no encoded characters, patterns, or dot segments.
     *
     * @param path configured path
     * @return true for a literal, normalized path
     */
    private static boolean isLiteralPath(String path) {
        return path != null && path.matches("(/[A-Za-z0-9._~-]+)+")
                && Arrays.stream(path.split("/")).noneMatch(segment -> ".".equals(segment) || "..".equals(segment));
    }

    /**
     * Configuration for SDC routes that are available before the registry is loaded.
     */
    @Getter
    @Setter
    public static class Sdc {

        /**
         * Optional bootstrap route.
         */
        private Bootstrap bootstrap;

        /**
         * Upstream coordinates for the private bootstrap operation.
         */
        @Getter
        @Setter
        public static class Bootstrap {

            /**
             * SDC method reserved for bootstrap requests.
             */
            private String method;

            /**
             * Complete upstream URI including any deployment prefix and the operation path.
             */
            private URI endpoint;

            /**
             * Operation path used for service identity and private egress policy.
             */
            private String operationPath;

        }
    }

    /**
     * Asset registry refresh settings.
     *
     * @author Kimi Liu
     */
    @Getter
    @Setter
    public static class Assets {

        /**
         * Whether periodic asset registry refresh is enabled.
         */
        private boolean refreshEnabled = true;

        /**
         * Incremental refresh interval in seconds.
         */
        private int incrementalRefreshIntervalSeconds = 60;

        /**
         * Full calibration refresh interval in seconds.
         */
        private int fullCalibrationIntervalSeconds = 600;

        /**
         * Modified-time overlap window in milliseconds for incremental refresh queries.
         */
        private long modifiedOverlapMs = 3000L;

        /**
         * Whether to run full calibration after startup.
         */
        private boolean fullCalibrationOnStartup = true;

        /**
         * Startup delay before asset registry refresh begins, in seconds.
         */
        private int refreshStartupDelaySeconds = 10;

        /**
         * Registry method used to resolve public slug forwarding assets. Blank disables registry-backed slug forwarding.
         */
        private String slugMethod;

        /**
         * Initializes asset refresh settings with refresh enabled and the default interval.
         */
        public Assets() {
            // No initialization required.
        }

    }

}
