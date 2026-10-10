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
import java.util.Locale;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable bus-metrics properties with structured host and exporter settings.
 *
 * @author Kimi Liu
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.METRICS)
public class MetricsProperties {

    /**
     * Whether metrics auto-configuration is enabled.
     */
    private final boolean enabled;
    /**
     * Selected provider key.
     */
    private final String provider;
    /**
     * Whether JVM metrics are enabled.
     */
    private final boolean jvm;
    /**
     * Whether HTTP request metrics are enabled.
     */
    private final boolean http;
    /**
     * Scrape endpoint settings.
     */
    private final Endpoint endpoint;
    /**
     * Spring startup metric settings.
     */
    private final Startup startup;
    /**
     * Metric cardinality guard settings.
     */
    private final Cardinality cardinality;
    /**
     * Service-level objective definitions.
     */
    private final List<SloDefinition> slo;
    /**
     * Native rate-window settings.
     */
    private final RateWindow rateWindow;
    /**
     * Cortex snapshot export settings.
     */
    private final Cortex cortex;
    /**
     * Host metric settings.
     */
    private final Host host;

    /**
     * Creates the complete immutable binding model.
     *
     * @param enabled     whether metrics auto-configuration is enabled
     * @param provider    selected provider key
     * @param jvm         whether JVM metrics are enabled
     * @param http        whether HTTP request metrics are enabled
     * @param endpoint    scrape endpoint settings
     * @param startup     Spring startup metric settings
     * @param cardinality metric cardinality guard settings
     * @param slo         service-level objective definitions
     * @param rateWindow  native rate-window settings
     * @param cortex      Cortex snapshot export settings
     * @param host        host metric settings
     */
    @ConstructorBinding
    public MetricsProperties(@DefaultValue("false") boolean enabled, @DefaultValue("native") String provider,
            @DefaultValue("true") boolean jvm, @DefaultValue("true") boolean http, @DefaultValue Endpoint endpoint,
            @DefaultValue Startup startup, @DefaultValue Cardinality cardinality, @DefaultValue List<SloDefinition> slo,
            @DefaultValue RateWindow rateWindow, @DefaultValue Cortex cortex, @DefaultValue Host host) {
        this.enabled = enabled;
        this.provider = provider == null ? "native" : provider;
        this.jvm = jvm;
        this.http = http;
        this.endpoint = endpoint == null ? new Endpoint() : endpoint;
        this.startup = startup == null ? new Startup() : startup;
        this.cardinality = cardinality == null ? new Cardinality() : cardinality;
        this.slo = slo == null ? List.of() : List.copyOf(slo);
        this.rateWindow = rateWindow == null ? new RateWindow() : rateWindow;
        this.cortex = cortex == null ? new Cortex() : cortex;
        this.host = host == null ? new Host() : host;
    }

    /**
     * Requires a positive duration.
     *
     * @param value duration to validate
     * @param name  configuration key suffix
     */
    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("bus.metrics." + name + " must be greater than zero");
        }
    }

    /**
     * Validates the bounded host snapshot cache interval.
     *
     * @param value cache interval
     * @param name  configuration key suffix
     * @return validated interval
     */
    private static Duration hostTtl(Duration value, String name) {
        requirePositive(value, name);
        if (value.compareTo(Duration.ofMillis(100)) < 0 || value.compareTo(Duration.ofSeconds(60)) > 0) {
            throw new IllegalArgumentException("bus.metrics." + name + " must be between 100ms and 60s");
        }
        return value;
    }

    /**
     * Validates an absolute servlet mapping path.
     *
     * @param value configured path
     * @param name  complete configuration key
     * @return validated path
     */
    private static String validPath(String value, String name) {
        if (value == null || value.isBlank() || !value.startsWith("/")) {
            throw new IllegalArgumentException(name + " must be an absolute non-blank path");
        }
        return value;
    }

    /**
     * Scrape endpoint options.
     *
     * @param enabled whether the scrape endpoint is enabled
     * @param path    absolute servlet mapping path
     */
    public record Endpoint(@DefaultValue("false") boolean enabled, @DefaultValue("/metricz") String path) {

        /**
         * Creates disabled endpoint defaults with legacy path fallback.
         */
        public Endpoint() {
            this(false, "/metricz");
        }

        /**
         * Creates endpoint settings with the default path.
         *
         * @param enabled whether the scrape endpoint is enabled
         */
        public Endpoint(boolean enabled) {
            this(enabled, "/metricz");
        }

        /**
         * Validates an explicitly configured endpoint path.
         *
         * @throws IllegalArgumentException if the endpoint path is blank or not absolute
         */
        @ConstructorBinding
        public Endpoint {
            path = validPath(path == null ? "/metricz" : path, "bus.metrics.endpoint.path");
        }
    }

    /**
     * Spring Boot startup metric options.
     *
     * @param enabled whether startup metrics are enabled
     */
    public record Startup(@DefaultValue("false") boolean enabled) {

        /**
         * Creates disabled defaults.
         */
        public Startup() {
            this(false);
        }

        /**
         * Selects the canonical constructor for nested property binding.
         */
        @ConstructorBinding
        public Startup {
            // No initialization required.
        }
    }

    /**
     * Cardinality guard options.
     *
     * @param defaultMax default maximum distinct values per guarded key
     * @param denyList   attribute keys that must always be denied
     * @param rules      explicit per-key cardinality rules
     */
    public record Cardinality(@DefaultValue("100") int defaultMax, @DefaultValue({
            "user_id", "trace_id", "request_id" }) List<String> denyList, @DefaultValue List<CardinalityRule> rules) {

        /**
         * Creates cardinality defaults.
         */
        public Cardinality() {
            this(100, List.of("user_id", "trace_id", "request_id"), List.of());
        }

        /**
         * Validates and copies cardinality settings.
         *
         * @throws IllegalArgumentException if the default maximum is not positive
         */
        @ConstructorBinding
        public Cardinality {
            if (defaultMax <= 0)
                throw new IllegalArgumentException("bus.metrics.cardinality.default-max must be positive");
            denyList = denyList == null ? List.of() : List.copyOf(denyList);
            rules = rules == null ? List.of() : List.copyOf(rules);
        }

        /**
         * Returns the default maximum distinct values.
         *
         * @return default maximum
         */
        public int getDefaultMax() {
            return defaultMax;
        }

        /**
         * Returns keys that are always denied.
         *
         * @return denied keys
         */
        public List<String> getDenyList() {
            return denyList;
        }

        /**
         * Returns explicit per-key rules.
         *
         * @return per-key rules
         */
        public List<CardinalityRule> getRules() {
            return rules;
        }
    }

    /**
     * One tag-cardinality rule.
     *
     * @param tag    guarded attribute key
     * @param policy cardinality policy name
     * @param max    maximum distinct values
     */
    public record CardinalityRule(String tag, String policy, int max) {

        /**
         * Validates the rule.
         *
         * @throws IllegalArgumentException if the tag is blank or the maximum is not positive
         */
        public CardinalityRule {
            policy = policy == null ? "first-n" : policy;
            if (tag == null || tag.isBlank() || max <= 0) {
                throw new IllegalArgumentException("Metrics cardinality rule requires tag and positive max");
            }
        }

        /**
         * Returns the guarded attribute key.
         *
         * @return tag key
         */
        public String getTag() {
            return tag;
        }

        /**
         * Returns the cardinality policy name.
         *
         * @return policy
         */
        public String getPolicy() {
            return policy;
        }

        /**
         * Returns the maximum distinct values.
         *
         * @return maximum
         */
        public int getMax() {
            return max;
        }
    }

    /**
     * One service-level objective definition.
     *
     * @param name          objective name
     * @param metric        source metric name
     * @param type          objective type
     * @param thresholdMs   latency threshold in milliseconds
     * @param percentile    requested percentile as a ratio
     * @param target        target success ratio
     * @param windowMinutes evaluation window in minutes
     */
    public record SloDefinition(String name, String metric, String type, long thresholdMs, double percentile,
            double target, int windowMinutes) {

        /**
         * Validates ratios and time window.
         *
         * @throws IllegalArgumentException if a ratio, threshold, or window is outside its supported bounds
         */
        public SloDefinition {
            if (percentile < 0 || percentile > 1 || target < 0 || target > 1 || thresholdMs <= 0 || windowMinutes <= 0)
                throw new IllegalArgumentException("Invalid metrics SLO bounds");
        }
    }

    /**
     * EWMA collection window options.
     *
     * @param enabled      whether periodic rate updates are enabled
     * @param tickInterval update interval
     */
    public record RateWindow(@DefaultValue("true") boolean enabled, @DefaultValue("5s") Duration tickInterval) {

        /**
         * Creates defaults.
         */
        public RateWindow() {
            this(true, Duration.ofSeconds(5));
        }

        /**
         * Validates the interval.
         *
         * @throws IllegalArgumentException if the tick interval is null, zero, or negative
         */
        @ConstructorBinding
        public RateWindow {
            requirePositive(tickInterval, "rate-window.tick-interval");
        }
    }

    /**
     * Cortex export options.
     *
     * @param enabled   whether Cortex export is enabled
     * @param interval  export interval
     * @param space     CacheX space name
     * @param serviceId service identifier, or blank to use the runtime default
     */
    public record Cortex(@DefaultValue("false") boolean enabled, @DefaultValue("15s") Duration interval,
            @DefaultValue("default") String space, @DefaultValue("") String serviceId) {

        /**
         * Creates disabled defaults.
         */
        public Cortex() {
            this(false, Duration.ofSeconds(15), Normal.DEFAULT, Normal.EMPTY);
        }

        /**
         * Validates the interval.
         *
         * @throws IllegalArgumentException if the export interval is null, zero, or negative
         */
        @ConstructorBinding
        public Cortex {
            requirePositive(interval, "cortex.interval");
        }
    }

    /**
     * Structured host metric options.
     *
     * @param enabled    whether host metrics are enabled
     * @param required   whether missing host dependencies must fail startup
     * @param convention semantic convention identifier
     * @param cacheTtl   snapshot cache lifetime
     * @param categories category switches
     * @param cpu        CPU limits
     * @param process    process options
     * @param disk       disk options
     * @param filesystem filesystem options
     * @param network    network options
     */
    public record Host(@DefaultValue("true") boolean enabled, @DefaultValue("false") boolean required,
            @DefaultValue("otel-1.44.0") String convention, @DefaultValue("1s") Duration cacheTtl,
            Categories categories, Cpu cpu, Process process, Disk disk, FileSystem filesystem, Network network) {

        /**
         * Creates documented defaults.
         */
        public Host() {
            this(true, false, "otel-1.44.0", Duration.ofSeconds(1), new Categories(), new Cpu(), new Process(),
                    new Disk(), new FileSystem(), new Network());
        }

        /**
         * Validates and fills nested defaults.
         *
         * @throws IllegalArgumentException if the convention is unsupported or the cache TTL is outside its bounds
         */
        @ConstructorBinding
        public Host {
            convention = convention == null ? "otel-1.44.0" : convention.trim().toLowerCase(Locale.ROOT);
            if (!"otel-1.44.0".equals(convention)) {
                throw new IllegalArgumentException("bus.metrics.host.convention must be otel-1.44.0");
            }
            cacheTtl = hostTtl(cacheTtl == null ? Duration.ofSeconds(1) : cacheTtl, "host.cache-ttl");
            categories = categories == null ? new Categories() : categories;
            cpu = cpu == null ? new Cpu() : cpu;
            process = process == null ? new Process() : process;
            disk = disk == null ? new Disk() : disk;
            filesystem = filesystem == null ? new FileSystem() : filesystem;
            network = network == null ? new Network() : network;
        }
    }

    /**
     * Host category switches.
     *
     * @param general    whether general host metrics are enabled
     * @param cpu        whether CPU metrics are enabled
     * @param memory     whether memory metrics are enabled
     * @param paging     whether paging metrics are enabled
     * @param disk       whether disk metrics are enabled
     * @param filesystem whether filesystem metrics are enabled
     * @param network    whether network metrics are enabled
     * @param process    whether process metrics are enabled
     * @param container  whether container metrics are enabled
     */
    public record Categories(@DefaultValue("true") boolean general, @DefaultValue("true") boolean cpu,
            @DefaultValue("true") boolean memory, @DefaultValue("true") boolean paging,
            @DefaultValue("true") boolean disk, @DefaultValue("true") boolean filesystem,
            @DefaultValue("true") boolean network, @DefaultValue("true") boolean process,
            @DefaultValue("true") boolean container) {

        /**
         * Enables all documented categories.
         */
        public Categories() {
            this(true, true, true, true, true, true, true, true, true);
        }

        /**
         * Selects the canonical constructor for nested property binding.
         */
        @ConstructorBinding
        public Categories {
            // No initialization required.
        }
    }

    /**
     * CPU resource limits.
     *
     * @param maxLogicalCpus maximum logical CPUs exported as distinct series
     */
    public record Cpu(@DefaultValue("1024") int maxLogicalCpus) {

        /**
         * Creates defaults.
         */
        public Cpu() {
            this(1024);
        }

        /**
         * Validates limits.
         *
         * @throws IllegalArgumentException if the logical CPU limit is not positive
         */
        @ConstructorBinding
        public Cpu {
            if (maxLogicalCpus <= 0)
                throw new IllegalArgumentException("max-logical-cpus must be positive");
        }
    }

    /**
     * Current-process options.
     *
     * @param currentOnly         whether collection is restricted to the current process
     * @param stateCounts         whether aggregate process state counts are enabled
     * @param stateCountsCacheTtl process state-count cache lifetime
     */
    public record Process(@DefaultValue("true") boolean currentOnly, @DefaultValue("false") boolean stateCounts,
            @DefaultValue("10s") Duration stateCountsCacheTtl) {

        /**
         * Creates defaults.
         */
        public Process() {
            this(true, false, Duration.ofSeconds(10));
        }

        /**
         * Validates supported shape.
         *
         * @throws IllegalArgumentException if collection is not current-process-only or the state TTL is invalid
         */
        @ConstructorBinding
        public Process {
            if (!currentOnly)
                throw new IllegalArgumentException("bus.metrics.host.process.current-only must be true");
            stateCountsCacheTtl = hostTtl(
                    stateCountsCacheTtl == null ? Duration.ofSeconds(10) : stateCountsCacheTtl,
                    "host.process.state-counts-cache-ttl");
        }
    }

    /**
     * Disk options.
     *
     * @param includeVirtual whether virtual disks are included
     * @param maxDevices     maximum devices exported as distinct series
     */
    public record Disk(@DefaultValue("false") boolean includeVirtual, @DefaultValue("128") int maxDevices) {

        /**
         * Creates defaults.
         */
        public Disk() {
            this(false, 128);
        }

        /**
         * Validates limits.
         *
         * @throws IllegalArgumentException if the device limit is not positive
         */
        @ConstructorBinding
        public Disk {
            if (maxDevices <= 0)
                throw new IllegalArgumentException("max-devices must be positive");
        }
    }

    /**
     * Filesystem options.
     *
     * @param localOnly      whether only local filesystems are included
     * @param mountpointMode mountpoint label privacy mode
     * @param maxFilesystems maximum filesystems exported as distinct series
     */
    public record FileSystem(@DefaultValue("false") boolean localOnly, @DefaultValue("keep") String mountpointMode,
            @DefaultValue("256") int maxFilesystems) {

        /**
         * Creates defaults.
         */
        public FileSystem() {
            this(false, "keep", 256);
        }

        /**
         * Validates privacy mode and limits.
         *
         * @throws IllegalArgumentException if the privacy mode is unsupported or the filesystem limit is not positive
         */
        @ConstructorBinding
        public FileSystem {
            mountpointMode = mountpointMode == null ? "keep" : mountpointMode.trim().toLowerCase(Locale.ROOT);
            if (!List.of("keep", "hash", "drop").contains(mountpointMode)) {
                throw new IllegalArgumentException("mountpoint-mode must be keep, hash or drop");
            }
            if (maxFilesystems <= 0)
                throw new IllegalArgumentException("max-filesystems must be positive");
        }
    }

    /**
     * Network options.
     *
     * @param includeLoopback whether loopback interfaces are included
     * @param includeVirtual  whether virtual interfaces are included
     * @param connections     whether connection metrics are enabled
     * @param maxInterfaces   maximum interfaces exported as distinct series
     */
    public record Network(@DefaultValue("false") boolean includeLoopback, @DefaultValue("true") boolean includeVirtual,
            @DefaultValue("true") boolean connections, @DefaultValue("128") int maxInterfaces) {

        /**
         * Creates defaults.
         */
        public Network() {
            this(false, true, true, 128);
        }

        /**
         * Validates limits.
         *
         * @throws IllegalArgumentException if the interface limit is not positive
         */
        @ConstructorBinding
        public Network {
            if (maxInterfaces <= 0)
                throw new IllegalArgumentException("max-interfaces must be positive");
        }
    }

}
