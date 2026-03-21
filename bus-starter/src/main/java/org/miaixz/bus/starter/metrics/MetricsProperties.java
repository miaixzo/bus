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
package org.miaixz.bus.starter.metrics;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for bus-metrics. Bound to prefix {@code bus.metrics}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.METRICS)
public class MetricsProperties {

    /**
     * Provider selection: "native" (default) or "micrometer".
     */
    private String provider = "native";

    /**
     * Enable JVM metrics (memory, GC, threads). Default: true.
     */
    private boolean jvm = true;

    /**
     * Enable system metrics (CPU, uptime). Default: true.
     */
    private boolean system = true;

    /**
     * Enable bus-health integration for hardware-accurate system metrics (JNA-backed). When true and bus-health is on
     * the classpath, replaces JvmMetrics+SystemMetrics with HealthMetrics (physical CPU ticks, real RAM, disk I/O,
     * network stats). Default: true (auto-detected).
     */
    private boolean health = true;

    /**
     * Enable HTTP request interceptor auto-registration. Default: true.
     */
    private boolean http = true;

    /**
     * Expose /metricz endpoint. Default: true.
     */
    private boolean endpoint = true;

    /**
     * Path for the metrics scrape endpoint. Default: /metricz.
     */
    private String path = "/metricz";

    /**
     * Cardinality guard configuration.
     */
    private Cardinality cardinality = new Cardinality();

    /**
     * SLO definitions.
     */
    private List<SloDefinition> slo = new ArrayList<>();

    /**
     * EWMA tick scheduler config.
     */
    private RateWindow rateWindow = new RateWindow();

    /**
     * cortex integration config.
     */
    private Cortex cortex = new Cortex();

    @Getter
    @Setter
    public static class Cardinality {

        /**
         * Default max distinct values per tag key (for unregistered keys).
         */
        private int defaultMax = 100;
        /**
         * Tag keys that are always denied.
         */
        private List<String> denyList = List.of("user_id", "trace_id", "request_id");
        /**
         * Per-key rules.
         */
        private List<CardinalityRule> rules = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class CardinalityRule {

        /**
         * Tag key this rule applies to.
         */
        private String tag;
        /**
         * Policy: "first-n", "top-n", or "deny".
         */
        private String policy = "first-n";
        /**
         * Max distinct values (for first-n and top-n).
         */
        private int max = 100;
    }

    @Getter
    @Setter
    public static class SloDefinition {

        private String name;
        private String metric;
        /**
         * "latency" or "availability".
         */
        private String type = "latency";
        private long thresholdMs = 300;
        private double percentile = 0.99;
        private double target = 0.999;
        private int windowMinutes = 30;
    }

    @Getter
    @Setter
    public static class RateWindow {

        private boolean enabled = true;
        private int tickIntervalSeconds = 5;
    }

    @Getter
    @Setter
    public static class Cortex {

        private boolean enabled = false;
        private int intervalSeconds = 15;
        private String serverAddr = "";
        private String namespace = "default";
        private String serviceId = "";
    }

}
