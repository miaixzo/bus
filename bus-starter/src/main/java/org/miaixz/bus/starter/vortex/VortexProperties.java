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
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.magic.Performance;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Vortex routing gateway.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(GeniusBuilder.VORTEX)
public class VortexProperties {

    /**
     * Creates an empty Vortex configuration property holder.
     */
    public VortexProperties() {

    }

    /**
     * The service port, specifying the port number the server listens on.
     */
    private int port;

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
     * Asset registry refresh settings.
     *
     * @author Kimi Liu
     * @since Java 21+
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

    }

}
