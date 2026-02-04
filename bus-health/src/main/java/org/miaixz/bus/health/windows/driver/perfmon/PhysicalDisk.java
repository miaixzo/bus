/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

/**
 * Utility to query PhysicalDisk performance counter
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PhysicalDisk {

    /**
     * Returns physical disk performance counters.
     *
     * @return Performance Counters for physical disks.
     */
    public static Pair<List<String>, Map<PhysicalDiskProperty, List<Long>>> queryDiskCounters() {
        if (PerfmonDisabled.PERF_DISK_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                PhysicalDiskProperty.class,
                PerfmonConsts.PHYSICAL_DISK,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_DISK_PHYSICAL_DISK_WHERE_NAME_NOT_TOTAL);
    }

    /**
     * Physical Disk performance counters.
     */
    public enum PhysicalDiskProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCE),
        // Remaining elements define counters
        DISKREADSPERSEC("Disk Reads/sec"), //
        DISKREADBYTESPERSEC("Disk Read Bytes/sec"), //
        DISKWRITESPERSEC("Disk Writes/sec"), //
        DISKWRITEBYTESPERSEC("Disk Write Bytes/sec"), //
        CURRENTDISKQUEUELENGTH("Current Disk Queue Length"), //
        PERCENTDISKTIME("% Disk Time");

        private final String counter;

        PhysicalDiskProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

}
