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
package org.miaixz.bus.metrics.builtin;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import org.miaixz.bus.metrics.Builder;

/**
 * Immutable configuration passed from an integration layer to host metric binders.
 *
 * @param required             whether collection failures must fail startup
 * @param convention           locked semantic convention identifier
 * @param cacheTtl             standard snapshot cache interval
 * @param processStateCacheTtl process-state inventory cache interval
 * @param categories           enabled host categories
 * @param cpu                  CPU collection limits
 * @param process              process collection options
 * @param disk                 disk collection options
 * @param fileSystem           filesystem collection options
 * @param network              network collection options
 * @author Kimi Liu
 */
public record HostMetricsOptions(boolean required, String convention, Duration cacheTtl, Duration processStateCacheTtl,
        Categories categories, Cpu cpu, Process process, Disk disk, FileSystem fileSystem, Network network) {

    /**
     * Validates and canonicalizes all options.
     */
    public HostMetricsOptions {
        convention = normalized(convention);
        if (!"OTEL_1_44".equals(convention)) {
            throw new IllegalArgumentException("Host metric convention must be OTEL_1_44");
        }
        cacheTtl = checkedTtl(cacheTtl, "Host cache TTL");
        processStateCacheTtl = checkedTtl(processStateCacheTtl, "Process-state cache TTL");
        categories = Objects.requireNonNull(categories, "Host categories must not be null");
        cpu = Objects.requireNonNull(cpu, "CPU options must not be null");
        process = Objects.requireNonNull(process, "Process options must not be null");
        disk = Objects.requireNonNull(disk, "Disk options must not be null");
        fileSystem = Objects.requireNonNull(fileSystem, "Filesystem options must not be null");
        network = Objects.requireNonNull(network, "Network options must not be null");
    }

    /**
     * Creates the documented OpenTelemetry 1.44 defaults.
     *
     * @return default host metric options
     */
    public static HostMetricsOptions defaults() {
        return new HostMetricsOptions(false, "OTEL_1_44", Duration.ofMillis(Builder.HOST_CACHE_TTL_MILLIS),
                Duration.ofMillis(Builder.HOST_PROCESS_STATE_CACHE_TTL_MILLIS),
                new Categories(true, true, true, true, true, true, true, true, true),
                new Cpu(Builder.HOST_MAX_LOGICAL_CPUS), new Process(true, false),
                new Disk(false, Builder.HOST_MAX_DEVICES), new FileSystem(false, "KEEP", Builder.HOST_MAX_FILE_SYSTEMS),
                new Network(false, true, true, Builder.HOST_MAX_INTERFACES));
    }

    /**
     * Validates a host snapshot cache interval.
     *
     * @param value cache interval
     * @param label diagnostic label
     * @return validated interval
     */
    private static Duration checkedTtl(Duration value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.compareTo(Duration.ofMillis(Builder.HOST_CACHE_TTL_MIN_MILLIS)) < 0
                || value.compareTo(Duration.ofMillis(Builder.HOST_CACHE_TTL_MAX_MILLIS)) > 0) {
            throw new IllegalArgumentException(label + " must be between 100ms and 60s");
        }
        return value;
    }

    /**
     * Normalizes a convention identifier for strict comparison.
     *
     * @param value convention identifier
     * @return normalized identifier
     */
    private static String normalized(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace('.', '_');
    }

    /**
     * Requires a positive resource limit.
     *
     * @param value resource limit
     * @param label diagnostic label
     */
    private static void positive(int value, String label) {
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be positive");
        }
    }

    /**
     * Category switches.
     *
     * @param general    general operating-system metrics
     * @param cpu        CPU metrics
     * @param memory     physical-memory metrics
     * @param paging     paging metrics
     * @param disk       disk metrics
     * @param filesystem filesystem metrics
     * @param network    network metrics
     * @param process    current-process metrics
     * @param container  container metrics
     */
    public record Categories(boolean general, boolean cpu, boolean memory, boolean paging, boolean disk,
            boolean filesystem, boolean network, boolean process, boolean container) {
    }

    /**
     * CPU inventory options.
     *
     * @param maxLogicalCpus maximum logical processors to expose
     */
    public record Cpu(int maxLogicalCpus) {

        /**
         * Validates the resource limit.
         */
        public Cpu {
            positive(maxLogicalCpus, "Maximum logical CPU count");
        }
    }

    /**
     * Process inventory options.
     *
     * @param currentOnly whether collection is limited to the current process
     * @param stateCounts whether system-wide process-state counts are enabled
     */
    public record Process(boolean currentOnly, boolean stateCounts) {

        /**
         * Rejects unsupported all-process collection.
         */
        public Process {
            if (!currentOnly) {
                throw new IllegalArgumentException("Only current-process metrics are supported");
            }
        }
    }

    /**
     * Disk inventory options.
     *
     * @param includeVirtual whether virtual devices are included
     * @param maxDevices     maximum devices to expose
     */
    public record Disk(boolean includeVirtual, int maxDevices) {

        /**
         * Validates the resource limit.
         */
        public Disk {
            positive(maxDevices, "Maximum disk count");
        }
    }

    /**
     * Filesystem inventory options.
     *
     * @param localOnly      whether collection is limited to local filesystems
     * @param mountpointMode mountpoint exposure policy
     * @param maxFileSystems maximum filesystems to expose
     */
    public record FileSystem(boolean localOnly, String mountpointMode, int maxFileSystems) {

        /**
         * Validates and canonicalizes filesystem options.
         */
        public FileSystem {
            mountpointMode = mountpointMode == null ? "" : mountpointMode.trim().toUpperCase(Locale.ROOT);
            if (!"KEEP".equals(mountpointMode) && !"HASH".equals(mountpointMode) && !"DROP".equals(mountpointMode)) {
                throw new IllegalArgumentException("Filesystem mountpoint mode must be KEEP, HASH or DROP");
            }
            positive(maxFileSystems, "Maximum filesystem count");
        }
    }

    /**
     * Network inventory options.
     *
     * @param includeLoopback whether loopback interfaces are included
     * @param includeVirtual  whether virtual interfaces are included
     * @param connections     whether connection-state metrics are enabled
     * @param maxInterfaces   maximum interfaces to expose
     */
    public record Network(boolean includeLoopback, boolean includeVirtual, boolean connections, int maxInterfaces) {

        /**
         * Validates the resource limit.
         */
        public Network {
            positive(maxInterfaces, "Maximum network-interface count");
        }
    }

}
