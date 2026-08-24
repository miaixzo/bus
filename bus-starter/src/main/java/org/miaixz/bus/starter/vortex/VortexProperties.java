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
     * Initializes Vortex properties with the documented transport and asset-refresh defaults.
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
                || assets.getRefreshStartupDelaySeconds() < 0 || assets.getSlugMethod() == null
                || assets.getSlugMethod().isBlank()) {
            throw new IllegalArgumentException("bus.vortex.assets refresh limits and slug-method are invalid");
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
         * Registry method used to resolve public slug forwarding assets.
         */
        private String slugMethod = Args.VORTEX_SLUG_GET;

        /**
         * Initializes asset refresh settings with refresh enabled and the default interval.
         */
        public Assets() {
            // No initialization required.
        }

    }

}
