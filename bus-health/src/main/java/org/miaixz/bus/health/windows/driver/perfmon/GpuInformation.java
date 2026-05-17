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
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

/**
 * Utility to query GPU Engine and GPU Adapter Memory performance counters. Available on Windows 10 version 1709 and
 * later.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class GpuInformation {

    /**
     * The GPU_ENGINE constant.
     */
    static final String GPU_ENGINE = "GPU Engine";

    /**
     * The GPU_ADAPTER_MEMORY constant.
     */
    static final String GPU_ADAPTER_MEMORY = "GPU Adapter Memory";

    /**
     * GPU Engine running time counter properties. Instance names have the form:
     * {@code pid_<PID>_luid_0x<HIGH>_0x<LOW>_phys_0_eng_<N>_engtype_<TYPE>}
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum GpuEngineProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element: instance filter (all instances)
        NAME("*"),
        // Running time in 100ns units (raw cumulative counter)
        RUNNING_TIME("Running Time"),
        // Total elapsed time in 100ns units (SecondValue of Running Time counter; idle = base - active)
        RUNNING_TIME_BASE("Running Time_Base");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new GpuEngineProperty instance.
         *
         * @param counter the counter
         */
        GpuEngineProperty(String counter) {
            this.counter = counter;
        }

        /**
         * Returns the counter.
         *
         * @return the get counter result
         */
        @Override
        public String getCounter() {
            return counter;
        }

    }

    /**
     * GPU Adapter Memory counter properties. Instance names have the form: {@code luid_0x<HIGH>_0x<LOW>_phys_0}
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum GpuAdapterMemoryProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element: instance filter (all instances)
        NAME("*"), DEDICATED_USAGE("Dedicated Usage"), SHARED_USAGE("Shared Usage");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new GpuAdapterMemoryProperty instance.
         *
         * @param counter the counter
         */
        GpuAdapterMemoryProperty(String counter) {
            this.counter = counter;
        }

        /**
         * Returns the counter.
         *
         * @return the get counter result
         */
        @Override
        public String getCounter() {
            return counter;
        }

    }

    /**
     * Creates a new GpuInformation instance.
     */
    private GpuInformation() {
    }

    /**
     * Queries GPU Engine running time counters for all instances.
     *
     * @return pair of instance name list and counter value map
     */
    public static Pair<List<String>, Map<GpuEngineProperty, List<Long>>> queryGpuEngineCounters() {
        return PerfCounterWildcardQuery.queryInstancesAndValuesFromPDH(GpuEngineProperty.class, GPU_ENGINE);
    }

    /**
     * Queries GPU Adapter Memory counters for all instances.
     *
     * @return pair of instance name list and counter value map
     */
    public static Pair<List<String>, Map<GpuAdapterMemoryProperty, List<Long>>> queryGpuAdapterMemoryCounters() {
        return PerfCounterWildcardQuery
                .queryInstancesAndValuesFromPDH(GpuAdapterMemoryProperty.class, GPU_ADAPTER_MEMORY);
    }

}
