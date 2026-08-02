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
 * @since Java 21+
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(GeniusBuilder.VORTEX)
public final class VortexProperties {

    /**
     * Initializes Vortex properties with the documented transport and asset-refresh defaults.
     */
    public VortexProperties() {
        // No initialization required.
    }

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
     * Performance optimization settings for request body processing and connection pooling.
     * <p>
     * These settings allow fine-tuning of memory usage and throughput trade-offs.
     */
    private Performance performance = Performance.builder().build();

    /**
     * Asset registry runtime configuration.
     */
    private Assets assets = new Assets();

    /**
     * Validates network, timeout, connection, cache, and refresh limits after binding.
     */
    public void validate() {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("bus.vortex.port must be in 1..65535");
        }
        if (performance == null || performance.getStreamingRequestThreshold() <= 0
                || performance.getMaxRequestSize() <= 0 || performance.getMaxMultipartRequestSize() <= 0
                || performance.getStreamingRequestThreshold() > performance.getMaxRequestSize()
                || performance.getMaxConnections() <= 0 || performance.getPendingAcquireTimeoutSeconds() <= 0
                || performance.getPendingAcquireMaxCount() < 0 || performance.getOutboundRetryBackoffMillis() <= 0
                || performance.getOutboundRetryMaxBackoffMillis() < performance.getOutboundRetryBackoffMillis()
                || performance.getOutboundMaxIdleSeconds() <= 0 || performance.getOutboundMaxLifeMinutes() <= 0
                || performance.getOutboundEvictSeconds() <= 0 || performance.getMaxProducerCacheSize() <= 0
                || performance.getCacheSize() <= 0 || performance.getCacheExpireMs() <= 0
                || performance.getSyncIntervalSeconds() <= 0 || performance.getStartupDelaySeconds() < 0
                || performance.getTimestampToleranceMinutes() <= 0) {
            throw new IllegalArgumentException("bus.vortex performance limits and timeouts are invalid");
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
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Assets {

        /**
         * Initializes asset refresh settings with refresh enabled and the default interval.
         */
        public Assets() {
            // No initialization required.
        }

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

    }

}
