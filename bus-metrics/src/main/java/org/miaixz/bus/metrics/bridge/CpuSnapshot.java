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
package org.miaixz.bus.metrics.bridge;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable CPU topology, cumulative time and frequency snapshot.
 *
 * @param physicalCount physical processor count
 * @param logicalCount  logical processor count
 * @param times         cumulative CPU-mode times
 * @param frequencies   current logical-processor frequencies
 * @author Kimi Liu
 */
public record CpuSnapshot(long physicalCount, long logicalCount, List<CpuTime> times, List<CpuFrequency> frequencies) {

    /**
     * Validates and orders the snapshot.
     */
    public CpuSnapshot {
        if (physicalCount < 0 || logicalCount < 0) {
            throw new IllegalArgumentException("CPU counts must not be negative");
        }
        Objects.requireNonNull(times, "CPU times must not be null");
        Objects.requireNonNull(frequencies, "CPU frequencies must not be null");
        times = times.stream().sorted(Comparator.comparingLong(CpuTime::logicalNumber).thenComparing(CpuTime::mode))
                .toList();
        frequencies = frequencies.stream().sorted(Comparator.comparingLong(CpuFrequency::logicalNumber)).toList();
    }

    /**
     * One cumulative CPU-mode time.
     *
     * @param logicalNumber logical processor number
     * @param mode          normalized CPU mode
     * @param seconds       cumulative time in seconds
     */
    public record CpuTime(long logicalNumber, String mode, double seconds) {

        /**
         * CPU modes exported by the host metrics convention.
         */
        private static final Set<String> MODES = Set
                .of("user", "nice", "system", "idle", "iowait", "interrupt", "steal");

        /**
         * Validates the CPU time.
         */
        public CpuTime {
            if (logicalNumber < 0 || !MODES.contains(mode) || !Double.isFinite(seconds) || seconds < 0) {
                throw new IllegalArgumentException("Invalid CPU time");
            }
        }
    }

    /**
     * One current logical-CPU frequency.
     *
     * @param logicalNumber logical processor number
     * @param hertz         current frequency in hertz
     */
    public record CpuFrequency(long logicalNumber, long hertz) {

        /**
         * Validates the CPU frequency.
         */
        public CpuFrequency {
            if (logicalNumber < 0 || hertz < 0) {
                throw new IllegalArgumentException("Invalid CPU frequency");
            }
        }
    }

}
