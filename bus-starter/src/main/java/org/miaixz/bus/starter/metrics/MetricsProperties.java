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

import java.time.Duration;
import java.util.List;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable bus-metrics properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.METRICS)
public class MetricsProperties {

    /**
     * Whether the metrics integration is enabled.
     */
    private final boolean enabled;
    /**
     * Metrics provider selected for collection and publication.
     */
    private final String provider;
    /**
     * Whether JVM runtime metrics are collected.
     */
    private final boolean jvm;
    /**
     * Whether operating-system metrics are collected.
     */
    private final boolean system;
    /**
     * Whether health metrics are included in the metrics endpoint.
     */
    private final boolean health;
    /**
     * Whether HTTP request metrics are collected.
     */
    private final boolean http;
    /**
     * Request path on which metrics are exposed.
     */
    private final String path;
    /**
     * Metrics endpoint activation and access settings.
     */
    private final Endpoint endpoint;
    /**
     * Spring Boot startup metric settings.
     */
    private final Startup startup;
    /**
     * Limits applied to metric tag cardinality.
     */
    private final Cardinality cardinality;
    /**
     * Service-level objectives used to configure metric histograms.
     */
    private final List<SloDefinition> slo;
    /**
     * Rolling window used for rate calculations.
     */
    private final RateWindow rateWindow;
    /**
     * Cortex export settings for remote metric publication.
     */
    private final Cortex cortex;

    /**
     * Creates metrics properties with stable defaults.
     *
     * @param enabled     whether the feature is enabled
     * @param provider    provider identifier or configuration
     * @param jvm         JVM metric settings
     * @param system      system metric settings
     * @param health      health metric settings
     * @param http        HTTP metric settings
     * @param path        configured path
     * @param endpoint    endpoint options
     * @param startup     Spring Boot startup metric settings
     * @param cardinality metric cardinality settings
     * @param slo         service-level objective settings
     * @param rateWindow  rate window
     * @param cortex      Cortex export settings
     */
    public MetricsProperties(@DefaultValue("false") boolean enabled, @DefaultValue("native") String provider,
            @DefaultValue("true") boolean jvm, @DefaultValue("true") boolean system,
            @DefaultValue("true") boolean health, @DefaultValue("true") boolean http,
            @DefaultValue("/metricz") String path, @DefaultValue Endpoint endpoint, @DefaultValue Startup startup,
            @DefaultValue Cardinality cardinality, @DefaultValue List<SloDefinition> slo,
            @DefaultValue RateWindow rateWindow, @DefaultValue Cortex cortex) {
        this.enabled = enabled;
        this.provider = provider;
        this.jvm = jvm;
        this.system = system;
        this.health = health;
        this.http = http;
        this.path = path;
        this.endpoint = endpoint == null ? new Endpoint() : endpoint;
        this.startup = startup == null ? new Startup() : startup;
        this.cardinality = cardinality == null ? new Cardinality() : cardinality;
        this.slo = slo == null ? List.of() : List.copyOf(slo);
        this.rateWindow = rateWindow == null ? new RateWindow() : rateWindow;
        this.cortex = cortex == null ? new Cortex() : cortex;
    }

    /**
     * Scrape endpoint options.
     *
     * @param enabled whether the feature is enabled
     */
    public record Endpoint(boolean enabled) {

        /**
         * Creates disabled endpoint defaults.
         */
        public Endpoint() {
            this(false);
        }
    }

    /**
     * Spring Boot startup metric options.
     *
     * @param enabled whether startup metrics are collected and published
     */
    public record Startup(@DefaultValue("false") boolean enabled) {

        /**
         * Creates disabled startup metric defaults.
         */
        public Startup() {
            this(false);
        }
    }

    /**
     * Cardinality guard options.
     *
     * @param defaultMax default maximum value
     * @param denyList   denied metric names
     * @param rules      configured rules
     */
    public record Cardinality(int defaultMax, List<String> denyList, List<CardinalityRule> rules) {

        /**
         * Creates cardinality defaults.
         */
        public Cardinality() {
            this(100, List.of("user_id", "trace_id", "request_id"), List.of());
        }

        /**
         * Validates cardinality limits and copies lists.
         */
        public Cardinality {
            if (defaultMax <= 0) {
                throw new IllegalArgumentException("bus.metrics.cardinality.default-max must be positive");
            }
            denyList = denyList == null ? List.of() : List.copyOf(denyList);
            rules = rules == null ? List.of() : List.copyOf(rules);
        }

        /**
         * Exposes the fallback cardinality limit applied when no rule matches.
         *
         * @return default maximum
         */
        public int getDefaultMax() {
            return defaultMax;
        }

        /**
         * Returns tag names that are rejected before metric publication.
         *
         * @return denied tag keys
         */
        public List<String> getDenyList() {
            return denyList;
        }

        /**
         * Returns the immutable per-tag cardinality rules.
         *
         * @return cardinality rules
         */
        public List<CardinalityRule> getRules() {
            return rules;
        }
    }

    /**
     * One tag-cardinality rule.
     *
     * @param tag    metric tag name
     * @param policy cardinality enforcement policy
     * @param max    maximum allowed value
     */
    public record CardinalityRule(String tag, String policy, int max) {

        /**
         * Validates one cardinality rule.
         */
        public CardinalityRule {
            policy = policy == null ? "first-n" : policy;
            if (tag == null || tag.isBlank() || max <= 0) {
                throw new IllegalArgumentException("Metrics cardinality rule requires tag and positive max");
            }
        }

        /**
         * Exposes the metric tag name governed by this cardinality rule.
         *
         * @return tag key
         */
        public String getTag() {
            return tag;
        }

        /**
         * Exposes the action taken after this tag exceeds its cardinality limit.
         *
         * @return policy name
         */
        public String getPolicy() {
            return policy;
        }

        /**
         * Exposes the maximum distinct values permitted for this tag.
         *
         * @return maximum cardinality
         */
        public int getMax() {
            return max;
        }
    }

    /**
     * One SLO definition.
     *
     * @param name          logical name
     * @param metric        metric identifier or sample
     * @param type          SLO comparison or aggregation type
     * @param thresholdMs   threshold ms
     * @param percentile    configured percentile
     * @param target        target metric value
     * @param windowMinutes window minutes
     */
    public record SloDefinition(String name, String metric, String type, long thresholdMs, double percentile,
            double target, int windowMinutes) {

        /**
         * Validates ratios and time window.
         */
        public SloDefinition {
            if (percentile < 0 || percentile > 1 || target < 0 || target > 1 || thresholdMs <= 0
                    || windowMinutes <= 0) {
                throw new IllegalArgumentException("Invalid metrics SLO bounds");
            }
        }
    }

    /**
     * EWMA collection window options.
     *
     * @param enabled      whether the feature is enabled
     * @param tickInterval tick interval
     */
    public record RateWindow(boolean enabled, Duration tickInterval) {

        /**
         * Creates rate-window defaults.
         */
        public RateWindow() {
            this(true, Duration.ofSeconds(5));
        }

        /**
         * Validates collection interval.
         */
        public RateWindow {
            requirePositive(tickInterval, "rate-window.tick-interval");
        }
    }

    /**
     * Cortex metrics export options.
     *
     * @param enabled    whether the feature is enabled
     * @param interval   metric export interval
     * @param serverAddr server addr
     * @param namespace  logical registry namespace
     * @param serviceId  service id
     */
    public record Cortex(boolean enabled, Duration interval, String serverAddr, String namespace, String serviceId) {

        /**
         * Creates Cortex export defaults.
         */
        public Cortex() {
            this(false, Duration.ofSeconds(15), Normal.EMPTY, Normal.DEFAULT, Normal.EMPTY);
        }

        /**
         * Validates export interval.
         */
        public Cortex {
            requirePositive(interval, "cortex.interval");
        }
    }

    /**
     * Validates a required positive duration property.
     *
     * @param value configured duration
     * @param name  configuration property suffix
     */
    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("bus.metrics." + name + " must be greater than zero");
        }
    }

}
