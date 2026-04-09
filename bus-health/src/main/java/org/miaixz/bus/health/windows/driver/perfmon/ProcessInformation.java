/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

/**
 * Utility to query Process Information performance counter
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class ProcessInformation {

    /**
     * Returns process counters.
     *
     * @return Process counters for each process.
     */
    public static Pair<List<String>, Map<ProcessPerformanceProperty, List<Long>>> queryProcessCounters() {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                ProcessPerformanceProperty.class,
                PerfmonConsts.PROCESS,
                PerfmonConsts.WIN32_PERFPROC_PROCESS_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * Returns handle counters
     *
     * @return Process handle counters for each process.
     */
    public static Pair<List<String>, Map<HandleCountProperty, List<Long>>> queryHandles() {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                HandleCountProperty.class,
                PerfmonConsts.PROCESS,
                PerfmonConsts.WIN32_PERFPROC_PROCESS);
    }

    /**
     * Returns cooked idle process performance counters.
     *
     * @return Cooked performance counters for idle process.
     */
    public static Pair<List<String>, Map<IdleProcessorTimeProperty, List<Long>>> queryIdleProcessCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                IdleProcessorTimeProperty.class,
                PerfmonConsts.PROCESS,
                PerfmonConsts.WIN32_PERFPROC_PROCESS_WHERE_IDPROCESS_0);
    }

    /**
     * Process performance counters
     */
    public enum ProcessPerformanceProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        PRIORITYBASE("Priority Base"), //
        ELAPSEDTIME("Elapsed Time"), //
        IDPROCESS("ID Process"), //
        CREATINGPROCESSID("Creating Process ID"), //
        IOREADBYTESPERSEC("IO Read Bytes/sec"), //
        IOWRITEBYTESPERSEC("IO Write Bytes/sec"), //
        WORKINGSETPRIVATE("Working Set - Private"), //
        WORKINGSET("Working Set"), //
        PAGEFAULTSPERSEC("Page Faults/sec");

        private final String counter;

        ProcessPerformanceProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

    /**
     * Handle performance counters
     */
    public enum HandleCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.TOTAL_INSTANCE),
        // Remaining elements define counters
        HANDLECOUNT("Handle Count");

        private final String counter;

        HandleCountProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

    /**
     * Processor performance counters
     */
    public enum IdleProcessorTimeProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.TOTAL_OR_IDLE_INSTANCES),
        // Remaining elements define counters
        PERCENTPROCESSORTIME("% Processor Time"), //
        ELAPSEDTIME("Elapsed Time");

        private final String counter;

        IdleProcessorTimeProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

}
